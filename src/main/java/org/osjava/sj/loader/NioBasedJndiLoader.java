package org.osjava.sj.loader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 2019-02-09
 */
public class NioBasedJndiLoader extends JndiLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(NioBasedJndiLoader.class);

    public NioBasedJndiLoader(final Hashtable env) {
        super(env);
    }

    /**
     * @param fileOrDirectory Not a jar file. To load jar files use {@link #loadJar(File, String, Context, boolean)}
     * @param preserveRootFileNameAsContextName true: If fileOrDirectory is a file, the file's name is taken as context name. This rule is only applied to files which are root files. Files found while traversing directories will always result in subcontexts named as the file. The only exception are files named "default".
     */
    public void load(File fileOrDirectory, Context ctxt, boolean preserveRootFileNameAsContextName) throws NamingException, IOException {
        // See https://github.com/h-thurow/Simple-JNDI/issues/7
        fileOrDirectory = new File(fileOrDirectory.getAbsolutePath());
        if (fileOrDirectory.isDirectory()) {
            loadDirectory(fileOrDirectory, fileOrDirectory.getPath(), ctxt, null, "");
        }
        else {
            loadFile(fileOrDirectory.toPath(), ctxt, null, preserveRootFileNameAsContextName);
        }
    }

    /**
     *
     * @param rootDir name-separator has to be platform independent always "/"
     * @param preserveFileNameAsContextName Siehe {@link #load(File, Context, boolean)}
     */
    public void loadJar(File jarFile, String rootDir, Context ctxt, boolean preserveFileNameAsContextName) throws IOException {
        Path path = Paths.get(jarFile.toURI());
        try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
            Files.walkFileTree(fs.getPath(rootDir), new MySimpleFileVisitor(rootDir, ctxt, "", preserveFileNameAsContextName));
        }
    }

    /**
     *
     * @param preserveFileNameAsContextName Siehe {@link #load(File, Context, boolean)}. Can be false in case of root files.
     */
    private void loadFile(final Path path, final Context ctxt, Context parentCtxt
            , final boolean preserveFileNameAsContextName) throws IOException, NamingException {
        LOGGER.debug("Loading {}", path);
        String parentName  = path.getFileName().toString();
        parentName = handleColonReplacement(parentName);
        Context subContext = ctxt;
        Properties properties = toProperties(path);
        String subName = null;
        if (isNotNamespacedTypeDefinition(properties)) {
            // preserve the file name as object name.
            subName = FilenameUtils.getBaseName(parentName);
            parentCtxt = subContext;
        }
        else if (!FilenameUtils.getBaseName(parentName).equals("default")) {
            parentName = FilenameUtils.getBaseName(parentName);
            if (preserveFileNameAsContextName) {
                subContext = ctxt.createSubcontext(parentName);
                parentCtxt = ctxt;
            }
            subName = parentName;
        }
        load(properties, subContext, parentCtxt, subName);
    }

    // IMPROVE Diese Methode auch in FileBasedJndiLoader verwenden.
    public Properties toProperties(Path path) throws IOException {
        SJProperties properties;

        if(path.getFileName().toString().endsWith(".xml")) {
            properties = new XmlSJProperties();
        }
        else if(path.getFileName().toString().endsWith(".ini")) {
            properties = new IniSJProperties();
        }
        else {
            properties = new CustomSJProperties();
        }

        properties.setDelimiter( (String) environment.get(DELIMITER) );

        try (InputStream stream = Files.newInputStream(path)){
            properties.load(stream);
            return properties;
        }
    }

    /**
     * Loads all .properties", .ini, .xml files in a directory into a context.
     */
    private void loadDirectory(File directory, final String platformSpecificRootDir, final Context ctxt, final Context parentCtxt, final String subName) throws NamingException, IOException {

        Files.walkFileTree(directory.toPath(), new MySimpleFileVisitor(platformSpecificRootDir, ctxt, "", false));
    }

    class MySimpleFileVisitor extends SimpleFileVisitor<Path> {

        private final String platformSpecificRootDir;
        private final Context ctxt;
        private final String subName;
        private final ArrayList<Context> contexts = new ArrayList<>();
        private final boolean preserveRootFileNameAsContextName;

        MySimpleFileVisitor(String platformSpecificRootDir, final Context ctxt, final String subName, final boolean preserveRootFileNameAsContextName) {
            if (StringUtils.endsWith(platformSpecificRootDir, File.separator)) {
                platformSpecificRootDir = platformSpecificRootDir.substring(0, platformSpecificRootDir.length() - 1);
            }
            this.platformSpecificRootDir = platformSpecificRootDir;
            this.ctxt = ctxt;
            this.subName = subName;
            contexts.add(this.ctxt);
            this.preserveRootFileNameAsContextName = preserveRootFileNameAsContextName;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            LOGGER.debug("preVisitDirectory: {}", dir);
            String dirString = dir.toString(); // dirString is platform specific
            if (StringUtils.endsWith(dirString, File.separator)) {
                dirString = dirString.substring(0, dirString.length() - 1);
            }
            if (!platformSpecificRootDir.equals(dirString)) {
                try {
//                    CompoundName compoundName = toCompoundName(dirString);
//                    String subCtxName = compoundName.get(compoundName.size() - 1);
                    String[] parts = StringUtils.split(dirString, File.separatorChar);
                    String subCtxName = parts[parts.length - 1];
                    if (!subCtxName.equals(".svn") && !subCtxName.equals("CVS")) {
                        subCtxName = handleColonReplacement(subCtxName);
                        contexts.add(contexts.get(contexts.size() - 1).createSubcontext(subCtxName));
                    }
                }
                catch (NamingException e) {
                    LOGGER.error("", e);
                }
            }
            return super.preVisitDirectory(dir, attrs);
        }

        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
            try {
                if (path.toString().equals(platformSpecificRootDir)) {
                    loadFile(path, contexts.get(contexts.size() - 1), null, preserveRootFileNameAsContextName);
                }
                else {
                    loadFile(path, contexts.get(contexts.size() - 1), null, true);
                }
            }
            catch (NamingException e) {
                LOGGER.error("path: {} ctxt: {} subName: {}", path, ctxt, subName);
                LOGGER.error("", e);
            }
            return super.visitFile(path, attrs);
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            LOGGER.debug("visitFileFailed: {}", file);
            return super.visitFileFailed(file, exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            LOGGER.debug("postVisitDirectory: {}", dir);
            if (!platformSpecificRootDir.equals(dir.toString())) {
                contexts.remove(contexts.size() - 1);
            }
            return super.postVisitDirectory(dir, exc);
        }
    }
}

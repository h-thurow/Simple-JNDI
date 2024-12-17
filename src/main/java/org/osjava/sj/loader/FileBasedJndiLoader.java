package org.osjava.sj.loader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 2019-02-09
 */
public class FileBasedJndiLoader extends JndiLoader {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public FileBasedJndiLoader(final Hashtable env) {
        super(env);
    }

    /**
     * Loads all .properties", .ini, .xml files in a directory or a single file into a context.
     */
    public void load(File fileOrDirectory, Context ctxt) throws NamingException, IOException {
        fileOrDirectory = new File(fileOrDirectory.getAbsolutePath());
        if (fileOrDirectory.isDirectory()) {
            loadDirectory(fileOrDirectory, ctxt, null, "");
        }
        else if (fileOrDirectory.isFile()) {
            boolean preserveFileNameAsContextName = BooleanUtils.toBoolean(
                    (String) environment.get(FILENAME_TO_CONTEXT));
            if (isSupportedFile(fileOrDirectory)) {
                loadFile(fileOrDirectory, ctxt, null, "", preserveFileNameAsContextName);
            }
        }
        else {
            LOGGER.warn("Not found: {}", fileOrDirectory.getAbsolutePath());
        }
    }

    /**
     *  @param preserveFileNameAsContextName If fileOrDirectory is a file, the file's name is taken as context name.
     *
     */
    public void load(File fileOrDirectory, Context ctxt, boolean preserveFileNameAsContextName) throws NamingException, IOException {
        // See https://github.com/h-thurow/Simple-JNDI/issues/7
        fileOrDirectory = new File(fileOrDirectory.getAbsolutePath());
        if (fileOrDirectory.isDirectory()) {
            loadDirectory(fileOrDirectory, ctxt, null, "");
        }
        else if (fileOrDirectory.isFile()) {
            loadFile(fileOrDirectory, ctxt, null, "", preserveFileNameAsContextName);
        }
        else {
            LOGGER.warn("Not found: {}", fileOrDirectory.getAbsolutePath());
        }
    }

    /**
     * Loads all .properties", .ini, .xml files in a directory into a context.
     */
    private void loadDirectory(File directory, Context ctxt, Context parentCtxt, String subName) throws NamingException, IOException {
        LOGGER.debug("Loading {}", directory.getAbsolutePath());
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String dirName = file.getName();
                    if (!dirName.equals(".svn") && !dirName.equals("CVS")) {
                        dirName = handleColonReplacement(dirName);
                        Context tmpCtxt = ctxt.createSubcontext(dirName);
                        loadDirectory(file, tmpCtxt, ctxt, dirName);
                    }
                }
                else {
                    String baseName = FilenameUtils.getBaseName(file.getName());
                    boolean preserveFileNameAsContextName = !baseName.equals("default");
                    if (isSupportedFile(file)) {
                        loadFile(file, ctxt, parentCtxt, subName, preserveFileNameAsContextName);
                    }
                }
            }
        }
    }

    /**
     * Loads any file, not only those files {@link #isSupportedFile(File)} returns true for.
     *
     * @param file Not a directory
     */
    private void loadFile(File file, Context ctxt, Context parentCtxt, String subName, boolean preserveFileNameAsContextName) throws NamingException, IOException {
        LOGGER.debug("Loading {}", file.getAbsolutePath());
        String parentName = file.getName();
        parentName = handleColonReplacement(parentName);
        Context subContext = ctxt;
        Properties properties = toProperties(file);
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

    /**
     *
     * @return xml file: {@link XmlSJProperties}. ini file: {@link IniSJProperties}. Sonst {@link CustomSJProperties}.
     */
    // IMPROVE Make package-private
    public Properties toProperties(File file) throws IOException {
//        System.err.println("LOADING: "+file);
        SJProperties properties;

        if(file.getName().endsWith(".xml")) {
            properties = new XmlSJProperties();
        }
        else if(file.getName().endsWith(".ini")) {
            properties = new IniSJProperties();
        }
        else {
            properties = new CustomSJProperties();
        }

        properties.setDelimiter( (String) environment.get(DELIMITER) );

        try (FileInputStream stream = new FileInputStream(file)) {
            properties.load(stream);
            return properties;
        }
    }
}

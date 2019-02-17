package org.osjava.sj.loader;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 2019-02-08
 */
public class LoadFromJarTest {


    /**
     * Read ZipEntries
     */
    @Test @Ignore
    public void jaredRoot() throws IOException {
        File jarFile = new File("src/test/simple-jndi-0.17.3-tests.jar");
        JarFile jar = new JarFile(jarFile);
        Enumeration<? extends JarEntry> enumeration = jar.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            System.out.println(zipEntry.getName());
        }

//        ZipEntry entry = jar.getEntry("META-INF/maven/");
//        entry.isDirectory();

    }

    /**
     * Read with NIO.
     */
    @Test @Ignore
    public void jaredRoot1() throws IOException {
        File jarFile = new File("src/test/simple-jndi-0.17.3-tests.jar");

        Path path = Paths.get(jarFile.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath("/roots"));
        for(Path p: directoryStream){
            System.out.println(p);
        }

    }

    /**
     * NIO and ZipEntries combined.
     */
    @Test @Ignore
    public void jaredRoot2() throws IOException {
        File file = new File("src/test/simple-jndi-0.17.3-tests.jar");
        JarFile jar = new JarFile(file);
        Path path = Paths.get(file.toURI());
        try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
//            String rootDir = "/root";
            String rootDir = "/roots";
            // Liest nicht auch die sub dirs
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath(rootDir));

            for(Path p: directoryStream){
                System.out.println(p);
                // java.lang.UnsupportedOperationException
//                if (p.toFile().isDirectory()) {
//                    System.out.println("is directory");
//                }
//                else if (p.toFile().isFile()) {
//                    System.out.println("is file");
//                }

                if (jar.getEntry(p.toString().substring(1)).isDirectory()) {
                    System.out.println("is directory");
                }
                else {
                    System.out.println("is file");
                    InputStream inputStream = Files.newInputStream(p);
                    String content = IOUtils.toString(inputStream, "utf8");
                    System.out.println(content.length());
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Traverse with SimpleFileVisitor.
     */
    @Test @Ignore
    public void jaredRoot3() throws IOException {
        File file = new File("src/test/simple-jndi-0.17.3-tests.jar");
        Path path = Paths.get(file.toURI());
        try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
            String rootDir = "/roots/untypedProperty";
//            String rootDir = "/";

/*
preVisitDirectory: /
preVisitDirectory: /root/
visitFile: /root/test.properties
postVisitDirectory: /root/
preVisitDirectory: /org/
preVisitDirectory: /org/osjava/
visitFile: /org/osjava/StringUtils.class
visitFile: /org/osjava/StringsToTypes.class
preVisitDirectory: /org/osjava/sj/
visitFile: /org/osjava/sj/SimpleJndiContextFactory.class
visitFile: /org/osjava/sj/SimpleJndiContextFactory$1.class
visitFile: /org/osjava/sj/SimpleJndi.class
visitFile: /org/osjava/sj/SimpleContextFactory.class
visitFile: /org/osjava/sj/MemoryContextFactory.class
visitFile: /org/osjava/sj/MemoryContextFactory$1.class
preVisitDirectory: /org/osjava/sj/loader/
visitFile: /org/osjava/sj/loader/XmlSJProperties.class
visitFile: /org/osjava/sj/loader/SJProperties.class
visitFile: /org/osjava/sj/loader/OrderedSet.class
visitFile: /org/osjava/sj/loader/JndiLoader.class
visitFile: /org/osjava/sj/loader/IniSJProperties.class
visitFile: /org/osjava/sj/loader/CustomSJProperties.class
preVisitDirectory: /org/osjava/sj/loader/convert/
visitFile: /org/osjava/sj/loader/convert/SJDataSourceConverter.class
visitFile: /org/osjava/sj/loader/convert/MapConverter.class
visitFile: /org/osjava/sj/loader/convert/DateConverter.class
visitFile: /org/osjava/sj/loader/convert/ConverterRegistry.class
visitFile: /org/osjava/sj/loader/convert/ConverterIF.class
visitFile: /org/osjava/sj/loader/convert/ConstructorConverter.class
visitFile: /org/osjava/sj/loader/convert/CharacterConverter.class
visitFile: /org/osjava/sj/loader/convert/BeanConverter.class
postVisitDirectory: /org/osjava/sj/loader/convert/
postVisitDirectory: /org/osjava/sj/loader/
preVisitDirectory: /org/osjava/sj/jndi/
visitFile: /org/osjava/sj/jndi/SimpleNameParser.class
visitFile: /org/osjava/sj/jndi/MemoryContext.class
visitFile: /org/osjava/sj/jndi/JndiUtils.class
visitFile: /org/osjava/sj/jndi/DelimiterConvertingContext.class
visitFile: /org/osjava/sj/jndi/ContextNames.class
visitFile: /org/osjava/sj/jndi/ContextBindings.class
postVisitDirectory: /org/osjava/sj/jndi/
postVisitDirectory: /org/osjava/sj/
preVisitDirectory: /org/osjava/datasource/
visitFile: /org/osjava/datasource/SJDataSource.class
visitFile: /org/osjava/datasource/PoolSetup.class
postVisitDirectory: /org/osjava/datasource/
postVisitDirectory: /org/osjava/
postVisitDirectory: /org/
preVisitDirectory: /META-INF/
preVisitDirectory: /META-INF/maven/
preVisitDirectory: /META-INF/maven/com.github.h-thurow/
preVisitDirectory: /META-INF/maven/com.github.h-thurow/simple-jndi/
visitFile: /META-INF/maven/com.github.h-thurow/simple-jndi/pom.properties
visitFile: /META-INF/maven/com.github.h-thurow/simple-jndi/pom.xml
postVisitDirectory: /META-INF/maven/com.github.h-thurow/simple-jndi/
postVisitDirectory: /META-INF/maven/com.github.h-thurow/
postVisitDirectory: /META-INF/maven/
visitFile: /META-INF/MANIFEST.MF
postVisitDirectory: /META-INF/
postVisitDirectory: /
 */
            Files.walkFileTree(fs.getPath(rootDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    System.out.println("preVisitDirectory: " + dir);
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    System.out.println("visitFile: " + file);

                    try (InputStream inputStream = Files.newInputStream(file)){
                        String content = IOUtils.toString(inputStream, "utf8");
                        System.out.println(content.length());
                    }

                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                    System.out.println("visitFileFailed: " + file);
                    return super.visitFileFailed(file, exc);
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    System.out.println("postVisitDirectory: " + dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find path to jar in file system
     */
    @Test @Ignore
    public void jaredRoot4() throws IOException, ClassNotFoundException {

        // Das JAR könnte gekennzeichnet werden durch eine Klasse, die es enthält. Vorteil ist, dass der Name des JAR nicht festgelegt werden muss, weil der sich, wenn er die Version enthält, mit jedem release ändern würde.

//        String path = StringUtils.class.getClassLoader().getResource("org/apache/commons/lang/StringUtils.class").getPath();
        // file:/Users/hot/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar!/org/apache/commons/lang/StringUtils.class
//        System.out.println(path);

        Class<?> clazz = Class.forName("org.apache.commons.lang.StringUtils");
        URL pathToJar = clazz.getProtectionDomain().getCodeSource().getLocation();
        // file:/Users/hot/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar
        System.out.println(pathToJar);

        // Oder der Name des JAR wird konfiguriert und der Pfad zu ihm im file system wird aus dem classpath extrahiert:

//        System.out.println(System.getProperty("java.class.path"));;
    }

    @Test
    public void rootInJarDefinition() {
        String[] parts = "jarMarkerClass=any.class.in.Jar,root=/path/to/root/in/jar".split("[=,]");
        System.out.println(parts[1]);
        System.out.println(parts[3]);
    }

    @Test
    public void loadFile() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        // Werte aus jndi.properties überschreiben
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        NioBasedJndiLoader loader = new NioBasedJndiLoader(env);
        File file = new File("src/test/resources/roots/test.properties");
        InitialContext initialContext = new InitialContext(env);
        loader.load(file, initialContext, true);
    }

    @Test
    public void loadJar() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        // Werte aus jndi.properties überschreiben
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        NioBasedJndiLoader loader = new NioBasedJndiLoader(env);
        File file = new File("src/test/simple-jndi-0.17.3-tests.jar");
        InitialContext initialContext = new InitialContext(env);
        loader.loadJar(file, "/roots/untypedProperty", initialContext, true);
        Context testCtx = (Context) initialContext.lookup("file1");
        assertNotNull(testCtx);
        String value = (String) testCtx.lookup("name");
        assertEquals("holger", value);
    }

    @Test
    public void loadJarWithSubcontexts() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        // Werte aus jndi.properties überschreiben
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        NioBasedJndiLoader loader = new NioBasedJndiLoader(env);
        File file = new File("src/test/simple-jndi-0.17.3-tests.jar");
        InitialContext ic = new InitialContext(env);
        loader.loadJar(file, "/roots/system-test", ic, true);
//        Context testCtx = (Context) ic.lookup("one");
//        assertNotNull(testCtx);
        String value = (String) ic.lookup("one/two/three/four");
        assertEquals("1234", value);
    }

    @Test
    public void loadJarWithSubcontexts2() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        // Werte aus jndi.properties überschreiben
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        NioBasedJndiLoader loader = new NioBasedJndiLoader(env);
        File file = new File("src/test/simple-jndi-0.17.3-tests.jar");
        InitialContext ic = new InitialContext(env);
        loader.loadJar(file, "/roots/sameNamesInDifferentBranches", ic, true);
        String value = (String) ic.lookup("context1/persons/holger/branch");
        assertEquals("context1", value);
        value = (String) ic.lookup("context2/persons/holger/branch");
        assertEquals("context2", value);
    }

    /**
     * root dir can end or not end with slash
     */
    @Test
    public void loadJarRootDirWithTrailingSlash() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        // Werte aus jndi.properties überschreiben
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        NioBasedJndiLoader loader = new NioBasedJndiLoader(env);
        File file = new File("src/test/simple-jndi-0.17.3-tests.jar");
        InitialContext ic = new InitialContext(env);
        loader.loadJar(file, "/roots/sameNamesInDifferentBranches/", ic, true);
        String value = (String) ic.lookup("context1/persons/holger/branch");
        assertEquals("context1", value);
        value = (String) ic.lookup("context2/persons/holger/branch");
        assertEquals("context2", value);
    }

    @Test
    public void loadDirectory() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        // Werte aus jndi.properties überschreiben
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        NioBasedJndiLoader loader = new NioBasedJndiLoader(env);
        File file = new File("src/test/resources/roots/untypedProperty");
        InitialContext ic = new InitialContext(env);
        loader.load(file, ic, true);
        Context testCtx = (Context) ic.lookup("file1");
        assertNotNull(testCtx);
        String value = (String) testCtx.lookup("name");
        assertEquals("holger", value);
    }

}
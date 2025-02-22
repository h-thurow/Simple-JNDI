package org.osjava.sj;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Simple test that asserts the correctness of OSGi metadata by starting an OSGi container, deploying Simple-JNDI with minimum dependencies (Common Lang, Common
 * IO) and performing a JNDI lookup from within the OSGi runtime.
 * 
 * @author gromanato
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiSuite {

    @Configuration
    public Option[] configuration() throws IOException {
        return new Option[] { CoreOptions.junitBundles(), CoreOptions.mavenBundle().groupId("commons-io").artifactId("commons-io").version("2.7"),
                CoreOptions.mavenBundle().groupId("org.apache.commons").artifactId("commons-lang3").version("3.17.0"), CoreOptions.url("file:target/classes/"), };
    }

    @Test
    public void testOSGiLookup() throws IOException, NamingException {
        
        Bundle bundle = FrameworkUtil.getBundle(SimpleJndiContextFactory.class);
        assertNotNull("bundle should be not null", bundle);
        assertEquals("bundle should be active", bundle.getState(), Bundle.ACTIVE);
        
        SimpleJndiContextFactory factory = new SimpleJndiContextFactory();
        File tempFolder = createJNDIConfiguration();
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(SimpleJndi.ROOT, tempFolder.getAbsolutePath());
        Context context = factory.getInitialContext(env);
        String admin = (String) context.lookup("application1.users.admin");
        Integer quantity = (Integer) context.lookup("application1.users.quantity");
        Boolean enabled = (Boolean) context.lookup("application1.users.enabled");

        FileUtils.forceDelete(tempFolder);

        assertEquals("should be fred as a string", "fred", admin);
        assertEquals("should be 5 as an integer", new Integer(5), quantity);
        assertEquals("should be true as a boolean", Boolean.TRUE, enabled);

    }

    protected File createJNDIConfiguration() throws IOException, FileNotFoundException {
        Path tempDir = Files.createTempDirectory("simpleJNDI");
        File tempDirFile = tempDir.toFile();
        tempDirFile.mkdirs();

        File applicationFolder = new File(tempDirFile, "application1");
        applicationFolder.mkdir();

        Properties properties = new Properties();
        properties.put("admin", "fred");
        properties.put("quantity", "5");
        properties.put("quantity.type", "java.lang.Integer");
        properties.put("enabled", "true");
        properties.put("enabled.type", "java.lang.Boolean");
        File propertiesFile = new File((applicationFolder), "users.properties");
        properties.store(new FileOutputStream(propertiesFile), null);

        propertiesFile.deleteOnExit();
        applicationFolder.deleteOnExit();
        tempDirFile.deleteOnExit();

        return tempDirFile;
    }

}

package commons.configuration;

import org.apache.commons.configuration.JNDIConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.jndi.MemoryContext;
import org.osjava.sj.loader.JndiLoader;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.junit.Assert.*;

public class JNDIConfigurationTest {

    @Before
    public void setUp() throws Exception {
        System.clearProperty(SimpleJndi.ROOT);
        System.clearProperty(SimpleJndi.SHARED);
        System.clearProperty(SimpleJndi.ENC);
        System.clearProperty(SimpleJndi.JNDI_SYNTAX_SEPARATOR);
        System.clearProperty(SimpleJndi.FILENAME_TO_CONTEXT);
        System.clearProperty(JndiLoader.SIMPLE_COLON_REPLACE);
        System.clearProperty(JndiLoader.SIMPLE_DELIMITER);
    }

    @Test
    public void slashDelimitedPropertiesUnshared() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/slashDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            assertEquals(true, jndiConf.getBoolean("java:comp/env/boolean_true"));
            int age = jndiConf.getInt("java:comp/env/parent/child1/size");
            assertEquals(186, age);
            assertEquals(MemoryContext.class, jndiConf.getProperty("java:comp/env/parent").getClass());

            assertNotNull(ctx.lookup("java:comp"));
            assertNotNull(ctx.lookup("java:comp/env"));
            assertNotNull(ctx.lookup("java:comp/env/parent"));
            assertNotNull(ctx.lookup("java:comp/env/parent/child1/name"));
            assertNotNull(ctx.lookup("java:comp/env/parent/child1/city"));
            assertNotNull(ctx.lookup("java:comp/env/parent/mother"));
            assertNotNull(ctx.lookup("java:comp/env/parent/mother/name"));
            age = (Integer) ctx.lookup("java:comp/env/parent/child1/size");
            assertEquals(186, age);

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * The org.osjava.sj.space property is not subject to delimiter parsing, so even when org.osjava.sj.delimiter is set to ".", you have to lookup "java:comp/env", not "java:comp.env"
     */
    @Test(expected = NamingException.class)
    public void dotDelimitedPropertiesCombinedWithSlashDelimitedENC() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/dotDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "."); // to be explicit
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx = new InitialContext(env);

            int size = (Integer) ctx.lookup("java:comp/env.parent.child1.size");
            assert size == 186;

            // NamingException: Invalid subcontext 'java:comp' in context ''
            ctx.lookup("java:comp.env.parent.child1.size");
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Property names must not contain ".", because JNDIConfiguration replaces them with "/" when calling Simple-JNDI.
     */
    @Test(expected = NoSuchElementException.class)
    public void dotDelimitedProperties() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/dotDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "."); // to be explicit
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx = new InitialContext(env);

            // Simple-JNDI 0.13.0:
            // Jan 08, 2017 10:30:22 AM org.apache.commons.configuration.JNDIConfiguration configurationError
//            WARNING: Internal error
//            javax.naming.NamingException: Invalid subcontext 'my' in context 'java:comp/env'
            // JNDIConfiguration interpretiert in JNDIConfiguration.getProperty points als context divider: key = key.replaceAll("\\.", "/");
            // Siehe keysWithPointDelimiterSetToPoint()
//            assertNull(jndiConf.getString("java:comp/env.my.home"));

            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);
            // NoSuchElementException
            jndiConf.getInt("java:comp/env.parent.child1.size");
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Delimiters can not be mixed up in property files with jndi.syntax.separator set. They can only be mixed up in lookups.
     */
    @Test
    public void mixedDelimitedPropertiesWithJndiSyntaxSeparator() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/mixedDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
//            env.put("org.osjava.sj.delimiter", ".");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);

            Context dotCtx = (Context) ctx.lookup("dot");
            assertEquals(MemoryContext.class, dotCtx.getClass());
            assertEquals("dot separated", ctx.lookup("dot/separated"));
            assertEquals("dot separated", ctx.lookup("dot.separated"));

            try {
                // ERROR org.osjava.sj.memory.MemoryContext - AbstractContext#lookup("slash/separated"): Invalid subcontext 'slash' in context '': AbstractContext{table={slash/separated=slash separated}, subContexts={dot=AbstractContext{table={separated=dot separated}
                ctx.lookup("slash");
                throw new AssertionError("We should not have arrived here!");
            }
            catch (NamingException e) {
                LoggerFactory.getLogger(this.getClass()).error("Expected Exception!!!", e);
            }

            try {
                // ERROR org.osjava.sj.memory.MemoryContext - AbstractContext#lookup("slash/separated"): Invalid subcontext 'slash' in context '': AbstractContext{table={slash/separated=slash separated}, subContexts={dot=AbstractContext{table={separated=dot separated}
                ctx.lookup("slash/separated");
                throw new AssertionError("We should not have arrived here!");
            }
            catch (NamingException e) {
                LoggerFactory.getLogger(this.getClass()).error("Expected Exception!!!", e);
            }
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * To make "." within property names work with JNDIConfiguration set jndi.syntax.separator = "/".
     */
    @Test
    public void dotDelimitedSharedAndVariableInterpolation() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/dotDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "."); // to be explicit
//            env.put("org.osjava.sj.delimiter", "\\.|/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put(SimpleJndi.SHARED, "true");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            final int size = (Integer) ctx.lookup("java:comp/env.parent.child1.size");
            assert size == 186;
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            assertEquals("/Users/hot", jndiConf.getString("java:comp/env.my.home"));
            assertEquals("/Users/hot", jndiConf.getString("java:comp.env.my.home"));
            assertEquals("/Users/hot", jndiConf.getString("java:comp/env/my/home"));
            assertEquals("${sys:user.home}", ctx.lookup("java:comp.env.my.home"));
            assertEquals("${sys:user.home}", ctx.lookup("java:comp/env/my/home"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void dotDelimitedUnsharedAndVariableInterpolation() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/dotDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "."); // to be explicit
//            env.put("org.osjava.sj.delimiter", "\\.|/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);

            final int size = (Integer) ctx.lookup("java:comp/env.parent.child1.size");
            assert size == 186;

            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            // variable interpolation (${sys:user.home})

            assertEquals("/Users/hot", jndiConf.getString("java:comp/env.my.home"));
            assertEquals("/Users/hot", jndiConf.getString("java:comp.env.my.home"));
            assertEquals("/Users/hot", jndiConf.getString("java:comp/env/my/home"));
            assertEquals("${sys:user.home}", ctx.lookup("java:comp.env.my.home"));
            assertEquals("${sys:user.home}", ctx.lookup("java:comp/env/my/home"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Chaining does not work with JNDIConfiguration.
     */
    @Test
    public void chained() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/chaining/my.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
//            env.put("org.osjava.sj.delimiter", "."); // to be explicit
//            env.put("org.osjava.sj.delimiter", "\\.|/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("jndi.syntax.separator", "/");

            ctx = new InitialContext(env);

            assertEquals("next.properties", ctx.lookup("java:comp/env/include"));

            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);
//            jndiConf.setThrowExceptionOnMissing(true);
            String includedString = jndiConf.getString("java:comp/env/string_in_chained_property_file");
            assertNull(includedString);
            // javax.naming.NamingException: Invalid subcontext 'include' in context 'java:comp/env'
            includedString = jndiConf.getString("java:comp/env/include/string_in_chained_property_file");
            assertNull(includedString);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * When setting ENC, variables must be prefixed with ENC too, e.g. ${java:comp/env.application.name}.
     */
    @Test
    public void variableInterpolationWithEnc() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/interpolation.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);
            assertEquals("${java:comp/env.application.name} ${java:comp/env.application.version}", ctx.lookup("java:comp/env/application/title"));
            assertEquals("Killer App 1.6.2", jndiConf.getString("java:comp/env/application/title"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void variableInterpolationWithOutEnc() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/interpolationNoEnc.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);
            assertEquals("${application.name} ${application.version}", ctx.lookup("application/title"));
            assertEquals("Killer App 1.6.2", jndiConf.getString("application/title"));

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * To avoid the need to prefix variables with an ENC you could use JNDIConfiguration's two argument constructor. It works but you must remove "java:comp/env" from the path argument, when calling one of JNDIConfiguration's getter methods.
     */
    @Test
    public void variableInterpolationWithEnc2() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/interpolationNoEnc.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("jndi.syntax.separator", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx = new InitialContext(env);
            assertEquals("${application.name} ${application.version}", ctx.lookup("java:comp/env/application/title"));

            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx, "java:comp/env");
            assertEquals("Killer App 1.6.2", jndiConf.getString("application/title"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * To be able to use "java:comp/env" in lookups.
     */
    @Test
    public void variableInterpolationWithEnc3() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.ROOT, "src/test/resources/roots/commons/configuration/interpolationNoEnc.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("jndi.syntax.separator", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx = new InitialContext(env);
            assertEquals("${application.name} ${application.version}", ctx.lookup("java:comp/env/application/title"));
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx) {
                @Override
                protected Object interpolate(Object value) {
                    if (value instanceof String) {
                        value = ((String) value).replace("${", "${java:comp/env/");
                    }
                    return super.interpolate(value);
                }
            };
            assertEquals("Killer App 1.6.2", jndiConf.getString("java:comp/env/application/title"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }
}
package commons.configuration;

import org.apache.commons.configuration.JNDIConfiguration;
import org.junit.Test;
import org.osjava.sj.SimpleJndi;

import javax.naming.InitialContext;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JNDIConfigurationTest {
    @Test
    public void slashDelimitedUnshared() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.SIMPLE_ROOT, "src/test/commons/configuration/slashDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            assertEquals(true, jndiConf.getBoolean("java:comp/env/boolean_true"));
            int age = jndiConf.getInt("java:comp/env/parent/child1/size");
            assertEquals(186, age);

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
     * property names must not contain "." when Simple-JNDI was configured with "/" as delimiter, because JNDIConfiguration replaces them with "/" when calling Simple-JNDI.
     * <p>
     * java.lang.RuntimeException: Illegal node/branch clash. At branch value 'size' an Object was found: 186: Ursache liegt in:
     * org.osjava.sj.loader.JndiLoader#load(java.util.Properties, javax.naming.Context, javax.naming.Context, java.lang.String)<br>
     *      String typePostfix = delimiter + "type";<br>
     * Muss geändert werden.
     * <p>
     * Auch verkehrt: "java:comp\.|/env"
     */
    @Test
    public void dotDelimitedShared() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.SIMPLE_ROOT, "src/test/commons/configuration/dotDelimiter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "."); // to be explicit
//            env.put("org.osjava.sj.delimiter", "\\.|/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put(SimpleJndi.SIMPLE_SHARED, "true");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            final int size = (Integer) ctx.lookup("java:comp/env.parent.child1.size");
            assert size == 186;
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            // Simple-JNDI 0.13.0:
            // Jan 08, 2017 10:30:22 AM org.apache.commons.configuration.JNDIConfiguration configurationError
//            WARNING: Internal error
//            javax.naming.NamingException: Invalid subcontext 'my' in context 'java:comp/env'
            // JNDIConfiguration interpretiert in JNDIConfiguration.getProperty points als context divider: key = key.replaceAll("\\.", "/");
            // Siehe keysWithPointDelimiterSetToPoint()
//            assertNull(jndiConf.getString("java:comp/env.my.home"));
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
    public void dotDelimitedUnshared() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.SIMPLE_ROOT, "src/test/commons/configuration/dotDelimiter");
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
            env.put(SimpleJndi.SIMPLE_ROOT, "src/test/commons/configuration/chaining/my.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
//            env.put("org.osjava.sj.delimiter", "."); // to be explicit
//            env.put("org.osjava.sj.delimiter", "\\.|/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("jndi.syntax.separator", "/");

            ctx = new InitialContext(env);

            assertEquals("next.properties", ctx.lookup("java:comp/env/include"));

            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            // javax.naming.NamingException: Invalid subcontext 'include' in context 'java:comp/env'
            assertEquals(null, jndiConf.getString("java:comp/env/include/string_in_chained_property_file"));

            assertEquals(null, jndiConf.getString("java:comp/env/string_in_chained_property_file"));

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * When setting ENC variables must be prefixed with ENC too, e.g. ${java:comp/env.application.name}.
     */
    @Test
    public void variableInterpolationWithEnc() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.SIMPLE_ROOT, "src/test/commons/configuration/interpolation.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);
            assertEquals("${java:comp/env.application.name} ${java:comp/env.application.version}", (String) ctx.lookup("java:comp/env/application/title"));
            assertEquals("Killer App 1.6.2", jndiConf.getString("java:comp/env/application/title"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void variableInterpolationNoEnc() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.SIMPLE_ROOT, "src/test/commons/configuration/interpolationNoEnc.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);
            assertEquals("${application.name} ${application.version}", (String) ctx.lookup("application/title"));
            assertEquals("Killer App 1.6.2", jndiConf.getString("application/title"));

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * To avoid the need to prefix variables with an ENC you could use JNDIConfiguration's two argument constructor. It works but the lookup pathes differ from the ones required in an application container. TODO Write JNDIConfiguration Adapter which removes ENC from all lookups (EncEnabledJNDIConfiguration). Or see Customizing interpolation http://commons.apache.org/proper/commons-configuration/userguide/howto_basicfeatures.html#Basic_features_and_AbstractConfiguration.
     */
    @Test
    public void variableInterpolationNoEnc2() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            env.put(SimpleJndi.SIMPLE_ROOT, "src/test/commons/configuration/interpolationNoEnc.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("jndi.syntax.separator", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx = new InitialContext(env);
            assertEquals("${application.name} ${application.version}", (String) ctx.lookup("java:comp/env/application/title"));
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx, "java:comp/env");
            assertEquals("Killer App 1.6.2", jndiConf.getString("application/title"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }
}
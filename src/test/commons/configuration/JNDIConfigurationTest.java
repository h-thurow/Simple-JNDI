package commons.configuration;

import org.apache.commons.configuration.JNDIConfiguration;
import org.junit.Test;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.loader.JndiLoader;

import javax.naming.InitialContext;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JNDIConfigurationTest {
    @Test
    public void slashDelimited() throws Exception {
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
    public void dotDelimited() throws Exception {
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
            assertEquals("${sys:user.home}", ctx.lookup("java:comp.env.my.home"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Simple-JNDI configured with "." as delimiter does not solve the problem encountered in {@link #dotDelimited()}.
     */
    @Test
    public void keysWithPointDelimiterSetToPoint() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            // Wird gemerged mit der config aus src/jndi.properties
            env.put(SimpleJndi.SIMPLE_ROOT, "src/configuration/simpleJndiRootPointDelimited");
            env.put(JndiLoader.SIMPLE_DELIMITER, ".");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            assertNotNull(ctx.lookup("java:comp/env.parent.child1.name"));

            assertEquals("${sys:user.home}", ctx.lookup("java:comp/env.my.home"));
            // ... aber diese Abfrage funktioniert trotzdem nicht, weil daraus in JNDIConfiguration java:comp/env/my/home wird, Simple-JNDI aber mit "." als delimiter konfiguriert wurde, also "java:comp/env.my.home" erwartet, um die einzelnen contexts zu extrahieren.
            assert null == jndiConf.getString("java:comp/env.my.home");

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void variableInterpolation() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            // Wird gemerged mit der config aus src/jndi.properties
            env.put(SimpleJndi.SIMPLE_ROOT, "src/configuration/simpleJndiRoot");
//            env.put(JndiLoader.SIMPLE_SHARED, "true");
            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx);

            // variable interpolation
            assertEquals("/Users/hot", jndiConf.getString("java:comp/env/my/home"));
            // Interpolated values are visible only through JNDIConfiguration, but not per lookup on the raw context. Die Interpolation wird erst in getString() vorgenommen auf dem von lookup gelieferten Wert.
            assertEquals("${sys:user.name}", ctx.lookup("java:comp/env/parent/interpolated"));
            assertEquals("hot", jndiConf.getString("java:comp/env/parent/interpolated"));

            assertEquals("hot", jndiConf.getString("java:comp/env/parent/interpolatedName"));

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Variable expansion does not work when using JNDIConfiguration's two argument constructor.
     */
    @Test
    public void variableInterpolationPrefixedContext() throws Exception {
        InitialContext ctx = null;
        try {
            final Properties env = new Properties();
            // Wird gemerged mit der config aus src/jndi.properties
            env.put(SimpleJndi.SIMPLE_ROOT, "src/configuration/simpleJndiRoot");
//            env.put(JndiLoader.SIMPLE_SHARED, "true");
            ctx = new InitialContext(env);
            final JNDIConfiguration jndiConf = new JNDIConfiguration(ctx, "java:comp/env");

            // variable interpolation
            assertEquals("/Users/hot", jndiConf.getString("my/home"));
            // Interpolated values are visible only through JNDIConfiguration, but not per lookup on the raw context. This is different from Simple-JNDI configured to use "." as delimiter. See keysWithPointDelimiterSetToPoint(). Die Interpolation wird erst in getString() vorgenommen auf dem von lookup gelieferten Wert.
            assertEquals("${sys:user.name}", ctx.lookup("java:comp/env/parent/interpolated"));
            assertEquals("hot", jndiConf.getString("parent/interpolated"));

            // Nicht interpoliert, da die JNDIConfiguration nur auf den Subkontexten von "java:comp/env" arbeitet.
            // Es wird ein Fehler gelogt:
//            Jan 08, 2017 1:55:21 PM org.apache.commons.configuration.JNDIConfiguration configurationError
//            WARNING: Internal error
//            javax.naming.NamingException: Invalid subcontext 'java:comp' in context 'java:comp/env'
            assertEquals("${java:comp/env/parent/interpolated}", jndiConf.getString("parent/interpolatedName"));
            assertEquals("hot", jndiConf.getString("parent/interpolatedNameUnprefixed"));

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }
}
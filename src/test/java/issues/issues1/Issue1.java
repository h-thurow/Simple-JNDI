package issues.issues1;

import org.junit.Test;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Hashtable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * See <a href=https://github.com/h-thurow/Simple-JNDI/issues/1>ENC problem</a>.
 */
public class Issue1 {

    /**
     * InitialContext ic = new InitialContext();
     ic.lookup("java:jboss/datasources/my_ds");
     But I get the error Invalid subcontext 'java:jboss' in context ''

     Content of the src/test/resources/resources/jndi.properties file:

     java.naming.factory.initial=org.osjava.sj.SimpleContextFactory
     org.osjava.sj.root=src/test/resources/resources/jndi
     org.osjava.sj.delimiter=/
     org.osjava.sj.space=java
     org.osjava.sj.jndi.shared=true
     Content of the src/test/resources/resources/jndi/jboss/datasources.properties file:

     my_ds.type=javax.sql.DataSource
     my_ds.driver=com.mysql.jdbc.Driver
     my_ds.url=jdbc:mysql://localhost:3306/my_ds
     my_ds.user=xxx
     my_ds.password=xxx
     */
    @Test
    public void keyWithoutNamespace() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/issues/issue1");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:jboss");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx = new InitialContext(env);
            final DataSource ds = (DataSource) ctx.lookup("java:jboss/datasources/mysql");
            assertNotNull(ds);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void keyWithNamespace() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/issues/issue1");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:jboss");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx = new InitialContext(env);
            DataSource ds = (DataSource) ctx.lookup("java:jboss/ds/my_ds");
            assertNull(ds);
            ds = (DataSource) ctx.lookup("java:jboss/ds/mysql");
            assertNotNull(ds);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }
}
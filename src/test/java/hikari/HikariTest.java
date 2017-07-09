package hikari;

import com.zaxxer.hikari.HikariJNDIFactory;
import org.junit.Test;
import spi.objectfactories.DemoBean;
import spi.objectfactories.DemoBeanFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 08.07.17
 */
public class HikariTest {

    @Test
    public void hikariCP() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/java/hikari/roots/HikariCP");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("jndi.syntax.separator", "/");
            env.put(Context.OBJECT_FACTORIES, HikariJNDIFactory.class.getName());
            ctx = new InitialContext(env);

            DataSource ds = (DataSource) ctx.lookup("HikariDataSource");
            Connection con = ds.getConnection();
            Statement stmnt = con.createStatement();
            //createTable(statement);
            stmnt.executeQuery("INSERT INTO DATATYPES_TEST (a_varchar) values ('test')");
            ResultSet rs = stmnt.executeQuery("SELECT count(*) FROM DATATYPES_TEST");
            rs.next();
            int count = rs.getInt(1);
            stmnt.executeQuery("DELETE FROM DATATYPES_TEST");
            con.close();
            assertEquals(1, count);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * See <a href=https://github.com/brettwooldridge/HikariCP/issues/928>HikariJNDIFactory should not throw a NamingException when !"javax.sql.DataSource".equals(ref.getClassName())</a>
     */
    @Test(expected = AssertionError.class)
    public void hikariCPAndBeans() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/java/hikari/roots/HikariCPAndBeans");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("jndi.syntax.separator", "/");
            env.put(Context.OBJECT_FACTORIES, HikariJNDIFactory.class.getName() + ":" + DemoBeanFactory.class.getName());
            ctx = new InitialContext(env);
            DemoBean demoBean = (DemoBean) ctx.lookup("DemoBean");
            assertNotNull(demoBean);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * To tackle the problem described at <a href=https://github.com/brettwooldridge/HikariCP/issues/928>HikariJNDIFactory should not throw a NamingException when !"javax.sql.DataSource".equals(ref.getClassName())</a> one can subclass {@link HikariJNDIFactory} and return null when a {@link javax.naming.NamingException} is thrown.
     */
    @Test
    public void hikariCPAndBeansSolutions1() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/java/hikari/roots/HikariCPAndBeans");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("jndi.syntax.separator", "/");
            env.put(Context.OBJECT_FACTORIES, NullReturningHikariJNDIFactory.class.getName() + ":" + DemoBeanFactory.class.getName());
            ctx = new InitialContext(env);
            DemoBean demoBean = (DemoBean) ctx.lookup("DemoBean");
            assertNotNull(demoBean);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * List {@link HikariJNDIFactory} always as the last Factory in the list.
     */
    @Test
    public void hikariCPAndBeansSolution2() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/java/hikari/roots/HikariCPAndBeans");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("jndi.syntax.separator", "/");
            env.put(Context.OBJECT_FACTORIES,  DemoBeanFactory.class.getName()+ ":" + HikariJNDIFactory.class.getName());
            ctx = new InitialContext(env);
            DemoBean demoBean = (DemoBean) ctx.lookup("DemoBean");
            assertNotNull(demoBean);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void hikariCPAndTypes() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/java/hikari/roots/HikariCPAndTypes");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("jndi.syntax.separator", "/");
            env.put(Context.OBJECT_FACTORIES, HikariJNDIFactory.class.getName() + ":" + DemoBeanFactory.class.getName());
            ctx = new InitialContext(env);
            int year = (int) ctx.lookup("typed.year");
            assertEquals(2017, year);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

}
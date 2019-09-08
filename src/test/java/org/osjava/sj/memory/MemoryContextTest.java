package org.osjava.sj.memory;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.jndi.MemoryContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Holger Thurow (thurow.h@gmail.com) on 04/02/2017.
 */
public class MemoryContextTest {

    @Test
    public void bindName() throws Exception {
        Hashtable env = new Hashtable();
//        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
//        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.separator", "/");
//        env.put("org.osjava.sj.jndi.shared", "true");

        final MemoryContext ctx = new MemoryContext(env);
        ctx.bind("name", "value");
        final String name = (String) ctx.lookup("name");
        ctx.close();
        assertEquals("value", name);
    }

    @Test
    public void loadViaInitialContext() throws Exception {
        InitialContext ctx = null;
        try {
            Hashtable env = new Hashtable();
            env.put("jndi.syntax.direction", "left_to_right");
            env.put("jndi.syntax.separator", "/");
            env.put("java.naming.factory.initial", "org.osjava.sj.MemoryContextFactory");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx = new InitialContext(env);
            final Context sub1 = ctx.createSubcontext("sub1");

            sub1.bind("name", "value");
            String name = (String) sub1.lookup("name");
            assertEquals("value", name);

            name = (String) ctx.lookup("sub1/name");
            assertEquals("value", name);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void subcontext() throws Exception {
        String name;
        MemoryContext ctx = null;
        try {
            Hashtable env = new Hashtable();
            /* The default is 'flat', which isn't hierarchial. */
            env.put("jndi.syntax.direction", "left_to_right");
//           Sonst java.lang.IllegalArgumentException: jndi.syntax.separator property required for non-flat syntax
//            at javax.naming.NameImpl.recordNamingConvention(NameImpl.java:223)
            env.put("jndi.syntax.separator", "/");
            ctx = new MemoryContext(env);
            final Context sub1 = ctx.createSubcontext("sub1");

            sub1.bind("name", "value");
            name = (String) sub1.lookup("name");
            assertEquals("value", name);

            name = (String) ctx.lookup("sub1/name");
            assertEquals("value", name);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * <p>Workaround for <a href="https://stackoverflow.com/questions/51911367/error-when-trying-to-use-simple-jndi">Error when trying to use Simple-JNDI</a>
     * </p>
     */
    @Test
    public void initializeWithoutJndiPropertiesFile() throws NamingException {

        InitialContext ic = null;
        InitialContext ic2 = null;
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");

            Hashtable env = new Hashtable();
            env.put("org.osjava.sj.jndi.shared", "true");

            ic = new InitialContext(env);

            ic.createSubcontext("java:/comp/env/jdbc");

            JDBCDataSource ds = new JDBCDataSource();
            ds.setDatabase("jdbc:hsqldb:hsql://localhost/xdb");
            ds.setUser("SA");
            ds.setPassword("");

            ic.bind("java:/comp/env/jdbc/myDS", ds);

            ic2 = new InitialContext(env);
            DataSource dataSource = (DataSource) ic2.lookup("java:/comp/env/jdbc/myDS");
            assertNotNull(dataSource);
        }
        finally {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
            if (ic != null) {
                ic.close();
            }
            if (ic2 != null) {
                ic2.close();
            }
        }
    }

    /**
     * See {@link #initializeWithoutJndiPropertiesFile()}
     */
    @Test
    public void initializeWithoutJndiPropertiesFile2() throws NamingException, IOException {
        InitialContext ic = null;
        InitialContext ic2 = null;
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
            System.setProperty(SimpleJndi.SHARED, "true");

            ic = new InitialContext();

            ic.createSubcontext("java:/comp/env/jdbc");

            JDBCDataSource ds = new JDBCDataSource();
            ds.setDatabase("jdbc:hsqldb:hsql://localhost/xdb");
            ds.setUser("SA");
            ds.setPassword("");

            ic.bind("java:/comp/env/jdbc/myDS", ds);

            ic2 = new InitialContext();
            DataSource dataSource = (DataSource) ic2.lookup("java:/comp/env/jdbc/myDS");
            assertNotNull(dataSource);
        }
        finally {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
            System.clearProperty(SimpleJndi.SHARED);
            if (ic != null) {
                ic.close();
            }
            if (ic2 != null) {
                ic2.close();
            }
        }
    }

}
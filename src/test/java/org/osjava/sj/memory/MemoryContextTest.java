package org.osjava.sj.memory;

import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;

/**
 * Created by hot on 04/02/2017.
 */
public class MemoryContextTest {
    @Test
    public void bindName() throws Exception {
        Hashtable env = new Hashtable();
//        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory");
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
        Hashtable env = new Hashtable();
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put("java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory");
        env.put("org.osjava.sj.jndi.shared", "true");
        InitialContext ctx = new InitialContext(env);
        final Context sub1 = ctx.createSubcontext("sub1");

        sub1.bind("name", "value");
        String name = (String) sub1.lookup("name");
        assertEquals("value", name);

        name = (String) ctx.lookup("sub1/name");
        assertEquals("value", name);
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

}
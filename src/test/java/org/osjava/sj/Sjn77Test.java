package org.osjava.sj;

import org.junit.After;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

public class Sjn77Test {

    @After
    public void tearDown() throws Exception {
        System.clearProperty("java.naming.factory.initial");
        System.clearProperty("org.osjava.sj.jndi.shared");
    }

    @Test
    public void testPut() throws NamingException {
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.MemoryContextFactory");
        InitialContext ic = new InitialContext();
        ic.bind("test", "value");
        assertEquals("value", ic.lookup("test"));
        ic.close();

        System.setProperty("org.osjava.sj.jndi.shared", "true");
        ic = new InitialContext();
        ic.bind("test", "value");
        assertEquals("value", ic.lookup("test"));
        ic.close();
    }
}

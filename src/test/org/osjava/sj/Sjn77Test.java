package org.osjava.sj;

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Sjn77Test extends TestCase {

    public void testPut() throws NamingException {
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory");
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

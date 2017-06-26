package org.osjava.sj;

import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

public class Sjn77Test {

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

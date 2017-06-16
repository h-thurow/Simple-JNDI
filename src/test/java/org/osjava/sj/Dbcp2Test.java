package org.osjava.sj;

import org.junit.Test;

import javax.naming.Reference;
import javax.sql.DataSource;

/*
javax.naming.spi.NamingManager
    public static Object getObjectInstance(Object refInfo,
                       Name name,
                       Context nameCtx,
                       Hashtable<?,?> environment)
                                throws Exception
    Creates an instance of an object for the specified object and environment.
    Service providers that implement only the Context interface should use this method.

 */
public class Dbcp2Test {
    @Test
    public void basicDataSourceFactory() throws Exception {
        Reference reference = new Reference(DataSource.class.getName());

    }
}
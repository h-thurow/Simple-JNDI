/*
 * Copyright (c) 2003-2005, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of Simple-JNDI nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.osjava.sj.loader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class NioBasedJndiLoaderTest {

    private Context ctxt;
    private NioBasedJndiLoader loader;

    @Before
    public void setUp() {

        /* The default is 'flat', which isn't hierarchial and not what I want. */
        /* Separator is required for non-flat */

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        /* For Directory-Naming
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        env.put(Context.URL_PKG_PREFIXES, "org.apache.naming");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        */


        loader = new NioBasedJndiLoader(env);
        
        try {
            ctxt = new InitialContext(env);
        } catch(NamingException ne) {
            ne.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        this.ctxt = null;
    }

    @Test
    public void testDefaultFile() {
        try {
            File file = new File("src/test/resources/roots/default.properties");
            loader.load(file, ctxt, true);
            List list = (List) ctxt.lookup("name");
            assertEquals( "Henri", list.get(0) );
            assertEquals( "Fred", list.get(1) );
            assertEquals( "Foo", ctxt.lookup("com.genjava") );
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: "+ioe.getMessage());
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    @Test
    public void testSubContext() {

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        env.put(JndiLoader.FILENAME_TO_CONTEXT, "true");

        /* For Directory-Naming
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        env.put(Context.URL_PKG_PREFIXES, "org.apache.naming");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/");
        */

        NioBasedJndiLoader loader = new NioBasedJndiLoader(env);

        try {
            ctxt = new InitialContext(env);
        } catch(NamingException ne) {
            ne.printStackTrace();
        }

        String dsString = "bing::::foofoo::::Boo";
        try {
            File file = new File("src/test/resources/roots/java.properties");
            loader.load(file, ctxt, true);
            Context subctxt = (Context) ctxt.lookup("java");
            assertEquals( dsString, subctxt.lookup("TestDS").toString() );
            DataSource ds = (DataSource) ctxt.lookup("java/TestDS");
            assertEquals( dsString, ds.toString() );
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: "+ioe.getMessage());
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    @Test
    public void testTopLevelDataSource() {
        String dsString = "org.gjt.mm.mysql.Driver::::jdbc:mysql://127.0.0.1/tmp::::sa";
        try {
            File file = new File("src/test/resources/roots/TopLevelDS.properties");
            loader.load(file, ctxt, true);
            DataSource ds = (DataSource) ctxt.lookup("TopLevelDS");
            assertEquals( dsString, ds.toString() );
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: "+ioe.getMessage());
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    @Test
    public void testSlashSeparatedNamespacedProperty() throws NamingException {
        Properties props = new Properties();
        props.put("my/name", "holger");
        loader.load( props, ctxt );
        String obj = (String) ctxt.lookup("my/name");
        assertEquals("holger", obj);
    }

    @Test
    public void testDbcp() throws IOException, NamingException {
        File file = new File("src/test/resources/roots/pooltest");
        loader.load(file, ctxt, true);
        DataSource ds = (DataSource) ctxt.lookup("TestDS");
        assertNotNull(ds);
        DataSource ds1 = (DataSource) ctxt.lookup("OneDS");
        assertNotNull(ds1);
        DataSource ds2 = (DataSource) ctxt.lookup("TwoDS");
        assertNotNull(ds2);
        DataSource ds3 = (DataSource) ctxt.lookup("ThreeDS");
        assertNotNull(ds3);
        try {
            Connection conn = ds.getConnection();
            fail("No database is hooked up, so this should have failed");
        } catch (SQLException sqle) {
            // expected
        }
    }
}

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

import java.io.File;
import java.io.IOException;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import junit.framework.TestCase;

public class JndiLoaderTest extends TestCase {

    private Context ctxt;
    private JndiLoader loader;

    public JndiLoaderTest(String name) {
        super(name);
    }

    public void setUp() {

        /* The default is 'flat', which isn't hierarchial and not what I want. */
        /* Separator is required for non-flat */

        Hashtable contextEnv = new Hashtable();

        /* For GenericContext */
        contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory");
        contextEnv.put("jndi.syntax.direction", "left_to_right");
        contextEnv.put("jndi.syntax.separator", "/");
        /**/

        /* For Directory-Naming
        contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        contextEnv.put(Context.URL_PKG_PREFIXES, "org.apache.naming");
        contextEnv.put("jndi.syntax.direction", "left_to_right");
        contextEnv.put("jndi.syntax.separator", "/");
        */

        contextEnv.put(JndiLoader.SIMPLE_DELIMITER, "/");

        loader = new JndiLoader(contextEnv);
        
        try {
            ctxt = new InitialContext(contextEnv);
        } catch(NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void tearDown() {
        this.ctxt = null;
    }

    public void testProperties() {
        try {
            Properties props = new Properties();
            props.put("foo", "13");
            props.put("bar/foo", "42");
            props.put("bar/test/foo", "101");
            loader.load( props, ctxt );
            assertEquals( "13", ctxt.lookup("foo") );
            assertEquals( "42", ctxt.lookup("bar/foo") );
            assertEquals( "101", ctxt.lookup("bar/test/foo") );
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    public void testDirectory() {
        try {
            File file = new File("src/test/config/");
            loader.loadDirectory( file, ctxt );
            assertEquals( "13", ctxt.lookup("test/value") );
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: "+ioe.getMessage());
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    public void testDefaultFile() {
        try {
            File file = new File("src/test/config/");
            loader.loadDirectory( file, ctxt );
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

    public void testSubContext() {
        String dsString = "bing::::foofoo::::Boo";
        try {
            File file = new File("src/test/config/");
            loader.loadDirectory( file, ctxt );
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

    public void testTopLevelDataSource() {
        String dsString = "org.gjt.mm.mysql.Driver::::jdbc:mysql://127.0.0.1/tmp::::sa";
        try {
            File file = new File("src/test/config/");
            loader.loadDirectory( file, ctxt );
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

    public void testBoolean() {
        try {
            Properties props = new Properties();
            props.put("foo", "true");
            props.put("foo/type", "java.lang.Boolean");
            loader.load( props, ctxt );
            assertEquals( new Boolean(true), ctxt.lookup("foo") );
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    public void testDate() {
        try {
            Properties props = new Properties();
            props.put("birthday", "2004-10-22");
            props.put("birthday/type", "java.util.Date");
            props.put("birthday/format", "yyyy-MM-dd");

            loader.load( props, ctxt );

            Date d = (Date) ctxt.lookup("birthday");
            Calendar c = Calendar.getInstance();
            c.setTime(d);

            assertEquals( 2004, c.get(Calendar.YEAR) );
            assertEquals( 10 - 1, c.get(Calendar.MONTH) );
            assertEquals( 22, c.get(Calendar.DATE) );
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    public void testConverterPlugin() {
        try {
            Properties props = new Properties();
            props.put("math", "Pi");
            // type is needed here as otherwise it does not know to allow subelements
            props.put("math/type", "magic number");
            props.put("math/converter", "org.osjava.sj.loader.convert.PiConverter");

            loader.load( props, ctxt );

            assertEquals( new Double(Math.PI), ctxt.lookup("math") );
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    public void testBeanConverter() {
        try {
            Properties props = new Properties();
            props.put("bean/type", "org.osjava.sj.loader.TestBean");
            props.put("bean/converter", "org.osjava.sj.loader.convert.BeanConverter");
            props.put("bean/text", "Example");

            loader.load( props, ctxt );

            TestBean testBean = new TestBean();
            testBean.setText("Example");

            assertEquals( testBean, ctxt.lookup("bean") );
        } catch(NamingException ne) {
            ne.printStackTrace();
            fail("NamingException: "+ne.getMessage());
        }
    }

    public void testDbcp() throws IOException, NamingException {
        File file = new File("src/test/config/");
        loader.loadDirectory( file, ctxt );
        DataSource ds = (DataSource) ctxt.lookup("pooltest/TestDS");
        DataSource ds1 = (DataSource) ctxt.lookup("pooltest/OneDS");
        DataSource ds2 = (DataSource) ctxt.lookup("pooltest/TwoDS");
        DataSource ds3 = (DataSource) ctxt.lookup("pooltest/ThreeDS");

        try {
            Connection conn = ds.getConnection();
            fail("No database is hooked up, so this should have failed");
        } catch (SQLException sqle) {
            // expected
        }
    }

}

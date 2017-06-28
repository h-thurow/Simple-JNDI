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
package org.osjava.sj;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimpleJndiTest {

    private InitialContext ctxt;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() throws NamingException {
        ctxt.close();
    }

    @Test
    public void testValueLookup() throws NamingException {
        Hashtable env = new Hashtable();
        env.put("org.osjava.sj.root", "src/test/resources/roots/test.properties");
        env.put("org.osjava.sj.filenameToContext", "true");
        ctxt = new InitialContext(env);
        assertEquals( "13", ctxt.lookup("test.value") );
    }

    @Test
    public void testListLookup() throws NamingException {
        Hashtable env = new Hashtable();
        env.put("org.osjava.sj.root", "src/test/resources/roots/thing");
        ctxt = new InitialContext(env);
        ArrayList list = new ArrayList();
        list.add( "24" );
        list.add( "25" );
        list.add( "99" );
        assertEquals(list, ctxt.lookup("type.bob.age") );
    }

    @Test
    public void testList2Lookup() throws NamingException {
        Hashtable env = new Hashtable();
        env.put("org.osjava.sj.root", "src/test/resources/roots/default.properties");
        ctxt = new InitialContext(env);
        ArrayList list2 = new ArrayList();
        list2.add( "Henri" );
        list2.add( "Fred" );
        assertEquals( list2, ctxt.lookup("name") );
        assertEquals( "yandell.org", ctxt.lookup("url") );
        assertEquals( "Foo", ctxt.lookup("com.genjava") );
    }

    @Test
    public void testXmlLookup() throws NamingException {
        Hashtable env = new Hashtable();
        env.put("org.osjava.sj.root", "src/test/resources/roots/xmltest.xml");
        env.put("org.osjava.sj.filenameToContext", "true");
        ctxt = new InitialContext(env);
        assertEquals( "13", ctxt.lookup("xmltest.config.value") );
        assertEquals( "Bang", ctxt.lookup("xmltest.config.four.five") );
        assertEquals( "three", ctxt.lookup("xmltest.config.one.two") );
        List list = new ArrayList();
        list.add("one");
        list.add("two");
        assertEquals( list, ctxt.lookup("xmltest.config.multi.item") );
    }

    @Test
    public void testIniLookup() throws NamingException {
        Hashtable env = new Hashtable();
        env.put("org.osjava.sj.root", "src/test/resources/roots/testini.ini");
        env.put("org.osjava.sj.filenameToContext", "true");
        ctxt = new InitialContext(env);
        assertEquals( "blockless", ctxt.lookup("testini.first") );
        assertEquals( "13", ctxt.lookup("testini.block1.value") );
        assertEquals( "pears", ctxt.lookup("testini.block2.apple") );
        assertEquals( "stairs", ctxt.lookup("testini.block2.orange") );
        assertEquals("\"multiple words value\"", ctxt.lookup("testini.block2.doubleQuotes"));
    }

    @Test
    public void testColonReplaceLookup() throws NamingException {
        Hashtable env = new Hashtable();
        env.put("org.osjava.sj.root", "src/test/resources/roots/java--.properties");
        env.put("org.osjava.sj.filenameToContext", "true");
        env.put("org.osjava.sj.colon.replace", "--");
        ctxt = new InitialContext(env);
        Context subCtxt = (Context) ctxt.lookup("java:");
        assertEquals( "42", ctxt.lookup("java:.magic") );
        assertEquals( "42", subCtxt.lookup("magic") );
    }

    @Test
    public void testDoubleDSLookup() throws NamingException {
        Hashtable env = new Hashtable();
        env.put("org.osjava.sj.root", "src/test/resources/roots/datasourceNamespaced.properties");
        env.put("org.osjava.sj.filenameToContext", "true");
        ctxt = new InitialContext(env);
        String dsString = "org.gjt.mm.mysql.Driver::::jdbc:mysql://127.0.0.1/tmp::::sa";
        DataSource fooDS = (DataSource) ctxt.lookup("datasourceNamespaced.com.foo.FooDS");
        DataSource barDS = (DataSource) ctxt.lookup("datasourceNamespaced.com.foo.BarDS");
        assertEquals( dsString, fooDS.toString() );
        assertEquals( dsString, barDS.toString() );
    }

}

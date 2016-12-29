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

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;


import junit.framework.TestCase;

public class SimpleContextTest extends TestCase {

    private InitialContext ctxt;

    public SimpleContextTest(String name) {
        super(name);
    }

    public void setUp() {
        try {
            ctxt = new InitialContext();
        } catch(NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void tearDown() {
        this.ctxt = null;
    }

    public void testValueLookup() throws NamingException {
        assertEquals( "13", this.ctxt.lookup("test.value") );
    }

    public void testListLookup() throws NamingException {
        ArrayList list = new ArrayList();
        list.add( "24" );
        list.add( "25" );
        list.add( "99" );
        assertEquals( list, this.ctxt.lookup("thing.type.bob.age") );
    }

    public void testList2Lookup() throws NamingException {
        ArrayList list2 = new ArrayList();
        list2.add( "Henri" );
        list2.add( "Fred" );
        assertEquals( list2, this.ctxt.lookup("name") );
        assertEquals( "yandell.org", this.ctxt.lookup("url") );
        assertEquals( "Foo", this.ctxt.lookup("com.genjava") );
    }

    public void testXmlLookup() throws NamingException {
//            System.err.println("XML: "+this.ctxt.lookup("xmltest") );
// TODO: Should this return something? XML?
//            System.err.println("XML: "+this.ctxt.lookup("xmltest.config") );
            assertEquals( "13", this.ctxt.lookup("xmltest.config.value") );
            assertEquals( "Bang", this.ctxt.lookup("xmltest.config.four.five") );
            assertEquals( "three", this.ctxt.lookup("xmltest.config.one.two") );
            List list = new ArrayList();
            list.add("one");
            list.add("two");
            assertEquals( list, this.ctxt.lookup("xmltest.config.multi.item") );
    }

    public void testIniLookup() throws NamingException {
        assertEquals( "blockless", this.ctxt.lookup("testini.first") );
        assertEquals( "13", this.ctxt.lookup("testini.block1.value") );
        assertEquals( "pears", this.ctxt.lookup("testini.block2.apple") );
        assertEquals( "stairs", this.ctxt.lookup("testini.block2.orange") );
    }

    public void testColonReplaceLookup() throws NamingException {
        assertEquals( "42", this.ctxt.lookup("java:.magic") );

        Context subCtxt = (Context) this.ctxt.lookup("java:");
        assertEquals( "42", subCtxt.lookup("magic") );
    }

    public void testDoubleDSLookup() throws NamingException {
        String dsString = "org.gjt.mm.mysql.Driver::::jdbc:mysql://127.0.0.1/tmp::::sa";
        DataSource fooDS = (DataSource) this.ctxt.lookup("nested-datasource.com.foo.FooDS");
        DataSource barDS = (DataSource) this.ctxt.lookup("nested-datasource.com.foo.BarDS");
        assertEquals( dsString, fooDS.toString() );
        assertEquals( dsString, barDS.toString() );
    }

}

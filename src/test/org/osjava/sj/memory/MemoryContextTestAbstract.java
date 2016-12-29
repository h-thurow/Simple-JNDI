/*
 * org.osjava.jndi.MemoryContext_createSubContextTest
 * $Id: MemoryContextTestAbstract.java 1872 2005-08-21 02:12:54Z hen $
 * $Rev: 1872 $ 
 * $Date: 2005-08-20 19:12:54 -0700 (Sat, 20 Aug 2005) $ 
 * $Author: hen $
 * $URL: https://osjava.googlecode.com/svn/releases/simple-jndi-0.11.4.1/src/test/org/osjava/sj/memory/MemoryContextTestAbstract.java $
 * 
 * Created on Feb 14, 2005
 *
 * Copyright (c) 2004, Robert M. Zigweid All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer. 
 *
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution. 
 *
 * + Neither the name of the Simple-JNDI nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without 
 *   specific prior written permission.
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


package org.osjava.sj.memory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

import static org.junit.Assert.*;

/**
 * @author rzigweid
 */
public abstract class MemoryContextTestAbstract {

    private Context context;
    private String delimiter;

    protected abstract String getDelimiter();

    @Before
    public void setUp() {
        this.delimiter = getDelimiter();
        
        /* Initial configuration voodoo for the default context. */
        Hashtable contextEnv = new Hashtable();
        contextEnv.put("java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory");

        /* The default is 'flat', which isn't hierarchial and not what I want. */
        contextEnv.put("jndi.syntax.direction", "left_to_right");

        /* Separator is required for non-flat */
        contextEnv.put("jndi.syntax.separator", this.delimiter);
        
        /* The intial context. */
        try {
            context = new InitialContext(contextEnv);
        } catch(NamingException e) {
            e.printStackTrace();
        }
    }
    
    @After
    public void tearDown() {
        try {
            context.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateSubContext1() {
        try {
            /* Create the subContext object */
            context.createSubcontext("path");

            /* Make sure that it exists. */
            Object sub = context.lookup("path");
            assertNotNull(sub);
        } catch (NamingException e) {
            fail("NamingException " + e.getMessage());
        }
    }

    @Test
    public void testCreateNestedSubcontext() {
        try {
            Context sub = context.createSubcontext("path");
            sub = sub.createSubcontext("one");
            sub = sub.createSubcontext("two");

            assertNotNull( context.lookup("path"+this.delimiter+"one"+this.delimiter+"two") );
        } catch (NamingException e) {
            e.printStackTrace();
            fail("NamingException " + e.getMessage());
        }
    }

    @Test
    public void testLookupInContext() {
        try {
            context.createSubcontext("path");
            context.bind("path"+this.delimiter+"foo", "42");
            assertEquals("42", context.lookup("path"+this.delimiter+"foo") );
        } catch (NamingException e) {
            fail("NamingException " + e.getMessage());
        }
    }

}

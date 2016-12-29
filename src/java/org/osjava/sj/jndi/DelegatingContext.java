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
package org.osjava.sj.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;


/**
 * Standard delegating pattern for JNDI Contexts.
 * Sub-classes of this may filter calls to the JNDI code. 
 */
public abstract class DelegatingContext implements Context {

    private Context target;

    public DelegatingContext(Context ctxt) {
        this.target = ctxt;
    }

    public Object lookup(Name name) throws NamingException {
        return this.target.lookup(name);
    }

    public Object lookup(String name) throws NamingException {
        return this.target.lookup(name);
    }

    public void bind(Name name, Object value) throws NamingException {
        this.target.bind(name, value);
    }

    public void bind(String name, Object value) throws NamingException {
        this.target.bind(name, value);
    }

    public void rebind(Name name, Object value) throws NamingException {
        this.target.rebind(name, value);
    }

    public void rebind(String name, Object value) throws NamingException {
        this.target.rebind(name, value);
    }

    public void unbind(Name name) throws NamingException {
        this.target.unbind(name);
    }

    public void unbind(String name) throws NamingException {
        this.target.unbind(name);
    }

    public void rename(Name name, Name name2) throws NamingException {
        this.target.rename(name, name2);
    }

    public void rename(String name, String name2) throws NamingException {
        this.target.rename(name, name2);
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return this.target.list(name);
    }

    public NamingEnumeration list(String name) throws NamingException {
        return this.target.list(name);
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return this.target.listBindings(name);
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        return this.target.listBindings(name);
    }

    public void destroySubcontext(Name name) throws NamingException {
        this.target.destroySubcontext(name);
    }

    public void destroySubcontext(String name) throws NamingException {
        this.target.destroySubcontext(name);
    }

    public Context createSubcontext(Name name) throws NamingException {
        return this.target.createSubcontext(name);
    }

    public Context createSubcontext(String name) throws NamingException {
        return this.target.createSubcontext(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return this.target.lookupLink(name);
    }

    public Object lookupLink(String name) throws NamingException {
        return this.target.lookupLink(name);
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return this.target.getNameParser(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
        return this.target.getNameParser(name);
    }

    public Name composeName(Name name, Name name2) throws NamingException {
        return this.target.composeName(name, name2);
    }

    public String composeName(String name, String name2) throws NamingException {
        return this.target.composeName(name, name2);
    }

    public Object addToEnvironment(String key, Object value) throws NamingException {
        return this.target.addToEnvironment(key, value);
    }

    public Object removeFromEnvironment(String key) throws NamingException {
        return this.target.removeFromEnvironment(key);
    }

    public Hashtable getEnvironment() throws NamingException {
        return this.target.getEnvironment();
    }

    public void close() throws NamingException {
        this.target.close();
    }

    public String getNameInNamespace() throws NamingException {
        return this.target.getNameInNamespace();
    }

    protected Context getTarget() {
        return this.target;
    }

}


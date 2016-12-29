/*
 * org.osjava.naming.ContextBindings
 * 
 * $Id: ContextBindings.java 1983 2005-09-03 14:03:38Z rzigweid $
 * $Rev: 1983 $
 * $Date: 2005-09-03 07:03:38 -0700 (Sat, 03 Sep 2005) $
 * $Author: rzigweid $
 * 
 * Created on Apr 20, 2004
 * Copyright (c) 2004, Robert M. Zigweid
 * All rights reserved.
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
 * + Neither the name of the Simple-JNDI nor the bindings of its contributors may
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
package org.osjava.sj.jndi;

import java.util.Iterator;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * This class represents a NamingEnumeration of the bindings of a Context. 
 * Originally authored by Henri Yandell and modified to make more flexable with other 
 * Context implementations.
 * 
 * @author Robert M. Zigweid and Henri Yandell
 * @version $Rev: 1983 $ $Date: 2005-09-03 07:03:38 -0700 (Sat, 03 Sep 2005) $
 */
public class ContextBindings implements NamingEnumeration {
    
    /**
     * A Map of the bindings of a Context.
     */
    private Map bindings = null;
    
    /**
     * The iterator utilized in the Enumeration
     */
    private Iterator iterator = null;

    /**
     * Creates a ContextBindings object based upon an a Map of names and the objects 
     * the names are bound to.  If <code>table</code> is modified after instantiation 
     * of ContextBindings, behavior is undefined and should be considered invalid.
     * 
     * @param table The table upon which the ContextBindings is based.
     */
    public ContextBindings(Map table) {
        bindings = table;
        iterator = bindings.keySet().iterator();
    }

    /**
     * Returns <code>true</code> if there are more elements available, otherwise
     * <code>false</code>.
     * 
     * @return <code>true</code> if there are more elements available, otherwise <code>
     *         false</code>
     */
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    /**
     * Returns <code>true</code> if there are more elements available, otherwise
     * <code>false</code>.
     * 
     * @return <code>true</code> if there are more elements available, otherwise <code>
     *         false</code>
     * @throws NamingException if a naming exception is encountered
     */
    public boolean hasMore() throws NamingException {
        if(bindings == null) {
            throw new NamingException();
        }
        return hasMoreElements();
    }

    /**
     * Returns a {@link Binding Binding} created from the next available name.
     * 
     * @return a Binding representing the binding of the name and the object bound to the
     *         name
     */
    public Object nextElement() {
        if(bindings == null) { 
            return null;
        }
        Object name = iterator.next();
        /* What comes out of the iterator should be a CompoundName */
        return new Binding(name.toString(), bindings.get(name));
    }

    /**
     * Returns a {@link Binding Binding} created from the next available name.
     * 
     * @return a Binding representing the binding of the name and the object bound to the
     *         name
     * @throws NamingException if a naming exception occurs
     */
    public Object next() throws NamingException {
        if(bindings == null) {
            throw new NamingException();
        }
        return nextElement();
    }

    /**
     * Close the ContextBindings instance, rendering it inoperable.
     */
    public void close() {
        bindings = null;
        iterator = null;
    }

}


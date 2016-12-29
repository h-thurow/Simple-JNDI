/*
 * org.osjava.naming.ContextBindings
 * 
 * $Id: ContextNames.java 1978 2005-08-30 01:30:33Z hen $
 * $Rev: 1978 $
 * $Date: 2005-08-29 18:30:33 -0700 (Mon, 29 Aug 2005) $
 * $Author: hen $
 * 
 * Created on Apr 27, 2004
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

import java.util.Map;

import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

/**
 * This class represents a NamingEnumeration of the class names of a Context. 
 * Originally authored by Henri Yandell and modified to make more flexable with other 
 * Context implementations.
 * 
 * @author Robert M. Zigweid and Henri Yandell
 * @version $Rev: 1978 $ $Date: 2005-08-29 18:30:33 -0700 (Mon, 29 Aug 2005) $
 */
public class ContextNames extends ContextBindings {

    /**
     * Creates a ContextNames object based upon an a Map of names and the objects 
     * the names are bound to.  If <code>table</code> is modified after instantiation 
     * of ContextBindings, behavior is undefined and should be considered invalid.
     * 
     * @param table The table upon which the ContextBindings is based.
     */
    public ContextNames(Map table) {
        super(table);
    }

    /**
     * Returns a {@link NameClassPair} created from the next available name.
     * 
     * @return a NameClassPair representing the binding of the name and the
     *         object bound to the name
     */
    public Object nextElement() {
        return super.nextElement();
    }

    /**
     * Returns a {@link NameClassPair} created from the next available name.
     * 
     * @return a NameClassPair representing the binding of the name and the
     *         object bound to the name
     * 
     * @throws NamingException if a naming exception occurs
     */
    public Object next() throws NamingException {
        Binding binding = null;
        binding = (Binding)super.next();
        return new NameClassPair(binding.getName(), 
            binding.getObject().getClass().getName());
    }
}


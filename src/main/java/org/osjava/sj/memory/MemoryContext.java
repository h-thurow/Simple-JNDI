/*
 * org.osjava.sj.memory.MemoryContext
 * $Id: MemoryContext.java 1743 2005-06-24 23:56:40Z rzigweid $
 * $Rev: 1743 $ 
 * $Date: 2005-06-24 16:56:40 -0700 (Fri, 24 Jun 2005) $ 
 * $Author: rzigweid $
 * $URL: https://osjava.googlecode.com/svn/releases/simple-jndi-0.11.4.1/src/java/org/osjava/sj/memory/MemoryContext.java $
 * 
 * Created on Dec 30, 2004
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

import org.osjava.sj.jndi.AbstractContext;

import javax.naming.*;
import java.util.Hashtable;

/**
 * A generic context that requires no DataSource backend.   It is designed to
 * live exclusively in memory and not have its state saved.
 * 
 * @author Robert M. Zigweid
 * @since Simple-JNDI 0.11
 * @version $Rev: 1743 $ $Date: 2005-06-24 16:56:40 -0700 (Fri, 24 Jun 2005) $
 */
public class MemoryContext extends AbstractContext {

    /**
     * 
     */
    public MemoryContext() {
        super();
    }

    /**
     * @param env
     */
    public MemoryContext(Hashtable env) {
        super(env);
    }

    /**
     * @param env
     * @param systemOverride
     */
    public MemoryContext(Hashtable env, boolean systemOverride) {
        super(env);
    }

    /**
     * @param env
     * @param parser
     */
    public MemoryContext(Hashtable env, NameParser parser) {
        super(env, parser);
    }

    /**
     * @param systemOverride
     */
    public MemoryContext(boolean systemOverride) {
        super();
    }

    /**
     * @param systemOverride
     * @param parser
     */
    public MemoryContext(boolean systemOverride, NameParser parser) {
        super(parser);
    }

    /**
     * @param parser
     */
    public MemoryContext(NameParser parser) {
        super(parser);
    }

    /**
     * @param env
     * @param systemOverride
     * @param parser
     */
    public MemoryContext(Hashtable env, boolean systemOverride, NameParser parser) {
        super(env, parser);
    }

    /**
     * @param that
     */
    public MemoryContext(AbstractContext that) {
        super(that);
    }

    /**
     * @see javax.naming.Context#createSubcontext(javax.naming.Name)
     */
    @Override
    public Context createSubcontext(Name name) throws NamingException {
        Context newContext;
        /* Get the subcontexts of this subcontext. */
        Hashtable subContexts = getSubContexts();

        if(name.size() > 1) {
            if(subContexts.containsKey(name.getPrefix(1))) {
                Context subContext = (Context)subContexts.get(name.getPrefix(1));
                newContext = subContext.createSubcontext(name.getSuffix(1));
                return newContext;
            }
            else {
                throw new NameNotFoundException("The subcontext " + name.getPrefix(1) + " was not found.");
            }
        }
        
        if(lookup(name) != null) {
            throw new NameAlreadyBoundException();
        }

        Name contextName = getNameParser((Name)null).parse(getNameInNamespace());
        contextName.addAll(name);
        newContext = new MemoryContext(this);
        ((AbstractContext)newContext).setNameInNamespace(contextName);
        bind(name, newContext);
        return newContext;
    }
}

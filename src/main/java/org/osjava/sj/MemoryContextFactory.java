/*
 * org.osjava.jndiMemoryContextFactory
 * $Id: MemoryContextFactory.java 1743 2005-06-24 23:56:40Z rzigweid $
 * $Rev: 1743 $ 
 * $Date: 2005-06-24 16:56:40 -0700 (Fri, 24 Jun 2005) $ 
 * $Author: rzigweid $
 * $URL: https://osjava.googlecode.com/svn/releases/simple-jndi-0.11.4.1/src/java/org/osjava/sj/memory/MemoryContextFactory.java $
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


package org.osjava.sj;

import org.apache.commons.lang3.BooleanUtils;
import org.osjava.sj.jndi.MemoryContext;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import static org.osjava.sj.jndi.MemoryContext.IGNORE_CLOSE;

/**
 * Initial Context Factory for {@link MemoryContext}. Note the difference to {@link SimpleJndiContextFactory}. Here an empty context is returned. No sub contexts or JNDI objects are initialized from sub directories or property files under a root directory. The org.osjava.sj.root property may even not be set.
 * 
 * @author Robert M. Zigweid, Holger Thurow
 * @since Simple-JNDI 0.11
 */
public class MemoryContextFactory extends ContextFactory implements InitialContextFactory {

    private static final ConcurrentHashMap<String, Context> contextsByRoot =
            new ConcurrentHashMap<String, Context>();

    /**
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public MemoryContextFactory() {
        super();
    }

    /**
     * @see InitialContextFactory#getInitialContext(Hashtable)
     */
    public Context getInitialContext(Hashtable environment) throws NamingException {
        overwriteEnvironmentWithSystemProperties(environment);
        initializeStandardJndiEnvironment(environment);
        final Boolean isShared = Boolean.valueOf(
                (String) environment.get("org.osjava.sj.jndi.shared"));
        if (!isShared) {
            return new MemoryContext(environment);
        }
        else {
            String root = (String) environment.get("org.osjava.sj.root");
            final Context ctx = contextsByRoot.get(root);
            // ctx.listBindings("").hasMore(): Ob alle Kontexte zerstört wurden.
            if (ctx != null) {
                String ignoreClose = (String) environment.get(IGNORE_CLOSE);
                ctx.addToEnvironment(
                        IGNORE_CLOSE,
                        BooleanUtils.toStringTrueFalse(BooleanUtils.toBoolean(ignoreClose)));
                return ctx;
            }
            else {
                final String finalRoot = root;
                MemoryContext context = new MemoryContext(environment) {
                    private boolean isClosed;
                    @Override
                    public void close() throws NamingException {
                        // When already closed getEnvironment() throws an Exception.
                        if (!isClosed) {
                            String ignoreClose = (String) getEnvironment().get(IGNORE_CLOSE);
                            if (!BooleanUtils.toBoolean(ignoreClose)) {
                                // first remove, so the context will be removed even when close()
                                // throws an Exception
                                contextsByRoot.remove(finalRoot);
                                super.forceClose();
                                isClosed = true;
                            }
                        }
                    }

                };
                contextsByRoot.put(root, context);
                return context;
            }
        }
    }

    /**
     * package-private: Only for Testing!
     */
    static void clearCache() {
        contextsByRoot.clear();
    }

}

/*
 * org.osjava.jndiSimpleContextFactory
 * $Id: SimpleContextFactory.java 1743 2005-06-24 23:56:40Z rzigweid $
 * $Rev: 1743 $ 
 * $Date: 2005-06-24 19:56:40 -0400 (Fri, 24 Jun 2005) $ 
 * $Author: rzigweid $
 * $URL: https://svn.osjava.org/svn/osjava/trunk/simple-jndi/src/java/org/osjava/sj/memory/SimpleContextFactory.java $
 * 
 * Created on Aug 20, 2005
 *
 * Copyright (c) 2005, Henri Yandell All rights reserved.
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Initial Context Factory for SimpleContexts
 * 
 * @author Henri Yandell, Holger Thurow
 * @since Simple-JNDI 0.11
 */
public class SimpleContextFactory implements InitialContextFactory {

    private static final ConcurrentHashMap<String, DelimiterConvertingContext> contextsByRoot =
            new ConcurrentHashMap<String, DelimiterConvertingContext>();

    public SimpleContextFactory() {
        super();
    }

    /**
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    @Override
    public Context getInitialContext(Hashtable environment) throws NamingException {
        final Boolean isShared = Boolean.valueOf(
                (String) environment.get(SimpleJndi.SIMPLE_SHARED));
        if (!isShared) {
            return new DelimiterConvertingContext(new SimpleJndi(environment).loadRoot());
        }
        else {
            final String root = (String) environment.get(SimpleJndi.SIMPLE_ROOT);
            final Context ctx = contextsByRoot.get(root);
            if (ctx != null) {
                return ctx;
            }
            else {
                InitialContext context = new SimpleJndi(environment).loadRoot();
                final DelimiterConvertingContext delimiterConvertingContext = new DelimiterConvertingContext(context) {
                    @Override
                    public void close() throws NamingException {
                        // first remove, so the context will be removed even when close()
                        // throws an Exception
                        contextsByRoot.remove(root);
                        target.close();
                    }

                };
                contextsByRoot.put(root, delimiterConvertingContext);
                return delimiterConvertingContext;
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

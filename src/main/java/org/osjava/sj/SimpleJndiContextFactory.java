/*
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

import org.apache.commons.lang.BooleanUtils;
import org.osjava.sj.jndi.DelimiterConvertingContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import static org.osjava.sj.jndi.MemoryContext.IGNORE_CLOSE;

/**
 * Unlike {@link MemoryContextFactory} this factory could theoretically (untested) return another {@link Context} implementation than {@link org.osjava.sj.jndi.MemoryContext} by setting {@link SimpleJndi#CONTEXT_FACTORY} to a different {@link InitialContextFactory} Implementation.
 *
 * @author Henri Yandell, Holger Thurow
 */
public class SimpleJndiContextFactory implements InitialContextFactory {
    private static final ConcurrentHashMap<String, DelimiterConvertingContext> contextsByRoot =
            new ConcurrentHashMap<String, DelimiterConvertingContext>();

    /**
     * package-private: Only for Testing!
     */
    static void clearCache() {
        contextsByRoot.clear();
    }

    /**
     * @see InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    @Override
    public Context getInitialContext(final Hashtable environment) throws NamingException {
        final Boolean isShared = Boolean.valueOf(
                (String) environment.get(SimpleJndi.SHARED));
        if (!isShared) {
            return new DelimiterConvertingContext(new SimpleJndi(environment).loadRoot());
        }
        else {
            final String root = (String) environment.get(SimpleJndi.ROOT);
            final Context ctx = contextsByRoot.get(root);
            if (ctx != null) {
                String ignoreClose = (String) environment.get(IGNORE_CLOSE);
                ctx.addToEnvironment(
                        IGNORE_CLOSE,
                        BooleanUtils.toStringTrueFalse(BooleanUtils.toBoolean(ignoreClose)));
                return ctx;
            }
            else {
                InitialContext context = new SimpleJndi(environment).loadRoot();
                final DelimiterConvertingContext delimiterConvertingContext = new DelimiterConvertingContext(context) {
                    private boolean isClosed;

                    @Override
                    public void close() throws NamingException {
                        // When already closed getEnvironment() throws an Exception.
                        if (!isClosed) {
                            String ignoreClose = (String) getEnvironment().get(IGNORE_CLOSE);
                            if (!BooleanUtils.toBoolean(ignoreClose)) {
                                // first remove, so the context will be removed even when close() throws an Exception.
                                contextsByRoot.remove(root);
                                target.close();
                                isClosed = true;
                            }
                        }
                    }
                };
                contextsByRoot.put(root, delimiterConvertingContext);
                return delimiterConvertingContext;
            }
        }
    }
}

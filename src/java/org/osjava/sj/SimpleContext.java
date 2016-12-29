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

import org.osjava.sj.jndi.DelegatingContext;
import org.osjava.sj.jndi.AbstractContext;

import java.io.File;
import java.io.IOException;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;

import org.osjava.sj.loader.JndiLoader;
import org.osjava.sj.loader.util.Utils;

// job is to hide the JndiLoader, apart from a jndi.properties entry
// can also handle switching . to / so that the delimiter may be settable
public class SimpleContext extends DelegatingContext {

    // root
    public static final String SIMPLE_ROOT = "org.osjava.sj.root";

    public static final String SIMPLE_DELEGATE = "org.osjava.sj.factory";

    // option for top level space; ie) java:comp
    public static final String SIMPLE_SPACE = "org.osjava.sj.space";

    /*
     * 
     * root
     *    org.osjava.jndi.root
     * separator, or just put them in as contexts?
     *    org.osjava.jndi.delimiter
     * option for top level space; ie) java:comp
     *    org.osjava.jndi.space
     * share the same InitialContext
     *    org.osjava.jndi.shared
     */
    public SimpleContext(Hashtable env) throws NamingException {
        super(createContext(env));

        JndiLoader loader = new JndiLoader(env);

        String root = (String) env.get(SIMPLE_ROOT);

        if(root == null) {
            throw new IllegalStateException("Property "+SIMPLE_ROOT+" is mandatory. ");
        }

        if(root.startsWith("file://")) {
            root = root.substring("file://".length());
        }

        if(!AbstractContext.isSharedAndLoaded()) {
            Context ctxt = this;
            String space = (String) env.get(SIMPLE_SPACE);
            if(space != null) {
                // make contexts for space...
                String[] array = Utils.split(space, (String) env.get(JndiLoader.SIMPLE_DELIMITER) );
                for(int i=0; i<array.length; i++) {
                    ctxt = ctxt.createSubcontext(array[i]);
                }
            }

            try {
                loader.loadDirectory( new File(root), ctxt );
            } catch(IOException ioe) {
                throw new NamingException("Unable to load data from directory: "+root+" due to error: "+ioe.getMessage());
            }
        }
    }
    
    private static InitialContext createContext(Hashtable env) throws NamingException {

        copyFromSystemProperties(env, JndiLoader.SIMPLE_DELIMITER);
        copyFromSystemProperties(env, SIMPLE_ROOT);
        copyFromSystemProperties(env, SIMPLE_SPACE);
        copyFromSystemProperties(env, JndiLoader.SIMPLE_SHARED);
        copyFromSystemProperties(env, SIMPLE_DELEGATE);
        
        env.put("jndi.syntax.direction", "left_to_right");
        if(!env.containsKey(JndiLoader.SIMPLE_DELIMITER)) {
            env.put(JndiLoader.SIMPLE_DELIMITER, ".");
        }
        env.put("jndi.syntax.separator", env.get(JndiLoader.SIMPLE_DELIMITER));

        if(!env.containsKey(SIMPLE_DELEGATE)) {
            env.put(SIMPLE_DELEGATE, "org.osjava.sj.memory.MemoryContextFactory");
        }

        env.put("java.naming.factory.initial", env.get(SIMPLE_DELEGATE) );

        return new InitialContext(env);
    }

    private static void copyFromSystemProperties(Hashtable env, String key) {
        if(System.getProperty(key) != null) {
            env.put(key, System.getProperty(key));
        }
    }

}

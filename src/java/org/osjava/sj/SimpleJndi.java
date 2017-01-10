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

import org.osjava.sj.loader.JndiLoader;
import org.osjava.sj.loader.util.Utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * job is to hide the JndiLoader, apart from a jndi.properties entry.
 * Can also handle switching . to / so that the delimiter may be settable.
 */
public class SimpleJndi {

    public static final String SIMPLE_ROOT = "org.osjava.sj.root";
    public static final String CONTEXT_FACTORY = "org.osjava.sj.factory";
    // option for top level space; ie) java:comp
    public static final String SIMPLE_SPACE = "org.osjava.sj.space";
    public static final String SIMPLE_SHARED = "org.osjava.sj.jndi.shared";
    private Hashtable environment;

    /**
     * root: {@link #SIMPLE_ROOT}<br>
     * separator, or just put them in as contexts?: org.osjava.jndi.delimiter<br>
     * option for top level space; ie) java:comp: {@link #SIMPLE_SPACE}<br>
     * share the same InitialContext: {@link #SIMPLE_SHARED}
     * <p>
     * By default allow system properties to override environment. Siehe
     * {@link org.osjava.sj.jndi.AbstractContext#AbstractContext(Hashtable)}
     */
    SimpleJndi(Hashtable environment) {
        this.environment = environment;
        overwriteEnvironmentWithSystemProperties();
    }

    InitialContext loadRoot() throws NamingException {
        initializeStandardJndiEnvironment();

        String root = getRoot(environment);

        final InitialContext initialContext = createInitialContext();
        Context ctxt = initialContext;
        ctxt = createENC(environment, ctxt);
        try {
            final JndiLoader loader = new JndiLoader(environment);
            final File rootFile = new File(root);
            if (rootFile.isDirectory()) {
                loader.loadDirectory(rootFile, ctxt );
            }
            else if(rootFile.isFile()) {
                loader.load(loader.toProperties(rootFile), ctxt);
            }
        } catch(IOException ioe) {
            throw new NamingException("Unable to load data from directory: "+root+" due to error: "+ioe.getMessage());
        }
        return initialContext;
    }

    /**
     * To simulate an environment naming context (ENC), the org.osjava.sj.space property
     * may be used. Whatever the property is set to will be automatically prepended to
     * every value loaded into the system. Thus org.osjava.sj.space=java:/comp/env
     * simulates the JNDI environment of Tomcat.
     */
    private Context createENC(Hashtable env, Context ctxt) throws NamingException {
        String space = (String) env.get(SIMPLE_SPACE);
        if(space != null) {
            String[] array = Utils.split(space, (String) env.get(JndiLoader.SIMPLE_DELIMITER) );
            for (String anArray : array) {
                ctxt = ctxt.createSubcontext(anArray);
            }
        }
        return ctxt;
    }

    private String getRoot(Hashtable env) {
        String root = (String) env.get(SIMPLE_ROOT);
        if(root == null) {
            throw new IllegalStateException("Property "+SIMPLE_ROOT+" is mandatory. ");
        }
        if(root.startsWith("file://")) {
            root = root.substring("file://".length());
        }
        return root;
    }

    private void initializeStandardJndiEnvironment() {
        environment.put("jndi.syntax.direction", "left_to_right");
        if(!environment.containsKey(JndiLoader.SIMPLE_DELIMITER)) {
            environment.put(JndiLoader.SIMPLE_DELIMITER, ".");
        }
        environment.put("jndi.syntax.separator", environment.get(JndiLoader.SIMPLE_DELIMITER));
    }

    private InitialContext createInitialContext() throws NamingException {
        if(!environment.containsKey(CONTEXT_FACTORY)) {
            environment.put(CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory");
        }
        environment.put("java.naming.factory.initial", environment.get(CONTEXT_FACTORY) );
        // Hier wird MemoryContextFactory#getInitialContext() gerufen!
        return new InitialContext(environment);
    }

    private void overwriteEnvironmentWithSystemProperties() {
        overwriteFromSystemProperty(JndiLoader.SIMPLE_DELIMITER);
        overwriteFromSystemProperty(SIMPLE_ROOT);
        overwriteFromSystemProperty(SIMPLE_SPACE);
        overwriteFromSystemProperty(JndiLoader.SIMPLE_SHARED);
        overwriteFromSystemProperty(CONTEXT_FACTORY);
    }

    private void overwriteFromSystemProperty(String key) {
        if(System.getProperty(key) != null) {
            environment.put(key, System.getProperty(key));
        }
    }

}

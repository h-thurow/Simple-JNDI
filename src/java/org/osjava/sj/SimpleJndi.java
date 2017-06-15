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

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.osjava.sj.loader.JndiLoader;
import org.osjava.sj.loader.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class SimpleJndi {

    public static final String SIMPLE_ROOT = "org.osjava.sj.root";
    public static final String CONTEXT_FACTORY = "org.osjava.sj.factory";
    /** Option for top level space (ENC), e.g. "java:comp/env". */
    public static final String SIMPLE_SPACE = "org.osjava.sj.space";
    public static final String SIMPLE_SHARED = "org.osjava.sj.jndi.shared";
    public static final String JNDI_SYNTAX_SEPARATOR = "jndi.syntax.separator";
    private static final Logger logger = LoggerFactory.getLogger(SimpleJndi.class);
    public static final String FILENAME_TO_CONTEXT = "org.osjava.sj.filenameToContext";
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJndi.class);

    private Hashtable<String, String> environment;

    SimpleJndi(Hashtable<String, String> environment) {
        this.environment = environment;
        overwriteEnvironmentWithSystemProperties();
    }

    InitialContext loadRoot() throws NamingException {
        initializeStandardJndiEnvironment();

        final InitialContext initialContext = createInitialContext();
        Context ctxt = initialContext;
        ctxt = createENC(environment, ctxt);
        JndiLoader loader = new JndiLoader(environment);
        String root = getRoot(environment);
        if (root != null && !root.isEmpty()) {
            final String[] roots = root.split(File.pathSeparator);
            for (String path : roots) {
                final File rootFile = new File(path);
                LOGGER.debug("Loading {}", rootFile.getAbsolutePath());
                try {
                    if (rootFile.isDirectory()) {
                        loader.loadDirectory(rootFile, ctxt);
                    }
                    else if (rootFile.isFile()) {
                        Context tmpCtx = ctxt;
                        if (environment.containsKey(FILENAME_TO_CONTEXT)) {
                            tmpCtx = ctxt.createSubcontext(FilenameUtils.removeExtension(
                                    rootFile.getName()));
                        }
                        loader.load(loader.toProperties(rootFile), tmpCtx);
                    }
                    else {
                        throw new NamingException("Unable to load data from " +
                                rootFile.getAbsolutePath());
                    }
                }
                catch (IOException e) {
                    throw new NamingException("Unable to load data from " +
                            rootFile.getAbsolutePath()+" due to error: " + e.getMessage());
                }
            }
        }
        else {
            logger.warn("Mistakenly no root provided?");
        }
        return initialContext;
    }

    /**
     * To simulate an environment naming context (ENC), the org.osjava.sj.space property
     * may be used. Whatever the property is set to will be automatically prepended to
     * every value loaded into the system. Thus org.osjava.sj.space=java:comp/env
     * simulates the JNDI environment of Tomcat.
     */
    private Context createENC(Hashtable env, Context ctxt) throws NamingException {
        String space = (String) env.get(SIMPLE_SPACE);
        if(space != null) {
            String delimiter = (String) env.get(JndiLoader.SIMPLE_DELIMITER);
            final Object separator = env.get(JNDI_SYNTAX_SEPARATOR);
            if (separator != null && !separator.equals(delimiter)) {
                delimiter = "\\" + delimiter + "|\\" + separator;
            }
            String[] contextNames = Utils.split(space, delimiter);
            for (String name : contextNames) {
                ctxt = ctxt.createSubcontext(name);
            }
        }
        return ctxt;
    }

    @Nullable
    private String getRoot(Hashtable env) {
        String root = (String) env.get(SIMPLE_ROOT);
//        if(root == null) {
//            throw new IllegalStateException("Property "+SIMPLE_ROOT+" is mandatory. ");
//        }
        if(root != null && root.startsWith("file://")) {
            root = root.substring("file://".length());
        }
        return root;
    }

    private void initializeStandardJndiEnvironment() {
        environment.put("jndi.syntax.direction", "left_to_right");
        if(!environment.containsKey(JndiLoader.SIMPLE_DELIMITER)) {
            environment.put(JndiLoader.SIMPLE_DELIMITER, ".");
        }
        if (!environment.containsKey(JNDI_SYNTAX_SEPARATOR)) {
            environment.put(JNDI_SYNTAX_SEPARATOR, environment.get(JndiLoader.SIMPLE_DELIMITER));
        }
    }

    private InitialContext createInitialContext() throws NamingException {
        if(!environment.containsKey(CONTEXT_FACTORY)) {
            environment.put(CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory");
        }
        environment.put("java.naming.factory.initial", environment.get(CONTEXT_FACTORY) );
        // Hier wird MemoryContextFactory#getInitialContext() gerufen!
        return new InitialContext(environment);
    }

    /**
     * Allow system properties to override environment. Siehe
     * {@link org.osjava.sj.jndi.AbstractContext#AbstractContext(Hashtable)}.
     */
    private void overwriteEnvironmentWithSystemProperties() {
        overwriteFromSystemProperty(SIMPLE_ROOT);
        overwriteFromSystemProperty(SIMPLE_SPACE);
        overwriteFromSystemProperty(CONTEXT_FACTORY);
        overwriteFromSystemProperty(SIMPLE_SHARED);
        overwriteFromSystemProperty(JNDI_SYNTAX_SEPARATOR);
        overwriteFromSystemProperty(FILENAME_TO_CONTEXT);
        overwriteFromSystemProperty(JndiLoader.SIMPLE_DELIMITER);
        overwriteFromSystemProperty(JndiLoader.SIMPLE_COLON_REPLACE);
    }

    private void overwriteFromSystemProperty(String key) {
        if(System.getProperty(key) != null) {
            environment.put(key, System.getProperty(key));
        }
    }

}

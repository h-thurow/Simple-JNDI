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

import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.Nullable;
import org.osjava.StringUtils;
import org.osjava.sj.loader.JndiLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.Hashtable;

public class SimpleJndi {

    public static final String ROOT = "org.osjava.sj.root";
    public static final String CONTEXT_FACTORY = "org.osjava.sj.factory";
    /** Option for top level space (ENC), e.g. "java:comp/env". */
    public static final String ENC = "org.osjava.sj.space";
    public static final String SHARED = "org.osjava.sj.jndi.shared";
    public static final String JNDI_SYNTAX_SEPARATOR = "jndi.syntax.separator";
    private static final Logger logger = LoggerFactory.getLogger(SimpleJndi.class);
    public static final String FILENAME_TO_CONTEXT = "org.osjava.sj.filenameToContext";
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJndi.class);

    private Hashtable<String, String> env;

    SimpleJndi(Hashtable<String, String> environment) {
        this.env = environment;
        overwriteEnvironmentWithSystemProperties();
    }

    InitialContext loadRoot() throws NamingException {
        initializeStandardJndiEnvironment();

        final InitialContext initialContext = createInitialContext();
        Context ctxt = initialContext;
        ctxt = createENC(env, ctxt);
        JndiLoader loader = new JndiLoader(env);
        String root = getRoot(env);
        if (root != null && !root.isEmpty()) {
            final String[] roots = root.split(File.pathSeparator);
            for (String path : roots) {
                final File rootFile = new File(path);
                LOGGER.debug("Loading {}", rootFile.getAbsolutePath());
                try {
                    loader.load(rootFile, ctxt, BooleanUtils.toBoolean(env.get(FILENAME_TO_CONTEXT)), true);
                }
                catch (Exception e) {
                    LOGGER.error("Unable to load: ",
                            rootFile.getAbsolutePath(), e);
                    initialContext.close();
                    throw new NamingException("" + e.getMessage());
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
        String space = (String) env.get(ENC);
        if(space != null) {
            String delimiter = (String) env.get(JndiLoader.DELIMITER);
            final Object separator = env.get(JNDI_SYNTAX_SEPARATOR);
            if (separator != null && !separator.equals(delimiter)) {
                delimiter = "\\" + delimiter + "|\\" + separator;
            }
            String[] contextNames = StringUtils.split(space, delimiter);
            for (String name : contextNames) {
                ctxt = ctxt.createSubcontext(name);
            }
        }
        return ctxt;
    }

    @Nullable
    private String getRoot(Hashtable env) {
        String root = (String) env.get(ROOT);
//        if(root == null) {
//            throw new IllegalStateException("Property "+ROOT+" is mandatory. ");
//        }
        if(root != null && root.startsWith("file://")) {
            root = root.substring("file://".length());
        }
        return root;
    }

    private void initializeStandardJndiEnvironment() {
        env.put("jndi.syntax.direction", "left_to_right");
        if(!env.containsKey(JndiLoader.DELIMITER)) {
            env.put(JndiLoader.DELIMITER, ".");
        }
        if (!env.containsKey(JNDI_SYNTAX_SEPARATOR)) {
            env.put(JNDI_SYNTAX_SEPARATOR, env.get(JndiLoader.DELIMITER));
        }
    }

    private InitialContext createInitialContext() throws NamingException {
        if(!env.containsKey(CONTEXT_FACTORY)) {
            env.put(CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        }
        env.put("java.naming.factory.initial", env.get(CONTEXT_FACTORY) );
        // Hier wird MemoryContextFactory#getInitialContext() gerufen!
        return new InitialContext(env);
    }

    /**
     * Allow system properties to override environment. Siehe
     * {@link org.osjava.sj.jndi.MemoryContext#MemoryContext(Hashtable)}.
     */
    private void overwriteEnvironmentWithSystemProperties() {
        overwriteFromSystemProperty(ROOT);
        overwriteFromSystemProperty(ENC);
        overwriteFromSystemProperty(CONTEXT_FACTORY);
        overwriteFromSystemProperty(SHARED);
        overwriteFromSystemProperty(JNDI_SYNTAX_SEPARATOR);
        overwriteFromSystemProperty(FILENAME_TO_CONTEXT);
        overwriteFromSystemProperty(JndiLoader.DELIMITER);
        overwriteFromSystemProperty(JndiLoader.COLON_REPLACE);
        overwriteFromSystemProperty(Context.OBJECT_FACTORIES);
    }

    private void overwriteFromSystemProperty(String key) {
        if(System.getProperty(key) != null) {
            env.put(key, System.getProperty(key));
        }
    }

}

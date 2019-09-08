package org.osjava.sj;

import org.osjava.sj.loader.JndiLoader;

import javax.naming.Context;
import java.util.Hashtable;

import static org.osjava.sj.SimpleJndi.CONTEXT_FACTORY;
import static org.osjava.sj.jndi.MemoryContext.IGNORE_CLOSE;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 2019-09-08
 */
class ContextFactory {
    /**
     * Allow system properties to override environment. See Issue #2: Add JndiLoader.SIMPLE_COLON_REPLACE to overwriteEnvironmentWithSystemProperties.
     */
    static void overwriteEnvironmentWithSystemProperties(final Hashtable<String, String> env) {
        overwriteWithSystemProperty(SimpleJndi.ROOT, env);
        overwriteWithSystemProperty(SimpleJndi.ENC, env);
        overwriteWithSystemProperty(SimpleJndi.SHARED, env);
        overwriteWithSystemProperty(SimpleJndi.JNDI_SYNTAX_SEPARATOR, env);
        overwriteWithSystemProperty(SimpleJndi.FILENAME_TO_CONTEXT, env);
        overwriteWithSystemProperty(SimpleJndi.PATH_SEPARATOR, env);
        overwriteWithSystemProperty(JndiLoader.DELIMITER, env);
        overwriteWithSystemProperty(JndiLoader.COLON_REPLACE, env);
        overwriteWithSystemProperty(Context.OBJECT_FACTORIES, env);
        overwriteWithSystemProperty(IGNORE_CLOSE, env);
        overwriteWithSystemProperty("jndi.syntax.direction", env);
        overwriteWithSystemProperty(CONTEXT_FACTORY, env);

    }

    static void initializeStandardJndiEnvironment(final Hashtable<String, String> env) {
        if (!env.containsKey("jndi.syntax.direction")) {
            env.put("jndi.syntax.direction", "left_to_right");
        }
        if(!env.containsKey(JndiLoader.DELIMITER)) {
            env.put(JndiLoader.DELIMITER, ".");
        }
        if (!env.containsKey(SimpleJndi.JNDI_SYNTAX_SEPARATOR)) {
            env.put(SimpleJndi.JNDI_SYNTAX_SEPARATOR, env.get(JndiLoader.DELIMITER));
        }
        if(!env.containsKey(CONTEXT_FACTORY)) {
            env.put(CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        }
        env.put("java.naming.factory.initial", env.get(CONTEXT_FACTORY));
        // Issue #16 Enhancement request: make org.osjava.sj.root not mandatory in jndi.properties.
        if (!env.containsKey(SimpleJndi.ROOT)) {
            env.put(SimpleJndi.ROOT, "");
        }
    }

    private static void overwriteWithSystemProperty(String key, final Hashtable<String, String> env) {
        String value = System.getProperty(key);
        if(value != null) {
            env.put(key, value);
        }
    }
}

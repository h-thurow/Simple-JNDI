package org.osjava.sj.jndi;

import org.jetbrains.annotations.NotNull;
import org.osjava.sj.loader.JndiLoader;

import javax.naming.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 02.07.17
 */
public class JndiUtils {

    public static Properties toProperties(Reference ref) {
        Properties props = new Properties();
        Enumeration<RefAddr> allRefAddresses = ref.getAll();
        while (allRefAddresses.hasMoreElements()) {
            RefAddr refAddr = allRefAddresses.nextElement();
            props.setProperty(refAddr.getType(), (String) refAddr.getContent());
        }
        return props;
    }

    public static Reference toReference(Properties props, String className) {
        final String factory = props.getProperty("javaxNamingSpiObjectFactory", null);
        Reference ref = new Reference(className, factory, null);
        for (Object key : props.keySet()) {
            ref.add(new StringRefAddr((String) key, props.getProperty((String) key)));
        }
        return ref;
    }

    /**
     * {@link CompositeName} to {@link CompoundName} conversion. See issue #14.
     */
    static Name toCompoundName(Name objName, final Properties env) throws InvalidNameException
    {
        if (objName instanceof CompositeName) {
            return toCompoundName(objName.toString(), env);
        }
        return objName;
    }

    @NotNull
    public static CompoundName toCompoundName(final String objName, final Properties env) throws InvalidNameException
    {
        Properties envCopy = new Properties(env);
        envCopy.setProperty("jndi.syntax.separator", env.getProperty(JndiLoader.DELIMITER));
        envCopy.setProperty("jndi.syntax.direction", (String) env.get("jndi.syntax.direction"));
        return new CompoundName(objName, envCopy);
    }


}

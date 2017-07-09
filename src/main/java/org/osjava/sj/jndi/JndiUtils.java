package org.osjava.sj.jndi;

import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
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
        Reference ref = new Reference(className);
        for (Object key : props.keySet()) {
            ref.add(new StringRefAddr((String) key, props.getProperty((String) key)));
        }
        return ref;
    }
}

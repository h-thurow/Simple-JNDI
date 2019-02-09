package org.osjava.sj.loader;

import org.junit.Test;
import org.osjava.sj.jndi.MemoryContext;

import javax.naming.Context;
import javax.naming.Name;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 18.06.17
 */
public class JndiLoaderUnitTest {
    @Test
    public void testGetTypeDefinitionSlashNamespaced() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        Properties typeDef = new Properties();
        typeDef.setProperty("ds/type", "javax.sql.DataSource");
        typeDef.setProperty("ds/url", "jdbc:sybase:Tds:b-sonar-omcdb.berlin.six.de:5000");
        typeDef.setProperty("ds/driver", "com.sybase.jdbc3.jdbc.SybDriver");
        typeDef.setProperty("ds/user", "user");
        typeDef.setProperty("ds/password", "password");
        JndiLoader loader = new JndiLoader(env);
        String typeDefinition = loader.getTypeDefinition(typeDef);
        assertEquals("ds/type", typeDefinition);

        typeDef.remove("ds/type");
        typeDefinition = loader.getTypeDefinition(typeDef);
        assertEquals(null, typeDefinition);
    }

    @Test
    public void testGetTypeDefinitionNotNamespaced() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");
        Properties typeDef = new Properties();
        typeDef.setProperty("type", "javax.sql.DataSource");
        typeDef.setProperty("url", "jdbc:sybase:Tds:b-sonar-omcdb.berlin.six.de:5000");
        typeDef.setProperty("driver", "com.sybase.jdbc3.jdbc.SybDriver");
        typeDef.setProperty("user", "user");
        typeDef.setProperty("password", "password");
        JndiLoader loader = new JndiLoader(env);
        String typeDefinition = loader.getTypeDefinition(typeDef);
        assertEquals("type", typeDefinition);
    }

    @Test
    public void testGetTypeDefinitionDotNamespaced() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, ".");

        Properties typeDef = new Properties();
        typeDef.setProperty("ds/type", "javax.sql.DataSource");
        typeDef.setProperty("ds/url", "jdbc:sybase:Tds:b-sonar-omcdb.berlin.six.de:5000");
        typeDef.setProperty("ds/driver", "com.sybase.jdbc3.jdbc.SybDriver");
        typeDef.setProperty("ds/user", "user");
        typeDef.setProperty("ds/password", "password");
        JndiLoader loader = new JndiLoader(env);
        String typeDefinition = loader.getTypeDefinition(typeDef);
        assertEquals("ds/type", typeDefinition);

        typeDef = new Properties();
        typeDef.setProperty("ds.type", "javax.sql.DataSource");
        typeDef.setProperty("ds.url", "jdbc:sybase:Tds:b-sonar-omcdb.berlin.six.de:5000");
        typeDef.setProperty("ds.driver", "com.sybase.jdbc3.jdbc.SybDriver");
        typeDef.setProperty("ds.user", "user");
        typeDef.setProperty("ds.password", "password");
        loader = new JndiLoader(env);
        typeDefinition = loader.getTypeDefinition(typeDef);
        assertEquals("ds.type", typeDefinition);
    }

    @Test
    public void testExtractContextDotSeparated() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, ".");

        JndiLoader loader = new JndiLoader(env);
        Name name = loader.extractContextName("ds.type");
        assert name != null;
        assertEquals("ds", name.toString());
    }

    @Test
    public void testExtractContextSlashSeparated() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);
        Name name = loader.extractContextName("ds/type");
        assert name != null;
        assertEquals("ds", name.toString());
    }

    @Test
    public void testCreateSubContextsSlashSeparated() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator", "/"); // Required for MemoryContext
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);
        Name name = loader.extractContextName("jdbc/sybase/ds/type");
        assert name != null;
        assertEquals("jdbc/sybase/ds", name.toString());

        Context deepestContext = loader.createSubContexts(name, new MemoryContext(env));
        assertNotNull(deepestContext);
        assertEquals("jdbc/sybase/ds", deepestContext.getNameInNamespace());
    }

    @Test
    public void testExtractContextNotNamespaced() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);
        Name name = loader.extractContextName("type");
        assertNull(name);
    }

    @Test
    public void testExtractContextNonMatchingSeparator() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);
        Name name = loader.extractContextName("ds.type");
        assertNull(name);
    }

    @Test
    public void extractObjectNameSlash() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);
        String objName = loader.extractObjectName("my/ctx/object");
        assertEquals("object", objName);

        objName = loader.extractObjectName("object");
        assertEquals("object", objName);
    }

    @Test
    public void extractObjectNameNonMatchingSeparator() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, ".");

        JndiLoader loader = new JndiLoader(env);
        String objName = loader.extractObjectName("my/ctx/object");
        assertEquals("my/ctx/object", objName);

        objName = loader.extractObjectName("object");
        assertEquals("object", objName);
    }

    @Test
    public void extractTypedProperties() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);

        Properties props = new Properties();
        props.setProperty("type", "javax.sql.DataSource");
        props.setProperty("url", "some_url");

        Map<String, Properties> map = loader.extractTypedProperties(props);
        Properties def = map.get("datasourceOrBeanProperty");
        assertNotNull(def);
        String type = def.getProperty("type");
        assertNotNull(type);
        assertEquals("javax.sql.DataSource", type);
    }

    @Test
    public void extractTypedPropertiesWithNamespace() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);

        Properties props = new Properties();
        props.setProperty("Sybase/type", "javax.sql.DataSource");
        props.setProperty("Sybase/url", "some_url");

        Map<String, Properties> map = loader.extractTypedProperties(props);
        Properties def = map.get("Sybase");
        assertNotNull(def);
        String type = def.getProperty("type");
        assertNotNull(type);
        assertEquals("javax.sql.DataSource", type);
    }

    @Test
    public void extractTypedPropertiesWithNamespace2() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);

        Properties props = new Properties();
        props.setProperty("jdbc/Sybase/type", "javax.sql.DataSource");
        props.setProperty("jdbc/Sybase/url", "some_url");

        Map<String, Properties> map = loader.extractTypedProperties(props);
        Properties def = map.get("jdbc/Sybase");
        assertNotNull(def);
        String type = def.getProperty("type");
        assertNotNull(type);
        assertEquals("javax.sql.DataSource", type);
    }

    @Test
    public void extractTypedPropertiesSimpleTypeWithNamespace() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);

        Properties props = new Properties();
        props.setProperty("my/int/type", "java.lang.Integer");
        props.setProperty("my/int", "1");

        Map<String, Properties> map = loader.extractTypedProperties(props);
        Properties def = map.get("my/int");
        assertNotNull(def);
        String type = def.getProperty("type");
        assertNotNull(type);
        assertEquals("java.lang.Integer", type);
    }

    @Test
    public void extractTypedPropertiesSimpleTypeWithoutNamespace() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
        env.put("jndi.syntax.direction", "left_to_right");
        //env.put("jndi.syntax.separator", "/");
        env.put(JndiLoader.DELIMITER, "/");

        JndiLoader loader = new JndiLoader(env);

        Properties props = new Properties();
        props.setProperty("int/type", "java.lang.Integer");
        props.setProperty("int", "1");

        Map<String, Properties> map = loader.extractTypedProperties(props);
        Properties def = map.get("int");
        assertNotNull(def);
        String type = def.getProperty("type");
        assertNotNull(type);
        assertEquals("java.lang.Integer", type);
    }

    @Test
    public void loadDbcp2BasicDataSource() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.OBJECT_FACTORIES, org.apache.commons.dbcp2.BasicDataSourceFactory.class.getName());
        JndiLoader loader = new JndiLoader(env);
        Properties properties = new Properties();
        properties.setProperty("username", "username");
        properties.setProperty("password", "password");
        properties.setProperty("url", "url");
        Object o = loader.processType(properties, javax.sql.DataSource.class.getName(), null);
        assertEquals(org.apache.commons.dbcp2.BasicDataSource.class.getName(), o.getClass().getName());
    }
}
package org.osjava.sj;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.osjava.sj.jndi.MemoryContext;
import org.osjava.sj.loader.JndiLoader;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class SimpleJndiNewTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        System.clearProperty(SimpleJndi.ROOT);
        System.clearProperty(SimpleJndi.SHARED);
        System.clearProperty(SimpleJndi.ENC);
        System.clearProperty(SimpleJndi.JNDI_SYNTAX_SEPARATOR);
        System.clearProperty(SimpleJndi.FILENAME_TO_CONTEXT);
        System.clearProperty(JndiLoader.SIMPLE_COLON_REPLACE);
        System.clearProperty(JndiLoader.SIMPLE_DELIMITER);
    }

    /**
     * 0.11.4.1: javax.naming.ContextNotEmptyException
     */
    @Test
    public void sharedContextWithDataSource() throws Exception {

        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
//        env.put("org.osjava.sj.root", workspaceDir + "/SimpleJndi/src/test/resources/roots/datasourcePool/");
            env.put("org.osjava.sj.root", "src/test/resources/roots/datasourcePool/");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);
            ctx2 = new InitialContext(env);

            DataSource ds = (DataSource) ctx1.lookup("java:comp/env/myDataSource");
            assert ds != null;

            DataSource ds2 = (DataSource) ctx2.lookup("java:comp/env/myDataSource");
            assert ds2 != null;
            //shared
            assert ds == ds2;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }
        }
    }

//    /**
//     * Needs reachable database.
//     */
//    @Test
//    public void sharedContextWithPool() throws Exception {
//
//        InitialContext ctx = null;
//        try {
//            final Hashtable<String, String> env = new Hashtable<String, String>();
////        env.put("org.osjava.sj.root", workspaceDir + "/SimpleJndi/src/test/resources/roots/datasourcePool/");
//            env.put("org.osjava.sj.root", "file://src/test/resources/roots/datasourcePool/");
//            env.put("org.osjava.sj.jndi.shared", "true");
//            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
//            env.put("org.osjava.sj.delimiter", "/");
//            env.put("org.osjava.sj.space", "java:comp/env");
//
//            DataSource ds3 = (DataSource) ctx.lookup("java:comp/env/myDataSource");
//            assert ds3 != null;
//        }
//        finally {
//            if (ctx != null) {
//                ctx.close();
//            }
//        }
//    }

    /**
     * You can not mix properties with and without namespace in datasource configuration files.
     */
    @Test
    public void sharedContextWithDataSourceMixedNamespaces() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
//        env.put("org.osjava.sj.root", workspaceDir + "/SimpleJndi/src/test/resources/roots/datasourcePool/");
            env.put("org.osjava.sj.root", "src/test/resources/roots/datasourceMixedNamespaces");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("org.osjava.sj.space", "java:comp/env");

            thrown.expect(NamingException.class);
            thrown.expectMessage("Required subelement 'driver'");
            ctx1 = new InitialContext(env);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * From one client on an context bounded objects are also visible in other contexts
     * if shared is true.
     */
    @Test
    public void sharedContext() throws Exception {
        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "file://src/test/resources/roots/shareContext1");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);
            String myContext1_name = (String) ctx1.lookup("java:comp/env/myContext1/name");
            assert "holger".equals(myContext1_name);

            final String directory1_file1_name2 = (String) ctx1.lookup(
                    "java:comp/env/directory1/directory1_file1/name2");
            assert "noBlanksIncluded".equals(directory1_file1_name2);

            ctx1.bind("afterwardsBinded", "yep");

            ctx2 = new InitialContext(env);
            final String afterwardsBinded = (String) ctx2.lookup("afterwardsBinded");
            assert "yep".equals(afterwardsBinded);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }

        }

    }

    /**
     * 0.11.4.1: java.lang.AssertionError (quotes are not stripped from value)
     */
    @Test
    public void quotationMarks() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "file://src/test/resources/roots/shareContext1");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
//        env.put("org.osjava.sj.jndi.shared", "true");
            ctx1 = new InitialContext(env);

            final String directory1_file1_name = (String) ctx1.lookup(
                    "java:comp/env/directory1/directory1_file1/name");
            assertEquals("file1inDirectory1", directory1_file1_name);

            final String directory1_file1_withBlanks = (String) ctx1.lookup(
                    "java:comp/env/directory1/directory1_file1/withBlanks");
            assertEquals("\"Mit blanks\"", directory1_file1_withBlanks);

            final String directory1_file1_singleQuoted = (String) ctx1.lookup(
                    "java:comp/env/directory1/directory1_file1/singleQuoted");
            assertEquals("'Mit blanks'", directory1_file1_singleQuoted);

            final String multiWordsWithoutQuotes = (String) ctx1.lookup("java:comp/env/directory1/directory1_file1/multiWordsWithoutQuotes");
            assertEquals("first second third", multiWordsWithoutQuotes);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * In einem Kontext nachträglich gebundene Objekte sind nicht in einem anderen
     * Kontext sichtbar, wenn org.osjava.sj.jndi.shared nicht true ist.
     */
    @Test(expected = NameNotFoundException.class)
    public void unsharedContext() throws Exception {
        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/resources/roots/shareContext1");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);
            final String name = (String) ctx1.lookup("java:comp/env/myContext1/name");
            assert "holger".equals(name);

            ctx1.bind("afterwardsBinded", "yep");
            final String afterwardsBindedInCtx1 = (String) ctx1.lookup("afterwardsBinded");
            assert "yep".equals(afterwardsBindedInCtx1);

            ctx2 = new InitialContext(env);
            ctx2.lookup("afterwardsBinded");
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }
        }
    }

    /**
     * 0.11.4.1: java.lang.AssertionError (name == null)
     * <p>
     * Shared contexts mit unterschiedlichen roots sehen nicht untereinander ihre Objekte.
     */
    @Test
    public void sharedContextsWithDifferentRoots() throws Exception {
        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/shareContext1");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);

            final Hashtable<String, String> env2 = new Hashtable<String, String>();
            env2.put("org.osjava.sj.root",
                    "src/test/resources/roots/datasourcePool");
            env2.put("org.osjava.sj.jndi.shared", "true");
            env2.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env2.put("org.osjava.sj.delimiter", "/");
            env2.put("org.osjava.sj.space", "java:comp/env");

            ctx2 = new InitialContext(env2);

            String name = null;
            try {
                name = (String) ctx2.lookup("java:comp/env/myContext1/name");
            }
            catch (NamingException e) {
                System.out.println("Expected Exception !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                e.printStackTrace();
            }
            assert name == null;

            DataSource ds = (DataSource) ctx2.lookup("java:comp/env/myDataSource");
            assert ds != null;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }
        }
    }

    @Test
    public void beanNoSetterNotShared() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/bean");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final MyBean bean = (MyBean) ctx1.lookup("java:comp/env/beanNoSetter");
            assert bean != null;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * java.lang.RuntimeException: Unable to find method setSize on class: org.osjava.sj.BeanWithSetterMixedTypes
     *  at org.osjava.sj.loader.convert.BeanConverter.convert(BeanConverter.java:101)
     *  <p>
     *  "Only String properties are supported." See org.osjava.sj.loader.convert.BeanConverter.
     */
    @Test
    public void beanSetterNotSharedMixedTypes() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/beanWithSetter");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            thrown.expect(NamingException.class);
            thrown.expectMessage("Unable to find method setSize");
            ctx1 = new InitialContext(env);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void beanSetterNotSharedStringsOnly() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/beanWithSetterStringsOnly");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final BeanWithSetterStringsOnly bean = (BeanWithSetterStringsOnly) ctx1.lookup("java:comp/env/bean");
            assert bean != null;
            assertEquals("Holger", bean.getFirstName());
            assertArrayEquals(new String[]{"DE", "E"}, bean.getLanguages().toArray());
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * 0.11.4.1: javax.naming.ContextNotEmptyException
     */
    @Test
    public void beanNoSetterShared() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/bean");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final MyBean beanNoSetter = (MyBean) ctx1.lookup("java:comp/env/beanNoSetter");
            assert beanNoSetter != null;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * 0.11.4.1: javax.naming.ContextNotEmptyException
     */
    @Test
    public void beanNoSetterSharedTwoInitialContexts() throws Exception {
        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/bean");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);
            ctx2 = new InitialContext(env);

            final MyBean beanNoSetter = (MyBean) ctx1.lookup("java:comp/env/beanNoSetter");
            assert beanNoSetter != null;

            final MyBean beanNoSetter2 = (MyBean) ctx2.lookup("java:comp/env/beanNoSetter");
            assert beanNoSetter2 != null;

            assert beanNoSetter == beanNoSetter2;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }
        }
    }

    @Test
    public void beanNoSetterNotSharedTwoInitialContexts() throws Exception {
        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/bean");
//        env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);
            ctx2 = new InitialContext(env);

            final MyBean beanNoSetter = (MyBean) ctx1.lookup("java:comp/env/beanNoSetter");
            assert beanNoSetter != null;

            final MyBean beanNoSetter2 = (MyBean) ctx2.lookup("java:comp/env/beanNoSetter");
            assert beanNoSetter2 != null;

            assert beanNoSetter != beanNoSetter2;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }
        }
    }

    /**
     * 0.11.4.1: javax.naming.ContextNotEmptyException
     */
    @Test
    public void beanNoSetterSharedNoPresetSpace() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/bean");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
//        env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final MyBean beanNoSetter = (MyBean) ctx1.lookup("beanNoSetter");
            assert beanNoSetter != null;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void untypedProperty() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/untypedProperty");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final String name = (String) ctx1.lookup("java:comp/env/file1/name");
            assert "holger".equals(name);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * Non matching org.osjava.sj.delimiter ("/" instead of ".") > no type casting applied.
     */
    @Test
    public void sharedContextWithDataSource2NonMatchingDelimiter() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/datasource");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx1 = new InitialContext(env);
            final String type = (String) ctx1.lookup("java:comp/env/ds/TestDS.type");
            assert "javax.sql.DataSource".equals(type);
            final String user = (String) ctx1.lookup("java:comp/env/ds/TestDS.user");
            assert "user".equals(user);
//        final DataSource ds = (DataSource) ctx1.lookup("java:comp/env/ds/TestDS");
//        assert ds != null;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void sharedContextWithDataSource2MatchingDelimiter() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/datasource");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx1 = new InitialContext(env);
            final DataSource ds = (DataSource) ctx1.lookup("java:comp/env.ds.TestDS");
            assert ds != null;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * Non matching delimiter ("." instead of "/") > no type casting applied.
     */
    @Test
    public void typedPropertyNonMatchingDelimiter() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/typedProperty");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx1 = new InitialContext(env);
            final String ageType = (String) ctx1.lookup("java:comp/env/file1/myInteger.type");
            assert "java.lang.Integer".equals(ageType);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void typedPropertyMatchingDelimiter() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/typedProperty");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", ".");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx1 = new InitialContext(env);
            final Integer age = (Integer) ctx1.lookup("java:comp/env.file1.myInteger");
            assert 123 == age;
            final int size = (Integer) ctx1.lookup("java:comp/env.file1.parent.child1.size");
            assert size == 186;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    /**
     * 0.11.4.1: java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Integer
     * <p>
     * Matching delimiter > type casting successful<br>
     * Vergleiche {@link #typedPropertyNonMatchingDelimiter()}
     */
    @Test
    public void typedProperty1() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/typedProperty2");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");
            env.put("org.osjava.sj.jndi.shared", "true");
            ctx1 = new InitialContext(env);
            final Integer myInteger = (Integer) ctx1.lookup("java:comp/env/file1/myInteger");
            assert myInteger == 56;
            final Boolean myBoolean = (Boolean) ctx1.lookup("java:comp/env/file1/myBoolean");
            assert myBoolean;
            final Character myCharacter = (Character) ctx1.lookup("java:comp/env/file1/myCharacter");
            assert myCharacter == 'D';
            final Long myLong = (Long) ctx1.lookup("java:comp/env/file1/myLong");
            assert myLong == 123456789L;
            final Byte myByte = (Byte) ctx1.lookup("java:comp/env/file1/myByte");
            assert myByte.intValue() == 127;
            final Double myDouble = (Double) ctx1.lookup("java:comp/env/file1/myDouble");
            assert myDouble.intValue() == 123;
            final Float myFloat = (Float) ctx1.lookup("java:comp/env/file1/myFloat");
            assert myFloat.floatValue() == Float.valueOf(0.1234567890123456789f).floatValue();
            final Short myShort = (Short) ctx1.lookup("java:comp/env/file1/myShort");
            assert myShort == (short) -32768;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void twoInitialContextsSharedWithUntypedProperty() throws Exception {
        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/untypedProperty");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);
            final String name = (String) ctx1.lookup("java:comp/env/file1/name");
            assert "holger".equals(name);

            ctx2 = new InitialContext(env);
            final String name2 = (String) ctx2.lookup("java:comp/env/file1/name");
            assert "holger".equals(name2);

            //noinspection StringEquality
            assert name == name2;
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }
        }
    }

    /**
     * 0.11.4.1: javax.naming.ContextNotEmptyException
     */
    @Test
    public void destroyContextShared() throws Exception {
        InitialContext ctx1 = null;
        InitialContext ctx2 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/shareContext1");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);
            ctx1.unbind("java:comp/env/myContext1");
            final NamingEnumeration<Binding> enumeration = ctx1.listBindings(
                    "java:comp/env/directory1/directory1_file1");
//            while (enumeration.hasMoreElements()) {
//                Binding binding = enumeration.nextElement();
//                ctx1.unbind("java:comp/env/directory1/directory1_file1/" + binding.getName());
//            }
            ctx1.destroySubcontext("java:comp/env/directory1/directory1_file1");
            ctx1.destroySubcontext("java:comp/env/directory1");
            ctx1.destroySubcontext("java:comp/env");
            ctx1.destroySubcontext("java:comp");

            ctx2 = new InitialContext(env);
            assert !ctx2.listBindings("").hasMore();
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
            if (ctx2 != null) {
                ctx2.close();
            }
        }
    }

    @Test(expected = javax.naming.NoInitialContextException.class)
    public void contextNoEnvProvided() throws Exception {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", "");
        // Die Instanzierung wirft bei fehlender JNDI Konfiguration noch keine Exception ...
        final InitialContext ctx = new InitialContext(env);
        // ... erst der Aufruf einer Methode an ihr:
        // javax.naming.NoInitialContextException: Need to specify class name in environment
        // or system property, or as an applet parameter, or in an application
        // resource file:  java.naming.factory.initial
        ctx.listBindings("");
    }

    /**
     * 0.11.4.1: javax.naming.NameAlreadyBoundException
     */
    @Test
    public void sameNamesInDifferentBranchesShared() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/sameNamesInDifferentBranches");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
//        env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final String loc = "context1/persons/holger/location";
            final String location = (String) ctx1.lookup("context1/persons/holger/branch");
            assertEquals("context1", location);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void closeContext() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "file://src/test/resources/roots/sameNamesInDifferentBranches");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
//        env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
                // "This method is idempotent: invoking it on a context that has already been closed has no effect. Invoking any other method on a closed context is not allowed, and results in undefined behaviour." See http://docs.oracle.com/javase/8/docs/api/javax/naming/Context.html#close.
                ctx1.close();
            }
        }
    }

    @Test
    public void testFileAsRoot() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/fileAsRoot.cfg");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
//        env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final String infoCmd = (String) ctx1.lookup("IMAGE_INFO_CMD");
            assertEquals(". /.profile_opix;$OCHOME/opt/Python-2.7/bin/python $OCHOME/opt/image_processor/scripts/imageinfo.py", infoCmd);
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void testFileListAsRoot() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                        "src/test/resources/roots/fileAsRoot.cfg" + File.pathSeparator +
                       "src/test/resources/roots/shareContext1/directory1/directory1_file1.properties" + File.pathSeparator +
                        // delibrately missing file.
                        //"doesNotExist.cfg" + File.pathSeparator +
                        // mix files with directories
                        "src/test/resources/roots/multiValueAttributes");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
//        env.put("org.osjava.sj.space", "java:comp/env");
            ctx1 = new InitialContext(env);
            final String infoCmd = (String) ctx1.lookup("IMAGE_INFO_CMD");
            assertEquals(". /.profile_opix;$OCHOME/opt/Python-2.7/bin/python $OCHOME/opt/image_processor/scripts/imageinfo.py", infoCmd);
            assertEquals("\"'quotes' \"inside\"\"", ctx1.lookup("quotesInside"));
            List<Boolean> booleans =
                    (List<Boolean>) ctx1.lookup("booleans/person/myBooleans");
            assertEquals(true, booleans.get(0));
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void testFileListAsRootTakeFileNameAsContext() throws Exception {
        InitialContext ctx1 = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();

            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/fileAsRoot.cfg" + File.pathSeparator +
                    "src/test/resources/roots/shareContext1/directory1/directory1_file1.properties" + File.pathSeparator +
                            // mix files with directories
                    "src/test/resources/roots/multiValueAttributes");

            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.filenameToContext", "true");
//        env.put("org.osjava.sj.space", "java:comp/env");

            ctx1 = new InitialContext(env);

            final String infoCmd = (String) ctx1.lookup("fileAsRoot/IMAGE_INFO_CMD");
            assertEquals(". /.profile_opix;$OCHOME/opt/Python-2.7/bin/python $OCHOME/opt/image_processor/scripts/imageinfo.py", infoCmd);
            assertEquals("\"'quotes' \"inside\"\"", ctx1.lookup("directory1_file1/quotesInside"));
            List<Boolean> booleans =
                    (List<Boolean>) ctx1.lookup("booleans/person/myBooleans");
            assertEquals(true, booleans.get(0));
        }
        finally {
            if (ctx1 != null) {
                ctx1.close();
            }
        }
    }

    @Test
    public void testEncPrefixedPropertiesRootFile() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/encPrefixedProperties.properties");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            ctx = new InitialContext(env);
            final String size = (String) ctx.lookup("java:comp/env/holger/thurow");
            assertEquals("186", size);
            Context javaCompCtx = (Context) ctx.lookup("java:comp");
            assertNotNull(javaCompCtx);
            Context envCtx = (Context) javaCompCtx.lookup("env");
            assertNotNull(envCtx);
            assertNotNull(ctx.lookup("java:comp/env"));
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void testEncPrefixedPropertiesRootDirectory() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/enc");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            ctx = new InitialContext(env);
            final String size = (String) ctx.lookup("java:comp/env/holger/thurow");
            assertEquals("186", size);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void testEncPrefixedPropertiesDotSeparatedRootDirectory() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/encDotSeparated");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "."); // default, but to be explicit
            ctx = new InitialContext(env);
            final String size = (String) ctx.lookup("java:comp.env.holger.thurow");
            assertEquals("186", size);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void testEncColonReplaced() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/encColonReplacement");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "."); // default, but to be explicit
            env.put("org.osjava.sj.colon.replace", "--");
            ctx = new InitialContext(env);
            final String name = (String) ctx.lookup("java:comp.env.name");
            assertEquals("Holger", name);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * TODO Support for "java:"? Just now one have to lookup contexts within "java:" as "java:/".
     */
    @Test
    public void testEncColonTerminated() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root",
                    "src/test/resources/roots/encColonTerminated");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.delimiter", "/");
            env.put("org.osjava.sj.space", "java:");
            ctx = new InitialContext(env);
            final String name = (String) ctx.lookup("java:/product/name");
            assertEquals("Simple-JNDI", name);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void usageWithoutRoot() throws Exception {
        InitialContext ctx = null;
        try {
            // Werte aus jndi.properties überschreiben
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            JndiLoader loader = new JndiLoader(ctx.getEnvironment());

            Properties props = new Properties();
            props.put("foo", "13");
            props.put("bar.foo", "42");
            props.put("bar.test.foo", "101");

            loader.load(props, ctx);

            assertEquals( "13", ctx.lookup("foo") );
            assertEquals( "42", ctx.lookup("bar/foo") );
            assertEquals( "101", ctx.lookup("bar/test/foo") );
            assertEquals(MemoryContext.class, ctx.lookup("bar/test").getClass());

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * You can load multiple property files one after the other. How to assign them to different context roots?
     */
    @Test
    public void usageWithoutRoot2() throws Exception {
        InitialContext ctx = null;
        try {
            // Werte aus jndi.properties überschreiben
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            JndiLoader loader = new JndiLoader(ctx.getEnvironment());

            Properties props = new Properties();
            props.put("bar.foo", "101");

            loader.load(props, ctx);

            props = new Properties();
            props.put("bar.foo2", "42");

            loader.load(props, ctx);

            assertEquals( "101", ctx.lookup("bar/foo") );
            assertEquals( "42", ctx.lookup("bar/foo2") );

        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void emptyContexts() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/resources/roots/emptyContexts");
            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
            Context emptyContext = (Context) ctx.lookup("java:comp/env");
            assertTrue(emptyContext != null);
            // TODO java:comp.env.jdbc ohne schließendes "=" als Kontextdefinition interpretieren?
            String jdbc = (String) emptyContext.lookup("jdbc");
            assertEquals("", jdbc);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    @Test
    public void contextWithContextsAndObjects() throws Exception {
        InitialContext ctx = null;
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("org.osjava.sj.root", "src/test/resources/roots/contextWithContextsAndObjects");
            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
            env.put("org.osjava.sj.jndi.shared", "true");
            env.put("org.osjava.sj.delimiter", ".");
//            env.put("jndi.syntax.separator", "/");
            ctx = new InitialContext(env);
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }
    
//    @Test
//    public void encDoubleBoundBug() throws Exception {
//        InitialContext ctx = null;
//        try {
//            final Hashtable<String, String> env = new Hashtable<String, String>();
//            env.put("org.osjava.sj.root", "src/test/resources/roots/encDoubleBoundBug.properties");
//            env.put("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
//            env.put("org.osjava.sj.jndi.shared", "true");
//            env.put("org.osjava.sj.delimiter", "/");
//            JndiLoader loader = new JndiLoader(env);
//            Context jdbcCtx = (Context) ctx.lookup("jdbc");
//            assertNull(jdbcCtx);
//
//        }
//        finally {
//            if (ctx != null) {
//                ctx.close();
//            }
//        }
//    }
}

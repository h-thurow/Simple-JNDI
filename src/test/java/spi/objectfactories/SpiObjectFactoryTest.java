package spi.objectfactories;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.osjava.sj.MemoryContextFactory;
import org.osjava.sj.SimpleContextFactory;
import org.osjava.sj.loader.JndiLoader;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 03.07.17
 */
public class SpiObjectFactoryTest {

    private static InitialContext ctx;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void tearDown() throws Exception {
        ctx.close();
    }

    @Test
    public void demoFactory() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, SimpleContextFactory.class.getName());
        env.put("org.osjava.sj.root", "src/test/java/spi/objectfactories/root");
        env.put("org.osjava.sj.jndi.shared", "true");
        env.put("org.osjava.sj.delimiter", ".");
        env.put("jndi.syntax.separator", "/");
        env.put(Context.OBJECT_FACTORIES, org.apache.commons.dbcp2.BasicDataSourceFactory.class.getName() + ":" + DemoBeanFactory.class.getName());

        ctx = new InitialContext(env);

        DemoBean bean = (DemoBean) ctx.lookup("myBean");
        assertNotNull(bean);
        assertEquals(bean.getSize(), 186);

        DemoBean bean2 = (DemoBean) ctx.lookup("myBean");
        assertTrue(bean == bean2);

    }

    @Test
    public void demoFactory2() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put("org.osjava.sj.jndi.shared", "true");
        env.put("org.osjava.sj.delimiter", ".");
        env.put("jndi.syntax.separator", "/");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put(Context.OBJECT_FACTORIES, org.apache.commons.dbcp2.BasicDataSourceFactory.class.getName() + ":" + DemoBeanFactory.class.getName());

        Properties properties = new Properties();
        properties.setProperty("org.osjava.sj.myBean.type", DemoBean.class.getName());
        properties.setProperty("org.osjava.sj.myBean.size", "186");
        properties.setProperty("org.osjava.sj.myBean.fullName", "Holger Thurow");

        ctx = new InitialContext(env);
        JndiLoader loader = new JndiLoader(env);
        loader.load(properties, ctx);

        Object o = ctx.lookup("org/osjava/sj/myBean");
        assertEquals(DemoBean.class.getName(), o.getClass().getName());
    }

    @Test
    public void severalObjectFactories() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put("org.osjava.sj.jndi.shared", "true");
        env.put("org.osjava.sj.delimiter", ".");
        env.put("jndi.syntax.separator", "/");
        env.put("jndi.syntax.direction", "left_to_right");
        env.put(Context.OBJECT_FACTORIES, DemoBeanFactory.class.getName() + ":" + DemoBeanFactory2.class.getName());

        Properties properties = new Properties();

        // DemoBean1 configuration

        properties.setProperty("org.osjava.sj.myBean.type", DemoBean.class.getName());
        properties.setProperty("org.osjava.sj.myBean.size", "186");
        properties.setProperty("org.osjava.sj.myBean.fullName", "Holger Thurow");

        // DemoBean2 configuration

        properties.setProperty("org.osjava.sj.myBean2.type", DemoBean2.class.getName());
        properties.setProperty("org.osjava.sj.myBean2.inhabitants", "3754418");
        properties.setProperty("org.osjava.sj.myBean2.city", "Berlin");

        ctx = new InitialContext(env);
        JndiLoader loader = new JndiLoader(env);
        loader.load(properties, ctx);

        Object demoBean1 = ctx.lookup("org/osjava/sj/myBean");
        assertEquals(DemoBean.class.getName(), demoBean1.getClass().getName());

        Object demoBean2 = ctx.lookup("org/osjava/sj/myBean2");
        assertEquals(DemoBean2.class.getName(), demoBean2.getClass().getName());
    }

    /**
     * <p>To use ObjectFactory implementations, which do not check the type of object to construct but always return an object when called. See <a href="https://github.com/h-thurow/Simple-JNDI/pull/21">Add support for tomcat factory attribute #21</a>
     * </p><p>
     * Different from {@link #demoFactory2()} the ObjectFactory implementations are not applied to {@link Context#OBJECT_FACTORIES}, instead they are applied to the new javaxNamingSpiObjectFactory property.
     * </p>
     */
    @Test
    public void javaxNamingSpiObjectFactory() throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put("org.osjava.sj.jndi.shared", "true");
        env.put("org.osjava.sj.delimiter", ".");
        env.put("jndi.syntax.separator", "/");
        env.put("jndi.syntax.direction", "left_to_right");

        Properties properties = new Properties();

        // DemoBean1 configuration

        properties.setProperty("org.osjava.sj.myBean.type", DemoBean.class.getName());
        // The new factory property
        properties.setProperty("org.osjava.sj.myBean.javaxNamingSpiObjectFactory", DemoBeanFactory.class.getName());
        properties.setProperty("org.osjava.sj.myBean.size", "186");
        properties.setProperty("org.osjava.sj.myBean.fullName", "Holger Thurow");

        // DemoBean2 configuration

        properties.setProperty("org.osjava.sj.myBean2.type", DemoBean2.class.getName());
        // The new factory property
        properties.setProperty("org.osjava.sj.myBean2.javaxNamingSpiObjectFactory", DemoBeanFactory2.class.getName());
        properties.setProperty("org.osjava.sj.myBean2.inhabitants", "3754418");
        properties.setProperty("org.osjava.sj.myBean2.city", "Berlin");

        ctx = new InitialContext(env);
        JndiLoader loader = new JndiLoader(env);
        loader.load(properties, ctx);

        Object demoBean1 = ctx.lookup("org/osjava/sj/myBean");
        assertNotNull(demoBean1);
        assertEquals(DemoBean.class.getName(), demoBean1.getClass().getName());

        Object demoBean2 = ctx.lookup("org/osjava/sj/myBean2");
        assertNotNull(demoBean2);
        assertEquals(DemoBean2.class.getName(), demoBean2.getClass().getName());
    }

    @Test
    public void deferedObjectCreation() throws NamingException
    {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put("org.osjava.sj.jndi.shared", "true");
        env.put("org.osjava.sj.delimiter", ".");
        env.put("jndi.syntax.separator", "/");
        env.put("jndi.syntax.direction", "left_to_right");

        Properties properties = new Properties();

        properties.setProperty("org.osjava.sj.deferedObjectFactory.type", DeferedObjectFactory.class.getName());
        // The new factory property
        properties.setProperty("org.osjava.sj.deferedObjectFactory.javaxNamingSpiObjectFactory", DeferedObjectFactory.class.getName());

        ctx = new InitialContext(env);
        JndiLoader loader = new JndiLoader(env);
        // object creation is defered, so no exception is thrown on loading.
        loader.load(properties, ctx);

        // object creation is defered until lookup.
        thrown.expectCause(CoreMatchers.<Throwable>instanceOf(DeferedObjectFactoryException.class));
        ctx.lookup("org/osjava/sj/deferedObjectFactory");

    }
}
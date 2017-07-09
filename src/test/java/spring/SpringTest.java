package spring;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import static org.junit.Assert.assertTrue;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 07.06.17
 */
public class SpringTest {

    @Test
    public void injectDataSource() throws Exception {
        try {
            System.setProperty("org.osjava.sj.root", "src/test/resources/spring/root");
            System.setProperty("jndi.syntax.separator", "/");
            System.setProperty("org.osjava.sj.space", "java:comp/env");
            ApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/spring/context.xml");
            Dao dao = (Dao) context.getBean("dao");
            assertTrue(dao.getDataSource() != null);
        }
        finally {
            System.clearProperty("org.osjava.sj.root");
            System.clearProperty("jndi.syntax.separator");
            System.clearProperty("org.osjava.sj.space");
        }
    }
}
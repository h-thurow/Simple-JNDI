package eclipselink;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 25.04.20
 */
public class EclipselinkTest {

    private static EntityManager entityManager;
    private static Properties env = new Properties();

    @BeforeClass
    public static void beforeClass()
    {
        env.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
        env.setProperty("org.osjava.sj.root", "src/test/resources/roots/eclipselink");
        env.setProperty("org.osjava.sj.jndi.shared", "true");
        env.setProperty("org.osjava.sj.delimiter", "/");
        env.setProperty("org.osjava.sj.space", "java:comp");

        for (final String key : env.stringPropertyNames()) {
            System.setProperty(key, env.getProperty(key));
        }

        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("default");
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterClass
    public static void afterClass(){
        entityManager.close();
        for (final String key : env.stringPropertyNames()) {
            System.clearProperty(key);
        }
    }

    @Test
    public void createRead()
    {
        Person person = new Person();
        person.setFistName("Holger");
        person.setSurname("Thurow");
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(person);
        transaction.commit();

        transaction = entityManager.getTransaction();
        transaction.begin();
        Person person1 = entityManager.find(Person.class, 1);
        assertEquals(person, person1);
        transaction.commit();
    }
}
package eclipselink;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import static org.junit.Assert.assertEquals;

/**
 * To execute the test, uncomment marked lines in src/test/resources/jndi.properties.
 *
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 25.04.20
 */
public class TestEclipselink {

    private static EntityManager entityManager;

    @BeforeClass
    public static void beforeClass() throws NamingException
    {
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("default");
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterClass
    public static void afterClass(){
        entityManager.close();
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
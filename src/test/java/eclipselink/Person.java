package eclipselink;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 25.04.20
 */
@Entity
public class Person {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    private String fistName;
    private String surname;

    @NotNull
    public String getFistName()
    {
        return fistName;
    }

    public void setFistName(@NotNull String firstName)
    {
        this.fistName = firstName;
    }

    @NotNull
    public String getSurname()
    {
        return surname;
    }

    public void setSurname(@NotNull String surname)
    {
        this.surname = surname;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person)) {
            return false;
        }

        final Person person = (Person) o;

        if (id != person.id) {
            return false;
        }
        if (!fistName.equals(person.fistName)) {
            return false;
        }
        return surname.equals(person.surname);
    }

    @Override
    public int hashCode()
    {
        int result = id;
        result = 31 * result + fistName.hashCode();
        result = 31 * result + surname.hashCode();
        return result;
    }
}

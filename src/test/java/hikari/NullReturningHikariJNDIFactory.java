package hikari;

import com.zaxxer.hikari.HikariJNDIFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Created by hot on 08.07.17.
 */
public class NullReturningHikariJNDIFactory extends HikariJNDIFactory {
    @Override
    public synchronized Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        try {
            return super.getObjectInstance(obj, name, nameCtx, environment);
        }
        catch (NamingException e) {
            return null;
        }
    }
}

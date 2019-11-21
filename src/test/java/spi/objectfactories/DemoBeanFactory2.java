package spi.objectfactories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 03.07.17
 */
public class DemoBeanFactory2 implements ObjectFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoBeanFactory2.class);

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        LOGGER.debug("{} called.", this.getClass().getName());
        Reference ref = (Reference) obj;
        String type = (String) ref.get("type").getContent();
        if (type.equals(DemoBean2.class.getName())) {
            String city = (String) ref.get("city").getContent();
            int inhabitants = Integer.valueOf((String) ref.get("inhabitants").getContent());
            return new DemoBean2(city, inhabitants);
        }
        else {
            return null;
        }
    }
}

package spi.objectfactories;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * Created by hot on 03.07.17.
 */
public class DemoBeanFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference ref = (Reference) obj;
        String type = (String) ref.get("type").getContent();
        if (type.equals(DemoBean.class.getName())) {
            String fullName = (String) ref.get("fullName").getContent();
            int size = Integer.valueOf((String) ref.get("size").getContent());
            return new DemoBean(fullName, size);
        }
        else {
            return null;
        }
    }
}

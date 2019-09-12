package org.osjava.sj.loader;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.junit.Test;

public class SJPropertiesSubstitutionTest {

    @Test
    public void systemPropertySubstitution() throws Exception {
        Properties props = new Properties();
        props.put("key1", "${sys:test.property}");
        props.put("key2", "value2");
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        props.store(output, null);
        
        
        SJProperties sjProperties = new SJProperties();
        sjProperties.load(new ByteArrayInputStream(output.toByteArray()));
        
        assertEquals("should not be substituted", "${sys:test.property}", sjProperties.get("key1"));
        assertEquals("value2", sjProperties.get("key2"));
        
        System.setProperty("test.property", "value1");
        sjProperties = new SJProperties();
        sjProperties.load(new ByteArrayInputStream(output.toByteArray()));
        System.clearProperty("test.property");
        
        assertEquals("should be substituted", "value1", sjProperties.get("key1"));
        assertEquals("value2", sjProperties.get("key2"));        
        
        
        
    }

}

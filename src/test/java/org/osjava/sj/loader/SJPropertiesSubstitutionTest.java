package org.osjava.sj.loader;

import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class SJPropertiesSubstitutionTest {

    private final String sysName = "test.property";
    private String literalValue = "${sj.sys:test.property}";

    @After
    public void tearDown() throws Exception {
        System.clearProperty(sysName);
    }

    @Test
    public void systemPropertySubstitution() throws Exception {
        Properties props = new Properties();
        props.put("sysKey", literalValue);
        String value2 = "value2";
        props.put("key2", value2);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        props.store(output, null);
        
        
        SJProperties sjProperties = new SJProperties();
        sjProperties.load(new ByteArrayInputStream(output.toByteArray()));
        
        assertEquals("should not be substituted", literalValue, sjProperties.get("sysKey"));
        assertEquals(value2, sjProperties.get("key2"));

        String value1 = "value1";
        System.setProperty(sysName, value1);
        sjProperties = new SJProperties();
        sjProperties.load(new ByteArrayInputStream(output.toByteArray()));
        System.clearProperty(sysName);
        
        assertEquals("should be substituted", value1, sjProperties.get("sysKey"));
        assertEquals(value2, sjProperties.get("key2"));
    }

    @Test
    public void noSuchSystemProperty() throws IOException {
        Properties props = new Properties();
        String key = "key";
        props.put(key, literalValue);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        props.store(output, null);
        SJProperties sjProperties = new SJProperties();
        sjProperties.load(new ByteArrayInputStream(output.toByteArray()));
        assertEquals("should not be substituted", literalValue, sjProperties.get(key));
    }

    @Test
    public void systemPropertySet() throws IOException {

        System.setProperty(sysName, "system property read");

        Properties props = new Properties();
        String key = "key";
        props.put(key, literalValue);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        props.store(output, null);
        SJProperties sjProperties = new SJProperties();
        sjProperties.load(new ByteArrayInputStream(output.toByteArray()));
        assertEquals("should be substituted", "system property read", sjProperties.get(key));
    }

    /**
     * <p>Variable interpolation happens only once on initialization.</p>
     */
    @Test
    public void propertyChangedAfterInitialisation() {

        System.setProperty(sysName, "systemProperty");

        SJProperties sjProperties = new SJProperties();
        String key = "key";
        sjProperties.setProperty(key, literalValue);
        assertEquals("systemProperty", sjProperties.get(key));

        System.setProperty(sysName, "systemPropertyChanged");

        assertEquals("systemProperty", sjProperties.get(key));
    }
}

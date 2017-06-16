package org.osjava.sj.loader.util;

import org.junit.Test;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

public class PropertiesTest {

    @Test
    public void testReplaceQuotes() throws Exception {
        final CustomProperties properties = new CustomProperties();
        final FileInputStream stream = new FileInputStream(
                "src/test/resources/roots/shareContext1/directory1/directory1_file1.properties");
        properties.load(stream);
        stream.close();
        assertEquals("Mit blanks", properties.getProperty("withBlanks"));
        assertEquals("Mit blanks", properties.getProperty("singleQuoted"));
        assertEquals("'quotes' \"inside\"", properties.getProperty("quotesInside"));
        assertEquals(" ", properties.getProperty("blank"));
    }
}

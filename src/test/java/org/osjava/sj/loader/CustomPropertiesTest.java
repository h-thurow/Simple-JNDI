package org.osjava.sj.loader;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class CustomPropertiesTest {

    /**
     * "Note that single-quotes or double-quotes are considered part of the string." See <a href=https://en.wikipedia.org/wiki/.properties>Wikipedia .properties</a>.
     */
    @Test
    public void quotes() throws Exception {
        final CustomProperties properties = new CustomProperties();
        final FileInputStream stream = new FileInputStream(
                "src/test/resources/roots/shareContext1/directory1/directory1_file1.properties");
        properties.load(stream);
        stream.close();
        assertEquals("\"Mit blanks\"", properties.getProperty("withBlanks"));
        assertEquals("'Mit blanks'", properties.getProperty("singleQuoted"));
        assertEquals("\"'quotes' \"inside\"\"", properties.getProperty("quotesInside"));
        assertEquals("\" \"", properties.getProperty("blank"));
    }

    @Test
    public void whitespace() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(" \tkey = \tvalue".getBytes());

        Properties origProps = new Properties();
        origProps.load(in);
        assertEquals("value", origProps.getProperty("key"));

        CustomProperties props = new CustomProperties();
        in.reset();
        props.load(in);
        assertEquals("value", props.getProperty("key"));
    }

    /**
     * "Trailing space is significant and presumed to be trimmed as required by the consumer." See <a href=https://en.wikipedia.org/wiki/.properties>Wikipedia .properties</a>.
     */
    @Test
    public void trailingWhitespace() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream("key = value   ".getBytes());

        Properties origProps = new Properties();
        origProps.load(in);
        assertEquals("value   ", origProps.getProperty("key"));

        CustomProperties props = new CustomProperties();
        in.reset();
        props.load(in);
        assertEquals("value   ", props.getProperty("key"));
    }

    /**
     * "there is no need to preceede the value characters =, and : by a backslash." See <a href=https://en.wikipedia.org/wiki/.properties>Wikipedia .properties</a>.
     *
     */
    @Test
    public void colonSeparated() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream("key : value: 1".getBytes());

        Properties origProps = new Properties();
        origProps.load(in);
        assertEquals("value: 1", origProps.getProperty("key"));

        CustomProperties props = new CustomProperties();
        in.reset();
        props.load(in);
        assertEquals("value: 1", props.getProperty("key"));
    }

    @Test
    public void whitespaceWithinKeys() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(" \tmy\\ key = \tvalue".getBytes());

        Properties origProps = new Properties();
        origProps.load(in);
        assertEquals("value", origProps.getProperty("my key"));

        CustomProperties props = new CustomProperties();
        in.reset();
        props.load(in);
        assertEquals("value", props.getProperty("my key"));
    }

    @Test
    public void multiLineValue() throws Exception {
        String multiLine = "first line \\n     second line";
        ByteArrayInputStream in = new ByteArrayInputStream(("key = " + multiLine).getBytes());

        Properties origProps = new Properties();
        origProps.load(in);
        String expected = "first line \n     second line";
        assertEquals(expected, origProps.getProperty("key"));
    }

    /**
     * See issue#4 <a href=https://github.com/h-thurow/Simple-JNDI/issues/4>'#' as a value of a property</a>.
     * <p>
     * "Comment lines in .properties files are denoted by the number sign (#) or the exclamation mark (!) as the first non blank character, in which all remaining text on that line is ignored. The backwards slash is used to escape a character." See https://en.wikipedia.org/wiki/.properties>Wikipedia .properties.
     */
    @Test
    public void hashInValue() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream("key = value#with#hash".getBytes());
        Properties origProps = new Properties();
        origProps.load(in);
        assertEquals("value#with#hash", origProps.getProperty("key"));

        CustomProperties props = new CustomProperties();
        in.reset();
        props.load(in);
        assertEquals("value#with#hash", props.getProperty("key"));
    }
}

package org.osjava.sj.loader.convert;

import org.junit.Test;
import org.osjava.sj.BeanWithSupportedSetters;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 29.11.17
 */
public class BeanConverterTest {

    @Test
    public void propertiesOmitted() throws Exception {
        BeanConverter converter = new BeanConverter();
        Properties properties = new Properties();
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals(null, bean.getString());
        assertEquals(null, bean.getCharSequence());
        assertEquals(false, bean.getBooleanPrimitive());
        assertEquals(null, bean.getBooleanObject());
        assertEquals(0, bean.getBytePrimitive());
        assertEquals(null, bean.getByteObject());
        assertEquals(0, (int)bean.getCharacterPrimitive());
        assertEquals(null, bean.getCharacterObject());
        assertEquals(0, bean.getShortPrimitive());
        assertEquals(null, bean.getShortObject());
        assertEquals(0, bean.getIntegerPrimitive());
        assertEquals(null, bean.getIntegerObject());
        assertEquals(0, bean.getLongPrimitive());
        assertEquals(null, bean.getLongObject());
        assertEquals(0, bean.getFloatPrimitive(), 0);
        assertEquals(null, bean.getFloatObject());
        assertEquals(0, bean.getDoublePrimitive(), 0);
        assertEquals(null, bean.getDoubleObject());
        assertEquals(null, bean.getBigDecimal());
        assertEquals(null, bean.getBigInteger());
        assertEquals(null, bean.getLocale());
        assertEquals(null, bean.getRoundingMode());
//        SimpleDateFormat format = new SimpleDateFormat();
//        format.applyPattern("yyyy-MM-dd'T'hh:mm");
//        Date expectedDate = format.parse("2017-11-21T08:30");
        assertEquals(null, bean.getUtilDate());
        assertEquals(null, bean.getSqlDate());
        assertEquals(null, bean.getTime());
        assertEquals(null, bean.getTimestamp());

    }

    @Test
    public void stringValueOmitted() throws Exception {
        BeanConverter converter = new BeanConverter();
        Properties properties = new Properties();
        properties.setProperty("string", "");
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals("", bean.getString());
    }

    @Test
    public void valuesOmitted() throws Exception {
        BeanConverter converter = new BeanConverter();
        Properties properties = new Properties();
        properties.setProperty("string", "");
        properties.setProperty("charSequence", "");
        properties.setProperty("booleanPrimitive", "");
        properties.setProperty("booleanObject", "");
        properties.setProperty("byteObject", "");
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals("", bean.getString());
        assertEquals("", bean.getCharSequence());
        assertEquals(false, bean.getBooleanPrimitive());
        assertEquals(null, bean.getBooleanObject());
        assertEquals(null, bean.getByteObject());
    }

    @Test
    public void trailingWhitespaceStringField() throws Exception {
        BeanConverter converter = new BeanConverter();
        Properties properties = new Properties();
        properties.setProperty("string", "trailing tab  ");
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals("trailing tab  ", bean.getString());
    }

    @Test
    public void trailingWhitespaceIntField() throws Exception {
        BeanConverter converter = new BeanConverter();
        Properties properties = new Properties();
        properties.setProperty("integerPrimitive", "1 ");
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals(1, bean.getIntegerPrimitive());
    }
}
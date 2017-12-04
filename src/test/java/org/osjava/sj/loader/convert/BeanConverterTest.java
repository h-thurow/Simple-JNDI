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
    public void propertiesOmitted() {
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
    public void toType() {
        assertEquals(null, BeanConverter.toShort("", null));
        assertEquals(null, BeanConverter.toCharacter("", null));
        assertEquals(null, BeanConverter.toByte("", null));
        assertEquals(null, BeanConverter.toInteger("", null));
        assertEquals(null, BeanConverter.toLong("", null));
    }

    @Test
    public void valuesOmitted() {
        Properties properties = new Properties();
        properties.setProperty("string", "");
        properties.setProperty("charSequence", "");
        properties.setProperty("booleanPrimitive", "");
        properties.setProperty("booleanObject", "");
        properties.setProperty("bytePrimitive", "");
        properties.setProperty("byteObject", "");
        properties.setProperty("characterPrimitive", "");
        properties.setProperty("characterObject", "");
        properties.setProperty("shortPrimitive", "");
        properties.setProperty("shortObject", "");
        properties.setProperty("integerPrimitive", "");
        properties.setProperty("integerObject", "");
        properties.setProperty("longPrimitive", "");
        properties.setProperty("longObject", "");
        properties.setProperty("floatPrimitive", "");
        properties.setProperty("floatObject", "");
        properties.setProperty("doublePrimitive", "");
        properties.setProperty("doubleObject", "");
        properties.setProperty("bigDecimal", "");
        properties.setProperty("bigInteger", "");
        properties.setProperty("locale", "");
        properties.setProperty("roundingMode", "");
        properties.setProperty("utilDate", "");
        properties.setProperty("sqlDate", "");
        properties.setProperty("time", "");
        properties.setProperty("timestamp", "");
        BeanConverter converter = new BeanConverter();
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals("", bean.getString());
        assertEquals("", bean.getCharSequence());
        assertEquals(false, bean.getBooleanPrimitive());
        assertEquals(null, bean.getBooleanObject());
        assertEquals(0, bean.getBytePrimitive());
        assertEquals(null, bean.getByteObject());
        assertEquals(0, bean.getCharacterPrimitive());
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
        assertEquals(null, bean.getUtilDate());
        assertEquals(null, bean.getSqlDate());
        assertEquals(null, bean.getTime());
        assertEquals(null, bean.getTimestamp());
    }

    @Test
    public void trailingWhitespaceStringField() {
        BeanConverter converter = new BeanConverter();
        Properties properties = new Properties();
        properties.setProperty("string", "trailing tab  ");
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals("trailing tab  ", bean.getString());
    }

    @Test
    public void trailingWhitespaceIntField() {
        BeanConverter converter = new BeanConverter();
        Properties properties = new Properties();
        properties.setProperty("integerPrimitive", "1 ");
        BeanWithSupportedSetters bean = (BeanWithSupportedSetters) converter.convert(properties, BeanWithSupportedSetters.class.getName());
        assertEquals(1, bean.getIntegerPrimitive());
    }

}
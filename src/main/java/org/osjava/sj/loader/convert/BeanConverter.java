/*
 * Copyright (c) 2005, Henri Yandell
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * + Neither the name of Simple-JNDI nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.osjava.sj.loader.convert;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Create an object using its empty constructor, then
 * call setXxx for each pseudo property. Only String
 * properties are supported.
 * <p>
 * <pre>
 * Foo.name=Arthur
 * Foo.answer=42
 * Foo.type=com.example.Person
 * Foo.converter=org.osjava.sj.loader.convert.BeanConverter
 * </pre>
 *
 * @author Henri Yandell, Jarrad Waterloo
 */
public class BeanConverter implements ConverterIF {

    private static Logger LOGGER = LoggerFactory.getLogger(BeanConverter.class);

    private static final Set<String> trueValues;
    private static final Set<String> falseValues;

    private static final int OLD_ENUM_STYLE = Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC;

    private static final List<SimpleDateFormat> dateTimeFormats;
    private static final List<SimpleDateFormat> dateFormats;
    private static final List<SimpleDateFormat> timeFormats;

    static {
        trueValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        falseValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Collections.addAll(trueValues, "true", "t", "yes", "y", "on", "1", "x", "-1");
        Collections.addAll(falseValues, "false", "f", "no", "n", "off", "0", "");
        dateTimeFormats = new ArrayList<SimpleDateFormat>();
        Collections.addAll(dateTimeFormats, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"),
                new SimpleDateFormat("yyyy-MM-dd"));
        for (SimpleDateFormat sdf : dateTimeFormats) {
            sdf.setLenient(false);
        }
        dateFormats = new ArrayList<SimpleDateFormat>();
        Collections.addAll(dateFormats, new SimpleDateFormat("yyyy-MM-dd"));
        for (SimpleDateFormat sdf : dateFormats) {
            sdf.setLenient(false);
        }
        timeFormats = new ArrayList<SimpleDateFormat>();
        Collections.addAll(timeFormats, new SimpleDateFormat("HH:mm:ss.SSSZ"),
                new SimpleDateFormat("HH:mm:ss.SSSXXX"),
                new SimpleDateFormat("HH:mm:ss"),
                new SimpleDateFormat("HH:mm"));
        for (SimpleDateFormat sdf : timeFormats) {
            sdf.setLenient(false);
        }
    }

    private static boolean convertStringToBooleanPrimitive(String value) {
        if (trueValues.contains(value)) {
            return true;
        }
        if (falseValues.contains(value)) {
            return false;
        }
        throw new RuntimeException("The value, \"" + value + "\", can not be converted to a boolean.");
    }

    private static PropertyDescriptor findPropertyDescriptorWithSetter(Class<?> clazz, String propertyName) {
        try {
            BeanInfo info = Introspector.getBeanInfo(clazz, Object.class);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if (pd.getName().equalsIgnoreCase(propertyName)) {
                    Method setter = pd.getWriteMethod();
                    if (setter != null) {
                        return pd;
                    }
                }
            }
        }
        catch (IntrospectionException ie) {
            throw new RuntimeException("Unable to find the properties of the bean of type, \"" + clazz + "\".", ie);
        }
        return null;
    }

    private static Field findOldStyleEnumField(Class<?> clazz, String value) {
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (clazz.equals(field.getType()) && field.getName().equalsIgnoreCase(value.trim()) && ((field.getModifiers() & OLD_ENUM_STYLE) == OLD_ENUM_STYLE)) {
                return field;
            }
        }
        return null;
    }

    private static Object convert(String value, Class<?> toWhat) {
        if (String.class.equals(toWhat)) {
            return value;// as is, ie. no trimming
        }
        else if (CharSequence.class.equals(toWhat)) {
            return value;// as is, ie. no trimming
        }
        else {
            String trimmedValue = value.trim();
            if (value.isEmpty() && !toWhat.isPrimitive()) {
                return null;
            }
            else {
                if (trimmedValue.length() == 0 && !toWhat.isPrimitive()) {
                    return null;
                }
                else if (boolean.class.equals(toWhat) || Boolean.class.equals(toWhat)) {
                    return convertStringToBooleanPrimitive(trimmedValue);
                }
                else if (byte.class.equals(toWhat)) {
                    return toByte(trimmedValue, (byte) 0);
                }
                else if (Byte.class.equals(toWhat)) {
                    return Byte.parseByte(trimmedValue);
                }
                else if (char.class.equals(toWhat)) {
                    return toCharacter(trimmedValue, (char) Character.UNASSIGNED);
                }
                else if (Character.class.equals(toWhat)) {
                    return value.charAt(0);
                }
                else if (short.class.equals(toWhat)) {
                    return toShort(trimmedValue, (short) 0);
                }
                else if (Short.class.equals(toWhat)) {
                    return Short.parseShort(trimmedValue);
                }
                else if (int.class.equals(toWhat)) {
                    return toInteger(trimmedValue, 0);
                }
                else if (Integer.class.equals(toWhat)) {
                    return Integer.parseInt(trimmedValue);
                }
                else if (long.class.equals(toWhat)) {
                    return toLong(trimmedValue, 0L);
                }
                else if (Long.class.equals(toWhat)) {
                    return Long.parseLong(trimmedValue);
                }
                else if (float.class.equals(toWhat)) {
                    return toFloat(trimmedValue, 0F);
                }
                else if (Float.class.equals(toWhat)) {
                    return Float.parseFloat(trimmedValue);
                }
                else if (double.class.equals(toWhat)) {
                    return toDouble(trimmedValue, 0D);
                }
                else if (Double.class.equals(toWhat)) {
                    return Double.parseDouble(value);
                }
                else if (Date.class.equals(toWhat)) {
                    value = trimmedValue;
                    for (SimpleDateFormat sdf : dateTimeFormats) {
                        try {
                            return sdf.parse(value);
                        }
                        catch (ParseException ignored) {
                        }
                    }
                    throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.util.Date\".");
                }
                else if (java.sql.Date.class.equals(toWhat)) {
                    value = trimmedValue;
                    for (SimpleDateFormat sdf : dateFormats) {
                        try {
                            return new java.sql.Date(sdf.parse(value).getTime());
                        }
                        catch (ParseException ignored) {
                        }
                    }
                    throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.sql.Date\".");
                }
                else if (java.sql.Time.class.equals(toWhat)) {
                    value = trimmedValue;
                    for (SimpleDateFormat sdf : timeFormats) {
                        try {
                            return new java.sql.Time(sdf.parse(value).getTime());
                        }
                        catch (ParseException ignored) {
                        }
                    }
                    throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.sql.Time\".");
                }
                else if (java.sql.Timestamp.class.equals(toWhat)) {
                    value = trimmedValue;
                    for (SimpleDateFormat sdf : dateTimeFormats) {
                        try {
                            return new java.sql.Timestamp(sdf.parse(value).getTime());
                        }
                        catch (ParseException ignored) {
                        }
                    }
                    throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.sql.Timestamp\".");
                }
                else if (toWhat.isEnum()) {
                    //return Enum.valueOf(toWhat, value.trim());
                    value = trimmedValue;
                    Object[] enumConstants = toWhat.getEnumConstants();
                    for (Object oe : enumConstants) {
                        Enum e = (Enum) oe;
                        if (e.name().equals(value)) {
                            return e;
                        }
                    }
                    for (Object oe : enumConstants) {
                        Enum e = (Enum) oe;
                        if (e.name().equalsIgnoreCase(value)) {
                            return e;
                        }
                    }
                    throw new RuntimeException("The value ,\"" + value + "\", is not a enumeration on enum " + toWhat);
                }
                else {
                    try {
                        Field field = findOldStyleEnumField(toWhat, trimmedValue);
                        if (field == null) {
                            Constructor constructor = toWhat.getConstructor(String.class);
                            return constructor.newInstance(value);
                        }
                        else {
                            return field.get(null);
                        }
                    }
                    catch (NoSuchMethodException e) {
                        throw new RuntimeException("Unable to find (String) constructor on class: " + toWhat, e);
                    }
                    catch (InstantiationException e) {
                        throw new RuntimeException("Unable to instantiate class: " + toWhat, e);
                    }
                    catch (IllegalAccessException e) {
                        throw new RuntimeException("Unable to access class: " + toWhat, e);
                    }
                    catch (InvocationTargetException e) {
                        throw new RuntimeException("Unable to invoke (String) constructor on class: " + toWhat, e);
                    }
                }
            }
        }
    }

    @Nullable
    static Short toShort(String value, @Nullable Short defaultValue) {
        return !value.isEmpty() ? (Short)Short.parseShort(value) : defaultValue;
    }

    @Nullable
    static Character toCharacter(String value, @Nullable Character defaultValue) {
        return !value.isEmpty() ? (Character) value.charAt(0) : defaultValue;
    }

    @Nullable
    static Byte toByte(String value, @Nullable Byte defaultValue) {
        return !value.isEmpty() ? (Byte)Byte.parseByte(value) : defaultValue;
    }

    @Nullable
    static Integer toInteger(String value, @Nullable Integer defaultValue) {
        return !value.isEmpty() ? (Integer)Integer.parseInt(value) : defaultValue;
    }

    @Nullable
    static Long toLong(String value, @Nullable Long defaultValue) {
        return !value.isEmpty() ? (Long)Long.parseLong(value) : defaultValue;
    }

    @Nullable
    static Float toFloat(String value, @Nullable Float defaultValue) {
        return !value.isEmpty() ? (Float)Float.parseFloat(value) : defaultValue;
    }

    @Nullable
    static Double toDouble(String value, @Nullable Double defaultValue) {
        return !value.isEmpty() ? (Double)Double.parseDouble(value) : defaultValue;
    }

    public Object convert(Properties properties, String type) {
        String value = properties.getProperty("");

        if (value != null) {
            throw new RuntimeException("Specify the value as a pseudo property as Beans have empty constructors");
        }

        String methodName = null;

        try {
            Class c = Class.forName(type);
            Object bean = c.newInstance();
            Iterator itr = properties.keySet().iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if ("converter".equals(key) || "type".equals(key)) {
                    continue;
                }
                Object property = properties.get(key);
                if (property instanceof String) {
                    /*
                    methodName = "set" + Character.toTitleCase(key.charAt(0)) + key.substring(1);
                    Method m = c.getMethod(methodName, String.class);
                    m.invoke(bean, property);
                    */
                    String strValue = (String) property;
                    PropertyDescriptor pd = findPropertyDescriptorWithSetter(c, key);
                    if (pd == null) {
                        throw new RuntimeException("No property with the name of \"" + key + "\" could be found on object of type, \"" + c + "\".");
                    }
                    else {
                        Class<?> propertyType = pd.getPropertyType();
                        Method setter = pd.getWriteMethod();
                        setter.invoke(bean, convert(strValue, propertyType));
                    }
                }
                else if (property instanceof List) {
                    List list = (List) property;
                    int sz = list.size();
                    key = "add" + Character.toTitleCase(key.charAt(0)) + key.substring(1);
                    Method m = c.getMethod(key, Integer.TYPE, String.class);
                    for (int i = 0; i < sz; i++) {
                        Object item = list.get(i);
                        if (item instanceof String) {
                            m.invoke(bean, new Integer(i), item);
                        }
                        else {
                            LOGGER.error("Processing List: properties={} type={} property={} key={} item={}", properties, type, property, key, item);
                            throw new RuntimeException("Only Strings and Lists of String are supported");
                        }
                    }
                }
                else {
                    LOGGER.error("Processing List: properties={} type={} methodName={} property={} key={}", properties, type, methodName, property, key);
                    throw new RuntimeException("Only Strings and Lists of Strings are supported");
                }
            }
            return bean;
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find class: " + type, e);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find method " + methodName + " on class: " + type, e);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate class: " + type, e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access class: " + type, e);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException("Unable to pass argument to class: " + type, e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException("Unable to invoke (String) constructor on class: " + type, e);
        }

    }

}

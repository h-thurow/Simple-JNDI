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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.osjava.StringsToTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Create an object using its empty constructor.
 * <pre>
 * name=Arthur
 * answer=42
 * type=com.example.Person
 * converter=org.osjava.sj.loader.convert.BeanConverter
 * </pre>
 *
 * @author Henri Yandell, Jarrad Waterloo, Holger Thurow
 */
public class BeanConverter implements ConverterIF {

    private static Logger LOGGER = LoggerFactory.getLogger(BeanConverter.class);

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

    private static Object convert(String value, Class<?> toWhat) {
        Object obj;
        if (String.class.equals(toWhat)) {
            obj = value;// as is, ie. no trimming
        }
        else if (CharSequence.class.equals(toWhat)) {
            obj = value;// as is, ie. no trimming
        }
        else {
            String trimmedValue = value.trim();
            if (trimmedValue.isEmpty() && !toWhat.isPrimitive()) {
                obj = null;
            }
            else {
                if (trimmedValue.length() == 0 && !toWhat.isPrimitive()) {
                    obj = null;
                }
                else if (boolean.class.equals(toWhat) || Boolean.class.equals(toWhat)) {
                    obj = StringsToTypes.toBoolean(trimmedValue);
                }
                else if (byte.class.equals(toWhat) || Byte.class.equals(toWhat)) {
                    obj = NumberUtils.toByte(trimmedValue);
                }
                else if (char.class.equals(toWhat) || Character.class.equals(toWhat)) {
                    obj = StringsToTypes.toCharacter(trimmedValue);
                }
                else if (short.class.equals(toWhat) || Short.class.equals(toWhat)) {
                    obj = NumberUtils.toShort(trimmedValue);
                }
                else if (int.class.equals(toWhat) || Integer.class.equals(toWhat)) {
                    obj = NumberUtils.toInt(trimmedValue);
                }
                else if (long.class.equals(toWhat) || Long.class.equals(toWhat)) {
                    obj = NumberUtils.toLong(trimmedValue);
                }
                else if (float.class.equals(toWhat) || Float.class.equals(toWhat)) {
                    obj = NumberUtils.toFloat(trimmedValue);
                }
                else if (double.class.equals(toWhat) || Double.class.equals(toWhat)) {
                    obj = NumberUtils.toDouble(trimmedValue);
                }
                else if (Date.class.equals(toWhat)) {
                    obj = StringsToTypes.toDate(trimmedValue);
                }
                else if (java.sql.Date.class.equals(toWhat)) {
                    obj = StringsToTypes.toSqlDate(trimmedValue);
                }
                else if (java.sql.Time.class.equals(toWhat)) {
                    obj = StringsToTypes.toTime(trimmedValue);
                }
                else if (java.sql.Timestamp.class.equals(toWhat)) {
                    obj = StringsToTypes.toTimestamp(trimmedValue);
                }
                else if (toWhat.isEnum()) {
                    obj = StringsToTypes.toEnum(toWhat, trimmedValue);
                }
                else {
                    obj = StringsToTypes.getOldStyleEnumFieldValue(toWhat, trimmedValue);
                    if (obj == ObjectUtils.NULL) {
                        obj = StringsToTypes.callConstructor(toWhat, value);
                    }
                }
            }
        }
        return obj;
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
            for (Object o : properties.keySet()) {
                String key = (String) o;
                if ("converter".equals(key) || "type".equals(key)) {
                    continue;
                }
                Object property = properties.get(key);
                if (property instanceof String) {
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
                            m.invoke(bean, i, item);
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

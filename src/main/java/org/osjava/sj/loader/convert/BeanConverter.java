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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import java.text.SimpleDateFormat;
import java.text.ParseException;

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
 */
public class BeanConverter implements ConverterIF {

    private static Logger LOGGER = LoggerFactory.getLogger(BeanConverter.class);

    private static final Set<String> trueValues;
    private static final Set<String> falseValues;

    public static final int OLD_ENUM_STYLE = Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC;

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
        for(SimpleDateFormat sdf : dateTimeFormats) {
            sdf.setLenient(false);
        }
        dateFormats = new ArrayList<SimpleDateFormat>();
        Collections.addAll(dateFormats, new SimpleDateFormat("yyyy-MM-dd"));
        for(SimpleDateFormat sdf : dateFormats) {
            sdf.setLenient(false);
        }
        timeFormats = new ArrayList<SimpleDateFormat>();
        Collections.addAll(timeFormats, new SimpleDateFormat("HH:mm:ss.SSSZ"),
            new SimpleDateFormat("HH:mm:ss.SSSXXX"),
            new SimpleDateFormat("HH:mm:ss"),
            new SimpleDateFormat("HH:mm"));
        for(SimpleDateFormat sdf : timeFormats) {
            sdf.setLenient(false);
        }
    }

    private static boolean convertStringToBooleanPrimitive(String value) {
        if(trueValues.contains(value)) {
            return true;
        }
        if(falseValues.contains(value)) {
            return false;
        }
        throw new RuntimeException("The value, \"" + value + "\", can not be converted to a boolean.");
    }

    private static PropertyDescriptor findPropertyDescriptorWithSetter(Class<?> clazz, String propertyName) {
        try {
            BeanInfo info = Introspector.getBeanInfo(clazz, Object.class);
            for(PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if(pd.getName().equalsIgnoreCase(propertyName)) {
                    Method setter = pd.getWriteMethod();
                    if(setter != null) {
                        return pd;
                    }
                }
            }
        } catch(IntrospectionException ie) {
            throw new RuntimeException("Unable to find the properties of the bean of type, \"" + clazz + "\".", ie);
        }
        return null;
    }

    private static Field findOldStyleEnumField(Class<?> clazz, String value) {
        Field[] fields = clazz.getFields();
        for(Field field : fields) {
            if(clazz.equals(field.getType()) && field.getName().equalsIgnoreCase(value.trim()) && ((field.getModifiers() & OLD_ENUM_STYLE) == OLD_ENUM_STYLE)) {
                return field;
            }
        }
        return null;
    }

    private static Object convert(String value, Class<?> toWhat) {
        if(String.class.equals(toWhat)) {
            return value;// as is, ie. no trimming
        } else if(CharSequence.class.equals(toWhat)) {
            return value;// as is, ie. no trimming
        } else if(value == null) {
            if(toWhat.isPrimitive()) {
                throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"" + toWhat + "\".");
            } else {
                return null;
            }
        } else if(value.trim().length() == 0) {
            if(toWhat.isPrimitive()) {
                throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"" + toWhat + "\".");
            } else {
                return null;
            }
        } else if(boolean.class.equals(toWhat) || Boolean.class.equals(toWhat)) {
            return convertStringToBooleanPrimitive(value.trim());
        } else if(byte.class.equals(toWhat) || Byte.class.equals(toWhat)) {
            return Byte.parseByte(value.trim());
        } else if(char.class.equals(toWhat) || Character.class.equals(toWhat)) {
            if(value.trim().length() == 1) {
                return value.trim().charAt(0);
            } else {
                throw new RuntimeException("The value, \"" + value + "\", could not be converted to a character.");
            }
        } else if(short.class.equals(toWhat) || Short.class.equals(toWhat)) {
            return Short.parseShort(value.trim());
        } else if(int.class.equals(toWhat) || Integer.class.equals(toWhat)) {
            return Integer.parseInt(value.trim());
        } else if(long.class.equals(toWhat) || Long.class.equals(toWhat)) {
            return Long.parseLong(value.trim());
        } else if(float.class.equals(toWhat) || Float.class.equals(toWhat)) {
            return Float.parseFloat(value.trim());
        } else if(double.class.equals(toWhat) || Double.class.equals(toWhat)) {
            return Double.parseDouble(value.trim());
        } else if(java.util.Date.class.equals(toWhat)) {
            value = value.trim();
            for(SimpleDateFormat sdf : dateTimeFormats) {
                try {
                    return sdf.parse(value);
                } catch(ParseException pe) {
                }
            }
            throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.util.Date\".");
        } else if(java.sql.Date.class.equals(toWhat)) {
            value = value.trim();
            for(SimpleDateFormat sdf : dateFormats) {
                try {
                    return new java.sql.Date(sdf.parse(value).getTime());
                } catch(ParseException pe) {
                }
            }
            throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.sql.Date\".");
        } else if(java.sql.Time.class.equals(toWhat)) {
            value = value.trim();
            for(SimpleDateFormat sdf : timeFormats) {
                try {
                    return new java.sql.Time(sdf.parse(value).getTime());
                } catch(ParseException pe) {
                }
            }
            throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.sql.Time\".");
        } else if(java.sql.Timestamp.class.equals(toWhat)) {
            value = value.trim();
            for(SimpleDateFormat sdf : dateTimeFormats) {
                try {
                    return new java.sql.Timestamp(sdf.parse(value).getTime());
                } catch(ParseException pe) {
                }
            }
            throw new RuntimeException("The value, \"" + value + "\", could not be converted to a \"java.sql.Timestamp\".");
        } else if(toWhat.isEnum()) {
            //return Enum.valueOf(toWhat, value.trim());
            value = value.trim();
            Object[] enumConstants = toWhat.getEnumConstants();
            for(Object oe : enumConstants) {
                Enum e = (Enum)oe;
                if(e.name().equals(value)) {
                    return e;
                }
            }
            for(Object oe : enumConstants) {
                Enum e = (Enum)oe;
                if(e.name().equalsIgnoreCase(value)) {
                    return e;
                }
            }
            throw new RuntimeException("The value ,\"" + value + "\", is not a enumeration on enum " + toWhat);
        } else {
            try {
                Field field = findOldStyleEnumField(toWhat, value.trim());
                if(field == null) {
                    Constructor constructor = toWhat.getConstructor(String.class);
                    return constructor.newInstance(value);
                } else {
                    return field.get(null);
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to find (String) constructor on class: " + toWhat, e);
            } catch (InstantiationException e) {
                throw new RuntimeException("Unable to instantiate class: " + toWhat, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access class: " + toWhat, e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Unable to invoke (String) constructor on class: " + toWhat, e);
            }
        }
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
                    String strValue = (String)property;
                    PropertyDescriptor pd = findPropertyDescriptorWithSetter(c, key);
                    if(pd == null) {
                        throw new RuntimeException("No property with the name of \"" + key + "\" could be found on object of type, \"" + c + "\".");
                    } else {
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

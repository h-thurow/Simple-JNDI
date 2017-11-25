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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

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

    static {
        trueValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        falseValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Collections.addAll(trueValues, "true", "t", "yes", "y", "on", "1", "x", "-1");
        Collections.addAll(falseValues, "false", "f", "no", "n", "off", "0", "");
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
                    try {
                        String strValue = (String)property;
                        BeanInfo info = Introspector.getBeanInfo( c, Object.class );
                        for ( PropertyDescriptor pd : info.getPropertyDescriptors() ) {
                            if(pd.getName().equalsIgnoreCase(key)) {
                                Method setter = pd.getWriteMethod();
                                if(setter == null) {
                                    throw new RuntimeException("The property, \"" + key + "\", on class, \"" + c + "\", was not defined.");
                                } else {
                                    Class<?> propertyType = pd.getPropertyType();
                                    if(String.class.equals(propertyType)) {
                                        setter.invoke(bean, strValue);
                                    } else if(CharSequence.class.equals(propertyType)) {
                                        setter.invoke(bean, strValue);
                                    } else if(boolean.class.equals(propertyType)) {
                                        setter.invoke(bean, convertStringToBooleanPrimitive(strValue));
                                    } else if(Boolean.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else {
                                            setter.invoke(bean, convertStringToBooleanPrimitive(strValue));
                                        }
                                    } else if(byte.class.equals(propertyType)) {
                                        setter.invoke(bean, Byte.parseByte(strValue));
                                    } else if(Byte.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else {
                                            setter.invoke(bean, Byte.parseByte(strValue));
                                        }
                                    } else if(char.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            throw new RuntimeException("The value, \"" + strValue + "\", could not be converted to a character.");
                                        } else if(strValue.trim().length() == 1) {
                                            setter.invoke(bean, strValue.charAt(0));
                                        } else {
                                            throw new RuntimeException("The value, \"" + strValue + "\", could not be converted to a character.");
                                        }
                                    } else if(Character.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 1) {
                                            setter.invoke(bean, strValue.charAt(0));
                                        } else {
                                            throw new RuntimeException("The value, \"" + strValue + "\", could not be converted to a character.");
                                        }
                                    } else if(short.class.equals(propertyType)) {
                                        setter.invoke(bean, Short.parseShort(strValue));
                                    } else if(Short.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else {
                                            setter.invoke(bean, Short.parseShort(strValue));
                                        }
                                    } else if(int.class.equals(propertyType)) {
                                        setter.invoke(bean, Integer.parseInt(strValue));
                                    } else if(Integer.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else {
                                            setter.invoke(bean, Integer.parseInt(strValue));
                                        }
                                    } else if(long.class.equals(propertyType)) {
                                        setter.invoke(bean, Long.parseLong(strValue));
                                    } else if(Long.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else {
                                            setter.invoke(bean, Long.parseLong(strValue));
                                        }
                                    } else if(float.class.equals(propertyType)) {
                                        setter.invoke(bean, Float.parseFloat(strValue));
                                    } else if(Float.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else {
                                            setter.invoke(bean, Float.parseFloat(strValue));
                                        }
                                    } else if(double.class.equals(propertyType)) {
                                        setter.invoke(bean, Double.parseDouble(strValue));
                                    } else if(Double.class.equals(propertyType)) {
                                        if(strValue == null) {
                                            setter.invoke(bean, null);
                                        } else if(strValue.trim().length() == 0) {
                                            setter.invoke(bean, null);
                                        } else {
                                            setter.invoke(bean, Double.parseDouble(strValue));
                                        }
                                    } else {
                                        try {
                                            Constructor constructor = propertyType.getConstructor(String.class);
                                            return constructor.newInstance(value);
                                        } catch (NoSuchMethodException e) {
                                            throw new RuntimeException("Unable to find (String) constructor on class: " + propertyType, e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch(IntrospectionException ie) {
                        LOGGER.error("Unable to find the properties of the bean of type, \"" + c + "\".", ie);
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

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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Convert a text value to any object whose constructor
 * takes a single String object.
 * <p>
 * For example, to get an Integer object for the lookup key
 * <i>com.example.Foo</i> you would have a com/example.properties
 * file containing the following lines.
 * <p>
 * <pre>
 * Foo=42
 * Foo.type=java.lang.Integer
 * </pre>
 */
public class ConstructorConverter implements Converter {

    Logger LOGGER = LoggerFactory.getLogger(ConstructorConverter.class);

    public Object convert(Properties properties, String type) {
        String value = properties.getProperty("valueToConvert");
        if (value == null) {
            LOGGER.error("properties={} type={}", properties, type);
            throw new RuntimeException("Missing value");
        }

        try {
            Class clazz = Class.forName(type);
            Constructor constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(value);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find class: " + type, e);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find (String) constructor on class: " + type, e);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate class: " + type, e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access class: " + type, e);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException("Unable to pass argument " + value + " to class: " + type, e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException("Unable to invoke (String) constructor on class: " + type, e);
        }

    }

}

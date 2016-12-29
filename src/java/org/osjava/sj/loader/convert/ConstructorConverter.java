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

import java.util.Properties;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Convert a text value to any object whose constructor 
 * takes a single String object. 
 *
 * For example, to get an Integer object for the lookup key 
 * <i>com.example.Foo</i> you would have a com/example.properties 
 * file containing the following three lines. 
 *
 * <pre>
 * Foo=42
 * Foo.type=java.lang.Integer
 * Foo.converter=org.osjava.sj.loader.convert.Constructor
 * </pre>
 */
public class ConstructorConverter implements Converter {

    public Object convert(Properties properties, String type) {
        String value = properties.getProperty("");

        if(value == null) {
            throw new RuntimeException("Missing value");
        }

        try {
            Class c = Class.forName(type);
            Constructor con = c.getConstructor( new Class[] { String.class } );
            return con.newInstance( new Object[] { value } );
        } catch(ClassNotFoundException cnfe) {
            throw new RuntimeException("Unable to find class: "+type, cnfe);
        } catch(NoSuchMethodException nsme) {
            throw new RuntimeException("Unable to find (String) constructor on class: "+type, nsme);
        } catch(InstantiationException ie) {
            throw new RuntimeException("Unable to instantiate class: "+type, ie);
        } catch(IllegalAccessException ie) {
            throw new RuntimeException("Unable to access class: "+type, ie);
        } catch(IllegalArgumentException iae) {
            throw new RuntimeException("Unable to pass argument "+value+" to class: "+type, iae);
        } catch(InvocationTargetException ite) {
            throw new RuntimeException("Unable to invoke (String) constructor on class: "+type, ite);
        }

    }

}

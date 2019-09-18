/*
 * Copyright (c) 2003, Henri Yandell
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

package org.osjava.sj.loader;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * To extract configuration from different file formats, e. g. .xml and .ini files, and not only from property files.
 */
public class SJProperties extends Properties {

    private String delimiter = ".";
    // our index for the ordering
    protected ArrayList index = new ArrayList();
    
    private StrSubstitutor substitutor;


    SJProperties() {
        super();
        substitutor = new StrSubstitutor(StrLookup.systemPropertiesLookup());
        substitutor.setVariablePrefix("${sys:");
    }

    /**
     * From {@link Properties}: "A property list can contain another property list as its "defaults"; this second property list is searched if the property key is not found in the original property list."
     * <p>
     * The defaults will need to be remembered for the save/store method.
     */
    SJProperties(Properties defaults) {
        super(defaults);
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        if (value instanceof String) {
            value = substitutor.replace(value);
        }
        if(index.contains(key)) {
            Object obj = get(key);
            if( !(obj instanceof List)) {
                List list = new LinkedList();
                list.add(obj);
                obj = list;
            }
            ((List)obj).add(value);
            value = obj;
        }
        if(!index.contains(key)) {
            index.add(key);
        }
        return super.put(key, value);
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        return put(key,value);
    }

    @Override
    public synchronized Object remove(Object key) {
        index.remove(key);
        return super.remove(key);
    }
    
    // simple implementation that depends on keySet.
    @Override
    public synchronized Enumeration propertyNames() {
        return Collections.enumeration( keySet() );
    }
    @Override
    public synchronized Enumeration keys() {
        return propertyNames();
    }

    @NotNull
    @Override
    public synchronized Set keySet() {
        return new OrderedSet(index);
    }
 
}

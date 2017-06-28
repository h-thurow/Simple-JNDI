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

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * To extract configuration from different file formats, e. g. .xml and .ini files, and not only from property files.
 */
public abstract class AbstractProperties extends Properties {

    private String delimiter = ".";
    // our index for the ordering
    protected ArrayList index = new ArrayList();

    public AbstractProperties() {
        super();
    }

    // the props attribute is for defaults. These will need to be
    // remembered for the save/store method.
    public AbstractProperties(Properties props) {
        super(props);
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    @Override
    public synchronized Object put(Object key, Object value) {
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

    @Override
    public synchronized Set keySet() {
        return new OrderedSet(index);
    }
 
    // TODO: Handle both save and store.
    /**
     * Currently will write out defaults as well, which is not 
     * in the specification.
     */
    @Override
    public void save(OutputStream outstrm, String header) {
        super.save(outstrm,header);
    }
    /**
     * Currently will write out defaults as well, which is not 
     * in the specification.
     */
    @Override
    public void store(OutputStream outstrm, String header) throws IOException {
        super.store(outstrm,header);
    }

    private String replace(String stringValue, String doubleQuote) {
        String value;
        stringValue = stringValue.substring(stringValue.indexOf(doubleQuote) + 1);
        if (stringValue.endsWith(doubleQuote)) {
            stringValue = stringValue.substring(0, stringValue.length() - 1);
        }
        value = stringValue;
        return value;
    }
}

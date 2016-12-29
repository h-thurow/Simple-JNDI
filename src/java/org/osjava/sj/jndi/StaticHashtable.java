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

package org.osjava.sj.jndi;

import java.util.Hashtable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;

/**
 * A hashtable that shares its space with any other instance of StaticHashtable.
 */
class StaticHashtable extends Hashtable {

    private Hashtable self;

    public StaticHashtable(Hashtable h) {
        self = h;
    }

    public synchronized int size() {
        return self.size();
    }

    public synchronized boolean isEmpty() {
        return self.isEmpty();
    }

    public synchronized java.util.Enumeration keys() {
        return self.keys();
    }

    public synchronized java.util.Enumeration elements() {
        return self.elements();
    }

    public synchronized boolean contains(Object obj) {
        return self.contains(obj);
    }

    public boolean containsValue(Object obj) {
        return self.containsValue(obj);
    }

    public synchronized boolean containsKey(Object obj) {
        return self.containsKey(obj);
    }

    public synchronized Object get(Object obj) {
        return self.get(obj);
    }

    public synchronized Object put(Object key, Object value) {
        return self.put(key, value);
    }

    public synchronized Object remove(Object obj) {
        return self.remove(obj);
    }

    public synchronized void putAll(Map map) {
        self.putAll(map);
    }

    public synchronized void clear() {
        self.clear();
    }

//    public synchronized Object clone()

    public synchronized String toString() {
        return self.toString();
    }

    public Set keySet() {
        return self.keySet();
    }

    public Set entrySet() {
        return self.entrySet();
    }

    public Collection values() {
        return self.values();
    }

    public synchronized boolean equals(Object obj) {
        return self.equals(obj);
    }

    public synchronized int hashCode() {
        return self.hashCode();
    }


}

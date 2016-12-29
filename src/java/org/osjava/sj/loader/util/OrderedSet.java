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
 * + Neither the name of Genjava-Core nor the names of its contributors 
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
// OrderedSet.java
//package com.generationjava.collections;
package org.osjava.sj.loader.util;

import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * An implementation of Set that guarentees ordering 
 * remains constant.
 */
class OrderedSet implements Set {

    private List list = null;

    public OrderedSet() {
        this.list = new LinkedList();
    }

    public OrderedSet(Collection c) {
        this();
        if(c != null) {
            Iterator iterator = c.iterator();
            while(iterator.hasNext()) {
                add(iterator.next());
            }
        }
    }
    
    /**
     * Create using the given List as the internal storage method.
     */
    public OrderedSet(List list) {
        this.list = list;
    }
    
    public boolean add(Object obj) {
     // Adds the specified element to this set if it is not already present (optional operation).         
         if(!contains(obj)) {
             list.add(obj);
             return true;
         }
        return false;
    }

    public boolean addAll(Collection c) {
     // Adds all of the elements in the specified collection to this set if they're not already present (optional operation).         
        boolean ret = false;
        if(c != null) {
            Iterator iterator = c.iterator();
            while(iterator.hasNext()) {
                if(add(iterator.next())) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    public void clear() {
     // Removes all of the elements from this set (optional operation).         
         list.clear();
    }

    public boolean contains(Object obj) {
     // Returns true if this set contains the specified element.         
         return list.contains(obj);
    }

    public boolean containsAll(Collection c) {
     // Returns true if this set contains all of the elements of the specified collection.         
         return list.containsAll(c);
    }

    public boolean equals(Object obj) {
     // Compares the specified object with this set for equality.
         return list.equals(obj);
    }

    public int hashCode() {
     // Returns the hash code value for this set.         
         return list.hashCode();
    }

    public boolean isEmpty() {
     // Returns true if this set contains no elements.         
         return list.isEmpty();
    }

    public Iterator iterator() {
     // Returns an iterator over the elements in this set.         
         return list.iterator();
    }

    public boolean remove(Object obj) {
     // Removes the specified element from this set if it is present (optional operation).         
         return list.remove(obj);
    }

    public boolean removeAll(Collection c) {
     // Removes from this set all of its elements that are contained in the specified collection (optional operation).         
         return list.removeAll(c);
    }

    public boolean retainAll(Collection c) {
     // Retains only the elements in this set that are contained in the specified collection (optional operation).         
        boolean ret = false;
        if(c != null) {
            Iterator iterator = c.iterator();
            while(iterator.hasNext()) {
                Object obj = iterator.next();
                if(!contains(obj)) {
                    remove(obj);
                    ret = true;
                }
            }
        }
        return ret;
    }

    public int size() {
     // Returns the number of elements in this set (its cardinality).         
         return list.size();
    }

    public Object[] toArray() {              
     // Returns an array containing all of the elements in this set.         
         return list.toArray();
    }

    public Object[] toArray(Object[] arr) {              
     // Returns an array containing all of the elements in this set whose runtime type is that of the specified array.
         return list.toArray(arr);
    }

}

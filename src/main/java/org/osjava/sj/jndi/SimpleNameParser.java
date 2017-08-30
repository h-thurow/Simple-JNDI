/*
 * Copyright (c) 2003-2005, Henri Yandell, Robert Zigweid
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
/*
 * org.osjava.threads.ThreadNameParser
 *
 * $URL: https://osjava.googlecode.com/svn/releases/simple-jndi-0.11.4.1/src/java/org/osjava/sj/jndi/SimpleNameParser.java $
 * $Id: SimpleNameParser.java 2587 2007-06-25 02:01:53Z flamefew $
 * $Rev: 2587 $
 * $Date: 2007-06-24 19:01:53 -0700 (Sun, 24 Jun 2007) $
 * $Author: flamefew $
 *
 * Created on Mar 24, 2004 by Robert M. Zigweid
 */
package org.osjava.sj.jndi;

import javax.naming.*;
import java.util.Properties;

/**
 * The NameParser for the Simple-JNDI.  
 * 
 * @author Robert M. Zigweid
 * @version $LastChangedRevision $ $LastChangedDate: 2007-06-24 19:01:53 -0700 (Sun, 24 Jun 2007) $
 * @since OSJava Threads 2.0
 */
public class SimpleNameParser implements NameParser {
    
    /*
     * The parent Context.  This is necessary for aquiring relevant data, like 
     * Properties that are used.
     */
    private Context parent = null;
    
    /*
     * The properties utilized by the SimpleNameParser when constructing new
     * names.
     */
    private Properties props = new Properties();
    
    /**
     * Creates a ThreadNameParser.  Any relevant information that is needed, 
     * such as the environment that is passed to {@link CompoundName CompoundName}
     * objects that are created.
     * 
     * @param parent ThreadContext that utilizes the name parser.
     * @throws NamingException if a naming exception is found.
     */
    public SimpleNameParser(Context parent) throws NamingException {
        this.parent = parent;
        /* Properties from the parent context are in a HashTable. */
        props.putAll(this.parent.getEnvironment());
    }

    /** 
     * Parses a name into its components.<br>
     * (Copied from {@link javax.naming.NameParser#parse(java.lang.String)}
     * 
     * @param name The non-null string name to parse.
     * @return A non-null parsed form of the name using the naming convention
     *         of this parser.
     * @throws InvalidNameException If the name does not conform to syntax 
     *         defined for the namespace.
     * @throws NamingException If a naming exception was encountered.
     */
    public Name parse(String name) 
        throws NamingException {
        if(name == null) {
            name = "";
        }
        Name ret = new CompoundName(name, props);
        return ret;
    }
    
    /* *
     * Determine whether or not <code>ob</code> is equal to this object.
     * If the ob is an instance of ThreadNameParser, it is considered to be 
     * equal.
     * <br/><br/>
     * <b>NOTE:</b> The above assumption may actually be false under two
     * circomstances.   Firstly, if the properties utilized by the contexts
     * are different.  Secondly, if the ThreadNameParser is subclassed.
     * 
     * @param ob the objct which is being compared to this one.
     * @see java.lang.Object#equals(java.lang.Object)
     */
     /* HEN: No hashCode() method, and I'm rather concerned with the 
             implementation of equals below.
    public boolean equals(Object ob) {
        if(ob instanceof SimpleNameParser) {
            return true;
        }
        return false;
    }    
    */
}

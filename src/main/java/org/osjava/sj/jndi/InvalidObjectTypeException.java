/*
 * org.osjava.naming.InvalidObjectTypeException
 * 
 * $Id: InvalidObjectTypeException.java 1978 2005-08-30 01:30:33Z hen $
 * $URL: https://osjava.googlecode.com/svn/releases/simple-jndi-0.11.4.1/src/java/org/osjava/sj/jndi/InvalidObjectTypeException.java $
 * $Rev: 1978 $
 * $Date: 2005-08-29 18:30:33 -0700 (Mon, 29 Aug 2005) $
 * $Author: hen $
 * 
 * Created on Sep 24, 2004.
 * Copyright (c) 2004, Robert M. Zigweid 
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer. 
 * 
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution. 
 * 
 * + Neither the name of the Simple-JNDI nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without 
 *   specific prior written permission.
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

import javax.naming.NamingException;

/**
 * An exception to indicate that the object that was being bound to a context 
 * is not a valid type for the context.
 * 
 * @author Robert M. Zigweid
 * @version $Rev: 1978 $ $Date: 2005-08-29 18:30:33 -0700 (Mon, 29 Aug 2005) $
 * @since  0.9.2 XXX: Henri confirm this
 */
public class InvalidObjectTypeException extends NamingException {

    /**
     * Create a new InvalidObjectTypeException with the given explanation.
     * 
     * @param explanation An explanation of why the exception was thrown.
     */
    public InvalidObjectTypeException(String explanation) {
        super(explanation);
    }

    /**
     * Create a new InvalidObjectTypeException with the default explanation.
     */
    public InvalidObjectTypeException() {
        super("Objects of this type are not supported by the context.");
    }

}

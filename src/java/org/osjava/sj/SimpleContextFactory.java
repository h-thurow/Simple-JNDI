/*
 * org.osjava.jndiSimpleContextFactory
 * $Id: SimpleContextFactory.java 1743 2005-06-24 23:56:40Z rzigweid $
 * $Rev: 1743 $ 
 * $Date: 2005-06-24 19:56:40 -0400 (Fri, 24 Jun 2005) $ 
 * $Author: rzigweid $
 * $URL: https://svn.osjava.org/svn/osjava/trunk/simple-jndi/src/java/org/osjava/sj/memory/SimpleContextFactory.java $
 * 
 * Created on Aug 20, 2005
 *
 * Copyright (c) 2005, Henri Yandell All rights reserved.
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


package org.osjava.sj;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Initial Context Factory for SimpleContexts
 * 
 * @author Henri Yandell
 * @since Simple-JNDI 0.11
 * @version $Rev: 1743 $ $Date: 2005-06-24 19:56:40 -0400 (Fri, 24 Jun 2005) $
 */
public class SimpleContextFactory implements InitialContextFactory {

    /**
     * Create a SimpleContextFactory
     */
    public SimpleContextFactory() {
        super();
    }

    /**
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(Hashtable environment) throws NamingException {
        SimpleContext context = new SimpleContext(environment);
        return context;
    }

}

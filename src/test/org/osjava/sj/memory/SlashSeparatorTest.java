/*
 * org.osjava.jndi.MemoryContext_createSubContextTest
 * $Id: SlashSeparatorTest.java 1871 2005-08-21 01:59:27Z hen $
 * $Rev: 1871 $ 
 * $Date: 2005-08-20 21:59:27 -0400 (Sat, 20 Aug 2005) $ 
 * $Author: hen $
 * $URL: https://svn.osjava.org/svn/osjava/trunk/simple-jndi/src/test/org/osjava/sj/memory/SlashSeparatorTest.java $
 * 
 * Created on Feb 14, 2005
 *
 * Copyright (c) 2004, Henri Yandell All rights reserved.
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

package org.osjava.sj.memory;

/**
 * @author bayard@generationjava.com
 */
public class SlashSeparatorTest extends MemoryContextTestAbstract {

    public SlashSeparatorTest(String name) {
        super(name);
    }

    protected String getDelimiter() {
        return "/";
    }

}

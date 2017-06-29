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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Functionally like the CustomProperties class in that it has
 * comments and an order, IniProperties reads .ini files. These
 * implicitly have a two level dotted notation, though any values
 * not in the two level are treated as simple one levels.
 * Comments are a semi-colon.
 */
public class IniProperties extends SJProperties {

    /**
     * Load in a .ini file. Semi-colons are comments. Blocks are denoted with square brackets. Values are then key=value pairs, with blocks being prepended to keys.
     */
    @Override
    public synchronized void load(InputStream in) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            String block = "";
            while ((line = reader.readLine()) != null) {

                line = line.trim();   // important?? bad??

                // handle blocks
                if (line.startsWith("[") && line.endsWith("]")) {
                    block = line.substring(1, line.length() - 1);
                }

                int idx = line.indexOf(';');
                // remove comment
                if (idx != -1) {
                    line = line.substring(0, idx);
                }

                // split equals sign
                idx = line.indexOf('=');
                if (idx != -1) {
                    String value = line.substring(idx + 1);
                    // Quoted values[edit]
                    // Some implementations allow values to be quoted, typically using
                    // double quotes and/or apostrophes. This allows for explicit declaration
                    // of whitespace, and/or for quoting of special characters (equals,
                    // semicolon, etc.). The standard Windows function GetPrivateProfileString
                    // supports this, and will remove quotation marks that surround the values.
                    // https://en.wikipedia.org/wiki/INI_file
                    if ("".equals(block)) {
                        setProperty(line.substring(0, idx), value);
                    }
                    else {
                        setProperty(block + getDelimiter() + line.substring(0, idx), value);
                    }
                }
                else {
                    // Blank line, or just a bad line. We ignore it
                }
            }
            reader.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}

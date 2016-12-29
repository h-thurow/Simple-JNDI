/*
 * Copyright (c) 2003-2005, Henri Yandell
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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.osjava.sj.loader.convert.ConvertRegistry;
import org.osjava.sj.loader.convert.Converter;

import org.osjava.sj.loader.util.AbstractProperties;
import org.osjava.sj.loader.util.CustomProperties;
import org.osjava.sj.loader.util.IniProperties;
import org.osjava.sj.loader.util.Utils;
import org.osjava.sj.loader.util.XmlProperties;

/**
 * Loads a .properties file into a JNDI server.
 */
public class JndiLoader {

    // separator, or just put them in as contexts?
    public static final String SIMPLE_DELIMITER = "org.osjava.sj.delimiter";

    // share the same InitialContext
    public static final String SIMPLE_SHARED = "org.osjava.sj.shared";

    // char(s) to replace : with on the filesystem in filenames
    public static final String SIMPLE_COLON_REPLACE = "org.osjava.sj.colon.replace";

    private static ConvertRegistry convertRegistry = new ConvertRegistry();

    private Hashtable table = new Hashtable();

    public JndiLoader() {
        this.table.put(SIMPLE_DELIMITER, "/");
    }
    
    public JndiLoader(Hashtable env) {
        if(!env.containsKey(SIMPLE_DELIMITER)) {
            throw new IllegalArgumentException("The property "+SIMPLE_DELIMITER+" is mandatory. ");
        }
        

        this.table.put(SIMPLE_DELIMITER, env.get(SIMPLE_DELIMITER));

        if(env.containsKey(SIMPLE_COLON_REPLACE)) {
            this.table.put(SIMPLE_COLON_REPLACE, env.get(SIMPLE_COLON_REPLACE));
        }

    }
    
    public void putParameter(String key, String value) {
        table.put(key, value);
    }

    public String getParameter(String key) {
        return (String) table.get(key);
    }

    /**
     * Loads all .properties files in a directory into a context
     */
    public void loadDirectory(File directory, Context ctxt) throws NamingException, IOException {
        loadDirectory(directory, ctxt, null, "");
    }
    public void loadDirectory(File directory, Context ctxt, Context parentCtxt, String ctxtName) throws NamingException, IOException {
// System.err.println("Loading directory. ");

        if( !directory.isDirectory() ) {
            throw new IllegalArgumentException("java.io.File parameter must be a directory. ["+directory+"]");
        }

        File[] files = directory.listFiles();
        if(files == null) {
// System.err.println("Null files. ");
            return;
        }

        for(int i=0; i<files.length; i++) {
            File file = files[i];
            String name = file.getName();

            String colonReplace = (String) this.table.get(SIMPLE_COLON_REPLACE);
            if(colonReplace != null) {
                if(name.indexOf(colonReplace) != -1) {
                    name = Utils.replace( name, colonReplace, ":" );
                }
            }
// System.err.println("Consider: "+name);
            // TODO: Replace hack with a FilenameFilter

            if( file.isDirectory() ) {
                // HACK: Hack to stop it looking in .svn or CVS
                if(name.equals(".svn") || name.equals("CVS")) {
                    continue;
                }

// System.err.println("Is directory. Creating subcontext: "+name);
                Context tmpCtxt = ctxt.createSubcontext( name );
                loadDirectory(file, tmpCtxt, ctxt, name);
            } else {
                // TODO: Make this a plugin system
                String[] extensions = new String[] { ".properties", ".ini", ".xml" };
                for(int j=0; j<extensions.length; j++) {
                    String extension = extensions[j];
                    if( file.getName().endsWith(extension) ) {
// System.err.println("Is "+extension+" file. "+name);
                        Context tmpCtxt = ctxt;
                        if(!file.getName().equals("default"+extension)) {
                            name = name.substring(0, name.length() - extension.length());
// System.err.println("Not default, so creating subcontext: "+name);
                            tmpCtxt = ctxt.createSubcontext( name );
                            parentCtxt = ctxt;
                            ctxtName = name;
                        }
                        load( loadFile(file), tmpCtxt, parentCtxt, ctxtName );
                    }
                }
            }
        }

    }

    private Properties loadFile(File file) throws IOException {
//        System.err.println("LOADING: "+file);
        AbstractProperties p = null;

        if(file.getName().endsWith(".xml")) {
            p = new XmlProperties();
        } else
        if(file.getName().endsWith(".ini")) {
            p = new IniProperties();
        } else {
            p = new CustomProperties();
        }

        p.setDelimiter( (String) this.table.get(SIMPLE_DELIMITER) );

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            p.load(fin);
            return p;
        } finally {
            if(fin != null) fin.close();
        }
    }


    /**
     * Loads a properties object into a context.
     */
    public void load(Properties properties, Context ctxt) throws NamingException {
        load(properties, ctxt, null, "");
    }
    public void load(Properties properties, Context ctxt, Context parentCtxt, String ctxtName) throws NamingException {
 //       System.err.println("Loading Properties: " + properties);

        String delimiter = (String) this.table.get(SIMPLE_DELIMITER);
        String typePostfix = delimiter + "type";

        // NOTE: "type" effectively turns on pseudo-nodes; if it 
        //       isn't there then other pseudo-nodes will result 
        //       in re-bind errors

        // scan for pseudo-nodes, aka "type":   foo.type
        // store in a temporary type table:    "foo", new Properties() with type="value"
        Map typeMap = new HashMap();
        Iterator iterator = properties.keySet().iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();

            if(key.equals("type") || key.endsWith( typePostfix )) {
                Properties tmp = new Properties();
                tmp.put( "type", properties.get(key) );
                if( key.equals( "type" ) ) {
                    typeMap.put( "", tmp );
                } else {
                    typeMap.put( key.substring(0, key.length() - typePostfix.length()), tmp );
                }
            }

        }

//        System.err.println("TypeMap: " + typeMap);

// if it matches a type root, then it should be added to the properties
// if not, then it should be placed in the context
// for each type properties
// call convert: pass a Properties in that contains everything starting with foo, but without the foo
// put objects in context

        iterator = properties.keySet().iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = properties.get(key);

            if(key.equals("type") || key.endsWith( typePostfix )) {
                continue;
            }

            if(typeMap.containsKey(key)) {
// System.err.println("Typed: "+key);
                ( (Properties) typeMap.get(key) ).put("", value);
                continue;
            }

            if(key.indexOf(delimiter) != -1) {
                String pathText = removeLastElement( key, delimiter );
                String nodeText = getLastElement( key, delimiter );
// System.err.println("PathText: "+pathText+" NodeText: "+nodeText);

                if(typeMap.containsKey(pathText)) {
// System.err.println("Sibling: "+key);
                    ( (Properties) typeMap.get(pathText) ).put(nodeText, value);
                    continue;
                }
            } else
            if(typeMap.containsKey("")) {
// System.err.println("Empty Sibling: "+key);
                    ( (Properties) typeMap.get("") ).put(key, value);
                    continue;
            }

// System.err.println("Putting: "+key);
            jndiPut( ctxt, key, properties.get(key) );
        }

        Iterator typeIterator = typeMap.keySet().iterator();
        while(typeIterator.hasNext()) {
            String typeKey = (String) typeIterator.next();
            Properties typeProperties = (Properties) typeMap.get(typeKey);

            Object value = convert(typeProperties);
// System.err.println("Putting typed: "+typeKey);
            if(typeKey.equals("")) {
                jndiPut( parentCtxt, ctxtName, value );
            } else {
                jndiPut( ctxt, typeKey, value );
            }
        }

    }

    private void jndiPut(Context ctxt, String key, Object value) throws NamingException {
        // here we need to break by the specified delimiter
//        System.err.println("Putting "+key+"="+value);

        // can't use String.split as the regexp will clash with the types of chars 
        // used in the delimiters. Could use Commons Lang. Quick hack instead.
//        String[] path = key.split( (String) this.table.get(SIMPLE_DELIMITER) );
        String[] path = Utils.split( key, (String) this.table.get(SIMPLE_DELIMITER) );

// System.err.println("LN: "+path.length);
        int lastIndex = path.length - 1;


        Context tmpCtxt = ctxt;

        for(int i=0; i < lastIndex; i++) {
            Object obj = tmpCtxt.lookup(path[i]);
            if(obj == null) {
// System.err.println("Creating subcontext: " + path[i] + " for " + key);
                tmpCtxt = tmpCtxt.createSubcontext(path[i]);
            } else
            if(obj instanceof Context) {
// System.err.println("Using subcontext: "+obj + " for " + key);
                tmpCtxt = (Context) obj;
            } else {
                throw new RuntimeException("Illegal node/branch clash. At branch value '"+path[i]+"' an Object was found: " +obj);
            }
        }
        
        Object obj = tmpCtxt.lookup(path[lastIndex]);
        if(obj instanceof Context) {
            tmpCtxt.destroySubcontext(path[lastIndex]);
            obj = null;
        }
        if(obj == null) {
// System.err.println("Binding: "+path[lastIndex]+" on "+key);
            tmpCtxt.bind( path[lastIndex], value );
        } else {
// System.err.println("Rebinding: "+path[lastIndex]+" on "+key);
            tmpCtxt.rebind( path[lastIndex], value );
        }
    }

    private static Object convert(Properties properties) {
        String type = properties.getProperty("type");
        // TODO: handle a plugin type system
        
        String converterClassName = properties.getProperty("converter");
        if(converterClassName != null) {
            try {
                Class converterClass = Class.forName( converterClassName );
                Converter converter = (Converter) converterClass.newInstance();
                return converter.convert(properties, type);
            } catch(ClassNotFoundException cnfe) {
                throw new RuntimeException("Unable to find class: "+converterClassName, cnfe);
            } catch(IllegalAccessException ie) {
                throw new RuntimeException("Unable to access class: "+type, ie);
            } catch(InstantiationException ie) {
                throw new RuntimeException("Unable to create Converter " + type + " via empty constructor. ", ie);
            }
        }

        // TODO: Support a way to set the default converters in the jndi.properties 
        //       and in the API itself
        Converter converter = convertRegistry.getConverter(type);
        if(converter != null) {
            return converter.convert(properties, type);
        }

        return properties.get("");

    }

    // String methods to make the using code more readable
    private static String getLastElement( String str, String delimiter ) {
        int idx = str.lastIndexOf(delimiter);
        return str.substring(idx + 1);
    }
    private static String removeLastElement( String str, String delimiter ) {
        int idx = str.lastIndexOf(delimiter);
        return str.substring(0, idx);
    }

}

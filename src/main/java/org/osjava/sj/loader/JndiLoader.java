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


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osjava.StringUtils;
import org.osjava.sj.jndi.JndiUtils;
import org.osjava.sj.loader.convert.ConverterIF;
import org.osjava.sj.loader.convert.ConverterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.*;
import javax.naming.spi.NamingManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class JndiLoader {

    public static final String DELIMITER = "org.osjava.sj.delimiter";

    /** char(s) to replace with ":" in filenames and directories. Siehe {@link #handleColonReplacement(String)}*/
    public static final String COLON_REPLACE = "org.osjava.sj.colon.replace";
    private static final Properties EMPTY_PROPERTIES = new Properties();

    private static ConverterRegistry converterRegistry = new ConverterRegistry();
    private final Properties envAsProperties;

    private Hashtable environment = new Hashtable();
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    public static final String FILENAME_TO_CONTEXT = "org.osjava.sj.filenameToContext";

    public JndiLoader(Hashtable env) {
        environment = new Hashtable(env);
        if(!environment.containsKey(DELIMITER)) {
            LOGGER.info("{} not set. Setting to \".\"", DELIMITER);
            environment.put(DELIMITER, ".");
        }
        if (!environment.containsKey("jndi.syntax.direction")) {
            LOGGER.warn("jndi.syntax.direction not set. Setting to \"left_to_right\"");
            environment.put("jndi.syntax.direction", "left_to_right");
        }
        Properties props = new Properties();
        props.putAll(environment);
        envAsProperties = props;
    }
    
    /**
     * Loads all .properties", .ini, .xml files in a directory or a single file into a context.
     */
    public void load(File fileOrDirectory, Context ctxt) throws NamingException, IOException {
        fileOrDirectory = new File(fileOrDirectory.getAbsolutePath());
        if (fileOrDirectory.isDirectory()) {
            loadDirectory(fileOrDirectory, ctxt, null, "");
        }
        else if (fileOrDirectory.isFile()) {
            boolean preserveFileNameAsContextName = BooleanUtils.toBoolean(
                    (String) environment.get(FILENAME_TO_CONTEXT));
            if (isSupportedFile(fileOrDirectory)) {
                loadFile(fileOrDirectory, ctxt, null, "", preserveFileNameAsContextName);
            }
        }
        else {
            LOGGER.warn("Not found: {}", fileOrDirectory.getAbsolutePath());
        }
    }

    /**
     *
     * @param preserveFileNameAsContextName If fileOrDirectory is a file, the file's name is taken as context name.
     * @param ignoreFileExtension true: If fileOrDirectory is a file, it will be processed as property file whatever its extension is.
     */
    public void load(File fileOrDirectory, Context ctxt, boolean preserveFileNameAsContextName, boolean ignoreFileExtension) throws NamingException, IOException {
        fileOrDirectory = new File(fileOrDirectory.getAbsolutePath());
        if (fileOrDirectory.isDirectory()) {
            loadDirectory(fileOrDirectory, ctxt, null, "");
        }
        else if (fileOrDirectory.isFile()) {
            if (!ignoreFileExtension) {
                if (isSupportedFile(fileOrDirectory)) {
                    loadFile(fileOrDirectory, ctxt, null, "", preserveFileNameAsContextName);
                }
            }
            else {
                loadFile(fileOrDirectory, ctxt, null, "", preserveFileNameAsContextName);
            }
        }
        else {
            LOGGER.warn("Not found: {}", fileOrDirectory.getAbsolutePath());
        }
    }

    /**
     * Loads all .properties", .ini, .xml files in a directory into a context.
     */
    private void loadDirectory(File directory, Context ctxt, Context parentCtxt, String subName) throws NamingException, IOException {
        LOGGER.debug("Loading {}", directory.getAbsolutePath());
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String dirName = file.getName();
                    if (!dirName.equals(".svn") && !dirName.equals("CVS")) {
                        dirName = handleColonReplacement(dirName);
                        Context tmpCtxt = ctxt.createSubcontext(dirName);
                        loadDirectory(file, tmpCtxt, ctxt, dirName);
                    }
                }
                else {
                    String baseName = FilenameUtils.getBaseName(file.getName());
                    boolean preserveFileNameAsContextName = !baseName.equals("default");
                    if (isSupportedFile(file)) {
                        loadFile(file, ctxt, parentCtxt, subName, preserveFileNameAsContextName);
                    }
                }
            }
        }
    }

    /**
     * Loads any file, not only those files {@link #isSupportedFile(File)} returns true for.
     *
     * @param file Not a directory
     */
    private void loadFile(File file, Context ctxt, Context parentCtxt, String subName, boolean preserveFileNameAsContextName) throws NamingException, IOException {
        LOGGER.debug("Loading {}", file.getAbsolutePath());
        String parentName = file.getName();
        parentName = handleColonReplacement(parentName);
        Context subContext = ctxt;
        Properties properties = toProperties(file);
        if (isNotNamespacedTypeDefinition(properties)) {
            // preserve the file name as object name.
            subName = FilenameUtils.getBaseName(parentName);
            parentCtxt = subContext;
        }
        else if (!FilenameUtils.getBaseName(parentName).equals("default")) {
            parentName = FilenameUtils.getBaseName(parentName);
            if (preserveFileNameAsContextName) {
                subContext = ctxt.createSubcontext(parentName);
                parentCtxt = ctxt;
            }
            subName = parentName;
        }
        load(properties, subContext, parentCtxt, subName);
    }

    /**
     * @return true: .properties, .ini, .xml file.
     */
    private boolean isSupportedFile(@NotNull File file) {
        String[] extensionsToProcess = new String[]{"properties", "ini", "xml"};
        String ext = FilenameUtils.getExtension(file.getName());
        for (String extToProcess : extensionsToProcess) {
            if (extToProcess.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For example a DataSource definition file with properties without namespace, e. g. "type=javax.sql.DataSource" instead of "Sybase/type=javax.sql.DataSource".
     */
    boolean isNotNamespacedTypeDefinition(Properties properties) {
        for (Object k : properties.keySet()) {
            String key = (String) k;
            if (key.equals("type")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces {@link JndiLoader#COLON_REPLACE} with ":" for building a ENC like "java:comp/env".
     */
    String handleColonReplacement(String name) {
        String colonReplace = (String) environment.get(COLON_REPLACE);
        if (colonReplace != null) {
            if (name.contains(colonReplace)) {
                name = StringUtils.replace(name, colonReplace, ":");
            }
        }
        return name;
    }

    /**
     *
     * @return xml file: {@link XmlSJProperties}. ini file: {@link IniSJProperties}. Sonst {@link CustomSJProperties}.
     */
    // TODO Make package-private
    public Properties toProperties(File file) throws IOException {
//        System.err.println("LOADING: "+file);
        SJProperties properties;

        if(file.getName().endsWith(".xml")) {
            properties = new XmlSJProperties();
        }
        else if(file.getName().endsWith(".ini")) {
            properties = new IniSJProperties();
        }
        else {
            properties = new CustomSJProperties();
        }

        properties.setDelimiter( (String) environment.get(DELIMITER) );

        try (FileInputStream stream = new FileInputStream(file)) {
            properties.load(stream);
            return properties;
        }
    }


    /**
     * Loads a properties object into a context.
     */
    public void load(Properties properties, Context ctxt) throws NamingException {
        load(properties, ctxt, null, "");
    }

    private void load(Properties properties, Context subContext, Context parentCtxt, String subName) throws NamingException {

        // NOTE: "type" effectively turns on pseudo-nodes; if it isn't there then other pseudo-nodes will result in re-bind errors.

        Map typeMap = extractTypedProperties(properties);
        Iterator iterator;

        // If it matches a type root, then it should be added to the properties. If not, then it should be placed in the context (jndiPut()).
        // For each type properties call convert: pass a Properties in that contains everything starting with foo, but without the foo.
        // Put objects in context.
        iterator = properties.keySet().iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = properties.get(key);
            final String delimiter = extractDelimiter(key);
            if (!key.equals("type") && extractTypeDeclaration(key) == null) {
                if (typeMap.containsKey("datasourceOrBeanProperty")) {
                    // files with a property named "type" without a namespace in the name.
                    ((Properties) typeMap.get("datasourceOrBeanProperty")).put(key, value);
                }
                else if(typeMap.containsKey(key)) {
                    // Reached only by keys with basic type declarations like type=java.lang.Integer.
                    // Gets processed by a converter.
                    ((Properties) typeMap.get(key)).put("valueToConvert", value);
                }
                else if(delimiter != null) {
                    String pathText = removeLastElement(key, delimiter);
                    String nodeText = getLastElement(key, delimiter);
                    if(typeMap.containsKey(pathText)) {
                        ((Properties) typeMap.get(pathText)).put(nodeText, value);
                    }
                    else {
                        jndiPut(subContext, key, value);
                    }
                }
                else {
                    jndiPut(subContext, key, value);
                }
            }
        }

        for (Object key : typeMap.keySet()) {
            String typeKey = (String) key;
            Properties typeProperties = (Properties) typeMap.get(typeKey);
            Object value = convert(typeProperties);
            if (typeKey.equals("datasourceOrBeanProperty")) {
                // Reached only by datasource and bean declarations? Yes, but not always! Not from org.osjava.sj.memory.JndiLoaderTest.testBeanConverter(). testBeanConverter() enters the "else" branch.  Not reached, when the attributes are prefixed with a namespace as in roots/datasource/ds.properties (used in SimpleJndiNewTest.sharedContextWithDataSource2MatchingDelimiter()).
                // rebind(): For every file there is already a context created and bound under the file's name. In case of bean or datasource declarations the binding must not be a context but the value (the bean, the datasource) itself. This is true as long as the datasource or bean properties are not namespaced. Then the "else" branch is executed.
                parentCtxt.rebind(subName, value);
            }
            else {
                jndiPut(subContext, typeKey, value);
            }
        }


    }

    /**
     * Scan for pseudo-nodes, aka "type" in foo.type. Store in a temporary map (typeMap): {foo: {type: typeValue}}
     */
    @NotNull
    Map<String, Properties> extractTypedProperties(Properties properties) throws InvalidNameException {
        Map typeMap = new HashMap<String, Properties>();
        Iterator iterator = properties.keySet().iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            final String type = extractTypeDeclaration(key);
            // key.equals("type"): type attribute without namespace
            // type != null: type attribute prefixed with namespace, e.g. "Sybase/type"
            if(key.equals("type") || type != null) {
                Properties props = new Properties();
                props.put("type", properties.get(key));
                if(key.equals("type")) {
                    // Reached only by datasource and bean declarations? Yes, but not always! Not from org.osjava.sj.memory.JndiLoaderTest.testBeanConverter(). testBeanConverter() enters the "else" branch. Not reached, when the attributes are prefixed with a namespace as in roots/datasource/ds.properties (used in SimpleJndiNewTest.sharedContextWithDataSource2MatchingDelimiter()).
                    typeMap.put("datasourceOrBeanProperty", props);
                }
                else {
                    final String keyWithoutType = key.substring(0, key.length() - type.length() - 1);
                    typeMap.put(keyWithoutType, props);
                }
            }
        }
        return typeMap;
    }

    private void processTypedProperty(Properties properties, Context subContext, String subName) throws NamingException {
        // TODO Hier müssen irgendwie DataSource definitions unterschieden werden von basic type definitions mit unterschiedlich tief verschachtelten namespaces.
        // DataSource and beans without namespaced attributes
        if (isNotNamespacedTypeDefinition(properties)) {
            String typeDefinition = getTypeDefinition(properties);
            Name contextName = extractContextName(typeDefinition);
            Context deepestCtx = subContext;
            Name objName;
            if (contextName != null) {
                if (contextName.size() > 1) {
                    contextName.remove(contextName.size() - 1); // last part is the name of the object to bind.
                    deepestCtx = createSubContexts(contextName, subContext);
                    objName = contextName.getSuffix(contextName.size() - 1);
                }
                else {
                    objName = contextName;
                }
            }
            else {
                objName = new CompoundName(subName, EMPTY_PROPERTIES);
            }

            Properties notNamespacedKeys = new Properties();
            for (Object k : properties.keySet()) {
                String key = (String) k;
                key = extractObjectName(key);
                String value = (String) properties.get(key);
                notNamespacedKeys.put(key, value);
            }
            jndiPut(deepestCtx, objName.toString(), convert(notNamespacedKeys));
        }
        else {
            throw new RuntimeException("Not implemented yet.");
        }
//        Name contextName = extractContextName(typeDefinition);
//        Context deepestCtx = subContext;
//        Name objName;
//        if (contextName != null) {
//            if (contextName.size() > 1) {
//                contextName.remove(contextName.size() - 1); // last part is the name of the object to bind.
//                deepestCtx = createSubContexts(contextName, subContext);
//                objName = contextName.getSuffix(contextName.size() - 1);
//            }
//            else {
//                objName = contextName;
//            }
//        }
//        else {
//            objName = new CompoundName(subName, EMPTY_PROPERTIES);
//        }
//
//        Properties notNamespacedKeys = new Properties();
//        for (Object k : properties.keySet()) {
//            String key = (String) k;
//            key = extractObjectName(key);
//            Object value = properties.get(key);
//            if (value != null) {
//                notNamespacedKeys.put(key, value);
//            }
//        }
//        jndiPut(deepestCtx, objName.toString(), convert(notNamespacedKeys, objName.toString()));
    }

    /**
     *
     * @return null: No type attribute found.
     */
    @Nullable
    String getTypeDefinition(@NotNull Properties properties) {
        for (Object k : properties.keySet()) {
            String key = (String) k;
            if (key.endsWith("type")) {
                return key;
            }
        }
        return null;
    }

    /**
     * If the attribute name is namespaced as in "my/context/objectName", the returned Name is "my/context", because objectName is interpreted not as a context name but as the name of the object to be bound to "my/context".
     *
     * @return null: Not a namespaced attribute, eg. only "myInt".
     */
    @Nullable
    Name extractContextName(String path) throws InvalidNameException {
        CompoundName name = toCompoundName(path);
        Name nameWithoutObjectName = name.size() > 1
                ? name.getPrefix(name.size() - 1)
                : null;
        return nameWithoutObjectName;
    }

    /**
     *
     * @param path one component ("object") or several components ("my/ctx/object").
     * @return Either the single component or the last component in a several component path.
     */
    @NotNull
    String extractObjectName(String path) throws InvalidNameException {
        CompoundName name = toCompoundName(path);
        return name.getSuffix(name.size() > 1 ? name.size() - 1 : 0).toString();
    }

    /**
     * @return  The CompoundName for path with respect to {@link JndiLoader#DELIMITER}.
     */
    @NotNull
    private CompoundName toCompoundName(@NotNull String path) throws InvalidNameException {
        Properties envCopy = new Properties(envAsProperties);
        envCopy.setProperty("jndi.syntax.separator", envAsProperties.getProperty(DELIMITER));
        envCopy.setProperty("jndi.syntax.direction", (String) envAsProperties.get("jndi.syntax.direction"));
        return new CompoundName(path, envCopy);
    }

    /**
     * Incompletely implemented: Let DELIMITER be a regular expression, e.g. "\.|\/".
     *
     * @return delimiter "." or "/" or whatever is found by {@link #DELIMITER}.
     */
    private String extractDelimiter(String key) {
        String delimiter = (String) environment.get(DELIMITER);
        if (delimiter.length() == 1) { // be downwards compatible
            delimiter = delimiter.replace(".", "\\.");
        }
        // TODO Compile once
        final Pattern pattern = Pattern.compile("^.+(" + delimiter + ").+");
        final Matcher matcher = pattern.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     *
     * @return "type" | null
     */
    @Nullable
    private String extractTypeDeclaration(@NotNull String key) throws InvalidNameException {
        String objName = extractObjectName(key);
        return objName.equals("type") ? objName : null;
    }

    /**
     * Creates contexts defined by namespaced property names, e.g. "my.namespaced.object=...". The last part (here "object") is the name under which the value is bound.
     */
    private void jndiPut(Context ctxt, String key, Object value) throws NamingException {
        Name name = extractContextName(key);
        if (name != null) {
            Context deepestCtx = createSubContexts(name, ctxt);
            deepestCtx.bind(extractObjectName(key), value);
        }
        else {
            ctxt.bind(toCompoundName(key), value);
        }
    }

    /**
     *
     * @param name Name of the contexts to be created in parentContext.
     */
    Context createSubContexts(Name name, Context parentContext) throws NamingException {
        Context currentCtx = parentContext;
        for(int i=0; i < name.size(); i++) {
            Object obj;
            try {
                obj = currentCtx.lookup(name.get(i));
                if (obj instanceof Context) {
                    currentCtx = (Context) obj;
                }
                else {
                    LOGGER.error("createSubContexts() CompoundName={} Name '{}' already occupied by '{}'.", name, name.get(i), obj);
                    throw new NotContextException(name.get(i) + " already occupied by " + obj);
                }
            }
            catch (NameNotFoundException e) {
                // component does not exist
                currentCtx = currentCtx.createSubcontext(name.get(i));
            }
        }
        return currentCtx;
    }

    @Nullable
    private Object convert(Properties properties) {
        String type = properties.getProperty("type");
        Object obj = properties.get("valueToConvert");

        String converterClassName = properties.getProperty("converter");
        if (converterClassName != null) {
            obj = callConverter(properties, type, converterClassName);
        }
        else {
            obj = processType(properties, type, obj);
        }
        return obj;

    }

    @Nullable
    Object processType(Properties properties, String type, Object obj) {
        Object o = null;
        if (environment.containsKey(Context.OBJECT_FACTORIES)) {
            try {
                Reference reference = JndiUtils.toReference(properties, type);
                o = NamingManager.getObjectInstance(reference, null, null, environment);
                o = o == reference ? null : o;
            }
            catch (Exception e) {
                LOGGER.error("processType() Exception caught: ", e);
            }
        }
        if (o == null) {
            ConverterIF converter = converterRegistry.getConverter(type);
            if (converter != null) {
                final Object values = properties.get("valueToConvert");
                if (values instanceof List) {
                    List<String> vals = (List<String>) values;
                    final LinkedList converted = new LinkedList();
                    for (String val : vals) {
                        final Properties props = new Properties();
                        props.setProperty("valueToConvert", val);
                        converted.add(converter.convert(props, type));
                    }
                    obj = converted;
                }
                else {
                    obj = converter.convert(properties, type);
                }
            }
        }
        else {
            obj = o;
        }
        return obj;
    }

    private static Object callConverter(Properties properties, String type, String converterClassName) {
        Object obj;
        try {
            Class converterClass = Class.forName(converterClassName);
            ConverterIF converter = (ConverterIF) converterClass.newInstance();
            obj = converter.convert(properties, type);
        }
        catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Unable to find class: " + converterClassName, cnfe);
        }
        catch (IllegalAccessException ie) {
            throw new RuntimeException("Unable to access class: " + type, ie);
        }
        catch (InstantiationException ie) {
            throw new RuntimeException("Unable to create Converter " + type + " via empty constructor. ", ie);
        }
        return obj;
    }

    private static String getLastElement( String str, String delimiter ) {
        int idx = str.lastIndexOf(delimiter);
        return str.substring(idx + 1);
    }
    private static String removeLastElement( String str, String delimiter ) {
        int idx = str.lastIndexOf(delimiter);
        return str.substring(0, idx);
    }

}

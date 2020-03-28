/*
 * Copyright (c) 2003 - 2005, Henri Yandell, Robert M. Zigweid
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

import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.*;
import javax.naming.spi.NamingManager;
import java.util.*;

/**
 * @author Robert M. Zigweid
 * @since Simple-JNDI 0.11
 */
public class MemoryContext implements Cloneable, Context  {

    public static final String IGNORE_CLOSE = "org.osjava.sj.jndi.ignoreClose";

    private Map<Name, Object> namesToObjects = Collections.synchronizedMap(new HashMap<Name, Object>());
    private Map<Name, Context> subContexts = Collections.synchronizedMap(new HashMap<Name, Context>());
    private Hashtable env = new Hashtable();
    private NameParser nameParser;
    /* The full name of this context. */
    private Name nameInNamespace = null;
    private boolean nameLock = false;
    private static Logger LOGGER = LoggerFactory.getLogger(MemoryContext.class);

    /**
     * @param env a Hashtable containing the Context's environment.
     */
    public MemoryContext(Hashtable env) {
        this(env, null);
    }

    /**
     * IMPROVE Mandatory: Read jndi configuration from system properties otherwise it will not work correctly.
     */
    protected MemoryContext() {
        this(null, null);
        throw new RuntimeException("Not fully implemented");
    }

    /**
     * @param parser the NameParser being used by the Context.
     */
    protected MemoryContext(NameParser parser) {
        this(null, parser);
    }

    /**
     * @param env a Hashtable containing the Context's environment.
     * @param parser the NameParser being used by the Context.
     */
    protected MemoryContext(Hashtable env, NameParser parser) {
        if(env != null) {
            this.env = (Hashtable)env.clone();
        }

        if(parser == null) {
            try {
                nameParser = new SimpleNameParser(this);
            } catch (NamingException e) {
                /* 
                 * XXX: This should never really occur.  If it does, there is 
                 * a severe problem.  I also don't want to throw the exception
                 * right now because that would break compatability, even 
                 * though it is probably the right thing to do.  This might
                 * get upgraded to a fixme.
                 */
                e.printStackTrace();
            }
        }
        try {
            nameInNamespace = nameParser.parse("");
        } catch (NamingException e) {
            /* This shouldn't be an issue at this point */
            e.printStackTrace();
        }
    }

    /* **********************************************************************
     * Implementation of methods specified by java.lang.naming.Context      *
     * **********************************************************************/

    @Override
    public String toString() {
        return "AbstractContext{" +
                "namesToObjects=" + namesToObjects +
                ", subContexts=" + subContexts +
                ", env=" + env +
                ", nameParser=" + nameParser +
                ", nameInNamespace=" + nameInNamespace +
                ", nameLock=" + nameLock +
                '}';
    }

    /**
     *
     * @see javax.naming.Context#lookup(javax.naming.Name)
     * @throws OperationNotSupportedException if name is empty, because "a new instance of this context" (see {@link Context#lookup(Name)}) is at this time an unsupported operation.
     */
    @Override
    public Object lookup(@NotNull Name name) throws NamingException {
        if (name.size() == 0) {
            return newInstance();
        }
        else {
            Name objName = name.getPrefix(1);
            if (name.size() > 1) { // A subcontext is lookuped.
                if (subContexts.containsKey(objName)) {
                    return subContexts.get(objName).lookup(name.getSuffix(1));
                }
                String msg = "AbstractContext#lookup(\"{}\"): Invalid subcontext '{}' in context '{}': {}";
                LOGGER.error(msg, name, objName, getNameInNamespace(), this);
                throw new NamingException();
            }
            else { // Can be a subcontext or an object.
                if (namesToObjects.containsKey(name)) {
                    Object o = namesToObjects.get(objName);
                    if (o instanceof Reference) {
                        Object instance;
                        try {
                            instance = NamingManager.getObjectInstance(o, null, null, getEnvironment());
                        }
                        catch (Exception e) {
                            LOGGER.error("", e);
                            NamingException namingException = new NamingException();
                            namingException.setRootCause(e);
                            throw namingException;
                        }
                        o = instance == o ? null : instance;
                        namesToObjects.put(objName, o);
                    }
                    return o;
                }
                if (subContexts.containsKey(name)) {
                    return subContexts.get(name);
                }
                LOGGER.debug("AbstractContext#lookup() {} not found in {}", name, this);
                throw new NameNotFoundException(name.toString());
            }
        }
    }

    /**
     * IMPROVE To be implemented? See {@link OperationNotSupportedException}.
     */
    @Nullable
    private Object newInstance() throws OperationNotSupportedException {
        Context clone;
        try {
            clone = (Context) this.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new OperationNotSupportedException();
        }
        return clone;
    }

    /**
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    @Override
    public Object lookup(@NotNull String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
     */
    @Override
    public void bind(@NotNull Name name, @Nullable Object object) throws NamingException {
        if(name.size() == 0) {
            throw new InvalidNameException("Cannot bind to an empty name.");
        }
        else if(name.size() > 1) {
            Name prefix = name.getPrefix(1);
            if(subContexts.containsKey(prefix)) {
                subContexts.get(prefix).bind(name.getSuffix(1), object);
            }
            else {
                LOGGER.error("No such subcontext: {} in {}", prefix, this);
                throw new NameNotFoundException(prefix + "");
            }
        }
        else {
            /* Determine if the name is already bound */
            if(namesToObjects.containsKey(name) ||
                    subContexts.containsKey(name)) {
                LOGGER.error("bind() {} already bound in {}", name, this);
                throw new NameAlreadyBoundException("Name " + name.toString()
                    + " already bound.  Use rebind() to override");
            }
            if (object instanceof Context) {
                subContexts.put(name, (Context) object);
            }
            else {
                namesToObjects.put(name, object);
            }
        }
    }

    /**
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    @Override
    public void bind(@NotNull String name, @Nullable Object object) throws NamingException {
        bind(nameParser.parse(name), object);
    }

    /**
     * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
     */
    @Override
    public void rebind(@NotNull Name name, @Nullable Object object) throws NamingException {
        if(name.isEmpty()) {
            throw new InvalidNameException("Cannot bind to empty name");
        }
        /* Look up the target context first. */
        Object targetContext = lookup(name.getPrefix(name.size() - 1));
        if(targetContext == null || !(targetContext instanceof Context)) {
            throw new NamingException("Cannot bind object.  Target context does not exist.");
        }
        unbind(name);
        bind(name, object);
    }

    /**
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    @Override
    public void rebind(@NotNull String name, @Nullable Object object) throws NamingException {
        rebind(nameParser.parse(name), object);
    }

    /**
     * @see javax.naming.Context#unbind(javax.naming.Name)
     */
    @Override
    public void unbind(@NotNull Name name) throws NamingException {
        if(name.isEmpty()) {
            throw new InvalidNameException("Cannot unbind to empty name");
        }
        else if(name.size() == 1) {
            namesToObjects.remove(name);
            subContexts.remove(name);
        }
        else {
            Object targetContext = lookup(name.getPrefix(name.size() - 1));
            if(targetContext == null || !(targetContext instanceof Context)) {
                NamingException e = new NamingException("Cannot unbind object.");
                LOGGER.error("Can not unbind object with name={} from targetContext={}.", name, targetContext);
                LOGGER.error("", e);
                throw e;
            }
            ((Context)targetContext).unbind(name.getSuffix(name.size() - 1));
        }


    }

    /**
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    @Override
    public void unbind(@NotNull String name) throws NamingException {
        unbind(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
     */
    @Override
    public void rename(@NotNull Name oldName, @NotNull Name newName) throws NamingException {
        /* Confirm that this works.  We might have to catch the exception */
        Object old = lookup(oldName);
        if(newName.isEmpty()) {
            throw new InvalidNameException("Cannot bind to empty name");
        }
        
        if(old == null) {
            throw new NamingException("Name '" + oldName + "' not found.");
        }

        /* If the new name is bound throw a NameAlreadyBoundException */
        if(lookup(newName) != null) {
            throw new NameAlreadyBoundException("Name '" + newName + "' already bound");
        }

        unbind(oldName);
        unbind(newName);
        bind(newName, old);
        /* If the object is a Thread, or a ThreadContext, give it the new name. */
        if(old instanceof Thread) {
            ((Thread)old).setName(newName.toString());
        }
    }

    /**
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    @Override
    public void rename(@NotNull String oldName, @NotNull String newName) throws NamingException {
        rename(nameParser.parse(oldName), nameParser.parse(newName));
    }
    /* End of Write-functionality */

    /* Start of List functionality */
    /**
     * @see javax.naming.Context#list(javax.naming.Name)
     */
    @Override
    public NamingEnumeration list(@NotNull Name name) throws NamingException {
//      if name is a directory, we should do the same as we do above
//      if name is a properties file, we should return the keys (?)
//      issues: default.properties ?
        if(name == null || name.isEmpty()) {
            /* Because there are two mappings that need to be used here,
             * create a new mapping and add the two maps to it.  This also 
             * adds the safety of cloning the two maps so the original is
             * unharmed. */
            Map enumStore = new HashMap();
            enumStore.putAll(namesToObjects);
            enumStore.putAll(subContexts);
            NamingEnumeration enumerator = new ContextNames(enumStore);
            return enumerator;
        }
        /* Look for a subcontext */
        Name subName = name.getPrefix(1);
        if(namesToObjects.containsKey(subName)) {
            /* Nope, actual object */
            throw new NotContextException(name + " cannot be listed");
        }
        if(subContexts.containsKey(subName)) {
            return subContexts.get(subName).list(name.getSuffix(1));
        }
        /* Couldn't find the subcontext and it wasn't pointing at us, throw
         * an exception. */
        /* IMPROVE: Give this a better message */
        throw new NamingException();
    }


    /**
     * @see javax.naming.Context#list(java.lang.String)
     */
    @Override
    public NamingEnumeration list(@NotNull String name) throws NamingException {
        return list(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#listBindings(javax.naming.Name)
     */
    @Override
    public NamingEnumeration listBindings(@NotNull Name name) throws NamingException {
        if(name == null || name.isEmpty()) {
            /* Because there are two mappings that need to be used here,
             * create a new mapping and add the two maps to it.  This also 
             * adds the safety of cloning the two maps so the original is
             * unharmed. */
            Map enumStore = new HashMap();
            enumStore.putAll(namesToObjects);
            enumStore.putAll(subContexts);
            return new ContextBindings(enumStore);
        }
        /* Look for a subcontext */
        Name subName = name.getPrefix(1);
        if(subContexts.containsKey(subName)) {
            return subContexts.get(subName).listBindings(name.getSuffix(1));
        }
        else {
        /* Couldn't find the subcontext and it wasn't pointing at us, throw an exception. */
            throw new NamingException("AbstractContext#listBindings(\"" + name + "\"): subcontext not found.");
        }
    }

    /**
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    @Override
    public NamingEnumeration listBindings(@NotNull String name) throws NamingException {
        return listBindings(nameParser.parse(name));
    }
    /* End of List functionality */

    /**
     * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
     */
    @Override
    public void destroySubcontext(Name name) throws NamingException {
        if(name.size() > 1) {
            if(subContexts.containsKey(name.getPrefix(1))) {
                Context subContext = subContexts.get(name.getPrefix(1));
                destroySubcontexts(subContext);
                return;
            } 
            /* IMPROVE: Better message might be necessary */
            throw new NameNotFoundException();
        }
        /* Look at the contextStore to see if the name is bound there */
        if(namesToObjects.containsKey(name)) {
            throw new NotContextException();
        }
        /* Look for the subcontext */
        if(!subContexts.containsKey(name)) {
            throw new NameNotFoundException();
        }
        Context subContext = subContexts.get(name);
        destroySubcontexts(subContext);
        subContext.close();
        subContexts.remove(name);
    }

    private void destroySubcontexts(Context context) throws NamingException {
        NamingEnumeration<Binding> bindings = context.listBindings("");
        while (bindings.hasMore()) {
            final Binding binding = bindings.next();
            // Context.listBindings() may only be called with subcontexts.
            String name = binding.getName();
            if (binding.getObject() instanceof Context) {
                Context subContext = (Context) binding.getObject();
                destroySubcontexts(subContext);
                context.destroySubcontext(name);
            }
            else {
                // CompoundName compoundName = new CompoundName(binding.getName(), new Properties());
                LOGGER.trace("Unbind {}", name);
                // Here name is always a single component name. To force handling as such:
                Properties syntax = new Properties();
                syntax.setProperty("jndi.syntax.direction", "flat");
                context.unbind(new CompoundName(name, syntax));
            }
        }
    }

    /**
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    @Override
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#createSubcontext(javax.naming.Name)
     */
    @Override
    public Context createSubcontext(Name name) throws NamingException {
        Context newContext;

        if(name.size() > 1) {
            if(subContexts.containsKey(name.getPrefix(1))) {
                Context subContext = subContexts.get(name.getPrefix(1));
                newContext = subContext.createSubcontext(name.getSuffix(1));
                return newContext;
            }
            else {
                throw new NameNotFoundException("The subcontext " + name.getPrefix(1) + " was not found (" + name + ").");
            }
        }
        try {
            lookup(name);
        }
        catch (NameNotFoundException ignore) { }

        Name contextName = getNameParser((Name)null).parse(getNameInNamespace());
        contextName.addAll(name);
        newContext = new MemoryContext(this.env);
        ((MemoryContext)newContext).setNameInNamespace(contextName);
        bind(name, newContext);
        return newContext;
    }

    /**
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#lookupLink(javax.naming.Name)
     */
    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name);
    }

    /**
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#getNameParser(javax.naming.Name)
     */
    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        /* Not sure this conditional is adequate. It might still cause problems with foo.foo name structures. */
        if(name == null ||
           name.isEmpty() || 
           (name.size() == 1 && name.toString().equals(getNameInNamespace()))) {
            return nameParser;
        }
        Name subName = name.getPrefix(1); 
        if(subContexts.containsKey(subName)) {
            return subContexts.get(subName).getNameParser(name.getSuffix(1));
        }
        throw new NotContextException();
    }

    /**
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
     */
    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        // XXX: NO IDEA IF THIS IS RIGHT
        if(name == null || prefix == null) {
            throw new NamingException("Arguments must not be null");
        }
        Name retName = (Name)prefix.clone();
        retName.addAll(name);
        return retName;
    }

    /**
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    @Override
    public String composeName(String name, String prefix) throws NamingException {
        Name retName = composeName(nameParser.parse(name), nameParser.parse(prefix));
        /* toString pretty much is guaranteed to exist */
        return retName.toString();
    }

    /**
     * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
     */
    @Override
    public Object addToEnvironment(String name, Object object) {
        if(this.env == null) {
            return null;
        }
        return this.env.put(name, object);
    }

    /**
     * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
     */
    @Override
    public Object removeFromEnvironment(String name) {
        if(this.env == null) {
            return null;
        }
        return this.env.remove(name);
    }

    /**
     * @see javax.naming.Context#getEnvironment()
     */
    @Override
    public Hashtable getEnvironment() {
        if(this.env == null) {
            return new Hashtable();
        }
        return (Hashtable)this.env.clone();
    }

    /**
     * @see javax.naming.Context#close()
     */
    @Override
    public void close() throws NamingException {
        String ignoreClose = (String) env.get(IGNORE_CLOSE);
        if (!BooleanUtils.toBoolean(ignoreClose)) {
            forceClose();
        }
    }

    /**
     * @see javax.naming.Context#getNameInNamespace()
     */
    @Override
    public String getNameInNamespace() {
        return nameInNamespace.toString();
    }

    /* **********************************************************************
     * Implementation other methods used by the Context.                    *
     * **********************************************************************/

    /**
     * Determine whether or not the context is empty.  Objects bound directly
     * to the context or subcontexts are all that is considered.  The 
     * environment of the context is not considered.
     *    
     * @return true of the context is empty, else false.
     */
    public boolean isEmpty() {
        return (namesToObjects.size() > 0 || subContexts.size() > 0);
    }

    /**
     * Set the name of the Context.  This is only used from createSubcontext. 
     * It might get replaced by adding more constructors, but there is really
     * no reason to expose it publicly anyway.
     * 
     * @param name the Name of the context.
     * @throws NamingException if the subContext already has a name.
     */
    private void setNameInNamespace(Name name) throws NamingException {
        if(nameLock) {
            if(nameInNamespace != null || !nameInNamespace.isEmpty()) {
                LOGGER.error("Name already set: name={} nameInNamespace={} this={}", name, nameInNamespace, this);
                throw new NamingException("Name already set.");
            }
        }
        nameInNamespace = name;
        nameLock = true;
    }

    public void forceClose() throws NamingException {
        destroySubcontexts(this);
        env = null;
        namesToObjects = null;
        subContexts = null;
    }
}



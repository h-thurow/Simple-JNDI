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

import javax.naming.*;
import java.util.*;

/**
 * The heart of the system, the abstract implementation of context for 
 * simple-jndi.  There are no abstract methods in this class, but it is
 * not meant to be instantiated, but extended instead.
 *
 * @author Robert M. Zigweid
 * @since Simple-JNDI 0.11
 * @version $Rev: 2684 $ $Date: 2008-05-24 22:19:06 -0700 (Sat, 24 May 2008) $
 */
public abstract class AbstractContext implements Cloneable, Context  {

    // table is used as a read-write cache which sits
    // above the file-store
    private Hashtable table = new Hashtable();
    private Hashtable subContexts = new Hashtable();
    private Hashtable env = new Hashtable();
    private NameParser nameParser;
    /* 
     * The name full name of this context. 
     */
    private Name nameInNamespace = null;
    private boolean nameLock = false;

    /* **********************************************************************
     * Constructors.                                                        *
     * Even though this class cannot be instantiated, it provides default   *
     * implemenation for doing so in hopes of making Contexts that extend   *
     * this class easier.                                                   *
     * **********************************************************************/
    /**
     * Creates a AbstractContext.
     */
    protected AbstractContext() {
        this((Hashtable)null);
    }
    
    /**
     * Creates a AbstractContext.<br>
     * By default allow system properties to override.
     * 
     * @param env a Hashtable containing the Context's environemnt.
     */
    protected AbstractContext(Hashtable env) {
        /* By default allow system properties to override. */
        this(env, true, null);
    }
    
    /**
     * Creates a AbstractContext.
     * 
     * @param env a Hashtable containing the Context's environment.
     * @param systemOverride allow System Parameters to override the
     *        environment that is passed in.
     */
    protected AbstractContext(Hashtable env, boolean systemOverride) {
        this(env, systemOverride, null);
    }

    /**
     * Creates a AbstractContext.
     * 
     * @param env a Hashtable containing the Context's environment.
     * @param parser the NameParser being used by the Context.
     */
    protected AbstractContext(Hashtable env, NameParser parser) {
        this(env, true, parser);
    }

    /**
     * Creates a AbstractContext.
     * 
     * @param systemOverride allow System Parameters to override the
     *        environment that is passed in.
     */
    protected AbstractContext(boolean systemOverride) {
        this(null, systemOverride, null);
    }

    /**
     * Creates a AbstractContext.
     * 
     * @param systemOverride allow System Parameters to override the
     *        environment that is passed in.
     * @param parser the NameParser being used by the Context.
     */
    protected AbstractContext(boolean systemOverride, NameParser parser) {
        this(null, systemOverride, parser);
    }

    /**
     * Creates a AbstractContext.
     * 
     * @param parser the NameParser being used by the Context.
     */
    protected AbstractContext(NameParser parser) {
        this(null, true, parser);
    }

    /**
     * Creates a AbstractContext.
     * 
     * @param env a Hashtable containing the Context's environment.
     * @param systemOverride allow System Parameters to override the
     *        environment that is passed in. TODO Not supported. Support?
     * @param parser the NameParser being used by the Context.
     */
    protected AbstractContext(Hashtable env, boolean systemOverride, NameParser parser) {
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
            // TODO Auto-generated catch block
            /* This shouldn't be an issue at this point */
            e.printStackTrace();
        }
    }

    /**
     * Create a new context based upon the environment of the passed context.
     * @param that
     */
    protected AbstractContext(AbstractContext that) {
        this(that.env);
    }

    
    /* **********************************************************************
     * Implementation of methods specified by java.lang.naming.Context      *
     * **********************************************************************/
    /** 
     * Return the named object.  
     * This implementation looks for things in the following order:<br/>
     * <ol>
     * <li>The empty element to duplicate the context.</li>
     * <li>A named object in the environment.</li>
     * <li>A named object in the Context's store</li>
     * <li>A named sub-Context of this Context</li>
     * </ol>
     * Unlike many implementations of the JNDI specification, this 
     * implementation returns null when a name is not found, rather than 
     * throwing an exception. The specification does not appear to state 
     * which way it should be. 
     *
     * @see javax.naming.Context#lookup(javax.naming.Name)
     */
    @Override
    public Object lookup(Name name) throws NamingException {
        /* 
         * The string form of the name will be used in several places below 
         * if not matched in the hashtable.
         */
        String stringName = name.toString();
        /*
         * If name is empty then this context is to be cloned.  This is 
         * required based upon the javadoc of Context.  UGH!
         */
        if(name.size() == 0) {
            Object ret = null;
            try {
                ret = (AbstractContext)this.clone();
            } catch(CloneNotSupportedException e) {
                /* 
                 * TODO: Improve error handling.  I'm not quite sure yet what 
                 *       should be done, but this almost certainly isn't it.
                 */
                e.printStackTrace();
            }
            if(ret != null) {
                return ret;
            }
        }

        Name objName = name.getPrefix(1);
        if(name.size() > 1) {
            /* Look in a subcontext. */
            if(subContexts.containsKey(objName)) {
                return ((Context)subContexts.get(objName)).lookup(name.getSuffix(1));
            }
            throw new NamingException("Invalid subcontext '" + objName.toString() + "' in context '" 
                            + getNameInNamespace() + "'");
        }
        
        /* Lookup the object in this context */
        if(table.containsKey(name)) {
            return table.get(objName);
        }
        
        /* 
         * Lookup the object from the subcontexts table and return it if found.
         */
        if(subContexts.containsKey(name)) {
            return subContexts.get(name);
        }
        /* Nothing could be found.  Return null. */
        /*
         * XXX: Is this right?  Should a NamingException be thrown here 
         *      instead because nothing could be found?
         */
        return null;
    }

    /**
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    @Override
    public Object lookup(String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
     */
    @Override
    public void bind(Name name, Object object) throws NamingException {
        /* 
         * If the name of obj doesn't start with the name of this context, 
         * it is an error, throw a NamingException
         */
        if(name.size() > 1) {
            Name prefix = name.getPrefix(1);
            if(subContexts.containsKey(prefix)) {
                ((Context)subContexts.get(prefix)).bind(name.getSuffix(1), object);
                return;
            }
        }
        if(name.size() == 0) {
            throw new InvalidNameException("Cannot bind to an empty name");
        }
        /* Determine if the name is already bound */
        if(table.containsKey(name) ||
                subContexts.containsKey(name)) {
            throw new NameAlreadyBoundException("Name " + name.toString()
                + " already bound.  Use rebind() to override");
        }
        
        if(object instanceof Context) {
            subContexts.put(name, object);
        } else {
            table.put(name, object);
        }
    }

    /**
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    @Override
    public void bind(String name, Object object) throws NamingException {
        bind(nameParser.parse(name), object);
    }

    /**
     * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
     */
    @Override
    public void rebind(Name name, Object object) throws NamingException {
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
    public void rebind(String name, Object object) throws NamingException {
        rebind(nameParser.parse(name), object);
    }

    /**
     * @see javax.naming.Context#unbind(javax.naming.Name)
     */
    @Override
    public void unbind(Name name) throws NamingException {
        if(name.isEmpty()) {
            throw new InvalidNameException("Cannot unbind to empty name");
        }

        if(name.size() == 1) {
            if(table.containsKey(name)) {
                table.remove(name);
            }
            if (subContexts.containsKey(name)) {
                subContexts.remove(name);
            }
            return;
        }
        
        /* Look up the target context first. */
        Object targetContext = lookup(name.getPrefix(name.size() - 1));
        if(targetContext == null || !(targetContext instanceof Context)) {
            throw new NamingException("Cannot unbind object.  Target context does not exist.");
        }
        ((Context)targetContext).unbind(name.getSuffix(name.size() - 1));
    }

    /**
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    @Override
    public void unbind(String name) throws NamingException {
        unbind(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
     */
    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
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
        /* 
         * If the object is a Thread, or a ThreadContext, give it the new 
         * name.
         */
        if(old instanceof Thread) {
            ((Thread)old).setName(newName.toString());
        }
    }

    /**
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(nameParser.parse(oldName), nameParser.parse(newName));
    }
    /* End of Write-functionality */

    /* Start of List functionality */
    /**
     * @see javax.naming.Context#list(javax.naming.Name)
     */
    @Override
    public NamingEnumeration list(Name name) throws NamingException {
//      if name is a directory, we should do the same as we do above
//      if name is a properties file, we should return the keys (?)
//      issues: default.properties ?
        if(name == null || name.isEmpty()) {
            /* 
             * Because there are two mappings that need to be used here, 
             * create a new mapping and add the two maps to it.  This also 
             * adds the safety of cloning the two maps so the original is
             * unharmed.
             */
            Map enumStore = new HashMap();
            enumStore.putAll(table);
            enumStore.putAll(subContexts);
            NamingEnumeration enumerator = new ContextNames(enumStore);
            return enumerator;
        }
        /* Look for a subcontext */
        Name subName = name.getPrefix(1);
        if(table.containsKey(subName)) {
            /* Nope, actual object */
            throw new NotContextException(name + " cannot be listed");
        }
        if(subContexts.containsKey(subName)) {
            return ((Context)subContexts.get(subName)).list(name.getSuffix(1));
        }
        /* 
         * Couldn't find the subcontext and it wasn't pointing at us, throw
         * an exception.
         */
        /* TODO: Give this a better message */
        throw new NamingException();
    }


    /**
     * @see javax.naming.Context#list(java.lang.String)
     */
    @Override
    public NamingEnumeration list(String name) throws NamingException {
        return list(nameParser.parse(name));
    }

    /**
     * @see javax.naming.Context#listBindings(javax.naming.Name)
     */
    @Override
    public NamingEnumeration listBindings(Name name) throws NamingException {
        if(name == null || name.isEmpty()) {
            /* 
             * Because there are two mappings that need to be used here, 
             * create a new mapping and add the two maps to it.  This also 
             * adds the safety of cloning the two maps so the original is
             * unharmed.
             */
            Map enumStore = new HashMap();
            enumStore.putAll(table);
            enumStore.putAll(subContexts);
            return new ContextBindings(enumStore);
        }
        /* Look for a subcontext */
        Name subName = name.getPrefix(1);
        if(table.containsKey(subName)) {
            /* Nope, actual object */
            throw new NotContextException(name + " cannot be listed");
        }
        if(subContexts.containsKey(subName)) {
            return ((Context)subContexts.get(subName)).listBindings(name.getSuffix(1));
        }
        /* 
         * Couldn't find the subcontext and it wasn't pointing at us, throw
         * an exception.
         */
        throw new NamingException();
    }

    /**
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    @Override
    public NamingEnumeration listBindings(String name) throws NamingException {
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
                Context subContext = (Context)subContexts.get(name.getPrefix(1));
                destroySubcontexts(subContext);
                return;
            } 
            /* TODO: Better message might be necessary */
            throw new NameNotFoundException();
        }
        /* Look at the contextStore to see if the name is bound there */
        if(table.containsKey(name)) {
            throw new NotContextException();
        }
        /* Look for the subcontext */
        if(!subContexts.containsKey(name)) {
            throw new NameNotFoundException();
        }
        Context subContext = (Context)subContexts.get(name);
        destroySubcontexts(subContext);
        subContext.close();
        subContexts.remove(name);
    }

    private void destroySubcontexts(Context context) throws NamingException {
        NamingEnumeration<Binding> bindings = context.listBindings("");
        while (bindings.hasMore()) {
            final Binding binding = bindings.next();
            try {
                final NamingEnumeration<Binding> enumeration
                        = context.listBindings(binding.getName());
                if (enumeration.hasMore()) {
                    destroySubcontexts((Context) context.lookup(binding.getName()));
                }
            }
            catch (NotContextException e) {
                context.unbind(binding.getName());
            }
        }
        bindings = context.listBindings("");
        while (bindings.hasMore()) {
            final Binding binding = bindings.next();
            final NamingEnumeration<Binding> enumeration
                    = context.listBindings(binding.getName());
            if (enumeration.hasMore()) {
                destroySubcontexts((Context) context.lookup(binding.getName()));
            }
            context.destroySubcontext(binding.getName());
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
    public abstract Context createSubcontext(Name name) throws NamingException;
    /* TODO: Put this example implemenation into the javadoc.
    /* Example implementation 
    public Context createSubcontext(Name name) throws NamingException {
        Context newContext;
        Hashtable subContexts = getSubContexts();

        if(name.size() > 1) {
            if(subContexts.containsKey(name.getPrefix(1))) {
                Context subContext = (Context)subContexts.get(name.getPrefix(1));
                newContext = subContext.createSubcontext(name.getSuffix(1));
                return newContext;
            } 
            throw new NameNotFoundException("The subcontext " + name.getPrefix(1) + " was not found.");
        }
        
        if(lookup(name) != null) {
            throw new NameAlreadyBoundException();
        }

        Name contextName = getNameParser(getNameInNamespace())
            .parse(getNameInNamespace());
        contextName.addAll(name);
        newContext = new GenericContext(this);
        ((AbstractContext)newContext).setName(contextName);
        subContexts.put(name, newContext);
        return newContext;
    }

    */
    
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
        /* 
         * XXX: Not sure this conditional is adequate.  It might still cause
         *      problems with foo.foo name structures.
         */
        if(name == null ||
           name.isEmpty() || 
           (name.size() == 1 && name.toString().equals(getNameInNamespace()))) {
            return nameParser;
        }
        Name subName = name.getPrefix(1); 
        if(subContexts.containsKey(subName)) {
            return ((Context)subContexts.get(subName)).getNameParser(name.getSuffix(1));
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
    public Object addToEnvironment(String name, Object object) throws NamingException {
        if(this.env == null) {
            return null;
        }
        return this.env.put(name, object);
    }

    /**
     * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
     */
    @Override
    public Object removeFromEnvironment(String name) throws NamingException {
        if(this.env == null) {
            return null;
        }
        return this.env.remove(name);
    }

    /**
     * @see javax.naming.Context#getEnvironment()
     */
    @Override
    public Hashtable getEnvironment() throws NamingException {
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
        destroySubcontexts(this);

        // TODO This block is never entered by current tests.
        while(table.size() > 0 || subContexts.size() > 0) {
            Iterator it = table.keySet().iterator();
            List toRemove = new LinkedList();
            while(it.hasNext()) {
                Name name = (Name)it.next();

                Object entry = table.get(name);

                if(entry instanceof Thread) {
                    Thread thread = (Thread) entry;
                    if(thread.isAlive()) {
                        toRemove.add(name);
                    }
                } else {
                    toRemove.add(name);
                }
            }
            for(it = toRemove.iterator(); it.hasNext();) {
                table.remove(it.next());
            }

            toRemove.clear();
            it = subContexts.keySet().iterator();
            while(it.hasNext()) {
                Name name = (Name)it.next();
                AbstractContext context = (AbstractContext)subContexts.get(name);
                if(context.isEmpty()) {
                    toRemove.add(name);
                }
            }
            for(it = toRemove.iterator(); it.hasNext();) {
                subContexts.remove(it.next());
            }
        }
        this.env = null;
        this.table = null;
    }

    /**
     * @see javax.naming.Context#getNameInNamespace()
     */
    @Override
    public String getNameInNamespace() throws NamingException {
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
        return (table.size() > 0 || subContexts.size() > 0);
    }

    /**
     * Set the name of the Context.  This is only used from createSubcontext. 
     * It might get replaced by adding more constructors, but there is really
     * no reason to expose it publicly anyway.
     * 
     * @param name the Name of the context.
     * @throws NamingException if the subContext already has a name.
     */
    public void setNameInNamespace(Name name) throws NamingException {
        if(nameLock) {
            if(nameInNamespace != null || !nameInNamespace.isEmpty()) {
                throw new NamingException("Name already set.");
            }
        }
        nameInNamespace = name;
        nameLock = true;
    }
    
    /**
     * Set the name of the Context.  This is only used from createSubcontext. 
     * It might get replaced by adding more constructors, but there is really
     * no reason to expose it publicly anyway.
     * 
     * @param name a String representation of the Name of the context.
     * @throws NamingException if the subContext already has a name.
     */
    protected void setNameInNamespace(String name) throws NamingException {
        setNameInNamespace(nameParser.parse(name));
    }

    /**
     * Convenience method returning the subcontexts that this context parents.
     * @return a Hashtable of context objects that are parented by this 
     *         context.
     */
    protected Hashtable getSubContexts() {
        return (Hashtable)subContexts.clone();
    }
}



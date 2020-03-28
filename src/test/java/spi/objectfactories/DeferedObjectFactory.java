package spi.objectfactories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 01.03.20
 */
public class DeferedObjectFactory implements ObjectFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeferedObjectFactory.class);

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        throw new DeferedObjectFactoryException("defered object created.");
    }
}

class DeferedObjectFactoryException extends Exception {
    public DeferedObjectFactoryException()
    {
        super();
    }

    public DeferedObjectFactoryException(final String message)
    {
        super(message);
    }

    public DeferedObjectFactoryException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public DeferedObjectFactoryException(final Throwable cause)
    {
        super(cause);
    }

    protected DeferedObjectFactoryException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
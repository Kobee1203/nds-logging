package org.nds.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Factory for creating {@link Logger} instances
 * </p>
 * 
 * @author Nicolas Dos Santos
 * 
 */
public class LoggerFactory {

    /**
     * Instance of LoggerFactory singleton
     */
    private static LoggerFactory factory = null;

    /**
     * The {@link Logger} instances that have already been created, keyed by logger name.
     */
    private final Map<String, Logger> instances = new HashMap<String, Logger>();

    private LoggerFactory() {
    }

    private final synchronized static LoggerFactory getInstance() {
        if (factory == null) {
            factory = new LoggerFactory();
        }
        return factory;
    }

    public final static Logger getLogger(String name, String tag) {
        Logger logger = getInstance().instances.get(name);
        if (logger == null) {
            logger = new Logger(name, tag);
        }
        return logger;
    }

    public final static Logger getLogger(String name) {
        return getLogger(name, null);
    }

    public final static Logger getLogger(Class<?> clazz, String tag) {
        return getLogger(clazz.getName(), tag);
    }

    public final static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz, null);
    }
}

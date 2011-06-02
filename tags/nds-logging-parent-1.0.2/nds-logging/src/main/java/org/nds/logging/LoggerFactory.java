package org.nds.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.AndroidLogFactory;
import org.apache.commons.logging.LogFactory;

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

    private static boolean androidLoggable = false;

    /**
     * The {@link Logger} instances that have already been created, keyed by logger name.
     */
    private final Map<String, Logger> instances = new HashMap<String, Logger>();

    private LoggerFactory() {
        try {
            if (android.os.Build.ID != null) {
                androidLoggable = true;
            }
        } catch (Exception e) {
        }
    }

    private final synchronized static LoggerFactory getInstance() {
        if (factory == null) {
            factory = new LoggerFactory();
        }
        return factory;
    }

    public final static Logger getLogger(String name) {
        Logger logger = getInstance().instances.get(name);
        if (logger == null) {
            if (androidLoggable) {
                logger = new Logger(name, AndroidLogFactory.getLog(name));
            } else {
                logger = new Logger(name, LogFactory.getLog(name));
            }
            getInstance().instances.put(name, logger);
        }
        return logger;
    }

    public final static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}

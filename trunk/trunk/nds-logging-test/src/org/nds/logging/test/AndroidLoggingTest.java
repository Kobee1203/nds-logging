package org.nds.logging.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.impl.SimpleLog;
import org.nds.logging.Logger;
import org.nds.logging.LoggerFactory;

import android.test.AndroidTestCase;

public class AndroidLoggingTest extends AndroidTestCase {

    private static final Logger log2 = LoggerFactory.getLogger("TAGTEST");
    private static final Logger log = LoggerFactory.getLogger(AndroidLoggingTest.class);

    public AndroidLoggingTest() {
    }

    @Override
    protected void setUp() throws Exception {
    }

    public void testLogger() throws InterruptedException {
        System.out.println(AndroidLoggingTest.class.getClassLoader());
        getConfigurationFile(AndroidLoggingTest.class.getClassLoader(), "commons-logging.properties");
        Properties props = getConfigurationFile(AndroidLoggingTest.class.getClassLoader(), "simplelog.properties");
        InputStream in = getResourceAsStream("simplelog.properties");
        in = getContextClassLoader().getResourceAsStream("/simplelog.properties");
        in = getClass().getResourceAsStream("/simplelog.properties");
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.trace("test log trace/verbose");
        log.debug("test log debug");
        log.info("test log info.");
        log.warn("test log warning");
        log.error("test log error");
    }

    public void testLogger2() throws InterruptedException {
        log2.trace("test2 log trace/verbose");
        log2.debug("test2 log debug");
        log2.info("test2 log info.");
        log2.warn("test2 log warning");
        log2.error("test2 log error");
    }

    private InputStream getResourceAsStream(final String name) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader threadCL = getContextClassLoader();

                if (threadCL != null) {
                    InputStream is = threadCL.getResourceAsStream(name);
                    return is;
                } else {
                    InputStream is = ClassLoader.getSystemResourceAsStream(name);
                    return is;
                }
            }
        });
    }

    private ClassLoader getContextClassLoader() {
        ClassLoader classLoader = null;

        if (classLoader == null) {
            try {
                // Are we running on a JDK 1.2 or later system?
                Method method = Thread.class.getMethod("getContextClassLoader", (Class[]) null);

                // Get the thread context class loader (if there is one)
                try {
                    classLoader = (ClassLoader) method.invoke(Thread.currentThread(), (Class[]) null);
                } catch (IllegalAccessException e) {
                    ; // ignore
                } catch (InvocationTargetException e) {
                    /**
                     * InvocationTargetException is thrown by 'invoke' when the method being invoked (getContextClassLoader) throws an exception.
                     * 
                     * getContextClassLoader() throws SecurityException when the context class loader isn't an ancestor of the calling class's class
                     * loader, or if security permissions are restricted.
                     * 
                     * In the first case (not related), we want to ignore and keep going. We cannot help but also ignore the second with the logic
                     * below, but other calls elsewhere (to obtain a class loader) will trigger this exception where we can make a distinction.
                     */
                    if (e.getTargetException() instanceof SecurityException) {
                        ; // ignore
                    } else {
                        // Capture 'e.getTargetException()' exception for details
                        // alternate: log 'e.getTargetException()', and pass back 'e'.
                        throw new LogConfigurationException("Unexpected InvocationTargetException", e.getTargetException());
                    }
                }
            } catch (NoSuchMethodException e) {
                // Assume we are running on JDK 1.1
                ; // ignore
            }
        }

        if (classLoader == null) {
            classLoader = SimpleLog.class.getClassLoader();
        }

        // Return the selected class loader
        return classLoader;
    }

    private Properties getProperties(final URL url) {
        PrivilegedAction action = new PrivilegedAction() {
            public Object run() {
                try {
                    InputStream stream = url.openStream();
                    if (stream != null) {
                        Properties props = new Properties();
                        props.load(stream);
                        stream.close();
                        return props;
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }

                return null;
            }
        };
        return (Properties) AccessController.doPrivileged(action);
    }

    private final Properties getConfigurationFile(ClassLoader classLoader, String fileName) {

        Properties props = null;
        double priority = 0.0;
        URL propsUrl = null;
        try {
            Enumeration urls = getResources(classLoader, fileName);

            if (urls == null) {
                return null;
            }

            while (urls.hasMoreElements()) {
                URL url = (URL) urls.nextElement();

                Properties newProps = getProperties(url);
                if (newProps != null) {
                    if (props == null) {
                        propsUrl = url;
                        props = newProps;
                        String priorityStr = props.getProperty("priority");
                        priority = 0.0;
                        if (priorityStr != null) {
                            priority = Double.parseDouble(priorityStr);
                        }

                        System.err.println("[LOOKUP] Properties file found at '" + url + "'" + " with priority " + priority);
                    } else {
                        String newPriorityStr = newProps.getProperty("priority");
                        double newPriority = 0.0;
                        if (newPriorityStr != null) {
                            newPriority = Double.parseDouble(newPriorityStr);
                        }

                        if (newPriority > priority) {
                            System.err.println("[LOOKUP] Properties file at '" + url + "'" + " with priority " + newPriority + " overrides file at '"
                                    + propsUrl + "'" + " with priority " + priority);

                            propsUrl = url;
                            props = newProps;
                            priority = newPriority;
                        } else {
                            System.err.println("[LOOKUP] Properties file at '" + url + "'" + " with priority " + newPriority
                                    + " does not override file at '" + propsUrl + "'" + " with priority " + priority);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SecurityException thrown while trying to find/read config files.");
        }

        if (props == null) {
            System.err.println("[LOOKUP] No properties file of name '" + fileName + "' found.");
        } else {
            System.err.println("[LOOKUP] Properties file of name '" + fileName + "' found at '" + propsUrl + '"');
        }

        return props;
    }

    private Enumeration getResources(final ClassLoader loader, final String name) {
        PrivilegedAction action = new PrivilegedAction() {
            public Object run() {
                try {
                    if (loader != null) {
                        return loader.getResources(name);
                    } else {
                        return ClassLoader.getSystemResources(name);
                    }
                } catch (IOException e) {
                    System.err.println("Exception while trying to find configuration file " + name + ":" + e.getMessage());
                    return null;
                } catch (NoSuchMethodError e) {
                    // we must be running on a 1.1 JVM which doesn't support
                    // ClassLoader.getSystemResources; just return null in
                    // this case.
                    return null;
                }
            }
        };
        Object result = AccessController.doPrivileged(action);
        return (Enumeration) result;
    }
}

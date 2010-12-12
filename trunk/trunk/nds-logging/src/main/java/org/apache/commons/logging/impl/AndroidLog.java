package org.apache.commons.logging.impl;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.LogConfigurationException;

import android.util.Log;

/**
 * <p>
 * Implementation of Log for Android platform that sends all enabled log messages, for all defined loggers, to the Android logging System. The
 * following system properties are supported to configure the behavior of this logger:
 * </p>
 * <ul>
 * <li><code>org.apache.commons.logging.androidlog.defaultlog</code> - Default logging detail level for all instances of AndroidLog. Must be one of
 * ("trace", "debug", "info", "warn", "error", or "fatal"). If not specified, defaults to "info".</li>
 * <li><code>org.apache.commons.logging.androidlog.log.xxxxx</code> - Logging detail level for a AndroidLog instance named "xxxxx". Must be one of
 * ("trace", "debug", "info", "warn", "error", or "fatal"). If not specified, the default logging detail level is used.</li>
 * <li><code>org.apache.commons.logging.androidlog.showlogname</code> - Set to <code>true</code> if you want the Log instance name to be included in
 * output messages. Defaults to <code>false</code>.</li>
 * <li><code>org.apache.commons.logging.androidlog.showShortLogname</code> - Set to <code>true</code> if you want the last component of the name to be
 * included in output messages. Defaults to <code>true</code>.</li>
 * <li><code>org.apache.commons.logging.androidlog.showdatetime</code> - Set to <code>true</code> if you want the current date and time to be included
 * in output messages. Default is <code>false</code>.</li>
 * <li><code>org.apache.commons.logging.androidlog.dateTimeFormat</code> - The date and time format to be used in the output messages. The pattern
 * describing the date and time format is the same that is used in <code>java.text.SimpleDateFormat</code>. If the format is not specified or is
 * invalid, the default format is used. The default format is <code>yyyy/MM/dd HH:mm:ss:SSS zzz</code>.</li>
 * </ul>
 * 
 * <p>
 * In addition to looking for system properties with the names specified above, this implementation also checks for a class loader resource named
 * <code>"androidlog.properties"</code>, and includes any matching definitions from this resource (if it exists).
 * </p>
 * 
 * @author Nicolas Dos Santos
 */
public class AndroidLog extends SimpleLog {

    private static final long serialVersionUID = -7864001067995532343L;

    /** All system properties used by <code>AndroidLog</code> start with this */
    static protected final String systemPrefix = "org.apache.commons.logging.androidlog.";

    /** Properties loaded from androidlog.properties */
    static protected final Properties androidLogProps = new Properties();

    /** The short name of this simple log instance */
    private String shortLogName = null;

    public AndroidLog(String name) {
        super(name);
    }

    // ------------------------------------------------------------ Initializer

    private static String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
            ; // Ignore
        }
        return (prop == null) ? androidLogProps.getProperty(name) : prop;
    }

    private static String getStringProperty(String name, String dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : prop;
    }

    private static boolean getBooleanProperty(String name, boolean dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : "true".equalsIgnoreCase(prop);
    }

    // Initialize class attributes.
    // Load properties file, if found.
    // Override with system properties.
    static {
        // Add props from the resource androidlog.properties
        InputStream in = getResourceAsStream("androidlog.properties");
        if (null != in) {
            try {
                androidLogProps.load(in);
                in.close();
            } catch (java.io.IOException e) {
                // ignored
            }
        }

        showLogName = getBooleanProperty(systemPrefix + "showlogname", showLogName);
        showShortName = getBooleanProperty(systemPrefix + "showShortLogname", showShortName);
        showDateTime = getBooleanProperty(systemPrefix + "showdatetime", showDateTime);

        if (showDateTime) {
            dateTimeFormat = getStringProperty(systemPrefix + "dateTimeFormat", dateTimeFormat);
            try {
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            } catch (IllegalArgumentException e) {
                // If the format pattern is invalid - use the default format
                dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            }
        }
    }

    /**
     * Return the thread context class loader if available. Otherwise return null.
     * 
     * The thread context class loader is available for JDK 1.2 or later, if certain security conditions are met.
     * 
     * @exception LogConfigurationException
     *                if a suitable class loader cannot be identified.
     */
    private static ClassLoader getContextClassLoader() {
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
            classLoader = AndroidLog.class.getClassLoader();
        }

        // Return the selected class loader
        return classLoader;
    }

    private static InputStream getResourceAsStream(final String name) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader threadCL = getContextClassLoader();

                if (threadCL != null) {
                    return threadCL.getResourceAsStream(name);
                } else {
                    return ClassLoader.getSystemResourceAsStream(name);
                }
            }
        });
    }

    /**
     * <p>
     * Do the actual logging. This method assembles the message and then calls <code>write()</code> to cause it to be written.<br/>
     * Calls {@link android.util.Log} to write log for the Android platform.
     * </p>
     * 
     * @param type
     *            One of the LOG_LEVEL_XXX constants defining the log level
     * @param message
     *            The message itself (typically a String)
     * @param t
     *            The exception whose stack trace should be logged
     */
    @Override
    protected void log(int type, Object message, Throwable t) {
        // Use a string buffer for better performance
        StringBuffer buf = new StringBuffer();

        // Append date-time if so configured
        if (showDateTime) {
            Date now = new Date();
            String dateText;
            synchronized (dateFormatter) {
                dateText = dateFormatter.format(now);
            }
            buf.append(dateText);
            buf.append(" ");
        }

        // Append a readable representation of the log level
        switch (type) {
            case SimpleLog.LOG_LEVEL_TRACE:
                buf.append("[TRACE] ");
                break;
            case SimpleLog.LOG_LEVEL_DEBUG:
                buf.append("[DEBUG] ");
                break;
            case SimpleLog.LOG_LEVEL_INFO:
                buf.append("[INFO] ");
                break;
            case SimpleLog.LOG_LEVEL_WARN:
                buf.append("[WARN] ");
                break;
            case SimpleLog.LOG_LEVEL_ERROR:
                buf.append("[ERROR] ");
                break;
            case SimpleLog.LOG_LEVEL_FATAL:
                buf.append("[FATAL] ");
                break;
        }

        // Append the name of the log instance if so configured
        if (showShortName) {
            if (shortLogName == null) {
                // Cut all but the last component of the name for both styles
                shortLogName = logName.substring(logName.lastIndexOf(".") + 1);
                shortLogName = shortLogName.substring(shortLogName.lastIndexOf("/") + 1);
            }
            buf.append(String.valueOf(shortLogName)).append(" - ");
        } else if (showLogName) {
            buf.append(String.valueOf(logName)).append(" - ");
        }

        // Append the message
        buf.append(String.valueOf(message));

        // Append stack trace if not null
        if (t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");

            java.io.StringWriter sw = new java.io.StringWriter(1024);
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            buf.append(sw.toString());
        }

        // Print to the appropriate destination
        write(buf);

        switch (type) {
            case SimpleLog.LOG_LEVEL_TRACE:
                Log.v(logName, buf.toString());
                break;
            case SimpleLog.LOG_LEVEL_DEBUG:
                Log.d(logName, buf.toString());
                break;
            case SimpleLog.LOG_LEVEL_INFO:
                Log.i(logName, buf.toString());
                break;
            case SimpleLog.LOG_LEVEL_WARN:
                Log.w(logName, buf.toString());
                break;
            case SimpleLog.LOG_LEVEL_ERROR:
                Log.e(logName, buf.toString());
                break;
            case SimpleLog.LOG_LEVEL_FATAL:
                Log.e(logName, buf.toString());
                break;
        }
    }

    /**
     * <p>
     * Write the content of the message accumulated in the specified <code>StringBuffer</code> to the appropriate output destination. The default
     * implementation writes to <code>System.err</code>.
     * </p>
     * 
     * @param buffer
     *            A <code>StringBuffer</code> containing the accumulated text to be logged
     */
    @Override
    protected void write(StringBuffer buffer) {
        super.write(buffer);
    }
}

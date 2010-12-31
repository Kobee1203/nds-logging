package org.apache.commons.logging.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.AndroidLog;
import org.apache.commons.logging.AndroidLogFactory;
import org.apache.commons.logging.LogConfigurationException;

/**
 * <p>
 * Concrete subclass of {@link AndroidLogFactory} that implements the following algorithm to dynamically select a logging implementation class to
 * instantiate a wrapper for.
 * </p>
 * <ul>
 * <li>Use a factory configuration attribute named <code>org.apache.commons.logging.AndroidLog</code> to identify the requested implementation class.</li>
 * <li>Use the <code>org.apache.commons.logging.AndroidLog</code> system property to identify the requested implementation class.</li>
 * <li>Return an instance of <code>org.apache.commons.logging.impl.SimpleAndroidLog</code>.</li>
 * </ul>
 * 
 * <p>
 * If the selected {@link AndroidLog} implementation class has a <code>setLogFactory()</code> method that accepts a {@link AndroidLogFactory}
 * parameter, this method will be called on each newly created instance to identify the associated factory. This makes factory configuration
 * attributes available to the AndroidLog instance, if it so desires.
 * </p>
 * 
 * <p>
 * This factory will remember previously created <code>AndroidLog</code> instances for the same name, and will return them on repeated requests to the
 * <code>getInstance()</code> method.
 * </p>
 * 
 * @author Nicolas Dos Santos
 */

public class AndroidLogFactoryImpl extends AndroidLogFactory {

    /** AndroidLog class name */
    private static final String LOGGING_IMPL_ANDROID_LOGGER = "org.apache.commons.logging.impl.SimpleAndroidLog";

    private static final String PKG_IMPL = "org.apache.commons.logging.impl.";
    private static final int PKG_LEN = PKG_IMPL.length();

    // ----------------------------------------------------------- Constructors

    /**
     * Public no-arguments constructor required by the lookup mechanism.
     */
    public AndroidLogFactoryImpl() {
        super();
        initDiagnostics(); // method on this object
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Instance created.");
        }
    }

    // ----------------------------------------------------- Manifest Constants

    /**
     * The name (<code>org.apache.commons.logging.AndroidLog</code>) of the system property identifying our {@link AndroidLog} implementation class.
     */
    public static final String LOG_PROPERTY = "org.apache.commons.logging.AndroidLog";

    /**
     * The names of classes that will be tried (in order) as logging adapters. Each class is expected to implement the AndroidLog interface, and to
     * throw NoClassDefFound or ExceptionInInitializerError when loaded if the underlying logging library is not available. Any other error indicates
     * that the underlying logging library is available but broken/unusable for some reason.
     */
    private static final String[] classesToDiscover = { "org.apache.commons.logging.impl.SimpleAndroidLog" };

    // ----------------------------------------------------- Instance Variables

    /**
     * The string prefixed to every message output by the logDiagnostic method.
     */
    private String diagnosticPrefix;

    /**
     * Configuration attributes.
     */
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * The {@link org.apache.commons.logging.AndroidLog} instances that have already been created, keyed by logger name.
     */
    protected Map<String, AndroidLog> instances = new HashMap<String, AndroidLog>();

    /**
     * The one-argument constructor of the {@link org.apache.commons.logging.AndroidLog} implementation class that will be used to create new
     * instances. This value is initialized by <code>getLogConstructor()</code>, and then returned repeatedly.
     */
    protected Constructor<?> logConstructor = null;

    /**
     * The signature of the Constructor to be used.
     */
    protected Class<?> logConstructorSignature[] = { java.lang.String.class };

    /**
     * The one-argument <code>setLogFactory</code> method of the selected {@link org.apache.commons.logging.AndroidLog} method, if it exists.
     */
    protected Method logMethod = null;

    /**
     * The signature of the <code>setLogFactory</code> method to be used.
     */
    protected Class<?> logMethodSignature[] = { AndroidLogFactory.class };

    // --------------------------------------------------------- Public Methods

    /**
     * Return the configuration attribute with the specified name (if any), or <code>null</code> if there is no such attribute.
     * 
     * @param name
     *            Name of the attribute to return
     */
    @Override
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }

    /**
     * Return an array containing the names of all currently defined configuration attributes. If there are no such attributes, a zero length array is
     * returned.
     */
    @Override
    public String[] getAttributeNames() {
        return attributes.keySet().toArray(new String[attributes.size()]);
    }

    /**
     * Convenience method to derive a name from the specified class and call <code>getInstance(String)</code> with it.
     * 
     * @param clazz
     *            Class for which a suitable AndroidLog name will be derived
     * 
     * @exception LogConfigurationException
     *                if a suitable <code>AndroidLog</code> instance cannot be returned
     */
    @Override
    public AndroidLog getInstance(Class<?> clazz) throws LogConfigurationException {
        return (getInstance(clazz.getName()));
    }

    /**
     * <p>
     * Construct (if necessary) and return a <code>AndroidLog</code> instance, using the factory's current set of configuration attributes.
     * </p>
     * 
     * <p>
     * <strong>NOTE</strong> - Depending upon the implementation of the <code>AndroidLogFactory</code> you are using, the <code>AndroidLog</code>
     * instance you are returned may or may not be local to the current application, and may or may not be returned again on a subsequent call with
     * the same name argument.
     * </p>
     * 
     * @param name
     *            Logical name of the <code>AndroidLog</code> instance to be returned (the meaning of this name is only known to the underlying
     *            logging implementation that is being wrapped)
     * 
     * @exception LogConfigurationException
     *                if a suitable <code>AndroidLog</code> instance cannot be returned
     */
    @Override
    public AndroidLog getInstance(String name) throws LogConfigurationException {

        AndroidLog instance = instances.get(name);
        if (instance == null) {
            instance = newInstance(name);
            instances.put(name, instance);
        }
        return (instance);

    }

    /**
     * Release any internal references to previously created {@link org.apache.commons.logging.AndroidLog} instances returned by this factory. This is
     * useful in environments like servlet containers, which implement application reloading by throwing away a ClassLoader. Dangling references to
     * objects in that class loader would prevent garbage collection.
     */
    @Override
    public void release() {

        logDiagnostic("Releasing all known loggers");
        instances.clear();
    }

    /**
     * Remove any configuration attribute associated with the specified name. If there is no such attribute, no action is taken.
     * 
     * @param name
     *            Name of the attribute to remove
     */
    @Override
    public void removeAttribute(String name) {

        attributes.remove(name);

    }

    /**
     * Set the configuration attribute with the specified name. Calling this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     * <p>
     * This method can be used to set logging configuration programmatically rather than via system properties. It can also be used in code running
     * within a container (such as a webapp) to configure behaviour on a per-component level instead of globally as system properties would do. To use
     * this method instead of a system property, call
     * 
     * <pre>
     * AndroidLogFactory.getFactory().setAttribute(...)
     * </pre>
     * 
     * This must be done before the first AndroidLog object is created; configuration changes after that point will be ignored.
     * <p>
     * This method is also called automatically if AndroidLogFactory detects a commons-logging.properties file; every entry in that file is set
     * automatically as an attribute here.
     * 
     * @param name
     *            Name of the attribute to set
     * @param value
     *            Value of the attribute to set, or <code>null</code> to remove any setting for this attribute
     */
    @Override
    public void setAttribute(String name, Object value) {
        if (logConstructor != null) {
            logDiagnostic("setAttribute: call too late; configuration already performed.");
        }

        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    // ------------------------------------------------------ 
    // Static Methods
    //
    // These methods only defined as workarounds for a java 1.2 bug;
    // theoretically none of these are needed.
    // ------------------------------------------------------ 

    /**
     * Workaround for bug in Java1.2; in theory this method is not needed. See AndroidLogFactory.isDiagnosticsEnabled.
     */
    protected static boolean isDiagnosticsEnabled() {
        return AndroidLogFactory.isDiagnosticsEnabled();
    }

    /**
     * Workaround for bug in Java1.2; in theory this method is not needed. See AndroidLogFactory.getClassLoader.
     * 
     * @since 1.1
     */
    protected static ClassLoader getClassLoader(Class<?> clazz) {
        return AndroidLogFactory.getClassLoader(clazz);
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Calculate and cache a string that uniquely identifies this instance, including which classloader the object was loaded from.
     * <p>
     * This string will later be prefixed to each "internal logging" message emitted, so that users can clearly see any unexpected behaviour.
     * <p>
     * Note that this method does not detect whether internal logging is enabled or not, nor where to output stuff if it is; that is all handled by
     * the parent AndroidLogFactory class. This method just computes its own unique prefix for log messages.
     */
    private void initDiagnostics() {
        // It would be nice to include an identifier of the context classloader
        // that this AndroidLogFactoryImpl object is responsible for. However that
        // isn't possible as that information isn't available. It is possible
        // to figure this out by looking at the logging from AndroidLogFactory to
        // see the context & impl ids from when this object was instantiated,
        // in order to link the impl id output as this object's prefix back to
        // the context it is intended to manage.
        // Note that this prefix should be kept consistent with that 
        // in AndroidLogFactory.
        Class<?> clazz = this.getClass();
        ClassLoader classLoader = getClassLoader(clazz);
        String classLoaderName;
        try {
            if (classLoader == null) {
                classLoaderName = "BOOTLOADER";
            } else {
                classLoaderName = objectId(classLoader);
            }
        } catch (SecurityException e) {
            classLoaderName = "UNKNOWN";
        }
        diagnosticPrefix = "[AndroidLogFactoryImpl@" + System.identityHashCode(this) + " from " + classLoaderName + "] ";
    }

    /**
     * Output a diagnostic message to a user-specified destination (if the user has enabled diagnostic logging).
     * 
     * @param msg
     *            diagnostic message
     * @since 1.1
     */
    protected void logDiagnostic(String msg) {
        if (isDiagnosticsEnabled()) {
            logRawDiagnostic(diagnosticPrefix + msg);
        }
    }

    /**
     * Create and return a new {@link org.apache.commons.logging.AndroidLog} instance for the specified name.
     * 
     * @param name
     *            Name of the new logger
     * 
     * @exception LogConfigurationException
     *                if a new instance cannot be created
     */
    protected AndroidLog newInstance(String name) throws LogConfigurationException {

        AndroidLog instance = null;
        try {
            if (logConstructor == null) {
                instance = discoverAndroidLogImplementation(name);
            } else {
                Object params[] = { name };
                instance = (AndroidLog) logConstructor.newInstance(params);
            }

            if (logMethod != null) {
                Object params[] = { this };
                logMethod.invoke(instance, params);
            }

            return (instance);

        } catch (LogConfigurationException lce) {

            // this type of exception means there was a problem in discovery
            // and we've already output diagnostics about the issue, etc.; 
            // just pass it on
            throw lce;

        } catch (InvocationTargetException e) {
            // A problem occurred invoking the Constructor or Method 
            // previously discovered
            Throwable c = e.getTargetException();
            if (c != null) {
                throw new LogConfigurationException(c);
            } else {
                throw new LogConfigurationException(e);
            }
        } catch (Throwable t) {
            // A problem occurred invoking the Constructor or Method 
            // previously discovered
            throw new LogConfigurationException(t);
        }
    }

    //  ------------------------------------------------------ Private Methods

    /**
     * Read the specified system property, using an AccessController so that the property can be read if JCL has been granted the appropriate security
     * rights even if the calling code has not.
     * <p>
     * Take care not to expose the value returned by this method to the calling application in any way; otherwise the calling app can use that info to
     * access data that should not be available to it.
     */
    private static String getSystemProperty(final String key, final String def) throws SecurityException {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key, def);
            }
        });
    }

    /**
     * Fetch the parent classloader of a specified classloader.
     * <p>
     * If a SecurityException occurs, null is returned.
     * <p>
     * Note that this method is non-static merely so logDiagnostic is available.
     */
    private ClassLoader getParentClassLoader(final ClassLoader cl) {
        try {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return cl.getParent();
                }
            });
        } catch (SecurityException ex) {
            logDiagnostic("[SECURITY] Unable to obtain parent classloader");
            return null;
        }

    }

    /**
     * Attempts to create a AndroidLog instance for the given category name. Follows the discovery process described in the class javadoc.
     * 
     * @param logCategory
     *            the name of the log category
     * 
     * @throws LogConfigurationException
     *             if an error in discovery occurs, or if no adapter at all can be instantiated
     */
    private AndroidLog discoverAndroidLogImplementation(String logCategory) throws LogConfigurationException {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Discovering a AndroidLog implementation...");
        }

        AndroidLog result = null;

        // See if the user specified the AndroidLog implementation to use
        String specifiedLogClassName = findUserSpecifiedLogClassName();

        if (specifiedLogClassName != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Attempting to load user-specified log class '" + specifiedLogClassName + "'...");
            }

            result = createAndroidLogFromClass(specifiedLogClassName, logCategory, true);
            if (result == null) {
                StringBuffer messageBuffer = new StringBuffer("User-specified log class '");
                messageBuffer.append(specifiedLogClassName);
                messageBuffer.append("' cannot be found or is not useable.");

                // Mistyping or misspelling names is a common fault.
                // Construct a good error message, if we can
                if (specifiedLogClassName != null) {
                    informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_ANDROID_LOGGER);
                }
                throw new LogConfigurationException(messageBuffer.toString());
            }

            return result;
        }

        if (isDiagnosticsEnabled()) {
            logDiagnostic("No user-specified AndroidLog implementation; performing discovery"
                    + " using the standard supported logging implementations...");
        }
        for (int i = 0; (i < classesToDiscover.length) && (result == null); ++i) {
            result = createAndroidLogFromClass(classesToDiscover[i], logCategory, true);
        }

        if (result == null) {
            throw new LogConfigurationException("No suitable AndroidLog implementation");
        }

        return result;
    }

    /**
     * Appends message if the given name is similar to the candidate.
     * 
     * @param messageBuffer
     *            <code>StringBuffer</code> the message should be appended to, not null
     * @param name
     *            the (trimmed) name to be test against the candidate, not null
     * @param candidate
     *            the candidate name (not null)
     */
    private void informUponSimilarName(final StringBuffer messageBuffer, final String name, final String candidate) {
        if (name.equals(candidate)) {
            // Don't suggest a name that is exactly the same as the one the
            // user tried...
            return;
        }

        // If the user provides a name that is in the right package, and gets
        // the first 5 characters of the adapter class right (ignoring case),
        // then suggest the candidate adapter class name.
        if (name.regionMatches(true, 0, candidate, 0, PKG_LEN + 5)) {
            messageBuffer.append(" Did you mean '");
            messageBuffer.append(candidate);
            messageBuffer.append("'?");
        }
    }

    /**
     * Checks system properties and the attribute map for a AndroidLog implementation specified by the user under the property names
     * {@link #LOG_PROPERTY} or {@link #LOG_PROPERTY_OLD}.
     * 
     * @return classname specified by the user, or <code>null</code>
     */
    private String findUserSpecifiedLogClassName() {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Trying to get log class from attribute '" + LOG_PROPERTY + "'");
        }
        String specifiedClass = (String) getAttribute(LOG_PROPERTY);

        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property '" + LOG_PROPERTY + "'");
            }
            try {
                specifiedClass = getSystemProperty(LOG_PROPERTY, null);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("No access allowed to system property '" + LOG_PROPERTY + "' - " + e.getMessage());
                }
            }
        }

        // Remove any whitespace; it's never valid in a classname so its
        // presence just means a user mistake. As we know what they meant,
        // we may as well strip the spaces.
        if (specifiedClass != null) {
            specifiedClass = specifiedClass.trim();
        }

        return specifiedClass;
    }

    /**
     * Attempts to load the given class, find a suitable constructor, and instantiate an instance of AndroidLog.
     * 
     * @param logAdapterClassName
     *            classname of the AndroidLog implementation
     * 
     * @param logCategory
     *            argument to pass to the AndroidLog implementation's constructor
     * 
     * @param affectState
     *            <code>true</code> if this object's state should be affected by this method call, <code>false</code> otherwise.
     * 
     * @return an instance of the given class, or null if the logging library associated with the specified adapter is not available.
     * 
     * @throws LogConfigurationException
     *             if there was a serious error with configuration and the handleFlawedDiscovery method decided this problem was fatal.
     */
    private AndroidLog createAndroidLogFromClass(String logAdapterClassName, String logCategory, boolean affectState) throws LogConfigurationException {

        if (isDiagnosticsEnabled()) {
            logDiagnostic("Attempting to instantiate '" + logAdapterClassName + "'");
        }

        Object[] params = { logCategory };
        AndroidLog logAdapter = null;
        Constructor<?> constructor = null;

        Class<?> logAdapterClass = null;
        ClassLoader currentCL = getClassLoader(AndroidLogFactoryImpl.class);

        for (;;) {
            // Loop through the classloader hierarchy trying to find
            // a viable classloader.
            logDiagnostic("Trying to load '" + logAdapterClassName + "' from classloader " + objectId(currentCL));
            try {
                if (isDiagnosticsEnabled()) {
                    // Show the location of the first occurrence of the .class file
                    // in the classpath. This is the location that ClassLoader.loadClass
                    // will load the class from -- unless the classloader is doing
                    // something weird. 
                    URL url;
                    String resourceName = logAdapterClassName.replace('.', '/') + ".class";
                    if (currentCL != null) {
                        url = currentCL.getResource(resourceName);
                    } else {
                        url = ClassLoader.getSystemResource(resourceName + ".class");
                    }

                    if (url == null) {
                        logDiagnostic("Class '" + logAdapterClassName + "' [" + resourceName + "] cannot be found.");
                    } else {
                        logDiagnostic("Class '" + logAdapterClassName + "' was found at '" + url + "'");
                    }
                }

                Class<?> c = null;
                try {
                    c = Class.forName(logAdapterClassName, true, currentCL);
                } catch (ClassNotFoundException originalClassNotFoundException) {
                    // The current classloader was unable to find the log adapter 
                    // in this or any ancestor classloader. There's no point in
                    // trying higher up in the hierarchy in this case..
                    String msg = "" + originalClassNotFoundException.getMessage();
                    logDiagnostic("The log adapter '" + logAdapterClassName + "' is not available via classloader " + objectId(currentCL) + ": "
                            + msg.trim());
                    try {
                        // Try the class classloader.
                        // This may work in cases where the TCCL
                        // does not contain the code executed or JCL.
                        // This behaviour indicates that the application 
                        // classloading strategy is not consistent with the
                        // Java 1.2 classloading guidelines but JCL can
                        // and so should handle this case.
                        c = Class.forName(logAdapterClassName);
                    } catch (ClassNotFoundException secondaryClassNotFoundException) {
                        // no point continuing: this adapter isn't available
                        msg = "" + secondaryClassNotFoundException.getMessage();
                        logDiagnostic("The log adapter '" + logAdapterClassName
                                + "' is not available via the AndroidLogFactoryImpl class classloader: " + msg.trim());
                        break;
                    }
                }

                constructor = c.getConstructor(logConstructorSignature);
                Object o = constructor.newInstance(params);

                // Note that we do this test after trying to create an instance
                // [rather than testing AndroidLog.class.isAssignableFrom(c)] so that
                // we don't complain about AndroidLog hierarchy problems when the
                // adapter couldn't be instantiated anyway.
                if (o instanceof AndroidLog) {
                    logAdapterClass = c;
                    logAdapter = (AndroidLog) o;
                    break;
                }

                // Oops, we have a potential problem here. An adapter class
                // has been found and its underlying lib is present too, but
                // there are multiple AndroidLog interface classes available making it
                // impossible to cast to the type the caller wanted. We 
                // certainly can't use this logger, but we need to know whether
                // to keep on discovering or terminate now.
                //
                // The handleFlawedHierarchy method will throw 
                // LogConfigurationException if it regards this problem as
                // fatal, and just return if not.
                handleFlawedHierarchy(currentCL, c);
            } catch (NoClassDefFoundError e) {
                // We were able to load the adapter but it had references to
                // other classes that could not be found. This simply means that
                // the underlying logger library is not present in this or any
                // ancestor classloader. There's no point in trying higher up
                // in the hierarchy in this case..
                String msg = "" + e.getMessage();
                logDiagnostic("The log adapter '" + logAdapterClassName + "' is missing dependencies when loaded via classloader "
                        + objectId(currentCL) + ": " + msg.trim());
                break;
            } catch (ExceptionInInitializerError e) {
                // A static initializer block or the initializer code associated 
                // with a static variable on the log adapter class has thrown
                // an exception.
                //
                // We treat this as meaning the adapter's underlying logging
                // library could not be found.
                String msg = "" + e.getMessage();
                logDiagnostic("The log adapter '" + logAdapterClassName + "' is unable to initialize itself when loaded via classloader "
                        + objectId(currentCL) + ": " + msg.trim());
                break;
            } catch (LogConfigurationException e) {
                // call to handleFlawedHierarchy above must have thrown
                // a LogConfigurationException, so just throw it on                
                throw e;
            } catch (Throwable t) {
                // handleFlawedDiscovery will determine whether this is a fatal
                // problem or not. If it is fatal, then a LogConfigurationException
                // will be thrown.
                handleFlawedDiscovery(logAdapterClassName, currentCL, t);
            }

            if (currentCL == null) {
                break;
            }

            // try the parent classloader
            currentCL = getParentClassLoader(currentCL);
        }

        if ((logAdapter != null) && affectState) {
            this.logConstructor = constructor;

            // Identify the <code>setLogFactory</code> method (if there is one)
            try {
                this.logMethod = logAdapterClass.getMethod("setLogFactory", logMethodSignature);
                logDiagnostic("Found method setLogFactory(AndroidLogFactory) in '" + logAdapterClassName + "'");
            } catch (Throwable t) {
                this.logMethod = null;
                logDiagnostic("[INFO] '" + logAdapterClassName + "' from classloader " + objectId(currentCL) + " does not declare optional method "
                        + "setLogFactory(AndroidLogFactory)");
            }

            logDiagnostic("AndroidLog adapter '" + logAdapterClassName + "' from classloader " + objectId(logAdapterClass.getClassLoader())
                    + " has been selected for use.");
        }

        return logAdapter;
    }

    /**
     * Generates an internal diagnostic logging of the discovery failure and then throws a <code>LogConfigurationException</code> that wraps the
     * passed <code>Throwable</code>.
     * 
     * @param logAdapterClassName
     *            is the class name of the AndroidLog implementation that could not be instantiated. Cannot be <code>null</code>.
     * 
     * @param classLoader
     *            is the classloader that we were trying to load the logAdapterClassName from when the exception occurred.
     * 
     * @param discoveryFlaw
     *            is the Throwable created by the classloader
     * 
     * @throws LogConfigurationException
     *             ALWAYS
     */
    private void handleFlawedDiscovery(String logAdapterClassName, ClassLoader classLoader, Throwable discoveryFlaw) {

        if (isDiagnosticsEnabled()) {
            logDiagnostic("Could not instantiate AndroidLog '" + logAdapterClassName + "' -- " + discoveryFlaw.getClass().getName() + ": "
                    + discoveryFlaw.getLocalizedMessage());

            if (discoveryFlaw instanceof InvocationTargetException) {
                // Ok, the lib is there but while trying to create a real underlying
                // logger something failed in the underlying lib; display info about
                // that if possible.
                InvocationTargetException ite = (InvocationTargetException) discoveryFlaw;
                Throwable cause = ite.getTargetException();
                if (cause != null) {
                    logDiagnostic("... InvocationTargetException: " + cause.getClass().getName() + ": " + cause.getLocalizedMessage());

                    if (cause instanceof ExceptionInInitializerError) {
                        ExceptionInInitializerError eiie = (ExceptionInInitializerError) cause;
                        Throwable cause2 = eiie.getException();
                        if (cause2 != null) {
                            logDiagnostic("... ExceptionInInitializerError: " + cause2.getClass().getName() + ": " + cause2.getLocalizedMessage());
                        }
                    }
                }
            }
        }

        throw new LogConfigurationException(discoveryFlaw);
    }

    /**
     * Report a problem loading the log adapter, then either return (if the situation is considered recoverable) or throw a LogConfigurationException.
     * <p>
     * There are two possible reasons why we successfully loaded the specified log adapter class then failed to cast it to a AndroidLog object:
     * <ol>
     * <li>the specific class just doesn't implement the AndroidLog interface (user screwed up), or
     * <li>the specified class has bound to a AndroidLog class loaded by some other classloader; AndroidLog@classloaderX cannot be cast to
     * AndroidLog@classloaderY.
     * </ol>
     * <p>
     * Here we try to figure out which case has occurred so we can give the user some reasonable feedback.
     * 
     * @param badClassLoader
     *            is the classloader we loaded the problem class from, ie it is equivalent to badClass.getClassLoader().
     * 
     * @param badClass
     *            is a Class object with the desired name, but which does not implement AndroidLog correctly.
     * 
     * @throws LogConfigurationException
     *             when the situation should not be recovered from.
     */
    private void handleFlawedHierarchy(ClassLoader badClassLoader, Class<?> badClass) throws LogConfigurationException {

        boolean implementsLog = false;
        String logInterfaceName = AndroidLog.class.getName();
        Class<?> interfaces[] = badClass.getInterfaces();
        for (Class<?> interface1 : interfaces) {
            if (logInterfaceName.equals(interface1.getName())) {
                implementsLog = true;
                break;
            }
        }

        if (implementsLog) {
            // the class does implement an interface called AndroidLog, but
            // it is in the wrong classloader
            if (isDiagnosticsEnabled()) {
                try {
                    ClassLoader logInterfaceClassLoader = getClassLoader(AndroidLog.class);
                    logDiagnostic("Class '" + badClass.getName() + "' was found in classloader " + objectId(badClassLoader)
                            + ". It is bound to a AndroidLog interface which is not" + " the one loaded from classloader "
                            + objectId(logInterfaceClassLoader));
                } catch (Throwable t) {
                    logDiagnostic("Error while trying to output diagnostics about" + " bad class '" + badClass + "'");
                }
            }

            StringBuffer msg = new StringBuffer();
            msg.append("Terminating logging for this context ");
            msg.append("due to bad log hierarchy. ");
            msg.append("You have more than one version of '");
            msg.append(AndroidLog.class.getName());
            msg.append("' visible.");
            if (isDiagnosticsEnabled()) {
                logDiagnostic(msg.toString());
            }

            throw new LogConfigurationException(msg.toString());
        } else {
            // this is just a bad adapter class
            StringBuffer msg = new StringBuffer();
            msg.append("Terminating logging for this context. ");
            msg.append("AndroidLog class '");
            msg.append(badClass.getName());
            msg.append("' does not implement the AndroidLog interface.");
            if (isDiagnosticsEnabled()) {
                logDiagnostic(msg.toString());
            }

            throw new LogConfigurationException(msg.toString());
        }
    }

}

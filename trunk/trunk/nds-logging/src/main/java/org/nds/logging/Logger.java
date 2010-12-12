package org.nds.logging;

import org.apache.commons.logging.Log;

/**
 * <p>
 * A Logger object is used to log messages for an Android application or any other java application.<br/>
 * <i>This can be useful when developing an Android library, and we write unit tests (JUnit) and integration tests (Android Unit tests).</i>
 * </p>
 * <p>
 * It is based on two types of log:
 * </p>
 * <ul>
 * <li>Android.util.Log: used to log an Android application</li>
 * <li>Org.apache.commons.logging.Log: used for logging Any Other java application</li>
 * </ul>
 * <br/>
 * <h3>Android Log:</h3>
 * <p>
 * The default level of any tag is set to INFO. This means that any level above and including INFO will be logged. Before you make any calls to a
 * logging method you should check to see if your tag should be logged. You can change the default level by setting a system property: 'setprop
 * log.tag.<YOUR_LOG_TAG> <LEVEL>' Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS. SUPPRESS will turn off all logging
 * for your tag. You can also create a local.prop file that with the following in it: 'log.tag.<YOUR_LOG_TAG>=<LEVEL>' and place that in
 * /data/local.prop.
 * </p>
 * <p>
 * <i>To push files, open && browse in Terminal/CommandLine to ".../android_sdk/tools"</i> <br/>
 * <u>Syntax:</u> " <b>adb push</b> &lt;local_source&gt; &lt;emulator_destination&gt; "
 * </p>
 * <p>
 * In our case, the file local.prop must contain at least:<br/>
 * <i>log.tag.Logger = DEBUG</i><br/>
 * 'Logger' tag is the default Logger class.<br/>
 * We can add more tags to their level if the application uses several different tags.<br/>
 * </p>
 * <h3>Other Java application:</h3>
 * <p>
 * It uses the Jakarta Commons Logging (JCL). (<a
 * href="http://commons.apache.org/logging/guide.html">http://commons.apache.org/logging/guide.html</a>)<br/>
 * </p>
 * <p>
 * As far as possible, JCL tries to be as unobtrusive as possible. In most cases, including the (full) commons-logging.jar in the classpath should
 * result in JCL configuring itself in a reasonable manner.<br/>
 * There's a good chance that it'll guess (discover) your preferred logging system and you won't need to do any configuration of JCL at all!<br/>
 * Note, however, that if you have a particular preference then providing a simple <b>commons-logging.properties</b> file which specifies the concrete
 * logging library to be used is recommended, since (in this case) JCL will log only to that system and will report any configuration problems that
 * prevent that system being used.<br/>
 * Commons-Logging looks for a configuration file called <b>commons-logging.properties</b> in the CLASSPATH. This file must define, at the minimum,
 * the property org.apache.commons.logging.Log, and it should be equal to the fully qualified name of one of the implementations of the Log interface
 * listed above.<br/>
 * When no particular logging library is specified then JCL will silently ignore any logging library that it finds but cannot initialise and continue
 * to look for other alternatives. This is a deliberate design decision; no application should fail to run because a "guessed" logging library cannot
 * be used. To ensure an exception is reported when a particular logging library cannot be used, use one of the available JCL configuration mechanisms
 * to force that library to be selected (ie disable JCL's discovery process).
 * </p>
 * <p>
 * JCL is distributed with a very simple Log implementation named <b>org.apache.commons.logging.impl.SimpleLog</b>. This is intended to be a minimal
 * implementation and those requiring a fully functional open source logging system are directed to Log4J.<br/>
 * Simple implementation of Log that sends all enabled log messages, for all defined loggers, to System.err. The following system properties are
 * supported to configure the behavior of this logger:
 * </p>
 * <p>
 * <ul>
 * <li>org.apache.commons.logging.simplelog.defaultlog - Default logging detail level for all instances of SimpleLog. Must be one of ("trace",
 * "debug", "info", "warn", "error", or "fatal"). If not specified, defaults to "info".</li>
 * <li>org.apache.commons.logging.simplelog.log.xxxxx - Logging detail level for a SimpleLog instance named "xxxxx". Must be one of ("trace", "debug",
 * "info", "warn", "error", or "fatal"). If not specified, the default logging detail level is used.<br/>
 * org.apache.commons.logging.simplelog.showlogname - Set to true if you want the Log instance name to be included in output messages. Defaults to
 * false.</li>
 * <li>org.apache.commons.logging.simplelog.showShortLogname - Set to true if you want the last component of the name to be included in output
 * messages. Defaults to true.</li>
 * <li>org.apache.commons.logging.simplelog.showdatetime - Set to true if you want the current date and time to be included in output messages.
 * Default is false.</li>
 * <li>org.apache.commons.logging.simplelog.dateTimeFormat - The date and time format to be used in the output messages. The pattern describing the
 * date and time format is the same that is used in java.text.SimpleDateFormat. If the format is not specified or is invalid, the default format is
 * used. The default format is yyyy/MM/dd HH:mm:ss:SSS zzz.</li>
 * </ul>
 * </p>
 * <p>
 * In addition to looking for system properties with the names specified above, this implementation also checks for a class loader resource named
 * <b>simplelog.properties</b>, and includes any matching definitions from this resource (if it exists).<br/>
 * </p>
 * 
 * @author Nicolas Dos Santos
 * 
 */
public class Logger {

    private final Log log;

    protected Logger(String name, Log log) {
        this.log = log;

        System.out.println("[" + name + "] trace enabled: " + log.isTraceEnabled());
        System.out.println("[" + name + "] debug enabled: " + log.isDebugEnabled());
        System.out.println("[" + name + "] info enabled: " + log.isInfoEnabled());
        System.out.println("[" + name + "] warn enabled: " + log.isWarnEnabled());
        System.out.println("[" + name + "] error enabled: " + log.isErrorEnabled());
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    public void trace(Object message, Object... params) {
        if (isTraceEnabled()) {
            if (params != null && params.length > 0 && params[0] instanceof Throwable) {
                trace(message, params[0], paramsWithoutFirst(params));
            } else {
                log.trace(message);
            }
        }
    }

    public void trace(String message, Throwable t, Object... params) {
        if (isTraceEnabled()) {
            log.trace(String.format(message, params), t);
        }
    }

    public void debug(String message, Object... params) {
        if (isDebugEnabled()) {
            if (params != null && params.length > 0 && params[0] instanceof Throwable) {
                debug(message, (Throwable) params[0], paramsWithoutFirst(params));
            } else {
                log.debug(String.format(message, params));
            }
        }
    }

    public void debug(String message, Throwable t, Object... params) {
        if (isDebugEnabled()) {
            log.debug(String.format(message, params), t);
        }
    }

    public void info(String message, Object... params) {
        if (isInfoEnabled()) {
            if (params != null && params.length > 0 && params[0] instanceof Throwable) {
                info(message, (Throwable) params[0], paramsWithoutFirst(params));
            } else {
                log.info(String.format(message, params));
            }
        }
    }

    public void info(String message, Throwable t, Object... params) {
        if (isInfoEnabled()) {
            log.info(String.format(message, params), t);
        }
    }

    public void warn(String message, Object... params) {
        if (isWarnEnabled()) {
            if (params != null && params.length > 0 && params[0] instanceof Throwable) {
                warn(message, (Throwable) params[0], paramsWithoutFirst(params));
            } else {
                log.warn(String.format(message, params));
            }
        }
    }

    public void warn(String message, Throwable t, Object... params) {
        if (isWarnEnabled()) {
            log.warn(String.format(message, params), t);
        }
    }

    public void error(String message, Object... params) {
        if (isErrorEnabled()) {
            if (params != null && params.length > 0 && params[0] instanceof Throwable) {
                error(message, (Throwable) params[0], paramsWithoutFirst(params));
            } else {
                log.error(String.format(message, params));
            }
        }
    }

    public void error(String message, Throwable t, Object... params) {
        if (isErrorEnabled()) {
            log.error(String.format(message, params), t);
        }
    }

    public void fatal(String message, Object... params) {
        if (isFatalEnabled()) {
            if (params != null && params.length > 0 && params[0] instanceof Throwable) {
                fatal(message, (Throwable) params[0], paramsWithoutFirst(params));
            } else {
                log.fatal(String.format(message, params));
            }
        }
    }

    public void fatal(String message, Throwable t, Object... params) {
        if (isFatalEnabled()) {
            log.fatal(String.format(message, params), t);
        }
    }

    private static Object[] paramsWithoutFirst(Object... params) {
        Object[] newParams = new Object[params.length - 1];
        if (newParams.length > 0) {
            System.arraycopy(params, 1, newParams, 0, newParams.length);
        }
        return newParams;
    }

}

package org.nds.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import android.util.Config;

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

    private String TAG = "Logger";

    private final Log log;

    private static Boolean verboseLoggable;
    private static Boolean debugLoggable;
    private static Boolean infoLoggable;
    private static Boolean warnLoggable;
    private static Boolean errorLoggable;

    protected Logger(String name, String tag) {
        this.TAG = tag != null ? tag : this.TAG;
        try {
            if (verboseLoggable == null) {
                verboseLoggable = android.util.Log.isLoggable(TAG, android.util.Log.VERBOSE);
            }
        } catch (Throwable t) {
            verboseLoggable = false;
        }
        try {
            if (debugLoggable == null) {
                debugLoggable = android.util.Log.isLoggable(TAG, android.util.Log.DEBUG);
            }
        } catch (Throwable t) {
            debugLoggable = false;
        }
        try {
            if (infoLoggable == null) {
                infoLoggable = android.util.Log.isLoggable(TAG, android.util.Log.INFO);
            }
        } catch (Throwable t) {
            infoLoggable = false;
        }
        try {
            if (warnLoggable == null) {
                warnLoggable = android.util.Log.isLoggable(TAG, android.util.Log.WARN);
            }
        } catch (Throwable t) {
            warnLoggable = false;
        }
        try {
            if (errorLoggable == null) {
                errorLoggable = android.util.Log.isLoggable(TAG, android.util.Log.ERROR);
            }
        } catch (Throwable t) {
            errorLoggable = false;
        }

        log = LogFactory.getLog(name);
    }

    // //////// TRACE //////////

    public void trace(String tag, String msg, Throwable tr) {
        if (Config.LOGD && verboseLoggable) {
            android.util.Log.v(tag, msg, tr);
        } else {
            log.trace(msg, tr);
        }
    }

    public void trace(String tag, String msg) {
        if (Config.LOGD && verboseLoggable) {
            android.util.Log.v(tag, msg);
        } else {
            log.trace(msg);
        }
    }

    public void trace(String msg, Throwable tr) {
        trace(TAG, msg, tr);
    }

    public void trace(String msg) {
        trace(TAG, msg);
    }

    // //////// DEBUG //////////

    public void debug(String tag, String msg, Throwable tr) {
        if (Config.LOGD && debugLoggable) {
            android.util.Log.d(tag, msg, tr);
        } else {
            log.debug(msg, tr);
        }
    }

    public void debug(String tag, String msg) {
        if (Config.LOGD && debugLoggable) {
            android.util.Log.d(tag, msg);
        } else {
            log.debug(msg);
        }
    }

    public void debug(String msg, Throwable tr) {
        debug(TAG, msg, tr);
    }

    public void debug(String msg) {
        debug(TAG, msg);
    }

    // //////// INFO //////////

    public void info(String tag, String msg, Throwable tr) {
        if (Config.LOGD && infoLoggable) {
            android.util.Log.i(tag, msg, tr);
        } else {
            log.info(msg, tr);
        }
    }

    public void info(String tag, String msg) {
        if (Config.LOGD && infoLoggable) {
            android.util.Log.i(tag, msg);
        } else {
            log.info(msg);
        }
    }

    public void info(String msg, Throwable tr) {
        info(TAG, msg, tr);
    }

    public void info(String msg) {
        info(TAG, msg);
    }

    // //////// WARN //////////

    public void warn(String tag, String msg, Throwable tr) {
        if (Config.LOGD && warnLoggable) {
            android.util.Log.w(tag, msg, tr);
        } else {
            log.warn(msg, tr);
        }
    }

    public void warn(String tag, String msg) {
        if (Config.LOGD && warnLoggable) {
            android.util.Log.w(tag, msg);
        } else {
            log.warn(msg);
        }
    }

    public void warn(String msg, Throwable tr) {
        warn(TAG, msg, tr);
    }

    public void warn(String msg) {
        warn(TAG, msg);
    }

    // //////// ERROR //////////

    public void error(String tag, String msg, Throwable tr) {
        if (Config.LOGD && errorLoggable) {
            android.util.Log.e(tag, msg, tr);
        } else {
            log.error(msg, tr);
        }
    }

    public void error(String tag, String msg) {
        if (Config.LOGD && errorLoggable) {
            android.util.Log.e(tag, msg);
        } else {
            log.error(msg);
        }
    }

    public void error(String msg, Throwable tr) {
        error(TAG, msg, tr);
    }

    public void error(String msg) {
        error(TAG, msg);
    }
}

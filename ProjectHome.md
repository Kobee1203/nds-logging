A Logger used to log messages for an Android application or any other java application.
This can be useful when developing an Android library, and we write unit tests (JUnit) and integration tests (Android Unit tests).

It is based on two types of log:

  * `android.util.Log`: used to log an Android application
  * `org.apache.commons.logging.Log`: used for logging Any Other java application

If you want to add the log management on a class, simply add the line:
```
   private static final Logger log = LoggerFactory.getLogger(Foo.class);
```
or
```
   private static final Logger log = LoggerFactory.getLogger("your_log_name");
```

# Configuration #

It's based on Apache Commons Logging (JCL). To learn how it works and how you can configure file and commons-logging.properties simplelog.properties, see http://commons.apache.org/logging/guide.html.

1. In the case of a Java standard, log management will be identical to the log management with Apache Commons Logging.

2. In the case of an Android application, the configuration is defined in terms of files:
  * **android-commons-logging.properties** : equivalent file to the JCL file **commons-logging.properties**. The allowed configuration attributes are:
    * `org.apache.commons.logging.LogFactory` : defines the implementation of `org.apache.commons.logging.AndroidLogFactory`. The default implementation is `org.apache.commons.logging.impl.AndroidLogFactoryImpl`.
    * `org.apache.commons.logging.AndroidLog` : defines the implementation of `org.apache.commons.logging.AndroidLog` managed by the implementation of `org.apache.commons.logging.AndroidLogFactory`. The default implementation is `org.apache.commons.logging.impl.SimpleAndroidLog`.
  * **androidlog.properties** : equivalent file to the JCL file **simplelog.properties**. The allowed configuration attributes are:
    * `org.apache.commons.logging.androidlog.defaultlog` : Default logging detail level for all instances of `AndroidLog`. Must be one of ("trace", "debug", "info", "warn", "error", or "fatal"). If not specified, defaults to "info".
    * `org.apache.commons.logging.androidlog.log.xxxxx` : Logging detail level for a `AndroidLog` instance named "xxxxx". Must be one of ("trace", "debug", "info", "warn", "error", or "fatal"). If not specified, the default logging detail level is used.
    * `org.apache.commons.logging.androidlog.showlogname` : Set to true if you want the `AndroidLog` instance name to be included in output messages. Defaults to false.
    * `org.apache.commons.logging.androidlog.showShortLogname` : Set to true if you want the last component of the name to be included in output messages. Defaults to true.
    * `org.apache.commons.logging.androidlog.showdatetime` : Set to true if you want the current date and time to be included in output messages. Default is false.
    * `org.apache.commons.logging.androidlog.dateTimeFormat` : The date and time format to be used in the output messages. The pattern describing the date and time format is the same that is used in `java.text.SimpleDateFormat`. If the format is not specified or is invalid, the default format is used. The default format is yyyy/MM/dd HH:mm:ss:SSS zzz.
    * `org.apache.commons.logging.androidlog.showlevel` : Set to true if you want the level to be included in output messages. Default is false.
    * `org.apache.commons.logging.androidlog.showShortTag` : Set to true if you want to use the short tag. Default is false.

# Example #
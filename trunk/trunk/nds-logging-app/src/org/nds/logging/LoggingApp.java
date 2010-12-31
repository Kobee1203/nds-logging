package org.nds.logging;

import android.app.Activity;
import android.os.Bundle;

public class LoggingApp extends Activity {

    private static final Logger log = LoggerFactory.getLogger(LoggingApp.class);

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        log.debug("LoggingApp#onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        log.info("Activity initialized.");
    }

}

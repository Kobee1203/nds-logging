package org.nds.logging.test;

import org.nds.logging.Logger;
import org.nds.logging.LoggerFactory;

import android.test.AndroidTestCase;
import android.util.Log;

public class LoggingTest extends AndroidTestCase {

    private static final Logger log = LoggerFactory.getLogger(LoggingTest.class);

    public void testLogger() throws InterruptedException {
        log.trace("test log trace/verbose");
        log.debug("test log debug");
        log.info("TAG_INFO", "test log info with tag TAG_INFO.");
        log.warn("test log warning");
        log.error("test log error");
        Log.println(0, "TOTO", "test println");
        System.out.println("test System out println");
    }

}

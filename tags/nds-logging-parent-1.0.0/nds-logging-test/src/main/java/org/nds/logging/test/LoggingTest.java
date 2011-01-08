package org.nds.logging.test;

import org.junit.Test;
import org.nds.logging.Logger;
import org.nds.logging.LoggerFactory;

public class LoggingTest {

    /**
     * This log uses /common-logging.properties and /simplelog.properties
     */
    private static final Logger log = LoggerFactory.getLogger(LoggingTest.class);

    @Test
    public void testLogger() {
        System.out.println(LoggingTest.class.getClassLoader());
        log.trace("test log trace/verbose");
        log.debug("test log debug");
        log.info("test log info.");
        log.warn("test log warning");
        log.error("test log error");
    }
}

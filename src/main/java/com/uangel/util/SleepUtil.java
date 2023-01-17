package com.uangel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dajin kim
 */
public class SleepUtil {
    static final Logger log = LoggerFactory.getLogger(SleepUtil.class);

    private SleepUtil() {
        // Do Nothing
    }

    public static void trySleep(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error("SleepUtil.trySleep.Exception ", e);
            Thread.currentThread().interrupt();
        }
    }
}

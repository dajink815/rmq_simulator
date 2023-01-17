package com.uangel.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dajin kim
 */
@Slf4j
public class SleepUtil {

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

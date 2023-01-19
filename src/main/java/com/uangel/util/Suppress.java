package com.uangel.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dajin kim
 */
public class Suppress {
    private final long interval;    // unit : ms
    private final Map<String, Long> lastTimeMap = new ConcurrentHashMap<>();

    public Suppress(long interval) {
        this.interval = interval;
    }

    public boolean touch() {
        return touch("");
    }

    public boolean touch(String key) {
        long now = System.currentTimeMillis();
        long last = lastTimeMap.computeIfAbsent(key, k -> now);
        boolean accept = now - last >= interval;
        if (accept) {
            last = now;
            lastTimeMap.put(key, last);
        }
        return accept;
    }
}

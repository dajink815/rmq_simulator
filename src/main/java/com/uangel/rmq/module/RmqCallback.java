package com.uangel.rmq.module;

import java.util.Date;

@FunctionalInterface
public interface RmqCallback {
    void onReceived(byte[] msg, Date ts);
}

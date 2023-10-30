package com.uangel.rmq.module;

@FunctionalInterface
public interface RmqCallback {
    void onReceived(byte[] msg);
}

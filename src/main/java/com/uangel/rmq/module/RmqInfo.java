package com.uangel.rmq.module;

import lombok.Getter;
import lombok.Setter;

/**
 * @author dajin kim
 */
@Getter
public class RmqInfo {
    private final String host;
    private final String user;
    @Setter
    private String pass;
    private final String rmqName;
    private final int port;

    public RmqInfo(String host, String user, String pass, String rmqName, int port) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.rmqName = rmqName;
        this.port = port;
    }
}

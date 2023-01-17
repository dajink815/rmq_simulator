package com.uangel.service;

/**
 * @author dajin kim
 */
public enum ServiceDefine {

    U_RMQ("u_rmq"),
    PW_ALG("PBEWITHMD5ANDDES");

    private String str;
    private int num;

    ServiceDefine(String str) {
        this.str = str;
    }

    ServiceDefine(int num) {
        this.num = num;
    }

    public String getStr() {
        return str;
    }

    public int getNum() {
        return num;
    }
}

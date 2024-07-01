package com.uangel.rmq.util;

import com.uangel.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class RmqMsgPrinter {

    private RmqMsgPrinter() {
        // nothing
    }

    public static void printSendMsg(String target, byte[] msg) {
        String json = new String(msg, StandardCharsets.UTF_8);
        json = JsonUtil.buildPretty(json);

        String jsonUpper = json.toUpperCase();
        if (!jsonUpper.contains("HB") && !jsonUpper.contains("HEARTBEAT"))
            log.debug("SendTo --> [{}]\r\n[{}]", target, json);
        else
            log.trace("SendTo --> [{}]\r\n[{}]", target, json);
    }

    public static void printRcvMsg(String json) {
        String jsonUpper = json.toUpperCase();
        if (!jsonUpper.contains("HB") && !jsonUpper.contains("HEARTBEAT"))
            log.debug("RcvMsg [{}]", json);
        else
            log.trace("RcvMsg [{}]", json);
    }
}

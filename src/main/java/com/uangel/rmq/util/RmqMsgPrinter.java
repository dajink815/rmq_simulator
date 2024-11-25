package com.uangel.rmq.util;

import com.uangel.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class RmqMsgPrinter {

    private RmqMsgPrinter() {
        // nothing
    }

    public static void printSendMsg(String target, Object msg) {
        String json = String.valueOf(msg);
        if (isHBMsg(json)) {
            log.trace("SendTo --> [{}]\r\n[{}]", target, json);
        } else {
            log.debug("SendTo --> [{}]\r\n[{}]", target, json);
        }
    }
    public static void printSendMsg(String target, byte[] msg) {
        String json = new String(msg, StandardCharsets.UTF_8);
        try {
            json = JsonUtil.buildPretty(json);
        } catch (Exception e) {
            // ignore - ProtoBuffer 메시지 buildPretty 실패
        }

        if (isHBMsg(json)) {
            log.trace("SendTo --> [{}]\r\n[{}]", target, json);
        } else {
            log.debug("SendTo --> [{}]\r\n[{}]", target, json);
        }
    }

    public static void printRcvMsg(String json) {
        if (isHBMsg(json)) {
            log.trace("RcvMsg [{}]", json);
        } else {
            log.debug("RcvMsg [{}]", json);
        }
    }

    private static boolean isHBMsg(String json) {
        String jsonUpper = json.toUpperCase();
        return jsonUpper.contains("HB") || jsonUpper.contains("HEARTBEAT");
    }
}

package com.uangel.rmq.util;

import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public class RmqParser implements Serializable {
    private static final long serialVersionUID = 222678860554205348L;

    public static String parsingCallId(JsonElement json) {
        String callId = "";
        try {
            if (json.getAsJsonObject().get("callId") != null) {
                callId = json.getAsJsonObject().get("callId").getAsString();
            }
        } catch (Exception e) {
            log.error("RmqParser.parsingCallId.Exception ", e);
        }
        return callId;
    }

    public static String parsingTaskId(JsonElement json) {
        String taskId = "";
        try {
            if (json.getAsJsonObject().get("taskId") != null) {
                taskId = json.getAsJsonObject().get("taskId").getAsString();
            }
        } catch (Exception e) {
            log.error("RmqParser.parsingTaskId.Exception ", e);
        }
        return taskId;
    }
}

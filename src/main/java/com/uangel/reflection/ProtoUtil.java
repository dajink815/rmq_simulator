package com.uangel.reflection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dajin kim
 */
public class ProtoUtil {
    static final Logger log = LoggerFactory.getLogger(ProtoUtil.class);

    public ProtoUtil() {
        // nothing
    }

    // JSON -> ClassType Object
    public static <T> T parse(String json, Type classType) {
        Gson gson = new Gson();
        return gson.fromJson(json, classType);
    }

    public static String protoToJson(MessageOrBuilder message) {
        try {
            return JsonFormat.printer().includingDefaultValueFields().print(message);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Message fromJson(String json) throws IOException {
        Struct.Builder structBuilder = Struct.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(json, structBuilder);
        return structBuilder.build();
    }

    public static String toJson(MessageOrBuilder messageOrBuilder) throws IOException {
        return JsonFormat.printer().includingDefaultValueFields().print(messageOrBuilder);
    }

    // Object -> JSON
    public static String buildProto(Object obj) throws InvalidProtocolBufferException {
        return JsonFormat.printer().includingDefaultValueFields().print((MessageOrBuilder) obj);
    }


}

package com.uangel.scenario.handler.base;

import com.github.javaparser.utils.StringEscapeUtils;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.uangel.model.SessionInfo;
import com.uangel.reflection.JarReflection;
import com.uangel.reflection.ReflectionUtil;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.HeaderBodyInfo;
import com.uangel.scenario.phases.OutgoingPhase;
import com.uangel.scenario.type.FieldType;
import com.uangel.scenario.type.OutMsgType;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class ProtoMsgBuilder extends MsgBuilder {

    private final JarReflection jarReflection;

    public ProtoMsgBuilder(SessionInfo sessionInfo, OutMsgType type) {
        super(sessionInfo, type);
        this.jarReflection = scenario.getJarReflection();
    }

    // SessionInfo Null
    public ProtoMsgBuilder(Scenario scenario, OutMsgType type) {
        super(scenario, type);
        this.jarReflection = scenario.getJarReflection();
    }

    @Override
    public byte[] build(OutgoingPhase outgoingPhase) {
        // Check Instance Type
        if (!checkType(outgoingPhase)) return new byte[0];

        try {
            String pkgBase = scenario.getCmdInfo().getProtoPkg();

            // get Builder
            String msgClassName = pkgBase + outgoingPhase.getClassName();
            Object msgBuilder = jarReflection.getNewBuilder(msgClassName);

            // set subMessages
            for (HeaderBodyInfo msgInfo : outgoingPhase.getHeaderBodyInfos()) {
                Object subMsgObj = getSubMessage(msgInfo, pkgBase);
                if (subMsgObj == null) continue;
                msgBuilder = jarReflection.invokeObjMethod(jarReflection.getMethodName(msgInfo.getClassName()), msgBuilder, subMsgObj);
            }

            // Build Message
            Object msgResult = jarReflection.build(msgBuilder);
            // Type ??? ??????
            if (isSendType()) {
                log.debug("Build SendMsg \r\n[{}]", msgResult);
            } else {
                log.debug("Build LoopMsg [{}]", outgoingPhase.getMsgName());
                //System.out.println(msgResult);
            }
            msgResult = unescapeBodyFields((GeneratedMessageV3) msgResult);

            return jarReflection.toByteArray(msgResult);
        } catch (Exception e) {
            log.error("ProtoMsgBuilder.build.Exception ", e);
        }

        return new byte[0];
    }


    public Object getSubMessage(HeaderBodyInfo msgInfo, String pkgBase) {

        try {
            Object builder = jarReflection.getNewBuilder(pkgBase + msgInfo.getClassName());
            List<FieldInfo> fieldInfos = msgInfo.getFieldInfos();
            Map<String, String> fields = new HashMap<>();

            for (FieldInfo fieldInfo : fieldInfos) {
                String fieldName = fieldInfo.getName();
                String methodName = jarReflection.getMethodName(fieldName);
                FieldType type = fieldInfo.getType();
                String value = fieldInfo.getValue();

                // ??? ??????

                // Exec
                if (StringUtil.notNull(fieldInfo.getExec())) {
                    String exec = fieldInfo.getExec();
                    value = ReflectionUtil.getExecResult(exec);
                    if (value == null) {
                        log.warn("ProtoMsgBuilder - Exec field processing failed (fieldName:{}, exec:{})", fieldName, exec);
                        continue;
                    }
                }

                // Keyword
                KeywordMapper keywordMapper = scenario.getKeywordMapper();
                value = keywordMapper.replaceKeyword(value, sessionInfo);

                if (StringUtil.isNull(value)) continue;

                // Type ??? ??????
                if (FieldType.STR.equals(type)) {
                    builder = jarReflection.invokeObjMethod(methodName, builder, value);
                } else if (FieldType.INT.equals(type)) {
                    builder = jarReflection.invokeIntMethod(methodName, builder, Integer.parseInt(value));
                } else if (FieldType.LONG.equals(type)) {
                    builder = jarReflection.invokeLongMethod(methodName, builder, Long.parseLong(value));
                } else if (FieldType.BOOL.equals(type)) {
                    builder = jarReflection.invokeBoolMethod(methodName, builder, Boolean.parseBoolean(value));
                }

                fields.put(fieldName, value);
            }

            if (!fields.isEmpty()) {
                if (ObjectUtils.isNotEmpty(sessionInfo)) sessionInfo.addFields(fields);
                else scenario.addFields(fields);
            }

            return jarReflection.build(builder);

        } catch (Exception e) {
            log.error("ProtoMsgBuilder.getSubMessage.Exception ", e);
        }

        return null;
    }

    private Object unescapeBodyFields(GeneratedMessageV3 message) {
        try {
            if (message == null) {
                log.warn("ProtoMsgBuilder.unescapeBodyFields Message Null");
                return message;
            }
            Message.Builder msgBuilder = message.toBuilder();
            Message.Builder bodyBuilder = message.getAllFields().entrySet().stream()
                    .filter(entry -> !entry.getKey().getName().equals("header"))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .map(o -> ((GeneratedMessageV3) o).toBuilder())
                    .orElseThrow();

            bodyBuilder.getAllFields().keySet().stream()
                    .filter(o -> o.getType() == Descriptors.FieldDescriptor.Type.STRING)
                    .filter(o -> bodyBuilder.getField(o) instanceof String)
                    .forEach(key -> {
                        String value = (String) bodyBuilder.getField(key);
                        String converted = StringEscapeUtils.unescapeJava(value);
                        bodyBuilder.setField(key, converted);
                    });
            msgBuilder.getAllFields().keySet().stream().filter(o -> !o.getName().equals("header"))
                    .forEach(o -> msgBuilder.setField(o, bodyBuilder.build()));
            return msgBuilder.build();
        } catch (Exception e) {
            log.error("ProtoMsgBuilder.unescapeBodyFields.Exception ", e);
            return message;
        }
    }
}

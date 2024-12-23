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
import com.uangel.scenario.model.StructInfo;
import com.uangel.scenario.phases.OutgoingPhase;
import com.uangel.scenario.type.FieldType;
import com.uangel.scenario.type.OutMsgType;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProtoMsgBuilder extends MsgBuilder {

    private final JarReflection jarReflection;
    private final String pkgBase;

    public ProtoMsgBuilder(SessionInfo sessionInfo, OutMsgType type) {
        super(sessionInfo, type);
        this.jarReflection = scenario.getJarReflection();
        pkgBase = scenario.getCmdInfo().getProtoPkg();
    }

    // SessionInfo Null
    public ProtoMsgBuilder(Scenario scenario, OutMsgType type) {
        super(scenario, type);
        this.jarReflection = scenario.getJarReflection();
        pkgBase = scenario.getCmdInfo().getProtoPkg();
    }

    @Override
    public byte[] build(OutgoingPhase outgoingPhase) {
        // Check Instance Type
        if (!checkType(outgoingPhase)) return new byte[0];

        try {
            // get Builder
            String msgClassName = pkgBase + outgoingPhase.getClassName();
            Object msgBuilder = jarReflection.getNewBuilder(msgClassName);
            buildProtoMsg(msgBuilder, outgoingPhase.getHeaderBodyInfos());

            // Build Message
            Object msgResult = jarReflection.build(msgBuilder);

            // UnEscape
            msgResult = unescapeBodyFields((GeneratedMessageV3) msgResult);

            // Type 별 로그
            if (isSendType()) {
                /* 2023.09.06
                 * Send 시나리오 (Send Phase 로 시작하는 시나리오) 경우
                 * SessionInfo 의 ID가 지정 되어 테스트 갯수 만큼 미리 생성됨
                 *
                 * 시뮬레이터 스크립트에서 -k command 를 이용해 지정한
                 * Keyword Field (dialogId, callId...)는 세션을 구별하기 위한 구분자 필드를 의미
                 *
                 * Send 시나리오에서 세션의 Keyword Field 를 사용자가 지정한 값으로 사용하는 경우
                 * 미리 생성한 SessionInfo 의 ID 와
                 * 사용자가 설정한 Keyword Field 값이 일치하지 않아 세션 찾지 못하는 문제 발생
                 *
                 * 사용자가 Keyword Field 를 별도의 값으로 지정하여
                 * SessionInfo 에 설정된 ID 값과 다른 경우 변경된 값으로 다시 설정하도록 수정
                 * */

                // Parse Keyword Field
                Map<String, String> fields = jarReflection.getAllFieldsMap(msgResult);
                String sessionId = fields.get(scenario.getCmdInfo().getFieldKeyword());
                // todo 첫 메시지 일 때만 Id 변경
                scenario.getSessionManager().changeSessionId(sessionInfo.getSessionId(), sessionId);
                log.debug("Build SendMsg \r\n[{}]", msgResult);
            } else {
                log.trace("Build LoopMsg [{}]", outgoingPhase.getMsgName());
            }

            return jarReflection.toByteArray(msgResult);
        } catch (Exception e) {
            log.error("ProtoMsgBuilder.build.Exception ({})", outgoingPhase.getMsgName(), e);
        }

        return new byte[0];
    }

    private void buildProtoMsg(Object msgBuilder, List<HeaderBodyInfo> childrenInfo) {
        for (HeaderBodyInfo childInfo : childrenInfo) {
            Object childObj = buildSubMsg(childInfo.getClassType(), childInfo.getClassName(), childInfo.getFieldInfos(), childInfo.getStructList());
            if (childObj == null) continue;
            String methodName = jarReflection.getSetterMethodName(childInfo.getClassName());
            msgBuilder = jarReflection.invokeObjMethod(methodName, msgBuilder, childObj);
        }
    }

    // parent Object 는 builder 가 와야 함
    private void setStructMsg(Object parentBuilder, List<StructInfo> structInfos) {
        for (StructInfo structInfo : structInfos) {
            Object structObj = buildSubMsg(structInfo.getClassName(), structInfo.getClassName(), structInfo.getFieldInfos(), structInfo.getStructList());
            if (structObj == null) continue;
            String methodName;
            if (structInfo.isRepeated())
                methodName = jarReflection.getAddMethodName(structInfo.getName());
            else
                methodName = jarReflection.getSetterMethodName(structInfo.getName());
            parentBuilder = jarReflection.invokeObjMethod(methodName, parentBuilder, structObj);
/*            System.out.println("Struct Class : " + obj.getClass().getName() + ", setMethod : " + parentBuilder.getClass().getName() + "." + setterMethod);
            System.out.println("SubMessage Builder : " + parentBuilder);*/
        }
    }

    private Object buildSubMsg(String classType, String className, List<FieldInfo> fieldInfos, List<StructInfo> structInfos) {
        try {
            // SubMessage builder
            Object builder = jarReflection.getNewBuilder(pkgBase + classType);
            Map<String, String> fields = new HashMap<>();

            // Struct Node Message build
            setStructMsg(builder, structInfos);

            for (FieldInfo fieldInfo : fieldInfos) {
                String fieldName = fieldInfo.getName();
                String methodName = jarReflection.getSetterMethodName(fieldName);
                FieldType type = fieldInfo.getType();
                String value = fieldInfo.getValue();

                // 값 체크

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

                // Type 별 세팅
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

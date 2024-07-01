package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.reflection.ReflectionUtil;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.HeaderBodyInfo;
import com.uangel.scenario.model.StructInfo;
import com.uangel.scenario.phases.OutgoingPhase;
import com.uangel.scenario.type.FieldType;
import com.uangel.scenario.type.OutMsgType;
import com.uangel.util.JsonUtil;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonMsgBuilder extends MsgBuilder {

    public JsonMsgBuilder(SessionInfo sessionInfo, OutMsgType type) {
        super(sessionInfo, type);
    }

    // SessionInfo Null
    public JsonMsgBuilder(Scenario scenario, OutMsgType type) {
        super(scenario, type);
    }

    @Override
    public byte[] build(OutgoingPhase outgoingPhase) {
        // Check Instance Type
        if (!checkType(outgoingPhase)) return new byte[0];

        try {
            JSONObject jsonObject = new JSONObject();
            buildJsonMsg(jsonObject, outgoingPhase.getHeaderBodyInfos());

            // Send Bytes
            String json = jsonObject.toJSONString();
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

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
                Map<String, String> fields = JsonUtil.getAllJsonFields(json);
                String sessionId = fields.get(scenario.getCmdInfo().getFieldKeyword());
                scenario.getSessionManager().changeSessionId(sessionInfo.getSessionId(), sessionId);
            }

            return bytes;
        } catch (Exception e) {
            log.error("JsonMsgBuilder.build.Exception ({})", outgoingPhase.getMsgName(), e);
        }

        return new byte[0];
    }

    private void buildJsonMsg(JSONObject jsonObject, List<HeaderBodyInfo> childrenInfo) {
        for (HeaderBodyInfo childInfo : childrenInfo) {
            JSONObject data = buildSubMsg(childInfo.getFieldInfos(), childInfo.getStructList());
            if (data.isEmpty()) continue;
            jsonObject.put(childInfo.getClassType(), data);
        }
    }

    private void setStructMsg(JSONObject parentObj, List<StructInfo> structInfos) {
        for (StructInfo structInfo : structInfos) {
            JSONObject structObj = buildSubMsg(structInfo.getFieldInfos(), structInfo.getStructList());
            if (structObj.isEmpty()) continue;
            parentObj.put(structInfo.getName(), structObj);
        }
    }

    private JSONObject buildSubMsg(List<FieldInfo> fieldInfos, List<StructInfo> structInfos) {

        try {
            JSONObject data = new JSONObject();
            Map<String, String> fields = new HashMap<>();

            // Struct Node Message build
            setStructMsg(data, structInfos);

            for (FieldInfo fieldInfo : fieldInfos) {
                String fieldName = fieldInfo.getName();
                FieldType type = fieldInfo.getType();
                String value = fieldInfo.getValue();

                // 값 체크

                // Exec
                if (StringUtil.notNull(fieldInfo.getExec())) {
                    String exec = fieldInfo.getExec();
                    value = ReflectionUtil.getExecResult(exec);
                    if (value == null) {
                        log.warn("JsonMsgBuilder - Exec field processing failed (fieldName:{}, exec:{})", fieldName, exec);
                        continue;
                    }
                }

                // Keyword
                KeywordMapper keywordMapper = scenario.getKeywordMapper();
                value = keywordMapper.replaceKeyword(value, sessionInfo);

                if (StringUtil.isNull(value)) continue;

                // Type 별 세팅
                if (FieldType.STR.equals(type)) {
                    data.put(fieldName, value);
                } else if (FieldType.INT.equals(type)) {
                    data.put(fieldName, Integer.parseInt(value));
                } else if (FieldType.LONG.equals(type)) {
                    data.put(fieldName, Long.parseLong(value));
                } else if (FieldType.BOOL.equals(type)) {
                    data.put(fieldName, Boolean.parseBoolean(value));
                }

                fields.put(fieldName, value);
            }

            if (!fields.isEmpty()) {
                if (ObjectUtils.isNotEmpty(sessionInfo)) sessionInfo.addFields(fields);
                else scenario.addFields(fields);
            }

            return data;

        } catch (Exception e) {
            log.error("JsonMsgBuilder.getSubMessage.Exception ", e);
        }

        return new JSONObject();
    }
}

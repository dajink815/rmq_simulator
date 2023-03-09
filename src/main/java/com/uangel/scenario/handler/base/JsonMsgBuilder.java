package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.reflection.ReflectionUtil;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.HeaderBodyInfo;
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

/**
 * @author dajin kim
 */
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

            // set subMessages
            for (HeaderBodyInfo msgInfo : outgoingPhase.getHeaderBodyInfos()) {
                JSONObject data = getSubMessage(msgInfo);
                if (data == null || data.isEmpty()) continue;
                jsonObject.put(msgInfo.getClassName(), data);
            }

            //log.debug("Pretty Json \r\n[{}]", JsonUtil.buildPretty(jsonObject));

            // Send Bytes
            String json = jsonObject.toJSONString();
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            // Type 별 로그
            if (isSendType()) {
                log.debug("Build SendMsg \r\n[{}]", JsonUtil.buildPretty(new String(bytes)));
            } else {
                log.debug("Build LoopMsg [{}]", outgoingPhase.getMsgName());
                System.out.println(JsonUtil.buildPretty(new String(bytes)));
            }

            return bytes;
        } catch (Exception e) {
            log.error("JsonMsgBuilder.build.Exception ", e);
        }

        return new byte[0];
    }


    public JSONObject getSubMessage(HeaderBodyInfo msgInfo) {

        try {
            JSONObject data = new JSONObject();
            List<FieldInfo> fieldInfos = msgInfo.getFieldInfos();
            Map<String, String> fields = new HashMap<>();

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

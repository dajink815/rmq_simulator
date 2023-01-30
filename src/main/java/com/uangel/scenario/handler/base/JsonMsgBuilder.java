package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.reflection.ReflectionUtil;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.HeaderBodyInfo;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.FieldType;
import com.uangel.util.JsonUtil;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author dajin kim
 */
@Slf4j
public class JsonMsgBuilder implements MsgBuilder {

    private final SessionInfo sessionInfo;
    private final Scenario scenario;

    public JsonMsgBuilder(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
        this.scenario = sessionInfo.getScenario();
    }

    @Override
    public byte[] build(SendPhase sendPhase) {
        try {
            JSONObject jsonObject = new JSONObject();
            for (HeaderBodyInfo msgInfo : sendPhase.getHeaderBodyInfos()) {
                JSONObject data = getSubMessage(msgInfo);
                if (data == null) continue;
                jsonObject.put(msgInfo.getClassName(), data);
            }

            //log.debug("Pretty Json \r\n[{}]", JsonUtil.buildPretty(jsonObject));

            // Send Bytes
            String json = jsonObject.toJSONString();
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            json = JsonUtil.buildPretty(new String(bytes));
            log.debug("Build SendMsg \r\n[{}]", json);

            // Parse Fields

            return bytes;
        } catch (Exception e) {
            log.error("JsonMsgBuilder.build.Exception ", e);
        }
        return new byte[0];
    }


    public JSONObject getSubMessage(HeaderBodyInfo msgInfo) {
        JSONObject data = new JSONObject();

        try {
            List<FieldInfo> fieldInfos = msgInfo.getFieldInfos();
            for (FieldInfo fieldInfo : fieldInfos) {
                String name = fieldInfo.getName();
                FieldType type = fieldInfo.getType();
                String value = fieldInfo.getValue();

                // 값 체크

                // Exec
                if (StringUtil.isNull(value)) {
                    String exec = fieldInfo.getExec();
                    value = ReflectionUtil.getExecResult(exec);
                    if (value == null) continue;
                }

                // Keyword
                KeywordMapper keywordMapper = scenario.getKeywordMapper();
                value = keywordMapper.replaceKeyword(sessionInfo, value);

                // Type 별 세팅
                if (FieldType.STR.equals(type)) {
                    data.put(name, value);
                } else if (FieldType.INT.equals(type)) {
                    data.put(name, Integer.parseInt(value));
                } else if (FieldType.LONG.equals(type)) {
                    data.put(name, Long.parseLong(value));
                } else if (FieldType.BOOL.equals(type)) {
                    data.put(name, Boolean.parseBoolean(value));
                }

            }

        } catch (Exception e) {
            log.error("JsonMsgBuilder.getSubMessage.Exception ", e);
        }
        return data;
    }
}

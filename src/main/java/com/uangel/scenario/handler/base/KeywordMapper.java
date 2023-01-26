package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.reflection.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dajin kim
 */
@Slf4j
public class KeywordMapper {
    private static KeywordMapper keywordMapper = null;

    public static final Pattern keyPattern = Pattern.compile("\\[(.*?)\\]");
    private final Map<String, String> execCmdMap = new HashMap<>();

    private KeywordMapper() {
        // nothing
    }

    public static KeywordMapper getInstance() {
        if (keywordMapper == null)
             keywordMapper = new KeywordMapper();
        return keywordMapper;
    }

    public void addUserCmd(String cmd, String execStr) {
        execCmdMap.put(cmd, execStr);
    }
    public String getExecByCmd(String cmd) {
        return execCmdMap.get(cmd);
    }
    public Map<String, String> getExecCmdMap() {
        return execCmdMap;
    }

    public String replaceKeyword(SessionInfo sessionInfo, String keyword) {
        Matcher m = keyPattern.matcher(keyword);

        // [] 포함돼 있는 모든 단어 처리
        while (m.find()) {
            String result = getValue(sessionInfo, m.group(1));   // 중괄호 제외한 값
            if (result != null) keyword = keyword.replace(m.group(0), result);
        }
        return keyword;
    }

    private String getValue(SessionInfo sessionInfo, String keyword) {

        try {
            // 저장된 exec 명령어 처리
            String cmd;
            if ((cmd = getExecByCmd(keyword)) != null) {

                // exec 명령어로 Reflection 실행
/*                    ReflectionUtil.TypeValuePair typeValuePair = ReflectionUtil.exec(cmd);
                    if (typeValuePair != null) {
                        keyword = typeValuePair.value.toString();
                    }*/

                return ReflectionUtil.getExecResult(cmd);
            }

            switch (keyword) {
                case "call_id" :
                    return sessionInfo.getSessionId();
                case "call_number" :
                    return Integer.toString(sessionInfo.getSessionNum());
                default:
                    break;
            }

            if (keyword.startsWith("last_")) {
                return getLastKeyword(sessionInfo, keyword);
            }

        } catch (Exception e) {
            log.error("KeywordMapper.getValue.Exception ", e);
        }
        return null;
    }

    public String getLastKeyword(SessionInfo sessionInfo, String keyword) {
        Map<String, String> fields = sessionInfo.getFields();
        String fieldName = keyword.substring("last_".length());

        if (fields != null && fields.containsKey(fieldName)) {
            return fields.get(fieldName); // trim?
        }
        return null;
    }
}

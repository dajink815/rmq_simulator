package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.reflection.ReflectionUtil;
import com.uangel.scenario.Scenario;
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
    private static final Pattern keyPattern = Pattern.compile("\\[(.*?)\\]");
    private static final String LAST = "last_";
    private final Map<String, String> execCmdMap = new HashMap<>();

    private final Scenario scenario;

    public KeywordMapper(Scenario scenario) {
        this.scenario = scenario;
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

    public String replaceKeyword(String keyword, SessionInfo sessionInfo) {
        String before = keyword;
        Matcher m = keyPattern.matcher(keyword);

        // [] 포함돼 있는 모든 단어 처리
        while (m.find()) {
            String result = getValue(m.group(1), sessionInfo);   // 중괄호 제외한 값
            if (result != null) keyword = keyword.replace(m.group(0), result);
        }
        //if (!before.equals(keyword)) log.debug("ReplaceKeyword ({} -> {})", before, keyword);
        return keyword;
    }

    private String getValue(String keyword, SessionInfo sessionInfo) {
        try {
            // todo 예외처리 - exec 예약 명령어 없는 경우 로그, last_ 필드값 없는 경우
            
            // 저장된 exec 명령어 처리
            String cmd;
            if ((cmd = getExecByCmd(keyword)) != null) {
                // todo 예외처리 테스트
                return ReflectionUtil.getExecResult(cmd);
            }

            switch (keyword) {
                case "call_id" :
                    if (sessionInfo != null) return sessionInfo.getSessionId();
                    break;
                case "call_number" :
                    if (sessionInfo != null) return Integer.toString(sessionInfo.getSessionNum());
                    break;
                case "rmq_local" :
                    return scenario.getCmdInfo().getRmqLocal();
                case "rmq_target" :
                    return scenario.getCmdInfo().getRmqTarget();
                default:
                    break;
            }

            if (keyword.startsWith(LAST)) {
                String fieldName = keyword.substring(LAST.length());
                if (sessionInfo != null) return sessionInfo.getFieldValue(fieldName);
                else return scenario.getFieldValue(fieldName);
            }

        } catch (Exception e) {
            log.error("KeywordMapper.getValue.Exception ", e);
        }
        return null;
    }
}

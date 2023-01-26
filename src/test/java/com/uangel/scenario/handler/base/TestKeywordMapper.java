package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dajin kim
 */
@Slf4j
public class TestKeywordMapper {

    private final KeywordMapper keywordMapper = KeywordMapper.getInstance();
    private final SessionInfo sessionInfo = new SessionInfo(0);

    @Before
    public void prepareUserCmd() {
        keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
        keywordMapper.addUserCmd("timestamp", "java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\"))");
        //keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");

        sessionInfo.addField("tId" ,"TEST_TID");
    }

    @Test
    public void testExecCmd() {
        String keyword = "[tId]_[timestamp]";
        String result = keywordMapper.replaceKeyword(sessionInfo, keyword);
        log.debug("{} => {}", keyword, result);

        keyword = "[timestamp]";
        result = keywordMapper.replaceKeyword(sessionInfo, keyword);
        log.debug("{} => {}", keyword, result);
    }

    @Test
    public void testLastKeyword() {
        String keyword = "last_tId";
        String result = keywordMapper.getLastKeyword(sessionInfo, keyword);
        log.debug("{} => {}", keyword, result);
    }

    @Test
    public void testReplaceKeyword() {
        String keyword = "[call_id]~~[last_tId],CallNum=[call_number]";
        String result = keywordMapper.replaceKeyword(sessionInfo, keyword);
        log.debug("1. {} => \r\n{}", keyword, result);

        // 저장 되지 않는 exec, keyword 호출 -> 문자열 그대로 리턴
        keyword = "[dialog],CallNum=[call_number]";
        result = keywordMapper.replaceKeyword(sessionInfo, keyword);
        log.debug("2. {} => \r\n{}", keyword, result);

        // 저장 되지 않은 last field 호출 -> 문자열 그대로 리턴
        keyword = "[last_dialog]//[timestamp]";
        result = keywordMapper.replaceKeyword(sessionInfo, keyword);
        log.debug("3. {} => \r\n{}", keyword, result);
    }
}

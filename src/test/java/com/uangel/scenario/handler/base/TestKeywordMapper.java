package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author dajin kim
 */
@Slf4j
public class TestKeywordMapper {

    private KeywordMapper keywordMapper;
    private SessionInfo sessionInfo;

    @Before
    public void prepareUserCmd() throws IOException, SAXException {

        String filePath = "./src/main/resources/scenario/mrfc_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);

        keywordMapper = new KeywordMapper(scenario);
        keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
        keywordMapper.addUserCmd("timestamp", "java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\"))");
        //keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");

        sessionInfo = new SessionInfo(0, scenario);
        sessionInfo.addField("tId" ,"TEST_TID");
        scenario.addField("tId", "SCENARIO_TID");
    }

    @Test
    public void testExecCmd() {
        String keyword = "[tId]_[timestamp]";
        String result = keywordMapper.replaceKeyword(keyword, sessionInfo);
        log.debug("{} => {}", keyword, result);

        keyword = "[timestamp]";
        result = keywordMapper.replaceKeyword(keyword, sessionInfo);
        log.debug("{} => {}", keyword, result);
    }

    @Test
    public void testReplaceKeyword() {
        String keyword = "[call_id]~~[last_tId],CallNum=[call_number]";
        String result = keywordMapper.replaceKeyword(keyword, sessionInfo);
        log.debug("1. {} => \r\n{}", keyword, result);

        // 저장 되지 않는 exec, keyword 호출 -> 문자열 그대로 리턴
        keyword = "[dialog],CallNum=[call_number]";
        result = keywordMapper.replaceKeyword(keyword, sessionInfo);
        log.debug("2. {} => \r\n{}", keyword, result);

        // 저장 되지 않은 last field 호출 -> 문자열 그대로 리턴
        keyword = "[last_dialog]//[timestamp]";
        result = keywordMapper.replaceKeyword(keyword, sessionInfo);
        log.debug("3. {} => \r\n{}", keyword, result);
    }

    @Test
    public void testReplaceLastField() {
        String keyword = "[last_tId]";
        String result = keywordMapper.replaceKeyword(keyword, null);
        log.debug("1. {} => {}", keyword, result);

        // 저장 되지 않은 last field 호출 -> 문자열 그대로 리턴
        keyword = "[last_dialog]//[timestamp]";
        result = keywordMapper.replaceKeyword(keyword, null);
        log.debug("2. {} => {}", keyword, result);

        // 저장 되지 않은 keyword 호출 -> 문자열 그대로 리턴
        keyword = "[call_id], CallNum=[call_number]";
        result = keywordMapper.replaceKeyword(keyword, null);
        log.debug("3. {} => {}", keyword, result);
    }

}

package com.uangel.rmq.handler;

import com.uangel.command.CommandInfo;
import com.uangel.reflection.JarReflection;
import com.uangel.reflection.ProtoUtil;
import com.uangel.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqProtoConsumer {
    private final Scenario scenario;
    private final CommandInfo config;
    private final JarReflection jarReflection;
    private final IncomingHandler handler;


    public RmqProtoConsumer(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
        this.jarReflection = scenario.getJarReflection();
        this.handler = new IncomingHandler(scenario);
    }

    public void protoMsgProcessing(byte[] msg) {
        if (scenario.isTestEnded()) return;

        try {
            // Byte Array -> Object
            String className = config.getProtoPkg() + scenario.getMsgClassName();
            Object msgObj = jarReflection.parseFrom(className, msg);

            if (msgObj == null) {
                log.warn("RmqProtoConsumer.protoMsgProcessing Fail - MSgObj is Null [Class:{}]", className);
                return;
            }
            // Object -> Pretty Json
            String json = ProtoUtil.buildProto(msgObj);

            // Parse Keyword Field
            Map<String, String> fields = jarReflection.getAllFieldsMap(msgObj);
            String sessionId = fields.get(config.getFieldKeyword());
/*            if (sessionId == null) {
                // LogIn HB 같은 메시지는 keyword field 없음
                log.debug("Doesnt have sessionId [{}]", fields.get("type"));
                //return;
            } else {
                log.debug("RmqProtoConsumer RecvMsg [{}]", msgObj);
            }*/

            {
                String jsonUpper = json.toUpperCase();
                if (!jsonUpper.contains("HB") && !jsonUpper.contains("HEARTBEAT"))
                    log.debug("RmqProtoConsumer RecvMsg [{}]", msgObj);
            }

            handler.handle(sessionId, json, fields);

/*            if (sessionManager == null) {
                log.warn("RmqProtoConsumer Fail - SessionManager is Null");
                return;
            }

            if (fields.isEmpty()) {
                log.warn("RmqProtoConsumer Fail - Msg Fields Map is Empty");
                return;
            }

            // sessionId로 SessionManager 에 등록된 SessionInfo 조회
            SessionInfo sessionInfo = sessionManager.getSessionInfo(sessionId);
            if (sessionInfo != null) {
                ProcRecvPhase recvPhase = sessionInfo.getProcRecvPhase();
                recvPhase.handleMessage(json, sessionId, fields);
            } else if (!scenario.isOutScenario()) {
                if (sessionId == null || sessionId.isEmpty()) return;

                // SessionInfo 없는 경우 세션 생성 후 시나리오 첫번째 부터 시작
                // SessionManager createSessionInfo 호출 -> sessionId 인자값 전달
                SessionInfo newSessionInfo = sessionManager.createSessionInfo(sessionId);
                newSessionInfo.getCurrentIdx().set(scenario.getFirstRecvPhaseIdx());

                if (newSessionInfo != null) {
                    if (!newSessionInfo.getProcRecvPhase().handleMessage(json, sessionId, fields))
                        log.warn("[{}] [{}] ProtoConsumer Fail - new session recvPhase.handleMessage fail [MSG:{}]", scenario.getName(), sessionId, json);
                } else {
                    log.warn("[{}] [{}] ProtoConsumer Fail - new session created fail [MSG:{}]", scenario.getName(), sessionId, json);
                }
            }*/

        } catch (Exception e) {
            log.error("RmqProtoConsumer.protoMsgProcessing.Exception ", e);
        }

    }
}

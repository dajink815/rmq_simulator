package com.uangel.rmq.handler;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.model.SessionManager;
import com.uangel.reflection.JarReflection;
import com.uangel.reflection.ProtoUtil;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.phases.ProcRecvPhase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqProtoConsumer {
    private final Scenario scenario;
    private final CommandInfo config;
    private final JarReflection jarReflection;
    private final SessionManager sessionManager;

    public RmqProtoConsumer(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
        this.jarReflection = scenario.getJarReflection();
        this.sessionManager = scenario.getSessionManager();
    }

    public void protoMsgProcessing(byte[] msg) {
        if (scenario.isTestEnded()) return;

        try {
            // Byte Array -> Object
            String className = config.getProtoPkg() + scenario.getMsgClassName();
            log.debug("RmqProtoConsumer ClassName [{}]", className);
            Object msgObj = jarReflection.parseFrom(className, msg);
            log.debug("RmqProtoConsumer RecvMsg [{}]", msgObj);

            // Object -> Pretty Json
            String json = ProtoUtil.buildProto(msgObj);
            log.debug("RmqProtoConsumer PrettyJson [{}]", json);

            // Parse KeyWord
            Map<String, String> fields = jarReflection.getAllFieldsMap(msgObj);
            String sessionId = fields.get(config.getFieldKeyword());

            // sessionId로 SessionManager 에 등록된 SessionInfo 조회
            SessionInfo sessionInfo = sessionManager.getSessionInfo(sessionId);
            if (sessionInfo != null) {
                ProcRecvPhase recvPhase = sessionInfo.getProcRecvPhase();
                recvPhase.handleMessage(json, sessionId, fields);
            }
            // SessionInfo 조회 실패시 ProcRecvPhase 조회
            else {
                List<ProcRecvPhase> procRecvPhaseList = sessionManager.getRecvPhaseList();
                if (procRecvPhaseList.isEmpty()) return;

                scenario.getExecutorService().submit(() -> {
                    for (ProcRecvPhase procRecvPhase : procRecvPhaseList) {
                        if(procRecvPhase.handleMessage(json, sessionId, fields)){
                            break;
                        }
                    }
                });
            }

        } catch (Exception e) {
            log.error("RmqProtoConsumer.protoMsgProcessing.Exception ", e);
        }

    }
}

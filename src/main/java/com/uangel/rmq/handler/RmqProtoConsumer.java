package com.uangel.rmq.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.model.SessionManager;
import com.uangel.reflection.JarReflection;
import com.uangel.reflection.ProtoUtil;
import com.uangel.scenario.handler.phases.ProcRecvPhase;
import com.uangel.service.AppInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqProtoConsumer {

    private final AppInstance appInstance = AppInstance.getInstance();
    private final CommandInfo config = appInstance.getCmdInfo();
    private final JarReflection jarReflection = appInstance.getJarReflection();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public RmqProtoConsumer() {
        // nothing
    }

    public void protoMsgProcessing(byte[] msg) {
        if (appInstance.isTestEnded()) return;

        try {
            // Byte Array -> Object
            String className = config.getProtoPkg() + config.getMsgClass();
            Object msgObj = jarReflection.parseFrom(className, msg);

            // Object -> Pretty Json
            String json = ProtoUtil.buildProto(msgObj);

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

                appInstance.getExecutorService().submit(() -> {
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

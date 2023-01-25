package com.uangel.model;

import com.uangel.scenario.Scenario;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.RecvPhase;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.PhaseType;
import com.uangel.service.AppInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * RMQ 메시지 관련 정보 관리
 *
 * @author dajin kim
 */
@Slf4j
public class MsgInfoManager {
    private static MsgInfoManager manager = null;
    private final List<MsgInfo> msgInfoList = new ArrayList<>();

    private MsgInfoManager() {
        // nothing
    }

    public static MsgInfoManager getInstance() {
        if (manager == null)
            manager= new MsgInfoManager();
        return manager;
    }

    public List<MsgInfo> getMsgInfoList() {
        return msgInfoList;
    }

    public void initList() {
        Scenario scenario = AppInstance.getInstance().getScenario();
        if (scenario == null) {

            return;
        }
        List<MsgPhase> phases = scenario.phases();
        for (MsgPhase msgPhase : phases) {
            if (msgPhase instanceof SendPhase) {
                SendPhase sendPhase = (SendPhase) msgPhase;
                createMsgInfo(sendPhase.getMsgName(), PhaseType.SEND);
            } else if (msgPhase instanceof RecvPhase) {
                RecvPhase recvPhase = (RecvPhase) msgPhase;
                createMsgInfo(recvPhase.getMsgName(), PhaseType.RECV);
            } else {
                createMsgInfo(PhaseType.PAUSE.getValue(), PhaseType.PAUSE);
            }
        }
    }

    // MsgInfo List 는 초기 생성 후 추가 & 삭제 데이터 변동 없음
    public void createMsgInfo(String msgName, PhaseType phaseType) {
        synchronized (msgInfoList) {
            MsgInfo msgInfo = new MsgInfo(msgName, phaseType);
            msgInfoList.add(msgInfo);
        }
    }

    public MsgInfo getMsgInfo(int idx) {
        return msgInfoList.get(idx);
    }

    public int getMsgInfoIdx(MsgInfo msgInfo) {
        return msgInfoList.indexOf(msgInfo);
    }

    public List<String> getMsgNameList() {
        List<String> msgNameList = new ArrayList<>();
        synchronized (msgInfoList) {
            for (MsgInfo msgInfo : msgInfoList) {
                msgNameList.add(msgInfo.getMsgName());
            }
        }
        return msgNameList;
    }
}

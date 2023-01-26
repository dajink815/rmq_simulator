package com.uangel.model;

import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.RecvPhase;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.PhaseType;
import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * RMQ 메시지 관련 정보 관리
 *
 * @author dajin kim
 */
@Slf4j
@Getter
public class MsgInfoManager {
    protected final List<MsgInfo> msgInfoList = new ArrayList<>();
    protected String msgClassName;

    protected MsgInfoManager() {
        // nothing
    }

    public List<MsgInfo> getMsgInfoList() {
        return msgInfoList;
    }

    protected void initList(List<MsgPhase> phases) {
        if (phases == null || phases.isEmpty()) {
            return;
        }

        for (MsgPhase msgPhase : phases) {
            if (msgPhase instanceof SendPhase) {
                SendPhase sendPhase = (SendPhase) msgPhase;
                createMsgInfo(sendPhase.getMsgName(), PhaseType.SEND);
                setMsgClassName(sendPhase.getClassName());
            } else if (msgPhase instanceof RecvPhase) {
                RecvPhase recvPhase = (RecvPhase) msgPhase;
                createMsgInfo(recvPhase.getMsgName(), PhaseType.RECV);
                setMsgClassName(recvPhase.getClassName());
            } else {
                createMsgInfo(PhaseType.PAUSE.getValue(), PhaseType.PAUSE);
            }
        }
    }

    // MsgInfo List 는 초기 생성 후 추가 & 삭제 데이터 변동 없음
    private void createMsgInfo(String msgName, PhaseType phaseType) {
        synchronized (msgInfoList) {
            MsgInfo msgInfo = new MsgInfo(msgName, phaseType);
            msgInfoList.add(msgInfo);
        }
    }

    private void setMsgClassName(String name) {
        if (StringUtil.isNull(msgClassName))
            msgClassName = name;
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

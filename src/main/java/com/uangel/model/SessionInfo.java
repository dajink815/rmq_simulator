package com.uangel.model;

import com.uangel.executor.UScheduledExecutorService;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.phases.ProcPausePhase;
import com.uangel.scenario.handler.phases.ProcRecvPhase;
import com.uangel.scenario.handler.phases.ProcSendPhase;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.PausePhase;
import com.uangel.scenario.phases.RecvPhase;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.service.AppInstance;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dajin kim
 */
@Slf4j
@Getter
public class SessionInfo {
    private static final AppInstance instance = AppInstance.getInstance();

    private final UScheduledExecutorService executorService;
    private final Scenario scenario = instance.getScenario();

    private String sessionId;
    private int sessionNum;

    // todo 무슨 차이?
    private final AtomicInteger currentIdx = new AtomicInteger();
    private final AtomicInteger phaseCounter = new AtomicInteger();
    private long lastExecTime = System.currentTimeMillis();

    private final ProcSendPhase procSendPhase;
    private final ProcRecvPhase procRecvPhase;
    private final ProcPausePhase procPausePhase;

    private final Map<String, String> fields = new HashMap<>();

    private boolean isSessionEnded = false;

    public SessionInfo(int sessionNum) {
        this.sessionNum = sessionNum + 1;
        // 중복 체크?
        this.sessionId = UUID.randomUUID() + "_" + this.sessionNum;
        this.executorService = instance.getExecutorService();
        this.procSendPhase = new ProcSendPhase(this);
        this.procRecvPhase = new ProcRecvPhase(this);
        this.procPausePhase = new ProcPausePhase(this);
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void addFieldValue(Map<String, String> addFields) {
        fields.putAll(addFields);
    }

    public String getFieldValue(String key) {
        return fields.get(key);
    }

    public void start() {
        this.execPhase(0);
    }

    public void stop(String reason) {
        this.isSessionEnded = true;

        // stat
        //log.debug("({}) Call Ended [{}] [{}]", callInfo.getCallId(), reason, this.scenarioStat);
    }

    public int getCurIdx() {
        return currentIdx.get();
    }

    public int increaseCurIdx() {
        return currentIdx.incrementAndGet();
    }

    public void execPhase(int step) {
        if (step >= instance.getScenarioSize()) {
            stop("End Of Scenario");
            return;
        }

        this.executorService.submit(() -> {
            try {
                if (isSessionEnded) return;
                phaseCounter.incrementAndGet();
                MsgPhase phase = scenario.phases().get(step);
                if (phase instanceof SendPhase) {
                    //log.debug("({}) SEND Phase Start [{}]", callInfo.getCallId(), phase.getIdx());
                    procSendPhase.run(phase);
                } else if (phase instanceof RecvPhase) {
                    //log.debug("({}) RECV Phase Start [{}]", callInfo.getCallId(), phase.getIdx());
                    procRecvPhase.run(phase);
                }  else if (phase instanceof PausePhase) {
                    //log.debug("({}) PAUSE Phase Start [{}] [{}]", callInfo.getCallId(), phase.getIdx(), ((Pause) phase).getMillisecnods());
                    procPausePhase.run(phase);
                }
                lastExecTime = System.currentTimeMillis();
            } catch (Exception e) {
                //log.warn("({}) Err Occurs while exec call phase.", callInfo.getCallId(), e);
                stop("Exception Occurs while execute call phase. " + e);
            }
        });
    }
}

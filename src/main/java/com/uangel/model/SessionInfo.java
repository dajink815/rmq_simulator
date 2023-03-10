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

    private final UScheduledExecutorService executorService;
    private String sessionId;
    private final int sessionNum;
    private final Scenario scenario;

    private final ProcSendPhase procSendPhase;
    private final ProcRecvPhase procRecvPhase;
    private final ProcPausePhase procPausePhase;

    private final AtomicInteger currentIdx = new AtomicInteger();
    private final Map<String, String> fields = new HashMap<>();
    private boolean isSessionEnded = false;

    public SessionInfo(int sessionNum, Scenario scenario) {
        this.sessionNum = sessionNum + 1;
        // 중복 체크?
        this.sessionId = UUID.randomUUID() + "_" + this.sessionNum;
        this.scenario = scenario;
        this.executorService = scenario.getExecutorService();
        this.procSendPhase = new ProcSendPhase(this);
        this.procRecvPhase = new ProcRecvPhase(this);
        this.procPausePhase = new ProcPausePhase(this);
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void addFields(Map<String, String> addFields) {
        fields.putAll(addFields);
    }
    public void addField(String fieldName, String value) {
        fields.put(fieldName, value);
    }

    public String getFieldValue(String key) {
        return fields.get(key);
    }

    public void start() {
        this.execPhase(0);
    }

    public void stop(String reason) {
        this.isSessionEnded = true;

        // todo stat
        log.debug("({}) Session Ended [{}]", sessionId, reason);
    }

    public int getCurIdx() {
        return currentIdx.get();
    }

    public int increaseCurIdx() {
        return currentIdx.incrementAndGet();
    }

    public void execPhase(int step) {
        if (step >= scenario.getScenarioSize()) {
            stop("End Of Scenario");
            return;
        }

        this.executorService.submit(() -> {
            try {
                if (isSessionEnded) return;
                MsgPhase phase = scenario.phases().get(step);
                if (phase instanceof SendPhase) {
                    log.debug("({}) SEND Phase Start [{}]", sessionId, phase.getIdx());
                    procSendPhase.run(phase);
                } else if (phase instanceof RecvPhase) {
                    log.debug("({}) RECV Phase Start [{}]", sessionId, phase.getIdx());
                    procRecvPhase.run(phase);
                }  else if (phase instanceof PausePhase p) {
                    log.debug("({}) PAUSE Phase Start [{}] [{}]", sessionId, phase.getIdx(), p.getMilliSeconds());
                    procPausePhase.run(p);
                }
            } catch (Exception e) {
                log.warn("({}) Err Occurs while exec call phase.", sessionId, e);
                stop("Exception Occurs while execute call phase. " + e);
            }
        });
    }
}

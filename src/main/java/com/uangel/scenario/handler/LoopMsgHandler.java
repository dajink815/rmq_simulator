package com.uangel.scenario.handler;

import com.uangel.executor.UScheduledExecutorService;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.phases.ProcLoopPhase;
import com.uangel.scenario.phases.LoopPhase;
import com.uangel.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dajin kim
 */
@Slf4j
public class LoopMsgHandler {

    private final Scenario scenario;
    private final UScheduledExecutorService executorService;
    private final ProcLoopPhase procLoopPhase;
    private final List<LoopPhase> loopPhases;

    public LoopMsgHandler(Scenario scenario) {
        this.scenario = scenario;
        this.procLoopPhase = new ProcLoopPhase(scenario);
        this.executorService = scenario.getExecutorService();
        this.loopPhases = scenario.getLoopPhases();
    }

    public void start() {
        if (executorService == null) return;

        for (LoopPhase loopPhase : loopPhases) {
            int reTrans = loopPhase.getReTrans();

            // re-trans 0 혹은 없으면 process 전체 1회만 실행
            if (reTrans == 0) {
                log.info("Process Loop Message ({})", loopPhase.getMsgName());
                procLoopPhase.run(loopPhase);
            }
            // re-trans 주기로 process 종료 전까지 반복 실행하도록 스케쥴러에 등록
            else {
                log.info("Register Loop Message ({}, {}ms)", loopPhase.getMsgName(), loopPhase.getReTrans());
                executorService.scheduleWithFixedDelay(
                        () -> procLoopPhase.run(loopPhase), 0, reTrans, TimeUnit.MILLISECONDS);
            }

            // Multiple LoopPhases 메시지 간 간격
            SleepUtil.trySleep(500);
        }

    }



}

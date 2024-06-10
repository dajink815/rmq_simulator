package com.uangel.model;

import com.uangel.command.CommandInfo;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.phases.ProcRecvPhase;
import com.uangel.util.SleepUtil;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dajin kim
 */
@Slf4j
public class SessionManager {

    // 처리 중인 세션 관리
    private final Map<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();
    // 처리한 총 세션 개수
    private final AtomicInteger totalSessionCnt = new AtomicInteger();

    private final Scenario scenario;

    public SessionManager(Scenario scenario) {
        log.info("[{}] SessionManager Started", scenario.getName());
        this.scenario = scenario;
    }

    public void createSessionByRate() {
        // Inbound 시나리오는 세션 미리 생성 X
        if (!scenario.isOutScenario()) {
            log.info("[{}] Scenario is Inbound type. Don't create session periodically.", scenario.getName());
            return;
        }

        UScheduledExecutorService executorService = scenario.getExecutorService();
        if (executorService == null) return;
        CommandInfo cmdInfo = scenario.getCmdInfo();

        // RatePeriod 간격으로 Rate 개수만큼 new Session 생성
        executorService.scheduleAtFixedRate(() -> {
            for (int i = 1; i <= cmdInfo.getRate(); i++) {
                // check gracefully shutdown
                this.createSessionInfo();
                if (i % 100 == 0) SleepUtil.trySleep(20);
            }
        }, 0, cmdInfo.getRatePeriod(), TimeUnit.MILLISECONDS);
    }

    public void removeEndedSession() {
        // 시뮬레이터 동작 1초 후부터 매 1초마다 종료된 세션 삭제
        UScheduledExecutorService executorService = scenario.getExecutorService();
        if (executorService == null) return;


        // scheduleWithFixedDelay : 종료 처리 끝난 시간으로 부터 1000ms 후마다 반복
        executorService.scheduleWithFixedDelay(() -> this.getSessionList().stream()
                        .filter(Objects::nonNull)
                        .filter(SessionInfo::isSessionEnded)
                        .forEach(this::deleteSessionInfo),
                1000, 1000, TimeUnit.MILLISECONDS);
    }

    public SessionInfo getSessionInfo(String sessionId) {
        if (StringUtil.isNull(sessionId)) return null;
        SessionInfo sessionInfo = sessionMap.get(sessionId);
        if (sessionInfo == null) {
            log.warn("sessionInfo [{}] is null", sessionId);
        }
        return sessionInfo;
    }

    // todo Session 생성하는 스레드 풀과 실행하는 스레드 풀 분리
    private void createSessionInfo() {
        // shutdown 체크?
        CommandInfo cmdInfo = scenario.getCmdInfo();
        if (sessionMap.size() >= cmdInfo.getLimit() ||
                (cmdInfo.getMaxCall() > 0 && getTotalSessionCnt() >= cmdInfo.getMaxCall())) {
            // log
            return;
        }

        try {
            //SessionInfo sessionInfo = new SessionInfo(getTotalSessionCnt(), scenario);
            SessionInfo sessionInfo = new SessionInfo(getTotalSessionCnt() + 1, scenario);
            if (sessionMap.putIfAbsent(sessionInfo.getSessionId(), sessionInfo) == null) {
                log.info("Created SessionInfo [{}]", sessionInfo.getSessionId());
                sessionInfo.start();
                totalSessionCnt.getAndIncrement();
            } else {
                log.warn("Created SessionInfo Fail - Duplicated SessionID [{}]", sessionInfo.getSessionId());
            }
        } catch (Exception e) {
            log.warn("SessionManager.createSessionInfo.Exception ", e);
        }

    }

    // sessionId 인자 값 받아 SessionInfo 생성하는 함수
    public SessionInfo createSessionInfo(String sessionId) {
        // todo Inbound 시나리오도 세션 생성 조건 추가? or 메시지 받는대로 생성?

        try {
            if (sessionId == null) return null;

            return sessionMap.computeIfAbsent(sessionId, info -> {
                SessionInfo sessionInfo = new SessionInfo(sessionId, getTotalSessionCnt() + 1, scenario);
                log.info("Created SessionInfo [{}]", sessionId);

                // 메시지 받아서 세션 생성되는 경우는 createSessionInfo 에서 start 호출 X, 메시지 수신한 곳에서 처리
                //sessionInfo.start();
                totalSessionCnt.getAndIncrement();
                return sessionInfo;
            });
        } catch (Exception e) {
            log.warn("SessionManager.createSessionInfo.Exception ", e);
        }
        return null;
    }

    private void deleteSessionInfo(SessionInfo sessionInfo) {
        SessionInfo removeInfo = sessionMap.remove(sessionInfo.getSessionId());
        if (removeInfo != null) {
            log.info("Deleted SessionInfo [{}]", sessionInfo.getSessionId());
        }
    }

    public synchronized void changeSessionId(String curId, String changeId) {
        if (sessionMap.containsKey(curId) && !curId.equals(changeId)) {
            log.info("Changed Session ID [{} -> {}]", curId, changeId);
            SessionInfo sessionInfo = sessionMap.get(curId);
            sessionMap.remove(curId);
            sessionInfo.setSessionId(changeId);
            sessionMap.put(changeId, sessionInfo);
        }
    }

    public List<SessionInfo> getSessionList() {
        synchronized (sessionMap) {
            return new ArrayList<>(sessionMap.values());
        }
    }

    public List<ProcRecvPhase> getRecvPhaseList() {
        //return getSessionList().stream().map(SessionInfo::getProcRecvPhase).toList();
        return getSessionList().stream()
                .filter(sessionInfo -> sessionInfo.getCurIdx() == scenario.getFirstRecvPhaseIdx())
                .map(SessionInfo::getProcRecvPhase).toList();
    }

    public int getCurrentSessionCnt() {
        return sessionMap.size();
    }

    public boolean isSessionEmpty() {
        return sessionMap.isEmpty();
    }

    public int getTotalSessionCnt() {
        return totalSessionCnt.get();
    }

    public boolean checkIndex(SessionInfo sessionInfo) {
        return sessionInfo.getCurIdx() < scenario.getScenarioSize();
    }
}

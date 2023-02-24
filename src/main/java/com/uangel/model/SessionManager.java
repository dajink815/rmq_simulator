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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author dajin kim
 */
@Slf4j
public class SessionManager {

    // 처리 중인 세션 관리
    private final Set<SessionInfo> sessionList = ConcurrentHashMap.newKeySet();
    private final Map<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();
    // 처리한 총 세션 개수
    private final AtomicInteger totalSessionCnt = new AtomicInteger();

    private final Scenario scenario;

    public SessionManager(Scenario scenario) {
        log.info("[{}] SessionManager Started", scenario.getName());
        this.scenario = scenario;
    }

    public void createSessionByRate() {
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

    private void createSessionInfo() {
        // shutdown 체크?
        CommandInfo cmdInfo = scenario.getCmdInfo();
        if (sessionList.size() >= cmdInfo.getLimit() || (cmdInfo.getMaxCall() > 0
                && getTotalSessionCnt() >= cmdInfo.getMaxCall())) {
            // log
            return;
        }

        try {
            SessionInfo sessionInfo = new SessionInfo(getTotalSessionCnt(), scenario);
            log.info("Created SessionInfo [{}]", sessionInfo.getSessionId());
            sessionList.add(sessionInfo);
            sessionMap.put(sessionInfo.getSessionId(), sessionInfo);
            sessionInfo.start();
            increaseTotalSessionCnt();
        } catch (Exception e) {
            log.warn("SessionManager.createSessionInfo.Exception ", e);
        }

    }

    private void deleteSessionInfo(SessionInfo sessionInfo) {
        log.info("Deleted SessionInfo [{}]", sessionInfo.getSessionId());
        sessionList.remove(sessionInfo);
    }

    public void changeSessionId(String curId, String changeId) {
        if (sessionMap.containsKey(curId) && !curId.equals(changeId)) {
            log.info("Changed Session ID [{} -> {}]", curId, changeId);
            SessionInfo sessionInfo = sessionMap.get(curId);
            sessionMap.remove(curId);
            sessionInfo.setSessionId(changeId);
            sessionMap.put(changeId, sessionInfo);
        }
    }

    public List<SessionInfo> getSessionList() {
        synchronized (sessionList) {
            return new ArrayList<>(sessionList);
        }
    }

    public List<ProcRecvPhase> getRecvPhaseList() {
        return getSessionList().stream().map(SessionInfo::getProcRecvPhase).collect(Collectors.toList());
    }

    public boolean isSessionEmpty() {
        return sessionList.isEmpty();
    }

    public int getTotalSessionCnt() {
        return totalSessionCnt.get();
    }

    public void increaseTotalSessionCnt() {
        totalSessionCnt.getAndIncrement();
    }

    public boolean checkIndex(SessionInfo sessionInfo) {
        return sessionInfo.getCurIdx() < scenario.getScenarioSize();

    }
}

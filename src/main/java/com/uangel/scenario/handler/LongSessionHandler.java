package com.uangel.scenario.handler;

import com.uangel.command.CommandInfo;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.model.SessionInfo;
import com.uangel.model.SessionManager;
import com.uangel.scenario.Scenario;
import com.uangel.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
public class LongSessionHandler {
    private static final int INTERVAL = 1000;
    private final CommandInfo cmdInfo;
    private final SessionManager sessionManager;
    private final UScheduledExecutorService executorService;

    public LongSessionHandler(Scenario scenario) {
        log.info("[{}] LongSessionHandler Started", scenario.getName());
        this.cmdInfo = scenario.getCmdInfo();
        this.sessionManager = scenario.getSessionManager();
        this.executorService = scenario.getExecutorService();
    }

    public void start() {
        if (executorService == null) return;
        executorService.scheduleAtFixedRate(
                this::checkLongSession, INTERVAL - System.currentTimeMillis() % INTERVAL,
                INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void checkLongSession() {
        try {
            sessionManager.getSessionIds().stream()
                    .map(sessionManager::getSessionInfo)
                    .filter(Objects::nonNull)
                    .forEach(sessionInfo -> {
                        if (checkTimeout(sessionInfo)) {
                            String sessionId = sessionInfo.getSessionId();
                            String createTime = DateFormatUtil.formatYmdHmsS(sessionInfo.getCreateTime());
                            log.info("({}) Remove Long Session. C[{}] Gap[{}] Timer[{}s]",
                                    sessionId, createTime,
                                    System.currentTimeMillis() - sessionInfo.getCreateTime(),
                                    cmdInfo.getLongSession());
                            sessionManager.deleteSessionInfo(sessionInfo);
                        }
                    });
        } catch (Exception e) {
            log.error("Error has occurred while Handling Long Session", e);
        }
    }

    private boolean checkTimeout(SessionInfo sessionInfo) {
        long timer = cmdInfo.getLongSession() * 1000L;
        long createTime = sessionInfo.getCreateTime();
        return createTime > 0 && timer > 0
                && createTime + timer < System.currentTimeMillis();
    }
}

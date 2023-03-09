package com.uangel;

import com.uangel.command.CommandInfo;
import com.uangel.command.CommandLineManager;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.model.SessionManager;
import com.uangel.rmq.RmqManager;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.scenario.handler.LoopMsgHandler;
import com.uangel.scenario.handler.base.KeywordMapper;
import com.uangel.util.JsonUtil;
import com.uangel.util.SleepUtil;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.List;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class ScenarioRunner {

    private Scenario scenario;
    private UScheduledExecutorService scheduledExecutorService;
    private RmqManager rmqManager;

    //private boolean isShutdown = false;


    public ScenarioRunner() {
        // nothing
    }

    public String run(String[] args) {

        // Parse Command Line
        CommandInfo cmdInfo = CommandLineManager.parseCommandLine(args);
        if (cmdInfo == null) {

            return null;
        }

        try {
            // Build Scenario
            scenario = ScenarioBuilder.fromXMLFileName(cmdInfo.getScenarioFile());
            if (scenario == null) {
                log.error("ScenarioRunner Scenario Build Fail");
                return null;
            }

            // Scenario Validity


            String scenarioName = scenario.getName();
            scenario.setCmdInfo(cmdInfo);
            log.info("[{}] Scenario Parsing Completed {}", scenarioName, scenario.getMsgNameList());

            // Load ProtoBuf Package File
            if (!scenario.initJarReflection(cmdInfo)) {

                return null;
            }

            // Keyword
            KeywordMapper keywordMapper = new KeywordMapper(scenario);
            scenario.setKeywordMapper(keywordMapper);

            // User Defined Command (ReflectionUtil)
            keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
            keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");
            if (StringUtil.notNull(cmdInfo.getUserCmdFilePath())) {
                // parse & add UserCmd
                Map<String, String> userCmdFields = JsonUtil.getAllFileFields(cmdInfo.getUserCmdFilePath());
                userCmdFields.forEach(keywordMapper::addUserCmd);
            }
            log.debug("[{}] User Defined Command (Size:{})\r\n{}", scenarioName, keywordMapper.getExecCmdMapSize(), keywordMapper.getExecCmdMap());

            // Statistics


            String threadName = scenarioName + "@" + hashCode();
            Thread.currentThread().setName(threadName);

            // Create Thread Pool
            // todo 기본으로 필요한 스레드 최저 개수 체크
            int threadSize = cmdInfo.getThreadSize() <= 0 ?
                    Runtime.getRuntime().availableProcessors() : cmdInfo.getThreadSize();
            this.scheduledExecutorService = new UScheduledExecutorService(threadSize,
                    new BasicThreadFactory.Builder()
                            .namingPattern(threadName + "-%d")
                            .priority(Thread.MAX_PRIORITY)
                            .build());
            log.info("[{}] Scenario Runner Start (CorePool:{})", scenarioName, threadSize);
            scenario.setExecutorService(scheduledExecutorService);

            // Load RMQ
            rmqManager = new RmqManager(scenario);
            boolean rmqResult = rmqManager.start();
            if (!rmqResult) {
                log.error("ScenarioRunner Stop. Fail to RmqManager.start");
                return null;
            }
            scenario.setRmqManager(rmqManager);

            // SessionManager
            SessionManager sessionManager = new SessionManager(scenario);
            scenario.setSessionManager(sessionManager);

            // Loop Message
            LoopMsgHandler loopMsgHandler = new LoopMsgHandler(scenario);
            loopMsgHandler.start();

            // Scenario Run
            // Loop 메시지 먼저 처리 후 시나리오 시작
            sessionManager.createSessionByRate();

            // Remove Ended Session
            sessionManager.removeEndedSession();

            // Screen


            // Check if the scenario is over and clean up resources
            // when the scenario is over
            while (!scenario.isTestEnded()) {
                if (cmdInfo.getMaxCall() > 0
                        && sessionManager.getTotalSessionCnt() >= cmdInfo.getMaxCall()
                        && sessionManager.isSessionEmpty()) {
                    stop("Scenario Ended"); //
                }/* else if (isShutdown && sipCallList.isEmpty()) {
                    stop(); // "Shut down gracefully"
                }*/ else {
                    SleepUtil.trySleep(500);
                }
            }

        } catch (Exception e) {
            log.error("ScenarioRunner.run.Exception ", e);
        }
        return null;

    }

    // todo 테스트 종료 조건 추가
    public synchronized void stop(String reason) {
        if (scenario == null || scenario.isTestEnded()) return;
        scenario.setTestEnded(true);

        if (rmqManager != null) rmqManager.stop();

        if (this.scheduledExecutorService != null) {
            List<Runnable> interruptedTask = this.scheduledExecutorService.shutdownNow();
            if (!interruptedTask.isEmpty())
                log.warn("Main ExecutorService was Terminated, RemainedTask: {}", interruptedTask.size());
        }

        log.info("Stop Scenario Runner ({})", reason);

    }

    // shutdownGracefully

    // handleCommand

}

package com.uangel;

import com.uangel.command.CommandInfo;
import com.uangel.command.CommandLineManager;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.model.SessionManager;
import com.uangel.reflection.JarReflection;
import com.uangel.rmq.RmqManager;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.scenario.handler.base.KeywordMapper;
import com.uangel.util.SleepUtil;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.List;

/**
 * @author dajin kim
 */
@Slf4j
public class ScenarioRunner {

    private Scenario scenario;
    private String name;

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


            scenario.setCmdInfo(cmdInfo);
            //log.debug("{}", cmdInfo);
            log.debug("{}", scenario.getMsgNameList());

            // Load ProtoBuf Package File
            if (!scenario.initJarReflection(cmdInfo)) {

                return null;
            }

            KeywordMapper keywordMapper = new KeywordMapper();
            scenario.setKeywordMapper(keywordMapper);

            // User Defined Command (ReflectionUtil)
            // todo parse & add UserCmd
            keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
            keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");




            // Statistics


            this.name = scenario.getName() + "@" + hashCode();
            Thread.currentThread().setName(name);

            // Create Thread Pool
            // todo 기본으로 필요한 스레드 최저 개수 체크
            int threadSize = cmdInfo.getThreadSize() <= 0 ?
                    Runtime.getRuntime().availableProcessors() : cmdInfo.getThreadSize();
            this.scheduledExecutorService = new UScheduledExecutorService(threadSize,
                    new BasicThreadFactory.Builder()
                            .namingPattern(this.name + "-%d")
                            .priority(Thread.MAX_PRIORITY)
                            .build());
            log.info("Scenario Runner Start (CorePool:{})", threadSize);
            scenario.setExecutorService(scheduledExecutorService);

            // Load RMQ
            rmqManager = new RmqManager(scenario);
            boolean rmqResult = rmqManager.start();
            if (!rmqResult) {
                log.error("ScenarioRunner Stop. Fail to RmqManager.start");
                return null;
            }
            scenario.setRmqManager(rmqManager);

            // Scenario Run
            SessionManager sessionManager = new SessionManager(scenario);
            scenario.setSessionManager(sessionManager);
            sessionManager.createSessionByRate();

            // Remove Ended Session
            sessionManager.removeEndedSession();

            // Screen


            // Check if the scenario is over and clean up resources
            // when the scenario is over
            while (!scenario.isTestEnded()) {
                if (cmdInfo.getMaxCall() > 0
                        && sessionManager.getSessionCnt() >= cmdInfo.getMaxCall()
                        && sessionManager.isSessionEmpty()) {
                    stop(); // "Scenario Ended"
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
    public synchronized void stop() {
        if (scenario == null || scenario.isTestEnded()) return;
        scenario.setTestEnded(true);

        if (rmqManager != null) rmqManager.stop();

        if (this.scheduledExecutorService != null) {
            List<Runnable> interruptedTask = this.scheduledExecutorService.shutdownNow();
            if (!interruptedTask.isEmpty())
                log.warn("Main ExecutorService was Terminated, RemainedTask: {}", interruptedTask.size());
        }

        log.debug("Stop Scenario Runner ");

    }

    // shutdownGracefully

    // handleCommand

}

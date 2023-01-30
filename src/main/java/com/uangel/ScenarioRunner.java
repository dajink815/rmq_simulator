package com.uangel;

import com.uangel.command.CommandInfo;
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.List;

/**
 * @author dajin kim
 */
@Slf4j
public class ScenarioRunner {

    private CommandInfo cmdInfo;
    private Scenario scenario;
    private String name;

    private SessionManager sessionManager;
    private UScheduledExecutorService scheduledExecutorService;

    public RmqManager rmqManager;

    //private boolean isShutdown = false;


    public ScenarioRunner() {
        // nothing
    }

    public String run(String[] args) {

        // Parse Command Line -> todo 별도 모듈로 분리
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(CommandInfo.createOptions(), args);
            this.cmdInfo = new CommandInfo(cmd);
            if (cmd.hasOption("h") || (cmdInfo.getScenarioFile() == null)) {
                // todo cmdLineSyntax 수정
                new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", CommandInfo.createOptions());
                return null;
            }
            // mode, proto jar file

            // reflection exec 실행 테스트

            // rmq pw

        } catch (Exception e) {
            log.error("[{}] ScenarioRunner.run.CommandLine.Exception ", this.name, e);
            new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", CommandInfo.createOptions());
            return null;
        }

        try {
            // Scenario Setting
            scenario = ScenarioBuilder.fromXMLFileName(cmdInfo.getScenarioFile());
            if (scenario == null) {
                log.error("ScenarioRunner Scenario Build Fail");
                return null;
            }
            scenario.setCmdInfo(cmdInfo);
            //log.debug("{}", cmdInfo);
            log.debug("{}", scenario.getMsgNameList());

            if (scenario.isProtoType()) {
                JarReflection jarReflection = new JarReflection(cmdInfo.getProtoFile());
                // Load Proto jar file & Check Proto base package name
                if (!jarReflection.loadJarFile() || StringUtil.isNull(cmdInfo.getProtoPkg())) {
                    log.error("Check Proto jar file, base package path ({}, {})", cmdInfo.getProtoFile(), cmdInfo.getProtoPkg());
                    return null;
                }
                scenario.setJarReflection(jarReflection);
            }

            // User Defined Command (Reflection)
            KeywordMapper keywordMapper = new KeywordMapper();
            scenario.setKeywordMapper(keywordMapper);

            // addUserCmd
            keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
            keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");

            // Scenario Validity



            // Statistics


            this.name = scenario.getName() + "@" + hashCode();
            Thread.currentThread().setName(name);

            // Create Thread Pool
            int threadSize = this.cmdInfo.getThreadSize() <= 0 ?
                    Runtime.getRuntime().availableProcessors() : this.cmdInfo.getThreadSize();
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
                log.error("ScenarioRunner Stop. Fail to RmqManager.start()");
                return null;
            }
            scenario.setRmqManager(rmqManager);

            // Scenario Run
            sessionManager = new SessionManager(scenario);
            scenario.setSessionManager(sessionManager);
            sessionManager.createSessionByRate();

            // Remove Ended Session
            sessionManager.removeEndedSession();

            // Screen


            // Check if the scenario is over and clean up resources
            // when the scenario is over
            while (!scenario.isTestEnded()) {
                if (this.cmdInfo.getMaxCall() > 0
                        && sessionManager.getSessionCnt() >= this.cmdInfo.getMaxCall()
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

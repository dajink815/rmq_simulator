package com.uangel;

import com.uangel.command.CommandInfo;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

/**
 * @author dajin kim
 */
@Slf4j
public class ScenarioRunner {

    private CommandInfo cmdInfo;
    private String name;
    private Scenario scenario;

    private UScheduledExecutorService scheduledExecutorService;



    public ScenarioRunner() {
        // nothing
    }

    public void run(String[] args) {

        // Parse Command Line
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(CommandInfo.createOptions(), args);
            this.cmdInfo = new CommandInfo(cmd);
            if (cmd.hasOption("h") || (cmdInfo.getScenarioFile() == null)) {
                // todo
                new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", CommandInfo.createOptions());
                return;
            }
            // mode, proto jar file

        } catch (Exception e) {
            log.warn("[{}] Err occurs", this.name, e);
            new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", CommandInfo.createOptions());
            return;
        }

        try {
            // Scenario Setting
            this.scenario = ScenarioBuilder.fromXMLFilename(cmdInfo.getScenarioFile());
            // Scenario Validity


            log.debug("{}", cmdInfo);
            log.debug("{}", scenario);

            // Statistics


            this.name = scenario.getName() + "@" + hashCode();
            Thread.currentThread().setName(name);

            // Thread Pool
            int threadSize = this.cmdInfo.getThreadSize() <= 0 ?
                    Runtime.getRuntime().availableProcessors() : this.cmdInfo.getThreadSize();
            this.scheduledExecutorService = new UScheduledExecutorService(threadSize,
                    new BasicThreadFactory.Builder()
                            .namingPattern(this.name + "-%d")
                            //.daemon(true)
                            .priority(Thread.MAX_PRIORITY)
                            .build());
            log.info("Scenario Runner Start (CorePool:{})", threadSize);

            // Load RMQ



            // Scenario Run



            // Remove Ended Call



        } catch (Exception e) {
            log.error("ScenarioRunner.run.Exception ", e);
        }

    }
}

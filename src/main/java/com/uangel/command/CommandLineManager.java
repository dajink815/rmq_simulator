package com.uangel.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;

/**
 * @author dajin kim
 */
@Slf4j
public class CommandLineManager {

    private CommandLineManager() {
        // nothing
    }

    public static CommandInfo parseCommandLine(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandInfo cmdInfo;
        try {
            CommandLine cmd = parser.parse(CommandInfo.createOptions(), args);
            cmdInfo = new CommandInfo(cmd);
            if (cmd.hasOption("h") || (cmdInfo.getScenarioFile() == null)) {
                new HelpFormatter().printHelp("urmqgen.jar [OPTIONS] (see -h options)", CommandInfo.createOptions());
                return null;
            }
            // todo CommandLine 기능 추가

            // mode, proto jar file

            // reflection exec 실행 테스트

            // rmq pw

        } catch (Exception e) {
            log.error("ScenarioRunner.run.CommandLine.Exception ",  e);
            new HelpFormatter().printHelp("urmqgen.jar [OPTIONS] (see -h options)", CommandInfo.createOptions());
            return null;
        }
        return cmdInfo;
    }
}

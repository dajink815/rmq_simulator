package com.uangel;

import com.uangel.ScenarioRunner;
import org.junit.Test;

/**
 * @author dajin kim
 */
public class TestScenarioRunner {

    @Test
    public void testScenarioRunner() {
        String localIp = "127.0.0.1";
        String[] args = {"-sf", "./src/main/resources/scenario/mrfc_basic.xml",
                "-t", "proto", "-pf", "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar",
                "-rl", "local_queue", "-rh", localIp,
                "-rp", "5672", "-m", "1"};

        new ScenarioRunner().run(args);
    }

    @Test
    public void testHelpCommandInfo() {
        String[] args = {"-h"};
        new ScenarioRunner().run(args);
    }
}

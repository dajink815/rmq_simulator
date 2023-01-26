package com.uangel;

import com.uangel.executor.UScheduledExecutorService;
import com.uangel.scenario.handler.base.KeywordMapper;
import com.uangel.util.SleepUtil;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author dajin kim
 */
public class TestScenarioRunner {

    private final List<String> uac = new ArrayList<>();
    private final List<String> uas = new ArrayList<>();

    @Test
    public void testHelpCommandInfo() {
        String[] args = {"-h"};
        new ScenarioRunner().run(args);
    }

    @Test
    public void testMRFExternalBasicFlow() {
        String uacQueue = "T_MRFC";
        String uasQueue = "T_MRFP";
        String host = "192.168.7.34";
        String user = "acs";
        String port = "5672";
        String pass = "/0Un3ig1ynr9ZHdEPM/22w==";

        // Uac
        addUacArgs("sf", "./src/main/resources/scenario/mrfc_basic.xml");
        addUacArgs("rl", uacQueue);
        addUacArgs("rt", uasQueue);
        // Uas
        addUasArgs("sf", "./src/main/resources/scenario/mrfp_basic.xml");
        addUasArgs("rl", uasQueue);
        addUasArgs("rt", uacQueue);
        // Common
        addCommonArgs("k", "dialogId");
        addCommonArgs("t", "proto");
        addCommonArgs("pf", "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar");
        addCommonArgs("pkg", "com.uangel.protobuf.mrfp.external");
        addCommonArgs("rh", host);
        addCommonArgs("ru", user);
        addCommonArgs("rp", port);
        addCommonArgs("rpw", pass);
        addCommonArgs("rth", host);
        addCommonArgs("rtu", user);
        addCommonArgs("rtp", port);
        addCommonArgs("rtpw", pass);
        addCommonArgs("rts", "2");
        addCommonArgs("rqs", "5");
        addCommonArgs("ts", "5");
        addCommonArgs( "m", "1");

        //CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUasArgs()));
        //CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUacArgs()));

        //System.out.println(f);
        //System.out.println(f2);

        //CompletableFuture<String> f = new CompletableFuture<>();

        UScheduledExecutorService UasExecutor = new UScheduledExecutorService(1,
                new BasicThreadFactory.Builder()
                        .namingPattern("UasExecutor" + "-%d")
                        .priority(Thread.MAX_PRIORITY)
                        .build());
        UScheduledExecutorService UacExecutor = new UScheduledExecutorService(1,
                new BasicThreadFactory.Builder()
                        .namingPattern("UacExecutor" + "-%d")
                        .priority(Thread.MAX_PRIORITY)
                        .build());
        CompletableFuture<String> f = (CompletableFuture<String>) UasExecutor.submit(() -> new ScenarioRunner().run(getUasArgs()));
        SleepUtil.trySleep(500);
        CompletableFuture<String> f2 = (CompletableFuture<String>) UacExecutor.submit(() -> new ScenarioRunner().run(getUacArgs()));

        SleepUtil.trySleep(30000);
    }

    public void addUacArgs(String option, String value) {
        if (!option.startsWith("-"))
            option = "-" + option;
        uac.add(option);
        uac.add(value);
    }

    public String[] getUacArgs() {
        return  uac.toArray(new String[0]);
    }

    public void addUasArgs(String option, String value) {
        if (!option.startsWith("-"))
            option = "-" + option;
        uas.add(option);
        uas.add(value);
    }

    public String[] getUasArgs() {
        return  uas.toArray(new String[0]);
    }

    public void addCommonArgs(String option, String value) {
        addUacArgs(option, value);
        addUasArgs(option, value);
    }

    public void printArr(String[] arr) {
        if ((arr.length % 2) != 0) return;
        for (int i = 0; i < arr.length; i+=2) {
            System.out.println("Option : " + arr[i] + " , " + arr[i+1]);
        }
    }
}

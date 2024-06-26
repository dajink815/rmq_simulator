package com.uangel;

import com.uangel.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author dajin kim
 */
@Slf4j
public class TestScenarioRunner {

    private static final String MRFC_BASIC = "mrfc_basic.xml";
    private static final String MRFP_BASIC = "mrfp_basic.xml";
    private static final String MRFC_HB = "mrfc_basic_hb.xml";
    private static final String MRFP_HB = "mrfp_basic_hb.xml";
    private final List<String> uac = new ArrayList<>();
    private final List<String> uas = new ArrayList<>();

    @Test
    public void testHelpCommandInfo() {
        String[] args = {"-h"};
        new ScenarioRunner().run(args);
    }

    @Test
    public void testExternalBasicFlow() throws ExecutionException, InterruptedException {
        prepareMrfOptions(MRFC_BASIC, MRFP_BASIC, 1);
        // MRFP
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUasArgs()));
        SleepUtil.trySleep(1000);
        // MRFC
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUacArgs()));

        log.debug("f : [{}]", f.get());
        log.debug("f2 : [{}]", f2.get());
    }

    @Test
    public void testMultipleExternalBasicFlow() throws ExecutionException, InterruptedException {
        prepareMrfOptions(MRFC_BASIC, MRFP_BASIC, 5);
        // MRFP
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUasArgs()));
        SleepUtil.trySleep(1000);
        // MRFC
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUacArgs()));

        log.debug("f : [{}]", f.get());
        log.debug("f2 : [{}]", f2.get());
    }

    @Test
    public void testExternalHBFlow() throws ExecutionException, InterruptedException {
        prepareMrfOptions(MRFC_HB, MRFP_HB, 1);
        // MRFP
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUasArgs()));
        SleepUtil.trySleep(1000);
        // MRFC
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUacArgs()));

        log.debug("f : [{}]", f.get());
        log.debug("f2 : [{}]", f2.get());
    }

    @Test
    public void pbxInOnly() throws ExecutionException, InterruptedException {
        preparePbxOptions("a2s_inonly.xml", "awf_inonly.xml", 1);
        // AWF
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUasArgs()));
        SleepUtil.trySleep(1000);

        // A2S
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUacArgs()));

        log.debug("f : [{}]", f.get());
        log.debug("f2 : [{}]", f2.get());
    }

    public String[] getUacArgs() {
        return  uac.toArray(new String[0]);
    }
    public String[] getUasArgs() {
        return  uas.toArray(new String[0]);
    }

    public void prepareMrfOptions(String uacFile, String uasFile, int callNum) {
        String uacQueue = "T_MRFC";
        String uasQueue = "T_MRFP";
        String host = "192.168.7.34";
        String user = "acs";
        String port = "5672";
        String pass = "/0Un3ig1ynr9ZHdEPM/22w==";

        // Uac
        addUacArgs("sf", "./src/test/resources/scenario/mrf/" + uacFile);
        addUacArgs("rl", uacQueue);
        addUacArgs("rt", uasQueue);
        // Uas
        addUasArgs("sf", "./src/test/resources/scenario/mrf/" + uasFile);
        addUasArgs("rl", uasQueue);
        addUasArgs("rt", uacQueue);
        // Common
        addCommonArgs("test_mode", "true");
        addCommonArgs("k", "dialogId");
        addCommonArgs("t", "proto");
        addCommonArgs("pf", "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar");
        addCommonArgs("pkg", "com.uangel.protobuf.mrfp.external");
        addCommonArgs("user_cmd", "./src/test/resources/json/user_cmd.json");
        addCommonArgs("rh", host);
        addCommonArgs("ru", user);
        addCommonArgs("rp", port);
        addCommonArgs("rpw", pass);
        addCommonArgs("rth", host);
        addCommonArgs("rtu", user);
        addCommonArgs("rtp", port);
        addCommonArgs("rtpw", pass);
        addCommonArgs("rqs", "5");
        addCommonArgs("ts", "5");
        addCommonArgs( "m", Integer.toString(callNum));
    }

    public void preparePbxOptions(String uacFile, String uasFile, int callNum) {
        String uacQueue = "T_A2S";
        String uasQueue = "T_AWF";
        String host = "192.168.7.34";
        String user = "acs";
        String port = "5672";
        String pass = "/0Un3ig1ynr9ZHdEPM/22w==";

        // Uac
        addUacArgs("sf", "./src/test/resources/scenario/pbx/" + uacFile);
        addUacArgs("rl", uacQueue);
        addUacArgs("rt", uasQueue);
        // Uas
        addUasArgs("sf", "./src/test/resources/scenario/pbx/" + uasFile);
        addUasArgs("rl", uasQueue);
        addUasArgs("rt", uacQueue);
        // Common
        //addCommonArgs("long_session", "5");       // LongSession Test
        addCommonArgs("test_mode", "true");
        addCommonArgs("k", "callId");
        addCommonArgs("t", "json");
        addCommonArgs("rh", host);
        addCommonArgs("ru", user);
        addCommonArgs("rp", port);
        addCommonArgs("rpw", pass);
        addCommonArgs("rth", host);
        addCommonArgs("rtu", user);
        addCommonArgs("rtp", port);
        addCommonArgs("rtpw", pass);
        addCommonArgs("rqs", "5");
        addCommonArgs("ts", "5");
        addCommonArgs( "m", Integer.toString(callNum));
    }

    public void addCommonArgs(String option, String value) {
        addUacArgs(option, value);
        addUasArgs(option, value);
    }

    public void addUacArgs(String option, String value) {
        if (!option.startsWith("-"))
            option = "-" + option;
        uac.add(option);
        uac.add(value);
    }

    public void addUasArgs(String option, String value) {
        if (!option.startsWith("-"))
            option = "-" + option;
        uas.add(option);
        uas.add(value);
    }

    public void printArr(String[] arr) {
        if ((arr.length % 2) != 0) return;
        for (int i = 0; i < arr.length; i+=2) {
            System.out.println("Option : " + arr[i] + " , " + arr[i+1]);
        }
    }
}

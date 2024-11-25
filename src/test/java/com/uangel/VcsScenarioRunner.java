package com.uangel;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class VcsScenarioRunner {
    private static final String AMF_BASIC = "amf_vcif.xml";

    private final List<String> uac = new ArrayList<>();
    private final List<String> uas = new ArrayList<>();


    @Test
    public void testExternalBasicFlow() throws ExecutionException, InterruptedException {
        prepareVcsOptions(AMF_BASIC, 1);
        // AMF
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> new ScenarioRunner().run(getUacArgs()));

        log.debug("f : [{}]", f.get());
    }


    public void prepareVcsOptions(String uacFile, int callNum) {
        String uacQueue = "T_A2S";
        String uasQueue = "T_AWF";
        String host = "192.168.7.34";
        String user = "acs";
        String port = "5672";
        String pass = "/0Un3ig1ynr9ZHdEPM/22w==";

        // Uac
        addUacArgs("sf", "./src/test/resources/scenario/vcs/" + uacFile);
        addUacArgs("rl", uacQueue);
        addUacArgs("rt", uasQueue);
        // Uas
        //addUasArgs("sf", "./src/test/resources/scenario/vcs/" + uasFile);
        addUasArgs("rl", uasQueue);
        addUasArgs("rt", uacQueue);
        // Common
        //addCommonArgs("long_session", "5");       // LongSession Test
        addCommonArgs("test_mode", "true");
        addCommonArgs("k", "callId");
        addCommonArgs("t", "proto");
        addCommonArgs("pf", "./src/main/resources/proto/vcs-proto-msg-0.5.0.JDK11.jar");
        addCommonArgs("pkg", "com.uangel.vcs");
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

    public String[] getUacArgs() {
        return  uac.toArray(new String[0]);
    }
    public String[] getUasArgs() {
        return  uas.toArray(new String[0]);
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
}

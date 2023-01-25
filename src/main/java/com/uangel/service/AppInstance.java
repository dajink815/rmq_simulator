package com.uangel.service;

import com.uangel.command.CommandInfo;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.model.SimType;
import com.uangel.reflection.JarReflection;
import com.uangel.scenario.Scenario;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.BlockingQueue;

/**
 * @author dajin kim
 */
@Getter
@Setter
public class AppInstance {
    private static AppInstance instance = null;

    // Config
    private CommandInfo cmdInfo;
    // Scenario
    private Scenario scenario;
    private boolean isTestEnded;

    // Thread
    private UScheduledExecutorService executorService;
    // RMQ
    private BlockingQueue<byte[]> rmqQueue;

    private JarReflection jarReflection;

    private AppInstance() {
        // nothing
    }

    public static AppInstance getInstance() {
        if (instance == null) {
            instance = new AppInstance();
        }

        return instance;
    }

    public boolean isProtoType() {
        if (cmdInfo != null)
            return SimType.PROTO.equals(cmdInfo.getType());
        return false;
    }

    public int getScenarioSize() {
        return this.scenario.phases().size();
    }

}

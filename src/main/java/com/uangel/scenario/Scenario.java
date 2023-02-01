package com.uangel.scenario;

import com.uangel.command.CommandInfo;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.model.MsgInfoManager;
import com.uangel.model.SessionManager;
import com.uangel.model.SimType;
import com.uangel.reflection.JarReflection;
import com.uangel.rmq.RmqManager;
import com.uangel.scenario.handler.base.KeywordMapper;
import com.uangel.scenario.phases.MsgPhase;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dajin kim
 */
@Slf4j
@Getter
@Setter
public class Scenario extends MsgInfoManager {

    private final String name;
    private final List<MsgPhase> phases;
    private CommandInfo cmdInfo;
    private SessionManager sessionManager;
    private RmqManager rmqManager;
    private UScheduledExecutorService executorService;
    private JarReflection jarReflection;
    private KeywordMapper keywordMapper;
    private boolean isTestEnded;

    public Scenario(String name, List<MsgPhase> phases) {
        this.name = name;
        this.phases = phases;
        super.initList(phases);
    }

    public String getName() {
    public List<MsgPhase> phases() {
        return Collections.unmodifiableList(this.phases);
    }

    public MsgPhase getPhase(int idx) {
        return this.phases.get(idx);
    }

    public int getScenarioSize() {
        return phases.size();
    }

    public String getName() {
        return this.name;
    }

    public boolean isProtoType() {
        if (cmdInfo != null)
            return SimType.PROTO.equals(cmdInfo.getType());
        return false;
    }

    public void schedule(Runnable command, long delay) {
        this.executorService.schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    public boolean initJarReflection(CommandInfo cmdInfo) {
        if (isProtoType()) {
            JarReflection jarRef = new JarReflection(cmdInfo.getProtoFile());
            // Load Proto jar file & Check Proto base package name
            if (!jarRef.loadJarFile() || StringUtil.isNull(cmdInfo.getProtoPkg())) {
                log.error("Check Proto jar file, base package path ({}, {})", cmdInfo.getProtoFile(), cmdInfo.getProtoPkg());
                return false;
            }
            setJarReflection(jarRef);
        }
        return true;
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "name='" + name + '\'' +
                ", phases=" + phases +
                '}';
    }
}

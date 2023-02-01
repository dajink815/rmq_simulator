package com.uangel.scenario;

import com.uangel.command.CommandInfo;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.model.MsgInfoManager;
import com.uangel.model.SessionManager;
import com.uangel.model.SimType;
import com.uangel.reflection.JarReflection;
import com.uangel.rmq.RmqManager;
import com.uangel.scenario.handler.base.KeywordMapper;
import com.uangel.scenario.phases.LoopPhase;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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
    private List<LoopPhase> loopPhases;
    private final Map<String, String> fields = new HashMap<>();
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
        return this.name;
    }

    public List<MsgPhase> phases() {
        return Collections.unmodifiableList(this.phases);
    }
    public MsgPhase getPhase(int idx) {
        return this.phases.get(idx);
    }
    public int getScenarioSize() {
        return phases.size();
    }

    public LoopPhase getLoopPhase(int idx) {
        if (loopPhases == null) return null;
        return this.loopPhases.get(idx);
    }

    public void addFields(Map<String, String> addFields) {
        fields.putAll(addFields);
    }
    public void addField(String fieldName, String value) {
        fields.put(fieldName, value);
    }
    public String getFieldValue(String key) {
        return fields.get(key);
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
        int phaseSize = (phases != null)? phases.size() : 0;
        int loopPhaseSize = (loopPhases != null)? loopPhases.size() : 0;

        return "Scenario{" +
                "name='" + name + '\'' +
                ", phases(" + phaseSize + ")=" + phases +
                ", loopPhases(" + loopPhaseSize + ")=" + loopPhases +
                '}';
    }
}

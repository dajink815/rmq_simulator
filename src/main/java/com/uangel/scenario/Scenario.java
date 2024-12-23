package com.uangel.scenario;

import com.uangel.ScenarioRunner;
import com.uangel.command.CommandInfo;
import com.uangel.executor.UScheduledExecutorService;
import com.uangel.model.MsgInfoManager;
import com.uangel.model.SessionManager;
import com.uangel.model.SimType;
import com.uangel.reflection.JarReflection;
import com.uangel.rmq.RmqManager;
import com.uangel.scenario.handler.base.KeywordMapper;
import com.uangel.scenario.phases.*;
import com.uangel.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author dajin kim
 */
@Slf4j
@Data
public class Scenario extends MsgInfoManager {

    private final String name;
    private final List<MsgPhase> phases;
    private final int firstRcvIdx;
    private final int firstSendIdx;
    private List<LoopPhase> loopPhases;
    private Map<String, LabelPhase> labelPhaseMap;
    private final Map<String, String> fields = new HashMap<>();
    private CommandInfo cmdInfo;
    private SessionManager sessionManager;
    private RmqManager rmqManager;
    private UScheduledExecutorService executorService;
    private JarReflection jarReflection;
    private KeywordMapper keywordMapper;
    private boolean isTestEnded;
    private ScenarioRunner scenarioRunner;
    // 시나리오 Outbound 타입 여부
    private boolean outScenario;

    public Scenario(String name, List<MsgPhase> phases) {
        this.name = name;
        this.phases = phases;
        super.initList(phases);
        this.firstRcvIdx = getFirstRecvPhaseIdx();
        this.firstSendIdx = getFirstSendPhaseIdx();
        this.outScenario = determineScenarioType(phases);

        log.info("[{}] Started Scenario (size:{}, isOutbound:{}, firstRcv:{}, firstSend:{})",
                name, phases.size(), outScenario, firstRcvIdx, firstSendIdx);
        RecvPhase firstRcvPhase = (RecvPhase) phases.get(firstRcvIdx);

        log.info("[{}]", firstRcvPhase.getMsgName());
    }

    private boolean determineScenarioType(List<MsgPhase> phases) {
        return phases.stream()
                .filter(phase -> {
                    if (phase instanceof RecvPhase recvPhase) {
                        return !recvPhase.getOptional();
                    }
                    return true;
                })
                .findFirst()
                .map(SendPhase.class::isInstance)
                .orElse(false);
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

    public SendPhase getFirstSendPhase() {
        for (MsgPhase msgPhase : phases) {
            if (msgPhase instanceof SendPhase s) {
                return s;
            }
        }
        return null;
    }

    public int getFirstSendPhaseIdx() {
        int index = 0;
        for (MsgPhase msgPhase : phases) {
            if (msgPhase instanceof SendPhase) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public int getFirstRecvPhaseIdx() {
        int index = 0;
        for (MsgPhase msgPhase : phases) {
            if (msgPhase instanceof RecvPhase r && !r.getOptional()) {
                return index;
            }
            index++;
        }
        return -1;
    }


    public LoopPhase getLoopPhase(int idx) {
        if (loopPhases == null || loopPhases.isEmpty()) return null;
        return this.loopPhases.get(idx);
    }

    public LabelPhase getLabelPhase(String id) {
        return labelPhaseMap.get(id);
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
        int loopPhaseSize = loopPhases.size();
        int labelPhaseSize = labelPhaseMap.size();

        return "Scenario{" +
                "NAME='" + name + "'\n" +
                ", PHASES(" + phaseSize + ")=" + phases + "\n" +
                ", LOOP(" + loopPhaseSize + ")=" + loopPhases + "\n" +
                ", LABEL(" + labelPhaseSize + ")=" + labelPhaseMap + "\n" +
                '}';
    }

    public boolean checkFirstMsg(String json) {
        String msgName = getFirstRcvPhaseName();
        return checkMsgType(json, msgName);
    }

    public String getFirstRcvPhaseName() {
        if (this.firstRcvIdx < 0) return null;
        RecvPhase firstRcvPhase = (RecvPhase) phases.get(firstRcvIdx);
        return firstRcvPhase.getMsgName();
    }

    // todo 모든 수신한 메시지 메시지 타입 체크
    public boolean checkMsgType(String json, String msgName) {
        if (msgName == null || msgName.isEmpty()) return false;

        String msgCamel = StringUtil.snakeToCamel(msgName).toUpperCase();
        String msgSnake = StringUtil.camelToSnake(msgName).toUpperCase();
        if (StringUtil.isNull(msgCamel) && StringUtil.isNull(msgSnake)) return false;

        log.debug("msgName [{}] msgCamel [{}] msgSnake [{}]", msgName, msgCamel, msgSnake);

        json = json.toUpperCase();
        msgName = msgName.toUpperCase();
        return json.contains(msgName) || json.contains(msgCamel) || json.contains(msgSnake);
    }
}

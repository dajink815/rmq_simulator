package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.reflection.JarReflection;
import com.uangel.reflection.ReflectionUtil;
import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.HeaderBodyInfo;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.FieldType;
import com.uangel.service.AppInstance;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author dajin kim
 */
@Slf4j
public class ProtoMsgBuilder implements MsgBuilder {

    private final AppInstance instance = AppInstance.getInstance();
    private final JarReflection jarReflection = instance.getJarReflection();
    private final SessionInfo sessionInfo;

    public ProtoMsgBuilder(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    @Override
    public byte[] build(SendPhase sendPhase) {
        try {
            String pkgBase = instance.getCmdInfo().getProtoPkg();
            if (!pkgBase.endsWith(".")) {
                pkgBase += ".";
            }

            // get Builder
            String msgClassName = pkgBase + sendPhase.getClassName();
            Object msgBuilder = jarReflection.getNewBuilder(msgClassName);

            // set subMessages
            for (HeaderBodyInfo msgInfo : sendPhase.getHeaderBodyInfos()) {
                Object subMsgObj = getSubMessage(msgInfo, pkgBase);
                if (subMsgObj == null) continue;
                msgBuilder = jarReflection.invokeObjMethod(jarReflection.getMethodName(msgInfo.getClassName()), msgBuilder, subMsgObj);
            }

            // Build Message
            Object msgResult = jarReflection.build(msgBuilder);
            log.debug("Build SendMsg \r\n[{}]", msgResult);

            return jarReflection.toByteArray(msgResult);
        } catch (Exception e) {
            log.error("ProtoMsgBuilder.build.Exception ", e);
        }
        //return null;
        return new byte[0];
    }


    public Object getSubMessage(HeaderBodyInfo msgInfo, String pkgBase) {

        try {
            Object builder = jarReflection.getNewBuilder(pkgBase + msgInfo.getClassName());

            List<FieldInfo> fieldInfos = msgInfo.getFieldInfos();

            for (FieldInfo fieldInfo : fieldInfos) {
                String methodName = jarReflection.getMethodName(fieldInfo.getName());
                FieldType type = fieldInfo.getType();
                String value = fieldInfo.getValue();

                // 값 체크

                // Exec
                if (StringUtil.isNull(value)) {
                    String exec = fieldInfo.getExec();
                    value = ReflectionUtil.getExecResult(exec);
                    if (value == null) continue;
                }

                // Keyword
                KeywordMapper keywordMapper = KeywordMapper.getInstance();
                value = keywordMapper.replaceKeyword(sessionInfo, value);

                // Type 별 세팅
                if (FieldType.STR.equals(type)) {
                    builder = jarReflection.invokeObjMethod(methodName, builder, value);
                } else if (FieldType.INT.equals(type)) {
                    builder = jarReflection.invokeIntMethod(methodName, builder, Integer.parseInt(value));
                } else if (FieldType.LONG.equals(type)) {
                    builder = jarReflection.invokeLongMethod(methodName, builder, Long.parseLong(value));
                } else if (FieldType.BOOL.equals(type)) {
                    builder = jarReflection.invokeBoolMethod(methodName, builder, Boolean.parseBoolean(value));
                }

            }

            return jarReflection.build(builder);

        } catch (Exception e) {
            log.error("ProtoMsgBuilder.getSubMessage.Exception ", e);
        }

        return null;
    }
}

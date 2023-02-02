package com.uangel.scenario.model;

import lombok.Getter;

import java.util.List;

/**
 * send, recv 하위 노드인 header, body 정보 포함하는 객체
 *
 * @author dajin kim
 */
@Getter
public class HeaderBodyInfo {
    private final String className;
    private final List<FieldInfo> fieldInfos;

    public HeaderBodyInfo(String className, List<FieldInfo> fieldInfos) {
        this.className = className;
        this.fieldInfos = fieldInfos;
    }

    @Override
    public String toString() {
        return "{" + className +
                ", fields=" + fieldInfos +
                '}';
    }
}

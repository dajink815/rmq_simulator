package com.uangel.scenario.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * send, recv 하위 노드인 header, body 정보 포함하는 객체
 *
 * @author dajin kim
 */
@Getter
@Setter
@ToString
public class HeaderBodyInfo {
    private String className;
    private List<FieldInfo> fieldInfos;

}

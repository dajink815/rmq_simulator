package com.uangel.scenario.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author dajin kim
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class FieldInfo {
    private String name;
    private String type;
    private String value;
    private String exec;
}

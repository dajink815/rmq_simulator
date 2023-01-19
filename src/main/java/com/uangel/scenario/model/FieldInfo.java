package com.uangel.scenario.module;

import com.uangel.model.FieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author dajin kim
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class FieldInfo {
    private String name;
    private FieldType type;
    private String value;
    private String exec;
}

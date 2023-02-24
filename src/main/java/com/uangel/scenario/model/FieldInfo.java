package com.uangel.scenario.model;

import com.uangel.scenario.type.FieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author dajin kim
 */
@Getter
@Setter
@AllArgsConstructor
public class FieldInfo {
    private String name;
    private FieldType type;
    private String value;
    private String exec;

    @Override
    public String toString() {
        if (exec == null)
            return name + "(" + type + "):" + value;

        return name + "(" + type + "):" + value +
                ", exec=" + exec;
    }
}

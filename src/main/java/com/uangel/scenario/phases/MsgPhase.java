package com.uangel.scenario.phases;

import com.uangel.scenario.XmlParser;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import java.util.regex.Pattern;

/**
 * @author dajin kim
 */
@ToString
@Getter
@Slf4j
public class MsgPhase extends XmlParser {
    public static final Pattern initialSpaces = Pattern.compile("^\\s*");

    private final int idx;

    protected MsgPhase(Node xmlNode, int idx) {
        this(xmlNode, idx, false);
    }
    protected MsgPhase(Node xmlNode, int idx, boolean isProtoMode) {
        super(xmlNode, isProtoMode);
        this.idx = idx;
    }
}

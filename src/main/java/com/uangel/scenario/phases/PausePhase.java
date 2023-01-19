package com.uangel.scenario.phases;

import com.uangel.scenario.type.AttrName;
import com.uangel.util.XmlUtil;
import lombok.ToString;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@ToString
public class PausePhase extends MsgPhase {

    private final Integer milliSeconds;

    public PausePhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        NamedNodeMap attr = xmlNode.getAttributes();

        this.milliSeconds = XmlUtil.getIntParam(attr.getNamedItem(AttrName.MILLIS.getValue()));
    }
}

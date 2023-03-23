package com.uangel.scenario.phases.actions;

import com.uangel.util.StringUtil;
import com.uangel.util.XmlUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author dajin kim
 */
@Getter
@Slf4j
public class ExecNode {
    public final String rtpStream;
    public final String playPcapAudio;

    public ExecNode(Node xmlNode) {
        NamedNodeMap attr = xmlNode.getAttributes();
        this.rtpStream = XmlUtil.getStrParam(attr.getNamedItem("rtp_stream"));
        this.playPcapAudio = XmlUtil.getStrParam(attr.getNamedItem("play_pcap_audio"));
        if (rtpStream != null && playPcapAudio != null) {
            log.warn("Check Exec Node Attribute (rtp_stream:{}, play_pcap_audio:{})", rtpStream, playPcapAudio);
        }
        //System.out.println("ExecNode => RTP:" + rtpStream + ", PCAP:" + playPcapAudio);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (StringUtil.notNull(rtpStream))
            sb.append("RTP=").append(rtpStream);
        if (StringUtil.notNull(playPcapAudio)) {
            if (sb.toString().contains("RTP")) sb.append(" & ");
            sb.append("PCAP=").append(playPcapAudio);
        }
        return sb.toString();
    }
}

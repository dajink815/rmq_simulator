package com.uangel.media.rtp;

import com.uangel.ScenarioRunner;
import com.uangel.media.pcap.EthernetFrame;
import com.uangel.media.pcap.PacketPpi;
import com.uangel.media.pcap.Pcap;
import com.uangel.media.pcap.RtpPacket;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author kangmoo Heo
 */
public class PcapAudioPlayer extends MediaPlayer {
    private static final Logger logger = getLogger(PcapAudioPlayer.class);
    private static final Map<String, List<RtpPacket>> pcapDatas = new ConcurrentHashMap<>();

    private List<RtpPacket> rtpDatas;
    private final AtomicInteger curIdx = new AtomicInteger();

    public PcapAudioPlayer(ScenarioRunner scenarioRunner, String callId, String command) {
        super(scenarioRunner, callId, command);
    }

    @Override
    public synchronized void sendNextPacket() {
        int idx = curIdx.getAndIncrement();
        if (idx >= rtpDatas.size()) {
            int loopCnt = loopCount.getAndDecrement();
            if (loopCnt < 0) {
                // 무한 반복 재생
                loopCount.set(-1);
                this.curIdx.set(0);
            } else if (loopCnt == 0) {
                // 카운트 0일 시 종료
                stop("Media Play Done");
                return;
            } else {
                // 유한 반복
                this.curIdx.set(0);
            }
            return;
        }

        byte[] data = rtpDatas.get(idx).getPayload();
        int payloadType = this.payloadType < 0 ? rtpDatas.get(idx).getPayloadtype() : this.payloadType;
        int seqNoGap = 1;
        int timestampGap = scenarioRunner.getCmdInfo().getMediaTimestampGap();

        if (idx != 0) {
            seqNoGap = rtpDatas.get(idx).getSequencenumber() - rtpDatas.get(idx - 1).getSequencenumber();
            timestampGap = rtpDatas.get(idx).getTimestamp() - rtpDatas.get(idx - 1).getTimestamp();
        }

        RtpPacket rtpPacket = new RtpPacket(payloadType, seqNo.addAndGet(seqNoGap), timeStamp.addAndGet(timestampGap), data, data.length);
        sendPacketByRtp(rtpPacket);

    }

    @Override
    public void play(int fromPort, InetSocketAddress targetAddr) {
        try {
            this.fromPort = fromPort;
            this.targetAddr = targetAddr;
            synchronized (pcapDatas) {
                if (!pcapDatas.containsKey(fileName)) {
                    Pcap pcap = Pcap.fromFile(fileName);
                    pcapDatas.put(fileName, pcap.packets().stream()
                            .map(packet -> {
                                try {
                                    int pos;
                                    byte[] packetData;
                                    if (packet.body() instanceof EthernetFrame || packet.body() instanceof PacketPpi) {
                                        pos = 42;
                                        packetData = packet._raw_body();
                                    } else {
                                        pos = 44;
                                        packetData = (byte[]) packet.body();
                                    }
                                    byte[] rtp = new byte[packetData.length - pos];
                                    System.arraycopy(packetData, pos, rtp, 0, packetData.length - pos);
                                    return new RtpPacket(rtp, rtp.length, packet.tsSec() * 1000 + packet.tsUsec() / 1000);
                                } catch (Exception e) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .filter(rtpPacket -> rtpPacket.getPayload() != null)
                            .filter(rtpPacket -> rtpPacket.getVersion() == 2)
                            .filter(rtpPacket -> rtpPacket.getPayloadSize() == rtpPacket.getPayload().length)
                            .collect(Collectors.toList()));
                }
            }
            rtpDatas = pcapDatas.get(fileName);
            isRunning.set(true);
        } catch (Exception e) {
            logger.warn("({}) File Stream Play Fail", callId, e);
            stop("File Stream Play Fail. " + e.getMessage());
            scenarioRunner.stop("Can not read pcap File. [" + fileName + "]");
        }
    }

    @Override
    public void stop(String reason) {
        nettyChannelManager.deallocPort(fromPort);
        isRunning.set(false);
        logger.debug("({}) () () Media Play Stop. [{}]", callId, reason);
    }
}

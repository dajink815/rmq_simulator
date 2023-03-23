package com.uangel.media.rtp;

import com.uangel.ScenarioRunner;
import com.uangel.media.netty.NettyChannelManager;
import com.uangel.media.pcap.RtpPacket;
import lombok.Getter;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *  Media Player
 * @author kangmoo Heo
 */
@Getter
public abstract class MediaPlayer {
    private static final Logger logger = getLogger(MediaPlayer.class);
    protected static final NettyChannelManager nettyChannelManager = NettyChannelManager.getInstance();
    protected ScenarioRunner scenarioRunner;
    protected String callId;
    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    // Media Info
    protected String fileName;
    protected int payloadType = -1;
    protected AtomicInteger loopCount = new AtomicInteger(1);
    // RTP Data
    protected AtomicInteger seqNo = new AtomicInteger(0);
    protected AtomicInteger timeStamp = new AtomicInteger(0);

    protected int fromPort;
    protected InetSocketAddress targetAddr;

    public MediaPlayer(ScenarioRunner scenarioRunner, String callId, String command) {
        this.scenarioRunner = scenarioRunner;
        this.callId = callId;
        String[] cmd = command.split(",");
        this.fileName = cmd[0];
        try {
            if (cmd.length >= 2) this.loopCount.set(Integer.parseInt(cmd[1].trim()));
            if (cmd.length >= 3) this.payloadType = Integer.parseInt(cmd[2].trim());
            logger.debug("Media Player Setting. File:{}, Loop Count:{}, PayloadType:{}", fileName, loopCount, payloadType);
        } catch (Exception e) {
            logger.warn("Wrong Media Play Command Option. [{}]", cmd);
            scenarioRunner.stop("Wrong Media Play Command Option. [" + cmd + "]");
        }
    }

    protected void sendPacketByRtp(byte[] data) {
        RtpPacket rtpPacket = new RtpPacket(payloadType, seqNo.incrementAndGet(), timeStamp.addAndGet(scenarioRunner.getCmdInfo().getMediaTimestampGap()), data, data.length);
        sendPacketByRtp(rtpPacket);
    }

    protected void sendPacketByRtp(RtpPacket rtpPacket) {
        nettyChannelManager.sendMsg(fromPort, targetAddr, rtpPacket.getpacket());
    }

    public abstract void sendNextPacket();

    public abstract void play(int fromPort, InetSocketAddress targetAddr);

    public void pause() {
        this.isRunning.set(false);
    }

    public void resume() {
        this.isRunning.set(true);
    }

    public abstract void stop(String reason);

    public boolean isRunning() {
        return this.isRunning.get();
    }
}

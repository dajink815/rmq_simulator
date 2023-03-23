package com.uangel.media.rtp;

import com.uangel.ScenarioRunner;
import com.uangel.codec.AlawCodec;
import com.uangel.codec.MulawCodec;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.net.InetSocketAddress;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author kangmoo Heo
 */
public class WavPlayer extends MediaPlayer {
    private static final Logger logger = getLogger(WavPlayer.class);
    private FileInputStream fileInputStream;

    public WavPlayer(ScenarioRunner scenarioRunner, String callId, String command) {
        super(scenarioRunner, callId, command);
        this.payloadType = 8;
    }

    // 현재 음성만 전달 가능
    // TODO wav파일 반복 재생 기능 추가해야함
    @Override
    public void sendNextPacket() {
        try {
            byte[] data = new byte[320];
            if (fileInputStream.read(data) != 320) {

                stop("Media Play Done");
                return;
            }
            byte[] encoded = payloadType == 0 ? MulawCodec.encode(data) : AlawCodec.encode(data);
            sendPacketByRtp(encoded);
        } catch (Exception e) {
            logger.warn("({}) Exception While Media Playing", callId, e);
            stop("Exception While Media Playing. " + e.getMessage());
        }
    }

    @Override
    public void play(int fromPort, InetSocketAddress targetAddr) {
        try {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            this.fromPort = fromPort;
            this.targetAddr = targetAddr;

            // 현재 .wav 파일은 pcmu만 지원
            fileInputStream = new FileInputStream(fileName);
            // wav 파일 헤더 제거
            byte[] header = new byte[44];
            if(fileInputStream.read(header) < 44){
                logger.warn("Can not read wav File");
                return;
            }
            isRunning.set(true);
            // TODO Manager에 추가
        } catch (Exception e) {
            logger.warn("({}) File Stream Play Fail", callId, e);
            stop("File Stream Play Fail. " + e.getMessage());
            scenarioRunner.stop("Can not read wav File. [" + fileName + "]");
        }
    }

    public void stop(String reason) {
        try {
            fileInputStream.close();
        } catch (Exception e) {
            logger.warn("Fail to close Media Player", e);
        }
        nettyChannelManager.deallocPort(fromPort);
        isRunning.set(false);
        logger.debug("({}) () () Media Play Stop. [{}]", callId, reason);
    }
}

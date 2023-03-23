package com.uangel.media.info;

import com.uangel.media.netty.NettyChannelManager;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author kangmoo Heo
 */
@Slf4j
@Data
public class MediaInfo {
    private static final NettyChannelManager nettyChannelManager = NettyChannelManager.getInstance();

    private final String sessionId;
    private final int sessionNum;

    private Channel channel;

    // todo set
    private InetSocketAddress localAudioAddr;
    private InetSocketAddress remoteAudioAddr;

    public MediaInfo(String sessionId, int sessionNum) {
        this.sessionId = sessionId;
        this.sessionNum = sessionNum;
    }

    // todo
    public Channel openRtpChannel(){
        if(this.channel != null) return null;
        this.channel = nettyChannelManager.allocPort();
        return this.channel;
    }

    public void closeRtpChannel(){
        try {
            if (channel == null) return;
            synchronized (channel){
                String[] addr = channel.localAddress().toString().split(":");
                int port = Integer.parseInt(addr[addr.length - 1]);
                nettyChannelManager.deallocPort(port);
            }
        } catch (NumberFormatException e) {
            log.error("CallInfo.closeRtpChannel.Exception ", e);
        }
    }


}

/*
 * Copyright (C) 2021. Uangel Corp. All rights reserved.
 */

package com.uangel.media.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NettyChannelManager {
    private static final Logger logger = LoggerFactory.getLogger(NettyChannelManager.class);

    private final Map<Integer, Channel> serverChannelMap = new ConcurrentHashMap<>();
    private final Queue<Integer> remainingPort = new ConcurrentLinkedQueue<>();
    private NettyRTPServer rtpServer = null;
    private boolean useRtpRange;

    private static final NettyChannelManager INSTANCE = new NettyChannelManager();

    private NettyChannelManager() {
    }

    public static NettyChannelManager getInstance() {
        return INSTANCE;
    }

    public boolean setRtpPortRange(int minRtpPort, int maxRtpPort) {
        if (minRtpPort <= 0 || maxRtpPort <= 0 || maxRtpPort < minRtpPort) return false;

        for (int port = minRtpPort; port <= maxRtpPort; port++) {
            try {
                remainingPort.add(port);
            } catch (Exception e) {
                logger.error("() () () Exception to RTP port resource in Queue", e);
            }
        }
        logger.info("() () () Ready to RTP port resource in Queue. (port range: {} - {})",
                minRtpPort, maxRtpPort);
        useRtpRange = true;
        return true;
    }

    /**
     * 포트 할당
     * @return 포트바인딩된 Channel
     */
    public Channel allocPort(int port) {
        Channel channel = rtpServer.openChannel(port);
        if (channel == null) {
            logger.warn("RTP Port Alloc Fail. {}", port);
            return null;
        }
        String[] addr = channel.localAddress().toString().split(":");
        serverChannelMap.put(Integer.parseInt(addr[addr.length - 1]), channel);
        return channel;
    }

    public Channel allocPort() {
        if (!useRtpRange) return allocPort(0);
        Channel channel = null;
        for (int i = 0; i < remainingPort.size(); i++) {
            Integer port = remainingPort.poll();
            if (port != null) {
                channel = allocPort(port);
                if (channel == null && useRtpRange) remainingPort.add(port);
            }
            if (channel != null) break;
        }
        return channel;
    }

    /**
     * 포트 할당 해제
     * @param port 할당 해제 할 포트
     */
    public void deallocPort(int port) {
        Optional.ofNullable(serverChannelMap.get(port)).ifPresent(channel -> rtpServer.closeChannel(channel));
        serverChannelMap.remove(port);
        if (useRtpRange) remainingPort.add(port);
    }

    public List<Channel> getRtpChannels() {
        synchronized (serverChannelMap) {
            return new ArrayList<>(serverChannelMap.values());
        }
    }

    public boolean openRtpServer(int consumerCount) {
        try {
            if (rtpServer == null) {
                rtpServer = new NettyRTPServer();
                rtpServer.run(consumerCount);
            }
        } catch (Exception e) {
            logger.warn("Fail to Open RTP Server", e);
            return false;
        }
        return true;
    }

    /**
     * Port로 채널을 찾는 메서드
     * @param port Channel을 찾을 포트 (Key)
     * @return 포트 바인딩된 Channel. 없을 경우 null
     */
    public Channel getChannelByPort(int port) {
        return serverChannelMap.get(port);
    }

    /**
     * 메시지 전송
     * @param fromPort From Port. 포트바인딩된 채널이 없으면 전송하지 않음
     * @param toAddr Dest Address
     * @param data 전송할 data
     */
    public void sendMsg(int fromPort, InetSocketAddress toAddr, byte[] data) {
        sendMsg(fromPort, toAddr, Unpooled.copiedBuffer(data));
    }

    /**
     * 메시지 전송
     * @param port From Port. 포트바인딩된 채널이 없으면 전송하지 않음
     * @param toAddr Dest Address
     * @param buf 전송할 data
     */
    public void sendMsg(int port, InetSocketAddress toAddr, ByteBuf buf) {
        Channel ch = getChannelByPort(port);
        if (ch == null) return;
        synchronized (ch){
            if(!ch.isOpen()) return;
            if (toAddr == null) {
                logger.debug("Media Dest is null");
                return;
            }
            ch.writeAndFlush(new DatagramPacket(buf, toAddr));
        }
    }

    public void close(){
        if (rtpServer != null) rtpServer.close();
    }
}

/*
 * Copyright (C) 2021. Uangel Corp. All rights reserved.
 */

/**
 * @file NettyRTPServer.java
 * @author Tony Lim
 */

package com.uangel.media.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadFactory;

public class NettyRTPServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyRTPServer.class);
    Bootstrap b;
    NioEventLoopGroup group;

    public NettyRTPServer run (int consumerCount) throws Exception {
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("RTPServer-NioEventLoop-%d")
                .daemon(true)
                .priority(Thread.MAX_PRIORITY)
                .build();
        group = new NioEventLoopGroup(consumerCount, threadFactory);
        try {
            int bufferSize = 65536;
            b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .option(ChannelOption.SO_SNDBUF, bufferSize)
                    .option(ChannelOption.SO_RCVBUF, bufferSize)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {

                        @Override
                        public void initChannel (final NioDatagramChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IncommingPacketHandler());
                        }
                    });

        } catch (Exception e) {
            logger.error("Exception {}", e);
        } finally {
            logger.info("In Server Finally");
        }
        return null;
    }

    public void close ( ) {
        group.shutdownGracefully();
    }

    /**
     * netty server channel을 생성한다.
     *
     * @param ip   bind ip
     * @param port bind port
     * @return Channel
     */
    public Channel openChannel (String ip, int port) {
        InetAddress address;
        ChannelFuture channelFuture;

        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            logger.warn("() () () UnknownHostException is occured. ip={} {}", ip, e.getMessage());
            return null;
        }

        try {
            channelFuture = b.bind(address, port).sync();
            return channelFuture.channel();
        } catch (InterruptedException e) {
            logger.warn("() () () channel Interrupted! socket={}:{} {}", ip, port, e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Channel openChannel (int port) {
        ChannelFuture channelFuture;
        try {
            channelFuture = b.bind(port).sync();
            return channelFuture.channel();
        } catch (InterruptedException e) {
            logger.warn("() () () channel Interrupted! port={}", port, e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * netty server channel을 close 한다.
     *
     * @param ch 종료하려는 channel
     */
    public void closeChannel(Channel ch) {
        synchronized (ch){
            if (ch != null) {
                ch.closeFuture();
                ch.close();
            }
        }
    }

}

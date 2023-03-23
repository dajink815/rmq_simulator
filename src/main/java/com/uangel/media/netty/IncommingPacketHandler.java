/*
 * Copyright (C) 2021. Uangel Corp. All rights reserved.
 */

package com.uangel.media.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncommingPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger logger = LoggerFactory.getLogger(IncommingPacketHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        if (buf == null) { return; }
        if (buf.readableBytes() > 0) {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            // logger.debug("Recv UDP Message={}", data);
        }
    }
}
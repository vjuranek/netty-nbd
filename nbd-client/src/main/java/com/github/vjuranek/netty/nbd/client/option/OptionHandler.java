package com.github.vjuranek.netty.nbd.client.option;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class OptionHandler extends SimpleChannelInboundHandler<ByteBuf> {

    protected final int option;
    // TODO: hash map entry for capacity higher than one and concurrent options?
    protected final BlockingDeque<byte[]> reply = new LinkedBlockingDeque<>();

    public OptionHandler(int option) {
        this.option = option;
    }

    public byte[] getReply() throws InterruptedException {
        return reply.take();
    }

    public abstract void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;
}
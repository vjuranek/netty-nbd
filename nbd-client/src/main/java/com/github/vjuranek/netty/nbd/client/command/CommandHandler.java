package com.github.vjuranek.netty.nbd.client.command;

import com.github.vjuranek.netty.nbd.protocol.reply.NbdReply;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class CommandHandler extends SimpleChannelInboundHandler<ByteBuf> {

    protected final BlockingDeque<NbdReply> reply = new LinkedBlockingDeque<>();

    public NbdReply getReply() throws InterruptedException {
        return reply.take();
    }

    public abstract void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;
}
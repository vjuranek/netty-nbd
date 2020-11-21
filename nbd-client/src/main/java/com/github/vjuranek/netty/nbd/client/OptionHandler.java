package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class OptionHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final int option;
    private byte[] reply = new byte[] {};

    public OptionHandler(int option) {
        this.option = option;
    }

    public byte[] getReply() {
        return reply;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        long optionReplyMagic = msg.getLong(0);
        int option = msg.getInt(8);
        int reply = msg.getInt(12);

        if (optionReplyMagic != Constants.OPTION_REPLAY_MAGIC) {
            throw new IllegalArgumentException(String.format("Expected option reply magic, but got %x", optionReplyMagic));
        }

        if (this.option != option) {
            throw new IllegalStateException(String.format("Expected option for %x, but got for %x", this.option, option));
        }

        if (reply != Constants.NBD_REP_ACK) {
            throw new IllegalStateException(String.format("Server doesn't accept option %x", option));
        }

        int replySize = msg.getInt(16);
        this.reply = new byte[replySize];
        msg.readBytes(this.reply);

        ctx.pipeline().remove(this);
    }
}

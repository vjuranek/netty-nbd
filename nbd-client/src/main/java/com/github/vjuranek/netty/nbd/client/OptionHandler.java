package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class OptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        handleOptionReply((ByteBuf) msg, Constants.NBD_OPT_STRUCTURED_REPLY);
    }

    private int handleOptionReply(ByteBuf msg, int expectedOption) {
        long optionReplyMagic = msg.getLong(0);
        int option = msg.getInt(8);
        int reply = msg.getInt(12);
        int replySize = msg.getInt(16);

        if (optionReplyMagic != Constants.OPTION_REPLAY_MAGIC) {
            throw new IllegalArgumentException(String.format("Expected option reply magic, but got %x", optionReplyMagic));
        }

        if (expectedOption != option) {
            throw new IllegalStateException(String.format("Expected option for %x, but got for %x", expectedOption, option));
        }

        if (reply != Constants.NBD_REP_ACK) {
            throw new IllegalStateException(String.format("Server doesn't accept option %x", option));
        }

        return replySize;
    }
}

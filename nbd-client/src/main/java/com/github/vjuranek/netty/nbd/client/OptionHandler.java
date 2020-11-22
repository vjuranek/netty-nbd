package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class OptionHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final int option;
    // TODO: hash map entry for capacity higher than one and concurrent options.
    private final BlockingDeque<byte[]> reply = new LinkedBlockingDeque<>(1);

    public OptionHandler(int option) {
        this.option = option;
    }

    public byte[] getReply() throws InterruptedException {
        return reply.take();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        long optionReplyMagic = msg.getLong(0);
        int option = msg.getInt(8);
        int reply = msg.getInt(12);
        assertReply(optionReplyMagic, option, reply);

        int replySize = msg.getInt(16);
        byte[] content = new byte[replySize];
        msg.readBytes(content);
        this.reply.offer(content);

        ctx.pipeline().remove(this);
    }

    private final void assertReply(long optionReplyMagic, int option, int reply) {
        if (optionReplyMagic != Constants.OPTION_REPLAY_MAGIC) {
            throw new IllegalArgumentException(String.format("Expected option reply magic, but got %x", optionReplyMagic));
        }

        if (this.option != option) {
            throw new IllegalStateException(String.format("Expected option for %x, but got for %x", this.option, option));
        }

        if (reply != Constants.NBD_REP_ACK) {
            throw new IllegalStateException(String.format("Server doesn't accept option %x", option));
        }
    }
}

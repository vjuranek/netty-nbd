package com.github.vjuranek.netty.nbd.client.option;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;


public class AssertHandler extends OptionHandler {

    public AssertHandler(int option) {
        super(option);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        long optionReplyMagic = msg.readLong();
        int option = msg.readInt();
        int reply = msg.readInt();
        assertReply(optionReplyMagic, option, reply);

        int replySize = msg.readInt();
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

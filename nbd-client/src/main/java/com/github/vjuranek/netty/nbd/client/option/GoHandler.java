package com.github.vjuranek.netty.nbd.client.option;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class GoHandler extends OptionHandler {

    public GoHandler() {
        super(Constants.NBD_OPT_GO);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        long optionReplyMagic = msg.getLong(0);
        int option = msg.getInt(8);
        int reply = msg.getInt(12);

        int replySize = msg.getInt(16);
        byte[] content = new byte[replySize];
        msg.readBytes(content);
        this.reply.offer(content);

        ctx.pipeline().remove(this);
    }

}

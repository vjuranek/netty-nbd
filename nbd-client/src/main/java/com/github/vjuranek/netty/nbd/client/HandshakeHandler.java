package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class HandshakeHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        long nbdMagic = buf.getLong(0);
        long iHaveOpt = buf.getLong(8);
        short flags = buf.getShort(16);

        if (nbdMagic != Constants.NBD_MAGIC) {
            throw new IllegalArgumentException(String.format("Expected NBDMAGIC, but got %x", nbdMagic));
        }

        if (iHaveOpt != Constants.I_HAVE_OPT) {
            throw new IllegalArgumentException(String.format("Expected IHAVEOPT, but got %x", iHaveOpt));
        }

        if ((flags & Constants.NBD_FLAG_FIXED_NEWSTYLE) == 0) {
            throw new IllegalArgumentException(String.format("Unexpected flags %s", flags));
        }

        ByteBuf b = Unpooled.buffer(4);
        b.writeInt(Constants.NBD_FLAG_C_FIXED_NEWSTYLE);
        ChannelFuture f = ctx.writeAndFlush(b);
        f.addListener(writeFailed);

        ctx.pipeline().remove(this);
    }

    private final ChannelFutureListener writeFailed = (ChannelFuture future) -> {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
            future.channel().close();
        }
    };
}

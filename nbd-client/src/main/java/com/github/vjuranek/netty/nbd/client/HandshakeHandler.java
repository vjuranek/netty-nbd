package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;


public class HandshakeHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        long nbdMagic = msg.readLong();
        long iHaveOpt = msg.readLong();
        short flags = msg.readShort();
        assertReply(nbdMagic, iHaveOpt, flags);

        ByteBuf b = Unpooled.buffer(4);
        b.writeInt(Constants.NBD_FLAG_C_FIXED_NEWSTYLE);
        ChannelFuture f = ctx.writeAndFlush(b);
        f.addListener(Utils.writeFailed);

        ctx.pipeline().remove(this);
    }

    private final void assertReply(long nbdMagic, long iHaveOpt, short flags) {
        if (nbdMagic != Constants.NBD_MAGIC) {
            throw new IllegalArgumentException(String.format("Expected NBDMAGIC, but got %x", nbdMagic));
        }

        if (iHaveOpt != Constants.I_HAVE_OPT) {
            throw new IllegalArgumentException(String.format("Expected IHAVEOPT, but got %x", iHaveOpt));
        }

        if ((flags & Constants.NBD_FLAG_FIXED_NEWSTYLE) == 0) {
            throw new IllegalArgumentException(String.format("Unexpected flags %s", flags));
        }
    }
}

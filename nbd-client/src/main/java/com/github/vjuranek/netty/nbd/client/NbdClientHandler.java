package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import com.github.vjuranek.netty.nbd.protocol.Phase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class NbdClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Phase protocolPhase;

    public NbdClientHandler() {
        protocolPhase = Phase.HANDSHAKE;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        switch (protocolPhase) {
            case HANDSHAKE:
                handshakePhase(ctx, msg);
                break;
            case TRANSMISSION:
                throw new NotImplementedException();
            default:
                throw new IllegalStateException(String.format("%s is not allowed phase", protocolPhase));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void handshakePhase(ChannelHandlerContext ctx, ByteBuf msg) {
        ChannelPipeline pipeline = ctx.pipeline();
        ChannelHandlerContext c;
        long magic = msg.getLong(0);

        if (magic == Constants.NBD_MAGIC) {
            c = ctx;
        } else if (magic == Constants.OPTION_REPLAY_MAGIC) {
            c = pipeline.context(HandshakeHandler.class);
        } else {
            throw new IllegalStateException(String.format("Unknown magic %x", magic));
        }

        if (c != null) {
            c.fireChannelRead(msg);
            sendOption(ctx, Constants.NBD_OPT_STRUCTURED_REPLY, new byte[]{});
        }
    }

    private void sendOption(ChannelHandlerContext ctx, int option, byte[] data) {
        int len = 16 + data.length; // long + int + int + data length
        ByteBuf b = Unpooled.buffer(len);
        b.writeLong(Constants.I_HAVE_OPT);
        b.writeInt(option);
        b.writeInt(data.length);
        if (data.length > 0) {
            b.writeBytes(data);
        }
        ChannelFuture f = ctx.writeAndFlush(b);
        f.addListener(writeFailed);
    }

    private final ChannelFutureListener writeFailed = (ChannelFuture future) -> {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
            future.channel().close();
        }
    };
}

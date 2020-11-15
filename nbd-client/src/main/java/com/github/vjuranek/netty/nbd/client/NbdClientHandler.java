package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import com.github.vjuranek.netty.nbd.protocol.Phase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class NbdClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Phase protocolPhase;

    public NbdClientHandler() {
        protocolPhase = Phase.HANDSHAKE;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
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
        //ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void handshakePhase(ChannelHandlerContext ctx, ByteBuf msg) {
        long magic = msg.getLong(0);
        if (magic == Constants.NBD_MAGIC) {
            doHandshake(ctx, msg);
            sendOption(ctx, Constants.NBD_OPT_STRUCTURED_REPLY, new byte[]{});
        } else if (magic == Constants.OPTION_REPLAY_MAGIC) {
            handleOptionReply(msg, Constants.NBD_OPT_STRUCTURED_REPLY);
        } else {
            throw new IllegalStateException(String.format("Unknown magic %x", magic));
        }
    }

    private void doHandshake(ChannelHandlerContext ctx, ByteBuf msg) {
        long nbdMagic = msg.getLong(0);
        long iHaveOpt = msg.getLong(8);
        short flags = msg.getShort(16);

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

    private final ChannelFutureListener writeFailed = (ChannelFuture future) -> {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
            future.channel().close();
        }
    };
}

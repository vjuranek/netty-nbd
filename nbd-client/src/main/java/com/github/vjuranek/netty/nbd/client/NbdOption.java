package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class NbdOption {

    private final Channel channel;
    private final int option;
    private final OptionHandler handler;

    public NbdOption(Channel channel, int option) {
        this.channel = channel;
        this.option = option;
        this.handler = new OptionHandler(option);
    }

    public Channel getChannel() {
        return channel;
    }

    public void send() {
        send(new byte[] {});
    }

    public void send(byte[] data) {
        getChannel().pipeline().addLast(handler);

        int len = 16 + data.length; // long + int + int + data length
        ByteBuf b = Unpooled.buffer(len);
        b.writeLong(Constants.I_HAVE_OPT);
        b.writeInt(option);
        b.writeInt(data.length);
        if (data.length > 0) {
            b.writeBytes(data);
        }

        ChannelFuture f = getChannel().pipeline().writeAndFlush(b);
        f.addListener(Utils.writeFailed);
    }

    public byte[] getReply() throws InterruptedException {
        return handler.getReply();
    }
}

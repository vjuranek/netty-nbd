package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.command.NbdCmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class NbdCommand {

    private final Channel channel;
    private final NbdCmd command;

    public NbdCommand(Channel channel, NbdCmd command) {
        this.channel = channel;
        this.command = command;
    }

    public Channel getChannel() {
        return channel;
    }

    public void send() {
        ByteBuf b = Unpooled.buffer(command.cmdLength());
        b.writeBytes(command.encode());

        ChannelFuture f = getChannel().pipeline().writeAndFlush(b);
        f.addListener(Utils.writeFailed);
    }
}

package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.command.NbdCmd;
import io.netty.channel.Channel;

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
        getChannel().pipeline().writeAndFlush(command.asBuffer()).addListener(Utils.writeFailed);
    }
}

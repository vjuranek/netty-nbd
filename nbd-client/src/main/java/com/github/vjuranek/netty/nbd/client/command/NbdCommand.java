package com.github.vjuranek.netty.nbd.client.command;


import io.netty.channel.Channel;

public class NbdCommand {

    private final Channel channel;

    private NbdCommand() {
        throw new IllegalArgumentException("Channel has to be provided");
    }

    public NbdCommand(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}

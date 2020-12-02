package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.client.option.NbdOption;
import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

public final class NbdClient {

    static final String HOST = "127.0.0.1";
    private static final EventLoopGroup group = new NioEventLoopGroup();

    private final Channel channel;

    public NbdClient() throws  InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                //.option(ChannelOption.AUTO_READ, false)
                .handler(new NbdChannelInitializer());

        b.remoteAddress(HOST, Constants.NBD_PORT);
        ChannelFuture f = b.connect().sync();

        this.channel = f.channel();
    }

    public byte[] structuredReplyOption() throws InterruptedException {
        NbdOption structReply = new NbdOption(this.channel, Constants.NBD_OPT_STRUCTURED_REPLY);
        structReply.send();
        return structReply.getReply();
    }

    public void close() {
//        NbdCommand cmd = new NbdCommand(this.channel, new DiscCmd());
//        cmd.send();
        NbdOption abort = new NbdOption(this.channel, Constants.NBD_OPT_ABORT);
        abort.send();
    }

    public static void main(String[] args) throws Exception {
        NbdClient client;
        try {
            client = new NbdClient();
            byte[] reply = client.structuredReplyOption();
            System.out.println("REPLY: " + new String(reply));
            client.close();
        } catch(InterruptedException e) {
            // no-op
        } finally {
            shutdown();
        }

    }

    private static void shutdown() {
        Future<?> f = group.shutdownGracefully();
        f.syncUninterruptibly();
    }
}
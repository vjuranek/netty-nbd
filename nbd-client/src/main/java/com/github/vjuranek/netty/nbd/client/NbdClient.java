package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.client.option.GoOption;
import com.github.vjuranek.netty.nbd.client.option.NbdOption;
import com.github.vjuranek.netty.nbd.protocol.Constants;
import com.github.vjuranek.netty.nbd.protocol.Phase;
import com.github.vjuranek.netty.nbd.protocol.command.DiscCmd;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    private Phase phase;

    public NbdClient() throws  InterruptedException {
        this.phase = Phase.HANDSHAKE;

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

    public int goOption(String exportName) throws InterruptedException {
        GoOption go = new GoOption(this.channel, exportName);
        go.send();
        byte[] reply = go.getReply();
        int rc = Unpooled.buffer(4).writeBytes(reply).readInt();
        if (rc == Constants.NBD_REP_ACK) {
            this.phase = Phase.TRANSMISSION;
        }
        return rc;
    }

    public void close() {
        NbdCommand cmd = new NbdCommand(this.channel, new DiscCmd());
        cmd.send();
//        NbdOption abort = new NbdOption(this.channel, Constants.NBD_OPT_ABORT);
//        abort.send();
    }

    public static void main(String[] args) throws Exception {
        NbdClient client;
        try {
            client = new NbdClient();
            byte[] reply = client.structuredReplyOption();
            System.out.println("STRUCT OPT REPLY: " + new String(reply));
            int rc = client.goOption("test");
            System.out.println("GO OPT REPLY: " + rc);
            System.out.println("client phase: " + client.phase);
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
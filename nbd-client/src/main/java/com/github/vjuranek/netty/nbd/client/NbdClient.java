package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.client.command.ReadHandler;
import com.github.vjuranek.netty.nbd.client.option.GoOption;
import com.github.vjuranek.netty.nbd.client.option.NbdOption;
import com.github.vjuranek.netty.nbd.protocol.Constants;
import com.github.vjuranek.netty.nbd.protocol.Phase;
import com.github.vjuranek.netty.nbd.protocol.command.DiscCmd;
import com.github.vjuranek.netty.nbd.protocol.command.ReadCmd;
import io.netty.bootstrap.Bootstrap;
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

    public byte[] read(long offset, int length) throws IllegalStateException, InterruptedException {
        if (this.phase != Phase.TRANSMISSION) {
            throw new IllegalStateException("Cannot send READ command if not in transmission phase.");
        }

        ReadCmd readCmd = new ReadCmd(offset, length);
        ReadHandler handler = new ReadHandler(readCmd.getHandle());
        this.channel.pipeline().addLast(handler);
        NbdCommand cmd = new NbdCommand(this.channel, readCmd);
        cmd.send();
        return handler.getReply().getData();
    }

    public void close() {
        switch (this.phase) {
            case HANDSHAKE:
                NbdOption abort = new NbdOption(this.channel, Constants.NBD_OPT_ABORT);
                abort.send();
                break;
            case TRANSMISSION:
                NbdCommand cmd = new NbdCommand(this.channel, new DiscCmd());
                cmd.send();
                break;
        }
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
            // TODO: has to be less than 1004 bytes (20 bytes header + 1004 - some Netty buffer with default 1024B ?)
            byte[] data = client.read(0L, 512);
            for (byte b : data) {
                System.out.printf("%x", b);
            }
            System.out.println();
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
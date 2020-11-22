package com.github.vjuranek.netty.nbd.client;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class Utils {

    public static final ChannelFutureListener writeFailed = (ChannelFuture future) -> {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
            future.channel().close();
        }
    };
}

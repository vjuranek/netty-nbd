package com.github.vjuranek.netty.nbd.protocol.reply;

public interface NbdReply {
    long getHandle();
    byte[] getData();
}

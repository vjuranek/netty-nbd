package com.github.vjuranek.netty.nbd.client.option;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.Set;

public class InfoOption extends NbdOption {

    private final String exportName;
    private final Set<Short> infoReqs;

    public InfoOption(Channel channel, String exportName, Set<Short> infoReqs) {
        super(channel, Constants.NBD_OPT_INFO, new GoHandler());
        this.exportName = exportName;
        this.infoReqs = infoReqs;
    }

    public void send() {
        int len = 4 + exportName.length() + 2;
        ByteBuf buf = Unpooled.buffer(len);
        buf.writeInt(exportName.length());
        buf.writeBytes(exportName.getBytes());
        buf.writeShort(infoReqs.size());
        for(Short info : infoReqs) {
            buf.writeShort(info);
        }
        super.send(buf.array());
    }
}

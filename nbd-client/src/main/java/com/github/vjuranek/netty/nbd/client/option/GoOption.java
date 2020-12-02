package com.github.vjuranek.netty.nbd.client.option;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

public class GoOption extends NbdOption {

    private final String exportName;

    public GoOption(Channel channel, String exportName) {
        super(channel, Constants.NBD_OPT_GO, new GoHandler());
        this.exportName = exportName;
    }

    public void send() {
        int len = 4 + exportName.length() + 2;
        ByteBuf buf = Unpooled.buffer(len);
        buf.writeInt(exportName.length());
        buf.writeBytes(exportName.getBytes());
        // TODO: write NBD_INFO requests
        buf.writeShort(0);
        super.send(buf.array());
    }
}

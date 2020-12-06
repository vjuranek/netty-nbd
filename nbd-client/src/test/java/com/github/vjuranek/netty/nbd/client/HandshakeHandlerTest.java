package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class HandshakeHandlerTest {

    @Test
    public void testHandshake() {
        EmbeddedChannel channel = new EmbeddedChannel(new HandshakeHandler());

        ByteBuf buf = Unpooled.buffer(18);
        buf.writeLong(Constants.NBD_MAGIC);
        buf.writeLong(Constants.I_HAVE_OPT);
        buf.writeShort(Constants.NBD_FLAG_FIXED_NEWSTYLE);

        channel.writeInbound(buf);
        assertTrue(channel.finish());

        ByteBuf out = channel.readOutbound();
        assertEquals(Constants.NBD_FLAG_C_FIXED_NEWSTYLE, out.readInt());
        out.release();
    }
}

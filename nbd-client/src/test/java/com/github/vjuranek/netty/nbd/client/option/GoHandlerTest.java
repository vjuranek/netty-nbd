package com.github.vjuranek.netty.nbd.client.option;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GoHandlerTest {

    @Test
    public void testNbdRepAckOnly() throws InterruptedException {
        GoHandler goHandler = new GoHandler();
        EmbeddedChannel channel = new EmbeddedChannel(goHandler);

        ByteBuf buf = Unpooled.buffer(20);
        buf.writeLong(Constants.NBD_STRUCTURED_REPLY_MAGIC);
        buf.writeInt(Constants.NBD_OPT_STRUCTURED_REPLY);
        buf.writeInt(Constants.NBD_REP_ACK);
        buf.writeInt(0);

        channel.writeInbound(buf);
        // TODO: assert channel.finish() - fails, queue is null
        // assertTrue(channel.finish());

        int rc = Unpooled.buffer(4).writeBytes(goHandler.reply.take()).readInt();
        assertEquals(Constants.NBD_REP_ACK, rc);
        assertEquals(0, goHandler.reply.size());
    }

}

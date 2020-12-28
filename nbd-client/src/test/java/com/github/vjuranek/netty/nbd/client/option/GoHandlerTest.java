package com.github.vjuranek.netty.nbd.client.option;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testNbdInfoBlockSize() throws InterruptedException {
        GoHandler goHandler = new GoHandler();
        EmbeddedChannel channel = new EmbeddedChannel(goHandler);

        ByteBuf buf = Unpooled.buffer(20);
        buf.writeLong(Constants.NBD_STRUCTURED_REPLY_MAGIC);
        buf.writeInt(Constants.NBD_OPT_STRUCTURED_REPLY);
        buf.writeInt(Constants.NBD_REP_ACK);
        buf.writeInt(Constants.INFO_EXPORT_REPLY_LENGTH);
        buf.writeShort(Constants.NBD_INFO_EXPORT);
        buf.writeInt(512);
        buf.writeInt(512);
        buf.writeInt(512);

        channel.writeInbound(buf);

        int rc = Unpooled.buffer(4).writeBytes(goHandler.reply.take()).readInt();
        assertEquals(Constants.NBD_REP_ACK, rc);
        assertEquals(0, goHandler.reply.size());
    }

    @Test
    public void testNbdInfoExport() throws InterruptedException {
        GoHandler goHandler = new GoHandler();
        EmbeddedChannel channel = new EmbeddedChannel(goHandler);

        ByteBuf buf = Unpooled.buffer(20);
        buf.writeLong(Constants.NBD_STRUCTURED_REPLY_MAGIC);
        buf.writeInt(Constants.NBD_OPT_STRUCTURED_REPLY);
        buf.writeInt(Constants.NBD_REP_ACK);
        buf.writeInt(Constants.INFO_EXPORT_REPLY_LENGTH);
        buf.writeShort(Constants.NBD_INFO_EXPORT);
        buf.writeLong(10L);
        buf.writeShort(0);

        channel.writeInbound(buf);

        int rc = Unpooled.buffer(4).writeBytes(goHandler.reply.take()).readInt();
        assertEquals(Constants.NBD_REP_ACK, rc);
        assertEquals(0, goHandler.reply.size());
    }

    @Test
    public void testNbdInfoExportWrongSize() throws InterruptedException {
        GoHandler goHandler = new GoHandler();
        EmbeddedChannel channel = new EmbeddedChannel(goHandler);

        ByteBuf buf = Unpooled.buffer(20);
        buf.writeLong(Constants.NBD_STRUCTURED_REPLY_MAGIC);
        buf.writeInt(Constants.NBD_OPT_STRUCTURED_REPLY);
        buf.writeInt(Constants.NBD_REP_ACK);
        buf.writeInt(Constants.INFO_EXPORT_REPLY_LENGTH - 1);
        buf.writeShort(Constants.NBD_INFO_EXPORT);
        buf.writeLong(10L);
        buf.writeShort(0);

        try {
            channel.writeInbound(buf);
            // TODO: wait for a short time? The calls are async.
            throw new AssertionError("Handler should throw an exception");
        } catch (IllegalStateException e) {
            assertEquals("Size of NBD_INFO_EXPORT has to be 12, but got 11", e.getMessage());
        }

    }

    @Test
    public void testNbdInfoNoReadableBytes() throws InterruptedException {
        GoHandler goHandler = new GoHandler();
        EmbeddedChannel channel = new EmbeddedChannel(goHandler);

        ByteBuf buf = Unpooled.buffer(20);
        buf.writeLong(Constants.NBD_STRUCTURED_REPLY_MAGIC);
        buf.writeInt(Constants.NBD_OPT_STRUCTURED_REPLY);
        buf.writeInt(Constants.NBD_REP_ACK);
        buf.writeInt(Constants.INFO_EXPORT_REPLY_LENGTH);
        // Assume malformed message - write just one byte
        buf.writeByte(0);

        try {
            channel.writeInbound(buf);
            // TODO: wait for a short time? The calls are async.
            throw new AssertionError("Handler should throw an exception");
        } catch (IndexOutOfBoundsException e) {
            assertTrue(e.getMessage().startsWith("readerIndex(20) + length(2) exceeds writerIndex(21)"));
        }

    }

    @Test
    public void testWrongMagic() throws InterruptedException {
        GoHandler goHandler = new GoHandler();
        EmbeddedChannel channel = new EmbeddedChannel(goHandler);

        ByteBuf buf = Unpooled.buffer(20);
        // Wrong reply magic.
        buf.writeLong(0L);
        buf.writeInt(Constants.NBD_OPT_STRUCTURED_REPLY);
        buf.writeInt(Constants.NBD_REP_ACK);
        buf.writeInt(Constants.INFO_EXPORT_REPLY_LENGTH);
        buf.writeByte(0);

        try {
            channel.writeInbound(buf);
            // TODO: wait for a short time? The calls are async.
            throw new AssertionError("Handler should throw an exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Expected option reply magic, but got 0"));
        }

    }

}

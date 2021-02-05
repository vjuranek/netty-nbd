package com.github.vjuranek.netty.nbd.client.command;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import com.github.vjuranek.netty.nbd.protocol.reply.DataChunk;
import com.github.vjuranek.netty.nbd.protocol.reply.NbdReply;
import com.github.vjuranek.netty.nbd.protocol.reply.SimpleReply;
import com.github.vjuranek.netty.nbd.protocol.reply.StructuredReply;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * TODO:
 * - handle errors
 * - redesign for parallel/async usage
 * - allow to handle multiple data chunks send by NBD server (need to check NBD_REPLY_FLAG_DONE for data chunks)
 */
public class ReadHandler extends CommandHandler {

    public static final short NBD_REPLY_TYPE_NONE = 0;
    public static final short NBD_REPLY_TYPE_OFFSET_DATA = 1;

    public static final short NBD_REPLY_FLAG_DONE = 0b0000000000000001;

    private final long handle;
    private final int length; // length of simple reply message

    private boolean firstChunk;
    private ReplyMagic replyMagic;
    private short replyFlags;
    private short replyType;

    public ReadHandler(long handle) {
        this.handle = handle;
        this.length = 0;
        this.firstChunk = true;
    }

    public ReadHandler(long handle, int length) {
        this.handle = handle;
        this.length = length;
        this.firstChunk = true;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (this.firstChunk) {
            int replyMagic = msg.readInt();
            switch (replyMagic) {
                case Constants.NBD_SIMPLE_REPLY_MAGIC:
                    this.replyMagic = ReplyMagic.SIMPLE_REPLY;
                    break;
                case Constants.NBD_STRUCTURED_REPLY_MAGIC:
                    this.replyMagic = ReplyMagic.STRUCTURED_REPLY;
                    break;
                default:
                    throw new IllegalStateException(String.format("Unknown read reply magic %x", replyMagic));
            }
        }

        NbdReply reply;
        switch (this.replyMagic) {
            case SIMPLE_REPLY:
                this.replyMagic = ReplyMagic.SIMPLE_REPLY;
                reply = new SimpleReply(msg, this.length);
                if (this.handle == reply.getHandle()) {
                    this.reply.offer(reply);
                }
                break;
            case STRUCTURED_REPLY:
                this.replyMagic = ReplyMagic.STRUCTURED_REPLY;
                if (this.firstChunk) {
                    reply = new StructuredReply(msg);
                    this.replyFlags = ((StructuredReply) reply).getFlags();
                    this.replyType = ((StructuredReply) reply).getType();
                } else {
                    reply = new StructuredReply(msg, this.replyFlags, this.replyType, this.handle);
                }
                if (this.handle == reply.getHandle()) {
                    handleStructuredReply((StructuredReply) reply);
                }
                break;
        }

        this.firstChunk = false;
    }

    private void handleStructuredReply(StructuredReply reply) {
        switch (reply.getType()) {
            case NBD_REPLY_TYPE_NONE:
                if ((NBD_REPLY_FLAG_DONE & reply.getFlags()) != NBD_REPLY_FLAG_DONE) {
                    throw new IllegalStateException("None type reply hasn't  NBD_REPLY_FLAG_DONE");
                }
                break;
            case NBD_REPLY_TYPE_OFFSET_DATA:
                this.reply.offer(new DataChunk(this.handle, reply.getData(), this.firstChunk));
                break;
            default:
                throw new IllegalStateException("Unknown reply type " + reply.getType());
        }
    }

    private enum ReplyMagic {
        SIMPLE_REPLY,
        STRUCTURED_REPLY
    }

}

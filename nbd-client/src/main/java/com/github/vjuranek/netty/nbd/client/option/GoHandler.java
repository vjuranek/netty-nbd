package com.github.vjuranek.netty.nbd.client.option;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class GoHandler extends OptionHandler {

    public GoHandler() {
        super(Constants.NBD_OPT_GO);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int rc = 0;
        while (rc != Constants.NBD_REP_ACK) {
            long optionReplyMagic = msg.readLong();
            int option = msg.readInt();
            rc = msg.readInt();
            int length = msg.readInt();

            if (msg.readableBytes() > 1) {
                handleNbdInfo(msg, length);
            }
        }
        ByteBuf reply = Unpooled.buffer(4);
        reply.writeInt(rc);
        this.reply.offer(reply.array());

        ctx.pipeline().remove(this);
    }

    private void handleNbdInfo(ByteBuf msg, int dataLength) {
        switch (msg.readShort()) {
            case Constants.NBD_INFO_BLOCK_SIZE:
                if (dataLength != 14) {
                    throw new IllegalStateException("Size of NBD_INFO_BLOCK_SIZE has to be 14, but got " + dataLength);
                }
                int minBlockSize = msg.readInt();
                int prefferedBlockSize = msg.readInt();
                int maxBlockSize = msg.readInt();
                break;
            case Constants.NBD_INFO_EXPORT:
                if (dataLength != 12) {
                    throw new IllegalStateException("Size of NBD_INFO_EXPORT has to be 12, but got " + dataLength);
                }
                long exportSize = msg.readLong();
                short flags = msg.readShort();
                break;
            default:
                msg.readBytes(dataLength);
                //throw new IllegalStateException("Unsupported info option " + info_type);
        }
    }

}

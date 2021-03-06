package com.github.vjuranek.netty.nbd.protocol.reply;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataChunk implements NbdReply {
    private final long handle;
    private final long offset;
    private final byte[] data;

    public DataChunk(long handle, byte[] payload, boolean isOffset) {
        this.handle = handle;
        if (isOffset) {
            this.offset = ByteBuffer.wrap(Arrays.copyOf(payload, 8)).getLong();
            this.data = Arrays.copyOfRange(payload, 8, payload.length);
        } else {
            this.offset = -1;
            this.data = Arrays.copyOfRange(payload, 0, payload.length);
        }
    }

    @Override
    public long getHandle() {
        return handle;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}

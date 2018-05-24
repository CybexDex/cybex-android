package com.cybexmobile.fc.io;

import java.nio.ByteBuffer;

public class DataStreamEncoder implements BaseEncoder {

    private ByteBuffer mByteBuffer;

    public DataStreamEncoder(int nSize) {
        mByteBuffer = ByteBuffer.allocate(nSize);
    }
    @Override
    public void write(byte[] data) {
        mByteBuffer.put(data);
    }

    @Override
    public void write(byte[] data, int off, int len) {
        mByteBuffer.put(data, off, len);
    }

    @Override
    public void write(byte data) {
        mByteBuffer.put(data);
    }

    public byte[] getData() {
        return mByteBuffer.array();
    }
}

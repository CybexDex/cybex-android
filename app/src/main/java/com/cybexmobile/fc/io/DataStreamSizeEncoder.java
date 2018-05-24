package com.cybexmobile.fc.io;

public class DataStreamSizeEncoder implements BaseEncoder {

    private int mnSize = 0;

    @Override
    public void write(byte[] data) {
        mnSize += data.length;
    }

    @Override
    public void write(byte[] data, int off, int len) {
        mnSize += len;
    }

    @Override
    public void write(byte data) {
        mnSize += 1;
    }

    public int getSize() {
        return mnSize;
    }
}

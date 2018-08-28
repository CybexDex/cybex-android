package com.cybex.provider.fc.io;

public interface BaseEncoder {

    void write(byte[] data);
    void write(byte[] data, int off, int len);
    void write(byte data);
}

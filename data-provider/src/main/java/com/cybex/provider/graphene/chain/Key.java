package com.cybex.provider.graphene.chain;

public class Key {
    private String compressed;
    private String uncompressed;
    private String private_key;
    private String public_key;
    private String address;

    public Key(String compressed, String uncompressed, String private_key, String public_key, String address) {
        this.address = address;
        this.public_key = public_key;
        this.compressed = compressed;
        this.uncompressed = uncompressed;
        this.private_key = private_key;
    }
}

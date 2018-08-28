package com.cybex.provider.graphene.chain;

public class PublicKey {

    private byte[] key_data = new byte[33];
    private byte[] key_data_uncompressed = new byte[65];

    public PublicKey(byte[] key, boolean isCompressed) {
        if (isCompressed) {
            System.arraycopy(key, 0, key_data, 0, key_data.length);
        } else {
            System.arraycopy(key, 0, key_data_uncompressed, 0, key_data_uncompressed.length);
        }
    }

    public byte[] getKeyByte(boolean isCompressed) {
        if (isCompressed) {
            return key_data;
        } else {
            return key_data_uncompressed;
        }
    }

    public static boolean is_canonical(CompactSignature c) {
        /*return !(c.data[1] & 0x80)
                && !(c.data[1] == 0 && !(c.data[2] & 0x80))
                && !(c.data[33] & 0x80)
                && !(c.data[33] == 0 && !(c.data[34] & 0x80));*/

        boolean bCompareOne = ((c.data[1] & 0x80) == 0);
        boolean bCompareTwo = ((c.data[1] == 0) && ((c.data[2] & 0x80) == 0)) == false;
        boolean bCompareThree = ((c.data[33] & 0x80) == 0);
        boolean bCompareFour = ((c.data[33] == 0) && ((c.data[34] & 0x80) ==0)) == false;

        return bCompareOne && bCompareTwo && bCompareThree && bCompareFour;
    }
}

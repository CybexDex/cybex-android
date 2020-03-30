package io.enotes.sdk.repository.db.converter;

import android.arch.persistence.room.TypeConverter;

import java.math.BigInteger;

/**
 * Card value convert BigInteger to String
 * if it's a invalid String, will return BigInteger.ZERO
 */
public class ValueConverter {
    @TypeConverter
    public static String fromBigInteger(BigInteger bigInteger) {
        if (bigInteger == null) {
            return "0";
        }

        return bigInteger.toString();
    }

    @TypeConverter
    public static BigInteger toBigInteger(String value) {
        if (value == null || value.isEmpty()) {
            return BigInteger.ZERO;
        }
        try {
            return new BigInteger(value);
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }
}

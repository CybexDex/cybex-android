package io.enotes.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.ethereum.core.CallTransaction;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;

/**
 * ERC20 token utils
 */
public class ERC20 {


    private static CallTransaction.Function balanceOfFunction;
    private static CallTransaction.Function nameFunction;
    private static CallTransaction.Function symbolFunction;
    private static CallTransaction.Function transferFunction;
    private static CallTransaction.Function decimalsFunction;

    /**
     * balanceOf abi function
     *
     * @return
     */
    public static CallTransaction.Function balanceOf() {
        if (balanceOfFunction == null) {
            balanceOfFunction = CallTransaction.Function.fromSignature("balanceOf", new String[]{"address"}, new String[]{"uint256"});
        }
        return balanceOfFunction;
    }

    /**
     * name abi function
     *
     * @return
     */
    public static CallTransaction.Function name() {
        if (nameFunction == null) {
            nameFunction = CallTransaction.Function.fromSignature("name", new String[]{}, new String[]{"string"});
        }
        return nameFunction;
    }

    /**
     * symbol abi function
     *
     * @return
     */
    public static CallTransaction.Function symbol() {
        if (symbolFunction == null) {
            symbolFunction = CallTransaction.Function.fromSignature("symbol", new String[]{}, new String[]{"string"});
        }
        return symbolFunction;
    }

    /**
     * decimals abi function
     *
     * @return
     */
    public static CallTransaction.Function decimals() {
        if (decimalsFunction == null) {
            decimalsFunction = CallTransaction.Function.fromSignature("decimals", new String[]{}, new String[]{"uint8"});
        }
        return decimalsFunction;
    }

    /**
     * transfer abi function
     *
     * @return
     */
    public static CallTransaction.Function transfer() {
        if (transferFunction == null) {
            transferFunction = CallTransaction.Function.fromSignature("transfer", new String[]{"address", "uint256"}, new String[]{"bool"});
        }
        return transferFunction;
    }

    /**
     * decode balanceOf result
     *
     * @param result
     * @return
     */
    public static String decodeBalanceOfFunctionResult(String result) {
        if (result == null || result.isEmpty()) return null;
        Object[] objects = balanceOf().decodeResult(ByteUtil.hexStringToBytes(result.substring(2)));
        if (objects != null && objects.length == 1)
            return ((BigInteger) objects[0]).toString();
        return null;
    }

    /**
     * decode name result
     *
     * @param result
     * @return
     */
    public static String decodeNameFunctionResult(String result) {
        if (result == null || result.isEmpty()) return null;
        Object[] objects = name().decodeResult(ByteUtil.hexStringToBytes(result.substring(2)));
        if (objects != null && objects.length == 1)
            return ((String) objects[0]).toString();
        return null;
    }

    /**
     * decode decimals result
     *
     * @param result
     * @return
     */
    public static int decodeDecimalsFunctionResult(String result) {
        if (result == null || result.isEmpty()) return 0;
        Object[] objects = decimals().decodeResult(ByteUtil.hexStringToBytes(result.substring(2)));
        if (objects != null && objects.length == 1)
            return ((BigInteger) objects[0]).intValue();
        return 0;
    }

    /**
     * decode sybmol result
     *
     * @param result
     * @return
     */
    public static String decodeSymbolFunctionResult(String result) {
        if (result == null || result.isEmpty()) return null;
        Object[] objects = name().decodeResult(ByteUtil.hexStringToBytes(result.substring(2)));
        if (objects != null && objects.length == 1)
            return ((String) objects[0]).toString();
        return null;
    }
}

package io.enotes.sdk.utils;

import android.support.annotation.Nullable;

import org.ethereum.core.CallTransaction;
import org.ethereum.util.ByteUtil;

public class ContractUtils {
    // MNS contract function
    private static final String ABI_FUNCTION_KEY_OF = "keyOf";

    private static final String ABI_PARAM_TYPE_UINT256 = "uint256";
    private static final String ABI_PARAM_TYPE_BYTES32 = "bytes32";
    private static final String ABI_PARAM_TYPE_BYTES = "bytes";

    private static CallTransaction.Function sFunctionKeyOf;


    /**
     * Get keyOf abi function.
     * {"constant":false,"inputs":[{"name":"_mid","type":"bytes32"},{"name":"_bid","type":"uint256"}],
     * "name":"keyOf",
     * "outputs":[{"name":"_addr","type":"string"}],
     * "payable":false,"type":"function"}
     *
     * @return
     */
    public static CallTransaction.Function getAbiFunctionKeyOf() {
        if (sFunctionKeyOf == null) {
            sFunctionKeyOf = CallTransaction.Function.fromSignature(ABI_FUNCTION_KEY_OF,
                    new String[]{ABI_PARAM_TYPE_BYTES32, ABI_PARAM_TYPE_BYTES32},
                    new String[]{ABI_PARAM_TYPE_BYTES});
        }
        return sFunctionKeyOf;
    }

    /**
     * Decode the result of keyOf abi function
     *
     * @param result
     * @return
     */
    @Nullable
    public static String decodeAbiFunctionKeyOfResult(@Nullable String result) {
        if (result == null || result.isEmpty()) return null;
        Object[] objects = ContractUtils.getAbiFunctionKeyOf().decodeResult(ByteUtil.hexStringToBytes(result.substring(2)));
        if (objects != null && objects.length == 1)
            return ByteUtil.toHexString((byte[]) objects[0]);
        return null;
    }
}

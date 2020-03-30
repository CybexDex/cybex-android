package io.enotes.sdk.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.google.common.math.LongMath;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.BigInteger;


import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.repository.db.entity.Card;

import static io.enotes.sdk.constant.Constant.BlockChain.BITCOIN;
import static io.enotes.sdk.constant.Constant.BlockChain.BITCOIN_CASH;
import static io.enotes.sdk.constant.Constant.BlockChain.CYBEX;
import static io.enotes.sdk.constant.Constant.BlockChain.ETHEREUM;
import static io.enotes.sdk.constant.Constant.BlockChain.RIPPLE;
import static java.lang.Integer.parseInt;
import static org.bitcoinj.core.Coin.COIN;
import static org.bitcoinj.core.Coin.MICROCOIN;
import static org.bitcoinj.core.Coin.MILLICOIN;
import static org.bitcoinj.core.Coin.SMALLEST_UNIT_EXPONENT;

public class CardUtils {


    @Retention(RetentionPolicy.SOURCE)
    @StringDef(value = {BITCOIN, ETHEREUM, BITCOIN_CASH})
    @interface BlockChain {
    }

    /**
     * The number of satoshis equal to one bitcoin.
     */
    private static final BigDecimal BITCOIN_SATOSHI = BigDecimal.valueOf(LongMath.pow(10, SMALLEST_UNIT_EXPONENT));
    /**
     * The number of weis equal to one eth.
     */
    private static final BigDecimal ETH_WEI = new BigDecimal("1000000000000000000");


    /**
     * Check whether it's BTC.
     *
     * @param blockChain
     * @return
     */
    public static boolean isBTC(String blockChain) {
        return BITCOIN.equals(blockChain) || BITCOIN_CASH.equals(blockChain);
    }

    /**
     * Check whether it's ETH.
     *
     * @param blockChain
     * @return
     */
    public static boolean isETH(String blockChain) {
        return ETHEREUM.equals(blockChain);
    }

    /**
     * Get address of the Crypto Currency.
     *
     * @param card
     * @param blockChain
     * @return null if no public key or not supported code type
     */
    @Nullable
    public static String getAddress(@NonNull Card card, @BlockChain String blockChain) {
        if (blockChain.equals(BITCOIN)) {
            if (card.getCert().getNetWork() == 0)
                return card.getBitcoinMainAddress();
            else
                return card.getBitcoinTest3Address();
        } else if (blockChain.equals(BITCOIN_CASH)) {
            if (card.getCert().getNetWork() == 0)
                return card.getBitcoinCashMainAddress();
            else
                return card.getBitcoinCashTest3Address();
        } else if (blockChain.equals(ETHEREUM))
            return card.getEthTxAddress();
        else if (blockChain.equals(RIPPLE))
            return card.getRippleAddress();
        else if (blockChain.equals(CYBEX))
            return card.getEthTxAddress();
        return null;
    }

    /**
     * Mixed-case checksum address encoding
     * （reference：https://eips.ethereum.org/EIPS/eip-55）
     *
     * @param address
     * @return
     */
    public static String getEthEncodeAddress(String address) {
        address = address.toLowerCase().replace("0x", "");
        String hash = ByteUtil.toHexString(HashUtil.sha3(address.getBytes()));
        StringBuffer ret = new StringBuffer();
        ret.append("0x");
        for (int i = 0; i < address.length(); i++) {
            if (parseInt(String.valueOf(hash.charAt(i)), 16) >= 8) {
                ret.append(String.valueOf(address.charAt(i)).toUpperCase());
            } else {
                ret.append(String.valueOf(address.charAt(i)));
            }
        }
        return ret.toString();
    }

    /**
     * Convert common to smallest unit of the digit currency,
     * such as bitcoin to satoshi, eth to wei.
     *
     * @param value
     * @return
     * @throws IllegalArgumentException if {@code value} does not contain a valid string representation
     *                                  of a big decimal.
     */
    public static BigInteger main2SmallestUnit(String value, @BlockChain String blockChain) {
        if (blockChain.equals(BITCOIN_CASH) || blockChain.equals(BITCOIN))
            return bitcoin2Satoshi(value);
        else if (blockChain.equals(ETHEREUM))
            return eth2wei(value);
        throw new IllegalArgumentException("unsupported code type:" + blockChain);
    }

    /**
     * Convert smallest to common unit of the digit currency,
     * such as satoshi to bitcoin, wei to eth.
     *
     * @param value
     * @param blockChain
     * @return
     */
    public static BigDecimal smallestUnit2Main(BigInteger value, @BlockChain String blockChain) {
        if (blockChain.equals(BITCOIN_CASH) || blockChain.equals(BITCOIN))
            return satoshi2bitcoin(value);
        else if (blockChain.equals(ETHEREUM))
            return wei2eth(value);
        throw new IllegalArgumentException("unsupported code type:" + blockChain);
    }

    /**
     * Convert smallest to common unit of the digit currency,
     * such as satoshi to bitcoin, wei to eth.
     *
     * @param value
     * @param blockChain
     * @return
     * @throws IllegalArgumentException if {@code value} does not contain a valid string representation
     *                                  of a big decimal.
     */
    public static BigDecimal smallestUnit2Main(String value, @BlockChain String blockChain) {
        if (blockChain.equals(BITCOIN_CASH) || blockChain.equals(BITCOIN))
            return satoshi2bitcoin(value);
        else if (blockChain.equals(ETHEREUM))
            return wei2eth(value);
        throw new IllegalArgumentException("unsupported code type:" + blockChain);
    }

    /**
     * Convert bitcoin to satoshi.
     * To get the value {@link BigInteger#longValue()}
     *
     * @param bitcoinValue
     * @return
     * @throws IllegalArgumentException if {@code bitcoinValue} does not contain a valid string representation
     *                                  of a big decimal.
     */
    public static BigInteger bitcoin2Satoshi(String bitcoinValue) {
        if (bitcoinValue == null || bitcoinValue.isEmpty())
            return BigInteger.ZERO;
        try {
            return new BigDecimal(bitcoinValue).multiply(BITCOIN_SATOSHI).toBigInteger();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert satoshi to bitcoin.
     * To get the String value {@link BigDecimal#toString()}
     *
     * @param satoshiValue
     * @return
     */
    public static BigDecimal satoshi2bitcoin(BigInteger satoshiValue) {
        if (satoshiValue == null)
            return BigDecimal.ZERO;
        return new BigDecimal(satoshiValue.toString()).divide(BITCOIN_SATOSHI);
    }

    /**
     * Convert satoshi
     * Different interval values get different denominations
     *
     * @param satoshiValue
     * @return
     */
    public static String getBitcoinValue(BigInteger satoshiValue) {
        org.bitcoinj.core.Coin coin = org.bitcoinj.core.Coin.valueOf(satoshiValue.longValue());
        BigDecimal bigDecimal = new BigDecimal(satoshiValue);
        if (bigDecimal.compareTo(new BigDecimal("0")) == 0) {
            return BigDecimal.ZERO.toString() + " BTC";
        } else if (coin.compareTo(MICROCOIN) == -1) {//satoshi
            return coin.toString() + " satoshi";
        } else if (coin.compareTo(MICROCOIN) >= 0 && coin.compareTo(MILLICOIN) == -1) {
            return formatBalance(bigDecimal.divide(new BigDecimal(MICROCOIN.value))) + " uBTC";
        } else if (coin.compareTo(MILLICOIN) >= 0 && coin.compareTo(COIN) == -1) {
            return formatBalance(bigDecimal.divide(new BigDecimal(MILLICOIN.value))) + " mBTC";
        } else if (coin.compareTo(COIN) >= 0) {
            return formatBalance(bigDecimal.divide(new BigDecimal(COIN.value))) + " BTC";
        }
        return BigDecimal.ZERO.toString() + " BTC";
    }

    /**
     * Convert wei
     * Different interval values get different denominations
     *
     * @param weiValue
     * @return
     */
    public static String getEthValue(BigInteger weiValue) {
        BigDecimal bigDecimal = new BigDecimal(weiValue);
        if (bigDecimal.compareTo(new BigDecimal("0")) == 0) {
            return BigDecimal.ZERO.toString() + " ETH";
        } else if (bigDecimal.compareTo(new BigDecimal("1000")) == -1) {
            return formatBalance(bigDecimal) + " wei";
        } else if (bigDecimal.compareTo(new BigDecimal("1000")) >= 0 && bigDecimal.compareTo(new BigDecimal("1000000")) == -1) {
            return formatBalance(bigDecimal.divide(new BigDecimal("1000"))) + " Kwei";
        } else if (bigDecimal.compareTo(new BigDecimal("1000000")) >= 0 && bigDecimal.compareTo(new BigDecimal("1000000000")) == -1) {
            return formatBalance(bigDecimal.divide(new BigDecimal("1000000"))) + " Mwei";
        } else if (bigDecimal.compareTo(new BigDecimal("1000000000")) >= 0 && bigDecimal.compareTo(new BigDecimal("1000000000000")) == -1) {
            return formatBalance(bigDecimal.divide(new BigDecimal("1000000000"))) + " Gwei";
        } else if (bigDecimal.compareTo(new BigDecimal("1000000000000")) >= 0) {
            return formatBalance(bigDecimal.divide(new BigDecimal("1000000000000000000"))) + " ETH";
        }
        return BigDecimal.ZERO.toString() + " ETH";
    }


    /**
     * Keep 5 decimal digits
     *
     * @return
     */
    private static String formatBalance(BigDecimal bigDecimal) {
        String balance;
        if (bigDecimal.scale() > 5) {
            balance = bigDecimal.setScale(5, BigDecimal.ROUND_HALF_DOWN).toString();
        } else {
            balance = bigDecimal.toString();
        }
        if (balance.contains(".") && balance.length() > 7) {
            balance = balance.substring(0, 7);
        }
        return balance;
    }

    private static String formatBalanceForEth(BigDecimal bigDecimal) {
        if (bigDecimal.scale() > 5) {
            return bigDecimal.setScale(5, BigDecimal.ROUND_HALF_DOWN).toString();
        } else {
            return bigDecimal.toString();
        }
    }

    public static String formatBalance(BigDecimal bigDecimal, int scale) {
        if (bigDecimal.scale() != 0) {
            return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_DOWN).toString();
        } else {
            return bigDecimal.toString();
        }
    }

    /**
     * Convert satoshi to bitcoin.
     * To get the String value {@link BigDecimal#toString()}
     *
     * @param satoshiValue
     * @return
     * @throws IllegalArgumentException if {@code satoshiValue} does not contain a valid string representation
     *                                  of a big decimal.
     */
    public static BigDecimal satoshi2bitcoin(String satoshiValue) {
        if (satoshiValue == null || satoshiValue.isEmpty())
            return BigDecimal.ZERO;
        try {
            return new BigDecimal(satoshiValue).divide(BITCOIN_SATOSHI);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert eth to wei.
     *
     * @param ethValue
     * @return
     * @throws IllegalArgumentException if {@code ethValue} does not contain a valid string representation
     *                                  of a big decimal.
     */
    public static BigInteger eth2wei(String ethValue) {
        if (ethValue == null || ethValue.isEmpty())
            return BigInteger.ZERO;
        try {
            return new BigDecimal(ethValue).multiply(ETH_WEI).toBigInteger();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert eth to wei.
     * To get the String value {@link BigDecimal#toString()}
     *
     * @param weiValue
     * @return
     */
    public static BigDecimal wei2eth(BigInteger weiValue) {
        if (weiValue == null)
            return BigDecimal.ZERO;
        return new BigDecimal(weiValue.toString()).divide(ETH_WEI);
    }

    /**
     * Convert eth to wei.
     * To get the String value {@link BigDecimal#toString()}
     *
     * @param weiValue
     * @return
     * @throws IllegalArgumentException if {@code weiValue} does not contain a valid string representation
     *                                  of a big decimal.
     */
    public static BigDecimal wei2eth(String weiValue) {
        if (weiValue == null || weiValue.isEmpty())
            return BigDecimal.ZERO;
        try {
            return new BigDecimal(weiValue).divide(ETH_WEI);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getChain(Card card) {
        String chain = "";
        int netWork = card.getCert().getNetWork();
        if (CardUtils.isBTC(card.getCert().getBlockChain())) {
            if (netWork == 0) {
                chain = "Bitcoin";
            } else if (netWork == 1) {
                chain = "Bitcoin Testnet";
            } else {
                chain = "not_support_chain";
            }
        } else {
            if (TextUtils.isEmpty(card.getCert().getTokenAddress())) {
                if (netWork == 1) {
                    chain = "Ethereum";
                } else if (netWork == 3) {
                    chain = "Ethereum Ropsten (Testnet)";
                } else if (netWork == 4) {
                    chain = "Ethereum Rinkeby (Testnet)";
                } else if (netWork == 42) {
                    chain = "Ethereum Kovan (Testnet)";
                } else {
                    chain = "not_support_chain";
                }
            } else {

            }

        }
        return chain;
    }
}

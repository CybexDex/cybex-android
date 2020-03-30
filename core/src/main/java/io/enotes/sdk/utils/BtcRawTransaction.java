package io.enotes.sdk.utils;

import android.text.TextUtils;
import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VarInt;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.RedeemData;
import org.ethereum.util.ByteUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.repository.provider.CardProvider;
import io.enotes.sdk.utils.bch.MoneyNetwork;
import io.enotes.sdk.utils.bch.bitcoincash.BitcoinCashAddressFormatter;

import static io.enotes.sdk.utils.SignatureUtils.str2BtcSignature;
import static org.bitcoinj.core.Utils.int64ToByteStreamLE;
import static org.bitcoinj.core.Utils.reverseBytes;
import static org.bitcoinj.core.Utils.uint32ToByteArrayLE;
import static org.bitcoinj.core.Utils.uint32ToByteStreamLE;
import static org.bitcoinj.core.Utils.uint64ToByteArrayLE;


public class BtcRawTransaction {
    private static final String TAG = "BtcRawTransaction";
    private static final EnumSet<Script.VerifyFlag> MINIMUM_VERIFY_FLAGS = EnumSet.of(Script.VerifyFlag.P2SH,
            Script.VerifyFlag.NULLDUMMY);
    private CardProvider cardProvider;
    private Card card;
    private static NetworkParameters currentBtcNetWork;
    public static final Coin MIN_NONDUST_OUTPUT = Coin.valueOf(546);

    public Transaction createRawTransaction(Card card, CardProvider cardProvider, long fees, String toAddress, long changeCount, String changeAddress, List<EntUtxoEntity> utxos) throws CommandException {
        return createRawTransaction(card, cardProvider, fees, toAddress, changeCount, changeAddress, utxos, "0");
    }

    public Transaction createRawTransaction(Card card, CardProvider cardProvider, long fees, String toAddress, long changeCount, String changeAddress, List<EntUtxoEntity> utxos, String omniValue) throws CommandException {
        this.card = card;
        this.cardProvider = cardProvider;
        if (card.getCert().getNetWork() == Constant.Network.BTC_TESTNET) {
            currentBtcNetWork = TestNet3Params.get();
        } else {
            currentBtcNetWork = MainNetParams.get();
        }

        Transaction transaction = new Transaction(currentBtcNetWork);
        long amount = 0;
        long toCount = 0;
        //get intPut
        for (EntUtxoEntity unSpent : utxos) {
            amount += Long.valueOf(unSpent.getBalance());
            Transaction preTx = new Transaction(currentBtcNetWork);//need a tx for set input
            for (int i = 0; i <= unSpent.getOutput_no(); i++) {
                if (i == unSpent.getOutput_no()) {
                    TransactionOutput transactionOutput = new TransactionOutput(currentBtcNetWork, preTx, Coin.valueOf(Long.valueOf(unSpent.getBalance())), ByteUtil.hexStringToBytes(unSpent.getScript()));
                    preTx.addOutput(transactionOutput);
                } else {
                    TransactionOutput fakeOutput = new TransactionOutput(currentBtcNetWork, preTx, Coin.valueOf(1), new byte[]{});
                    preTx.addOutput(fakeOutput);
                }
            }
            Class<? extends Transaction> aClass1 = preTx.getClass();
            try {
                Method declaredMethod = aClass1.getDeclaredMethod("setHash", Sha256Hash.class);
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(preTx, new Sha256Hash(unSpent.getTxid()));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            transaction.addInput(preTx.getOutput(unSpent.getOutput_no()));
        }
        //create usdt transaction
        if (!TextUtils.isEmpty(card.getCert().getTokenAddress()) && card.getCert().getTokenAddress().equals("31")) {
            return createOmniRawTransaction(transaction, card, amount, fees, toAddress, utxos, omniValue);
        }
        // normal btc
        if (amount == 0) {
            Log.e(TAG, "utxo = 0 ");
        } else if (amount <= fees + changeCount) {
            Log.e(TAG, "your utxo can not spent fees and change");
        }
        toCount = amount - fees - changeCount;
        LogUtils.i(TAG, "\namount=" + amount + "\nfees=" + fees + "\ntoCount=" + toCount + "\nchangeCount=" + changeCount);
        Address toAddr = null;
        if (card.getCert().getBlockChain().equals(Constant.BlockChain.BITCOIN)) {
            toAddr = Address.fromBase58(currentBtcNetWork, toAddress);
        } else if (card.getCert().getBlockChain().equals(Constant.BlockChain.BITCOIN_CASH)) {
            byte[] hash = BitcoinCashAddressFormatter.decodeCashAddress(toAddress, card.getCert().getNetWork() == 0 ? MoneyNetwork.MAIN : MoneyNetwork.TEST).getHash();
            toAddr = Address.fromP2SHHash(currentBtcNetWork, hash);
        }
        if (toAddr == null) {
            throw new CommandException(ErrorCode.SDK_ERROR, "toAddress decode is null");
        }
        TransactionOutput to = new TransactionOutput(currentBtcNetWork, transaction, Coin.valueOf(toCount), toAddr);
        transaction.addOutput(to);
        if (changeCount > 0) {
            Address toChange = null;
            if (card.getCert().getBlockChain().equals(Constant.BlockChain.BITCOIN)) {
                toChange = Address.fromBase58(currentBtcNetWork, changeAddress);
            } else if (card.getCert().getBlockChain().equals(Constant.BlockChain.BITCOIN_CASH)) {
                byte[] hash = BitcoinCashAddressFormatter.decodeCashAddress(changeAddress, card.getCert().getNetWork() == 0 ? MoneyNetwork.MAIN : MoneyNetwork.TEST).getHash();
                toChange = Address.fromP2SHHash(currentBtcNetWork, hash);
            }
            if (toChange == null) {
                throw new CommandException(ErrorCode.SDK_ERROR, "toChange decode is null");
            }
            TransactionOutput change = new TransactionOutput(currentBtcNetWork, transaction, Coin.valueOf(changeCount), toChange);
            transaction.addOutput(change);
        }
        Log.i(TAG, "get no sign tx");
        signTransactionInputs(transaction);
        return transaction;
    }

    public Transaction createOmniRawTransaction(Transaction transaction, Card card, long totalInputAmount, long fees, String toAddress, List<EntUtxoEntity> utxos, String omniValue) throws CommandException {
        Address refAddress = Address.fromBase58(currentBtcNetWork, toAddress);
        if (refAddress != null) {
            transaction.addOutput(Transaction.MIN_NONDUST_OUTPUT, refAddress);                   // Reference (destination) address output
        }

        long amountOut = sum(transaction.getOutputs());
        long amountChange = totalInputAmount - amountOut - fees;

        // If change is negative, transaction is invalid
        if (amountChange < 0) {
            throw new CommandException(ErrorCode.SDK_ERROR, "Insufficient Bitcoin to build Omni Transaction");
        }
        // If change is positive, return it all to the sending address
        if (amountChange > 0) {
            // Add a change output
            transaction.addOutput(Coin.valueOf(amountChange), Address.fromBase58(currentBtcNetWork, card.getAddress()));
        }
        //6f6d6e69 is omni hex
        try {
            String txHex = "6f6d6e69" + createSimpleSendHex(Long.valueOf(card.getCert().getTokenAddress()), Long.valueOf(omniValue));
            byte[] payload = hexToBinary(txHex);
            Script opReturnScript = ScriptBuilder.createOpReturnScript(payload);
            TransactionOutput output = new TransactionOutput(currentBtcNetWork, null, Coin.ZERO, opReturnScript.getProgram());
            transaction.addOutput(output);// add omni output for simple send
        } catch (Exception e) {
            throw new CommandException(ErrorCode.SDK_ERROR, "create omni transaction fail");
        }
        signTransactionInputs(transaction);
        return transaction;
    }

    /**
     * Calculate the total value of a collection of transaction outputs.
     *
     * @param outputs list of transaction outputs to total
     * @return total value in satoshis
     */
    private long sum(Collection<TransactionOutput> outputs) {
        long sum = 0;
        for (TransactionOutput output : outputs) {
            sum += output.getValue().value;
        }
        return sum;
    }

    /**
     * Creates a hex-encoded raw transaction of type 0: "simple send".
     *
     * @param currencyId currency ID to send
     * @param amount     amount to send
     * @return Hex encoded string for the transaction
     */
    public String createSimpleSendHex(long currencyId, long amount) {
        String rawTxHex = String.format("00000000%08x%016x", currencyId, amount);
        return rawTxHex;
    }

    /**
     * Convert a hexadecimal string representation of binary data
     * to byte array.
     *
     * @param hex Hexadecimal string
     * @return binary data
     */
    static byte[] hexToBinary(String hex) {
        int length = hex.length();
        byte[] bin = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            bin[i / 2] = (byte) ((hi << 4) + lo);
        }
        return bin;
    }


    private void signTransactionInputs(Transaction tx) throws CommandException {
        if (tx == null)
            throw new CommandException(ErrorCode.SDK_ERROR, "transaction is null");
        String pubKey = card.getCurrencyPubKey();
        ECKey ecKey = ECKey.fromPublicOnly(ByteUtil.hexStringToBytes(pubKey));
        KeyChainGroup keyChainGroup = new KeyChainGroup(currentBtcNetWork);
        keyChainGroup.importKeys(ecKey);
        int numInputs = tx.getInputs().size();
        for (int i = 0; i < numInputs; i++) {
            TransactionInput txIn = tx.getInput(i);
            if (txIn.getConnectedOutput() == null) {
                continue;
            }

            try {
                txIn.getScriptSig().correctlySpends(tx, i, txIn.getConnectedOutput().getScriptPubKey());
                continue;
            } catch (ScriptException e) {
//                Log.e("test", e.getMessage());
            }

            Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
            RedeemData redeemData = txIn.getConnectedRedeemData(keyChainGroup);
            txIn.setScriptSig(scriptPubKey.createEmptyInputScript(redeemData.keys.get(0), redeemData.redeemScript));
        }
        TransactionSigner.ProposedTransaction proposal = new TransactionSigner.ProposedTransaction(tx);
        signInputs(proposal, keyChainGroup);
    }

    private boolean signInputs(TransactionSigner.ProposedTransaction propTx, KeyBag keyBag) throws CommandException {
        Log.i(TAG, " start signInputs");
        Transaction tx = propTx.partialTx;
        int numInputs = tx.getInputs().size();
        for (int i = 0; i < numInputs; i++) {
            TransactionInput txIn = tx.getInput(i);
            if (txIn.getConnectedOutput() == null) {
                continue;
            }

            try {
                txIn.getScriptSig().correctlySpends(tx, i, txIn.getConnectedOutput().getScriptPubKey(), MINIMUM_VERIFY_FLAGS);
                continue;
            } catch (ScriptException e) {
                // Expected.
            }

            RedeemData redeemData = txIn.getConnectedRedeemData(keyBag);
            Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
            ECKey pubKey = redeemData.keys.get(0);
            if (pubKey instanceof DeterministicKey)
                propTx.keyPaths.put(scriptPubKey, (((DeterministicKey) pubKey).getPath()));


            Script inputScript = txIn.getScriptSig();
            byte[] script = redeemData.redeemScript.getProgram();
            try {
                TransactionSignature signature;
                if (card.getCert().getBlockChain().equals(Constant.BlockChain.BITCOIN_CASH)) {
                    signature = calculateBCHSignature(tx, i, script);
                } else {
                    signature = calculateSignature(tx, i, script, Transaction.SigHash.ALL, false);
                }

                int sigIndex = 0;
                inputScript = scriptPubKey.getScriptSigWithSignature(inputScript, signature.encodeToBitcoin(), sigIndex);
                txIn.setScriptSig(inputScript);
            } catch (ECKey.KeyIsEncryptedException e) {
                throw e;
            } catch (ECKey.MissingPrivateKeyException e) {
            }

        }
        return true;
    }

    private TransactionSignature calculateSignature(Transaction transaction, int inputIndex,
                                                    byte[] redeemScript,
                                                    Transaction.SigHash hashType, boolean anyoneCanPay) throws CommandException {
        Sha256Hash hash = hashForSignature(transaction, inputIndex, redeemScript, hashType, anyoneCanPay);
        return new TransactionSignature(getECDSASignature(hash.getBytes()), hashType, anyoneCanPay);
    }

    private TransactionSignature calculateBCHSignature(Transaction transaction, int inputIndex,
                                                       byte[] redeemScript) throws CommandException {
        Sha256Hash hash = hashForBCHSignature(transaction, inputIndex, redeemScript);
        ECKey.ECDSASignature ecdsaSignature = getECDSASignature(hash.getBytes());
        TransactionSignature transactionSignature = new TransactionSignature(ecdsaSignature.r, ecdsaSignature.s, 0x41);
        return transactionSignature;
    }


    private ECKey.ECDSASignature getECDSASignature(byte[] transHash) throws CommandException {
        if (cardProvider != null && card != null) {
            String signature = cardProvider.verifyCoinAndSignTx(card, transHash);
            LogUtils.i(TAG, "sig_der=:\n" + signature);
            return str2BtcSignature(signature);
        } else {
            throw new CommandException(ErrorCode.SDK_ERROR, "card or cardManager id null");
        }
    }

    private Sha256Hash hashForSignature(Transaction transaction, int inputIndex, byte[] redeemScript,
                                        Transaction.SigHash type, boolean anyoneCanPay) {
        byte sigHashType = (byte) TransactionSignature.calcSigHashValue(type, anyoneCanPay);
        return hashForSignature(transaction, inputIndex, redeemScript, sigHashType);
    }

    private Sha256Hash hashForSignature(Transaction trans, int inputIndex, byte[] connectedScript, byte sigHashType) {

        try {
            Transaction tx = currentBtcNetWork.getDefaultSerializer().makeTransaction(trans.bitcoinSerialize());
            Class<? extends Transaction> inputClass = tx.getClass();
            // 获取Method对象
            Method method = null;
            int length = 0;
            Field lengthF = inputClass.getSuperclass().getSuperclass().getDeclaredField("length");
            lengthF.setAccessible(true);
            length = (int) lengthF.get(tx);

            for (int i = 0; i < getInputs(tx).size(); ++i) {
                ((TransactionInput) getInputs(tx).get(i)).clearScriptBytes();
            }

            connectedScript = Script.removeAllInstancesOfOp(connectedScript, 171);
            TransactionInput input = (TransactionInput) getInputs(tx).get(inputIndex);
            method = input.getClass().getDeclaredMethod("setScriptBytes",
                    byte[].class);
            method.setAccessible(true); // 抑制Java的访问控制检查
            method.invoke(input, new Object[]{connectedScript});
            int i;
            if ((sigHashType & 31) == Transaction.SigHash.NONE.value) {
                setOutPuts(tx, new ArrayList(0));

                for (i = 0; i < getInputs(tx).size(); ++i) {
                    if (i != inputIndex) {
                        ((TransactionInput) getInputs(tx).get(i)).setSequenceNumber(0L);
                    }
                }
            } else if ((sigHashType & 31) == Transaction.SigHash.SINGLE.value) {
                if (inputIndex >= getOutputs(tx).size()) {
                    return Sha256Hash.wrap("0100000000000000000000000000000000000000000000000000000000000000");
                }

                setOutPuts(tx, new ArrayList(getOutputs(tx).subList(0, inputIndex + 1)));

                for (i = 0; i < inputIndex; ++i) {
                    getOutputs(tx).set(i, new TransactionOutput(currentBtcNetWork, tx, Coin.NEGATIVE_SATOSHI, new byte[0]));
                }

                for (i = 0; i < getInputs(tx).size(); ++i) {
                    if (i != inputIndex) {
                        ((TransactionInput) getInputs(tx).get(i)).setSequenceNumber(0L);
                    }
                }
            }

            if ((sigHashType & Transaction.SigHash.ANYONECANPAY.value) == Transaction.SigHash.ANYONECANPAY.value) {
                setInPuts(tx, new ArrayList());
                getInputs(tx).add(input);
            }

            ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(length == -2147483648 ? 256 : length + 4);
            tx.bitcoinSerialize(bos);
            Utils.uint32ToByteStreamLE((long) (255 & sigHashType), bos);
            LogUtils.i(TAG, "Serialize3=\n" + ByteUtil.toHexString(bos.toByteArray()));
            Sha256Hash hash = Sha256Hash.twiceOf(bos.toByteArray());
            bos.close();
            return hash;
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * sign bch transaction
     * need set hash type is 4
     *
     * @param transaction not sign transaction
     */
    private Sha256Hash hashForBCHSignature(Transaction transaction, int index, byte[] script) throws CommandException {
        StringBuffer sbPrevouts = new StringBuffer();
        StringBuffer sbSequence = new StringBuffer();
        String outpoint = "";
        long value = 0;
        String sequence = "";
        for (int i = 0; i < transaction.getInputs().size(); i++) {
            TransactionInput input = transaction.getInput(i);
            sbPrevouts.append(ByteUtil.toHexString(input.getOutpoint().unsafeBitcoinSerialize()));
            sbSequence.append(new BigInteger(input.getSequenceNumber() + "").toString(16));
            if (i == index) {
                outpoint = ByteUtil.toHexString(input.getOutpoint().unsafeBitcoinSerialize());
                value = input.getValue().value;
                sequence = new BigInteger(input.getSequenceNumber() + "").toString(16);
            }
//            //txId
//            LogUtils.i(TAG, "txID +index = " + ByteUtil.toHexString(input.getOutpoint().unsafeBitcoinSerialize()));
//            //Sequence
//            LogUtils.i(TAG, "Sequence = " + new BigInteger(input.getSequenceNumber() + "").toString(16));
//            //script
//            LogUtils.i(TAG, "Script = " + ByteUtil.toHexString(script));
        }


        Sha256Hash hashPrevouts = Sha256Hash.twiceOf(ByteUtil.hexStringToBytes(sbPrevouts.toString()));
        Sha256Hash hashSequence = Sha256Hash.twiceOf(ByteUtil.hexStringToBytes(sbSequence.toString()));

        ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(1024);
        StringBuffer sbSignature = new StringBuffer();
        try {
            //version
            byte[] bVersion = new byte[4];
            uint32ToByteArrayLE(transaction.getVersion(), bVersion, 0);
            String versionHex = ByteUtil.toHexString(bVersion);
            LogUtils.i(TAG, "version = " + versionHex);
            LogUtils.i(TAG, "sbPrevouts = " + sbPrevouts.toString());
            LogUtils.i(TAG, "hashPrevouts = " + hashPrevouts.toString());
            LogUtils.i(TAG, "sbSequence = " + hashSequence.toString());
            LogUtils.i(TAG, "hashSequence = " + hashSequence.toString());
            LogUtils.i(TAG, "outpoint = " + outpoint);
            String scriptCode = ByteUtil.toHexString(new VarInt(script.length).encode()) + ByteUtil.toHexString(script);
            LogUtils.i(TAG, "script = " + scriptCode);
            byte[] bValue = new byte[8];
            uint64ToByteArrayLE(value, bValue, 0);
            String valueHex = ByteUtil.toHexString(bValue);
            LogUtils.i(TAG, "value = " + valueHex);
            LogUtils.i(TAG, "sequence = " + sequence);

            StringBuffer sbOutput = new StringBuffer();
            for (TransactionOutput out : transaction.getOutputs()) {
                byte[] bValueOutput = new byte[8];
                uint64ToByteArrayLE(out.getValue().value, bValueOutput, 0);
                String valueHexOutput = ByteUtil.toHexString(bValueOutput);
                String scriptOutput = ByteUtil.toHexString(new VarInt(out.getScriptBytes().length).encode()) + ByteUtil.toHexString(out.getScriptBytes());
                sbOutput.append(valueHexOutput);
                sbOutput.append(scriptOutput);
            }
            LogUtils.i(TAG, "sbOutput = " + sbOutput);
            Sha256Hash hashOutput = Sha256Hash.twiceOf(ByteUtil.hexStringToBytes(sbOutput.toString()));
            LogUtils.i(TAG, "hashOutput = " + hashOutput.toString());

            byte[] bLockTime = new byte[4];
            uint32ToByteArrayLE(transaction.getLockTime(), bLockTime, 0);
            String lockTime = ByteUtil.toHexString(bLockTime);
            LogUtils.i(TAG, "lockTime = " + lockTime);

            byte[] bHashType = new byte[4];
            uint32ToByteArrayLE(0x41, bHashType, 0);
            String hashTypeString = ByteUtil.toHexString(bHashType);
            LogUtils.i(TAG, "hashTypeString = " + hashTypeString);

            sbSignature.append(versionHex).append(hashPrevouts.toString()).append(hashSequence.toString()).append(outpoint).append(scriptCode).append(valueHex).append(sequence).append(hashOutput.toString()).append(lockTime).append(hashTypeString);
            LogUtils.i(TAG, "sbSignature = " + sbSignature.toString());
            Sha256Hash hashSignature = Sha256Hash.twiceOf(ByteUtil.hexStringToBytes(sbSignature.toString()));
            LogUtils.i(TAG, "hashSignature = " + hashSignature.toString());
            return hashSignature;


        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(ErrorCode.SDK_ERROR, "calculateBCHTransactionInputs error");
        }
    }

    private ArrayList<TransactionOutput> getOutputs(Transaction tx) {
        Class<? extends Transaction> txClass = tx.getClass();
        try {
            Field outputsF = txClass.getDeclaredField("outputs");
            outputsF.setAccessible(true);
            return (ArrayList<TransactionOutput>) outputsF.get(tx);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<TransactionInput> getInputs(Transaction tx) {
        Class<? extends Transaction> txClass = tx.getClass();
        try {
            Field outputsF = txClass.getDeclaredField("inputs");
            outputsF.setAccessible(true);
            return (ArrayList<TransactionInput>) outputsF.get(tx);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setOutPuts(Transaction tx, ArrayList<TransactionOutput> outputs) {
        Class<? extends Transaction> txClass = tx.getClass();
        try {
            Field outputsF = txClass.getDeclaredField("outputs");
            outputsF.setAccessible(true);
            outputsF.set(tx, outputs);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setInPuts(Transaction tx, ArrayList<TransactionInput> outputs) {
        Class<? extends Transaction> txClass = tx.getClass();
        try {
            Field outputsF = txClass.getDeclaredField("inputs");
            outputsF.setAccessible(true);
            outputsF.set(tx, outputs);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}

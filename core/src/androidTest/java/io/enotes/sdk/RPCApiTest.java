package io.enotes.sdk;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.core.ENotesSDK;
import io.enotes.sdk.repository.ProviderFactory;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.repository.api.entity.EntCallEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;
import io.enotes.sdk.repository.api.entity.EntExchangeRateEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;
import io.enotes.sdk.repository.api.entity.EntGasEntity;
import io.enotes.sdk.repository.api.entity.EntGasPriceEntity;
import io.enotes.sdk.repository.api.entity.EntNonceEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;
import io.enotes.sdk.repository.api.entity.EntTransactionEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.repository.provider.ApiProvider;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RPCApiTest {
    private static final String TAG = "RPCApiTest";
    private static final String BTC_ADDRESS = "2N5rJVCfwz2X8iiYmPqKsuczohSwptrVxLk";
    private static final String ETH_ADDRESS = "0xC954D9BC070Ba97Bb80E0095ad2CC3A037906540";
    private static final String XRP_ADDRESS = "rBT5h55iftNBwnFA1CsvtHuo7LgXfZYabE";
    private static final String BTC_TXID = "7f827d4a3ae3b6e408fa1737f12f9dbfa2bb8fd79e0e3e63256a6e78310790a4";
    private static final String ETH_TXID = "0xa4bcfc7d1dd2db21df8d74da00fb865775033b4b94821a45a3dd94aacf3cda4f";
    private static final String XRP_TXID = "535B7E155FF7023A61886FC5D7C4FF62753FC710DCD2CA5BDB5755CC17DF165D";
    private static final String BTC_TX_HEX = "01000000014f24f52acae45d359aceae0f1bbf24fe9ca513a86de5e8553c2f897bd9fc219c01000000db0047304402200664e0949a562c306255fae70bc01cbee91bd3ef952b1cbc8c75fedb89ecc20c02205394211dc96c35124d4576e01ca619c14e33c876ab13f3754de8f8931a23383e01493046022100faab17fd8eaae4e3c8ec87ab003290fcc3691654f2c54fe1ab2e69bf6f797165022100db9f47d68358781f5de520240c9c7b97d7a3ce221ba8638e04ec3a78d9ecd9e30147522102632178d046673c9729d828cfee388e121f497707f810c131e0d3fc0fe0bd66d62103a0951ec7d3a9da9de171617026442fcd30f34d66100fab539853b43f508787d452aeffffffff0240420f000000000017a9145e785f3cb8254f81d3fdfa14e69d3b9bbe95ea678730ca09c90000000017a9148ce5408cfeaddb7ccb2545ded41ef478109454848700000000";
    private static final String ETH_TX_HEX = "0xf86d827317843b9aca0082520894c954d9bc070ba97bb80e0095ad2cc3a037906540880de0b6b3a76400008078a064cf0d65d6c6949dc45768ea9869cd4a370de8437b9be07067b6c5ac0ea85cc6a03f0caf399c067bf91e3f4c4433e089c392bc6a61e0532568b68782f599088001";
    private static final String XRP_TX_HEX = "120000228000000024000000016140000000000186A0684000000000000064732103385769B7ADA42823FB63CB11E4CD25508F687A037784E3779493117DFE5D22027446304402206BFD90D68BE256BD205F17721A1DBE924F9D8AF8D566630E1C925F05DB9410DF022073A2C9CA4C75C0C4A565AC991BDF43DCA685498EB98EF098ED89CD147D264E9E81144E030C394228D6A8B023BE1343AFF3CCE3728BC48314D77DB6E137B829BF596CEA8EB8A71932554CE1E4";
    private static final String CALL_DATA = "0x95c6fa61f319d9523579074cf82dd04de07d42fa5dede2ddccd53713da7d06a44f1a87be0e2c9aa962262100508a2a2ff6665bcd9d731eea5bb62d15caed02c55da9991b";
    private static final String CONTRACT_ADDRESS = "0x5C036d8490127ED26E3A142024082eaEE482BbA2";
    private ApiProvider apiProvider;

    public RPCApiTest() {
        ENotesSDK.config.debugCard=true;
        ENotesSDK.config.isRequestENotesServer=true;
        apiProvider = ProviderFactory.getInstance(null).getApiProvider();
    }

    ///////////////////Universal//////////////////////

    @Test
    public void testBalanceOfBtc() {
        Resource<EntBalanceEntity> balance = getValue(apiProvider.getBalance(Constant.BlockChain.BITCOIN, Constant.Network.BTC_TESTNET, BTC_ADDRESS));
        assertTrue(balance != null);
        assertTrue(balance.status == Status.SUCCESS);
        assertNotNull(balance.data);
        Log.i(TAG, "btc balance = " + balance.data.getBalance());
    }

    @Test
    public void testBalanceOfEth() {
        Resource<EntBalanceEntity> balance = getValue(apiProvider.getBalance(Constant.BlockChain.ETHEREUM, Constant.Network.ETH_KOVAN, ETH_ADDRESS));
        assertTrue(balance != null);
        assertTrue(balance.status == Status.SUCCESS);
        assertNotNull(balance.data);
        Log.i(TAG, "eth balance = " + balance.data.getBalance());
    }

    @Test
    public void testBalanceOfXrp() {
        Resource<EntBalanceEntity> balance = getValue(apiProvider.getBalance(Constant.BlockChain.RIPPLE, Constant.Network.BTC_MAINNET, XRP_ADDRESS));
        assertTrue(balance != null);
        assertTrue(balance.status == Status.SUCCESS);
        assertNotNull(balance.data);
        Log.i(TAG, "xrp balance = " + balance.data.getBalance() + " sequence = " + balance.data.getSequence());
    }

    @Test
    public void testTransactionReceiptForBtc() {
        Resource<EntConfirmedEntity> confirmed = getValue(apiProvider.getTransactionReceipt(Constant.BlockChain.BITCOIN, Constant.Network.BTC_TESTNET, BTC_TXID));
        assertTrue(confirmed != null);
        assertTrue(confirmed.status == Status.SUCCESS);
        assertNotNull(confirmed.data);
        Log.i(TAG, "btc transaction receipt = " + confirmed.data.getConfirmations());

    }

    @Test
    public void testTransactionReceiptForEth() {
        Resource<EntConfirmedEntity> confirmed = getValue(apiProvider.getTransactionReceipt(Constant.BlockChain.ETHEREUM, Constant.Network.ETH_KOVAN, ETH_TXID));
        assertTrue(confirmed != null);
        assertTrue(confirmed.status == Status.SUCCESS);
        assertNotNull(confirmed.data);
        Log.i(TAG, "eth transaction receipt = " + confirmed.data.getConfirmations());

    }

    @Test
    public void testTransactionReceiptForXrp() {
        Resource<EntConfirmedEntity> confirmed = getValue(apiProvider.getTransactionReceipt(Constant.BlockChain.RIPPLE, Constant.Network.BTC_TESTNET, XRP_TXID));
        assertTrue(confirmed != null);
        assertTrue(confirmed.status == Status.SUCCESS);
        assertNotNull(confirmed.data);
        Log.i(TAG, "xrp transaction receipt = " + confirmed.data.getConfirmations());

    }

    @Test
    public void testSendRawTransactionForBtc() {
        Resource<EntSendTxEntity> entity = getValue(apiProvider.sendRawTransaction(Constant.BlockChain.BITCOIN, Constant.Network.BTC_TESTNET, BTC_TX_HEX));
        assertTrue(entity != null);
        if (entity.status == Status.SUCCESS) {
            assertNotNull(entity.data);
            Log.i(TAG, "btc sendRawTransaction id = " + entity.data.getTxid());
        } else {
            assertNotNull(entity.message);
            Log.i(TAG, "btc sendRawTransaction error = " + entity.message);
        }

    }

    @Test
    public void testSendRawTransactionForEth() {
        Resource<EntSendTxEntity> entity = getValue(apiProvider.sendRawTransaction(Constant.BlockChain.ETHEREUM, Constant.Network.ETH_KOVAN, ETH_TX_HEX));
        assertTrue(entity != null);
        if (entity.status == Status.SUCCESS) {
            assertNotNull(entity.data);
            Log.i(TAG, "eth sendRawTransaction id = " + entity.data.getTxid());
        } else {
            assertNotNull(entity.message);
            Log.i(TAG, "eth sendRawTransaction error = " + entity.message);
        }

    }

    @Test
    public void testSendRawTransactionForXrp() {
        Resource<EntSendTxEntity> entity = getValue(apiProvider.sendRawTransaction(Constant.BlockChain.RIPPLE, Constant.Network.BTC_TESTNET, XRP_TX_HEX));
        assertTrue(entity != null);
        if (entity.status == Status.SUCCESS) {
            assertNotNull(entity.data);
            Log.i(TAG, "xrp sendRawTransaction id = " + entity.data.getTxid());
        } else {
            assertNotNull(entity.message);
            Log.i(TAG, "xrp sendRawTransaction error = " + entity.message);
        }

    }

    ///////////////////Bitcoin//////////////////////
    @Test
    public void testEstimateFee() {
        Resource<EntFeesEntity> entity = getValue(apiProvider.estimateFee(Constant.BlockChain.BITCOIN, Constant.Network.BTC_TESTNET));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "btc estimate fees = " + entity.data.getFast());
    }

    @Test
    public void testUnSpend() {
        Resource<List<EntUtxoEntity>> entity = getValue(apiProvider.getUnSpend(Constant.BlockChain.BITCOIN, Constant.Network.BTC_TESTNET, BTC_ADDRESS));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "btc utxo size = " + entity.data.size());
    }

    ///////////////////Ethereum//////////////////////
    @Test
    public void testEstimateGas() {
        Resource<EntGasEntity> entity = getValue(apiProvider.estimateGas(Constant.Network.ETH_KOVAN, ETH_ADDRESS, ETH_ADDRESS, "10000000000000000", "0", null));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "eth estimate gas = " + entity.data.getGas());
    }

    @Test
    public void testGasPrice() {
        Resource<EntGasPriceEntity> entity = getValue(apiProvider.getGasPrice(Constant.Network.ETH_KOVAN));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "eth gas price = " + entity.data.getFast());
    }

    @Test
    public void testEstimateXrpFee() {
        Resource<EntFeesEntity> entity = getValue(apiProvider.estimateFee(Constant.BlockChain.RIPPLE, Constant.Network.BTC_TESTNET));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "xrp estimate fees = " + entity.data.getFast());
    }

    @Test
    public void testNonce() {
        Resource<EntNonceEntity> entity = getValue(apiProvider.getNonce(Constant.Network.ETH_KOVAN, ETH_ADDRESS));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "eth nonce = " + entity.data.getNonce());
    }

    @Test
    public void testCall() {
        Resource<EntCallEntity> entity = getValue(apiProvider.call(Constant.Network.ETH_KOVAN, CONTRACT_ADDRESS, CALL_DATA));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "eth call = " + entity.data.getResult());
    }

    @Test
    public void testBtcTransactionList() {
        Resource<List<EntTransactionEntity>> entity = getValue(apiProvider.getTransactionList(Constant.BlockChain.BITCOIN, Constant.Network.BTC_TESTNET, BTC_ADDRESS,""));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        for (EntTransactionEntity entTransactionEntity : entity.data) {
            Log.i(TAG, "TransactionList -> \n" + entTransactionEntity.toString());
        }
    }

    @Test
    public void testEthTransactionList() {
        Resource<List<EntTransactionEntity>> entity = getValue(apiProvider.getTransactionList(Constant.BlockChain.ETHEREUM, Constant.Network.ETH_KOVAN, ETH_ADDRESS,""));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        for (EntTransactionEntity entTransactionEntity : entity.data) {
            Log.i(TAG, "TransactionList -> \n" + entTransactionEntity.toString());
        }
    }

    @Test
    public void testXrpTransactionList() {
        Resource<List<EntTransactionEntity>> entity = getValue(apiProvider.getTransactionList(Constant.BlockChain.RIPPLE, Constant.Network.BTC_TESTNET, XRP_ADDRESS,""));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        for (EntTransactionEntity entTransactionEntity : entity.data) {
            Log.i(TAG, "TransactionList -> \n" + entTransactionEntity.toString());
        }
    }

    @Test
    public void testEthTokenTransactionList() {
        Resource<List<EntTransactionEntity>> entity = getValue(apiProvider.getTransactionList(Constant.BlockChain.ETHEREUM, Constant.Network.ETH_KOVAN, "0x4013F07264c31A4B0303B05eA5a6eBD08dC919a0","0x0F57219668B6B82f2a846fc84BBD2c7D4ceA3B1b"));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        for (EntTransactionEntity entTransactionEntity : entity.data) {
            Log.i(TAG, "TransactionList -> \n" + entTransactionEntity.toString());
        }
    }

    @Test
    public void testOmniBalance() {
        Resource<EntBalanceEntity> entity = getValue(apiProvider.getOmniBalance(Constant.Network.BTC_MAINNET, "12aGZoKeX2pryP7ywyNFFfLiaUp1KyfZzD","31"));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "testOmniBalance -> \n" + entity.data.getBalance().toString());
    }

    private static <T> T getValue(LiveData<T> liveData) {
        try {
            final Object[] objects = new Object[1];
            final CountDownLatch latch = new CountDownLatch(1);

            Observer observer = new Observer() {
                @Override
                public void onChanged(@Nullable Object o) {
                    objects[0] = o;
                    latch.countDown();
                    liveData.removeObserver(this);
                }
            };
            liveData.observeForever(observer);
            latch.await();
            return (T) objects[0];
        } catch (InterruptedException e) {
            return null;
        }
    }
}

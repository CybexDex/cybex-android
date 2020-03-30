package io.enotes.sdk;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.Status;
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
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.repository.provider.ApiProvider;
import io.enotes.sdk.repository.provider.api.ExchangeRateApiProvider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExChangeRateApiTest {
    private static final String TAG = "ExChangeRateApiTest";
    private ApiProvider apiProvider;

    public ExChangeRateApiTest() {
        apiProvider = ProviderFactory.getInstance(null).getApiProvider();
    }

    @Test
    public void testBTCExchangeRate() {
        Resource<EntExchangeRateEntity> entity = getValue(apiProvider.getExchangeRate(Constant.CardType.BTC));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "exchangeRate = \n" + entity.data.toString());
    }

    @Test
    public void testETHExchangeRate() {
        Resource<EntExchangeRateEntity> entity = getValue(apiProvider.getExchangeRate(Constant.CardType.ETH));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "exchangeRate = \n" + entity.data.toString());
    }

    @Test
    public void testGUSDExchangeRate() {
        Resource<EntExchangeRateEntity> entity = getValue(apiProvider.getExchangeRate(Constant.CardType.GUSD));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "exchangeRate = \n" + entity.data.toString());
    }

    @Test
    public void testBCHExchangeRate() {
        Resource<EntExchangeRateEntity> entity = getValue(apiProvider.getExchangeRate(Constant.CardType.BCH));
        assertTrue(entity != null);
        assertTrue(entity.status == Status.SUCCESS);
        assertNotNull(entity.data);
        Log.i(TAG, "exchangeRate = \n" + entity.data.toString());
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

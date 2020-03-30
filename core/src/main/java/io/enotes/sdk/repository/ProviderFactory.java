package io.enotes.sdk.repository;


import android.content.Context;

import io.enotes.sdk.repository.card.ICardReader;
import io.enotes.sdk.repository.card.ICardScanner;
import io.enotes.sdk.repository.provider.ApiProvider;
import io.enotes.sdk.repository.provider.CardProvider;

public class ProviderFactory {
    private static ProviderFactory providerFactory;
    private CardProvider cardProvider;
    private ApiProvider apiProvider;
    private Context context;

    private ProviderFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    public static ProviderFactory getInstance(Context context) {
        if (providerFactory == null) providerFactory = new ProviderFactory(context);
        return providerFactory;
    }

    public CardProvider getCardProvider() {
        if (cardProvider == null) {
            cardProvider = new CardProvider(context);
            cardProvider.setScanReadMode(ICardScanner.BOTH_MODE, ICardReader.BOTH_MANUAL_AUTO_MODE);
        }
        return cardProvider;
    }

    public ApiProvider getApiProvider() {
        if (apiProvider == null) apiProvider = new ApiProvider(context);
        return apiProvider;
    }

}

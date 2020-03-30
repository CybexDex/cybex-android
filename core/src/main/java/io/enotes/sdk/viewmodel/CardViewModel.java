package io.enotes.sdk.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import io.enotes.sdk.repository.ProviderFactory;
import io.enotes.sdk.repository.provider.CardProvider;

public class CardViewModel extends AndroidViewModel {
    private CardProvider cardProvider;

    public CardViewModel(@NonNull Application application) {
        super(application);
        cardProvider = ProviderFactory.getInstance(application).getCardProvider();
    }

    public CardProvider getCardProvider() {
        return cardProvider;
    }
}

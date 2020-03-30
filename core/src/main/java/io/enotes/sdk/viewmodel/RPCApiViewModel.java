package io.enotes.sdk.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import io.enotes.sdk.repository.ProviderFactory;
import io.enotes.sdk.repository.provider.ApiProvider;

public class RPCApiViewModel extends AndroidViewModel{
    private ApiProvider apiProvider;
    public RPCApiViewModel(@NonNull Application application) {
        super(application);
        apiProvider = ProviderFactory.getInstance(application).getApiProvider();
    }

    public ApiProvider getApiProvider() {
        return apiProvider;
    }
}

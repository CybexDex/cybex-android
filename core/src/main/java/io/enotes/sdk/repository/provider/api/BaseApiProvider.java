package io.enotes.sdk.repository.provider.api;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Context;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.core.ENotesSDK;
import io.enotes.sdk.repository.api.ApiResponse;
import io.enotes.sdk.repository.api.entity.BaseENotesEntity;
import io.enotes.sdk.repository.api.entity.BaseEthEntity;
import io.enotes.sdk.repository.api.entity.ResponseEntity;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.LogUtils;
import io.enotes.sdk.utils.Utils;


public abstract class BaseApiProvider {
    protected static final String TAG = "ApiProvider";
    private Context context;

    public BaseApiProvider(Context context) {
        this.context = context;
    }

    /**
     * add source in order
     */
    public <T> MediatorLiveData<Resource<T>> addLiveDataSource(LiveData<Resource<T>>... source) {
        MediatorLiveData<Resource<T>> mutableLiveData = new MediatorLiveData<>();
        List<LiveData<Resource<T>>> sourceList = new ArrayList<>();
        for (LiveData<Resource<T>> liveData : source) {
            sourceList.add(liveData);
        }
        //remove eNotes server
        if (!ENotesSDK.config.isRequestENotesServer)
            sourceList.remove(sourceList.size() - 1);
        recurLiveDataSource(mutableLiveData, sourceList);
        return mutableLiveData;
    }

    /**
     * add source in order
     */
    public <T> MediatorLiveData<Resource<T>> addLiveDataSourceNoENotes(LiveData<Resource<T>>... source) {
        MediatorLiveData<Resource<T>> mutableLiveData = new MediatorLiveData<>();
        List<LiveData<Resource<T>>> sourceList = new ArrayList<>();
        for (LiveData<Resource<T>> liveData : source) {
            sourceList.add(liveData);
        }
        recurLiveDataSource(mutableLiveData, sourceList);
        return mutableLiveData;
    }

    /**
     * recur source
     */
    private <T> void recurLiveDataSource(MediatorLiveData<Resource<T>> mutableLiveData, List<LiveData<Resource<T>>> sourceList) {
        if (context != null && !Utils.isNetworkConnected(context)) {
            mutableLiveData.postValue(Resource.error(ErrorCode.NET_UNAVAILABLE, "net unavailable"));
            return;
        }
        if (sourceList != null && sourceList.size() > 0) {
            mutableLiveData.addSource(sourceList.get(0), (newData) -> {
                if (newData.status == Status.SUCCESS) {
                    mutableLiveData.postValue(newData);
                } else {
                    if (sourceList.size() == 1) {
                        LogUtils.e(TAG, newData.message);
                        handlerErrorLiveData(mutableLiveData, newData.message);
                    } else {
                        sourceList.remove(0);
                        LogUtils.e(TAG, newData.message);
                        recurLiveDataSource(mutableLiveData, sourceList);
                    }
                }
            });
        }
    }

    /**
     * add source in random
     */
    public <T> MediatorLiveData<Resource<T>> addLiveDataSourceRandom(LiveData<Resource<T>>... source) {
        MediatorLiveData<Resource<T>> mutableLiveData = new MediatorLiveData<>();
        List<LiveData<Resource<T>>> sourceList = new ArrayList<>();
        for (LiveData<Resource<T>> liveData : source) {
            sourceList.add(liveData);
        }
        //remove eNotes server
        if (!ENotesSDK.config.isRequestENotesServer)
            sourceList.remove(sourceList.size() - 1);
        recurLiveDataSourceRandom(mutableLiveData, sourceList);
        return mutableLiveData;
    }

    /**
     * recur source
     */
    private <T> void recurLiveDataSourceRandom(MediatorLiveData<Resource<T>> mutableLiveData, List<LiveData<Resource<T>>> sourceList) {
        if (sourceList != null && sourceList.size() > 0) {
            LiveData<Resource<T>> sourceLiveData = null;
            if (sourceList.size() == 1) {
                sourceLiveData = sourceList.get(0);
            } else {
                int r = new Random().nextInt(100) % (sourceList.size() - 1);

                sourceLiveData = sourceList.get(r);
            }
            LiveData<Resource<T>> finalSourceLiveData = sourceLiveData;
            mutableLiveData.addSource(sourceLiveData, (newData) -> {
                if (newData != null && newData.status == Status.SUCCESS) {
                    mutableLiveData.postValue(newData);
                } else {
//                    reportBug2Server(sourceList.size() == 1 ? true : false, newData.data == null ? "" : newData.data.getClass().getName(), newData.message);
                    String message = newData == null ? "" : newData.message;
                    if (sourceList.size() == 1) {
                        LogUtils.e(TAG, message);
                        handlerErrorLiveData(mutableLiveData, message);
                    } else {
                        LogUtils.e(TAG, message);
                        sourceList.remove(finalSourceLiveData);
                        recurLiveDataSource(mutableLiveData, sourceList);
                    }
                }
            });
        }
    }

    /**
     * addSourceForES
     */
    protected <T> LiveData<Resource<T>> addSourceForES(LiveData<ApiResponse<ResponseEntity<T>>> es) {
        MediatorLiveData<Resource<T>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(es, (api -> {
            if (api.isSuccessful()) {
                if (api.body.getCode() == 0)
                    mediatorLiveData.postValue(Resource.success(api.body.getData()));
                else
                    mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.body.getMessage()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * addSourceForEsList
     */
    protected <T extends BaseENotesEntity> LiveData<Resource<T>> addSourceForEsList(LiveData<ApiResponse<List<ResponseEntity<T>>>> es, String blockChain) {
        MediatorLiveData<Resource<T>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(es, (api -> {
            if (api.isSuccessful()) {
                if (api.body != null && api.body.size() > 0 && api.body.get(0).getCode() == 0 && api.body.get(0).getData() != null) {
                    api.body.get(0).getData().setCoinType(blockChain);
                    mediatorLiveData.postValue(Resource.success(api.body.get(0).getData()));
                } else {
                    mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.body.get(0).getMessage()));
                }
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * addSourceForEsListAll
     */
    protected <T> LiveData<Resource<List<ResponseEntity<T>>>> addSourceForEsListAll(LiveData<ApiResponse<List<ResponseEntity<T>>>> es) {
        MediatorLiveData<Resource<List<ResponseEntity<T>>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(es, (api -> {
            if (api.isSuccessful()) {
                if (api.body != null && api.body.size() > 0) {
                    mediatorLiveData.postValue(Resource.success(api.body));
                } else {
                    mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, "list is null"));
                }
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


    /**
     * checkEthError
     */
    protected <T> boolean checkEthError(MediatorLiveData<Resource<T>> mediatorLiveData, Object obj, boolean showError) {
        if (obj != null && obj instanceof BaseEthEntity) {
            BaseEthEntity ethEntity = (BaseEthEntity) obj;
            if (ethEntity.getError() != null) {
                if (showError)
                    mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, ethEntity.getError().getMessage()));
                return true;
            }
        }
        return false;
    }

    protected <T> void handlerErrorLiveData(MediatorLiveData<Resource<T>> mediatorLiveData, String error) {
        mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, error));
        if (!error.contains("Too many requests. Please try again later")) {
            if (error.toLowerCase().contains("insufficient funds") || error.contains("There is another transaction with same nonce in the queue") || error.contains("too low") || error.toLowerCase().contains("replacement transaction underpriced")) {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, "No balance available"));
            } else
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, error));
        }
    }


    protected BigInteger getSha3BigInteger(String str) {
        LogUtils.i(TAG, str + "->" + ByteUtil.toHexString(HashUtil.sha3((str.getBytes()))));
        return ByteUtil.bytesToBigInteger(HashUtil.sha3((str.getBytes())));
    }

}

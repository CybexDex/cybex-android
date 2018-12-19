package com.cybex.provider.websocket;

import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.google.gson.Gson;

import java.lang.ref.SoftReference;
import java.lang.reflect.Type;

public class ReplyProcessImpl<T> implements IReplyProcess<T> {
    private String strError;
    private T mT;
    private Type mType;
    private Throwable exception;
    private String strResponse;
    private SoftReference<MessageCallback<T>> mCallbackReference;

    public ReplyProcessImpl(Type type, MessageCallback<T> callback) {
        mType = type;
        mCallbackReference = new SoftReference<>(callback);
    }

    @Override
    public void processTextToObject(String strText) {
        try {
            Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
            mT = gson.fromJson(strText, mType);
        } catch (Exception e) {
            e.printStackTrace();
            strError = e.getMessage();
            strResponse = strText;
        }
    }

    @Override
    public T getReply() {
        return mT;
    }

    @Override
    public String getError() {
        return strError;
    }

    @Override
    public void notifyFailure(Throwable t) {
        exception = t;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public String getResponse() {
        return strResponse;
    }

    @Override
    public MessageCallback<T> getCallback() {
        return mCallbackReference.get();
    }

    @Override
    public void release() {
        mCallbackReference.clear();
    }
}
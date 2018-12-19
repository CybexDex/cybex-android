package com.cybex.provider.websocket;

public class DelayCall<T>{

    public String flag;
    public Call call;
    public ReplyProcessImpl<T> replyProcess;

    public DelayCall(Call call, ReplyProcessImpl<T> replyProcess){
        this.call = call;
        this.replyProcess = replyProcess;
    }

    public DelayCall(String flag, Call call, ReplyProcessImpl<T> replyProcess){
        this.flag = flag;
        this.call = call;
        this.replyProcess = replyProcess;
    }
}

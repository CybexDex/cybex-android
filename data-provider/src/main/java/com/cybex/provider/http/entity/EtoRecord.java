package com.cybex.provider.http.entity;

public class EtoRecord {

    public static class Type {
        public static final String RECEIVE = "receive";//相对与项目方为收到Token，对于用户则为参与ETO
        public static final String SEND = "send";////相对与项目方为发送Token，对于用户则为收到Token
    }

    public static class Reason {
        public static final String REASON_1 = "1";//项目不存在
        public static final String REASON_2 = "2";//用户不在项目白名单
        public static final String REASON_3 = "3";//用户条件不符合
        public static final String REASON_4 = "4";//不在众筹期间之内
        public static final String REASON_5 = "5";//项目控制状态不是ok或stop
        public static final String REASON_6 = "6";//不是指定币种
        public static final String REASON_7 = "7";//项目的众筹额度已满
        public static final String REASON_8 = "8";//这名用户的众筹额度已满
        public static final String REASON_9 = "9";//低于个人众筹下限
        public static final String REASON_10 = "10";//项目剩余额度低于个人众筹下限
        public static final String REASON_11 = "11";//个人剩余额度低于个人众筹下限
        public static final String REASON_12 = "12";//一部分充值超过了Project的众筹总额度
        public static final String REASON_13 = "13";//一部分充值超过了个人众筹上线
        public static final String REASON_14 = "14";//超过指定的精度
        public static final String REASON_15 = "15";//带锁定期的转账
        public static final String REASON_101 = "101";//代币转账失败


    }

    private String id;
    //
    private String project_id;
    //
    private String project_name;
    //cybex用户名
    private String user_id;
    //接受打款记录receive,send
    private String ieo_type;
    //状态ok
    private String ieo_status;
    //token类型
    private String token;
    //金额
    private float token_count;
    //
    private String memo;
    //区块号
    private int block_num;
    //区块中第几个交易
    private int trade_num;
    //有reason就表示未完全成功
    //reason对应关系见
    // https://docs.google.com/document/d/13rZ3ICtJtmx2d43x6V8ASHOn5Tyk58tn5vw9Mek8yZ4/edit
    private String reason;
    //
    private String created_at;
    //
    private String update_at;
    //
    private String lock_at;

    public EtoRecord(String id, String project_id, String project_name, String user_id, String ieo_type,
                     String ieo_status, String token, float token_count, String memo, int block_num,
                     int trade_num, String reason, String created_at, String update_at, String lock_at) {
        this.id = id;
        this.project_id = project_id;
        this.project_name = project_name;
        this.user_id = user_id;
        this.ieo_type = ieo_type;
        this.ieo_status = ieo_status;
        this.token = token;
        this.token_count = token_count;
        this.memo = memo;
        this.block_num = block_num;
        this.trade_num = trade_num;
        this.reason = reason;
        this.created_at = created_at;
        this.update_at = update_at;
        this.lock_at = lock_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getIeo_type() {
        return ieo_type;
    }

    public void setIeo_type(String ieo_type) {
        this.ieo_type = ieo_type;
    }

    public String getIeo_status() {
        return ieo_status;
    }

    public void setIeo_status(String ieo_status) {
        this.ieo_status = ieo_status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public float getToken_count() {
        return token_count;
    }

    public void setToken_count(float token_count) {
        this.token_count = token_count;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public int getBlock_num() {
        return block_num;
    }

    public void setBlock_num(int block_num) {
        this.block_num = block_num;
    }

    public int getTrade_num() {
        return trade_num;
    }

    public void setTrade_num(int trade_num) {
        this.trade_num = trade_num;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(String update_at) {
        this.update_at = update_at;
    }

    public String getLock_at() {
        return lock_at;
    }

    public void setLock_at(String lock_at) {
        this.lock_at = lock_at;
    }
}

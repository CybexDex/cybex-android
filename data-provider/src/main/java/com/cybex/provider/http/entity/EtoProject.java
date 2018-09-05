package com.cybex.provider.http.entity;

import java.io.Serializable;

public class EtoProject implements Serializable {

    public static class Status {
        public static final String PRE = "pre";//即将开始
        public static final String OK = "ok";//进行中
        public static final String FINISH = "finish";//已结束
        public static final String FAIL = "fail";//失败
    }

    //项目ID
    private String id;
    //项目名字
    private String name;
    //状态（pre ok finish fail）
    private String status;
    //
    private String control_status;//暂时不用
    //项目开始时间
    private String start_at;
    //项目预计结束时间
    private String end_at;
    //项目实际结束时间
    private String finish_at;
    //关闭时间
    private String close_at;//暂时不用
    //发币时间 马上发币时为null
    private String offer_at;
    //锁定时间 不锁定为null
    private String lock_at;
    //兑换比例
    private float rate;
    //
    private int token_count;//暂时不用
    //token在cybex中的资产名
    private String token;
    //token名字
    private String token_name;
    //参投所用base在cybex中的资产名
    private String base_token;
    //参投所有base名字
    private String base_token_name;
    //精度（接受最小单位）
    private int base_accuracy;
    //个人最大总额度
    private float base_max_quote;
    //个人单次最小额度
    private float base_min_quote;
    //项目软顶
    private String base_soft_cap;
    //项目base总额度
    private float base_token_count;
    //项目base当前额度
    private float current_base_token_count;
    //当前参投用户
    private int current_user_count;
    //
    private String type;
    //
    private int deleted;
    //
    private String created_at;
    //
    private String update_at;
    //收款地址
    private String receive_address;
    //项目排序 高->低
    private int score;
    //online的展示，pre_online的调试用
    private String control;
    //banner大的在banner栏中靠前
    private int banner;
    //0不准预约，1可以预约
    private boolean is_user_in;
    //
    private String _id;
    //
    private String account;
    //项目ID
    private String project;
    //
    private String timestamp;
    //
    private int __v;
    //关联父项目ID
    private int parent;
    //结束区块
    private String t_finish_block;
    //结束花费时间
    private String t_total_time;
    //关键词
    private String adds_keyword;
    private String adds_keyword__lang_en;
    //项目优势
    private String adds_advantage;
    private String adds_advantage__lang_en;
    //项目官网
    private String adds_website;
    private String adds_website__lane_en;
    //项目banner
    private String adds_banner;
    private String adds_banner__lang_en;
    //项目logo
    private String adds_logo;
    private String adds_logo__lang_en;
    //白皮书地址
    private String adds_whitepaper;
    private String adds_whitepaper__lane_en;
    //项目详情地址
    private String adds_detail;
    private String adds_detail__lang_en;
    //项目进度
    private float current_percent;
    //
    private int index;

    public EtoProject(String id, String name, String status, String control_status, String start_at,
                      String end_at, String finish_at, String close_at, String offer_at, String lock_at,
                      float rate, int token_count, String token, String token_name, String base_token,
                      String base_token_name, int base_accuracy, float base_max_quote, float base_min_quote,
                      String base_soft_cap, float base_token_count, float current_base_token_count,
                      int current_user_count, String type, int deleted, String created_at, String update_at,
                      String receive_address, int score, String control, int banner, boolean is_user_in,
                      String _id, String account, String project, String timestamp, int __v, int parent,
                      String t_finish_block, String t_total_time, String adds_keyword,
                      String adds_keyword__lang_en, String adds_advantage, String adds_advantage__lang_en,
                      String adds_website, String adds_website__lane_en, String adds_banner,
                      String adds_banner__lang_en, String adds_logo, String adds_logo__lang_en,
                      String adds_whitepaper, String adds_whitepaper__lane_en, String adds_detail,
                      String adds_detail__lang_en, float current_percent, int index) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.control_status = control_status;
        this.start_at = start_at;
        this.end_at = end_at;
        this.finish_at = finish_at;
        this.close_at = close_at;
        this.offer_at = offer_at;
        this.lock_at = lock_at;
        this.rate = rate;
        this.token_count = token_count;
        this.token = token;
        this.token_name = token_name;
        this.base_token = base_token;
        this.base_token_name = base_token_name;
        this.base_accuracy = base_accuracy;
        this.base_max_quote = base_max_quote;
        this.base_min_quote = base_min_quote;
        this.base_soft_cap = base_soft_cap;
        this.base_token_count = base_token_count;
        this.current_base_token_count = current_base_token_count;
        this.current_user_count = current_user_count;
        this.type = type;
        this.deleted = deleted;
        this.created_at = created_at;
        this.update_at = update_at;
        this.receive_address = receive_address;
        this.score = score;
        this.control = control;
        this.banner = banner;
        this.is_user_in = is_user_in;
        this._id = _id;
        this.account = account;
        this.project = project;
        this.timestamp = timestamp;
        this.__v = __v;
        this.parent = parent;
        this.t_finish_block = t_finish_block;
        this.t_total_time = t_total_time;
        this.adds_keyword = adds_keyword;
        this.adds_keyword__lang_en = adds_keyword__lang_en;
        this.adds_advantage = adds_advantage;
        this.adds_advantage__lang_en = adds_advantage__lang_en;
        this.adds_website = adds_website;
        this.adds_website__lane_en = adds_website__lane_en;
        this.adds_banner = adds_banner;
        this.adds_banner__lang_en = adds_banner__lang_en;
        this.adds_logo = adds_logo;
        this.adds_logo__lang_en = adds_logo__lang_en;
        this.adds_whitepaper = adds_whitepaper;
        this.adds_whitepaper__lane_en = adds_whitepaper__lane_en;
        this.adds_detail = adds_detail;
        this.adds_detail__lang_en = adds_detail__lang_en;
        this.current_percent = current_percent;
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getControl_status() {
        return control_status;
    }

    public void setControl_status(String control_status) {
        this.control_status = control_status;
    }

    public String getStart_at() {
        return start_at;
    }

    public void setStart_at(String start_at) {
        this.start_at = start_at;
    }

    public String getEnd_at() {
        return end_at;
    }

    public void setEnd_at(String end_at) {
        this.end_at = end_at;
    }

    public String getFinish_at() {
        return finish_at;
    }

    public void setFinish_at(String finish_at) {
        this.finish_at = finish_at;
    }

    public String getClose_at() {
        return close_at;
    }

    public void setClose_at(String close_at) {
        this.close_at = close_at;
    }

    public String getOffer_at() {
        return offer_at;
    }

    public void setOffer_at(String offer_at) {
        this.offer_at = offer_at;
    }

    public String getLock_at() {
        return lock_at;
    }

    public void setLock_at(String lock_at) {
        this.lock_at = lock_at;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public int getToken_count() {
        return token_count;
    }

    public void setToken_count(int token_count) {
        this.token_count = token_count;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken_name() {
        return token_name;
    }

    public void setToken_name(String token_name) {
        this.token_name = token_name;
    }

    public String getBase_token() {
        return base_token;
    }

    public void setBase_token(String base_token) {
        this.base_token = base_token;
    }

    public String getBase_token_name() {
        return base_token_name;
    }

    public void setBase_token_name(String base_token_name) {
        this.base_token_name = base_token_name;
    }

    public int getBase_accuracy() {
        return base_accuracy;
    }

    public void setBase_accuracy(int base_accuracy) {
        this.base_accuracy = base_accuracy;
    }

    public float getBase_max_quote() {
        return base_max_quote;
    }

    public void setBase_max_quote(float base_max_quote) {
        this.base_max_quote = base_max_quote;
    }

    public float getBase_min_quote() {
        return base_min_quote;
    }

    public void setBase_min_quote(float base_min_quote) {
        this.base_min_quote = base_min_quote;
    }

    public String getBase_soft_cap() {
        return base_soft_cap;
    }

    public void setBase_soft_cap(String base_soft_cap) {
        this.base_soft_cap = base_soft_cap;
    }

    public float getBase_token_count() {
        return base_token_count;
    }

    public void setBase_token_count(float base_token_count) {
        this.base_token_count = base_token_count;
    }

    public float getCurrent_base_token_count() {
        return current_base_token_count;
    }

    public void setCurrent_base_token_count(float current_base_token_count) {
        this.current_base_token_count = current_base_token_count;
    }

    public int getCurrent_user_count() {
        return current_user_count;
    }

    public void setCurrent_user_count(int current_user_count) {
        this.current_user_count = current_user_count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
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

    public String getReceive_address() {
        return receive_address;
    }

    public void setReceive_address(String receive_address) {
        this.receive_address = receive_address;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public int getBanner() {
        return banner;
    }

    public void setBanner(int banner) {
        this.banner = banner;
    }

    public boolean isIs_user_in() {
        return is_user_in;
    }

    public void setIs_user_in(boolean is_user_in) {
        this.is_user_in = is_user_in;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int get__v() {
        return __v;
    }

    public void set__v(int __v) {
        this.__v = __v;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public String getT_finish_block() {
        return t_finish_block;
    }

    public void setT_finish_block(String t_finish_block) {
        this.t_finish_block = t_finish_block;
    }

    public String getT_total_time() {
        return t_total_time;
    }

    public void setT_total_time(String t_total_time) {
        this.t_total_time = t_total_time;
    }

    public String getAdds_keyword() {
        return adds_keyword;
    }

    public void setAdds_keyword(String adds_keyword) {
        this.adds_keyword = adds_keyword;
    }

    public String getAdds_keyword__lang_en() {
        return adds_keyword__lang_en;
    }

    public void setAdds_keyword__lang_en(String adds_keyword__lang_en) {
        this.adds_keyword__lang_en = adds_keyword__lang_en;
    }

    public String getAdds_advantage() {
        return adds_advantage;
    }

    public void setAdds_advantage(String adds_advantage) {
        this.adds_advantage = adds_advantage;
    }

    public String getAdds_advantage__lang_en() {
        return adds_advantage__lang_en;
    }

    public void setAdds_advantage__lang_en(String adds_advantage__lang_en) {
        this.adds_advantage__lang_en = adds_advantage__lang_en;
    }

    public String getAdds_website() {
        return adds_website;
    }

    public void setAdds_website(String adds_website) {
        this.adds_website = adds_website;
    }

    public String getAdds_website__lane_en() {
        return adds_website__lane_en;
    }

    public void setAdds_website__lane_en(String adds_website__lane_en) {
        this.adds_website__lane_en = adds_website__lane_en;
    }

    public String getAdds_banner() {
        return adds_banner;
    }

    public void setAdds_banner(String adds_banner) {
        this.adds_banner = adds_banner;
    }

    public String getAdds_banner__lang_en() {
        return adds_banner__lang_en;
    }

    public void setAdds_banner__lang_en(String adds_banner__lang_en) {
        this.adds_banner__lang_en = adds_banner__lang_en;
    }

    public String getAdds_logo() {
        return adds_logo;
    }

    public void setAdds_logo(String adds_logo) {
        this.adds_logo = adds_logo;
    }

    public String getAdds_logo__lang_en() {
        return adds_logo__lang_en;
    }

    public void setAdds_logo__lang_en(String adds_logo__lang_en) {
        this.adds_logo__lang_en = adds_logo__lang_en;
    }

    public String getAdds_whitepaper() {
        return adds_whitepaper;
    }

    public void setAdds_whitepaper(String adds_whitepaper) {
        this.adds_whitepaper = adds_whitepaper;
    }

    public String getAdds_whitepaper__lane_en() {
        return adds_whitepaper__lane_en;
    }

    public void setAdds_whitepaper__lane_en(String adds_whitepaper__lane_en) {
        this.adds_whitepaper__lane_en = adds_whitepaper__lane_en;
    }

    public String getAdds_detail() {
        return adds_detail;
    }

    public void setAdds_detail(String adds_detail) {
        this.adds_detail = adds_detail;
    }

    public String getAdds_detail__lang_en() {
        return adds_detail__lang_en;
    }

    public void setAdds_detail__lang_en(String adds_detail__lang_en) {
        this.adds_detail__lang_en = adds_detail__lang_en;
    }

    public float getCurrent_percent() {
        return current_percent;
    }

    public void setCurrent_percent(float current_percent) {
        this.current_percent = current_percent;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

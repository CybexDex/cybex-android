package com.cybexmobile.graphene.chain;

import java.io.Serializable;
import java.util.List;

public class AccountObject implements Serializable {
    public ObjectId<AccountObject> id;
    public String membership_expiration_date;
    public String registrar;
    public String referrer;
    public String lifetime_referrer;
    public int network_fee_percentage;
    public int lifetime_referrer_fee_percentage;
    public int referrer_rewards_percentage;
    public String name;
    public Authority owner;
    public Authority active;
    public Types.account_options options;
    public String statistics;
    public List<String> whitelisting_accounts;
    public List<String> whitelisted_accounts;
    public List<String> blacklisted_accounts;
    public List<String> blacklisting_accounts;
    public List<Object> owner_special_authority;
    public List<Object> active_special_authority;
    public Integer top_n_control_flags;
}

package com.cybexmobile.faucet;

import com.cybexmobile.graphene.chain.Types;

import java.util.List;
import java.util.Map;

public class CreateAccountRequest {

    public Cap cap;
    public Account account;

    public static class Account {
        public String name;
        public Types.public_key_type owner_key;
        public Types.public_key_type active_key;
        public Types.public_key_type memo_key;
        public String refcode;
        public String referrer;
    }

    public static class Cap {
        public String id;
        public String captcha;
    }

}

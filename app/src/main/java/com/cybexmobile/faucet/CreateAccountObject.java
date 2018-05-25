package com.cybexmobile.faucet;

import com.cybexmobile.graphene.chain.Types;

import java.util.List;
import java.util.Map;

public class CreateAccountObject {

    public static class response_error {
        public List<String> base;
    }

    public static class response_fail_error {
        public Map<String, List<String>> error;
    }

    public static class create_account_response {
        public Object account;
        public response_error error;
    }
    public static class cap {
        public String id;
        public String captcha;
    }
    public static class account {
        public String name;
        public Types.public_key_type owner_key;
        public Types.public_key_type active_key;
        public Types.public_key_type memo_key;
        public String refcode;
        public String referrer;
    }

    public cap cap;
    public account account;

}

package com.cybexmobile.graphene.chain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Authority implements Serializable {

    private Integer weight_threshold;
    public HashMap<ObjectId<AccountObject>, Integer> account_auths = new HashMap<>();
    private HashMap<Types.public_key_type, Integer> key_auths = new HashMap<>();
    private HashMap<Address, Integer> address_auths = new HashMap<>();

    public Authority(int nWeightThreshold, Types.public_key_type publicKeyType, int nWeightType) {
        weight_threshold = nWeightThreshold;
        key_auths.put(publicKeyType, nWeightType);
    }

    public boolean is_public_key_type_exist(Types.public_key_type publicKeyType) {
        return key_auths.containsKey(publicKeyType);
    }

    public List<Types.public_key_type> get_keys() {
        List<Types.public_key_type> listKeyType = new ArrayList<>();
        listKeyType.addAll(key_auths.keySet());
        return listKeyType;
    }
}

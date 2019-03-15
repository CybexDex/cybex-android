package com.cybex.provider.graphene.chain;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Authority implements Serializable {

    public Integer weight_threshold;
    public HashMap<ObjectId<AccountObject>, Integer> account_auths = new HashMap<>();
    public HashMap<Types.public_key_type, Integer> key_auths = new HashMap<>();
    public HashMap<Address, Integer> address_auths = new HashMap<>();

    public Authority(){

    }

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

    public void putNewKeys(Types.public_key_type public_key_type, int weight) {
        key_auths.put(public_key_type, weight);
    }

    public static class AuthoritySerializer implements JsonSerializer<Authority> {
        @Override
        public JsonElement serialize(Authority src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject authority = new JsonObject();
            authority.addProperty("weight_threshold", src.weight_threshold);
            JsonArray keyAuthArray = new JsonArray();
            JsonArray accountAuthArray = new JsonArray();
            JsonArray addressAuthArray = new JsonArray();
            for (Types.public_key_type  public_key_type : src.key_auths.keySet()) {
                JsonArray subArray = new JsonArray();
                subArray.add(public_key_type.toString());
                subArray.add(src.key_auths.get(public_key_type));
                keyAuthArray.add(subArray);
            }
            authority.add("account_auths", accountAuthArray);
            authority.add("key_auths", keyAuthArray);
            authority.add("address_auths", addressAuthArray);
            return authority;
        }
    }
}

package com.cybexmobile.graphene.chain;

import java.util.List;

public class FullAccountObject {

    public AccountObject account;
    public List<LimitOrderObject> limit_orders;
    public List<AccountBalanceObject> balances;


//    public static class deserializer implements JsonDeserializer<FullAccountObject> {
//        @Override
//        public FullAccountObject deserialize(JsonElement json, Type typeOfT,
//                                               JsonDeserializationContext context)
//                throws JsonParseException {
//            if (!json.isJsonArray()) {
//                throw new JsonParseException("invalid full account entry");
//            }
//            JsonArray arr = json.getAsJsonArray();
//            if (arr.size() < 2) {
//                throw new JsonParseException("unexpected element count in account entry");
//            }
//            JsonObject fullAccountJson = arr.get(1).getAsJsonObject();
//            JsonObject accountJson = fullAccountJson.getAsJsonObject("account");
//            if (accountJson == null) {
//                throw new JsonParseException("missing 'account' field");
//            }
//            JsonArray limitOrdersJson = fullAccountJson.getAsJsonArray("limit_orders");
//            if (limitOrdersJson == null || !limitOrdersJson.isJsonArray()) {
//                throw new JsonParseException("missing 'limit_orders' field");
//            }
//            FullAccountObject fullAccountObject = new FullAccountObject();
//            fullAccountObject.account = context.deserialize(accountJson, AccountObject.class);
//            fullAccountObject.limit_orders = new ArrayList<>(limitOrdersJson.size());
//            for (int i = 0; i < limitOrdersJson.size(); i++) {
//                LimitOrderObject limitOrder = context.deserialize(
//                        limitOrdersJson.get(i).getAsJsonObject(), LimitOrderObject.class);
//                fullAccountObject.limit_orders.add(limitOrder);
//            }
//            return fullAccountObject;
//        }
//    }
}

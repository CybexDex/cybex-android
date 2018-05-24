package com.cybexmobile.graphene.chain;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class FullAccountObjectReply {
    public static class full_account_object_reply_deserializer implements JsonDeserializer<FullAccountObjectReply> {

        @Override
        public FullAccountObjectReply deserialize(JsonElement json,
                                                  Type typeOfT,
                                                  JsonDeserializationContext context) throws JsonParseException {
            JsonArray jsonArray = json.getAsJsonArray();
            FullAccountObjectReply fullAccountObjectReply = new FullAccountObjectReply();
            fullAccountObjectReply.name = jsonArray.get(0).getAsString();
            fullAccountObjectReply.fullAccountObject = context.deserialize(jsonArray.get(1), FullAccountObject.class);

            return fullAccountObjectReply;
        }
    }

    String name;
    public FullAccountObject fullAccountObject;
}

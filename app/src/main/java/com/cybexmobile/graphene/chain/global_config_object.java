package com.cybexmobile.graphene.chain;

import com.cybexmobile.Common.gson_common_deserializer;
import com.cybexmobile.Common.gson_common_serializer;
import com.cybexmobile.crypto.ripemd160_object;
import com.cybexmobile.crypto.sha256_object;
import com.google.gson.GsonBuilder;

import java.util.Date;

public class global_config_object {
    private static global_config_object mConfigObject = new global_config_object();
    private GsonBuilder mGsonBuilder;

    public static global_config_object getInstance() {
        return mConfigObject;
    }

    private global_config_object() {
        mGsonBuilder = new GsonBuilder();
        mGsonBuilder.registerTypeAdapter(types.public_key_type.class, new types.public_key_type_deserializer());
        mGsonBuilder.registerTypeAdapter(types.public_key_type.class, new types.public_type_serializer());
        mGsonBuilder.registerTypeAdapter(object_id.class, new object_id.object_id_deserializer());
        mGsonBuilder.registerTypeAdapter(object_id.class, new object_id.object_id_serializer());
        mGsonBuilder.registerTypeAdapter(Date.class, new gson_common_deserializer.DateDeserializer());
        mGsonBuilder.registerTypeAdapter(Date.class, new gson_common_serializer.DateSerializer());
        mGsonBuilder.registerTypeAdapter(compact_signature.class, new compact_signature.compact_signature_serializer());
        mGsonBuilder.registerTypeAdapter(full_account_object_reply.class, new full_account_object_reply.full_account_object_reply_deserializer());
        mGsonBuilder.registerTypeAdapter(ripemd160_object.class, new ripemd160_object.ripemd160_object_deserializer());
        mGsonBuilder.registerTypeAdapter(sha256_object.class, new sha256_object.sha256_object_deserializer());
        mGsonBuilder.registerTypeAdapter(sha256_object.class, new sha256_object.sha256_object_serializer());
    }

    public GsonBuilder getGsonBuilder() {
        return mGsonBuilder;
    }
}

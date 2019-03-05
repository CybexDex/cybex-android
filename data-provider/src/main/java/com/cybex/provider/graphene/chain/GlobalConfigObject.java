package com.cybex.provider.graphene.chain;

import com.cybex.provider.common.GsonCommonDeserializer;
import com.cybex.provider.common.GsonCommonSerializer;
import com.cybex.provider.crypto.Ripemd160Object;
import com.cybex.provider.crypto.Sha256Object;
import com.google.gson.GsonBuilder;

import java.util.Date;

public class GlobalConfigObject {
    private static GlobalConfigObject mConfigObject = new GlobalConfigObject();
    private GsonBuilder mGsonBuilder;

    public static GlobalConfigObject getInstance() {
        return mConfigObject;
    }

    private GlobalConfigObject() {
        mGsonBuilder = new GsonBuilder();
        mGsonBuilder.registerTypeAdapter(Types.vote_id_type.class, new Types.vote_id_type_deserializer());
        mGsonBuilder.registerTypeAdapter(Types.public_key_type.class, new Types.public_key_type_deserializer());
        mGsonBuilder.registerTypeAdapter(Types.public_key_type.class, new Types.public_type_serializer());
        mGsonBuilder.registerTypeAdapter(ObjectId.class, new ObjectId.object_id_deserializer());
        mGsonBuilder.registerTypeAdapter(ObjectId.class, new ObjectId.object_id_serializer());
        mGsonBuilder.registerTypeAdapter(Date.class, new GsonCommonDeserializer.DateDeserializer());
        mGsonBuilder.registerTypeAdapter(Date.class, new GsonCommonSerializer.DateSerializer());
        mGsonBuilder.registerTypeAdapter(CompactSignature.class, new CompactSignature.compact_signature_serializer());
        mGsonBuilder.registerTypeAdapter(FullAccountObjectReply.class, new FullAccountObjectReply.full_account_object_reply_deserializer());
        mGsonBuilder.registerTypeAdapter(Ripemd160Object.class, new Ripemd160Object.ripemd160_object_deserializer());
        mGsonBuilder.registerTypeAdapter(Sha256Object.class, new Sha256Object.sha256_object_deserializer());
        mGsonBuilder.registerTypeAdapter(Sha256Object.class, new Sha256Object.sha256_object_serializer());
//        mGsonBuilder.registerTypeAdapter(Operations.operation_type.class, new Operations.operation_type.operation_type_deserializer());
//        mGsonBuilder.registerTypeAdapter(Operations.operation_type.class, new Operations.operation_type.operation_type_serializer());
        mGsonBuilder.registerTypeAdapter(Authority.class, new Authority.AuthoritySerializer());
    }

    public GsonBuilder getGsonBuilder() {
        return mGsonBuilder;
    }
}

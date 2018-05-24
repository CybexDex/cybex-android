package com.cybexmobile.graphene.chain;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;

public class ObjectId<T> implements Serializable{
    static class ObjectIdManager {
        private HashMap<Type, Integer> mMapTypeToId = new HashMap<>();

        ObjectIdManager() {
//            mMapTypeToId.put(asset_object.class, Types.object_type.asset_object_type);
//            mMapTypeToId.put(account_object.class, Types.object_type.account_object_type);
//            mMapTypeToId.put(LimitOrderObject.class, Types.object_type.limit_order_object_type);
        }
    }

    static ObjectIdManager objectIdManager = new ObjectIdManager();


    int space_id;
    int type_id;
    int instance;

    public <T> ObjectId(int nInstance, Class<T> classOfT) {
        instance = nInstance;
        type_id = objectIdManager.mMapTypeToId.get(classOfT);
        space_id = 1;

    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%d.%d.%d", space_id, type_id, instance);
    }

    @Override
    public boolean equals(Object obj) {
        ObjectId objectId = (ObjectId) obj;
        if (space_id == objectId.space_id &&
                type_id == objectId.type_id &&
                instance == objectId.instance) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public ObjectId(int nSpaceId, int nTypeId, int nInstance) {
        space_id = nSpaceId;
        type_id = nTypeId;
        instance = nInstance;
    }

    public int get_instance() {
        return instance;
    }

    /*public ObjectId<T> create_from_string(String strId) {
        int nFirstDot = strId.indexOf('.');
        int nSecondDot = strId.indexOf('.', nFirstDot + 1);
        if (nFirstDot == -1 || nSecondDot == -1) {
            return null;
        }

        int nSpaceId = Integer.valueOf(strId.substring(0, nFirstDot));
        int nTypeId = Integer.valueOf(strId.substring(nFirstDot + 1, nSecondDot));
        int nInstance = Integer.valueOf(strId.substring(nSecondDot + 1));
        ObjectId objectId = new ObjectId(nSpaceId, nTypeId, nInstance);

        return objectId;
    }*/

    public static <T> ObjectId<T> create_from_string(String strId) {
        if (strId.matches("\\d+.\\d+.\\d+") == false) {
            return null;
        }

        int nFirstDot = strId.indexOf('.');
        int nSecondDot = strId.indexOf('.', nFirstDot + 1);
        if (nFirstDot == -1 || nSecondDot == -1) {
            return null;
        }

        int nSpaceId = Integer.valueOf(strId.substring(0, nFirstDot));
        int nTypeId = Integer.valueOf(strId.substring(nFirstDot + 1, nSecondDot));
        int nInstance = Integer.valueOf(strId.substring(nSecondDot + 1));

        ObjectId<T> objectId = new ObjectId<>(nSpaceId, nTypeId, nInstance);

        return objectId;
    }

    public static class object_id_deserializer implements JsonDeserializer<ObjectId> {
        @Override
        public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String strContent = json.getAsString();
            int nFirstDot = strContent.indexOf('.');
            int nSecondDot = strContent.indexOf('.', nFirstDot + 1);
            if (nFirstDot == -1 || nSecondDot == -1) {
                throw new JsonParseException(strContent + " is invalid");
            }

            int nSpaceId = Integer.valueOf(strContent.substring(0, nFirstDot));
            int nTypeId = Integer.valueOf(strContent.substring(nFirstDot + 1, nSecondDot));
            int nInstance = Integer.valueOf(strContent.substring(nSecondDot + 1));
            ObjectId objectId = new ObjectId(nSpaceId, nTypeId, nInstance);

            return objectId;
        }
    }

    public static class object_id_serializer implements JsonSerializer<ObjectId> {
        @Override
        public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
}

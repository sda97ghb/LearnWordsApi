package auxiliary;

import java.lang.reflect.Type;

import com.google.gson.*;
import org.bson.types.ObjectId;

public class MongoObjectIdTypeAdapter implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {
    @Override
    public ObjectId deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        try {
            return new ObjectId(jsonElement.getAsJsonObject().get("$oid").getAsString());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public JsonElement serialize(ObjectId objectId, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("$oid", objectId.toHexString());
        return jsonObject;
    }
}
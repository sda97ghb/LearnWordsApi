package auxiliary;

import com.google.gson.*;

import java.lang.reflect.Type;

public class MongoLongTypeAdapter implements JsonSerializer<Long>, JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement instanceof JsonPrimitive) {
            return jsonElement.getAsLong();
        }
        else {
            // {"$numberLong":"1526729956416"}
            return jsonElement.getAsJsonObject().get("$numberLong").getAsLong();
        }
    }

    @Override
    public JsonElement serialize(Long aLong, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(aLong);
    }
}

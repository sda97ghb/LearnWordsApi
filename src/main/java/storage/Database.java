package storage;

import auxiliary.MongoLongTypeAdapter;
import auxiliary.MongoObjectIdTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.codecs.ObjectIdCodec;
import org.bson.codecs.StringCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class Database {
    private Database() {
    }

    private static final MongoDatabase database = new MongoClient()
            .getDatabase("learnWords")
            .withCodecRegistry(CodecRegistries.fromCodecs(
                    new StringCodec(),
                    new ObjectIdCodec(),
                    new StorageUserCodec(),
                    new StorageDeckCodec(),
                    new StorageCardCodec()));

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ObjectId.class, new MongoObjectIdTypeAdapter())
            .registerTypeAdapter(Long.class, new MongoLongTypeAdapter())
            .create();

    public static MongoDatabase getMongoDatabase() {
        return database;
    }

    public static <T> MongoCollection<T> getCollection(Class<T> clazz) {
        return getMongoDatabase().getCollection(clazz.getName(), clazz);
    }

    public static <T> T getById(ObjectId id, Class<T> clazz) {
        return getCollection(clazz).find(Filters.eq("_id", id)).first();
    }

    public static Gson getGson() {
        return gson;
    }

    public static Bson getFilter(Class c, Object o) {
        List<Bson> eqs = new LinkedList<>();
        for (Field field : c.getDeclaredFields()) {
            if (field.getAnnotation(StorageFilter.class) == null)
                continue;
            String name = field.getName();
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            if (serializedName != null)
                name = serializedName.value();
            try {
                eqs.add(Filters.eq(name, field.get(o)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return Filters.and((Bson[]) eqs.toArray());
    }
}

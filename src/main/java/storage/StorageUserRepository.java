package storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;

public class StorageUserRepository {
    private static MongoCollection<StorageUser> getDbCollection() {
        return Database.getCollection(StorageUser.class);
    }

    public static StorageUser getById(ObjectId id) {
        return Database.getById(id, StorageUser.class);
    }

    public static StorageUser getByEmail(String email) {
        return getDbCollection().find(Filters.eq("email", email)).first();
    }

    public static void insert(StorageUser storageUser) {
        getDbCollection().insertOne(storageUser);
    }

    public static void replaceWithId(ObjectId userId, StorageUser storageUser) {
        getDbCollection().replaceOne(Filters.eq("_id", userId), storageUser);
    }
}

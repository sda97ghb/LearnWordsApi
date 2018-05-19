package storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;

public class StorageDeckRepository {
    private static MongoCollection<StorageDeck> getDbCollection() {
        return Database.getCollection(StorageDeck.class);
    }

    public static StorageDeck getById(ObjectId id) {
        return Database.getById(id, StorageDeck.class);
    }

    public static StorageDeck getByOwnerIdAndName(ObjectId ownerId, String name) {
        return getDbCollection().find(
            Filters.and(
                Filters.eq("owner", ownerId),
                Filters.eq("name", name)
            )
        ).first();
    }

    public static void insert(StorageDeck storageDeck) {
        getDbCollection().insertOne(storageDeck);
    }

    public static void replaceWithId(ObjectId deckId, StorageDeck storageDeck) {
        getDbCollection().replaceOne(Filters.eq("_id", deckId), storageDeck);
    }

    public static DeleteResult deleteWithId(ObjectId id) {
        return StorageDeckRepository.getDbCollection().deleteOne(Filters.eq("_id", id));
    }

    public static List<StorageDeck> getAll() {
        List<StorageDeck> storageDecks = new LinkedList<>();
        for (StorageDeck storageDeck : getDbCollection().find())
            storageDecks.add(storageDeck);
        return storageDecks;
    }
}

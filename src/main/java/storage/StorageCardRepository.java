package storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;

public class StorageCardRepository {
    private static MongoCollection<StorageCard> getDbCollection() {
        return Database.getCollection(StorageCard.class);
    }

    public static StorageCard getById(ObjectId id) {
        return Database.getById(id, StorageCard.class);
    }

    public static StorageCard getByDeckIdAndWordAndComment(ObjectId deckId, String word, String comment) {
        return Database.getCollection(StorageCard.class)
            .find(Filters.and(
                Filters.eq("deck", deckId),
                Filters.eq("word", word),
                Filters.eq("comment", comment)
            ))
            .first();
    }

    public static void insert(StorageCard storageCard) {
        getDbCollection().insertOne(storageCard);
    }

    public static void replaceWithId(ObjectId id, StorageCard storageCard) {
        getDbCollection().replaceOne(Filters.eq("_id", id), storageCard);
    }

    public static void deleteWithId(ObjectId id) {
        getDbCollection().deleteOne(Filters.eq("_id", id));
    }

    public static List<StorageCard> getAll() {
        List<StorageCard> storageCards = new LinkedList<>();
        for (StorageCard storageCard : getDbCollection().find())
            storageCards.add(storageCard);
        return storageCards;
    }
}

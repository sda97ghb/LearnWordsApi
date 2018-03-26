package mappers;

import api.ApiCard;
import com.mongodb.client.model.Filters;
import storage.Database;
import storage.StorageCard;
import storage.StorageDeck;
import storage.StorageUser;

public class CardMapper implements Mapper<StorageCard, ApiCard> {
    @Override
    public ApiCard mapStorageToApi(StorageCard storageCard) {
        StorageDeck deck = Database.getCollection(StorageDeck.class)
                .find(Filters.eq("_id", storageCard.getDeck())).first();
        if (deck == null)
            return null;
        StorageUser user = Database.getCollection(StorageUser.class)
                .find(Filters.eq("_id", deck.getOwner())).first();
        if (user == null)
            return null;

        ApiCard apiCard = new ApiCard();
        apiCard.setOwner(user.getEmail());
        apiCard.setDeck(deck.getName());
        apiCard.setWord(storageCard.getWord());
        apiCard.setComment(storageCard.getComment());
        apiCard.setTranslation(storageCard.getTranslation());
        apiCard.setDifficulty(storageCard.getDifficulty());
        apiCard.setHidden(storageCard.isHidden());
        return apiCard;
    }

    @Override
    public StorageCard mapApiToStorage(ApiCard apiCard) {
        StorageUser user = Database.getCollection(StorageUser.class)
                .find(Filters.eq("email", apiCard.getOwner())).first();
        if (user == null)
            return null;
        StorageDeck deck = Database.getCollection(StorageDeck.class)
                .find(Filters.and(
                        Filters.eq("owner", user.getId()),
                        Filters.eq("name", apiCard.getDeck())
                ))
                .first();
        if (deck == null)
            return null;
        StorageCard storageCard = Database.getCollection(StorageCard.class)
                .find(Filters.and(
                        Filters.eq("deck", deck.getId()),
                        Filters.eq("word", apiCard.getWord()),
                        Filters.eq("comment", apiCard.getComment())
                )).first();
        if (storageCard == null) {
            storageCard = new StorageCard();
            storageCard.setDeck(deck.getId());
            storageCard.setWord(apiCard.getWord());
            storageCard.setComment(apiCard.getComment());
        }
        storageCard.setTranslation(apiCard.getTranslation());
        storageCard.setDifficulty(apiCard.getDifficulty());
        storageCard.setHidden(apiCard.isHidden());
        return storageCard;
    }
}

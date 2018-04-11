package mappers;

import api.ApiCard;
import com.mongodb.client.model.Filters;
import storage.*;

public class CardMapper implements Mapper<StorageCard, ApiCard> {
    @Override
    public ApiCard mapStorageToApi(StorageCard storageCard) {
        StorageDeck deck = StorageDeckRepository.getById(storageCard.getDeck());
        if (deck == null)
            return null;
        StorageUser user = StorageUserRepository.getById(deck.getOwner());
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
        StorageUser user = StorageUserRepository.getByEmail(apiCard.getOwner());
        if (user == null)
            return null;
        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), apiCard.getDeck());
        if (deck == null)
            return null;
        StorageCard storageCard = StorageCardRepository.getByDeckIdAndWordAndComment(deck.getId(), apiCard.getWord(), apiCard.getComment());
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

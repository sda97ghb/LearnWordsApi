package mappers;

import api.*;
import com.mongodb.client.model.Filters;
import storage.Database;
import org.bson.types.ObjectId;
import storage.StorageCard;
import storage.StorageDeck;
import storage.StorageUser;

import java.util.Objects;
import java.util.stream.Collectors;

public class DeckMapper implements Mapper<StorageDeck, ApiDeck> {
    private StorageDeck storageDeck;

    private String getUserEmailFromUserId(ObjectId id) {
        StorageUser user = Database.getCollection(StorageUser.class)
                .find(Filters.eq("_id", id)).first();
        return user == null ? null : user.getEmail();
    }

    private StorageCard getCardFromObjectId(ObjectId objectId) {
        return Database.getCollection(StorageCard.class)
                .find(Filters.eq("_id", objectId)).first();
    }

    private ApiCardReference getApiCardReference(StorageCard storageCard) {
        return new ApiCardReference(storageCard.getWord(), storageCard.getComment());
    }

    private ApiCard getApiCard(StorageCard storageCard) {
        return new CardMapper().mapStorageToApi(storageCard);
    }

    private ObjectId getCardIdFromApiCardReference(ApiCardReference apiCardReference) {
        StorageCard storageCard = Database.getCollection(StorageCard.class)
                .find(Filters.and(
                        Filters.eq("deck", this.storageDeck.getId()),
                        Filters.eq("word", apiCardReference.getWord()),
                        Filters.eq("comment", apiCardReference.getComment())
                ))
                .first();
        return storageCard == null ? null : storageCard.getId();
    }

    @Override
    public ApiDeck mapStorageToApi(StorageDeck storageDeck) {
        ApiDeck apiDeck = new ApiDeck();
        apiDeck.setOwner(getUserEmailFromUserId(storageDeck.getOwner()));
        apiDeck.setName(storageDeck.getName());
        apiDeck.setFromLanguage(storageDeck.getFromLanguage());
        apiDeck.setToLanguage(storageDeck.getToLanguage());
        if (storageDeck.getCards() != null)
            apiDeck.setCards(storageDeck.getCards().stream()
                    .map(this::getCardFromObjectId)
                    .filter(Objects::nonNull)
                    .map(this::getApiCardReference)
                    .collect(Collectors.toList()));
        return apiDeck;
    }

    public ApiExpandedDeck mapStorageToApiExpanded(StorageDeck storageDeck) {
        ApiExpandedDeck apiExpandedDeck = new ApiExpandedDeck();
        apiExpandedDeck.setOwner(getUserEmailFromUserId(storageDeck.getOwner()));
        apiExpandedDeck.setName(storageDeck.getName());
        apiExpandedDeck.setFromLanguage(storageDeck.getFromLanguage());
        apiExpandedDeck.setToLanguage(storageDeck.getToLanguage());
        if (storageDeck.getCards() != null)
            apiExpandedDeck.setCards(storageDeck.getCards().stream()
                    .map(this::getCardFromObjectId)
                    .filter(Objects::nonNull)
                    .map(this::getApiCard)
                    .collect(Collectors.toList()));
        return apiExpandedDeck;
    }

    public ApiDeckInfo mapStorageToApiInfo(StorageDeck storageDeck) {
        ApiDeckInfo apiDeckInfo = new ApiDeckInfo();
        apiDeckInfo.setOwner(getUserEmailFromUserId(storageDeck.getOwner()));
        apiDeckInfo.setName(storageDeck.getName());
        apiDeckInfo.setFromLanguage(storageDeck.getFromLanguage());
        apiDeckInfo.setToLanguage(storageDeck.getToLanguage());
        if (storageDeck.getCards() != null) {
            apiDeckInfo.setNumberOfCards(storageDeck.getCards().stream()
                    .map(this::getCardFromObjectId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    .size()
            );
            apiDeckInfo.setNumberOfHiddenCards(storageDeck.getCards().stream()
                    .map(this::getCardFromObjectId)
                    .filter(Objects::nonNull)
                    .map(StorageCard::isHidden)
                    .filter(Boolean::booleanValue)
                    .collect(Collectors.toList())
                    .size()
            );
        }
        else {
            apiDeckInfo.setNumberOfCards(0);
            apiDeckInfo.setNumberOfHiddenCards(0);
        }
        return apiDeckInfo;
    }

    @Override
    public StorageDeck mapApiToStorage(ApiDeck apiDeck) {
        StorageUser user = Database.getCollection(StorageUser.class)
                .find(Filters.eq("email", apiDeck.getOwner())).first();
        if (user == null)
            return null;
        StorageDeck storageDeck = Database.getCollection(StorageDeck.class)
                .find(Filters.and(
                        Filters.eq("owner", user.getId()),
                        Filters.eq("name", apiDeck.getName())
                ))
                .first();
        if (storageDeck == null) {
            storageDeck = new StorageDeck();
            storageDeck.setOwner(user.getId());
            storageDeck.setName(apiDeck.getName());
        }
        else {
            this.storageDeck = storageDeck;
            storageDeck.setCards(apiDeck.getCards().stream()
                    .map(this::getCardIdFromApiCardReference)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        storageDeck.setFromLanguage(apiDeck.getFromLanguage());
        storageDeck.setToLanguage(apiDeck.getToLanguage());
        return storageDeck;
    }
}

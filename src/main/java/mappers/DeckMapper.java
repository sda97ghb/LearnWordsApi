package mappers;

import api.*;
import auxiliary.TimestampFactory;
import org.bson.types.ObjectId;
import storage.*;

import java.util.Objects;
import java.util.stream.Collectors;

public class DeckMapper implements Mapper<StorageDeck, ApiDeck> {
    private StorageDeck storageDeck;

    private String getEmailOfUserWithId(ObjectId id) {
        StorageUser user = StorageUserRepository.getById(id);
        return user == null ? null : user.getEmail();
    }

    private ApiCardReference getApiCardReference(StorageCard storageCard) {
        return new ApiCardReference(storageCard.getWord(), storageCard.getComment());
    }

    private ApiCard getApiCard(StorageCard storageCard) {
        return new CardMapper().mapStorageToApi(storageCard);
    }

    @Override
    public ApiDeck mapStorageToApi(StorageDeck storageDeck) {
        ApiDeck apiDeck = new ApiDeck();
        apiDeck.setOwner(getEmailOfUserWithId(storageDeck.getOwner()));
        apiDeck.setName(storageDeck.getName());
        apiDeck.setFromLanguage(storageDeck.getFromLanguage());
        apiDeck.setToLanguage(storageDeck.getToLanguage());
        if (storageDeck.getCards() != null)
            apiDeck.setCards(storageDeck.getCards().stream()
                    .map(StorageCardRepository::getById)
                    .filter(Objects::nonNull)
                    .map(this::getApiCardReference)
                    .collect(Collectors.toList()));
        return apiDeck;
    }

    public ApiExpandedDeck mapStorageToApiExpanded(StorageDeck storageDeck) {
        ApiExpandedDeck apiExpandedDeck = new ApiExpandedDeck();
        apiExpandedDeck.setOwner(getEmailOfUserWithId(storageDeck.getOwner()));
        apiExpandedDeck.setName(storageDeck.getName());
        apiExpandedDeck.setFromLanguage(storageDeck.getFromLanguage());
        apiExpandedDeck.setToLanguage(storageDeck.getToLanguage());
        if (storageDeck.getCards() != null)
            apiExpandedDeck.setCards(storageDeck.getCards().stream()
                    .map(StorageCardRepository::getById)
                    .filter(Objects::nonNull)
                    .map(this::getApiCard)
                    .collect(Collectors.toList()));
        return apiExpandedDeck;
    }

    public ApiDeckInfo mapStorageToApiInfo(StorageDeck storageDeck) {
        ApiDeckInfo apiDeckInfo = new ApiDeckInfo();
        apiDeckInfo.setOwner(getEmailOfUserWithId(storageDeck.getOwner()));
        apiDeckInfo.setName(storageDeck.getName());
        apiDeckInfo.setFromLanguage(storageDeck.getFromLanguage());
        apiDeckInfo.setToLanguage(storageDeck.getToLanguage());
        if (storageDeck.getCards() != null) {
            apiDeckInfo.setNumberOfCards(storageDeck.getCards().stream()
                    .map(StorageCardRepository::getById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    .size()
            );
            apiDeckInfo.setNumberOfHiddenCards(storageDeck.getCards().stream()
                    .map(StorageCardRepository::getById)
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
        StorageUser user = StorageUserRepository.getByEmail(apiDeck.getOwner());
        if (user == null)
            return null;
        StorageDeck storageDeck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), apiDeck.getName());
        if (storageDeck == null) {
            storageDeck = new StorageDeck();
            storageDeck.setOwner(user.getId());
            storageDeck.setName(apiDeck.getName());
        }
        else {
            this.storageDeck = storageDeck;
            storageDeck.setCards(apiDeck.getCards().stream()
                .map(this::getStorageCardFromApiCardReference)
                .filter(Objects::nonNull)
                .map(StorageCard::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        }
        storageDeck.setFromLanguage(apiDeck.getFromLanguage());
        storageDeck.setToLanguage(apiDeck.getToLanguage());
        storageDeck.setTimestamp(TimestampFactory.getTimestamp());
        return storageDeck;
    }

    private StorageCard getStorageCardFromApiCardReference(ApiCardReference apiCardReference) {
        return StorageCardRepository.getByDeckIdAndWordAndComment(
            storageDeck.getId(), apiCardReference.getWord(), apiCardReference.getComment());
    }
}

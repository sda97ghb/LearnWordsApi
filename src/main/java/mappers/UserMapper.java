package mappers;

import api.ApiExpandedUser;
import api.ApiSharedDeckReference;
import api.ApiUser;
import auxiliary.TimestampFactory;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;
import storage.*;

import java.util.Objects;
import java.util.stream.Collectors;

public class UserMapper implements Mapper<StorageUser, ApiUser> {
    private StorageUser storageUser;

    private ApiSharedDeckReference getApiSharedDeckReferenceFromStorageDeck(StorageDeck storageDeck) {
        return new ApiSharedDeckReference(storageUser.getEmail(), storageDeck.getName());
    }

    private ObjectId getDeckIdFromName(String name) {
        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(storageUser.getId(), name);
        return deck == null ? null : deck.getId();
    }

    private ObjectId getDeckIdFromApiSharedDeckReference(ApiSharedDeckReference apiSharedDeckReference) {
        StorageUser user = StorageUserRepository.getByEmail(apiSharedDeckReference.getEmail());
        if (user == null)
            return null;
        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), apiSharedDeckReference.getName());
        return deck == null ? null : deck.getId();
    }

    @Override
    public ApiUser mapStorageToApi(StorageUser storageUser) {
        this.storageUser = storageUser;

        ApiUser apiUser = new ApiUser();
        apiUser.setEmail(storageUser.getEmail());
        apiUser.setPersonalDecks(storageUser.getPersonalDecks().stream()
                .map(StorageDeckRepository::getById)
                .filter(Objects::nonNull)
                .map(StorageDeck::getName)
                .collect(Collectors.toList()));
        apiUser.setSharedDecks(storageUser.getSharedDecks().stream()
                .map(StorageDeckRepository::getById)
                .filter(Objects::nonNull)
                .map(this::getApiSharedDeckReferenceFromStorageDeck)
                .collect(Collectors.toList()));
        return apiUser;
    }

    public ApiExpandedUser mapStorageToApiExpanded(StorageUser storageUser) {
        this.storageUser = storageUser;

        DeckMapper deckMapper = new DeckMapper();

        ApiExpandedUser apiUser = new ApiExpandedUser();
        apiUser.setEmail(storageUser.getEmail());
        apiUser.setPersonalDecks(storageUser.getPersonalDecks().stream()
                .map(StorageDeckRepository::getById)
                .filter(Objects::nonNull)
                .map(deckMapper::mapStorageToApiInfo)
                .collect(Collectors.toList()));
        apiUser.setSharedDecks(storageUser.getSharedDecks().stream()
                .map(StorageDeckRepository::getById)
                .filter(Objects::nonNull)
                .map(this::getApiSharedDeckReferenceFromStorageDeck)
                .collect(Collectors.toList()));
        return apiUser;
    }

    @Override
    public StorageUser mapApiToStorage(ApiUser apiUser) {
        StorageUser storageUser = StorageUserRepository.getByEmail(apiUser.getEmail());
        if (storageUser == null) {
            storageUser = new StorageUser();
            storageUser.setEmail(apiUser.getEmail());
        }
        else {
            this.storageUser = storageUser;
            storageUser.setPersonalDecks(apiUser.getPersonalDecks().stream()
                    .map(this::getDeckIdFromName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            storageUser.setSharedDecks(apiUser.getSharedDecks().stream()
                    .map(this::getDeckIdFromApiSharedDeckReference)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        storageUser.setTimestamp(TimestampFactory.getTimestamp());

        return storageUser;
    }
}

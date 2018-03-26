package mappers;

import api.ApiSharedDeckReference;
import api.ApiUser;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;
import storage.Database;
import storage.StorageDeck;
import storage.StorageUser;

import java.util.Objects;
import java.util.stream.Collectors;

public class UserMapper implements Mapper<StorageUser, ApiUser> {
    private StorageUser storageUser;

    private StorageUser getStorageUserWithEmail(String email) {
        return Database.getCollection(StorageUser.class)
                .find(Filters.eq("email", email)).first();
    }

    private StorageDeck getDeckFromDatabase(ObjectId objectId) {
        return Database.getCollection(StorageDeck.class).find(Filters.eq("_id", objectId)).first();
    }

    private ApiSharedDeckReference getApiSharedDeckReferenceFromStorageDeck(StorageDeck storageDeck) {
        return new ApiSharedDeckReference(storageUser.getEmail(), storageDeck.getName());
    }

    private ObjectId getDeckIdFromName(String name) {
        StorageDeck deck = Database.getCollection(StorageDeck.class)
                .find(Filters.and(Filters.eq("owner", storageUser.getId()),
                        Filters.eq("name", name))).first();
        return deck == null ? null : deck.getId();
    }

    private ObjectId getDeckIdFromApiSharedDeckReference(ApiSharedDeckReference apiSharedDeckReference) {
        StorageUser user = Database.getCollection(StorageUser.class)
                .find(Filters.eq("email", apiSharedDeckReference.getEmail()))
                .first();
        if (user == null)
            return null;
        StorageDeck deck = Database.getCollection(StorageDeck.class)
                .find(Filters.and(Filters.eq("owner", user.getId()),
                        Filters.eq("name", apiSharedDeckReference.getName()))).first();
        return deck == null ? null : deck.getId();
    }

    @Override
    public ApiUser mapStorageToApi(StorageUser storageUser) {
        this.storageUser = storageUser;

        ApiUser apiUser = new ApiUser();
        apiUser.setEmail(storageUser.getEmail());
        apiUser.setPersonalDecks(storageUser.getPersonalDecks().stream()
                .map(this::getDeckFromDatabase)
                .filter(Objects::nonNull)
                .map(StorageDeck::getName)
                .collect(Collectors.toList()));
        apiUser.setSharedDecks(storageUser.getSharedDecks().stream()
                .map(this::getDeckFromDatabase)
                .filter(Objects::nonNull)
                .map(this::getApiSharedDeckReferenceFromStorageDeck)
                .collect(Collectors.toList()));
        return apiUser;
    }

    @Override
    public StorageUser mapApiToStorage(ApiUser apiUser) {
        StorageUser storageUser = getStorageUserWithEmail(apiUser.getEmail());
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

        return storageUser;
    }
}

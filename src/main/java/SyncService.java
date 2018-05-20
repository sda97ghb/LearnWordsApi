import apiannotation.ApiParameter;
import apiannotation.ApiRequest;
import apiannotation.ApiRequireAuthorization;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import storage.*;
import sync.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SyncService extends ApiService {

    SyncService() {
    }

    SyncService(GoogleIdToken.Payload idTokenPayload) {
        super(idTokenPayload);
    }

    // Actual api methods

    @ApiRequest(entity = "user", method = "register")
    @ApiRequireAuthorization
    public byte[] registerUser() {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null) {
            user = new StorageUser();
            user.setEmail(email);
            StorageUserRepository.insert(user);
        }

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "decks", method = "save")
    @ApiRequireAuthorization
    public byte[] saveDecks(@ApiParameter("decks") List<SyncDeck> decks) {
        StorageUser user = StorageUserRepository.getByEmail(getEmail());
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        for (SyncDeck syncDeck : decks) {
            StorageDeck storageDeck = new StorageDeck();
            storageDeck.setTimestamp(syncDeck.getTimestamp());
            storageDeck.setName(syncDeck.getName());
            storageDeck.setToLanguage(syncDeck.getToLanguage());
            storageDeck.setFromLanguage(syncDeck.getFromLanguage());
            storageDeck.setOwner(user.getId());
            StorageDeckRepository.insert(storageDeck);
        }

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "decks", method = "query")
    @ApiRequireAuthorization
    public byte[] queryDecks(@ApiParameter("decks") List<String> deckNames) {
        StorageUser user = StorageUserRepository.getByEmail(getEmail());
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        List<SyncDeck> syncDecks = deckNames.stream()
            .map(deckName -> StorageDeckRepository.getByOwnerIdAndName(user.getId(), deckName))
            .map(storageDeck -> {
                if (storageDeck == null)
                    return null;
                SyncDeck syncDeck = new SyncDeck();
                syncDeck.setTimestamp(storageDeck.getTimestamp());
                syncDeck.setName(storageDeck.getName());
                syncDeck.setFromLanguage(storageDeck.getFromLanguage());
                syncDeck.setToLanguage(storageDeck.getToLanguage());
                return syncDeck;
            })
            .collect(Collectors.toList());

        return new ApiResponse(syncDecks).toJson().getBytes();
    }

    @ApiRequest(entity = "decks", method = "delete")
    @ApiRequireAuthorization
    public byte[] deleteDecks(@ApiParameter("decks") List<String> deckNames) {
        StorageUser user = StorageUserRepository.getByEmail(getEmail());
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        deckNames.stream()
            .map(deckName -> StorageDeckRepository.getByOwnerIdAndName(user.getId(), deckName))
            .forEach(storageDeck -> StorageDeckRepository.deleteWithId(storageDeck.getId()));

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "decks", method = "dump")
    @ApiRequireAuthorization
    public byte[] dumpDecks() {
        List<SyncDeckDump> syncDeckDumps = StorageDeckRepository.getAll().stream()
            .map(storageDeck -> {
                SyncDeckDump syncDeckDump = new SyncDeckDump();
                syncDeckDump.setName(storageDeck.getName());
                syncDeckDump.setTimestamp(storageDeck.getTimestamp());
                return syncDeckDump;
            })
            .collect(Collectors.toList());

        return new ApiResponse(syncDeckDumps).toJson().getBytes();
    }

    @ApiRequest(entity = "cards", method = "save")
    @ApiRequireAuthorization
    public byte[] saveCards(@ApiParameter("cards") List<SyncCard> cards) {
        StorageUser user = StorageUserRepository.getByEmail(getEmail());
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        cards.stream()
            .map(syncCard -> {
                StorageDeck storageDeck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), syncCard.getDeckName());
                if (storageDeck == null)
                    return null;

                StorageCard storageCard = new StorageCard();
                storageCard.setTimestamp(syncCard.getTimestamp());
                storageCard.setDeck(storageDeck.getId());
                storageCard.setWord(syncCard.getWord());
                storageCard.setComment(syncCard.getComment());
                storageCard.setTranslation(syncCard.getTranslation());
                storageCard.setDifficulty(syncCard.getDifficulty());
                storageCard.setHidden(syncCard.isHidden());

                return storageCard;
            })
            .filter(Objects::nonNull)
            .forEach(StorageCardRepository::insert);

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "cards", method = "query")
    @ApiRequireAuthorization
    public byte[] queryCards(@ApiParameter("cards") List<SyncCardId> cardIds) {
        StorageUser user = StorageUserRepository.getByEmail(getEmail());
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        List<SyncCard> syncCards = cardIds.stream()
            .map(syncCardId -> {
                StorageDeck storageDeck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), syncCardId.getDeckName());
                if (storageDeck == null)
                    return null;

                StorageCard storageCard = StorageCardRepository.getByDeckIdAndWordAndComment(
                    storageDeck.getId(), syncCardId.getWord(), syncCardId.getComment());

                SyncCard syncCard = new SyncCard();
                syncCard.setTimestamp(storageCard.getTimestamp());
                syncCard.setDeckName(syncCardId.getDeckName());
                syncCard.setWord(storageCard.getWord());
                syncCard.setComment(storageCard.getComment());
                syncCard.setTranslation(storageCard.getTranslation());
                syncCard.setDifficulty(storageCard.getDifficulty());
                syncCard.setHidden(storageCard.isHidden());
                return syncCard;
            })
            .collect(Collectors.toList());

        return new ApiResponse(syncCards).toJson().getBytes();
    }

    @ApiRequest(entity = "cards", method = "delete")
    @ApiRequireAuthorization
    public byte[] deleteCards(@ApiParameter("cards") List<SyncCardId> cardIds) {
        StorageUser user = StorageUserRepository.getByEmail(getEmail());
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        cardIds.stream()
            .map(syncCardId -> {
                StorageDeck storageDeck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), syncCardId.getDeckName());
                if (storageDeck == null)
                    return null;

                return StorageCardRepository.getByDeckIdAndWordAndComment(storageDeck.getId(), syncCardId.getWord(), syncCardId.getComment());
            })
            .filter(Objects::nonNull)
            .map(StorageCard::getId)
            .forEach(StorageCardRepository::deleteWithId);

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "cards", method = "dump")
    @ApiRequireAuthorization
    public byte[] dumpCards() {
        List<SyncCardDump> syncCardDumps = StorageCardRepository.getAll().stream()
            .map(storageCard -> {
                StorageDeck storageDeck = StorageDeckRepository.getById(storageCard.getDeck());

                SyncCardDump syncCardDump = new SyncCardDump();
                syncCardDump.setTimestamp(storageCard.getTimestamp());
                syncCardDump.setDeckName(storageDeck == null ? null : storageDeck.getName());
                syncCardDump.setWord(storageCard.getWord());
                syncCardDump.setComment(storageCard.getComment());
                return syncCardDump;
            })
            .collect(Collectors.toList());

        return new ApiResponse(syncCardDumps).toJson().getBytes();
    }
}

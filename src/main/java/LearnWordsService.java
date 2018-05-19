import api.ApiCard;
import api.ApiDeck;
import apiannotation.ApiParameter;
import apiannotation.ApiRequest;
import apiannotation.ApiRequireAuthorization;
import auxiliary.GoogleAuthorization;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import mappers.CardMapper;
import mappers.DeckMapper;
import mappers.UserMapper;
import org.bson.types.ObjectId;
import storage.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public class LearnWordsService extends ApiService {

    LearnWordsService() {
    }

    LearnWordsService(GoogleIdToken.Payload idTokenPayload) {
        super(idTokenPayload);
    }

    // Test methods

    @ApiRequest(entity = "test", method = "getThread")
    public byte[] test_getThread() {
        return Thread.currentThread().getName().getBytes();
    }

    @ApiRequest(entity = "test", method = "echo")
    public byte[] testEcho(@ApiParameter("message") String message) {
        return message.getBytes();
    }

    @ApiRequest(entity = "test", method = "idToken")
    public byte[] testIdToken(@ApiParameter("id_token") String idToken) {
        try {
            return new ApiResponse(GoogleAuthorization.getEmail(idToken)).toJson().getBytes();
        }
        catch (GeneralSecurityException | IOException | GoogleAuthorization.InvalidIdTokenException e) {
            e.printStackTrace();
            return new ApiError(ApiError.METHOD, 1, e.getMessage()).toJson().getBytes();
        }
    }

    @ApiRequest(entity = "test", method = "rotate")
    public byte[] testRotate() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new ApiResponse("Hello World!").toJson().getBytes();
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

    @ApiRequest(entity = "user", method = "get")
    @ApiRequireAuthorization
    public byte[] getUser() {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        return new ApiResponse(new UserMapper().mapStorageToApi(user)).toJson().getBytes();
    }

    @ApiRequest(entity = "user", method = "getExpanded")
    @ApiRequireAuthorization
    public byte[] getExpandedUser() {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        return new ApiResponse(new UserMapper().mapStorageToApiExpanded(user)).toJson().getBytes();
    }

    @ApiRequest(entity = "user", method = "dumpDT")
    @ApiRequireAuthorization
    public byte[] dumpDT() {
        return new ApiResponse(new DTDump(getEmail())).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "get")
    @ApiRequireAuthorization
    public byte[] getDeck(@ApiParameter("name") String deckName) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), deckName);
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with this name.").toJson().getBytes();

        return new ApiResponse(new DeckMapper().mapStorageToApi(deck)).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "getExpanded")
    @ApiRequireAuthorization
    public byte[] getExpandedDeck(@ApiParameter("name") String deckName) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), deckName);
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with this name.").toJson().getBytes();

        return new ApiResponse(new DeckMapper().mapStorageToApiExpanded(deck)).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "save")
    @ApiRequireAuthorization
    public byte[] saveDeck(@ApiParameter("deck") ApiDeck deck) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        deck.setOwner(email);
        StorageDeck storageDeck = new DeckMapper().mapApiToStorage(deck);

        StorageDeck storedDeck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), deck.getName());
        if (storedDeck == null) {
            StorageDeckRepository.insert(storageDeck);

            storedDeck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), deck.getName());
            if (storedDeck == null)
                return new ApiError(ApiError.METHOD, 2, "Unable to store the deck.").toJson().getBytes();

            user.getPersonalDecks().add(storedDeck.getId());
            StorageUserRepository.replaceWithId(user.getId(), user);
        }
        else {
            storageDeck.setId(storedDeck.getId());
            StorageDeckRepository.replaceWithId(storageDeck.getId(), storageDeck);
        }
        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "delete")
    @ApiRequireAuthorization
    public byte[] deleteDeck(@ApiParameter("name") String name) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), name);
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with this name.").toJson().getBytes();

        user.getPersonalDecks().remove(deck.getId());
        StorageUserRepository.replaceWithId(user.getId(), user);

        for (ObjectId cardId : deck.getCards())
            StorageCardRepository.deleteWithId(cardId);

        long countOfDeleted = StorageDeckRepository.deleteWithId(deck.getId()).getDeletedCount();

        return countOfDeleted == 1
                ? new ApiResponse(null).toJson().getBytes()
                : new ApiError(ApiError.METHOD, 3, "Unable to delete the card.").toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "update")
    @ApiRequireAuthorization
    public byte[] updateDeck(@ApiParameter("name") String name, @ApiParameter("properties") Map<String, Object> properties) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), name);
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with this name.").toJson().getBytes();

        for (Map.Entry<String, Object> property : properties.entrySet()) {
            switch (property.getKey()) {
                case "name":
                    deck.setName((String) property.getValue());
                    break;
                case "fromLanguage":
                    deck.setFromLanguage((String) property.getValue());
                    break;
                case "toLanguage":
                    deck.setToLanguage((String) property.getValue());
                    break;
                default:
                    return new ApiError(ApiError.METHOD, 3, "Unknown property: " + property).toJson().getBytes();
            }
        }

        StorageDeckRepository.replaceWithId(deck.getId(), deck);

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "save")
    @ApiRequireAuthorization
    public byte[] saveCard(@ApiParameter("card") ApiCard card) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), card.getDeck());
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + card.getDeck() + ".").toJson().getBytes();

        card.setOwner(email);
        StorageCard newCard = new CardMapper().mapApiToStorage(card);

        StorageCard oldCard = StorageCardRepository.getByDeckIdAndWordAndComment(deck.getId(), card.getWord(), card.getComment());
        if (oldCard == null) {
            StorageCardRepository.insert(newCard);

            newCard = StorageCardRepository.getByDeckIdAndWordAndComment(deck.getId(), card.getWord(), card.getComment());
            if (newCard == null)
                return new ApiError(ApiError.METHOD, 3, "Unable to store the card.").toJson().getBytes();

            deck.getCards().add(newCard.getId());
            StorageDeckRepository.replaceWithId(deck.getId(), deck);
        }
        else {
            newCard.setId(oldCard.getId());
            StorageCardRepository.replaceWithId(newCard.getId(), newCard);
        }

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "get")
    @ApiRequireAuthorization
    public byte[] getCard(@ApiParameter("deck") String deckName, @ApiParameter("word") String word, @ApiParameter("comment") String comment) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), deckName);
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + deckName + ".").toJson().getBytes();

        StorageCard card = StorageCardRepository.getByDeckIdAndWordAndComment(deck.getId(), word, comment);
        if (card == null)
            return new ApiError(ApiError.METHOD, 3, deckName + " does not contain the card with word " + word + " and comment " + comment + ".").toJson().getBytes();

        return new ApiResponse(new CardMapper().mapStorageToApi(card)).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "delete")
    @ApiRequireAuthorization
    public byte[] deleteCard(@ApiParameter("deck") String deckName,
                             @ApiParameter("word") String word, @ApiParameter("comment") String comment) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), deckName);
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + deckName + ".").toJson().getBytes();

        StorageCard card = StorageCardRepository.getByDeckIdAndWordAndComment(deck.getId(), word, comment);
        if (card == null)
            return new ApiError(ApiError.METHOD, 3, deckName + " does not have any card with word " + word + " and comment " + comment + ".").toJson().getBytes();

        StorageCardRepository.deleteWithId(card.getId());

        deck.getCards().remove(card.getId());
        StorageDeckRepository.replaceWithId(deck.getId(), deck);

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "update")
    @ApiRequireAuthorization
    public byte[] updateCard(@ApiParameter("deck") String deckName,
                             @ApiParameter("word") String word, @ApiParameter("comment") String comment,
                             @ApiParameter("properties") Map<String, Object> properties) {
        String email = getEmail();

        StorageUser user = StorageUserRepository.getByEmail(email);
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = StorageDeckRepository.getByOwnerIdAndName(user.getId(), deckName);
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + deckName + ".").toJson().getBytes();

        StorageCard card = StorageCardRepository.getByDeckIdAndWordAndComment(deck.getId(), word, comment);
        if (card == null)
            return new ApiError(ApiError.METHOD, 3, deckName + " does not contain the card with word " + word + " and comment " + comment + ".").toJson().getBytes();

        for (Map.Entry<String, Object> property : properties.entrySet()) {
            switch (property.getKey()) {
                case "word":
                    card.setWord((String) property.getValue());
                    break;
                case "comment":
                    card.setComment((String) property.getValue());
                    break;
                case "translation":
                    card.setTranslation((String) property.getValue());
                    break;
                case "difficulty":
                    card.setDifficulty(((Number) property.getValue()).intValue());
                    break;
                case "hidden":
                    card.setHidden((boolean) property.getValue());
                    break;
                default:
                    return new ApiError(ApiError.METHOD, 4, "Unknown property: " + property).toJson().getBytes();
            }
        }

        StorageCardRepository.replaceWithId(card.getId(), card);

        return new ApiResponse(null).toJson().getBytes();
    }
}

import api.ApiCard;
import api.ApiDeck;
import apiannotation.ApiParameter;
import apiannotation.ApiRequest;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import mappers.CardMapper;
import mappers.DeckMapper;
import mappers.UserMapper;
import storage.Database;
import storage.StorageCard;
import storage.StorageDeck;
import storage.StorageUser;

import java.util.Map;

public class LearnWordsService {
    @ApiRequest(entity = "user", method = "add")
    public byte[] addUser(@ApiParameter("email") String email, @ApiParameter("password") String password) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user != null)
            return new ApiError(ApiError.METHOD, 1, "User is already registered.").toJson().getBytes();

        user = new StorageUser();
        user.setEmail(email);
        user.setPassword(password);
        users.insertOne(user);

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "user", method = "get")
    public byte[] getUser(@ApiParameter("email") String email) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        return new ApiResponse(Map.of("user", new UserMapper().mapStorageToApi(user))).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "get")
    public byte[] getDeck(@ApiParameter("email") String email, @ApiParameter("name") String deckName) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", deckName)
        )).first();
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with this name.").toJson().getBytes();

        return new ApiResponse(Map.of("deck", new DeckMapper().mapStorageToApi(deck))).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "getExpanded")
    public byte[] getExpandedDeck(@ApiParameter("email") String email, @ApiParameter("name") String deckName) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", deckName)
        )).first();
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with this name.").toJson().getBytes();

        return new ApiResponse(Map.of("deck", new DeckMapper().mapStorageToApiExpanded(deck))).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "save")
    public byte[] saveDeck(@ApiParameter("email") String email, @ApiParameter("deck") ApiDeck deck) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        deck.setOwner(email);
        StorageDeck storageDeck = new DeckMapper().mapApiToStorage(deck);

        StorageDeck storedDeck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", deck.getName())
        )).first();
        if (storedDeck == null) {
            decks.insertOne(storageDeck);

            storedDeck = decks.find(Filters.and(
                    Filters.eq("owner", user.getId()),
                    Filters.eq("name", deck.getName())
            )).first();
            if (storedDeck == null)
                return new ApiError(ApiError.METHOD, 2, "Unable to store the deck.").toJson().getBytes();

            user.getPersonalDecks().add(storedDeck.getId());
            users.replaceOne(Filters.eq("_id", user.getId()), user);
        }
        else {
            storageDeck.setId(storedDeck.getId());
            decks.replaceOne(Filters.eq("_id", storageDeck.getId()), storageDeck);
        }
        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "delete")
    public byte[] deleteDeck(@ApiParameter("email") String email, @ApiParameter("name") String name) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", name)
        )).first();
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with this name.").toJson().getBytes();

        user.getPersonalDecks().remove(deck.getId());
        users.replaceOne(Filters.eq("_id", user.getId()), user);

        long countOfDeleted = decks.deleteOne(Filters.eq("_id", deck.getId())).getDeletedCount();

        return countOfDeleted == 1
                ? new ApiResponse(null).toJson().getBytes()
                : new ApiError(ApiError.METHOD, 3, "Unable to delete the card.").toJson().getBytes();
    }

    @ApiRequest(entity = "deck", method = "update")
    public byte[] updateDeck(@ApiParameter("email") String email, @ApiParameter("name") String name,
                             @ApiParameter("properties") Map<String, Object> properties) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", name)
        )).first();
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

        decks.replaceOne(Filters.eq("_id", deck.getId()), deck);

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "save")
    public byte[] saveCard(@ApiParameter("email") String email, @ApiParameter("card") ApiCard card) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);
        MongoCollection<StorageCard> cards = Database.getCollection(StorageCard.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", card.getDeck())
        )).first();
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + card.getDeck() + ".").toJson().getBytes();

        card.setOwner(email);
        StorageCard newCard = new CardMapper().mapApiToStorage(card);

        StorageCard oldCard = cards.find(Filters.and(
                Filters.eq("deck", deck.getId()),
                Filters.eq("word", card.getWord()),
                Filters.eq("comment", card.getComment())
        )).first();
        if (oldCard == null) {
            cards.insertOne(newCard);

            newCard = cards.find(Filters.and(
                    Filters.eq("deck", deck.getId()),
                    Filters.eq("word", card.getWord()),
                    Filters.eq("comment", card.getComment())
            )).first();
            if (newCard == null)
                return new ApiError(ApiError.METHOD, 3, "Unable to store the card.").toJson().getBytes();

            deck.getCards().add(newCard.getId());
            decks.replaceOne(Filters.eq("_id", deck.getId()), deck);
        }
        else {
            newCard.setId(oldCard.getId());
            cards.replaceOne(Filters.eq("_id", newCard.getId()), newCard);
        }

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "get")
    public byte[] getCard(@ApiParameter("email") String email, @ApiParameter("deck") String deckName,
                          @ApiParameter("word") String word, @ApiParameter("comment") String comment) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);
        MongoCollection<StorageCard> cards = Database.getCollection(StorageCard.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", deckName)
        )).first();
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + deckName + ".").toJson().getBytes();

        StorageCard card = cards.find(Filters.and(
                Filters.eq("deck", deck.getId()),
                Filters.eq("word", word),
                Filters.eq("comment", comment)
        )).first();
        if (card == null)
            return new ApiError(ApiError.METHOD, 3, deckName + " does not contain the card with word " + word + " and comment " + comment + ".").toJson().getBytes();

        return new ApiResponse(Map.of("card", new CardMapper().mapStorageToApi(card))).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "delete")
    public byte[] deleteCard(@ApiParameter("email") String email, @ApiParameter("deck") String deckName,
                             @ApiParameter("word") String word, @ApiParameter("comment") String comment) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);
        MongoCollection<StorageCard> cards = Database.getCollection(StorageCard.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", deckName)
        )).first();
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + deckName + ".").toJson().getBytes();

        StorageCard card = cards.find(Filters.and(
                Filters.eq("deck", deck.getId()),
                Filters.eq("word", word),
                Filters.eq("comment", comment)
        )).first();
        if (card == null)
            return new ApiError(ApiError.METHOD, 3, deckName + " does not have any card with word " + word + " and comment " + comment + ".").toJson().getBytes();

        cards.deleteOne(Filters.eq("_id", card.getId()));

        deck.getCards().remove(card.getId());
        decks.replaceOne(Filters.eq("_id", deck.getId()), deck);

        return new ApiResponse(null).toJson().getBytes();
    }

    @ApiRequest(entity = "card", method = "update")
    public byte[] updateCard(@ApiParameter("email") String email, @ApiParameter("deck") String deckName,
                             @ApiParameter("word") String word, @ApiParameter("comment") String comment,
                             @ApiParameter("properties") Map<String, Object> properties) {
        MongoCollection<StorageUser> users = Database.getCollection(StorageUser.class);
        MongoCollection<StorageDeck> decks = Database.getCollection(StorageDeck.class);
        MongoCollection<StorageCard> cards = Database.getCollection(StorageCard.class);

        StorageUser user = users.find(Filters.eq("email", email)).first();
        if (user == null)
            return new ApiError(ApiError.METHOD, 1, "There is no user with this email.").toJson().getBytes();

        StorageDeck deck = decks.find(Filters.and(
                Filters.eq("owner", user.getId()),
                Filters.eq("name", deckName)
        )).first();
        if (deck == null)
            return new ApiError(ApiError.METHOD, 2, "There is no deck with name " + deckName + ".").toJson().getBytes();

        StorageCard card = cards.find(Filters.and(
                Filters.eq("deck", deck.getId()),
                Filters.eq("word", word),
                Filters.eq("comment", comment)
        )).first();
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

        cards.replaceOne(Filters.eq("_id", card.getId()), card);

        return new ApiResponse(null).toJson().getBytes();
    }
}

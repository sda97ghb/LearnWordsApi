package storage;

import com.google.gson.annotations.SerializedName;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;

public class StorageDeck {
    @SerializedName("_id")
    private ObjectId id;

    @SerializedName("owner")
    @StorageFilter
    private ObjectId owner;

    @SerializedName("name")
    @StorageFilter
    private String name;

    @SerializedName("fromLanguage")
    private String fromLanguage;

    @SerializedName("toLanguage")
    private String toLanguage;

    @SerializedName("cards")
    private List<ObjectId> cards = new LinkedList<>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getOwner() {
        return owner;
    }

    public void setOwner(ObjectId owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromLanguage() {
        return fromLanguage;
    }

    public void setFromLanguage(String fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public String getToLanguage() {
        return toLanguage;
    }

    public void setToLanguage(String toLanguage) {
        this.toLanguage = toLanguage;
    }

    public List<ObjectId> getCards() {
        return cards;
    }

    public void setCards(List<ObjectId> cards) {
        this.cards = cards;
    }
}

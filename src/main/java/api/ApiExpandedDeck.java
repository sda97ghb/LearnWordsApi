package api;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class ApiExpandedDeck {
    @SerializedName("owner")
    private String owner;

    @SerializedName("name")
    private String name;

    @SerializedName("fromLanguage")
    private String fromLanguage;

    @SerializedName("toLanguage")
    private String toLanguage;

    @SerializedName("cards")
    private List<ApiCard> cards = new LinkedList<>();

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
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

    public List<ApiCard> getCards() {
        return cards;
    }

    public void setCards(List<ApiCard> cards) {
        this.cards = cards;
    }
}

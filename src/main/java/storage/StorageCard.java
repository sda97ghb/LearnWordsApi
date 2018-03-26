package storage;

import com.google.gson.annotations.SerializedName;
import org.bson.types.ObjectId;

public class StorageCard {
    @SerializedName("_id")
    private ObjectId id;

    @StorageFilter
    @SerializedName("deck")
    private ObjectId deck;

    @StorageFilter
    @SerializedName("word")
    private String word;

    @StorageFilter
    @SerializedName("comment")
    private String comment;

    @SerializedName("translation")
    private String translation;

    @SerializedName("difficulty")
    private int difficulty;

    @SerializedName("hidden")
    private boolean hidden;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getDeck() {
        return deck;
    }

    public void setDeck(ObjectId deck) {
        this.deck = deck;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}

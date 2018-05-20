package storage;

import com.google.gson.annotations.SerializedName;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;

public class StorageUser {
    @SerializedName("_id")
    private ObjectId id;

    @SerializedName("timestamp")
    private Long timestamp = 0L;

    @SerializedName("email")
    @StorageFilter
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("personalDecks")
    private List<ObjectId> personalDecks = new LinkedList<>();

    @SerializedName("sharedDecks")
    private List<ObjectId> sharedDecks = new LinkedList<>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ObjectId> getPersonalDecks() {
        return personalDecks;
    }

    public void setPersonalDecks(List<ObjectId> personalDecks) {
        this.personalDecks = personalDecks;
    }

    public List<ObjectId> getSharedDecks() {
        return sharedDecks;
    }

    public void setSharedDecks(List<ObjectId> sharedDecks) {
        this.sharedDecks = sharedDecks;
    }
}

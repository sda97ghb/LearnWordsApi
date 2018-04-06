package api;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class ApiExpandedUser {
    @SerializedName("email")
    private String email;

    @SerializedName("personalDecks")
    private List<ApiDeckInfo> personalDecks = new LinkedList<>();

    @SerializedName("sharedDecks")
    private List<ApiSharedDeckReference> sharedDecks = new LinkedList<>();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ApiDeckInfo> getPersonalDecks() {
        return personalDecks;
    }

    public void setPersonalDecks(List<ApiDeckInfo> personalDecks) {
        this.personalDecks = personalDecks;
    }

    public List<ApiSharedDeckReference> getSharedDecks() {
        return sharedDecks;
    }

    public void setSharedDecks(List<ApiSharedDeckReference> sharedDecks) {
        this.sharedDecks = sharedDecks;
    }
}

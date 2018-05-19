import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public class ApiService {
    private GoogleIdToken.Payload idTokenPayload;

    ApiService() {
        idTokenPayload = null;
    }

    ApiService(GoogleIdToken.Payload idTokenPayload) {
        this.idTokenPayload = idTokenPayload;
    }

    protected GoogleIdToken.Payload getIdTokenPayload() {
        return idTokenPayload;
    }

    protected String getEmail() {
        if (idTokenPayload == null)
            return "";
        return getIdTokenPayload().getEmail();
    }
}

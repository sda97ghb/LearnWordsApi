package auxiliary;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAuthorization {
    public static class InvalidIdTokenException extends Exception {
        InvalidIdTokenException() {
            super("Invalid ID token.");
        }
    }

    private static final JsonFactory jsonFactory = new JacksonFactory();
    private static final HttpTransport transport = new NetHttpTransport();

    private static final String OAuth2WebClientId = "267917494384-oh291kv8mg1d8iov2epiojfg3apolbl8.apps.googleusercontent.com";

    private static final GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Collections.singletonList(OAuth2WebClientId))
            .build();

    private static GoogleIdToken getIdToken(String idTokenString) throws InvalidIdTokenException, GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null)
            throw new InvalidIdTokenException();
        return idToken;
    }

    public static GoogleIdToken.Payload getIdTokenPayload(String idTokenString) throws GeneralSecurityException, IOException, InvalidIdTokenException {
        return getIdToken(idTokenString).getPayload();
    }

    public static String getEmail(String idTokenString) throws GeneralSecurityException, IOException, InvalidIdTokenException {
        return getIdTokenPayload(idTokenString).getEmail();
    }

//        String userId = payload.getSubject();
//        String email = payload.getEmail();
//        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
//        String name = (String) payload.get("name");
//        String pictureUrl = (String) payload.get("picture");
//        String locale = (String) payload.get("locale");
//        String familyName = (String) payload.get("family_name");
//        String givenName = (String) payload.get("given_name");
}
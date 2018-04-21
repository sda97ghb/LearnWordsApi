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

public class UserVerifier {
    public static class InvalidIdTokenException extends Exception {
        InvalidIdTokenException() {
            super("Invalid ID token.");
        }
    }

    private static final JsonFactory jsonFactory = new JacksonFactory();
    private static final HttpTransport transport = new NetHttpTransport();

    private static final GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Collections.singletonList("267917494384-oh291kv8mg1d8iov2epiojfg3apolbl8.apps.googleusercontent.com"))
            .build();

    public static String getEmail(String idTokenString) throws GeneralSecurityException, IOException, InvalidIdTokenException {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Print user identifier
//            String userId = payload.getSubject();
//            System.out.println("User ID: " + userId);

            // Get profile information from payload
//            String email = payload.getEmail();
//            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
//            String name = (String) payload.get("name");
//            String pictureUrl = (String) payload.get("picture");
//            String locale = (String) payload.get("locale");
//            String familyName = (String) payload.get("family_name");
//            String givenName = (String) payload.get("given_name");

            return payload.getEmail();

        } else {
            throw new InvalidIdTokenException();
        }
    }
}
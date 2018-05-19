import java.util.Objects;

public class RequestId {
    private String entity;
    private String method;
    private String idToken;

    public RequestId(String entity, String method) {
        this.entity = entity;
        this.method = method;
        this.idToken = null;
    }

    public String getEntity() {
        return entity;
    }

    public String getMethod() {
        return method;
    }

    public String getIdToken() {
        return idToken;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, method);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RequestId))
            return false;
        RequestId other = (RequestId) obj;
        return other.entity.equals(entity) &&
                other.method.equals(method);
    }

    @Override
    public String toString() {
        return "entity = " + entity +
            ", method = " + method +
            ", idToken = " + (idToken == null ? "null" : idToken.substring(0, 10) + "...");
    }
}

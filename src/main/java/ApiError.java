import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class ApiError {
    public static final String SERVER = "server";
    public static final String AUTHORIZATION = "authorization";
    public static final String METHOD = "method";

    @SerializedName("type")
    private String type;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    public ApiError(String type, int code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }

    public String toJson() {
        return new Gson().toJson(new Wrapper(this));
    }

    class Wrapper {
        @SerializedName("error")
        Object error;

        Wrapper(Object error) {
            this.error = error;
        }
    }
}

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.util.Map;

public class ApiResponse {
    Object response;

    public ApiResponse(Object response) {
        this.response = response == null ? JSONObject.NULL : response;
    }

    public String toJson() {
        return new Gson().toJson(new Wrapper(response));
    }

    class Wrapper {
        @SerializedName("response")
        Object response;

        Wrapper(Object response) {
            this.response = response;
        }
    }
}

import apiannotation.ApiParameter;
import apiannotation.ApiRequest;

import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import java.util.*;

public class ServiceExecutor {
    class NotAnnotatedParameterException extends RuntimeException {
        NotAnnotatedParameterException(String name) {
            super(name);
        }
    }

    private Map<RequestId, Method> handlers = new HashMap<>();

    public ServiceExecutor(Class clazz) throws NotAnnotatedParameterException {
        for (Method classMethod : clazz.getMethods()) {
            ApiRequest apiRequest = classMethod.getAnnotation(ApiRequest.class);
            if (apiRequest == null)
                continue;

            handlers.put(new RequestId(apiRequest.entity(), apiRequest.method()), classMethod);

            for (Parameter parameter : classMethod.getParameters())
                if (!parameter.isAnnotationPresent(ApiParameter.class))
                    throw new NotAnnotatedParameterException(parameter.getName());
        }
    }

    public byte[] invoke(String requestString) {
        Gson gson = new Gson();
        JSONObject jo;
        RequestId requestId;
        try {
            jo = new JSONObject(requestString);
            requestId = gson.fromJson(requestString, RequestId.class);
        }
        catch (JSONException | JsonSyntaxException e) {
            return new ApiError(ApiError.SERVER, 4, "Invalid JSON: " + e.getMessage() + ".").toJson().getBytes();
        }

        Method classMethod = handlers.get(requestId);
        if (classMethod == null)
            return new ApiError(ApiError.SERVER, 2, "There is no handler for this pair of entity and method.").toJson().getBytes();

        List<Object> parameterValues = new LinkedList<>();
        for (Parameter parameter : classMethod.getParameters()) {
            String parameterName = parameter.getAnnotation(ApiParameter.class).value();
//            System.out.println(parameterName + " as " + parameter.getType());
            Object value;
            try {
                value = jo.get(parameterName);
            }
            catch (JSONException e) {
                return new ApiError(ApiError.SERVER, 3, "Required parameter is not found: " + parameterName + ".").toJson().getBytes();
            }
            // Values may be any mix of JSONObjects, JSONArrays, Strings, Booleans, Integers, Longs, Doubles or JSONObject.NULL.
            if (value.equals(JSONObject.NULL))
                parameterValues.add(null);
            else if (value instanceof JSONObject)
                parameterValues.add(gson.fromJson(value.toString(), parameter.getType()));
            else if (value instanceof JSONArray)
                return new ApiError(ApiError.SERVER, 103, "Internal error. See ServiceExecutor for more details.").toJson().getBytes();
            else
                parameterValues.add(value);
        }

        try {
            return (byte[]) classMethod.invoke(new LearnWordsService(), parameterValues.toArray());
        } catch (java.lang.IllegalAccessException e) {
            return new ApiError(ApiError.SERVER, 101, "Internal error. See ServiceExecutor for more details.").toJson().getBytes();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return new ApiError(ApiError.SERVER, 102, "Internal error. See ServiceExecutor for more details.").toJson().getBytes();
        }
    }
}

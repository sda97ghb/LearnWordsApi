import apiannotation.ApiParameter;
import apiannotation.ApiRequest;

import apiannotation.ApiRequireAuthorization;
import auxiliary.GoogleAuthorization;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.*;

public class ServiceExecutor {
    class NotAnnotatedParameterException extends RuntimeException {
        NotAnnotatedParameterException(String name) {
            super(name);
        }
    }

    private Class clazz;

    private Map<RequestId, Method> handlers = new HashMap<>();

    public <T extends ApiService> ServiceExecutor(Class<T> clazz) throws NotAnnotatedParameterException {
        this.clazz = clazz;

        for (Method classMethod : clazz.getMethods()) {
            ApiRequest apiRequest = classMethod.getAnnotation(ApiRequest.class);
            if (apiRequest == null)
                continue;

            handlers.put(new RequestId(apiRequest.entity(), apiRequest.method()), classMethod);

            for (Parameter parameter : classMethod.getParameters())
                if (!parameter.isAnnotationPresent(ApiParameter.class))
                    throw new NotAnnotatedParameterException("Class " + clazz.getName() +
                        ", method " + classMethod.getName() + ", parameter " + parameter.getName());
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
            else if (value instanceof JSONArray) {
                String s = value.toString();
                Type parameterizedType = parameter.getParameterizedType();
                Object o = gson.fromJson(s, parameterizedType);
                parameterValues.add(o);
            }
//                return new ApiError(ApiError.SERVER, 103, "Internal error. Current version of ServiceExecutor does not support array parameters.").toJson().getBytes();
            else
                parameterValues.add(value);
        }

        ApiService apiService;
//        LearnWordsService learnWordsService;
        if (classMethod.isAnnotationPresent(ApiRequireAuthorization.class)) {
            try {
                String idToken = requestId.getIdToken();
                if (idToken == null)
                    return new ApiError(ApiError.AUTHORIZATION, 1, "Required parameter \"idToken\" is not present.").toJson().getBytes();
                apiService = (ApiService) clazz.getDeclaredConstructor(GoogleIdToken.Payload.class).newInstance(GoogleAuthorization.getIdTokenPayload(idToken));
//                learnWordsService = new LearnWordsService(GoogleAuthorization.getIdTokenPayload(idToken));
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                return new ApiError(ApiError.AUTHORIZATION, 2, e.getMessage()).toJson().getBytes();
            } catch (GoogleAuthorization.InvalidIdTokenException e) {
                e.printStackTrace();
                return new ApiError(ApiError.AUTHORIZATION, 3, e.getMessage()).toJson().getBytes();
            } catch (IOException e) {
                e.printStackTrace();
                return new ApiError(ApiError.AUTHORIZATION, 4, e.getMessage()).toJson().getBytes();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new ApiError(ApiError.AUTHORIZATION, 5, e.getMessage()).toJson().getBytes();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return new ApiError(ApiError.AUTHORIZATION, 6, e.getMessage()).toJson().getBytes();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return new ApiError(ApiError.AUTHORIZATION, 7, e.getMessage()).toJson().getBytes();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return new ApiError(ApiError.AUTHORIZATION, 8, e.getMessage()).toJson().getBytes();
            }
        }
        else {
            try {
                apiService = (ApiService) clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return new ApiError(ApiError.SERVER, 100, e.getMessage()).toJson().getBytes();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new ApiError(ApiError.SERVER, 101, e.getMessage()).toJson().getBytes();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return new ApiError(ApiError.SERVER, 102, e.getMessage()).toJson().getBytes();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return new ApiError(ApiError.SERVER, 103, e.getMessage()).toJson().getBytes();
            }
//            learnWordsService = new LearnWordsService();
        }

        try {
            return (byte[]) classMethod.invoke(apiService, parameterValues.toArray());
        } catch (java.lang.IllegalAccessException e) {
            return new ApiError(ApiError.SERVER, 101, "Internal error 101. See ServiceExecutor for more details.").toJson().getBytes();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return new ApiError(ApiError.SERVER, 102, "Internal error 102. See ServiceExecutor for more details.").toJson().getBytes();
        }
    }
}

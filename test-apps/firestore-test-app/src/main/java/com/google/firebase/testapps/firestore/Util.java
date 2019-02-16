package com.google.firebase.testapps.firestore;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Util {
    private final static String customAuthAddress = "http://192.168.1.116:3000/auth/login";
    private final static String registerAddress = "http://192.168.1.116:3000/auth/register";

    public final static String facebookUrl = "https://192.168.1.116:3000/auth/oauth2/facebook";


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    static OkHttpClient client = new OkHttpClient();

    public static Task<String> getCustomTokenWithUsernameAndPassword(String username, String password) {
        final TaskCompletionSource<String> source = new TaskCompletionSource<>();

        String jsonString = "{\"password\": \"" + password + "\",\"email\": \"" + username + "\"}";

        System.out.println("json body: " + jsonString);

        RequestBody body = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url(customAuthAddress)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                source.setException(e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                try {
                    JSONObject obj = new JSONObject(jsonData);

                    boolean hasToken = obj.has("token");

                    String token = "";

                    if (hasToken) {
                        token = obj.getString("token");
                    } else {
                        token = null;
                    }

                    source.setResult(token);

                } catch (JSONException e) {
                    source.setException(e);
                    e.printStackTrace();
                }
            }
        });


        return source.getTask();
    }

    public static Task<String> createUser(String username, String password) {
        final TaskCompletionSource<String> source = new TaskCompletionSource<>();

        String jsonString = "{\"password\": \"" + password + "\",\"confirm_password\": \"" + password + "\",\"name\": \"" + username + "\",\"email\": \"" + username + "\"}";

        System.out.println("json body: " + jsonString);

        RequestBody body = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url(registerAddress)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                source.setException(e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();

                try {
                    JSONObject obj = new JSONObject(jsonData);
                    boolean hasError = obj.has("errors");

                    String errors = "";

                    if (hasError) {
                        errors = obj.getString("errors");
                    } else {
                        errors = null;
                    }

                    source.setResult(errors);

                } catch (JSONException e) {
                    e.printStackTrace();
                    source.setException(e);
                }

            }
        });


        return source.getTask();
    }
}

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
    private final static String customAuthAddress = "http://192.168.1.114:3000/auth/login";

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    static OkHttpClient client = new OkHttpClient();

    public static Task<String> getCustomTokenWithUsernameAndPassword(String username, String password) {
        final TaskCompletionSource<String> source = new TaskCompletionSource<>();

        RequestBody body = RequestBody.create(JSON, "{\"password\": \"12345\",\"email\": \"m3night@gmail.com\"}");
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
                    String token = obj.getString("token");

                    source.setResult(token);

                } catch (JSONException e) {
                    source.setException(e);
                }

            }
        });


        return source.getTask();
    }
}

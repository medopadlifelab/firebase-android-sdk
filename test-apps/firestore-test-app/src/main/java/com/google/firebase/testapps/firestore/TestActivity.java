// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.testapps.firestore;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestActivity extends Activity {
    private static final Map<String, Object> restaurant = new HashMap<>();
    private static final String TAG = TestActivity.class.toString();

    static {
        restaurant.put("location", "Google MTV");
    }

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView restaurantTextView;
    private final CountingIdlingResource idlingResource =
            new CountingIdlingResource("Firebase firestore listener");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        init();

        auth.signOut();

        if (auth.getCurrentUser() != null) {
            // already signed in
            setDocument();
        } else {
            // not signed in
            // TODO show sign-in login page intent and after successful login return token
            String username = "mark.biria@gmail.com";
            String password = "12345";

            startTaskLogin(username, password);
        }

        // Signout of any existing sessions and sign in with email and password
//    auth.signOut();
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        restaurantTextView = this.findViewById(R.id.restaurant);
        idlingResource.increment();

        // Since offline persistence is enabled by default, the event listener is invoked even without
        db.setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build());

        // Listen for a change to the collection
        db.collection("restaurants")
                .document("Baadal")
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(
                                    @javax.annotation.Nullable DocumentSnapshot snapshot,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (snapshot != null && snapshot.exists()) {
                                    restaurantTextView.setText(snapshot.getData().toString());
                                    idlingResource.decrement();
                                }
                            }
                        });
    }

    private void setDocument() {
        db.collection("restaurants").document("Baadal").set(restaurant);
    }

    public Task<AuthResult> startTaskLogin(String username, String password) {
        /**
         * Use Tasks API to chain 3 login steps together
         * Refer to this blog post for more details about Tasks API:
         *   https://firebase.googleblog.com/2016/09/become-a-firebase-taskmaster-part-1.html
         **/

        Task<AuthResult> combinedTask =
                // STEP 1: User logins with life os username and password
                Util.getCustomTokenWithUsernameAndPassword(username, password)
                        .continueWithTask(new Continuation<String, Task<AuthResult>>() {
                            @Override
                            public Task<AuthResult> then(@NonNull Task<String> task) throws Exception {
                                // STEP 2: Use Firebase Custom Auth token to login Firebase
                                String customToken = task.getResult();
                                System.out.println("token: " + customToken);

                                return auth.signInWithCustomToken(customToken)
                                        .addOnSuccessListener(
                                                new OnSuccessListener<AuthResult>() {
                                                    @Override
                                                    public void onSuccess(AuthResult authResult) {
                                                        System.out.println("Signed in: "+ auth.getCurrentUser().getDisplayName()+" "+auth.getCurrentUser().getEmail()+" "+auth.getCurrentUser().getUid());
                                                        Toast.makeText(TestActivity.this, "Signed in", Toast.LENGTH_LONG).show();

                                                        setDocument();
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        System.out.println("Failed to sign in");
                                                        Log.d(TAG, e.toString());

                                                        Toast.makeText(TestActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                            }
                        });

        return combinedTask;
    }

    @VisibleForTesting
    @NonNull
    @Keep
    public IdlingResource getIdlingResource() {
        return idlingResource;
    }
}

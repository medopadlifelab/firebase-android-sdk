/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.testapps.firestore;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CustomAuthActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "CustomAuth";

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);

        // Buttons
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);
        findViewById(R.id.verifyEmailButton).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();


        Task<String> combinedTask =
                // STEP 1: Create user in lifeos username and password
                Util.createUser(email, password)
                        .continueWithTask(new Continuation<String, Task<String>>() {
                            @Override
                            public Task<String> then(@NonNull Task<String> task) throws Exception {

                                final TaskCompletionSource<String> source = new TaskCompletionSource<>();

                                // STEP 2: Use Firebase Custom Auth token to login Firebase
                                String errors = task.getResult();
                                System.out.println("errors: " + errors);

                                if (task.isSuccessful() && errors == null) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    Toast.makeText(CustomAuthActivity.this, "createUserWithEmail:success.",
                                            Toast.LENGTH_SHORT).show();

//                                    FirebaseUser user = mAuth.getCurrentUser();
//                                    updateUI(user);
                                } else if (task.isSuccessful() && errors != null) {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure: " + errors);
                                    Toast.makeText(CustomAuthActivity.this, errors,
                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(null);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(CustomAuthActivity.this, "createUserWithEmail:failure.",
                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(null);
                                }

                                hideProgressDialog();

                                return source.getTask();
                            }
                        });
    }

    private void signIn(String username, String password) {
        Log.d(TAG, "signIn:" + username);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        Task<AuthResult> combinedTask =
                // STEP 1: User logins with life os username and password
                Util.getCustomTokenWithUsernameAndPassword(username, password)
                        .continueWithTask(new Continuation<String, Task<AuthResult>>() {
                            @Override
                            public Task<AuthResult> then(@NonNull Task<String> task) throws Exception {
                                // STEP 2: Use Firebase Custom Auth token to login Firebase
                                String customToken = task.getResult();
                                System.out.println("token: " + customToken);

                                if (task.isSuccessful() && customToken != null) {
                                    return mAuth.signInWithCustomToken(customToken)
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<AuthResult>() {
                                                        @Override
                                                        public void onSuccess(AuthResult authResult) {
                                                            Toast.makeText(CustomAuthActivity.this, "Signed in", Toast.LENGTH_LONG).show();

                                                            // Sign in success, update UI with the signed-in user's information
                                                            Log.d(TAG, "signInWithToken:success");
                                                            FirebaseUser user = mAuth.getCurrentUser();
                                                            updateUI(user);

                                                            hideProgressDialog();
                                                        }
                                                    })
                                            .addOnFailureListener(
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(CustomAuthActivity.this, e.toString(), Toast.LENGTH_LONG).show();

                                                            // If sign in fails, display a message to the user.
                                                            Log.w(TAG, "signInWithEmail:failure", e);
                                                            Toast.makeText(CustomAuthActivity.this, "Authentication failed.",
                                                                    Toast.LENGTH_SHORT).show();
                                                            updateUI(null);
                                                            mStatusTextView.setText(R.string.auth_failed);

                                                            hideProgressDialog();
                                                        }
                                                    });
                                } else if (task.isSuccessful() && customToken == null) {
                                    hideProgressDialog();
                                }

                                return null;
                            }
                        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.verifyEmailButton).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        findViewById(R.id.verifyEmailButton).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(CustomAuthActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(CustomAuthActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                    user.getUid()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.emailPasswordButtons).setVisibility(View.GONE);
            findViewById(R.id.emailPasswordFields).setVisibility(View.GONE);
            findViewById(R.id.signedInButtons).setVisibility(View.VISIBLE);

            findViewById(R.id.verifyEmailButton).setEnabled(!user.isEmailVerified());
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.emailPasswordFields).setVisibility(View.VISIBLE);
            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());

//            Uri facebookUri = Uri.parse("https://opar.auth0.com/authorize?audience=https://opar.auth0.com/userinfo&auth0Client=eyJuYW1lIjoiMDAtbG9naW4iLCJ2ZXJzaW9uIjoiMC4wLjEtU05BUFNIT1QifQ==&scope=openid&response_type=code&code_challenge_method=S256&redirect_uri=demo://opar.auth0.com/android/com.auth0.samples/callback&state=Sid6yvBzUYc75xBFcuPwJ6Mfp3Rzdj6-p4te7YBH9J0&code_challenge=7iWKXl5HDWHc9HvfN30ccKe9STnYvT69Hd34MKpnqPI&client_id=F10xC2ggXFT1cPuVKlyOQ7xbHjWdcSJu");
//
//            AuthenticationActivity.authenticateUsingBrowser(CustomAuthActivity.this, facebookUri);

        } else if (i == R.id.emailSignInButton) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.signOutButton) {
            signOut();
        } else if (i == R.id.verifyEmailButton) {
            sendEmailVerification();
        }
    }
}

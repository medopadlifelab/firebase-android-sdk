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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Demonstrate Firebase Authentication using a Facebook access token.
 */
public class FacebookLoginActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "FacebookLogin";

    private static final int OAUTH_REQUEST_CODE = 131;

    private TextView mStatusTextView;
    private TextView mDetailTextView;

    private FirebaseAuth mAuth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);
        findViewById(R.id.buttonFacebookSignout).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.buttonFacebookLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivityForResult(new Intent(FacebookLoginActivity.this, SignInActivity.class), OAUTH_REQUEST_CODE);
                SignInActivity.authenticateUsingBrowser(FacebookLoginActivity.this, new SignInActivity.AuthCallback() {
                    @Override
                    public void onFailure(Exception exception) {

                    }

                    @Override
                    public void onSuccess(String token) {
                        System.out.println("token: " + token);

                        mAuth.signInWithCustomToken(token)
                                .addOnSuccessListener(
                                        new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                Toast.makeText(FacebookLoginActivity.this, "Signed in", Toast.LENGTH_LONG).show();

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
                                                Toast.makeText(FacebookLoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();

                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "signInWithEmail:failure", e);
                                                Toast.makeText(FacebookLoginActivity.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                                updateUI(null);
                                                mStatusTextView.setText(R.string.auth_failed);

                                                hideProgressDialog();
                                            }
                                        });

                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void signOut() {
        mAuth.signOut();

        //TODO logout user from backend

        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.facebook_status_fmt, user.getDisplayName()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.buttonFacebookLogin).setVisibility(View.GONE);
            findViewById(R.id.buttonFacebookSignout).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.buttonFacebookLogin).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonFacebookSignout).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonFacebookSignout) {
            signOut();
        }
    }
}
package com.google.firebase.testapps.firestore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SignInActivity extends Activity {

    private boolean intentLaunched;

    static final String EXTRA_INTENT_LAUNCHED = "EXTRA_INTENT_LAUNCHED";
    static final String EXTRA_USE_BROWSER = "EXTRA_USE_BROWSER";

    static AuthCallback authCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            intentLaunched = savedInstanceState.getBoolean(EXTRA_INTENT_LAUNCHED, false);
        }

//        setContentView(R.layout.login);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_INTENT_LAUNCHED, intentLaunched);
    }

    static void authenticateUsingBrowser(@NonNull Context context, AuthCallback callback) {
        authCallback = callback;

        Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(SignInActivity.EXTRA_USE_BROWSER, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!intentLaunched && getIntent().getExtras() == null) {
            //Activity was launched in an unexpected way
            finish();
            return;
        } else if (!intentLaunched && getIntent().getData() == null) {
            intentLaunched = true;
            launchAuthenticationIntent();
            return;
        }

        if (getIntent().getData() != null) {
            deliverSuccessfulAuthenticationResult(getIntent());
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    private void launchAuthenticationIntent() {
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://example.com?token=1234&uid=mark.biria@gmail.com")).
        Intent browserIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Util.facebookUrl)).
                setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
        startActivity(browserIntent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    void deliverSuccessfulAuthenticationResult(Intent result) {
//            FacebookLoginActivity.setResult(intent);
        String customToken = result.getData().getQueryParameter("token");
        String pid = result.getData().getQueryParameter("pid");

        System.out.println("token: " + customToken + " pid: " + pid);

        authCallback.onSuccess(customToken);
        finish();
    }

    public interface AuthCallback {
        void onFailure(Exception exception);

        void onSuccess(String token);
    }
}

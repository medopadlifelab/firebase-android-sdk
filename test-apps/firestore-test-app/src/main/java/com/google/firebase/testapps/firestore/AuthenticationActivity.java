package com.google.firebase.testapps.firestore;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationActivity extends Activity {

    private static final String TAG = AuthenticationActivity.class.getSimpleName();

    static final String EXTRA_USE_BROWSER = "com.auth0.android.EXTRA_USE_BROWSER";
    static final String EXTRA_USE_FULL_SCREEN = "com.auth0.android.EXTRA_USE_FULL_SCREEN";
    static final String EXTRA_CONNECTION_NAME = "com.auth0.android.EXTRA_CONNECTION_NAME";
    static final String EXTRA_AUTHORIZE_URI = "com.auth0.android.EXTRA_AUTHORIZE_URI";
    static final String EXTRA_INTENT_LAUNCHED = "com.auth0.android.EXTRA_INTENT_LAUNCHED";
    static final String EXTRA_CT_OPTIONS = "com.auth0.android.EXTRA_CT_OPTIONS";

    private boolean intentLaunched;

    static void authenticateUsingBrowser(@NonNull Context context, @NonNull Uri authorizeUri) {
        Intent intent = new Intent(context, AuthenticationActivity.class);
        intent.putExtra(AuthenticationActivity.EXTRA_AUTHORIZE_URI, authorizeUri);
        intent.putExtra(AuthenticationActivity.EXTRA_USE_BROWSER, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    static void authenticateUsingWebView(@NonNull Activity activity, @NonNull Uri authorizeUri, int requestCode, String connection, boolean useFullScreen) {
        Intent intent = new Intent(activity, AuthenticationActivity.class);
        intent.putExtra(AuthenticationActivity.EXTRA_AUTHORIZE_URI, authorizeUri);
        intent.putExtra(AuthenticationActivity.EXTRA_USE_BROWSER, false);
        intent.putExtra(AuthenticationActivity.EXTRA_USE_FULL_SCREEN, useFullScreen);
        intent.putExtra(AuthenticationActivity.EXTRA_CONNECTION_NAME, connection);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            deliverSuccessfulAuthenticationResult(data);
        }
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_INTENT_LAUNCHED, intentLaunched);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            intentLaunched = savedInstanceState.getBoolean(EXTRA_INTENT_LAUNCHED, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!intentLaunched && getIntent().getExtras() == null) {
            //Activity was launched in an unexpected way
            finish();
            return;
        } else if (!intentLaunched) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void launchAuthenticationIntent() {
        Bundle extras = getIntent().getExtras();
        Uri authorizeUri = extras.getParcelable(EXTRA_AUTHORIZE_URI);

        //TOOD for webview i think
        if (!extras.getBoolean(EXTRA_USE_BROWSER, true)) {
            Log.w(TAG, "Should'nt be here");
        }
//            Intent intent = new Intent(this, WebAuthActivity.class);
//            intent.setData(authorizeUri);
//            intent.putExtra(WebAuthActivity.CONNECTION_NAME_EXTRA, extras.getString(EXTRA_CONNECTION_NAME));
//            intent.putExtra(WebAuthActivity.FULLSCREEN_EXTRA, extras.getBoolean(EXTRA_USE_FULL_SCREEN));
//            //The request code value can be ignored
//            startActivityForResult(intent, 33);
//            return;
//        }
    }

    @VisibleForTesting
    void deliverSuccessfulAuthenticationResult(Intent result) {
        final Map<String, String> values = getValuesFromUri(result.getData());
        if (values.isEmpty()) {
            Log.w(TAG, "The response didn't contain any of these values: code, state, id_token, access_token, token_type, refresh_token");
        }
        Log.w(TAG, "The parsed CallbackURI contains the following values: " + values);
    }


    public static Map<String, String> getValuesFromUri(@NonNull Uri uri) {
        return asMap(uri.getQuery() != null ? uri.getQuery() : uri.getFragment());
    }

    private static Map<String, String> asMap(@Nullable String valueString) {
        if (valueString == null) {
            return new HashMap<>();
        }
        final String[] entries = valueString.length() > 0 ? valueString.split("&") : new String[]{};
        Map<String, String> values = new HashMap<>(entries.length);
        for (String entry : entries) {
            final String[] value = entry.split("=");
            if (value.length == 2) {
                values.put(value[0], value[1]);
            }
        }
        return values;
    }

}


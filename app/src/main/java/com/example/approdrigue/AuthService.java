package com.example.approdrigue;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simplified secure storage for authentication tokens.
 */
public class AuthService {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS = "accessToken";
    private static final String KEY_ID = "idToken";

    /** Save tokens after login. */
    public static void saveTokens(Context context, String accessToken, String idToken) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_ID, idToken)
                .apply();
    }

    /** Load tokens on startup. */
    public static TokenPair loadTokens(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(KEY_ACCESS, null);
        String idToken = prefs.getString(KEY_ID, null);
        return new TokenPair(accessToken, idToken);
    }

    /** Delete tokens on sign out. */
    public static void deleteTokens(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_ACCESS).remove(KEY_ID).apply();
    }

    /** Simple holder for a pair of tokens. */
    public static class TokenPair {
        public final String accessToken;
        public final String idToken;

        public TokenPair(String accessToken, String idToken) {
            this.accessToken = accessToken;
            this.idToken = idToken;
        }
    }
}

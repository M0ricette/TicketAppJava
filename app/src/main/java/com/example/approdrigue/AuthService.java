package com.example.approdrigue;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Simplified secure storage for authentication tokens.
 */
public class AuthService {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS = "accessToken";
    private static final String KEY_ID     = "idToken";

    /** Save tokens after login. */
    public static void saveTokens(Context context, String accessToken, String idToken) {
        try {
            MasterKey key = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    context, PREF_NAME, key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            prefs.edit()
                    .putString(KEY_ACCESS, accessToken)
                    .putString(KEY_ID,     idToken)
                    .apply();
        } catch (Exception e) {
            // Fallback to normal SharedPreferences if encryption fails
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_ACCESS, accessToken)
                    .putString(KEY_ID,     idToken)
                    .apply();
        }
    }

    /** Load tokens on startup. */
    public static TokenPair loadTokens(Context context) {
        String accessToken;
        String idToken;
        try {
            MasterKey key = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    context, PREF_NAME, key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            accessToken = prefs.getString(KEY_ACCESS, null);
            idToken     = prefs.getString(KEY_ID,     null);
        } catch (Exception e) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            accessToken = prefs.getString(KEY_ACCESS, null);
            idToken     = prefs.getString(KEY_ID,     null);
        }
        return new TokenPair(accessToken, idToken);
    }

    /** Delete tokens on sign out. */
    public static void deleteTokens(Context context) {
        try {
            MasterKey key = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    context, PREF_NAME, key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            prefs.edit().remove(KEY_ACCESS).remove(KEY_ID).apply();
        } catch (Exception e) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_ACCESS).remove(KEY_ID).apply();
        }
    }

    /** Simple holder for a pair of tokens. */
    public static class TokenPair {
        public final String accessToken;
        public final String idToken;
        public TokenPair(String accessToken, String idToken) {
            this.accessToken = accessToken;
            this.idToken     = idToken;
        }
    }
}

package com.example.approdrigue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class AuthService {
    private static final String FILE_NAME = "secure_prefs";
    private static final String KEY_ID_TOKEN = "idToken";
    private static final String KEY_ACCESS_TOKEN = "accessToken";

    public static void saveTokens(Context context, String accessToken, String idToken) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit()
                    .putString(KEY_ID_TOKEN, idToken)
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TokenPair loadTokens(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            String access = prefs.getString(KEY_ACCESS_TOKEN, null);
            String id = prefs.getString(KEY_ID_TOKEN, null);
            return new TokenPair(access, id);
        } catch (Exception e) {
            e.printStackTrace();
            return new TokenPair(null, null);
        }
    }

    public static void clear(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SharedPreferences getPrefs(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static class TokenPair {
        public final String accessToken;
        public final String idToken;

        public TokenPair(String accessToken, String idToken) {
            this.accessToken = accessToken;
            this.idToken = idToken;
        }
    }
}

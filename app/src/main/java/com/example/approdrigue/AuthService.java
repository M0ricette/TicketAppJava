package com.example.approdrigue;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class AuthService {
    private static final String PREF_NAME = "secure_prefs";
    private static final String KEY_ID_TOKEN = "id_token";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    public static void saveTokens(Context context, String accessToken, String idToken) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            prefs.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_ID_TOKEN, idToken)
                    .apply();
        } catch (Exception e) {
            Log.e("AuthService", "Erreur lors de l'enregistrement des tokens", e);
        }
    }

    public static void deleteTokens(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            prefs.edit().clear().apply();
        } catch (Exception e) {
            Log.e("AuthService", "Erreur lors de la suppression des tokens", e);
        }
    }

    public static String loadIdToken(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            return prefs.getString(KEY_ID_TOKEN, null);
        } catch (Exception e) {
            Log.e("AuthService", "Erreur lors du chargement du token", e);
            return null;
        }
    }

    private static SharedPreferences getEncryptedPrefs(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}

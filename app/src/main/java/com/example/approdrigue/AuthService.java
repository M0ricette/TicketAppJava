package com.example.approdrigue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Classe utilitaire pour stocker, récupérer et effacer les tokens Cognito
 * (idToken et accessToken) de façon sécurisée via EncryptedSharedPreferences.
 */
public class AuthService {

    // Nom du fichier de SharedPreferences chiffrées
    private static final String FILE_NAME = "secure_prefs";

    // Clés associées aux valeurs enregistrées
    private static final String KEY_ID_TOKEN = "idToken";
    private static final String KEY_ACCESS_TOKEN = "accessToken";

    /**
     * 🔐 Enregistre les tokens Cognito dans un SharedPreferences sécurisé.
     *
     * @param context      le contexte Android
     * @param accessToken  le token d'accès Cognito (utilisé pour appeler les APIs)
     * @param idToken      le token d'identité Cognito (utilisé pour l’authentification utilisateur)
     */
    public static void saveTokens(Context context, String accessToken, String idToken) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit()
                    .putString(KEY_ID_TOKEN, idToken)
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .apply(); // Sauvegarde les valeurs de façon asynchrone
        } catch (Exception e) {
            e.printStackTrace(); // Affiche l’erreur si problème d’accès ou de chiffrement
        }
    }

    /**
     * 🔓 Récupère les tokens enregistrés localement.
     *
     * @param context le contexte Android
     * @return un objet TokenPair contenant accessToken et idToken (ou null si absent)
     */
    public static TokenPair loadTokens(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            String access = prefs.getString(KEY_ACCESS_TOKEN, null);
            String id = prefs.getString(KEY_ID_TOKEN, null);
            return new TokenPair(access, id); // Regroupe les deux tokens dans un seul objet
        } catch (Exception e) {
            e.printStackTrace();
            return new TokenPair(null, null); // En cas d’erreur, retourne un objet vide
        }
    }

    /**
     * 🧹 Efface les tokens enregistrés (utile lors d'une déconnexion).
     *
     * @param context le contexte Android
     */
    public static void clear(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().clear().apply(); // Supprime toutes les clés enregistrées
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 🔐 Initialise et retourne un SharedPreferences chiffré à l’aide d’un MasterKey.
     *
     * @param context le contexte Android
     * @return un SharedPreferences sécurisé avec AES256
     * @throws Exception en cas d’erreur de génération de la clé ou accès sécurisé
     */
    private static SharedPreferences getPrefs(Context context) throws Exception {
        // Création ou récupération de la clé principale de chiffrement
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // Création du fichier chiffré avec protection des clés et des valeurs
        return EncryptedSharedPreferences.create(
                context,
                FILE_NAME, // nom du fichier
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // chiffrement des clés
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // chiffrement des valeurs
        );
    }

    /**
     * 📦 Classe simple pour regrouper accessToken et idToken ensemble.
     */
    public static class TokenPair {
        public final String accessToken;
        public final String idToken;

        public TokenPair(String accessToken, String idToken) {
            this.accessToken = accessToken;
            this.idToken = idToken;
        }
    }
}

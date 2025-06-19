package com.example.approdrigue;

import android.util.Base64;

import org.json.JSONObject;

/**
 * Classe utilitaire pour travailler avec les **tokens JWT** (JSON Web Token).
 * Elle permet de :
 * - vérifier si un token est expiré
 * - décoder la partie "payload" du token
 */
public final class JwtUtils {

    // 🔒 Constructeur privé : empêche l’instanciation de cette classe utilitaire
    private JwtUtils() {}

    /**
     * 🔹 Vérifie si un token est expiré (ou invalide).
     *
     * @param idToken le token JWT à analyser
     * @return true si le token est expiré ou mal formé
     */
    public static boolean isTokenExpired(String idToken) {
        try {
            // 🔸 Un token JWT est composé de 3 parties : header.payload.signature
            String[] parts = idToken.split("\\.");

            // ✅ Vérifie qu’il y a bien 3 parties
            if (parts.length != 3) {
                return true; // token mal formé
            }

            // 🔸 Décodage du payload (partie 2)
            String payload = new String(
                    Base64.decode(
                            parts[1],
                            Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP
                    )
            );

            // 🔸 Conversion en JSON pour accéder aux champs internes
            JSONObject json = new JSONObject(payload);

            // 🔸 Extraction du champ "exp" (expiration timestamp en secondes)
            long exp = json.getLong("exp");

            // 🔸 Heure actuelle (en secondes)
            long now = System.currentTimeMillis() / 1000L;

            // ❌ Retourne true si le token est déjà expiré
            return now >= exp;

        } catch (Exception e) {
            // ⚠️ Si une exception survient (ex : mal formé), on considère que le token est invalide
            return true;
        }
    }

    /**
     * 🔹 Décode le contenu (payload) d’un JWT.
     *
     * @param token le token JWT à analyser
     * @return un objet JSON représentant le payload
     * @throws Exception si le token est mal formé ou le décodage échoue
     */
    public static JSONObject decodePayload(String token) throws Exception {
        String[] parts = token.split("\\.");

        // ❌ Vérifie qu’il y a bien 3 parties
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed JWT");
        }

        // 🔸 Décodage du payload (partie 2)
        String payload = new String(
                Base64.decode(
                        parts[1],
                        Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP
                )
        );

        // 🔸 Retourne le payload en tant qu’objet JSON
        return new JSONObject(payload);
    }
}

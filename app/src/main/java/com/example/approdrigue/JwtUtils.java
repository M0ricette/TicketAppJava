package com.example.approdrigue;

import android.util.Base64;

import org.json.JSONObject;

/**
 * Utility helpers for working with JWT tokens.
 */
public final class JwtUtils {
    private JwtUtils() {}

    /** Return true if the token is expired or malformed. */
    public static boolean isTokenExpired(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                return true;
            }
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
            JSONObject json = new JSONObject(payload);
            long exp = json.getLong("exp");
            long now = System.currentTimeMillis() / 1000L;
            return now >= exp;
        } catch (Exception e) {
            return true;
        }
    }
}

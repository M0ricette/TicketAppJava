package com.example.approdrigue;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

/**
 * Small helper demonstrating how to call the ticket API from Java code.
 * This class is not wired into the UI but can be used from an Activity.
 */
public class Main {
    private static final String TAG = "Main";

    private String idToken;

    public void loadTokens(Context context) {
        AuthService.TokenPair pair = AuthService.loadTokens(context);
        idToken = pair.idToken;
    }

    public void fetchVenue(Context context) {
        try {
            if (idToken == null || JwtUtils.isTokenExpired(idToken)) {
                Log.e(TAG, "Token missing or expired");
                return;
            }
            JSONObject obj = TicketApi.fetchVenueConfig(idToken);
            Log.i(TAG, "Venue id: " + obj.optString("venue_id"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch venue", e);
        }
    }

    public void fetchTicket(String code) {
        try {
            if (idToken == null || JwtUtils.isTokenExpired(idToken)) {
                Log.e(TAG, "Token missing or expired");
                return;
            }
            JSONObject obj = TicketApi.fetchTicket(idToken, code);
            Log.i(TAG, "Ticket response: " + obj.toString());
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch ticket", e);
        }
    }

    public void validateTicket(String code) {
        try {
            if (idToken == null || JwtUtils.isTokenExpired(idToken)) {
                Log.e(TAG, "Token missing or expired");
                return;
            }
            JSONObject obj = TicketApi.confirmValidation(idToken, code);
            Log.i(TAG, "Validation response: " + obj.toString());
        } catch (Exception e) {
            Log.e(TAG, "Failed to validate ticket", e);
        }
    }
}
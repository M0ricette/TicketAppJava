package com.example.approdrigue;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Minimal network layer to call the AWS endpoints.
 */
public class TicketApi {
    private static final String TAG = "TicketApi";

    private static final String VENUE_CONFIG_URL = "https://ao6sidd69h.execute-api.eu-north-1.amazonaws.com/prod/venue-config";
    private static final String TICKET_URL = "https://3jzfq2vywc.execute-api.us-east-1.amazonaws.com/TestAPI/ticket";
    private static final String VALIDATE_URL = "https://3jzfq2vywc.execute-api.us-east-1.amazonaws.com/TestAPI/ticket/validate";
    private static final String API_KEY = "dP9BkICR991gxbYDfEwgy6sKONdcEKxo2Tw3Uru9";

    private static HttpURLConnection openConnection(String urlStr, String idToken) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        if (idToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + idToken);
        }
        return conn;
    }

    public static JSONObject fetchVenueConfig(String idToken) throws Exception {
        HttpURLConnection conn = openConnection(VENUE_CONFIG_URL, idToken);
        conn.setRequestMethod("GET");
        return readJson(conn);
    }

    public static JSONObject fetchTicket(String idToken, String code) throws Exception {
        String url = TICKET_URL + "?code=" + code;
        HttpURLConnection conn = openConnection(url, idToken);
        conn.setRequestProperty("x-api-key", API_KEY);
        conn.setRequestMethod("GET");
        return readJson(conn);
    }

    public static JSONObject confirmValidation(String idToken, String code) throws Exception {
        HttpURLConnection conn = openConnection(VALIDATE_URL, idToken);
        conn.setRequestProperty("x-api-key", API_KEY);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String body = new JSONObject().put("code", code).toString();
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }
        return readJson(conn);
    }

    private static JSONObject readJson(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream()
        ));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        if (status < 200 || status >= 300) {
            Log.e(TAG, "Request failed: " + status + " " + sb.toString());
            throw new RuntimeException("HTTP " + status);
        }
        return new JSONObject(sb.toString());
    }
}

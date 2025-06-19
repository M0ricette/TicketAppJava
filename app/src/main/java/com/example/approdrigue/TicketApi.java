package com.example.approdrigue;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TicketApi {
    // Ton vrai endpoint API Gateway
    private static final String BASE_URL =
            "https://3jzfq2vywc.execute-api.us-east-1.amazonaws.com/TestAPI";

    public static JSONObject fetchVenueConfig(String token, String apiKey) throws Exception {
        URL url = new URL(BASE_URL + "/ticket"); // <- /ticket, pas /venue
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("x-api-key", apiKey);

        int status = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                status < 400 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        if (status >= 200 && status < 300) {
            return new JSONObject(response.toString());
        } else {
            JSONObject error = new JSONObject(response.toString());
            throw new Exception("Request failed: " + status + " " + error.toString());
        }
    }

    public static JSONObject fetchTicket(String token, String apiKey, String code) throws Exception {
        String encodedCode = URLEncoder.encode(code, "UTF-8");
        URL url = new URL(BASE_URL + "/ticket?code=" + encodedCode);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("x-api-key", apiKey);

        int status = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                status < 400 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        if (status >= 200 && status < 300) {
            return new JSONObject(response.toString());
        } else {
            JSONObject error = new JSONObject(response.toString());
            throw new Exception("Request failed: " + status + " " + error.toString());
        }
    }

    public static JSONObject confirmValidation(String token, String apiKey, String code) throws Exception {
        URL url = new URL(BASE_URL + "/ticket/validate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("x-api-key", apiKey);
        conn.setDoOutput(true);

        String payload = new JSONObject().put("code", code).toString();
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                status < 400 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        if (status >= 200 && status < 300) {
            return new JSONObject(response.toString());
        } else {
            JSONObject error = new JSONObject(response.toString());
            throw new Exception("Request failed: " + status + " " + error.toString());
        }
    }
}

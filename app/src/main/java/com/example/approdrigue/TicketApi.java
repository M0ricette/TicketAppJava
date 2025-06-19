package com.example.approdrigue;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Classe utilitaire permettant d'effectuer des requêtes HTTP
 * vers l'API AWS (API Gateway) pour gérer les tickets.
 */
public class TicketApi {

    // 🔗 URL de base de l'API déployée sur API Gateway
    private static final String BASE_URL =
            "https://3jzfq2vywc.execute-api.us-east-1.amazonaws.com/TestAPI";

    /**
     * 🔹 Requête GET sur `/ticket` pour récupérer les infos de la salle.
     *
     * @param token  le JWT (idToken ou accessToken Cognito)
     * @param apiKey la clé d’API définie sur API Gateway
     * @return un JSONObject contenant la config de la salle (venue)
     * @throws Exception en cas d'erreur HTTP ou parsing JSON
     */
    public static JSONObject fetchVenueConfig(String token, String apiKey) throws Exception {
        // 👉 Crée une URL pointant vers /ticket
        URL url = new URL(BASE_URL + "/ticket");

        // 👉 Prépare la connexion HTTP GET
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token); // Sécurisation via JWT
        conn.setRequestProperty("x-api-key", apiKey); // Clé API Gateway

        // 🔍 Lit la réponse HTTP
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

        // ✅ Si la réponse est OK (200 <= status < 300)
        if (status >= 200 && status < 300) {
            return new JSONObject(response.toString());
        } else {
            // ❌ En cas d'erreur HTTP
            JSONObject error = new JSONObject(response.toString());
            throw new Exception("Request failed: " + status + " " + error.toString());
        }
    }

    /**
     * 🔹 Requête GET `/ticket?code=...` pour obtenir un ticket précis.
     *
     * @param token  le JWT
     * @param apiKey la clé d’API
     * @param code   le code unique du ticket (souvent un code QR)
     * @return un JSONObject contenant les données du ticket
     * @throws Exception en cas d'erreur
     */
    public static JSONObject fetchTicket(String token, String apiKey, String code) throws Exception {
        // ✅ Encode le code du ticket pour l’URL (ex: caractères spéciaux)
        String encodedCode = URLEncoder.encode(code, "UTF-8");

        // 👉 Création de l’URL complète
        URL url = new URL(BASE_URL + "/ticket?code=" + encodedCode);

        // 🔧 Prépare la connexion HTTP GET
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("x-api-key", apiKey);

        // 🔍 Lecture de la réponse
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

        // ✅ Succès
        if (status >= 200 && status < 300) {
            return new JSONObject(response.toString());
        } else {
            // ❌ Échec
            JSONObject error = new JSONObject(response.toString());
            throw new Exception("Request failed: " + status + " " + error.toString());
        }
    }

    /**
     * 🔹 Requête POST `/ticket/validate` pour valider un ticket.
     *
     * @param token  JWT
     * @param apiKey Clé d’API Gateway
     * @param code   Code du ticket à valider
     * @return JSON de réponse (souvent "success" ou les infos du ticket validé)
     * @throws Exception si erreur HTTP
     */
    public static JSONObject confirmValidation(String token, String apiKey, String code) throws Exception {
        // 👉 URL du endpoint de validation
        URL url = new URL(BASE_URL + "/ticket/validate");

        // 🔧 Prépare une connexion POST
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("x-api-key", apiKey);
        conn.setDoOutput(true); // Pour pouvoir écrire un corps JSON

        // 📨 Corps JSON envoyé avec le code du ticket
        String payload = new JSONObject().put("code", code).toString();
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        // 🔍 Lecture de la réponse
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

        // ✅ OK
        if (status >= 200 && status < 300) {
            return new JSONObject(response.toString());
        } else {
            // ❌ KO
            JSONObject error = new JSONObject(response.toString());
            throw new Exception("Request failed: " + status + " " + error.toString());
        }
    }
}

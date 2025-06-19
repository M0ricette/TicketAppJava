package com.example.approdrigue;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.approdrigue.BuildConfig;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.auth.options.AuthSignOutOptions;
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult;
import com.amplifyframework.core.Amplify;

import org.json.JSONObject;


/**
 * Activité principale de l’application, affichée après la connexion.
 * Permet de vérifier et valider des tickets.
 */
public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "Dashboard";

    // 🔐 Clé d'API pour les requêtes vers API Gateway
    private static final String API_KEY = BuildConfig.API_KEY;

    // 🧩 Références aux éléments de l'UI
    private Button btnLogout, btnCheck, btnValidate;
    private EditText editCode;
    private TextView textResult;
    private View viewStatus;

    // 🔑 Token JWT et état de validité du ticket
    private String idToken;
    private boolean ticketValid = false;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 🔗 Lien avec les éléments graphiques
        btnLogout   = findViewById(R.id.btnLogout);
        btnCheck    = findViewById(R.id.btnCheck);
        btnValidate = findViewById(R.id.btnValidate);
        editCode    = findViewById(R.id.editCode);
        textResult  = findViewById(R.id.textResult);
        viewStatus  = findViewById(R.id.viewStatus);

        // 🔐 Chargement du token Cognito enregistré localement
        AuthService.TokenPair tokens = AuthService.loadTokens(this);
        idToken = tokens.idToken;

        accessToken = tokens.accessToken;
        // 🧭 Gestion des boutons
        btnLogout.setOnClickListener(v -> logout()); // Déconnexion
        btnCheck.setOnClickListener(v -> checkTokenThen(this::fetchTicket)); // Vérifier ticket
        btnValidate.setOnClickListener(v -> checkTokenThen(this::validateTicket)); // Valider ticket
    }

    /**
     * 🔓 Déconnexion globale (local + côté AWS Cognito).
     */
    private void logout() {
        Log.i(TAG, "🚪 Déconnexion globale...");
        AuthSignOutOptions options = AuthSignOutOptions.builder()
                .globalSignOut(true)
                .build();

        Amplify.Auth.signOut(
                options,
                result -> runOnUiThread(() -> {
                    if (result instanceof AWSCognitoAuthSignOutResult.CompleteSignOut) {
                        AuthService.clear(this); // Supprime les tokens locaux
                        startActivity(new Intent(this, LoginActivity.class)); // Retour à la page de login
                        finish(); // Ferme l'activité actuelle
                    } else {
                        Toast.makeText(this, "Déconnexion partielle", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    /**
     * 🧪 Vérifie si le token est expiré avant d'exécuter une action (GET ou POST).
     */
    private void checkTokenThen(Runnable action) {
        AuthService.TokenPair tokens = AuthService.loadTokens(this);
        String accessToken = tokens.accessToken;
        if (accessToken == null || JwtUtils.isTokenExpired(accessToken)) {
            runOnUiThread(() -> textResult.setText("Token expiré. Reconnectez-vous."));
            return;
        }
        action.run(); // Si OK, on exécute l'action (fetch ou validate)
    }




    /**
     * 🔍 Vérifie un ticket via une requête GET sur /ticket?code=...
     */
    private void fetchTicket() {
        String code = editCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Entrez un code de ticket.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Exécute l’appel réseau en tâche de fond
        new AsyncTask<Void, Void, JSONObject>() {
            Exception error;

            @Override
            protected void onPreExecute() {
                Toast.makeText(DashboardActivity.this, "🔍 Vérification…", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected JSONObject doInBackground(Void... v) {
                try {
                    return TicketApi.fetchTicket(idToken, API_KEY, code);
                } catch (Exception e) {
                    error = e;
                    Log.e(TAG, "fetchTicket error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    boolean used = result.optBoolean("utilisé", false);
                    ticketValid = (result.optInt("statut", 1) == 0 && !used);

                    // Affiche le résultat dans l’interface
                    textResult.setText(
                            "Ticket: " + result.optString("n_tickets", code) +
                                    "\n" + (ticketValid ? "✅ VALIDE" : "❌ INVALIDE")
                    );

                    btnValidate.setVisibility(ticketValid ? View.VISIBLE : View.GONE);
                    viewStatus.setBackgroundColor(ticketValid ? 0xFF00AA00 : 0xFFAA0000); // vert ou rouge
                } else {
                    textResult.setText("Erreur fetchTicket: " +
                            (error != null ? error.getMessage() : "Inconnu"));
                    btnValidate.setVisibility(View.GONE);
                    viewStatus.setBackgroundColor(0xFFAA0000);
                }
            }
        }.execute();
    }

    /**
     * ✅ Envoie une requête POST pour valider le ticket (si il est valide).
     */
    private void validateTicket() {
        if (!ticketValid) {
            Toast.makeText(this, "Ticket invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String code = editCode.getText().toString().trim();

        new AsyncTask<Void, Void, JSONObject>() {
            Exception error;

            @Override
            protected void onPreExecute() {
                Toast.makeText(DashboardActivity.this, "✅ Validation…", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected JSONObject doInBackground(Void... v) {
                try {
                    return TicketApi.confirmValidation(idToken, API_KEY, code);
                } catch (Exception e) {
                    error = e;
                    Log.e(TAG, "validateTicket error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    textResult.append("\nValidé à: " + result.optString("timestamp"));
                    fetchTicket(); // Recharge l'état du ticket (ex : utilisé = true)
                } else {
                    textResult.setText("Erreur validation: " +
                            (error != null ? error.getMessage() : "Inconnu"));
                    viewStatus.setBackgroundColor(0xFFAA0000);
                }
            }
        }.execute();
    }
}

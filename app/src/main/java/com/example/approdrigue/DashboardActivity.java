package com.example.approdrigue;

import android.content.Intent;                      // ⬅️ manquait
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.auth.options.AuthSignOutOptions;             // ✅ correct
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult;
import com.amplifyframework.core.Amplify;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "Dashboard";
    private static final String API_KEY =
            "dP9BkICR991gxbYDfEwgy6sKONdcEKxo2Tw3Uru9";

    private Button btnLogout, btnCheck, btnValidate;
    private EditText editCode;
    private TextView textResult;
    private View viewStatus;

    private String idToken;
    private boolean ticketValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnLogout   = findViewById(R.id.btnLogout);
        btnCheck    = findViewById(R.id.btnCheck);
        btnValidate = findViewById(R.id.btnValidate);
        editCode    = findViewById(R.id.editCode);
        textResult  = findViewById(R.id.textResult);
        viewStatus  = findViewById(R.id.viewStatus);

        AuthService.TokenPair tokens = AuthService.loadTokens(this);
        idToken = tokens.idToken;

        btnLogout.setOnClickListener(v -> logout());
        btnCheck.setOnClickListener(v -> checkTokenThen(this::fetchTicket));
        btnValidate.setOnClickListener(v -> checkTokenThen(this::validateTicket));
    }

    private void logout() {
        Log.i(TAG, "🚪 Déconnexion globale...");
        AuthSignOutOptions options = AuthSignOutOptions.builder()
                .globalSignOut(true)
                .build();

        Amplify.Auth.signOut(
                options,
                result -> runOnUiThread(() -> {
                    if (result instanceof AWSCognitoAuthSignOutResult.CompleteSignOut) {
                        AuthService.clear(this);
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Déconnexion partielle", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }


    private void checkTokenThen(Runnable action) {
        AuthService.TokenPair tokens = AuthService.loadTokens(this);
        String accessToken = tokens.accessToken;
        if (accessToken == null || isTokenExpired(accessToken)) {
            runOnUiThread(() -> textResult.setText("Token expiré. Reconnectez-vous."));
            return;
        }
        action.run();
    }

    private boolean isTokenExpired(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            JSONObject payload = new JSONObject(new String(
                    Base64.decode(parts[1], Base64.URL_SAFE),
                    StandardCharsets.UTF_8
            ));
            long exp = payload.getLong("exp");
            return System.currentTimeMillis() / 1000 >= exp;
        } catch (Exception e) {
            Log.e(TAG, "Erreur parsing JWT", e);
            return true;
        }
    }

    private void fetchTicket() {
        String code = editCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Entrez un code de ticket.", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    textResult.setText(
                            "Ticket: " + result.optString("n_tickets", code) +
                                    "\n" + (ticketValid ? "✅ VALIDE" : "❌ INVALIDE")
                    );
                    btnValidate.setVisibility(ticketValid ? View.VISIBLE : View.GONE);
                    viewStatus.setBackgroundColor(ticketValid ? 0xFF00AA00 : 0xFFAA0000);
                } else {
                    textResult.setText(
                            "Erreur fetchTicket: " +
                                    (error != null ? error.getMessage() : "Inconnu")
                    );
                    btnValidate.setVisibility(View.GONE);
                    viewStatus.setBackgroundColor(0xFFAA0000);
                }
            }
        }.execute();
    }

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
                    fetchTicket();
                } else {
                    textResult.setText(
                            "Erreur validation: " +
                                    (error != null ? error.getMessage() : "Inconnu")
                    );
                    viewStatus.setBackgroundColor(0xFFAA0000);
                }
            }
        }.execute();
    }
}

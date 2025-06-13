package com.example.approdrigue;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.amplifyframework.core.AmplifyConfiguration;

import com.amplifyframework.auth.options.AuthWebUISignInOptions;

import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnLogout, btnCheck, btnValidate;
    private EditText editCode;
    private TextView textVenue, textResult;
    private View viewStatus;

    private String idToken;
    private boolean ticketValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1️⃣ Initialise Amplify AVANT tout appel à Amplify.Auth
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(
                    AmplifyConfiguration.fromConfigFile(getApplicationContext(), R.raw.amplifyconfiguration),
                    getApplicationContext()
            );

            Log.i("AmplifyDebug", "✅ Amplify configuré");
        } catch (Exception e) {
            Log.e("AmplifyDebug", "❌ Échec de la configuration Amplify", e);
        }


        // 2️⃣ Récupère toutes tes views
        btnLogin   = findViewById(R.id.btnLogin);
        btnLogout  = findViewById(R.id.btnLogout);
        btnCheck   = findViewById(R.id.btnCheck);
        btnValidate= findViewById(R.id.btnValidate);
        editCode   = findViewById(R.id.editCode);
        textVenue  = findViewById(R.id.textVenue);
        textResult = findViewById(R.id.textResult);
        viewStatus = findViewById(R.id.viewStatus);

        // 3️⃣ Branche le clic sur le bouton Login pour appeler login()
        btnLogin.setOnClickListener(v -> {
            Log.i("AmplifyDebug", "Appel de login()");
            login();
        });

        // (… tu peux aussi initialiser ici tes autres listeners, par ex. btnCheck et btnValidate)
    }

    private void login() {
        Amplify.Auth.signInWithWebUI(
                this,
                AuthWebUISignInOptions.builder().build(),
                result -> {
                    Log.i("AuthQuickStart", "signInWithWebUI OK");
                    fetchSessionAndStore();
                    runOnUiThread(() -> textResult.setText("Connecté"));
                },
                error -> {
                    Log.e("AuthQuickStart", "signInWithWebUI ÉCHEC", error);
                    runOnUiThread(() -> textResult.setText("Erreur: " + error.getMessage()));
                }
        );
    }

    private void fetchSessionAndStore() {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    if (result.isSignedIn() && result instanceof AWSCognitoAuthSession) {
                        AWSCognitoAuthSession cognito = (AWSCognitoAuthSession) result;
                        String idToken     = cognito.getUserPoolTokensResult().getValue().getIdToken();
                        String accessToken = cognito.getUserPoolTokensResult().getValue().getAccessToken();
                        AuthService.saveTokens(this, accessToken, idToken);
                        this.idToken = idToken;
                        runOnUiThread(() -> {
                            textResult.setText("Connecté");
                            btnLogout.setVisibility(View.VISIBLE);
                            fetchVenue();
                        });
                    }
                },
                error -> Log.e("MainActivity", "Session error", error)
        );
    }

    private void logout() {
        Amplify.Auth.signOut(result -> {
            // Ton code logout reste inchangé
            if (result instanceof com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult.CompleteSignOut) {
                runOnUiThread(() -> textResult.setText("Déconnecté"));
            } else if (result instanceof com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult.PartialSignOut) {
                runOnUiThread(() -> textResult.setText("Déconnexion partielle"));
            } else if (result instanceof com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult.FailedSignOut) {
                runOnUiThread(() -> textResult.setText("Échec déconnexion"));
            }
        });
    }

    private void fetchVenue() {
        if (idToken == null || JwtUtils.isTokenExpired(idToken)) {
            runOnUiThread(() -> textResult.setText("Token expiré, veuillez vous reconnecter"));
            return;
        }
        new AsyncTask<Void, Void, JSONObject>() {
            Exception error;
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    return TicketApi.fetchVenueConfig(idToken);
                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }
            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    textVenue.setText("Salle: " + result.optString("venue_id"));
                } else {
                    textResult.setText("Erreur: " + error);
                }
            }
        }.execute();
    }

    private void fetchTicket() {
        final String code = editCode.getText().toString().trim();
        if (idToken == null || JwtUtils.isTokenExpired(idToken)) {
            textResult.setText("Token expiré, veuillez vous reconnecter");
            return;
        }
        new AsyncTask<Void, Void, JSONObject>() {
            Exception error;
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    return TicketApi.fetchTicket(idToken, code);
                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }
            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    int status = result.optInt("status", 1);
                    boolean used = result.optBoolean("used", false);
                    ticketValid = (status == 0 && !used);
                    String codeQR = result.optString("n_tickets", code);
                    textResult.setText("Ticket: " + codeQR + "\n" +
                            (ticketValid ? "TICKET VALIDE" : "TICKET INVALIDE"));
                    btnValidate.setVisibility(ticketValid ? View.VISIBLE : View.GONE);
                    viewStatus.setBackgroundColor(ticketValid ? 0xFF00AA00 : 0xFFAA0000);
                } else {
                    textResult.setText("Erreur: " + error);
                }
            }
        }.execute();
    }

    private void validateTicket() {
        final String code = editCode.getText().toString().trim();
        if (idToken == null || JwtUtils.isTokenExpired(idToken)) {
            textResult.setText("Token expiré, veuillez vous reconnecter");
            return;
        }
        new AsyncTask<Void, Void, JSONObject>() {
            Exception error;
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    return TicketApi.confirmValidation(idToken, code);
                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }
            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    textResult.append("\nValidé à: " + result.optString("timestamp"));
                    viewStatus.setBackgroundColor(0xFF00AA00);
                    btnValidate.setVisibility(View.GONE);
                } else {
                    textResult.setText("Erreur: " + error);
                }
            }
        }.execute();
    }
}

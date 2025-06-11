package com.example.approdrigue;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;

import org.json.JSONObject;

/**
 * Basic UI to interact with the ticket API.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnLogout;
    private Button btnCheck;
    private Button btnValidate;
    private EditText editCode;
    private TextView textVenue;
    private TextView textResult;
    private View viewStatus;

    private String idToken;
    private boolean ticketValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
        } catch (Exception e) {
            Log.e("MainActivity", "Amplify init failed", e);
        }

        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);
        btnCheck = findViewById(R.id.btnCheck);
        btnValidate = findViewById(R.id.btnValidate);
        editCode = findViewById(R.id.editCode);
        textVenue = findViewById(R.id.textVenue);
        textResult = findViewById(R.id.textResult);
        viewStatus = findViewById(R.id.viewStatus);

        loadTokens();

        btnLogin.setOnClickListener(v -> login());
        btnLogout.setOnClickListener(v -> logout());
        btnCheck.setOnClickListener(v -> fetchTicket());
        btnValidate.setOnClickListener(v -> validateTicket());
    }

    private void loadTokens() {
        AuthService.TokenPair pair = AuthService.loadTokens(this);
        idToken = pair.idToken;
        if (idToken != null && !JwtUtils.isTokenExpired(idToken)) {
            btnLogout.setVisibility(View.VISIBLE);
            fetchVenue();
        } else if (idToken != null) {
            // Token expired, clear it
            AuthService.deleteTokens(this);
            idToken = null;
        }
    }

    private void login() {
        Amplify.Auth.signInWithWebUI(
                this,
                result -> {
                    fetchSessionAndStore();
                    runOnUiThread(() -> textResult.setText("Connecté"));
                },
                error -> runOnUiThread(() -> textResult.setText("Erreur: " + error.getMessage()))
        );
    }

    private void fetchSessionAndStore() {
        Amplify.Auth.fetchAuthSession(
                session -> {
                    AWSCognitoAuthSession cognito = (AWSCognitoAuthSession) session;
                    if (cognito.isSignedIn()) {
                        String access = cognito.getUserPoolTokens().getAccessToken();
                        String id = cognito.getUserPoolTokens().getIdToken();
                        AuthService.saveTokens(this, access, id);
                        idToken = id;
                        try {
                            JSONObject claims = JwtUtils.decodePayload(id);
                            Log.i("MainActivity", "JWT claims: " + claims.toString());
                        } catch (Exception e) {
                            Log.w("MainActivity", "Failed to decode token", e);
                        }
                        runOnUiThread(() -> {
                            btnLogout.setVisibility(View.VISIBLE);
                            fetchVenue();
                        });
                    }
                },
                error -> Log.e("MainActivity", "Session error", error)
        );
    }

    private void logout() {
        Amplify.Auth.signOut(
                () -> runOnUiThread(() -> textResult.setText("Déconnecté")),
                error -> runOnUiThread(() -> textResult.setText("Erreur: " + error.getMessage()))
        );
        AuthService.deleteTokens(this);
        idToken = null;
        btnLogout.setVisibility(View.GONE);
        viewStatus.setBackgroundColor(0xFFCCCCCC);
    }

    private void fetchVenue() {
        if (idToken == null || JwtUtils.isTokenExpired(idToken)) {
            textResult.setText("Token expiré, veuillez vous reconnecter");
            return;
        }
        new AsyncTask<Void,Void,JSONObject>(){
            Exception error;
            @Override protected JSONObject doInBackground(Void... voids) {
                try {
                    return TicketApi.fetchVenueConfig(idToken);
                } catch (Exception e) { error = e; return null; }
            }
            @Override protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    textVenue.setText("Salle: "+result.optString("venue_id"));
                } else {
                    textResult.setText("Erreur: "+error);
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
        new AsyncTask<Void,Void,JSONObject>(){
            Exception error;
            @Override protected JSONObject doInBackground(Void... voids) {
                try {
                    return TicketApi.fetchTicket(idToken, code);
                } catch (Exception e) { error = e; return null; }
            }
            @Override protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    int statut = result.optInt("statut", 1);
                    boolean used = result.optBoolean("utilisé", false);
                    ticketValid = (statut == 0 && !used);
                    String codeQR = result.optString("n_tickets", code);
                    textResult.setText("Ticket: "+codeQR+"\n"+(ticketValid?"TICKET VALIDE":"TICKET INVALIDE"));
                    btnValidate.setVisibility(ticketValid ? View.VISIBLE : View.GONE);
                    viewStatus.setBackgroundColor(ticketValid ? 0xFF00AA00 : 0xFFAA0000);
                } else {
                    textResult.setText("Erreur: "+error);
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
        new AsyncTask<Void,Void,JSONObject>(){
            Exception error;
            @Override protected JSONObject doInBackground(Void... voids) {
                try {
                    return TicketApi.confirmValidation(idToken, code);
                } catch (Exception e) { error = e; return null; }
            }
            @Override protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    textResult.append("\nValidé à: "+result.optString("timestamp"));
                    viewStatus.setBackgroundColor(0xFFAA0000);
                    btnValidate.setVisibility(View.GONE);
                } else {
                    textResult.setText("Erreur: "+error);
                }
            }
        }.execute();
    }
}

package com.example.approdrigue;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
        if (idToken != null) {
            btnLogout.setVisibility(View.VISIBLE);
            fetchVenue();
        }
    }

    private void login() {
        // Placeholder: you would implement Cognito sign-in here
        textResult.setText("Connexion non implémentée");
    }

    private void logout() {
        AuthService.deleteTokens(this);
        idToken = null;
        btnLogout.setVisibility(View.GONE);
        textResult.setText("Déconnecté");
        viewStatus.setBackgroundColor(0xFFCCCCCC);
    }

    private void fetchVenue() {
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

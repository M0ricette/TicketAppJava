package com.example.approdrigue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.auth.options.AuthWebUISignInOptions;
import com.amplifyframework.auth.result.AuthSessionResult;
import com.amplifyframework.core.Amplify;

/**
 * Activité de connexion qui utilise Amazon Cognito via AWS Amplify.
 * Elle permet à l'utilisateur de se connecter avec une interface web (OAuth).
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity"; // Pour les logs
    private Button btnLogin; // Bouton de connexion

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 Étape 1 : (facultatif ici) on pourrait configurer Amplify ici,
        // mais apparemment c'est déjà fait dans une autre classe.
        try {
            Log.i(TAG, "Amplify configuré"); // Juste un log ici
        } catch (Exception e) {
            Log.e(TAG, "Erreur configuration Amplify", e);
        }

        // 🔹 Étape 2 : Affichage de la vue `activity_login.xml`
        setContentView(R.layout.activity_login);

        // 🔹 Récupère le bouton de login dans le layout
        btnLogin = findViewById(R.id.btnLogin);

        // 🔹 Quand on clique sur le bouton, on lance la procédure de connexion
        btnLogin.setOnClickListener(v -> startSignIn());

        // 🔹 Étape 3 : Vérifie si l'utilisateur est déjà connecté
        Amplify.Auth.fetchAuthSession(
                session -> {
                    // ✅ Si l'utilisateur est connecté, on peut directement passer à l'écran suivant
                    if (session.isSignedIn() &&
                            session instanceof AWSCognitoAuthSession) {
                        goToDashboard();
                    }
                },
                error -> Log.e(TAG, "Échec fetchAuthSession", error) // ❌ Si erreur, on affiche un log
        );
    }

    /**
     * Cette méthode est appelée quand l’activité reçoit un "Intent" après une redirection web (OAuth).
     * Elle permet de finaliser la connexion Cognito.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Amplify.Auth.handleWebUISignInResponse(intent); // Permet de finaliser la redirection de Cognito
    }

    /**
     * Démarre le processus de connexion via l’interface WebUI de Cognito.
     */
    private void startSignIn() {
        Amplify.Auth.signInWithWebUI(
                this, // contexte Android
                AuthWebUISignInOptions.builder().build(), // options par défaut
                result -> {
                    // ✅ Connexion réussie
                    Log.i(TAG, "Connexion réussie");
                    fetchSessionAndProceed(); // On récupère les tokens
                },
                error -> {
                    // ❌ Si une erreur se produit
                    String msg = error.getMessage() != null
                            ? error.getMessage().toLowerCase()
                            : "";

                    // Cas particulier : utilisateur déjà connecté
                    if (msg.contains("already a user signed in")) {
                        Log.w(TAG, "Déjà connecté, on poursuit");
                        fetchSessionAndProceed();
                    } else {
                        // On affiche un message d'erreur dans l'UI
                        runOnUiThread(() -> {
                            Log.e(TAG, "Erreur signInWithWebUI", error);
                            Toast.makeText(this,
                                    "Échec connexion: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    /**
     * Récupère les tokens JWT depuis la session Cognito,
     * puis les enregistre en local pour les futurs appels API.
     */
    private void fetchSessionAndProceed() {
        Amplify.Auth.fetchAuthSession(
                session -> {
                    // ✅ Vérifie que la session est valide et qu’on utilise Cognito
                    if (session.isSignedIn() &&
                            session instanceof AWSCognitoAuthSession) {
                        AWSCognitoAuthSession cognito =
                                (AWSCognitoAuthSession) session;

                        // ✅ On vérifie que les tokens sont bien récupérés
                        if (cognito.getUserPoolTokensResult()
                                .getType() == AuthSessionResult.Type.SUCCESS) {

                            // 🔐 On récupère les tokens JWT
                            String accessToken =
                                    cognito.getUserPoolTokensResult()
                                            .getValue()
                                            .getAccessToken();

                            String idToken =
                                    cognito.getUserPoolTokensResult()
                                            .getValue()
                                            .getIdToken();

                            // 💾 On les sauvegarde via une classe utilitaire (non visible ici)
                            AuthService.saveTokens(this, accessToken, idToken);

                            // 🔁 Ensuite on passe à l’activité principale
                            goToDashboard();
                        }
                    }
                },
                error -> Log.e(TAG, "Erreur fetchAuthSession", error) // ❌ Log si erreur
        );
    }

    /**
     * Redirige l’utilisateur vers l’activité Dashboard.
     */
    private void goToDashboard() {
        runOnUiThread(() -> {
            // Crée une Intent vers l’activité Dashboard
            startActivity(
                    new Intent(LoginActivity.this,
                            DashboardActivity.class)
            );
            finish(); // Termine LoginActivity pour éviter de revenir dessus
        });
    }
}

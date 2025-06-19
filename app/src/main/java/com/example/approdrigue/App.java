// app/src/main/java/com/example/approdrigue/App.java
package com.example.approdrigue;

import android.app.Application;
import android.util.Log;

import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

public class App extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i(TAG, "✅ Amplify configuré une fois");
        } catch (Exception e) {
            Log.w(TAG, "🔄 Amplify déjà configuré ou erreur lors de la config", e);
        }
    }
}

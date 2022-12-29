package com.example.weatherapiapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.spotify.sdk.android.auth.LoginActivity;

import Connectors.UserService;
import Connectors.VolleyCallBack;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private SharedPreferences msharedPreferences;
    private RequestQueue queue;
    private SharedPreferences.Editor editor;

    private static final String CLIENT_ID = "";
    private static final String REDIRECT_URI = "";
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";
    //

    private void authenticateSpotify() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

//    private void waitForUserInfo() {
//        UserService userService = new UserService(queue, msharedPreferences);
//        userService.get(() -> {
//            User user = userService.getUser();
//            editor = getSharedPreferences("SPOTIFY", 0).edit();
//            editor.putString("userid", user.id);
//            Log.d("STARTING", "GOT USER INFORMATION");
//            // We use commit instead of apply because we need the information stored immediately
//            editor.commit();
//            startMainActivity();
//        });
//    }

    private void waitForUserInfo() {
        UserService userService = new UserService(queue, msharedPreferences);
        userService.get(new VolleyCallBack() {
            @Override
            public void onSuccess() {
                User user = userService.getUser();
                editor = getSharedPreferences("SPOTIFY", 0).edit();
                editor.putString("userid", user.id);
                Log.d("STARTING", "GOT USER INFORMATION");
                // We use commit instead of apply because we need the information stored immediately
                editor.commit();
                startMainActivity();
            }
        });
    }


    private void startMainActivity() {
        Intent newintent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(newintent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    logMessage("Got token: " + response.getAccessToken());
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.d("STARTING", "GOT AUTH TOKEN");
                    editor.apply();
                    waitForUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    logError("Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    logError("Auth result: " + response.getError());
            }
        }
    }

    private void logMessage(String msg) {
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG, msg);
    }

    private void logError(String msg) {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);


        authenticateSpotify();

        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);
        //queue = Volley.newRequestQueue(this);

    }
}
package com.example.weatherapiapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Connectors.SongService;
import Connectors.VolleyCallBack;

public class MainActivity extends Activity {
    private TextView userView;
    private TextView songView;
    private Button addBtn;
    private Song song;

    private SongService songService;
    private ArrayList<Song> recentlyPlayedTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songService = new SongService(getApplicationContext());
        userView = (TextView) findViewById(R.id.user);
        songView = (TextView) findViewById(R.id.song);
        addBtn = (Button) findViewById(R.id.add);

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        userView.setText(sharedPreferences.getString("userid", "No User"));

        String url = "https://api.spotify.com/v1/me/playlists";
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(MainActivity.this, "king", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);

        getTracks();

        addBtn.setOnClickListener(addListener);

    }



    private View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            songService.addSongToLibrary(song);
            if (recentlyPlayedTracks.size() > 0) {
                recentlyPlayedTracks.remove(0);
            }
            updateSong();
        }
    };


    private void getTracks() {
        songService.getRecentlyPlayedTracks(new VolleyCallBack() {
            @Override
            public void onSuccess() {
                recentlyPlayedTracks = songService.getSongs();
                updateSong();
            }
        });
    }

    private void updateSong() {
        if (recentlyPlayedTracks.size() > 0) {
            songView.setText(recentlyPlayedTracks.get(0).getName());
            song = recentlyPlayedTracks.get(0);
        }
    }
}

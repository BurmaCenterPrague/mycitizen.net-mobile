package net.mycitizen.mcn;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by chris on 24/10/14.
 */
public class DetailYoutubeVideo extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener {

    private YouTubePlayer YPlayer;
    private static final String YoutubeDeveloperKey = "AIzaSyDMEFeiY8svbm6ruunPtE_fjCnDCe0ZogY";
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private String videoId;
    public ApiConnector api;

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_youtube);

        try {
            api = new ApiConnector(this);
            if (!api.isNetworkAvailable()) {
                Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                finish();
            }

            videoId = getIntent().getStringExtra("videoId");
            Log.d(Config.DEBUG_TAG, "videoId: " + videoId);

            if (videoId == null || videoId.equals("")) {
                Toast.makeText(getApplicationContext(), "Wrong video ID.", Toast.LENGTH_LONG).show();
                finish();
            }

            setTitle(getIntent().getStringExtra("title"));

            String url = "http://gdata.youtube.com/feeds/api/videos/" + videoId + "?v=2&alt=json";

            downloadTask task = new downloadTask();
            task.execute(url);

            RelativeLayout video_wrapper = (RelativeLayout) findViewById(R.id.detail_video_youtube_wrapper_api);
            video_wrapper.setVisibility(View.VISIBLE);
            YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
            youTubeView.initialize(YoutubeDeveloperKey, this);
        } catch (Exception e) {
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(
                    "There was an error initializing the YouTubePlayer",
                    errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
// Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(YoutubeDeveloperKey, this);
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            YPlayer = player;
/*
* Now that this variable YPlayer is global you can access it
* throughout the activity, and perform all the player actions like
* play, pause and seeking to a position by code.
*/
            YPlayer.cueVideo(videoId);
        }
    }

    private class downloadTask extends AsyncTask<String, Void, String> {

        String description = "";
        TextView description_view = (TextView) findViewById(R.id.description_view);

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            Log.d(Config.DEBUG_TAG, "Trying url: " + url);
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.d(Config.DEBUG_TAG, "Failed to download description");
                }
            } catch (ClientProtocolException e) {
                Log.d(Config.DEBUG_TAG, "ClientProtocolException");
                description = getString(R.string.error_loading_data);
                description_view.setText(Html.fromHtml(description));
            } catch (IOException e) {
                Log.d(Config.DEBUG_TAG, "IOException");
                description = getString(R.string.error_loading_data);
                description_view.setText(Html.fromHtml(description));
            }
            return builder.toString();

        }

        @Override
        protected void onPostExecute(String result) {

            Log.d(Config.DEBUG_TAG, "Description: " + result);
            String description;
            TextView description_view = (TextView) findViewById(R.id.description_view);
            try {
                JSONObject json = new JSONObject(result);
                JSONObject entry = json.getJSONObject("entry");
                JSONObject group = entry.getJSONObject("media$group");
                JSONObject text = group.getJSONObject("media$description");
                description = text.getString("$t");
            } catch (JSONException e) {
                description = getString(R.string.error_loading_data);
                Log.d(Config.DEBUG_TAG, "fields not found");
            }
            // Log.d(Config.DEBUG_TAG, description);

            description = description.replaceAll("\\r?\\n", "<br />");
            description = description.replace("  ", " &emsp;");
            // make links clickable
            description = description.replaceAll("(\\A|\\s)((http|https|ftp|mailto):\\S+)(\\s|\\z)",
                    "$1<a href=\"$2\">$2</a>$4");
            description_view.setText(Html.fromHtml(description));
            description_view.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }


}

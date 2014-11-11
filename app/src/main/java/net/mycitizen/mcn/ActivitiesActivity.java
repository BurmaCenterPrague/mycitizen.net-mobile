/*
 * Copyright (C) 2013ff. mycitizen.net
 *
 * Licensed under the GPLv3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mycitizen.mcn;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by christoph on 08.11.14.
 */
public class ActivitiesActivity extends BaseActivity {

    String Activities;
    TextView activities_header;
    WebView activities_content;
    String timeframe;
    ActionBar actionBar;
    String header, content;
    String[] content_cache = new String[4];
    String[] header_cache = new String[4];
    long timeUpdated;
    int id = 0;

    ProgressDialog loader = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activities_view);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.recent_activities));
        actionBar.setIcon(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activities_content = (WebView) findViewById(R.id.activites_content);
        activities_header = (TextView) findViewById(R.id.activities_header);

        if (timeframe == null) {
            timeframe = "today";
        }


        final Button activities_more = (Button) findViewById(R.id.activities_more);
        activities_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timeframe.equals("today")) {
                    timeframe = "yesterday";
                } else if (timeframe.equals("yesterday")) {
                    timeframe = "week";
                } else if (timeframe.equals("week")) {
                    timeframe = "month";
                    //activities_more.setVisibility(View.GONE);
                } else if (timeframe.equals("month")) {
                    timeframe = "today";
                }

                loader = loadingDialog();
                DashboardInit task = new DashboardInit();
                task.execute(timeframe);
            }
        });


        loader = loadingDialog();
        DashboardInit task = new DashboardInit();
        task.execute(timeframe);

    }


    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class DashboardInit extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... input) {
            String timeframe = input[0];

            if (timeframe == null) return "error";

            if (System.currentTimeMillis() - timeUpdated < 300000) {
                if (timeframe.equals("today") && content_cache[0] != null && header_cache[0] != null) {
                    content = content_cache[0];
                    header = header_cache[0];
                    return null;
                } else if (timeframe.equals("yesterday") && content_cache[1] != null && header_cache[1] != null) {
                    content = content_cache[1];
                    header = header_cache[1];
                    return null;
                } else if (timeframe.equals("week") && content_cache[2] != null && header_cache[2] != null) {
                    content = content_cache[2];
                    header = header_cache[2];
                    return null;
                } else if (timeframe.equals("month") && content_cache[3] != null && header_cache[3] != null) {
                    content = content_cache[3];
                    header = header_cache[3];
                    return null;
                }
            }

            ApiConnector api = new ApiConnector(ActivitiesActivity.this);
            ArrayList<String> data = null;
            if (api.sessionInitiated()) {
                data = api.createActivities(timeframe);
            }

            timeUpdated = System.currentTimeMillis();

            if (data == null || data.size() != 2) {
                header = "";
                content = getString(R.string.error_loading_data);
            } else {
                header = data.get(0);
                content = data.get(1);

                if (timeframe.equals("today")) {
                    content_cache[0] = content;
                    header_cache[0] = header;
                } else if (timeframe.equals("yesterday")) {
                    content_cache[1] = content;
                    header_cache[1] = header;
                } else if (timeframe.equals("week")) {
                    content_cache[2] = content;
                    header_cache[2] = header;
                    //activities_more.setVisibility(View.GONE);
                } else if (timeframe.equals("month")) {
                    content_cache[3] = content;
                    header_cache[3] = header;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null && result.equals("error")) return;

            activities_header.setText(header);

            // Attribute id added to html to force WebView to update the page.
            activities_content.loadData("<html id=\""+String.valueOf(id)+"\"><body style=\"background-color:#D0CCC3;\">"+content+"</body></html>", "text/html", null);
            if (loader != null) loader.dismiss();
            id++;
        }
    }

}

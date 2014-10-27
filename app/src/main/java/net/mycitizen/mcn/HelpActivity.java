package net.mycitizen.mcn;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.widget.TextView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;

public class HelpActivity extends ActionBarActivity {
    ProgressDialog loader = null;

    TextView help_content;
    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.help));
        actionBar.setIcon(null);

        loader = loadingDialog();

        help_content = (TextView) findViewById(R.id.help_content);

        Intent intent = getIntent();

        String topic = intent.getStringExtra("topic");

        Uri data = getIntent().getData();
        if (data != null) {
            System.out.println(data.toString());
            topic = data.toString().replace("net.mycitizen.mcn://", "");
        }
        DashboardInit task = new DashboardInit();
        task.execute(new String[]{topic});

    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class DashboardInit extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(HelpActivity.this);
            String data = null;
            if (api.sessionInitiated()) {
                data = api.createHelp(type[0]);
            }

            if (data == null) {
                data = "";
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            //parse data to view
            help_content.setText(Html.fromHtml(result));
            //Linkify.addLinks(help_content, Linkify.ALL);
            help_content.setMovementMethod(LinkMovementMethod.getInstance());
            loader.dismiss();
        }
    }


}

package net.mycitizen.mcn;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class LostPasswordActivity extends ActionBarActivity {
    ProgressDialog loader = null;

    EditText email;
    Button send;

    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lost_password_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Forgot Password");
        actionBar.setIcon(null);
        email = (EditText) findViewById(R.id.user_email);

        send = (Button) findViewById(R.id.send_email);
        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                loader = loadingDialog();

                DashboardInit task = new DashboardInit();
                task.execute(new String[]{email.getText().toString()});
            }
        });


        Button close = (Button) findViewById(R.id.close_help);
        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LostPasswordActivity.this, HelpActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("topic", "forgot_password");

                startActivity(intent);
            }
        });
    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class DashboardInit extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(LostPasswordActivity.this);
            Boolean data = null;
            //if(api.sessionInitiated()) {
            data = api.requestLostPassword(type[0]);
            //}	
            return data;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //parse data to view


            loader.dismiss();

            if (result) {
                Toast.makeText(getApplicationContext(), getString(R.string.email_sent), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.email_not_sent), Toast.LENGTH_LONG).show();
            }
        }
    }


}

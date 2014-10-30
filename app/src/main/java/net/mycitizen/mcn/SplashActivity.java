package net.mycitizen.mcn;

import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class SplashActivity extends ActionBarActivity {
    public ApiConnector api;

    String saved_login;
    String saved_password;

    ProgressDialog loader = null;

    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        actionBar = getSupportActionBar();
        actionBar.setTitle("MyCitizen");
        actionBar.setIcon(null);

        SharedPreferences settings = SplashActivity.this.getSharedPreferences(Config.localStorageName, 0);

        String PHPSESSID = settings.getString("PHPSESSID", null);
        saved_login = settings.getString("login", null);
        saved_password = settings.getString("password", null);

        if (settings.getString("ui_language", null) != null) {
            String loc = "en";
            if (settings.getString("ui_language", null).equals("ces")) {
                loc = "cs_MM";
            } else if (settings.getString("ui_language", null).equals("hlt")) {
                loc = "ht_MM";
            } else if (settings.getString("ui_language", null).equals("mrh")) {
                loc = "mt_MM";
            } else if (settings.getString("ui_language", null).equals("zom")) {
                loc = "zm_MM";
            } else {
                loc = "en";
            }
            System.out.println("Locale set to saved language: " + settings.getString("ui_language", null));
            Locale locale = new Locale(loc);

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        //api = new ApiConnector(LoginActivity.this);

        if (saved_login != null && saved_password != null) {


            loader = loadingDialog();

            AppInit task = new AppInit();
            task.execute(new String[]{"nothing"});
        } else {

            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

            //String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);

            startActivity(intent);

            finish();

        }


    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class AppInit extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            //Looper.prepare();


            api = new ApiConnector(SplashActivity.this);

            api.removeSession();

            api.sessionInit(saved_login, saved_password);

            if (api.sessionInitiated()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {
                Intent intent = new Intent(SplashActivity.this, WidgetActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
                try {
                    loader.dismiss();
                } catch (Exception e) {
                }

                SplashActivity.this.finish();
            } else {
                try {
                    loader.dismiss();
                } catch (Exception e) {
                }

                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);

                SplashActivity.this.finish();
            }
        }
    }


}

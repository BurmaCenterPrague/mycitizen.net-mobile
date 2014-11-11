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
import android.util.Log;

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
        actionBar.setTitle("MyCitizen.net");
        actionBar.setIcon(null);

        SharedPreferences settings = SplashActivity.this.getSharedPreferences(Config.localStorageName, 0);

        String PHPSESSID = settings.getString("PHPSESSID", null);
        saved_login = settings.getString("login", null);
        saved_password = settings.getString("password", null);

        if (settings.getString("ui_language", null) != null) {
            String loc = Config.codeToLocale(getApplicationContext(), settings.getString("ui_language", null));
            Log.d(Config.DEBUG_TAG, "Locale set to saved language: " + settings.getString("ui_language", null));
            Locale locale = new Locale(loc);

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
            super.onConfigurationChanged(config);
        }


        if (saved_login != null && saved_password != null) {
            // resetting temporary variables
            SharedPreferences user_settings = getSharedPreferences("MyCitizen", 0);
            SharedPreferences.Editor editor = user_settings.edit();
            editor.putInt("connection_strength", 50);
            editor.putLong("time_connection_measured", 0);
            editor.putLong("last_network_request", 2001);
            editor.putLong("time_connection_measured", 0);
            editor.putLong("time_users_retrieved", 0);
            editor.putLong("time_groups_retrieved", 0);
            editor.putLong("time_resources_retrieved", 0);
            editor.putLong("last_time_retrieved_tags", 0);
            editor.commit();
            loader = loadingDialog();


            AppInit task = new AppInit();
            task.execute("nothing");
        } else {

            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

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

                api.determineConnectionQuality();

                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {
                Intent intent = new Intent(SplashActivity.this, WidgetActivity.class);


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

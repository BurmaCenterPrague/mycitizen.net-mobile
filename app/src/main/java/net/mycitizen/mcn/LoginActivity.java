package net.mycitizen.mcn;


import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.ToggleButton;

import java.util.Locale;

public class LoginActivity extends ActionBarActivity {
    public ApiConnector api;

    ProgressDialog loader = null;

    String login_field_1;
    String password_field_1;

    CheckBox remember_me;

    Button password_field, login_field, login_button, forgotten_password;

    ActionBar actionBar;

    AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();

        String activityType = intent.getStringExtra("type");

        setContentView(R.layout.login_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setIcon(null);
        actionBar.hide();

        SharedPreferences settings = LoginActivity.this.getSharedPreferences(Config.localStorageName, 0);

        String saved_login = settings.getString("login", null);
        String saved_password = settings.getString("password", null);
        String api = settings.getString("usedApi", "");

        if (settings.getString("ui_language", null) != null) {
            String loc = Config.codeToLocale(getApplicationContext(), settings.getString("ui_language", null));
            Log.d(Config.DEBUG_TAG, "Locale set to language: " + settings.getString("ui_language", null));
            Locale locale = new Locale(loc);

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
            super.onConfigurationChanged(config);
        }


        if (settings.getString("userAgent", null) == null) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("userAgent", new WebView(this).getSettings().getUserAgentString());
            editor.commit();
        }

        Log.d(Config.DEBUG_TAG, "LoginActivity.java: SAVED " + saved_login + " " + saved_password);


        if (saved_login != null && saved_password != null) {
            loader = loadingDialog();

            EditText login_f = (EditText) findViewById(R.id.login_login);
            login_f.setText(saved_login);
            EditText password_f = (EditText) findViewById(R.id.login_password);
            password_f.setText(saved_password);

            login_field_1 = saved_login;
            password_field_1 = saved_password;

                LoggingIn task = new LoggingIn();
                task.execute("nothing");

        }

        remember_me = (CheckBox) findViewById(R.id.login_remember);

        if (activityType != null && activityType.equals("fromRegister")) {
            EditText login_field = (EditText) findViewById(R.id.login_login);
            login_field.setText(intent.getStringExtra("username"));
            EditText password_field = (EditText) findViewById(R.id.login_password);
            password_field.setText(intent.getStringExtra("password"));
            Toast.makeText(getApplicationContext(), getString(R.string.registration_finish), Toast.LENGTH_LONG).show();
        } else if (activityType != null && activityType.equals("apiError")) {
            Toast.makeText(getApplicationContext(), getString(R.string.apierror), Toast.LENGTH_LONG).show();
        }


        final ToggleButton show_password = (ToggleButton) findViewById(R.id.show_password);

        show_password.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText password_field = (EditText) findViewById(R.id.login_password);

                if (show_password.isChecked()) {
                    password_field.setTransformationMethod(null);
                    password_field.setSelection(password_field.getText().length());
                } else {
                    password_field.setTransformationMethod(new PasswordTransformationMethod());
                    password_field.setSelection(password_field.getText().length());
                }
            }
        });

        final ImageView logo = (ImageView) findViewById(R.id.logo);

        logo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mycitizen.net"));
                startActivity(browserIntent);
            }
        });


        login_button = (Button) findViewById(R.id.login_submit);

        //Typeface tf_mm3 = Typeface.createFromAsset(getAssets(), "fonts/mm3.ttf");
        //login.setTypeface(tf_mm3);

        login_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                ApiConnector api = new ApiConnector(getApplicationContext());
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), R.string.not_available_offline, Toast.LENGTH_LONG).show();
                    return;
                }
                loader = loadingDialog();

                EditText login_field = (EditText) findViewById(R.id.login_login);
                EditText password_field = (EditText) findViewById(R.id.login_password);


                if (!login_field.getText().toString().equals("") && !password_field.getText().toString().equals("")) {

                    login_field_1 = login_field.getText().toString();
                    password_field_1 = password_field.getText().toString();

                    LoggingIn task = new LoggingIn();
                    task.execute("nothing");


                } else {
                    loader.dismiss();
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error_empty), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button getstarted = (Button) findViewById(R.id.login_getstarted);
        getstarted.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, GetStartedActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
                finish();
            }
        });

        Button help = (Button) findViewById(R.id.login_help);
        help.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, HelpActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("topic", "login");

                startActivity(intent);
            }
        });


        forgotten_password = (Button) findViewById(R.id.login_forgotten_password);
        forgotten_password.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LostPasswordActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);


                startActivity(intent);
            }
        });

        welcomeScreen();

    }

    @Override
    protected void onResume() {
        super.onResume();

        welcomeScreen();

    }


    private void welcomeScreen() {
        SharedPreferences settings = LoginActivity.this.getSharedPreferences(Config.localStorageName, 0);

        EditText password_field = (EditText) findViewById(R.id.login_password);
        EditText login_field = (EditText) findViewById(R.id.login_login);
        String api = settings.getString("usedApi", "");
        Button login_submit = (Button) findViewById(R.id.login_submit);
        Button login_forgotten_password = (Button) findViewById(R.id.login_forgotten_password);
        CheckBox remember_me = (CheckBox) findViewById(R.id.login_remember);
        LinearLayout login_welcome = (LinearLayout) findViewById(R.id.login_welcome);
        ToggleButton show_password = (ToggleButton) findViewById(R.id.show_password);


        Log.d(Config.DEBUG_TAG, "api: " + api);
        if (api.equals("")) {
            password_field.setVisibility(View.GONE); //setEnabled(false);
            login_field.setVisibility(View.GONE); //setEnabled(false);
            login_submit.setVisibility(View.GONE);
            login_forgotten_password.setEnabled(false);
            login_forgotten_password.setText("");
            remember_me.setVisibility(View.GONE);
            show_password.setVisibility(View.GONE);
            login_welcome.setVisibility(View.VISIBLE);

        } else {
            password_field.setVisibility(View.VISIBLE); //setEnabled(true);
            login_field.setVisibility(View.VISIBLE); //setEnabled(true);
            login_submit.setVisibility(View.VISIBLE);
            login_forgotten_password.setEnabled(true);
            login_forgotten_password.setText(R.string.forgotpassword);
            remember_me.setVisibility(View.VISIBLE);
            show_password.setVisibility(View.VISIBLE);
            login_welcome.setVisibility(View.GONE);
        }

    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class LoggingIn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            //Looper.prepare();
            api = new ApiConnector(LoginActivity.this);

            api.removeSession();
            String result = api.sessionInit(login_field_1, password_field_1);

            if (api.sessionInitiated()) {
                return "success";
            } else {
                return result;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            SharedPreferences settings = LoginActivity.this.getSharedPreferences(Config.localStorageName, 0);

            String saved_login = settings.getString("login", null);
            String saved_password = settings.getString("password", null);

            SharedPreferences save_settings = LoginActivity.this.getSharedPreferences("MyCitizen", 0);
            SharedPreferences.Editor editor = save_settings.edit();

            if (result.equals("success")) {

                if (saved_login == null || saved_password == null) {


                    editor.putString("current_login", login_field_1);

                    if (remember_me.isChecked()) {
                        editor.putString("login", login_field_1);
                        editor.putString("password", password_field_1);
                        editor.commit();

                    } else {
                        editor.putString("login", login_field_1);
                        editor.putString("password", null);

                        editor.commit();

                    }

                    Intent incoming_intent = getIntent();
                    String activityType = incoming_intent.getStringExtra("type");

                    Intent intent;
                    int visits = settings.getInt("number_visits",0);
                    Log.d(Config.DEBUG_TAG, "visits: " + visits);

                    int filled = 0;

                    int logged_user_id = settings.getInt("logged_user_id", 0);
                    Log.d(Config.DEBUG_TAG, "logged_user_id: " + logged_user_id);

                    if (logged_user_id != 0) {
                       filled = settings.getInt("profile_filled",0);
                    }

                    Log.d(Config.DEBUG_TAG, "filled: " + filled);

                    intent = new Intent(LoginActivity.this, WidgetActivity.class);
                    if ((activityType != null && activityType.equals("fromRegister")) || (visits < 15 && filled < 2)) {
                        intent.putExtra("profile", "incomplete");
                    }

                    intent.putExtra("origin", "login");


                    startActivity(intent);
                    if (loader != null) {
                        loader.dismiss();
                    }

                    finish();
                }

            } else {
                if (loader != null) {
                    loader.dismiss();
                }
                Log.d(Config.DEBUG_TAG, "LOGIN RESPONSE " + result);

                if (result.equals("not_active")) {
                    editor.putString("login", null);
                    editor.putString("password", null);

                    editor.commit();
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error_unactive), Toast.LENGTH_LONG).show();
                } else if (result.equals("login_failed")) {
                    editor.putString("login", null);
                    editor.putString("password", null);

                    editor.commit();
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error_login_failed), Toast.LENGTH_LONG).show();
                } else if (result.equals("ip_blocked")) {
                    editor.putString("login", null);
                    editor.putString("password", null);

                    editor.commit();
                    Toast.makeText(getApplicationContext(), getString(R.string.ip_blocked), Toast.LENGTH_LONG).show();
                } else {

                    if (saved_login != null && saved_password != null) {

                        Toast.makeText(getApplicationContext(), getString(R.string.apierror)+" " +"Trying to start in offline mode.", Toast.LENGTH_LONG).show();
                        Log.d(Config.DEBUG_TAG, "Bad data from API, but logged in before - continue in offline mode");


                        Intent incoming_intent = getIntent();
                        String activityType = incoming_intent.getStringExtra("type");

                        Intent intent;
                        int visits = settings.getInt("number_visits",0);
                        Log.d(Config.DEBUG_TAG, "visits: " + visits);

                        int filled = 0;

                        int logged_user_id = settings.getInt("logged_user_id", 0);
                        Log.d(Config.DEBUG_TAG, "logged_user_id: " + logged_user_id);

                        if (logged_user_id != 0) {
                            filled = settings.getInt("profile_filled",0);
                        }

                        Log.d(Config.DEBUG_TAG, "filled: " + filled);

                        intent = new Intent(LoginActivity.this, WidgetActivity.class);
                        if ((activityType != null && activityType.equals("fromRegister")) || (visits < 15 && filled < 2)) {
                            intent.putExtra("profile", "incomplete");
                        }

                        intent.putExtra("origin", "login");


                        startActivity(intent);
                        if (loader != null) {
                            loader.dismiss();
                        }

                        finish();
                    }
                }
            }

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loader != null) {
            loader.dismiss();
            loader = null;
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        /*builder.setMessage(getString(R.string.really_quit))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        dialog.cancel();
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        dialog = builder.create();

        dialog.show();*/
        finish();

    }


}


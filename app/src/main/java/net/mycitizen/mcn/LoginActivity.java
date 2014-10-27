package net.mycitizen.mcn;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class LoginActivity extends ActionBarActivity {
    public ApiConnector api;

    ProgressDialog loader = null;

    String login_field;
    String password_field;

    CheckBox remember_me;

    Button password, login;

    ActionBar actionBar;

    AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();

        String activityType = intent.getStringExtra("type");

        setContentView(R.layout.login_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //BitmapDrawable draw = (BitmapDrawable)getResources().getDrawable(R.drawable.all_background);
        //RelativeLayout background = (RelativeLayout)findViewById(R.id.background);
        //draw.setTileModeX(TileMode.REPEAT);
        //draw.setTileModeY(TileMode.CLAMP);
        //background.setBackgroundDrawable(draw);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setIcon(null);
        actionBar.hide();

        SharedPreferences settings = LoginActivity.this.getSharedPreferences(Config.localStorageName, 0);

        String saved_login = settings.getString("login", null);
        String saved_password = settings.getString("password", null);
        String api = settings.getString("usedApi", "");


        if (settings.getString("userAgent", null) == null) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("userAgent", new WebView(this).getSettings().getUserAgentString());
            editor.commit();
        }

        System.out.println("LoginActivity.java: SAVED " + saved_login + " " + saved_password);


        if (saved_login != null && saved_password != null) {
            loader = loadingDialog();

            EditText login_f = (EditText) findViewById(R.id.login_login);
            login_f.setText(saved_login);
            EditText password_f = (EditText) findViewById(R.id.login_password);
            password_f.setText(saved_password);

            login_field = saved_login;
            password_field = saved_password;

            LogingIn task = new LogingIn();
            task.execute(new String[]{"nothing"});


        }

        remember_me = (CheckBox) findViewById(R.id.login_remember);

        if (activityType != null && activityType.equals("fromRegister")) {
            EditText login = (EditText) findViewById(R.id.login_login);
            login.setText(intent.getStringExtra("username"));
            EditText password = (EditText) findViewById(R.id.login_password);
            password.setText(intent.getStringExtra("password"));
            Toast.makeText(getApplicationContext(), getString(R.string.registration_finish), Toast.LENGTH_LONG).show();
        } else if (activityType != null && activityType.equals("apiError")) {
            Toast.makeText(getApplicationContext(), getString(R.string.apierror), Toast.LENGTH_LONG).show();
        }

        login = (Button) findViewById(R.id.login_submit);

        //Typeface tf_mm3 = Typeface.createFromAsset(getAssets(), "fonts/mm3.ttf");
        //login.setTypeface(tf_mm3);

        login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                loader = loadingDialog();

                EditText login = (EditText) findViewById(R.id.login_login);
                EditText password = (EditText) findViewById(R.id.login_password);


                if (!login.getText().toString().equals("") && !password.getText().toString().equals("")) {

                    login_field = login.getText().toString();
                    password_field = password.getText().toString();

                    LogingIn task = new LogingIn();
                    task.execute(new String[]{"nothing"});


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


        password = (Button) findViewById(R.id.login_forgotten_password);
        password.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LostPasswordActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);


                startActivity(intent);
            }
        });

        if (api.equals("")) {
            password.setEnabled(false);
            login.setEnabled(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = LoginActivity.this.getSharedPreferences(Config.localStorageName, 0);

        String saved_login = settings.getString("login", null);
        String saved_password = settings.getString("password", null);
        String api = settings.getString("usedApi", "");

        if (api.equals("")) {
            password.setEnabled(false);
            login.setEnabled(false);
        } else {
            password.setEnabled(true);
            login.setEnabled(true);
        }

    }


    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class LogingIn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            //Looper.prepare();
            api = new ApiConnector(LoginActivity.this);

            api.removeSession();
            String result = api.sessionInit(login_field, password_field);

            if (api.sessionInitiated()) {
                return "success";
            } else {
                return result;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {


                SharedPreferences settings = LoginActivity.this.getSharedPreferences(Config.localStorageName, 0);

                String saved_login = settings.getString("login", null);
                String saved_password = settings.getString("password", null);

                if (saved_login == null || saved_password == null) {
                    SharedPreferences save_settings = LoginActivity.this.getSharedPreferences("MyCitizen", 0);
                    SharedPreferences.Editor editor = save_settings.edit();

                    editor.putString("current_login", login_field);

                    if (remember_me.isChecked()) {

                        editor.putString("login", login_field);
                        editor.putString("password", password_field);
                        System.out.println("SAVING " + login_field + " " + password_field);
                        editor.commit();

                        Intent intent = new Intent(LoginActivity.this, WidgetActivity.class);
                        intent.putExtra("origin", "login");

                        //String message = editText.getText().toString();
                        //intent.putExtra(EXTRA_MESSAGE, message);

                        startActivity(intent);
                        if (loader != null) {
                            loader.dismiss();
                        }

                        finish();

                    } else {

                        editor.putString("login", null);
                        editor.putString("password", null);

                        editor.commit();

                        Intent intent = new Intent(LoginActivity.this, WidgetActivity.class);

                        //String message = editText.getText().toString();
                        //intent.putExtra(EXTRA_MESSAGE, message);

                        startActivity(intent);
                        loader.dismiss();
                        finish();
                    }

                }

            } else {
                if (loader != null) {
                    loader.dismiss();
                }
                System.out.println("LOGIN RESPONSE " + result);
                SharedPreferences save_settings = LoginActivity.this.getSharedPreferences("MyCitizen", 0);
                SharedPreferences.Editor editor = save_settings.edit();


                if (result.equals("not_active")) {
                    editor.putString("login", null);
                    editor.putString("password", null);

                    editor.commit();
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error_unactive), Toast.LENGTH_LONG).show();
                } else if (result.equals("unkonwn_user")) {
                    editor.putString("login", null);
                    editor.putString("password", null);

                    editor.commit();
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error_unkonown_user), Toast.LENGTH_LONG).show();
                } else if (result.equals("ip_blocked")) {
                    editor.putString("login", null);
                    editor.putString("password", null);

                    editor.commit();
                    Toast.makeText(getApplicationContext(), getString(R.string.ip_blocked), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.login_error_unkonown_error), Toast.LENGTH_LONG).show();
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
        builder.setMessage(getString(R.string.really_quit))
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

        dialog.show();

    }


}


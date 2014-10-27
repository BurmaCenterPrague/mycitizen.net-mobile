package net.mycitizen.mcn;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class RegisterActivity extends ActionBarActivity {
    public ApiConnector api;

    EditText username;
    EditText password;
    EditText email;

    ProgressDialog loader = null;

    boolean can_continue = true;

    ActionBar actionBar;
    String used_lng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_title_register);
        actionBar.setIcon(null);
        api = new ApiConnector(this);

        username = (EditText) findViewById(R.id.sign_up_username);
        password = (EditText) findViewById(R.id.sign_up_password);
        email = (EditText) findViewById(R.id.sign_up_email);

        SharedPreferences save_settings = RegisterActivity.this.getSharedPreferences("MyCitizen", 0);
        String used_api = save_settings.getString("usedApi", null);
        used_lng = save_settings.getString("default_lng", null);

        username.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    CheckUsername task = new CheckUsername();
                    task.execute(new String[]{username.getText().toString()});
                }
            }
        });

        Button cancel = (Button) findViewById(R.id.signup_cancel);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //finish();

                Intent intent = new Intent(RegisterActivity.this, HelpActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("topic", "login");

                startActivity(intent);
            }
        });

        Button register = (Button) findViewById(R.id.sign_up_register);

        register.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                String username_field = username.getText().toString();
                String password_field = password.getText().toString();
                String email_field = email.getText().toString();

                if (username_field.equals("")) {
                    Toast.makeText(getApplicationContext(), "You have to input your username", Toast.LENGTH_LONG).show();
                } else if (password_field.equals("")) {
                    Toast.makeText(getApplicationContext(), "You have to input your password", Toast.LENGTH_LONG).show();
                } else if (email_field.equals("")) {
                    Toast.makeText(getApplicationContext(), "You have to input your email", Toast.LENGTH_LONG).show();
                } else if (password_field.length() < 8) {
                    Toast.makeText(getApplicationContext(), "Your password is too short. Use password with at least 8 characters", Toast.LENGTH_LONG).show();
                } else if (!password_field.matches(".*[A-Z]+.*")) {
                    Toast.makeText(getApplicationContext(), "Your password must contain at least one capital letter", Toast.LENGTH_LONG).show();
                } else if (!password_field.matches(".*[0-9]+.*")) {
                    Toast.makeText(getApplicationContext(), "Your password must contain at least one number", Toast.LENGTH_LONG).show();
                } else if (!password_field.matches(".*[a-z]+.*")) {
                    Toast.makeText(getApplicationContext(), "Your password must contain at least one lower case letter", Toast.LENGTH_LONG).show();
                } else {

                    loader = loadingDialog();

                    RegisterInit task = new RegisterInit();
                    task.execute(new String[]{username_field, email_field, password_field, used_lng});


                }
            }
        });


        TextView api_url = (TextView) findViewById(R.id.used_api);
        if (used_api != null) {
            String domain = used_api.replaceAll("http[s]?://", "");
            domain = domain.replaceAll("/[Aa][Pp][Ii]/", "");
            api_url.setText(domain);
        }

    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class RegisterInit extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            //Looper.prepare();

            String[] type = urls;

            String result = api.registerUser(type[0], type[1], type[2], type[3]);

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            //parse data to view
            if (result.equals("registration_ok")) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

                intent.putExtra("type", "fromRegister");
                intent.putExtra("username", username.getText().toString());
                intent.putExtra("password", password.getText().toString());

                loader.dismiss();
                startActivity(intent);
                finish();
            } else {
                loader.dismiss();
                if (result.equals("email_exists")) {
                    Toast.makeText(getApplicationContext(), "This email is already in use.", Toast.LENGTH_LONG).show();
                } else if (result.equals("invalid_email")) {
                    Toast.makeText(getApplicationContext(), "Please correct your email address.", Toast.LENGTH_LONG).show();
                } else if (result.equals("login_exists")) {
                    Toast.makeText(getApplicationContext(), "This username is already in use.", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private class CheckUsername extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            //Looper.prepare();

            String[] type = urls;

            String result = api.checkUser(type[0]);

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            //parse data to view
            if (!result.equals("username_available")) {

                Toast.makeText(getApplicationContext(), "Username is already taken!", Toast.LENGTH_LONG).show();
            }

        }
    }


}

package net.mycitizen.mcn;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import java.io.InputStream;

public class RegisterActivity extends ActionBarActivity {
    public ApiConnector api;

    EditText username;
    EditText password;
    EditText email;

    TextView security_question;
    EditText security_question_answer;
    ImageView captcha_image;

    ProgressDialog loader = null;

    boolean can_continue = true;

    ActionBar actionBar;
    String ui_language;

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

        security_question = (TextView) findViewById(R.id.security_question);
        security_question_answer = (EditText) findViewById(R.id.security_question_answer);
        captcha_image = (ImageView) findViewById(R.id.captcha_image);

        SharedPreferences save_settings = RegisterActivity.this.getSharedPreferences("MyCitizen", 0);


            if (save_settings.getString("registration_question", null) != null) {
                security_question.setText(save_settings.getString("registration_question", null));
                findViewById(R.id.security_question_wrapper).setVisibility(View.VISIBLE);


                String captcha_url = save_settings.getString("usedApi", "").replace("API/", "images/") + "captcha.jpg";
                Log.d(Config.DEBUG_TAG, "captcha_url: " + captcha_url);
                new DownloadImageTask((ImageView) findViewById(R.id.captcha_image))
                        .execute(captcha_url);
            }

        String used_api = save_settings.getString("usedApi", null);
        ui_language = save_settings.getString("ui_language", null);

        username.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus && !username.getText().toString().equals("")) {
                    CheckUsername task = new CheckUsername();
                    task.execute(username.getText().toString());
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

                // todo extract string literals
                if (username_field.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter a username.", Toast.LENGTH_LONG).show();
                } else if (password_field.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter a password.", Toast.LENGTH_LONG).show();
                } else if (email_field.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter your email address.", Toast.LENGTH_LONG).show();
                } else if (password_field.length() < 8) {
                    Toast.makeText(getApplicationContext(), "Your password is too short (at least 8 characters).", Toast.LENGTH_LONG).show();
                } else if (!password_field.matches(".*[A-Z]+.*")) {
                    Toast.makeText(getApplicationContext(), "Your password must contain at least one capital letter.", Toast.LENGTH_LONG).show();
                } else if (!password_field.matches(".*[0-9]+.*")) {
                    Toast.makeText(getApplicationContext(), "Your password must contain at least one number.", Toast.LENGTH_LONG).show();
                } else if (!password_field.matches(".*[a-z]+.*")) {
                    Toast.makeText(getApplicationContext(), "Your password must contain at least one lower case letter.", Toast.LENGTH_LONG).show();
                } else {


                    // captcha

                    String security_answer = security_question_answer.getText().toString();

                    loader = loadingDialog();

                    RegisterInit task = new RegisterInit();
                    // Register with the chosen UI language. API will reset to English if it doesn't exist on the deployment.
                    task.execute(username_field, email_field, password_field, ui_language, security_answer);


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

            String result = api.registerUser(type[0], type[1], type[2], type[3], type[4]);

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
                    Toast.makeText(getApplicationContext(), getString(R.string.email_already_in_use), Toast.LENGTH_LONG).show();
                } else if (result.equals("invalid_email")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.email_address_wrong), Toast.LENGTH_LONG).show();
                } else if (result.equals("login_exists")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.username_already_in_use), Toast.LENGTH_LONG).show();
                } else if (result.equals("wrong_answer")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.wrong_security_answer), Toast.LENGTH_LONG).show();
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

                Toast.makeText(getApplicationContext(), getString(R.string.username_already_in_use), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.d(Config.DEBUG_TAG, "Error fetching image: " + e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            /*int height = result.getHeight();
            bmImage.getLayoutParams().height = height;
            */
            if (result == null) {
                return;
            }
            int imageViewWidth = bmImage.getWidth();
            float width = (float) result.getWidth();
            float height = imageViewWidth / width * (float) result.getHeight();
            Bitmap output = Bitmap.createScaledBitmap(result, imageViewWidth, (int) height, false);
            bmImage.setImageBitmap(output);
            bmImage.requestLayout();

        }
    }
}

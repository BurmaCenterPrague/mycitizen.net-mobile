package net.mycitizen.mcn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import org.acra.ACRA;

public class GetStartedActivity extends ActionBarActivity {
    RelativeLayout page1;
    RelativeLayout page2;
    RelativeLayout page3;

    LinkedHashMap<String, String> supported_lng;

    Spinner deployment;
    EditText deployment2;
    Spinner get_started_language;
    RadioGroup distance_unit;

    ProgressDialog loader = null;
    ActionBar actionBar;
    Config cfg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.get_started_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_title_get_started);
        actionBar.setIcon(null);
        actionBar.hide();
        final ApiConnector api = new ApiConnector(this);
        SharedPreferences save_settings = GetStartedActivity.this.getSharedPreferences("MyCitizen", 0);


        Configuration config = new Configuration();
        String loc = Config.codeToLocale(getApplicationContext(), save_settings.getString("ui_language", null));
        Locale locale = new Locale(loc);
        config.locale = locale;
        super.onConfigurationChanged(config);

        cfg = new Config(GetStartedActivity.this);

        page1 = (RelativeLayout) findViewById(R.id.get_started_1);
        page2 = (RelativeLayout) findViewById(R.id.get_started_2);
        page3 = (RelativeLayout) findViewById(R.id.get_started_3);

        if (getIntent().getIntExtra("page", 1) == 2) {
            page1.setVisibility(View.GONE);
            page2.setVisibility(View.VISIBLE);
        }
        supported_lng = Config.getUiLanguages();

        Log.d(Config.DEBUG_TAG, "language supported_lng: " + supported_lng);
        if (supported_lng != null) {
            ArrayList<String> items = new ArrayList<String>();
            Iterator<Entry<String, String>> it = supported_lng.entrySet().iterator();
            while (it.hasNext()) {
                Entry pairs = (Entry) it.next();
                Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                //pairs.getKey();
                items.add(pairs.getValue().toString());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            get_started_language = (Spinner) findViewById(R.id.get_started_language);
            get_started_language.setAdapter(adapter);

            String lng_default = save_settings.getString("ui_language", null);

            Log.d(Config.DEBUG_TAG, "lng_default: " + lng_default);

            if (lng_default != null) {
                int lngPosition = adapter.getPosition(Config.translateLanguageCodeToName(getApplicationContext(), lng_default));
                Log.d(Config.DEBUG_TAG, "set language: position: " + lngPosition + ", code: " + Config.translateLanguageCodeToName(getApplicationContext(), lng_default));
                if (lngPosition >= 0) {
                    get_started_language.setSelection(lngPosition);
                }
            }

        }

        Button from_1 = (Button) findViewById(R.id.button_get_started_1_next);
        from_1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences save_settings = GetStartedActivity.this.getSharedPreferences("MyCitizen", 0);
                SharedPreferences.Editor editor = save_settings.edit();

                editor.putString("ui_language", get_started_language.getSelectedItem().toString());

                String oldLanguage = save_settings.getString("ui_language", "eng");

                if (get_started_language.getSelectedItem().toString().equals("Čeština")) {
                    Locale locale = new Locale("cs");
                    editor.putString("ui_language", "ces");
                    editor.putString("logged_user_language", "Čeština");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    ACRA.getErrorReporter().putCustomData("locale", String.valueOf(locale));
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                } else if (get_started_language.getSelectedItem().toString().equals("Matu")) {
                    Locale locale = new Locale("ht");
                    editor.putString("ui_language", "hlt");
                    editor.putString("logged_user_language", "Matu");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    ACRA.getErrorReporter().putCustomData("locale", String.valueOf(locale));
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                } else if (get_started_language.getSelectedItem().toString().equals("Mara")) {
                    Locale locale = new Locale("mh");
                    editor.putString("ui_language", "mrh");
                    editor.putString("logged_user_language", "Mara");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    ACRA.getErrorReporter().putCustomData("locale", String.valueOf(locale));
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                } else if (get_started_language.getSelectedItem().toString().equals("Zolai")) {
                    Locale locale = new Locale("zu");
                    editor.putString("ui_language", "zom");
                    editor.putString("logged_user_language", "Zolai");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    ACRA.getErrorReporter().putCustomData("locale", String.valueOf(locale));
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());
                } else if (get_started_language.getSelectedItem().toString().equals("नहीं")) {
                    Locale locale = new Locale("hi");
                    editor.putString("ui_language", "hin");
                    editor.putString("logged_user_language", "नहीं");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    ACRA.getErrorReporter().putCustomData("locale", String.valueOf(locale));
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());
                } else {
                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);
                    editor.putString("ui_language", "eng");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    ACRA.getErrorReporter().putCustomData("locale", String.valueOf(locale));
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                }

                String checked_unit;

                if (distance_unit.getCheckedRadioButtonId() == R.id.distance_unit_mile) {
                    checked_unit = "mi";
                } else {
                    checked_unit = "km";
                }


                editor.putString("distance_unit", checked_unit);

                editor.commit();

                if (!oldLanguage.equals(save_settings.getString("ui_language", "eng"))) {
                    // Toast.makeText(getApplicationContext(), R.string.language_quit,Toast.LENGTH_LONG).show();

                    Intent intent_restart = new Intent(GetStartedActivity.this, GetStartedActivity.class);
                    intent_restart.putExtra("page", 2);
                    startActivity(intent_restart);
                    finish();
                }

                Log.d(Config.DEBUG_TAG, "old language: " + oldLanguage);
                Log.d(Config.DEBUG_TAG, "Language: " + get_started_language.getSelectedItem().toString());

                /*

                If language changed: restart app

                Intent mStartActivity = new Intent(context, SplashActivity.class);
int mPendingIntentId = 123456;
PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
System.exit(0);


                 */

                page1.setVisibility(View.GONE);
                page2.setVisibility(View.VISIBLE);
            }
        });

        Button from_2 = (Button) findViewById(R.id.button_get_started_2_next);
        from_2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!deployment2.getText().toString().equals("")) {
                    ApiConnector api = new ApiConnector(getApplicationContext());

                    if (!api.isNetworkAvailable()) {
                        Toast.makeText(getApplicationContext(),R.string.not_available_offline, Toast.LENGTH_LONG).show();
                        return;
                    }
                    loader = loadingDialog();

                    SharedPreferences save_settings = GetStartedActivity.this.getSharedPreferences("MyCitizen", 0);
                    SharedPreferences.Editor editor = save_settings.edit();

                    editor.putString("userApi", deployment2.getText().toString());


                    editor.commit();

                    ApiChecker task = new ApiChecker();
                    task.execute(deployment2.getText().toString());

                } else {


                    SharedPreferences save_settings = GetStartedActivity.this.getSharedPreferences("MyCitizen", 0);
                    SharedPreferences.Editor editor = save_settings.edit();
                    Log.d(Config.DEBUG_TAG, "DEPLOYMENT " + deployment.getSelectedItem().toString());
                    editor.putString("usedApi", cfg.translateApiUrlLabel(deployment.getSelectedItem().toString()));
                    Log.d(Config.DEBUG_TAG, "TRANSLATION " + cfg.translateApiUrlLabel(deployment.getSelectedItem().toString()));
                    editor.commit();

/*

                    ApiChecker task = new ApiChecker();
                    task.execute(new String[]{deployment.getSelectedItem().toString()});
*/

                    page2.setVisibility(View.GONE);
                    page3.setVisibility(View.VISIBLE);
                }

                DataHandler db = new DataHandler(getApplicationContext());
                db.clearTable();


            }
        });


        Button login = (Button) findViewById(R.id.button_get_started_3_login);
        login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
                finish();
            }
        });

        /*
        Button close = (Button) findViewById(R.id.button_get_started_1_close);
        close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		*/

        Button help1 = (Button) findViewById(R.id.button_get_started_1_help);
        help1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //page1.setVisibility(View.VISIBLE);
                //page2.setVisibility(View.GONE);

                Intent intent = new Intent(GetStartedActivity.this, HelpActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("topic", "get_started");

                startActivity(intent);
            }
        });

        Button help2 = (Button) findViewById(R.id.button_get_started_2_help);
        help2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //page1.setVisibility(View.VISIBLE);
                //page2.setVisibility(View.GONE);

                Intent intent = new Intent(GetStartedActivity.this, HelpActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("topic", "get_started_2");

                startActivity(intent);
            }
        });

        Button help3 = (Button) findViewById(R.id.button_get_started_3_help);
        help3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //page2.setVisibility(View.VISIBLE);
                //page3.setVisibility(View.GONE);

                Intent intent = new Intent(GetStartedActivity.this, HelpActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("topic", "get_started_3");

                startActivity(intent);
            }
        });

        Button register = (Button) findViewById(R.id.button_get_started_3_register);
        register.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                DeploymentInfo task = new DeploymentInfo();
                task.execute();
            }
        });

        deployment = (Spinner) findViewById(R.id.get_started_deployment);

        ArrayList<String> items = new ArrayList<String>();


        String deployments[] = cfg.getDeploymentLabels();

        for (int i = 0; i < deployments.length; i++) {
            items.add(deployments[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(GetStartedActivity.this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deployment.setAdapter(adapter);


        String deployment_default = save_settings.getString("usedApi", null);
        String manual_deployment_default = save_settings.getString("userApi", null);
        int deploymentPosition = -1;
        if (deployment_default != null) {
            deploymentPosition = adapter.getPosition(cfg.translateApiUrl(deployment_default));
            if (deploymentPosition >= 0) {
                deployment.setSelection(deploymentPosition);
            }
        }
        deployment.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        deployment2 = (EditText) findViewById(R.id.get_started_deployment_2);
        if (manual_deployment_default != null && deploymentPosition == -1) {
            deployment2.setText(manual_deployment_default);
        }

        distance_unit = (RadioGroup) findViewById(R.id.distance_unit);

        String distance_unit_default = save_settings.getString("distance_unit", null);

        if (distance_unit_default != null) {
            if (distance_unit_default.equals("km")) {
                distance_unit.check(R.id.distance_unit_km);
            } else {
                distance_unit.check(R.id.distance_unit_mile);
            }
        } else {

            distance_unit.check(R.id.distance_unit_km);
        }
    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class ApiChecker extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(GetStartedActivity.this);
            String prefix_a = "http://";
            String prefix_b = "https://";
            String postfix = "/API/";


            if (api.apiExists(prefix_a + type[0] + postfix)) {
                return prefix_a + type[0] + postfix;

            } else if (api.apiExists(prefix_b + type[0] + postfix)) {
                return prefix_b + type[0] + postfix;

            } else {
                return null;
            }


        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                DataHandler db = new DataHandler(getApplicationContext());
                db.clearTable();

                SharedPreferences save_settings = GetStartedActivity.this.getSharedPreferences("MyCitizen", 0);
                SharedPreferences.Editor editor = save_settings.edit();

                editor.putString("usedApi", result);
                editor.putString("login", null);
                editor.putString("password", null);
                editor.putString("login", null);
                Log.d(Config.DEBUG_TAG, "usedApi " + result);
                editor.commit();


                if (loader != null) {
                    loader.dismiss();
                }

                page2.setVisibility(View.GONE);
                page3.setVisibility(View.VISIBLE);
            } else {
                if (loader != null) {
                    loader.dismiss();
                }
                Toast.makeText(getApplicationContext(), getString(R.string.error_deployment_test_failed), Toast.LENGTH_LONG).show();
            }

        }
    }

    private class DeploymentInfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            ApiConnector api = new ApiConnector(GetStartedActivity.this);

            api.getDeploymentInfo();

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Intent intent = new Intent(GetStartedActivity.this, RegisterActivity.class);

            startActivity(intent);
            finish();

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
        if (page1.getVisibility() == View.VISIBLE) {
            page1.setVisibility(View.GONE);
            Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (page2.getVisibility() == View.VISIBLE) {
            page2.setVisibility(View.GONE);
            page1.setVisibility(View.VISIBLE);
        } else if (page3.getVisibility() == View.VISIBLE) {
            page3.setVisibility(View.GONE);
            page2.setVisibility(View.VISIBLE);
        }


    }

}

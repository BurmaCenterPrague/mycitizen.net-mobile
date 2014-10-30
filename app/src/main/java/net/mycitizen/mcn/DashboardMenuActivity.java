package net.mycitizen.mcn;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v7.app.ActionBar;

import java.util.ArrayList;

public class DashboardMenuActivity extends BaseActivity {
    public ApiConnector api;


    WebView dashboard_info;

    AlertDialog dialog;
    ProgressDialog loader = null;
    ActionBar actionBar;
    Button dashboard_create_group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_dashboard);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle("MyCitizen.net");
        actionBar.setIcon(null);

        api = new ApiConnector(this);

        dashboard_connection = (ProgressBar) findViewById(R.id.dashboard_connection);
        dashboard_connection.getProgressDrawable().setColorFilter(Color.parseColor("#C40C0C"), Mode.SRC_IN);
        dashboard_connection.setProgress(5);

        SharedPreferences user_settings = DashboardMenuActivity.this.getSharedPreferences("MyCitizen", 0);

        Button dashboard_help = (Button) findViewById(R.id.dashboard_help);
        dashboard_help.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardMenuActivity.this, HelpActivity.class);

                intent.putExtra("topic", "dashboard");
                startActivity(intent);
            }
        });
        Button dashboard_profile = (Button) findViewById(R.id.dashboard_profile);
        dashboard_profile.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), "This section is not avalable in offline mode.", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(DashboardMenuActivity.this, ProfileMainActivity.class);
                    //String message = editText.getText().toString();
                    //intent.putExtra(EXTRA_MESSAGE, message);

                    startActivity(intent);
                }
            }
        });
        dashboard_create_group = (Button) findViewById(R.id.dashboard_create_group);
        dashboard_create_group.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), "This section is not avalable in offline mode.", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(DashboardMenuActivity.this, CreateGroupActivity.class);
                    //String message = editText.getText().toString();
                    //intent.putExtra(EXTRA_MESSAGE, message);

                    startActivity(intent);
                }
            }
        });

        boolean can_create_group = user_settings.getBoolean("create_group_rights", false);
        if (!can_create_group) {
            dashboard_create_group.setEnabled(false);
        }
        Button dashboard_refresh = (Button) findViewById(R.id.dashboard_refresh);
        if (!api.isNetworkAvailable()) {
            dashboard_refresh.setVisibility(View.GONE);
        }
        dashboard_refresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.loading_data), Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), getString(R.string.please_wait), Toast.LENGTH_LONG).show();

                    // 1. Determine the Connection Quality
                    // 2. Retrieve the Deployment information
                    // 3. Reset timeout to retrieve Users, Groups and Resources and save to database

                    fillDatabaseTask task = new fillDatabaseTask();
                    task.execute();

                }
            }
        });

        Button dashboard_logout = (Button) findViewById(R.id.dashboard_logout);
        dashboard_logout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardMenuActivity.this);
                builder.setMessage("Do you really want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                dialog.cancel();
                                SharedPreferences save_settings = DashboardMenuActivity.this.getSharedPreferences("MyCitizen", 0);
                                SharedPreferences.Editor editor = save_settings.edit();

                                editor.putString("login", null);
                                editor.putString("password", null);

                                editor.commit();

                                Intent intent = new Intent("kill");
                                intent.setType("content://sparta");
                                sendBroadcast(intent);

                                Intent intent2 = new Intent(DashboardMenuActivity.this, LoginActivity.class);

                                //String message = editText.getText().toString();
                                //intent.putExtra(EXTRA_MESSAGE, message);

                                startActivity(intent2);

                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.cancel();
                            }
                        });
                // Create the AlertDialog object and return it
                dialog = builder.create();

                dialog.show();


            }

        });


        Button menu_filter = (Button) findViewById(R.id.widget_menu_filter);
        SharedPreferences icon_settings = DashboardMenuActivity.this.getSharedPreferences("MyCitizen", 0);
        if (icon_settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        }
        menu_filter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardMenuActivity.this, FilterMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
                finish();
            }
        });

        dashboard_button_toggle = (ToggleButton) findViewById(R.id.widget_menu_dashboard);

        dashboard_button_toggle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(DashboardMenuActivity.this, WidgetActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                //startActivity(intent);
                finish();
            }
        });

        messages_button = (Button) findViewById(R.id.widget_menu_messages);

        CheckUnreadMessages messagesCheck = new CheckUnreadMessages();
        messagesCheck.execute(new String[]{});

        messages_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardMenuActivity.this, MessagesActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("type", "dialog_inbox");
                startActivity(intent);
                finish();
            }
        });

        dashboard_info = (WebView) findViewById(R.id.dashboard_info);

        SharedPreferences save_settings = DashboardMenuActivity.this.getSharedPreferences("MyCitizen", 0);
        String used_api = save_settings.getString("usedApi", null);
        used_api = used_api.replace("http://", "");
        used_api = used_api.replace("https://", "");
        used_api = used_api.replace("/API/", "");

        String username = save_settings.getString("current_login", null);
        String visibility = save_settings.getString("logged_user_visibility", null);
        String deployment_name = save_settings.getString("deployment_name","");
        String deployment_description = save_settings.getString("deployment_description","");

        String messages = "0";
        String data_source = "";

        if (save_settings.getString("data_source", "").equals("network")) {
            data_source = "Data loaded from network.";
        } else if (save_settings.getString("data_source", "").equals("database")) {
            data_source = "Data loaded from database.";
        }

        String text = "<div><table><tr><td colspan=2><h1>" + getString(R.string.dashboard) + "</h1></td></tr>" +
                "<tr><td><b>Version:</b></td><td><span>" + Config.version + "</span></td></tr>" +
                "<tr><td><b>" + getString(R.string.deployment) + ":</b></td><td><span><a href=\"http://" + used_api + "\">"+deployment_name+"</a></span></td></tr>" +
                "<tr><td colspan=\"2\"><span>" + deployment_description + "</span></td></tr>" +
                "<tr><td><b>" + getString(R.string.username) + ":</b></td><td><span>" + username + "</span></td></tr>" +
                "<tr><td><b>" + getString(R.string.visibility) + ":</b></td><td><span>" + visibility + "</span></td></tr>" +
                "<tr><td><b>" + getString(R.string.dashboard_messages) + ":</b></td><td><span>" + messages + " " + getString(R.string.dashboard_new_messages) +
                //"</span></td></tr></table>" + data_source +
                "</div>";
        dashboard_info.loadData(text, "text/html", "UTF-8");
        //dashboard_info.setText(Html.fromHtml(text));
        loader = loadingDialog();

        DashboardInit task = new DashboardInit();
        task.execute(new String[]{""});


    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class DashboardInit extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //widget_list.setAdapter(null);
                }
            });


            String[] type = urls;

            ApiConnector api = new ApiConnector(DashboardMenuActivity.this);
            String result = "";
            String unread_messages = "0";
            boolean can_create_groups = false;
            if (api.sessionInitiated()) {

                if (api.isNetworkAvailable()) {

                    //unread_messages = api.getUnreadMessages();
                    can_create_groups = api.CanCreateGroups();
                }
            }
            result = String.valueOf(can_create_groups);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {

                boolean can_create_groups = Boolean.valueOf(result);


                if (!can_create_groups) {
                    dashboard_create_group.setEnabled(false);
                }
            }

            loader.cancel();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    private class fillDatabaseTask extends AsyncTask<Void, Void, Void> {

        // ApiConnector api = new ApiConnector(DashboardMenuActivity.this);
        SharedPreferences settings = getSharedPreferences(Config.localStorageName, 0);

        @Override
        protected Void doInBackground(Void... voids) {
            api.determineConnectionQuality();

            api.getDeploymentInfo();

            // todo: first delete all table contents? (to get rid of items that are gone)
            ArrayList<DataObject> users = api.getData("user", null, true, String.valueOf(settings.getInt("length_users_retrieved", 20)));
            ArrayList<DataObject> groups = api.getData("group", null, true, String.valueOf(settings.getInt("length_groups_retrieved", 20)));
            ArrayList<DataObject> resources = api.getData("resource", "filter[type][0]=2&filter[type][1]=3&filter[type][2] 4&filter[type][3]=5&filter[type][5]=6", true, String.valueOf(settings.getInt("length_resources_retrieved", 50)));


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Toast.makeText(DashboardMenuActivity.this, getString(R.string.finished), Toast.LENGTH_LONG).show();
            System.out.println("Executed: " + result);

            settings = getSharedPreferences(Config.localStorageName, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("time_users_retrieved", 0);
            editor.putLong("time_groups_retrieved", 0);
            editor.putLong("time_resources_retrieved", 0);

            editor.commit();
        }

    }


}

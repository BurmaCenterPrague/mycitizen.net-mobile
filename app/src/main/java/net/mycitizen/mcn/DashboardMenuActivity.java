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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        actionBar.setTitle(R.string.dashboard);
        actionBar.setIcon(null);

        api = new ApiConnector(this);

        dashboard_connection = (ProgressBar) findViewById(R.id.dashboard_connection);
        dashboard_connection.getProgressDrawable().setColorFilter(Color.parseColor("#C40C0C"), Mode.SRC_IN);
        dashboard_connection.setProgress(5);

        SharedPreferences user_settings = DashboardMenuActivity.this.getSharedPreferences("MyCitizen", 0);

        Button dashboard_activity = (Button) findViewById(R.id.dashboard_activity);
        dashboard_activity.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardMenuActivity.this, ActivitiesActivity.class);

                intent.putExtra("timeframe", "today");
                startActivity(intent);
            }
        });
        Button dashboard_profile = (Button) findViewById(R.id.dashboard_profile);
        dashboard_profile.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
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
            dashboard_create_group.setVisibility(View.GONE); //setEnabled(false);
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
                builder.setMessage(getString(R.string.logout_confirmation_question))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                dialog.cancel();
                                SharedPreferences save_settings = DashboardMenuActivity.this.getSharedPreferences("MyCitizen", 0);
                                SharedPreferences.Editor editor = save_settings.edit();


                                editor.putString("login", null);
                                editor.putString("password", null);
                                editor.putBoolean("create_group_rights", false);

                                editor.putLong("time_connection_measured", 0);
                                editor.putLong("time_users_retrieved", 0);
                                editor.putLong("time_groups_retrieved", 0);
                                editor.putLong("time_resources_retrieved", 0);
                                editor.putInt("length_users_retrieved", 0);
                                editor.putInt("length_groups_retrieved", 0);
                                editor.putInt("length_resources_retrieved", 0);
                                editor.putLong("last_time_retrieved_tags", 0);

                                editor.commit();

                                Intent intent = new Intent("kill");
                                intent.setType("content://sparta");
                                sendBroadcast(intent);

                                Intent intent2 = new Intent(DashboardMenuActivity.this, LoginActivity.class);

                                startActivity(intent2);

                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
        messagesCheck.execute();

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
        String user_language = save_settings.getString("logged_user_language", "unknown");

        String messages = "0";
        String data_source = "";

        if (save_settings.getString("data_source", "").equals("network")) {
            data_source = "Data loaded from network.";
        } else if (save_settings.getString("data_source", "").equals("database")) {
            data_source = "Data loaded from database.";
        }

        String profile_general_ok;
        String profile_tags_ok;
        String profile_portrait_ok;
        String profile_description_ok;
        String profile_location_ok;


        // int logged_user_id = save_settings.getInt("logged_user_id", 0);
        int profile_filled = save_settings.getInt("profile_filled",0);
        Boolean location_filled = save_settings.getBoolean("location_filled", false);
        Boolean description_filled = save_settings.getBoolean("description_filled", false);
        Boolean tags_filled = save_settings.getBoolean("tags_filled", false);
        Boolean portrait_filled = save_settings.getBoolean("portrait_filled", false);
        profile_general_ok = (profile_filled > 0) ? "<font color='green'>✓</font>" : "<font color='red'>&nbsp;x</font>";
        profile_location_ok = (location_filled) ? "<font color='green'>✓</font>" : "<font color='red'>&nbsp;x</font>";
        profile_description_ok = (description_filled) ? "<font color='green'>✓</font>" : "<font color='red'>&nbsp;x</font>";
        profile_tags_ok = (tags_filled) ? "<font color='green'>✓</font>" : "<font color='red'>&nbsp;x</font>";
        profile_portrait_ok = (portrait_filled) ? "<font color='green'>✓</font>" : "<font color='red'>&nbsp;x</font>";

        String text = "<html><body style=\"background-color:#eae9e3;\"><div><table>" +
                "<tr><td><b>Version:</b></td><td><span>" + Config.version + "</span></td></tr>" +
                "<tr><td><b>" + getString(R.string.deployment) + ":</b></td><td><span><a href=\"http://" + used_api + "\">"+deployment_name+"</a></span></td></tr>" +
                "<tr><td colspan=\"2\"><span>" + deployment_description + "</span></td></tr>" +
                "<tr><td colspan=\"2\">&nbsp;</td></tr>"+
                "<tr><td><b>" + getString(R.string.username) + ":</b></td><td><span>" + username + "</span></td></tr>" +
                "<tr><td><b>" + getString(R.string.visibility) + ":</b></td><td><span>" + visibility + "</span></td></tr>" +
                "<tr><td><b>" + getString(R.string.dashboard_messages) + ":</b></td><td><span>" + messages + " " + getString(R.string.dashboard_new_messages) +
                "<tr><td><b>" + getString(R.string.language) + ":</b></td><td><span>" + user_language + "</span></td></tr>" +
                //"</span></td></tr></table>" + data_source +
                "<tr><td colspan=\"2\"><b>" + getString(R.string.profile_completed) + ":</b></td></tr>"+
                "<tr><td>" + "General" + "</td><td>" + profile_general_ok + "</td></tr>"+
                "<tr><td>" + getString(R.string.description) + "</td><td>" + profile_description_ok + "</td></tr>"+
                "<tr><td>" + getString(R.string.tags) + "</td><td>" + profile_tags_ok + "</td></tr>"+
                "<tr><td>" + "Avatar" + "</td><td>" + profile_portrait_ok + "</td></tr>"+
                "<tr><td>" + "Location" + "</td><td>" + profile_location_ok + "</td></tr>"+
                "</div></body></html>";
        dashboard_info.loadData(text, "text/html; charset=UTF-8", "UTF-8");
        //dashboard_info.setText(Html.fromHtml(text));
        loader = loadingDialog();

        CanCreateGroup task2 = new CanCreateGroup();
        task2.execute("");


    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class CanCreateGroup extends AsyncTask<String, Void, String> {
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
            String result;
            String unread_messages = "0";
            boolean can_create_groups = false;
            if (api.sessionInitiated()) {
                if (api.isNetworkAvailable()) {
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
                    dashboard_create_group.setVisibility(View.GONE);//setEnabled(false);
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
            Log.d(Config.DEBUG_TAG, "Executed: " + result);

            settings = getSharedPreferences(Config.localStorageName, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("time_users_retrieved", 0);
            editor.putLong("time_groups_retrieved", 0);
            editor.putLong("time_resources_retrieved", 0);

            editor.commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_help:
                Intent intent = new Intent(DashboardMenuActivity.this, HelpActivity.class);
                intent.putExtra("topic", "dashboard");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

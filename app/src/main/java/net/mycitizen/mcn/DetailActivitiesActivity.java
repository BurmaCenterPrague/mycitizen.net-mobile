package net.mycitizen.mcn;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DetailActivitiesActivity extends BaseActivity {
    public ApiConnector api;

    TextView title;
    ImageView avatar;
    ScrollView information;

    TextView user_list_title = null;
    TextView group_list_title = null;
    TextView resource_list_title = null;

    ListView user_list = null;
    ListView group_list = null;
    ListView resource_list = null;

    ProgressDialog loader = null;

    String objectType;
    String objectId;

    ArrayList<DataObject> user_list_items;
    ArrayList<DataObject> group_list_items;
    ArrayList<DataObject> resource_list_items;

    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new ApiConnector(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.connections));
        actionBar.setIcon(null);
        loader = loadingDialog();

        Intent intent = getIntent();
        objectType = intent.getStringExtra("objectType");
        objectId = intent.getStringExtra("objectId");

        setContentView(R.layout.detail_activities_view);

        title = (TextView) findViewById(R.id.detail_activities_title);

        user_list = (ListView) findViewById(R.id.object_users);
        group_list = (ListView) findViewById(R.id.object_groups);
        resource_list = (ListView) findViewById(R.id.object_resources);

        user_list_title = (TextView) findViewById(R.id.object_users_title);
        group_list_title = (TextView) findViewById(R.id.object_groups_title);
        resource_list_title = (TextView) findViewById(R.id.object_resources_title);

        if (objectType.equals("user")) {
            user_list.setVisibility(View.GONE);
            user_list_title.setVisibility(View.GONE);
            group_list_title.setText(getString(R.string.user_member_of_groups));
            resource_list_title.setText(getString(R.string.user_subscribed_resources));

        } else if (objectType.equals("group")) {
            group_list.setVisibility(View.GONE);
            group_list_title.setVisibility(View.GONE);
            user_list_title.setText(getString(R.string.group_members));
            resource_list_title.setText(getString(R.string.group_resources));

        } else if (objectType.equals("resource")) {
            resource_list.setVisibility(View.GONE);
            resource_list_title.setVisibility(View.GONE);
            user_list_title.setText(getString(R.string.subscribed_users));
            group_list_title.setText(getString(R.string.subscribed_groups));
        }

        user_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                DataObject widget_object = user_list_items.get(position);

                Intent intent = new Intent(DetailActivitiesActivity.this, DetailActivity.class);

                intent.putExtra("ObjectType", widget_object.getObjectType());
                intent.putExtra("ObjectId", String.valueOf(widget_object.getObjectId()));

                startActivity(intent);
            }
        });

        group_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                DataObject widget_object = group_list_items.get(position);

                Intent intent = new Intent(DetailActivitiesActivity.this, DetailActivity.class);

                intent.putExtra("ObjectType", widget_object.getObjectType());
                intent.putExtra("ObjectId", String.valueOf(widget_object.getObjectId()));

                System.out.println("XXXX: " + String.valueOf(widget_object.getObjectId()));
                startActivity(intent);
            }
        });

        resource_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                DataObject widget_object = resource_list_items.get(position);

                Intent intent = new Intent(DetailActivitiesActivity.this, DetailActivity.class);

                intent.putExtra("ObjectType", widget_object.getObjectType());
                intent.putExtra("ObjectId", String.valueOf(widget_object.getObjectId()));

                startActivity(intent);
            }
        });

        DashboardInit task = new DashboardInit();
        task.execute(new String[]{objectType, objectId});

        Button menu_filter = (Button) findViewById(R.id.widget_menu_filter);
        SharedPreferences icon_settings = DetailActivitiesActivity.this.getSharedPreferences("MyCitizen", 0);
        if (icon_settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        }
        menu_filter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivitiesActivity.this, FilterMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
            }
        });

        dashboard_button = (Button) findViewById(R.id.widget_menu_dashboard);

        dashboard_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivitiesActivity.this, DashboardMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
            }
        });

        messages_button = (Button) findViewById(R.id.widget_menu_messages);
        CheckUnreadMessages messages = new CheckUnreadMessages();
        messages.execute(new String[]{});
        messages_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivitiesActivity.this, MessagesActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("type", "dialog_inbox");
                startActivity(intent);
            }
        });


    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class DashboardInit extends AsyncTask<String, Void, DataObject> {
        @Override
        protected DataObject doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(DetailActivitiesActivity.this);
            DataObject object = null;
            if (api.sessionInitiated()) {
                object = api.getDetail(type[0], Integer.valueOf(type[1]));
            }
            if (objectType.equals("user")) {
                //user_list_items = result.getUsers();
                group_list_items = object.getGroups(api);
                resource_list_items = object.getResources(api);


            } else if (objectType.equals("group")) {
                user_list_items = object.getUsers(api);
                //group_list_items = result.getGroups();
                resource_list_items = object.getResources(api);


            } else if (objectType.equals("resource")) {
                user_list_items = object.getUsers(api);
                group_list_items = object.getGroups(api);
                //resource_list_items = result.getResources();


            }
            return object;
        }

        @Override
        protected void onPostExecute(DataObject result) {
            //parse data to view
            //if(title != null) {
            if (objectType.equals("user")) {
                //title.setText(((UserObject)result).getName());
                actionBar.setTitle(((UserObject) result).getName() + " - " + getString(R.string.connections));
            } else if (objectType.equals("group")) {
                //title.setText(((GroupObject)result).getTitle());
                actionBar.setTitle(((GroupObject) result).getTitle() + " - " + getString(R.string.connections));
            } else if (objectType.equals("resource")) {
                //title.setText(((ResourceObject)result).getTitle());
                actionBar.setTitle(((ResourceObject) result).getTitle() + " - " + getString(R.string.connections));
            }
            //}
            //avatar.
            if (objectType.equals("user")) {
                //user_list_items = result.getUsers();

                //user_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "tag",user_list_items));
                group_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "widget", group_list_items));
                resource_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "widget", resource_list_items));

            } else if (objectType.equals("group")) {


                user_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "widget", user_list_items));
                //group_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "tag",group_list_items));
                resource_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "widget", resource_list_items));

            } else if (objectType.equals("resource")) {


                user_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "widget", user_list_items));
                group_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "widget", group_list_items));
                //resource_list.setAdapter(new ObjectListItemAdapter(DetailActivitiesActivity.this, "tag",resource_list_items));

            }

            loader.dismiss();
        }
    }


}

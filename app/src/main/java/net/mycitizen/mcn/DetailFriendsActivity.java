package net.mycitizen.mcn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailFriendsActivity extends BaseActivity {
    public ApiConnector api;

    TextView title;
    ImageView avatar;
    ScrollView information;

    TextView user_list_title = null;

    ListView user_list = null;

    ProgressDialog loader = null;

    String objectType;
    String objectId;

    ArrayList<DataObject> user_list_items;

    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new ApiConnector(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.friends));
        actionBar.setIcon(null);
        loader = loadingDialog();

        Intent intent = getIntent();
        objectType = intent.getStringExtra("objectType");
        objectId = intent.getStringExtra("objectId");

        setContentView(R.layout.detail_friends);

        title = (TextView) findViewById(R.id.detail_friends_title);
        user_list = (ListView) findViewById(R.id.user_friends);
        title.setText(getString(R.string.friends));

        user_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                DataObject widget_object = user_list_items.get(position);

                Intent intent = new Intent(DetailFriendsActivity.this, DetailActivity.class);

                intent.putExtra("ObjectType", widget_object.getObjectType());
                intent.putExtra("ObjectId", String.valueOf(widget_object.getObjectId()));

                startActivity(intent);
            }
        });


        DashboardInit task = new DashboardInit();
        task.execute(objectType, objectId);

        Button menu_filter = (Button) findViewById(R.id.widget_menu_filter);
        SharedPreferences icon_settings = DetailFriendsActivity.this.getSharedPreferences("MyCitizen", 0);
        if (icon_settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        }
        menu_filter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailFriendsActivity.this, FilterMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
            }
        });

        dashboard_button = (Button) findViewById(R.id.widget_menu_dashboard);

        dashboard_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailFriendsActivity.this, DashboardMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
            }
        });

        messages_button = (Button) findViewById(R.id.widget_menu_messages);
        CheckUnreadMessages messages = new CheckUnreadMessages();
        messages.execute();
        messages_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailFriendsActivity.this, MessagesActivity.class);

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

            ApiConnector api = new ApiConnector(DetailFriendsActivity.this);
            DataObject object = null;
            if (api.sessionInitiated()) {
                object = api.getDetail(type[0], Integer.valueOf(type[1]));
            }
            user_list_items = object.getUsers(api);


            return object;
        }

        @Override
        protected void onPostExecute(DataObject result) {
            //parse data to view

            actionBar.setTitle(((UserObject) result).getName() + " - " +
                    getString(R.string.menu_friends));
            //avatar.
            user_list.setAdapter(new ObjectListItemAdapter(DetailFriendsActivity.this, "widget", user_list_items));

            loader.dismiss();
        }
    }


}

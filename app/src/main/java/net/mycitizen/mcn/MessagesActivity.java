package net.mycitizen.mcn;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.support.v7.app.ActionBar;

public class MessagesActivity extends BaseActivity {
    public ApiConnector api;

    ProgressDialog loader = null;

    ListView message_list = null;

    ArrayList<DataObject> message_list_items;

    ToggleButton filter_opened, filter_removed;

    EditText response_input;

    String filter;

    String objectType, objectId, objectName;

    DataObject author;

    ActionBar actionBar;

    int logged_user_id;

    String messageViewType;

    int last_index = 0;
    int last_top = 0;
    int length = 10;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = MessagesActivity.this.getSharedPreferences(Config.localStorageName, 0);
        String inbox_filter = settings.getString("inbox_filter", null);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.menu_messages);
        actionBar.setIcon(null);
        Boolean removed_toggle_active = false;
        Boolean notopened_toggle_active = false;

        // todo: set opened filter to "new" if first visit and user has unread messages

        if (inbox_filter != null) {
            try {
                JSONObject filter_object = new JSONObject(inbox_filter);

                if (filter_object.has("filter_removed")) {
                    Boolean filter_trash = filter_object.getBoolean("filter_removed");
                    if (filter_trash) {


                        removed_toggle_active = true;
                    }
                }

                if (filter_object.has("filter_opened")) {
                    Boolean filter_open = filter_object.getBoolean("filter_opened");
                    if (filter_open) {
                        notopened_toggle_active = true;
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();

            }
        }

        logged_user_id = settings.getInt("logged_user_id", 0);

        Intent intent = getIntent();

        objectType = intent.getStringExtra("objectType");
        objectId = intent.getStringExtra("objectId");
        objectName = intent.getStringExtra("recipient");

        if (objectName != null) {
            actionBar.setTitle(objectName);
        }

        messageViewType = intent.getStringExtra("type");

        Log.d(Config.DEBUG_TAG, "messageViewType: " + messageViewType);
        if (messageViewType != null && messageViewType.equals("dialog_inbox")) {

            filter = ""; //"filter[type][0]=1&filter[type][1]=8&filter[type][2]=9&filter[type][3]=10&filter[user_id]=" + logged_user_id;
            setContentView(R.layout.dialog_inbox);

            filter_opened = (ToggleButton) findViewById(R.id.inbox_message_toggle);
            if (notopened_toggle_active) {
                filter_opened.setChecked(true);
                filter += "&filter[opened]=1";
            }

            // Log.d(Config.DEBUG_TAG, "objectType, objectId: "+objectType+", "+objectId);
            filter_opened.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences save_settings = MessagesActivity.this.getSharedPreferences("MyCitizen", 0);
                    SharedPreferences.Editor editor = save_settings.edit();

                    if (isChecked) {

                        editor.putString("inbox_filter", generateFilterStorage());
                        filter += "&filter[opened]=1";

                    } else {

                        editor.putString("inbox_filter", null);
                        filter = filter.replace("&filter[opened]=1", "");
                    }

                    editor.commit();

                    loader = loadingDialog();

                    DashboardInit task = new DashboardInit();
                    task.execute(objectType, objectId);
                }
            });


            filter_removed = (ToggleButton) findViewById(R.id.inbox_trash_toggle);
            if (removed_toggle_active) {
                filter_removed.setChecked(true);
                filter += "&filter[trash]=1";
            }
            filter_removed.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences save_settings = MessagesActivity.this.getSharedPreferences("MyCitizen", 0);
                    SharedPreferences.Editor editor = save_settings.edit();

                    if (isChecked) {

                        editor.putString("inbox_filter", generateFilterStorage());
                        filter += "&filter[trash]=1";
                    } else {

                        editor.putString("inbox_filter", null);
                        filter = filter.replace("&filter[trash]=1", "");
                    }

                    editor.commit();

                    loader = loadingDialog();

                    DashboardInit task = new DashboardInit();
                    task.execute(objectType, objectId);
                }
            });

        } else {
            setContentView(R.layout.response_inbox);

            if (objectType.equals("user")) {
                filter = "filter[type][0]=1&filter[all_members_only][0][type]=1&filter[all_members_only][0][id]=" + logged_user_id + "&filter[all_members_only][1][type]=1&filter[all_members_only][1][id]=" + objectId;
                // actionBar.setTitle("Messages");
            } else if (objectType.equals("group")) {
                filter = "filter[type][0]=8&filter[all_members_only][0][type]=2&filter[all_members_only][0][id]=" + objectId;
                // actionBar.setTitle("Chat");
            } else if (objectType.equals("resource")) {
                // filter = "filter[type][0]=8&filter[resource_id]="+objectId;
                filter = "filter[type][0]=8&filter[all_members_only][0][type]=3&filter[all_members_only][0][id]=" + objectId;
                // actionBar.setTitle("Comments");
            }

            response_input = (EditText) findViewById(R.id.response_input);
            response_input.clearFocus();
            Button send_response = (Button) findViewById(R.id.send_response);

            api = new ApiConnector(this);

            if (api.isNetworkAvailable()) {
                response_input.setEnabled(true);
                response_input.setHint(R.string.your_message_hint);
                send_response.setEnabled(true);
            } else {
                response_input.setEnabled(false);
                response_input.setHint(R.string.not_available_offline);
                send_response.setEnabled(false);
            }

            send_response.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    loader = loadingDialog();
                    Log.d(Config.DEBUG_TAG, "OBJECT ID " + objectId);
                    String response = response_input.getText().toString();

                    MessageSender task = new MessageSender();
                    task.execute(objectType, objectId, response);
                }
            });

        }


        loader = loadingDialog();


        DashboardInit task = new DashboardInit();
        task.execute(objectType, objectId);

        message_list = (ListView) findViewById(R.id.message_list);

        message_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

            }

        });

        Button menu_filter = (Button) findViewById(R.id.widget_menu_filter);
        SharedPreferences icon_settings = MessagesActivity.this.getSharedPreferences("MyCitizen", 0);
        if (icon_settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        }
        menu_filter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MessagesActivity.this, FilterMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
                finish();
            }
        });

        dashboard_button = (Button) findViewById(R.id.widget_menu_dashboard);

        dashboard_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagesActivity.this, DashboardMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
                finish();
            }
        });

        messages_button = (Button) findViewById(R.id.widget_menu_messages);
        CheckUnreadMessages messages = new CheckUnreadMessages();
        messages.execute();
        messages_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MessagesActivity.this, WidgetActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                //startActivity(intent);
                finish();
            }
        });


        // TextView load_more = (TextView) findViewById(R.id.message_load_more);
        message_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {


            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                if (i == 0) {
                    length += 10;

                    api = new ApiConnector(getApplicationContext());
                    if (api.isNetworkAvailable()) {
                        // todo find better trigger, and fix
                        //Toast.makeText(getApplicationContext(), getString(R.string.loading_more)+" - under development", Toast.LENGTH_LONG).show();

                        // last_index = message_list.getFirstVisiblePosition();
                        // DashboardInit task = new DashboardInit();
                        //task.execute(new String[]{null, null});
                    } else {
                        // Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class DashboardInit extends AsyncTask<String, Void, ArrayList<DataObject>> {
        @Override
        protected ArrayList<DataObject> doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(MessagesActivity.this);
            ArrayList<DataObject> messages = null;
            if (api.sessionInitiated()) {
                SharedPreferences settings = MessagesActivity.this.getSharedPreferences(Config.localStorageName, 0);

                int logged_user_id = settings.getInt("logged_user_id", 0);
                if (messageViewType.equals("response_inbox") && type[1] != null) {
                    author = api.getDetail(type[0], Integer.valueOf(type[1]));
                }

                Log.d(Config.DEBUG_TAG, "DashboardInit, filter: " + filter + ", length: " + length);

                messages = api.getData("messages", filter, false, Integer.toString(length));
            }

            // if (messages != null) Log.d(Config.DEBUG_TAG, "messages.size(): "+messages.size());

            if (messages == null || (messages.size() == 0)) {
                    DataObject object;
                    object = new ResourceObject(-1);
                    if (api.isNetworkAvailable()) {
                        ((ResourceObject) object).setSecondaryTitle(getString(R.string.nothing_found));
                    } else {
                        ((ResourceObject) object).setSecondaryTitle(getString(R.string.no_offline_data));
                    }
                    ((ResourceObject) object).setSubType(0);
                    //((ResourceObject) object).setIconId(null);
                    if (messages == null) {
                        ArrayList<DataObject> objects = new ArrayList<DataObject>();
                        objects.add(object);
                        return objects;
                    } else {
                        messages.add(object);
                    }
            }
            return messages;
        }

        @Override
        protected void onPostExecute(ArrayList<DataObject> result) {
            message_list_items = result;
            if (result != null) {

                String message_type = "inbox_message";

                if (objectType != null && !objectType.equals("user")) {
                    message_type = "inbox_message_chat";
                }
                ObjectListItemAdapter o = new ObjectListItemAdapter(MessagesActivity.this, message_type, result);
                o.setOnAdapterActionListener(new OnAdapterActionListener() {

                    @Override
                    public void onTrashMessageAction(int message_id) {
                        loader = loadingDialog();
                        Trasher task = new Trasher();
                        task.execute(String.valueOf(message_id));
                    }

                    @Override
                    public void onUntrashMessageAction(int message_id) {
                        loader = loadingDialog();
                        Untrasher task = new Untrasher();
                        task.execute(String.valueOf(message_id));
                    }

                    @Override
                    public void onAcceptMessageAction(int message_id, int sender_id) {
                        loader = loadingDialog();
                        Responder task = new Responder();
                        task.execute("accept", String.valueOf(message_id), String.valueOf(sender_id));
                    }

                    @Override
                    public void onDeclineMessageAction(int message_id, int sender_id) {
                        loader = loadingDialog();
                        Responder task = new Responder();
                        task.execute("decline", String.valueOf(message_id), String.valueOf(sender_id));
                    }

                    @Override
                    public void onChangeTag(int tag_id, boolean status) {

                    }
                });
                message_list.setAdapter(o);


                // scroll to latest message at bottom
                if (last_index == 0) {
                    last_index = message_list_items.size();
                    message_list.invalidate();
                    message_list.setSelectionFromTop(last_index, 10);
                }


            }

            if (messageViewType.equals("response_inbox")) {

                try {
                    if (objectType.equals("user")) {
                        actionBar.setTitle( ((UserObject) author).getName() + ": " + getString(R.string.menu_messages));
                    } else if (objectType.equals("group")) {
                        actionBar.setTitle( ((GroupObject) author).getTitle() + ": " + getString(R.string.menu_messages_group));
                    } else if (objectType.equals("resource")) {
                        actionBar.setTitle( ((ResourceObject) author).getTitle() + ": " + getString(R.string.menu_messages_resource));
                    }
                } catch (NullPointerException e) {}
            }

            loader.dismiss();
        }
    }

    private class MessageSender extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(MessagesActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {
                result = api.sendMessage(type[0], type[1], type[2]);

            }

            if (result) {
                DashboardInit task = new DashboardInit();
                task.execute(objectType, objectId);

                if (response_input != null) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            response_input.setText("");
                        }
                    });

                }
                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {

            } else {

            }
            loader.dismiss();
        }
    }

    private class Trasher extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(MessagesActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {
                result = api.sendToTrash(Integer.valueOf(type[0]));

            }

            if (result) {
                DashboardInit task = new DashboardInit();
                task.execute(objectType, objectId);


                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {

            } else {

            }
            if (response_input != null) {
                response_input.setText("");
            }
            loader.dismiss();
        }
    }

    private class Untrasher extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(MessagesActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {
                result = api.sendFromTrash(Integer.valueOf(type[0]));

            }

            if (result) {
                DashboardInit task = new DashboardInit();
                task.execute(objectType, objectId);


                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {

            } else {

            }
            if (response_input != null) {
                response_input.setText("");
            }
            loader.dismiss();
        }
    }

    private class Emptyer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(MessagesActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {
                result = api.emptyTrash();

            }

            if (result) {
                DashboardInit task = new DashboardInit();
                task.execute(objectType, objectId);


                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {

            } else {

            }
            if (response_input != null) {
                response_input.setText("");
            }
            loader.dismiss();
        }
    }

    private class Responder extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(MessagesActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {
                if (type[0].equals("accept")) {
                    result = api.acceptFriendship(Integer.valueOf(type[1]), Integer.valueOf(type[2]));
                } else {
                    result = api.declineFriendship(Integer.valueOf(type[1]), Integer.valueOf(type[2]));
                }
            }

            if (result) {
                DashboardInit task = new DashboardInit();
                task.execute(objectType, objectId);


                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {

            } else {

            }
            if (response_input != null) {
                response_input.setText("");
            }
            loader.dismiss();
        }
    }

    public String generateFilterStorage() {
        String result = "{";
        if (filter_removed.isChecked()) {

            result += "'filter_removed':true";
        }
        if (filter_opened.isChecked()) {
            if (!result.equals("{")) {
                result += ",";
            }
            result += "'filter_opened':true";
        }

        result += "}";
        return result;

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (objectType != null && !objectType.equals("user")) {

        } else {
            inflater.inflate(R.menu.messages, menu);
        }
        inflater.inflate(R.menu.help, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_empty_trash:

                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Emptyer task = new Emptyer();
                            task.execute();
                        }
                    });

                }
                return true;
            case R.id.menu_help:
                intent = new Intent(MessagesActivity.this, HelpActivity.class);

                intent.putExtra("topic", "messages");
                startActivity(intent);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // TextView load_more = (TextView) findViewById(R.id.message_load_more);
    // load_more.setVisibility(View.VISIBLE);



}

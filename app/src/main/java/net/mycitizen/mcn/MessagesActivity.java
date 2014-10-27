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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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

    String objectType, objectId, recipient;

    DataObject author;

    ActionBar actionBar;

    int logged_user_id;

    String messageViewType;

    int last_index = 0;
    int last_top = 0;

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
        actionBar.setTitle("Messages");
        actionBar.setIcon(null);
        Boolean removed_toggle_active = false;
        Boolean notopened_toggle_active = false;

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


        System.out.println(objectType + " " + objectId);

        messageViewType = intent.getStringExtra("type");

        if (messageViewType != null && messageViewType.equals("dialog_inbox")) {

            filter = "filter[type][0]=1&filter[type][1]=9&filter[type][2]=10&filter[user_id]=" + logged_user_id;
            setContentView(R.layout.dialog_inbox);

            filter_opened = (ToggleButton) findViewById(R.id.inbox_message_toggle);
            if (notopened_toggle_active) {
                filter_opened.setChecked(true);
                filter += "&filter[opened]=1";
            }
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
                    task.execute(new String[]{objectType, objectId});
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
                    task.execute(new String[]{objectType, objectId});
                }
            });

        } else {
            setContentView(R.layout.response_inbox);

            if (objectType.equals("user")) {
                filter = "filter[type][0]=1&filter[all_members_only][0][type]=1&filter[all_members_only][0][id]=" + logged_user_id + "&filter[all_members_only][1][type]=1&filter[all_members_only][1][id]=" + objectId;
                actionBar.setTitle("Messages");
            } else if (objectType.equals("group")) {
                filter = "filter[type][0]=8&filter[all_members_only][0][type]=2&filter[all_members_only][0][id]=" + objectId;
                actionBar.setTitle("Chat");
            } else if (objectType.equals("resource")) {
                // filter = "filter[type][0]=8&filter[resource_id]="+objectId;
                filter = "filter[type][0]=8&filter[all_members_only][0][type]=3&filter[all_members_only][0][id]=" + objectId;
                actionBar.setTitle("Comments");
            }

            response_input = (EditText) findViewById(R.id.response_input);

            Button send_response = (Button) findViewById(R.id.send_response);
            send_response.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    loader = loadingDialog();
                    System.out.println("OBJECT ID " + objectId);
                    String response = response_input.getText().toString();

                    MessageSender task = new MessageSender();
                    task.execute(new String[]{objectType, objectId, response});
                }
            });

            Button load_more = (Button) findViewById(R.id.message_load_more);
            if (load_more != null) {
                load_more.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MessagesActivity.this, "Loading more (under development)",
                        //        Toast.LENGTH_LONG).show();

                        System.out.println("clicked on load more");
                        ApiConnector api = new ApiConnector(MessagesActivity.this);
                        if (api.isNetworkAvailable()) {
                            Intent intent = new Intent(MessagesActivity.this, MessagesActivity.class);
                            intent.putExtra("objectType", objectType);
                            intent.putExtra("objectId", objectId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MessagesActivity.this, getString(R.string.not_available_offline),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        }

/*
        ImageView avatar = (ImageView) findViewById(R.id.avatar);
        avatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagesActivity.this, DetailActivity.class);
                intent.putExtra("ObjectType", "1");
                //intent.putExtra("ObjectId", String.valueOf(widget_object.getObjectId()));
                System.out.println("OBJECT ID " + objectId);

                //startActivity(intent);
            }
        });
        */
        loader = loadingDialog();

        api = new ApiConnector(this);

        DashboardInit task = new DashboardInit();
        task.execute(new String[]{objectType, objectId});

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
        messages.execute(new String[]{});
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
                if (messageViewType.equals("response_inbox")) {
                    author = api.getDetail(type[0], Integer.valueOf(type[1]));
                }
                messages = api.createDashboard("resource", filter, false, "10");
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
                        task.execute(new String[]{String.valueOf(message_id)});
                    }

                    @Override
                    public void onUntrashMessageAction(int message_id) {
                        loader = loadingDialog();
                        Untrasher task = new Untrasher();
                        task.execute(new String[]{String.valueOf(message_id)});
                    }

                    @Override
                    public void onAcceptMessageAction(int message_id, int sender_id) {
                        loader = loadingDialog();
                        Responder task = new Responder();
                        task.execute(new String[]{"accept", String.valueOf(message_id), String.valueOf(sender_id)});
                    }

                    @Override
                    public void onDeclineMessageAction(int message_id, int sender_id) {
                        loader = loadingDialog();
                        Responder task = new Responder();
                        task.execute(new String[]{"decline", String.valueOf(message_id), String.valueOf(sender_id)});
                    }

                    @Override
                    public void onChangeTag(int tag_id, boolean status) {

                    }
                });
                message_list.setAdapter(o);


                // scroll to bottom

                last_index = message_list_items.size();
                //message_list.setAdapter(new ObjectListItemAdapter(MessagesActivity.this, message_type, result));
                message_list.invalidate();

                message_list.setSelectionFromTop(last_index, 10);
                /*
                    if (last_index != 0) {

                        last_index = 0;
                        last_top = 0;
                    }
*/
            }

            if (messageViewType.equals("response_inbox")) {

                if (objectType.equals("user")) {
                    actionBar.setTitle("Messages (" + ((UserObject) author).getName() + ")");
                } else if (objectType.equals("group")) {
                    actionBar.setTitle("Chat (" + ((GroupObject) author).getTitle() + ")");
                } else if (objectType.equals("resource")) {
                    actionBar.setTitle("Comments (" + ((ResourceObject) author).getTitle() + ")");
                }
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
                task.execute(new String[]{objectType, objectId});

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
                task.execute(new String[]{objectType, objectId});


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
                task.execute(new String[]{objectType, objectId});


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
                task.execute(new String[]{objectType, objectId});


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
                task.execute(new String[]{objectType, objectId});


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
                            task.execute(new String[]{});
                        }
                    });

                }
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

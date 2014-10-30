package net.mycitizen.mcn;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.support.v7.app.ActionBar;

public class WidgetActivity extends BaseActivity implements OnTouchListener {
    public ApiConnector api;
    int last_index = 0;
    int last_top = 0;

    boolean forceReload = false;

    String length = "10";

    ProgressDialog loader = null;
    final Handler handler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what) {

                case 1:
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            loader = loadingDialog();
                        }
                    });
                    break;
                case 2:
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                        }
                    });

                    break;
                case 3:


                    break;
                case 4:

                    break;
                case 5:

                    break;
                case 6:
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            System.out.println("DataUpdate finish");
                            if (loader != null) {
                                loader.dismiss();
                            }

                        }
                    });
                    //myProgress.dismiss();
                    break;

            }
        }
    };
    ListView widget_list = null;
    ArrayList<DataObject> widget_list_items;
    ToggleButton tab_user, tab_group, tab_resource;
    String selected_tab = "user";
    float last_x = 0;
    AlertDialog dialog;
    ActionBar actionBar;
    Button menu_filter;
    private KillReceiver mKillReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKillReceiver = new KillReceiver();
        registerReceiver(mKillReceiver,
                IntentFilter.create("kill", "content://sparta"));


        api = new ApiConnector(this);

        setContentView(R.layout.widget_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Widgets");
        actionBar.setIcon(null);
        actionBar.hide();

        widget_list = (ListView) findViewById(R.id.widget_list);
        widget_list.setOnTouchListener(this);

        widget_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (widget_list_items != null) {
                    System.out.println("Size: " + widget_list_items.size() + ", Position: " + position);
                    DataObject widget_object = widget_list_items.get(position);

                    // message, not clickable
                    if (String.valueOf(widget_object.getObjectId()).equals("-1")) {
                        return;
                    }

                    // load more
                    if (String.valueOf(widget_object.getObjectId()).equals("0")) {

                        Intent intent = getIntent();
                        //intent.putExtra("origin", "widget");
                        forceReload = true;
                        length = Integer.toString(widget_list_items.size() + 9);

                        System.out.println("load more, new length: " + length);
                        DashboardInit task = new DashboardInit();
                        last_index = widget_list.getFirstVisiblePosition();
                        View v = widget_list.getChildAt(0);
                        int top = (v == null) ? 0 : v.getTop();
                        last_top = top;
                        loader = loadingDialog();
                        task.execute(new String[]{selected_tab, length, "load_more"});
                        // finish();
                        return;
                    }

                    Intent intent = new Intent(WidgetActivity.this, DetailActivity.class);

                    intent.putExtra("ObjectType", widget_object.getObjectType());
                    intent.putExtra("ObjectId", String.valueOf(widget_object.getObjectId()));

                    System.out.println("ObjectId: " + String.valueOf(widget_object.getObjectId()));

                    last_index = widget_list.getFirstVisiblePosition();
                    View v = widget_list.getChildAt(0);
                    int top = (v == null) ? 0 : v.getTop();
                    last_top = top;

                    startActivity(intent);
                }
            }
        });


        //loader = loadingDialog();
        DashboardInit task = new DashboardInit();
        task.execute(new String[]{"user", length});

        tab_user = (ToggleButton) findViewById(R.id.widget_select_user);
        tab_group = (ToggleButton) findViewById(R.id.widget_select_group);
        tab_resource = (ToggleButton) findViewById(R.id.widget_select_resource);

        tab_user.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loader = loadingDialog();
                DashboardInit task = new DashboardInit();
                length = Config.retrieveLength(getApplicationContext());
                task.execute(new String[]{"user", length});
                selected_tab = "user";
                tab_user.setChecked(true);
                tab_group.setChecked(false);
                tab_resource.setChecked(false);
            }
        });


        tab_group.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loader = loadingDialog();
                DashboardInit task = new DashboardInit();
                length = Config.retrieveLength(getApplicationContext());
                task.execute(new String[]{"group", length});
                selected_tab = "group";
                tab_user.setChecked(false);
                tab_group.setChecked(true);
                tab_resource.setChecked(false);
            }
        });


        tab_resource.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loader = loadingDialog();
                DashboardInit task = new DashboardInit();
                length = Config.retrieveLength(getApplicationContext());
                task.execute(new String[]{"resource", length});
                selected_tab = "resource";
                tab_user.setChecked(false);
                tab_group.setChecked(false);
                tab_resource.setChecked(true);
            }
        });

        menu_filter = (Button) findViewById(R.id.widget_menu_filter);
        SharedPreferences settings = WidgetActivity.this.getSharedPreferences("MyCitizen", 0);
        if (settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        }
        menu_filter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WidgetActivity.this, FilterMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
            }
        });

        dashboard_button = (Button) findViewById(R.id.widget_menu_dashboard);

        dashboard_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WidgetActivity.this, DashboardMenuActivity.class);

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
                Intent intent = new Intent(WidgetActivity.this, MessagesActivity.class);

                //String message = editText.getText().toString();
                intent.putExtra("type", "dialog_inbox");

                startActivity(intent);
            }
        });


    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = WidgetActivity.this.getSharedPreferences("MyCitizen", 0);
        if (settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        } else {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_off), null, null);
        }

        Intent intent = getIntent();
        String origin = intent.getStringExtra("origin");
        System.out.println("origin: " + origin);

        if ((origin != null && (origin.equals("filter") || origin.equals("login") || origin.equals("widget")))
                || forceReload) {
            loader = loadingDialog();
            DashboardInit task = new DashboardInit();
            task.execute(new String[]{selected_tab, length});
            intent.removeExtra("origin");
            forceReload = false;
        } else {
// temporarily
            if (loader != null) {
                loader.dismiss();
            }
            // loader = loadingDialog();
            // DashboardInit task = new DashboardInit();
            // task.execute(new String[]{selected_tab, length});

        }


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last_x = event.getX();
            case MotionEvent.ACTION_UP:
                if (event.getX() > last_x || event.getX() < last_x) {
                    System.out.println(event.getX() + " " + last_x);
                    if ((event.getX() - last_x) < -120) {
                        System.out.println("SWIPE LEFT");
                        last_x = 0;
                        if (selected_tab.equals("user")) {
                            loader = loadingDialog();
                            DashboardInit task = new DashboardInit();
                            length = Config.retrieveLength(getApplicationContext());
                            task.execute(new String[]{"group", length});
                            selected_tab = "group";
                            tab_user.setChecked(false);
                            tab_group.setChecked(true);
                            tab_resource.setChecked(false);
                        } else if (selected_tab.equals("group")) {
                            loader = loadingDialog();
                            DashboardInit task = new DashboardInit();
                            length = Config.retrieveLength(getApplicationContext());
                            task.execute(new String[]{"resource", length});
                            selected_tab = "resource";
                            tab_user.setChecked(false);
                            tab_group.setChecked(false);
                            tab_resource.setChecked(true);
                        }

                        return true;
                    } else if ((event.getX() - last_x) > 120) {
                        System.out.println("SWIPE RIGHT");
                        last_x = 0;

                        if (selected_tab.equals("resource")) {
                            loader = loadingDialog();
                            DashboardInit task = new DashboardInit();
                            length = Config.retrieveLength(getApplicationContext());
                            task.execute(new String[]{"group", length});
                            selected_tab = "group";
                            tab_user.setChecked(false);
                            tab_group.setChecked(true);
                            tab_resource.setChecked(false);
                        } else if (selected_tab.equals("group")) {
                            loader = loadingDialog();
                            DashboardInit task = new DashboardInit();
                            task.execute(new String[]{"user", length});
                            selected_tab = "user";
                            tab_user.setChecked(true);
                            tab_group.setChecked(false);
                            tab_resource.setChecked(false);
                        }
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mKillReceiver);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WidgetActivity.this);
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

    private class DashboardInit extends AsyncTask<String, Void, ArrayList<DataObject>> {

        @Override
        protected ArrayList<DataObject> doInBackground(String... urls) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //widget_list.setAdapter(null);
                }
            });


            String[] type = urls;

            ApiConnector api = new ApiConnector(WidgetActivity.this);
            ArrayList<DataObject> widgets = null;

            if (api.sessionInitiated()) {
                String filter = null;
                if (type[0].equals("resource")) {
                    filter = "filter[type][0]=2&filter[type][1]=3&filter[type][2]=4&filter[type][3]=5&filter[type][5]=6";
                }
                if (type.length > 1) {
                    String length = type[1];
                } else {
                    String length = "10"; //Config.retrieveLength(getApplicationContext());
                }

                widgets = api.getData(type[0], filter, true, length);

                if (widgets != null)
                    System.out.println("Asked for " + length + " items, received: " + widgets.size()+", used filter: "+filter);
                else
                    System.out.println("Asked for " + length + " items, received: NULL, used filter: "+filter);

                //System.out.println("Number of items: "+widgets.size());

                if (widgets != null && widgets.size() == 0) {
                    if (type[0].equals("user")) {
                        DataObject object = null;
                        object = new UserObject(-1);
                        if (api.isNetworkAvailable()) {
                            ((UserObject) object).setName(getString(R.string.nothing_found));
                        } else {
                            ((UserObject) object).setName(getString(R.string.no_offline_data));
                        }
                        ((UserObject) object).setIconId(null);
                        ((UserObject) object).setNow_online("");
                        widgets.add(object);
                    }

                    if (type[0].equals("group")) {
                        DataObject object = null;
                        object = new GroupObject(-1);
                        if (api.isNetworkAvailable()) {
                            ((GroupObject) object).setTitle(getString(R.string.nothing_found));
                        } else {
                            ((GroupObject) object).setTitle(getString(R.string.no_offline_data));
                        }
                        ((GroupObject) object).setIconId(null);
                        widgets.add(object);
                    }
                    if (type[0].equals("resource")) {
                        DataObject object = null;
                        object = new ResourceObject(-1);
                        if (api.isNetworkAvailable()) {
                            ((ResourceObject) object).setTitle(getString(R.string.nothing_found));
                        } else {
                            ((ResourceObject) object).setTitle(getString(R.string.no_offline_data));
                        }
                        ((ResourceObject) object).setIconId(null);

                        widgets.add(object);
                    }
                }

                // if widgets nicht null und size == 0 , Nachricht zeigen
                if (api.isNetworkAvailable()) {
                    api.getSupportedTags();
                }
            }
            return widgets;
        }

        @Override
        protected void onPostExecute(ArrayList<DataObject> result) {
            widget_list_items = result;

            if (result != null) {
                widget_list.setAdapter(new ObjectListItemAdapter(WidgetActivity.this, "widget", result));
                widget_list.invalidate();

                if (last_index != 0) {
                    widget_list.setSelectionFromTop(last_index, last_top);
                    last_index = 0;
                    last_top = 0;
                }
            }

            if (loader != null) {
                try {
                    loader.dismiss();
                    // todo set tabs
                    /*
                    tab_user.setChecked(true);
                    tab_group.setChecked(false);
                    tab_resource.setChecked(false);
                    */
                } catch (IllegalArgumentException e) {
                    System.out.println("I caught an IllegalArgumentException.");
                }
            }

        }
    }

    class StatusUpdater extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();

            String action_type = b.getString("type");

            if (action_type.equals("start")) {
                System.out.println("DataUpdater startr eceived ");
                Message msg = handler.obtainMessage();
                msg.what = 1;

                handler.dispatchMessage(msg);
            } else if (action_type.equals("progress")) {
                Message msg = handler.obtainMessage();
                msg.what = 2;
                Bundle data = new Bundle();
                data.putInt("percentage", b.getInt("percentage"));
                data.putInt("file_count", b.getInt("file_count"));
                data.putInt("file_total_count", b.getInt("file_total_count"));
                data.putString("download_name", b.getString("download_name"));
                msg.setData(data);
                handler.dispatchMessage(msg);
            } else if (action_type.equals("finish")) {
                Message msg = handler.obtainMessage();
                msg.what = 6;
                Bundle data = new Bundle();

                msg.setData(data);
                handler.dispatchMessage(msg);
            }
        }

    }

    private final class KillReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }

}

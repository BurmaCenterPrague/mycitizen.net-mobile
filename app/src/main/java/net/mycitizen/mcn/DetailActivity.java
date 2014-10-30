package net.mycitizen.mcn;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.widget.VideoView;

import java.security.acl.Group;


public class DetailActivity extends BaseActivity {
    public ApiConnector api;

    String title;
    ImageView avatar;
    TextView information;
    ListView tag_list = null;
    Button button_subscribe;
    Button button_unsubscribe;
    TextView realName;
    TextView online_status;
    ProgressDialog loader = null;
    AlertDialog dialog;
    String objectType;
    String objectId;
    int contentType;
    String url;
    String videoId;

    String subscriptionStatus;

    ActionBar actionBar;

    PopupWindow popupMessage;
    LinearLayout layoutOfPopup;
    Button insidePopupButton;
    TextView popupText;
    ListView popupList;

    boolean connection_rights = false;
    boolean map_visible = true;

    private VideoView videoView;
    private MediaController mController;
    private Uri uriYouTube;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loader = loadingDialog();
        api = new ApiConnector(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.loadingdotdotdot));
        actionBar.setIcon(null);

        Intent intent = getIntent();
        objectType = intent.getStringExtra("ObjectType");
        objectId = intent.getStringExtra("ObjectId");

        setContentView(R.layout.detail_view);
        realName = (TextView) findViewById(R.id.real_name);
        //title = (TextView) findViewById(R.id.detail_title);
        information = (TextView) findViewById(R.id.information);
        online_status = (TextView) findViewById(R.id.online_status);

        // tag_list = (ListView) findViewById(R.id.tag_list);
        tag_list = new ListView(this);

        avatar = (ImageView) findViewById(R.id.avatar);

        popupInit();


        DashboardInit task = new DashboardInit();
        task.execute(new String[]{objectType, objectId});

        Button menu_filter = (Button) findViewById(R.id.widget_menu_filter);
        SharedPreferences icon_settings = DetailActivity.this.getSharedPreferences("MyCitizen", 0);
        if (icon_settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        }
        menu_filter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, FilterMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
            }
        });

        ImageView avatar = (ImageView) findViewById(R.id.avatar);

        avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("objectType: " + objectType);
                Intent browserIntent;

                if (objectType.equals("user")) {
                    if (url != null && !url.equals("")) {
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    }
                }

                if (objectType.equals("group")) {

                }

                if (objectType.equals("resource")) {
                    System.out.println("content_type: " + contentType);
                    System.out.println("url: " + url);

                    switch (contentType) {
                        case 30: // organization
                            // open link
                            if (url != null && !url.equals("")) {
                                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }
                            break;
                        case 40: // document
                            // open link
                            if (url != null && !url.equals("")) {
                                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }
                            break;
                        case 60: // other
                            // open link
                            if (url != null && !url.equals("")) {
                                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }
                            break;
                        case 20: // event
                            // open link
                            if (url != null && !url.equals("")) {
                                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }
                            break;
                        // Vimeo:
                        case 52: // media_vimeo
                            // open link
                            if (url != null && !url.equals("")) {
                                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }

                        /*
                    RelativeLayout video_wrapper = (RelativeLayout) findViewById(R.id.detail_video_webview_wrapper);
                    if (video_wrapper.getVisibility() == View.VISIBLE) {
                        video_wrapper.setVisibility(View.GONE);
                    } else {


                        video_wrapper.setVisibility(View.VISIBLE);
                        WebView mWebView = (WebView) findViewById(R.id.detail_video_webview);
                        mWebView.getSettings().setJavaScriptEnabled(true);
                        mWebView.getSettings().setAppCacheEnabled(true);
                        mWebView.getSettings().setDomStorageEnabled(true);
                        mWebView.getSettings().
                        //  WebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                        mWebView.loadUrl("http://player.vimeo.com/video/24577973?player_id=player&autoplay=1&title=0&byline=0&portrait=0&api=1&maxheight=480&maxwidth=800");

                    }
                    */
                            break;

                        case 51: // media_youtube

                            // YouTube: toggle open+start/close

                            Intent intent = new Intent(DetailActivity.this, DetailYoutubeVideo.class);
                            intent.putExtra("videoId", videoId);
                            intent.putExtra("title", title);

                            startActivity(intent);


                            /* old way
                            RelativeLayout video_wrapper = (RelativeLayout) findViewById(R.id.detail_video_youtube_wrapper);
                            if (video_wrapper.getVisibility() == View.VISIBLE) {
                                video_wrapper.setVisibility(View.GONE);
                                if (videoView != null) {
                                    videoView.stopPlayback();
                                }
                                return;
                            } else {
                                videoView = (VideoView) findViewById(R.id.detail_video_youtube);
                                mController = new MediaController(DetailActivity.this);
                                videoView.setMediaController(mController);
                                video_wrapper.setVisibility(View.VISIBLE);
                                videoView.requestFocus();
                                RTSPUrlTask task = new RTSPUrlTask();
                                task.execute(url);
                            }

                            */
                            break;

                        case 55: // media_soundcloud

                            break;
                        case 54: // media_bambuser
                            break;
                    }
                }
            }
        });


        avatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (url != null && !url.equals("")) {
                    Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
                } else if (videoId != null && !videoId.equals("")) {
                    Toast.makeText(getApplicationContext(), "YouTube", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_link), Toast.LENGTH_LONG).show();
                }

                return true;
            }
        });

        dashboard_button = (Button) findViewById(R.id.widget_menu_dashboard);

        dashboard_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, DashboardMenuActivity.class);

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
                Intent intent = new Intent(DetailActivity.this, MessagesActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("type", "dialog_inbox");

                startActivity(intent);
            }
        });

        button_subscribe = (Button) findViewById(R.id.button_subscribe);
        button_unsubscribe = (Button) findViewById(R.id.button_unsubscribe);

        if (objectType.equals("user")) {
            button_subscribe.setText(getString(R.string.request_friendship));
            button_unsubscribe.setText(getString(R.string.cancel_friendship));
            SharedPreferences settings = DetailActivity.this.getSharedPreferences(Config.localStorageName, 0);

            int logged_user_id = settings.getInt("logged_user_id", 0);
            System.out.println("IDDDD " + logged_user_id + " " + objectId);
            if (logged_user_id > 0 && Integer.valueOf(objectId) == logged_user_id) {
                System.out.println("VISIBILITY GONE");
                button_subscribe.setVisibility(View.GONE);
                button_unsubscribe.setVisibility(View.GONE);
            }
        } else if (objectType.equals("group")) {
            button_subscribe.setText(getString(R.string.join_group));
            button_unsubscribe.setText(getString(R.string.leave_group));

            SharedPreferences settings = DetailActivity.this.getSharedPreferences(Config.localStorageName, 0);

            int logged_user_id = settings.getInt("logged_user_id", 0);
            System.out.println("IDDDD " + logged_user_id + " " + objectId);
            if (logged_user_id > 0 && Integer.valueOf(objectId) == logged_user_id) {
                System.out.println("VISIBILITY GONE");
                button_subscribe.setVisibility(View.GONE);
                button_unsubscribe.setVisibility(View.GONE);
            }
        } else {
            button_subscribe.setText(getString(R.string.subscribe));
            button_unsubscribe.setText(getString(R.string.unsubscribe));

            SharedPreferences settings = DetailActivity.this.getSharedPreferences(Config.localStorageName, 0);

            int logged_user_id = settings.getInt("logged_user_id", 0);
            System.out.println("IDDDD " + logged_user_id + " " + objectId);
            if (logged_user_id > 0 && Integer.valueOf(objectId) == logged_user_id) {
                System.out.println("VISIBILITY GONE");
                button_subscribe.setVisibility(View.GONE);
                button_unsubscribe.setVisibility(View.GONE);
            }
        }

        button_subscribe.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    loader = loadingDialog();
                    String objectAction = "1";

                    SubscribeInit task = new SubscribeInit();
                    task.execute(new String[]{objectType, objectId, objectAction});
                }
            }
        });

        button_unsubscribe.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                    builder.setMessage(getString(R.string.really_unsubscribe))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                    dialog.cancel();
                                    loader = loadingDialog();
                                    String objectAction = "0";

                                    UnSubscribeInit task = new UnSubscribeInit();
                                    task.execute(new String[]{objectType, objectId, objectAction});

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


            ApiConnector api = new ApiConnector(DetailActivity.this);
            DataObject object = null;
            if (api.sessionInitiated()) {
                object = api.getDetail(type[0], Integer.valueOf(type[1]));
            }
            return object;
        }

        @Override
        protected void onPostExecute(DataObject result) {
            //parse data to view

            //todo different toast for offline
            if (result == null) {
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.item_not_available_offline), Toast.LENGTH_LONG).show();

                    DetailActivity.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.detail_unaccessible), Toast.LENGTH_LONG).show();

                    DetailActivity.this.finish();
                }
            } else {
                if (objectType.equals("user")) {
                    try {
                        title = ((UserObject) result).getName();
                        actionBar.setTitle(((UserObject) result).getName());
                        realName.setText(((UserObject) result).getRealName());
                        realName.setGravity(Gravity.CENTER_HORIZONTAL);

                        online_status.setText(Html.fromHtml(((UserObject) result).getOnlineStatusOutput()));
                    } catch (NullPointerException e) {
                        title = getString(R.string.undefined_name);
                        actionBar.setTitle(getString(R.string.undefined_name));
                    }

                    if (((UserObject) result).getIconBitmap() != null) {

                        avatar.setImageBitmap(((UserObject) result).getIconBitmap());

                    } else {
                        System.out.println("WTF");
                        avatar.setImageBitmap(api.defaultUserIcon());
                    }
                    try {
                        Context ctx = getApplicationContext();
                        information.setText(Html.fromHtml(((UserObject) result).getDetail(ctx)));
                        information.setMovementMethod(LinkMovementMethod.getInstance());

                    } catch (NullPointerException e) {
                        information.setText("");
                    }

                    try {

                        if (((UserObject) result).getRelationshipMeUser() == 0 && ((UserObject) result).getRelationshipUserMe() == 0) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }

                        if (((UserObject) result).getRelationshipMeUser() == 0 && ((UserObject) result).getRelationshipUserMe() == 1) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                            button_subscribe.setText(getString(R.string.accept_friendship));
                        }

                        if (((UserObject) result).getRelationshipMeUser() == 1 && ((UserObject) result).getRelationshipUserMe() == 0) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.GONE);
                        }

                        if (((UserObject) result).getRelationshipMeUser() == 1 && ((UserObject) result).getRelationshipUserMe() == 1) {
                            // connection_rights = true;
                            button_unsubscribe.setVisibility(View.VISIBLE);
                            button_subscribe.setVisibility(View.GONE);
                        }
                        if (((UserObject) result).getRelationshipMeUser() == 1 && ((UserObject) result).getRelationshipUserMe() == 2) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }
                        if (((UserObject) result).getRelationshipMeUser() == 2 && ((UserObject) result).getRelationshipUserMe() == 1) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }
                        if (((UserObject) result).getRelationshipMeUser() == 2 && ((UserObject) result).getRelationshipUserMe() == 2) {
                            connection_rights = true;
                            button_unsubscribe.setVisibility(View.VISIBLE);
                            button_subscribe.setVisibility(View.GONE);
                        }

                        if (((UserObject) result).getRelationshipMeUser() == 3) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }

                        if (((UserObject) result).getRelationshipUserMe() == 3) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }

                        if (((UserObject) result).getPosition() == null) {
                            map_visible = false;
                        }


                        SharedPreferences settings = DetailActivity.this.getSharedPreferences(Config.localStorageName, 0);

                        int logged_user_id = settings.getInt("logged_user_id", 0);

                        // permission to see myself
                        if (logged_user_id > 0 && Integer.valueOf(objectId) == logged_user_id) {
                            connection_rights = true;
                            button_subscribe.setVisibility(View.GONE);
                            button_unsubscribe.setVisibility(View.GONE);
                        }

                        // system moderators and administrators can always view
                        int user_access_level = settings.getInt("user_access_level", 1);
                        if (user_access_level > 1) {
                            connection_rights = true;
                        }

                    } catch (NullPointerException e) {

                    }
                    try {
                        subscriptionStatus = String.valueOf(((UserObject) result).getRelationshipMeUser()) + String.valueOf(((UserObject) result).getRelationshipUserMe());
                    } catch (NullPointerException e) {

                    }
                    try {
                        url = ((UserObject) result).getUrl();
                        if (url != null && !url.equals("") && !url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "http://" + url;
                        }
                    } catch (NullPointerException e) {
                        url = "";
                    }

                } else if (objectType.equals("group")) {
                    try {
                        title = ((GroupObject) result).getTitle();
                        realName.setText(((GroupObject) result).getTitle());
                        actionBar.setTitle(((GroupObject) result).getTitle());
                    } catch (NullPointerException e) {
                        title = "Undefined Name";
                        actionBar.setTitle("Undefined Name");
                    }

                    try {
                        Context ctx = getApplicationContext();
                        information.setText(Html.fromHtml(((GroupObject) result).getDetail(ctx)));
                        information.setMovementMethod(LinkMovementMethod.getInstance());
                    } catch (NullPointerException e) {
                        information.setText("");
                    }
                    try {
                        if (((GroupObject) result).getIconBitmap() != null) {

                            avatar.setImageBitmap(((GroupObject) result).getIconBitmap());

                        }
                        System.out.println("Relationship me -> group: " + ((GroupObject) result).getRelationshipMeGroup());
                        if (((GroupObject) result).getRelationshipMeGroup() == 0) {
                            System.out.println("Setting to gone");
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }
                        if (((GroupObject) result).getRelationshipMeGroup() == 1) {
                            connection_rights = true;
                            button_subscribe.setVisibility(View.GONE);
                            button_unsubscribe.setVisibility(View.VISIBLE);
                        }
                        /*
                        if (((GroupObject) result).getRelationshipMeGroup() == 2) {
                            connection_rights = true;
                            button_subscribe.setVisibility(View.GONE);
                            button_unsubscribe.setVisibility(View.VISIBLE);
                        }
                        if (((GroupObject) result).getRelationshipMeGroup() == 3) {
                            button_subscribe.setVisibility(View.VISIBLE);
                            button_unsubscribe.setVisibility(View.GONE);
                        }
                        */
                    } catch (NullPointerException e) {

                    }
                    if (((GroupObject) result).getPosition() == null) {
                        map_visible = false;
                    }
                    try {
                        subscriptionStatus = String.valueOf(((GroupObject) result).getRelationshipMeGroup());
                    } catch (NullPointerException e) {

                    }
                } else if (objectType.equals("resource")) {
                    int subtype = ((ResourceObject) result).getSubType();
                    switch (subtype) {
                        case 2:
                            avatar.setImageDrawable(getResources().getDrawable(R.drawable.ico_calendar));
                            break;
                        case 3:
                            avatar.setImageDrawable(getResources().getDrawable(R.drawable.ico_organization));
                            break;
                        case 4:
                            avatar.setImageDrawable(getResources().getDrawable(R.drawable.ico_note));
                            break;
                        case 5:
                            avatar.setImageDrawable(getResources().getDrawable(R.drawable.ico_video));
                            break;
                        case 6:
                            avatar.setImageDrawable(getResources().getDrawable(R.drawable.ico_note));
                            break;
                    }

                    try {
                        contentType = ((ResourceObject) result).getContentType();
                    } catch (NullPointerException e) {
                        contentType = 0;
                    }

                    try {
                        if (contentType == 51) {
                            videoId = ((ResourceObject) result).getUrl();
                        } else {
                            url = ((ResourceObject) result).getUrl();
                            if (url != null && !url.equals("") && !url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "http://" + url;
                            }
                        }
                    } catch (NullPointerException e) {
                        url = "";
                    }

                    try {
                        title = ((ResourceObject) result).getTitle();
                        realName.setText(((ResourceObject) result).getTitle());
                        actionBar.setTitle(((ResourceObject) result).getTitle());
                    } catch (NullPointerException e) {
                        title = "Undefined Title";
                        actionBar.setTitle("Undefined Title");
                    }
                    //avatar.
                    try {
                        Context ctx = getApplicationContext();
                        information.setText(Html.fromHtml(((ResourceObject) result).getDetail(ctx)));
                        information.setMovementMethod(MovementCheck.getInstance());
                        //information.setMovementMethod(LinkMovementMethod.getInstance());
                    } catch (NullPointerException e) {
                        information.setText("");
                    }
                    try {
                        System.out.println("Relationship me -> resource: " + ((ResourceObject) result).getRelationshipMeResource());
                        if (((ResourceObject) result).getRelationshipMeResource() == 0) {
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }
                        if (((ResourceObject) result).getRelationshipMeResource() == 1) {
                            connection_rights = true;
                            button_subscribe.setVisibility(View.GONE);
                            button_unsubscribe.setVisibility(View.VISIBLE);
                        }
                        /*
                        if (((ResourceObject) result).getRelationshipMeResource() == 2) {
                            connection_rights = true;
                            button_subscribe.setVisibility(View.GONE);
                            button_unsubscribe.setVisibility(View.VISIBLE);
                        }
                        if (((ResourceObject) result).getRelationshipMeResource() == 3) {
                            button_subscribe.setVisibility(View.VISIBLE);
                            button_unsubscribe.setVisibility(View.GONE);
                        }
                        */
                    } catch (NullPointerException e) {

                    }
                    if (((ResourceObject) result).getPosition() == null) {
                        map_visible = false;
                    }
                    try {
                        subscriptionStatus = String.valueOf(((ResourceObject) result).getRelationshipMeResource());
                    } catch (NullPointerException e) {

                    }
                }
                ActivityCompat.invalidateOptionsMenu(DetailActivity.this);
                try {
                    tag_list.setAdapter(new ObjectListItemAdapter(DetailActivity.this, "tag", result.getTags()));
                } catch (NullPointerException e) {

                }

            }
            loader.dismiss();
        }
    }

    private class SubscribeInit extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;


            ApiConnector api = new ApiConnector(DetailActivity.this);
            String object = null;
            if (api.sessionInitiated()) {
                object = api.subscribe(type[0], type[1], Integer.valueOf(type[2]));

            }
            return object;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            //parse data to view
            if (objectType.equals("user")) {

                if (result.equals("00")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }

                if (result.equals("01")) {
                    button_unsubscribe.setVisibility(View.VISIBLE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }

                if (result.equals("10")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.GONE);
                }

                if (result.equals("11")) {
                    button_unsubscribe.setVisibility(View.VISIBLE);
                    button_subscribe.setVisibility(View.GONE);
                }
                if (result.equals("12")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("21")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("22")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                subscriptionStatus = result;
            } else if (objectType.equals("group")) {

                if (result.equals("0")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("1")) {
                    button_subscribe.setVisibility(View.GONE);
                    button_unsubscribe.setVisibility(View.VISIBLE);
                }
                subscriptionStatus = result;
            } else if (objectType.equals("resource")) {


                if (result.equals("0")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("1")) {
                    button_subscribe.setVisibility(View.GONE);
                    button_unsubscribe.setVisibility(View.VISIBLE);
                }
                subscriptionStatus = result;
            }

            loader.dismiss();
        }
    }

    private class UnSubscribeInit extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;


            ApiConnector api = new ApiConnector(DetailActivity.this);
            String object = null;
            if (api.sessionInitiated()) {
                object = api.subscribe(type[0], type[1], Integer.valueOf(type[2]));

            }
            return object;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            //parse data to view
            if (objectType.equals("user")) {

                if (result.equals("00")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }

                if (result.equals("01")) {
                    button_unsubscribe.setVisibility(View.VISIBLE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }

                if (result.equals("10")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.GONE);
                }

                if (result.equals("11")) {
                    button_unsubscribe.setVisibility(View.VISIBLE);
                    button_subscribe.setVisibility(View.GONE);
                }
                if (result.equals("12")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("21")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("22")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                subscriptionStatus = result;
            } else if (objectType.equals("group")) {

                if (result.equals("0")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("1")) {
                    button_subscribe.setVisibility(View.GONE);
                    button_unsubscribe.setVisibility(View.VISIBLE);
                }
                subscriptionStatus = result;
            } else if (objectType.equals("resource")) {


                if (result.equals("0")) {
                    button_unsubscribe.setVisibility(View.GONE);
                    button_subscribe.setVisibility(View.VISIBLE);
                }
                if (result.equals("1")) {
                    button_subscribe.setVisibility(View.GONE);
                    button_unsubscribe.setVisibility(View.VISIBLE);
                }
                subscriptionStatus = result;
            }

            loader.dismiss();
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (objectType.equals("group")) {
            inflater.inflate(R.menu.detail_group, menu);
        } else if (objectType.equals("resource")) {
            inflater.inflate(R.menu.detail_resource, menu);
        } else {
            inflater.inflate(R.menu.detail, menu);
        }

        inflater.inflate(R.menu.detail_tags, menu);
        inflater.inflate(R.menu.help, menu);

        if (!connection_rights) {
            MenuItem menu_friends = menu.findItem(R.id.menu_friends);
            MenuItem menu_connections = menu.findItem(R.id.menu_connections);
            MenuItem menu_messages = menu.findItem(R.id.menu_messages);
            if (menu_friends != null) {
                menu_friends.setVisible(false);
            }
            if (menu_connections != null) {
                menu_connections.setVisible(false);
            }
            if (menu_messages != null) {
                menu_messages.setVisible(false);
            }

        }
        if (!map_visible) {
            menu.findItem(R.id.menu_locations).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_friends:
                popupMessage.dismiss();
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    intent = new Intent(DetailActivity.this, DetailFriendsActivity.class);
                    intent.putExtra("objectType", objectType);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
                return true;
            case R.id.menu_connections:
                popupMessage.dismiss();
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    intent = new Intent(DetailActivity.this, DetailConnectionsActivity.class);
                    intent.putExtra("objectType", objectType);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
                return true;

            case R.id.menu_locations:
                popupMessage.dismiss();
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    intent = new Intent(DetailActivity.this, MapActivity.class);
                    intent.putExtra("type", "show");
                    intent.putExtra("objectType", objectType);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
                return true;
            case R.id.menu_messages:
                popupMessage.dismiss();
                intent = new Intent(DetailActivity.this, MessagesActivity.class);
                intent.putExtra("type", "response_inbox");
                intent.putExtra("objectType", objectType);
                intent.putExtra("objectId", objectId);
                intent.putExtra("recipient", title);

                startActivity(intent);
                return true;


            case R.id.menu_tags:
                if (popupMessage.isShowing()) {
                    popupMessage.dismiss();
                } else {
                    TextView popup_anchor = (TextView) findViewById(R.id.popup_anchor);
                    popupMessage.showAsDropDown(popup_anchor, 0, 0);
                }
                return true;

            case R.id.menu_help:
                intent = new Intent(DetailActivity.this, HelpActivity.class);

                intent.putExtra("topic", objectType + "_detail");
                startActivity(intent);
                return true;

            default:
                popupMessage.dismiss();
                return super.onOptionsItemSelected(item);

        }
    }


    public void popupInit() {
        layoutOfPopup = new LinearLayout(this);
        TextView title = new TextView(this);
        title.setText(R.string.tags);
        title.setTextSize(18);
        title.setPadding(20, 5, 5, 10);
        layoutOfPopup.addView(tag_list);
        tag_list.addHeaderView(title);
        layoutOfPopup.setBackgroundColor(Color.parseColor("#DDEAE9E3"));
        tag_list.setPadding(5, 30, 5, 5);
        popupMessage = new PopupWindow(layoutOfPopup, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        popupMessage.setContentView(layoutOfPopup);
    }

    /*
    private class RTSPUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = getRTSPVideoUrl(urls[0]);
            System.out.println("response: " + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("result: " + result);
            startPlaying(result);
        }

        public String getRTSPVideoUrl(String urlYoutube) {
            try {
                String gdy = "http://gdata.youtube.com/feeds/api/videos/";
                DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                String id = extractYoutubeId(urlYoutube);
                URL url = new URL(gdy + id);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                Document doc = dBuilder.parse(connection.getInputStream());
                Element el = doc.getDocumentElement();
                NodeList list = el.getElementsByTagName("media:content");
                String cursor = urlYoutube;
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node != null) {
                        NamedNodeMap nodeMap = node.getAttributes();
                        HashMap<String, String> maps = new HashMap<String, String>();
                        for (int j = 0; j < nodeMap.getLength(); j++) {
                            Attr att = (Attr) nodeMap.item(j);
                            maps.put(att.getName(), att.getValue());
                        }
                        if (maps.containsKey("yt:format")) {
                            String f = maps.get("yt:format");
                            if (maps.containsKey("url"))
                                cursor = maps.get("url");
                            if (f.equals("1"))
                                return cursor;
                        }
                    }
                }
                return cursor;
            } catch (Exception ex) {
                return urlYoutube;
            }
        }

        public String extractYoutubeId(String url) throws MalformedURLException {
            String query = new URL(url).getQuery();
            String[] param = query.split("&");
            String id = null;
            for (String row : param) {
                String[] param1 = row.split("=");
                if (param1[0].equals("v")) {
                    id = param1[1];
                }
            }
            return id;
        }
    }

    void startPlaying(String url) {
        uriYouTube = Uri.parse(url);
        videoView.setVideoURI(uriYouTube);
        System.out.println("starts playing");
        videoView.start();
    }
*/


}


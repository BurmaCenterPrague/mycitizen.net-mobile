/*
 * Copyright (C) 2013ff. mycitizen.net
 *
 * Licensed under the GPLv3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on any "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.widget.VideoView;


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
    String objectType, objectId;
    Menu menu;

    int contentType;
    int user_level;
    String url;
    String videoId;

    String subscriptionStatus;

    ActionBar actionBar;

    PopupWindow popupMessage;
    LinearLayout layoutOfPopup;
    RelativeLayout detail_root;
    Button insidePopupButton;
    TextView popupText;
    ListView popupList;

    Button friends_v, connections_v, map_v, message_v;

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
        detail_root = (RelativeLayout) findViewById(R.id.detail_root);
        detail_root.setVisibility(View.INVISIBLE);
        realName = (TextView) findViewById(R.id.real_name);
        //title = (TextView) findViewById(R.id.detail_title);
        information = (TextView) findViewById(R.id.information);
        online_status = (TextView) findViewById(R.id.online_status);

        friends_v = (Button) findViewById(R.id.button_friends);

        if (objectType.equals("group") || objectType.equals("resource")) {
            friends_v.setVisibility(View.GONE);
        }
        connections_v = (Button) findViewById(R.id.button_connections);
        map_v = (Button) findViewById(R.id.button_map);
        message_v = (Button) findViewById(R.id.button_messaging);

        friends_v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMessage.dismiss();
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent;
                    intent = new Intent(DetailActivity.this, DetailFriendsActivity.class);
                    intent.putExtra("objectType", objectType);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
            }
        });

        connections_v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMessage.dismiss();
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent;
                    intent = new Intent(DetailActivity.this, DetailConnectionsActivity.class);
                    intent.putExtra("objectType", objectType);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
            }
        });

        map_v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMessage.dismiss();
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_available_offline), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent;
                    intent = new Intent(DetailActivity.this, MapActivity.class);
                    intent.putExtra("type", "show");
                    intent.putExtra("objectType", objectType);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
            }
        });

        message_v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMessage.dismiss();
                Intent intent;
                intent = new Intent(DetailActivity.this, MessagesActivity.class);
                intent.putExtra("type", "response_inbox");
                intent.putExtra("objectType", objectType);
                intent.putExtra("objectId", objectId);
                intent.putExtra("recipient", title);
                startActivity(intent);
            }
        });

        // tag_list = (ListView) findViewById(R.id.tag_list);
        tag_list = new ListView(this);

        avatar = (ImageView) findViewById(R.id.avatar);

        popupInit();


        DashboardInit task = new DashboardInit();
        task.execute(objectType, objectId);

        Button menu_filter = (Button) findViewById(R.id.widget_menu_filter);
        SharedPreferences icon_settings = DetailActivity.this.getSharedPreferences(Config.localStorageName, 0);
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
                Log.d(Config.DEBUG_TAG, "objectType: " + objectType);
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
                    Log.d(Config.DEBUG_TAG, "content_type: " + contentType);
                    Log.d(Config.DEBUG_TAG, "url: " + url);

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

                            break;

                        case 51: // media_youtube

                            Intent intent = new Intent(DetailActivity.this, DetailYoutubeVideo.class);
                            intent.putExtra("videoId", videoId);
                            intent.putExtra("title", title);

                            startActivity(intent);

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

                startActivity(intent);
            }
        });

        messages_button = (Button) findViewById(R.id.widget_menu_messages);

        CheckUnreadMessages messages = new CheckUnreadMessages();
        messages.execute();

        messages_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, MessagesActivity.class);
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
            if (logged_user_id > 0 && Integer.valueOf(objectId) == logged_user_id) {
                Log.d(Config.DEBUG_TAG, "VISIBILITY GONE");
                button_subscribe.setVisibility(View.GONE);
                button_unsubscribe.setVisibility(View.GONE);
            }
        } else if (objectType.equals("group")) {
            button_subscribe.setText(getString(R.string.join_group));
            button_unsubscribe.setText(getString(R.string.leave_group));

            SharedPreferences settings = DetailActivity.this.getSharedPreferences(Config.localStorageName, 0);

            int logged_user_id = settings.getInt("logged_user_id", 0);
            if (logged_user_id > 0 && Integer.valueOf(objectId) == logged_user_id) {
                Log.d(Config.DEBUG_TAG, "VISIBILITY GONE");
                button_subscribe.setVisibility(View.GONE);
                button_unsubscribe.setVisibility(View.GONE);
            }
        } else {
            button_subscribe.setText(getString(R.string.subscribe));
            button_unsubscribe.setText(getString(R.string.unsubscribe));

            SharedPreferences settings = DetailActivity.this.getSharedPreferences(Config.localStorageName, 0);

            int logged_user_id = settings.getInt("logged_user_id", 0);
            if (logged_user_id > 0 && Integer.valueOf(objectId) == logged_user_id) {
                Log.d(Config.DEBUG_TAG, "VISIBILITY GONE");
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
                    task.execute(objectType, objectId, objectAction);
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
                    builder.setMessage(getString(R.string.really_disconnect))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                    dialog.cancel();
                                    loader = loadingDialog();
                                    String objectAction = "0";

                                    UnSubscribeInit task = new UnSubscribeInit();
                                    task.execute(objectType, objectId, objectAction);

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
                detail_root.setVisibility(View.VISIBLE);
                if (objectType.equals("user")) {
                    try {
                        title = ((UserObject) result).getName();
                        actionBar.setTitle(((UserObject) result).getName());
                        String realName_s = ((UserObject) result).getRealName();
                        if (realName_s != null && !realName_s.equals("")) {
                            realName.setText(realName_s);
                        } else {
                            realName.setText(title);
                        }

                        realName.setGravity(Gravity.CENTER_HORIZONTAL);

                        if (((UserObject) result).getStatus().equals("0")) {
                            realName.setTextColor(getResources().getColor(R.color.opaque_red));
                        }

                        online_status.setText(Html.fromHtml(((UserObject) result).getOnlineStatusOutput()));
                    } catch (NullPointerException e) {
                        title = getString(R.string.undefined_name);
                        actionBar.setTitle(getString(R.string.undefined_name));
                    }

                    if (((UserObject) result).getIconBitmap() != null) {
                        avatar.setImageBitmap(((UserObject) result).getIconBitmap());

                    } else {
                        Log.d(Config.DEBUG_TAG, "WTF");
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
                            map_v.setVisibility(View.INVISIBLE);
                        }


                        SharedPreferences settings = getApplicationContext().getSharedPreferences(Config.localStorageName, 0);

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

                        ImageView connection_v = (ImageView) findViewById(R.id.connection_status);
                        if (((UserObject) result).getRelationshipMeUser() != 2) {
                            connection_v.setAlpha(0.2f);
                            message_v.setVisibility(View.INVISIBLE);
                        }

                        ImageView role_v = (ImageView) findViewById(R.id.user_role);
                        int user_role = ((UserObject) result).getAccess();
                        if (user_role == 3) {
                            role_v.setImageResource(R.drawable.ico_administrator);
                        } else if (user_role == 2) {
                            role_v.setImageResource(R.drawable.ico_moderator);
                        } else {
                            role_v.setImageResource(R.drawable.ic_action_person);
                        }
                        user_level = user_role;


                            if (menu != null) {
                                MenuItem adminMenu = menu.findItem(R.menu.admin);
                                if (adminMenu != null) {
                                    if (settings.getInt("user_access_level", 0) <= user_role) {
                                        menu.getItem(0).setVisible(false);
                                    }
                                }
                            }


                        ImageView source_v = (ImageView) findViewById(R.id.data_source);
                        if (((UserObject) result).getSource().equals(ApiConnector.NETWORK)) {
                            source_v.setImageResource(R.drawable.ic_action_cloud);
                        } else {
                            source_v.setImageResource(R.drawable.ic_action_storage);
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
                        title = getString(R.string.undefined_name);
                        actionBar.setTitle(R.string.undefined_name);
                    }
                    online_status.setVisibility(View.GONE);

                    if (((GroupObject) result).getStatus().equals("0")) {
                        realName.setTextColor(getResources().getColor(R.color.opaque_red));
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
                        Log.d(Config.DEBUG_TAG, "Relationship me -> group: " + ((GroupObject) result).getRelationshipMeGroup());
                        if (((GroupObject) result).getRelationshipMeGroup() == 0) {
                            Log.d(Config.DEBUG_TAG, "Setting to gone");
                            button_unsubscribe.setVisibility(View.GONE);
                            button_subscribe.setVisibility(View.VISIBLE);
                        }
                        if (((GroupObject) result).getRelationshipMeGroup() == 1) {
                            connection_rights = true;
                            button_subscribe.setVisibility(View.GONE);
                            button_unsubscribe.setVisibility(View.VISIBLE);
                        }

                    } catch (NullPointerException e) {

                    }
                    if (((GroupObject) result).getPosition() == null) {
                        map_visible = false;
                        map_v.setVisibility(View.INVISIBLE);
                    }
                    try {
                        subscriptionStatus = String.valueOf(((GroupObject) result).getRelationshipMeGroup());
                    } catch (NullPointerException e) {
                    }

                    ImageView connection_v = (ImageView) findViewById(R.id.connection_status);
                    if (((GroupObject) result).getRelationshipMeGroup() == 0){
                        connection_v.setAlpha(0.2f);
                        message_v.setVisibility(View.INVISIBLE);
                    }

                    SharedPreferences settings = getApplicationContext().getSharedPreferences(Config.localStorageName, 0);
                    if (menu != null) {
                        MenuItem adminMenu = menu.findItem(R.menu.admin);
                        if (adminMenu != null) {
                            if (settings.getInt("user_access_level", 0) <= 1) {
                                menu.getItem(0).setVisible(false);
                            }
                        }
                    }

                    ImageView source_v = (ImageView) findViewById(R.id.data_source);
                    if (((GroupObject) result).getSource().equals(ApiConnector.NETWORK)) {
                        source_v.setImageResource(R.drawable.ic_action_cloud);
                    } else {
                        source_v.setImageResource(R.drawable.ic_action_storage);
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
                        title = getString(R.string.undefined_name);
                        actionBar.setTitle(R.string.undefined_name);
                    }
                    if (((ResourceObject) result).getStatus().equals("0")) {
                        realName.setTextColor(getResources().getColor(R.color.opaque_red));
                    }

                    online_status.setVisibility(View.GONE);

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
                        Log.d(Config.DEBUG_TAG, "Relationship me -> resource: " + ((ResourceObject) result).getRelationshipMeResource());
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
                        map_v.setVisibility(View.INVISIBLE);
                    }
                    try {
                        subscriptionStatus = String.valueOf(((ResourceObject) result).getRelationshipMeResource());
                    } catch (NullPointerException e) {

                    }

                    ImageView connection_v = (ImageView) findViewById(R.id.connection_status);
                    if (((ResourceObject) result).getRelationshipMeResource() == 0) {
                        connection_v.setAlpha(0.2f);
                        message_v.setVisibility(View.INVISIBLE);
                    }

                    SharedPreferences settings = getApplicationContext().getSharedPreferences(Config.localStorageName, 0);
                    if (menu != null) {
                        MenuItem adminMenu = menu.findItem(R.menu.admin);
                        if (adminMenu != null) {
                            if (settings.getInt("user_access_level", 0) <= 1) {
                                menu.getItem(0).setVisible(false);
                            }
                        }
                    }

                    ImageView source_v = (ImageView) findViewById(R.id.data_source);
                    if (((ResourceObject) result).getSource().equals(ApiConnector.NETWORK)) {
                        source_v.setImageResource(R.drawable.ic_action_cloud);
                    } else {
                        source_v.setImageResource(R.drawable.ic_action_storage);
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


            if (!connection_rights) {
                if (friends_v != null) {
                    friends_v.setVisibility(View.GONE);
                }
                if (connections_v != null) {
                    connections_v.setVisibility(View.GONE);
                }
                if (message_v != null) {
                    message_v.setVisibility(View.GONE);
                }

            }
            if (!map_visible) {
                map_v.setVisibility(View.GONE);
            }

            loader.dismiss();
            finish();
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        SharedPreferences settings = getApplicationContext().getSharedPreferences(Config.localStorageName, 0);

        if ( ((objectType.equals("group") || objectType.equals("resource")) && settings.getInt("user_access_level", 0) > 1)
                || ((objectType.equals("user") && settings.getInt("user_access_level", 0) > user_level) ) ) {
            inflater.inflate(R.menu.admin, menu);
        }


        inflater.inflate(R.menu.detail_tags, menu);
        inflater.inflate(R.menu.help, menu);


        this.menu = menu;
        return super.onCreateOptionsMenu(menu);

    }


    public boolean onOptionsItemSelected(MenuItem item) {

        // TODO add handling of activate, deactivate

        Intent intent;
        switch (item.getItemId()) {
/*
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
*/

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



}


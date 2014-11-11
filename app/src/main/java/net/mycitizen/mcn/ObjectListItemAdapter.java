package net.mycitizen.mcn;

import java.security.acl.Group;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ObjectListItemAdapter extends ArrayAdapter<DataObject> {
    private final Context context;
    private final ArrayList<DataObject> values;
    private final String listType;

    private OnAdapterActionListener onAdapterActionListener = null;
    Context ctx;

    public ObjectListItemAdapter(Context context, String listType, ArrayList<DataObject> values) {
        super(context, R.layout.widget_list_item, values);
        this.ctx = context;
        this.context = context;
        this.values = values;
        this.listType = listType;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        SharedPreferences settings = ctx.getSharedPreferences(Config.localStorageName, 0);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        if (listType.equals("widget")) {
            rowView = inflater.inflate(R.layout.widget_list_item, parent, false);
        } else if (listType.equals("tag")) {
            rowView = inflater.inflate(R.layout.tag_list_item, parent, false);
        } else if (listType.equals("message")) {
            rowView = inflater.inflate(R.layout.message_list_other_item, parent, false);
            int logged_user_id = settings.getInt("logged_user_id", 0);
            int author_id = ((ResourceObject) values.get(position)).getResponseUser();

            if (logged_user_id == author_id) {
                rowView = inflater.inflate(R.layout.message_list_owner_item, parent, false);
            }
        } else if (listType.equals("inbox_message")) {
            rowView = inflater.inflate(R.layout.message_list_other_item, parent, false);
            int logged_user_id = settings.getInt("logged_user_id", 0);
            int author_id = ((ResourceObject) values.get(position)).getResponseUser();

            if (logged_user_id == author_id) {
                rowView = inflater.inflate(R.layout.message_list_owner_item, parent, false);
            }
        } else if (listType.equals("inbox_message_chat")) {
            rowView = inflater.inflate(R.layout.message_list_other_item, parent, false);
            int logged_user_id = settings.getInt("logged_user_id", 0);
            int author_id = ((ResourceObject) values.get(position)).getResponseUser();

            if (logged_user_id == author_id) {
                rowView = inflater.inflate(R.layout.message_list_owner_item, parent, false);
            }
        } else if (listType.equals("profile_tag")) {
            rowView = inflater.inflate(R.layout.profile_tag_list_item, parent, false);

        } else if (listType.equals("filter_tag")) {
            rowView = inflater.inflate(R.layout.profile_tag_list_item, parent, false);
        }

        if (rowView != null) {
            ImageView icon = (ImageView) rowView.findViewById(R.id.object_icon);
            TextView objectTitle = (TextView) rowView.findViewById(R.id.object_title);
            // TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
            LinearLayout row = (LinearLayout) rowView.findViewById(R.id.widget_row);

            if (values.get(position).getObjectType().equals("user")) {

                if (((UserObject) values.get(position)).getId() == 0) {
                    // load more

                    rowView.findViewById(R.id.widget_item_bottom_row).setVisibility(View.GONE);
                    LinearLayout details = (LinearLayout) rowView.findViewById(R.id.widget_item_bottom_row);
                    if (details != null) {
                        details.setVisibility(View.GONE);
                    }

                    if (icon != null) {
                        icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_expand));
                    }
                } else if (((UserObject) values.get(position)).getId() == -1) {
                    // info message
                    if (icon != null) {
                        icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_warning));
                    }
                    LinearLayout details = (LinearLayout) rowView.findViewById(R.id.widget_item_bottom_row);
                    if (details != null) {
                        details.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (((UserObject) values.get(position)).getIconBitmap() != null && icon != null) {
                        icon.setImageBitmap(((UserObject) values.get(position)).getIconBitmap());
                    } else {
                        ApiConnector api = new ApiConnector(ctx);
                        if (icon != null) {
                            icon.setImageBitmap(api.defaultUserIcon());
                        }
                    }
                }

                String onlineStatus = "";

                if (((UserObject) values.get(position)).getNow_online().equals("1")) {
                    onlineStatus = "  <font color=\"#37AB44\"><big>●</big></font>";
                }

                objectTitle.setText(Html.fromHtml(((UserObject) values.get(position)).getName()));
                if (((UserObject) values.get(position)).getStatus().equals("0") && ((UserObject) values.get(position)).getId() > 0) {
                    objectTitle.setTextColor(context.getResources().getColor(R.color.opaque_red));
                }

                ImageView objectConnectionStatus = (ImageView) rowView.findViewById(R.id.connection_messaging);
                TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
                objectOnlineStatus.setText(Html.fromHtml(onlineStatus));

                // String connectionInfo = "";

                ImageView connection_v = (ImageView) rowView.findViewById(R.id.connection_status);
                if (((UserObject) values.get(position)).getRelationshipMeUser() == 2) {

                    //row.setBackgroundColor(Color.parseColor("#55E3DED3"));
                    objectConnectionStatus.setImageResource(R.drawable.ic_action_chat);


                    // connection_v.setImageResource(R.drawable.ic_action_favorite);

                    objectConnectionStatus.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, MessagesActivity.class);
                            intent.putExtra("type", "response_inbox");
                            intent.putExtra("objectType", "user");
                            intent.putExtra("objectId", Integer.toString(((UserObject) values.get(position)).getId()));
                            intent.putExtra("recipient", ((UserObject) values.get(position)).getName());
                            ctx.startActivity(intent);
                        }
                    });
                } else {
                    connection_v.setAlpha(0.2f);
                }

                Log.d(Config.DEBUG_TAG, "user role: " + ((UserObject) values.get(position)).getAccess());
                ImageView role_v = (ImageView) rowView.findViewById(R.id.user_role);
                if (((UserObject) values.get(position)).getAccess() == 3) {
                    role_v.setImageResource(R.drawable.ico_administrator);
                } else if (((UserObject) values.get(position)).getAccess() == 2) {
                    role_v.setImageResource(R.drawable.ico_moderator);
                } else {
                    role_v.setImageResource(R.drawable.ic_action_person);
                }

                Log.d(Config.DEBUG_TAG, "map_status: " + ((UserObject) values.get(position)).getPosition());
                ImageView map_v = (ImageView) rowView.findViewById(R.id.map_status);
                if (((UserObject) values.get(position)).getPosition() == null) {
                    map_v.setAlpha(0.2f);
                }

                ImageView source_v = (ImageView) rowView.findViewById(R.id.data_source);
                if (((UserObject) values.get(position)).getSource().equals(ApiConnector.NETWORK)) {
                    source_v.setImageResource(R.drawable.ic_action_cloud);
                } else {
                    source_v.setImageResource(R.drawable.ic_action_storage);
                }

                // Log.d(Config.DEBUG_TAG, "online_status is " + ((UserObject) values.get(position)).getNow_online());
                // objectOnlineStatus.setText("online");
            } else if (values.get(position).getObjectType().equals("group")) {

                rowView.findViewById(R.id.user_role).setVisibility(View.GONE);

                TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
                if (objectOnlineStatus != null) {
                    objectOnlineStatus.setVisibility(View.GONE);
                }

                if (((GroupObject) values.get(position)).getId() == 0) {
                    // load more

                    rowView.findViewById(R.id.widget_item_bottom_row).setVisibility(View.GONE);
                    LinearLayout details = (LinearLayout) rowView.findViewById(R.id.widget_item_bottom_row);
                    if (details != null) {
                        details.setVisibility(View.INVISIBLE);
                    }

                    if (icon != null) {
                        icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_expand));
                    }
                } else if (((GroupObject) values.get(position)).getId() == -1) {
                    // info message
                    if (icon != null) {
                        icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_warning));
                    }
                    LinearLayout details = (LinearLayout) rowView.findViewById(R.id.widget_item_bottom_row);
                    if (details != null) {
                        details.setVisibility(View.INVISIBLE);
                    }
                } else {

                    if (((GroupObject) values.get(position)).getIconBitmap() != null && icon != null) {
                        icon.setImageBitmap(((GroupObject) values.get(position)).getIconBitmap());
                    } else {
                        ApiConnector api = new ApiConnector(ctx);
                        if (icon != null) {
                            icon.setImageBitmap(api.defaultUserIcon());
                        }
                    }
                }
                objectTitle.setText(Html.fromHtml(((GroupObject) values.get(position)).getTitle()));
                if (((GroupObject) values.get(position)).getStatus().equals("0") && ((GroupObject) values.get(position)).getId() > 0) {
                    objectTitle.setTextColor(context.getResources().getColor(R.color.opaque_red));
                }

                ImageView objectConnectionStatus = (ImageView) rowView.findViewById(R.id.connection_messaging);
                //String connectionInfo = "";

                ImageView connection_v = (ImageView) rowView.findViewById(R.id.connection_status);

                if (((GroupObject) values.get(position)).getRelationshipMeGroup() == 1) {
                    //row.setBackgroundColor(Color.parseColor("#55E3DED3"));
                    objectConnectionStatus.setImageResource(R.drawable.ic_action_chat);


                    //connection_v.setImageResource(R.drawable.ic_action_favorite);

                    objectConnectionStatus.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, MessagesActivity.class);
                            intent.putExtra("type", "response_inbox");
                            intent.putExtra("objectType", "group");
                            intent.putExtra("objectId", Integer.toString(((GroupObject) values.get(position)).getId()));
                            intent.putExtra("recipient", ((GroupObject) values.get(position)).getTitle());
                            ctx.startActivity(intent);
                        }
                    });
                } else {
                    connection_v.setAlpha(0.2f);
                }

                ImageView map_v = (ImageView) rowView.findViewById(R.id.map_status);
                if (((GroupObject) values.get(position)).getPosition() != null) {
                    //map_v.setImageResource(R.drawable.ic_action_map);
                } else {
                    map_v.setAlpha(0.2f);
                }

                ImageView source_v = (ImageView) rowView.findViewById(R.id.data_source);
                if (((GroupObject) values.get(position)).getSource().equals(ApiConnector.NETWORK)) {
                    source_v.setImageResource(R.drawable.ic_action_cloud);
                } else {
                    source_v.setImageResource(R.drawable.ic_action_storage);
                }

            } else if (values.get(position).getObjectType().equals("resource")) {

                ImageView userRole = (ImageView) rowView.findViewById(R.id.user_role);
                if (userRole != null) {
                    userRole.setVisibility(View.GONE);
                }

                TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
                if (objectOnlineStatus != null) {
                    objectOnlineStatus.setVisibility(View.GONE);
                }

                if (listType.equals("inbox_message") || listType.equals("inbox_message_chat")) {

                    if (((ResourceObject) values.get(position)).getId() == 0) {
                        // "load more"
                        RelativeLayout message_wrapper = (RelativeLayout) rowView.findViewById(R.id.message_wrapper);
                        message_wrapper.setVisibility(View.GONE);

                        TextView load_more = (TextView) rowView.findViewById(R.id.message_load_more);
                        load_more.setVisibility(View.VISIBLE);

                    } else {
                        int subtype = ((ResourceObject) values.get(position)).getSubType();
                        if (subtype == 1 || subtype == 8) {
                            // PM, group chat or comment
                            String html_body = ((ResourceObject) values.get(position)).getSecondaryTitle();
                            URLImageParser p = new URLImageParser(objectTitle, ctx);
                            Spanned htmlSpan = Html.fromHtml(html_body, p, null);
                            objectTitle.setText(htmlSpan);
                            objectTitle.setMovementMethod(LinkMovementMethod.getInstance());
                            Button re = (Button) rowView.findViewById(R.id.button_a);
                            ImageView avatar = (ImageView) rowView.findViewById(R.id.avatar);


                            int logged_user_id = settings.getInt("logged_user_id", 0);
                            int author_id = ((ResourceObject) values.get(position)).getResponseUser();

                            if (((ResourceObject) values.get(position)).getIconBitmap() != null) {
                                avatar.setImageBitmap(((ResourceObject) values.get(position)).getIconBitmap());
                            }


                            if (logged_user_id == author_id) {
                                re.setVisibility(View.GONE);
                            } else {
                                re.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        ApiConnector api = new ApiConnector(ctx);
                                        if (api.isNetworkAvailable()) {


                                            Intent intent = new Intent(ctx, MessagesActivity.class);
                                            intent.putExtra("type", "response_inbox");
                                            intent.putExtra("objectType", "user");
                                            int author_id = ((ResourceObject) values.get(position)).getResponseUser();
                                            intent.putExtra("objectId", String.valueOf(author_id));
                                            ctx.startActivity(intent);
                                        } else {
                                            Toast.makeText(ctx, ctx.getString(R.string.not_available_offline),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            Button trash = (Button) rowView.findViewById(R.id.button_b);
                            if (listType.equals("inbox_message_chat")) {
                                trash.setVisibility(View.GONE);
                            }
                            if (((ResourceObject) values.get(position)).isDeleted()) {
                                trash.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        if (onAdapterActionListener != null) {
                                            int id = values.get(position).getObjectId();
                                            onAdapterActionListener.onUntrashMessageAction(id);
                                        }
                                    }
                                });
                                trash.setBackgroundResource(R.drawable.ico_trash_un);

                            } else {
                                trash.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        if (onAdapterActionListener != null) {
                                            int id = values.get(position).getObjectId();
                                            onAdapterActionListener.onTrashMessageAction(id);
                                        }
                                    }
                                });
                                trash.setBackgroundResource(R.drawable.ico_trash);
                            }

                            ImageView author_avatar = (ImageView) rowView.findViewById(R.id.avatar);
                            author_avatar.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    // int message_id = ((ResourceObject) values.get(position)).getObjectId();
                                    SharedPreferences settings = ctx.getSharedPreferences(Config.localStorageName, 0);
                                    int logged_user_id = settings.getInt("logged_user_id", 0);
                                    int author_id = ((ResourceObject) values.get(position)).getResponseUser();

                                    //rowView = inflater.inflate(R.layout.message_list_owner_item, parent, false);
                                    Log.d(Config.DEBUG_TAG, "Clicked on avatar of user_id: " + author_id);

                                    Intent intent = new Intent(ctx, DetailActivity.class);
                                    intent.putExtra("ObjectType", "user");
                                    intent.putExtra("ObjectId", String.valueOf(author_id));
                                    ctx.startActivity(intent);


                                }
                            });
                        } else if (subtype == 9) {
                            // system message
                            String html_body = ((ResourceObject) values.get(position)).getSecondaryTitle();
                            URLImageParser p = new URLImageParser(objectTitle, ctx);
                            Spanned htmlSpan = Html.fromHtml(html_body, p, null);
                            objectTitle.setText(htmlSpan);
                            objectTitle.setMovementMethod(LinkMovementMethod.getInstance());
                            Button re = (Button) rowView.findViewById(R.id.button_a);
                            ImageView avatar = (ImageView) rowView.findViewById(R.id.avatar);


                            int logged_user_id = settings.getInt("logged_user_id", 0);
                            int author_id = ((ResourceObject) values.get(position)).getResponseUser();

                            if (((ResourceObject) values.get(position)).getIconBitmap() != null) {
                                avatar.setImageBitmap(((ResourceObject) values.get(position)).getIconBitmap());
                            } else {
                                avatar.setImageResource(R.drawable.ic_action_warning);
                            }

                            re.setVisibility(View.GONE);

                            Button trash = (Button) rowView.findViewById(R.id.button_b);

                            trash.setVisibility(View.GONE);

                        } else if (subtype == 10) {
                            // friendship requests
                            String html_body = ((ResourceObject) values.get(position)).getSecondaryTitle();
                            URLImageParser p = new URLImageParser(objectTitle, ctx);
                            Spanned htmlSpan = Html.fromHtml(html_body, p, null);
                            objectTitle.setText(htmlSpan);
                            objectTitle.setMovementMethod(LinkMovementMethod.getInstance());

                            Button accept = (Button) rowView.findViewById(R.id.button_a);
                            accept.setBackgroundResource(R.drawable.ic_action_accept);// setText("ACC");
                            if (((ResourceObject) values.get(position)).isDeleted()) {
                                accept.setVisibility(View.GONE);
                            } else {
                                accept.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        if (onAdapterActionListener != null) {
                                            int id = values.get(position).getObjectId();
                                            int sender_id = ((ResourceObject) values.get(position)).getResponseUser();
                                            onAdapterActionListener.onAcceptMessageAction(id, sender_id);
                                        }
                                    }
                                });
                            }
                            Button decline = (Button) rowView.findViewById(R.id.button_b);
                            decline.setBackgroundResource(R.drawable.ic_action_cancel); //setText("DEC");
                            if (((ResourceObject) values.get(position)).isDeleted()) {
                                decline.setVisibility(View.GONE);
                            } else {
                                decline.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        if (onAdapterActionListener != null) {
                                            int id = values.get(position).getObjectId();
                                            int sender_id = ((ResourceObject) values.get(position)).getResponseUser();
                                            onAdapterActionListener.onDeclineMessageAction(id, sender_id);
                                        }
                                    }
                                });
                            }

                            ImageView avatar = (ImageView) rowView.findViewById(R.id.avatar);
                            if (((ResourceObject) values.get(position)).getIconBitmap() != null) {
                                avatar.setImageBitmap(((ResourceObject) values.get(position)).getIconBitmap());
                            } else {
                                avatar.setImageResource(R.drawable.ic_action_warning);
                            }
                        } else if (subtype == 0) {
                            // disappointing message on empty lists
                            Button accept = (Button) rowView.findViewById(R.id.button_a);
                            accept.setVisibility(View.GONE);
                            Button decline = (Button) rowView.findViewById(R.id.button_b);
                            decline.setVisibility(View.GONE);
                            Button re = (Button) rowView.findViewById(R.id.button_a);
                            re.setVisibility(View.GONE);
                            Button trash = (Button) rowView.findViewById(R.id.button_b);
                            trash.setVisibility(View.GONE);
                            ImageView avatar = (ImageView) rowView.findViewById(R.id.avatar);
                            avatar.setImageResource(R.drawable.ic_action_warning);

                            String html_body = ((ResourceObject) values.get(position)).getSecondaryTitle();
                            URLImageParser p = new URLImageParser(objectTitle, ctx);
                            Spanned htmlSpan = Html.fromHtml(html_body, p, null);
                            objectTitle.setText(htmlSpan);
                            objectTitle.setGravity(Gravity.CENTER);

                        }
                    }
                } else {

                    objectTitle.setText(Html.fromHtml(((ResourceObject) values.get(position)).getTitle()));
                    if (((ResourceObject) values.get(position)).getStatus().equals("0") && ((ResourceObject) values.get(position)).getId() > 0) {
                        objectTitle.setTextColor(context.getResources().getColor(R.color.opaque_red));
                    }
                    if (icon != null) {
                        if (((ResourceObject) values.get(position)).getId() == 0) {
                            // load more

                            rowView.findViewById(R.id.widget_item_bottom_row).setVisibility(View.GONE);
                            LinearLayout details = (LinearLayout) rowView.findViewById(R.id.widget_item_bottom_row);
                            if (details != null) {
                                details.setVisibility(View.INVISIBLE);
                            }

                            icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_expand));
                        } else if (((ResourceObject) values.get(position)).getId() == -1) {
                            // info message
                            icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_warning));
                            LinearLayout details = (LinearLayout) rowView.findViewById(R.id.widget_item_bottom_row);
                            if (details != null) {
                                details.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            switch (((ResourceObject) values.get(position)).getSubType()) {
                                case 2:
                                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ico_calendar));
                                    break;
                                case 3:
                                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ico_organization));
                                    break;
                                case 4:
                                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ico_note));
                                    break;
                                case 5:
                                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ico_video));
                                    break;
                                case 6:
                                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ico_note));
                                    break;
                            }
                        }
                    }

                    ImageView objectConnectionStatus = (ImageView) rowView.findViewById(R.id.connection_messaging);
                    // String connectionInfo = "";

                    ImageView connection_v = (ImageView) rowView.findViewById(R.id.connection_status);
                    if (((ResourceObject) values.get(position)).getRelationshipMeResource() == 1) {

                        //row.setBackgroundColor(Color.parseColor("#55E3DED3"));
                        objectConnectionStatus.setImageResource(R.drawable.ic_action_chat);


                        // connection_v.setImageResource(R.drawable.ic_action_favorite);

                        objectConnectionStatus.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ctx, MessagesActivity.class);
                                intent.putExtra("type", "response_inbox");
                                intent.putExtra("objectType", "resource");
                                intent.putExtra("objectId", Integer.toString(((ResourceObject) values.get(position)).getId()));
                                intent.putExtra("recipient", ((ResourceObject) values.get(position)).getTitle());
                                ctx.startActivity(intent);
                            }
                        });
                    } else {
                        connection_v.setAlpha(0.2f);
                    }

                    ImageView map_v = (ImageView) rowView.findViewById(R.id.map_status);
                    if (((ResourceObject) values.get(position)).getPosition() != null) {
                        //map_v.setImageResource(R.drawable.ic_action_map);
                    } else {
                        map_v.setAlpha(0.2f);
                    }

                    ImageView source_v = (ImageView) rowView.findViewById(R.id.data_source);
                    if (((ResourceObject) values.get(position)).getSource().equals(ApiConnector.NETWORK)) {
                        source_v.setImageResource(R.drawable.ic_action_cloud);
                    } else {
                        source_v.setImageResource(R.drawable.ic_action_storage);
                    }

                }
            } else if (values.get(position).getObjectType().equals("tag")) {
                if (listType.equals("profile_tag")) {
                    CheckBox objectStatus = (CheckBox) rowView.findViewById(R.id.object_status);
                    if (((TagObject) values.get(position)).getStatus()) {
                        objectStatus.setChecked(true);
                    }
                    objectStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            if (onAdapterActionListener != null) {
                                //int id = ((ResourceObject)values.get(position)).getObjectId();
                                //int sender_id = ((ResourceObject)values.get(position)).getResponseUser();
                                onAdapterActionListener.onChangeTag(values.get(position).getObjectId(), isChecked);
                            }
                        }
                    });
                }
                if (listType.equals("filter_tag")) {
                    CheckBox objectStatus = (CheckBox) rowView.findViewById(R.id.object_status);
                    if (((TagObject) values.get(position)).getStatus()) {
                        objectStatus.setChecked(true);
                    }
                    objectStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            //ApiConnector api = new ApiConnector(ctx);
                            //api.changeProfileTag(((TagObject)values.get(position)).getObjectId(), isChecked);
                        }
                    });
                }
                objectTitle.setText(((TagObject) values.get(position)).getTitle());
            }
        }


        return rowView;
    }

    public void setOnAdapterActionListener(OnAdapterActionListener actionListener) {
        this.onAdapterActionListener = actionListener;
    }


}

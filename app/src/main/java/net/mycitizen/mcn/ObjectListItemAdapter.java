package net.mycitizen.mcn;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        //tady sachovat s obsahem layoutu

		/*
        LinearLayout item = (LinearLayout) rowView.findViewById(R.id.item);
		if(position == 0) {
			item.setBackgroundResource(R.drawable.widget_list_item_top_style);
		} else if(position == (values.size() - 1)) {
			item.setBackgroundResource(R.drawable.widget_list_item_bottom_style);
		} else {
			item.setBackgroundResource(R.drawable.widget_list_item_style);
		}
		*/


        ImageView icon = (ImageView) rowView.findViewById(R.id.object_icon);
        TextView objectTitle = (TextView) rowView.findViewById(R.id.object_title);
        // TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
        LinearLayout row = (LinearLayout) rowView.findViewById(R.id.widget_row);

        if (values.get(position).getObjectType().equals("user")) {

            if (((UserObject) values.get(position)).getId() == 0) {
                // load more
                if (icon != null) {
                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_expand));
                }
            } else if (((UserObject) values.get(position)).getId() == -1) {
                // info message
                if (icon != null) {
                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_warning));
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

            TextView objectConnectionStatus = (TextView) rowView.findViewById(R.id.connection_status);
            TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
            objectOnlineStatus.setText(Html.fromHtml(onlineStatus));

            String connectionInfo = "";

            if (((UserObject) values.get(position)).getConnectionStatus().equals("1")) {
                connectionInfo += "  <big>✓︎</big>";
                objectConnectionStatus.setText(Html.fromHtml(connectionInfo));

                row.setBackgroundColor(Color.parseColor("#A0FFFFFF"));
            }

            // System.out.println("online_status is " + ((UserObject) values.get(position)).getNow_online());
            // objectOnlineStatus.setText("online");
        } else if (values.get(position).getObjectType().equals("group")) {

            TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
            if (objectOnlineStatus != null) {
                objectOnlineStatus.setVisibility(View.GONE);
            }

            if (((GroupObject) values.get(position)).getId() == 0) {
                // load more
                if (icon != null) {
                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_expand));
                }
            } else if (((GroupObject) values.get(position)).getId() == -1) {
                // info message
                if (icon != null) {
                    icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_warning));
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

            TextView objectConnectionStatus = (TextView) rowView.findViewById(R.id.connection_status);
            String connectionInfo = "";

            if (((GroupObject) values.get(position)).getConnectionStatus().equals("1")) {
                connectionInfo += "  <big>✓</big>";
                objectConnectionStatus.setText(Html.fromHtml(connectionInfo));

                row.setBackgroundColor(Color.parseColor("#A0FFFFFF"));
            }

        } else if (values.get(position).getObjectType().equals("resource")) {

            TextView objectOnlineStatus = (TextView) rowView.findViewById(R.id.online_status);
            if (objectOnlineStatus != null) {
                objectOnlineStatus.setVisibility(View.GONE);
            }

            if (listType.equals("inbox_message") || listType.equals("inbox_message_chat")) {

                if (((ResourceObject) values.get(position)).getId() == 0) {
                    // "load more"
                    RelativeLayout message_wrapper = (RelativeLayout) rowView.findViewById(R.id.message_wrapper);
                    message_wrapper.setVisibility(View.GONE);

                    Button load_more = (Button) rowView.findViewById(R.id.message_load_more);
                    load_more.setVisibility(View.VISIBLE);

                } else {

                    int subtype = ((ResourceObject) values.get(position)).getSubType();
                    if (subtype == 1 || subtype == 8) {
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
                                        int id = ((ResourceObject) values.get(position)).getObjectId();
                                        onAdapterActionListener.onUntrashMessageAction(id);
                                    }
                                }
                            });
                            System.out.println("INVALIDATE THIS");
                            trash.setBackgroundResource(R.drawable.ico_trash_un);

                        } else {
                            trash.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (onAdapterActionListener != null) {
                                        int id = ((ResourceObject) values.get(position)).getObjectId();
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
                                System.out.println("Clicked on avatar of user_id: " + author_id);

                                Intent intent = new Intent(ctx, DetailActivity.class);
                                intent.putExtra("ObjectType", "user");
                                intent.putExtra("ObjectId", String.valueOf(author_id));
                                ctx.startActivity(intent);


                            }
                        });
                    } else if (subtype == 9) {
                        Button accept = (Button) rowView.findViewById(R.id.button_a);
                        accept.setText("ACC");
                        if (((ResourceObject) values.get(position)).isDeleted()) {
                            accept.setVisibility(View.GONE);
                        } else {
                            accept.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (onAdapterActionListener != null) {
                                        int id = ((ResourceObject) values.get(position)).getObjectId();
                                        int sender_id = ((ResourceObject) values.get(position)).getResponseUser();
                                        onAdapterActionListener.onAcceptMessageAction(id, sender_id);
                                    }
                                }
                            });
                        }
                        Button decline = (Button) rowView.findViewById(R.id.button_b);
                        decline.setText("DEC");
                        if (((ResourceObject) values.get(position)).isDeleted()) {
                            decline.setVisibility(View.GONE);
                        } else {
                            decline.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (onAdapterActionListener != null) {
                                        int id = ((ResourceObject) values.get(position)).getObjectId();
                                        int sender_id = ((ResourceObject) values.get(position)).getResponseUser();
                                        onAdapterActionListener.onDeclineMessageAction(id, sender_id);
                                    }
                                }
                            });
                        }
                    }
                }
            } else {

                objectTitle.setText(Html.fromHtml(((ResourceObject) values.get(position)).getTitle()));
                if (icon != null) {
                    if (((ResourceObject) values.get(position)).getId() == 0) {
                        // load more
                        icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_expand));
                    } else if (((ResourceObject) values.get(position)).getId() == -1) {
                        // info message
                        icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_warning));
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

                TextView objectConnectionStatus = (TextView) rowView.findViewById(R.id.connection_status);
                String connectionInfo = "";

                if (((ResourceObject) values.get(position)).getConnectionStatus().equals("1")) {
                    connectionInfo += "  <big>✓</big>";
                    objectConnectionStatus.setText(Html.fromHtml(connectionInfo));

                    row.setBackgroundColor(Color.parseColor("#A0FFFFFF"));
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
                            onAdapterActionListener.onChangeTag(((TagObject) values.get(position)).getObjectId(), isChecked);
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

        return rowView;
    }

    public void setOnAdapterActionListener(OnAdapterActionListener actionListener) {
        this.onAdapterActionListener = actionListener;
    }


}

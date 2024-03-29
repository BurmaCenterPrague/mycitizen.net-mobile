package net.mycitizen.mcn;

import java.util.LinkedHashMap;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class UserObject extends DataObject {
    private String objectType;
    private int objectId;

    private String name;
    private String firstName;
    private String lastName;

    private String now_online;

    private String status;

    private String description;

    private String gpsx;
    private String gpsy;

    private int visibility;
    private int access;
    private String language;

    private String email;

    private String phone;
    private String url;

    private String notification_timer;

    private String iconId;

    private int relationship_me_user;
    private int relationship_user_me;

    private int id;

    private String source = "d";

    public UserObject(int id, String firstName, String lastName, String login) {
        super(id, "user");
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = login;
        this.id = id;

    }

    public UserObject(int id) {
        super(id, "user");
        this.id = id;
        Log.d(Config.DEBUG_TAG, "id: " + id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNow_online() {
        if (now_online != null) {
            return now_online;
        } else {
            return "0";
        }
    }

    public void setNow_online(String now_online) {
        this.now_online = now_online;
    }

    public String getOnlineStatusOutput() {
        if (this.now_online != null) {
            if (this.now_online.equals("1")) {
                return "<font color=\"#37AB44\"><big>●</big> online</font>";
            }
        }
        return "";
    }

    /*
    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }


    public String getConnectionStatus() {
        if (this.connectionStatus != null) {
            return this.connectionStatus;
        } else {
            return "0";
        }
    }

    public String getConnectionStatusOutput() {
        if (this.connectionStatus != null) {
            if (this.connectionStatus.equals("1")) {
                return "friend";
            }
        }
        return "";
    }
    */

    public void setRealName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return this.description;
    }

    public String getRealName() {
        return this.firstName + " " + this.lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getUrl() {
        return this.url;
    }

    public String getNotificationTimer() {
        return this.notification_timer;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setNotificationTimer(String timer) {
        this.notification_timer = timer;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIconId(String icon) {

        this.iconId = icon;
    }

    public Bitmap getIconBitmap() {
        if (this.iconId != null) {
            return ApiConnector.decodeBase64(this.iconId);
        }

        return null;
    }

    public String getIconId() {
        return iconId;
    }

    public String getIconName() {

        return "";
    }

    public void setPosition(String gpsx, String gpsy) {
        this.gpsx = gpsx;
        this.gpsy = gpsy;
    }

    public GeoPoint getPosition() {
        if (gpsx == null || gpsy == null) {
            return null;
        }
        if (this.gpsx.equals("null") || this.gpsy.equals("null")) {
            return null;
        }
        try {
            return new GeoPoint(Double.valueOf(gpsx), Double.valueOf(gpsy));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getVisibility() {
        return this.visibility;
    }

    public void setVisibility(int vis) {
        this.visibility = vis;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String lng) {
        this.language = lng;
    }

    public int getAccess() {
        return this.access;
    }

    public void setAccess(int acs) {
        this.access = acs;
    }

    public String getDetail(Context ctx) {
        String html = "";

        html += "<div>";
        if (this.visibility == 1) {
            html += "<span><b>" + ctx.getString(R.string.visibility) + ": </b></span><span>" + ctx.getString(R.string.visibility_world) + "</span><br/>";
        } else if (this.visibility == 2) {
            html += "<span><b>" + ctx.getString(R.string.visibility) + ": </b></span><span>" + ctx.getString(R.string.visibility_members) + "</span><br/>";
        } else {
            html += "<span><b>" + ctx.getString(R.string.visibility) + ": </b></span><span>" + ctx.getString(R.string.visibility_private) + "</span><br/>";
        }

        ApiConnector api = new ApiConnector(ctx);
        String language_name = Config.translateLanguageCodeToName(ctx, this.language);
            html += "<span><b>" + ctx.getString(R.string.language) + ": </b></span><span>"+language_name+"</span><br/>";


        if (this.phone != null && !this.phone.equals("")) {
            html += "<span><b>" + ctx.getString(R.string.profile_phone) + ": </b></span><span>" + this.phone + "</span>";
        }
        /*
        if(this.url != null  && !this.url.equals("")) {
            html += "<span><b>"+ctx.getString(R.string.homepage)+": </b></span><span><a href=\""+this.url+"\">"+this.url+"</a></span>";
        }
        */

        html += "<div>" + this.description + "</div>";
        html += "</div>";
        return html;
    }

    public int getRelationshipMeUser() {
        return this.relationship_me_user;
    }

    public int getRelationshipUserMe() {
        return this.relationship_user_me;
    }

    public void setRelationshipMeUser(int relationship) {
        this.relationship_me_user = relationship;
    }

    public void setRelationshipUserMe(int relationship) {
        this.relationship_user_me = relationship;
    }

    @Override
    public LinkedHashMap<String, String> getAllData() {
        return null;
    }

    public int getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return this.name;
    }

    public String getStatus() {
        if (status != null && !status.equals("")) {
            return status;
        } else {
            return "1";
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

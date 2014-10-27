package net.mycitizen.mcn;

import java.util.LinkedHashMap;

import org.osmdroid.util.GeoPoint;

import android.graphics.Bitmap;

public class GroupObject extends DataObject {


    private String title;

    private String description;

    private String gpsx;
    private String gpsy;

    private int visibility;
    private int access;
    private String language;

    private String iconId;

    private String connectionStatus;

    private int id;

    private int relationship_me_group;

    public GroupObject(int id) {
        super(id, "group");
        this.id = id;
    }

    public GroupObject(int id, String title) {
        super(id, "group");
        this.title = title;
        this.id = id;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return this.description;
    }

    public void setPosition(String gpsx, String gpsy) {
        this.gpsx = gpsx;
        this.gpsy = gpsy;
    }

    public GeoPoint getPosition() {
        if (gpsx == null || gpsy == null) {
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

    public String getDetail() {
        String html = "<div>";
        if (this.visibility == 1) {
            html += "<span><b>Visibility: </b></span><span>world</span><br/>";
        } else if (this.visibility == 2) {
            html += "<span><b>Visibility: </b></span><span>registered</span><br/>";
        } else {
            html += "<span><b>Visibility: </b></span><span>friends/members</span><br/>";
        }
        if (this.language.equals("mya")) {
            html += "<span><b>Language: </b></span><span>Burmese</span><br/>";
        } else {
            html += "<span><b>Language: </b></span><span>English</span><br/>";
        }
        html += "<div>" + this.description + "</div>";
        html += "</div>";
        return html;
    }

    public int getRelationshipMeGroup() {
        return this.relationship_me_group;
    }

    public void setRelationshipMeGroup(int relationship) {
        this.relationship_me_group = relationship;
    }

    @Override
    public LinkedHashMap<String, String> getAllData() {
        return null;
    }

    public int getId() {
        return id;
    }
}

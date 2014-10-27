package net.mycitizen.mcn;

import java.util.LinkedHashMap;

import org.osmdroid.util.GeoPoint;

import android.content.SyncAdapterType;
import android.graphics.Bitmap;

public class ResourceObject extends DataObject {
    private String title;
    private String secondaryTitle;

    private int subType = 0;

    private String description;

    private String gpsx;
    private String gpsy;

    private int visibility;
    private int access;
    private String language;

    private String resourceType;

    private int statusFlag, viewedFlag, trashFlag;

    private int relationship_me_resource;

    private boolean trashed;
    private int response_user;
    private String response_user_name;
    private String ownerAvatar;

    private String connectionStatus;

    private int contentType;
    private String url;

    private int id;

    public ResourceObject(int id) {
        super(id, "resource");
        this.id = id;

    }

    public ResourceObject(int id, String title) {
        super(id, "resource");
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

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String title) {
        this.secondaryTitle = title;
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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String res_type) {
        this.resourceType = res_type;
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

    public void setSubType(int type) {
        this.subType = type;
    }

    public int getSubType() {
        return this.subType;
    }

    public void setStatusFlag(int status) {
        this.statusFlag = status;
    }

    public void setViewedFlag(int viewed) {
        this.viewedFlag = viewed;
    }

    public void setTrashFlag(int trash) {
        this.trashFlag = trash;
    }

    public int setStatusFlag() {
        return this.statusFlag;
    }

    public int setViewedFlag() {
        return this.viewedFlag;
    }

    public int setTrashFlag() {
        return this.trashFlag;
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

    public int getRelationshipMeResource() {
        return this.relationship_me_resource;
    }

    public boolean isDeleted() {
        return this.trashed;
    }

    public void setDeleted(boolean trash) {
        this.trashed = trash;
    }

    public void setResponseUser(int user) {
        this.response_user = user;
    }

    public int getResponseUser() {
        return this.response_user;
    }

    public void setResponseUserName(String user) {
        this.response_user_name = user;
    }

    public String getResponseUserName() {
        return this.response_user_name;
    }

    public void setRelationshipMeResource(int relationship) {
        this.relationship_me_resource = relationship;
    }

    @Override
    public LinkedHashMap<String, String> getAllData() {
        return null;
    }

    public void setIconId(String icon) {

        this.ownerAvatar = icon;
    }

    public Bitmap getIconBitmap() {
        if (this.ownerAvatar != null) {
            return ApiConnector.decodeBase64(this.ownerAvatar);
        }

        return null;
    }

    public String getIconId() {
        return ownerAvatar;
    }

    public int getId() {
        return id;
    }


    public int getContentType() {
        System.out.println("subType: " + subType + ", contentType: " + contentType);
        return 10 * subType + contentType;
    }

    public void setContentType(String contentTypeLabel) {
        if (contentTypeLabel.equals("media_youtube")) {
            contentType = 1;
        }
        if (contentTypeLabel.equals("media_vimeo")) {
            contentType = 2;
        }
        if (contentTypeLabel.equals("media_bambuser")) {
            contentType = 3;
        }
        if (contentTypeLabel.equals("media_soundcloud")) {
            contentType = 4;
        }
        System.out.println("contentTypeLabel: " + contentTypeLabel + ", contentType: " + contentType);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        System.out.println("url set to " + url);
    }
}

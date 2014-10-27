package net.mycitizen.mcn;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

public class ApiConnector {
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    protected Context context;
    protected String login;
    protected String password;
    protected Config cfg;

    /*
    private String small_plus = "/9j/4AAQSkZJRgABAQEASABIAAD//gA7Q1JFQVRPUjogZ2QtanBlZyB2MS4wICh1c2luZyBJSkcgSlBFRyB2NjIpLCBxdWFsaXR5ID0gOTAK/9sAQwADAgIDAgIDAwMDBAMDBAUIBQUEBAUKBwcGCAwKDAwLCgsLDQ4SEA0OEQ4LCxAWEBETFBUVFQwPFxgWFBgSFBUU/9sAQwEDBAQFBAUJBQUJFA0LDRQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQU/8IAEQgAFAAUAwERAAIRAQMRAf/EABgAAAIDAAAAAAAAAAAAAAAAAAYHBAUI/8QAFAEBAAAAAAAAAAAAAAAAAAAAAP/aAAwDAQACEAMQAAABKStDsBCSLkfAhzYh/8QAGhAAAgMBAQAAAAAAAAAAAAAABAYCAwUHJP/aAAgBAQABBQL39E1s9LBMrV3WB2al58DMpQULAL4KRT0c6q4Z1SlAl6PEEpAG/8QAFBEBAAAAAAAAAAAAAAAAAAAAMP/aAAgBAwEBPwEf/8QAFBEBAAAAAAAAAAAAAAAAAAAAMP/aAAgBAgEBPwEf/8QAJxAAAgIBAgUEAwEAAAAAAAAAAgQBAwUAERIhIkFRBhNh4SQxcfD/2gAIAQEABj8CcAHLcfgE7Jp/HnY2C78/H1p6304+/jXk2CXkrLOkzHz8f7bRRlZFPIr2FReE9yjvp/05a81j3knCMpVs9uwx8/MfWsi45kcmnWs/YUQd3BXeEbdZ8urfvOsll0zmpW1o4r35cUedFlRK5TIrDuLCx8BT/dGnl8k7erXO/tRbtBbedVrr1jTTXHCID+o1/8QAHxAAAgICAwADAAAAAAAAAAAAAREAITFBYZGxgdHh/9oACAEBAAE/IbbVSm/0WwjR4a0XMvJQZNi+yxDxZrACLAcvsHU1IvamGVv32JZg2+MBGi+ggSxMsV0vn6DMZADkKBK/eVNmYFNgVfsGfOHQJ//aAAwDAQACAAMAAAAQkkkkH//EABQRAQAAAAAAAAAAAAAAAAAAADD/2gAIAQMBAT8QH//EABQRAQAAAAAAAAAAAAAAAAAAADD/2gAIAQIBAT8QH//EABwQAQEAAwEAAwAAAAAAAAAAAAERACExQVFhof/aAAgBAQABPxBdr9HvNdAiQOipc2TU4QV6tCvuLbRvikmZYQE4Q8YrbYzFO2oVYeWlEwRUQi0AAKKcmMbZTEyiF2j8AdjjJUzyQrIB0kBrwwRvkWYSj57oWiO8hgISfAP1Xaqqq5//2Q==";
    private String big_plus = "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";
*/

    private long repeat_measuring_speed = 60000; // in milliseconds
    private long retrieve_lists_timeout = 300000; // in milliseconds

    public ApiConnector(Context ctx) {
        this.context = ctx;

        cfg = new Config(ctx);

    }


    public DataObject getDetail(String type, int id) {
        DataObject object = null;
        String api_url = null;
        String api_params = null;

        if (type.equals("user")) {
            object = new UserObject(id);
            api_url = cfg.getApiUrl() + "Base/UserData.json";
            api_params = "user_id=" + id;
        } else if (type.equals("group")) {
            object = new GroupObject(id);
            api_url = cfg.getApiUrl() + "Base/GroupData.json";
            api_params = "group_id=" + id;
        } else if (type.equals("resource")) {
            object = new ResourceObject(id);
            api_url = cfg.getApiUrl() + "Base/ResourceData.json";
            api_params = "resource_id=" + id;
        }

        String responseBody;

        if (isNetworkAvailable()) {

            System.out.println("Retrieving detail from network: " + type);
            responseBody = getNetworkResult(api_url, api_params);

        } else {

            System.out.println("Retrieving detail from database " + type);
            responseBody = getLocalResult(type, "detail", api_params);

        }

        if (responseBody == null) {
            return null;
        }

        try {

            JSONObject responseobject = new JSONObject(responseBody);

            if (responseobject.has("error")) {
                return null;
            }


            if (isNetworkAvailable()) {
                DataHandler db = new DataHandler(context);
                System.out.println(responseBody);
                LinkedHashMap<String, String> params = db.prepareData("detail", type, responseBody);
                LinkedHashMap<String, String> search_params = new LinkedHashMap<String, String>();
                search_params.put("id", String.valueOf(id));

                if (db.objectExists(search_params)) {
                    db.updateObject(type, id, params);
                } else {

                    if (params != null) {
                        params.put("id", String.valueOf(id));
                        params.put("type", type);

                        db.insertObject(type, params);
                    }
                }
                db.close();
            }
            if (type.equals("user")) {
                String name = responseobject.getString("user_name");
                String surname = responseobject.getString("user_surname");
                ((UserObject) object).setRealName(name, surname);
                String login = responseobject.getString("user_login");
                ((UserObject) object).setName(login);
                String online_status = responseobject.getString("now_online");
                ((UserObject) object).setNow_online(online_status);
                String email = responseobject.getString("user_email");
                ((UserObject) object).setEmail(email);
                String description = responseobject.getString("user_description");
                ((UserObject) object).setDescription(description);
                String gps_x = responseobject.getString("user_position_x");
                String gps_y = responseobject.getString("user_position_y");
                ((UserObject) object).setPosition(gps_x, gps_y);
                int visibility = responseobject.optInt("user_visibility_level");
                ((UserObject) object).setVisibility(visibility);
                int access = responseobject.optInt("user_access_level");
                ((UserObject) object).setAccess(access);
                String language = responseobject.getString("user_language_iso_639_3");
                ((UserObject) object).setLanguage(language);
                int relationship_me_user = responseobject.optInt("logged_user_user");
                ((UserObject) object).setRelationshipMeUser(relationship_me_user);
                int relationship_user_me = responseobject.optInt("user_logged_user");
                ((UserObject) object).setRelationshipUserMe(relationship_user_me);
                if (responseobject.has("user_portrait")) {
                    String image = responseobject.getString("user_portrait");
                    ((UserObject) object).setIconId(image);
                }
                if (responseobject.has("user_phone")) {
                    String phone = responseobject.getString("user_phone");
                    ((UserObject) object).setPhone(phone);
                }
                if (responseobject.has("user_url")) {
                    String url = responseobject.getString("user_url");
                    System.out.println("user_url: " + responseobject.getString("user_url"));
                    ((UserObject) object).setUrl(url);
                }
                if (responseobject.has("user_send_notifications") && !responseobject.isNull("user_send_notifications")) {
                    int timer = responseobject.getInt("user_send_notifications");
                    ((UserObject) object).setNotificationTimer(String.valueOf(timer));
                }


            } else if (type.equals("group")) {
                String name = responseobject.getString("group_name");
                ((GroupObject) object).setTitle(name);
                String description = responseobject.getString("group_description");
                ((GroupObject) object).setDescription(description);
                String gps_x = responseobject.getString("group_position_x");
                String gps_y = responseobject.getString("group_position_y");
                ((GroupObject) object).setPosition(gps_x, gps_y);
                int visibility = responseobject.optInt("group_visibility_level");
                ((GroupObject) object).setVisibility(visibility);
                int access = responseobject.optInt("group_access_level");
                ((GroupObject) object).setAccess(access);
                String language = responseobject.getString("group_language_iso_639_3");
                ((GroupObject) object).setLanguage(language);
                int relationship_me_group = responseobject.optInt("logged_user_member");
                ((GroupObject) object).setRelationshipMeGroup(relationship_me_group);
                if (responseobject.has("group_portrait")) {
                    String image = responseobject.getString("group_portrait");
                    ((GroupObject) object).setIconId(image);
                }
            } else if (type.equals("resource")) {
                String name = responseobject.getString("resource_name");
                ((ResourceObject) object).setTitle(name);

                String gps_x = responseobject.getString("resource_position_x");
                String gps_y = responseobject.getString("resource_position_y");
                ((ResourceObject) object).setPosition(gps_x, gps_y);
                int visibility = 1;
                if (!responseobject.isNull("resource_visibility_level")) {
                    visibility = responseobject.getInt("resource_visibility_level");
                }
                ((ResourceObject) object).setVisibility(visibility);
                if (responseobject.has("resource_access_level")) {
                    int access = 0;
                    if (!responseobject.isNull("resource_access_level")) {
                        access = responseobject.optInt("resource_access_level");
                    }
                    ((ResourceObject) object).setAccess(access);
                }
                String language = "eng";
                if (!responseobject.isNull("resource_language_iso_639_3")) {
                    language = responseobject.getString("resource_language_iso_639_3");
                }
                ((ResourceObject) object).setLanguage(language);
                int subtype = 0;
                if (!responseobject.isNull("resource_type")) {
                    subtype = responseobject.optInt("resource_type");
                }
                ((ResourceObject) object).setSubType(subtype);

                String media_type = null;
                if (responseobject.has("media_type")) {

                    media_type = responseobject.getString("media_type");

                    ((ResourceObject) object).setContentType(media_type);
                }

                int status = 1;
                if (!responseobject.isNull("resource_status")) {
                    status = responseobject.optInt("resource_status");
                }
                ((ResourceObject) object).setStatusFlag(status);
                int viewed = 0;
                if (!responseobject.isNull("resource_viewed")) {
                    viewed = responseobject.optInt("resource_viewed");
                }
                ((ResourceObject) object).setViewedFlag(viewed);
                int trash = 0;
                if (!responseobject.isNull("resource_trash")) {
                    trash = responseobject.optInt("resource_trash");
                }
                ((ResourceObject) object).setTrashFlag(trash);
                if (!responseobject.isNull("message_text")) {
                    String extratext = responseobject.getString("message_text");
                    System.out.println("message: " + extratext);
                    ((ResourceObject) object).setSecondaryTitle(extratext);
                }

                String url;
                if (!responseobject.isNull("event_url") && !responseobject.getString("event_url").equals("")) {
                    url = responseobject.getString("event_url");
                    ((ResourceObject) object).setUrl(url);
                }

                if (!responseobject.isNull("organization_url") && !responseobject.getString("organization_url").equals("")) {
                    url = responseobject.getString("organization_url");
                    ((ResourceObject) object).setUrl(url);
                }

                if (!responseobject.isNull("text_information_url") && !responseobject.getString("text_information_url").equals("")) {
                    url = responseobject.getString("text_information_url");
                    ((ResourceObject) object).setUrl(url);
                }

                if (!responseobject.isNull("other_url") && !responseobject.getString("other_url").equals("")) {
                    url = responseobject.getString("other_url");
                    ((ResourceObject) object).setUrl(url);
                }

                if (!responseobject.isNull("media_link") && !responseobject.getString("media_link").equals("")) {
                    url = responseobject.getString("media_link");
                    ((ResourceObject) object).setUrl(url);
                }

                String description = "<div>";

                description += responseobject.getString("resource_description");

                if (!responseobject.isNull("event_description") && !responseobject.getString("event_description").equals("")) {
                    description += "<br/><br/><span>Event description:</span><br/><div>" + responseobject.getString("event_description") + "</div>";
                }

                if (!responseobject.isNull("organization_information") && !responseobject.getString("organization_information").equals("")) {
                    description += "<br/><span>Organization information:</span><br/><div>" + responseobject.getString("organization_information") + "</div>";
                }

                if (!responseobject.isNull("text_information") && !responseobject.getString("text_information").equals("")) {
                    description += "<br/><br/><span>Text information:</span><br/><div>" + responseobject.getString("text_information") + "</div>";
                }

                if (!responseobject.isNull("event_timestamp") && !responseobject.getString("event_timestamp").equals("")) {
                    description += "<br/><br/><span>Date and time:</span> <span>" + responseobject.getString("event_timestamp") + "</span>";
                    //description += "<br/><br/><span>Event term:</span><span>"+DateFormat.format("yyyy-MM-dd", Long.valueOf(responseobject.getString("event_timestamp")) * 1000)+"</span>";
                }


                description += "</div>";

                ((ResourceObject) object).setDescription(description);


                int relationship_me_resource = 0;
                if (!responseobject.isNull("logged_user_member")) {
                    relationship_me_resource = responseobject.optInt("logged_user_member");
                }
                ((ResourceObject) object).setRelationshipMeResource(relationship_me_resource);
                if (responseobject.has("owner_portrait")) {
                    String image = responseobject.getString("owner_portrait");
                    ((ResourceObject) object).setIconId(image);
                }
            }

            JSONArray object_tags = responseobject.getJSONArray("tags");
            ArrayList<DataObject> tags = new ArrayList<DataObject>();

            for (int xs = 0; xs < object_tags.length(); xs++) {
                JSONObject tag = object_tags.getJSONObject(xs);

                String tag_id = tag.getString("id");
                String tag_name = tag.getString("tag_name");
                int tag_parent_id = tag.getInt("tag_parent_id");

                TagObject tag_to_add = new TagObject(Integer.valueOf(tag_id), tag_name);
                tag_to_add.setTag_parent_id(tag_parent_id);
                tags.add((DataObject) tag_to_add);


            }

            object.setTags(tags);
            return object;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return null;
        }

    }

    public String getHelpData(String topic) {
        return "";
    }


    public ArrayList<DataObject> createDashboard(String type, String filter, boolean use_filter_override, String length) {

        ArrayList<DataObject> objects = new ArrayList<DataObject>();
        String api_url = cfg.getApiUrl() + "Base/Data.json";
        String api_params = null;

        if (type.equals("user")) {
            api_params = "type[0]=1&filter[count]=" + length;
        } else if (type.equals("group")) {
            api_params = "type[1]=2&filter[count]=" + length;
        } else if (type.equals("resource")) {
            api_params = "type[2]=3&filter[count]=" + length;
        }
        String filter_override = createFilter();

        if (filter_override != null && use_filter_override) {
            api_params += filter_override;
        }

        if (filter != null) {
            api_params += "&" + filter;
        }

        System.out.println("api_params: " + api_params);
        String responseBody;

        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);
        long time_users_retrieved = settings.getLong("time_users_retrieved", 0);
        long time_groups_retrieved = settings.getLong("time_groups_retrieved", 0);
        long time_resources_retrieved = settings.getLong("time_resources_retrieved", 0);
        int length_users_retrieved = settings.getInt("length_users_retrieved", 0);
        int length_groups_retrieved = settings.getInt("length_groups_retrieved", 0);
        int length_resources_retrieved = settings.getInt("length_resources_retrieved", 0);
        long time_lists_retrieved = 0;
        int length_retrieved = 0;
        if (type.equals("user")) {
            time_lists_retrieved = time_users_retrieved;
            length_retrieved = length_users_retrieved;
        } else if (type.equals("group")) {
            time_lists_retrieved = time_groups_retrieved;
            length_retrieved = length_groups_retrieved;
        } else if (type.equals("resource")) {
            time_lists_retrieved = time_resources_retrieved;
            length_retrieved = length_resources_retrieved;
        }

        int length_int = Integer.parseInt(length);
        String source = "";
        SharedPreferences.Editor editor = settings.edit();


        if (isNetworkAvailable()
                && settings.getBoolean("filter_active", false)
                && (time_lists_retrieved + retrieve_lists_timeout < System.currentTimeMillis()
                || length_retrieved < length_int)) {

            System.out.println("Retrieving data from network: " + type);
            responseBody = getNetworkResult(api_url, api_params);

            if (type.equals("user")) {
                editor.putLong("time_users_retrieved", System.currentTimeMillis());
                editor.putInt("length_users_retrieved", length_int);
            } else if (type.equals("group")) {
                editor.putLong("time_groups_retrieved", System.currentTimeMillis());
                editor.putInt("length_groups_retrieved", length_int);
            } else if (type.equals("resource")) {
                editor.putLong("time_resources_retrieved", System.currentTimeMillis());
                editor.putInt("length_resources_retrieved", length_int);
            }

            source = "network";
            editor.putString("data_source", "network");

        } else {

            System.out.println("Retrieving data from database: type - " + type + " api_params - " + api_params);
            responseBody = getLocalResult(type, "dashboard", api_params);

            source = "database";
            editor.putString("data_source", "database");
        }
        editor.commit();


        if (responseBody == null) {
            return null;
        }


        try {

            JSONArray response = new JSONArray(responseBody);


            for (int i = 0; i < response.length(); i++) {
                JSONObject responseobject = response.getJSONObject(i);

                // Don't even display hidden items
                int hidden = 0;
                try {
                    hidden = responseobject.getInt("hidden");
                } catch (JSONException e) {
                    System.out.println("no 'hidden'");
                } finally {
                    System.out.println("hidden: " + hidden);
                    if (hidden == 1) {
                        continue;
                    }
                }

                String connectionStatus = "0";
                DataObject object = null;
                if (type.equals("user")) {
                    // SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);

                    int logged_user_id = settings.getInt("logged_user_id", 0);

                    int id = responseobject.getInt("id");

                    /*
                    if (id == 0 && source.equals("database")) {
                        continue;
                    }
                    */

                    String name = responseobject.getString("name");
                    String object_type = responseobject.getString("type_name");

                    String now_online = "";
                    if (responseobject.has("now_online") && isNetworkAvailable()) {
                        now_online = responseobject.getString("now_online");
                    }
                    System.out.println("online_status: " + now_online);

                    String image = null;
                    if (responseobject.has("avatar")) {
                        image = responseobject.getString("avatar");
                    }


                    if (responseobject.has("user_logged_user") && responseobject.has("logged_user_user")) {
                        if (responseobject.getString("user_logged_user").equals("2") && responseobject.getString("logged_user_user").equals("2")) {
                            connectionStatus = "1";
                            responseobject.put("user_object_status", "1");
                        } else {
                            connectionStatus = "0";
                            responseobject.put("user_object_status", "0");
                        }
                    } else if (responseobject.has("user_object_status")) {
                        connectionStatus = responseobject.getString("user_object_status");
                    }


                    object = new UserObject(id);
                    ((UserObject) object).setName(name);
                    ((UserObject) object).setIconId(image);
                    ((UserObject) object).setNow_online(now_online);
                    ((UserObject) object).setConnectionStatus(connectionStatus);

                    // save retrieved data to DB
                    if (isNetworkAvailable()) {
                        DataHandler db = new DataHandler(context);

                        LinkedHashMap<String, String> params = db.prepareData("dashboard", type, responseobject.toString());
                        LinkedHashMap<String, String> search_params = new LinkedHashMap<String, String>();
                        search_params.put("id", String.valueOf(id));

                        if (db.objectExists(search_params)) {
                            db.updateObject("user", id, params);
                        } else {

                            if (params != null) {
                                params.put("id", String.valueOf(id));
                                params.put("type", "user");

                                db.insertObject("user", params);
                            }
                        }
                        db.close();

                    }

                } else if (type.equals("group")) {
                    int id = responseobject.getInt("id");
                    if (source.equals("database") && id == 0) {
                        continue;
                    }
                    String name = responseobject.getString("name");
                    String object_type = responseobject.getString("type_name");


                    object = new GroupObject(id);
                    ((GroupObject) object).setTitle(name);
                    if (responseobject.has("avatar")) {
                        String image = responseobject.getString("avatar");
                        ((GroupObject) object).setIconId(image);
                    }

/*
                    if (id == 0 && source.equals("database")) {
                        continue;
                    }
*/

                    if (responseobject.has("logged_user_member")) {
                        connectionStatus = responseobject.getString("logged_user_member");
                    }

                    ((GroupObject) object).setConnectionStatus(connectionStatus);

                    // save retrieved data to DB
                    if (isNetworkAvailable()) {
                        DataHandler db = new DataHandler(context);

                        LinkedHashMap<String, String> params = db.prepareData("dashboard", type, responseobject.toString());
                        LinkedHashMap<String, String> search_params = new LinkedHashMap<String, String>();
                        search_params.put("id", String.valueOf(id));
                        System.out.println("NULL " + responseobject.toString());
                        if (db.objectExists(search_params)) {
                            db.updateObject("group", id, params);
                        } else {

                            if (params != null) {
                                params.put("id", String.valueOf(id));
                                params.put("type", "group");

                                db.insertObject("group", params);
                            } else {
                                System.out.println("NULL " + responseobject.toString());
                            }
                        }
                        db.close();

                    }
                } else if (type.equals("resource")) {
                    int id = responseobject.getInt("id");
                    /*
                    if (id == 0 && source.equals("database")) {
                        continue;
                    }
                    */
                    String name = responseobject.getString("name");
                    String object_type = responseobject.getString("type_name");
                    int sub_type = responseobject.getInt("type");

                    object = new ResourceObject(id);
                    ((ResourceObject) object).setTitle(name);

                    ((ResourceObject) object).setSubType(sub_type);

                    if (!responseobject.isNull("resource_data")) {
                        JSONObject object_data = responseobject.getJSONObject("resource_data");
                        if (object_data.has("message_text")) {

                            String message_text = object_data.getString("message_text");

                            ((ResourceObject) object).setSecondaryTitle(message_text);
                        }
                    }
                    if (responseobject.has("message_text")) {

                        String message_text = responseobject.getString("message_text");
                        System.out.println("MESSAGE2: " + message_text);
                        ((ResourceObject) object).setSecondaryTitle(message_text);
                    }


                    if (responseobject.has("media_type")) {

                        String media_type = responseobject.getString("media_type");

                        ((ResourceObject) object).setContentType(media_type);
                    }

                    String url;
                    if (!responseobject.isNull("event_url") && !responseobject.getString("event_url").equals("")) {
                        url = responseobject.getString("event_url");
                        ((ResourceObject) object).setUrl(url);
                    }

                    if (!responseobject.isNull("organization_url") && !responseobject.getString("organization_url").equals("")) {
                        url = responseobject.getString("organization_url");
                        ((ResourceObject) object).setUrl(url);
                    }

                    if (!responseobject.isNull("text_information_url") && !responseobject.getString("text_information_url").equals("")) {
                        url = responseobject.getString("text_information_url");
                        ((ResourceObject) object).setUrl(url);
                    }

                    if (!responseobject.isNull("other_url") && !responseobject.getString("other_url").equals("")) {
                        url = responseobject.getString("other_url");
                        ((ResourceObject) object).setUrl(url);
                    }

                    if (!responseobject.isNull("media_link") && !responseobject.getString("media_link").equals("")) {
                        url = responseobject.getString("media_link");
                        ((ResourceObject) object).setUrl(url);
                    }

                    if (responseobject.has("trashed")) {
                        if (!responseobject.isNull("trashed")) {
                            int trashed = responseobject.getInt("trashed");

                            Boolean tr = false;
                            if (trashed > 0) {
                                tr = true;
                            }
                            ((ResourceObject) object).setDeleted(tr);
                        }
                    }
                    if (responseobject.has("resource_trash")) {
                        if (!responseobject.isNull("resource_trash")) {
                            int trashed = responseobject.getInt("resource_trash");

                            Boolean tr = false;
                            if (trashed > 0) {
                                tr = true;
                            }
                            ((ResourceObject) object).setDeleted(tr);
                        }
                    }

                    if (responseobject.has("author")) {
                        if (!responseobject.isNull("author")) {
                            int author_id = responseobject.getInt("author");

                            ((ResourceObject) object).setResponseUser(author_id);
                        }
                    }

                    if (responseobject.has("owner_portrait")) {
                        String image = responseobject.getString("owner_portrait");
                        ((ResourceObject) object).setIconId(image);
                    }

                    if (responseobject.has("resource_type")) {
                        if (!responseobject.isNull("resource_type")) {
                            int subtype = responseobject.getInt("resource_type");
                            System.out.println("SUBTYPE: " + subtype);
                            ((ResourceObject) object).setSubType(subtype);
                        }
                    }

                    if (responseobject.has("logged_user_member")) {
                        connectionStatus = responseobject.getString("logged_user_member");
                    }

                    ((ResourceObject) object).setConnectionStatus(connectionStatus);

                    // save retrieved data to DB
                    if (isNetworkAvailable()) {
                        DataHandler db = new DataHandler(context);
                        LinkedHashMap<String, String> params = db.prepareData("dashboard", type, responseobject.toString());
                        LinkedHashMap<String, String> search_params = new LinkedHashMap<String, String>();
                        search_params.put("id", String.valueOf(id));

                        if (db.objectExists(search_params)) {
                            db.updateObject("resource", id, params);
                        } else {

                            if (params != null) {
                                params.put("id", String.valueOf(id));
                                params.put("type", "resource");

                                db.insertObject("resource", params);
                            }
                        }
                        db.close();

                    }


                }
                objects.add(object);
            }

            // todo: shift object with id 0 to the end
            // or: when writing to db, first delete with id=-1


            return objects;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return null;
        }


    }


    public LinkedHashMap<String, String> getSupportedLanguages() {

        LinkedHashMap<String, String> languages = new LinkedHashMap<String, String>();
        languages.put("en", "English");
        languages.put("ces", "Čeština");
        languages.put("hlt", "Matu");
        languages.put("mrh", "Mara");
        languages.put("zom", "Zolai");

        return languages;

    }

    public LinkedHashMap<Integer, String> getSupportedTimers() {

        LinkedHashMap<Integer, String> notifications = new LinkedHashMap<Integer, String>();
        notifications.put(0, "-");
        notifications.put(1, context.getString(R.string.once_per_hour));
        notifications.put(24, context.getString(R.string.once_per_day));
        notifications.put(168, context.getString(R.string.once_per_week));

        return notifications;

    }

    public LinkedHashMap<String, String> getSupportedTags() {

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/Tags.json", null);
            if (responseBody != null) {
                DataHandler db = new DataHandler(context);
                if (db.settingsEntryExists("tags")) {
                    db.updateTags(responseBody);
                } else {
                    db.insertTags(responseBody);
                }
                db.close();
            }
        } else {

            responseBody = getLocalResult("tags", "tags", null);
        }

        if (responseBody == null) {
            return null;
        }

        try {

            JSONObject responseobject = new JSONObject(responseBody);

            boolean status = (Boolean) responseobject.get("result");

            if (!status) {
                return null;

            } else {
                String prefix;
                LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();
                JSONArray languages = new JSONArray(responseobject.getString("tags"));
                for (int i = 0; i < languages.length(); i++) {
                    JSONObject lang = languages.getJSONObject(i);
                    if (lang.has("level") && lang.getString("level").equals("1")) {
                        prefix = " ↳ ";
                    } else {
                        prefix = "";
                    }
                    res.put(lang.getString("tag_id"), prefix + lang.getString("tag_name"));
                }
                return res;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return null;
        }


    }

    protected String createSession(final String login, final String password) {
        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);

        String session = null;

        HttpURLConnection connection;
        OutputStreamWriter request = null;

        URL url = null;
        String response = null;
        String parameters = "PASS=" + password + "&USER=" + login;

        try {

            url = new URL(cfg.getApiUrl() + "Base/Login.json");
            HttpURLConnection http = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                http = https;
            } else {
                http = (HttpURLConnection) url.openConnection();
            }

            connection = http;
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String ua = settings.getString("userAgent", null);

            if (ua != null) {
                connection.setRequestProperty("User-Agent", ua);
            } else {
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
            }
            connection.setDoOutput(true);

            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();

            String line = "";
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            // Response from server after login process will be stored in response variable.
            response = sb.toString();
            System.out.println(response);
            JSONObject result = new JSONObject(response);
            boolean status = result.getBoolean("result");

            if (!status) {
                String error = result.getString("error");

                //Toast.makeText(this.context, error, Toast.LENGTH_LONG).show();

                return error;

            }


            isr.close();
            reader.close();

            if (connection.getHeaderFields() != null) {
                System.out.println("COOKIE: " + connection.getHeaderFields());
                if (connection.getHeaderFields().get("Set-Cookie") != null) {
                    for (String cookie : connection.getHeaderFields().get("Set-Cookie")) {
                        if (cookie != null) {
                            String cookie_str = cookie.split(";", 2)[0];

                            if (cookie_str.contains("MYCITIZEN_SID")) {
                                session = cookie_str.replace("MYCITIZEN_SID=", "");
                            }
                        }
                    }
                } else {
                    for (String cookie : connection.getHeaderFields().get("set-cookie")) {
                        if (cookie != null) {
                            String cookie_str = cookie.split(";", 2)[0];

                            if (cookie_str.contains("MYCITIZEN_SID")) {
                                session = cookie_str.replace("MYCITIZEN_SID=", "");
                            }
                        }
                    }
                }
            }

            //we store session id if we received one
            if (session != null) {

                SharedPreferences.Editor editor = settings.edit();

                editor.putString("MYCITIZEN_SID", session);
                editor.putString("UserAgent", ua);

                editor.putInt("logged_user_id", result.getInt("user_id"));

                JSONObject data = result.getJSONObject("data");
                //System.out.println(result.getJSONObject("data").toString());
                  /*
                  if(data.getInt("user_language") == 1) {
    	  			editor.putString("logged_user_language", "English");
    	  		} else if(data.getInt("user_language") == 2) {
    	  			editor.putString("logged_user_language", "Burmese");
    	  		} else {
    	  			editor.putString("logged_user_language", "English");
    	  		}
    	  		*/

                if (data.getInt("user_creation_rights") == 1) {
                    editor.putBoolean("create_group_rights", true);
                } else {
                    editor.putBoolean("create_group_rights", false);
                }

                if (data.getInt("user_visibility_level") == 1) {
                    editor.putString("logged_user_visibility", "world");
                } else if (data.getInt("user_visibility_level") == 2) {
                    editor.putString("logged_user_visibility", "members");
                } else {
                    editor.putString("logged_user_visibility", "private");
                }

                editor.putInt("user_access_level", data.getInt("user_access_level"));


                JSONArray tags = data.getJSONArray("tags");
                System.out.println("Loading tags");
                String logged_user_tags = "";
                for (int i = 0; i < tags.length(); i++) {
                    JSONObject o = tags.getJSONObject(i);

                    int tag_id = o.getInt("id");

                    if (!logged_user_tags.equals("")) {
                        logged_user_tags += ",";
                    }
                    logged_user_tags += tag_id;

                }

                logged_user_tags = "[" + logged_user_tags + "]";
                editor.putString("logged_user_tags", logged_user_tags);

                editor.commit();

                return "success";
            } else {
                return "session_null";
            }


        } catch (IOException e) {
            // Error
            e.printStackTrace();
            failFunction();
            return "io_error";
        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return "couldnt_parse_result";
        }

    }

    protected boolean apiExists(String apiurl) {

        HttpURLConnection connection;
        OutputStreamWriter request = null;

        URL url = null;
        String response = null;
        String parameters = "";

        try {
            url = new URL(apiurl + "Base/Login.json");
            HttpURLConnection http = null;
            // By default, this implementation of HttpURLConnection requests that servers use gzip compression.

            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                http = https;
            } else {
                http = (HttpURLConnection) url.openConnection();
            }


            connection = http;
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            //String ua = new WebView(context).getSettings().getUserAgentString();
            //connection.setRequestProperty("User-Agent",ua);
            connection.setDoOutput(true);

            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();

            String line = "";
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            // Response from server after login process will be stored in response variable.
            response = sb.toString();


            isr.close();
            reader.close();

            JSONObject result = new JSONObject(response);
            if (result.has("result")) {
                return true;
            }

            return false;

        } catch (IOException e) {
            // Error
            e.printStackTrace();
            failFunction();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    protected boolean removeSession() {
        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("MYCITIZEN_SID", null);
        editor.putString("UserAgent", null);

        editor.commit();
        return true;
    }

    protected String sessionInit(String login, String password) {
        //boolean result = createSession(login, password);
        if (!sessionInitiated()) {

            String result = createSession(login, password);

            return result;
        } else {

            return "success";
        }
    }

    public boolean sessionInitiated() {

        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);
        String MYCITIZEN_SID = settings.getString("MYCITIZEN_SID", null);

        if (MYCITIZEN_SID != null) {
            return true;
        } else {
            if (!isNetworkAvailable()) {
                return true;
            }
            String saved_login = settings.getString("login", null);
            String saved_password = settings.getString("password", null);
            if (saved_login != null && saved_password != null) {
                String res = createSession(saved_login, saved_password);
                if (res.equals("success")) {
                    System.out.println("sessionInitiated success");
                    return true;
                }
            }
            return false;
        }

    }


    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();

    }

    public String subscribe(String objectType, String objectId, int objectAction) {

        String api_params = "objectType=" + objectType + "&objectId=" + objectId + "&objectAction=" + objectAction;

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/Subscribe.json", api_params);
        } else {
            responseBody = getLocalResult(objectType, cfg.getApiUrl() + "Base/Subscribe.json", api_params);
        }

        if (responseBody == null) {
            return null;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");
            String result_str = (String) result.getString("result_str");

            if (!status) {
                return null;

            }


            return result_str;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return null;
        }

    }

    public boolean sendMessage(String objectType, String objectId, String message) {

        String api_params = "objectType=" + objectType + "&objectId=" + objectId + "&message=" + message;

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/SendMessage.json", api_params);
        } else {
            responseBody = getLocalResult("resource", cfg.getApiUrl() + "Base/SendMessage.json", api_params);
        }

        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }


            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean CanCreateGroups() {

        String api_params = "";

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/CanCreateGroups.json", api_params);
        } else {
            responseBody = getLocalResult("resource", cfg.getApiUrl() + "Base/CanCreateGroups.json", api_params);
        }
        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("group_creation_rights");

            if (!status) {
                return false;

            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean requestLostPassword(String email) {

        String api_params = "user_email=" + email;

        String responseBody;

        responseBody = getNetworkResult(cfg.getApiUrl() + "Base/RequestPasswordChange.json", api_params);


        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }


            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean sendToTrash(int id) {

        String api_params = "message_id=" + id;

        String responseBody;

        responseBody = getNetworkResult(cfg.getApiUrl() + "Base/MoveToTrash.json", api_params);


        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public String getUnreadMessages() {

        String responseBody;

        responseBody = getNetworkResult(cfg.getApiUrl() + "Base/UnreadMessages.json", null);


        if (responseBody == null) {
            return "0";
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return "0";

            } else {
                return result.getString("unread_messages");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return "0";
        }

    }

    public boolean emptyTrash() {

        String api_params = "";

        String responseBody;

        responseBody = getNetworkResult(cfg.getApiUrl() + "Base/EmptyTrash.json", api_params);


        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean sendFromTrash(int id) {

        String api_params = "message_id=" + id;

        String responseBody;

        responseBody = getNetworkResult(cfg.getApiUrl() + "Base/MoveFromTrash.json", api_params);


        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean acceptFriendship(int message_id, int friend_id) {

        String api_params = "friend_id=" + friend_id + "&message_id=" + message_id;

        String responseBody;

        responseBody = getNetworkResult(cfg.getApiUrl() + "Base/AcceptFriendship.json", api_params);


        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean declineFriendship(int message_id, int friend_id) {

        String api_params = "friend_id=" + friend_id + "&message_id=" + message_id;

        String responseBody;

        responseBody = getNetworkResult(cfg.getApiUrl() + "Base/DeclineFriendship.json", api_params);


        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean changeProfile(String firstName, String lastName, String userEmail, String userPhone, String userUrl, String userTimer, String userDescription, String visibilityLevel, String gpsx, String gpsy, String image, String language) {


        gpsx = String.valueOf((Double) (Double.valueOf(gpsx) / 1000000));
        gpsy = String.valueOf((Double) (Double.valueOf(gpsy) / 1000000));
        String api_params;
        if (image != null) {
            api_params = "firstName=" + firstName + "&lastName=" + lastName + "&email=" + userEmail + "&phone=" + userPhone + "&url=" + userUrl + "&user_send_notifications=" + userTimer + "&description=" + userDescription + "&visibility=" + visibilityLevel + "&position_gpsx=" + (gpsx) + "&position_gpsy=" + gpsy + "&image=" + image + "&language_iso_639_3=" + language;
        } else {
            api_params = "firstName=" + firstName + "&lastName=" + lastName + "&email=" + userEmail + "&phone=" + userPhone + "&url=" + userUrl + "&user_send_notifications=" + userTimer + "&description=" + userDescription + "&visibility=" + visibilityLevel + "&position_gpsx=" + (gpsx) + "&position_gpsy=" + gpsy + "&language_iso_639_3=" + language;
        }
        System.out.println(api_params);
        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/ChangeProfile.json", api_params);
        } else {
            responseBody = getLocalResult("user", cfg.getApiUrl() + "Base/ChangeProfile.json", api_params);
        }

        if (responseBody == null) {
            return false;
        }

        try {
            System.out.println(responseBody);
            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }


            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public String createGroup(String name, String description, String visibilityLevel, String gpsx, String gpsy, String selected_tags, String language) {


        gpsx = String.valueOf((Double) (Double.valueOf(gpsx) / 1000000));
        gpsy = String.valueOf((Double) (Double.valueOf(gpsy) / 1000000));

        String api_params = "name=" + name + "&description=" + description + "&visibility=" + visibilityLevel + "&position_gpsx=" + (gpsx) + "&position_gpsy=" + gpsy + "&tags=" + selected_tags + "&language=" + language;

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/CreateGroup.json", api_params);
        } else {
            responseBody = getLocalResult("user", cfg.getApiUrl() + "Base/CreateGroup.json", api_params);
        }

        if (responseBody == null) {
            return null;
        }

        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return result.getString("message");

            }


            return result.getString("group_id");

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return null;
        }

    }

    public boolean changeProfileTag(int TagId, boolean TagStatus) {

        String api_params = "tagId=" + TagId + "&tagStatus=" + TagStatus;

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/ChangeProfileTag.json", api_params);
        } else {
            responseBody = getLocalResult("user", cfg.getApiUrl() + "Base/ChangeProfileTag.json", api_params);
        }

        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }


            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public boolean sendProfileImage(String encodedImage) {

        String api_params = "image=" + encodedImage;

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/ChangeProfileImage.json", api_params);
        } else {
            responseBody = getLocalResult("user", cfg.getApiUrl() + "Base/ChangeProfileImage.json", api_params);
        }

        if (responseBody == null) {
            return false;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return false;

            }


            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return false;
        }

    }

    public String registerUser(String user_login, String user_email, String user_password, String language) {


        String api_params = "login=" + user_login + "&email=" + user_email + "&password=" + user_password + "&language=" + language;

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/Register.json", api_params);
        } else {
            responseBody = getLocalResult("user", cfg.getApiUrl() + "Base/Register.json", api_params);
        }

        if (responseBody == null) {
            return "unknown_error";
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return result.getString("error");

            }


            return "registration_ok";

        } catch (JSONException e) {
            e.printStackTrace();
            return "unknown_error";
        }

    }

    public String checkUser(String username) {


        String api_params = "username=" + username;

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/Userexists.json", api_params);
        } else {
            responseBody = getLocalResult("user", cfg.getApiUrl() + "Base/Userexists.json", api_params);
        }

        if (responseBody == null) {
            return "unknown_error";
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");

            if (!status) {
                return result.getString("error");

            }


            return "username_available";

        } catch (JSONException e) {
            e.printStackTrace();
            return "unknown_error";
        }

    }

    public int hasUnreadMessages() {
        String api_params = "";

        String responseBody;
        if (isNetworkAvailable()) {
            responseBody = getNetworkResult(cfg.getApiUrl() + "Base/Hasmessage.json", api_params);
        } else {
            responseBody = getLocalResult("user", cfg.getApiUrl() + "Base/Hasmessage.json", api_params);
        }

        if (responseBody == null) {
            return 0;
        }
        try {

            JSONObject result = new JSONObject(responseBody);
            boolean status = (Boolean) result.getBoolean("result");
            int count = (int) result.getInt("message_count");


            return count;

        } catch (JSONException e) {
            e.printStackTrace();
            failFunction();
            return 0;
        }

    }

    public String createHelp(String theme) {
        //LinkedHashMap<String,String> help = new LinkedHashMap<String,String>();
        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);
        String loc = "eng";
        if (settings.getString("saved_lng", null) != null) {
            loc = "eng";
            if (settings.getString("saved_lng", null) == "ces") {
                loc = "ces";
            } else if (settings.getString("saved_lng", null) == "hlt") {
                loc = "hlt";
            } else if (settings.getString("saved_lng", null) == "mrh") {
                loc = "mrh";
            } else if (settings.getString("saved_lng", null) == "zom") {
                loc = "zom";
            } else {
                loc = "eng";
            }

        }

        StringBuffer stringBuffer = new StringBuffer();
        Resources res = context.getResources();
        XmlResourceParser xpp = res.getXml(context.getResources().getIdentifier("help_" + loc, "xml", context.getPackageName()));
        try {
            xpp.next();
            int eventType = xpp.getEventType();

            String current_tag = "";
            String current_body = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    //stringBuffer.append("--- Start XML ---");
                } else if (eventType == XmlPullParser.START_TAG) {

                    if (!xpp.getName().equals("help")) {
                        current_tag = xpp.getName();

                    }
                } else if (eventType == XmlPullParser.END_TAG) {


                } else if (eventType == XmlPullParser.TEXT) {

                    if (current_tag.equals(theme)) {
                        //help.put(current_tag, current_body);

                        String body = xpp.getText();

                        body = body.replace("{DEPLOYMENT_URL}", cfg.getApiUrl().replace("/API/", ""));

                        stringBuffer.append(body);
                        current_tag = "";

                    }


                }
                eventType = xpp.next();
            }
            //stringBuffer.append("\n--- End XML ---");
            return stringBuffer.toString();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            failFunction();
        } catch (IOException e) {
            e.printStackTrace();
            failFunction();
        }


        stringBuffer.append("\n--- End XML ---");
        return stringBuffer.toString();


    }

    public String createFilter() {
        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);

        String filter = settings.getString("help", null);
        String inbox_filter = settings.getString("inbox_filter", null);
        ;

        String filter_params = "";

        if (filter != null) {
            try {
                JSONObject filter_object = new JSONObject(filter);
                if (filter_object.has("filter_input")) {
                    String filter_input = filter_object.getString("filter_input");
                    if (!filter_input.equals("")) {
                        filter_params += "&filter[name]=" + filter_input;
                    }
                }
                if (filter_object.has("filter_tag")) {
                    JSONArray tags = filter_object.getJSONArray("filter_tag");
                    for (int i = 0; i < tags.length(); i++) {
                        int tag_id = tags.getInt(i);
                        filter_params += "&filter[tags][" + tag_id + "]=1";
                    }
                }
                if (filter_object.has("filter_language")) {
                    String filter_language = filter_object.getString("filter_language");
                    String filter_language_iso = "eng";
                    if (!filter_language.equals("")) {
                        if (filter_language.equals("English")) {
                            filter_language = "1";
                            filter_language_iso = "eng";
                        } else if (filter_language.equals("Burmese")) {
                            filter_language = "2";
                            filter_language_iso = "mya";
                        }
                        filter_params += "&filter[language]=" + filter_language;
                        filter_params += "&filter[language_iso_639_3]=" + filter_language_iso;
                    }
                }
                if (filter_object.has("filter_my")) {
                    Boolean filter_my = filter_object.getBoolean("filter_my");
                    if (filter_my) {

                        int logged_user_id = settings.getInt("logged_user_id", 0);

                        filter_params += "&filter[user_id]=" + logged_user_id;
                    }
                }

                String location_filter_gpsx = settings.getString("filter_gpsx", null);
                String location_filter_gpsy = settings.getString("filter_gpsy", null);
                int location_filter_radius = settings.getInt("filter_radius", 0);

                if (location_filter_gpsx != null && location_filter_gpsy != null && location_filter_radius > 0) {
                    double gpsx = Double.valueOf(location_filter_gpsx) / 1000000;
                    double gpsy = Double.valueOf(location_filter_gpsy) / 1000000;

                    filter_params += "&filter[mapfilter][center][lat]=" + gpsx + "&filter[mapfilter][center][lng]=" + gpsy + "&filter[mapfilter][radius][length]=" + location_filter_radius;

                }


            } catch (JSONException e) {
                e.printStackTrace();
                // todo Instead of returning to LoginActivity, switch to offline mode.
                /*
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("last_network_request", 5000);
                editor.putLong("time_connection_measured", System.currentTimeMillis());
                editor.commit();
                */
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra("type", "apiError");
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);


                ((Activity) context).startActivity(intent);

                ((Activity) context).finish();
                return null;
            }

        }
        /*
		if(inbox_filter != null) {
			try {
				JSONObject filter_object = new JSONObject(inbox_filter);
							
				if(filter_object.has("filter_removed")) {
					Boolean filter_trash = filter_object.getBoolean("filter_removed");
					if(filter_trash) {
					
						
					
						filter_params += "&filter[trash]=1";
					} 
				}
				
				if(filter_object.has("filter_opened")) {
					Boolean filter_open = filter_object.getBoolean("filter_opened");
					if(filter_open) {
					
						
					
						filter_params += "&filter[opened]=0";
					} 
				}
		    	
		    	
			} catch (JSONException e) {
				e.printStackTrace();
				failFunction();
				return null;
			}
		}
		*/
        if (filter_params.equals("")) {
            return null;
        }
        return filter_params;
    }

    public LinkedHashMap<String, String> createLocalFilter(String params) {
        LinkedHashMap<String, String> where = new LinkedHashMap<String, String>();

        if (params == null) {
            return null;
        }
        String[] p = params.split("&");
        for (int i = 0; i < p.length; i++) {
            String[] key_value = p[i].split("=");
            try {
                String key = key_value[0];
                String value = key_value[1];

                where.put(key, value);
            } catch (Exception e) {

            }
        }

        return where;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            System.out.println("CONNECTIVITY: " + activeNetworkInfo.isConnected());
        }
        //return  false;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getNetworkResult(String api_url, String api_params) {
        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);


        // Determine only if last time more than x seconds ago
        long last_determined = settings.getLong("time_connection_measured", 0);
        long time_passed = last_determined + repeat_measuring_speed;
        System.out.println("Determine connection quality, last time " + (System.currentTimeMillis() - last_determined) / 1000 + " seconds ago.");
        if (time_passed < System.currentTimeMillis()) {

            determineConnectionQuality();
        } else {
            System.out.println("... skipped");
        }

        String MYCITIZEN_SID = settings.getString("MYCITIZEN_SID", null);
        String ua = settings.getString("UserAgent", null);

        HttpURLConnection connection;
        OutputStreamWriter request = null;

        URL url = null;

        if (api_params != null) {
            System.out.println("api_params: " + api_params);
        }
        try {

            url = new URL(api_url);

            HttpURLConnection http = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                http = https;
            } else {
                http = (HttpURLConnection) url.openConnection();
            }
            connection = http;
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Cookie", "MYCITIZEN_SID=" + MYCITIZEN_SID);
            //connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("User-Agent", ua);
            connection.setDoOutput(true);

            request = new OutputStreamWriter(connection.getOutputStream());

            if (api_params != null) {
                int speed = settings.getInt("connection_strength", 0);
                if (speed <= 50) {
                    api_params += "&speed=0";
                } else {
                    api_params += "&speed=1";
                }
                request.write(api_params);
            } else {
                int speed = settings.getInt("connection_strength", 0);
                if (speed <= 50) {
                    api_params += "speed=0";
                } else {
                    api_params += "speed=1";
                }
                request.write(api_params);

            }

            request.flush();
            request.close();

            String line = "";
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            // Response from server after login process will be stored in response variable.
            String responseBody = sb.toString();
            // You can perform UI operations here


            isr.close();
            reader.close();

            return responseBody;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            failFunction();
        }
        return null;

    }

    public void determineConnectionQuality() {

        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);

        try {
            long start_time = System.currentTimeMillis();
            URL url = new URL(cfg.getApiUrl() + "Base/Login.json");
            HttpURLConnection urlc = null;
            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                urlc = https;
            } else {
                urlc = (HttpURLConnection) url.openConnection();
            }

            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            urlc.setRequestProperty("Connection", "close");

            urlc.setConnectTimeout(1000 * 5); // mTimeout is in seconds
            urlc.connect();

            if (urlc.getResponseCode() == 200) {

                long stop_time = System.currentTimeMillis();

                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("last_network_request", stop_time - start_time);
                editor.commit();
            }
        } catch (SocketTimeoutException e) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("last_network_request", 5000);
            editor.commit();
        } catch (MalformedURLException e1) {
            System.out.println("e malformed");
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getLocalResult(String type, String url, String params) {
        DataHandler db = new DataHandler(context);
        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);
        SharedPreferences.Editor editor = settings.edit();
        //editor.putLong("last_network_request", 10000);
        editor.commit();

        LinkedHashMap<String, String> p = createLocalFilter(params);

        if (url.equals("detail")) {
            return db.getObject(type, p);
        } else if (url.equals("dashboard")) {
            return db.getObjects(type, p);
        } else if (url.equals("tags")) {
            return db.getTags();
        } else {
            db.close();
            return null;
        }

    }

    public static String encodeTobase64(Bitmap image) {
        if (image != null) {
            Bitmap immagex = image;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);

            System.out.println("Encoded: " + imageEncoded);
            return imageEncoded;
        } else return "";
    }

    public static String encode(byte[] d) {

        if (d == null) return null;
        byte data[] = new byte[d.length + 2];
        System.arraycopy(d, 0, data, 0, d.length);
        byte dest[] = new byte[(data.length / 3) * 4];

        // 3-byte to 4-byte conversion
        for (int sidx = 0, didx = 0; sidx < d.length; sidx += 3, didx += 4) {
            dest[didx] = (byte) ((data[sidx] >>> 2) & 077);
            dest[didx + 1] = (byte) ((data[sidx + 1] >>> 4) & 017 |
                    (data[sidx] << 4) & 077);
            dest[didx + 2] = (byte) ((data[sidx + 2] >>> 6) & 003 |
                    (data[sidx + 1] << 2) & 077);
            dest[didx + 3] = (byte) (data[sidx + 2] & 077);
        }

        // 0-63 to ascii printable conversion
        for (int idx = 0; idx < dest.length; idx++) {
            if (dest[idx] < 26) dest[idx] = (byte) (dest[idx] + 'A');
            else if (dest[idx] < 52) dest[idx] = (byte) (dest[idx] + 'a' - 26);
            else if (dest[idx] < 62) dest[idx] = (byte) (dest[idx] + '0' - 52);
            else if (dest[idx] < 63) dest[idx] = (byte) '+';
            else dest[idx] = (byte) '/';
        }

        // add padding
        for (int idx = dest.length - 1; idx > (d.length * 4) / 3; idx--) {
            dest[idx] = (byte) '=';
        }
        return new String(dest);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeBase64(String input) {
        try {
            byte[] decodedByte = Base64.decode(input, Base64.NO_PADDING | Base64.NO_WRAP);
            BitmapFactory.Options options = new BitmapFactory.Options();

            if (decodedByte.length > 50000) {
                options.inSampleSize = 8;
            }
            Bitmap temp = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length, options);

            int height = temp.getHeight();
            int width = temp.getWidth();

            return Bitmap.createScaledBitmap(temp, width, height, true);
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        }
        return null;
    }

    public Bitmap defaultUserIcon() {
        Bitmap temp = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_img);

        int height = temp.getHeight();
        int width = temp.getWidth();

        return Bitmap.createScaledBitmap(temp, width, height, true);
    }

    public void failFunction() {
        if (isNetworkAvailable()) {

            // todo Instead of returning to LoginActivity, switch to offline mode.
            /*
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("last_network_request", 5000);
                editor.putLong("time_connection_measured", System.currentTimeMillis());
                editor.commit();
                */
            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra("type", "apiError");


            ((Activity) context).startActivity(intent);

            ((Activity) context).finish();
        }
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

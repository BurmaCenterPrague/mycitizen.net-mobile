package net.mycitizen.mcn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHandler {
    public static String Lock = "dblock";
    public static int RES_TYPE_MESSAGE = 1;
    public static int RES_TYPE_AUDIO = 2;
    public static int RES_TYPE_VIDEO = 3;

    public static int DATA_OBJECT_TYPE_USER = 1;
    public static int DATA_OBJECT_TYPE_GROUP = 2;
    public static int DATA_OBJECT_TYPE_RESOURCE = 3;

    private static final String DATABASE_NAME = "myCitizenCache";

    private static final String TABLE_NAME_OBJECTS = "objects";
    private static final String TABLE_NAME_SETTINGS = "settings";

    public static final int DATABASE_VERSION = 3;

    private Context context;
    private SQLiteDatabase db;
    private OpenDatabase open;
    private SQLiteStatement statement;
    private String query;

    public DataHandler(Context context) {
        this.context = context;
        open = new OpenDatabase(this.context);
        this.db = open.getWritableDatabase();
        if (!open.mainTableExists()) {


            recreateDatabase();
        }
        this.db = open.getWritableDatabase();

    }

    public void recreateDatabase() {
        synchronized (Lock) {
            OpenDatabase open = new OpenDatabase(this.context);
            open.createDataBase();
        }
    }

    public void close() {
        this.db.close();
    }

    public boolean insertObject(String oType, LinkedHashMap<String, String> data) {
        String types = "";
        String values = "";
        Log.d(Config.DEBUG_TAG, "INSERT");
        int column = 1;

        try {


            Iterator<Entry<String, String>> it = data.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();

                String key = pairs.getKey().toString();
                String value = DatabaseUtils.sqlEscapeString(pairs.getValue().toString());

                if (!types.equals("")) {
                    types += ",";
                }
                types += "`" + key + "`";

                if (!values.equals("")) {
                    values += ",";
                }
                values += value;


                //this.statement.bindString(column++, (value));

            }

            query = "INSERT INTO `" + TABLE_NAME_OBJECTS + "` (" + types + ") VALUES (" + values + ")";
            Log.d(Config.DEBUG_TAG, query);
            this.statement = this.db.compileStatement(query);
            this.statement.executeInsert();

            this.statement.close();
        } catch (Exception e) {
            e.printStackTrace();

        }

        return true;
    }

    public boolean updateObject(String oType, int oId, LinkedHashMap<String, String> data) {

        String types = "";
        String values = "";
        String last_update = Long.toString(System.currentTimeMillis());

        int column = 1;

        if (data == null) {
            return false;
        }

        try {

            Iterator<Entry<String, String>> it = data.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();

                String key = pairs.getKey().toString();
                String value = DatabaseUtils.sqlEscapeString(pairs.getValue().toString());

                if (!types.equals("")) {
                    types += ",";
                }
                types += "`" + key + "` = " + value;


                //this.statement.bindString(column++, (value));

            }


            if (oId == 0) {
                query = "DELETE FROM `" + TABLE_NAME_OBJECTS + "` WHERE `type` = '" + oType + "' AND `id` = '0'";
            }
            //this.statement.bindString(column++, (oType));
            //this.statement.bindLong(column++, (oId));


            query = "UPDATE `" + TABLE_NAME_OBJECTS + "` SET " + types + ", `last_update` = " + last_update + " WHERE `type` = '" + oType + "' AND `id` = '" + oId + "'";
            // Log.d(Config.DEBUG_TAG, "updateObject "+query);
            // query += "; UPDATE `" + TABLE_NAME_OBJECTS + "` SET `last_update` = " + last_update + " WHERE `type` = '" + oType + "' AND `id` = '" + oId + "'";
            this.statement = this.db.compileStatement(query);

            this.statement.executeInsert();

            //query = "UPDATE `" + TABLE_NAME_OBJECTS + "` SET `last_update` = " + last_update + " WHERE `type` = '" + oType + "' AND `id` = '" + oId + "'";
            //this.statement = this.db.compileStatement(query);
            //this.statement.execute();

            this.statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean objectExists(LinkedHashMap<String, String> params) {

        String where = translateFilterStorageWHERE(params);
        int size = translateFilterStorageSize(params);
        String[] where_values = translateFilterStorageVALUE(params, size);
        // Log.d(Config.DEBUG_TAG, "where_values: " + Arrays.toString(where_values));

        Cursor cursor = this.db.rawQuery("SELECT id FROM " + TABLE_NAME_OBJECTS + " WHERE " + where, where_values);

        Boolean found = false;
        while (cursor.moveToNext()) {
            found = true;
            break;
        }
        cursor.close();

        return found;

    }

    public String getObjects(String type, LinkedHashMap<String, String> params) {
        Log.d(Config.DEBUG_TAG, "getObjects, params " + params);
        String where = translateFilterStorageWHERE(params);
        int size = translateFilterStorageSize(params);
        String[] where_values = translateFilterStorageVALUE(params, size);

        String data = "[";

        Log.d(Config.DEBUG_TAG, "getObjects, WHERE " + where);
        Log.d(Config.DEBUG_TAG, "getObjects, where_values: " + Arrays.toString(where_values));
        try {
            Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME_OBJECTS + " WHERE " + where, where_values);
            while (cursor.moveToNext()) {
                int count = cursor.getColumnCount();
                if (!data.equals("[")) {
                    data += ",";
                }
                data += "{";
                String subdata = "";
                for (int i = 0; i < count; i++) {
                    String key = cursor.getColumnName(i);
                    String value = cursor.getString(i);

                    if (!subdata.equals("")) {
                        subdata += ",";
                    }
                    //Log.d(Config.DEBUG_TAG, type + " " + key + " " + translateKeyFromDb(type, key));
                    if (translateKeyFromDb(type, key).equals("resource_data")) {
                        subdata += translateKeyFromDb(type, key) + ": " + value;
                    } else {
                        subdata += translateKeyFromDb(type, key) + ": " + JSONObject.quote(value);
                    }
                    if (key.equals("title")) {
                        subdata += ",name: " + JSONObject.quote(value);
                    }
                }
                data += subdata;
                data += "}";

            }
            cursor.close();
            data += "]";
            Log.d(Config.DEBUG_TAG, "getObjects, data: " + data);

        } catch (Exception e) {
            Log.d(Config.DEBUG_TAG, e.getMessage());
        }

        return data;
    }

    public String getObject(String type, LinkedHashMap<String, String> params) {
        String data = "{";

        String where = translateFilterStorageWHERE(params);
        int size = translateFilterStorageSize(params);
        String[] where_values = translateFilterStorageVALUE(params, size);

        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME_OBJECTS + " WHERE " + where, where_values);

        while (cursor.moveToNext()) {
            int count = cursor.getColumnCount();

            for (int i = 0; i < count; i++) {
                String key = cursor.getColumnName(i);
                String value = cursor.getString(i);
                if (!data.equals("{")) {
                    data += ",";
                }
                if (translateKeyFromDb(type, key).equals("tags")) {
                    data += translateKeyFromDb(type, key) + ": " + value;
                } else {
                    data += translateKeyFromDb(type, key) + ": " + JSONObject.quote(value);
                }
            }

        }
        cursor.close();

        data += "}";

        return data;
    }


    public String getLastUpdate(String type, LinkedHashMap<String, String> params) {
        String where = translateFilterStorageWHERE(params);
        int size = translateFilterStorageSize(params);
        String[] where_values = translateFilterStorageVALUE(params, size);

        Cursor cursor = this.db.rawQuery("SELECT last_update FROM " + TABLE_NAME_OBJECTS + " WHERE " + where, where_values);

        String value = "0";
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            if (cursor.getString(0) != null) {
                value = cursor.getString(0);
            }
        }

        // Log.d(Config.DEBUG_TAG, "where: "+where+", where_values: "+where_values.toString()+", getLastUpdate: " + value);
        cursor.close();
        return value;
    }


    public String getTags() {

        String data = "";

        Cursor cursor = this.db.rawQuery("SELECT value FROM " + TABLE_NAME_SETTINGS + " WHERE `key` = 'tags'", null);

        while (cursor.moveToNext()) {
            data = cursor.getString(0);
        }
        cursor.close();

        if (data.equals("")) {
            data = "{}";
        }
        return data;
    }

    public void insertTags(String json) {
        String query = "INSERT INTO `" + TABLE_NAME_SETTINGS + "` (`key`,`value`) VALUES ('tags',?)";

        this.statement = this.db.compileStatement(query);
        this.statement.bindString(1, json);

        this.statement.executeInsert();

        this.statement.close();
    }

    public void updateTags(String json) {
        String query = "UPDATE `" + TABLE_NAME_SETTINGS + "` SET `value` = ? WHERE `key` = 'tags'";
        this.statement = this.db.compileStatement(query);
        this.statement.bindString(1, json);

        this.statement.executeInsert();

        this.statement.close();
    }

    public boolean settingsEntryExists(String key) {
        Cursor cursor = this.db.rawQuery("SELECT id FROM " + TABLE_NAME_SETTINGS + " WHERE key = 'tags'", null);

        int data = 0;
        while (cursor.moveToNext()) {

            data++;
        }
        cursor.close();

        return data != 0;
    }

    public String translateFilterStorageWHERE(LinkedHashMap<String, String> params) {
        String where = "";

        Iterator<Entry<String, String>> it = params.entrySet().iterator();
        String subtype = "";
        ArrayList<String> defined_tags = new ArrayList<String>();

        while (it.hasNext()) {
            HashMap.Entry pairs = (HashMap.Entry) it.next();
            Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
            //pairs.getKey();
            String key = pairs.getKey().toString();
            String value = pairs.getValue().toString();

            if (key.equals("filter[mapfilter][center][lat]") ||
                    key.equals("filter[mapfilter][center][lng]") ||
                    key.equals("filter[mapfilter][radius][length]") ||

                    doRegexp("filter[all_members_only]", key)) {
                continue;
            }

            String sql_key;

            if (key.equals("type[0]") || key.equals("type[1]") || key.equals("type[2]")) {
                if (!where.equals("")) {
                    where += " AND ";
                }
                sql_key = "type";
                // todo: should it not be "= id for type"?
                where += sql_key + " = ?";
            } else if (key.equals("filter[language_iso_639_3]")) {

                if (!value.equals(this.context.getString(R.string.language_all))) {
                    if (!where.equals("")) {
                        where += " AND ";
                    }
                    sql_key = "language";
                    where += sql_key + " = ?";
                }
            } else if (key.equals("filter[name]")) {
                if (!where.equals("")) {
                    where += " AND ";
                }
                sql_key = "title";
                where += sql_key + " LIKE '%" + value + "%'";
            } else if (key.equals("filter[type]") || key.equals("filter[type][0]") || key.equals("filter[type][1]") || key.equals("filter[type][2]") || key.equals("filter[type][3]") || key.equals("filter[type][4]") || key.equals("filter[type][5]") || key.equals("filter[type][6]") || key.equals("filter[type][7]") || key.equals("filter[type][8]")) {

                if (!subtype.equals("")) {
                    subtype += " OR ";
                }
                sql_key = "subtype";
                subtype += sql_key + " = " + value;
            } else if (key.equals("id") || key.equals("user_id") || key.equals("group_id") || key.equals("resource_id")) {
                if (!where.equals("")) {
                    where += " AND ";
                }
                sql_key = "id";

                where += sql_key + " = ?";
            } else if (key.equals("filter[trash]")) {
                if (!where.equals("")) {
                    where += " AND ";
                }
                sql_key = "trashed";
                where += sql_key + " = ?";
            } else if (key.equals("filter[viewed]")) {
                if (!where.equals("")) {
                    where += " AND ";
                }
                sql_key = "viewed";
                where += sql_key + " = ?";
            } else if (doRegexp("filter[tags]", key)) {
                String tag_id = key.replace("filter[tags][", "").replace("]", "");

                defined_tags.add(tag_id);
            } else if (doRegexp("filter[user_id]", key)) {
                if (!where.equals("")) {
                    where += " AND ";
                }
                sql_key = "user_object_status";
                where += sql_key + " = 1";
            }


        }

        if (defined_tags.size() > 0) {
            Log.d(Config.DEBUG_TAG, "DEFINED TAGS");
            if (!where.equals("")) {

                where += " AND ";
            }
            String tags = "";
            Iterator<String> tg_i = defined_tags.iterator();
            while (tg_i.hasNext()) {
                String tg = tg_i.next();
                Log.d(Config.DEBUG_TAG, tg);
                if (!tags.equals("")) {
                    tags += " AND ";
                }
                tags += "tags LIKE '%\"id\":" + tg + ",%'";
            }

            where += "(" + tags + ")";
        }

        if (!subtype.equals("")) {
            if (!where.equals("")) {
                Log.d(Config.DEBUG_TAG, "SUBTYPE");
                where += " AND ";
            }


            where += "(" + subtype + ")";
        }

        if (where.equals("")) {
            where = "1=1";
        }
        Log.d(Config.DEBUG_TAG, "WHERE: " + where);
        return where;

    }

    public int translateFilterStorageSize(LinkedHashMap<String, String> params) {
        int size = 0;

        Iterator<Entry<String, String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pairs = (HashMap.Entry) it.next();

            String key = pairs.getKey().toString();
            String value = pairs.getValue().toString();
            if (key.equals("filter[mapfilter][center][lat]") ||
                    key.equals("filter[mapfilter][center][lng]") ||
                    key.equals("filter[mapfilter][radius][length]") ||

                    doRegexp("filter[all_members_only]", key) ||
                    doRegexp("filter[tags]", key)) {
                continue;
            }

            if (key.equals("type[0]") || key.equals("type[1]") || key.equals("type[2]")) {
                size++;
            } else if (key.equals("filter[language_iso_639_3]")) {
                if (!value.equals(this.context.getString(R.string.language_all))) {
                    size++;
                }
            } else if (key.equals("filter[name]")) {
                //size++;
            } else if (key.equals("filter[type]")) {
                size++;
            } else if (key.equals("id") || key.equals("user_id") || key.equals("group_id") || key.equals("resource_id")) {
                size++;
            } else if (key.equals("filter[trash]")) {
                size++;
            } else if (key.equals("filter[viewed]")) {
                size++;
            }


        }


        return size;

    }

    public boolean doRegexp(String needle_regexp, String haystack) {
        Pattern p = Pattern.compile(needle_regexp.replace("[", "\\[").replace("]", "\\]"), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = p.matcher(haystack);
        // Log.d(Config.DEBUG_TAG, "XXX: " + needle_regexp.replace("[", "\\[").replace("]", "\\]") + " " + haystack);
        String match = "";
        return matcher.find();
    }

    public String[] translateFilterStorageVALUE(LinkedHashMap<String, String> params, int size) {
        String[] value = new String[size];

        Iterator<Entry<String, String>> it = params.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            HashMap.Entry pairs = (HashMap.Entry) it.next();

            String key = pairs.getKey().toString();
            String value_p = pairs.getValue().toString();
            if (key.equals("filter[mapfilter][center][lat]") ||
                    key.equals("filter[mapfilter][center][lng]") ||
                    key.equals("filter[mapfilter][radius][length]") ||
                    doRegexp("filter[all_members_only]", key) ||
                    doRegexp("filter[tags]", key)) {
                continue;
            }

            if (key.equals("type[0]") || key.equals("type[1]") || key.equals("type[2]")) {
                if (pairs.getValue().toString().equals("1")) {
                    value[i] = "user";
                } else if (pairs.getValue().toString().equals("2")) {
                    value[i] = "group";
                } else {
                    value[i] = "resource";
                }
                i++;
            } else if (key.equals("filter[language_iso_639_3]")) {
                if (!value_p.equals(this.context.getString(R.string.language_all))) { //"All"
                    value[i] = pairs.getValue().toString();
                    i++;
                }
            } else if (key.equals("filter[name]")) {
                //value[i] = pairs.getValue().toString();
                //i++;
            } else if (key.equals("filter[type]")) {
                value[i] = pairs.getValue().toString();
                i++;
            } else if (key.equals("id") || key.equals("user_id") || key.equals("group_id") || key.equals("resource_id")) {
                value[i] = pairs.getValue().toString();
                i++;
            } else if (key.equals("filter[trash]")) {
                value[i] = String.valueOf(1);
                i++;
            } else if (key.equals("filter[viewed]")) {
                value[i] = String.valueOf(1);
                i++;
            }


        }

        return value;

    }

    public String translateKeyToDb(String type, String db_key) {
        String translation = db_key;
        if (db_key.equals("message_text")) {
            translation = "description2";
        } else if (db_key.equals("name")) {
            translation = "title";
        } else if (db_key.equals("type_name")) {
            translation = "type";
        } else if (db_key.equals("user_login")) {
            translation = "title";
        } else if (db_key.equals("user_name")) {
            translation = "title2";
        } else if (db_key.equals("user_surname")) {
            translation = "title3";
        } else if (db_key.equals("group_name")) {
            translation = "title";
        } else if (db_key.equals("resource_name")) {
            translation = "title";
        } else if (db_key.equals("user_email")) {
            translation = "email";
        } else if (db_key.equals("tags")) {
            translation = "tags";
        } else if (db_key.equals("user_description")) {
            translation = "description";
        } else if (db_key.equals("group_description")) {
            translation = "description";
        } else if (db_key.equals("resource_description")) {
            translation = "description";
        } else if (db_key.equals("user_position_x")) {
            translation = "gpsx";
        } else if (db_key.equals("user_position_y")) {
            translation = "gpsy";
        } else if (db_key.equals("group_position_x")) {
            translation = "gpsx";
        } else if (db_key.equals("group_position_y")) {
            translation = "gpsy";
        } else if (db_key.equals("resource_position_x")) {
            translation = "gpsx";
        } else if (db_key.equals("resource_position_y")) {
            translation = "gpsy";
        } else if (db_key.equals("user_visibility_level")) {
            translation = "visibility";
        } else if (db_key.equals("group_visibility_level")) {
            translation = "visibility";
        } else if (db_key.equals("resource_visibility_level")) {
            translation = "visibility";
        } else if (db_key.equals("user_access_level")) {
            translation = "access";
        } else if (db_key.equals("access_level")) {
            translation = "access";
        } else if (db_key.equals("group_access_level")) {
            translation = "access";
        } else if (db_key.equals("resource_access_level")) {
            translation = "access";
        } else if (db_key.equals("user_language_iso_639_3")) {
            translation = "language";
        } else if (db_key.equals("group_language_iso_639_3")) {
            translation = "language";
        } else if (db_key.equals("resource_language_iso_639_3")) {
            translation = "language";
        } else if (db_key.equals("resource_type")) {
            translation = "subtype";
        } else if (db_key.equals("media_type")) {
            translation = "media_type";
        } else if (db_key.equals("user_status")) {
            translation = "status";
        } else if (db_key.equals("group_status")) {
            translation = "status";
        } else if (db_key.equals("resource_status")) {
            translation = "status";
        } else if (db_key.equals("user_viewed")) {
            translation = "viewed";
        } else if (db_key.equals("group_viewed")) {
            translation = "viewed";
        } else if (db_key.equals("resource_viewed")) {
            translation = "viewed";
        } else if (db_key.equals("resource_trash")) {
            translation = "trashed";
        } else if (db_key.equals("user_object_status")) {
            translation = "user_object_status";
        } else if (db_key.equals("resource_data")) {
            translation = "subdata";
        } else if (db_key.equals("image")) {
            translation = "image";
        } else if (db_key.equals("access_level")) {
            translation = "access";
        } else if (db_key.equals("status")) {
            translation = "status";
        } else if (db_key.equals("viewed")) {
            translation = "viewed";
        } else if (db_key.equals("trashed")) {
            translation = "trashed";
        } else if (db_key.equals("message_type")) {
            translation = "subtype";
        } else if (db_key.equals("now_online")) {
            translation = "now_online";
        } else if (db_key.equals("url")) {
            translation = "url";
        }
        return translation;
    }

    public String translateKeyFromDb(String type, String db_key) {
        String translation = db_key;
        if (type.equals("user")) {
            if (db_key.equals("title")) {
                translation = "user_login";
            } else if (db_key.equals("title2")) {
                translation = "user_name";
            } else if (db_key.equals("title3")) {
                translation = "user_surname";
            } else if (db_key.equals("email")) {
                translation = "user_email";
            } else if (db_key.equals("description")) {
                translation = "user_description";
            } else if (db_key.equals("gpsx")) {
                translation = "user_position_x";
            } else if (db_key.equals("gpsy")) {
                translation = "user_position_y";
            } else if (db_key.equals("access")) {
                translation = "user_access_level";
            } else if (db_key.equals("visibility")) {
                translation = "user_visibility_level";
            } else if (db_key.equals("language")) {
                translation = "user_language_iso_639_3";
            } else if (db_key.equals("tags")) {
                translation = "tags";
            } else if (db_key.equals("status")) {
                translation = "status";
            } else if (db_key.equals("viewed")) {
                translation = "user_viewed";
            } else if (db_key.equals("user_logged_user")) {
                translation = "user_logged_user";
            } else if (db_key.equals("logged_user_user")) {
                translation = "logged_user_user";
            } else if (db_key.equals("id")) {
                translation = "id";
            } else if (db_key.equals("type")) {
                translation = "type_name";
            } else if (db_key.equals("image")) {
                translation = "user_portrait";
            } else if (db_key.equals("now_online")) {
                translation = "now_online";
            } else if (db_key.equals("url")) {
                translation = "user_url";
            }
        } else if (type.equals("group")) {
            if (db_key.equals("title")) {
                translation = "group_name";
            } else if (db_key.equals("description")) {
                translation = "group_description";
            } else if (db_key.equals("gpsx")) {
                translation = "group_position_x";
            } else if (db_key.equals("gpsy")) {
                translation = "group_position_y";
            } else if (db_key.equals("access")) {
                translation = "group_access_level";
            } else if (db_key.equals("visibility")) {
                translation = "group_visibility_level";
            } else if (db_key.equals("language")) {
                translation = "group_language_iso_639_3";
            } else if (db_key.equals("tags")) {
                translation = "tags";
            } else if (db_key.equals("status")) {
                translation = "status";
            } else if (db_key.equals("viewed")) {
                translation = "group_viewed";
            } else if (db_key.equals("logged_user_member")) {
                translation = "logged_user_member";
            } else if (db_key.equals("id")) {
                translation = "id";
            } else if (db_key.equals("type")) {
                translation = "type_name";
            } else if (db_key.equals("image")) {
                translation = "group_portrait";
            }
        } else {
            if (db_key.equals("title")) {
                translation = "resource_name";
            } else if (db_key.equals("description")) {
                translation = "resource_description";
            } else if (db_key.equals("gpsx")) {
                translation = "resource_position_x";
            } else if (db_key.equals("gpsy")) {
                translation = "resource_position_y";
            } else if (db_key.equals("access")) {
                translation = "resource_access_level";
            } else if (db_key.equals("visibility")) {
                translation = "resource_visibility_level";
            } else if (db_key.equals("language")) {
                translation = "resource_language_iso_639_3";
            } else if (db_key.equals("tags")) {
                translation = "tags";
            } else if (db_key.equals("status")) {
                translation = "status";
            } else if (db_key.equals("viewed")) {
                translation = "resource_viewed";
            } else if (db_key.equals("logged_user_member")) {
                translation = "logged_user_member";
            } else if (db_key.equals("trashed")) {
                translation = "resource_trash";
            } else if (db_key.equals("description2")) {
                translation = "message_text";
            } else if (db_key.equals("subtype")) {
                translation = "type";
            } else if (db_key.equals("id")) {
                translation = "id";
            } else if (db_key.equals("type")) {
                translation = "type_name";
            } else if (db_key.equals("subdata")) {
                translation = "resource_data";
            } else if (db_key.equals("url")) {
                translation = "other_url";
            } else if (db_key.equals("media_type")) {
                translation = "media_type";
            }
        }

        return translation;
    }

    public LinkedHashMap<String, String> prepareData(String vType, String type, String json) {

        LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

        try {

            JSONObject responseobject = new JSONObject(json);


            if (type.equals("user")) {
                if (responseobject.has("user_name")) {
                    String name = responseobject.getString("user_name");
                    data.put(translateKeyToDb(type, "user_name"), name);
                }

                if (responseobject.has("user_portrait")) {
                    String image = responseobject.getString("user_portrait");
                    data.put(translateKeyToDb(type, "image"), image);
                }

                if (responseobject.has("user_surname")) {
                    String surname = responseobject.getString("user_surname");
                    data.put(translateKeyToDb(type, "user_surname"), surname);
                }
                if (responseobject.has("user_login")) {
                    String login = responseobject.getString("user_login");
                    data.put(translateKeyToDb(type, "user_login"), login);
                }
                if (responseobject.has("name")) {
                    String name = responseobject.getString("name");
                    data.put(translateKeyToDb(type, "user_login"), name);
                }

                if (responseobject.has("status")) {
                    String status = responseobject.getString("status");
                    data.put(translateKeyToDb(type, "status"), status);
                }
                if (responseobject.has("user_email")) {
                    String email = responseobject.getString("user_email");
                    data.put(translateKeyToDb(type, "user_email"), email);
                }
                if (responseobject.has("user_description")) {
                    String description = responseobject.getString("user_description");
                    data.put(translateKeyToDb(type, "user_description"), description);
                }
                if (responseobject.has("user_position_x")) {
                    String gps_x = responseobject.getString("user_position_x");
                    data.put(translateKeyToDb(type, "user_position_x"), gps_x);
                }
                if (responseobject.has("user_position_y")) {
                    String gps_y = responseobject.getString("user_position_y");
                    data.put(translateKeyToDb(type, "user_position_y"), gps_y);
                }
                if (responseobject.has("user_visibility_level")) {
                    int visibility = responseobject.optInt("user_visibility_level");
                    data.put(translateKeyToDb(type, "user_visibility_level"), String.valueOf(visibility));
                }
                if (responseobject.has("user_access_level")) {
                    int access = responseobject.optInt("user_access_level");
                    data.put(translateKeyToDb(type, "user_access_level"), String.valueOf(access));
                }
                if (responseobject.has("user_language")) {
                    int language = responseobject.optInt("user_language");
                    data.put(translateKeyToDb(type, "user_language"), String.valueOf(language));
                }
                if (responseobject.has("user_language_iso_639_3")) {
                    String user_language_iso_639_3 = responseobject.getString("user_language_iso_639_3");
                    data.put(translateKeyToDb(type, "user_language_iso_639_3"), user_language_iso_639_3);
                }
                if (responseobject.has("visibility_level")) {
                    int visibility_level = responseobject.optInt("visibility_level");
                    data.put(translateKeyToDb(type, "user_visibility_level"), String.valueOf(visibility_level));
                }
                if (responseobject.has("access_level")) {
                    int access_level = responseobject.optInt("access_level");
                    data.put(translateKeyToDb(type, "user_access_level"), String.valueOf(access_level));
                }
                if (responseobject.has("logged_user_user")) {
                    int logged_user_user = responseobject.optInt("logged_user_user");
                    data.put(translateKeyToDb(type, "logged_user_user"), String.valueOf(logged_user_user));
                }
                if (responseobject.has("user_logged_user")) {
                    int user_logged_user = responseobject.optInt("user_logged_user");
                    data.put(translateKeyToDb(type, "user_logged_user"), String.valueOf(user_logged_user));
                }

                if (responseobject.has("now_online")) {
                    String now_online = responseobject.getString("now_online");
                    data.put(translateKeyToDb(type, "now_online"), now_online);
                }

            } else if (type.equals("group")) {
                if (responseobject.has("group_name")) {
                    String name = responseobject.getString("group_name");
                    data.put(translateKeyToDb(type, "group_name"), name);
                }
                if (responseobject.has("name")) {
                    String name = responseobject.getString("name");
                    data.put(translateKeyToDb(type, "group_name"), name);
                }
                if (responseobject.has("status")) {
                    String status = responseobject.getString("status");
                    data.put(translateKeyToDb(type, "status"), status);
                }
                if (responseobject.has("group_description")) {
                    String description = responseobject.getString("group_description");
                    data.put(translateKeyToDb(type, "group_description"), description);
                }
                if (responseobject.has("group_position_x")) {
                    String gps_x = responseobject.getString("group_position_x");
                    data.put(translateKeyToDb(type, "group_position_x"), gps_x);
                }
                if (responseobject.has("group_position_y")) {
                    String gps_y = responseobject.getString("group_position_y");
                    data.put(translateKeyToDb(type, "group_position_y"), gps_y);
                }
                if (responseobject.has("group_visibility_level")) {
                    int visibility = responseobject.optInt("group_visibility_level");
                    data.put(translateKeyToDb(type, "group_visibility_level"), String.valueOf(visibility));
                }
                if (responseobject.has("visibility_level")) {
                    int visibility = responseobject.optInt("visibility_level");
                    data.put(translateKeyToDb(type, "group_visibility_level"), String.valueOf(visibility));
                }
                if (responseobject.has("group_access_level")) {
                    int access = responseobject.optInt("group_access_level");
                    data.put(translateKeyToDb(type, "group_access_level"), String.valueOf(access));
                }
                if (responseobject.has("access_level")) {
                    int access = responseobject.optInt("access_level");
                    data.put(translateKeyToDb(type, "group_access_level"), String.valueOf(access));
                }
                if (responseobject.has("group_language")) {
                    int language = responseobject.optInt("group_language");
                    data.put(translateKeyToDb(type, "group_language"), String.valueOf(language));
                }
                if (responseobject.has("group_language_iso_639_3")) {
                    String group_language_iso_639_3 = responseobject.getString("group_language_iso_639_3");
                    data.put(translateKeyToDb(type, "group_language_iso_639_3"), group_language_iso_639_3);
                }
                if (responseobject.has("logged_user_member")) {
                    int user_object_status = responseobject.getInt("logged_user_member");
                    data.put(translateKeyToDb(type, "user_object_status"), String.valueOf(user_object_status));
                }

                if (responseobject.has("group_portrait")) {
                    String image = responseobject.getString("group_portrait");
                    data.put(translateKeyToDb(type, "image"), image);
                }
            } else if (type.equals("resource")) {
                if (responseobject.has("resource_name")) {
                    String name = responseobject.getString("resource_name");
                    data.put(translateKeyToDb(type, "resource_name"), name);
                }
                if (responseobject.has("name")) {
                    String name = responseobject.getString("name");
                    data.put(translateKeyToDb(type, "resource_name"), name);
                }
                if (responseobject.has("status")) {
                    String status = responseobject.getString("status");
                    data.put(translateKeyToDb(type, "status"), status);
                }
                if (responseobject.has("resource_description")) {
                    String description = responseobject.getString("resource_description");
                    data.put(translateKeyToDb(type, "resource_description"), description);
                }

                if (responseobject.has("resource_position_x")) {
                    String gps_x = responseobject.getString("resource_position_x");
                    data.put(translateKeyToDb(type, "resource_position_x"), gps_x);
                }

                if (responseobject.has("resource_position_y")) {
                    String gps_y = responseobject.getString("resource_position_y");
                    data.put(translateKeyToDb(type, "resource_position_y"), gps_y);
                }
                int visibility = 1;
                if (!responseobject.isNull("resource_visibility_level")) {
                    visibility = responseobject.optInt("resource_visibility_level");
                }
                data.put(translateKeyToDb(type, "resource_visibility_level"), String.valueOf(visibility));


                if (responseobject.has("resource_access_level")) {
                    int access = 0;
                    if (!responseobject.isNull("resource_access_level")) {

                        access = responseobject.optInt("resource_access_level");
                    }
                    data.put(translateKeyToDb(type, "resource_access_level"), String.valueOf(access));


                }

                if (responseobject.has("resource_language")) {
                    int language = responseobject.optInt("resource_language");
                    data.put(translateKeyToDb(type, "resource_language"), String.valueOf(language));
                }
                if (responseobject.has("resource_language_iso_639_3")) {
                    String resource_language_iso_639_3 = responseobject.getString("resource_language_iso_639_3");
                    data.put(translateKeyToDb(type, "resource_language_iso_639_3"), resource_language_iso_639_3);
                }

                int subtype = 0;
                if (!responseobject.isNull("resource_type")) {
                    subtype = responseobject.optInt("resource_type");
                }
                if (!responseobject.isNull("message_type")) {
                    subtype = responseobject.optInt("message_type");
                }
                data.put(translateKeyToDb(type, "resource_type"), String.valueOf(subtype));


                int status = 1;
                if (!responseobject.isNull("resource_status")) {
                    status = responseobject.optInt("resource_status");
                }
                data.put(translateKeyToDb(type, "resource_status"), String.valueOf(status));


                int viewed = responseobject.optInt("resource_viewed");
                data.put(translateKeyToDb(type, "resource_viewed"), String.valueOf(viewed));

                int trash = 0;
                if (!responseobject.isNull("resource_trash")) {
                    trash = responseobject.optInt("resource_trash");
                }
                if (!responseobject.isNull("trashed")) {
                    trash = responseobject.optInt("trashed");
                }

                data.put(translateKeyToDb(type, "resource_trash"), String.valueOf(trash));

                if (!responseobject.isNull("viewed")) {

                    data.put(translateKeyToDb(type, "resource_viewed"), String.valueOf(responseobject.optInt("viewed")));
                }

                if (responseobject.has("media_type")) {
                    String media_type = responseobject.getString("media_type");
                    data.put(translateKeyToDb(type, "media_type"), media_type);
                }

                if (responseobject.has("message_text")) {
                    String extratext = responseobject.getString("message_text");
                    data.put(translateKeyToDb(type, "message_text"), extratext);


                    data.put(translateKeyToDb(type, "resource_data"), "{message_text:\"" + extratext + "\"}");
                }

                if (responseobject.has("resource_data")) {
                    JSONObject resource_data = responseobject.getJSONObject("resource_data");
                    if (resource_data.has("message_text")) {
                        String extratext = responseobject.getJSONObject("resource_data").getString("message_text");
                        data.put(translateKeyToDb(type, "message_text"), extratext);

                        data.put(translateKeyToDb(type, "resource_data"), "{message_text:" + JSONObject.quote(extratext) + "}");
                    }

                    String url;
                    if (!resource_data.isNull("event_url") && !resource_data.getString("event_url").equals("")) {
                        url = resource_data.getString("event_url");
                        data.put(translateKeyToDb(type, "url"), url);
                    }

                    if (!resource_data.isNull("organization_url") && !resource_data.getString("organization_url").equals("")) {
                        url = resource_data.getString("organization_url");
                        data.put(translateKeyToDb(type, "url"), url);
                    }

                    if (!resource_data.isNull("text_information_url") && !resource_data.getString("text_information_url").equals("")) {
                        url = resource_data.getString("text_information_url");
                        data.put(translateKeyToDb(type, "url"), url);
                    }

                    if (!resource_data.isNull("other_url") && !resource_data.getString("other_url").equals("")) {
                        url = resource_data.getString("other_url");
                        data.put(translateKeyToDb(type, "url"), url);
                    }

                    if (!resource_data.getJSONObject("resource_data").isNull("media_link") && !resource_data.getString("media_link").equals("")) {
                        url = resource_data.getString("media_link");
                        data.put(translateKeyToDb(type, "url"), url);
                    }
                }


                if (!responseobject.isNull("logged_user_member")) {
                    int user_object_status = responseobject.getInt("logged_user_member");
                    data.put(translateKeyToDb(type, "user_object_status"), String.valueOf(user_object_status));
                }

            }

            if (responseobject.has("tags")) {
                JSONArray object_tags = responseobject.getJSONArray("tags");

                data.put(translateKeyToDb(type, "tags"), object_tags.toString());
            }


            return data;

        } catch (JSONException e) {
            return null;
        }
    }

    public void clearTable() {
        synchronized (Lock) {
            this.db.delete(TABLE_NAME_OBJECTS, null, null);
            this.db.delete(TABLE_NAME_SETTINGS, null, null);

        }
    }

    private static class OpenDatabase extends SQLiteOpenHelper {
        private static String DB_PATH = "/data/data/net.mycitizen.mcn/databases/";
        Context ctx;

        public OpenDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.ctx = context;

            Log.d(Config.DEBUG_TAG, "OPEN DATABASE");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_OBJECTS + "(id TEXT PRIMARY KEY, type TEXT, subtype TEXT, language TEXT,title TEXT,title2 TEXT, title3 TEXT, description TEXT, description2 TEXT, email TEXT, gpsx TEXT, gpsy TEXT, image TEXT, tags TEXT, visibility TEXT, access TEXT, status TEXT, viewed TEXT, trashed TEXT, subdata TEXT, user_object_status TEXT, now_online TEXT, last_update TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SETTINGS + "(id TEXT PRIMARY KEY, key TEXT,value TEXT)");


            Log.d(Config.DEBUG_TAG, "Database created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_OBJECTS, null);
            int exists_now_online = cursor.getColumnIndex("now_online");
            if (exists_now_online < 0) {
                db.execSQL("ALTER TABLE " + TABLE_NAME_OBJECTS + " ADD COLUMN now_online TEXT;");
            }

            int exists_last_update = cursor.getColumnIndex("last_update");
            if (exists_last_update < 0) {
                db.execSQL("ALTER TABLE " + TABLE_NAME_OBJECTS + " ADD COLUMN last_update TEXT;");
            }
            Log.d(Config.DEBUG_TAG, "Database upgraded from version " + oldVersion + " to " + newVersion);

        }

        public boolean mainTableExists() {
            Log.d(Config.DEBUG_TAG, "MAIN TABLE EXISTS");
            String myPath = DB_PATH + DATABASE_NAME;
            SQLiteDatabase checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            String sql = "SELECT name FROM sqlite_master WHERE name='" + TABLE_NAME_OBJECTS + "'";
            Cursor c = checkDB.rawQuery(sql, null);

            boolean next = false;
            if (c.moveToNext()) {
                next = true;
            }
            checkDB.close();

            return next;

        }


        public void createDataBase() {

            try {

                copyDataBase();


            } catch (IOException e) {

                throw new Error("Error copying database");

            }


        }

        private void copyDataBase() throws IOException {
            Log.d(Config.DEBUG_TAG, "database recreating");
            //Open your local db as the input stream
            InputStream myInput = ctx.getAssets().open(DATABASE_NAME);

            // Path to the just created empty db
            String outFileName = DB_PATH + DATABASE_NAME;

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();

        }

    }

}


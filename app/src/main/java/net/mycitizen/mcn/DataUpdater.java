package net.mycitizen.mcn;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;


public class DataUpdater extends Service {
    private final int TIMEOUT_CONNECTION = 5000;//5sec
    private final int TIMEOUT_SOCKET = 15000;//15sec

    long total_length = 0;
    int file_total_count = 0;
    int file_count = 0;

    ArrayList<String> image_urls = new ArrayList<String>();

    String mdialog_download_name = "";
    long mdialog_download_progress = 0;

    WakeLock wakeLock = null;
    //NotificationManager mNotificationManager;
    //Notification notification;

    //final int HELLO_ID = 888666;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(Config.DEBUG_TAG, "DataUpdater start");
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyWakeLock");

        wakeLock.acquire();

        updateData();


    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    private void onPause() {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void updateData() {
        Intent i = new Intent();
        i.putExtra("type", "start");
        i.setAction("net.mycitizen.mcn.DATA_UPDATE");
        sendBroadcast(i);

        Config cfg = new Config(DataUpdater.this.getApplicationContext());

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(cfg.getApiUrl());

        try {


            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);

            JSONArray response = new JSONArray(responseBody);
            Log.d(Config.DEBUG_TAG, "updating data response length" + response.length());
            // Build the return string.

	        /*
            if(response.length() > 0) {
	        	DataHandler db = new DataHandler(this);
				db.clearTable();
				db.close();
	        }
	        */

            for (int i1 = 0; i1 < response.length(); i1++) {
                JSONObject response_object = response.getJSONObject(i1);

                int object_id = response_object.getInt("id");
                String object_type = response_object.getString("type");

                String language = response_object.getString("language");
                String title = response_object.getString("title");
                String description_1 = response_object.getString("description_simple");
                String description_2 = response_object.getString("description_extended");

                JSONObject position = response_object.getJSONObject("position");

                int lat = (int) (Float.valueOf(position.getString("lat")) * 1000000);
                int lng = (int) (Float.valueOf(position.getString("lng")) * 1000000);

                JSONArray tags = response_object.getJSONArray("tags");

                ArrayList<TagObject> tag_list = new ArrayList<TagObject>();

                for (int im = 0; im < tags.length(); im++) {
                    JSONObject tag = tags.getJSONObject(im);

                    String tag_name = tag.getString("title");
                    int tag_parent_id;
                    int tag_id = tag.getInt("id");

                    TagObject tag_to_add = new TagObject(tag_id, tag_name);

                    if (tag.has("tag_parent_id")) {
                        tag_parent_id = tag.getInt("tag_parent_id");
                        tag_to_add.setTag_parent_id(tag_parent_id);
                    }

                    tag_list.add(tag_to_add);
                }

                LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

                DataHandler db = new DataHandler(this);
                LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
                params.put("id", String.valueOf(object_id));
                params.put("type", object_type);

                if (db.objectExists(params)) {
                    db.updateObject(object_type, object_id, data);
                } else {
                    db.insertObject(object_type, data);
                }
                db.close();


            }

            new Thread(new Runnable() {

                @Override
                public void run() {
                    Iterator<String> iterator = image_urls.iterator();

                    while (iterator.hasNext()) {
                        String url = iterator.next();
                        downloadImage(url);
                    }

                    Intent i3 = new Intent();
                    i3.putExtra("type", "finish");
                    i3.setAction("net.mycitizen.mcn.DATA_UPDATE");
                    sendBroadcast(i3);


                    //refresh();
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                    DataUpdater.this.stopSelf();

                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
            Intent i3 = new Intent();
            i3.putExtra("type", "finish");
            i3.setAction("net.mycitizen.mcn.DATA_UPDATE");
            sendBroadcast(i3);


            if (wakeLock.isHeld()) {
                wakeLock.release();
            }

            DataUpdater.this.stopSelf();
        }


    }

    public boolean createEmptyFile(String name) {
        File sdDir = Environment.getExternalStorageDirectory();
        File pictureFileDir = new File(sdDir, "myCitizen");

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            return false;
        }

        String filename = pictureFileDir.getPath() + File.separator + name;

        File file_exists = new File(filename);
        try {
            return file_exists.createNewFile();
        } catch (IOException e) {
            return false;
        }


    }

    public boolean downloadImage(String name) {

        InputStream is = null;

        String photoFile = name;

        File sdDir = Environment.getExternalStorageDirectory();
        File pictureFileDir = new File(sdDir, "myCitizen");

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            return false;
        }

        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        file_count++;
        File file_exists = new File(filename);

        Config cfg = new Config(DataUpdater.this.getApplicationContext());

        String path = cfg.getApiUrl() + "images/" + name;
        mdialog_download_name = name;
        HttpURLConnection ucon = null;
        try {
            URL image_url = new URL(path);
            ucon = (HttpURLConnection) image_url.openConnection();

            ucon.connect();


            long lenghtOfFile = ucon.getContentLength();

            if (file_exists.exists() && file_exists.length() == lenghtOfFile) {
                return true;
            }

            File pictureFile = new File(filename);


            BufferedInputStream inStream = new BufferedInputStream(image_url.openStream(), 1024 * 5);
            FileOutputStream outStream = new FileOutputStream(pictureFile);
            byte[] buff = new byte[5 * 1024];

            //Read bytes (and store them) until there is nothing more to read(-1)
            int len;
            mdialog_download_progress = 0;
            while ((len = inStream.read(buff)) != -1) {

                mdialog_download_progress += len;
                outStream.write(buff, 0, len);
                Intent i = new Intent();
                i.putExtra("type", "progress");
                i.putExtra("file_count", file_count);
                i.putExtra("file_total_count", file_total_count);
                i.putExtra("download_name", mdialog_download_name);
                int percentage = (int) (((float) (mdialog_download_progress) / (float) (lenghtOfFile)) * 100);

                i.putExtra("percentage", percentage);
                i.setAction("net.mycitizen.mcn.DATA_UPDATE");
                sendBroadcast(i);

            }

            //clean up
            outStream.flush();
            outStream.close();
            inStream.close();
            ucon.disconnect();

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Log.d(Config.DEBUG_TAG, "skipping " + name);
            if (ucon != null) {
                ucon.disconnect();
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            if (ucon != null) {
                ucon.disconnect();
            }
            return false;
        }

        return true;
    }


}


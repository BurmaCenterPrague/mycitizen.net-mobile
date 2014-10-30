package net.mycitizen.mcn;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class ProfileMainActivity extends ActionBarActivity implements OnTouchListener, LocationListener {
    EditText profile_name;
    EditText profile_surname;
    EditText profile_email;
    EditText profile_phone;
    EditText profile_url;
    Spinner profile_notification_timer;
    EditText profile_description;
    Spinner profile_language;
    RadioGroup profile_visibility;
    String original_email;
    boolean inEditMode = false;
    AlertDialog dialog;
    ProgressDialog loader = null;

    UserObject myProfile;

    int logged_user_id;

    ImageView profile_portrait_image;

    private static final String TAG = "CallCamera";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
    private static final int SELECT_IMAGE_ACTIVITY_REQ = 1;

    Uri fileUri = null;
    ImageView photoImage = null;

    String selectedImagePath;

    ArrayList<DataObject> tags;
    ListView taglist;

    LocationManager locationManager;
    double latitude, longitude;
    boolean language_changed = false;
    MapView mapView;

    IMapController mc;
    Projection p;

    MapOverlay overlay = null;

    GeoPoint mapCenter;
    GeoPoint gpsCenter;

    ToggleButton profile_menu_main, profile_menu_description, profile_menu_portrait, profile_menu_tags, profile_menu_location;

    ApiConnector api;

    LinkedHashMap<String, String> supported_lng;
    LinkedHashMap<String, String> supported_tags;
    LinkedHashMap<Integer, String> supported_timers;

    ArrayList<DataObject> items;

    ToggleButton map_edit;

    ActionBar actionBar;
    Bitmap myBitmap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_title_profile);
        actionBar.setIcon(null);
        api = new ApiConnector(this);

        SharedPreferences settings = ProfileMainActivity.this.getSharedPreferences(Config.localStorageName, 0);

        logged_user_id = settings.getInt("logged_user_id", 0);

        loader = loadingDialog();
        DashboardInit task = new DashboardInit();
        task.execute(new String[]{"user", String.valueOf(logged_user_id)});

        profile_name = (EditText) findViewById(R.id.profile_name);
        profile_surname = (EditText) findViewById(R.id.profile_surname);
        profile_email = (EditText) findViewById(R.id.profile_email);
        profile_phone = (EditText) findViewById(R.id.profile_phone);
        profile_url = (EditText) findViewById(R.id.profile_url);
        profile_notification_timer = (Spinner) findViewById(R.id.profile_notification_timer);

        profile_language = (Spinner) findViewById(R.id.profile_api);

        profile_visibility = (RadioGroup) findViewById(R.id.profile_visibility);

        profile_description = (EditText) findViewById(R.id.profile_description);

        profile_portrait_image = (ImageView) findViewById(R.id.profile_portrait_image);


        taglist = (ListView) findViewById(R.id.profile_tag_list);

        mapView = (MapView) findViewById(R.id.mapview);
        //mapView.setUseDataConnection(false);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);

        //mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);


        mapView.setOnTouchListener(this);


        profile_menu_main = (ToggleButton) findViewById(R.id.profile_menu_main);
        profile_menu_description = (ToggleButton) findViewById(R.id.profile_menu_description);
        profile_menu_portrait = (ToggleButton) findViewById(R.id.profile_menu_portrait);
        profile_menu_tags = (ToggleButton) findViewById(R.id.profile_menu_tags);
        profile_menu_location = (ToggleButton) findViewById(R.id.profile_menu_map);

        profile_menu_main.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout main_tab = (LinearLayout) findViewById(R.id.profile_tab_main);
                LinearLayout description_tab = (LinearLayout) findViewById(R.id.profile_tab_description);
                RelativeLayout portrait_tab = (RelativeLayout) findViewById(R.id.profile_tab_portrait);
                LinearLayout tags_tab = (LinearLayout) findViewById(R.id.profile_tab_tags);
                LinearLayout map_tab = (LinearLayout) findViewById(R.id.profile_tab_map);

                main_tab.setVisibility(View.VISIBLE);
                description_tab.setVisibility(View.GONE);
                portrait_tab.setVisibility(View.GONE);
                tags_tab.setVisibility(View.GONE);
                map_tab.setVisibility(View.GONE);

                profile_menu_main.setChecked(true);
                profile_menu_description.setChecked(false);
                profile_menu_portrait.setChecked(false);
                profile_menu_tags.setChecked(false);
                profile_menu_location.setChecked(false);
            }
        });


        profile_menu_description.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout main_tab = (LinearLayout) findViewById(R.id.profile_tab_main);
                LinearLayout description_tab = (LinearLayout) findViewById(R.id.profile_tab_description);
                RelativeLayout portrait_tab = (RelativeLayout) findViewById(R.id.profile_tab_portrait);
                LinearLayout tags_tab = (LinearLayout) findViewById(R.id.profile_tab_tags);
                LinearLayout map_tab = (LinearLayout) findViewById(R.id.profile_tab_map);

                main_tab.setVisibility(View.GONE);
                description_tab.setVisibility(View.VISIBLE);
                portrait_tab.setVisibility(View.GONE);
                tags_tab.setVisibility(View.GONE);
                map_tab.setVisibility(View.GONE);

                profile_menu_main.setChecked(false);
                profile_menu_description.setChecked(true);
                profile_menu_portrait.setChecked(false);
                profile_menu_tags.setChecked(false);
                profile_menu_location.setChecked(false);
            }
        });


        profile_menu_portrait.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout main_tab = (LinearLayout) findViewById(R.id.profile_tab_main);
                LinearLayout description_tab = (LinearLayout) findViewById(R.id.profile_tab_description);
                RelativeLayout portrait_tab = (RelativeLayout) findViewById(R.id.profile_tab_portrait);
                LinearLayout tags_tab = (LinearLayout) findViewById(R.id.profile_tab_tags);
                LinearLayout map_tab = (LinearLayout) findViewById(R.id.profile_tab_map);

                main_tab.setVisibility(View.GONE);
                description_tab.setVisibility(View.GONE);
                portrait_tab.setVisibility(View.VISIBLE);
                tags_tab.setVisibility(View.GONE);
                map_tab.setVisibility(View.GONE);

                profile_menu_main.setChecked(false);
                profile_menu_description.setChecked(false);
                profile_menu_portrait.setChecked(true);
                profile_menu_tags.setChecked(false);
                profile_menu_location.setChecked(false);
            }
        });

        profile_menu_tags.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout main_tab = (LinearLayout) findViewById(R.id.profile_tab_main);
                LinearLayout description_tab = (LinearLayout) findViewById(R.id.profile_tab_description);
                RelativeLayout portrait_tab = (RelativeLayout) findViewById(R.id.profile_tab_portrait);
                LinearLayout tags_tab = (LinearLayout) findViewById(R.id.profile_tab_tags);
                LinearLayout map_tab = (LinearLayout) findViewById(R.id.profile_tab_map);

                main_tab.setVisibility(View.GONE);
                description_tab.setVisibility(View.GONE);
                portrait_tab.setVisibility(View.GONE);
                tags_tab.setVisibility(View.VISIBLE);
                map_tab.setVisibility(View.GONE);

                profile_menu_main.setChecked(false);
                profile_menu_description.setChecked(false);
                profile_menu_portrait.setChecked(false);
                profile_menu_tags.setChecked(true);
                profile_menu_location.setChecked(false);
            }
        });

        profile_menu_location.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout main_tab = (LinearLayout) findViewById(R.id.profile_tab_main);
                LinearLayout description_tab = (LinearLayout) findViewById(R.id.profile_tab_description);
                RelativeLayout portrait_tab = (RelativeLayout) findViewById(R.id.profile_tab_portrait);
                LinearLayout tags_tab = (LinearLayout) findViewById(R.id.profile_tab_tags);
                LinearLayout map_tab = (LinearLayout) findViewById(R.id.profile_tab_map);

                main_tab.setVisibility(View.GONE);
                description_tab.setVisibility(View.GONE);
                portrait_tab.setVisibility(View.GONE);
                tags_tab.setVisibility(View.GONE);
                map_tab.setVisibility(View.VISIBLE);

                profile_menu_main.setChecked(false);
                profile_menu_description.setChecked(false);
                profile_menu_portrait.setChecked(false);
                profile_menu_tags.setChecked(false);
                profile_menu_location.setChecked(true);
            }
        });

        Button profile_portrait_camera = (Button) findViewById(R.id.profile_portrait_camera);
        profile_portrait_camera.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = getOutputPhotoFile();
                fileUri = Uri.fromFile(file);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);
            }
        });

        Button profile_portrait_gallery = (Button) findViewById(R.id.profile_portrait_gallery);
        profile_portrait_gallery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_IMAGE_ACTIVITY_REQ);
            }
        });

        Button profile_portrait_remove = (Button) findViewById(R.id.profile_portrait_remove);
        profile_portrait_remove.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                profile_portrait_image.setImageDrawable(null);
            }
        });
        /*
        Button profile_portrait_save = (Button) findViewById(R.id.profile_portrait_save);
        profile_portrait_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
								profile_portrait_image.getDrawable();
			}
		});
        */

        Button my_position = (Button) findViewById(R.id.map_my_location);
        my_position.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mapCenter != null) {
                    mc.setCenter(mapCenter);
                }
            }
        });

        Button gps_position = (Button) findViewById(R.id.map_gps_location);
        gps_position.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (latitude != 0 && longitude != 0) {
                    mc.setCenter(new GeoPoint(latitude, longitude));
                }
                /*
                if(mapCenter !=  null) {
					mc.setCenter(mapCenter);
				}
				*/
            }
        });

        map_edit = (ToggleButton) findViewById(R.id.map_edit_location);
        map_edit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO system zadavani bodu na mapu
                if (map_edit.isChecked()) {
                    inEditMode = true;
                } else {
                    inEditMode = false;
                }
            }
        });

        Button gps_remove = (Button) findViewById(R.id.map_remove_location);
        gps_remove.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO system zadavani bodu na mapu
                overlay.updateMyPosition((GeoPoint) p.fromPixels(0, 0));
                mapView.invalidate();
            }
        });

        Button zoomin = (Button) findViewById(R.id.zoomin);
        zoomin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mc.zoomIn();
            }
        });

        Button zoomout = (Button) findViewById(R.id.zoomout);
        zoomout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mc.zoomOut();
            }
        });
         
         /*
        profile_language.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                    	language_changed = true;
                    	AlertDialog.Builder builder = new AlertDialog.Builder(ProfileMainActivity.this);
	           	        builder.setMessage(getString(R.string.language_quit))
	           	               .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
	           	                   public void onClick(DialogInterface dialog, int id) {
	           	                       // FIRE ZE MISSILES!
	           	                	   dialog.cancel();
	           	                	  // System.exit(0);
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

                    public void onNothingSelected(AdapterView<?> parent) {
                        
                    }
                }); 
        */

    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class Tager extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(ProfileMainActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {
                boolean status = false;
                if (Boolean.valueOf(type[1])) {
                    status = true;
                }
                System.out.println(status);
                result = api.changeProfileTag(Integer.valueOf(type[0]), status);
            }

            if (result) {

                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {

            } else {

            }
            loader.dismiss();
        }
    }

    private class DashboardInit extends AsyncTask<String, Void, DataObject> {
        @Override
        protected DataObject doInBackground(String... urls) {

            String[] type = urls;


            ApiConnector api = new ApiConnector(ProfileMainActivity.this);
            DataObject object = null;
            if (api.sessionInitiated()) {
                supported_lng = api.getSupportedLanguages();
                supported_tags = api.getSupportedTags();
                supported_timers = api.getSupportedTimers();

                myProfile = (UserObject) api.getDetail(type[0], Integer.valueOf(type[1]));
            }
            return myProfile;
        }

        @Override
        protected void onPostExecute(DataObject result) {
            EditText profile_name = (EditText) findViewById(R.id.profile_name);
            EditText profile_surname = (EditText) findViewById(R.id.profile_surname);
            EditText profile_email = (EditText) findViewById(R.id.profile_email);
            RadioGroup profile_visibility = (RadioGroup) findViewById(R.id.profile_visibility);

            profile_name.setText(((UserObject) result).getFirstName());
            profile_surname.setText(((UserObject) result).getLastName());
            profile_email.setText(((UserObject) result).getEmail());
            profile_phone.setText(((UserObject) result).getPhone());
            System.out.println("KOZA " + ((UserObject) result).getUrl());
            profile_url.setText(((UserObject) result).getUrl());

            original_email = ((UserObject) result).getEmail();
            switch (((UserObject) result).getVisibility()) {
                case 1:
                    profile_visibility.check(R.id.profile_visibility_world);

                    break;
                case 2:
                    profile_visibility.check(R.id.profile_visibility_member);

                    break;
                case 3:
                    profile_visibility.check(R.id.profile_visibility_private);

                    break;
            }

            profile_portrait_image.setImageBitmap(((UserObject) result).getIconBitmap());


            profile_description.setText(((UserObject) result).getDescription());

            if (supported_lng != null) {
                ArrayList<String> items = new ArrayList<String>();
                Iterator<Entry<String, String>> it = supported_lng.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                    //pairs.getKey();
                    items.add(pairs.getValue().toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfileMainActivity.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                profile_language.setAdapter(adapter);

                SharedPreferences settings = ProfileMainActivity.this.getSharedPreferences(Config.localStorageName, 0);
                System.out.println("SAKRA " + settings.getString("logged_user_language", "English"));
                int lngPosition = adapter.getPosition(settings.getString("logged_user_language", "English"));
                if (lngPosition >= 0) {
                    profile_language.setSelection(lngPosition);
                }

            }

            if (supported_timers != null) {
                String saved_timer = ((UserObject) result).getNotificationTimer();

                String saved_text = "";
                ArrayList<String> items = new ArrayList<String>();
                Iterator<Entry<Integer, String>> it = supported_timers.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    System.out.println("TIMER " + pairs.getKey().toString() + " " + saved_timer);
                    if (pairs.getKey().toString().equals(saved_timer)) {
                        saved_text = pairs.getValue().toString();
                        System.out.println("TIMER HERE");
                    }
                    items.add(pairs.getValue().toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfileMainActivity.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                profile_notification_timer.setAdapter(adapter);

                if (saved_text != null) {
                    int lngPosition = adapter.getPosition(saved_text);
                    if (lngPosition >= 0) {
                        profile_notification_timer.setSelection(lngPosition);
                    }
                }

            }

            ArrayList<DataObject> my_tags = ((UserObject) result).getTags();


            if (supported_tags != null) {
                items = new ArrayList<DataObject>();
                Iterator<Entry<String, String>> it = supported_tags.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                    //pairs.getKey();
                    TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());
                    Iterator<DataObject> iter = my_tags.iterator();
                    while (iter.hasNext()) {
                        TagObject c = (TagObject) iter.next();
                        if (c.getObjectId() == current_item.getObjectId()) {
                            System.out.println(c.getStatus());
                            current_item.setStatus(true);
                            break;
                        }
                    }

                    items.add(current_item);

                }
                ObjectListItemAdapter tad = new ObjectListItemAdapter(ProfileMainActivity.this, "profile_tag", items);
                tad.setOnAdapterActionListener(new OnAdapterActionListener() {

                    @Override
                    public void onUntrashMessageAction(int message_id) {

                    }

                    @Override
                    public void onTrashMessageAction(int message_id) {

                    }

                    @Override
                    public void onDeclineMessageAction(int message_id, int sender_id) {

                    }

                    @Override
                    public void onChangeTag(int tag_id, boolean status) {
                        //ApiConnector api = new ApiConnector(ProfileMainActivity.this);
                        //api.changeProfileTag(tag_id, status);

                        Tager task = new Tager();
                        task.execute(new String[]{String.valueOf(tag_id), String.valueOf(status)});
                    }

                    @Override
                    public void onAcceptMessageAction(int message_id, int sender_id) {

                    }
                });
                taglist.setAdapter(tad);


            }


            mapCenter = ((UserObject) result).getPosition();

            overlay = new MapOverlay(getApplicationContext(), mapView.getWidth(), mapView.getHeight(), mapCenter, new GeoPoint(latitude, longitude));

            mc = mapView.getController();
            if (mapCenter == null) {
                mapCenter = new GeoPoint(15.5454, 50.3244);
            }
            mc.setCenter(mapCenter);

            overlay.updateCenter(mapCenter);

            mc.setZoom(15);

            p = mapView.getProjection();


            List<Overlay> o = mapView.getOverlays();
            o.add(overlay);

            mc.setCenter(mapCenter);

            loader.dismiss();
        }
    }

    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                System.out.println("FAILED CREATE DIR");
                return null;
            }
        }
        System.out.println(directory.getPath() + File.separator + "IMG_"
                + ".jpg");
        return new File(directory.getPath() + File.separator + "IMG_"
                + ".jpg");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = null;
                if (data == null) {
                    // A known bug here! The image should have saved in fileUri

                    photoUri = fileUri;

                } else {
                    photoUri = data.getData();
                    Toast.makeText(this, "Image saved successfully in: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8; // might try 8 also
                //options.inJustDecodeBounds = true;

                String file = new File(photoUri.getPath()).getAbsolutePath();

                myBitmap = BitmapFactory.decodeFile(file, options);
                if (myBitmap != null) {
                    ExifInterface exif;
                    int rotationAngle = 0;
                    try {
                        exif = new ExifInterface(file);

                        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                            rotationAngle = 180;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                            rotationAngle = 270;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Matrix matrix = new Matrix();
                    matrix.setRotate(rotationAngle, (float) myBitmap.getWidth() / 2, (float) myBitmap.getHeight() / 2);
                    myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);

                    myBitmap = Bitmap.createScaledBitmap(myBitmap, 160, 200, true);

                    profile_portrait_image.setImageBitmap(myBitmap);
                } else {
                    Toast.makeText(this, "Photo couldn't be loaded properly", Toast.LENGTH_SHORT).show();
                }
                // showPhoto(photoUri);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Callout for image capture failed!",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == SELECT_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8; // might try 8 also


                myBitmap = BitmapFactory.decodeFile(selectedImagePath, options);

                if (myBitmap != null) {

                    ExifInterface exif;
                    int rotationAngle = 0;
                    try {
                        exif = new ExifInterface(selectedImagePath);

                        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                            rotationAngle = 180;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                            rotationAngle = 270;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Matrix matrix = new Matrix();
                    matrix.setRotate(rotationAngle, (float) myBitmap.getWidth() / 2, (float) myBitmap.getHeight() / 2);


                    myBitmap = Bitmap.createScaledBitmap(myBitmap, 160, 200, true);

                    // String encodedstring = ApiConnector.encodeTobase64(myBitmap);


                    //myBitmap = ApiConnector.decodeBase64(encodedstring);

                    profile_portrait_image.setImageBitmap(myBitmap);
                } else {
                    Toast.makeText(this, "Photo couldn't be loaded properly", Toast.LENGTH_SHORT).show();
                }
                // showPhoto(photoUri);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Callout for image capture failed!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (profile_menu_location.isChecked() && inEditMode) {

                    //GeoPoint gp = p.fromPixels((int)x, (int)y);

                    Projection p = mapView.getProjection();

                    overlay.updateMyPosition((GeoPoint) p.fromPixels(x, y));
                    mapCenter = (GeoPoint) p.fromPixels(x, y);
                    mapView.invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:


                break;
            case MotionEvent.ACTION_DOWN:


                //GeoPoint gp = p.fromPixels((int)x, (int)y);
                //mc.setCenter(gp);
                //mc.zoomIn();

                break;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            //location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);

        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            //location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);

        }
    }

    @Override
    protected void onPause() {
        super.onStop();
        SaveProfile task = new SaveProfile();
        task.execute(new String[]{""});
    }

    private class SaveProfile extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;
            SharedPreferences settings = getSharedPreferences(Config.localStorageName, 0);
            SharedPreferences.Editor editor = settings.edit();
            ApiConnector api = new ApiConnector(ProfileMainActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {

                int visibility = 1;
                if (profile_visibility.getCheckedRadioButtonId() == R.id.profile_visibility_private) {
                    visibility = 3;
                    editor.putString("logged_user_visibility", "private");
                } else if (profile_visibility.getCheckedRadioButtonId() == R.id.profile_visibility_member) {
                    visibility = 2;
                    editor.putString("logged_user_visibility", "members");
                } else if (profile_visibility.getCheckedRadioButtonId() == R.id.profile_visibility_world) {
                    visibility = 1;
                    editor.putString("logged_user_visibility", "world");
                }
                editor.commit();


                //System.out.println(ApiConnector.encodeTobase64(myBitmap));

                String encodedImage = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (myBitmap != null) {

                    encodedImage = ApiConnector.encodeTobase64(myBitmap);
                }

                int profile_notification_selected_timer = 0;

                if (supported_timers != null) {

                    String saved_text = profile_notification_timer.getSelectedItem().toString();

                    Iterator<Entry<Integer, String>> it = supported_timers.entrySet().iterator();
                    while (it.hasNext()) {
                        HashMap.Entry pairs = (HashMap.Entry) it.next();
                        System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                        if (pairs.getValue().equals(saved_text)) {
                            profile_notification_selected_timer = Integer.valueOf(pairs.getKey().toString());
                        }

                    }


                }
                // String lng = "eng";

                String languageCode = api.translateLanguageNameToCode(profile_language.getSelectedItem().toString());

                editor.putString("logged_user_language", profile_language.getSelectedItem().toString());
                // Don't save a saved_lng - that is for UI only!
                editor.putString("ui_language", languageCode);

                /*
                if (profile_language.getSelectedItem().toString().equals("Čeština")) {
                    lng = "ces";

                    editor.putString("logged_user_language", "Čeština");

                    editor.putString("saved_lng", "ces");
                    Locale locale = new Locale("cs_MM");

                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);

                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                } else if (profile_language.getSelectedItem().toString().equals("Matu")) {
                    lng = "hlt";

                    editor.putString("logged_user_language", "Matu");
                    editor.putString("saved_lng", "hlt");
                    Locale locale = new Locale("ht_MM");

                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);

                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());
                } else if (profile_language.getSelectedItem().toString().equals("Mara")) {
                    lng = "mrh";

                    editor.putString("logged_user_language", "Mara");
                    editor.putString("saved_lng", "mrh");
                    Locale locale = new Locale("mt_MM");

                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);

                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());
                } else if (profile_language.getSelectedItem().toString().equals("Zolai")) {
                    lng = "zom";

                    editor.putString("logged_user_language", "Zolai");
                    editor.putString("saved_lng", "zom");
                    Locale locale = new Locale("zm_MM");

                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);

                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());
                } else {
                    lng = "eng";
                    editor.putString("logged_user_language", "English");
                    editor.putString("saved_lng", "eng");

                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);

                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());
                }
                */
                editor.commit();
                result = api.changeProfile(profile_name.getText().toString(), profile_surname.getText().toString(), profile_email.getText().toString(), profile_phone.getText().toString(), profile_url.getText().toString(), String.valueOf(profile_notification_selected_timer), profile_description.getText().toString(), String.valueOf(visibility), String.valueOf(mapCenter.getLatitudeE6()), String.valueOf(mapCenter.getLongitudeE6()), encodedImage, languageCode);

            }


            if (result) {

                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {
                if (!original_email.equals(profile_email.getText().toString())) {
                    Toast.makeText(ProfileMainActivity.this, getString(R.string.profile_saved_email_changed) + " " + getString(R.string.language_quit), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ProfileMainActivity.this, getString(R.string.profile_saved) + " " + getString(R.string.language_quit), Toast.LENGTH_LONG).show();
                }


            } else {
                Toast.makeText(ProfileMainActivity.this, getString(R.string.profile_not_saved), Toast.LENGTH_LONG).show();
            }
            //loader.dismiss();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        gpsCenter = new GeoPoint(latitude, longitude);

        if (overlay != null && mapView != null) {
            overlay.updateMyLocation(new GeoPoint(latitude, longitude));
            mapView.invalidate();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


}

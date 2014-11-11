package net.mycitizen.mcn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;


public class CreateGroupActivity extends ActionBarActivity implements OnTouchListener, LocationListener {

    EditText creategroup_name;
    EditText creategroup_description;

    RadioGroup creategroup_visibility;
    CheckBox open_group;

    String activeTab = "main";

    ProgressDialog loader = null;

    ArrayList<DataObject> tags;
    ListView taglist;

    LocationManager locationManager;
    double latitude, longitude;

    MapView mapView;

    IMapController mc;
    Projection p;

    MapOverlay overlay = null;

    GeoPoint mapCenter;

    ToggleButton creategroup_menu_tags, creategroup_menu_location;
    Button creategroup_menu_submit;

    ApiConnector api;

    LinkedHashMap<String, String> supported_lng;
    LinkedHashMap<String, String> supported_tags;

    ArrayList<DataObject> items;

    ToggleButton map_edit;

    ArrayList<Integer> selected_tags;

    Spinner creategroup_language;

    ActionBar actionBar;
    boolean inEditMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creategroup_view);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_title_create_group);
        actionBar.setIcon(null);
        api = new ApiConnector(this);

        selected_tags = new ArrayList<Integer>();

        SharedPreferences settings = CreateGroupActivity.this.getSharedPreferences(Config.localStorageName, 0);

        if (latitude == 0 || longitude == 0) {
            latitude = Double.valueOf(settings.getString("gps_default_latitude", "0"));
            longitude = Double.valueOf(settings.getString("gps_default_longitude", "0"));
        }

        loader = loadingDialog();
        DashboardInit task = new DashboardInit();
        task.execute();

        creategroup_name = (EditText) findViewById(R.id.creategroup_name);


        creategroup_visibility = (RadioGroup) findViewById(R.id.creategroup_visibility);

        creategroup_description = (EditText) findViewById(R.id.creategroup_description);

        taglist = (ListView) findViewById(R.id.creategroup_tag_list);

        mapView = (MapView) findViewById(R.id.mapview);
        //mapView.setUseDataConnection(false);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);

        //mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);


        mapView.setOnTouchListener(this);

        creategroup_language = (Spinner) findViewById(R.id.create_group_lng);


        creategroup_menu_tags = (ToggleButton) findViewById(R.id.creategroup_tags);
        creategroup_menu_location = (ToggleButton) findViewById(R.id.creategroup_map);
        creategroup_menu_submit = (Button) findViewById(R.id.creategroup_submit);


        creategroup_menu_tags.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout main_tab = (LinearLayout) findViewById(R.id.creategroup_tab_main);
                LinearLayout tags_tab = (LinearLayout) findViewById(R.id.creategroup_tab_tags);
                LinearLayout map_tab = (LinearLayout) findViewById(R.id.creategroup_tab_map);

                if (activeTab.equals("tags")) {
                    main_tab.setVisibility(View.VISIBLE);
                    tags_tab.setVisibility(View.GONE);
                    map_tab.setVisibility(View.GONE);

                    creategroup_menu_tags.setChecked(false);
                    creategroup_menu_location.setChecked(false);

                    activeTab = "main";
                    actionBar.setTitle(R.string.top_title_create_group);
                } else {
                    main_tab.setVisibility(View.GONE);
                    tags_tab.setVisibility(View.VISIBLE);
                    map_tab.setVisibility(View.GONE);

                    creategroup_menu_tags.setChecked(true);
                    creategroup_menu_location.setChecked(false);

                    activeTab = "tags";
                    actionBar.setTitle(R.string.top_title_create_group_tags);
                }
            }
        });

        creategroup_menu_location.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout main_tab = (LinearLayout) findViewById(R.id.creategroup_tab_main);
                LinearLayout tags_tab = (LinearLayout) findViewById(R.id.creategroup_tab_tags);
                LinearLayout map_tab = (LinearLayout) findViewById(R.id.creategroup_tab_map);

                if (activeTab.equals("map")) {
                    main_tab.setVisibility(View.VISIBLE);
                    tags_tab.setVisibility(View.GONE);
                    map_tab.setVisibility(View.GONE);

                    creategroup_menu_tags.setChecked(false);
                    creategroup_menu_location.setChecked(false);

                    activeTab = "main";
                    actionBar.setTitle(R.string.top_title_create_group);

                } else {
                    main_tab.setVisibility(View.GONE);
                    tags_tab.setVisibility(View.GONE);
                    map_tab.setVisibility(View.VISIBLE);

                    creategroup_menu_tags.setChecked(false);
                    creategroup_menu_location.setChecked(true);

                    activeTab = "map";
                    actionBar.setTitle(R.string.top_title_create_group_map);
                }
            }
        });

        creategroup_menu_submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SaveProfile task = new SaveProfile();
                task.execute("");

                finish();
            }
        });

        Button my_position = (Button) findViewById(R.id.map_my_location);
        my_position.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mapCenter != null) {
                    mc.setCenter(mapCenter);
                }
            }
        });

        map_edit = (ToggleButton) findViewById(R.id.map_edit_location);
        map_edit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO system zadavani bodu na mapu
                inEditMode = map_edit.isChecked();
            }
        });

    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    /*
    private class Tager extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
          	String[] type = urls;
            	
           	ApiConnector api = new ApiConnector(CreateGroupActivity.this);
           	boolean result = false;
            if(api.sessionInitiated()) {
            	boolean status = false;
            	if(Boolean.valueOf(type[1])) {
            		status = true;
            	}
            	Log.d(Config.DEBUG_TAG, status);
				result = api.changeProfileTag(Integer.valueOf(type[0]), status);
            }	
            
            if(result) {
            	
            	return "true";
            } 
           	return "false";
        }

        @Override
        protected void onPostExecute(String result) {
           	if(result.equals("true")) {
           		
           	} else {
           		
           	}
           	loader.dismiss();
        }
    }
    */
    private class DashboardInit extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String[] type = urls;


            ApiConnector api = new ApiConnector(CreateGroupActivity.this);
            DataObject object = null;
            if (api.sessionInitiated()) {
                supported_lng = api.getSupportedLanguages();
                supported_tags = api.getSupportedTags();


            }
            return "true";
        }

        @Override
        protected void onPostExecute(String result) {
            creategroup_visibility.check(R.id.creategroup_visibility_world);

            if (supported_lng != null) {
                ArrayList<String> items = new ArrayList<String>();
                Iterator<Entry<String, String>> it = supported_lng.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                    //pairs.getKey();
                    items.add(pairs.getValue().toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateGroupActivity.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                creategroup_language.setAdapter(adapter);


            }


            if (supported_tags != null) {
                items = new ArrayList<DataObject>();
                Iterator<Entry<String, String>> it = supported_tags.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    Log.d(Config.DEBUG_TAG, "Tag: " + pairs.getKey().toString() + " " + pairs.getValue().toString());
                    //pairs.getKey();
                    TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());
                    /*
                    Iterator<DataObject> iter = my_tags.iterator();
            		while(iter.hasNext()) {
            			TagObject c = (TagObject)iter.next();
            			if(c.getObjectId() == current_item.getObjectId()) {
            				Log.d(Config.DEBUG_TAG, c.getStatus());
            				current_item.setStatus(true);
            				break;
            			}
            		}
            		*/
                    items.add(current_item);

                }
                ObjectListItemAdapter tad = new ObjectListItemAdapter(CreateGroupActivity.this, "profile_tag", items);
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

                        //Tager task = new Tager();
                        //task.execute(new String[] { String.valueOf(tag_id),String.valueOf(status) });

                        for (int i = 0; i < items.size(); i++) {
                            Iterator<DataObject> it = items.iterator();
                            while (it.hasNext()) {
                                TagObject tag = (TagObject) it.next();
                                if (tag.getObjectId() == tag_id) {
                                    tag.setStatus(status);
                                }
                            }
                        }
                    }

                    @Override
                    public void onAcceptMessageAction(int message_id, int sender_id) {

                    }
                });
                taglist.setAdapter(tad);


            }

            mapCenter = new GeoPoint(latitude, longitude);

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


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (creategroup_menu_location.isChecked() && inEditMode) {

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

    }

    private class SaveProfile extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(CreateGroupActivity.this);
            String result = null;
            if (api.sessionInitiated()) {

                int visibility = 1;
                if (creategroup_visibility.getCheckedRadioButtonId() == R.id.profile_visibility_private) {
                    visibility = 3;
                } else if (creategroup_visibility.getCheckedRadioButtonId() == R.id.profile_visibility_member) {
                    visibility = 2;
                } else if (creategroup_visibility.getCheckedRadioButtonId() == R.id.profile_visibility_world) {
                    visibility = 1;
                }

                String selected_tags = "";

                Iterator<DataObject> it = items.iterator();

                while (it.hasNext()) {
                    TagObject tag = (TagObject) it.next();
                    if (tag.getStatus()) {
                        if (!selected_tags.equals("")) {
                            selected_tags += ",";

                        }
                        selected_tags += tag.getObjectId();
                    }
                }
                String filter_language = "1";
                if (creategroup_language.getSelectedItem().toString().equals("English")) {
                    filter_language = "1";
                } else if (creategroup_language.getSelectedItem().toString().equals("Burmese")) {
                    filter_language = "2";
                }


                result = api.createGroup(creategroup_name.getText().toString(), creategroup_description.getText().toString(), String.valueOf(visibility), String.valueOf(mapCenter.getLatitudeE6()), String.valueOf(mapCenter.getLongitudeE6()), selected_tags, filter_language);

            }

            if (result != null) {


                return result;

            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("false")) {
                if (result.equals("no_rights")) {
                    Toast.makeText(getApplicationContext(), "You have no rights to create group.", Toast.LENGTH_LONG).show();
                } else {

                    Intent intent = new Intent(CreateGroupActivity.this, DetailActivity.class);

                    intent.putExtra("ObjectType", "group");
                    intent.putExtra("ObjectId", result);

                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Unexpected error occurred.", Toast.LENGTH_LONG).show();
            }
            //loader.dismiss();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        SharedPreferences settings = getSharedPreferences("MyCitizen", 0);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("last_latitude", Double.toString(latitude));
            editor.putString("last_longitude", Double.toString(longitude));
        }
        if (latitude == 0 || longitude == 0) {
            String deployment_latitude = settings.getString("deployment_latitude", "0");
            String deployment_longitude = settings.getString("deployment_longitude", "0");
            latitude = Long.valueOf(settings.getString("last_latitude", deployment_latitude));
            longitude = Long.valueOf(settings.getString("last_longitude", deployment_longitude));
        }

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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();


        inflater.inflate(R.menu.help, menu);

        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_help:
                intent = new Intent(CreateGroupActivity.this, HelpActivity.class);

                intent.putExtra("topic", "create_group");
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}

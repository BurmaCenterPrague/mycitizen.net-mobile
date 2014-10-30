package net.mycitizen.mcn;

import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView.Projection;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class MapActivity extends ActionBarActivity implements OnTouchListener, LocationListener {
    public ApiConnector api;

    LocationManager locationManager;
    double latitude, longitude;

    ProgressDialog loader = null;

    MapView mapView;

    IMapController mc;
    Projection p;

    MapOverlay overlay = null;

    GeoPoint mapCenter;

    boolean inEditMode = false;
    ToggleButton edit;

    SeekBar circleRadius;

    boolean first_update = true;

    String mapType;

    String location_filter_gpsx = null, location_filter_gpsy = null;
    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = new ApiConnector(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_title_map);
        actionBar.setIcon(null);


        Intent intent = getIntent();
        mapType = intent.getStringExtra("type");

        if (mapType != null && mapType.equals("filter_edit")) {

            setContentView(R.layout.filter_map_view);
            actionBar.setTitle(R.string.top_title_filter_map);

            SharedPreferences settings = MapActivity.this.getSharedPreferences(Config.localStorageName, 0);

            location_filter_gpsx = settings.getString("filter_gpsx", null);
            location_filter_gpsy = settings.getString("filter_gpsy", null);
            int location_filter_radius = settings.getInt("filter_radius", 0);

            System.out.println("load " + location_filter_gpsx + " " + location_filter_gpsy);
            if (location_filter_gpsx == null || location_filter_gpsy == null) {
                //location_filter_gpsx = "50278137";
                //location_filter_gpsy = "14328357";
            } else {
                System.out.println("START: " + location_filter_gpsx + " " + location_filter_gpsy);
                mapCenter = new GeoPoint(Integer.valueOf(location_filter_gpsx), Integer.valueOf(location_filter_gpsy));
            }
            circleRadius = (SeekBar) findViewById(R.id.map_radius);
            circleRadius.setMax(500000);
            circleRadius.setEnabled(false);
            System.out.println("PROGRESS " + location_filter_radius);
            circleRadius.setProgress(location_filter_radius);

            circleRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {

                    // parabola for better fine-tuning in close range
                    double radius = Math.pow(((double) progress) / 500, 2.2);

                    overlay.updateCircle((int) radius);
                    mapView.invalidate();
                }
            });

            Button my_location = (Button) findViewById(R.id.map_my_location);
            my_location.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mc.setCenter(new GeoPoint(latitude, longitude));
                    mapView.invalidate();
                }
            });

            Button filter_location = (Button) findViewById(R.id.map_filter_location);
            filter_location.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mapCenter != null) {
                        mc.setCenter(mapCenter);
                        mapView.invalidate();
                    }
                }
            });

            edit = (ToggleButton) findViewById(R.id.map_edit_location);

            edit.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO system zadavani bodu na mapu
                    if (edit.isChecked()) {
                        inEditMode = true;
                        circleRadius.setEnabled(true);
                    } else {
                        inEditMode = false;
                        circleRadius.setEnabled(false);
                    }
                }
            });

            Button delete = (Button) findViewById(R.id.map_remove_location);
            delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    overlay.updateCenter(null);
                    mapView.invalidate();

                    SharedPreferences save_settings = MapActivity.this.getSharedPreferences("MyCitizen", 0);
                    SharedPreferences.Editor editor = save_settings.edit();

                    editor.putString("filter_gpsx", null);
                    editor.putString("filter_gpsy", null);
                    editor.putInt("filter_radius", 0);

                    editor.commit();
                }
            });

            mapView = (MapView) findViewById(R.id.mapview);

            //mapView.setMultiTouchControls(true);
            //mapView.setBuiltInZoomControls(true);


            mapView.setOnTouchListener(this);

            overlay = new MapOverlay(getApplicationContext(), mapView.getWidth(), mapView.getHeight(), mapCenter, new GeoPoint(latitude, longitude));

            mc = mapView.getController();
            if (mapCenter != null) {
                System.out.println("XXX: " + mapCenter.getLatitudeE6() + " " + mapCenter.getLongitudeE6());
                //mc.setCenter(mapCenter);

                overlay.updateCenter(mapCenter);
                mapView.invalidate();
            }
            mc.setZoom(15);

            p = mapView.getProjection();

            List<Overlay> o = mapView.getOverlays();
            o.add(overlay);

            overlay.updateCircle(location_filter_radius);

            if (location_filter_gpsx != null && location_filter_gpsy != null) {
                mc.setCenter(new GeoPoint(Integer.valueOf(location_filter_gpsx), Integer.valueOf(location_filter_gpsy)));
            }

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
        } else {


            setContentView(R.layout.show_map_view);
            String objectType = intent.getStringExtra("objectType");
            if (objectType != null) {
                if (objectType.equals("resource")) {
                    actionBar.setTitle(R.string.top_title_detail_resource_map);

                } else if (objectType.equals("group")) {
                    actionBar.setTitle(R.string.top_title_detail_group_map);

                } else {
                    actionBar.setTitle(R.string.top_title_detail_user_map);

                }
            }
            String objectId = intent.getStringExtra("objectId");

            loader = loadingDialog();

            DashboardInit task = new DashboardInit();
            task.execute(new String[]{objectType, objectId});

            Button mylocation = (Button) findViewById(R.id.map_my_location);
            mylocation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mc.setCenter(new GeoPoint(latitude, longitude));
                }
            });

            Button objectlocation = (Button) findViewById(R.id.map_object_location);
            objectlocation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mapCenter != null) {
                        mc.setCenter(mapCenter);
                    }
                }
            });

            mapView = (MapView) findViewById(R.id.mapview);

            //mapView.setMultiTouchControls(true);

            mapView.setOnTouchListener(this);
            //mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);

            overlay = new MapOverlay(getApplicationContext(), mapView.getWidth(), mapView.getHeight(), mapCenter, new GeoPoint(latitude, longitude));

            mc = mapView.getController();
            if (mapCenter != null) {
                System.out.println("XXX: " + mapCenter.getLatitudeE6() + " " + mapCenter.getLongitudeE6());
                //mc.setCenter(mapCenter);

                overlay.updateCenter(mapCenter);
                mapView.invalidate();
            }
            mc.setZoom(15);

            p = mapView.getProjection();

            List<Overlay> o = mapView.getOverlays();
            o.add(overlay);

            if (mapCenter != null) {
                mc.setCenter(mapCenter);
            }

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
        }


    }


    @Override

    public boolean onTouch(View v, MotionEvent event) {


        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (inEditMode) {

                    //GeoPoint gp = p.fromPixels((int)x, (int)y);

                    Projection p = mapView.getProjection();

                    GeoPoint finale = (GeoPoint) p.fromPixels(x, y);
                    overlay.updateMyPosition((GeoPoint) p.fromPixels(x, y));
                    mapCenter = (GeoPoint) p.fromPixels(x, y);

                    SharedPreferences save_settings = MapActivity.this.getSharedPreferences("MyCitizen", 0);
                    SharedPreferences.Editor editor = save_settings.edit();

                    editor.putString("filter_gpsx", String.valueOf(finale.getLatitudeE6()));
                    editor.putString("filter_gpsy", String.valueOf(finale.getLongitudeE6()));
                    editor.putInt("filter_radius", circleRadius.getProgress());
                    System.out.println("PROGRESSSET " + circleRadius.getProgress());
                    System.out.println("save " + finale.getLatitudeE6() + " " + finale.getLongitudeE6());
                    editor.commit();

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
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapType != null && mapType.equals("filter_edit")) {
            SharedPreferences save_settings = MapActivity.this.getSharedPreferences("MyCitizen", 0);
            SharedPreferences.Editor editor = save_settings.edit();
            editor.putInt("filter_radius", circleRadius.getProgress());
            editor.commit();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        overlay.updateMyLocation(new GeoPoint(latitude, longitude));

        if (first_update) {
            if (mapType != null && mapType.equals("filter_edit") && location_filter_gpsx == null || location_filter_gpsy == null) {
                mc.setCenter(new GeoPoint(latitude, longitude));
            }
            first_update = false;
        }
        mapView.invalidate();
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

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }


    private class DashboardInit extends AsyncTask<String, Void, DataObject> {
        @Override
        protected DataObject doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(MapActivity.this);
            DataObject object = null;
            if (api.sessionInitiated()) {
                if (type[0].equals("user")) {
                    mapCenter = ((UserObject) api.getDetail(type[0], Integer.valueOf(type[1]))).getPosition();
                } else if (type[0].equals("group")) {
                    mapCenter = ((GroupObject) api.getDetail(type[0], Integer.valueOf(type[1]))).getPosition();
                } else if (type[0].equals("resource")) {
                    mapCenter = ((ResourceObject) api.getDetail(type[0], Integer.valueOf(type[1]))).getPosition();
                }

                object = api.getDetail(type[0], Integer.valueOf(type[1]));


            }

            return object;
        }

        @Override
        protected void onPostExecute(DataObject result) {
            //parse data to view
            overlay.updateCenter(mapCenter);

            loader.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_help:
                Intent intent = new Intent(MapActivity.this, HelpActivity.class);

                if (mapType != null && mapType.equals("filter_edit")) {
                    intent.putExtra("topic", "filter_map");
                } else {
                    intent.putExtra("topic", "map");

                }
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

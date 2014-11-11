package net.mycitizen.mcn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import net.mycitizen.mcn.MultiSpinner.MultiSpinnerListener;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class FilterMenuActivity extends BaseActivity implements LocationListener {
    public ApiConnector api;
    ProgressDialog loader = null;
    ToggleButton filter_status;
    EditText filter_input;
    MultiSpinner filter_tag;
    Spinner filter_language;
    Spinner filter_map_alternative;
    CheckBox filter_my;

    LocationManager locationManager;
    double latitude, longitude;

    LinkedHashMap<Integer, Boolean> filter_tag_result = null;

    LinkedHashMap<String, String> supported_lng;
    LinkedHashMap<String, String> supported_tags;

    ArrayList<DataObject> my_tags;

    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_filter);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_title_filter);
        actionBar.setIcon(null);

        api = new ApiConnector(this);

        my_tags = new ArrayList<DataObject>();

        filter_map_alternative = (Spinner) findViewById(R.id.filter_map_alternative);

        filter_map_alternative.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                filter_status.setChecked(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        filter_input = (EditText) findViewById(R.id.filter_input);

        filter_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter_status.setChecked(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        filter_tag = (MultiSpinner) findViewById(R.id.filter_tags);
        filter_language = (Spinner) findViewById(R.id.filter_language);

        filter_language.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                filter_status.setChecked(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        filter_my = (CheckBox) findViewById(R.id.filter_only_my);
        loader = loadingDialog();

        Init task = new Init();
        task.execute("");


        filter_tag.setMultiselectListener(new MultiSpinnerListener() {

            @Override
            public void onItemsSelected(LinkedHashMap<Integer, Boolean> result) {
                Iterator<Entry<Integer, Boolean>> it = result.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                    filter_tag_result = result;
                    filter_status.setChecked(true);
                }
            }
        });

        filter_status = (ToggleButton) findViewById(R.id.filter_status);


        filter_status.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences save_settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
                SharedPreferences.Editor editor = save_settings.edit();

                if (isChecked) {

                    editor.putString("help", generateFilterStorage());


                } else {

                    editor.putString("help", null);
                }

                editor.commit();
            }
        });


        Button filter_suggest = (Button) findViewById(R.id.filter_suggest);
        filter_suggest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                filter_status.setChecked(true);
                filter_input.setText("");

                filter_my.setChecked(false);

                SharedPreferences settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
                String logged_user_language = settings.getString("logged_user_language", "eng");

                ArrayAdapter langAdap = (ArrayAdapter) filter_language.getAdapter();

                int langPosition = langAdap.getPosition(logged_user_language);
                filter_language.setSelection(langPosition);

                String logged_user_tags = settings.getString("logged_user_tags", "[]");

                ArrayList<DataObject> my_tags = new ArrayList<DataObject>();

                try {
                    JSONArray logged_user_tags_array = new JSONArray(logged_user_tags);
                    for (int i = 0; i < logged_user_tags_array.length(); i++) {
                        int tag_id = logged_user_tags_array.getInt(i);
                        TagObject t = new TagObject(tag_id, "");
                        t.setStatus(true);
                        my_tags.add(t);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (supported_tags != null) {
                    List<Integer> default_items_ids = new ArrayList<Integer>();
                    List<String> default_items = new ArrayList<String>();
                    List<Boolean> default_items_vals = new ArrayList<Boolean>();

                    if (filter_tag_result != null) {
                        filter_tag_result.clear();
                    } else {
                        filter_tag_result = new LinkedHashMap<Integer, Boolean>();
                    }

                    ArrayList<DataObject> items = new ArrayList<DataObject>();
                    Iterator<Entry<String, String>> it = supported_tags.entrySet().iterator();
                    while (it.hasNext()) {
                        HashMap.Entry pairs = (HashMap.Entry) it.next();
                        Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                        //pairs.getKey();
                        TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());

                            Iterator<DataObject> iter = my_tags.iterator();
                            while (iter.hasNext()) {
                                TagObject c = (TagObject) iter.next();
                                if (c.getObjectId() == current_item.getObjectId()) {
                                    Log.d(Config.DEBUG_TAG, String.valueOf(c.getStatus()));
                                    current_item.setStatus(true);
                                    break;
                                }
                            }

                        filter_tag_result.put(current_item.getObjectId(), current_item.getStatus());
                        default_items_ids.add(current_item.getObjectId());
                        default_items.add(current_item.getTitle());
                        default_items_vals.add(current_item.getStatus());
                    }


                    filter_tag.setItems(default_items_ids, default_items, default_items_vals, getString(R.string.tags_all));

                }

            }
        });

        Button filter_reset = (Button) findViewById(R.id.filter_reset);
        filter_reset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                filter_status.setChecked(false);
                filter_input.setText("");

                filter_tag.setAll(false);

                filter_language.setSelection(0);

                filter_my.setChecked(false);

                filter_tag_result = null;

                filter_map_alternative.setSelection(0);

                SharedPreferences save_settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
                SharedPreferences.Editor editor = save_settings.edit();

                editor.putString("help", null);
                editor.putString("filter_gpsx", null);
                editor.putString("filter_gpsy", null);
                editor.putInt("filter_radius", 0);
                editor.putString("filter_map_alternative", null);

                editor.commit();
            }
        });

        Button filter_map = (Button) findViewById(R.id.filter_map);
        filter_map.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                filter_status.setChecked(true);
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), R.string.not_available_offline, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(FilterMenuActivity.this, MapActivity.class);

                    //String message = editText.getText().toString();
                    intent.putExtra("type", "filter_edit");

                    startActivity(intent);
                }
            }
        });

        ToggleButton menu_filter = (ToggleButton) findViewById(R.id.widget_menu_filter);
        SharedPreferences icon_settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
        if (icon_settings.getBoolean("filter_active", false)) {
            menu_filter.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_filter_on), null, null);
        }
        menu_filter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FilterMenuActivity.this, WidgetActivity.class);

                //String message = editText.getText().toString();
                intent.putExtra("origin", "filter");

                startActivity(intent);
                finish();
            }
        });

        dashboard_button = (Button) findViewById(R.id.widget_menu_dashboard);

        dashboard_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FilterMenuActivity.this, DashboardMenuActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
                finish();
            }
        });

        messages_button = (Button) findViewById(R.id.widget_menu_messages);
        CheckUnreadMessages messages = new CheckUnreadMessages();
        messages.execute();
        messages_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FilterMenuActivity.this, MessagesActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("type", "dialog_inbox");
                startActivity(intent);
                finish();
            }
        });


        SharedPreferences settings = getSharedPreferences(Config.localStorageName, 0);

        int strength = settings.getInt("connection_strength", 0);

        Log.d(Config.DEBUG_TAG, "strength: " + strength);
        if (strength < 50) {
            Button map_button = (Button) findViewById(R.id.filter_map);
            map_button.setVisibility(View.GONE);
            filter_map_alternative.setVisibility(View.VISIBLE);

            ArrayList<String> items = new ArrayList<String>();
            String unit = settings.getString("distance_unit", "km");

            items.add("radius");
            items.add("10 " + unit);
            items.add("50 " + unit);
            items.add("100 " + unit);
            items.add("500 " + unit);
            items.add("1000 " + unit);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(FilterMenuActivity.this, android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            filter_map_alternative.setAdapter(adapter);

        }
    }

    public String generateFilterStorage() {
        String result = "{";

        result += "'filter_input':'" + filter_input.getText().toString() + "',";

        if (filter_tag_result != null) {
            String tag_ids = "";
            Iterator<Entry<Integer, Boolean>> it = filter_tag_result.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                if (((Boolean) pairs.getValue())) {
                    if (!tag_ids.equals("")) {
                        tag_ids += ",";
                    }
                    tag_ids += pairs.getKey();
                }
            }

            result += "'filter_tag':[" + tag_ids + "],";

        }
        if (filter_language != null && filter_language.getSelectedItem() != null) {
            String languageName = filter_language.getSelectedItem().toString();
            String languageCode = Config.translateLanguageNameToCode(getApplicationContext(), languageName);
            result += "'filter_language':'" + languageCode + "',";
        }

        if (filter_map_alternative != null && filter_map_alternative.getSelectedItem() != null) {
            String mapDistance = filter_map_alternative.getSelectedItem().toString();
            if (!mapDistance.equals("radius")) {
                result += "'filter_map_alternative':'" + mapDistance + "',";
                SharedPreferences settings = getSharedPreferences(Config.localStorageName, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("filter_gpsx", String.valueOf(latitude * 100000));
                editor.putString("filter_gpsy", String.valueOf(longitude * 100000));
                editor.commit();
            }
        }

        if (filter_my.isChecked()) {
            result += "'filter_my':true,";
        } else {
            result += "'filter_my':false,";
        }

        if (filter_status.isChecked()) {
            result += "'active':true";
        } else {
            result += "'active':false";
        }
        result += "}";
        Log.d(Config.DEBUG_TAG, "FILTER: " + result);
        return result;

    }

    public void translateFilterStorage(String storage) throws JSONException {
        Log.d(Config.DEBUG_TAG, "translateFilterStorage");

        if (storage == null) {
            return;
        }
        JSONObject json = new JSONObject(storage);
        if (json.getBoolean("active")) {
            filter_status.setChecked(true);
        } else {
            filter_status.setChecked(false);
        }

        if (json.getBoolean("filter_my")) {
            filter_my.setChecked(true);
        } else {
            filter_my.setChecked(false);
        }
        Log.d(Config.DEBUG_TAG, json.getString("filter_input"));
        filter_input.setText(json.getString("filter_input"));
        Log.d(Config.DEBUG_TAG, "filter_input: " + filter_input.getText().toString());

        ArrayAdapter langAdap = (ArrayAdapter) filter_language.getAdapter();
        if (json.getString("filter_language") == null) {
            int langPosition = 0;
            filter_language.setSelection(langPosition);
        } else {
            String languageName = Config.translateLanguageCodeToName(getApplicationContext(), json.getString("filter_language"));
            // Log.d(Config.DEBUG_TAG, "language code: "+json.getString("filter_language")+", name: "+languageName);
            int langPosition = langAdap.getPosition(languageName);
            filter_language.setSelection(langPosition);
        }

        ArrayAdapter mapAdap = (ArrayAdapter) filter_map_alternative.getAdapter();
        if (json.has("filter_map_alternative") && json.getString("filter_map_alternative") == null) {
            Log.d(Config.DEBUG_TAG, "filter_map_alternative: " + json.getString("filter_map_alternative"));
            int mapPosition = 0;
            filter_map_alternative.setSelection(mapPosition);
        } else {
            int mapPosition = mapAdap.getPosition(json.getString("filter_map_alternative"));
            filter_map_alternative.setSelection(mapPosition);
        }

        JSONArray tags = json.getJSONArray("filter_tag");

        if (my_tags != null)
            my_tags.clear();

        for (int xs = 0; xs < tags.length(); xs++) {
            int tag = tags.getInt(xs);

            TagObject tagO = new TagObject(tag, "");
            tagO.setStatus(true);
            my_tags.add(tagO);
            Log.d(Config.DEBUG_TAG, "Saved TAG " + tag);

        }


        if (supported_tags != null) {
            List<Integer> default_items_ids = new ArrayList<Integer>();
            List<String> default_items = new ArrayList<String>();
            List<Boolean> default_items_vals = new ArrayList<Boolean>();

            if (filter_tag_result != null) {
                filter_tag_result.clear();
            } else {
                filter_tag_result = new LinkedHashMap<Integer, Boolean>();
            }

            ArrayList<DataObject> items = new ArrayList<DataObject>();
            Iterator<Entry<String, String>> it = supported_tags.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                //pairs.getKey();
                TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());
                if (my_tags != null) {
                    Iterator<DataObject> iter = my_tags.iterator();
                    while (iter.hasNext()) {
                        TagObject c = (TagObject) iter.next();
                        if (c.getObjectId() == current_item.getObjectId()) {
                            Log.d(Config.DEBUG_TAG, String.valueOf(c.getStatus()));
                            current_item.setStatus(true);
                            break;
                        }
                    }
                }

                filter_tag_result.put(current_item.getObjectId(), current_item.getStatus());
                default_items_ids.add(current_item.getObjectId());
                default_items.add(current_item.getTitle());
                default_items_vals.add(current_item.getStatus());
            }


            filter_tag.setItems(default_items_ids, default_items, default_items_vals, getString(R.string.tags_all));

        }


    }

    @Override
    protected void onPause() {
        super.onStop();

        SharedPreferences save_settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
        SharedPreferences.Editor editor = save_settings.edit();

        boolean filter_active = false;

        if (filter_my.isChecked()) {
            filter_active = true;
            Log.d(Config.DEBUG_TAG, "FILTER: filter_my");
        }
        if (save_settings.getString("filter_gpsx", null) != null && save_settings.getString("filter_gpsy", null) != null) {
            filter_active = true;
            Log.d(Config.DEBUG_TAG, "FILTER: filter_map");
        }

        if (!filter_input.getText().toString().equals("")) {
            filter_active = true;
            Log.d(Config.DEBUG_TAG, "FILTER: filter_input");
        }

        if (filter_language.getSelectedItemPosition() > 0) {
            filter_active = true;
            Log.d(Config.DEBUG_TAG, "FILTER: filter_lang");
        }

        if (filter_map_alternative.getSelectedItemPosition() > 0) {
            filter_active = true;
            Log.d(Config.DEBUG_TAG, "FILTER: filter_map_alternative");
        }

        String tag_ids = "";
        if (filter_tag_result != null) {
            Iterator<Entry<Integer, Boolean>> it = filter_tag_result.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                if (((Boolean) pairs.getValue())) {
                    if (!tag_ids.equals("")) {
                        tag_ids += ",";
                    }
                    tag_ids += pairs.getKey();
                }
            }
        }
        if (!tag_ids.equals("")) {

            filter_active = true;
            Log.d(Config.DEBUG_TAG, "FILTER: filter_tag");
        }
        if (filter_active) {
            Log.d(Config.DEBUG_TAG, "FILTER REFRESH");
            editor.putString("help", generateFilterStorage());
            editor.putBoolean("filter_active", true);
            if (filter_my.isChecked()) {

                editor.putString("filter_gpsx", null);
                editor.putString("filter_gpsy", null);
                editor.putInt("filter_radius", 0);
            }


        } else {
            editor.putString("help", null);
            editor.putBoolean("filter_active", false);
        }

        editor.commit();

    }

    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class Init extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {

            String[] type = urls;



            ApiConnector api = new ApiConnector(FilterMenuActivity.this);

            if (api.sessionInitiated()) {
                supported_lng = api.getSupportedLanguages();
                supported_tags = api.getSupportedTags();

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (supported_lng != null) {

                ArrayList<String> items = new ArrayList<String>();
                Iterator<Entry<String, String>> it = supported_lng.entrySet().iterator();
                items.add(getString(R.string.language_all));
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
//                    Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                    items.add(pairs.getValue().toString());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(FilterMenuActivity.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                filter_language.setAdapter(adapter);


            }

            if (supported_tags != null) {
                List<Integer> default_items_ids = new ArrayList<Integer>();
                List<String> default_items = new ArrayList<String>();
                List<Boolean> default_items_vals = new ArrayList<Boolean>();

                ArrayList<DataObject> items = new ArrayList<DataObject>();
                Iterator<Entry<String, String>> it = supported_tags.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    Log.d(Config.DEBUG_TAG, pairs.getKey().toString() + " " + pairs.getValue().toString());
                    //pairs.getKey();
                    TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());
                    if (my_tags != null) {
                        Iterator<DataObject> iter = my_tags.iterator();
                        while (iter.hasNext()) {
                            TagObject c = (TagObject) iter.next();
                            if (c.getObjectId() == current_item.getObjectId()) {
                                Log.d(Config.DEBUG_TAG, String.valueOf(c.getStatus()));
                                current_item.setStatus(true);
                                break;
                            }
                        }
                    }
                    items.add(current_item);
                    default_items_ids.add(current_item.getObjectId());
                    default_items.add(current_item.getTitle());
                    default_items_vals.add(current_item.getStatus());
                }
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, default_items);
                //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //filter_tag.setAdapter(new ObjectListItemAdapter(FilterMenuActivity.this,"filter_tag", items));


                filter_tag.setItems(default_items_ids, default_items, default_items_vals, getString(R.string.tags_all));


            }

            SharedPreferences settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
            try {
                translateFilterStorage(settings.getString("help", null));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

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
                Intent intent = new Intent(FilterMenuActivity.this, HelpActivity.class);
                intent.putExtra("topic", "filter");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        Location location = null;
        super.onStart();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

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

        Log.d(Config.DEBUG_TAG, "latitude: " + latitude + ", longitude: " + longitude);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();

        longitude = location.getLongitude();
        Log.d(Config.DEBUG_TAG, "latitude: " + latitude + ", longitude: " + longitude);
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

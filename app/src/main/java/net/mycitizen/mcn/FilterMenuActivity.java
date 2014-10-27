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

import net.mycitizen.mcn.MultiSpinner.MultiSpinnerListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

public class FilterMenuActivity extends BaseActivity {
    public ApiConnector api;
    ProgressDialog loader = null;
    ToggleButton filter_status;
    EditText filter_input;
    MultiSpinner filter_tag;
    Spinner filter_language;
    CheckBox filter_my;

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
        task.execute(new String[]{""});


        filter_tag.setMultiselectListener(new MultiSpinnerListener() {

            @Override
            public void onItemsSelected(LinkedHashMap<Integer, Boolean> result) {
                System.out.println("david");

                Iterator<Entry<Integer, Boolean>> it = result.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pairs = (HashMap.Entry) it.next();
                    System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
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

/*
        Button help = (Button) findViewById(R.id.button_filter_help);
        help.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //page1.setVisibility(View.VISIBLE);
                //page2.setVisibility(View.GONE);

                Intent intent = new Intent(FilterMenuActivity.this, HelpActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra("topic", "help");

                startActivity(intent);
            }
        });
*/

        Button filter_suggest = (Button) findViewById(R.id.filter_suggest);
        filter_suggest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                filter_status.setChecked(true);
                filter_input.setText("");

                filter_my.setChecked(false);

                SharedPreferences settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
                String logged_user_language = settings.getString("logged_user_language", "English");

                ArrayAdapter langAdap = (ArrayAdapter) filter_language.getAdapter();

                int langPosition = langAdap.getPosition(logged_user_language);
                filter_language.setSelection(langPosition);

                //filter_tag.setSelection(0);

                //filter_language.setSelection(0);
                String logged_user_tags = settings.getString("logged_user_tags", "[]");
                System.out.println("LUT " + logged_user_tags);
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
                        System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                        //pairs.getKey();
                        TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());
                        if (my_tags != null) {
                            Iterator<DataObject> iter = my_tags.iterator();
                            while (iter.hasNext()) {
                                TagObject c = (TagObject) iter.next();
                                if (c.getObjectId() == current_item.getObjectId()) {
                                    System.out.println(c.getStatus());
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
                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, default_items);
                    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //filter_tag.setAdapter(new ObjectListItemAdapter(FilterMenuActivity.this,"filter_tag", items));


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

                SharedPreferences save_settings = FilterMenuActivity.this.getSharedPreferences("MyCitizen", 0);
                SharedPreferences.Editor editor = save_settings.edit();

                editor.putString("help", null);
                editor.putString("filter_gpsx", null);
                editor.putString("filter_gpsy", null);
                editor.putInt("filter_radius", 0);

                editor.commit();
            }
        });

        Button filter_map = (Button) findViewById(R.id.filter_map);
        filter_map.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                filter_status.setChecked(true);
                if (!api.isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(), "This section is not avalable in offline mode.", Toast.LENGTH_LONG).show();
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
        messages.execute(new String[]{});
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


    }

    public String generateFilterStorage() {
        String result = "{";

        result += "'filter_input':'" + filter_input.getText().toString() + "',";

        if (filter_tag_result != null) {
            String tag_ids = "";
            Iterator<Entry<Integer, Boolean>> it = filter_tag_result.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                if (((Boolean) pairs.getValue())) {
                    if (!tag_ids.equals("")) {
                        tag_ids += ",";
                    }
                    tag_ids += pairs.getKey();
                }
            }

            result += "'filter_tag':[" + tag_ids + "],";

        }
        result += "'filter_language':'" + filter_language.getSelectedItem() + "',";

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
        System.out.println("FILTER: " + result);
        return result;

    }

    public void translateFilterStorage(String storage) throws JSONException {
        System.out.println("TGS");

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
        System.out.println(json.getString("filter_input"));
        filter_input.setText(json.getString("filter_input"));
        System.out.println(filter_input.getText().toString());

        ArrayAdapter langAdap = (ArrayAdapter) filter_language.getAdapter();
        if (json.getString("filter_language") == null) {
            int langPosition = 0;
            filter_language.setSelection(langPosition);
        } else {
            int langPosition = langAdap.getPosition(json.getString("filter_language"));
            filter_language.setSelection(langPosition);
        }

        JSONArray tags = json.getJSONArray("filter_tag");

        if (my_tags != null)
            my_tags.clear();

        for (int xs = 0; xs < tags.length(); xs++) {
            int tag = tags.getInt(xs);

            TagObject tagO = new TagObject(tag, "");
            tagO.setStatus(true);
            my_tags.add((DataObject) tagO);
            System.out.println("Saved TAG " + tag);

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
                System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                //pairs.getKey();
                TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());
                if (my_tags != null) {
                    Iterator<DataObject> iter = my_tags.iterator();
                    while (iter.hasNext()) {
                        TagObject c = (TagObject) iter.next();
                        if (c.getObjectId() == current_item.getObjectId()) {
                            System.out.println(c.getStatus());
                            current_item.setStatus(true);
                            break;
                        }
                    }
                }
                System.out.println("TTTTT");
                filter_tag_result.put(current_item.getObjectId(), current_item.getStatus());
                default_items_ids.add(current_item.getObjectId());
                default_items.add(current_item.getTitle());
                default_items_vals.add(current_item.getStatus());
            }
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, default_items);
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //filter_tag.setAdapter(new ObjectListItemAdapter(FilterMenuActivity.this,"filter_tag", items));


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
            System.out.println("FILTER: filter_my");
        }
        if (save_settings.getString("filter_gpsx", null) != null && save_settings.getString("filter_gpsy", null) != null) {
            filter_active = true;
            System.out.println("FILTER: filter_map");
        }

        if (!filter_input.getText().toString().equals("")) {
            filter_active = true;
            System.out.println("FILTER: filter_input");
        }

        if (filter_language.getSelectedItemPosition() > 0) {
            filter_active = true;
            System.out.println("FILTER: filter_lang");
        }

        String tag_ids = "";
        if (filter_tag_result != null) {
            Iterator<Entry<Integer, Boolean>> it = filter_tag_result.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
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
            System.out.println("FILTER: filter_tag");
        }
        if (filter_active) {
            System.out.println("FILTER REFRESH");
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
                    System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                    //pairs.getKey();
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
                    System.out.println(pairs.getKey().toString() + " " + pairs.getValue().toString());
                    //pairs.getKey();
                    TagObject current_item = new TagObject(Integer.valueOf(pairs.getKey().toString()), pairs.getValue().toString());
                    if (my_tags != null) {
                        Iterator<DataObject> iter = my_tags.iterator();
                        while (iter.hasNext()) {
                            TagObject c = (TagObject) iter.next();
                            if (c.getObjectId() == current_item.getObjectId()) {
                                System.out.println(c.getStatus());
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
            case R.id.filter_help:
                Intent intent = new Intent(FilterMenuActivity.this, HelpActivity.class);
                intent.putExtra("topic", "filter");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

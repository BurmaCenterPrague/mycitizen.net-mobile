package net.mycitizen.mcn;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class TagsActivity extends ActionBarActivity {
    ArrayList<DataObject> tags;
    ListView taglist;

    ProgressDialog loader = null;

    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_tags_view);
        loader = loadingDialog();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Tags");
        actionBar.setIcon(null);

        taglist = (ListView) findViewById(R.id.profile_tag_list);

        Intent intent = getIntent();

        String objectType = intent.getStringExtra("ObjectType");
        String objectId = intent.getStringExtra("ObjectId");
        Log.d(Config.DEBUG_TAG, "SSS: " + objectId);
        DashboardInit task = new DashboardInit();
        task.execute(objectType, objectId);


        ImageButton profile_tags_save = (ImageButton) findViewById(R.id.profile_tags_save);
        profile_tags_save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

    }


    private ProgressDialog loadingDialog() {

        return ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait), true);

    }

    private class DashboardInit extends AsyncTask<String, Void, ArrayList<DataObject>> {
        @Override
        protected ArrayList<DataObject> doInBackground(String... urls) {
            String[] type = urls;

            ApiConnector api = new ApiConnector(TagsActivity.this);
            ArrayList<DataObject> tags = null;
            DataObject object = null;
            if (api.sessionInitiated()) {
                object = api.getDetail(type[0], Integer.valueOf(type[1]));

            }
            if (object != null) {
                tags = object.getTags();
            }
            return tags;
        }

        @Override
        protected void onPostExecute(ArrayList<DataObject> result) {
            tags = result;
            ObjectListItemAdapter tad = new ObjectListItemAdapter(TagsActivity.this, "profile_tag", result);
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
                    ApiConnector api = new ApiConnector(TagsActivity.this);
                    api.changeProfileTag(tag_id, status);
                }

                @Override
                public void onAcceptMessageAction(int message_id, int sender_id) {

                }
            });
            taglist.setAdapter(tad);
            loader.dismiss();
        }
    }


}

package net.mycitizen.mcn;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;


public class ProfileDescriptionActivity extends ActionBarActivity {
    EditText description;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_description_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        description = (EditText) findViewById(R.id.description);

        ImageButton profile_description_save = (ImageButton) findViewById(R.id.profile_description_save);
        profile_description_save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                description.getText().toString();
                //todo save

                Intent intent = new Intent(ProfileDescriptionActivity.this, ProfileMainActivity.class);

                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                startActivity(intent);
            }
        });

    }


}

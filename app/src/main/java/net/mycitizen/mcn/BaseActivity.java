package net.mycitizen.mcn;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

public class BaseActivity extends ActionBarActivity {
    protected ProgressBar dashboard_connection = null;
    protected ToggleButton filter_button_toggle = null;
    protected ToggleButton dashboard_button_toggle = null;
    protected ToggleButton messages_button_toggle = null;
    protected Button filter_button = null;
    protected Button dashboard_button = null;
    protected Button messages_button = null;


    private void registerReceivers() {
        registerReceiver(mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void updateConnection() {
        SharedPreferences user_settings = BaseActivity.this.getSharedPreferences("MyCitizen", 0);
        SharedPreferences.Editor editor = user_settings.edit();


        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo currentNetworkInfo = cm.getActiveNetworkInfo();


        // do application-specific task(s) based on the current network state, such
        // as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.
        Log.d(Config.DEBUG_TAG, "currentNetworkInfo: " + currentNetworkInfo);
        //activity.loadUrl("javascript:window.location.reload();");
        int strength = 5;
        if (currentNetworkInfo != null) {
            if (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        Integer linkSpeed = wifiInfo.getLinkSpeed(); //measured using WifiInfo.LINK_SPEED_UNITS
                    }
                    if ((user_settings.getLong("last_network_request", -1) > 0 && user_settings.getLong("last_network_request", -1) < 2000) || user_settings.getLong("last_network_request", -1) == -1) {
                        strength = 100;
                    } else {
                        if (user_settings.getLong("last_network_request", -1) == 0 || user_settings.getLong("last_network_request", -1) == 5000) {
                            strength = 5;
                        } else {
                            strength = 50;
                        }
                    }
                }
            } else {
                if ((user_settings.getLong("last_network_request", -1) > 0 && user_settings.getLong("last_network_request", -1) < 2000) || user_settings.getLong("last_network_request", -1) == -1) {
                    strength = 75;

                } else {
                    if (user_settings.getLong("last_network_request", -1) == 0 || user_settings.getLong("last_network_request", -1) == 5000) {
                        strength = 1;
                    } else {
                        strength = 25;
                    }
                }
            }


            if (strength <= 25) {
                if (dashboard_button != null) {
                    dashboard_button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_low), null, null);
                }
                if (dashboard_button_toggle != null) {
                    dashboard_button_toggle.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_low), null, null);
                }

            } else if (strength <= 75) {
                if (dashboard_button != null) {
                    dashboard_button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_middle), null, null);
                }
                if (dashboard_button_toggle != null) {
                    dashboard_button_toggle.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_middle), null, null);
                }

            } else {
                if (dashboard_button != null) {
                    dashboard_button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_fast), null, null);
                }
                if (dashboard_button_toggle != null) {
                    dashboard_button_toggle.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_fast), null, null);
                }
            }



            if (dashboard_connection != null) {
                dashboard_connection.setProgress(strength);
                if (strength <= 25) {
                    dashboard_connection.getProgressDrawable().setColorFilter(Color.parseColor("#C40C0C"), Mode.SRC_IN);
                } else if (strength <= 75) {
                    dashboard_connection.getProgressDrawable().setColorFilter(Color.parseColor("#FFBF00"), Mode.SRC_IN);
                } else {
                    dashboard_connection.getProgressDrawable().setColorFilter(Color.parseColor("#16911C"), Mode.SRC_IN);
                }
            }
        } else {
            editor.putLong("last_network_request", -1);
            editor.commit();
            strength = 1;

            if (dashboard_button != null) {
                dashboard_button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_low), null, null);
            }
            if (dashboard_button_toggle != null) {
                dashboard_button_toggle.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_dashboard_low), null, null);
            }


            if (dashboard_connection != null) {
                dashboard_connection.setProgress(5);

                dashboard_connection.getProgressDrawable().setColorFilter(Color.parseColor("#C40C0C"), Mode.SRC_IN);

            }
        }


        editor.putInt("connection_strength", strength);
        long timeMeasured = System.currentTimeMillis();
        editor.putLong("time_connection_measured", timeMeasured);

        editor.commit();

        Log.d(Config.DEBUG_TAG, "last_network_request: " + user_settings.getLong("last_network_request", 0));
        Log.d(Config.DEBUG_TAG, "strength: " + strength);
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnection();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //registerReceivers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mConnReceiver);
        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mConnReceiver);
        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        registerReceivers();

        Log.d(Config.DEBUG_TAG, "resuming");
        updateConnection();
    }

    protected class CheckUnreadMessages extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String[] type = urls;
            int unread = 0;
            ApiConnector api = new ApiConnector(BaseActivity.this);
            boolean result = false;
            if (api.sessionInitiated()) {
                unread = api.hasUnreadMessages();
            }

            if (unread > 0) {

                return "true";
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {
                if (messages_button != null) {
                    messages_button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_message_new), null, null);
                }
                if (messages_button_toggle != null) {
                    messages_button_toggle.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_message_new), null, null);
                }
            } else {
                if (messages_button != null) {
                    messages_button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_message_no), null, null);
                }
                if (messages_button_toggle != null) {
                    messages_button_toggle.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ico_message_no), null, null);
                }
            }

        }
    }
}

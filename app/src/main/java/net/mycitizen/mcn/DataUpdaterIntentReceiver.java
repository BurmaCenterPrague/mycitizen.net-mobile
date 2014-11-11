package net.mycitizen.mcn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DataUpdaterIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("net.mycitizen.mcn.DataUpdater");
        Log.d(Config.DEBUG_TAG, "before net.mycitizen.mcn.DataUpdater");
        context.startService(serviceIntent);
        Log.d(Config.DEBUG_TAG, "after net.mycitizen.mcn.DataUpdater");
    }

}

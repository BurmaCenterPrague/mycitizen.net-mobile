package net.mycitizen.mcn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DataUpdaterIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("net.mycitizen.mcn.DataUpdater");
        System.out.println("before net.mycitizen.mcn.DataUpdater");
        context.startService(serviceIntent);
        System.out.println("after net.mycitizen.mcn.DataUpdater");
    }

}

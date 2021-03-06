package com.tehike.mst.client.project.linphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.tehike.mst.client.project.utils.Logutil;


public class KeepAliveHandler extends BroadcastReceiver {
    private static final String TAG = "KeepAliveHandler";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SipManager.getLcIfManagerNotDestroyOrNull() != null) {
            SipManager.getLc().refreshRegisters();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Logutil.e( "Cannot sleep for 2s");
            }
        }
    }
}

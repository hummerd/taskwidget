package com.dima.tkswidget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TaskSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();

    private static TaskSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new TaskSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}

package com.dima.tkswidget.remote.view;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.dima.tkswidget.SettingsController;
import com.dima.tkswidget.TaskProvider;

/**
 * Created by dima on 12/23/13.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Context context = this.getApplicationContext();
        TaskProvider taskProvider = new TaskProvider(context);
        SettingsController settings = new SettingsController(context);

        return new WidgetRemoteViewsFactory(context, intent, taskProvider, settings);
    }
}

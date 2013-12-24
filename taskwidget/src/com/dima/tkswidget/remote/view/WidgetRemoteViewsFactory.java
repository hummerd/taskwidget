package com.dima.tkswidget.remote.view;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.dima.tkswidget.R;
import com.dima.tkswidget.SettingsController;
import com.dima.tkswidget.TaskProvider;
import com.google.api.services.tasks.model.Task;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dima on 12/23/13.
 */
public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private TaskProvider mTaskProvider;
    private SettingsController mSettings;
    private List<Task> mTasks = null;

    public WidgetRemoteViewsFactory(Context context, Intent intent, TaskProvider taskProvider, SettingsController settings) {
        mSettings = settings;
        mTaskProvider = taskProvider;
        mContext = context;
        mAppWidgetId = intent.getIntExtra(
           AppWidgetManager.EXTRA_APPWIDGET_ID,
           AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // Initialize the data set.
    public void onCreate() {
        String listId = mSettings.loadWidgetList(mAppWidgetId);
        if (listId == null) {
            return;
        }

        mTasks = mTaskProvider.getListTasks(listId);
        Collections.sort(mTasks, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task2) {
                String pos1 = task.getPosition();
                String pos2 = task2.getPosition();
                if (pos1 == null && pos2 == null)
                    return 0;

                if (pos1 == null)
                    return 1;

                if (pos2 == null)
                    return -1;

                return pos1.compareTo(pos2);
            }
        });
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv;
        Task task = mTasks.get(position);

        if (task.getStatus().equals("completed")) {
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_light_dimmed);
        } else {
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_light);
        }

        rv.setTextViewText(R.id.widget_item_light_text, task.getTitle());
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int i) {
        return mTasks.get(i).getId().hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

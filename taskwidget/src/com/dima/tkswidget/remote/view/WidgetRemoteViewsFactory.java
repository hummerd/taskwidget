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
        // position will always range from 0 to getCount() - 1.

        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_light);
        rv.setTextViewText(R.id.widget_item_light_text, mTasks.get(position).getTitle());
//
//        // Next, set a fill-intent, which will be used to fill in the pending intent template
//        // that is set on the collection view in StackWidgetProvider.
//        Bundle extras = new Bundle();
//        extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
//        Intent fillInIntent = new Intent();
//        fillInIntent.putExtras(extras);
//        // Make it possible to distinguish the individual on-click
//        // action of a given item
//        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
//

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
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

package com.dima.tkswidget.remote.view;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.dima.tkswidget.LogHelper;
import com.dima.tkswidget.R;
import com.dima.tkswidget.SettingsController;
import com.dima.tkswidget.TaskProvider;
import com.google.api.services.tasks.model.Task;

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
  private volatile List<Task> mTasks = null;
  private boolean mIsBright;


  public WidgetRemoteViewsFactory(Context context, Intent intent, TaskProvider taskProvider, SettingsController settings) {
    mSettings = settings;
    mTaskProvider = taskProvider;
    mContext = context;
    mAppWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID);
    LogHelper.d("Widget id " + mAppWidgetId);
    mIsBright = isBright(context);
  }

  // Initialize the data set.
  @Override
  public void onCreate() {
  }

  @Override
  public void onDataSetChanged() {
    LogHelper.d("WidgetRemoteViewsFactory onDataSetChanged");
    Thread thread = new Thread() {
      public void run() {
        mTasks = loadTasks(); //Task content provider can only be accessed from another thread
      }
    };
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
    }
  }

  @Override
  public void onDestroy() {
    LogHelper.d("WidgetRemoteViewsFactory onDestroy");
    mContext = null;
    mTaskProvider = null;
    mSettings = null;
    mTasks = null;
  }

  @Override
  public int getCount() {
    LogHelper.d("WidgetRemoteViewsFactory getCount");
    return mTasks == null ? 0 : mTasks.size();
  }

  @Override
  public RemoteViews getViewAt(int position) {
    LogHelper.d("WidgetRemoteViewsFactory getViewAt" + position);
    RemoteViews rv;
    Task task = mTasks.get(position);

    if (task.getStatus().equals("completed")) {
      rv = new RemoteViews(mContext.getPackageName(), getItemViewPassive());
    } else {
      rv = new RemoteViews(mContext.getPackageName(), getItemViewActive());
    }

    rv.setTextViewText(R.id.widget_item_light_text, task.getTitle());

    Intent fillInIntent = new Intent();
    fillInIntent.putExtra("not used", task.getId());
    rv.setOnClickFillInIntent(R.id.widget_item_light_text, fillInIntent);

    return rv;
  }

  @Override
  public RemoteViews getLoadingView() {
    return null;
  }

  @Override
  public int getViewTypeCount() {
    return 3;
  }

  @Override
  public long getItemId(int i) {
    LogHelper.d("WidgetRemoteViewsFactory getItemId" + i);
    return mTasks.get(i).getId().hashCode();
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  private List<Task> loadTasks() {
    String listId = mSettings.loadWidgetList(mAppWidgetId);
    if (listId == null) {
      return null;
    }

    List<Task> tasks = mTaskProvider.getListTasks(listId);
    Collections.sort(tasks, new Comparator<Task>() {
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
    return tasks;
  }

  private int getItemViewActive() {
    return mIsBright
      ? R.layout.widget_item
      : R.layout.widget_item_light;
  }

  private int getItemViewPassive() {
     return mIsBright
        ? R.layout.widget_item
        : R.layout.widget_item_light_dimmed;
  }

  private boolean isBright(Context context) {
    AppWidgetManager wm = AppWidgetManager.getInstance(context);
    AppWidgetProviderInfo wpi = wm.getAppWidgetInfo(mAppWidgetId);
    if (wpi == null)
      return true;

    if (wpi.initialLayout == R.layout.widget_blue
        || wpi.initialLayout == R.layout.widget_green
        || wpi.initialLayout == R.layout.widget_orange
        || wpi.initialLayout == R.layout.widget_purp
        || wpi.initialLayout == R.layout.widget_red)
      return true;

    return false;
  }
}

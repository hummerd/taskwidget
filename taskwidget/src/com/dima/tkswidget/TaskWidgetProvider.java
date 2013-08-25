package com.dima.tkswidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


public class TaskWidgetProvider extends AppWidgetProvider {

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        LogHelper.d("delete widgets started ", appWidgetIds);

        SettingsController controller = new SettingsController(context);
        controller.clearPrefs(appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        LogHelper.d("onDisabled");
    }

    @Override
    public void onEnabled(Context context) {
        LogHelper.d("onEnabled");
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.d("onReceive");
        super.onReceive(context, intent);

    	String action = intent.getAction();
    	LogHelper.d(action);
    	WidgetController controller = new WidgetController(context, null);
    	controller.performAction(action,  intent);
    }    
	
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        LogHelper.d("update widgets started ", appWidgetIds);

    	WidgetController controller = new WidgetController(context, appWidgetManager);
        controller.updateWidgetsAsync(appWidgetIds);
	}
}

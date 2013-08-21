package com.dima.tkswidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


public class TaskWidgetProvider extends AppWidgetProvider {

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        
        SettingsController controller = new SettingsController(context);
        controller.clearPrefs(appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	super.onReceive(context, intent);
    	LogHelper.d("onReceive");
    	
    	String action = intent.getAction();
    	LogHelper.d(action);
    	WidgetController controller = new WidgetController(context);
    	controller.performAction(action,  intent);
    }    
	
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

    	WidgetController controller = new WidgetController(context);
    	for (int id : appWidgetIds) {
        	RemoteViews views = controller.getWidgetViews();
        	controller.setupEvents(views, id);
        	controller.updateWidgets(views, id);
        	controller.applySettings(views, id);
        	
        	appWidgetManager.updateAppWidget(id, views);		
		}
    	
    	super.onUpdate(context, appWidgetManager, appWidgetIds);
    	LogHelper.i("update widgets started ");
	}
}

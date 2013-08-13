package com.dima.tkswidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;


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
    	String action = intent.getAction();
    	    	
    	LogHelper.d("onReceive");
    	LogHelper.d(action);

    	WidgetController controller = new WidgetController(context);
    	controller.performAction(action,  intent);
    	
        super.onReceive(context, intent);
    }    
	
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	LogHelper.i("update widgets started ");
        
    	WidgetController controller = new WidgetController(context);
    	controller.setupEvents(appWidgetIds);
    	controller.updateWidgets(appWidgetIds);
    	
        super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}

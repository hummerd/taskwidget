package dima.soft;

import dima.soft.activity.ActionSelect;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;


public class TaskWidgetProvider extends AppWidgetProvider {

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        
        WidgetController updater = new WidgetController(context);
        updater.clearPrefs(appWidgetIds);
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
    	    	
        if (action.equals(UpdateWidgetsTask.LIST_CLICK_ACTION)) {     
        	int wId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        	openCfgGUI(context, wId);        	
        } else if (action.equals(UpdateWidgetsTask.TASKS_CLICK_ACTION)) {
        	openTasksGUI(context);
        }

        super.onReceive(context, intent);
    }    
	
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	LogHelper.i("update widgets started ");
        
    	WidgetController controller = new WidgetController(context);
    	controller.launchUpdateService(appWidgetIds, true);
    	
        super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

    
    private void openTasksGUI(Context context) {
    	LogHelper.i("widget textViewList clicked");
        Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/tasks/android"));
        openBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(openBrowser);    	
    }
    
    private void openCfgGUI(final Context context, final int widgetId) {
    	LogHelper.i("widget textViewTasks clicked");

        Intent openCfg = new Intent(context, ActionSelect.class);
        openCfg.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        openCfg.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	context.startActivity(openCfg);
    }
}

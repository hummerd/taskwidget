package dima.soft;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;
import dima.soft.GoogleServiceAuthentificator.AuthentificatedCallback;
import dima.soft.WidgetController.UpdateService;


public class UpdateWidgetsTask extends AsyncTask<UpdateWidgetsTask.UpdateWidgetParams, Void, UpdateWidgetsTask.UpdateWidgetParams> {
	
    public static final String LIST_CLICK_ACTION = "dima.soft.taskwidget.OPEN_TASKS";
    public static final String TASKS_CLICK_ACTION = "dima.soft.taskwidget.OPEN_CFG";
    
    private final static String NEW_LINE = System.getProperty("line.separator");
    
    
	private static class AccountWidgets {			
		public String accountName;
		public List<Integer> widgtetsIds;
	}
		
    public static class UpdateWidgetParams {
    	public int[] widgetIds;
    	public Context context;
    	public boolean silent;
    }
	
    
	@Override
    protected UpdateWidgetParams doInBackground(UpdateWidgetParams... prms) {
		LogHelper.d("update task started");
		Context context = prms[0].context;
		int[] widgetIds = prms[0].widgetIds;
		
		for (int wId : widgetIds) {
			
			LogHelper.d("Updating widget with id:");
			LogHelper.d(String.valueOf(wId));
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.taskwidget);
			
			setupEvents(context, views, wId);
			LogHelper.d("update task events setted");
			updateData(context, views, wId);
			LogHelper.d("update task started");
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			appWidgetManager.updateAppWidget(wId, views);			
		}
	
		return prms[0];
    }    
	
	@Override
	protected void onPostExecute(UpdateWidgetParams result) {
		LogHelper.d("update task onPostExecute");
		
		Intent intent = new Intent(result.context, UpdateService.class);
		result.context.stopService(intent);
		
		if (!result.silent) {
			int duration = Toast.LENGTH_SHORT;
			Toast.makeText(result.context, "Tasks widget updated", duration).show();
		}
		
		super.onPostExecute(result);
	}
	
	
	protected void setupEvents(Context context, RemoteViews views, int widgetId) {
        Intent intent = new Intent(context, TaskWidgetProvider.class);
        intent.setAction(LIST_CLICK_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.textViewList, actionPendingIntent);
        	       
        
        intent = new Intent(context, TaskWidgetProvider.class);
        intent.setAction(TASKS_CLICK_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.textViewTasks, actionPendingIntent);
	}
	
	protected void updateData(final Context context, final RemoteViews views, final int widgetId)
	{
		String accName = getAccountName(context, widgetId);
		if (accName == null) {
			LogHelper.w("account name is null");
			return;
		}
		
		GoogleServiceAuthentificator auth = new GoogleServiceAuthentificator(TasksClientInfo.AUTH_TOKEN_TYPE, accName, context);
		auth.authentificate(new AuthentificatedCallback() {
			public boolean authentificated(GoogleAccessProtectedResource protectRes, boolean lastTry) {
				return updateList(protectRes, context, views, widgetId);
			}
		});								
	}
	
	
	protected List<AccountWidgets> getGroupedAccounts(Context context, int[] widgetIds) {
		List<AccountWidgets> result = new ArrayList<AccountWidgets>();
		
		for (int wId : widgetIds) {
			LogHelper.d("Updating widget with id:");
			LogHelper.d(String.valueOf(wId));
			
			String accName = getAccountName(context, wId);
			if (accName == null) {
				continue;
			}
			
			AccountWidgets widgets = findAccountWidget(result, accName);
			
			if (widgets == null) {
				widgets = new AccountWidgets();
				widgets.accountName = accName;
				widgets.widgtetsIds = new ArrayList<Integer>();
				result.add(widgets);
			}
			
			widgets.widgtetsIds.add(wId);
		}		
		
		return result;
	}
	
	protected AccountWidgets findAccountWidget(List<AccountWidgets> src, String accountName) {
		for (AccountWidgets aw : src) {
			if (aw.accountName.equals(accountName)) {
				return aw;
			}
		}
		return null;
	}
	
    protected String getAccountName(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(
            	"tw.prefs" + widgetId, 
            	Activity.MODE_PRIVATE);
        
        return prefs.getString("tw.prefs.account", null);
    }
    
    protected boolean updateList(
	    	GoogleAccessProtectedResource protectRes, 
	    	Context context,
	    	RemoteViews views,
	    	int widgetId) {
    	
    	if (protectRes == null) {
    		return false;
    	}
    	
    	GoogleTasksLoader tasksLoader = new GoogleTasksLoader(protectRes);
    	
    	try
    	{
    		LogHelper.i("loading list");
	    	String listId = getListId(context, widgetId);
	    	
	    	if (listId == null || listId.length() == 0) {
	    		LogHelper.w("listid is null");
	    		return true;
	    	}

	    	TaskList list = tasksLoader.getTasksList(listId);
	    	List<Task> tasks = tasksLoader.getTasks(listId);
	    	
	    	StringBuffer bufferTasks = new StringBuffer();
	    	StringBuffer bufferCompletedTasks = new StringBuffer();
	    	for (int i = 0; i < tasks.size(); i++) {
	    		Task tsk = tasks.get(i);
	    		
	    		if (tsk.getStatus().equals("completed")) {
	    			bufferCompletedTasks.append(tsk.getTitle());
	    			bufferCompletedTasks.append(NEW_LINE);
	    		} else {
		    		bufferTasks.append(tsk.getTitle());
		    		bufferTasks.append(NEW_LINE);	    			
	    		}
			}
	    	
	    	if (bufferTasks.length() > 0)
	    		bufferTasks.delete(bufferTasks.length() - NEW_LINE.length(), bufferTasks.length());

	    	if (bufferCompletedTasks.length() > 0)
	    		bufferCompletedTasks.delete(bufferCompletedTasks.length() - NEW_LINE.length(), bufferCompletedTasks.length());
	    	
	    	LogHelper.i("widget update successful");
	    	views.setTextViewText(R.id.textViewList, list.getTitle());
	    	views.setTextViewText(R.id.textViewTasks, bufferTasks);
	    	views.setTextViewText(R.id.textViewCompletedTasks, bufferCompletedTasks);
    	}
    	catch(Exception ex)
    	{
    		LogHelper.w("failed to update tasks list");
    		String msg = ex.toString();
    		if (msg != null && msg.length() != 0) {
    			LogHelper.w(msg);
    		}
    		ex.printStackTrace();
    		return false;
    	}			
    	
    	return true;
    }	
    
    protected String getListId(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(
            	"tw.prefs" + widgetId, 
            	Activity.MODE_PRIVATE);
        
        return prefs.getString("tw.prefs.listid", null);    	
    }
}

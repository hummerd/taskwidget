package com.dima.tkswidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.dima.tkswidget.activity.ActionSelect;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.dima.tkswidget.R;


public class WidgetController {
	public static String ACCOUNT_TYPE = "com.google";
	
	private static final String PREF_SCHEME = "tw.prefs";
	private static final String PREF_ACCOUNT_NAME = "tw.prefs.account";
	private static final String PREF_LIST_ID = "tw.prefs.listid";
	private static final String PREF_LIST_NAME = "tw.prefs.listname";

    //private static final String WIDGET_IDS_EXTRA = "widgetIds";
    //private static final String SILENT_MODE_EXTRA = "silent";
    private final static String NEW_LINE = System.getProperty("line.separator");
    
    private static final String LIST_CLICK_ACTION = "com.dima.taskwidget.OPEN_TASKS";
    private static final String TASKS_CLICK_ACTION = "com.dima.taskwidget.OPEN_CFG";
    private static final String TASKS_UPDATED_ACTION = "com.dima.taskwidget.TASKS_UPDATED";
    
    
	private static class AccountWidgets {			
		public String accountName;
		public List<Integer> widgtetsIds;
	}
	
	
	
	protected final Context m_context;
	//protected final UpdateWidgetsTask m_updateTask;
	protected final TaskProvider m_taskSource;
	
	public WidgetController(Context context) {
		m_context = context;
		m_taskSource = new TaskProvider(m_context);
	}
	

	public void clearPrefs(int[] widgetId) {
		for (int id : widgetId) {
	        SharedPreferences customSharedPreference = getPrefs(id);
	        SharedPreferences.Editor editor = customSharedPreference.edit();
	        editor.clear();
	        editor.commit();			
		}
	}
	
	public void saveWidgetAccount(int widgetId, String accountName) {
        SharedPreferences customSharedPreference = getPrefs(widgetId);
        SharedPreferences.Editor editor = customSharedPreference.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
	}

	public void saveWidgetList(int widgetId, String listId, String listName) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
        SharedPreferences.Editor editor = customSharedPreference.edit();
        editor.putString(PREF_LIST_ID, listId);
        editor.putString(PREF_LIST_NAME, listName);
        editor.commit();
	}
	
	public String loadWidgetAccount(int widgetId) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
		return customSharedPreference.getString(PREF_ACCOUNT_NAME, null);
	}

	public String loadWidgetList(int widgetId) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
		return customSharedPreference.getString(PREF_LIST_ID, null);		
	}

	public String loadWidgetListName(int widgetId) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
		return customSharedPreference.getString(PREF_LIST_NAME, null);		
	}
	
	public void startSync() {
		int[] ids = getWidgetsIds();
		startSync(ids);
	}
	
	public void startSync(int[] widgetIds) {
		List<AccountWidgets> aw = getGroupedAccounts(widgetIds);
		
		for (AccountWidgets accountWidgets : aw) {
			Account acc = getAccount(accountWidgets.accountName);
			ContentResolver.requestSync(acc, TaskMetadata.AUTHORITY, Bundle.EMPTY);
		}
	}
	
	public void setWidgetsIds(int[] widgetId) {
		String[] ids = getWidgetIds();
		HashSet<String> uniqueIds = new HashSet<String>(Arrays.asList(ids));
		
		for (int id : widgetId) {
			uniqueIds.add(Integer.toString(id));		
		}
		
		StringBuffer buffer = new StringBuffer(uniqueIds.size() * 4);
		for (String id : uniqueIds) {
			buffer.append(id);
			buffer.append(",");
		}
		buffer.deleteCharAt(buffer.length() - 1);
		
		SharedPreferences prefs = m_context.getSharedPreferences(PREF_SCHEME + ".widgetIds", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("widgetIds", buffer.toString());
		editor.commit();
	}
	
	public int[] getWidgetsIds() {
		String[] ids = getWidgetIds();
		int[] result = new int[ids.length];
		
		int i = 0;
		for (String id : ids) {
			result[i++] = Integer.parseInt(id);
		}
		
		return result;
	}
	
	public void updateWidgets() {
		int[] ids = getWidgetsIds();
		updateWidgets(ids);
	}
	
	public void updateWidgets(int[] widgetIds) {
		RemoteViews views = new RemoteViews(m_context.getPackageName(), R.layout.taskwidget);
		
		for (int id : widgetIds) {
			LogHelper.i("loading list");
			String listId = loadWidgetList(id);
			TaskList list = m_taskSource.getList(listId);
			List<Task> tasks = m_taskSource.getListTasks(listId);
			
			LogHelper.i("updating widget");
			updateWidget(views, list, tasks);
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(m_context);
			appWidgetManager.updateAppWidget(id, views);	
		}
	}
	
	public void setupEvents(int[] widgetIds) {
		RemoteViews views = new RemoteViews(m_context.getPackageName(), R.layout.taskwidget);
		
		for (int id : widgetIds) {
			LogHelper.i("updating widget events");
			setupEvents(views, id);
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(m_context);
			appWidgetManager.updateAppWidget(id, views);	
		}	
	}
	
	public void notifyUpdateCompleted() {
		Intent intent = new Intent(TASKS_UPDATED_ACTION);
		m_context.sendBroadcast(intent);
	}
	
	public void performAction(String actionName, Intent intent) {
        if (actionName.equals(WidgetController.LIST_CLICK_ACTION)) {     
        	int wId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        	openCfgGUI(wId);        	
        } else if (actionName.equals(WidgetController.TASKS_CLICK_ACTION)) {
        	openTasksGUI();
        } else if (actionName.equals(TASKS_UPDATED_ACTION)) {
        	updateWidgets();
        }
	}
	
	
	
	protected void openTasksGUI() {
    	LogHelper.i("widget textViewList clicked");
        Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/tasks/android"));
        openBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        m_context.startActivity(openBrowser);    	
    }
    
	protected void openCfgGUI( final int widgetId) {
    	LogHelper.i("widget textViewTasks clicked");

        Intent openCfg = new Intent(m_context, ActionSelect.class);
        openCfg.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        openCfg.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
        m_context.startActivity(openCfg);
    }
    
	protected String[] getWidgetIds() {
		SharedPreferences prefs = m_context.getSharedPreferences(PREF_SCHEME + ".widgetIds", Activity.MODE_PRIVATE);
		String idString = prefs.getString("widgetIds", "");
		String[] ids = idString == "" ? new String[0] : idString.split(",");
		return ids;
	}
	
	protected SharedPreferences getPrefs(int widgetId) {
        return m_context.getSharedPreferences(
        		PREF_SCHEME + widgetId, 
            	Activity.MODE_PRIVATE);		
	}
	
	protected void setupEvents(RemoteViews views, int widgetId) {
        Intent intent = new Intent(m_context, TaskWidgetProvider.class);
        intent.setAction(LIST_CLICK_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(m_context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.textViewList, actionPendingIntent);
        	       
        
        intent = new Intent(m_context, TaskWidgetProvider.class);
        intent.setAction(TASKS_CLICK_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        
        actionPendingIntent = PendingIntent.getBroadcast(m_context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.textViewTasks, actionPendingIntent);
	}
	
	protected boolean updateWidget(
		RemoteViews views,
		TaskList list,
		List<Task> tasks) {
	    	
    	try
    	{
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
	
	protected List<AccountWidgets> getGroupedAccounts(int[] widgetIds) {
		List<AccountWidgets> result = new ArrayList<AccountWidgets>();
		
		for (int wId : widgetIds) {
			LogHelper.d("Updating widget with id:");
			LogHelper.d(String.valueOf(wId));
			
			String accName = getAccountName(m_context, wId);
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
    
	protected Account getAccount(String accountName) {
		Account[] accounts = AccountManager.get(m_context).getAccountsByType(ACCOUNT_TYPE);

		if (accounts.length <= 0)
			return null;

		for (int i = 0; i < accounts.length; i++) {
			if (accountName.equals(accounts[i].name)) {
				return accounts[i];
			}
		}
		
		return null;
	}
}

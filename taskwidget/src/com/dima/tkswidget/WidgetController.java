package com.dima.tkswidget;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.dima.tkswidget.activity.ActionSelect;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;


public class WidgetController {
	public static String ACCOUNT_TYPE = "com.google";
	
    //private static final String WIDGET_IDS_EXTRA = "widgetIds";
    //private static final String SILENT_MODE_EXTRA = "silent";
    private final static String NEW_LINE = System.getProperty("line.separator");
    
    private static final String LIST_CLICK_ACTION = "com.dima.taskwidget.OPEN_TASKS";
    private static final String TASKS_CLICK_ACTION = "com.dima.taskwidget.OPEN_CFG";
    
    public static final String TASKS_SYNC_STATE = "com.dima.taskwidget.TASKS_SYNC_STATE";
    public static final int SYNC_STATE_STARTED = 1;
    public static final int SYNC_STATE_LISTS_UPDATED = 2;
    public static final int SYNC_STATE_TASKS_UPDATED = 3;
    public static final int SYNC_STATE_FINISHED = 4;
    
	private static class AccountWidgets {			
		public String accountName;
		public List<Integer> widgtetsIds;
	}
	
	
	protected final Context m_context;
	protected final TaskProvider m_taskSource;
	protected final ComponentName m_name;
	protected final SettingsController m_settings;
	
	
	public WidgetController(Context context) {
		m_context = context;
		m_name = new ComponentName(m_context, TaskWidgetProvider.class.getName());
		m_settings = new SettingsController(m_context);
		m_taskSource = new TaskProvider(m_context);
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
	
	public boolean isSyncInProgress(int widgetId) {
		String accName = m_settings.loadWidgetAccount(widgetId);
		if (accName == null) {
			return false;
		}

		Account acc = getAccount(accName);
		boolean r = ContentResolver.isSyncActive(acc, TaskMetadata.AUTHORITY);
		return r;
	}
	
	public void updateWidgets() {
		int[] ids = getWidgetsIds();
		AppWidgetManager manager = AppWidgetManager.getInstance(m_context);
		
		for (int id : ids) {
			RemoteViews views = getWidgetViews();
			updateWidgets(views, id);	
			manager.updateAppWidget(id, views);			
		}
	}
	
	public RemoteViews getWidgetViews(){
		return new RemoteViews(m_context.getPackageName(), R.layout.taskwidget);
	}
	
	public void updateWidgets(RemoteViews views, int widgetId) {
		LogHelper.d("Updating widget with id:");
		LogHelper.d(String.valueOf(widgetId));

		String listId = m_settings.loadWidgetList(widgetId);
		if (listId == null) {
			return;
		}
		TaskList list = m_taskSource.getList(listId);
		List<Task> tasks = m_taskSource.getListTasks(listId);

		updateWidget(views, list, tasks);
	}
	
	public void applySettings(RemoteViews views, int widgetId) {
		Boolean margin = m_settings.loadWidgetMargin(widgetId);
		setMargin(views, margin, widgetId);
	}
	
	public void setMargin(RemoteViews views, Boolean margin, int widgetId) {
		int v = margin ? View.VISIBLE : View.GONE;
		views.setViewVisibility(R.id.spacer_bottom, v);
		views.setViewVisibility(R.id.spacer_top, v);
		views.setViewVisibility(R.id.spacer_left, v);
		views.setViewVisibility(R.id.spacer_right, v);
	}
	
	public void setupEvents(RemoteViews views, int widgetId) {
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
	
	public void notifySyncState(int flag) {
		Intent intent = new Intent(m_context, TaskWidgetProvider.class);
		intent.setAction(TASKS_SYNC_STATE);
		intent.setFlags(flag);
		m_context.sendBroadcast(intent);
	}
	
	public void performAction(String actionName, Intent intent) {
        if (actionName.equals(LIST_CLICK_ACTION)) {     
        	int wId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        	openCfgGUI(wId);    
        	
        } else if (actionName.equals(TASKS_CLICK_ACTION)) {
        	openTasksGUI();
        	
        } else if (actionName.equals(TASKS_SYNC_STATE)) {
        	int flag = intent.getFlags();
        	
        	if (flag == SYNC_STATE_STARTED) {
        		showUpdateState(true);
        		
        	} else if (flag == SYNC_STATE_TASKS_UPDATED) {
        		updateWidgets();
        		
        	} else if (flag == SYNC_STATE_FINISHED) {
        		showUpdateState(false);
        	}
        }
	}
	
	
	protected void showUpdateState(Boolean updating) {
		int[] widgetIds = getWidgetsIds();
		AppWidgetManager manager = AppWidgetManager.getInstance(m_context);
		
		for (int id : widgetIds) {
			RemoteViews views = getWidgetViews();
				
			int v = updating ? View.VISIBLE : View.GONE;
			views.setViewVisibility(R.id.imageRefresh, v);
			manager.updateAppWidget(id, views);
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
    
	protected int[] getWidgetsIds() {
		AppWidgetManager manager = AppWidgetManager.getInstance(m_context);
		return manager.getAppWidgetIds(m_name);
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
			String accName = m_settings.loadWidgetAccount(wId);
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

package dima.soft;

import android.app.Activity;
//import android.app.AlarmManager;
//import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
//import android.os.SystemClock;


public class WidgetController {
	private static final String PREF_SCHEME = "tw.prefs";
	private static final String PREF_ACCOUNT_NAME = "tw.prefs.account";
	private static final String PREF_LIST_ID = "tw.prefs.listid";
	private static final String PREF_LIST_NAME = "tw.prefs.listname";

    private static final String WIDGET_IDS_EXTRA = "widgetIds";
    private static final String SILENT_MODE_EXTRA = "silent";
	
    
    public static class UpdateService extends Service {  
    	
	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
        	LogHelper.i("update service onStartCommand");
            
        	if (intent == null)
        		return START_REDELIVER_INTENT;
        	
        	int[] appWidgetIds = intent.getExtras().getIntArray(WIDGET_IDS_EXTRA);
        	boolean silent = intent.getExtras().getBoolean(SILENT_MODE_EXTRA);
        	
        	UpdateWidgetsTask.UpdateWidgetParams updateParams = new UpdateWidgetsTask.UpdateWidgetParams();
    		updateParams.widgetIds = appWidgetIds;
    		updateParams.context = this;
    		updateParams.silent = silent;
    		
    		UpdateWidgetsTask task = new UpdateWidgetsTask();
    		LogHelper.d("update service start async task");
    		task.execute(updateParams);
                		
    		LogHelper.i("update service exiting");
	    	return START_REDELIVER_INTENT;
	    }

	    @Override
	    public void onDestroy() {
	    	LogHelper.d("update service onDestroy");
	    	super.onDestroy();
	    }
	    
	    @Override
	    public void onLowMemory() {
	    	LogHelper.d("update service onLowMemory");
	    	super.onLowMemory();
	    }
	    
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }	
    
	
	protected Context m_context;
	protected UpdateWidgetsTask m_updateTask;
	
	
	public WidgetController(Context context) {
		m_context = context;
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
	
	public void launchAlarmUpdate(Context context, int[] widgetIds) {
//		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//		PendingIntent intent = new PendingIntent();
//		
//		alarm.setInexactRepeating(
//				AlarmManager.ELAPSED_REALTIME,  
//				SystemClock.elapsedRealtime() , 
//				AlarmManager.INTERVAL_HALF_HOUR, 
//				intent);
	}
	
	public void launchUpdateService(int[] widgetIds, boolean silent) {
		LogHelper.i("Starting update service");
    	Intent intent = new Intent(m_context, UpdateService.class);
    	intent.putExtra(WIDGET_IDS_EXTRA, widgetIds);
    	m_context.startService(intent);
	}
	
			
	protected SharedPreferences getPrefs(int widgetId) {
        return m_context.getSharedPreferences(
        		PREF_SCHEME + widgetId, 
            	Activity.MODE_PRIVATE);		
	}
}

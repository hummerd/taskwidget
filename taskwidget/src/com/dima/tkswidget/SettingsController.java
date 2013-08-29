package com.dima.tkswidget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsController {
	
	private static final String PREF_SCHEME = "tw.prefs";
	private static final String PREF_ACCOUNT_NAME = "tw.prefs.account";
	private static final String PREF_LIST_ID = "tw.prefs.listid";
	private static final String PREF_LIST_NAME = "tw.prefs.listname";
	
	protected final Context m_context;
	
	
	public SettingsController(Context context) {
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

	public String loadWidgetAccount(int widgetId) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
		return customSharedPreference.getString(PREF_ACCOUNT_NAME, null);
	}
    
	public void saveWidgetList(int widgetId, String listId, String listName) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
        SharedPreferences.Editor editor = customSharedPreference.edit();
        editor.putString(PREF_LIST_ID, listId);
        editor.putString(PREF_LIST_NAME, listName);
        editor.commit();
	}
	
	public String loadWidgetList(int widgetId) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
		return customSharedPreference.getString(PREF_LIST_ID, null);		
	}

	public String loadWidgetListName(int widgetId) {
		SharedPreferences customSharedPreference = getPrefs(widgetId);
		return customSharedPreference.getString(PREF_LIST_NAME, null);		
	}
	
	
	protected SharedPreferences getPrefs(int widgetId) {
        return m_context.getSharedPreferences(
        		PREF_SCHEME + widgetId, 
            	Activity.MODE_PRIVATE);		
	}
}

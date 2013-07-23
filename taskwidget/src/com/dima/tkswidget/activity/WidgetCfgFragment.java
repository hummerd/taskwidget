package com.dima.tkswidget.activity;

import java.util.Arrays;
import java.util.List;

import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.OnSharedPreferenceChangeListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
//import android.preference.Preference;
import android.support.v4.app.FragmentActivity;
//import android.view.View;
//import android.widget.Button;





import com.dima.tkswidget.LogHelper;
import com.dima.tkswidget.R;
//import com.dima.tkswidget.GoogleServiceAuthentificator.AuthentificatedCallback;
import com.dima.tkswidget.TaskMetadata;
import com.dima.tkswidget.TaskProvider;
import com.dima.tkswidget.WidgetController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.services.tasks.model.TaskList;

public class WidgetCfgFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	private int[] m_appWidgetIds;
	private Preference m_tasksListPreference;
	private Preference m_accountPreference;
	private String m_accountName;
	private WidgetController m_widgetController;
	private TaskProvider m_taskProvider;
	private AccountManager m_accountManager;
	private ProgressDialog m_progressDialog;
	private ContentObserver m_listUpdated = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			stopUpdating();
			super.onChange(selfChange, uri);
		}
	};
	private Object m_statusHandle = null;
	private SyncStatusObserver m_updateStatus = new SyncStatusObserver () {
		@Override
		public void onStatusChanged(int which) {
			int f = which;
			f++;
		}
	};
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    initActivity();
	    
	    addPreferencesFromResource(R.xml.cfgpref);
	    //setContentView(R.layout.preflayout);

	    initCustomPrefs();
	    
	    LogHelper.d("onCreate cfg activity");
	    LogHelper.d(String.valueOf(m_appWidgetIds[0]));
	    
	    m_widgetController = new WidgetController(super.getActivity());
	    m_taskProvider = new TaskProvider(super.getActivity());
	    m_accountName = m_widgetController.loadWidgetAccount(m_appWidgetIds[0]);
	    m_tasksListPreference.setEnabled(m_accountName != null);
	    
	    checkAccounts();
	    checkList();
	}
	
	@Override
	public void onStop() {
		if (m_progressDialog != null) {
			m_progressDialog.cancel();
		}
		LogHelper.d("Stop activity");
		super.onStop();
	}
	
	@Override
	public void onPause() {
		LogHelper.d("Pause activity");
		stopUpdating();
		super.onPause();
	}
	
	private void initCustomPrefs() {
	    m_accountPreference = (Preference)findPreference(R.id.pref_account);
	    m_accountPreference.setOnPreferenceClickListener(onAccountSelect);

	    m_tasksListPreference = (Preference)findPreference(R.id.pref_tasks_list);
	    m_tasksListPreference.setOnPreferenceClickListener(onTasksListSelect);
	    
//	    Button btn = (Button)findViewById(R.id.btnok);
//	    btn.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				finishWithOk();
//			}
//		});
	}
	
	private void initActivity() {
		FragmentActivity act = super.getActivity();
		Intent intent = act.getIntent();
		Bundle extras = intent.getExtras();
		
		m_appWidgetIds = new int[] { extras.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, 
            AppWidgetManager.INVALID_APPWIDGET_ID) };
	
	    m_accountManager = AccountManager.get(act);
	}
	
	private void checkAccounts() {
	    Account[] accounts = m_accountManager.getAccountsByType(WidgetController.ACCOUNT_TYPE);
	    if (accounts.length <= 0) {
	    	m_accountPreference.setEnabled(false);
	    	m_accountPreference.setSummary(R.string.addaccount);
	    } else if (accounts.length > 0) {
	    	m_accountPreference.setEnabled(true);
	    	
	    	if (accounts.length == 1 && m_accountName == null) {
	    		m_accountName = accounts[0].name;
	    	}
	    }
	    
	    String summary;
	    if (m_accountName != null) {
	    	summary = super.getResources().getString(R.string.usingaccount, m_accountName);
	    } else {
	    	summary = super.getResources().getString(R.string.selectacount);
	    }
	    
	    m_accountPreference.setSummary(summary);
	}
	
	private void checkList() {
		String listName = m_widgetController.loadWidgetListName(m_appWidgetIds[0]);
		String summary;
		
		if (listName == null) {
			summary = super.getResources().getString(R.string.selectlist);
		} else {
			summary = super.getResources().getString(R.string.usinglist, listName);
		}
		
		m_tasksListPreference.setSummary(summary);
	}
	
	
	private OnPreferenceClickListener onAccountSelect = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			return selectAccount();
	    }
	};
	
	private OnPreferenceClickListener onTasksListSelect = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			selectList();
			return true;
	    }
	};

	private boolean selectAccount() {
		Account[] accounts = m_accountManager.getAccountsByType(WidgetController.ACCOUNT_TYPE);
		
		if (accounts.length <= 0)
			return false;
		
		final String[] accountNames = new String[accounts.length];
		for (int i = 0; i < accountNames.length; i++) {
			accountNames[i] = accounts[i].name;
		}

		//m_accountPreference = (ListPreference)findPreference(R.id.pref_account);
		//m_accountPreference.setEntryValues(accountNames);
		AlertDialog.Builder builder = new AlertDialog.Builder(super.getActivity());
		builder.setTitle("Select account");
		builder.setItems(
			accountNames, 
			new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	storeAccount(accountNames[item]);
		    	updateLists();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		
		return true;
	}
	
	private void storeAccount(String accountName) {
		m_widgetController.saveWidgetAccount(m_appWidgetIds[0], accountName);
		m_accountName = accountName;
		m_tasksListPreference.setEnabled(m_accountName != null);
		checkAccounts();
	}
		
	private void updateLists() {
		int serviceAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(super.getActivity());
		if (serviceAvailable != ConnectionResult.SUCCESS) {
			Dialog d = GooglePlayServicesUtil.getErrorDialog(serviceAvailable, super.getActivity(), 1);
			d.show();
			return;
		}
		
		m_progressDialog = ProgressDialog.show(super.getActivity(), "", super.getResources().getString(R.string.loading), true);
		
		ContentResolver resolver = super.getActivity().getContentResolver();
		resolver.registerContentObserver(TaskMetadata.TASK_LIST_INFO.CONTENT_DIR, true, m_listUpdated);
		m_statusHandle = ContentResolver.addStatusChangeListener(
				ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
				+ ContentResolver.SYNC_OBSERVER_TYPE_PENDING
				+ ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, 
				m_updateStatus);
		m_widgetController.startSync();
	}
	
	private void stopUpdating() {
		ContentResolver resolver = getActivity().getContentResolver();
		resolver.unregisterContentObserver(m_listUpdated);
		
		if (m_statusHandle != null) {
			ContentResolver.removeStatusChangeListener(m_statusHandle);
		}
		
		if (m_progressDialog != null) {
			m_progressDialog.cancel();
		}
	}
	
	private void selectList() {
		final List<TaskList> lists = m_taskProvider.getLists();
		if (lists.size() <= 0)
			return;
		
		final String[] listsNames = new String[lists.size()];
		for (int i = 0; i < listsNames.length; i++) {
			listsNames[i] = lists.get(i).getTitle();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(super.getActivity());
		builder.setTitle("Select list");
		builder.setItems(
			listsNames, 
			new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	int ix = Arrays.asList(listsNames).indexOf(listsNames[item]);
		    	m_widgetController.saveWidgetList(
		    			m_appWidgetIds[0], 
		    			lists.get(ix).getId(),
		    			lists.get(ix).getTitle());
		    	checkList();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
//	private void finishWithOk() {
//		LogHelper.i("finishWithOk");
//		
//		m_widgetController.launchUpdateService(m_appWidgetIds, false);
//		
//		Intent resultValue = new Intent();
//		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_appWidgetIds[0]);
//		setResult(RESULT_OK, resultValue);
//		finish();	
//	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	}
}

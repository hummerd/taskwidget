package com.dima.tkswidget.activity;

import java.util.Arrays;
import java.util.List;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.OnSharedPreferenceChangeListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.dima.tkswidget.GoogleServiceAuthentificator;
import com.dima.tkswidget.LogHelper;
import com.dima.tkswidget.R;
import com.dima.tkswidget.TaskProvider;
import com.dima.tkswidget.WidgetController;
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

	
	private final BroadcastReceiver m_syncFinishedReceiver = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	stopUpdating();
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
	    updateLists();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		LogHelper.d("Stop activity");
	}
	
	@Override
	public void onPause() {
		super.getActivity().unregisterReceiver(m_syncFinishedReceiver);
		stopUpdating();
		super.onPause();
		LogHelper.d("Pause activity");
	}
	
	@Override
	public void onResume() {
		super.getActivity().registerReceiver(m_syncFinishedReceiver, new IntentFilter(WidgetController.TASKS_SYNC_FINISHED));
	    super.onResume();
	    LogHelper.d("Resume activity");
	}
	
	
	private void initCustomPrefs() {
	    m_accountPreference = (Preference)findPreference(R.id.pref_account);
	    m_accountPreference.setOnPreferenceClickListener(onAccountSelect);

	    m_tasksListPreference = (Preference)findPreference(R.id.pref_tasks_list);
	    m_tasksListPreference.setOnPreferenceClickListener(onTasksListSelect);
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
		if (m_accountName == null) {
			return;
		}
		
		final Activity activity = super.getActivity();
		
		GoogleServiceAuthentificator auth = new GoogleServiceAuthentificator(m_accountName, activity);
		auth.authentificateActivityAsync(activity, 2, 3, new Runnable() {
			@Override
			public void run() {
				m_progressDialog = ProgressDialog.show(activity, "", activity.getResources().getString(R.string.loading), true);
				m_widgetController.startSync();
			}
		});
	}
	
	private void stopUpdating() {
		
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
		    	//checkList();
		    	finishWithOk();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void finishWithOk() {
		LogHelper.i("finishWithOk");
		
		m_widgetController.updateWidgets(m_appWidgetIds);
		
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_appWidgetIds[0]);
		super.getActivity().setResult(Activity.RESULT_OK, resultValue);
		super.getActivity().finish();	
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	}
}

package com.dima.tkswidget.activity;

import java.util.Arrays;
import java.util.List;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.OnSharedPreferenceChangeListener;
import org.holoeverywhere.preference.SwitchPreference;
import org.holoeverywhere.widget.Toast;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.dima.tkswidget.LogHelper;
import com.dima.tkswidget.R;
import com.dima.tkswidget.SettingsController;
import com.dima.tkswidget.TaskProvider;
import com.dima.tkswidget.WidgetController;
import com.google.api.services.tasks.model.TaskList;

public class WidgetCfgFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	private int m_appWidgetIds;
	private Preference m_tasksListPreference;
	private Preference m_accountPreference;
	private SwitchPreference m_marginPreference;
	private String m_accountName;
	private TaskProvider m_taskProvider;
	private AccountManager m_accountManager;
	private SettingsController m_settings;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    initActivity();
	    
	    addPreferencesFromResource(R.xml.cfgpref);
	    //setContentView(R.layout.preflayout);

	    initCustomPrefs();
	    
	    LogHelper.d("onCreate cfg activity");
	    LogHelper.d(String.valueOf(m_appWidgetIds));
	    
	    Context context = super.getActivity();
	    m_taskProvider = new TaskProvider(context);
	    m_settings = new SettingsController(context);
	    m_accountName = m_settings.loadWidgetAccount(m_appWidgetIds);
	    m_tasksListPreference.setEnabled(m_accountName != null);
	    
	    setDefaultAccount();
	    setAccountSummary();
	    setListSummary();
	    
	    Boolean margin = m_settings.loadWidgetMargin(m_appWidgetIds);
	    m_marginPreference.setChecked(margin);
	}
	
	
	private void initCustomPrefs() {
	    m_accountPreference = (Preference)findPreference(R.id.pref_account);
	    m_accountPreference.setOnPreferenceClickListener(onAccountSelect);

	    m_tasksListPreference = (Preference)findPreference(R.id.pref_tasks_list);
	    m_tasksListPreference.setOnPreferenceClickListener(onTasksListSelect);
	    
	    m_marginPreference = (SwitchPreference)findPreference(R.id.pref_margin);
	    m_marginPreference.setOnPreferenceChangeListener(onMarginPreferenceChange);
	}
	
	private void initActivity() {
		FragmentActivity act = super.getActivity();
		Intent intent = act.getIntent();
		Bundle extras = intent.getExtras();
		
		m_appWidgetIds = extras.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, 
            AppWidgetManager.INVALID_APPWIDGET_ID);
	
	    m_accountManager = AccountManager.get(act);
	}
	
	private void setDefaultAccount() {
		if (m_accountName != null)
			return;
		
	    Account[] accounts = m_accountManager.getAccountsByType(WidgetController.ACCOUNT_TYPE);
	    if (accounts.length <= 0) {
	    	m_accountPreference.setEnabled(false);
	    	m_accountPreference.setSummary(R.string.addaccount);
	    } else if (accounts.length > 0) {
	    	m_accountPreference.setEnabled(true);
	    	
	    	if (accounts.length == 1) {
	    		storeAccount(accounts[0].name);
	    	}
	    }	
	}
	
	private void setAccountSummary() {    
	    String summary;
	    if (m_accountName != null) {
	    	summary = super.getResources().getString(R.string.usingaccount, m_accountName);
	    } else {
	    	summary = super.getResources().getString(R.string.selectacount);
	    }
	    
	    m_accountPreference.setSummary(summary);
	}
	
	private void setListSummary() {
		String listName = m_settings.loadWidgetListName(m_appWidgetIds);
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

	private OnPreferenceChangeListener onMarginPreferenceChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			m_settings.saveWidgetMargin(m_appWidgetIds, (Boolean)newValue);
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
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		
		return true;
	}
	
	private void storeAccount(String accountName) {
		m_settings.saveWidgetAccount(m_appWidgetIds, accountName);
		m_accountName = accountName;
		m_tasksListPreference.setEnabled(m_accountName != null);
		setAccountSummary();
	}
	
	private void selectList() {
		final List<TaskList> lists = m_taskProvider.getLists();
		if (lists.size() <= 0)
		{
			Toast
				.makeText(super.getActivity(), "No lists found, sync your lists or create new one.", Toast.LENGTH_LONG)
				.show();
			return;
		}
		
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
		    	m_settings.saveWidgetList(
		    			m_appWidgetIds, 
		    			lists.get(ix).getId(),
		    			lists.get(ix).getTitle());
		    	setListSummary();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	}
}

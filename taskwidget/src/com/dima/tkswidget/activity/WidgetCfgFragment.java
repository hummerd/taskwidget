package com.dima.tkswidget.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.dima.tkswidget.LogHelper;
import com.dima.tkswidget.R;
import com.dima.tkswidget.SettingsController;
import com.dima.tkswidget.TaskProvider;
import com.dima.tkswidget.WidgetController;
import com.google.api.services.tasks.model.TaskList;

import java.util.Arrays;
import java.util.List;

public class WidgetCfgFragment extends PreferenceFragment {
	
	private int m_appWidgetId;
	private Preference m_tasksListPreference;
	private Preference m_accountPreference;
    private Preference m_updateFreqPreference;
	private String m_accountName;
	private TaskProvider m_taskProvider;
	private AccountManager m_accountManager;
	private SettingsController m_settings;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    initActivity();
	    
	    addPreferencesFromResource(R.xml.cfgpref);

	    initCustomPrefs();
	    
	    LogHelper.d("onCreate cfg activity");
	    LogHelper.d(String.valueOf(m_appWidgetId));
	    
	    Context context = super.getActivity();
	    m_taskProvider = new TaskProvider(context);
	    m_settings = new SettingsController(context);
	    m_accountName = m_settings.loadWidgetAccount(m_appWidgetId);
	    m_tasksListPreference.setEnabled(m_accountName != null);
	    
	    setDefaultAccount();
	    setAccountSummary();
	    setListSummary();
	    setUpdateFreqSummary();
	}
	
	
	private void initCustomPrefs() {
	    m_accountPreference = (Preference)findPreference("prefAcc");
	    m_accountPreference.setOnPreferenceClickListener(onAccountSelect);

	    m_tasksListPreference = (Preference)findPreference("prefList");
	    m_tasksListPreference.setOnPreferenceClickListener(onTasksListSelect);

        m_updateFreqPreference = (Preference)findPreference("prefUpdateFreq");
        m_updateFreqPreference.setOnPreferenceClickListener(onUpdateFrequencySelect);
	}
	
	private void initActivity() {
		Activity act = super.getActivity();
		Intent intent = act.getIntent();
		Bundle extras = intent.getExtras();
		
		m_appWidgetId = extras.getInt(
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
		String listName = m_settings.loadWidgetListName(m_appWidgetId);
		String summary;
		
		if (listName == null) {
			summary = super.getResources().getString(R.string.selectlist);
		} else {
			summary = super.getResources().getString(R.string.usinglist, listName);
		}
		
		m_tasksListPreference.setSummary(summary);
	}
	
	private void setUpdateFreqSummary() {
        WidgetController controller = new WidgetController(super.getActivity(), null);
        long freq = controller.getSyncFreq(m_appWidgetId);
        
        String summary = findFreqLabel(freq);
		m_updateFreqPreference.setSummary(summary);
	}
	
	private Preference.OnPreferenceClickListener onAccountSelect = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			return selectAccount();
	    }
	};
	
	private Preference.OnPreferenceClickListener onTasksListSelect = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			selectList();
			return true;
	    }
	};

    private Preference.OnPreferenceClickListener onUpdateFrequencySelect = new Preference.OnPreferenceClickListener() {
    	@Override
    	public boolean onPreferenceClick(Preference preference) {
            selectUpdateFreq();
            return true;
        }
    };
	
	private String findFreqLabel(long freq) {
		String[] labels = getResources().getStringArray(R.array.updateFreq);
		
		int[] arr = getResources().getIntArray(R.array.updateFreqVal);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == freq) {
				return labels[i];
			}
		}
		
		return "";
	}
	
	private boolean selectAccount() {
		Account[] accounts = m_accountManager.getAccountsByType(WidgetController.ACCOUNT_TYPE);
		
		if (accounts.length <= 0)
			return false;
		
		final String[] accountNames = new String[accounts.length];
		for (int i = 0; i < accountNames.length; i++) {
			accountNames[i] = accounts[i].name;
		}

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
		m_settings.saveWidgetAccount(m_appWidgetId, accountName);
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
								m_appWidgetId, 
								lists.get(ix).getId(),
								lists.get(ix).getTitle());
						setListSummary();
					}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

    private void selectUpdateFreq() {
        AlertDialog.Builder builder = new AlertDialog.Builder(super.getActivity());
        String[] labels = getResources().getStringArray(R.array.updateFreq);
        builder.setTitle("Select update freq");
        builder.setItems(
        		labels,
        		new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int item) {
        				int[] arr = getResources().getIntArray(R.array.updateFreqVal);
        				setUpdateFreq(arr[item]);
        				setUpdateFreqSummary();
        			}
        		});
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUpdateFreq(long freq) {
        WidgetController controller = new WidgetController(super.getActivity(), null);
        controller.setSyncFreq(freq);
    }
}

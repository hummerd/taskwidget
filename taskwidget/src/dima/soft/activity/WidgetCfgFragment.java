package dima.soft.activity;

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
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.preference.Preference;
import android.support.v4.app.FragmentActivity;
//import android.view.View;
//import android.widget.Button;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.TaskList;

import dima.soft.GoogleServiceAuthentificator;
import dima.soft.GoogleTasksLoader;
import dima.soft.LogHelper;
import dima.soft.R;
import dima.soft.TasksClientInfo;
import dima.soft.WidgetController;
import dima.soft.GoogleServiceAuthentificator.AuthentificatedCallback;

public class WidgetCfgFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	private class LoadListTaskPrms {
		GoogleAccountCredential preotectRes;
		ProgressDialog dialog;
		boolean lastTry;
		List<TaskList> result;
		GoogleServiceAuthentificator authentificator;
	}
	
    private class LoadListsTask extends AsyncTask<LoadListTaskPrms, Void, LoadListTaskPrms> {
    	@Override
        protected LoadListTaskPrms doInBackground(LoadListTaskPrms... params) {
    		try {
    			if (params[0].preotectRes != null) {
	    			GoogleTasksLoader tasksLoader = new GoogleTasksLoader(params[0].preotectRes);
	    			params[0].result = tasksLoader.getTasksLists();
    			}
    		}
    		catch(Exception ex) {
    			ex.printStackTrace();
    		}
    		
    		return params[0];
        }    
    	
    	@Override
    	protected void onPostExecute(LoadListTaskPrms result) {
    		
    		if (result.lastTry && result.result == null) {
    			result.dialog.cancel();
    			return;
    		}
    		
    		if (result.result == null) {
    			result.authentificator.invalidateAuthToken();
    			selectList(true, result.dialog);
    		}
    		else {
    			selectList(result.result);
    			result.dialog.cancel();
    		}
    	}
    }
	
	private int[] m_appWidgetIds;
	private Preference m_tasksListPreference;
	private Preference m_accountPreference;
	private String m_accountName;
	private WidgetController m_widgetController;
	private AccountManager m_accountManager;
	
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
	    m_accountName = m_widgetController.loadWidgetAccount(m_appWidgetIds[0]);
	    m_tasksListPreference.setEnabled(m_accountName != null);
	    
	    checkAccounts();
	    checkList();
	}
	
	@Override
	public void onStop() {
		LogHelper.d("Stop activity");
		super.onStop();
	}
	
	@Override
	public void onPause() {
		LogHelper.d("Pause activity");
		super.onPause();
	}
	
	private void initCustomPrefs() {
	    m_accountPreference = (Preference)findPreference("prefAccount");
	    m_accountPreference.setOnPreferenceClickListener(onAccountSelect);

	    m_tasksListPreference = (Preference)findPreference("prefTasksList");
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
	    Account[] accounts = m_accountManager.getAccountsByType(GoogleServiceAuthentificator.ACCOUNT_TYPE);
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
			return selectList(false, null);		
	    }
	};

	private boolean selectAccount() {
		Account[] accounts = m_accountManager.getAccountsByType(GoogleServiceAuthentificator.ACCOUNT_TYPE);
		
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
		m_widgetController.saveWidgetAccount(m_appWidgetIds[0], accountName);
		m_accountName = accountName;
		m_tasksListPreference.setEnabled(m_accountName != null);
		checkAccounts();
	}
		
	private boolean selectList(final boolean lastTry, ProgressDialog dialog) {
		if (m_accountName == null || m_accountName.length() <= 0)
			return false;
		
		ConnectivityManager conn = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = conn.getActiveNetworkInfo();
		if (ni.getState() != State.CONNECTED) {
			return false;
		}
		
		final ProgressDialog currentDialog = dialog == null 
				? ProgressDialog.show(super.getActivity(), "", super.getResources().getString(R.string.loading), true)
				: dialog;
				
		try {
			final GoogleServiceAuthentificator auth = new GoogleServiceAuthentificator(
					TasksClientInfo.AUTH_TOKEN_TYPE, 
					m_accountName, 
					super.getActivity());
			
			auth.authentificate(super.getActivity(), new AuthentificatedCallback() {
				public boolean authentificated(GoogleAccountCredential protectRes, boolean lt) {			
					LoadListTaskPrms taskPrms = new LoadListTaskPrms();
					taskPrms.dialog = currentDialog;
					taskPrms.preotectRes = protectRes;
					taskPrms.lastTry = lastTry;
					taskPrms.authentificator = auth;
					
					LoadListsTask task = new LoadListsTask();
					task.execute(taskPrms);
					return true;
				}
			});
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return true;
	}
	
	private void selectList(final List<TaskList> lists) {
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
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
}

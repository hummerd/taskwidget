package com.dima.tkswidget.activity;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.PreferenceActivity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dima.tkswidget.GoogleServiceAuthentificator;
import com.dima.tkswidget.LogHelper;
import com.dima.tkswidget.R;
import com.dima.tkswidget.SettingsController;
import com.dima.tkswidget.WidgetController;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


/**
 * @author Dima Kozlov
 *
 */
public class WidgetCfg extends PreferenceActivity {
	
	private WidgetController m_widgetController;
	private SettingsController m_settings;
	private ProgressDialog m_progressDialog = null;
	private int[] m_appWidgetIds;
	private MenuItem m_refreshMenu = null;
	
	private final BroadcastReceiver m_syncFinishedReceiver = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	stopUpdating();
	    }
	};
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        super.getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, new WidgetCfgFragment())
            .commit();
        
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		m_widgetController = new WidgetController(this);
		m_settings = new SettingsController(this);
		m_appWidgetIds = new int[] { extras.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, 
            AppWidgetManager.INVALID_APPWIDGET_ID) };
    }
    
	@Override
	public void onPause() {
		unregisterReceiver(m_syncFinishedReceiver);
		stopUpdating();
		super.onPause();
		LogHelper.d("Pause activity");
	}
	
	@Override
	public void onResume() {
		registerReceiver(m_syncFinishedReceiver, new IntentFilter(WidgetController.TASKS_SYNC_FINISHED));
	    super.onResume();
	    LogHelper.d("Resume activity");
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = super.getSupportMenuInflater();
        inflater.inflate(R.menu.cfgmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.cfgmenu_done:
            	finishWithOk();
                return true;
            case R.id.cfgmenu_refresh:
            	refresh(item);
            	updateLists();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void refresh(MenuItem item) {
    	m_refreshMenu = item;
    	
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_view, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.drawable.anim_rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        item.setActionView(iv);
    }
    
	private void finishWithOk() {
		LogHelper.i("finishWithOk");
		
		m_widgetController.setupEvents(m_appWidgetIds);
		m_widgetController.updateWidgets(m_appWidgetIds);
		
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_appWidgetIds[0]);
		setResult(Activity.RESULT_OK, resultValue);
		finish();	
	}
	
	private void updateLists() {
		String accountName = m_settings.loadWidgetAccount(m_appWidgetIds[0]);
		
		if (accountName == null) {
			stopUpdating();
			return;
		}
		
		GoogleServiceAuthentificator auth = new GoogleServiceAuthentificator(accountName, this);
		auth.authentificateActivityAsync(this, 2, 3, 
			new Runnable() {
				@Override
				public void run() {
					//m_progressDialog = ProgressDialog.show(WidgetCfg.this, "", getResources().getString(R.string.loading), true);
					m_widgetController.startSync();
				}
			},
			new Runnable() {
				@Override
				public void run() {
					stopUpdating();
					
				}
			});
	}
	
	private void stopUpdating() {	
		if (m_progressDialog != null) {
			m_progressDialog.cancel();
		}
		
		if (m_refreshMenu != null) {
			View v = m_refreshMenu.getActionView();
			if (v != null) {
				v.clearAnimation();
				m_refreshMenu.setActionView(null);	
			}
		}
	}
}




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
import android.widget.RemoteViews;


/**
 * @author Dima Kozlov
 *
 */
public class WidgetCfg extends PreferenceActivity {
	
	private WidgetController m_widgetController;
	private SettingsController m_settings;
	private ProgressDialog m_progressDialog = null;
	private int m_appWidgetId;
	private MenuItem m_refreshMenu = null;
	
	private final BroadcastReceiver m_syncFinishedReceiver = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	switch (intent.getFlags()) {
			case WidgetController.SYNC_STATE_STARTED:
				refresh();
				break;
			case WidgetController.SYNC_STATE_FINISHED: 
			case WidgetController.SYNC_STATE_LISTS_UPDATED:
				stopUpdating();
				break;
			}
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
		m_appWidgetId = extras.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, 
            AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    
	@Override
	public void onPause() {
		super.onPause();
		LogHelper.d("Pause activity");
		
		unregisterReceiver(m_syncFinishedReceiver);
		stopUpdating();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		LogHelper.d("Resume activity");
		
		registerReceiver(m_syncFinishedReceiver, new IntentFilter(WidgetController.TASKS_SYNC_STATE));
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = super.getSupportMenuInflater();
        inflater.inflate(R.menu.cfgmenu, menu);
        boolean r = super.onCreateOptionsMenu(menu);
        return r;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	boolean r = super.onPrepareOptionsMenu(menu);
    	
    	 m_refreshMenu = menu.findItem(R.id.cfgmenu_refresh);
		boolean syncInProgress = m_widgetController.isSyncInProgress(m_appWidgetId);
		if (syncInProgress) {
			refresh();
		}
		
		return r;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.cfgmenu_done:
            	finishWithOk();
                return true;
            case R.id.cfgmenu_refresh:
            	refresh();
            	updateLists();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void refresh() {
		View v = m_refreshMenu.getActionView();
		if (v != null)
			return;

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_view, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.drawable.anim_rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        m_refreshMenu.setActionView(iv);
    }
    
	private void finishWithOk() {
		LogHelper.i("finishWithOk");
		
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_appWidgetId);
		setResult(Activity.RESULT_OK, resultValue);
		finish();	
		
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
    	RemoteViews views = m_widgetController.getWidgetViews();
    	m_widgetController.setupEvents(views, m_appWidgetId);
    	m_widgetController.updateWidgets(views, m_appWidgetId);
    	m_widgetController.applySettings(views, m_appWidgetId);
    	manager.updateAppWidget(m_appWidgetId, views);
	}
	
	private void updateLists() {
		String accountName = m_settings.loadWidgetAccount(m_appWidgetId);
		
		if (accountName == null) {
			stopUpdating();
			return;
		}
		
		GoogleServiceAuthentificator auth = new GoogleServiceAuthentificator(accountName, this);
		auth.authentificateActivityAsync(this, 2, 3, 
			new Runnable() {
				@Override
				public void run() {	
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




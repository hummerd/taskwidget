package com.dima.tkswidget.activity;

import com.dima.tkswidget.GoogleServiceAuthenticator;
import com.dima.tkswidget.LogHelper;
import com.dima.tkswidget.R;
import com.dima.tkswidget.SettingsController;
import com.dima.tkswidget.WidgetController;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


/**
 * @author Dima Kozlov
 */
public class WidgetCfg extends Activity {

  private WidgetController m_widgetController;
  private SettingsController m_settings;
  private int m_appWidgetId;
  private MenuItem m_refreshMenu = null;

  private final BroadcastReceiver m_syncFinishedReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      int flag = intent.getIntExtra(WidgetController.TASKS_SYNC_STATE, -1);
      LogHelper.d("WidgetCfg got " + flag);
      switch (flag) {
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

  public static void showWidgetCfg(Context context, int widgetId) {
    Intent openCfg = new Intent(context, WidgetCfg.class);
    openCfg.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
    openCfg.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    context.startActivity(openCfg);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    LogHelper.d("Resume activity");
    super.onCreate(savedInstanceState);

    super.getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new WidgetCfgFragment())
        .commit();

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    m_widgetController = new WidgetController(this, null);
    m_settings = new SettingsController(this);
    m_appWidgetId = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID);
  }

  @Override
  public void onPause() {
    LogHelper.d("Pause activity");
    super.onPause();

    unregisterReceiver(m_syncFinishedReceiver);
    stopUpdating();
  }

  @Override
  public void onResume() {
    LogHelper.d("Resume activity");
    super.onResume();

    boolean syncInProgress = m_widgetController.isSyncInProgress(m_appWidgetId);
    if (syncInProgress) {
      refresh();
    }

    registerReceiver(m_syncFinishedReceiver, new IntentFilter(WidgetController.TASKS_SYNC_STATE));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = super.getMenuInflater();
    inflater.inflate(R.menu.cfgmenu, menu);
    return true;
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
    LogHelper.d("Refresh");

    if (m_refreshMenu == null)
      return;

    View v = m_refreshMenu.getActionView();
    if (v != null)
      return;

    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_view, null);

    Animation rotation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
    iv.startAnimation(rotation);

    m_refreshMenu.setActionView(iv);
  }

  private void finishWithOk() {
    LogHelper.i("finishWithOk");

    Intent resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_appWidgetId);
    setResult(Activity.RESULT_OK, resultValue);
    finish();

    m_widgetController.updateWidgetsAsync(new int[]{ m_appWidgetId });
  }

  private void updateLists() {
    String accountName = m_settings.loadWidgetAccount(m_appWidgetId);

    if (accountName == null) {
      stopUpdating();
      return;
    }

    Account[] accounts = AccountManager.get(this).getAccounts();
    Account account = null;
    for (Account a : accounts) {
      if (a.name.equals(accountName)) {
        account = a;
        break;
      }
    }

    if (account == null) {
      stopUpdating();
      return;
    }

    GoogleServiceAuthenticator auth = new GoogleServiceAuthenticator(account, this);
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
    LogHelper.d("Stop updating");
    if (m_refreshMenu != null) {
      View v = m_refreshMenu.getActionView();
      if (v != null) {
        v.clearAnimation();
        m_refreshMenu.setActionView(null);
      }
    }
  }
}




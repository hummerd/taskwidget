package dima.soft.activity;

import dima.soft.R;
import dima.soft.WidgetController;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


public class ActionSelect extends Activity {
	
	private int[] m_appWidgetIds;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		tryFillWidgetId();
		showActionDialog();
	}
	
	private void showActionDialog() {	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setItems(R.array.cfgactions, new DialogInterface.OnClickListener() {				
				public void onClick(DialogInterface dialog, int which) {
					executeAction(which);
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finish();					
				}
			})
			.setTitle("Choose action")
			.create()
			.show();
	}
	
	private void executeAction(int actionId) {
    	switch (actionId) {
		case 0: // 0 - update
			WidgetController controller = new WidgetController(this);
			controller.launchUpdateService(m_appWidgetIds, false);
			break;
		case 1: // 1 - configure
	        Intent openCfg = new Intent(this, WidgetCfg.class);
	        openCfg.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_appWidgetIds[0]);
	        openCfg.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(openCfg);					
			break;					
		}
    	
    	finish();
	}
	
	private void tryFillWidgetId() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		m_appWidgetIds = new int[] { extras.getInt(
	            AppWidgetManager.EXTRA_APPWIDGET_ID, 
	            AppWidgetManager.INVALID_APPWIDGET_ID) };		
	}
}

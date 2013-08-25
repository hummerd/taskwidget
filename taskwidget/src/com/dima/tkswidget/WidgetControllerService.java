package com.dima.tkswidget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dima on 8/25/13.
 */
public class WidgetControllerService extends IntentService {
    private static final String UPDATE_ACTION = "com.dima.taskwidget.controller.service.UPDATE";
    private static final String WIDGET_IDS  = "com.dima.taskwidget.extra.WIDGETIDS";
    private static final String WIDGET_ID  = "com.dima.taskwidget.extra.WIDGETID";


    public static void updateWidgets(Context context, int[] widgetIds) {
        Intent i = new Intent(context, WidgetControllerService.class);
        i.setAction(UPDATE_ACTION);
        i.putExtra(WIDGET_IDS, widgetIds);
        context.startService(i);
    }


    public WidgetControllerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        if (action.equals(UPDATE_ACTION)) {
            int[] ids = intent.getIntArrayExtra(WIDGET_IDS);
            WidgetController controller = new WidgetController(this, null);
            controller.updateWidgets(ids);
        }
    }
}

package dima.soft.activity;

import org.holoeverywhere.preference.PreferenceActivity;

import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
//import android.preference.Preference.OnPreferenceClickListener;
//import android.preference.PreferenceActivity;

/**
 * @author Administrator
 *
 */
public class WidgetCfg extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new WidgetCfgFragment())
                .commit();
    }
}




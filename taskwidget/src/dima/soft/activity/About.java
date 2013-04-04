package dima.soft.activity;

import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceActivity;

import android.os.Bundle;
//import android.preference.Preference;
//import android.preference.PreferenceActivity;
//import android.preference.Preference.OnPreferenceClickListener;
import dima.soft.R;


public class About extends PreferenceActivity {
	
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    addPreferencesFromResource(R.xml.aboutpref);
	    initCustomPrefs();
	}
	
	
	@SuppressWarnings("deprecation")
	private void initCustomPrefs() {
	    Preference customPref = (Preference)findPreference("prefAccount");
	    customPref.setOnPreferenceClickListener(onShowAboutDialog);
	
	}
	
	private OnPreferenceClickListener onShowAboutDialog = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			
			return false;
	    }
	};
}

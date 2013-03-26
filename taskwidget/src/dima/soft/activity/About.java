package dima.soft.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import dima.soft.R;


public class About extends PreferenceActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    addPreferencesFromResource(R.xml.aboutpref);
	    initCustomPrefs();
	}
	
	
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

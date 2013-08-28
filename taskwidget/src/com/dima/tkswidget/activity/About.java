package com.dima.tkswidget.activity;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class About extends ListActivity {

	private class AboutItem	{
		public CharSequence mName;
		public CharSequence mDescription;
		
		public AboutItem(CharSequence name, CharSequence description) {
			mName = name;
			mDescription = description;
		}
	}
	
	private class AboutItemAdapter extends ArrayAdapter<AboutItem> {
		private AboutItem[] mObjects;
		
		
		public AboutItemAdapter(Context context, AboutItem[] objects) {
			super(context, android.R.id.text1, objects);
			mObjects = objects;
		}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }
		       
			AboutItem obj = mObjects[position];
			TextView textName = (TextView)convertView.findViewById(android.R.id.text1);
			textName.setText(obj.mName);
			
			TextView textDesc = (TextView)convertView.findViewById(android.R.id.text2);
			textDesc.setText(obj.mDescription);
			return convertView;
		}
	}
	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    AboutItem[] items = new AboutItem[] { 
	    		new AboutItem("Version", "Version description"), 
	    		new AboutItem("License", "License description") };
	    AboutItemAdapter adapter = new AboutItemAdapter(this, items);
	    setListAdapter(adapter);
	}
}

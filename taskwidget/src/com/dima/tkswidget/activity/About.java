package com.dima.tkswidget.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dima.tkswidget.WidgetController;


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
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    AboutItem[] items = new AboutItem[] {
					new AboutItem("Refresh", "Refresh all widgets"),
					new AboutItem("Version", "Version description"),
	    		new AboutItem("License", "License description") };
	    AboutItemAdapter adapter = new AboutItemAdapter(this, items);
	    setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0) {
			WidgetController wc = new WidgetController(this, null);
			wc.updateWidgets();
		}
	}
}

package com.dima.tkswidget;

import android.util.Log;

public class LogHelper {
	private static final String tag = "gtw";
	
	public static void e(String msg, Exception e) {
		Log.e(tag, msg, e);
	}
	
	public static void i(String msg) {
		Log.i(tag, msg);
	}
	
	public static void d(String msg) {
		Log.d(tag, msg);
	}
	
	public static void w(String msg) {
		Log.w(tag, msg);
	}
}
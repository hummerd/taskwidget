package com.dima.tkswidget;

import android.util.Log;

import java.util.Arrays;

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

    public static void d(String msg, int[] array) {
        Log.d(tag, msg);
        Log.d(tag, Arrays.toString(array));
    }

	public static void w(String msg) {
		Log.w(tag, msg);
	}
}

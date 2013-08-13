package com.dima.tkswidget;

import java.io.IOException;
import java.util.Collections;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.UserRecoverableNotifiedException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.TasksScopes;


/**
 * Don't forget to to register app with certificate fingerprint in api console.
 * 
 * @author Dima Kozlov
 */
public class GoogleServiceAuthentificator {

	private static void processAuthError(Exception exc, Activity activity, int requestCodePlayServ, int requestRecoverAuth) {
		if (exc.getClass() == GooglePlayServicesAvailabilityException.class) {
    		GooglePlayServicesAvailabilityException e = (GooglePlayServicesAvailabilityException)exc;
    	    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
    	    	e.getConnectionStatusCode(),
    	    	activity,
    	    	requestCodePlayServ);
    	    dialog.show();
    	    
    	} else if (exc.getClass() == UserRecoverableAuthException.class) {
    		UserRecoverableAuthException e = (UserRecoverableAuthException)exc;
    		activity.startActivityForResult(
    		   	e.getIntent(), 
    		   	requestRecoverAuth);
    	}
	}
	
	
	protected String m_accountName;
	protected GoogleAccountCredential m_accessProtectedResource = null;
	protected String m_scope;
	protected Context m_context;
	
	
	public GoogleServiceAuthentificator(
			String accountName, 
			Context context) {
		m_accountName = accountName;
		m_context = context;
		m_accessProtectedResource = GoogleAccountCredential.usingOAuth2(m_context, Collections.singleton(TasksScopes.TASKS));
		m_scope = m_accessProtectedResource.getScope();
		m_accessProtectedResource.setSelectedAccountName(m_accountName);
	}

	public void authentificateActivityAsync(
			final Activity activity, 
			final int requestCodePlayServ, 
			final int requestRecoverAuth, 
			final Runnable onAuthSucceded,
			final Runnable onAuthFailed) 
	{
		AsyncTask<Void, Void, Exception> task = new AsyncTask<Void, Void, Exception>() {
		    @Override
		    protected Exception doInBackground(Void... params) {
		    	try {
					GoogleAuthUtil.getToken(m_context, m_accountName, m_scope);
				} catch (Exception e) {
					LogHelper.e("Exception on GoogleAuthUtil.getToken: " + e.getMessage(), e);
					e.printStackTrace();
					return e;
				}
				
				return null;
		    }
		    
		    @Override
		    protected void onPostExecute(Exception result) {
		    	super.onPostExecute(result);
		    	
		    	if (result == null && onAuthFailed != null) {
		    		onAuthSucceded.run();	
		    	} else {
		    		onAuthFailed.run();
		    		processAuthError(result, activity, requestCodePlayServ, requestRecoverAuth);
		    	}
		    }
		};
		task.execute((Void)null);
	}
	
	public String authentificateActivity(final Activity activity, final int requestCodePlayServ, final int requestRecoverAuth) 
	{
		try {
			return GoogleAuthUtil.getToken(m_context, m_accountName, m_scope);
		} catch (Exception exc) {
			LogHelper.e("Exception on GoogleAuthUtil.getToken: " + exc.getMessage(), exc);
			exc.printStackTrace();
			processAuthError(exc, activity, requestCodePlayServ, requestRecoverAuth);
		}

		return null;
	}

	public String authentificateSyncAdapter(String authority, Bundle syncBundle) 
			throws UserRecoverableNotifiedException, IOException, GoogleAuthException {
		Bundle b = new Bundle();
		String token = GoogleAuthUtil.getTokenWithNotification(
				m_context, 
				m_accountName, 
				m_scope, 
				b, 
				authority, 
				syncBundle);
		
		return token;
	}
	
	public GoogleAccountCredential getAccessProtectedResource() {
		return m_accessProtectedResource;
	}
}
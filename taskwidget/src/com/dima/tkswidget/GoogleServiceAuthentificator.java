package com.dima.tkswidget;

import java.io.IOException;
import java.util.Collections;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.TasksScopes;


/**
 * Don't forget to to register app with certificate fingerprint in api console.
 * 
 * @author Dima Kozlov
 */
public class GoogleServiceAuthentificator {
	
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

	public void authentificateActivity() 
			throws IOException, GoogleAuthException {
		GoogleAuthUtil.getToken(m_context, m_accountName, m_scope);
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
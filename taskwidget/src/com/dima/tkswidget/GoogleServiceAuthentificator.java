package com.dima.tkswidget;

import java.io.IOException;
import java.util.Collections;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.TasksScopes;

import android.content.Context;



public class GoogleServiceAuthentificator {
	protected String m_accountName;
	protected GoogleAccountCredential m_accessProtectedResource = null;
	protected Context m_context;
	
	
	public GoogleServiceAuthentificator(
			String accountName, 
			Context context) {
		m_accountName = accountName;
		m_context = context;
	}

	public void authentificate() 
			throws IOException, GoogleAuthException {
		authentificateInternal(m_accountName, m_context);
	}

	public GoogleAccountCredential getAccessProtectedResource() {
		return m_accessProtectedResource;
	}
	
	protected void authentificateInternal(
			final String account,
			final Context context) 
					throws IOException, GoogleAuthException {
		LogHelper.d("authentificateInternal");
		m_accessProtectedResource = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(TasksScopes.TASKS));
		m_accessProtectedResource.setSelectedAccountName(account);
		//m_accessProtectedResource.getToken();
	}
}
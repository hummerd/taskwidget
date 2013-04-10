package dima.soft;

import java.io.IOException;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;



public class GoogleServiceAuthentificator {

	public static String ACCOUNT_TYPE = "com.google";
	
	
	public interface AuthentificatedCallback {
		boolean authentificated(GoogleAccountCredential protectRes, boolean lastTry);	
	}

	
	protected String m_accountName;
	protected String m_authToken;
	protected String m_authTokenType;
	protected GoogleAccountCredential m_accessProtectedResource = null;
	protected Context m_context;
	
	
	public GoogleServiceAuthentificator(
			String authTokenType, 
			String accountName, 
			Context context) {
		m_authTokenType = authTokenType;
		m_accountName = accountName;
		m_context = context;
	}

	public void authentificate(
			final Activity activity,
			final AuthentificatedCallback callback) {
		AccountManager accountManager = AccountManager.get(m_context);
		Account account = getAccount(accountManager);
		authentificateInternal(account, accountManager, activity, callback, false);
	}
	
	public void authentificate(final AuthentificatedCallback callback) {
		AccountManager accountManager = AccountManager.get(m_context);
		Account account = getAccount(accountManager);
		authentificateInternal(accountManager, account, callback, false);
	}

	public void invalidateAuthToken() {
		LogHelper.d("invalidate auth token");
		if (m_authToken != null) {
			AccountManager accountManager = AccountManager.get(m_context);
			accountManager.invalidateAuthToken(ACCOUNT_TYPE, m_authToken);
		}
	}
	
	public String getAuthToken() {
		return m_authToken;
	}

	public GoogleAccountCredential getAccessProtectedResource() {
		return m_accessProtectedResource;
	}
	
	
	protected Account getAccount(AccountManager accountManager) {
		Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

		if (accounts.length <= 0)
			return null;

		for (int i = 0; i < accounts.length; i++) {
			if (m_accountName.equals(accounts[i].name)) {
				return accounts[i];
			}
		}
		
		return null;
	}
	
	protected void authentificateInternal(
			final Account account,
			final AccountManager accountManager,
			final Activity activity,
			final AuthentificatedCallback callback,
			final boolean lastTry) {
		LogHelper.d("authentificateInternal with activity");
		m_accessProtectedResource = GoogleAccountCredential.usingOAuth2(activity, m_authToken);
		
//		accountManager.getAuthToken(account, m_authTokenType, null, activity,
//				new AccountManagerCallback<Bundle>() {
//					public void run(AccountManagerFuture<Bundle> future) {
//						m_authToken = null;
//						m_accessProtectedResource = null;
//						
//						try {
//							Bundle bundle = future.getResult();
//							
//							if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
//								LogHelper.d("got auth token");
//								m_authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
//								m_accessProtectedResource = GoogleAccountCredential.usingOAuth2(m_authToken);
//				            } else {
//				            	LogHelper.w("no auth token in bundle");
//				            }
//						} catch (OperationCanceledException e) {
//							e.printStackTrace();
//						} catch (AuthenticatorException e) {
//							e.printStackTrace();
//						} catch (IOException e) {
//							e.printStackTrace();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}	
//						
//						boolean actionSucceded = false;
//						if (lastTry || m_accessProtectedResource != null) {
//							actionSucceded = callback.authentificated(m_accessProtectedResource, lastTry);
//						}
//						
//						if(!actionSucceded)
//						{
//							LogHelper.d("possibly wrong auth token");
//							invalidateAuthToken();
//							if (!lastTry) {
//								authentificateInternal(account, accountManager, activity, callback, true);
//							}
//						}
//					}
//				},
//				null);	
	}
	
	public void authentificateInternal(
			AccountManager accountManager,
			Account account,			
			final AuthentificatedCallback callback, 
			boolean lastTry) {	
		m_authToken = null;
		m_accessProtectedResource = null;
		
		try {
			LogHelper.d("getting auth token");
			m_authToken = accountManager.blockingGetAuthToken(account, m_authTokenType, true);
			
			if (m_authToken != null) {
				LogHelper.d("got auth token");
				//m_accessProtectedResource = GoogleAccountCredential( );
			} else {
				LogHelper.w("failed to get auth token");
			}
		} catch (OperationCanceledException e) {
			LogHelper.w("user canceled access to tasks service");
		} catch (AuthenticatorException e) {
			LogHelper.w("authentificator exception");
		} catch (IOException e) {
			LogHelper.w(e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean actionSucceded = false;
		if (lastTry || m_accessProtectedResource != null) {
			actionSucceded = callback.authentificated(m_accessProtectedResource, lastTry);
		}
		
		if(!actionSucceded)
		{
			LogHelper.d("possibly wrong auth token");
			invalidateAuthToken();
			if (!lastTry) {
				authentificateInternal(accountManager, account, callback, true);
			}
		}
	}
}
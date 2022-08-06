package com.grieex.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.grieex.ui.MainActivity;

/*
 * Implement AbstractAccountAuthenticator and stub out all
 * of its methods
 */
class Authenticator extends AbstractAccountAuthenticator {

	private final Context mContext;

	// Simple constructor
	public Authenticator(Context context) {
		super(context);

		this.mContext = context;
	}

	// Editing properties is not supported
	@Override
	public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
		throw new UnsupportedOperationException();
	}

	// Don't add additional accounts
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse r, String s, String s2, String[] strings, Bundle options) {

		// Log.d("udinic", TAG + "> addAccount");

		final Intent intent = new Intent(mContext, MainActivity.class);
		// intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType);
		// intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
		// intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT,
		// true);
		// intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
		// response);

		Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;

	}

	// Ignore attempts to confirm credentials
	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse r, Account account, Bundle bundle) {
		return null;
	}

	// Getting an authentication token is not supported
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse r, Account account, String s, Bundle bundle) {
		throw new UnsupportedOperationException();
	}

	// Getting a label for the auth token is not supported
	@Override
	public String getAuthTokenLabel(String s) {
		throw new UnsupportedOperationException();
	}

	// Updating user credentials is not supported
	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse r, Account account, String s, Bundle bundle) {
		throw new UnsupportedOperationException();
	}

	// Checking features for the account is not supported
	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse r, Account account, String[] strings) {
		throw new UnsupportedOperationException();
	}
}
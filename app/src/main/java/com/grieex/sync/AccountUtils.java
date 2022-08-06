package com.grieex.sync;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.grieex.helper.Constants;
public class AccountUtils {


    public static void createAccount(Context context) {
        // try to create a new account
        AccountManager manager = AccountManager.get(context);
        Account account = new Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE);

        boolean isNewAccountAdded;
        try {
            isNewAccountAdded = manager != null && manager.addAccountExplicitly(account, null, null);
        } catch (SecurityException e) {
            // Timber.e(e,
            // "Setting up account...FAILED Account could not be added");
            return;
        }
        if (isNewAccountAdded) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, Constants.CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync
            // when the network is up
            ContentResolver.setSyncAutomatically(account, Constants.CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system
            // may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(account, Constants.CONTENT_AUTHORITY, new Bundle(), Constants.SYNC_FREQUENCY);
        }

        // Timber.d("Setting up account...DONE");
    }

//	public static void removeAccount(Activity activity) {
//		AccountManager manager = AccountManager.get(activity);
//		Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE);
//		for (Account account : accounts) {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//				manager.removeAccount(account, activity, null, null);
//			} else {
//				manager.removeAccount(account, null, null);
//			}
//		}
//	}

    public static boolean isAccountExists(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        AccountManager manager = AccountManager.get(context);

        Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE);
        return accounts.length > 0;
    }

    public static Account getAccount(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE);

        // return first available account
        if (accounts.length > 0) {
            return accounts[0];
        }

        return null;
    }

    public static void startSync(Context context) {
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        //AccountManager manager = AccountManager.get(context);
        Account account = new Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
        ContentResolver.requestSync(account, Constants.CONTENT_AUTHORITY, new Bundle());
    }
}

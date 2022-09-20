package com.grieex.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dropbox.core.android.Auth;
import com.grieex.GrieeX;
import com.grieex.R;
import com.grieex.dropbox.DropboxClientFactory;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.helper.Utils;
import com.grieex.ui.dialogs.BackupRestoreDialog;
import com.grieex.ui.dialogs.BatchProcessingDialog;
import com.grieex.ui.dialogs.DropboxDialog;
import com.grieex.ui.dialogs.ThemeDialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getName();
    private String UilCachePath;
    private TextView tvClearCache;
    private TextView tvLanguage;
    private ImageView ivDropboxOk;
    private Context mContext;

//    DropboxAPI<AndroidAuthSession> mApi;
//    private boolean mDropboxLoggedIn;

    private int iSelectedLanguage = 0;
    private int iSelectedNotificationTime = 0;
    private boolean ShowDropboxDialog = false;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (GrieeXSettings.RELEASE_MODE) {
            GrieeX.getInstance().trackScreenView(this.getClass().getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        try {
//            if (Utils.isProInstalled(mContext)) {
//                // We create a new AuthSession so that we can use the Dropbox API.
//                AndroidAuthSession session = buildSession();
//                mApi = new DropboxAPI<>(session);
//                // checkAppKeySetup();
//                // Display the proper UI state if logged in or not
//                setLoggedIn(mApi.getSession().isLinked());
//            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isProInstalled(mContext)) {
            DropboxInit();

            if (ShowDropboxDialog) {
                ivDropboxOk.setVisibility(View.VISIBLE);
                DropboxDialog d = new DropboxDialog(mContext);
                d.setCustomEventListener(new DropboxDialog.OnCustomEventListener() {
                    @Override
                    public void onLogoutDropbox() {
                        logOut();
                        onResume();
                    }
                });
                d.showDialog();
            }
        }


//        if (Utils.isProInstalled(mContext)) {
//            AndroidAuthSession session = mApi.getSession();
//
//            // The next part must be inserted in the onResume() method of the
//            // activity from which session.startAuthentication() was called, so
//            // that Dropbox authentication completes properly.
//            if (session.authenticationSuccessful()) {
//                try {
//                    // Mandatory call to complete the auth
//                    session.finishAuthentication();
//
//                    // Store it locally in our app for later use
//                    storeAuth(session);
//                    setLoggedIn(true);
//
//                    // String DropboxLastSyncDate =
//                    // Prefs.with(this).getString(Constants.Pref_Dropbox_Last_Update_Date,
//                    // "");
//                    // if (TextUtils.isEmpty(DropboxLastSyncDate)) {
//                    // SyncDropBox();
//                    // }
//
//                    if (ShowDropboxDialog) {
//                        ivDropboxOk.setVisibility(View.VISIBLE);
//                        DropboxDialog d = new DropboxDialog(mContext, mApi);
//                        d.setCustomEventListener(new DropboxDialog.OnCustomEventListener() {
//                            @Override
//                            public void onLogoutDropbox() {
//                                logOut();
//                                onResume();
//                            }
//                        });
//                        d.showDialog();
//                    }
//                } catch (IllegalStateException e) {
//                    showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
//                    // Log.i(TAG, "Error authenticating", e);
//                }
//            }
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        try {
            final CharSequence[] itemsLanguages = mContext.getResources().getStringArray(R.array.Languages);

            tvLanguage = v.findViewById(R.id.tvLanguage);
            String strLang = GrieeXSettings.getLocale(mContext);
            if (strLang.contains("en")) {
                tvLanguage.setText(itemsLanguages[0]);
                iSelectedLanguage = 0;
            } else if (strLang.contains("tr")) {
                tvLanguage.setText(itemsLanguages[1]);
                iSelectedLanguage = 1;
            }

            LinearLayout llLanguage = v.findViewById(R.id.llLanguage);
            llLanguage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setCancelable(true);
                    alertDialog.setTitle(mContext.getString(R.string.choose_language));
                    alertDialog.setSingleChoiceItems(itemsLanguages, iSelectedLanguage, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            iSelectedLanguage = item;
                        }
                    });
                    alertDialog.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                if (iSelectedLanguage == 0) {
                                    GrieeXSettings.setLocale(mContext, "en");
                                } else if (iSelectedLanguage == 1) {
                                    GrieeXSettings.setLocale(mContext, "tr");
                                }

                                String locale = GrieeXSettings.getLocale(mContext);

                                Utils.setDefaultLocale(mContext, locale);

                                if (locale.contains("en")) {
                                    tvLanguage.setText(itemsLanguages[0]);
                                } else if (locale.contains("tr")) {
                                    tvLanguage.setText(itemsLanguages[1]);
                                }
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }
                    });

                    alertDialog.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog.show();
                }
            });

            LinearLayout llTheme = v.findViewById(R.id.llTheme);
            llTheme.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    ThemeDialog dialog = new ThemeDialog(getActivity());
                    dialog.show();
                }
            });

            UilCachePath = Glide.getPhotoCacheDir(mContext).getAbsolutePath();
            tvClearCache = v.findViewById(R.id.tvClearCache);
            tvClearCache.setText(String.format(getActivity().getString(R.string.clear_cache), Utils.getFolderSize(UilCachePath)));

            LinearLayout llClearCache = v.findViewById(R.id.llClearCache);
            llClearCache.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.app_name);
                    builder.setMessage(R.string.clear_cache_alert);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Glide.get(GrieeX.getInstance().getContext()).clearMemory();

                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.get(GrieeX.getInstance().getContext()).clearDiskCache();

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tvClearCache.setText(String.format(getActivity().getString(R.string.clear_cache), Utils.getFolderSize(UilCachePath)));
                                        }
                                    });
                                }
                            });
                        }
                    });

                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            ivDropboxOk = v.findViewById(R.id.ivDropboxOk);
            ImageView ivDropboxSync = v.findViewById(R.id.ivDropboxSync);
            TextView tvDropbox = v.findViewById(R.id.tvDropbox);
            if (Utils.isProInstalled(mContext)) {
                if (hasToken()) {
                    ivDropboxOk.setVisibility(View.VISIBLE);
                }
            }


            LinearLayout llDropbox = v.findViewById(R.id.llDropbox);
            llDropbox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isProInstalled(mContext)) {
                        if (!Connectivity.isConnected(mContext)) {
                            Toast.makeText(mContext, mContext.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (hasToken()) {
                            DropboxDialog d = new DropboxDialog(mContext);
                            d.setCustomEventListener(new DropboxDialog.OnCustomEventListener() {
                                @Override
                                public void onLogoutDropbox() {
                                    logOut();
                                    onResume();
                                }
                            });
                            d.showDialog();
                        } else {
                            ShowDropboxDialog = true;
                            Auth.startOAuth2Authentication(mContext, getString(R.string.dropbox_app_key));
                        }
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.alert4), Toast.LENGTH_SHORT).show();
                    }
                }
            });


            LinearLayout llBatchProcessing = v.findViewById(R.id.llBatchProcessing);
            llBatchProcessing.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isProInstalled(mContext)) {
                        BatchProcessingDialog d = new BatchProcessingDialog(mContext);
                        d.showDialog();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.alert4), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            LinearLayout llBackup = v.findViewById(R.id.llBackup);
            llBackup.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

//						BackupRestoreDialog d = new BackupRestoreDialog(mContext);
//						d.showDialog();
                    BackupRestoreDialog newFragment = BackupRestoreDialog.newInstance();
                    //FragmentManager fm = getActivity().getSupportFragmentManager();
                    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");

                }
            });

            LinearLayout llRateGooglePlay = v.findViewById(R.id.llRateGooglePlay);
            llRateGooglePlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isProInstalled(mContext)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=com.grieex.pro"));
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=com.grieex"));
                        startActivity(intent);
                    }

                }
            });


            iSelectedNotificationTime = GrieeXSettings.getNotificationTimeIndex(mContext);
            final CharSequence[] itemsNotificationTimes = mContext.getResources().getStringArray(R.array.series_notifications);
            LinearLayout llNotificationTime = v.findViewById(R.id.llNotificationTime);
            llNotificationTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setCancelable(true);
                    alertDialog.setTitle(mContext.getString(R.string.notification_time));
                    alertDialog.setSingleChoiceItems(itemsNotificationTimes, iSelectedNotificationTime, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            iSelectedNotificationTime = item;
                        }
                    });
                    alertDialog.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                GrieeXSettings.setNotificationTimeFromIndex(mContext, iSelectedNotificationTime);
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }
                    });

                    alertDialog.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog.show();
                }
            });

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void DropboxInit() {
        String accessToken = Prefs.with(mContext).getString(Constants.Pref_Dropbox_Access_Token, "");
        if (accessToken.isEmpty()) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                Prefs.with(mContext).save(Constants.Pref_Dropbox_Access_Token, accessToken);
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
    }

    private boolean hasToken() {
        String accessToken = Prefs.with(mContext).getString(Constants.Pref_Dropbox_Access_Token, "");
        return !accessToken.isEmpty();
    }

    private void clearKeys() {
        Prefs.with(mContext).save(Constants.Pref_Dropbox_Access_Token, "");
    }

    private void logOut() {
        // Remove credentials from the session
        // mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        //setLoggedIn(false);
        ShowDropboxDialog = false;
        ivDropboxOk.setVisibility(View.GONE);
    }
}

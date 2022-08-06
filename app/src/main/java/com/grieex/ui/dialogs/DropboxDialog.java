package com.grieex.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dropbox.core.v2.files.FileMetadata;
import com.grieex.R;
import com.grieex.dropbox.DownloadFileTask;
import com.grieex.dropbox.DropboxClientFactory;
import com.grieex.dropbox.UploadFileTask;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DateUtils;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.update.UpdateManager;


import java.io.File;

public class DropboxDialog extends Dialog {

    private static final String TAG = DropboxDialog.class.getName();
    private final Context mContext;
    private boolean IsSyncAnimationStarted = false;
    private ImageView ivDropboxSync;
    private RelativeLayout rlPleaseWait;
    private ProgressBar progressBar;

    private ImageButton btnDownload;
    private ImageButton btnUpload;

    private OnCustomEventListener mListener;

    public interface OnCustomEventListener {
        void onLogoutDropbox();
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        mListener = eventListener;
    }


    public DropboxDialog(Context context) {
        super(context);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(R.string.dropbox);
        mContext = context;
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_dropbox, null));
        // getWindow().setWindowAnimations(R.style.DialogAnimation);
        initPopup();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void initPopup() {
        try {
            progressBar = findViewById(R.id.progressBar);
            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.GONE);
            rlPleaseWait = findViewById(R.id.rlPleaseWait);
            ivDropboxSync = findViewById(R.id.ivDropboxSync);

            Button btnLogoutDropbox = findViewById(R.id.btnLogoutDropbox);
            btnLogoutDropbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        Prefs.with(mContext).save(Constants.Pref_Dropbox_Access_Token, "");
                        mListener.onLogoutDropbox();
                        dismiss();
                    }
                }
            });

            btnUpload = findViewById(R.id.btnUpload);
            btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Connectivity.isConnected(mContext)) {
                        Toast.makeText(mContext, mContext.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.app_name);
                    builder.setMessage(R.string.alert5);
                    builder.setPositiveButton(R.string.yes, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            IsSyncAnimationStarted = true;
                            rlPleaseWait.setVisibility(View.VISIBLE);
                            SyncAnimation();

                            new UploadFileTask(mContext, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
                                @Override
                                public void onProgress(int percent) {
                                    progressBar.setProgress(percent);
                                }

                                @Override
                                public void onUploadComplete(FileMetadata result) {
                                    SyncCompleted();
                                    Prefs.with(mContext).save(Constants.Pref_Dropbox_Last_Update_Date, DateUtils.DateTimeNowString());

                                    showToast(mContext.getString(R.string.dropbox_uploaded));
                                    dismiss();
                                }

                                @Override
                                public void onError(Exception e) {
                                    SyncCompleted();
                                    showToast(e.getMessage());
                                }
                            }).execute();
                        }
                    });

                    builder.setNegativeButton(R.string.no, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            btnDownload = findViewById(R.id.btnDownload);
            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Connectivity.isConnected(mContext)) {
                        Toast.makeText(mContext, mContext.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.app_name);
                    builder.setMessage(R.string.alert6);
                    builder.setPositiveButton(R.string.yes, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            IsSyncAnimationStarted = true;
                            rlPleaseWait.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.VISIBLE);
                            SyncAnimation();

                            new DownloadFileTask(mContext, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
                                @Override
                                public void onProgress(long completed, long totalSize) {
                                    int percent = (int) (100.0 * (double) completed / totalSize + 0.5);
                                    progressBar.setProgress(percent);
                                }

                                @Override
                                public void onDownloadComplete(File result) {
                                    if (result != null) {
                                        SyncCompleted();
                                        Prefs.with(mContext).save(Constants.Pref_Dropbox_Last_Download_Date, DateUtils.DateTimeNowString());
                                        showToast(mContext.getString(R.string.dropbox_downloaded));

                                        BroadcastNotifier mBroadcaster = new BroadcastNotifier(mContext);
                                        mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU_COUNT);
                                        dismiss();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    SyncCompleted();
                                    showToast(e.getMessage());
                                }
                            }).execute();

                        }
                    });

                    builder.setNegativeButton(R.string.no, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public void showDialog() {
        // WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        // lp.copyFrom(getWindow().getAttributes());
        // lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        // lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        setCancelable(true);
        setCanceledOnTouchOutside(true);
        show();
        // getWindow().setAttributes(lp);
    }

    private void SyncAnimation() {
        btnDownload.setEnabled(false);
        btnUpload.setEnabled(false);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        ivDropboxSync.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate));
    }

    private void SyncCompleted() {
        if (UpdateManager.NewVersionFound(mContext)) {
            UpdateManager.Start(mContext.getApplicationContext());
            dismiss();
            return;
        }

        btnDownload.setEnabled(true);
        btnUpload.setEnabled(true);
        IsSyncAnimationStarted = false;
        rlPleaseWait.setVisibility(View.INVISIBLE);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        // String DropboxLastSyncDate =
        // Prefs.with(mContext).getString(Constants.Pref_Dropbox_Last_Sync_Date,
        // "");
        // if (!TextUtils.isEmpty(DropboxLastSyncDate)) {
        // tvDropbox.setText(Html.fromHtml("Dropbox <font color='#C9C9C9'>(" +
        // DateTimeUtils.getDateFormat(DropboxLastSyncDate,
        // Constants.DATE_FORMAT2) + ")</font>"));
        // }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}

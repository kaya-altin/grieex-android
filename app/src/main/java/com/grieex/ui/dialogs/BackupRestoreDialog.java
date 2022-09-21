package com.grieex.ui.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.grieex.R;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.FileUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.update.UpdateManager;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupRestoreDialog extends DialogFragment {

    private static final int FILE_SELECT_CODE = 0;
    private final String TAG = BackupRestoreDialog.class.getName();
    private Context mContext;
    private final PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
//            Toast.makeText(mContext, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(mContext, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }


    };
    private boolean IsAnimationStarted = false;
    private ImageView ivDropboxSync;
    private RelativeLayout rlPleaseWait;
    private ImageButton btnBackup;
    private ImageButton btnRestore;

    public static BackupRestoreDialog newInstance() {
        return new BackupRestoreDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_backup, container, false);

        try {
            getDialog().setTitle(R.string.backup);

            rlPleaseWait = v.findViewById(R.id.rlPleaseWait);
            ivDropboxSync = v.findViewById(R.id.ivDropboxSync);

            btnBackup = v.findViewById(R.id.btnBackup);
            btnBackup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Compress().execute();
                }
            });

            btnRestore = v.findViewById(R.id.btnRestore);
            btnRestore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showFileChooser();
                }
            });

            TedPermission.with(mContext)
                    .setPermissionListener(permissionlistener)
                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            String[] blacklist = new String[]{"com.dropbox.android", "cn.wps.moffice"};
//            startActivityForResult(
//                    Intent.createChooser(intent, "Select a File Manager"),
//                    FILE_SELECT_CODE);
            startActivityForResult(generateCustomChooserIntent(intent, blacklist), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(mContext, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent generateCustomChooserIntent(Intent prototype, String[] forbiddenChoices) {
        List<Intent> targetedShareIntents = new ArrayList<>();
        List<HashMap<String, String>> intentMetaInfo = new ArrayList<>();
        Intent chooserIntent;

        Intent dummy = new Intent(prototype.getAction());
        dummy.setType(prototype.getType());
        List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(dummy, 0);

        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                if (resolveInfo.activityInfo == null || Arrays.asList(forbiddenChoices).contains(resolveInfo.activityInfo.packageName))
                    continue;

                HashMap<String, String> info = new HashMap<>();
                info.put("packageName", resolveInfo.activityInfo.packageName);
                info.put("className", resolveInfo.activityInfo.name);
                info.put("simpleName", String.valueOf(resolveInfo.activityInfo.loadLabel(mContext.getPackageManager())));
                intentMetaInfo.add(info);
            }

            if (!intentMetaInfo.isEmpty()) {
                // sorting for nice readability
                Collections.sort(intentMetaInfo, new Comparator<HashMap<String, String>>() {
                    @Override
                    public int compare(HashMap<String, String> map, HashMap<String, String> map2) {
                        return map.get("simpleName").compareTo(map2.get("simpleName"));
                    }
                });

                // create the custom intent list
                for (HashMap<String, String> metaInfo : intentMetaInfo) {
                    Intent targetedShareIntent = (Intent) prototype.clone();
                    targetedShareIntent.setPackage(metaInfo.get("packageName"));
                    targetedShareIntent.setClassName(metaInfo.get("packageName"), metaInfo.get("className"));
                    targetedShareIntents.add(targetedShareIntent);
                }

                chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), "Select a File Manager");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[targetedShareIntents.size()]));
                return chooserIntent;
            }
        }

        return Intent.createChooser(prototype, "Select a File Manager");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                Log.d(TAG, "File Uri: " + uri.toString());


                // Get the path
                try {
                    String path = FileUtils.getPath(mContext, uri);
                    String ext = FileUtils.getFileExtension(path);
                    if (!ext.equals(".gbf") && !ext.equals(".db")) {
                        Toast.makeText(mContext, getString(R.string.alert8), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ext.equals(".gbf")) {
                        new Decompress(path).execute();
                    } else if (ext.equals(".db")) {
                        if (!TextUtils.isEmpty(path) && path.contains("GrieeX.db")) {
                            new FileCopy(path).execute();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, getString(R.string.alert8), Toast.LENGTH_SHORT).show();
                    //NLog.e(TAG, e);
                }
                //Log.d(TAG, "File Path: " + path);
                // Get the file instance
                // File file = new File(path);
                // Initiate the upload
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //    public void showDialog() {
//        // WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        // lp.copyFrom(getWindow().getAttributes());
//        // lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        // lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//
//        setCancelable(true);
//        setCanceledOnTouchOutside(true);
//        show();
//        // getWindow().setAttributes(lp);
//    }

    private void SyncAnimation() {
        btnBackup.setEnabled(false);
        btnRestore.setEnabled(false);
        //setCancelable(false);
        // setCanceledOnTouchOutside(false);
//        new RotationAnimation(ivDropboxSync).setPivot(RotationAnimation.PIVOT_CENTER).setDuration(2000).setListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                if (IsAnimationStarted)
//                    SyncAnimation();
//            }
//        }).animate();

        ivDropboxSync.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate));
    }

    private void SyncCompleted() {
        if (UpdateManager.NewVersionFound(mContext)) {
            UpdateManager.Start(mContext.getApplicationContext());
            dismiss();
            return;
        }

        btnBackup.setEnabled(true);
        btnRestore.setEnabled(true);
        IsAnimationStarted = false;
        rlPleaseWait.setVisibility(View.INVISIBLE);

        BroadcastNotifier mBroadcaster = new BroadcastNotifier(mContext);
        mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU_COUNT);
        //setCancelable(true);
        //setCanceledOnTouchOutside(true);
        // String DropboxLastSyncDate =
        // Prefs.with(mContext).getString(Constants.Pref_Dropbox_Last_Sync_Date,
        // "");
        // if (!TextUtils.isEmpty(DropboxLastSyncDate)) {
        // tvDropbox.setText(Html.fromHtml("Dropbox <font color='#C9C9C9'>(" +
        // DateTimeUtils.getDateFormat(DropboxLastSyncDate,
        // Constants.DATE_FORMAT2) + ")</font>"));
        // }
    }


//    public class Compress {
//        private static final int BUFFER = 1024;
//
//        private String[] _files;
//        private String _zipFile;
//
//        public Compress(String[] files, String zipFile) {
//            _files = files;
//            _zipFile = zipFile;
//
//        }
//
//        public void zip() {
//            try {
//                FileUtils.dirChecker(GrieeXSettings.BACKUP_PATH);
//
//                BufferedInputStream origin = null;
//                FileOutputStream dest = new FileOutputStream(_zipFile);
//
//                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
//
//                byte data[] = new byte[BUFFER];
//
//                for (int i = 0; i < _files.length; i++) {
//                    FileInputStream fi = new FileInputStream(_files[i]);
//                    origin = new BufferedInputStream(fi, BUFFER);
//                    ZipEntry entry = new ZipEntry("Database/" + _files[i].substring(_files[i].lastIndexOf("/") + 1));
//                    out.putNextEntry(entry);
//                    int count;
//                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
//                        out.write(data, 0, count);
//                    }
//                    origin.close();
//                }
//
//                out.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//    }

    private class Compress extends AsyncTask<Void, Integer, Integer> {
        private static final int BUFFER = 1024;

        Compress() {
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                FileUtils.dirChecker(GrieeXSettings.BACKUP_PATH);
                String _zipFile = GrieeXSettings.BACKUP_PATH + "GrieeX_" + DateUtils.ConvertDateToString(Constants.DATE_FORMAT13, DateUtils.DateTimeNowString()) + ".gbf";
                String[] s = new String[1];
                s[0] = GrieeXSettings.DB_PATHFULL;

                BufferedInputStream origin;
                FileOutputStream dest = new FileOutputStream(_zipFile);

                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

                byte data[] = new byte[BUFFER];

                for (String _file : s) {
                    FileInputStream fi = new FileInputStream(_file);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry("Database/" + _file.substring(_file.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }

                out.close();

            } catch (Exception e) {
                NLog.e(TAG, e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            //	progress.setProgress(per);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            IsAnimationStarted = true;
            rlPleaseWait.setVisibility(View.VISIBLE);
            SyncAnimation();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            SyncCompleted();
            dismiss();
            Toast.makeText(mContext, GrieeXSettings.BACKUP_PATH + " - " + mContext.getString(R.string.backup_completed), Toast.LENGTH_SHORT).show();
        }
    }

    private class Decompress extends AsyncTask<Void, Integer, Integer> {

        private final String _zipFile;
        private int per = 0;

        Decompress(String zipFile) {
            _zipFile = zipFile;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {

                byte[] buffer = new byte[1024];
                int length;

                //ZipFile zip = new ZipFile(_zipFile);
                //progress.setMax(zip.size());

                FileInputStream fin = new FileInputStream(_zipFile);
                ZipInputStream zin = new ZipInputStream(fin);
                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    if (ze.isDirectory()) {
                        FileUtils.dirChecker(ze.getName());
                    } else {
                        per++;
                        publishProgress(per);

                        String _location;
                        if (ze.getName().contains("GrieeX.db")) {
                            DatabaseHelper.getInstance(mContext).close();
                            _location = GrieeXSettings.DB_PATHFULL;
                        } else {
                            continue;
                        }

                        FileOutputStream fout = new FileOutputStream(_location);

                        while ((length = zin.read(buffer)) > 0) {
                            fout.write(buffer, 0, length);
                        }

                        zin.closeEntry();
                        fout.close();
                    }

                }
                zin.close();
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            IsAnimationStarted = true;
            rlPleaseWait.setVisibility(View.VISIBLE);
            SyncAnimation();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            //	progress.setProgress(per);
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            DatabaseHelper.getInstance(mContext).openDataBase();
            SyncCompleted();
            dismiss();
            Toast.makeText(mContext, mContext.getString(R.string.restore_completed), Toast.LENGTH_SHORT).show();
        }
    }

    private class FileCopy extends AsyncTask<Void, Integer, Integer> {

        private final String _sourceFile;
        private String _location;
        private int per = 0;

        FileCopy(String sourceFile) {
            _sourceFile = sourceFile;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                DatabaseHelper.getInstance(mContext).close();

                String _location = GrieeXSettings.DB_PATHFULL;

                FileUtils.FileCopy(_sourceFile, _location);
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            IsAnimationStarted = true;
            rlPleaseWait.setVisibility(View.VISIBLE);
            SyncAnimation();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            //	progress.setProgress(per);
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            DatabaseHelper.getInstance(mContext).openDataBase();
            SyncCompleted();
            dismiss();
            Toast.makeText(mContext, mContext.getString(R.string.restore_completed), Toast.LENGTH_SHORT).show();
        }
    }

}

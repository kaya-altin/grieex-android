package com.grieex.dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.ProgressInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Async task to upload a file to a directory
 */
public class UploadFileTask extends AsyncTask<Void, Void, FileMetadata> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onProgress(int percent);

        void onUploadComplete(FileMetadata result);

        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(Void... params) {

        File localFile = new File(GrieeXSettings.DB_PATH, GrieeXSettings.DB_NAME);

        if (localFile != null) {
            String remoteFolderPath = "/databases/";

            String remoteFileName = localFile.getName();
            try {
                ProgressInputStream progressInputStream = new ProgressInputStream(new FileInputStream(localFile), localFile.getTotalSpace(), percent -> mCallback.onProgress(percent));

                return mDbxClient.files().uploadBuilder(remoteFolderPath + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(progressInputStream);
            } catch (DbxException | IOException e) {
                mException = e;
            }

//            try (InputStream inputStream = new FileInputStream(localFile)) {
//                return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
//                        .withMode(WriteMode.OVERWRITE)
//                        .uploadAndFinish(inputStream);
//            } catch (DbxException | IOException e) {
//                mException = e;
//            }
        }

        return null;
    }
}
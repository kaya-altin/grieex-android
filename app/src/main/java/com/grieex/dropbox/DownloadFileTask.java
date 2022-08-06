package com.grieex.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.ProgressOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
public class DownloadFileTask extends AsyncTask<Void, Void, File> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onProgress(long completed, long totalSize);
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public DownloadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(GrieeXSettings.DB_PATH);
            File file = new File(GrieeXSettings.DB_PATHFULL);


            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                path.mkdirs();
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.
            DbxDownloader<FileMetadata> dl = mDbxClient.files().download("/databases/GrieeX.db");
            long size = dl.getResult().getSize();
            OutputStream outputStream = new FileOutputStream(file);

            dl.download(new ProgressOutputStream(size, outputStream, mCallback::onProgress));

            outputStream.flush();
            outputStream.close();

//            try (OutputStream outputStream = new FileOutputStream(file)) {
//                mDbxClient.files().download("/databases/GrieeX.db")
//                        .download(outputStream);
//            }

           return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
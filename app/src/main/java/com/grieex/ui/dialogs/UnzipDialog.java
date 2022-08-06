package com.grieex.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.grieex.R;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

class UnzipDialog extends Dialog {

	private final String TAG = UnzipDialog.class.getName();
	private final Context mContext;

	private ProgressBar progress;
	private TextView tvState;

	private OnCustomEventListener mListener;

	interface OnCustomEventListener {
		void onCompleted();
	}

	public void setCustomEventListener(OnCustomEventListener eventListener) {
		mListener = eventListener;
	}

	private UnzipDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = context;
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_unzip, null));

		_dirChecker(GrieeXSettings.DB_PATH);
		// _dirChecker(GrieeXSettings.getImagePath() + "Posters" +
		// File.separator);

		initPopup();
	}

	private void initPopup() {
		try {
			progress = findViewById(R.id.progress);
			tvState = findViewById(R.id.tvState);
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	public void showDialog() {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		show();
		getWindow().setAttributes(lp);

		tvState.setText(R.string.please_wait);
		new Decompress(Environment.getExternalStorageDirectory() + "/GrieeXAndroid.db").execute();
	}

	private String GetFileExt(String FileName) {
		return FileName.substring((FileName.lastIndexOf(".") + 1), FileName.length());
	}

	private class Decompress extends AsyncTask<Void, Integer, Integer> {

		private final String _zipFile;
        private int per = 0;

		Decompress(String zipFile) {
			_zipFile = zipFile;
		}

		@SuppressWarnings("ResourceType")
		@Override
		protected Integer doInBackground(Void... params) {
			try {

				byte[] buffer = new byte[1024];
				int length;

				ZipFile zip = new ZipFile(_zipFile);
				progress.setMax(zip.size());

				FileInputStream fin = new FileInputStream(_zipFile);
				ZipInputStream zin = new ZipInputStream(fin);
				ZipEntry ze;
				while ((ze = zin.getNextEntry()) != null) {
					if (ze.isDirectory()) {
						_dirChecker(ze.getName());
					} else {
						per++;
						publishProgress(per);

						// if (GetFileExt(ze.getName()).equals("jpg")) {
						// _location = GrieeXSettings.getImagePath() + "Posters"
						// + File.separator;
						// } else
                        String _location;
                        if (GetFileExt(ze.getName()).equals("db")) {
							DatabaseHelper.getInstance(mContext).close();
							_location = GrieeXSettings.DB_PATH;
						} else {
							continue;
						}

						FileOutputStream fout = new FileOutputStream(_location + ze.getName());

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
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			progress.setProgress(per);
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			File file = new File(Environment.getExternalStorageDirectory(), "GrieeXAndroid.db");
			file.delete();

			DatabaseHelper.getInstance(mContext).openDataBase();
			dismiss();
			if (mListener != null)
				mListener.onCompleted();
		}
	}

	private void _dirChecker(String dir) {
		File f = new File(dir);

		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}

}

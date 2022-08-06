package com.grieex.update;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MergeDatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = MergeDatabaseHelper.class.getName();
	private static MergeDatabaseHelper helper;

	public synchronized static MergeDatabaseHelper getInstance(Context context) {
		if (helper == null)
			helper = new MergeDatabaseHelper(context.getApplicationContext());

		return helper;
	}

	private SQLiteDatabase myDataBase;
	private final Context myContext;

	private MergeDatabaseHelper(Context context) {
		super(context, GrieeXSettings.DB_NAME + ".merge", null, 1);
		this.myContext = context;
	}

	private void createDataBase() {
		File _Folder = new File(GrieeXSettings.DB_PATH);
		if (!_Folder.exists())
			_Folder.mkdirs();

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// this.getReadableDatabase();
			// do nothing - database already exist
		} else {
			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			// this.getReadableDatabase();

			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;

		try {
			checkDB = SQLiteDatabase.openDatabase(GrieeXSettings.DB_MERGE_PATHFULL, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
		}

		if (checkDB != null) {
			checkDB.close();
		}

		return checkDB != null;
	}

	private void copyDataBase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(GrieeXSettings.ASSETS_DB_NAME);

		// Path to the just created empty db
		String outFileName = GrieeXSettings.DB_PATHFULL + ".merge";

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	private void openDataBase() throws SQLException {
		myDataBase = SQLiteDatabase.openDatabase(GrieeXSettings.DB_MERGE_PATHFULL, null, SQLiteDatabase.OPEN_READWRITE);
	}

	@Override
	public synchronized void close() {
		if (myDataBase != null) {
			myDataBase.close();
			myDataBase = null;
			helper = null;
		}

		try {
			File dataFile = new File(GrieeXSettings.DB_MERGE_PATHFULL);
			if (dataFile.exists())
				dataFile.delete();
		} catch (Exception e) {

		}

		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void ExecuteQuery(String query) throws SQLException {
		if (myDataBase == null) {
			createDataBase();
			openDataBase();
		}

		if (!myDataBase.isOpen()) {
			openDataBase();
		}

		// myDataBase.beginTransaction();
		try {
			android.database.sqlite.SQLiteStatement q = myDataBase.compileStatement(query);
			q.execute();

			// myDataBase.setTransactionSuccessful();
		} catch (SQLException e) {
			NLog.e(TAG, e);
		} finally {
			// myDataBase.endTransaction();
			// myDataBase.close();
		}
	}

	public long ExecuteQueries(String[] queries) {
		long res = -1;
		if (myDataBase == null) {
			createDataBase();
			openDataBase();
		}

		if (!myDataBase.isOpen()) {
			openDataBase();
		}

		// myDataBase.beginTransaction();
		try {
			for (String query : queries) {
				SQLiteStatement q = myDataBase.compileStatement(query);
				res = q.executeInsert();
			}
			// myDataBase.setTransactionSuccessful();
		} catch (SQLException e) {
			NLog.e(TAG, e);
		} finally {
			// myDataBase.endTransaction();
		}

		return res;
	}

	public ArrayList<?> GetCursorWithObject(String query, Class<?> issueObj) {
		if (myDataBase == null) {
			createDataBase();
			openDataBase();
		}

		if (!myDataBase.isOpen()) {
			openDataBase();
		}

		ArrayList<IDataModelObject> dmObjects = new ArrayList<>();
		Cursor crsr = null;
		// myDataBase.beginTransaction();
		try {
			crsr = myDataBase.rawQuery(query, null);

			if (crsr != null) {
				// /crsr.moveToFirst();
				while (crsr.moveToNext()) {
					IDataModelObject dmObject = (IDataModelObject) issueObj.newInstance();
					dmObject.LoadWithCursorRow(crsr);
					dmObjects.add(dmObject);
				}
			}
		} catch (Exception e) {
			NLog.e(TAG, e);
		} finally {
			if (crsr != null) {
				crsr.close();
			}
			// myDataBase.endTransaction();
		}
		return dmObjects;
	}

	public Cursor GetCursor(String query) {
		if (myDataBase == null) {
			createDataBase();
			openDataBase();
		}

		if (!myDataBase.isOpen()) {
			openDataBase();
		}

		// myDataBase.beginTransaction();
		try {
			return myDataBase.rawQuery(query, null);
		} catch (SQLException e) {
			NLog.e(TAG, e);
		} finally {
			// myDataBase.endTransaction();
		}
		return null;
	}

}
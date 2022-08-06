package com.grieex.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.grieex.interfaces.IDataModelObject;
import com.grieex.model.IncomingEpisode;
import com.grieex.model.tables.Backdrop;
import com.grieex.model.tables.Cast;
import com.grieex.model.tables.Episode;
import com.grieex.model.tables.Imdb250;
import com.grieex.model.tables.Lists;
import com.grieex.model.tables.ListsMovie;
import com.grieex.model.tables.ListsSeries;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Queue;
import com.grieex.model.tables.Season;
import com.grieex.model.tables.Series;
import com.grieex.model.tables.Trailer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    @SuppressLint("StaticFieldLeak")
    private static DatabaseHelper helper;

    private SQLiteDatabase myDataBase;
    private final Context myContext;

    public synchronized static DatabaseHelper getInstance(Context context) {
        if (helper == null)
            helper = new DatabaseHelper(context.getApplicationContext());

        return helper;
    }

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    private DatabaseHelper(Context context) {
        super(context, GrieeXSettings.DB_NAME, null, GrieeXSettings.DB_VERSION);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     */
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

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;

        try {
            checkDB = SQLiteDatabase.openDatabase(GrieeXSettings.DB_PATHFULL, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database does't exist yet.
            Log.d("CheckDatabase", GrieeXSettings.DB_PATHFULL + "\r\n" + e.getMessage());
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(GrieeXSettings.ASSETS_DB_NAME);

        // Path to the just created empty db
        String outFileName = GrieeXSettings.DB_PATHFULL;

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

    public void openDataBase() throws SQLException {
        myDataBase = SQLiteDatabase.openDatabase(GrieeXSettings.DB_PATHFULL, null, SQLiteDatabase.OPEN_READWRITE);
        myDataBase.setLocale(new Locale("tr_TR"));
        myDataBase.disableWriteAheadLogging();
    }

    public void DeleteDataBase() {
        close();

        File dbFile = new File(GrieeXSettings.DB_PATHFULL);
        if (dbFile.exists())
            dbFile.delete();
    }

    @Override
    public synchronized void close() {

        if (myDataBase != null) {
            myDataBase.close();
            helper = null;
            myDataBase = null;
        }

        super.close();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("GrieeX", "onUpgrade");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    public String GetOneField(String query) {
        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        Cursor cursor = null;
        try {
            cursor = myDataBase.rawQuery(query, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursor.getString(0);
            }

        } catch (SQLException e) {
            NLog.e(TAG, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public ArrayList<String> GetOneFieldStringList(String query) {
        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        ArrayList<String> returnList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = myDataBase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    returnList.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            return returnList;
        } catch (SQLException e) {
            NLog.e(TAG, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return returnList;
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
            //  myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            String s = ex.getMessage();
            Log.d("Error on ExecuteQuery", s + " " + query);
            NLog.e(TAG, ex);
        } finally {
            // myDataBase.endTransaction();
        }
    }

    public void ExecuteQueries(String[] queries) {
        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }


        for (String query : queries) {
            try {
                SQLiteStatement q = myDataBase.compileStatement(query);
                q.execute();
            } catch (SQLException e) {
                NLog.e(TAG, e);
            }
        }
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
        Cursor cursor = null;
        // myDataBase.beginTransaction();
        try {
            cursor = myDataBase.rawQuery(query, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    IDataModelObject dmObject = (IDataModelObject) issueObj.newInstance();
                    dmObject.LoadWithCursorRow(cursor);
                    dmObjects.add(dmObject);
                }
            }
            // myDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            if (cursor != null) {
                cursor.close();
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

        Cursor crsr;
        // myDataBase.beginTransaction();
        try {
            crsr = myDataBase.rawQuery(query, null);
            return crsr;
        } catch (Exception ex) {
            NLog.e(TAG, ex);
        } finally {
            // myDataBase.endTransaction();

        }

        return null;
    }

    public void ExecuteQueryWithContentValues(String tableName, ContentValues vl) throws SQLException {
        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        myDataBase.beginTransaction();
        try {

            myDataBase.insert(tableName, null, vl);

            myDataBase.setTransactionSuccessful();
            //	Log.d("GrieeX ExecuteQueryWithContentValues", "insert/update okdir");
        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            myDataBase.endTransaction();
        }
    }

    // **************** Movies ****************//

    public long addMovie(Movie m) {
        long iReturn = -1;
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            iReturn = myDataBase.insert(m.GetTableName(), null, m.GetContentValuesForDB());
        } catch (Exception e) {
            iReturn = 0;
            NLog.e(TAG, e);
        }

        return iReturn;
    }

    public int updateMovie(Movie m) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            return myDataBase.update(Movie.TABLE_NAME, m.GetContentValuesForDB(), Movie.COLUMNS._ID + " = ?", new String[]{String.valueOf(m.getID())});
        } catch (Exception e) {
            NLog.e(TAG, e);
            return 0;
        }
    }

    public void deleteMovie(Movie m) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            myDataBase.delete(Movie.TABLE_NAME, Movie.COLUMNS._ID + " = ?", new String[]{String.valueOf(m.getID())});
            ExecuteQuery("Delete From " + Queue.TABLE_NAME + " Where " + Queue.COLUMNS.ObjectID + "=" + String.valueOf(m.getID()));
            ExecuteQuery("Delete From " + Cast.TABLE_NAME + " Where " + Cast.COLUMNS.ObjectID + "=" + String.valueOf(m.getID()) + " and CollectionType=" + Constants.CollectionType.Movie.value);
            ExecuteQuery("Delete From " + com.grieex.model.tables.File.TABLE_NAME + " Where " + com.grieex.model.tables.File.COLUMNS.MovieID + "=" + String.valueOf(m.getID()));
            ExecuteQuery("Delete From " + Backdrop.TABLE_NAME + " Where " + Backdrop.COLUMNS.ObjectID + "=" + String.valueOf(m.getID()) + " and CollectionType=" + Constants.CollectionType.Movie.value);
            ExecuteQuery("Delete From " + ListsMovie.TABLE_NAME + " Where " + ListsMovie.COLUMNS.MovieID + "=" + String.valueOf(m.getID()));
            ExecuteQuery("Delete From " + Trailer.TABLE_NAME + " Where " + Trailer.COLUMNS.ObjectID + "=" + String.valueOf(m.getID()) + " and CollectionType=" + Constants.CollectionType.Movie.value);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }

    }

    public void deleteMovieById(long id) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            myDataBase.delete(Movie.TABLE_NAME, Movie.COLUMNS._ID + " = ?", new String[]{String.valueOf(id)});

            ExecuteQuery("Delete From " + Backdrop.TABLE_NAME + " Where " + Backdrop.COLUMNS.ObjectID + "=" + String.valueOf(id) + " and CollectionType=" + Constants.CollectionType.Movie.value);
            ExecuteQuery("Delete From " + Cast.TABLE_NAME + " Where " + Cast.COLUMNS.ObjectID + "=" + String.valueOf(id) + " and CollectionType=" + Constants.CollectionType.Movie.value);
            ExecuteQuery("Delete From " + Queue.TABLE_NAME + " Where " + Queue.COLUMNS.ObjectID + "=" + String.valueOf(id));
            ExecuteQuery("Delete From " + com.grieex.model.tables.File.TABLE_NAME + " Where " + com.grieex.model.tables.File.COLUMNS.MovieID + "=" + String.valueOf(id));
            ExecuteQuery("Delete From " + ListsMovie.TABLE_NAME + " Where " + ListsMovie.COLUMNS.MovieID + "=" + String.valueOf(id));
            ExecuteQuery("Delete From " + Trailer.TABLE_NAME + " Where " + Trailer.COLUMNS.ObjectID + "=" + String.valueOf(id) + " and CollectionType=" + Constants.CollectionType.Movie.value);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public ArrayList<Movie> getMovies() {
        try {

            return (ArrayList<Movie>) helper.GetCursorWithObject("Select * From " + Movie.TABLE_NAME + " Order By " + Movie.COLUMNS.OriginalName + " COLLATE LOCALIZED", Movie.class);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public ArrayList<Backdrop> getBackdrops(int id) {
        try {

            return (ArrayList<Backdrop>) helper.GetCursorWithObject("Select * From " + Backdrop.TABLE_NAME + " Where " + Backdrop.COLUMNS.ObjectID + "=" + id, Backdrop.class);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public Movie getMovie(int id) {
        try {
            ArrayList<Movie> movies = (ArrayList<Movie>) helper.GetCursorWithObject("Select * From " + Movie.TABLE_NAME + " Where " + Movie.COLUMNS._ID + "=" + id, Movie.class);
            if (movies.size() > 0) {
                Movie m = movies.get(0);
                m.setCast(getCasts(id, Constants.CollectionType.Movie));
                m.setFiles(getFiles(id));
                m.setTrailers(getTrailers(id));
                return m;
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    private ArrayList<Cast> getCasts(int id, Constants.CollectionType collectionType) {
        try {
            return (ArrayList<Cast>) helper.GetCursorWithObject("SELECT * FROM Casts Where ObjectID=" + id + " and CollectionType=" + collectionType.value, Cast.class);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public ArrayList<Cast> getCastsTop(int id, int iLimit, Constants.CollectionType collectionType) {
        try {
            return (ArrayList<Cast>) helper.GetCursorWithObject("SELECT * FROM Casts Where ObjectID=" + id + " and CollectionType=" + collectionType.value + " LIMIT " + iLimit, Cast.class);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    private ArrayList<com.grieex.model.tables.File> getFiles(int id) {
        try {
            return (ArrayList<com.grieex.model.tables.File>) helper.GetCursorWithObject("SELECT * FROM Files Where Files.MovieID=" + id, com.grieex.model.tables.File.class);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    private ArrayList<Trailer> getTrailers(int id) {
        try {
            return (ArrayList<Trailer>) helper.GetCursorWithObject("SELECT * FROM " + Trailer.TABLE_NAME + " Where " + Trailer.COLUMNS.ObjectID + "=" + id, Trailer.class);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public void fillMovies(ArrayList<Movie> localData) {
        if (localData == null | (localData != null && localData.size() == 0))
            return;

        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }


        boolean canpassbindargs = false;
        try {

            String sqlInsert = "insert into " + Movie.TABLE_NAME + " (" +
                    Movie.COLUMNS.OriginalName + "," +
                    Movie.COLUMNS.OtherName + "," +
                    Movie.COLUMNS.Director + "," +
                    Movie.COLUMNS.Writer + "," +
                    Movie.COLUMNS.Genre + "," +
                    Movie.COLUMNS.Year + "," +
                    Movie.COLUMNS.UserRating + "," +
                    Movie.COLUMNS.Votes + "," +
                    Movie.COLUMNS.ImdbUserRating + "," +
                    Movie.COLUMNS.ImdbVotes + "," +
                    Movie.COLUMNS.TmdbUserRating + "," +
                    Movie.COLUMNS.TmdbVotes + "," +
                    Movie.COLUMNS.RunningTime + "," +
                    Movie.COLUMNS.Country + "," +
                    Movie.COLUMNS.Language + "," +
                    Movie.COLUMNS.EnglishPlot + "," +
                    Movie.COLUMNS.OtherPlot + "," +
                    Movie.COLUMNS.Budget + "," +
                    Movie.COLUMNS.ProductionCompany + "," +
                    Movie.COLUMNS.ImdbNumber + "," +
                    Movie.COLUMNS.TmdbNumber + "," +
                    Movie.COLUMNS.ArchivesNumber + "," +
                    Movie.COLUMNS.Subtitle + "," +
                    Movie.COLUMNS.Dubbing + "," +
                    Movie.COLUMNS.PersonalRating + "," +
                    Movie.COLUMNS.UserColumn1 + "," +
                    Movie.COLUMNS.UserColumn2 + "," +
                    Movie.COLUMNS.UserColumn3 + "," +
                    Movie.COLUMNS.UserColumn4 + "," +
                    Movie.COLUMNS.UserColumn5 + "," +
                    Movie.COLUMNS.UserColumn6 + "," +
                    Movie.COLUMNS.RlsType + "," +
                    Movie.COLUMNS.RlsGroup + "," +
                    Movie.COLUMNS.Poster + "," +
                    Movie.COLUMNS.Note + "," +
                    Movie.COLUMNS.Seen + "," +
                    Movie.COLUMNS.IsSyncWaiting + "," +
                    Movie.COLUMNS.ContentProvider + "," +
                    Movie.COLUMNS.InsertDate + "," +
                    Movie.COLUMNS.UpdateDate + ") " + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            myDataBase.beginTransaction();
            for (int i = 0; i < localData.size(); i++) {
                Movie item = localData.get(i);

                DatabaseUtils.bindObjectToProgram(insert, 1, item.getOriginalName());
                DatabaseUtils.bindObjectToProgram(insert, 2, item.getOtherName());
                DatabaseUtils.bindObjectToProgram(insert, 3, item.getDirector());
                DatabaseUtils.bindObjectToProgram(insert, 4, item.getWriter());
                DatabaseUtils.bindObjectToProgram(insert, 5, item.getGenre());
                DatabaseUtils.bindObjectToProgram(insert, 6, item.getYear());
                DatabaseUtils.bindObjectToProgram(insert, 7, item.getUserRating());
                DatabaseUtils.bindObjectToProgram(insert, 8, item.getVotes());
                DatabaseUtils.bindObjectToProgram(insert, 9, item.getImdbUserRating());
                DatabaseUtils.bindObjectToProgram(insert, 10, item.getImdbVotes());
                DatabaseUtils.bindObjectToProgram(insert, 11, item.getTmdbUserRating());
                DatabaseUtils.bindObjectToProgram(insert, 12, item.getTmdbVotes());
                DatabaseUtils.bindObjectToProgram(insert, 13, item.getRunningTime());
                DatabaseUtils.bindObjectToProgram(insert, 14, item.getCountry());
                DatabaseUtils.bindObjectToProgram(insert, 15, item.getLanguage());
                DatabaseUtils.bindObjectToProgram(insert, 16, item.getEnglishPlot());
                DatabaseUtils.bindObjectToProgram(insert, 17, item.getOtherPlot());
                DatabaseUtils.bindObjectToProgram(insert, 18, item.getBudget());
                DatabaseUtils.bindObjectToProgram(insert, 19, item.getProductionCompany());
                DatabaseUtils.bindObjectToProgram(insert, 20, item.getImdbNumber());
                DatabaseUtils.bindObjectToProgram(insert, 21, item.getTmdbNumber());
                DatabaseUtils.bindObjectToProgram(insert, 22, item.getArchivesNumber());
                DatabaseUtils.bindObjectToProgram(insert, 23, item.getSubtitle());
                DatabaseUtils.bindObjectToProgram(insert, 24, item.getDubbing());
                DatabaseUtils.bindObjectToProgram(insert, 25, item.getPersonalRating());
                DatabaseUtils.bindObjectToProgram(insert, 26, item.getUserColumn1());
                DatabaseUtils.bindObjectToProgram(insert, 27, item.getUserColumn2());
                DatabaseUtils.bindObjectToProgram(insert, 28, item.getUserColumn3());
                DatabaseUtils.bindObjectToProgram(insert, 29, item.getUserColumn4());
                DatabaseUtils.bindObjectToProgram(insert, 30, item.getUserColumn5());
                DatabaseUtils.bindObjectToProgram(insert, 31, item.getUserColumn6());
                DatabaseUtils.bindObjectToProgram(insert, 32, item.getRlsType());
                DatabaseUtils.bindObjectToProgram(insert, 33, item.getRlsGroup());
                DatabaseUtils.bindObjectToProgram(insert, 34, item.getPoster());
                DatabaseUtils.bindObjectToProgram(insert, 35, item.getNote());
                DatabaseUtils.bindObjectToProgram(insert, 36, item.getSeen());
                DatabaseUtils.bindObjectToProgram(insert, 37, item.getIsSyncWaiting());
                DatabaseUtils.bindObjectToProgram(insert, 38, item.getContentProvider());
                DatabaseUtils.bindObjectToProgram(insert, 39, item.getInsertDate());
                DatabaseUtils.bindObjectToProgram(insert, 40, item.getUpdateDate());

                insert.execute();

            }
            myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            if (ex.getMessage().contains("bindargs")) {
                canpassbindargs = true;
            } else
                NLog.e(TAG, ex);
        } finally {
            myDataBase.endTransaction();
        }

        if (canpassbindargs) {
            try {
                myDataBase.beginTransaction();
                for (int i = 0; i < localData.size(); i++) {
                    Movie item = localData.get(i);

                    ContentValues cv = new ContentValues();
                    cv.put(Movie.COLUMNS.OriginalName, item.getOriginalName());
                    cv.put(Movie.COLUMNS.OtherName, item.getOtherName());
                    cv.put(Movie.COLUMNS.Director, item.getDirector());
                    cv.put(Movie.COLUMNS.Writer, item.getWriter());
                    cv.put(Movie.COLUMNS.Genre, item.getGenre());
                    cv.put(Movie.COLUMNS.Year, item.getYear());
                    cv.put(Movie.COLUMNS.UserRating, item.getUserRating());
                    cv.put(Movie.COLUMNS.Votes, item.getVotes());
                    cv.put(Movie.COLUMNS.ImdbUserRating, item.getImdbUserRating());
                    cv.put(Movie.COLUMNS.ImdbVotes, item.getImdbVotes());
                    cv.put(Movie.COLUMNS.TmdbUserRating, item.getTmdbUserRating());
                    cv.put(Movie.COLUMNS.RunningTime, item.getRunningTime());
                    cv.put(Movie.COLUMNS.Country, item.getCountry());
                    cv.put(Movie.COLUMNS.Language, item.getLanguage());
                    cv.put(Movie.COLUMNS.EnglishPlot, item.getEnglishPlot());
                    cv.put(Movie.COLUMNS.OtherPlot, item.getOtherPlot());
                    cv.put(Movie.COLUMNS.Budget, item.getBudget());
                    cv.put(Movie.COLUMNS.ProductionCompany, item.getProductionCompany());
                    cv.put(Movie.COLUMNS.ImdbNumber, item.getImdbNumber());
                    cv.put(Movie.COLUMNS.TmdbNumber, item.getTmdbNumber());
                    cv.put(Movie.COLUMNS.ArchivesNumber, item.getArchivesNumber());
                    cv.put(Movie.COLUMNS.Subtitle, item.getSubtitle());
                    cv.put(Movie.COLUMNS.Dubbing, item.getDubbing());
                    cv.put(Movie.COLUMNS.PersonalRating, item.getPersonalRating());
                    cv.put(Movie.COLUMNS.UserColumn1, item.getUserColumn1());
                    cv.put(Movie.COLUMNS.UserColumn2, item.getUserColumn2());
                    cv.put(Movie.COLUMNS.UserColumn3, item.getUserColumn3());
                    cv.put(Movie.COLUMNS.UserColumn4, item.getUserColumn4());
                    cv.put(Movie.COLUMNS.UserColumn5, item.getUserColumn5());
                    cv.put(Movie.COLUMNS.UserColumn6, item.getUserColumn6());
                    cv.put(Movie.COLUMNS.RlsType, item.getRlsType());
                    cv.put(Movie.COLUMNS.RlsGroup, item.getRlsGroup());
                    cv.put(Movie.COLUMNS.Poster, item.getPoster());
                    cv.put(Movie.COLUMNS.Note, item.getNote());
                    cv.put(Movie.COLUMNS.Seen, item.getSeen());
                    cv.put(Movie.COLUMNS.IsSyncWaiting, item.getIsSyncWaiting());
                    cv.put(Movie.COLUMNS.ContentProvider, item.getContentProvider());
                    cv.put(Movie.COLUMNS.InsertDate, item.getInsertDate());
                    cv.put(Movie.COLUMNS.UpdateDate, item.getUpdateDate());

                    myDataBase.insert(Movie.TABLE_NAME, "", cv);

                }
                myDataBase.setTransactionSuccessful();
            } catch (Exception ex) {
                NLog.e(TAG, ex);
            } finally {
                myDataBase.endTransaction();
            }
        }
    }

    public void fillFiles(ArrayList<com.grieex.model.tables.File> localData, String MovieID) {
        if (localData == null | (localData != null && localData.size() == 0))
            return;

        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }


        boolean canpassbindargs = false;
        try {

            String sqlInsert = "insert into " + com.grieex.model.tables.File.TABLE_NAME + " (" +
                    com.grieex.model.tables.File.COLUMNS.FileName + "," +
                    com.grieex.model.tables.File.COLUMNS.Resolution + "," +
                    com.grieex.model.tables.File.COLUMNS.VideoCodec + "," +
                    com.grieex.model.tables.File.COLUMNS.VideoBitrate + "," +
                    com.grieex.model.tables.File.COLUMNS.Fps + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioCodec1 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioChannels1 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioBitrate1 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioSampleRate1 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioSize1 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioCodec2 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioChannels2 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioBitrate2 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioSampleRate2 + "," +
                    com.grieex.model.tables.File.COLUMNS.AudioSize2 + "," +
                    com.grieex.model.tables.File.COLUMNS.TotalFrames + "," +
                    com.grieex.model.tables.File.COLUMNS.Lenght + "," +
                    com.grieex.model.tables.File.COLUMNS.VideoSize + "," +
                    com.grieex.model.tables.File.COLUMNS.FileSize + "," +
                    com.grieex.model.tables.File.COLUMNS.Chapter + "," +
                    com.grieex.model.tables.File.COLUMNS.VideoAspectRatio + "," +
                    com.grieex.model.tables.File.COLUMNS.MovieID + ") " + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            myDataBase.beginTransaction();
            for (int i = 0; i < localData.size(); i++) {
                com.grieex.model.tables.File item = localData.get(i);
                DatabaseUtils.bindObjectToProgram(insert, 1, item.getFileName());
                DatabaseUtils.bindObjectToProgram(insert, 2, item.getResolution());
                DatabaseUtils.bindObjectToProgram(insert, 3, item.getVideoCodec());
                DatabaseUtils.bindObjectToProgram(insert, 4, item.getVideoBitrate());
                DatabaseUtils.bindObjectToProgram(insert, 5, item.getFps());
                DatabaseUtils.bindObjectToProgram(insert, 6, item.getAudioCodec1());
                DatabaseUtils.bindObjectToProgram(insert, 7, item.getAudioChannels1());
                DatabaseUtils.bindObjectToProgram(insert, 8, item.getAudioBitrate1());
                DatabaseUtils.bindObjectToProgram(insert, 9, item.getAudioSampleRate1());
                DatabaseUtils.bindObjectToProgram(insert, 10, item.getAudioSize1());
                DatabaseUtils.bindObjectToProgram(insert, 11, item.getAudioCodec2());
                DatabaseUtils.bindObjectToProgram(insert, 12, item.getAudioChannels2());
                DatabaseUtils.bindObjectToProgram(insert, 13, item.getAudioBitrate2());
                DatabaseUtils.bindObjectToProgram(insert, 14, item.getAudioSampleRate2());
                DatabaseUtils.bindObjectToProgram(insert, 15, item.getAudioSize2());
                DatabaseUtils.bindObjectToProgram(insert, 16, item.getTotalFrames());
                DatabaseUtils.bindObjectToProgram(insert, 17, item.getLenght());
                DatabaseUtils.bindObjectToProgram(insert, 18, item.getVideoSize());
                DatabaseUtils.bindObjectToProgram(insert, 19, item.getFileSize());
                DatabaseUtils.bindObjectToProgram(insert, 20, item.getChapter());
                DatabaseUtils.bindObjectToProgram(insert, 21, item.getVideoAspectRatio());
                DatabaseUtils.bindObjectToProgram(insert, 22, MovieID);

                insert.execute();

            }
            myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            if (ex.getMessage().contains("bindargs")) {
                canpassbindargs = true;
            } else
                NLog.e(TAG, ex);
        } finally {
            myDataBase.endTransaction();
        }

        if (canpassbindargs) {
            try {
                myDataBase.beginTransaction();
                for (int i = 0; i < localData.size(); i++) {
                    com.grieex.model.tables.File item = localData.get(i);

                    ContentValues cv = new ContentValues();
                    cv.put(com.grieex.model.tables.File.COLUMNS.FileName, item.getFileName());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Resolution, item.getResolution());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoCodec, item.getVideoCodec());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoBitrate, item.getVideoBitrate());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Fps, item.getFps());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioCodec1, item.getAudioCodec1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioChannels1, item.getAudioChannels1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioBitrate1, item.getAudioBitrate1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSampleRate1, item.getAudioSampleRate1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSize1, item.getAudioSize1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioCodec2, item.getAudioCodec2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioChannels2, item.getAudioChannels2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioBitrate2, item.getAudioBitrate2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSampleRate2, item.getAudioSampleRate2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSize2, item.getAudioSize2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.TotalFrames, item.getTotalFrames());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Lenght, item.getLenght());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoSize, item.getVideoSize());
                    cv.put(com.grieex.model.tables.File.COLUMNS.FileSize, item.getFileSize());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Chapter, item.getChapter());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoAspectRatio, item.getVideoAspectRatio());
                    cv.put(com.grieex.model.tables.File.COLUMNS.MovieID, MovieID);

                    myDataBase.insert(com.grieex.model.tables.File.TABLE_NAME, "", cv);
                }
                myDataBase.setTransactionSuccessful();
            } catch (Exception ex) {
                NLog.e(TAG, ex);
            } finally {
                myDataBase.endTransaction();
            }
        }
    }

    public long addCast(Cast c) {
        long iReturn = 1;
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            iReturn = myDataBase.insert(c.GetTableName(), null, c.GetContentValuesForDB());
        } catch (Exception e) {
            iReturn = 0;
            NLog.e(TAG, e);
        }

        return iReturn;
    }

    public void deleteCast(Cast c) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            myDataBase.delete(Cast.TABLE_NAME, Cast.COLUMNS._ID + " = ?", new String[]{String.valueOf(c.getID())});
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public void fillCast(ArrayList<Cast> localData, int ObjectID, Constants.CollectionType type) {
        if (localData == null | (localData != null && localData.size() == 0))
            return;

        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        try {
            String sqlInsert = "insert into " + Cast.TABLE_NAME + " (" + Cast.COLUMNS.Name + "," + Cast.COLUMNS.Character + "," + Cast.COLUMNS.Url + "," + Cast.COLUMNS.ImageUrl + "," + Cast.COLUMNS.CastID + "," + Cast.COLUMNS.ObjectID + "," + Cast.COLUMNS.CollectionType + ") " + " values (?,?,?,?,?,?,?)";
            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            String sqlUpdate = "update " + Cast.TABLE_NAME + " set " + Cast.COLUMNS.Name + "=?," + Cast.COLUMNS.Character + "=?," + Cast.COLUMNS.Url + "=?," + Cast.COLUMNS.ImageUrl + "=? where " + Cast.COLUMNS.CastID + "=? and " + Cast.COLUMNS.ObjectID + "=? and " + Cast.COLUMNS.CollectionType + "=?";
            android.database.sqlite.SQLiteStatement update = myDataBase.compileStatement(sqlUpdate);

            myDataBase.beginTransaction();

            for (int i = 0; i < localData.size(); i++) {
                Cast item = localData.get(i);

                if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                    Cursor cursor = myDataBase.rawQuery("Select " + Cast.COLUMNS.ObjectID + " From " + Cast.TABLE_NAME + " Where " + Cast.COLUMNS.CastID + "=" + item.getCastID() + " and " + Cast.COLUMNS.ObjectID + "=" + ObjectID + " and " + Cast.COLUMNS.CollectionType + "=" + type.value, null);

                    if (cursor != null) {
                        int cnt = cursor.getCount();
                        if (cnt > 0) {
                            item.setTransactionType(Constants.TransactionTypes.UPDATE);
                        }
                        cursor.close();
                    }
                }


                if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                    DatabaseUtils.bindObjectToProgram(insert, 1, item.getName());
                    DatabaseUtils.bindObjectToProgram(insert, 2, item.getCharacter());
                    DatabaseUtils.bindObjectToProgram(insert, 3, item.getUrl());
                    DatabaseUtils.bindObjectToProgram(insert, 4, item.getImageUrl());
                    DatabaseUtils.bindObjectToProgram(insert, 5, item.getCastID());
                    DatabaseUtils.bindObjectToProgram(insert, 6, ObjectID);
                    DatabaseUtils.bindObjectToProgram(insert, 7, type.value);

                    insert.execute();
                } else if (item.getTransactionType().equals(Constants.TransactionTypes.UPDATE)) {
                    DatabaseUtils.bindObjectToProgram(update, 1, item.getName());
                    DatabaseUtils.bindObjectToProgram(update, 2, item.getCharacter());
                    DatabaseUtils.bindObjectToProgram(update, 3, item.getUrl());
                    DatabaseUtils.bindObjectToProgram(update, 4, item.getImageUrl());
                    DatabaseUtils.bindObjectToProgram(update, 5, item.getCastID());
                    DatabaseUtils.bindObjectToProgram(update, 6, ObjectID);
                    DatabaseUtils.bindObjectToProgram(update, 7, type.value);

                    update.execute();
                }
            }

            myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            NLog.e(TAG, ex);
        } finally {
            myDataBase.endTransaction();
        }
    }

    public void fillBackdrops(ArrayList<Backdrop> localData, int ObjectID, Constants.CollectionType type) throws Exception {
        if (localData == null | (localData != null && localData.size() == 0))
            return;

        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        ExecuteQuery("Delete From " + Backdrop.TABLE_NAME + " Where " + Backdrop.COLUMNS.ObjectID + "=" + String.valueOf(ObjectID) + " and " + Backdrop.COLUMNS.CollectionType + "=" + type.value);

        try {

            String sqlInsert = "insert into " + Backdrop.TABLE_NAME + " (" + Backdrop.COLUMNS.Url + "," + Backdrop.COLUMNS.ImdbNumber + "," + Backdrop.COLUMNS.ObjectID + "," + Backdrop.COLUMNS.CollectionType + ") " + " values (?,?,?,?)";
            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            myDataBase.beginTransaction();
            for (int i = 0; i < localData.size(); i++) {
                Backdrop item = localData.get(i);
                DatabaseUtils.bindObjectToProgram(insert, 1, item.getUrl());
                DatabaseUtils.bindObjectToProgram(insert, 2, item.getImdbNumber());
                DatabaseUtils.bindObjectToProgram(insert, 3, ObjectID);
                DatabaseUtils.bindObjectToProgram(insert, 4, type.value);

                insert.execute();
            }
            myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            NLog.e(TAG, ex);
        } finally {
            myDataBase.endTransaction();
        }
    }

    public void fillTrailers(ArrayList<Trailer> localData, int ObjectID, Constants.CollectionType collectionType) {
        if (localData == null | (localData != null && localData.size() == 0))
            return;

        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        try {
            ExecuteQuery("Delete From " + Trailer.TABLE_NAME + " Where " + Trailer.COLUMNS.ObjectID + "=" + String.valueOf(ObjectID) + " and " + Trailer.COLUMNS.CollectionType + "+" + collectionType.value);

            String sqlInsert = "insert into " + Trailer.TABLE_NAME + " (" + Trailer.COLUMNS.ObjectID + "," + Trailer.COLUMNS.Url + "," + Trailer.COLUMNS.Type + "," + Trailer.COLUMNS.CollectionType + ") " + " values (?,?,?,?)";
            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            myDataBase.beginTransaction();
            for (int i = 0; i < localData.size(); i++) {
                Trailer item = localData.get(i);
                DatabaseUtils.bindObjectToProgram(insert, 1, ObjectID);
                DatabaseUtils.bindObjectToProgram(insert, 2, item.getUrl());
                DatabaseUtils.bindObjectToProgram(insert, 3, item.getType());
                DatabaseUtils.bindObjectToProgram(insert, 4, collectionType.value);

                insert.execute();
            }
            myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            NLog.e(TAG, ex);
        } finally {
            myDataBase.endTransaction();
        }
    }

    public long addMovieImdb250(Imdb250 m) {
        long iReturn = 1;
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            iReturn = myDataBase.insert(m.GetTableName(), null, m.GetContentValuesForDB());
        } catch (Exception e) {
            iReturn = 0;
            NLog.e(TAG, e);
        }

        return iReturn;
    }

    public long addLists(Lists c) {
        long iReturn = 1;
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            iReturn = myDataBase.insert(c.GetTableName(), null, c.GetContentValuesForDB());
        } catch (Exception e) {
            iReturn = 0;
            NLog.e(TAG, e);
        }

        return iReturn;
    }

    public void updateLists(Lists c) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            myDataBase.update(Lists.TABLE_NAME, c.GetContentValuesForDB(), Lists.COLUMNS._ID + " = ?", new String[]{String.valueOf(c.getID())});
        } catch (Exception e) {
            NLog.e(TAG, e);
        }

    }

    public long addListsMovies(ListsMovie c) {
        long iReturn = 1;
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }
            ExecuteQuery("Delete From " + ListsMovie.TABLE_NAME + " Where " + ListsMovie.COLUMNS.MovieID + "=" + String.valueOf(c.getMovieID()) + " and " + ListsMovie.COLUMNS.ListID + "=" + String.valueOf(c.getListID()));

            iReturn = myDataBase.insert(c.GetTableName(), null, c.GetContentValuesForDB());
        } catch (Exception e) {
            iReturn = 0;
            NLog.e(TAG, e);
        }

        return iReturn;
    }

    public long addListsSeries(ListsSeries c) {
        long iReturn = 1;
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }
            ExecuteQuery("Delete From " + ListsSeries.TABLE_NAME + " Where " + ListsSeries.COLUMNS.SeriesID + "=" + String.valueOf(c.getSeriesID()) + " and " + ListsSeries.COLUMNS.ListID + "=" + String.valueOf(c.getListID()));

            iReturn = myDataBase.insert(c.GetTableName(), null, c.GetContentValuesForDB());
        } catch (Exception e) {
            iReturn = 0;
            NLog.e(TAG, e);
        }

        return iReturn;
    }

    // **************** Series ****************//

    public long addSeries(Series m) {
        long iReturn = -1;
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            iReturn = myDataBase.insert(m.GetTableName(), null, m.GetContentValuesForDB());
        } catch (Exception e) {
            iReturn = 0;
            NLog.e(TAG, e);
        }

        return iReturn;
    }

    public int updateSeries(Series m) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            return myDataBase.update(m.GetTableName(), m.GetContentValuesForDB(), Series.COLUMNS._ID + " = ?", new String[]{String.valueOf(m.getID())});
        } catch (Exception e) {
            NLog.e(TAG, e);
            return 0;
        }
    }

    public void deleteSeries(Series m) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            ExecuteQuery("Delete From " + Series.TABLE_NAME + " Where " + Series.COLUMNS._ID + "=" + String.valueOf(m.getID()));
            ExecuteQuery("Delete From " + Season.TABLE_NAME + " Where " + Season.COLUMNS.SeriesId + "=" + String.valueOf(m.getID()));
            ExecuteQuery("Delete From " + Episode.TABLE_NAME + " Where " + Episode.COLUMNS.SeriesId + "=" + String.valueOf(m.getID()));
            ExecuteQuery("Delete From " + Cast.TABLE_NAME + " Where " + Cast.COLUMNS.ObjectID + "=" + String.valueOf(m.getID()) + " and CollectionType=" + Constants.CollectionType.Series.value);
            //ExecuteQuery("Delete From " + Queue.TABLE_NAME + " Where " + Queue.COLUMNS.ObjectID + "=" + String.valueOf(m.getID()));
        } catch (Exception e) {
            NLog.e(TAG, e);
        }

    }

    public void deleteSeriesById(long id) {
        try {
            if (myDataBase == null) {
                createDataBase();
                openDataBase();
            }

            if (!myDataBase.isOpen()) {
                openDataBase();
            }

            myDataBase.delete(Series.TABLE_NAME, Series.COLUMNS._ID + " = ?", new String[]{String.valueOf(id)});

            ExecuteQuery("Delete From " + Season.TABLE_NAME + " Where " + Season.COLUMNS.SeriesId + "=" + String.valueOf(id));
            ExecuteQuery("Delete From " + Episode.TABLE_NAME + " Where " + Episode.COLUMNS.SeriesId + "=" + String.valueOf(id));
            ExecuteQuery("Delete From " + Cast.TABLE_NAME + " Where " + Cast.COLUMNS.ObjectID + "=" + String.valueOf(id) + " and CollectionType=" + Constants.CollectionType.Movie.value);
            //ExecuteQuery("Delete From " + Queue.TABLE_NAME + " Where " + Queue.COLUMNS.ObjectID + "=" + String.valueOf(id));
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public ArrayList<Series> getSeries() {
        try {
            ArrayList<Series> series = (ArrayList<Series>) helper.GetCursorWithObject("Select * From " + Series.TABLE_NAME, Series.class);
            if (series.size() > 0) {
                for (Series s : series) {
                    s.setCast(getCasts(s.getID(), Constants.CollectionType.Series));
                    s.setSeasons(getSeasons(s.getID()));
                    s.setEpisodes(getEpisodes(s.getID()));
                }
                return series;
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public Series getSeries(int id) {
        try {
            ArrayList<Series> series = (ArrayList<Series>) helper.GetCursorWithObject("Select * From " + Series.TABLE_NAME + " Where " + Series.COLUMNS._ID + "=" + id, Series.class);
            if (series.size() > 0) {
                Series s = series.get(0);
                s.setCast(getCasts(s.getID(), Constants.CollectionType.Series));
                s.setSeasons(getSeasons(s.getID()));
                s.setEpisodes(getEpisodes(s.getID()));
                return s;
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    private ArrayList<Season> getSeasons(int id) {
        try {

            return (ArrayList<Season>) helper.GetCursorWithObject("SELECT Seasons.*, (SELECT Count(*) FROM Episodes Where SeriesId=Seasons.SeriesId and SeasonNumber=Seasons.Number) as EpisodeCountCustom, (SELECT Count(*) FROM Episodes Where SeriesId=Seasons.SeriesId and SeasonNumber=Seasons.Number and Watched=1) as WatchedCount FROM Seasons Where EpisodeCountCustom>0 and SeriesId=" + id + " Order By Seasons.Number desc", Season.class);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    private ArrayList<Episode> getEpisodes(int id) {
        try {

            return (ArrayList<Episode>) helper.GetCursorWithObject("Select * From " + Episode.TABLE_NAME + " Where " + Episode.COLUMNS.SeriesId + "=" + id, Episode.class);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public Episode getEpisode(String id) {
        try {
            ArrayList<Episode> episodes = (ArrayList<Episode>) helper.GetCursorWithObject("Select * From " + Episode.TABLE_NAME + " Where " + Episode.COLUMNS._ID + "=" + id, Episode.class);

            if (episodes.size() > 0) {
                return episodes.get(0);
            }

            return null;
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public boolean getSeriesExistFromtTraktTvId(int id) {
        try {
            String series = helper.GetOneField("Select Count(*) From " + Series.TABLE_NAME + " Where " + Series.COLUMNS.TraktId + "=" + id);
            if (!series.equals("0")) {
                return true;
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return false;
    }

    public String getSeriesIdFromtTraktTvIdOrImdbId(String id) {
        try {
            return helper.GetOneField("Select _id From " + Series.TABLE_NAME + " Where " + Series.COLUMNS.TraktId + "='" + id + "' or " + Series.COLUMNS.ImdbId + "='" + id + "' LIMIT 1");
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }


    public ArrayList<IncomingEpisode> getIncomingEpisodes() {
        ArrayList<IncomingEpisode> returnList = new ArrayList<>();
        String strSQL = "Select Series._id, Series.SeriesName, Series.Poster, Series.Network, Episodes.EpisodeName, Episodes.SeasonNumber, Episodes.EpisodeNumber, Episodes.FirstAiredMs FROM Series ";
        String strJoin = " INNER JOIN Episodes ON (Series._id = Episodes.SeriesId) ";
        String strWhere = " Where FirstAiredMs > " + DateUtils.DateTimeNow().getTime() + " and FirstAiredMs < " + DateUtils.DateTimeNow(+1).getTime();


        Cursor cursor = null;
        try {
            cursor = helper.myDataBase.rawQuery(strSQL + strJoin + strWhere, null);
            int idxID = cursor.getColumnIndex("_id");
            int idxSeriesName = cursor.getColumnIndex("SeriesName");
            int idxEpisodeName = cursor.getColumnIndex("EpisodeName");
            int idxFirstAiredMs = cursor.getColumnIndex("FirstAiredMs");
            int idxPoster = cursor.getColumnIndex("Poster");

            if (cursor.moveToFirst()) {
                do {
                    IncomingEpisode episode = new IncomingEpisode();
                    episode.setId(cursor.getInt(idxID));
                    episode.setSeriesName(cursor.getString(idxSeriesName));
                    episode.setEpisodeName(cursor.getString(idxEpisodeName));
                    episode.setFirstAiredMs(cursor.getLong(idxFirstAiredMs));
                    episode.setPoster(cursor.getString(idxPoster));

                    returnList.add(episode);
                } while (cursor.moveToNext());
            }
            return returnList;
        } catch (SQLException e) {
            NLog.e(TAG, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return returnList;
    }

    public void fillSeasons(ArrayList<Season> localData, int SeriesId) {
        if (localData == null | (localData != null && localData.size() == 0))
            return;

        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        boolean canpassbindargs = false;
        try {
            String sqlInsert = "insert into Seasons (SeriesId,AiredEpisodes,EpisodeCount,Number,Overview,TmdbId,TvdbId,TraktId,Rating,Votes,Poster) " + " values (?,?,?,?,?,?,?,?,?,?,?)";
            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            String sqlUpdate = "update Seasons set AiredEpisodes=?,EpisodeCount=?,Number=?,Overview=?,TmdbId=?,TvdbId=?,TraktId=?,Rating=?,Votes=?,Poster=? where TraktId=?";
            android.database.sqlite.SQLiteStatement update = myDataBase.compileStatement(sqlUpdate);


            myDataBase.beginTransaction();
            for (int i = 0; i < localData.size(); i++) {
                Season item = localData.get(i);

                if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                    Cursor cursor = myDataBase.rawQuery("Select " + Season.COLUMNS.TraktId + " From " + Season.TABLE_NAME + " Where " + Season.COLUMNS.TraktId + "=" + item.getTraktId(), null);

                    if (cursor != null) {
                        int cnt = cursor.getCount();
                        if (cnt > 0) {
                            item.setTransactionType(Constants.TransactionTypes.UPDATE);
                        }
                        cursor.close();
                    }
                }

                if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                    DatabaseUtils.bindObjectToProgram(insert, 1, SeriesId);
                    DatabaseUtils.bindObjectToProgram(insert, 2, item.getAiredEpisodes());
                    DatabaseUtils.bindObjectToProgram(insert, 3, item.getEpisodeCount());
                    DatabaseUtils.bindObjectToProgram(insert, 4, item.getNumber());
                    DatabaseUtils.bindObjectToProgram(insert, 5, item.getOverview());
                    DatabaseUtils.bindObjectToProgram(insert, 6, item.getTmdbId());
                    DatabaseUtils.bindObjectToProgram(insert, 7, item.getTvdbId());
                    DatabaseUtils.bindObjectToProgram(insert, 8, item.getTraktId());
                    DatabaseUtils.bindObjectToProgram(insert, 9, item.getRating());
                    DatabaseUtils.bindObjectToProgram(insert, 10, item.getVotes());
                    DatabaseUtils.bindObjectToProgram(insert, 11, item.getPoster());

                    insert.execute();
                } else if (item.getTransactionType().equals(Constants.TransactionTypes.UPDATE)) {
                    DatabaseUtils.bindObjectToProgram(update, 1, item.getAiredEpisodes());
                    DatabaseUtils.bindObjectToProgram(update, 2, item.getEpisodeCount());
                    DatabaseUtils.bindObjectToProgram(update, 3, item.getNumber());
                    DatabaseUtils.bindObjectToProgram(update, 4, item.getOverview());
                    DatabaseUtils.bindObjectToProgram(update, 5, item.getTmdbId());
                    DatabaseUtils.bindObjectToProgram(update, 6, item.getTvdbId());
                    DatabaseUtils.bindObjectToProgram(update, 7, item.getTraktId());
                    DatabaseUtils.bindObjectToProgram(update, 8, item.getRating());
                    DatabaseUtils.bindObjectToProgram(update, 9, item.getVotes());
                    DatabaseUtils.bindObjectToProgram(update, 10, item.getPoster());
                    DatabaseUtils.bindObjectToProgram(update, 11, item.getTraktId());

                    update.execute();
                }
            }
            myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            if (ex.getMessage().contains("bindargs")) {
                canpassbindargs = true;
            } else
                NLog.e(TAG, ex);
        } finally {
            myDataBase.endTransaction();
        }

        if (canpassbindargs) {
            try {
                myDataBase.beginTransaction();
                for (int i = 0; i < localData.size(); i++) {
                    Season item = localData.get(i);


                    if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                        Cursor cursor = myDataBase.rawQuery("Select " + Season.COLUMNS.TraktId + " From " + Season.TABLE_NAME + " Where " + Season.COLUMNS.TraktId + "=" + item.getTraktId(), null);

                        if (cursor != null) {
                            int cnt = cursor.getCount();
                            if (cnt > 0) {
                                item.setTransactionType(Constants.TransactionTypes.UPDATE);
                            }
                            cursor.close();
                        }
                    }

                    if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                        ContentValues cv = new ContentValues();
                        cv.put(Season.COLUMNS.SeriesId, SeriesId);
                        cv.put(Season.COLUMNS.AiredEpisodes, item.getAiredEpisodes());
                        cv.put(Season.COLUMNS.EpisodeCount, item.getEpisodeCount());
                        cv.put(Season.COLUMNS.Number, item.getNumber());
                        cv.put(Season.COLUMNS.Overview, item.getOverview());
                        cv.put(Season.COLUMNS.TmdbId, item.getTmdbId());
                        cv.put(Season.COLUMNS.TvdbId, item.getTvdbId());
                        cv.put(Season.COLUMNS.TraktId, item.getTraktId());
                        cv.put(Season.COLUMNS.Rating, item.getRating());
                        cv.put(Season.COLUMNS.Votes, item.getVotes());
                        cv.put(Season.COLUMNS.Poster, item.getPoster());

                        myDataBase.insert(Season.TABLE_NAME, "", cv);
                    } else if (item.getTransactionType().equals(Constants.TransactionTypes.UPDATE)) {
                        ContentValues cv = new ContentValues();
                        cv.put(Season.COLUMNS.SeriesId, SeriesId);
                        cv.put(Season.COLUMNS.AiredEpisodes, item.getAiredEpisodes());
                        cv.put(Season.COLUMNS.EpisodeCount, item.getEpisodeCount());
                        cv.put(Season.COLUMNS.Number, item.getNumber());
                        cv.put(Season.COLUMNS.Overview, item.getOverview());
                        cv.put(Season.COLUMNS.TmdbId, item.getTmdbId());
                        cv.put(Season.COLUMNS.TvdbId, item.getTvdbId());
                        cv.put(Season.COLUMNS.TraktId, item.getTraktId());
                        cv.put(Season.COLUMNS.Rating, item.getRating());
                        cv.put(Season.COLUMNS.Votes, item.getVotes());
                        cv.put(Season.COLUMNS.Poster, item.getPoster());

                        myDataBase.update(Season.TABLE_NAME, cv, Season.COLUMNS.TraktId + "=" + item.getTraktId(), null);
                    }
                }
                myDataBase.setTransactionSuccessful();
            } catch (Exception ex) {
                NLog.e(TAG, ex);
            } finally {
                myDataBase.endTransaction();
            }
        }
    }

    public void fillEpisodes(ArrayList<Episode> localData, int SeriesId) {
        if (localData == null | (localData != null && localData.size() == 0))
            return;

        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        boolean canpassbindargs = false;
        try {
            String sqlInsert = "insert into Episodes (SeriesId,EpisodeName,EpisodeNumber,FirstAiredMs,GuestStars,Overview,Rating,RatingCount,SeasonNumber,AirsAfterSeason,AirsBeforeSeason,AirsBeforeEpisode,EpisodeImage,LastUpdated,TvdbSeriesId,TvdbSeasonId,TvdbEpisodeId,Collected,Watched,Favorite) " + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            String sqlUpdate = "update Episodes set EpisodeName=?,EpisodeNumber=?,FirstAiredMs=?,GuestStars=?,Overview=?,Rating=?,RatingCount=?,SeasonNumber=?,AirsAfterSeason=?,AirsBeforeSeason=?,AirsBeforeEpisode=?,EpisodeImage=?,LastUpdated=?,TvdbSeriesId=?,TvdbSeasonId=? where SeriesId=? and SeasonNumber=? and EpisodeNumber=?";
            android.database.sqlite.SQLiteStatement update = myDataBase.compileStatement(sqlUpdate);

            myDataBase.beginTransaction();
            for (int i = 0; i < localData.size(); i++) {
                Episode item = localData.get(i);

                if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                    Cursor cursor = myDataBase.rawQuery("Select " + Episode.COLUMNS.TvdbEpisodeId + " From " + Episode.TABLE_NAME + " Where " + Episode.COLUMNS.SeriesId + "=" + SeriesId + " and " + Episode.COLUMNS.SeasonNumber + "=" + item.getSeasonNumber() + " and " + Episode.COLUMNS.EpisodeNumber + "=" + item.getEpisodeNumber(), null);

                    if (cursor != null) {
                        int cnt = cursor.getCount();
                        if (cnt > 0) {
                            item.setTransactionType(Constants.TransactionTypes.UPDATE);
                        }
                        cursor.close();
                    }
                }

                if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                    DatabaseUtils.bindObjectToProgram(insert, 1, SeriesId);
                    DatabaseUtils.bindObjectToProgram(insert, 2, item.getEpisodeName());
                    DatabaseUtils.bindObjectToProgram(insert, 3, item.getEpisodeNumber());
                    DatabaseUtils.bindObjectToProgram(insert, 4, item.getFirstAiredMs());
                    DatabaseUtils.bindObjectToProgram(insert, 5, item.getGuestStars());
                    DatabaseUtils.bindObjectToProgram(insert, 6, item.getOverview());
                    DatabaseUtils.bindObjectToProgram(insert, 7, item.getRating());
                    DatabaseUtils.bindObjectToProgram(insert, 8, item.getRatingCount());
                    DatabaseUtils.bindObjectToProgram(insert, 9, item.getSeasonNumber());
                    DatabaseUtils.bindObjectToProgram(insert, 10, item.getAirsAfterSeason());
                    DatabaseUtils.bindObjectToProgram(insert, 11, item.getAirsBeforeSeason());
                    DatabaseUtils.bindObjectToProgram(insert, 12, item.getAirsBeforeEpisode());
                    DatabaseUtils.bindObjectToProgram(insert, 13, item.getEpisodeImage());
                    DatabaseUtils.bindObjectToProgram(insert, 14, item.getLastUpdated());
                    DatabaseUtils.bindObjectToProgram(insert, 15, item.getTvdbSeriesId());
                    DatabaseUtils.bindObjectToProgram(insert, 16, item.getTvdbSeasonId());
                    DatabaseUtils.bindObjectToProgram(insert, 17, item.getTvdbEpisodeId());
                    DatabaseUtils.bindObjectToProgram(insert, 18, item.getCollected());
                    DatabaseUtils.bindObjectToProgram(insert, 19, item.getWatched());
                    DatabaseUtils.bindObjectToProgram(insert, 20, item.getFavorite());

                    insert.execute();
                } else if (item.getTransactionType().equals(Constants.TransactionTypes.UPDATE)) {
                    DatabaseUtils.bindObjectToProgram(update, 1, item.getEpisodeName());
                    DatabaseUtils.bindObjectToProgram(update, 2, item.getEpisodeNumber());
                    DatabaseUtils.bindObjectToProgram(update, 3, item.getFirstAiredMs());
                    DatabaseUtils.bindObjectToProgram(update, 4, item.getGuestStars());
                    DatabaseUtils.bindObjectToProgram(update, 5, item.getOverview());
                    DatabaseUtils.bindObjectToProgram(update, 6, item.getRating());
                    DatabaseUtils.bindObjectToProgram(update, 7, item.getRatingCount());
                    DatabaseUtils.bindObjectToProgram(update, 8, item.getSeasonNumber());
                    DatabaseUtils.bindObjectToProgram(update, 9, item.getAirsAfterSeason());
                    DatabaseUtils.bindObjectToProgram(update, 10, item.getAirsBeforeSeason());
                    DatabaseUtils.bindObjectToProgram(update, 11, item.getAirsBeforeEpisode());
                    DatabaseUtils.bindObjectToProgram(update, 12, item.getEpisodeImage());
                    DatabaseUtils.bindObjectToProgram(update, 13, item.getLastUpdated());
                    DatabaseUtils.bindObjectToProgram(update, 14, item.getTvdbSeriesId());
                    DatabaseUtils.bindObjectToProgram(update, 15, item.getTvdbSeasonId());
                    DatabaseUtils.bindObjectToProgram(update, 16, SeriesId);
                    DatabaseUtils.bindObjectToProgram(update, 17, item.getSeasonNumber());
                    DatabaseUtils.bindObjectToProgram(update, 18, item.getEpisodeNumber());

                    update.execute();
                }
            }
            myDataBase.setTransactionSuccessful();
        } catch (Exception ex) {
            if (ex.getMessage().contains("bindargs")) {
                canpassbindargs = true;
            } else
                NLog.e(TAG, ex);
        } finally {
            myDataBase.endTransaction();
        }

        if (canpassbindargs) {
            try {
                myDataBase.beginTransaction();
                for (int i = 0; i < localData.size(); i++) {
                    Episode item = localData.get(i);

                    if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                        Cursor cursor = myDataBase.rawQuery("Select " + Episode.COLUMNS.TvdbEpisodeId + " From " + Episode.TABLE_NAME + " Where " + Episode.COLUMNS.TvdbEpisodeId + " = '" + item.getTvdbEpisodeId() + "'", null);

                        if (cursor != null) {
                            int cnt = cursor.getCount();
                            if (cnt > 0) {
                                item.setTransactionType(Constants.TransactionTypes.UPDATE);
                            }
                            cursor.close();
                        }
                    }

                    if (item.getTransactionType().equals(Constants.TransactionTypes.INSERT)) {
                        ContentValues cv = new ContentValues();

                        cv.put(Episode.COLUMNS.SeriesId, SeriesId);
                        cv.put(Episode.COLUMNS.EpisodeName, item.getEpisodeName());
                        cv.put(Episode.COLUMNS.EpisodeNumber, item.getEpisodeNumber());
                        cv.put(Episode.COLUMNS.FirstAiredMs, item.getFirstAiredMs());
                        cv.put(Episode.COLUMNS.GuestStars, item.getGuestStars());
                        cv.put(Episode.COLUMNS.Overview, item.getOverview());
                        cv.put(Episode.COLUMNS.Rating, item.getRating());
                        cv.put(Episode.COLUMNS.RatingCount, item.getRatingCount());
                        cv.put(Episode.COLUMNS.SeasonNumber, item.getSeasonNumber());
                        cv.put(Episode.COLUMNS.AirsAfterSeason, item.getAirsAfterSeason());
                        cv.put(Episode.COLUMNS.AirsBeforeSeason, item.getAirsBeforeSeason());
                        cv.put(Episode.COLUMNS.AirsBeforeEpisode, item.getAirsBeforeEpisode());
                        cv.put(Episode.COLUMNS.EpisodeImage, item.getEpisodeImage());
                        cv.put(Episode.COLUMNS.LastUpdated, item.getLastUpdated());
                        cv.put(Episode.COLUMNS.TvdbSeriesId, item.getTvdbSeriesId());
                        cv.put(Episode.COLUMNS.TvdbSeasonId, item.getTvdbSeasonId());
                        cv.put(Episode.COLUMNS.TvdbEpisodeId, item.getTvdbEpisodeId());
                        cv.put(Episode.COLUMNS.Collected, item.getCollected());
                        cv.put(Episode.COLUMNS.Watched, item.getWatched());
                        cv.put(Episode.COLUMNS.Favorite, item.getFavorite());

                        myDataBase.insert(Episode.TABLE_NAME, "", cv);
                    } else if (item.getTransactionType().equals(Constants.TransactionTypes.UPDATE)) {
                        ContentValues cv = new ContentValues();

                        cv.put(Episode.COLUMNS.SeriesId, SeriesId);
                        cv.put(Episode.COLUMNS.EpisodeName, item.getEpisodeName());
                        cv.put(Episode.COLUMNS.EpisodeNumber, item.getEpisodeNumber());
                        cv.put(Episode.COLUMNS.FirstAiredMs, item.getFirstAiredMs());
                        cv.put(Episode.COLUMNS.GuestStars, item.getGuestStars());
                        cv.put(Episode.COLUMNS.Overview, item.getOverview());
                        cv.put(Episode.COLUMNS.Rating, item.getRating());
                        cv.put(Episode.COLUMNS.RatingCount, item.getRatingCount());
                        cv.put(Episode.COLUMNS.SeasonNumber, item.getSeasonNumber());
                        cv.put(Episode.COLUMNS.AirsAfterSeason, item.getAirsAfterSeason());
                        cv.put(Episode.COLUMNS.AirsBeforeSeason, item.getAirsBeforeSeason());
                        cv.put(Episode.COLUMNS.AirsBeforeEpisode, item.getAirsBeforeEpisode());
                        cv.put(Episode.COLUMNS.EpisodeImage, item.getEpisodeImage());
                        cv.put(Episode.COLUMNS.LastUpdated, item.getLastUpdated());
                        cv.put(Episode.COLUMNS.TvdbSeriesId, item.getTvdbSeriesId());
                        cv.put(Episode.COLUMNS.TvdbSeasonId, item.getTvdbSeasonId());
                        cv.put(Episode.COLUMNS.TvdbEpisodeId, item.getTvdbEpisodeId());
                        cv.put(Episode.COLUMNS.Collected, item.getCollected());
                        cv.put(Episode.COLUMNS.Watched, item.getWatched());

                        myDataBase.update(Episode.TABLE_NAME, cv, Episode.COLUMNS.TvdbEpisodeId + "=" + item.getTvdbEpisodeId(), null);
                    }
                }
                myDataBase.setTransactionSuccessful();
            } catch (Exception ex) {
                NLog.e(TAG, ex);
            } finally {
                myDataBase.endTransaction();
            }
        }
    }

}
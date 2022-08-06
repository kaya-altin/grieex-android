package com.grieex.update;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.grieex.helper.Constants;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;
import com.grieex.model.tables.Backdrop;
import com.grieex.model.tables.Cast;
import com.grieex.model.tables.Episode;
import com.grieex.model.tables.Movie;
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

public class RepairDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = RepairDatabaseHelper.class.getName();
    private static RepairDatabaseHelper helper;

    private SQLiteDatabase myDataBase;
    private final Context myContext;

    public synchronized static RepairDatabaseHelper getInstance(Context context) {
        if (helper == null)
            helper = new RepairDatabaseHelper(context.getApplicationContext());

        return helper;
    }

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    private RepairDatabaseHelper(Context context) {
        super(context, GrieeXSettings.DB_NAME+ ".repair", null, GrieeXSettings.DB_VERSION);
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
            checkDB = SQLiteDatabase.openDatabase(GrieeXSettings.DB_PATHFULL_REPAIR, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database does't exist yet.
            Log.d("CheckDatabase", GrieeXSettings.DB_PATHFULL_REPAIR + "\r\n" + e.getMessage());
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
        String outFileName = GrieeXSettings.DB_PATHFULL_REPAIR;

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
        myDataBase = SQLiteDatabase.openDatabase(GrieeXSettings.DB_PATHFULL_REPAIR, null, SQLiteDatabase.OPEN_READWRITE);
        myDataBase.setLocale(new Locale("tr_TR"));
    }

    public void DeleteDataBase() {
        close();

        File dbFile = new File(GrieeXSettings.DB_PATHFULL_REPAIR);
        if (dbFile.exists())
            dbFile.delete();
    }

    public void RepairFinish() {
        close();

        File dbFile = new File(GrieeXSettings.DB_PATHFULL_REPAIR);
        if (dbFile.exists())
            dbFile.renameTo(new File(GrieeXSettings.DB_PATHFULL));
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("GrieeX", "onUpgrade");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }



    private ArrayList<?> GetCursorWithObject(String query, Class<?> issueObj) {
        if (myDataBase == null) {
            createDataBase();
            openDataBase();
        }

        if (!myDataBase.isOpen()) {
            openDataBase();
        }

        ArrayList<IDataModelObject> dmObjects = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = myDataBase.rawQuery(query, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    IDataModelObject dmObject = (IDataModelObject) issueObj.newInstance();
                    dmObject.LoadWithCursorRow(cursor);
                    dmObjects.add(dmObject);
                }
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
        try {
            crsr = myDataBase.rawQuery(query, null);
            return crsr;
        } catch (Exception ex) {
            NLog.e(TAG, ex);
        }

        return null;
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


    private ArrayList<Cast> getCasts(int id, Constants.CollectionType collectionType) {
        try {
            return (ArrayList<Cast>) helper.GetCursorWithObject("SELECT * FROM Casts Where ObjectID=" + id + " and CollectionType=" + collectionType.value, Cast.class);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }


    public ArrayList<Trailer> getTrailers(int id) {
        try {
            return (ArrayList<Trailer>) helper.GetCursorWithObject("SELECT * FROM " + Trailer.TABLE_NAME + " Where " + Trailer.COLUMNS.ObjectID + "=" + id, Trailer.class);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
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

      private ArrayList<Season> getSeasons(int id) {
        try {

            return (ArrayList<Season>) helper.GetCursorWithObject("SELECT Seasons.*, (SELECT Count(*) FROM Episodes Where SeriesId=Seasons.SeriesId and SeasonNumber=Seasons.Number and Watched=1) as WatchedCount FROM Seasons Where SeriesId=" + id + " Order By Seasons.Number desc", Season.class);

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
                DatabaseUtils.bindObjectToProgram(insert, 1, localData.get(i).getOriginalName());
                DatabaseUtils.bindObjectToProgram(insert, 2, localData.get(i).getOtherName());
                DatabaseUtils.bindObjectToProgram(insert, 3, localData.get(i).getDirector());
                DatabaseUtils.bindObjectToProgram(insert, 4, localData.get(i).getWriter());
                DatabaseUtils.bindObjectToProgram(insert, 5, localData.get(i).getGenre());
                DatabaseUtils.bindObjectToProgram(insert, 6, localData.get(i).getYear());
                DatabaseUtils.bindObjectToProgram(insert, 7, localData.get(i).getUserRating());
                DatabaseUtils.bindObjectToProgram(insert, 8, localData.get(i).getVotes());
                DatabaseUtils.bindObjectToProgram(insert, 9, localData.get(i).getImdbUserRating());
                DatabaseUtils.bindObjectToProgram(insert, 10, localData.get(i).getImdbVotes());
                DatabaseUtils.bindObjectToProgram(insert, 11, localData.get(i).getTmdbUserRating());
                DatabaseUtils.bindObjectToProgram(insert, 12, localData.get(i).getTmdbVotes());
                DatabaseUtils.bindObjectToProgram(insert, 13, localData.get(i).getRunningTime());
                DatabaseUtils.bindObjectToProgram(insert, 14, localData.get(i).getCountry());
                DatabaseUtils.bindObjectToProgram(insert, 15, localData.get(i).getLanguage());
                DatabaseUtils.bindObjectToProgram(insert, 16, localData.get(i).getEnglishPlot());
                DatabaseUtils.bindObjectToProgram(insert, 17, localData.get(i).getOtherPlot());
                DatabaseUtils.bindObjectToProgram(insert, 18, localData.get(i).getBudget());
                DatabaseUtils.bindObjectToProgram(insert, 19, localData.get(i).getProductionCompany());
                DatabaseUtils.bindObjectToProgram(insert, 20, localData.get(i).getImdbNumber());
                DatabaseUtils.bindObjectToProgram(insert, 21, localData.get(i).getTmdbNumber());
                DatabaseUtils.bindObjectToProgram(insert, 22, localData.get(i).getArchivesNumber());
                DatabaseUtils.bindObjectToProgram(insert, 23, localData.get(i).getSubtitle());
                DatabaseUtils.bindObjectToProgram(insert, 24, localData.get(i).getDubbing());
                DatabaseUtils.bindObjectToProgram(insert, 25, localData.get(i).getPersonalRating());
                DatabaseUtils.bindObjectToProgram(insert, 26, localData.get(i).getUserColumn1());
                DatabaseUtils.bindObjectToProgram(insert, 27, localData.get(i).getUserColumn2());
                DatabaseUtils.bindObjectToProgram(insert, 28, localData.get(i).getUserColumn3());
                DatabaseUtils.bindObjectToProgram(insert, 29, localData.get(i).getUserColumn4());
                DatabaseUtils.bindObjectToProgram(insert, 30, localData.get(i).getUserColumn5());
                DatabaseUtils.bindObjectToProgram(insert, 31, localData.get(i).getUserColumn6());
                DatabaseUtils.bindObjectToProgram(insert, 32, localData.get(i).getRlsType());
                DatabaseUtils.bindObjectToProgram(insert, 33, localData.get(i).getRlsGroup());
                DatabaseUtils.bindObjectToProgram(insert, 34, localData.get(i).getPoster());
                DatabaseUtils.bindObjectToProgram(insert, 35, localData.get(i).getNote());
                DatabaseUtils.bindObjectToProgram(insert, 36, localData.get(i).getSeen());
                DatabaseUtils.bindObjectToProgram(insert, 37, localData.get(i).getIsSyncWaiting());
                DatabaseUtils.bindObjectToProgram(insert, 38, localData.get(i).getContentProvider());
                DatabaseUtils.bindObjectToProgram(insert, 39, localData.get(i).getInsertDate());
                DatabaseUtils.bindObjectToProgram(insert, 40, localData.get(i).getUpdateDate());

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
                    ContentValues cv = new ContentValues();
                    cv.put(Movie.COLUMNS.OriginalName, localData.get(i).getOriginalName());
                    cv.put(Movie.COLUMNS.OtherName, localData.get(i).getOtherName());
                    cv.put(Movie.COLUMNS.Director, localData.get(i).getDirector());
                    cv.put(Movie.COLUMNS.Writer, localData.get(i).getWriter());
                    cv.put(Movie.COLUMNS.Genre, localData.get(i).getGenre());
                    cv.put(Movie.COLUMNS.Year, localData.get(i).getYear());
                    cv.put(Movie.COLUMNS.UserRating, localData.get(i).getUserRating());
                    cv.put(Movie.COLUMNS.Votes, localData.get(i).getVotes());
                    cv.put(Movie.COLUMNS.ImdbUserRating, localData.get(i).getImdbUserRating());
                    cv.put(Movie.COLUMNS.ImdbVotes, localData.get(i).getImdbVotes());
                    cv.put(Movie.COLUMNS.TmdbUserRating, localData.get(i).getTmdbUserRating());
                    cv.put(Movie.COLUMNS.RunningTime, localData.get(i).getRunningTime());
                    cv.put(Movie.COLUMNS.Country, localData.get(i).getCountry());
                    cv.put(Movie.COLUMNS.Language, localData.get(i).getLanguage());
                    cv.put(Movie.COLUMNS.EnglishPlot, localData.get(i).getEnglishPlot());
                    cv.put(Movie.COLUMNS.OtherPlot, localData.get(i).getOtherPlot());
                    cv.put(Movie.COLUMNS.Budget, localData.get(i).getBudget());
                    cv.put(Movie.COLUMNS.ProductionCompany, localData.get(i).getProductionCompany());
                    cv.put(Movie.COLUMNS.ImdbNumber, localData.get(i).getImdbNumber());
                    cv.put(Movie.COLUMNS.TmdbNumber, localData.get(i).getTmdbNumber());
                    cv.put(Movie.COLUMNS.ArchivesNumber, localData.get(i).getArchivesNumber());
                    cv.put(Movie.COLUMNS.Subtitle, localData.get(i).getSubtitle());
                    cv.put(Movie.COLUMNS.Dubbing, localData.get(i).getDubbing());
                    cv.put(Movie.COLUMNS.PersonalRating, localData.get(i).getPersonalRating());
                    cv.put(Movie.COLUMNS.UserColumn1, localData.get(i).getUserColumn1());
                    cv.put(Movie.COLUMNS.UserColumn2, localData.get(i).getUserColumn2());
                    cv.put(Movie.COLUMNS.UserColumn3, localData.get(i).getUserColumn3());
                    cv.put(Movie.COLUMNS.UserColumn4, localData.get(i).getUserColumn4());
                    cv.put(Movie.COLUMNS.UserColumn5, localData.get(i).getUserColumn5());
                    cv.put(Movie.COLUMNS.UserColumn6, localData.get(i).getUserColumn6());
                    cv.put(Movie.COLUMNS.RlsType, localData.get(i).getRlsType());
                    cv.put(Movie.COLUMNS.RlsGroup, localData.get(i).getRlsGroup());
                    cv.put(Movie.COLUMNS.Poster, localData.get(i).getPoster());
                    cv.put(Movie.COLUMNS.Note, localData.get(i).getNote());
                    cv.put(Movie.COLUMNS.Seen, localData.get(i).getSeen());
                    cv.put(Movie.COLUMNS.IsSyncWaiting, localData.get(i).getIsSyncWaiting());
                    cv.put(Movie.COLUMNS.ContentProvider, localData.get(i).getContentProvider());
                    cv.put(Movie.COLUMNS.InsertDate, localData.get(i).getInsertDate());
                    cv.put(Movie.COLUMNS.UpdateDate, localData.get(i).getUpdateDate());

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

    public void fillFiles(ArrayList<com.grieex.model.tables.File> localData, long MovieID) {
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
                DatabaseUtils.bindObjectToProgram(insert, 1, localData.get(i).getFileName());
                DatabaseUtils.bindObjectToProgram(insert, 2, localData.get(i).getResolution());
                DatabaseUtils.bindObjectToProgram(insert, 3, localData.get(i).getVideoCodec());
                DatabaseUtils.bindObjectToProgram(insert, 4, localData.get(i).getVideoBitrate());
                DatabaseUtils.bindObjectToProgram(insert, 5, localData.get(i).getFps());
                DatabaseUtils.bindObjectToProgram(insert, 6, localData.get(i).getAudioCodec1());
                DatabaseUtils.bindObjectToProgram(insert, 7, localData.get(i).getAudioChannels1());
                DatabaseUtils.bindObjectToProgram(insert, 8, localData.get(i).getAudioBitrate1());
                DatabaseUtils.bindObjectToProgram(insert, 9, localData.get(i).getAudioSampleRate1());
                DatabaseUtils.bindObjectToProgram(insert, 10, localData.get(i).getAudioSize1());
                DatabaseUtils.bindObjectToProgram(insert, 11, localData.get(i).getAudioCodec2());
                DatabaseUtils.bindObjectToProgram(insert, 12, localData.get(i).getAudioChannels2());
                DatabaseUtils.bindObjectToProgram(insert, 13, localData.get(i).getAudioBitrate2());
                DatabaseUtils.bindObjectToProgram(insert, 14, localData.get(i).getAudioSampleRate2());
                DatabaseUtils.bindObjectToProgram(insert, 15, localData.get(i).getAudioSize2());
                DatabaseUtils.bindObjectToProgram(insert, 16, localData.get(i).getTotalFrames());
                DatabaseUtils.bindObjectToProgram(insert, 17, localData.get(i).getLenght());
                DatabaseUtils.bindObjectToProgram(insert, 18, localData.get(i).getVideoSize());
                DatabaseUtils.bindObjectToProgram(insert, 19, localData.get(i).getFileSize());
                DatabaseUtils.bindObjectToProgram(insert, 20, localData.get(i).getChapter());
                DatabaseUtils.bindObjectToProgram(insert, 21, localData.get(i).getVideoAspectRatio());
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
                    ContentValues cv = new ContentValues();
                    cv.put(com.grieex.model.tables.File.COLUMNS.FileName, localData.get(i).getFileName());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Resolution, localData.get(i).getResolution());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoCodec, localData.get(i).getVideoCodec());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoBitrate, localData.get(i).getVideoBitrate());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Fps, localData.get(i).getFps());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioCodec1, localData.get(i).getAudioCodec1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioChannels1, localData.get(i).getAudioChannels1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioBitrate1, localData.get(i).getAudioBitrate1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSampleRate1, localData.get(i).getAudioSampleRate1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSize1, localData.get(i).getAudioSize1());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioCodec2, localData.get(i).getAudioCodec2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioChannels2, localData.get(i).getAudioChannels2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioBitrate2, localData.get(i).getAudioBitrate2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSampleRate2, localData.get(i).getAudioSampleRate2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.AudioSize2, localData.get(i).getAudioSize2());
                    cv.put(com.grieex.model.tables.File.COLUMNS.TotalFrames, localData.get(i).getTotalFrames());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Lenght, localData.get(i).getLenght());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoSize, localData.get(i).getVideoSize());
                    cv.put(com.grieex.model.tables.File.COLUMNS.FileSize, localData.get(i).getFileSize());
                    cv.put(com.grieex.model.tables.File.COLUMNS.Chapter, localData.get(i).getChapter());
                    cv.put(com.grieex.model.tables.File.COLUMNS.VideoAspectRatio, localData.get(i).getVideoAspectRatio());
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

    public void fillCasts(ArrayList<Cast> localData, long MovieID) {
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

            String sqlInsert = "insert into " + Cast.TABLE_NAME + " (" +
                    Cast.COLUMNS.Name + "," +
                    Cast.COLUMNS.Character + "," +
                    Cast.COLUMNS.Url + "," +
                    Cast.COLUMNS.ImageUrl + "," +
                    Cast.COLUMNS.CastID + "," +
                    Cast.COLUMNS.CollectionType + "," +
                    Cast.COLUMNS.ObjectID + ") " + " values (?,?,?,?,?,?,?)";

            android.database.sqlite.SQLiteStatement insert = myDataBase.compileStatement(sqlInsert);

            myDataBase.beginTransaction();
            for (int i = 0; i < localData.size(); i++) {
                DatabaseUtils.bindObjectToProgram(insert, 1, localData.get(i).getName());
                DatabaseUtils.bindObjectToProgram(insert, 2, localData.get(i).getCharacter());
                DatabaseUtils.bindObjectToProgram(insert, 3, localData.get(i).getUrl());
                DatabaseUtils.bindObjectToProgram(insert, 4, localData.get(i).getImageUrl());
                DatabaseUtils.bindObjectToProgram(insert, 5, localData.get(i).getCastID());
                DatabaseUtils.bindObjectToProgram(insert, 6, localData.get(i).getCollectionType());
                DatabaseUtils.bindObjectToProgram(insert, 7, MovieID);

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
                    ContentValues cv = new ContentValues();
                    cv.put(Cast.COLUMNS.Name, localData.get(i).getName());
                    cv.put(Cast.COLUMNS.Character, localData.get(i).getCharacter());
                    cv.put(Cast.COLUMNS.Url, localData.get(i).getUrl());
                    cv.put(Cast.COLUMNS.ImageUrl, localData.get(i).getImageUrl());
                    cv.put(Cast.COLUMNS.CastID, localData.get(i).getCastID());
                    cv.put(Cast.COLUMNS.CollectionType, localData.get(i).getCollectionType());
                    cv.put(Cast.COLUMNS.ObjectID, MovieID);

                    myDataBase.insert(Cast.TABLE_NAME, "", cv);
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
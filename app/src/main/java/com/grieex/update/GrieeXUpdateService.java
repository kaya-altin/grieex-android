package com.grieex.update;

import android.content.Intent;

import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.FileUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.WakefulIntentService;
import com.grieex.model.tables.File;
import com.grieex.model.tables.Movie;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GrieeXUpdateService extends WakefulIntentService {
    private static final String TAG = GrieeXUpdateService.class.getName();
    private BroadcastNotifier mBroadcaster;
    private DatabaseHelper dbHelper;
    private int mOldVersion = 1;
    private boolean bRepairDatabase = false;

    public GrieeXUpdateService() {
        super("GrieeXUpdateService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBroadcaster = new BroadcastNotifier(this);
        dbHelper = DatabaseHelper.getInstance(this);
        mOldVersion = UpdateManager.GetDatabaseVersion(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void doWakefulWork(Intent intent) {
        mBroadcaster.broadcastIntentWithState(Constants.STATE_GRIEEX_UPDATE_STARTED);

        try {
            String[] sqlArray = getUpdate();

            if (sqlArray != null) {
                try {
                    if (sqlArray.length != 0) {
                        dbHelper.ExecuteQueries(sqlArray);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!bRepairDatabase) {
//                DatabaseMerge dm = new DatabaseMerge(this);
//                dm.start();

                SetDatabaseVersion(GrieeXSettings.DB_VERSION);
            } else {
                RepairDatabase();
            }
            mBroadcaster.broadcastIntentWithState(Constants.STATE_GRIEEX_UPDATE_COMPLETED);

            mBroadcaster = null;
            stopSelf();
        } catch (Exception e) {
            if (mBroadcaster != null)
                mBroadcaster.broadcastIntentWithState(Constants.STATE_GRIEEX_UPDATE_NOT_COMPLETED);
            mBroadcaster = null;
            NLog.e(TAG, e);
            stopSelf();
        }

    }

    private void SetDatabaseVersion(int iVersion) {
        try {
            dbHelper.ExecuteQuery("UPDATE android_database_version SET version=" + iVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getUpdate() {
        ArrayList<String> returnList = new ArrayList<>();

        if (mOldVersion < 2)
            returnList.addAll(getV2());

        if (mOldVersion < 3)
            returnList.addAll(getV3());

        if (mOldVersion < 4)
            returnList.addAll(getV4());

        if (mOldVersion < 5)
            returnList.addAll(getV5());

        if (mOldVersion < 6)
            returnList.addAll(getV6());

        if (mOldVersion < 8)
            returnList.addAll(getV8());

        if (mOldVersion < 9)
            returnList.addAll(getV9());

        if (mOldVersion < 10)
            returnList.addAll(getV10());

        if (mOldVersion < 11)
            returnList.addAll(getV11());

        return returnList.toArray(new String[returnList.size()]);
    }

    private ArrayList<String> getV2() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("ALTER TABLE Movies ADD COLUMN ImdbUserRating TEXT");
        returnList.add("ALTER TABLE Movies ADD COLUMN ImdbVotes TEXT");
        returnList.add("ALTER TABLE Movies ADD COLUMN TmdbUserRating TEXT");
        returnList.add("ALTER TABLE Movies ADD COLUMN TmdbVotes TEXT");

        return returnList;
    }

    private ArrayList<String> getV3() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("ALTER TABLE Movies ADD COLUMN TmdbNumber TEXT");
        returnList.add("CREATE TABLE Trailers (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, ObjectID INTEGER, Url TEXT, Type TEXT)");


        return returnList;
    }

    private ArrayList<String> getV4() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("ALTER TABLE Movies ADD COLUMN RlsType TEXT");
        returnList.add("ALTER TABLE Movies ADD COLUMN RlsGroup TEXT");


        return returnList;
    }

    private ArrayList<String> getV5() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("ALTER TABLE Movies ADD COLUMN UserColumn5 TEXT");
        returnList.add("ALTER TABLE Movies ADD COLUMN UserColumn6 TEXT");


        return returnList;
    }

    private ArrayList<String> getV6() {
//        ArrayList<String> returnList = new ArrayList<>();
//        returnList.add("Delete From Backdrops");
//        returnList.add("Delete From Casts");
//        returnList.add("Delete From Trailers");
//        returnList.add("Delete From Imdb250");
//        returnList.add("ALTER TABLE Backdrops ADD COLUMN CollectionType INTEGER");
//        returnList.add("ALTER TABLE Casts ADD COLUMN CollectionType INTEGER");
//        returnList.add("ALTER TABLE Trailers ADD COLUMN CollectionType INTEGER");
//        returnList.add("delete from sqlite_sequence where name='Backdrops'");
//        returnList.add("delete from sqlite_sequence where name='Casts'");
//        returnList.add("delete from sqlite_sequence where name='Trailers'");
//        returnList.add("delete from sqlite_sequence where name='Imdb250'");
//
//        returnList.add("CREATE TABLE Series (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, SeriesName TEXT, Overview TEXT, FirstAired TEXT, Network TEXT, ImdbId TEXT, TmdbId INTEGER, TvdbId INTEGER, TraktId INTEGER, Language TEXT, Country TEXT, Genres TEXT, Runtime TEXT, Certification TEXT, AirDay TEXT, AirTime TEXT, AirYear INTEGER, Timezone TEXT, Status INTEGER, Rating REAL, Votes INTEGER, SeriesLastUpdate TEXT, Poster TEXT, Fanart TEXT, Homepage TEXT, ContentProvider INTEGER, InsertDate DATE, UpdateDate DATE)");
//        returnList.add("CREATE TABLE Seasons (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, SeriesId INTEGER, AiredEpisodes INTEGER, EpisodeCount INTEGER, Number INTEGER, Overview TEXT, Rating TEXT, TmdbId INTEGER, TvdbId INTEGER, TraktId INTEGER, Votes TEXT, Poster TEXT)");
//        returnList.add("CREATE TABLE Episodes (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, SeriesId INTEGER, SeasonId INTEGER, EpisodeName TEXT, EpisodeNumber INTEGER, FirstAiredMs INTEGER, GuestStars TEXT, Overview TEXT, Rating TEXT, RatingCount TEXT, SeasonNumber INTEGER, AirsAfterSeason INTEGER, AirsBeforeSeason INTEGER, AirsBeforeEpisode INTEGER, EpisodeImage TEXT, LastUpdated INTEGER, TvdbEpisodeId INTEGER, TvdbSeriesId INTEGER, TvdbSeasonId INTEGER, Collected BOOLEAN, Watched BOOLEAN)");
//
//        returnList.add("CREATE INDEX idx_Backdrops_CollectionType ON Backdrops (CollectionType)");
//        returnList.add("CREATE INDEX idx_Backdrops_ObjectID ON Backdrops (ObjectID)");
//        returnList.add("CREATE INDEX idx_Casts_CollectionType ON Casts (CollectionType)");
//        returnList.add("CREATE INDEX idx_Casts_ObjectID ON Casts (ObjectID)");
//        returnList.add("CREATE INDEX idx_Episodes_EpisodeNumber ON Episodes (EpisodeNumber)");
//        returnList.add("CREATE INDEX idx_Episodes_SeasonId ON Episodes (SeasonId)");
//        returnList.add("CREATE INDEX idx_Episodes_SeasonNumber ON Episodes (SeasonNumber)");
//        returnList.add("CREATE INDEX idx_Episodes_SeriesId ON Episodes (SeriesId)");
//        returnList.add("CREATE INDEX idx_Files_MovieID ON Files (MovieID)");
//        returnList.add("CREATE INDEX idx_ListsMovies_ListID ON ListsMovies (ListID)");
//        returnList.add("CREATE INDEX idx_ListsMovies_MovieID ON ListsMovies (MovieID)");
//        returnList.add("CREATE INDEX idx_Queues_ObjectID ON Queues (ObjectID)");
//        returnList.add("CREATE INDEX idx_Queues_Type ON Queues (Type)");
//        returnList.add("CREATE INDEX idx_Seasons_Number ON Seasons (Number)");
//        returnList.add("CREATE INDEX idx_Seasons_SeriesId ON Seasons (SeriesId)");
//        returnList.add("CREATE INDEX idx_Trailers_CollectionType ON Trailers (CollectionType)");
//        returnList.add("CREATE INDEX idx_Trailers_ObjectID ON Trailers (ObjectID)");

        bRepairDatabase = true;
        return new ArrayList<>();
    }

    private ArrayList<String> getV8() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("Delete From Imdb250");
        returnList.add("ALTER TABLE Imdb250 ADD COLUMN Type INTEGER");


        return returnList;
    }

    private ArrayList<String> getV9() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("ALTER TABLE Series ADD COLUMN ImdbUserRating TEXT");
        returnList.add("ALTER TABLE Series ADD COLUMN ImdbVotes TEXT");
        returnList.add("ALTER TABLE Series ADD COLUMN TmdbUserRating TEXT");
        returnList.add("ALTER TABLE Series ADD COLUMN TmdbVotes TEXT");

        return returnList;
    }

    private ArrayList<String> getV10() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("ALTER TABLE Movies ADD COLUMN ReleaseDate TEXT");

        return returnList;
    }

    private ArrayList<String> getV11() {
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add("ALTER TABLE Lists ADD COLUMN ListType INTEGER");
        returnList.add("CREATE TABLE ListsSeries (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, ListID INTEGER, SeriesID INTEGER)");
        returnList.add("ALTER TABLE Movies ADD COLUMN Tags TEXT");
        returnList.add("ALTER TABLE Series ADD COLUMN Tags TEXT");
        returnList.add("UPDATE Lists SET ListType=1");

        return returnList;
    }

    private void RepairDatabase() {
        BackupDatabase();

        boolean isSuccessful = false;
        RepairDatabaseHelper repairDatabaseHelper = RepairDatabaseHelper.getInstance(this);
        try {
            ArrayList<Movie> oldMovies = (ArrayList<Movie>) dbHelper.GetCursorWithObject("Select * From Movies", Movie.class);

            for (Movie m : oldMovies) {
                long id = repairDatabaseHelper.addMovie(m);
                ArrayList<File> oldFiles = (ArrayList<File>) dbHelper.GetCursorWithObject("Select * From Files Where MovieID=" + m.getID(), File.class);
                // ArrayList<Cast> oldCasts = (ArrayList<Cast>) dbHelper.GetCursorWithObject("Select * From Casts Where ObjectID=" + m.getID(), Cast.class);

                repairDatabaseHelper.fillFiles(oldFiles, id);
                // repairDatabaseHelper.fillCasts(oldCasts, id);
            }
            isSuccessful = true;
        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            if (isSuccessful) {
                dbHelper.DeleteDataBase();
                repairDatabaseHelper.RepairFinish();
            }
        }
    }

    private void BackupDatabase() {
        int BUFFER = 1024;
        String[] _files;
        String _zipFile;

        try {
            FileUtils.dirChecker(GrieeXSettings.BACKUP_PATH);
            _zipFile = GrieeXSettings.BACKUP_PATH + "GrieeX_" + DateUtils.ConvertDateToString(Constants.DATE_FORMAT13, DateUtils.DateTimeNowString()) + ".gbf";
            String[] s = new String[1];
            s[0] = GrieeXSettings.DB_PATHFULL;
            _files = s;

            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(_zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] data = new byte[BUFFER];

            for (String _file : _files) {
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
    }

}

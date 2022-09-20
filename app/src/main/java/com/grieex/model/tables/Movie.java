package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Movie implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Movies";
    private static final String TAG = Movie.class.getName();
    private int _id;
    private String mOriginalName;
    private String mOtherName;
    private String mDirector;
    private String mWriter;
    private String mGenre;
    private String mYear;
    private String mUserRating;
    private String mVotes;
    private String mImdbUserRating;
    private String mImdbVotes;
    private String mTmdbUserRating;
    private String mTmdbVotes;
    private String mRunningTime;
    private String mCountry;
    private String mLanguage;
    private String mEnglishPlot;
    private String mOtherPlot;
    private String mBudget;
    private String mProductionCompany;
    private String mImdbNumber;
    private String mTmdbNumber;
    private String mReleaseDate;
    private String mArchivesNumber;
    private String mSubtitle;
    private String mDubbing;
    private String mPersonalRating;
    private String mUserColumn1;
    private String mUserColumn2;
    private String mUserColumn3;
    private String mUserColumn4;
    private String mUserColumn5;
    private String mUserColumn6;
    private String mRlsType;
    private String mRlsGroup;
    private String mPoster = "";
    private String mNote;
    private String mSeen = "0";
    private String mIsSyncWaiting = "0";
    private int mContentProvider;
    private String mInsertDate;
    private String mUpdateDate;
    // **************** Custom Fields **************** //
    private String mFirstCharacter;
    private ArrayList<Cast> mCast;
    private ArrayList<File> mFiles;
    private ArrayList<Backdrop> mBackdrops;
    private ArrayList<Trailer> mTrailers;
    private boolean mIsExisting = false;
    private boolean mIsSelected = false;

    public Movie() {

    }

    public Movie(int _id, String OriginalName, String OtherName, String Director, String Writer, String Genre, String Year, String UserRating, String Votes, String ImdbUserRating, String ImdbVotes, String TmdbUserRating, String TmdbVotes, String RunningTime, String Country, String Language, String EnglishPlot, String OtherPlot, String Budget, String ProductionCompany, String ImdbNumber, String TmdbNumber, String ReleaseDate, String ArchivesNumber, String Subtitle, String Dubbing, String PersonalRating, String UserColumn1, String UserColumn2, String UserColumn3, String UserColumn4, String UserColumn5, String UserColumn6, String RlsType, String RlsGroup, String Poster, String Note) {
        this._id = _id;
        this.mOriginalName = OriginalName;
        this.mOtherName = OtherName;
        this.mDirector = Director;
        this.mWriter = Writer;
        this.mGenre = Genre;
        this.mYear = Year;
        this.mUserRating = UserRating;
        this.mVotes = Votes;
        this.mImdbUserRating = ImdbUserRating;
        this.mImdbVotes = ImdbVotes;
        this.mTmdbUserRating = TmdbUserRating;
        this.mTmdbVotes = TmdbVotes;
        this.mRunningTime = RunningTime;
        this.mCountry = Country;
        this.mLanguage = Language;
        this.mEnglishPlot = EnglishPlot;
        this.mOtherPlot = OtherPlot;
        this.mBudget = Budget;
        this.mProductionCompany = ProductionCompany;
        this.mImdbNumber = ImdbNumber;
        this.mTmdbNumber = TmdbNumber;
        this.mArchivesNumber = ArchivesNumber;
        this.mReleaseDate = ReleaseDate;
        this.mSubtitle = Subtitle;
        this.mDubbing = Dubbing;
        this.mPersonalRating = PersonalRating;
        this.mUserColumn1 = UserColumn1;
        this.mUserColumn2 = UserColumn2;
        this.mUserColumn3 = UserColumn3;
        this.mUserColumn4 = UserColumn4;
        this.mUserColumn5 = UserColumn5;
        this.mUserColumn6 = UserColumn6;
        this.mRlsType = RlsType;
        this.mRlsGroup = RlsGroup;
        this.mPoster = Poster;
        this.mNote = Note;
    }

    public int getID() {
        return _id;
    }

    public void setID(int _id) {
        this._id = _id;
    }

    public String getOriginalName() {
        return mOriginalName;
    }

    public void setOriginalName(String OriginalName) {
        this.mOriginalName = OriginalName;
    }

    public String getOtherName() {
        return mOtherName;
    }

    public void setOtherName(String OtherName) {
        this.mOtherName = OtherName;
    }

    public String getDirector() {
        return mDirector;
    }

    public void setDirector(String Director) {
        this.mDirector = Director;
    }

    public String getWriter() {
        return mWriter;
    }

    public void setWriter(String Writer) {
        this.mWriter = Writer;
    }

    public String getGenre() {
        return mGenre;
    }

    public void setGenre(String Genre) {
        this.mGenre = Genre;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String Year) {
        this.mYear = Year;
    }

    public String getUserRating() {
        return mUserRating;
    }

    public void setUserRating(String UserRating) {
        this.mUserRating = UserRating;
    }

    public String getVotes() {
        return mVotes;
    }

    public void setVotes(String Votes) {
        this.mVotes = Votes;
    }

    public String getImdbUserRating() {
        return mImdbUserRating;
    }

    public void setImdbUserRating(String ImdbUserRating) {
        this.mImdbUserRating = ImdbUserRating;
    }

    public String getImdbVotes() {
        return mImdbVotes;
    }

    public void setImdbVotes(String ImdbVotes) {
        this.mImdbVotes = ImdbVotes;
    }

    public String getTmdbUserRating() {
        return mTmdbUserRating;
    }

    public void setTmdbUserRating(String TmdbUserRating) {
        this.mTmdbUserRating = TmdbUserRating;
    }

    public String getTmdbVotes() {
        return mTmdbVotes;
    }

    public void setTmdbVotes(String TmdbVotes) {
        this.mTmdbVotes = TmdbVotes;
    }

    public String getRunningTime() {
        return mRunningTime;
    }

    public void setRunningTime(String RunningTime) {
        this.mRunningTime = RunningTime;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String Country) {
        this.mCountry = Country;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String Language) {
        this.mLanguage = Language;
    }

    public String getEnglishPlot() {
        return mEnglishPlot;
    }

    public void setEnglishPlot(String EnglishPlot) {
        this.mEnglishPlot = EnglishPlot;
    }

    public String getOtherPlot() {
        return mOtherPlot;
    }

    public void setOtherPlot(String OtherPlot) {
        this.mOtherPlot = OtherPlot;
    }

    public String getBudget() {
        return mBudget;
    }

    public void setBudget(String Budget) {
        this.mBudget = Budget;
    }

    public String getProductionCompany() {
        return mProductionCompany;
    }

    public void setProductionCompany(String ProductionCompany) {
        this.mProductionCompany = ProductionCompany;
    }

    public String getImdbNumber() {
        return mImdbNumber;
    }

    public void setImdbNumber(String ImdbNumber) {
        this.mImdbNumber = ImdbNumber;
    }

    public String getTmdbNumber() {
        return mTmdbNumber;
    }

    public void setTmdbNumber(String TmdbNumber) {
        this.mTmdbNumber = TmdbNumber;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String ReleaseDate) {
        this.mReleaseDate = ReleaseDate;
    }

    public String getArchivesNumber() {
        return mArchivesNumber;
    }

    public void setArchivesNumber(String ArchivesNumber) {
        this.mArchivesNumber = ArchivesNumber;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    private void setSubtitle(String Subtitle) {
        this.mSubtitle = Subtitle;
    }

    public String getDubbing() {
        return mDubbing;
    }

    private void setDubbing(String Dubbing) {
        this.mDubbing = Dubbing;
    }

    public String getPersonalRating() {
        return mPersonalRating;
    }

    public void setPersonalRating(String PersonalRating) {
        this.mPersonalRating = PersonalRating;
    }

    public String getUserColumn1() {
        return mUserColumn1;
    }

    public void setUserColumn1(String UserColumn1) {
        this.mUserColumn1 = UserColumn1;
    }

    public String getUserColumn2() {
        return mUserColumn2;
    }

    public void setUserColumn2(String UserColumn2) {
        this.mUserColumn2 = UserColumn2;
    }

    public String getUserColumn3() {
        return mUserColumn3;
    }

    public void setUserColumn3(String UserColumn3) {
        this.mUserColumn3 = UserColumn3;
    }

    public String getUserColumn4() {
        return mUserColumn4;
    }

    public void setUserColumn4(String UserColumn4) {
        this.mUserColumn4 = UserColumn4;
    }

    public String getUserColumn5() {
        return mUserColumn5;
    }

    public void setUserColumn5(String UserColumn5) {
        this.mUserColumn5 = UserColumn5;
    }

    public String getUserColumn6() {
        return mUserColumn6;
    }

    public void setUserColumn6(String UserColumn6) {
        this.mUserColumn6 = UserColumn6;
    }

    public String getRlsType() {
        return mRlsType;
    }

    public void setRlsType(String RlsType) {
        this.mRlsType = RlsType;
    }

    public String getRlsGroup() {
        return mRlsGroup;
    }

    public void setRlsGroup(String RlsGroup) {
        this.mRlsGroup = RlsGroup;
    }

    public String getPoster() {
        return mPoster;
    }

    public void setPoster(String Poster) {
        this.mPoster = Poster;
    }

    public String getNote() {
        return mNote;
    }

    private void setNote(String Note) {
        this.mNote = Note;
    }

    public String getSeen() {
        return mSeen;
    }

    public void setSeen(String Seen) {
        this.mSeen = Seen;
    }

    public String getIsSyncWaiting() {
        return mIsSyncWaiting;
    }

    public void setIsSyncWaiting(String IsSyncWaiting) {
        this.mIsSyncWaiting = IsSyncWaiting;
    }

    public int getContentProvider() {
        return mContentProvider;
    }

    public void setContentProvider(int ContentProvider) {
        this.mContentProvider = ContentProvider;
    }

    public String getInsertDate() {
        return mInsertDate;
    }

    public void setInsertDate(String InsertDate) {
        this.mInsertDate = InsertDate;
    }

    public String getUpdateDate() {
        return mUpdateDate;
    }

    public void setUpdateDate(String UpdateDate) {
        this.mUpdateDate = UpdateDate;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.OriginalName, mOriginalName);
        values.put(COLUMNS.OtherName, mOtherName);
        values.put(COLUMNS.Director, mDirector);
        values.put(COLUMNS.Writer, mWriter);
        values.put(COLUMNS.Genre, mGenre);
        values.put(COLUMNS.Year, mYear);
        values.put(COLUMNS.UserRating, mUserRating);
        values.put(COLUMNS.Votes, mVotes);
        values.put(COLUMNS.ImdbUserRating, mImdbUserRating);
        values.put(COLUMNS.ImdbVotes, mImdbVotes);
        values.put(COLUMNS.TmdbUserRating, mTmdbUserRating);
        values.put(COLUMNS.TmdbVotes, mTmdbVotes);
        values.put(COLUMNS.RunningTime, mRunningTime);
        values.put(COLUMNS.Country, mCountry);
        values.put(COLUMNS.Language, mLanguage);
        values.put(COLUMNS.EnglishPlot, mEnglishPlot);
        values.put(COLUMNS.OtherPlot, mOtherPlot);
        values.put(COLUMNS.Budget, mBudget);
        values.put(COLUMNS.ProductionCompany, mProductionCompany);
        values.put(COLUMNS.ImdbNumber, mImdbNumber);
        values.put(COLUMNS.TmdbNumber, mTmdbNumber);
        values.put(COLUMNS.ReleaseDate, mReleaseDate);
        values.put(COLUMNS.ArchivesNumber, mArchivesNumber);
        values.put(COLUMNS.Subtitle, mSubtitle);
        values.put(COLUMNS.Dubbing, mDubbing);
        values.put(COLUMNS.PersonalRating, mPersonalRating);
        values.put(COLUMNS.UserColumn1, mUserColumn1);
        values.put(COLUMNS.UserColumn2, mUserColumn2);
        values.put(COLUMNS.UserColumn3, mUserColumn3);
        values.put(COLUMNS.UserColumn4, mUserColumn4);
        values.put(COLUMNS.UserColumn5, mUserColumn5);
        values.put(COLUMNS.UserColumn6, mUserColumn6);
        values.put(COLUMNS.RlsType, mRlsType);
        values.put(COLUMNS.RlsGroup, mRlsGroup);
        values.put(COLUMNS.Poster, mPoster);
        values.put(COLUMNS.Note, mNote);
        values.put(COLUMNS.InsertDate, mInsertDate);
        values.put(COLUMNS.UpdateDate, mUpdateDate);
        values.put(COLUMNS.Seen, mSeen);
        values.put(COLUMNS.IsSyncWaiting, mIsSyncWaiting);
        values.put(COLUMNS.ContentProvider, mContentProvider);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return "Movies";
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.OriginalName, COLUMNS.OtherName, COLUMNS.Director, COLUMNS.Writer, COLUMNS.Genre, COLUMNS.Year, COLUMNS.UserRating, COLUMNS.Votes, COLUMNS.ImdbUserRating, COLUMNS.ImdbVotes, COLUMNS.TmdbUserRating, COLUMNS.TmdbVotes, COLUMNS.RunningTime, COLUMNS.Country, COLUMNS.Language, COLUMNS.EnglishPlot, COLUMNS.OtherPlot, COLUMNS.Budget, COLUMNS.ProductionCompany, COLUMNS.ImdbNumber, COLUMNS.TmdbNumber, COLUMNS.ReleaseDate, COLUMNS.ArchivesNumber, COLUMNS.Subtitle, COLUMNS.Dubbing, COLUMNS.PersonalRating, COLUMNS.UserColumn1, COLUMNS.UserColumn2, COLUMNS.UserColumn3, COLUMNS.UserColumn4, COLUMNS.UserColumn5, COLUMNS.UserColumn6, COLUMNS.RlsType, COLUMNS.RlsGroup, COLUMNS.Poster, COLUMNS.Note, COLUMNS.Seen, COLUMNS.IsSyncWaiting, COLUMNS.ContentProvider, COLUMNS.InsertDate, COLUMNS.UpdateDate};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setOriginalName(cursor.getString(cursor.getColumnIndex(COLUMNS.OriginalName)));
                setOtherName(cursor.getString(cursor.getColumnIndex(COLUMNS.OtherName)));
                setDirector(cursor.getString(cursor.getColumnIndex(COLUMNS.Director)));
                setWriter(cursor.getString(cursor.getColumnIndex(COLUMNS.Writer)));
                setGenre(cursor.getString(cursor.getColumnIndex(COLUMNS.Genre)));
                setYear(cursor.getString(cursor.getColumnIndex(COLUMNS.Year)));
                setUserRating(cursor.getString(cursor.getColumnIndex(COLUMNS.UserRating)));
                setVotes(cursor.getString(cursor.getColumnIndex(COLUMNS.Votes)));
                setImdbUserRating(cursor.getString(cursor.getColumnIndex(COLUMNS.ImdbUserRating)));
                setImdbVotes(cursor.getString(cursor.getColumnIndex(COLUMNS.ImdbVotes)));
                setTmdbUserRating(cursor.getString(cursor.getColumnIndex(COLUMNS.TmdbUserRating)));
                setTmdbVotes(cursor.getString(cursor.getColumnIndex(COLUMNS.TmdbVotes)));
                setRunningTime(cursor.getString(cursor.getColumnIndex(COLUMNS.RunningTime)));
                setCountry(cursor.getString(cursor.getColumnIndex(COLUMNS.Country)));
                setLanguage(cursor.getString(cursor.getColumnIndex(COLUMNS.Language)));
                setEnglishPlot(cursor.getString(cursor.getColumnIndex(COLUMNS.EnglishPlot)));
                setOtherPlot(cursor.getString(cursor.getColumnIndex(COLUMNS.OtherPlot)));
                setBudget(cursor.getString(cursor.getColumnIndex(COLUMNS.Budget)));
                setProductionCompany(cursor.getString(cursor.getColumnIndex(COLUMNS.ProductionCompany)));
                setImdbNumber(cursor.getString(cursor.getColumnIndex(COLUMNS.ImdbNumber)));
                setTmdbNumber(cursor.getString(cursor.getColumnIndex(COLUMNS.TmdbNumber)));
                setReleaseDate(cursor.getString(cursor.getColumnIndex(COLUMNS.ReleaseDate)));
                setArchivesNumber(cursor.getString(cursor.getColumnIndex(COLUMNS.ArchivesNumber)));
                setSubtitle(cursor.getString(cursor.getColumnIndex(COLUMNS.Subtitle)));
                setDubbing(cursor.getString(cursor.getColumnIndex(COLUMNS.Dubbing)));
                setPersonalRating(cursor.getString(cursor.getColumnIndex(COLUMNS.PersonalRating)));
                setUserColumn1(cursor.getString(cursor.getColumnIndex(COLUMNS.UserColumn1)));
                setUserColumn2(cursor.getString(cursor.getColumnIndex(COLUMNS.UserColumn2)));
                setUserColumn3(cursor.getString(cursor.getColumnIndex(COLUMNS.UserColumn3)));
                setUserColumn4(cursor.getString(cursor.getColumnIndex(COLUMNS.UserColumn4)));
                setUserColumn5(cursor.getString(cursor.getColumnIndex(COLUMNS.UserColumn5)));
                setUserColumn6(cursor.getString(cursor.getColumnIndex(COLUMNS.UserColumn6)));
                setRlsType(cursor.getString(cursor.getColumnIndex(COLUMNS.RlsType)));
                setRlsGroup(cursor.getString(cursor.getColumnIndex(COLUMNS.RlsGroup)));
                setPoster(cursor.getString(cursor.getColumnIndex(COLUMNS.Poster)));
                setNote(cursor.getString(cursor.getColumnIndex(COLUMNS.Note)));
                setSeen(cursor.getString(cursor.getColumnIndex(COLUMNS.Seen)));
                setIsSyncWaiting(cursor.getString(cursor.getColumnIndex(COLUMNS.IsSyncWaiting)));
                setContentProvider(cursor.getInt(cursor.getColumnIndex(COLUMNS.ContentProvider)));
                setInsertDate(cursor.getString(cursor.getColumnIndex(COLUMNS.InsertDate)));
                setUpdateDate(cursor.getString(cursor.getColumnIndex(COLUMNS.UpdateDate)));

            }
        } catch (Exception e) {
            NLog.e("Movie", e);
        }

    }

    @Override
    public void LoadWithWhereColumn(Context ctx, String WhereColumn, String id) {
        Cursor cursor = null;
        try {
            DatabaseHelper dbHandler = DatabaseHelper.getInstance(ctx.getApplicationContext());
            cursor = dbHandler.GetCursor("Select * From " + TABLE_NAME + " Where " + WhereColumn + "=" + id);

            if (cursor.moveToFirst()) {
                LoadWithCursorRow(cursor);
            }

        } catch (Exception e) {
            NLog.e(TAG, e);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void LoadWithWhere(Context ctx, String Where) {
        Cursor cursor = null;
        try {
            DatabaseHelper dbHandler = DatabaseHelper.getInstance(ctx.getApplicationContext());
            cursor = dbHandler.GetCursor("Select * From " + TABLE_NAME + " Where " + Where);

            if (cursor.moveToFirst()) {
                LoadWithCursorRow(cursor);
            }

        } catch (Exception e) {
            NLog.e(TAG, e);

        } finally {
            if (cursor != null) {

                cursor.close();
            }
        }
    }

    public String getFirstCharacter() {
        return mFirstCharacter;
    }

    public void setFirstCharacter(String FirstCharacter) {
        this.mFirstCharacter = FirstCharacter;
    }

    public ArrayList<Cast> getCast() {
        return mCast;
    }

    public void setCast(ArrayList<Cast> Cast) {
        this.mCast = Cast;
    }

    public ArrayList<File> getFiles() {
        return mFiles;
    }

    public void setFiles(ArrayList<File> Files) {
        this.mFiles = Files;
    }

    public ArrayList<Backdrop> getBackdrops() {
        return mBackdrops;
    }

    public void setBackdrops(ArrayList<Backdrop> Backdrops) {
        this.mBackdrops = Backdrops;
    }

    public ArrayList<Trailer> getTrailers() {
        return mTrailers;
    }

    public void setTrailers(ArrayList<Trailer> Trailers) {
        this.mTrailers = Trailers;
    }

    public boolean getIsExisting() {
        return mIsExisting;
    }

    public void setIsExisting(boolean isExisting) {
        mIsExisting = isExisting;
    }

    public boolean getIsSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public static class COLUMNS {
        // Movies Table Columns names

        public static final String _ID = "_id";
        public static final String OriginalName = "OrginalName";
        public static final String OtherName = "OtherName";
        public static final String Director = "Director";
        public static final String Writer = "Writer";
        public static final String Genre = "Genre";
        public static final String Year = "Year";
        public static final String UserRating = "UserRating";
        public static final String Votes = "Votes";
        public static final String ImdbUserRating = "ImdbUserRating";
        public static final String ImdbVotes = "ImdbVotes";
        public static final String TmdbUserRating = "TmdbUserRating";
        public static final String TmdbVotes = "TmdbVotes";
        public static final String RunningTime = "RunningTime";
        public static final String Country = "Country";
        public static final String Language = "Language";
        public static final String EnglishPlot = "EnglishPlot";
        public static final String OtherPlot = "OtherPlot";
        public static final String Budget = "Budget";
        public static final String ProductionCompany = "ProductionCompany";
        public static final String ImdbNumber = "ImdbNumber";
        public static final String TmdbNumber = "TmdbNumber";
        public static final String ArchivesNumber = "ArchivesNumber";
        public static final String Subtitle = "Subtitle";
        public static final String Dubbing = "Dubbing";
        public static final String PersonalRating = "PersonalRating";
        public static final String UserColumn1 = "UserColumn1";
        public static final String UserColumn2 = "UserColumn2";
        public static final String UserColumn3 = "UserColumn3";
        public static final String UserColumn4 = "UserColumn4";
        public static final String UserColumn5 = "UserColumn5";
        public static final String UserColumn6 = "UserColumn6";
        public static final String RlsType = "RlsType";
        public static final String RlsGroup = "RlsGroup";
        public static final String Poster = "Poster";
        public static final String Note = "Note";
        public static final String Seen = "Seen";
        public static final String IsSyncWaiting = "IsSyncWaiting";
        public static final String ContentProvider = "ContentProvider";
        public static final String InsertDate = "InsertDate";
        public static final String UpdateDate = "UpdateDate";
        static final String ReleaseDate = "ReleaseDate";
    }
}

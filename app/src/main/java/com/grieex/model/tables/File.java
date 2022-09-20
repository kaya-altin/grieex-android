package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class File implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Files";
    private static final String TAG = File.class.getName();
    private int _id;
    private String mMovieID;
    private String mFileName;
    private String mResolution;
    private String mVideoCodec;
    private String mVideoBitrate;
    private String mFps;
    private String mAudioCodec1;
    private String mAudioChannels1;
    private String mAudioBitrate1;
    private String mAudioSampleRate1;
    private String mAudioSize1;
    private String mAudioCodec2;
    private String mAudioChannels2;
    private String mAudioBitrate2;
    private String mAudioSampleRate2;
    private String mAudioSize2;
    private String mTotalFrames;
    private String mLenght;
    private String mVideoSize;
    private String mFileSize;
    private String mChapter;
    private String mVideoAspectRatio;
    public File() {

    }

    public int getID() {
        return _id;
    }

    private void setID(int _id) {
        this._id = _id;
    }

    public String getMovieID() {
        return mMovieID;
    }

    private void setMovieID(String MovieID) {
        this.mMovieID = MovieID;
    }

    public String getFileName() {
        return mFileName;
    }

    private void setFileName(String FileName) {
        this.mFileName = FileName;
    }

    public String getResolution() {
        return mResolution;
    }

    private void setResolution(String Resolution) {
        this.mResolution = Resolution;
    }

    public String getVideoCodec() {
        return mVideoCodec;
    }

    private void setVideoCodec(String VideoCodec) {
        this.mVideoCodec = VideoCodec;
    }

    public String getVideoBitrate() {
        return mVideoBitrate;
    }

    private void setVideoBitrate(String VideoBitrate) {
        this.mVideoBitrate = VideoBitrate;
    }

    public String getFps() {
        return mFps;
    }

    private void setFps(String Fps) {
        this.mFps = Fps;
    }

    public String getAudioCodec1() {
        return mAudioCodec1;
    }

    private void setAudioCodec1(String AudioCodec1) {
        this.mAudioCodec1 = AudioCodec1;
    }

    public String getAudioChannels1() {
        return mAudioChannels1;
    }

    private void setAudioChannels1(String AudioChannels1) {
        this.mAudioChannels1 = AudioChannels1;
    }

    public String getAudioBitrate1() {
        return mAudioBitrate1;
    }

    private void setAudioBitrate1(String AudioBitrate1) {
        this.mAudioBitrate1 = AudioBitrate1;
    }

    public String getAudioSampleRate1() {
        return mAudioSampleRate1;
    }

    private void setAudioSampleRate1(String AudioSampleRate1) {
        this.mAudioSampleRate1 = AudioSampleRate1;
    }

    public String getAudioSize1() {
        return mAudioSize1;
    }

    private void setAudioSize1(String AudioSize1) {
        this.mAudioSize1 = AudioSize1;
    }

    public String getAudioCodec2() {
        return mAudioCodec2;
    }

    private void setAudioCodec2(String AudioCodec2) {
        this.mAudioCodec2 = AudioCodec2;
    }

    public String getAudioChannels2() {
        return mAudioChannels2;
    }

    private void setAudioChannels2(String AudioChannels2) {
        this.mAudioChannels2 = AudioChannels2;
    }

    public String getAudioBitrate2() {
        return mAudioBitrate2;
    }

    private void setAudioBitrate2(String AudioBitrate2) {
        this.mAudioBitrate2 = AudioBitrate2;
    }

    public String getAudioSampleRate2() {
        return mAudioSampleRate2;
    }

    private void setAudioSampleRate2(String AudioSampleRate2) {
        this.mAudioSampleRate2 = AudioSampleRate2;
    }

    public String getAudioSize2() {
        return mAudioSize2;
    }

    private void setAudioSize2(String AudioSize2) {
        this.mAudioSize2 = AudioSize2;
    }

    public String getTotalFrames() {
        return mTotalFrames;
    }

    private void setTotalFrames(String TotalFrames) {
        this.mTotalFrames = TotalFrames;
    }

    public String getLenght() {
        return mLenght;
    }

    private void setLenght(String Lenght) {
        this.mLenght = Lenght;
    }

    public String getVideoSize() {
        return mVideoSize;
    }

    private void setVideoSize(String VideoSize) {
        this.mVideoSize = VideoSize;
    }

    public String getFileSize() {
        return mFileSize;
    }

    private void setFileSize(String FileSize) {
        this.mFileSize = FileSize;
    }

    public String getChapter() {
        return mChapter;
    }

    private void setChapter(String Chapter) {
        this.mChapter = Chapter;
    }

    public String getVideoAspectRatio() {
        return mVideoAspectRatio;
    }

    private void setVideoAspectRatio(String VideoAspectRatio) {
        this.mVideoAspectRatio = VideoAspectRatio;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.MovieID, mMovieID);
        values.put(COLUMNS.FileName, mFileName);
        values.put(COLUMNS.Resolution, mResolution);
        values.put(COLUMNS.VideoCodec, mVideoCodec);
        values.put(COLUMNS.VideoBitrate, mVideoBitrate);
        values.put(COLUMNS.Fps, mFps);
        values.put(COLUMNS.AudioCodec1, mAudioCodec1);
        values.put(COLUMNS.AudioChannels1, mAudioChannels1);
        values.put(COLUMNS.AudioBitrate1, mAudioBitrate1);
        values.put(COLUMNS.AudioSampleRate1, mAudioSampleRate1);
        values.put(COLUMNS.AudioSize1, mAudioSize1);
        values.put(COLUMNS.AudioCodec2, mAudioCodec2);
        values.put(COLUMNS.AudioChannels2, mAudioChannels2);
        values.put(COLUMNS.AudioBitrate2, mAudioBitrate2);
        values.put(COLUMNS.AudioSampleRate2, mAudioSampleRate2);
        values.put(COLUMNS.AudioSize2, mAudioSize2);
        values.put(COLUMNS.TotalFrames, mTotalFrames);
        values.put(COLUMNS.Lenght, mLenght);
        values.put(COLUMNS.VideoSize, mVideoSize);
        values.put(COLUMNS.FileSize, mFileSize);
        values.put(COLUMNS.Chapter, mChapter);
        values.put(COLUMNS.VideoAspectRatio, mVideoAspectRatio);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.MovieID, COLUMNS.FileName, COLUMNS.Resolution, COLUMNS.VideoCodec, COLUMNS.VideoBitrate, COLUMNS.Fps, COLUMNS.AudioCodec1,
                COLUMNS.AudioChannels1, COLUMNS.AudioBitrate1, COLUMNS.AudioSampleRate1, COLUMNS.AudioSize1, COLUMNS.AudioCodec2, COLUMNS.AudioChannels2, COLUMNS.AudioBitrate2,
                COLUMNS.AudioSampleRate2, COLUMNS.AudioSize2, COLUMNS.TotalFrames, COLUMNS.Lenght, COLUMNS.VideoSize, COLUMNS.FileSize, COLUMNS.Chapter, COLUMNS.VideoAspectRatio};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setMovieID(cursor.getString(cursor.getColumnIndex(COLUMNS.MovieID)));
                setFileName(cursor.getString(cursor.getColumnIndex(COLUMNS.FileName)));
                setResolution(cursor.getString(cursor.getColumnIndex(COLUMNS.Resolution)));
                setVideoCodec(cursor.getString(cursor.getColumnIndex(COLUMNS.VideoCodec)));
                setVideoBitrate(cursor.getString(cursor.getColumnIndex(COLUMNS.VideoBitrate)));
                setFps(cursor.getString(cursor.getColumnIndex(COLUMNS.Fps)));
                setAudioCodec1(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioCodec1)));
                setAudioChannels1(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioChannels1)));
                setAudioBitrate1(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioBitrate1)));
                setAudioSampleRate1(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioSampleRate1)));
                setAudioSize1(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioSize1)));
                setAudioCodec2(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioCodec2)));
                setAudioChannels2(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioChannels2)));
                setAudioBitrate2(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioBitrate2)));
                setAudioSampleRate2(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioSampleRate2)));
                setAudioSize2(cursor.getString(cursor.getColumnIndex(COLUMNS.AudioSize2)));
                setTotalFrames(cursor.getString(cursor.getColumnIndex(COLUMNS.TotalFrames)));
                setLenght(cursor.getString(cursor.getColumnIndex(COLUMNS.Lenght)));
                setVideoSize(cursor.getString(cursor.getColumnIndex(COLUMNS.VideoSize)));
                setFileSize(cursor.getString(cursor.getColumnIndex(COLUMNS.FileSize)));
                setChapter(cursor.getString(cursor.getColumnIndex(COLUMNS.Chapter)));
                setVideoAspectRatio(cursor.getString(cursor.getColumnIndex(COLUMNS.VideoAspectRatio)));
            }
        } catch (Exception e) {
            NLog.e("Cast", e);
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

    public static class COLUMNS {
        public static final String MovieID = "MovieID";
        public static final String FileName = "FileName";
        public static final String Resolution = "Resolution";
        public static final String VideoCodec = "VideoCodec";
        public static final String VideoBitrate = "VideoBitrate";
        public static final String Fps = "Fps";
        public static final String AudioCodec1 = "AudioCodec1";
        public static final String AudioChannels1 = "AudioChannels1";
        public static final String AudioBitrate1 = "AudioBitrate1";
        public static final String AudioSampleRate1 = "AudioSampleRate1";
        public static final String AudioSize1 = "AudioSize1";
        public static final String AudioCodec2 = "AudioCodec2";
        public static final String AudioChannels2 = "AudioChannels2";
        public static final String AudioBitrate2 = "AudioBitrate2";
        public static final String AudioSampleRate2 = "AudioSampleRate2";
        public static final String AudioSize2 = "AudioSize2";
        public static final String TotalFrames = "TotalFrames";
        public static final String Lenght = "Lenght";
        public static final String VideoSize = "VideoSize";
        public static final String FileSize = "FileSize";
        public static final String Chapter = "Chapter";
        public static final String VideoAspectRatio = "VideoAspectRatio";
        static final String _ID = "_id";
    }
}

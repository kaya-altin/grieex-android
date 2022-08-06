package com.grieex.helper;

public class Constants {
    public static final int SYNC_FREQUENCY = 60 * 60 * 24; // 1 day (in seconds)
    // public static final int SYNC_FREQUENCY = 60 * 5; // 5 dakika
    public static final String CONTENT_AUTHORITY = "com.grieex.provider";
    public static final String ACCOUNT_NAME = "GrieeX Sync";
    public static final String ACCOUNT_TYPE = "com.grieex.account";

    // Defines a custom Intent action
    public static final String UNDEFINED_FILE_TYPE = "application/com.grieex.X";
    public static final String BROADCAST_ACTION = "com.grieex.BROADCAST";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = "com.grieex.STATUS";

    // Defines the key for the log "extra" in an Intent
    public static final String EXTENDED_STATUS_LOG = "com.grieex.LOG";

    public static final String EXTENDED_DATA_OBJECT = "com.grieex.OBJECT";
    public static final String EXTENDED_DATA_OBJECT2 = "com.grieex.OBJECT2";
    public static final String EXTENDED_DATA_OBJECT3 = "com.grieex.OBJECT3";

    public static final String STATE_IMPORT_DATA_SERVICE = "ImportDataServiceState";

    public static final String CHANNEL_ID = "default";

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String DATE_FORMAT1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String DATE_FORMAT2 = "dd.MM.yyyy HH:mm";
    public static final String DATE_FORMAT3 = "d MMM EEEE";
    public static final String DATE_FORMAT4 = "dd.MM.yyyy";
    public static final String DATE_FORMAT5 = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT6 = "dd MMMM yyyy EEEE";
    public static final String DATE_FORMAT7 = "dd MMMM yyyy";
    public static final String DATE_FORMAT8 = "yyyy.MM.dd";
    public static final String DATE_FORMAT9 = "HH:mm:ss";
    public static final String DATE_FORMAT10 = "hh:mm aa";
    public static final String DATE_FORMAT11 = "EEEE";
    public static final String DATE_FORMAT12 = "dd MMMM yyyy EEEE hh:mm aa";
    public static final String DATE_FORMAT13 = "dd.MM.yyyy.HH.mm";
    public static final String DATE_FORMAT14 = "dd MM yyyy HH:mm:ss";

    public static final String ListViewType = "ListViewType";


    public static final String ID = "id";
    public static final String Movie = "Movie";
    public static final String Series = "Series";
    public static final String MovieID = "MovieID";
    public static final String SeriesID = "SeriesID";
    public static final String SeasonNumber = "SeasonNumber";
    public static final String ImdbOrTraktID = "ImdbOrTraktID";
    public static final String TraktID = "TraktID";
    public static final String TvDbID = "TvDbID";
    public static final String ImageLink = "ImageLink";
    public static final String ActivityType = "ActivityType";
    public static final String IsExistDatabase = "isExistDatabase";


    public static final int STATE_NO_STATUS = -1;
    public static final int STATE_NOT_COMPLETED = 0;
    public static final int STATE_IMPORT_CONNECTING = 1;
    public static final int STATE_IMPORT_STARTED = 2;
    public static final int STATE_IMPORT_COMPLETED = 3;
    public static final int STATE_IMPORT_UPDATE_TEXT = 4;
    public static final int STATE_IMPORT_NOT_COMPLETED = 5;
    public static final int STATE_INSERT_MOVIE = 6;
    public static final int STATE_UPDATE_MOVIE = 7;
    public static final int STATE_DELETE_MOVIE = 8;
    public static final int STATE_REFRESH_SLIDE_MENU = 9;
    public static final int STATE_DROPBOX_SYNC_COMLETED = 10;
    public static final int STATE_REFRESH_SLIDE_MENU_COUNT = 11;
    public static final int STATE_INSERT_SERIES = 12;
    public static final int STATE_UPDATE_SERIES = 13;
    public static final int STATE_DELETE_SERIES = 14;
    public static final int STATE_SYNC_STARTED = 15;
    public static final int STATE_SYNC_ENDED = 16;
    public static final int STATE_TRAKT_LOGIN_STATE = 17;

    public static final int STATE_BATCH_PROCESSING_STARTED = 30;
    public static final int STATE_BATCH_PROCESSING_COMPLETED = 31;
    public static final int STATE_BATCH_PROCESSING_CANCELLED = 32;
    public static final int STATE_BATCH_PROCESSING_NOT_COMPLETED = 33;
    public static final int STATE_BATCH_PROCESSING_UPDATE_MOVIE = 34;

    public static final int REFRESH = 35;

    public static final int STATE_GRIEEX_UPDATE_STARTED = 101;
    public static final int STATE_GRIEEX_UPDATE_COMPLETED = 102;
    public static final int STATE_GRIEEX_UPDATE_NOT_COMPLETED = 103;

    public static final String BATCH_PROCESSING_MOVIE_INFOS = "BATCH_PROCESSING_MOVIE_INFOS";
    public static final String BATCH_PROCESSING_BACKDROPS = "BATCH_PROCESSING_BACKDROPS";
    public static final String BATCH_PROCESSING_POSTERS = "BATCH_PROCESSING_POSTERS";

    public static final String Pref_Locale_Key = "Locale";
    public static final String Pref_Locale_Default_Value = "en";
    public static final String Pref_ThemeId = "ThemeId";
    public static final String Pref_Dropbox_Access_Token = "dropbox-access-token";


    public static final String Pref_Dropbox_Last_Update_Date = "DropboxLastUpdateDate";
    public static final String Pref_Dropbox_Last_Download_Date = "DropboxLastDownloadDate";

    public static final String Pref_Last_Sync_Date = "LastSyncDate";
    public static final String Pref_Notification_Time = "NotificationTime";
    public static final String Pref_Last_Page = "LastPage";

    public static final String Pref_TraktAccessToken = "TraktAccessToken";
    public static final String Pref_TraktRefreshToken = "TraktRefreshToken";
    public static final String Pref_TraktExpiresIn = "TraktExpiresIn";
    public static final String Pref_TraktUserName = "TraktUserName";
    public static final String Pref_TraktAccessTokenExpiryDate= "TraktAccessTokenExpiryDate";

    public enum ContentProviders {
        Imdb(1), TMDb(2), Beyazperde(3), Sinemalar(4), TMDbTv(5), TvDb(6), TraktTv(7);

        public final int value;

        ContentProviders(int value) {
            this.value = value;
        }

        public static ContentProviders fromValue(int value) {
            for (ContentProviders style : ContentProviders.values()) {
                if (style.value == value) {
                    return style;
                }
            }
            return null;
        }
    }

    public enum CollectionType {
        Movie(1), Series(2);

        public final int value;

        CollectionType(int value) {
            this.value = value;
        }

        public static CollectionType fromValue(int value) {
            for (CollectionType style : CollectionType.values()) {
                if (style.value == value) {
                    return style;
                }
            }
            return null;
        }
    }

    public enum TransactionTypes {
        INSERT, UPDATE, DELETE, NONE
    }

    public enum Imdb250Type {
        Movie(1), Series(2);

        public final int value;

        Imdb250Type(int value) {
            this.value = value;
        }

        public static Imdb250Type fromValue(int value) {
            for (Imdb250Type style : Imdb250Type.values()) {
                if (style.value == value) {
                    return style;
                }
            }
            return null;
        }
    }


}

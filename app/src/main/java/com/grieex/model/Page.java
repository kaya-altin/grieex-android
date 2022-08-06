package com.grieex.model;

import java.io.Serializable;

public class Page implements Serializable {
    private String mPageID;
    private String mPageName;
    private String mPageTitle;
    private int mIcon;
    private PageTypes mPageType;
    private Object mObject;
    private int mCount;
    private boolean mSelectable = true;

//    public interface PageTypes {
//        int Separator = 0;
//        int MovieList = 1;
//        int PopularMovies = 2;
//        int NowPlayingMovies = 3;
//        int UpcomingMovies = 4;
//        int Series = 5;
//        int UpcomingSeries = 6;
//        int PopularSeries = 7;
//        int Search = 8;
//        int Imdb250 = 9;
//        int Imdb250Series = 10;
//        int Gallery = 11;
//        int CustomList = 12;
//        int GoProVersion = 50;
//        int Settings = 51;
//        int BatchProcessing = 52;
//        int About = 53;
//    }

    public enum PageTypes {
        Separator(0),
        MovieList(1),
        PopularMovies(2),
        NowPlayingMovies(3),
        UpcomingMovies(4),
        Series(5),
        UpcomingSeries(6),
        PopularSeries(7),
        Search(8),
        Imdb250(9),
        Imdb250Series(10),
        Gallery (11),
        CustomListMovies(12),
        CustomListSeries(13),
        GoProVersion(50),
        Settings(51),
        BatchProcessing(52),
        About(53);

        private final int value;
        PageTypes(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public Page() {
    }

    public Page(PageTypes PageType, String PageID, String PageName, String PageTitle, int Icon, boolean Selectable) {
        this.mPageID = PageID;
        this.mPageType = PageType;
        this.mPageName = PageName;
        this.mIcon = Icon;
        this.mSelectable = Selectable;
        this.mPageTitle = PageTitle;
    }

    public void setPageID(String PageID) {
        this.mPageID = PageID;
    }

    public String getPageID() {
        return mPageID;
    }

    public void setPageName(String PageName) {
        this.mPageName = PageName;
    }

    public String getPageName() {
        return mPageName;
    }

    public void setPageTitle(String PageTitle) {
        this.mPageTitle = PageTitle;
    }

    public String getPageTitle() {
        return mPageTitle;
    }

    public void setIcon(int Icon) {
        this.mIcon = Icon;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setPageType(PageTypes PageType) {
        this.mPageType = PageType;
    }

    public PageTypes getPageType() {
        return mPageType;
    }

    public void setSelectable(boolean Selectable) {
        this.mSelectable = Selectable;
    }

    public boolean getSelectable() {
        return mSelectable;
    }

    public void setObject(Object o) {
        this.mObject = o;
    }

    public Object getObject() {
        return mObject;
    }

    public void setCount(int Count) {
        this.mCount = Count;
    }

    public int getCount() {
        return mCount;
    }
}

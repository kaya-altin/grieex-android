package com.grieex.core;

public class SearchResult {
	private String mUrl = "";
	private String mKey = "";
	private String mTitle = "";
	private String mPoster = "";
	private String mYear = "";

	public SearchResult() {
	}

	public SearchResult(String key, String title) {
		this.mKey = key;
		this.mTitle = title;
		this.mPoster = "";
		this.mYear = "";
	}

	public SearchResult(String key, String title, String poster) {
		this.mKey = key;
		this.mTitle = title;
		this.mPoster = poster;
		this.mYear = "";
	}

	public SearchResult(String key, String title, String poster, String year) {
		this.mKey = key;
		this.mTitle = title;
		this.mPoster = poster;
		this.mYear = year;
	}

	public SearchResult(String url, String key, String title, String poster, String year) {
		this.mUrl = url;
		this.mKey = key;
		this.mTitle = title;
		this.mPoster = poster;
		this.mYear = year;
	}

	public void setUrl(String Url) {
		this.mUrl = Url;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setKey(String Key) {
		this.mKey = Key;
	}

	public String getKey() {
		return mKey;
	}

	public void setTitle(String Title) {
		this.mTitle = Title;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setPoster(String Poster) {
		this.mPoster = Poster;
	}

	public String getPoster() {
		return mPoster;
	}

	public void setYear(String Year) {
		this.mYear = Year;
	}

	public String getYear() {
		return mYear;
	}


	//Custom Fields
	private boolean mIsExisting=false;

	public void setIsExisting(boolean isExisting){
		mIsExisting = isExisting;
	}

	public boolean getIsExisting(){
		return mIsExisting;
	}
}

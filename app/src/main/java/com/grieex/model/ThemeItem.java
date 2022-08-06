package com.grieex.model;

public class ThemeItem {
	public ThemeItem() {

	}

	public ThemeItem(int id, int color, String ThemeName, int ThemeResourceId) {
		this.mId = id;
		this.mColor = color;
		this.mThemeName = ThemeName;
		this.mThemeResourceId= ThemeResourceId;
	}

	private int mId;
	private int mColor;
	private String mThemeName;
	private int mThemeResourceId;

	public void setId(int Id) {
		this.mId = Id;
	}

	public int getId() {
		return mId;
	}


	public void setColor(int Color) {
		this.mColor = Color;
	}

	public int getColor() {
		return mColor;
	}

	public void setThemeName(String ThemeName) {
		this.mThemeName = ThemeName;
	}

	public String getThemeName() {
		return mThemeName;
	}

	public void setThemeResourceId(int ThemeResourceId) {
		this.mThemeResourceId = ThemeResourceId;
	}

	public int getThemeResourceId() {
		return mThemeResourceId;
	}

}

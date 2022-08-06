package com.grieex.model;

public class CustomMenuItem {

	private int mId;
	private int mIcon;
	private String mText;
	private boolean mSelected = false;

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public int getIcon() {
		return mIcon;
	}

	public void setIcon(int Icon) {
		mIcon = Icon;
	}

	public String getText() {
		return mText;
	}

	public void setText(String Text) {
		mText = Text;
	}

	public boolean getSelected() {
		return mSelected;
	}

	public void setSelected(boolean Selected) {
		mSelected = Selected;
	}

	public CustomMenuItem(int id, int Icon, String Text) {
		this.mId = id;
		this.mIcon = Icon;
		this.mText = Text;
	}

	public CustomMenuItem(int id, int Icon, String Text, boolean Selected) {
		this.mId = id;
		this.mIcon = Icon;
		this.mText = Text;
		this.mSelected = Selected;
	}
}

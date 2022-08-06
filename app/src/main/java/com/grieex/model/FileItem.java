package com.grieex.model;

public class FileItem {
	public FileItem() {

	}

	public FileItem(String Character) {
		this.mText = Character;
	}

	public FileItem(String Character, boolean IsTitle) {
		this.mText = Character;
		this.mIsTitle = IsTitle;
	}

	private String mText;
	private boolean mIsTitle = false;

	public void setIsTitle(boolean IsTitle) {
		this.mIsTitle = IsTitle;
	}

	public boolean geIsTitle() {
		return mIsTitle;
	}

	public void setText(String Text) {
		this.mText = Text;
	}

	public String getText() {
		return mText;
	}

}

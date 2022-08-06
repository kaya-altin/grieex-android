package com.grieex.model;

public class Alphabet {
	public Alphabet() {

	}

	public Alphabet(int Index, String Character) {
		this.mIndex = Index;
		this.mCharacter = Character;
	}

	// Test
	private int mIndex;
	private String mCharacter;
	private boolean mIsActive = true;

	public void setIndex(int Index) {
		this.mIndex = Index;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setCharacter(String Character) {
		this.mCharacter = Character;
	}

	public String getCharacter() {
		return mCharacter;
	}

	public void setIsActive(boolean IsActive) {
		this.mIsActive = IsActive;
	}

	public boolean getIsActive() {
		return mIsActive;
	}
}

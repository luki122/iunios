package com.aurora.change.model;

public class NextDayPictureInfo {
	private String mPictureName;
	private String mPictureTime;
	private String mDimension;
	private String mUrlThumnail;
	private String mUrlOriginal;
	private String mTimeColor;
	private String mStatusBarColor;
	private String mCommentCity;
	private String mComment;
	private boolean isSaved;
	
	public NextDayPictureInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public void setPictureName(String name) {
		mPictureName = name;
	}
	
	public void setPictureTime(String time) {
		mPictureTime = time;
	}
	
	public void setPictureDimension(String dimension) {
		mDimension = dimension;
	}
	
	public void setPictureThumnailUrl(String url) {
		mUrlThumnail = url;
	}
	
	public void setPictureOriginalUrl(String url) {
		mUrlOriginal = url;
	}
	
	public void setPictureTimeColor(String color) {
		mTimeColor = color;
	}
	
	public void setPictureStatusColor(String color) {
		mTimeColor = color;
	}
	
	public void setPictureCommentCity(String city) {
		mCommentCity = city;
	}
	
	public void setPictureComment(String comment) {
		mComment = comment;
	}

	public void setIsSave(boolean b) {
		isSaved = b;
	}
	
	public String getPictureName() {
		return mPictureName;
	}
	
	public String getPictureTime() {
		return mPictureTime;
	}
	
	public String getPictureDimension() {
		return mDimension;
	}
	
	public String getPictureThumnailUrl() {
		return mUrlThumnail;
	}
	
	public String getPictureOriginalUrl() {
		return mUrlOriginal;
	}
	
	public String getPictureTimeColor() {
		return mTimeColor;
	}
	
	public String getPictureStatusColor() {
		return mTimeColor;
	}
	
	public String getPictureCommentCity() {
		return mCommentCity;
	}
	
	public String getPictureComment() {
		return mComment;
	}

	public boolean getIsSave() {
		return isSaved;
	}
	
}

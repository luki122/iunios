package com.aurora.datauiapi.data.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseUserInfo implements Parcelable{
	private String userIcon;
	private String userName;
	private String userID;
	private String userRemarks;
	private String friendCount;
	private String recoder;
	private String diary;
	private String backPublish;
	private String theme;
	private String share;
	private String groupName;
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public String getUserIcon() {
		return userIcon;
	}
	public void setUserIcon(String userIcon) {
		this.userIcon = userIcon;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUserRemarks() {
		return userRemarks;
	}
	public void setUserRemarks(String userRemarks) {
		this.userRemarks = userRemarks;
	}
	public String getFriendCount() {
		return friendCount;
	}
	public void setFriendCount(String friendCount) {
		this.friendCount = friendCount;
	}
	public String getRecoder() {
		return recoder;
	}
	public void setRecoder(String recoder) {
		this.recoder = recoder;
	}
	public String getDiary() {
		return diary;
	}
	public void setDiary(String diary) {
		this.diary = diary;
	}
	public String getBackPublish() {
		return backPublish;
	}
	public void setBackPublish(String backPublish) {
		this.backPublish = backPublish;
	}
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public String getShare() {
		return share;
	}
	public void setShare(String share) {
		this.share = share;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeString(userIcon);
		arg0.writeString(userName);
		arg0.writeString(userID);
		arg0.writeString(userRemarks);
		arg0.writeString(friendCount);
		arg0.writeString(recoder);
		arg0.writeString(diary);
		arg0.writeString(backPublish);
		arg0.writeString(theme);
		arg0.writeString(share);
		arg0.writeString(groupName);
	}
	
	public BaseUserInfo(UserInfoDetailInfo detailInfo){
		userIcon = detailInfo.getSpace().getAvatar();
		userName = detailInfo.getSpace().getUsername();
		userID = detailInfo.getSpace().getUid();
		userRemarks = detailInfo.getSpace().getBio();
		friendCount = detailInfo.getSpace().getFriends();
		recoder = detailInfo.getSpace().getDoings();
		diary = detailInfo.getSpace().getBlogs();
		backPublish = detailInfo.getSpace().getReplies();
		theme = detailInfo.getSpace().getThreads();
		share = detailInfo.getSpace().getSharings();
		groupName = detailInfo.getSpace().getGroup().getGrouptitle();
	}
	
	 public BaseUserInfo(){}
        public BaseUserInfo(Parcel parcel){
        	userIcon = parcel.readString();
        	userName = parcel.readString();
        	userID = parcel.readString();
        	userRemarks = parcel.readString();
        	friendCount = parcel.readString();
        	recoder = parcel.readString();
        	diary = parcel.readString();
        	backPublish = parcel.readString();
        	theme = parcel.readString();
        	share = parcel.readString();
        	groupName = parcel.readString();
    }
	
	public static final Parcelable.Creator<BaseUserInfo> CREATOR = new Parcelable.Creator<BaseUserInfo>() {

		@Override
		public BaseUserInfo createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return new BaseUserInfo(arg0);
		}

		@Override
		public BaseUserInfo[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new BaseUserInfo[arg0] ;
		}
	};
	
}

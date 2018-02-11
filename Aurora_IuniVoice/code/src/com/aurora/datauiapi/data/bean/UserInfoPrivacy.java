package com.aurora.datauiapi.data.bean;

public class UserInfoPrivacy {

	private UserInfoFeed feed;
	private UserInfoView view;
	private UserInfoProfile profile;
	public UserInfoFeed getFeed() {
		return feed;
	}
	public void setFeed(UserInfoFeed feed) {
		this.feed = feed;
	}
	public UserInfoView getView() {
		return view;
	}
	public void setView(UserInfoView view) {
		this.view = view;
	}
	public UserInfoProfile getProfile() {
		return profile;
	}
	public void setProfile(UserInfoProfile profile) {
		this.profile = profile;
	}
	
	
}

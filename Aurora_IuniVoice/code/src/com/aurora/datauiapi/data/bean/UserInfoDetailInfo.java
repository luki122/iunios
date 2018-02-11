package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class UserInfoDetailInfo{

	/**
	 * 
	 */
	private UserInfoSpace space;
	private ArrayList<UserInfoExtcredits> extcredits;
	public ArrayList<UserInfoExtcredits> getExtcredits() {
		return extcredits;
	}

	public void setExtcredits(ArrayList<UserInfoExtcredits> extcredits) {
		this.extcredits = extcredits;
	}

	public UserInfoSpace getSpace() {
		return space;
	}

	public void setSpace(UserInfoSpace space) {
		this.space = space;
	}
	
	
}

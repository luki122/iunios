package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class specials {

	private specialListtem special = new specialListtem();
	private List<appListtem> apps = new ArrayList<appListtem>();



	public specialListtem getSpecial() {
		return special;
	}

	public void setSpecial(specialListtem special) {
		this.special = special;
	}

	public List<appListtem> getApps() {
		return apps;
	}

	public void setApps(List<appListtem> apps) {
		this.apps = apps;
	}

}

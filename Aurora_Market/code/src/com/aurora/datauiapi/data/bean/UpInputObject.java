package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class UpInputObject {
private List<UpinputItem> instApps = new ArrayList<UpinputItem>();

	public List<UpinputItem> getInstApps() {
		return instApps;
	}

	public void setInstApps(List<UpinputItem> instApps) {
		this.instApps = instApps;
	}
}

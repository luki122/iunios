package com.aurora.worldtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WorldTimeUtils {
	
	static List<City> mWorldTimeSearchList = new ArrayList<City>();
    static List<City> mWorldTimeShowList = new ArrayList<City>();
    
	public static List<City> getmWorldTimeSearchList() {
		return mWorldTimeSearchList;
	}

	public static void setmWorldTimeSearchList(List<City> mWorldTimeSearchList) {
		WorldTimeUtils.mWorldTimeSearchList = mWorldTimeSearchList;
	}

	public static List<City> getmWorldTimeShowList() {
		return mWorldTimeShowList;
	}

	public static void setmWorldTimeShowList(List<City> mWorldTimeShowList) {
		WorldTimeUtils.mWorldTimeShowList = mWorldTimeShowList;
	}
	
	class SortByName implements Comparator {
		public int compare(Object o1, Object o2) {
			City s1 = (City) o1;
			City s2 = (City) o2;
			return s1.getName().compareTo(s2.getName());
		}
	}

}

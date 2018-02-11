package com.gionee.legalholiday;

public interface ILegalHoliday {
	public static final int DAY_TYPE_NORMAL = 0;
	public static final int DAY_TYPE_HOLIDAY = 0x3;
	public static final int DAY_TYPE_WORK_SHIFT = 0x1;
	
	public static final String LEGAL_HOLIDAY_PREF_NAME = "legal_holiday";
	public static final String LEGAL_HOLIDAY_YEAR_NUM = "year";
	public static final String LEGAL_HOLIDAY_YEAR_VERSION = "version";
	
	public int getDayType(int julianDay);
}

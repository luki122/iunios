/** 
 * Copyright (c) 2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.android.lunar;

interface ILunarService {
    String getTraditionalFestivalSimple();
    String getTraditionalFestival(int lunarYear, int lunarMonth, int lunarDay);
    String getFestivalSimple();
    String getFestival(int lunarMonth, int lunarDay);
    String getLunarSolarTerms(int year);
    String getAnimalsYearSimple();
    String getAnimalsYear(int lunarYear);
    String getChinaMonthStringSimple();
    String getChinaMonthString(int lunarMonth, boolean isLeapMonth);
    String getChinaDayStringSimple(boolean isDisplayLunarMonthForFirstDay);
    String getChinaDayString(int lunarMonth, int lunarDay, boolean isLeapMonth, boolean isDisplayLunarMonthForFirstDay);
    String getChinaYearStringSimple();
    String getChinaYearString(int lunarYear);
    String getLunarCalendarInfo();
    String getLunarStringForDayView(int year, int month, int monthDay);
    String getLunarDay(int year, int month, int monthDay);
    String getSeparationForType();
    String getIsSpecialFlag();
    String[] getLunarAndType(int year, int month);
}

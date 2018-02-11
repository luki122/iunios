package com.gionee.calendar.day;

import android.text.format.Time;

import com.gionee.almanac.GNAlmanacUtils.AlmanacInfo;

public interface MessageDispose {
	public void sendMessage(int msg,AlmanacInfo almanacInfo,Time time,int action,int type);

}

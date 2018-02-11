package com.aurora.voiceassistant.model;

import org.json.JSONObject;

import com.google.gson.JsonObject;

public class RspJson {
	private JSONObject jsonObject;
	
	public RspJson(JSONObject object) {
		// TODO Auto-generated constructor stub
		jsonObject = object;
	}
	
	public JSONObject getJSONObjectData() {
		return jsonObject;
	}
	
	/*public class Item {
		private RspJsonReminderAlarm rspJsonReminderAlarm;
	}*/
}

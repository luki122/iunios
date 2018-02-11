package com.aurora.weatherdata.interf;

import com.aurora.weatherdata.implement.Command;
import com.aurora.weatherdata.implement.DataResponse;

public interface INotifiableController {

	public final static int CODE_SUCCESS = 1000;
	public final static int CODE_UNKNONW_HOST = 1001;
	public final static int CODE_WRONG_DATA_FORMAT = 1002;
	public final static int CODE_REQUEST_TIME_OUT = 1003;
	public final static int CODE_CONNECT_ERROR = 1004;
	public final static int CODE_GENNERAL_IO_ERROR = 1005;
	public final static int CODE_NOT_FOUND_ERROR = 1006;
	public final static int CODE_JSON_PARSER_ERROR = 1007;
	public final static int CODE_JSON_MAPPING_ERROR = 1008; 
	public final static int CODE_UNCAUGHT_ERROR = 1009;
	public final static int CODE_NOT_NETWORK = 1010;

	public void onWrongConnectionState(int state, INotifiableManager manager, Command<?> source);

	public void onError(int code, String message, INotifiableManager manager);

	public void onMessage(String message);

	public void runOnUI(DataResponse<?> response);

}
package com.aurora.datauiapi.data;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.aurora.account.util.Log;
import com.aurora.account.util.WifiHelper;
import com.aurora.account.util.WifiHelper.NoNetworkException;
import com.aurora.account.util.WifiHelper.WifiStateException;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.exception.ServerException;
import com.aurora.datauiapi.data.exception.SessionExpiredException;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseManager implements INotifiableManager {

	/**
	 * @uml.property name="tAG"
	 */
	private final String TAG = "VideoManager";

	protected List<Runnable> failedIORequests = new ArrayList<Runnable>();
	/**
	 * @uml.property name="mHandler"
	 * @uml.associationEnd
	 */
	protected Handler mHandler;
	/**
	 * @uml.property name="mController"
	 * @uml.associationEnd
	 */
	protected INotifiableController mController = null;

	private static HandlerThread sHandlerThread;

	protected BaseManager(INotifiableController controller) {
	    if (sHandlerThread == null) {
	        sHandlerThread = new HandlerThread("account-manager-thread");
	        sHandlerThread.start();
	    }
	    mHandler = new Handler(sHandlerThread.getLooper());
	    mController = controller;
	}
	
	public void destroy() {
	    mController = null;
	    
//	    if (sHandlerThread != null) {
//	        sHandlerThread.quit();
//	        sHandlerThread = null;
//	    }
	}
	
	public <T extends BaseResponseObject> void setResponse(DataResponse<T> response, String result,
			Class<?> cl) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
	//	result = result.replaceAll("\t", "");
		try {
			mapper.configure(
					DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
					true);
			mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			mapper.getDeserializationConfig()
					.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);
	
	/*        StringWriter str=new StringWriter();
	        map.put("catid", 123);
	        //{"count":123,"page":123,"catid":123}
	        map.put("count", 123);
	        map.put("page", 123);
	        mapper.writeValue(str,map);*/
			response.value = (T) mapper.readValue(result, cl);
		} catch (JsonParseException e) {
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

//	public void setController(INotifiableController controller) {
//		mController = controller;
//	}

//	public void setHandler(Handler handler) {
//		mHandler = handler;
//	}

	public void reset() {

		failedIORequests.clear();

	}


	@Override
	public void onFinish(DataResponse<?> response) {
		// TODO Auto-generated method stub
		if (mController != null) {
			mController.runOnUI(response);
		}
	}

	@Override
	public void onWrongConnectionState(int state, Command<?> cmd) {
		// TODO Auto-generated method stub
		// failedRequests.add(cmd);
		if (mController != null)
			mController.onWrongConnectionState(state, this, cmd);

	}

	@Override
	public void onError(Exception exception, Command<?> cmd) {
		// TODO Auto-generated method stub
		int errCode;
		if (mController != null) {
			
			try {
				throw exception;
			} catch (JsonParseException e) {
				errCode = INotifiableController.CODE_JSON_PARSER_ERROR;
				// mController.onError(INotifiableController.CODE_JSON_PARSER_ERROR,
				// e.toString());
			} 
			catch(JsonMappingException e)
			{
				errCode = INotifiableController.CODE_JSON_PARSER_ERROR;
				// mController.onError(INotifiableController.CODE_JSON_PARSER_ERROR,
				// e.toString());
			}
			catch (IOException e) {
				if (e instanceof ConnectTimeoutException
						|| e instanceof SocketTimeoutException) {
					failedIORequests.add(cmd);
					errCode = INotifiableController.CODE_REQUEST_TIME_OUT;
					// mController.onError(INotifiableController.CODE_REQUEST_TIME_OUT,
					// e.toString());
				} else if (e instanceof UnknownHostException) {
					errCode = INotifiableController.CODE_UNKNONW_HOST;
				} else {
					errCode = INotifiableController.CODE_GENNERAL_IO_ERROR;
					// mController.onError(INotifiableController.CODE_GENNERAL_IO_ERROR,
					// e.toString());
				}
			} catch (IllegalArgumentException e) {
				errCode = INotifiableController.CODE_WRONG_DATA_FORMAT;
				// mController.onError(INotifiableController.CODE_WRONG_DATA_FORMAT,
				// e.toString());
			} catch (NoNetworkException e) {
				errCode = INotifiableController.CODE_NOT_NETWORK;
				// mController.onError(INotifiableController.CODE_WRONG_DATA_FORMAT,
				// e.toString());
			} catch (ServerException e) {
			    errCode = INotifiableController.CODE_SERVER_ERROR;
			} catch (SessionExpiredException e) {
			    errCode = INotifiableController.CODE_SESSION_EXPIRED_ERROR;
			} catch (Exception e) {
				errCode = INotifiableController.CODE_UNCAUGHT_ERROR;
				// mController.onError(INotifiableController.CODE_UNCAUGHT_ERROR,
				// e.toString());
			} /*
			 * finally { exception.printStackTrace(); }
			 */

			mController.onError(errCode, exception.toString(), this, exception);

		}
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		if (mController != null) {
			mController.onMessage(message);
		}
	}

	@Override
	public void onMessage(int code, String message) {
		// TODO Auto-generated method stub
		if (mController != null) {
			mController.onMessage(message);
		}
	}

	@Override
	public void retry() {
		// TODO Auto-generated method stub

		mHandler.post(new Runnable() {
			public void run() {
				while (failedIORequests.size() > 0) {
					if (mHandler.post(failedIORequests.get(0)))
						Log.d(TAG, "IO Runnable posted");
					else
						Log.d(TAG, "IO Runnable coudln't be posted");
					failedIORequests.remove(0);
				}
			}
		});

	}

	public static void assertWifiState(Context context)
			throws WifiStateException {
		if (context != null) {
			final int state = WifiHelper.getInstance(context).getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
				throw new WifiStateException(state);
			}
		}
	}
}

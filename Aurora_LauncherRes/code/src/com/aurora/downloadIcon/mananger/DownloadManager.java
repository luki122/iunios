package com.aurora.downloadIcon.mananger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.conn.ConnectTimeoutException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import com.aurora.downloadIcon.utils.Log;

import com.aurora.downloadIcon.bean.IconRequestProp;
import com.aurora.downloadIcon.bean.IconResponseObject;
import com.aurora.downloadIcon.bean.IconResponseProp;
import com.aurora.downloadIcon.http.HttpRequestDataProxy;
import com.aurora.downloadIcon.struct.Command;
import com.aurora.downloadIcon.struct.DataResponse;
import com.aurora.downloadIcon.struct.IDownloadIconManager;
import com.aurora.downloadIcon.struct.INotifiable;
import com.aurora.downloadIcon.struct.INotifiableController;
import com.aurora.downloadIcon.utils.Globals;
import com.aurora.downloadIcon.utils.ResolutionUtils;
import com.aurora.downloadIcon.utils.SystemUtils;
import com.aurora.downloadIcon.utils.Utils2IconLocal;

public class DownloadManager implements IDownloadIconManager, INotifiable {

	public static final String TAG = "DownloadManager";

	protected List<Runnable> failedReq = new ArrayList<Runnable>();

	protected INotifiableController mController;

	protected String mCoverId;

	protected int mModuleId;

	protected Handler mHandler;

	public static final int GET_ICON_INFO = 0;
	public static final int GET_ICON = 1;
	private List<IconResponseProp> temp2Server = new ArrayList<IconResponseProp>();

	public DownloadManager() {

	}

	public void setController(INotifiableController controller) {
		mController = controller;
	}

	public void setThreadHandle(Handler handler) {
		mHandler = handler;
	}

	public void reset() {
		if (failedReq != null)
			failedReq.clear();
	}

	private <T> void setResponse(DataResponse<T> response, String result,
			Class<?> cls) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			mapper.configure(
					DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
					true);
			mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			mapper.getDeserializationConfig()
					.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);
			response.value = (T) mapper.readValue(result, cls);
			response.tempValue = (T) mapper.readValue(result, cls);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO:xiejun
	public static ArrayList<IconRequestProp> getAllApplicationInfos(
			Context context) {
		ArrayList<IconRequestProp> requestProps = new ArrayList<IconRequestProp>();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		int iconDpi = activityManager.getLauncherLargeIconDensity();
		String device_resolution = ResolutionUtils.getResolution(iconDpi);
		//Setting resolution here is not wise.
		ResolutionUtils.setDEVICE_RESOLUTION(device_resolution);
		PackageManager pManager = context.getPackageManager();
		// first get Launch Actvities
		Intent activityIntent = new Intent(Intent.ACTION_MAIN, null);
		activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> activityInfos = pManager.queryIntentActivities(
				activityIntent, 0);
		for (ResolveInfo info : activityInfos) {
			ActivityInfo acInfo = info.activityInfo;
			if (acInfo != null) {
				IconRequestProp requestProp = new IconRequestProp();
				requestProp.setPackageName(acInfo.packageName);
				requestProp.setClassName(acInfo.name);
				String lable = "";
				CharSequence lableCharSequence = acInfo.loadLabel(pManager);
				//Log.i("test1",acInfo.packageName + "  ,  "+acInfo.packageName+"  ,  "+acInfo.name+"  ,   "+lableCharSequence);
				lable = lableCharSequence == null ? null : lableCharSequence
						.toString();
				requestProp.setLabel(lable);
				requestProp.setResolution(device_resolution);
				requestProps.add(requestProp);
			}
		}
		// second get all app pkg
		List<PackageInfo> packageInfos = pManager.getInstalledPackages(0);
		for (PackageInfo info : packageInfos) {
			IconRequestProp requestProp = new IconRequestProp();
			requestProp.setPackageName(info.packageName);
			requestProp.setClassName("");
			ApplicationInfo applicationInfo = info.applicationInfo;
			CharSequence appCharSequence = null;
			if (applicationInfo != null) {
				appCharSequence = pManager.getApplicationLabel(applicationInfo);
			}
			requestProp.setLabel(appCharSequence == null ? info.packageName
					: appCharSequence.toString());
			requestProp.setResolution(device_resolution);
			requestProps.add(requestProp);
		}
		Log.i("test","requestProps activityInfos  count = "+activityInfos.size()+"   package info count = "+packageInfos.size());
		return requestProps;
	}

	// TODO:xiejun
	public static void filterResponse2Download(IconResponseObject serverObject,
			IconResponseObject localObject) {
		//Log.i("test",
		//		" **********************filterResponse2Download  begin********************************");
		if ((serverObject != null && serverObject.getIconItems() != null)
				&& (localObject != null && localObject.getIconItems() != null)) {
			List<IconResponseProp> serverItems = serverObject.getIconItems();
			List<IconResponseProp> localItems = localObject.getIconItems();
			ArrayList<IconResponseProp> removeItems = new ArrayList<IconResponseProp>();
			for (IconResponseProp serverItem : serverItems) {
				for (IconResponseProp localItem : localItems) {
					if (serverItem.equals(localItem)) {
						//Log.i("test", "serverItem = " + serverItem.toString());
						//Log.i("test", "localItem = " + localItem.toString());
						removeItems.add(serverItem);
					}
				}
			}
			Log.i("test", "removeItems size  =  " + removeItems.size());
			if (removeItems.size() > 0) {
				serverItems.removeAll(removeItems);
			}
			for (IconResponseProp item : serverItems) {
				Log.i(TAG, item.toString());
			}
		}
		Log.i("test"," *********filterResponse2Download  end ***************");

	}

	@Override
	public void getIconResponseProp(
			final DataResponse<IconResponseObject> response,
			final Context context) {
		mHandler.post(new Command<IconResponseObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				// 1.try to get Http result
				ArrayList<IconRequestProp> requestProps = getAllApplicationInfos(context);
				String jsonString = list2Jason(requestProps);
				//Log.i("test","**************getData begein*********************");
				String result = HttpRequestDataProxy.doPost(Globals.HTTP_REQUEST_URL, jsonString, context);
				Log.i("test", "**************getData end*********************");
				//Log.i("test", result);
				String resultLocal = Utils2IconLocal.readIconServerDara();
				/*
				 * SystemUtils.getFromAssets(context, "icon_infos_Local.json");
				 */
				IconResponseObject localOject = null;
				if (resultLocal != null) {
					localOject = SystemUtils.readJson2Array(resultLocal);
				}
				//Log.i("test", "localOject = " + localOject + "  resultLocal = " + resultLocal);
				if (localOject != null) {
					Log.i("test", "localOject = "
							+ localOject.getIconItems().size());
				}
				// 2.setResponse to
				response.responeType = GET_ICON_INFO;
				setResponse(response, result, IconResponseObject.class);
				// 3.filter the reponse data by the Local database
				filterResponse2Download(response.value, localOject);
			}
		});
	}

	@Override
	public void getIcon(final DataResponse<IconResponseObject> response,
			final IconResponseObject iconResponseObject,
			final IconResponseObject tempIconResponseObject,
			final Context context) {
		mHandler.post(new Command<IconResponseObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				List<IconResponseProp> icons = iconResponseObject
						.getIconItems();
				List<IconResponseProp> temp = new ArrayList<IconResponseProp>();
				List<IconResponseProp> temp1 = new ArrayList<IconResponseProp>();
				response.responeType = GET_ICON;
				for (IconResponseProp prop : icons) {
					Log.i("test", "prop = " + prop.toString());
					Bitmap b = HttpRequestDataProxy.doGetBitmap(prop, context);
					if (b == null) {
						for (IconResponseProp prop2 : (tempIconResponseObject
								.getIconItems())) {
							if (prop2.equals(prop)) {
								temp.add(prop2);
							}
						}
					}
				}
				if (iconResponseObject.getIconItems() != null) {
					iconResponseObject.getIconItems().removeAll(temp1);
					response.value = iconResponseObject;
				}
				if (tempIconResponseObject.getIconItems() != null) {
					tempIconResponseObject.getIconItems().removeAll(temp);
				}
				writeDownloadIconProp2file(tempIconResponseObject);
				temp.clear();
			}
		});
	}

	@Override
	public void postActivity() {

	}

	@Override
	public void onFinish(DataResponse<?> response) {
		/*
		 * if(response.responeType == GET_ICON_INFO){ IconResponseObject
		 * iconResponseObject = (IconResponseObject)(response.value);
		 * if(iconResponseObject!=null){ List<IconResponseProp> icons =
		 * iconResponseObject.getIconItems(); if(icons.size()>0){ int i = 0;
		 * for(IconResponseProp icon: icons){
		 * //Log.i("test"," icon "+(++i)+"  :  "+icon.toString());
		 * 
		 * } } } }else{
		 */
		if (mController != null) {
			mController.runOnUI(response);
		}
		/* } */

	}

	@Override
	public void onWrongConnectionState(int state, Command<?> cmd) {
		if (mController != null) {
			mController.onWrongConnectionState(state, this, cmd);
		}

	}

	@Override
	public void onError(Exception exception, Command<?> cmd, String coverId,
			int moduleId) {
		int errCode;
		if (mController != null) {
			mModuleId = moduleId;
			mCoverId = coverId;
			try {
				throw exception;
			} catch (JSONException e) {
				errCode = INotifiableController.CODE_JSON_PARSER_ERROR;
				// mController.onError(INotifiableController.CODE_JSON_PARSER_ERROR,
				// e.toString());
			} catch (IOException e) {
				if (e instanceof ConnectTimeoutException
						|| e instanceof SocketTimeoutException) {
					failedReq.add(cmd);
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
			} catch (Exception e) {
				errCode = INotifiableController.CODE_UNCAUGHT_ERROR;
				// mController.onError(INotifiableController.CODE_UNCAUGHT_ERROR,
				// e.toString());
			} finally {
				if (exception != null) {
					exception.printStackTrace();
				}
			}
			mController.onError(errCode, exception.toString(), this);

		}

	}

	@Override
	public void onMessage(String message) {
		if (mController != null) {
			mController.onMessage(message);
		}
	}

	@Override
	public void onMessage(int code, String message) {
		if (mController != null) {
			mController.onMessage(message);
		}
	}

	@Override
	public void retry() {
		while (failedReq.size() > 0) {
			Runnable r = failedReq.get(0);
			if (mHandler.post(r)) {
				Log.i(TAG, "A request has been retry successed!");
			} else {
				Log.i(TAG, "request has been retry failed!");
				failedReq.remove(0);
			}

		}

	}

	private String list2Jason(List list) {
		ObjectMapper mapper = new ObjectMapper();
		String ret = "";
		/*
		 * for (Object object : list){ Log.i("test",""+object.toString()); }
		 * Log.i("test","list size = "+list.size());
		 */
		try {
			ret = mapper.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			Log.i("test", "JsonProcessingException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.i("test", "IOException");
			e.printStackTrace();
		}
		/* Log.i("test","ret = "+ret); */
		return ret;
	}

	private void writeDownloadIconProp2file(
			IconResponseObject iconResponseObject) {
		Log.i("test", "writeDownloadIconProp2file");
		if (iconResponseObject != null) {
			if (iconResponseObject.getIconItems() != null
					&& iconResponseObject.getIconItems().size() > 0) {
				String s = iconResponseObject.toJson();
				if (s != null) {
					Utils2IconLocal.writeIconServerData(s);
				}
			}

		}
	}

}

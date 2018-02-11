package com.aurora.account.contentprovider;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.aurora.account.AccountApp;
import com.aurora.account.activity.PhotoLoader;
import com.aurora.account.http.data.HttpRequestGetAccountData;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.CookieResultObject;

/**
 * 账户中心Provider
 * 
 * @author JimXia
 *
 * @date 2014年11月20日 上午10:52:25
 */
public class AccountProvider extends ContentProvider implements
		AccountInfoConstants {
	private static final String TAG = "AccountProvider";

	private static final boolean DBG = true;

	private static final int URL_ACCOUNT_INFO = 1;
	private static final int URL_ACCOUNT_COOKIE = 2;
	private static final UriMatcher sUrlMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sUrlMatcher.addURI(AUTHORITY, ACCOUNT_INFO, URL_ACCOUNT_INFO);
		sUrlMatcher.addURI(AUTHORITY, ACCOUNT_COOKIE, URL_ACCOUNT_COOKIE);
	}

	private void debug(String msg) {
		if (DBG) {
			Log.d(TAG, msg);
		}
	}

	@Override
	public boolean onCreate() {
		debug("Jim, onCreate");
		return true;
	}

	@Override
	public Cursor query(Uri url, String[] projectionIn, String selection,
			String[] selectionArgs, String sort) {
		int match = sUrlMatcher.match(url);
		switch (match) {
		case URL_ACCOUNT_INFO: {
			return getAccountInfo();
		}
		case URL_ACCOUNT_COOKIE: {
			return getCookie();
		}
		default: {
			return null;
		}
		}
	}

	/**
	 * 如果query接口返回的数据有改变，调用此接口来通知观察者
	 */
	public static void notifyQueryDataChanged() {
		/* 同步到网络会调用android的同步服务SyncManager来启动同步 */
		AccountApp.getInstance().getContentResolver()
				.notifyChange(QUERY_URI, null, false/* 不用同步到网络 */);
	}

	private Cursor getAccountInfo() {
		MatrixCursor mc = new MatrixCursor(ACCOUNT_INFO_COLUMNS, 1);
		final boolean hasLogin = BooleanPreferencesUtil.getInstance(
				getContext()).hasLogin();
		AccountPreferencesUtil pref = AccountPreferencesUtil
				.getInstance(getContext());
		mc.newRow()
				.add(hasLogin ? pref.getUserNick() : "")
				// .add(AccountPreferencesUtil.getUserPhotoURL())
				.add(hasLogin ? PhotoLoader.getFinalIconFile()
						.getAbsolutePath() : "")
				.add(hasLogin ? pref.getLastSyncFinishedTime() : 0)
				.add(hasLogin ? 1 : 0).add(hasLogin ? pref.getUserToken() : "")
				.add(hasLogin ? pref.getUserCookie() : "")
				.add(hasLogin ? pref.getUserID() : "");

		return mc;
	}

	private Cursor getCookie() {
		MatrixCursor mc = new MatrixCursor(ACCOUNT_COOKIE_COLUMNS, 1);
		final boolean hasLogin = BooleanPreferencesUtil.getInstance(
				getContext()).hasLogin();
		AccountPreferencesUtil pref = AccountPreferencesUtil
				.getInstance(getContext());
		
		if (hasLogin) {
			try {
				String result = HttpRequestGetAccountData.getUserCookie(
						pref.getUserID(), pref.getUserKey());

				ObjectMapper mapper = new ObjectMapper();

				try {
					mapper.configure(
							DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
							true);
					mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
					mapper.getDeserializationConfig()
							.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
									false);

					CookieResultObject obj = mapper.readValue(result,
							CookieResultObject.class);

					if (obj.getCode() == BaseResponseObject.CODE_SUCCESS) {
						mc.newRow().add(hasLogin ? obj.getTgt() : "");
					}

				} catch (JsonParseException e) {
					e.printStackTrace();

				} catch (JsonMappingException e) {
					e.printStackTrace();

				} catch (IOException e) {
					e.printStackTrace();

				}

			} catch (Exception e) {
				e.printStackTrace();
				return mc;
			}

		}

		return mc;
	}

	@Override
	public String getType(Uri url) {
		switch (sUrlMatcher.match(url)) {
		case URL_ACCOUNT_INFO:
			return ACCOUNT_INFO_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		throw new UnsupportedOperationException();
	}

	// private void checkPermission() {
	// getContext().enforceCallingOrSelfPermission("android.permission.WRITE_APN_SETTINGS",
	// "No permission to write APN settings");
	// }
}
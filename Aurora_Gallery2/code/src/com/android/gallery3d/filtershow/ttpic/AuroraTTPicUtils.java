package com.android.gallery3d.filtershow.ttpic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.gallery3d.filtershow.FilterShowActivity;
import com.tencent.ttpic.sdk.util.IntentUtils;
import com.tencent.ttpic.sdk.util.Pitu.Modules;
import com.tencent.ttpic.sdk.util.Pitu;

public class AuroraTTPicUtils {
	
	public static final int REQ_TO_PITU = 1001;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
	
	private static ArrayList<Uri> toUriList(Uri uri) {
		Log.i("SQF_LOG", " toUriList: uri:" + uri);
		ArrayList<Uri> uris = new ArrayList<Uri>();
		uris.add(uri);
		return uris;
	}
	
	public static Uri generateOutputPath() {
		String FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath();
		String time = sdf.format(new Date());
		return Uri.parse(FOLDER + "/DCIM/Camera/" + time + ".jpg");
	}
	
	public static void startBeautifyPhoto(FilterShowActivity activity, Uri uri) {
		Intent intent = IntentUtils.buildPituIntent(toUriList(uri), Pitu.Modules.MAIN_MODULE_EDITOR, generateOutputPath());
		activity.startActivityForResult(intent, REQ_TO_PITU);
		//activity.startActivity(intent);
		//activity.finish();
	}
	
	public static void startBeautifyPortrait(FilterShowActivity activity, Uri uri) {
		Intent intent = IntentUtils.buildPituIntent(toUriList(uri), Pitu.Modules.MAIN_MODULE_BEAUTY, generateOutputPath());
		activity.startActivityForResult(intent, REQ_TO_PITU);
		//activity.startActivity(intent);
		//activity.finish();
	}
	
	public static void startNaturalMakeUp(FilterShowActivity activity, Uri uri) {
		Intent intent = IntentUtils.buildPituIntent(toUriList(uri), Pitu.Modules.MAIN_MODULE_COSMESTIC, generateOutputPath());
		activity.startActivityForResult(intent, REQ_TO_PITU);
		//activity.startActivity(intent);
		//activity.finish();
	}
	
	public static void startJigsaw(FilterShowActivity activity, Uri uri) {
		Intent intent = IntentUtils.buildPituIntent(null, Pitu.Modules.MAIN_MODULE_COLLAGE, generateOutputPath());
		activity.startActivityForResult(intent, REQ_TO_PITU);
		//activity.startActivity(intent);
		//activity.finish();
	}
}

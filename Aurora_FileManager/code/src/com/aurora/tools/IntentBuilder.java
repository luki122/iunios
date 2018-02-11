package com.aurora.tools;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

public class IntentBuilder {
	private static final String TAG = "IntentBuilder";

	public static void viewFile(final Context context, final String filePath) {
		viewFile(context, filePath, null);
	}

	public static void viewFile(final Context context, final String filePath,
			String mimeType) {
		if (TextUtils.isEmpty(filePath)) {
			return;
		}
		LogUtil.d(TAG, "viewFile::" + filePath);

		String type;
		if (!TextUtils.isEmpty(mimeType)) {
			type = mimeType;
		} else {
			type = MimeTypeUtil.getMimeType(filePath);
		}
		if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
			if (type.startsWith("video")) {
				type = "video/*";
			}
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
			try {
				context.startActivity(intent);
				/*if(context instanceof FileExplorerTabActivity){
					((FileExplorerTabActivity)context).overridePendingTransition(
							com.aurora.R.anim.aurora_activity_open_enter,
							com.aurora.R.anim.aurora_activity_up_exit);
				}*/
			} catch (Exception e) {
				String msg = context.getResources().getString(
						R.string.msg_unable_open_file);
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		} else {
			aurora.app.AuroraAlertDialog.Builder dialogBuilder = new aurora.app.AuroraAlertDialog.Builder(
					context);
			dialogBuilder.setTitle(R.string.dialog_select_type);

			CharSequence[] menuItemArray = new CharSequence[] {
					context.getString(R.string.dialog_type_text),
					context.getString(R.string.dialog_type_audio),
					context.getString(R.string.dialog_type_video),
					context.getString(R.string.dialog_type_image) };
			dialogBuilder.setItems(menuItemArray,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String selectType = "*/*";
							switch (which) {
							case 0:
								selectType = "text/plain";
								break;
							case 1:
								selectType = "audio/*";
								break;
							case 2:
								selectType = "video/*";
								break;
							case 3:
								selectType = "image/*";
								break;
							}
							Intent intent = new Intent();
//							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							intent.setAction(android.content.Intent.ACTION_VIEW);
							intent.setDataAndType(
									Uri.fromFile(new File(filePath)),
									selectType);
							try {
								context.startActivity(intent);
//								if(context instanceof FileExplorerTabActivity){
//									((FileExplorerTabActivity)context).overridePendingTransition(
//											com.aurora.R.anim.aurora_activity_open_enter,
//											com.aurora.R.anim.aurora_activity_open_exit);
//								}
							} catch (Exception e) {
								String msg = context.getResources().getString(
										R.string.msg_unable_open_file);
								Toast.makeText(context, msg, Toast.LENGTH_LONG)
										.show();
							}
						}
					});
			dialogBuilder.show();
		}
	}

	public static Intent buildSendFile(List<FileInfo> files) {
		ArrayList<Uri> uris = new ArrayList<Uri>();
		Set<String> mimeSet = new HashSet<String>();
		boolean isMediaFile = false;
		String mimeType = "*/*";
		for (FileInfo file : files) {
			if (file.IsDir)
				continue;

			File fileIn = new File(file.filePath);
			mimeType = MimeTypeUtil.getMimeType(file.fileName);
			// LogUtil.log(TAG, "mimeType:"+mimeType);
			int fileType = MediaFile.getFileTypeByMimeType(mimeType);
			/*
			 * if(MediaFile.isAudioFileType(fileType)){ mimeType = "audio/";
			 * isMediaFile=true; }else
			 */
			if (MediaFile.isImageFileType(fileType)) {
				mimeType = "image/*";
				isMediaFile = true;
			}
			// else if (MediaFile.isVideoFileType(fileType)) {
			// mimeType = "video/";
			// isMediaFile=true;
			// }
			// LogUtil.log(TAG, "mimeType:"+mimeType+" fileType:"+fileType);
			mimeSet.add(mimeType);
			Uri u = Uri.fromFile(fileIn);
			uris.add(u);
		}

		if (uris.size() == 0)
			return null;

		boolean multiple = uris.size() > 1;
		Intent intent = new Intent(
				multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
						: android.content.Intent.ACTION_SEND);

		if (multiple) {
			if (mimeSet.size() == 1 && isMediaFile) {
				intent.setType(mimeType);
			} else {
				intent.setType("*/*");
			}
			LogUtil.d(TAG, "mimeSet.size():" + mimeSet.size());
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} else {
			if (mimeType.equals("application/vnd.android.package-archive")) {// apk
				intent.setType("application/*");
			} else {
				intent.setType(mimeType);
			}
			intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
		}
		LogUtil.d(TAG, "mimeType::" + mimeType);
		return intent;
	}

	/**
	 * 其他APP调用 返回单个数据
	 * 
	 * @param activity
	 * @param uri
	 */
	public static void getSinglePic(Activity activity, Uri uri) {
		Intent intent = new Intent(null, uri)
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		activity.setResult(Activity.RESULT_OK, intent);
		activity.finish();
	}

	/**
	 * 其他APP调用 返回多个数据
	 * 
	 * @param activity
	 * @param uri
	 */
	public static void getMorePic(Activity activity, ArrayList<String> paths) {

		Intent intent = new Intent()
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("image", paths);
		intent.putExtras(bundle);
		activity.setResult(Activity.RESULT_OK, intent);
		activity.finish();
	}

	/**
	 * 其他APP调用 返回多个数据
	 * 
	 * @param activity
	 * @param uri
	 */
	public static void getMoreVideo(Activity activity, ArrayList<String> paths) {

		Intent intent = new Intent()
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("video", paths);
		intent.putExtras(bundle);
		activity.setResult(Activity.RESULT_OK, intent);
		activity.finish();
	}

}

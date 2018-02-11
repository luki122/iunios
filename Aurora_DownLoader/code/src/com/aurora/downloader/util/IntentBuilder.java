package com.aurora.downloader.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.aurora.downloader.DownloadActivity;
import com.aurora.downloader.FileInfo;
import com.aurora.downloader.R;

public class IntentBuilder {
	private static final String TAG = "IntentBuilder";

	public static void viewFile(final DownloadActivity context,
			final String filePath) {
		String type = MimeTypeUtil.getMimeType(filePath);
		if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
			if (type.startsWith("video")) {
				type = "video/*";
			}
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
			try {
				context.startActivity(intent);
				context.overridePendingTransition(
						com.aurora.R.anim.aurora_activity_open_enter,
						com.aurora.R.anim.aurora_activity_open_exit);
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
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							intent.setAction(android.content.Intent.ACTION_VIEW);
							intent.setDataAndType(
									Uri.fromFile(new File(filePath)),
									selectType);
							try {
								context.startActivity(intent);
								context.overridePendingTransition(
										com.aurora.R.anim.aurora_activity_open_enter,
										com.aurora.R.anim.aurora_activity_open_exit);
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
		boolean is = false;
		String mimeType = "*/*";
		for (FileInfo file : files) {
			if(file==null||TextUtils.isEmpty(file.filePath)){
				continue;
			}
			File fileIn = new File(file.filePath);
			mimeType = MimeTypeUtil.getMimeType(file.fileName);
			// add by JXH 2014-7-18 多个图片分享 begin
			if (mimeType.startsWith("image/")) {
				is = true;
			}
			// add by JXH 2014-7-18 多个图片分享 end
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
			if (is) {
				intent.setType("image/*");
			} else {
				intent.setType("*/*");
			}
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} else {
			if (mimeType.equals("application/vnd.android.package-archive")) {// apk
				intent.setType("application/*");
			} else {
				intent.setType(mimeType);
			}
			intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
		}

		return intent;
	}

	public static Intent buildSendFilePath(List<String> filesPath) {
		ArrayList<Uri> uris = new ArrayList<Uri>();

		String mimeType = "*/*";
		for (String filePath : filesPath) {
			File fileIn = new File(filePath);
			mimeType = MimeTypeUtil.getMimeType(filePath);
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
			intent.setType("*/*");
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} else {
			intent.setType(mimeType);
			intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
		}

		return intent;
	}

}

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.contacts.vcard;

import java.util.Random;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.util.IntentFactory;

import android.util.Log;
import com.android.vcard.VCardEntry;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotificationImportExportListener implements VCardImportExportListener,
Handler.Callback {
	/** The tag used by vCard-related notifications. */
	/* package */ static final String DEFAULT_NOTIFICATION_TAG = "VCardServiceProgress";
	/**
	 * The tag used by vCard-related failure notifications.
	 * <p>
	 * Use a different tag from {@link #DEFAULT_NOTIFICATION_TAG} so that failures do not get
	 * replaced by other notifications and vice-versa.
	 */
	/* package */ static final String FAILURE_NOTIFICATION_TAG = "VCardServiceFailure";
	protected static final String TAG = "NotificationImportExportListener";

	private final NotificationManager mNotificationManager;
	private final Activity mContext;
	private final Handler mHandler;

	//aurora add zhouxiaobing 20131223 start
	private int seq_index=0;
	//aurora add zhouxiaobing 20131223 end    
	//Gionee:huangzy 20120716 add for CR00640706 start
	private static boolean mIsImporting = false;
	public static boolean isImporting() {
		return mIsImporting;
	}
	//Gionee:huangzy 20120716 add for CR00640706 end

	public NotificationImportExportListener(Activity activity) {
		mContext = activity;
		mNotificationManager = (NotificationManager) activity.getSystemService(
				Context.NOTIFICATION_SERVICE);
		mHandler = new Handler(this);
	}

	@Override
	public boolean handleMessage(Message msg) {
		String text = (String) msg.obj;
		Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
		return true;
	}

	@Override
	public void onImportProcessed(ImportRequest request, int jobId, int sequence) {
		//Gionee:huangzy 20120716 add for CR00640706 start
		mIsImporting = true;
		//Gionee:huangzy 20120716 add for CR00640706 end
		// Show a notification about the status
		final String displayName;
		final String message;
		if (request.displayName != null) {
			displayName = request.displayName;
			//        message = mContext.getString(R.string.vcard_import_will_start_message, displayName);//aurora add zhouxiaobing 20131205
		} else {
			displayName = mContext.getString(R.string.vcard_unknown_filename);
			//          message = mContext.getString(
			//                  R.string.vcard_import_will_start_message_with_default_name);//aurora add zhouxiaobing 20131205
		}
		//aurora add zhouxiaobing 20131205 start
		message = mContext.getString(
				R.string.aurora_start_daoru);
		//aurora add zhouxiaobing 20131205 end
		// We just want to show notification for the first vCard.
		if (sequence == 0) {
			// TODO: Ideally we should detect the current status of import/export and
			// show "started" when we can import right now and show "will start" when
			// we cannot.
			//Gionee <wangth><2013-05-27> modify for CR00819923 begin
			/*
            mHandler.obtainMessage(0, message).sendToTarget();
			 */
			if (!ImportVCardActivity.BLUETOOTH_PBAP_IMPORT.equals(displayName)) {
				mHandler.obtainMessage(0, message).sendToTarget();
			}
			//Gionee <wangth><2013-05-27> modify for CR00819923 end
		}

		final Notification notification = constructProgressNotification(mContext,
				VCardService.TYPE_IMPORT, message, message, jobId, displayName, -1, 0);
		mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobId, notification);
	}

	@Override
	public void onImportParsed(ImportRequest request, int jobId, VCardEntry entry, int currentCount,
			int totalCount) {
		if (entry.isIgnorable()) {
			return;
		}

		//Gionee:huangzy 20120716 add for CR00640706 start
		mIsImporting = true;
		//Gionee:huangzy 20120716 add for CR00640706 end

		final String totalCountString = String.valueOf(totalCount);
		final String tickerText =
				mContext.getString(R.string.progress_notifier_message,
						String.valueOf(currentCount),
						totalCountString,
						entry.getDisplayName());
		final String description = mContext.getString(R.string.importing_vcard_description,
				entry.getDisplayName());

		final Notification notification = constructProgressNotification(
				mContext.getApplicationContext(), VCardService.TYPE_IMPORT, description, tickerText,
				jobId, request.displayName, totalCount, currentCount);
		mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobId, notification);
	}

	@Override
	public void onImportFinished(final ImportRequest request, final int jobId, final Uri createdUri) {
		//Gionee:huangzy 20120716 add for CR00640706 start
		mIsImporting = false;
		//Gionee:huangzy 20120716 add for CR00640706 end

		final String description = mContext.getString(R.string.importing_vcard_finished_title,
				request.displayName);
		final Intent intent;
		if (createdUri != null) {
			// aurora <wangth> <2013-12-2> modify for aurora begin
			//            final long rawContactId = ContentUris.parseId(createdUri);
			//            final Uri contactUri = RawContacts.getContactLookupUri(
			//                    mContext.getContentResolver(), ContentUris.withAppendedId(
			//                            RawContacts.CONTENT_URI, rawContactId));
			//            //Gionee:huangzy 20130401 modify for CR00792013 start
			//        	/*intent = new Intent(Intent.ACTION_VIEW, contactUri);*/
			//        	intent = IntentFactory.newViewContactIntent(contactUri);
			//            //Gionee:huangzy 20130401 modify for CR00792013 end
			intent = IntentFactory.newGoPeopleIntent();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// aurora <wangth> <2013-12-2> modify for aurora end
		} else {
			intent = null;
		}
		final Notification notification =
				NotificationImportExportListener.constructFinishNotification(VCardService.TYPE_IMPORT,mContext,
						description, null, intent);
		mNotificationManager.notify(NotificationImportExportListener.DEFAULT_NOTIFICATION_TAG,
				jobId, notification);
		Log.v("AuroraContactImportExportActivity", "onImportFinished jobId="+jobId+"VCardService.import_vcard_num="+VCardService.import_vcard_num);   
		seq_index++;


		if(seq_index==VCardService.import_vcard_num)
		{
			mHandler.obtainMessage(0, mContext.getString(R.string.aurora_end_daoru)).sendToTarget();//aurora change zhouxiaobing 20131205
			Log.v("AuroraContactImportExportActivity", "onImportFinished");
			VCardService.mIsstart=false;
			seq_index=0;
		}


//		try{
//			Cursor cursor=mContext.getContentResolver().query(Contacts.CONTENT_URI,
//					new String[]{Contacts.NAME_RAW_CONTACT_ID,"photo_id"},
//					"photo_id is null",null,null);
//			Log.d(TAG,"cursor9:"+cursor.getCount());
//			if(cursor.getCount()>0){
//				Random random=new Random();
//				ContentValues values = new ContentValues();
//				while(cursor.moveToNext()){
//					if(cursor.isNull(cursor.getColumnIndex("photo_id"))){
//						values.put("photo_id", -random.nextInt(14)-100);		                    	
//						values.put("flag", 1);
//						mContext.getContentResolver().update(Contacts.CONTENT_URI, values, 
//								Contacts.NAME_RAW_CONTACT_ID + "=" + cursor.getInt(cursor.getColumnIndex(Contacts.NAME_RAW_CONTACT_ID)), null);
//					}
//				}	
//
//				//						mContext.sendBroadcast(new Intent("import_vcard_finished"));
//			}	
//		}catch(Exception e){
//			e.printStackTrace();
//		}
	}
	@Override
	public void onImportFailed(ImportRequest request) {
		//Gionee:huangzy 20120716 add for CR00640706 start
		mIsImporting = false;
		//Gionee:huangzy 20120716 add for CR00640706 end

		// TODO: a little unkind to show Toast in this case, which is shown just a moment.
		// Ideally we should show some persistent something users can notice more easily.
		mHandler.obtainMessage(0,
				mContext.getString(R.string.vcard_import_request_rejected_message)).sendToTarget();

		VCardService.mIsstart=false;//aurora add zhouxiaobing 20131223
	}

	@Override
	public void onImportCanceled(ImportRequest request, int jobId) {
		//Gionee:huangzy 20120716 add for CR00640706 start
		mIsImporting = false;
		//Gionee:huangzy 20120716 add for CR00640706 end

		final String description = mContext.getString(R.string.importing_vcard_canceled_title,
				request.displayName);
		final Notification notification =
				NotificationImportExportListener.constructCancelNotification(mContext, description);
		mNotificationManager.notify(NotificationImportExportListener.DEFAULT_NOTIFICATION_TAG,
				jobId, notification);

		VCardService.mIsstart=false;//aurora add zhouxiaobing 20131223
	}

	@Override
	public void onExportProcessed(ExportRequest request, int jobId) {
		final String displayName = request.destUri.getLastPathSegment();
		//        final String message = mContext.getString(R.string.vcard_export_will_start_message,
		//                displayName);
		final String message = mContext.getString(R.string.aurora_start_daochu);//aurora change zhouxiaobing 20131205
		mHandler.obtainMessage(0, message).sendToTarget();
		final Notification notification =
				NotificationImportExportListener.constructProgressNotification(mContext,
						VCardService.TYPE_EXPORT, message, message, jobId, displayName, -1, 0);
		mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobId, notification);
	}

	@Override
	public void onExportFailed(ExportRequest request) {
		mHandler.obtainMessage(0,
				mContext.getString(R.string.vcard_export_request_rejected_message)).sendToTarget();
		VCardService.mIsstart=false;//aurora add zhouxiaobing 20131223
	}
	@Override
	public void onExportFinished(ExportRequest request, int jobId, Uri uri)
	{
		mHandler.obtainMessage(0, mContext.getString(R.string.aurora_end_daochu)).sendToTarget();
	}
	@Override
	public void onCancelRequest(CancelRequest request, int type) {
		final String description = type == VCardService.TYPE_IMPORT ?
				mContext.getString(R.string.importing_vcard_canceled_title, request.displayName) :
					mContext.getString(R.string.exporting_vcard_canceled_title, request.displayName);
				final Notification notification = constructCancelNotification(mContext, description);
				mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, request.jobId, notification);
	}

	/**
	 * Constructs a {@link Notification} showing the current status of import/export.
	 * Users can cancel the process with the Notification.
	 *
	 * @param context
	 * @param type import/export
	 * @param description Content of the Notification.
	 * @param tickerText
	 * @param jobId
	 * @param displayName Name to be shown to the Notification (e.g. "finished importing XXXX").
	 * Typycally a file name.
	 * @param totalCount The number of vCard entries to be imported. Used to show progress bar.
	 * -1 lets the system show the progress bar with "indeterminate" state.
	 * @param currentCount The index of current vCard. Used to show progress bar.
	 */
	/* package */ static Notification constructProgressNotification(
			Context context, int type, String description, String tickerText,
			int jobId, String displayName, int totalCount, int currentCount) {
		// Note: We cannot use extra values here (like setIntExtra()), as PendingIntent doesn't
		// preserve them across multiple Notifications. PendingIntent preserves the first extras
		// (when flag is not set), or update them when PendingIntent#getActivity() is called
		// (See PendingIntent#FLAG_UPDATE_CURRENT). In either case, we cannot preserve extras as we
		// expect (for each vCard import/export request).
		//
		// We use query parameter in Uri instead.
		// Scheme and Authority is arbitorary, assuming CancelActivity never refers them.
		final Intent intent = new Intent(context, CancelActivity.class);
		final Uri uri = (new Uri.Builder())
				.scheme("invalidscheme")
				.authority("invalidauthority")
				.appendQueryParameter(CancelActivity.JOB_ID, String.valueOf(jobId))
				.appendQueryParameter(CancelActivity.DISPLAY_NAME, displayName)
				.appendQueryParameter(CancelActivity.TYPE, String.valueOf(type)).build();
		intent.setData(uri);

		final Notification.Builder builder = new Notification.Builder(context);
		builder.setOngoing(true)
		.setProgress(totalCount, currentCount, totalCount == - 1)
		/*.setTicker(tickerText)*/
		.setContentTitle(description)
		// Gionee:wangth 20120712 modify for CR00640215 begin
		/*
                .setSmallIcon(type == VCardService.TYPE_IMPORT ? android.R.drawable.stat_sys_download_done : android.R.drawable.stat_sys_upload_done)
		 */
		.setSmallIcon(type == VCardService.TYPE_IMPORT ? R.drawable.aurora_stat_sys_download_done : R.drawable.gn_stat_sys_upload_done_static)
		// Gionee:wangth 20120712 modify for CR00640215 end
		.setContentIntent(/*PendingIntent.getActivity(context, 0, intent, 0)*/null);//aurora change zhouxiaobing 20131205
		if (totalCount > 0) {
			builder.setContentText(context.getString(R.string.percentage,
					String.valueOf(currentCount * 100 / totalCount)));
			Intent intent2=new Intent("com.android.action.LAUNCH_CONTACTS_LIST");
			intent2.putExtra("percentage", currentCount * 100 / totalCount);
			intent2.putExtra("description", description);
			context.sendBroadcast(intent2);
		}
		return builder.getNotification();
	}

	/**
	 * Constructs a Notification telling users the process is canceled.
	 *
	 * @param context
	 * @param description Content of the Notification
	 */
	/* package */ static Notification constructCancelNotification(
			Context context, String description) {
		return new Notification.Builder(context)
		.setAutoCancel(true)
		.setSmallIcon(android.R.drawable.stat_notify_error)
		.setContentTitle(description)
		.setContentText(description)
		.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
		.getNotification();
	}

	/**
	 * Constructs a Notification telling users the process is finished.
	 *
	 * @param context
	 * @param description Content of the Notification
	 * @param intent Intent to be launched when the Notification is clicked. Can be null.
	 */
	/* package */ static Notification constructFinishNotification(int type,
			Context context, String title, String description, Intent intent) {
		return new Notification.Builder(context)
		.setAutoCancel(true)
		// Gionee:wangth 20120712 modify for CR00640215 begin
		/*
                .setSmallIcon(type == VCardService.TYPE_IMPORT ? android.R.drawable.stat_sys_download_done : android.R.drawable.stat_sys_upload_done)
		 */
		.setSmallIcon(type == VCardService.TYPE_IMPORT ? R.drawable.aurora_stat_sys_download_done : R.drawable.gn_stat_sys_upload_done_static)
		// Gionee:wangth 20120712 modify for CR00640215 end
		.setContentTitle(title)
		.setContentText(description)
		.setContentIntent(PendingIntent.getActivity(context, 0,
				(intent != null ? intent : new Intent()), 0))
				.getNotification();
	}

	/**
	 * Constructs a Notification telling the vCard import has failed.
	 *
	 * @param context
	 * @param reason The reason why the import has failed. Shown in description field.
	 */
	/* package */ static Notification constructImportFailureNotification(
			Context context, String reason) {
		return new Notification.Builder(context)
		.setAutoCancel(true)
		.setSmallIcon(android.R.drawable.stat_notify_error)
		.setContentTitle(context.getString(R.string.vcard_import_failed))
		.setContentText(reason)
		.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
		.getNotification();
	}

	@Override
	public void onComplete() {
		mContext.finish();
	}
}

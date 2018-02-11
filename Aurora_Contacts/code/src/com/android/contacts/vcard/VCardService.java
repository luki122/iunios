/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.contacts.vcard;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
//Gionee baorui 2012-04-26 add for CR00582516 begin
import java.text.SimpleDateFormat;
import java.util.Date;
//Gionee baorui 2012-04-26 add for CR00582516 end
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;



//Gionee:wangth 20120420 add for CR00576392 begin
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.android.contacts.ContactsUtils;

import java.util.ConcurrentModificationException;

import gionee.os.storage.GnStorageManager;
//Gionee:wangth 20120420 add for CR00576392 end

/**
 * The class responsible for handling vCard import/export requests.
 *
 * This Service creates one ImportRequest/ExportRequest object (as Runnable) per request and push
 * it to {@link ExecutorService} with single thread executor. The executor handles each request
 * one by one, and notifies users when needed.
 */
// TODO: Using IntentService looks simpler than using Service + ServiceConnection though this
// works fine enough. Investigate the feasibility.
public class VCardService extends Service {
    private final static String LOG_TAG = "VCardService";

    /* package */ final static boolean DEBUG = false;

    /* package */ static final int MSG_IMPORT_REQUEST = 1;
    /* package */ static final int MSG_EXPORT_REQUEST = 2;
    /* package */ static final int MSG_CANCEL_REQUEST = 3;
    /* package */ static final int MSG_REQUEST_AVAILABLE_EXPORT_DESTINATION = 4;
    /* package */ static final int MSG_SET_AVAILABLE_EXPORT_DESTINATION = 5;

    /**
     * Specifies the type of operation. Used when constructing a notification, canceling
     * some operation, etc.
     */
    /* package */ static final int TYPE_IMPORT = 1;
    /* package */ static final int TYPE_EXPORT = 2;

    /* package */ static final String CACHE_FILE_PREFIX = "import_tmp_";
    
    // Gionee:wangth 20120614 add for CR00624439 begin
    public static boolean mIsShareMode = false;
    // Gionee:wangth 20120614 add for CR00624439 end
//aurora add zhouxiaobing 20131212 start
    public static boolean mIsstart=false;
    public static int import_vcard_num;
//aurora add zhouxiaobing 20131212 end    

    private class CustomMediaScannerConnectionClient implements MediaScannerConnectionClient {
        final MediaScannerConnection mConnection;
        final String mPath;

        public CustomMediaScannerConnectionClient(String path) {
            mConnection = new MediaScannerConnection(VCardService.this, this);
            mPath = path;
        }

        public void start() {
            mConnection.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            if (DEBUG) { Log.d(LOG_TAG, "Connected to MediaScanner. Start scanning."); }
            mConnection.scanFile(mPath, null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            if (DEBUG) { Log.d(LOG_TAG, "scan completed: " + path); }
            mConnection.disconnect();
            removeConnectionClient(this);
        }
    }

    // Should be single thread, as we don't want to simultaneously handle import and export
    // requests.
    private final ExecutorService mExecutorService = ContactsApplication.getInstance().singleTaskService;

    private int mCurrentJobId;

    // Stores all unfinished import/export jobs which will be executed by mExecutorService.
    // Key is jobId.
    private final Map<Integer, ProcessorBase> mRunningJobMap =
            new HashMap<Integer, ProcessorBase>();
    // Stores ScannerConnectionClient objects until they finish scanning requested files.
    // Uses List class for simplicity. It's not costly as we won't have multiple objects in
    // almost all cases.
    private final List<CustomMediaScannerConnectionClient> mRemainingScannerConnections =
            new ArrayList<CustomMediaScannerConnectionClient>();

    /* ** vCard exporter params ** */
    // If true, VCardExporter is able to emits files longer than 8.3 format.
    private static final boolean ALLOW_LONG_FILE_NAME = false;

    private String mTargetDirectory;
    private String mFileNamePrefix;
    private String mFileNameSuffix;
    private int mFileIndexMinimum;
    private int mFileIndexMaximum;
    private String mFileNameExtension;
    private Set<String> mExtensionsToConsider;
    private String mErrorReason;
    private MyBinder mBinder;

    // File names currently reserved by some export job.
    private final Set<String> mReservedDestination = new HashSet<String>();
    /* ** end of vCard exporter params ** */
    private VCardImportExportListener mlistener;

    public class MyBinder extends Binder {
        public VCardService getService() {
            return VCardService.this;
        }
    }

   @Override
    public void onCreate() {	   
	   ContactsApplication.sendSimContactBroad(0);
        super.onCreate();
        mBinder = new MyBinder();
        Log.d(LOG_TAG, "vCard Service is being created.");
        initExporterParams();
        
    }

    private void initExporterParams() {
        // The following lines are provided and maintained by Mediatek inc.
        // mTargetDirectory = getString(R.string.config_export_dir);
        // The following lines are provided and maintained by Mediatek inc.
        mFileNamePrefix = getString(R.string.config_export_file_prefix);
        mFileNameSuffix = getString(R.string.config_export_file_suffix);
        mFileNameExtension = getString(R.string.config_export_file_extension);

        mExtensionsToConsider = new HashSet<String>();
        mExtensionsToConsider.add(mFileNameExtension);

        final String additionalExtensions =
            getString(R.string.config_export_extensions_to_consider);
        if (!TextUtils.isEmpty(additionalExtensions)) {
            for (String extension : additionalExtensions.split(",")) {
                String trimed = extension.trim();
                if (trimed.length() > 0) {
                    mExtensionsToConsider.add(trimed);
                }
            }
        }

        final Resources resources = getResources();
        mFileIndexMinimum = resources.getInteger(R.integer.config_export_file_min_index);
        mFileIndexMaximum = resources.getInteger(R.integer.config_export_file_max_index);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(LOG_TAG, "VCardService is being destroyed.");
        cancelAllRequestsAndShutdown();
        clearCache();
        ExportVCardActivity.isSdCard2=false;//aurora add zhouxiaobing 20131212
        
        ContactsApplication.sendSimContactBroad(1);
        super.onDestroy();
    }

    public synchronized void handleImportRequest(List<ImportRequest> requests,
            VCardImportExportListener listener) {
    	mIsstart=true;//aurora add zhouxiaobing 20131219
    	sendVcardLoadStart();
    	boolean is_failed=false;//aurora add zhouxiaobing 20131219
        if (DEBUG) {
            final ArrayList<String> uris = new ArrayList<String>();
            final ArrayList<String> displayNames = new ArrayList<String>();
            for (ImportRequest request : requests) {
                uris.add(request.uri.toString());
                displayNames.add(request.displayName);
            }
            Log.d(LOG_TAG,
                    String.format("received multiple import request (uri: %s, displayName: %s)",
                            uris.toString(), displayNames.toString()));
        }
        final int size = requests.size();
        import_vcard_num=size;//aurora add zhouxiaobing 20131223
        for (int i = 0; i < size; i++) {
            ImportRequest request = requests.get(i);

            // Gionee:wangth 20120709 modify for CR00640372 begin
            /*
            if (tryExecute(new ImportProcessor(this, listener, request, mCurrentJobId))) {
                if (listener != null) {
                    listener.onImportProcessed(request, mCurrentJobId, i);
                }
                mCurrentJobId++;
            } else {
                if (listener != null) {
                    listener.onImportFailed(request);
                }
                // A rejection means executor doesn't run any more. Exit.
                break;
            }
            */
            if (ContactsUtils.mIsGnContactsSupport) {
                if (tryExecute(new ImportProcessor(this, listener, request, 0))) {
                    if (listener != null) {
                        if (mCurrentJobId == 0 || ImportProcessor.mIsFinishImport) {
                            ImportProcessor.mIsFinishImport = false;
                            listener.onImportProcessed(request, 0, i);
                        }
                    }
                    mCurrentJobId++;
                } else {
                    if (listener != null) {
                        listener.onImportFailed(request);
                        is_failed=true;//aurora add zhouxiaobing 20131219
                    }
                    // A rejection means executor doesn't run any more. Exit.
                    break;
                }
            } else {
                if (tryExecute(new ImportProcessor(this, listener, request, mCurrentJobId))) {
                    if (listener != null) {
                        listener.onImportProcessed(request, mCurrentJobId, i);
                    }
                    mCurrentJobId++;
                } else {
                    if (listener != null) {
                        listener.onImportFailed(request);
                        is_failed=true;//aurora add zhouxiaobing 20131219
                    }
                    // A rejection means executor doesn't run any more. Exit.
                    break;
                }
            }
            // Gionee:wangth 20120709 modify for CR00640372 end
        }
        
//      if(!is_failed)  
//         Toast.makeText(this.getApplicationContext(), this.getApplication().getString(R.string.aurora_end_daoru), Toast.LENGTH_LONG).show();
      Log.v("AuroraContactImportExportActivity", "handleImportRequest");       
    }

    public synchronized void handleExportRequest(ExportRequest request,
            VCardImportExportListener listener) {
    	mIsstart=true;//aurora add zhouxiaobing 20131219
    	sendVcardLoadStart();
            mlistener=listener;//aurora add zhouxiaobing 20131205
        if (tryExecute(new ExportProcessor(this, request, mCurrentJobId))) {
            final String path = request.destUri.getEncodedPath();
            if (DEBUG) Log.d(LOG_TAG, "Reserve the path " + path);
            if (!mReservedDestination.add(path)) {
                Log.w(LOG_TAG,
                        String.format("The path %s is already reserved. Reject export request",
                                path));
                if (listener != null) {
                    listener.onExportFailed(request);
                }
                return;
            }

            if (listener != null) {
                listener.onExportProcessed(request, mCurrentJobId);
            }
            mCurrentJobId++;
        } else {
            if (listener != null) {
                listener.onExportFailed(request);
            }
        }
    }

    /**
     * Tries to call {@link ExecutorService#execute(Runnable)} toward a given processor.
     * @return true when successful.
     */
    private synchronized boolean tryExecute(ProcessorBase processor) {
        try {
            if (DEBUG) {
                Log.d(LOG_TAG, "Executor service status: shutdown: " + mExecutorService.isShutdown()
                        + ", terminated: " + mExecutorService.isTerminated());
            }
            mExecutorService.execute(processor);
            mRunningJobMap.put(mCurrentJobId, processor);
            return true;
        } catch (RejectedExecutionException e) {
            Log.w(LOG_TAG, "Failed to excetute a job.", e);
            return false;
        }
    }

    public synchronized void handleCancelRequest(CancelRequest request,
            VCardImportExportListener listener) {
        final int jobId = request.jobId;
        if (DEBUG) Log.d(LOG_TAG, String.format("Received cancel request. (id: %d)", jobId));
        final ProcessorBase processor = mRunningJobMap.remove(jobId);

        if (processor != null) {
            processor.cancel(true);
            final int type = processor.getType();
            if (listener != null) {
                listener.onCancelRequest(request, type);
            }
            if (type == TYPE_EXPORT) {
                final String path =
                        ((ExportProcessor)processor).getRequest().destUri.getEncodedPath();
                Log.i(LOG_TAG,
                        String.format("Cancel reservation for the path %s if appropriate", path));
                if (!mReservedDestination.remove(path)) {
                    Log.w(LOG_TAG, "Not reserved.");
                }
            }
            // Gionee:wangth 20120709 add for CR00640372 begin
            else if (ContactsUtils.mIsGnContactsSupport && type == TYPE_IMPORT) {
                Log.e(LOG_TAG, "cancel import vcard, stopSelf.");
                stopSelf();
            }
            // Gionee:wangth 20120709 add for CR00640372 end
        } else {
            // Gionee:wangth 20120709 add for CR00640372 begin
            if (ContactsUtils.mIsGnContactsSupport) {
                Log.e(LOG_TAG, "processor is null, stopSelf.");
                stopSelf();
            }
            // Gionee:wangth 20120709 add for CR00640372 end
            Log.w(LOG_TAG, String.format("Tried to remove unknown job (id: %d)", jobId));
        }
        stopServiceIfAppropriate();
        mIsstart=false;//aurora add zhouxiaobing 20131219 
    }

    public synchronized void handleRequestAvailableExportDestination(final Messenger messenger) {
        if (DEBUG) Log.d(LOG_TAG, "Received available export destination request.");
        // The following lines are provided and maintained by Mediatek inc.
        // mTargetDirectory = getString(R.string.config_export_dir);
        mSM = (StorageManager) getApplicationContext().getSystemService(STORAGE_SERVICE);
        //Gionee:wangth 20120616 modify for CR00576392 && CR00624439 && CR00624473 begin
        //mTargetDirectory = mSM.getDefaultPath();
        if (ContactsUtils.mIsGnContactsSupport) {
            if (mIsShareMode == true) {
                String path = GnStorageManager.getDefaultPath();
                String lastStr = String.valueOf(path.charAt(path.length() - 1));
                mIsShareMode = false;
                
                if (lastStr.equals("d")) {
                    mTargetDirectory = ContactImportExportActivity.mSDCard;
                } else if (lastStr.equals("2")) {
                    mTargetDirectory = ContactImportExportActivity.mSDCard2;
                } else {
                	mTargetDirectory = path;
                }
            } else {
                if (ContactsFeatureConstants.FeatureOption.MTK_2SDCARD_SWAP) {
                    if (ContactImportExportActivity.mSelectedStep1Postion == ContactImportExportActivity.mUSBStoragePostion || 
                            ContactImportExportActivity.mSelectedStep2Postion == ContactImportExportActivity.mUSBStoragePostion) {
                        if (ContactImportExportActivity.mStorageCount >= 2) {
                            mTargetDirectory = ContactImportExportActivity.mSDCard2;
                        } else {
                            mTargetDirectory = ContactImportExportActivity.mSDCard;
                            // Gionee:wangth 20120808 add for CR00671679 begin
                            if (ContactImportExportActivity.isExSdcardInserted()) {
                                mTargetDirectory = ContactImportExportActivity.mSDCard2;
                            }
                            // Gionee:wangth 20120808 add for CR00671679 end
                        }
                    } else {
                        if (ContactImportExportActivity.mStorageCount >= 2) {
                            mTargetDirectory = ContactImportExportActivity.mSDCard;
                        } else {
                            mTargetDirectory = ContactImportExportActivity.mSDCard;
                        }
                    }
                } else {
                    if (ContactImportExportActivity.mSelectedStep1Postion == ContactImportExportActivity.mUSBStoragePostion || 
                            ContactImportExportActivity.mSelectedStep2Postion == ContactImportExportActivity.mUSBStoragePostion) {
                        mTargetDirectory = ContactImportExportActivity.mSDCard;
                    } else {
                        mTargetDirectory = ContactImportExportActivity.mSDCard2;
                    }
                }
            }
        } else {
            mTargetDirectory = GnStorageManager.getDefaultPath();
        }
        
        if (GNContactsUtils.isOnlyQcContactsSupport()) {
        	mTargetDirectory = Environment.getExternalStorageDirectory().getPath();
        }
        //aurora add zhouxiaobing 20131211 start
        if(ExportVCardActivity.isSdCard2)
        {
        	mTargetDirectory=GnStorageManager.getInstance(ContactsApplication.getInstance()).getExternalStoragePath();
        }
        
       //aurora add zhouxiaobing 20131211 end
        //Gionee:wangth 20120616 modify for CR00576392 && CR00624439 && CR00624473 end
        Log.i("vcard", "mTargetDirectory : " + mTargetDirectory);
        // The following lines are provided and maintained by Mediatek inc.
        // Gionee baorui 2012-04-26 add for CR00582516 begin
        if (true == ContactsUtils.mIsGnContactsSupport) {
            String mDirectoryName = getString(R.string.gn_exportDirectoryName);
            mTargetDirectory += mDirectoryName;
        }
        // Gionee baorui 2012-04-26 add for CR00582516 end
        final String path = getAppropriateDestination(mTargetDirectory);
        final Message message;
        if (path != null) {
            message = Message.obtain(null,
                    VCardService.MSG_SET_AVAILABLE_EXPORT_DESTINATION, 0, 0, path);
        } else {
            message = Message.obtain(null,
                    VCardService.MSG_SET_AVAILABLE_EXPORT_DESTINATION,
                    R.id.dialog_fail_to_export_with_reason, 0, mErrorReason);
        }
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "Failed to send reply for available export destination request.", e);
        }
    }

    /**
     * Checks job list and call {@link #stopSelf()} when there's no job and no scanner connection
     * is remaining.
     * A new job (import/export) cannot be submitted any more after this call.
     */
    private synchronized void stopServiceIfAppropriate() {
        // Gionee:wangth 20120709 add for CR00640372 begin
        try {
	        // Gionee:wangth 20120709 add for CR00640372 end
	        if (mRunningJobMap.size() > 0) {
	            for (final Map.Entry<Integer, ProcessorBase> entry : mRunningJobMap.entrySet()) {
	                final int jobId = entry.getKey();
	                final ProcessorBase processor = entry.getValue();
	                if (processor.isDone()) {
	                    mRunningJobMap.remove(jobId);
	                } else {
	                    Log.i(LOG_TAG, String.format("Found unfinished job (id: %d)", jobId));
	                    return;
	                }
	            }
	        }
	
	        if (!mRemainingScannerConnections.isEmpty()) {
	            Log.i(LOG_TAG, "MediaScanner update is in progress.");
	            return;
	        }
	
	        Log.i(LOG_TAG, "No unfinished job. Stop this service.");
	        ContactsApplication.sendSimContactBroad(1);
	        //mExecutorService.shutdown();
	    	sendVcardLoadEnd();
	        stopSelf();
	        // Gionee:wangth 20120709 add for CR00640372 begin
        } catch (ConcurrentModificationException c) {
            Log.e(LOG_TAG, "ConcurrentModificationException");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Gionee:wangth 20120709 add for CR00640372 end
    }

    /* package */ synchronized void updateMediaScanner(String path) {
        if (DEBUG) {
            Log.d(LOG_TAG, "MediaScanner is being updated: " + path);
        }

        if (mExecutorService.isShutdown()) {
            Log.w(LOG_TAG, "MediaScanner update is requested after executor's being shut down. " +
                    "Ignoring the update request");
            return;
        }
        final CustomMediaScannerConnectionClient client =
                new CustomMediaScannerConnectionClient(path);
        mRemainingScannerConnections.add(client);
        client.start();
    }

    private synchronized void removeConnectionClient(
            CustomMediaScannerConnectionClient client) {
        if (DEBUG) {
            Log.d(LOG_TAG, "Removing custom MediaScannerConnectionClient.");
        }
        mRemainingScannerConnections.remove(client);
        stopServiceIfAppropriate();
    }

    /* package */ synchronized void handleFinishImportNotification(
            int jobId, boolean successful) {
        if (DEBUG) {
            Log.d(LOG_TAG, String.format("Received vCard import finish notification (id: %d). "
                    + "Result: %b", jobId, (successful ? "success" : "failure")));
        }
        if (mRunningJobMap.remove(jobId) == null) {
            Log.w(LOG_TAG, String.format("Tried to remove unknown job (id: %d)", jobId));
        }
        stopServiceIfAppropriate();
    }

    /* package */ synchronized void handleFinishExportNotification(
            int jobId, boolean successful) {
        if (DEBUG) {
            Log.d(LOG_TAG, String.format("Received vCard export finish notification (id: %d). "
                    + "Result: %b", jobId, (successful ? "success" : "failure")));
        }
        final ProcessorBase job = mRunningJobMap.remove(jobId);
        if (job == null) {
            Log.w(LOG_TAG, String.format("Tried to remove unknown job (id: %d)", jobId));
        } else if (!(job instanceof ExportProcessor)) {
            Log.w(LOG_TAG,
                    String.format("Removed job (id: %s) isn't ExportProcessor", jobId));
        } else {
            final String path = ((ExportProcessor)job).getRequest().destUri.getEncodedPath();
            if (DEBUG) Log.d(LOG_TAG, "Remove reserved path " + path);
            mReservedDestination.remove(path);
        }
        mlistener.onExportFinished(null,0,null);//aurora add zhouxiaobing 20131205
        stopServiceIfAppropriate();
        mIsstart=false;//aurora add zhouxiaobing 20131219
    }

    /**
     * Cancels all the import/export requests and calls {@link ExecutorService#shutdown()}, which
     * means this Service becomes no longer ready for import/export requests.
     *
     * Mainly called from onDestroy().
     */
    private synchronized void cancelAllRequestsAndShutdown() {
        for (final Map.Entry<Integer, ProcessorBase> entry : mRunningJobMap.entrySet()) {
            entry.getValue().cancel(true);
        }
        mRunningJobMap.clear();
        //mExecutorService.shutdown();
    }

    /**
     * Removes import caches stored locally.
     */
    private void clearCache() {
        for (final String fileName : fileList()) {
            if (fileName.startsWith(CACHE_FILE_PREFIX)) {
                // We don't want to keep all the caches so we remove cache files old enough.
                Log.i(LOG_TAG, "Remove a temporary file: " + fileName);
                deleteFile(fileName);
            }
        }
    }

    /**
     * Returns an appropriate file name for vCard export. Returns null when impossible.
     *
     * @return destination path for a vCard file to be exported. null on error and mErrorReason
     * is correctly set.
     */
    private String getAppropriateDestination(final String destDirectory) {
        /*
         * Here, file names have 5 parts: directory, prefix, index, suffix, and extension.
         * e.g. "/mnt/sdcard/prfx00001sfx.vcf" -> "/mnt/sdcard", "prfx", "00001", "sfx", and ".vcf"
         *      (In default, prefix and suffix is empty, so usually the destination would be
         *       /mnt/sdcard/00001.vcf.)
         *
         * This method increments "index" part from 1 to maximum, and checks whether any file name
         * following naming rule is available. If there's no file named /mnt/sdcard/00001.vcf, the
         * name will be returned to a caller. If there are 00001.vcf 00002.vcf, 00003.vcf is
         * returned.
         *
         * There may not be any appropriate file name. If there are 99999 vCard files in the
         * storage, for example, there's no appropriate name, so this method returns
         * null.
         */

        // Count the number of digits of mFileIndexMaximum
        // e.g. When mFileIndexMaximum is 99999, fileIndexDigit becomes 5, as we will count the
        // Gionee baorui 2012-04-26 add for CR00582516 begin
        if (true == ContactsUtils.mIsGnContactsSupport) {
            String bodyFormat = "%s%s%s";
            boolean numberIsAvailable = true;
            String body = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            String mFileName = sdf.format(new Date());

            for (String possibleExtension : mExtensionsToConsider) {
                body = String.format(bodyFormat, mFileNamePrefix, mFileName, mFileNameSuffix);
                File file = new File(String.format("%s/%s.%s", destDirectory, body,
                        possibleExtension));
                if (file.exists()) {
                    numberIsAvailable = false;
                    break;
                }
            }

            if (true == numberIsAvailable) {
                // Gionee:wangth 20120616 add for CR00624473 begin
                ContactImportExportActivity.mSaveTime = body;
                // Gionee:wangth 20120616 add for CR00624473 end
                return String.format("%s/%s.%s", destDirectory, body, mFileNameExtension);
            }
        }
        // Gionee baorui 2012-04-26 add for CR00582516 end
        int fileIndexDigit = 0;
        {
            // Calling Math.Log10() is costly.
            int tmp;
            for (fileIndexDigit = 0, tmp = mFileIndexMaximum; tmp > 0;
                fileIndexDigit++, tmp /= 10) {
            }
        }

        // %s05d%s (e.g. "p00001s")
        final String bodyFormat = "%s%0" + fileIndexDigit + "d%s";

        if (!ALLOW_LONG_FILE_NAME) {
            final String possibleBody =
                    String.format(bodyFormat, mFileNamePrefix, 1, mFileNameSuffix);
            if (possibleBody.length() > 8 || mFileNameExtension.length() > 3) {
                Log.e(LOG_TAG, "This code does not allow any long file name.");
                mErrorReason = getString(R.string.fail_reason_too_long_filename,
                        String.format("%s.%s", possibleBody, mFileNameExtension));
                Log.w(LOG_TAG, "File name becomes too long.");
                return null;
            }
        }

        for (int i = mFileIndexMinimum; i <= mFileIndexMaximum; i++) {
            boolean numberIsAvailable = true;
            String body = null;
            for (String possibleExtension : mExtensionsToConsider) {
                body = String.format(bodyFormat, mFileNamePrefix, i, mFileNameSuffix);
                final String path =
                        String.format("%s/%s.%s", destDirectory, body, possibleExtension);
                synchronized (this) {
                    if (mReservedDestination.contains(path)) {
                        if (DEBUG) {
                            Log.d(LOG_TAG, String.format("The path %s is reserved.", path));
                        }
                        numberIsAvailable = false;
                        break;
                    }
                }
                final File file = new File(path);
                if (file.exists()) {
                    numberIsAvailable = false;
                    break;
                }
            }
            if (numberIsAvailable) {
                return String.format("%s/%s.%s", destDirectory, body, mFileNameExtension);
            }
        }

        Log.w(LOG_TAG, "Reached vCard number limit. Maybe there are too many vCard in the storage");
        mErrorReason = getString(R.string.fail_reason_too_many_vcard);
        return null;
    }

    // The following lines are provided and maintained by Mediatek Inc.
    private String mQuerySelection = null;

    public void setQuerySelection(String selection) {
        mQuerySelection = selection;
    }

    public String getQuerySelection() {
        return mQuerySelection;
    }
    private StorageManager mSM;
    // The previous lines are provided and maintained by Mediatek Inc.
    
    //aurora add liguangyu 20140918 for phb start
    private static final String AURORA_ACTION_VCARD_LOAD_START = "com.android.contacts.ACTION_VCARD_LOAD_START";
    private static final String AURORA_ACTION_VCARD_LOAD_END = "com.android.contacts.ACTION_VCARD_LOAD_END";
    private void sendVcardLoadStart() {
        Intent intent = new Intent(AURORA_ACTION_VCARD_LOAD_START);
        sendBroadcast(intent);
    }
    
    private void sendVcardLoadEnd() {
        Intent intent = new Intent(AURORA_ACTION_VCARD_LOAD_END);
        sendBroadcast(intent);
    }
    //aurora add liguangyu 20140918 for phb end
}

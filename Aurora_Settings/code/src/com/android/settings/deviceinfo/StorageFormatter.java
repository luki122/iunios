package com.android.settings.deviceinfo;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

//import com.android.internal.R;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.google.android.collect.Lists;

/**
 * Takes care of unmounting and formatting external storage.
 */
public class StorageFormatter extends Service
        implements DialogInterface.OnCancelListener {
    static final String TAG = "StorageFormatter";

    public static final String FORMAT_ONLY = "com.android.internal.os.storage.FORMAT_ONLY";
    public static final String FORMAT_AND_FACTORY_RESET = "com.android.internal.os.storage.FORMAT_AND_FACTORY_RESET";

    public static final String EXTRA_ALWAYS_RESET = "always_reset";
    public static final String KEY_ERASE_FLAG = "erase_flag";

    // If non-null, the volume to format. Otherwise, will use the default external storage directory
    private StorageVolume mStorageVolume;

    public static final ComponentName COMPONENT_NAME
            = new ComponentName("android", StorageFormatter.class.getName());

    // Access using getMountService()
    private IMountService mMountService = null;

    private StorageManager mStorageManager = null;

    private PowerManager.WakeLock mWakeLock;

    private ProgressDialog mProgressDialog = null;

    private boolean mFactoryReset = false;
    private boolean mAlwaysReset = false;
    private String eraseFlag;

    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "Received storage state changed notification that " +
                    path + " changed state from " + oldState +
                    " to " + newState);
            updateProgressState();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }

        mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StorageFormatter");
        mWakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (FORMAT_AND_FACTORY_RESET.equals(intent.getAction())) {
            mFactoryReset = true;
        }
        if (intent.getBooleanExtra(EXTRA_ALWAYS_RESET, false)) {
            mAlwaysReset = true;
        }

        mStorageVolume = intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
        eraseFlag = intent.getStringExtra(KEY_ERASE_FLAG);
//        if(null != mStorageVolume) {
        	android.util.Log.e("hanping", "mStorageVolume->" + mStorageVolume);
//        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            if (!mAlwaysReset) {
                mProgressDialog.setOnCancelListener(this);
            }
            updateProgressState();
            mProgressDialog.show();
        }

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (mStorageManager != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCancel(DialogInterface dialog) {

        IMountService mountService = getMountService();
        try {
            final StorageVolume[] volumes = mountService.getVolumeList();
            final ArrayList<StorageVolume> physicalVols = getPhysicalExternalVolume(volumes);
            String extStoragePath = null;
            // find external storage path if storage volume not specified
            if (mStorageVolume == null) {
                if (physicalVols.size() == 0) {
                        updateProgressDialog(R.string.progress_nomediapresent);
                } else {
                        final StorageVolume physicalVol = physicalVols.get(0);
                        extStoragePath = physicalVol.toString();
                        mountService.mountVolume(extStoragePath);
                }
            }
            //else use the specified storage volume
            else {
                extStoragePath = mStorageVolume.getPath();
                mountService.mountVolume(extStoragePath);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed talking with mount service", e);
        }
        stopSelf();
    }

    void fail(int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        if (mAlwaysReset) {
            sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
        }
        stopSelf();
    }

    public static ArrayList<StorageVolume> getPhysicalExternalVolume(StorageVolume[] volumesphy) {
        int count = volumesphy.length;
        ArrayList<StorageVolume> volumes = Lists.newArrayList();
        for (int i=0; i < count ; i++) {
            if (!volumesphy[i].isEmulated()) {
                volumes.add(volumesphy[i]);
            }
        }
        return volumes;
    }

    void updateProgressState() {
        String status = null;
        String extStoragePath = null;
        StorageVolume physicalVol;
        try {
            final IMountService mountService = getMountService();
            final StorageVolume[] volumes = mountService.getVolumeList();
            final ArrayList<StorageVolume> physicalVols = getPhysicalExternalVolume(volumes);
            // find external storage path if storage volume not specified
            if (mStorageVolume == null) {
                if (physicalVols.size() == 0) {
                    updateProgressDialog(R.string.progress_nomediapresent);
                    return;
                    } else {
                        physicalVol = physicalVols.get(0);
                        status = mStorageManager.getVolumeState(physicalVol.getPath()) ;
                    }
                }
                //else use the specified storage volume
                else {
                        status = mStorageManager.getVolumeState(mStorageVolume.getPath());
                }
        }
        catch (RemoteException e) {
                Log.w(TAG, "Failed talking with mount service", e);
        }
        if (Environment.MEDIA_MOUNTED.equals(status)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
        	if(eraseFlag.equals("sdcard")) {
        		updateProgressDialog(R.string.sd_progress_unmounting);
        	} else if(eraseFlag.equals("otg")) {
        		updateProgressDialog(R.string.otg_progress_unmounting);
        	} else {
        		updateProgressDialog(R.string.in_progress_unmounting);
        	}
            
            try {
                final IMountService mountService = getMountService();
                final StorageVolume[] volumes = mountService.getVolumeList();
                final ArrayList<StorageVolume> physicalVols = getPhysicalExternalVolume(volumes);
                // find external storage path if storage volume not specified
                if (mStorageVolume == null) {
                    if (physicalVols.size() == 0) {
                        updateProgressDialog(R.string.progress_nomediapresent);
                        return;
                    } else {
                        physicalVol = physicalVols.get(0);
                        extStoragePath = physicalVol.getPath();
                        Log.e(TAG, "physicalVol : " + physicalVol.toString());
                        mountService.unmountVolume(extStoragePath, true, mFactoryReset);
                    }
                }
                //else use the specified storage volume
                else {
                        extStoragePath = mStorageVolume.getPath();
                        mountService.unmountVolume(extStoragePath, true, mFactoryReset);
                }
            }
            catch (RemoteException e) {
                Log.w(TAG, "Failed talking with mount service", e);
            }
        }
        else if (Environment.MEDIA_NOFS.equals(status)
                || Environment.MEDIA_UNMOUNTED.equals(status)
                || Environment.MEDIA_UNMOUNTABLE.equals(status)) {
        	if(eraseFlag.equals("sdcard")) {
        		updateProgressDialog(R.string.sd_progress_erasing);
        	} else if(eraseFlag.equals("otg")) {
        		updateProgressDialog(R.string.otg_progress_erasing);
        	} else {
        		updateProgressDialog(R.string.in_progress_erasing);
        	}
//            updateProgressDialog(R.string.progress_erasing);
            final IMountService mountService = getMountService();
            if (mountService != null) {
                new Thread() {
                    @Override
                    public void run() {
                        boolean success = false;
                        StorageVolume physicalVol = null;
                        ArrayList<StorageVolume> physicalVols = null;
                        String extStoragePath = null;
                        try {
                            final StorageVolume[] volumes = mountService.getVolumeList();
                            physicalVols = getPhysicalExternalVolume(volumes);
                            // find external storage path if storage volume not specified
                            if (mStorageVolume == null) {
                                if (physicalVols.size() == 0) {
                                    updateProgressDialog(R.string.progress_nomediapresent);
                                    return;
                                } else {
                                    physicalVol = physicalVols.get(0);
                                    extStoragePath = physicalVol.getPath();
                                }
                            }
                            //else use the specified storage volume
                            else {
                                extStoragePath = mStorageVolume.getPath();
                            }
                            mountService.formatVolume(extStoragePath);
                            success = true;
                        } catch (Exception e) {
                            Toast.makeText(StorageFormatter.this,
                                    R.string.format_error, Toast.LENGTH_LONG).show();
                        }
                        if (success) {
                            if (mFactoryReset) {
                                sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                                // Intent handling is asynchronous -- assume it will happen soon.
                                stopSelf();
                                return;
                            }
                        }
                        // If we didn't succeed, or aren't doing a full factory
                        // reset, then it is time to remount the storage.
                        if (!success && mAlwaysReset) {
                            sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                        } else {
                            try {
                                if(physicalVols.size() == 0) {
                                    updateProgressDialog(R.string.progress_nomediapresent);
                                    return;
                                } else {
                                    physicalVol = physicalVols.get(0);
                                    extStoragePath = mStorageVolume == null ?
                                        physicalVol.getPath() : mStorageVolume.getPath();
                                    mountService.mountVolume(extStoragePath);
                                }
                            } catch (RemoteException e) {
                                Log.w(TAG, "Failed talking with mount service", e);
                            }
                        }
                        stopSelf();
                        return;
                    }
                }.start();
            } else {
                Log.w(TAG, "Unable to locate IMountService");
            }
        } else if (Environment.MEDIA_BAD_REMOVAL.equals(status)) {
            fail(R.string.media_bad_removal);
        } else if (Environment.MEDIA_CHECKING.equals(status)) {
            if(eraseFlag.equals("sdcard")) {
            	fail(R.string.sd_media_checking);
        	} else if(eraseFlag.equals("otg")) {
        		fail(R.string.otg_media_checking);
        	} else {
        		fail(R.string.in_media_checking);
        	}
        } else if (Environment.MEDIA_REMOVED.equals(status)) {
            fail(R.string.media_removed);
        } else if (Environment.MEDIA_SHARED.equals(status)) {
            if(eraseFlag.equals("sdcard")) {
            	fail(R.string.sd_media_shared);
        	} else if(eraseFlag.equals("otg")) {
        		fail(R.string.otg_media_shared);
        	} else {
        		fail(R.string.sd_media_shared);
        	}
        } else {
            fail(R.string.media_unknown_state);
            Log.w(TAG, "Unknown storage state: " + status);
            stopSelf();
        }
    }

    public void updateProgressDialog(int msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mProgressDialog.show();
        }

        mProgressDialog.setMessage(getText(msg));
    }

    IMountService getMountService() {
        if (mMountService == null) {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                mMountService = IMountService.Stub.asInterface(service);
            } else {
                Log.e(TAG, "Can't get mount service");
            }
        }
        return mMountService;
    }
}

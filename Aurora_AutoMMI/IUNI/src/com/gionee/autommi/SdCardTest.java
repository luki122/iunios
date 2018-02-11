package com.gionee.autommi;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.StatFs;

public class SdCardTest extends BaseActivity {
	private StorageManager storageManager;
	public final static String TAG = "SdCardTest";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		storageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] volumes = storageManager.getVolumeList();
		String info = "";
		for(int i = 0; i < volumes.length; i++){
			Log.d(TAG, "----------------");
			String path = volumes[i].getPath();
			String state = storageManager.getVolumeState(path);
			if(path.contains("sdcard")) {
				StatFs stat = new StatFs(path);
				int allVol = (int) (((long)stat.getBlockCount()*stat.getBlockSize())/(1024*1024));
				int avaiableVol = (int)(((long)stat.getAvailableBlocks()*stat.getBlockSize())/(1024*1024));
				info += path + ":" + state + ":" + allVol + ":" + avaiableVol + "|";
			}
			 Log.d(TAG, path + " : " + state);
             Log.d(TAG, ""+volumes[i]);
		}
		info = info.substring(0, info.length()-1);
		((AutoMMI)getApplication()).recordResult(TAG, info, "2");
		Toast.makeText(this, info.replace("|", "\n"), Toast.LENGTH_LONG).show();
		
		/*
		 * if external sd card exist
		 *     /storage/sdcard0 ---->  external sd
		 *     /storage/sdcard1 ---->  internal sd
		 *  else 
		 *     /storage/sdcard0 -----> internal sd
		 *     /storage/sdcard1 -----> removed
		 * */
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
}

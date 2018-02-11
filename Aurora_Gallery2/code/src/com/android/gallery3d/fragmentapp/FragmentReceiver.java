package com.android.gallery3d.fragmentapp;

import java.io.File;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class FragmentReceiver extends BroadcastReceiver{

	private static final String MYIMAGE_CACHE_DIR = "mythumbs";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			//Log.i("zll", "zll ---- FragmentReceiver");
			
			MyClearAsyncTask mTask = new MyClearAsyncTask(context);
	        mTask.execute(100);
		}
	}
	
	private static void onInitCache(Context context) {
		String inCachePath = context.getCacheDir().getPath();
		if(inCachePath != null) {
			File inFile = new File(inCachePath + File.separator + MYIMAGE_CACHE_DIR);
			onDeleteFile(inFile);
		}
		
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			String outCachePath = context.getExternalCacheDir().getPath();
			if (outCachePath != null) {
				File inFile = new File(outCachePath + File.separator + MYIMAGE_CACHE_DIR);
				onDeleteFile(inFile);
			}
		}
		
		return;
	}

	private static void onDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            
            for(File f : childFile){
            	onDeleteFile(f);
            }
            file.delete();
        }
    }

	private class MyClearAsyncTask extends AsyncTask<Integer, Integer, String>{
		private Context mContext;
		
		public MyClearAsyncTask(Context c) {
			super();
			this.mContext = c;
		}

		@Override
		protected String doInBackground(Integer... params) {
			
			try {
				Log.i("zll", "zll ---- FragmentReceiver doInBackground");
				onInitCache(mContext);
			} catch (Exception e) {
				
			}
			return null;
		}
		
	}
	
}

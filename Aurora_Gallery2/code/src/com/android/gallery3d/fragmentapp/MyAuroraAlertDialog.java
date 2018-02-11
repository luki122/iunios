package com.android.gallery3d.fragmentapp;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.R.integer;
//import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import aurora.app.AuroraAlertDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.MenuItem;
import com.android.gallery3d.fragmentdata.GalleryItem;
import com.android.gallery3d.R;
import aurora.app.AuroraProgressDialog;



public class MyAuroraAlertDialog {

	private boolean mWaitOnStop;
	private AuroraProgressDialog mDialog;
	private Context mContext;
	MyAsyncTask mTask;
	private HashMap<Long, Boolean> mSelectMap;
	private List<GalleryItem> mList;
	
	private static final int MSG_TASK_COMPLETE = 1;
    private static final int MSG_TASK_UPDATE = 2;
    private static final int MSG_TASK_START = 3;
    private static final int MSG_DO_SHARE = 4;
    
    public static final int EXECUTION_RESULT_SUCCESS = 1;
    public static final int EXECUTION_RESULT_FAIL = 2;
    public static final int EXECUTION_RESULT_CANCEL = 3;
    
    private final static int MY_IMAGE_TYPE = 1;
	private final static int MY_VIDEO_TYOE = MY_IMAGE_TYPE+1;
	
	public interface MyProgressListener {
        public void onConfirmDialogShown();
        public void onConfirmDialogDismissed(boolean confirmed);
        public void onProgressStart();
        public void onProgressUpdate(int index);
        public void onProgressComplete(int result, int num);
    }
	
	public MyAuroraAlertDialog(Context context, HashMap<Long, Boolean> map, List<GalleryItem> tList) {
		super();
		
		mContext = context;
		if (mSelectMap != null) {
			mSelectMap.clear();
		}
		mSelectMap = new HashMap<Long, Boolean>();
		mSelectMap = map;
		mList = tList;
	}
	
	private class MyAsyncTask extends AsyncTask<Integer, Integer, String>{

		private AuroraProgressDialog progressBar;
		private MyProgressListener mListener;
		private boolean m_running;
		private int m_runresult;
		private int delete_num = 0;
		
		
		public MyAsyncTask(AuroraProgressDialog progressBar, MyProgressListener listener) {
			super();
			this.progressBar = progressBar;
			this.mListener = listener;
			m_running = true;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			m_running = false;
			m_runresult = EXECUTION_RESULT_SUCCESS;
		}

		@Override
		protected void onPostExecute(String result) {//finish
			super.onPostExecute(result);
			//Log.i("zll", "zll ---- onPostExecute ");
			progressBar.dismiss();
			if (mListener != null) {
				mListener.onProgressComplete(m_runresult, delete_num);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			if (mListener != null) {
				mListener.onProgressStart();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			//Log.i("zll", "zll ---- onProgressUpdate values[0]:"+values[0]);
			if (mListener != null) {
				mListener.onProgressUpdate(values[0]);
			}
		}

		@Override
		protected String doInBackground(Integer... params) {
			
			if ((mSelectMap == null) || (mSelectMap.size() <= 0)) {
				m_runresult = EXECUTION_RESULT_SUCCESS;
				return null;
			}
			
			int count = 0;
			Iterator iter = mSelectMap.entrySet().iterator();
			while (iter.hasNext()){
				if (!m_running) {
					break;
				}
				
				Map.Entry entry = (Map.Entry) iter.next();
				//Log.i(TAG, "zll --- delte key 22222");
				long key = Long.valueOf(String.valueOf(entry.getKey()));
				boolean value = Boolean.parseBoolean(entry.getValue().toString());
				if (!value) {
					continue;
				}
				
				//mSelectMap.put(key, false);
				//Log.i(TAG, "zll --- delte 1 key:"+key+",value:"+value+",pos:"+pos);
				try {
					Mydelete(key, getMyImgTypeFromID(key));
				} catch (Exception e) {
					m_runresult = EXECUTION_RESULT_FAIL;
					e.printStackTrace();
				}
				
				count++;
				delete_num = count;
				/*if(length > 0) {
                    // 如果知道响应的长度，调用publishProgress（）更新进度
                    publishProgress((int) ((count / (float) length) * 100));
                }*/
				publishProgress(count);
			}
		
			m_runresult = EXECUTION_RESULT_SUCCESS;
			return null;
		}
		
		private void Mydelete(Long id, int type) {  
			int num = 0;
			if (type == MY_IMAGE_TYPE) {
				num = mContext.getContentResolver().delete(Images.Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{String.valueOf(id)});
			}
			else {
				num = mContext.getContentResolver().delete(Video.Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{String.valueOf(id)});
			}
			
			//Log.i("zll", "zll ---- delete id:"+id+",num:"+num+",type:"+type);
			
			return;
		}
		
		private int getMyImgTypeFromID(Long id) {

			for (int i = 0; i < mList.size(); i++) {
				GalleryItem item = mList.get(i);
				if (item.getId() == id) {
					return item.getType();
				}
			}
			
			return -1;
		}
	}
	
	private class ConfirmDialogListener implements OnClickListener, OnCancelListener {
        private final int mActionId;
        private final MyProgressListener mListener;

        public ConfirmDialogListener(int actionId, MyProgressListener listener) {
            mActionId = actionId;
            mListener = listener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (mListener != null) {
                    mListener.onConfirmDialogDismissed(true);
                }
                onMyMenuClicked(mActionId, mListener);
            } else {
                if (mListener != null) {
                    mListener.onConfirmDialogDismissed(false);
                }
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (mListener != null) {
                mListener.onConfirmDialogDismissed(false);
            }
        }
    }
	
	private void onMyMenuClicked(int action, MyProgressListener listener) {
		onMyMenuClicked(action, listener, false, true);
    }
	
	public void onMyMenuClicked(int action, MyProgressListener listener,
            boolean waitOnStop, boolean showDialog) {
        int title;
        
        //Log.i("zll", "zll ---- onMyMenuClicked action :"+action);
        switch (action) {
            case R.id.action_delete:
                title = R.string.delete;
                break;
                
            case R.id.menu_delete:
                title = R.string.delete;
                break;
                
            default:
                return;
        }
        
        //Log.i("zll", "zll ---- onMyMenuClicked action :"+action);
        startAction(action, title, listener, waitOnStop, showDialog);
    }
	
	private static AuroraProgressDialog createProgressDialog(Context context, int titleId, int progressMax) {
        AuroraProgressDialog dialog = new AuroraProgressDialog(context);
        dialog.setTitle(titleId);
        dialog.setMax(progressMax);
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        if (progressMax > 1) {
            //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        
        return dialog;
    }
	
	public void startAction(int action, int title, MyProgressListener listener,
            boolean waitOnStop, boolean showDialog) {
        //ArrayList<Path> ids = mSelectionManager.getSelected(false);
        stopTaskAndDismissDialog();

        if (mSelectMap == null) {
			return;
		}
        
        int tSize = mSelectMap.size();
        mDialog = createProgressDialog(mContext, title, tSize);
        if (showDialog) {
            mDialog.show();
        }
        
        mTask = null;
        mTask = new MyAsyncTask(mDialog, listener);
        mTask.execute(100);
        //MediaOperation operation = new MediaOperation(action, ids, listener);
        //mTask = mActivity.getThreadPool().submit(operation, null);
        mWaitOnStop = waitOnStop;
    }
	
	public void stopTaskAndDismissDialog() {
		//Log.i("ddd", "zll --- stopTaskAndDismissDialog 1");
        if (mTask != null) {
        	//Log.i("ddd", "zll --- stopTaskAndDismissDialog 2");
            //if (!mWaitOnStop) 
        	mTask.cancel(true);
            mWaitOnStop = true;
            //mTask.waitDone();
            mDialog.dismiss();
            mDialog = null;
            mTask = null;
        }
    }
	
	public void onMenuClicked(int menuId, String confirmMsg, MyProgressListener listener) {
		
        final int action = menuId;
        if (confirmMsg != null) {
            if (listener != null) listener.onConfirmDialogShown();
            ConfirmDialogListener cdl = new ConfirmDialogListener(action, listener);
            new AuroraAlertDialog.Builder(mContext)
                    .setMessage(confirmMsg)
                    .setOnCancelListener(cdl)
                    .setPositiveButton(R.string.ok, cdl)
                    .setNegativeButton(R.string.cancel, cdl)
                    .create().show();
        } else {
            onMyMenuClicked(action, listener);
        }
    }
	
}

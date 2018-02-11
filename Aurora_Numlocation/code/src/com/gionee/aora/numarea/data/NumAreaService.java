package com.gionee.aora.numarea.data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.base.log.JLog;
import com.gionee.aora.numarea.R;
import com.gionee.aora.numarea.export.INumAreaManager;
import com.gionee.aora.numarea.export.INumAreaObserver;
import com.gionee.aora.numarea.export.IUpdataResult;
import com.gionee.aora.numarea.export.NumAreaInfo;
import com.gionee.aora.numarea.util.ALLSTATE;

public class NumAreaService extends Service 
{
	/**
	 * À´µç¹éÊôµØ¹ÜÀíÀà
	 */
	private NumAreaManager iManager;
	/**
	 * ÊÇ·ñ³õÊ¼»¯Íê±Ï
	 */
	private boolean isReady;
	/**
	 * À´µçÈ¥µçµ¯³öµÄ´°¿Ú
	 */
	static WindowManager windowManager_up_box;
	/**
	 * ÏÔÊ¾ºÅÂëÐÅÏ¢
	 */
	static TextView NumberArea_TextView;
	/**
	 * »ñµÃµÄºÅÂë
	 */
	private String phoneNumber_Info;
	private String phoneNumber;
	/**
	 * ÏßÐÔ²¼¾Ö
	 */
	LinearLayout layout;
	/**
	 * Ìî³ä
	 */
	LayoutInflater inflater = null;
	/**
	 * ±ê¼Ç
	 */
	private boolean tag = false;
	/**
	 * µ¯³ö¿òview
	 */
	View view = null;
	/**
	 * ´òÓ¡LOGµÄ¹¤¾ß
	 */
//	final static public JLog LOG = new JLog("NUM_AREA", ALLSTATE.LOG_STATE);

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		isReady = false;
		iManager = new NumAreaManager(getBaseContext());
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				iManager.initData();
				isReady = true;
//				LOG.print("Init iManager-------------->57");
			}
		}).start();
	}



	

	class ManagerView extends View 
	{

		public ManagerView(Context context)  
		{
			super(context);
			// TODO Auto-generated constructor stub
		}

	}

	
	

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return iNumAreaManager;
	}

	final private INumAreaManager.Stub iNumAreaManager = new INumAreaManager.Stub()
	{

		/**
		 * ÊÇ·ñ³õÊ¼»¯Íê±Ï
		 * 
		 * @return
		 */
		private boolean checkIsReady()
		{
			if (!isReady)
			{
//				LOG.print("³õÊ¼»¯Ê§°ÜÒì³£Å×³öNumAreaService");
				throw new IllegalArgumentException(getString(R.string.error_init_not_finish));
			}
			else
				return isReady;
		}

		/**
		 * Êý¾Ý¿âÉý¼¶
		 */
		@Override
		public void updataDB(INumAreaObserver aObserver) throws RemoteException
		{
			// TODO Auto-generated method stub
			if (checkIsReady()) iManager.updataDB(aObserver);
		}

		/**
		 * ÊäÈëºÅÂë²éÕÒµØÇø
		 */
		@Override
		public NumAreaInfo getNumAreaInfo(String aPhoneNum) throws RemoteException
		{
            /*gionee tianyr modify  for CR00715850 start*/
            if (aPhoneNum != null) {
                if (checkIsReady())
                    return iManager.getNumAreaInfo(aPhoneNum);
                else
                    return null;
            }

            return null;
            
            /*gionee tianyr modify  for CR00715850 end*/
		}


		@Override
		public void cancelUpdata() throws RemoteException
		{
			// TODO Auto-generated method stub
			if (checkIsReady()) iManager.cancelUpdata();
		}

		@Override
		public boolean registObserver(INumAreaObserver aObserver) throws RemoteException
		{
			// TODO Auto-generated method stub
			boolean result = iManager.registObserver(aObserver);
			if (isReady) iManager.notifyObserver(IUpdataResult.RESULT_INIT_FINISH);
			else
				iManager.notifyObserver(IUpdataResult.RESULT_INIT_PROCESSING);
			return result;
		}

		@Override
		public boolean unregistObserver(INumAreaObserver aObserver) throws RemoteException
		{
			// TODO Auto-generated method stub
			return iManager.unRegistObserver(aObserver);
		}

		/**
		 * ÊäÈë×Ö·û´®²éÕÒºÅÂë
		 */
		@Override
		public NumAreaInfo[] getAreaNumInfo(String aArea , String aTag) throws RemoteException 
		{
			if (checkIsReady()) return iManager.getAreaNumInfo(aArea, aTag);
			else
				return null;
		}

		/**
		 * µÃµ½³£ÓÃÐÅÏ¢ÁÐ±í
		 */
		@Override
		public NumAreaInfo[] getComAreaNumInfo() throws RemoteException
		{
			// TODO Auto-generated method stub
			if (checkIsReady()) return iManager.getComAreaNumInfo();
			else
				return null;
		}
/*		*//**
		 * µÃµ½ÔËÓªÉÌÐÅÏ¢
		 *//*
		@Override
		public MNO_IP_Info[] getMNOInfo() throws RemoteException
		{
			// TODO Auto-generated method stub
			if (checkIsReady()) return iManager.getMNOInfo();
			return null;
		}*/

	};
	@Override
	public boolean onUnbind(Intent intent)
	{
		// TODO Auto-generated method stub
		System.out.println("service is onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
	//	unregisterReceiver(batInfoReceiver);
		System.out.println("service is destory");
		super.onDestroy();
	}
}

/*package com.gionee.aora.numarea;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gionee.aora.numarea.data.NumAreaService;
import com.gionee.aora.numarea.export.INumAreaManager;
import com.gionee.aora.numarea.export.INumAreaObserver;
import com.gionee.aora.numarea.export.IUpdataResult;
*//**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file TestActivity.java
 * 摘要:
 *
 * @author yewei
 * @data 2011-5-20
 * @version 
 *
 *//*
public class TestActivity2 extends Activity implements OnClickListener{
	private INumAreaManager iManager;
	
	final static private String ACTION_NUMAREA = "gionee.aora.numarea";
	
	private Button iButton_Test,iButton_Search,iButton_Updata,iButton_Import,iButton_Distory;
	
	private EditText iEdit_Search;
	
	private TextView iTextView_Result;
	
	final static public int DIALOG_WAIT_FOR_INIT = IUpdataResult.RESULT_INIT_PROCESSING;
	
	final static public int DIALOG_WAIT_FOR_CONNECTING = 2 << 10;
	
	private AlertDialog iAlertDialog;
	
	private ProgressDialog iProgressDialog;
	
    *//** Called when the activity is first created. *//*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testmain);
        System.out.println("onCreate******************************");
        iButton_Search = (Button) findViewById(R.id.button_search);
        iButton_Search.setOnClickListener(this);
        iButton_Test = (Button) findViewById(R.id.button_test);
        iButton_Test.setOnClickListener(this);
        iButton_Updata = (Button) findViewById(R.id.button_updata);
        iButton_Updata.setOnClickListener(this);
        iButton_Import = (Button) findViewById(R.id.button_import_test);
        iButton_Import.setOnClickListener(this);
        iButton_Distory=(Button)findViewById(R.id.distory_myself);
        iButton_Distory.setOnClickListener(this);
        iEdit_Search = (EditText) findViewById(R.id.editText_search);
        iTextView_Result = (TextView) findViewById(R.id.textView_result);
        Intent startIntent = new Intent(this, NumAreaService.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(startIntent);
		System.out.println("onStart******************************");
        new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				System.out.print("onStart");
				// TODO Auto-generated method stub
		        Intent intent = new Intent(TestActivity2.this,com.gionee.aora.numarea.data.NumAreaService.class);
		        bindService(intent, iConnection, Context.BIND_AUTO_CREATE);

			}
		}).start();
//        manager.getNumAreaInfo("13510297738");
    }
    
    
    
     (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		
        
	}
	
	

	 (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 
	@Override
	protected void onPrepareDialog(int id , Dialog dialog)
	{
		// TODO Auto-generated method stub
//		BaseLog.print("onPrepareDialog="+dialog);
		if(dialog instanceof ProgressDialog)
		{
			System.out.println("--------------------124");
			iProgressDialog = (ProgressDialog) dialog;
			iProgressDialog.setIndeterminate(true);
		}
		else if(dialog instanceof AlertDialog)
		{
			System.out.println("--------------------130");
			iAlertDialog = (AlertDialog) dialog;
		}
		super.onPrepareDialog(id, dialog);
	}



	 (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 
	@Override
	protected Dialog onCreateDialog(int id)
	{
		// TODO Auto-generated method stub
//		BaseLog.print("create dialog = " + id);
		System.out.println("--------------------146");
		String message = "";
		if(iProgressDialog != null && iProgressDialog.isShowing()){
			iProgressDialog.dismiss();
			System.out.println("--------------------150");
			}
		if(id == DIALOG_WAIT_FOR_CONNECTING || id == DIALOG_WAIT_FOR_INIT)
		{
			System.out.println("--------------------154");
			iProgressDialog = new ProgressDialog(this);		
			iProgressDialog.setCancelable(false);
			iProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			switch(id)
			{
				case DIALOG_WAIT_FOR_INIT:
					message = getString(R.string.wait_init);
					System.out.println("--------------------162");
					break;
				case DIALOG_WAIT_FOR_CONNECTING:
					System.out.println("--------------------165");
					message = getString(R.string.wait_connecting);
					iProgressDialog.setButton("取消", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialoginterface , int i)
						{
							// TODO Auto-generated method stub
							try
							{
								System.out.println("--------------------175");
								iManager.cancelUpdata();
							}
							catch(RemoteException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});

					break;
			}
			iProgressDialog.setMessage(message);
			return iProgressDialog;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id)
		
		{
		
			case IUpdataResult.RESULT_ERROR_CONNECT_FAILD:
				builder.setMessage(R.string.error_connect_faild);
				break;
			case IUpdataResult.RESULT_ERROR_CONNECT_TIMEOUT:
				builder.setMessage(R.string.error_connect_timeout);
				break;
			case IUpdataResult.RESULT_ERROR_PARSE_DB_FAILD:
				builder.setMessage(R.string.error_parse_db);
				break;
			case IUpdataResult.RESULT_ERROR_UPDATA_PROCESSING:
				builder.setMessage(R.string.error_updata_processing);
				break;
			case IUpdataResult.RESULT_INIT_FINISH:
				System.out.println("--------------------210");
				builder.setMessage(R.string.init_finish);
				break;
			case IUpdataResult.RESULT_SUCCESS:
				System.out.println("--------------------214");
				builder.setMessage(R.string.success_connect);
				break;
			case IUpdataResult.RESULT_USER_CANCEL_UPDATA:
				builder.setMessage(R.string.user_cancel_updata);
				break;
			case IUpdataResult.RESULT_DB_IS_LAST_VERSION:
				builder.setMessage(R.string.db_is_last_version);
				break;
		}
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog , int which)
			{
				// TODO Auto-generated method stub
				System.out.println("--------------------230");
				dialog.dismiss();
			}
		});
		iAlertDialog = builder.create();
		return iAlertDialog;
	}

	private Handler iHandler = new Handler()
	{

		 (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 
		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
//			BaseLog.print("handleMessage=" + msg.what);
			if(!isFinishing())
			{
				if(msg.what == IUpdataResult.RESULT_SUCCESS){
					System.out.println("--------------------252");
					showDialog(DIALOG_WAIT_FOR_INIT);
				}
				else{
					System.out.println("--------------------256");
					showDialog(msg.what);
				}
			}
//			super.handleMessage(msg);
		}
		
	};

	private INumAreaObserver.Stub iNumAreaObserver = new INumAreaObserver.Stub()
	{
		
		@Override
		public void updata(int aResultCode) throws RemoteException
		{
			// TODO Auto-generated method stub
			System.out.println("--------------------272");
			switch(aResultCode)
			{
				case IUpdataResult.RESULT_INIT_FINISH:
				case IUpdataResult.RESULT_ERROR_CONNECT_FAILD:
				case IUpdataResult.RESULT_ERROR_CONNECT_TIMEOUT:
				case IUpdataResult.RESULT_SUCCESS:
				case IUpdataResult.RESULT_DB_IS_LAST_VERSION:
//					BaseLog.print("cancel dialog = " + aResultCode + "," + iProgressDialog);
					if(iProgressDialog != null)
						iProgressDialog.dismiss();
					break;
			}
			iHandler.sendEmptyMessage(aResultCode);
		}
	};

	private ServiceConnection iConnection = new ServiceConnection()
	{
		
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			// TODO Auto-generated method stub
			try
			{
				iManager.registObserver(iNumAreaObserver);
			}
			catch(RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			iManager = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name , IBinder service)
		{
			// TODO Auto-generated method stub
			iManager = INumAreaManager.Stub.asInterface(service);
			try
			{
				iManager.registObserver(iNumAreaObserver);
			}
			catch(RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}};
    
    private void testMobile() throws Exception
    {
    	System.out.println("--------------------330");
    	iManager.getNumAreaInfo("1359018");
    	iManager.getNumAreaInfo("13510297738");
    	iManager.getNumAreaInfo("+8613510297738");
    	iManager.getNumAreaInfo("1759813510297738");
    }
    
    private void testLocalPhoneNum() throws Exception
    {
    	iManager.getNumAreaInfo("+86021666666");
    	iManager.getNumAreaInfo("+860217777777");
    	iManager.getNumAreaInfo("+8602188888888");
    	iManager.getNumAreaInfo("+86021999999999");
    	
    	iManager.getNumAreaInfo("+860755666666");
    	iManager.getNumAreaInfo("+8607557777777");
    	iManager.getNumAreaInfo("+86075588888888");
    	iManager.getNumAreaInfo("+860755999999999");
    	
    	iManager.getNumAreaInfo("021666666");
    	iManager.getNumAreaInfo("0217777777");
    	iManager.getNumAreaInfo("02188888888");
    	iManager.getNumAreaInfo("021999999999");
    	
    	iManager.getNumAreaInfo("0755666666");
    	iManager.getNumAreaInfo("07557777777");
    	iManager.getNumAreaInfo("075588888888");
    	iManager.getNumAreaInfo("0755999999999");    
    	
    	iManager.getNumAreaInfo("17951021666666");
    	iManager.getNumAreaInfo("179510217777777");
    	iManager.getNumAreaInfo("1795102188888888");
    	iManager.getNumAreaInfo("17951021999999999");
    	
    	iManager.getNumAreaInfo("179510755666666");
    	iManager.getNumAreaInfo("1795107557777777");
    	iManager.getNumAreaInfo("17951075588888888");
    	iManager.getNumAreaInfo("179510755999999999");
    	
    	iManager.getNumAreaInfo("021");
    	iManager.getNumAreaInfo("0755");
    	iManager.getNumAreaInfo("028");
    	iManager.getNumAreaInfo("0731");
    	
    }
    
    private void testInternationalPhoneNum() throws Exception
    {
    	iManager.getNumAreaInfo("001666666");
    	iManager.getNumAreaInfo("0093666666");
    	iManager.getNumAreaInfo("00244666666");
    	iManager.getNumAreaInfo("001264666666");
    	iManager.getNumAreaInfo("0012345666666");
    	
    	iManager.getNumAreaInfo("17951001666666");
    	iManager.getNumAreaInfo("179510093666666");
    	iManager.getNumAreaInfo("1795100244666666");
    	iManager.getNumAreaInfo("17951001264666666");
    	iManager.getNumAreaInfo("179510012345666666");
    	
    	iManager.getNumAreaInfo("001");
    	iManager.getNumAreaInfo("0093");
    	iManager.getNumAreaInfo("00355");
    	iManager.getNumAreaInfo("001264");
    }
    
    private void testCommonNum() throws Exception
    {
    	iManager.getNumAreaInfo("110");
    	iManager.getNumAreaInfo("10086");
    	iManager.getNumAreaInfo("4008123123");
    }



	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.button_search:
				try
				{
					System.out.println("--------------------414");
					iTextView_Result.setText(iManager.getNumAreaInfo(iEdit_Search.getText().toString()).toString());
				}
				catch(Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v("aora_numarea", "查到的数据为空");
				}
				break;
			case R.id.button_test:
		        try
				{
					testMobile();
					testLocalPhoneNum();
			        testInternationalPhoneNum();
			        testCommonNum();
				}
				catch(Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.button_updata:
				try
				{
					iManager.updataDB(iNumAreaObserver);
				}
				catch(RemoteException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				showDialog(DIALOG_WAIT_FOR_CONNECTING);
				break;
			case R.id.button_import_test:
				import_test();
				
				case R.id.distory_myself:
					//unbindService(iConnection);
					Intent intent = new Intent();
					intent.setClass(TestActivity2.this, NumAreaService.class);
					stopService(intent);
					System.out.println("我在杀自己");
					try
					{
						throw new DeadObjectException();
					}
					catch(Exception e)
					{
						// TODO Auto-generated catch block
						System.out.println("throw is DeadObjectException");
						e.printStackTrace();
					}
				break;
		}
	}
	
	private void import_test()
	{
		File file = new File(Environment.getExternalStorageDirectory().toString() + "/aora_numarea.txt");
//		BaseLog.print(file.toString());
		if(file.exists())
		{
			try
			{
				DataInputStream bis = new DataInputStream(new FileInputStream(file));
				String value = null;
				while((value = bis.readLine()) != null)
				{
					if(!value.equals(""))
						Log.v("aora_numarea", "num=" + value + ",info=" + iManager.getNumAreaInfo(value));
				}
			}
			catch(FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	



	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		
		super.onPause();
		System.out.println("onPause***********************");
	}



	@Override
	protected void onRestart()
	{
		// TODO Auto-generated method stub
		super.onRestart();
		System.out.println("onRestart***********************");
	}



	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("onResume***********************");
	}



	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		super.onStop();
		System.out.println("onStop***********************");
	}
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		//unbindService(iConnection);
		System.out.println("onDestroy***********************");
	}
}*/
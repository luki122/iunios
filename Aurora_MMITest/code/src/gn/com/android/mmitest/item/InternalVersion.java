package gn.com.android.mmitest.item;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import gn.com.android.mmitest.Model;
import gn.com.android.mmitest.R;
import java.io.IOException;
import java.text.SimpleDateFormat;
import android.os.SystemProperties;

import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import android.os.SystemProperties;
import aurora.app.AuroraAlertDialog;
import java.io.FileReader;
import java.io.BufferedReader;
import android.text.format.Formatter;
import android.os.StatFs;
import android.os.Environment;
import java.io.File;
import android.text.format.Formatter;

public class InternalVersion extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		Log.v("adb_InternalVersion", "------onCreate----");		
		if("true".equals(SystemProperties.get("persist.radio.dispatchAllKey"))) {
			Log.v("adb_InternalVersion", "------is true----");
        	SystemProperties.set("persist.radio.dispatchAllKey", "false");
        }
		
		new AuroraDeviceSN().AuroraCreatQcNvItems(this);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v("adb_InternalVersion", "---onStop---");
		System.exit(0);
	}
	
	class AuroraDeviceSN {
		private  final String TAG = "AuroraDeviceSN";
		private  QcNvItems mQcNvItems;
		public  final String DEFAULT_SN = "iuni";
		private Context mContext;
		


		private  Runnable mRunnableClose = new Runnable() {
			@Override
			public void run() {
				if (mQcNvItems != null) {
					Log.d(TAG, "clear getSN factoryResult string ");
					// mQcNvItems.dispose();
				}
			}
		};

		private  QcRilHookCallback mQcrilHookCb = new QcRilHookCallback() {
			@Override
			public void onQcRilHookReady() {
				SystemProperties.set("persist.sys.aurora.device.sn", GnGetSN());
				Log.v(TAG, "GnGetSN() = " + GnGetSN());

				String gnznvernumber = SystemProperties
						.get("ro.gn.gnznvernumber");
				String type = SystemProperties.get("ro.build.type");
				String gnRom = SystemProperties.get("ro.gn.gnromvernumber");
				String gnvernumber = SystemProperties.get("ro.gn.gnvernumber");
				String buildTime = SystemProperties.get("ro.build.date");
				String gnvernumberrel = SystemProperties
						.get("ro.gn.gnvernumberrel");
				String uct = SystemProperties.get("ro.build.date.utc");
				String baseband = SystemProperties.get("gsm.version.baseband",
						"Unknown");

				ContentResolver cv = mContext.getContentResolver();
				// GIONEE: luohui 2012-06-19 modify for CR00625161 minute wrong
				// start->
				// SimpleDateFormat sdf = new
				// SimpleDateFormat("yyyy-MM-dd HH:dd");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				// GIONEE: luohui 2012-06-19 modify for CR00625161 minute wrong
				// end<-
				if (uct != null && !uct.equals("")) {
					buildTime = sdf.format(Long.parseLong(uct) * 1000);
				}

				// Gionee <huangzy> <2013-04-24> add for CR00800987 begin
				Resources resources = getResources();
				final String gnFtBtResultSuccess = resources
						.getString(R.string.gn_ft_bt_result_success);
				final String gnFtBtResultFail = resources
						.getString(R.string.gn_ft_bt_result_fail);
				final String gnFtBtResultNo = resources
						.getString(R.string.gn_ft_bt_result_no);

				String BtResultWCDMA = gnFtBtResultNo;
				String FtResultWCDMA = gnFtBtResultNo;

				String BtResultGSM = gnFtBtResultNo;
				String FtResultGSM = gnFtBtResultNo;

				String BtResultTD = gnFtBtResultNo;
				String FtResultTD = gnFtBtResultNo;

				// iuni gary.gou 20140514 modify start
				String gsmc2btResult = gnFtBtResultNo;
				String gsmc2ftResult = gnFtBtResultNo;
				String gsmc2antResult = gnFtBtResultNo;
				// iuni gary.gou 20140514 modify end

				// Gionee zhangxiaowei 20131116 modify for CR00935598 start
				String tdbtResult = gnFtBtResultNo;
				String tdftResult = gnFtBtResultNo;
				// Gionee zhangxiaowei 20131116 modify for CR00935598 end
				String lbtResult = gnFtBtResultNo;
				String lftResult = gnFtBtResultNo;

				String ltefddbtResult = gnFtBtResultNo;
				String ltefddftResult = gnFtBtResultNo;
				// Gionee <huangzy> <2013-04-24> add for CR00800987 end

			    Model mModel = Model.getInstance();
				
				TelephonyManager teleMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String sn = GnGetSN();// aurora change zhouxiaobing 20140211

				if (sn != null) {
					Log.d(TAG, "SN is " + sn);
					char[] barcodes = sn.toCharArray();
					// Gionee:wangth 20130301 modify for CR00773823 begin
					if (barcodes != null) {
						// Gionee <wangth><2013-04-17> modify for CR00798034
						if (barcodes.length >= 9) {
							if ('P' == barcodes[5])
								BtResultGSM = gnFtBtResultSuccess;
							if ('F' == barcodes[5])
								BtResultGSM = gnFtBtResultFail;

							if ('P' == barcodes[6])
								FtResultGSM = gnFtBtResultSuccess;
							if ('F' == barcodes[6])
								FtResultGSM = gnFtBtResultFail;

							if ('P' == barcodes[7])
								BtResultWCDMA = gnFtBtResultSuccess;
							if ('F' == barcodes[7])
								BtResultWCDMA = gnFtBtResultFail;

							if ('P' == barcodes[8])
								FtResultWCDMA = gnFtBtResultSuccess;
							if ('F' == barcodes[8])
								FtResultWCDMA = gnFtBtResultFail;
							// iuni gary.gou 20140514 modify start
							if (barcodes.length >= 32) {
								// iuni gary.gou 20140514 modify start
								if ('P' == barcodes[19]) {
									gsmc2btResult = gnFtBtResultSuccess;
								}
								if ('F' == barcodes[19]) {
									gsmc2btResult = gnFtBtResultFail;
								}

								if ('P' == barcodes[20]) {
									gsmc2ftResult = gnFtBtResultSuccess;
								}
								if ('F' == barcodes[20]) {
									gsmc2ftResult = gnFtBtResultFail;
								}

								if ('P' == barcodes[21]) {
									gsmc2antResult = gnFtBtResultSuccess;
								}
								if ('F' == barcodes[21]) {
									gsmc2antResult = gnFtBtResultFail;
								}
								// iuni gary.gou 20140514 modify end

								// Gionee zhangxiaowei 20131116 modify for
								// CR00935598 start
								if ('P' == barcodes[26])
									tdbtResult = gnFtBtResultSuccess;
								if ('F' == barcodes[26])
									tdbtResult = gnFtBtResultFail;

								if ('P' == barcodes[27])
									tdftResult = gnFtBtResultSuccess;
								if ('F' == barcodes[27])
									tdftResult = gnFtBtResultFail;

								if ('P' == barcodes[28])
									lbtResult = gnFtBtResultSuccess;
								if ('F' == barcodes[28])
									lbtResult = gnFtBtResultFail;

								if ('P' == barcodes[29])
									lftResult = gnFtBtResultSuccess;
								if ('F' == barcodes[29])
									lftResult = gnFtBtResultFail;

								// Begin add by gary.gou 20140707
								if ('P' == barcodes[30]) {
									ltefddbtResult = gnFtBtResultSuccess;
								}
								if ('F' == barcodes[30]) {
									ltefddbtResult = gnFtBtResultFail;
								}

								if ('P' == barcodes[31]) {
									ltefddftResult = gnFtBtResultSuccess;
								}
								if ('F' == barcodes[31]) {
									ltefddftResult = gnFtBtResultFail;
								}
								// End add by gary.gou 20140707

							}
						}
						// Gionee <wangth><2013-04-17> modify for CR00798034 end
					}
					// Gionee:wangth 20130301 modify for CR00773823 end
				}

				if (buildTime == null || buildTime.equals("")) {
					buildTime = resources.getString(R.string.isnull);
				}

				if (gnvernumber == null || gnvernumber.equals("")) {
					gnvernumber = resources.getString(R.string.isnull);
				}

				// Gionee <huangzy> <2013-04-24> add for CR00800987 begin
				StringBuilder messageBuilder = new StringBuilder(250);
				String typePart = ("user".equals(type) ? "" : "_" + type);
				String basebandPart = (TextUtils.isEmpty(baseband) ? "" : "\n"
						+ baseband);
				boolean isTdSupport = false;// SystemProperties.getBoolean("gn.mmi.tdscdma",
											// false);

				messageBuilder.append("[")
						.append(resources.getString(R.string.external_version))
						.append("]").append("\n").append(gnznvernumber)
						.append(typePart).append("\n").append(gnRom)
						.append(basebandPart);

				if (isTdSupport) {
					messageBuilder.append("\n[BT] GSM ").append(BtResultGSM)
							.append("; TD-SCDMA ").append(BtResultTD)
							.append("\n[FT] GSM ").append(FtResultGSM)
							.append("; TD-SCDMA ").append(FtResultTD);
				} else {
					if(mModel.isI1()){
					messageBuilder.append("\n[BT] GSM ").append(BtResultGSM)
							.append("; TD-SCDMA ").append(tdbtResult)
							.append("; WCDMA ").append(BtResultWCDMA)
							.append("; LTETDD ").append(lbtResult)
							.append("; LTEFDD ").append(ltefddbtResult)
							.append("\n[FT] GSM ").append(FtResultGSM)
							.append("; TD-SCDMA ").append(tdftResult)
							.append("; WCDMA ").append(FtResultWCDMA)
							.append("; LTETDD ").append(lftResult)
							.append("; LTEFDD ").append(ltefddftResult);
					
					messageBuilder.append("\n")
					         .append(getResources().getString(R.string.storage) + ": ")
					         .append(getSDTotalSize());
					
					messageBuilder.append("\n")
			         .append(getResources().getString(R.string.memory) + ": ")
			         .append(getTotalMemory() + "MB");
					
					}else{
						if(mModel.isU3()){
						messageBuilder.append("\n[BT] GSM ").append(BtResultGSM)
						.append("; TD-SCDMA ").append(tdbtResult)
						.append("; WCDMA ").append(BtResultWCDMA)
						.append("; GSMC2 ").append(gsmc2btResult)
						.append("; LTETDD ").append(lbtResult)
						.append("; LTEFDD ").append(ltefddbtResult)
						.append("\n[FT] GSM ").append(FtResultGSM)
						.append("; TD-SCDMA ").append(tdftResult)
						.append("; WCDMA ").append(FtResultWCDMA)
						.append("; GSMC2 ").append(gsmc2ftResult)
						.append("; LTETDD ").append(lftResult)
						.append("; LTEFDD ").append(ltefddftResult);
					  }else{
						  messageBuilder.append("\n[BT] GSM ").append(BtResultGSM)
							.append("; WCDMA ").append(BtResultWCDMA)
							.append("\n[FT] GSM ").append(FtResultGSM)
							.append("; WCDMA ").append(FtResultWCDMA);
					  }
					}
				}

				messageBuilder.append("\n[")
						.append(resources.getString(R.string.buildtime))
						.append("]").append(buildTime);
				// Gionee <huangzy> <2013-04-24> add for CR00800987 end

				AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(
						InternalVersion.this)
						.setTitle(R.string.internal_version)
						.setMessage(messageBuilder.toString())
						.setPositiveButton(resources.getString(R.string.close),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
										dialog = null;
										Log.v("adb_InternalVersion", "---onClick---");
										System.exit(0);
									}
								}).create();
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
				dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
						Log.v("adb_InternalVersion", "---onCancel---");
						//finish();
						System.exit(0);
					}
				});
				
			  dialog.show();
			  
			 
			}
		};

		private  String GnGetSN() {

			String factoryResult = DEFAULT_SN;
			try {
				factoryResult = mQcNvItems.getFactoryResult();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException ne) {
				ne.printStackTrace();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			Log.e(TAG, "getSN factoryResult :" + factoryResult);
			return factoryResult;
		}

		public  void AuroraCreatQcNvItems(Context context) {
			mContext = context;
			
			String sn = SystemProperties.get("persist.sys.aurora.device.sn",
					DEFAULT_SN);
			Log.v(TAG, "mQcNvItems = " + mQcNvItems + " sn = " + sn);
				mQcNvItems = new QcNvItems(context, mQcrilHookCb);
		}
		
		public String getSDTotalSize() { 
			  String filePath = Environment.getExternalStorageDirectory().getPath();
		    	long totalSize = 0;
		    	try {
		        File path = new File(filePath);
		        StatFs sf = new StatFs(path.getPath());  
		          
		        long blockSize = sf.getBlockSize();  
		        
		        long totalBlocks = sf.getBlockCount();
		        totalSize = blockSize * totalBlocks;
		    	}catch (IllegalArgumentException e) {
					Log.e(TAG, e.toString());
				}
		    	return Formatter.formatFileSize(InternalVersion.this, totalSize); 
		} 
		
		private long getTotalMemory() {  
	        String str1 = "/proc/meminfo";// 系统内存信息文件  
	        String str2;  
	        String[] arrayOfString;  
	        long initial_memory = 0;  
	  
	        try {  
	            FileReader localFileReader = new FileReader(str1);  
	            BufferedReader localBufferedReader = new BufferedReader(  
	                    localFileReader, 8192);  
	            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小  
	            arrayOfString = str2.split("\\s+");  
	            initial_memory = Integer.valueOf(arrayOfString[1]).intValue();
	            Log.v(TAG, "---------initial_memory---------==="+initial_memory);
	            localBufferedReader.close();  
	        } catch (IOException e) {  
	        }  
	          return initial_memory / 1024;
	    }  
	}

}
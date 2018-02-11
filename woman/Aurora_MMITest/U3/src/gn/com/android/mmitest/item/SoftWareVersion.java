
package gn.com.android.mmitest.item;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.os.StatFs;
import android.os.Environment;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

//Gionee zjy 2011-12-13 add for  CR00475554  start
import android.text.TextUtils;
import android.util.Log;
//Gionee zjy 2011-12-13 add for  CR00475554  end 

// add by zhangxiaowei start
//import com.android.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
// add by zhangxiaowei end
public class SoftWareVersion extends Activity implements OnClickListener,QcRilHookCallback {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private TextView mContentTv;

    private static String TAG = "SoftWareVersion";
  //Gionee xiaolin 20130827 modify for CR00845883 start
    QcNvItems nvItems = null;
  //Gionee xiaolin 20130827 modify for CR00845883 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //Gionee xiaolin 20130827 modify for CR00845883 start
        nvItems = new QcNvItems(this,this);
      //Gionee xiaolin 20130827 modify for CR00845883 end
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        
        setContentView(R.layout.common_textview);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mContentTv = (TextView) findViewById(R.id.test_content);
    }

    @Override
    public void onStart() {
        super.onStart();
     try {         
			 Log.d("aaaa", "nancyadd"); 
			 nvItems.getEgmrResult();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
    }

	public void onQcRilHookReady(){
		
        // Gionee zhangxiaowei 20131102 modify for CR00935758 start
		
		StringBuffer stringBuffer = new StringBuffer();
		String gnznvernumber = SystemProperties.get("ro.gn.gnznvernumber");
		String type = SystemProperties.get("ro.build.type");//eng or user
		String typePart = ("user".equals(type) ? "" : "_" + type);
		stringBuffer.append(gnznvernumber).append(typePart);
         String gnvernumber = stringBuffer.toString();
		Log.d(TAG, "gnvernumber = " + gnvernumber);

        // Gionee zhangxiaowei 20131102 modify for CR00935758 end
        String buildTime = SystemProperties.get("ro.build.date");
        String uct = SystemProperties.get("ro.build.date.utc");
		//Gionee zjy 2011-12-13 add for  CR00475554  start
		String gnvernumberrel = SystemProperties.get("ro.gn.gnvernumberrel");
		//Gionee zjy 2011-12-13 add for  CR00475554  end
        ContentResolver cv = this.getContentResolver();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:dd");
        if (uct != null && !uct.equals("")) {
            buildTime = sdf.format(Long.parseLong(uct) * 1000);
        }
        String btResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String ftResult = getResources().getString(R.string.gn_ft_bt_result_no);
		
        String gbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String gftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        
        //iuni gary.gou 20140514 modify  start
        String gsmc2btResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String gsmc2ftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String gsmc2antResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String ltefddbtResult = getResources().getString(R.string.gn_ft_bt_result_no); 
        String ltefddftResult = getResources().getString(R.string.gn_ft_bt_result_no); 
        //iuni gary.gou 20140514 modify  end
        
        // Gionee zhangxiaowei 20131116 modify for CR00935598 start
        String tdbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String tdftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        // Gionee zhangxiaowei 20131116 modify for CR00935598 end
        String lbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String snInfo = getResources().getString(R.string.sn_info);
   
        String sn = null; 
      try {
			 sn = nvItems.getEgmrResult();
			 Log.d(TAG, "sn:"+sn);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		snInfo = snInfo + sn;
		
		String factoryResult = null;
	try {
			
			 factoryResult = nvItems.getFactoryResult(); 
                         Log.v("gary", "nvItems.getFactoryResult()===========factoryResult=============" + nvItems.getFactoryResult());
			 Log.d(TAG, factoryResult+" : "+factoryResult.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null != factoryResult) {
			char[] barcodes = factoryResult.toCharArray();
                        
			if('P' == barcodes[5])
				gbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[5])
            	gbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            
        	if('P' == barcodes[6])
				gftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[6])
            	gftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
			
			if('P' == barcodes[7])
				btResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[7])
            	btResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            
        	if('P' == barcodes[8])
				ftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[8])
            	ftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            
            //iuni gary.gou 20140514 modify  start
            if('P' == barcodes[19]){
            	gsmc2btResult =  getResources().getString(R.string.gn_ft_bt_result_success);
            }
            if('F' == barcodes[19]){
            	gsmc2btResult =  getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            
            if('P' == barcodes[20]){
            	gsmc2ftResult =  getResources().getString(R.string.gn_ft_bt_result_success);
            }
            if('F' == barcodes[20]){
            	gsmc2ftResult =  getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            
            if('P' == barcodes[21]){
            	gsmc2antResult =  getResources().getString(R.string.gn_ft_bt_result_success);
            }
            if('F' == barcodes[21]){
            	gsmc2antResult =  getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            //iuni gary.gou 20140514 modify  end
            
            
         // Gionee zhangxiaowei 20131116 modify for CR00935598 start
            if('P' == barcodes[26])
            	tdbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[26])
            	tdbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            
        	if('P' == barcodes[27])
        		tdftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[27])
            	tdftResult = getResources().getString(R.string.gn_ft_bt_result_fail);

		if('P' == barcodes[28])
			lbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
		if('F' == barcodes[28])
			lbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);

		if('P' == barcodes[29])
			lftResult = getResources().getString(R.string.gn_ft_bt_result_success);
		if('F' == barcodes[29])
			lftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            
             //Begin add by gary.gou 20140707
             if('P' == barcodes[30]){
		ltefddbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
             }
             if('F' == barcodes[30]){
		 ltefddbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
             }
         
             if('P' == barcodes[31]){
		ltefddftResult = getResources().getString(R.string.gn_ft_bt_result_success);
             }
             if('F' == barcodes[31]){
		 ltefddftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
             }
             //End add by gary.gou 20140707
       }
		
		
        if (buildTime == null || buildTime.equals("")) {
            buildTime = getResources().getString(R.string.isnull);
        }

        if (gnvernumber == null || gnvernumber.equals("")) {
            gnvernumber = getResources().getString(R.string.isnull);
        } 
     // Gionee zhangxiaowei 20131116 modify for CR00935598 start 
        String content = getResources().getString(R.string.external_version) + ":" + gnvernumber
                + "\n" + snInfo + "\nGSM BT: " + gbtResult + "\nGSM FT: " + gftResult;
        if (SystemProperties.getBoolean("gn.mmi.wcdma", true)) {
            content += "\nWCDMA BT:" + btResult + "\nWCDMA FT:" + ftResult;
        }
        
        String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
    	String[] strName = deviceName.split("-");

        //iuni gary.gou 20140514 modify start
        if (SystemProperties.getBoolean("gn.mmi.gsmc2", true)) {
        	if(strName.length > 1 && (strName[1].equals("U3"))){
        		content += "\nGSMC2 BT:" + gsmc2btResult + "\nGSMC2 FT:" + gsmc2ftResult;
                }
        }
        //iuni gary.gou 20140514 modify end
        
        if (SystemProperties.getBoolean("gn.mmi.tdscdma", true)) {
            content += "\nTD-SCDMA BT:" + tdbtResult + "\nTD-SCDMA FT:" + tdftResult;
        }if (SystemProperties.getBoolean("gn.mmi.ltetdd", true)) {
		     content += "\nLTETDD BT:" + lbtResult + "\nLTETDD FT:" + lftResult;
        }
         
        //Begin add by gary.gou 20140707 
        if (SystemProperties.getBoolean("gn.mmi.ltefdd", true)) {
		     content += "\nLTEFDD BT:" + ltefddbtResult + "\nLTEFDD FT:" + ltefddftResult;
        }
        //End add by gary.gou 20140707
        
        if(strName.length > 1 && (strName[1].equals("U3m"))){
        	content += "\n" + getResources().getString(R.string.storage) + ": " + getSDTotalSize() ;
                content += "\n" + getResources().getString(R.string.memory) + ": " + getTotalMemory() + "MB" ;
        }

        content += "\n" + getResources().getString(R.string.buildtime) + ": " + buildTime;
        mContentTv.setText(content);
     // Gionee zhangxiaowei 20131116 modify for CR00935598 end      
        mRightBtn.setEnabled(true);
       /* 20130105
        try {
        	nvItems.getFactoryResult();
        } catch (IOException e) {
        	e.printStackTrace();
        }
		*/
      
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
	 return Formatter.formatFileSize(SoftWareVersion.this, totalSize); 
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }
            
            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }
    

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }
}

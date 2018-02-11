
package gn.com.android.mmitest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Message;
import android.view.KeyEvent;

// add by zhangxiaowei start
//import com.android.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcnvitems.QcNvItems;
// add by zhangxiaowei end

public class TestResult extends Activity {
    private TextView mTitleTv, mContentTv, mSNTv;

    private SharedPreferences mResultSP;
    private SharedPreferences mSNResultSP;
    private ArrayList<String> mResultList;
    private Handler mUiHandler;
    Button mQuickBtn;
    private static final String TAG = "TestResult";
    private static final int EVENT_RESPONSE_SN_WRITE = 1, EVENT_RESPONSE_SN_READ =2, EVENT_RESPONSE_AUTO_MODE_READ = 3;
    SharedPreferences.Editor mSNEditor;
    private int mCount;
    private boolean mSecWrite;
    private boolean mSecRead, mAuToSecRead;
    private String mSNToWrite;
    private Resources mRes;
    //Gionee xiaolin 20130827 modify for CR00845883 start
    private  QcNvItems nvItems = null;
  //Gionee xiaolin 20130827 modify for CR00845883 end
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //Gionee xiaolin 20130827 modify for CR00845883 start
        nvItems =  new QcNvItems(this);
      //Gionee xiaolin 20130827 modify for CR00845883  end
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        
        setContentView(R.layout.test_result);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mQuickBtn = (Button) findViewById(R.id.quit_btn);
        mQuickBtn.setEnabled(true);
        mSNTv = (TextView) findViewById(R.id.snlog);
        mRes = this.getResources();
        mTitleTv.setText(R.string.test_title);
        
        
        mQuickBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                TestResult.this.finish();
                Intent it = new Intent(TestResult.this, GnMMITest.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TestResult.this.startActivity(it);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mSNEditor.clear();
        mSNEditor.commit();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mCount = 0;
        mResultSP = getSharedPreferences("gn_mmi_test",
                Context.MODE_WORLD_WRITEABLE);
        mSNResultSP = getSharedPreferences("gn_mmi_sn",
                Context.MODE_WORLD_WRITEABLE);
        mSNEditor= mSNResultSP.edit();
      //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin 
        if(TestUtils.mIsAutoMode_2){
        	  mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(
                      R.array.auto_test_items_2)));
        }else {
        	 mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(
                     R.array.auto_test_items)));
		}
        if(TestUtils.mIsAutoMode_3){
      	  mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(
                    R.array.auto_test_items_3)));
      }else {
      	 mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(
                   R.array.auto_test_items)));
		}
      //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end
       
        StringBuilder sb = new StringBuilder();
        int value = 1;
        sb.append(this.getResources().getString(R.string.result_from_test) + "\n");
        for (int i = 0; i < mResultList.size(); i++) {
            value = mResultSP.getInt(mResultList.get(i), 1);
            if (0 == value) {
                mCount++;
                sb.append(mCount + ":   " + mResultList.get(i) + "\n");
            }
        }
       
        mContentTv.setText(sb.toString());
/*******************/        
        String factoryResult = "";
        try{
        	if (TestUtils.mIsAutoMode) {
	        	String oFS = nvItems.getFactoryResult();
	        	String nFS = getNewFactorySet(oFS);
	            nvItems.setFactoryResult(nFS+"0");        	
	        	oFS = nvItems.getFactoryResult();
	        	if (!oFS.equals(nFS)){
	        		Log.e(TAG, " fail to write factory set! try to write second time");
	            	nvItems.setFactoryResult(nFS+"0"); 
	        	} 
        	}
        	factoryResult = nvItems.getFactoryResult();
        } catch(IOException e){
        	e.printStackTrace();
        }
        
        

    	StringBuilder fnLog = new StringBuilder("\n"); 
        if (factoryResult.length() == 32) {
        	
        	if ('F' == factoryResult.charAt(12)) 
        		mTitleTv.setText(R.string.mmitest_fail);
        	else if ('P' == factoryResult.charAt(12))
        		mTitleTv.setText(R.string.mmitest_success);
        	else 
        		mTitleTv.setText(R.string.no_mmitest);
        	
			for (String key : TestUtils.factoryFlag.keySet()) {
				String testResult = "";
				int loc = Integer.parseInt(TestUtils.factoryFlag.get(key));
				if (factoryResult.charAt(loc) == 'P') {
					testResult = mRes.getString(R.string.right);
				} else if (factoryResult.charAt(loc) == 'F') {
					testResult = mRes.getString(R.string.wrong);
				} else {
					continue ;
				}
				fnLog.append(key + ": " + testResult + "\n");
			}
		}
		Log.d(TAG, fnLog.toString());
        mSNTv.setText(fnLog);
/********************/
    }
    
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    
    private String getNewFactorySet(String old) {
    	StringBuilder sb = new StringBuilder(old);
    	
    	char mmi_result = (mCount == 0)?'P':'F';
    	if(mmi_result != old.charAt(12))
    		sb.setCharAt(12, mmi_result);
    	
    	for (String key : TestUtils.factoryFlag.keySet()) {
    		String loc = TestUtils.factoryFlag.get(key);
    		char nV = mSNResultSP.getString(loc, "F").charAt(0);
    		char oV = old.charAt(Integer.parseInt(loc));
    		if (nV != oV) {
    			sb.setCharAt(Integer.parseInt(loc), nV);
    		} 
    	}
    	
    	return sb.toString();
    }
    
}

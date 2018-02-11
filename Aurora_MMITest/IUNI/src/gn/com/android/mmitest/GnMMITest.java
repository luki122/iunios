
package gn.com.android.mmitest;

import java.io.IOException;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import android.view.KeyEvent;
import android.os.PowerManager;
import android.os.SystemProperties;
import java.util.Arrays;
import java.util.List;
import android.provider.Settings;
import java.util.ArrayList;
import android.os.Build;
import com.android.internal.os.storage.ExternalStorageFormatter;

public class GnMMITest extends Activity implements OnItemClickListener {
    /** Called when the activity is first created. */


    private AlertDialog.Builder mBuilder;
    public static Handler mSetSNHandler;
    public HandlerThread mSetSNHandlerThread;
    static String TAG = "GnMMITest";
    SharedPreferences.Editor mSNEditor;
    WindowManager.LayoutParams mWL;
    private PowerManager mPM;
    private int clickCount;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list);
        mWL = getWindow().getAttributes();
        mWL.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // mWL.dispatchAllKey = 1;
        getWindow().setAttributes(mWL);
        
        //mPM = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		//mPM.dispatchAllKey(true);
       // MmiManager mMmiManager = (MmiManager)this.getSystemService("mmi");
		//mMmiManager.dispatchAllKey(true);
		
		SystemProperties.set("persist.radio.dispatchAllKey", "true");
			SystemProperties.set("persist.radio.dump", "true");
        
        ListView lv = (ListView) findViewById(R.id.main_listview);
        Button quitBtn = (Button) findViewById(R.id.quit_btn);
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
					//mPM.dispatchAllKey(false);
                	//MmiManager mMmiManager = (MmiManager)GnMMITest.this.getSystemService("mmi");
					//mMmiManager.dispatchAllKey(false);
					SystemProperties.set("persist.radio.dispatchAllKey", "false");
					SystemProperties.set("persist.radio.dump", "false");
					 //Enable systemui
			         sendBroadcast(new Intent("com.android.systemui.recent.AURORA_ENABLE_HANDLER"));
                System.exit(0);
            }
            
        });
        TestUtils.setAppContext(GnMMITest.this);
        mSNEditor = TestUtils.getSNSharedPreferencesEdit(this);
        
        //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin 

        String[] it = this.getResources().getStringArray(R.array.test_project_item);
       if(!SystemProperties.getBoolean("gn.mmi.autotest2", true)) {
                List<String> t = new ArrayList(Arrays.<String>asList(it));
                t.remove(4);
                it = t.toArray(new String[1]);
       }
       if(!SystemProperties.getBoolean("gn.mmi.autotest3", true)) {
           List<String> t = new ArrayList(Arrays.<String>asList(it));
           t.remove(5);
           it = t.toArray(new String[1]);
  }
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, it));
        
      //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end 
        lv.setOnItemClickListener(this);
        
        TestUtils.configTestItemArrays(this);    
        ////Disable systemui
        sendBroadcast(new Intent("com.android.systemui.recent.AURORA_DISABLE_HANDLER"));
    }

    


	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        switch (position) {
            case 0: {
                try {
                	Settings.Secure.setLocationProviderEnabled(getContentResolver(),LocationManager.GPS_PROVIDER, true);
                    TestUtils.openBtAndWifi(GnMMITest.this);
                    TestUtils.mIsAutoMode = true;
                    TestUtils.mIsAutoMode_2 = false;
                    TestUtils.mIsAutoMode_3 = false;
                    mSNEditor.clear();
                    mSNEditor.putBoolean("mIsAutoMode", true);
                    mSNEditor.commit();
                    startActivity(new Intent(this, Class.forName("gn.com.android.mmitest.item."
                            + TestUtils.getAutoItemKeys(this).get(0))));
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    TestUtils.mIsAutoMode = false;
                    e.printStackTrace();
                }
                break;
            }

            case 1: {
            	Settings.Secure.setLocationProviderEnabled(getContentResolver(),LocationManager.GPS_PROVIDER, true);
                TestUtils.openBtAndWifi(GnMMITest.this);
                TestUtils.mIsAutoMode = false;
                TestUtils.mIsAutoMode_2 = false;
                TestUtils.mIsAutoMode_3 = false;
                mSNEditor.clear();
                mSNEditor.commit();
                Log.e("lich", TAG+"145");
                startActivity(new Intent(this, SingleTestGridView.class));
                break;
            }

            case 2: {
                TestUtils.mIsAutoMode = false;
                TestUtils.mIsAutoMode_2 = false;
                TestUtils.mIsAutoMode_3 = false;
                mSNEditor.clear();
                mSNEditor.commit();
                Log.e("lich", TAG+"152");
                startActivity(new Intent(this, TestResult.class));
                break;
            }

            case 3: {
                if (null == mBuilder) {
                    mBuilder = new Builder(this);
                    mBuilder.setTitle(R.string.master_clear_title);
                    mBuilder.setMessage(R.string.master_clear_final_desc);
                    mBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            clickCount++;
                            if (clickCount > 1)
                                return;
                          
                            String buildModel = Build.MODEL;
                            if (buildModel.contains("U810")) {        	
        			Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                		intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                		startService(intent);
                            }else{
        			Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
	            		intent.putExtra("wipe_internal_data", "true");  
	                	sendBroadcast(intent);
                   	   }                            
                        }

                    });
                    mBuilder.setNegativeButton(android.R.string.cancel, null);
                }
                mBuilder.show();
                break;
            }
          //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin
            case 4: {
                try {
                	TestUtils.openBtAndWifi(GnMMITest.this);
                	TestUtils.mIsAutoMode_2 = true;
                	TestUtils.mIsAutoMode_3 = false;
                	TestUtils.mIsAutoMode = false;
                	mSNEditor.clear();
                	mSNEditor.putBoolean("mIsAutoMode", true);
                	mSNEditor.commit();
                	startActivity(new Intent(this, Class.forName("gn.com.android.mmitest.item."
                       + TestUtils.getAutoItemKeys_2(this).get(0))));
                	finish();
                } catch (ClassNotFoundException e) {
               // TODO Auto-generated catch block
                	TestUtils.mIsAutoMode_2 = false;
               }  	
               break; 
           }
            case 5: {
                try {
                	TestUtils.openBtAndWifi(GnMMITest.this);
                	TestUtils.mIsAutoMode_2 = false;
                	TestUtils.mIsAutoMode = false;
                	TestUtils.mIsAutoMode_3 = true;
                	mSNEditor.clear();
                	mSNEditor.putBoolean("mIsAutoMode", true);
                	mSNEditor.commit();
                	startActivity(new Intent(this, Class.forName("gn.com.android.mmitest.item."
                       + TestUtils.getAutoItemKeys_3(this).get(0))));
                	finish();
                } catch (ClassNotFoundException e) {
               // TODO Auto-generated catch block
                	TestUtils.mIsAutoMode_3 = false;
               }  	
               break; 
           }
        }
      //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end
	}
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    
    private static String[] keepArray;
	static{
            if(true == SystemProperties.get("ro.gn.oversea.product").equals("yes")){
                //GIONEE lijinfang 2012-11-21 modify for CR00734894 start
                if(true == SystemProperties.get("ro.gn.oversea.custom").equals("AFRICA_GIONEE")){
                    keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Music", "APK", "Free games"};
                //GIONEE lijinfang 2012-11-21 modify for CR00734894 end
                } else {			
                    keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool"};
                }
            } else {
                    keepArray = new String[]{"mapbar", "music", "video", "主题",".gn_apps.zip", "pctool", "音乐", "视频", "随变"};
            }
	}

    private static List<String> keepList = Arrays.asList(keepArray);
    private static String SDPATH = null;
    
    private static void EraseSD() {
        // Gionee xiaolin 20120620 modify for CR00626921 start
        SDPATH = "/mnt/sdcard";
        File sd = new File(SDPATH); 
        if (sd.canWrite())
            dFile(new File(SDPATH));
        
        SDPATH = "/mnt/sdcard2";
        sd = new File(SDPATH);
        if(sd.canWrite())
            dFile(new File(SDPATH));
       // Gionee xiaolin 20120620 modify for CR00626921 end
    }

    private static void dFile(File file) {
        for (String item : keepList) {
            if ((SDPATH+"/"+item).equalsIgnoreCase(file.toString()))
                return;
        }
         
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            } else if (file.isDirectory()) {
                Log.d(TAG, "dir :" + file.toString());
                File files[] = file.listFiles();
                if (files == null) {
                    Log.i(TAG, file  + " listFiles()"+ " return null");
                    return ;
                }
                for (int i = 0; i < files.length; i++) {
                    dFile(files[i]);
                }
            }
            
            if (!SDPATH.equals(file.toString())) {
                file.delete();
            }
            
        } else {
            Log.d(TAG, "要删除的文件不存在!");
        }   
    }

}

package com.android.phone;

import java.util.ArrayList;
import java.util.List;

import com.gionee.aora.numarea.export.INumAreaManager;
import com.gionee.aora.numarea.export.INumAreaObserver;
import com.gionee.aora.numarea.export.IUpdataResult;
import com.gionee.aora.numarea.export.NumAreaInfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sogou.hmt.sdk.manager.HmtSdkManager;

public class NumberAreaUtil {
	private static final String ACTION_NUMAREA = "gionee.aora.numarea";
	
	private static INumAreaManager mManager;	
	private static ServiceConnection mConnection;
	private static INumAreaObserver mObserver;
	private static boolean mIsAreaServiceConnected = false;
	// gionee xuhz 20120627 add for CR00627592 start
	private static boolean mIsDownLoading = false;
	// gionee xuhz 20120627 add for CR00627592 end
	
    private static List<INumAreaObserver> mNumAreaObservers;
    
    private static void ensureServiceConnection() {
    	if (null == mNumAreaObservers) {
    		mNumAreaObservers = new ArrayList<INumAreaObserver>(2);
    	}
    	if (null == mObserver) {
	    	mObserver = new INumAreaObserver.Stub() {
	    		@Override
	    		public void updata(int aResultCode) throws RemoteException {
				    // gionee xuhz 20120627 add for CR00627592 start
					mIsDownLoading = false;
				    // gionee xuhz 20120627 add for CR00627592 end
	    			switch (aResultCode)
	                {	    				
	    				case IUpdataResult.RESULT_SUCCESS:
	    					Log.i("James", "RESULT_SUCCESS");
	    					mIsAreaServiceConnected = true;
	    				    // gionee xuhz 20120627 add for CR00627592 start
	    					mIsDownLoading = true;
	    				    // gionee xuhz 20120627 add for CR00627592 end
	    					break;
	    				case IUpdataResult.RESULT_INIT_FINISH:
	    					Log.i("James", "RESULT_INIT_FINISH");
	    				    // gionee xuhz 20120627 add for CR00627592 start
	    					mIsDownLoading = false;
	    				    // gionee xuhz 20120627 add for CR00627592 end

	    					mIsAreaServiceConnected = true;
	    					break;
	    				case IUpdataResult.RESULT_ERROR_CONNECT_FAILD:
	    					Log.i("James", "RESULT_ERROR_CONNECT_FAILD");
	    					break;
	    				case IUpdataResult.RESULT_ERROR_CONNECT_TIMEOUT:
	    					Log.i("James", "RESULT_ERROR_CONNECT_TIMEOUT");
	    					break;
	    				case IUpdataResult.RESULT_DB_IS_LAST_VERSION:
	    					Log.i("James", "RESULT_DB_IS_LAST_VERSION");
	    					break;
	    				// gionee xuhz 20120627 add for CR00627592 start
	    				case IUpdataResult.RESULT_DOWNLOADING:
	    					Log.i("James", "RESULT_DOWNLOADING");
	    					mIsDownLoading = true;
	    					break;
	    				// gionee xuhz 20120627 add for CR00627592 end

	                }	    			
	    			
	    			if (mIsAreaServiceConnected && null != mNumAreaObservers) {
	    				for (INumAreaObserver stub : mNumAreaObservers) {
	    					stub.updata(aResultCode);
	    				}
	    			}
	             }
	        };
    	}
    	
    	if (null == mConnection) {
    		mConnection = new ServiceConnection() {
    			@Override
    			public void onServiceDisconnected(ComponentName name) {
    				mIsAreaServiceConnected = false;
    				if (null != mManager) {
    					try {
							mDeathNotifier.unlinkToDeath();
							mManager.unregistObserver(mObserver);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
    				}
    				clear();			
    			}
    			
    			@Override
    			public void onServiceConnected(ComponentName name , IBinder service) {
    				mManager = INumAreaManager.Stub.asInterface(service);
    				if (null != mManager) {
    					try {    						
							mManager.registObserver(mObserver);
							mDeathNotifier = getDeathNotifier(mManager);
							mDeathNotifier.linkToDeath();
						} catch (RemoteException e) {
							e.printStackTrace();
						}											
    				}
    			}
    	    };
    	}    
    }
    
    public static void unregistObserver(INumAreaObserver observer) {
    	if (null != mManager && null != mNumAreaObservers) {
			mNumAreaObservers.remove(observer);
	    }
    }
    
    private static void registObserver(INumAreaObserver observer) {
    	if (null != mManager && null != mNumAreaObservers && !mNumAreaObservers.contains(observer)) {
			mNumAreaObservers.add(observer);
	    }
    }
	
	public static boolean bindService(INumAreaObserver numAreaObserver) {
		if (mIsAreaServiceConnected) {
			if (!mNumAreaObservers.contains(numAreaObserver)) {
				registObserver(numAreaObserver);
			}			
			return true;
		} else {
			Intent aoraArea = new Intent(ACTION_NUMAREA);
			ensureServiceConnection();
			boolean result = false;
			try {
				result = PhoneApp.getInstance().bindService(AuroraPhoneUtils.getExplicitIntent(PhoneGlobals.getInstance(), aoraArea), mConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	
	public static void unbindService() {
		if (mIsAreaServiceConnected) {
		    // Gionee zhangxx 2012-05-17 add for CR00585554 begin
		    mIsAreaServiceConnected = false;
		    // Gionee zhangxx 2012-05-17 add for CR00585554 end
		    PhoneApp.getInstance().unbindService(mConnection);			
		}		
	}
	
    public static String getRightNumArea(NumAreaInfo info) {

        String numArea = null;
        String numAreaHead = null;
        String numAreaBase = null;

        if (null == info) {
            return null;
        }

        numArea = info.toString();

        //AURORA-START::aurora UI::Modify::Ukiliu::20130904
        if (!TextUtils.isEmpty(info.getiHeadInfo())) {
            numAreaHead = info.getiHeadInfo();
        }

        if (!TextUtils.isEmpty(info.getiBaseInfo())) {
            numAreaBase = info.getiBaseInfo();
        }
        
        if (null != numArea) {
            if (null != numAreaHead && null != numAreaBase) {
                numArea = numAreaHead + "  " + numAreaBase;
                if (numAreaBase.equals(numAreaHead)) {
                    numArea = numAreaBase;
                }
            } else if (null == numAreaHead && null != numAreaBase) {
                numArea = numAreaBase;
            } else if (null == numAreaBase && null != numAreaHead) {
                numArea = numAreaHead;
            }
        }
        //AURORA-END::aurora UI::Modify::Ukiliu::20130904

        return numArea;
    }
    
    public static String getNumAreaFromAora(String number) {

    	if(SogouUtils.isInit()) {
    		//aurora modify liguangyu 20140927 for #8852 start
    		String result = "";
    		try {
    			result = HmtSdkManager.getInstance().getRegionName(number);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		//aurora modify liguangyu 20140927 for #8852 end
          	if(TextUtils.isEmpty(result)) { 
		      	if(!TextUtils.isEmpty(number) 
		      			&& (number.replace(" ", "").length() == 7 || number.replace(" ", "").length() == 8)
		      			&& !number.startsWith("0")
		        			&& !number.startsWith("400")
		        			&& !number.startsWith("800")
		      			) {
		      		result = PhoneGlobals.getInstance().getString(R.string.aurora_local_number);
		      	}  else if (!TextUtils.isEmpty(number) 	              			
		        			&& (number.startsWith("400") || number.startsWith("800"))
		        			&& number.replace(" ", "").length() == 10
		        			){
		      		result = PhoneGlobals.getInstance().getString(R.string.aurora_service_number);
		        } else {
		        	result = "";
		      	}
		      	if(result.equalsIgnoreCase("")) {
		  			String[] hotlineInfo = GnHotLinesUtil.getInfo(PhoneGlobals.getInstance(), number);
		  			if (null != hotlineInfo) {
		  				result = hotlineInfo[0];
		  			}	
		      	}
          	}
    		return result;
    	}
    	
        String numArea = null;
        
        if(mManager == null) {
        	bindService(null);
        }
        // gionee xuhz 20120907 modify for CR00704430 start
        if (null != mManager && true == mIsAreaServiceConnected && null != number) {
        	//old: if (null != mManager && true == mIsAreaServiceConnected && null != number && !mIsDownLoading) {
        // gionee xuhz 20120907 modify for CR00704430 end
            try {
                NumAreaInfo info = mManager.getNumAreaInfo(number);
                
                if (null == info) {
                	//aurora change liguangyu 20131224 start
//                    return ""; //Gionee:wangth 20120718 modify for CR00651280
                	if(!TextUtils.isEmpty(number) 
                			&& (number.replace(" ", "").length() == 7 || number.replace(" ", "").length() == 8)
                			&& !number.startsWith("0")
	              			&& !number.startsWith("400")
	              			&& !number.startsWith("800")
                			) {
                		numArea = PhoneGlobals.getInstance().getString(R.string.aurora_local_number);
                	}  else if (!TextUtils.isEmpty(number) 	              			
	              			&& (number.startsWith("400") || number.startsWith("800"))
	              			&& number.replace(" ", "").length() == 10
	              			){
	              		numArea = PhoneGlobals.getInstance().getString(R.string.aurora_service_number);
	              	} else {
                		numArea = "";
                	}
                    //aurora change liguangyu 20131224 end
                	if(numArea.equalsIgnoreCase("")) {
	        			String[] hotlineInfo = GnHotLinesUtil.getInfo(PhoneGlobals.getInstance(), number);
	        			if (null != hotlineInfo) {
	        				numArea = hotlineInfo[0];
	        			}	
                	}
                } else {
                    numArea = getRightNumArea(info);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NullPointerException ne) { //Gionee:wangth 20121120 add for CR00733616
                ne.printStackTrace();
            }
        }
        return numArea;
    }
    
    private static void clear() {
    	mManager = null;
    	mIsAreaServiceConnected = false;
    	mObserver = null;
    	mConnection = null;
    	if (null != mNumAreaObservers) {
    		mNumAreaObservers.clear();
    	}
    }
    
    private static DeathNotifier mDeathNotifier;
    static DeathNotifier getDeathNotifier(INumAreaManager mgr) {
    	return new DeathNotifier(mgr);
    }
}

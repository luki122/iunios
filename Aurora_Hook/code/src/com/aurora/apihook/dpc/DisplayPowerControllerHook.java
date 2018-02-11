package com.aurora.apihook.dpc;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.FloatMath;
import android.util.Log;


public class DisplayPowerControllerHook{
	private String TAG ="DisplayPowerControllerHook";
	
	private Context mContext; 
	AuroraBrightnessObserver  auroraBrightnessObserver;
	private boolean DEBUG;
	
	private Class<?> clz;
	public void after_DisplayPowerController(MethodHookParam param){
		Log.v(TAG, "after_DisplayPowerController");
		mContext =(Context)(param.args)[1];
		auroraBrightnessObserver = new AuroraBrightnessObserver(new Handler());
		DEBUG = ClassHelper.getStaticBooleanField(param.thisObject.getClass(), "DEBUG");
		clz = param.thisObject.getClass();
	}
	
	
	public void before_updateAutoBrightness(MethodHookParam param){
		Log.v(TAG, "before_updateAutoBrightness");
		boolean mAmbientLuxValid = ClassHelper.getBooleanField(param.thisObject, "mAmbientLuxValid");
		if (!mAmbientLuxValid) {
			//param.setResult(param.thisObject);
	        return;
	    }
		
		float mAmbientLux = ClassHelper.getFloatField(param.thisObject, "mAmbientLux");
	    boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = ClassHelper.getStaticBooleanField(param.thisObject.getClass(), "USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT");
	    float SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA  = ClassHelper.getStaticFloatField(param.thisObject.getClass(), "SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA");
	    boolean USE_TWILIGHT_ADJUSTMENT =  ClassHelper.getStaticBooleanField(param.thisObject.getClass(), "USE_TWILIGHT_ADJUSTMENT");
	    
	    Object mPowerRequest = ClassHelper.getObjectField(param.thisObject,"mPowerRequest");
		float screenAutoBrightnessAdjustment = ClassHelper.getFloatField(mPowerRequest, "screenAutoBrightnessAdjustment");
		
		Object mScreenAutoBrightnessSpline = ClassHelper.getFloatField(param.thisObject, "mScreenAutoBrightnessSpline");
		float value = (Float)ClassHelper.callMethod(mScreenAutoBrightnessSpline, "interpolate", mAmbientLux);
		float gamma = 1.0f;
			
		if(USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT && screenAutoBrightnessAdjustment != 0.0f){
			final float adjGamma = FloatMath.pow(SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA,
	                   Math.min(1.0f, Math.max(-1.0f,
	                           -screenAutoBrightnessAdjustment)));
	           gamma *= adjGamma;
	           if (DEBUG) {
	               Log.d(TAG, "updateAutoBrightness: adjGamma=" + adjGamma);
	           }
		}
		Log.v(TAG, "before_updateAutoBrightness----1111");
		Object mTwilight = ClassHelper.getObjectField(param.thisObject,"mTwilight");
		if(USE_TWILIGHT_ADJUSTMENT){
			Object state = ClassHelper.callMethod(mTwilight, "getCurrentState");
			if(state != null && (Boolean)ClassHelper.callMethod(state, "isNight")){
				final long now = System.currentTimeMillis();
				final float earlyGamma = (Float)ClassHelper.callStaticMethod(param.thisObject.getClass(), "getTwilightGamma", 
						now,ClassHelper.callMethod(state, "getYesterdaySunset"),ClassHelper.callMethod(state, "getTodaySunrise"));
				
				 final float lateGamma =(Float)ClassHelper.callStaticMethod(param.thisObject.getClass(), "getTwilightGamma", 
							now,ClassHelper.callMethod(state, "getTodaySunset"),ClassHelper.callMethod(state, "getTomorrowSunrise"));
				  gamma *= earlyGamma * lateGamma;
	                if (DEBUG) {
	                    Log.d(TAG, "updateAutoBrightness: earlyGamma=" + earlyGamma
	                            + ", lateGamma=" + lateGamma);
	                }
				}
			}
		
		 if (gamma != 1.0f) {
	            final float in = value;
	            value = FloatMath.pow(value, gamma);
	            if (DEBUG) {
	                Log.d(TAG, "updateAutoBrightness: gamma=" + gamma
	                        + ", in=" + in + ", out=" + value);
	            }
	        }
		 Log.v(TAG, "before_updateAutoBrightness----22222");
		int newScreenAutoBrightness = auroraGetBrightnessValue();
		
		int mScreenAutoBrightness = ClassHelper.getIntField(param,"mScreenAutoBrightness");
		
		if (mScreenAutoBrightness != newScreenAutoBrightness) {
			if (DEBUG) {
                Log.d(TAG, "updateAutoBrightness: mScreenAutoBrightness="
                        + mScreenAutoBrightness + ", newScreenAutoBrightness="
                        + newScreenAutoBrightness);
            }
			
			ClassHelper.setIntField(param, "mScreenAutoBrightness", newScreenAutoBrightness);
			ClassHelper.setFloatField(param, "mLastScreenAutoBrightnessGamma", gamma);
			
			boolean sendUpdate = (Boolean)param.args[0];
			if(sendUpdate){
				ClassHelper.callMethod(param, "sendUpdatePowerState");
			}

		 }
		 
		 Log.v(TAG, "before_updateAutoBrightness----33333");
		 
		 param.setResult(param.thisObject);
	}
	
	
	private int auroraGetBrightnessValue(){
    	return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 100);
    }
	
	public void after_setLightSensorEnabled(MethodHookParam param){
		Log.v(TAG, "after_setLightSensorEnabled--begin");
		SensorManager mSensorManager = (SensorManager)ClassHelper.getObjectField(param.thisObject, "mSensorManager");
		SensorEventListener mSensor = (SensorEventListener)ClassHelper.getObjectField(param.thisObject,"mLightSensorListener");
		mSensorManager.unregisterListener(mSensor);
		Log.v(TAG, "after_setLightSensorEnabled--End");
	}
	
	
	public void after_updatePowerState(MethodHookParam param){
		 Log.v(TAG, "after_updatePowerState--begin");
		 boolean enable = false;
		 boolean useAutoBrightness = false;
		 boolean wantScreenOnResult = false;
		 
		 Class<?> clz = ClassHelper.findClass("com.android.server.power.DisplayPowerController",null);//new DisplayPowerRequest;
		 if(clz != null){
			 try {
				 Object tempObject = ClassHelper.getObjectField(param.thisObject,"mPowerRequest");
				 if(tempObject != null){
					 useAutoBrightness = ClassHelper.getBooleanField(tempObject, "useAutoBrightness");
					 int screenState = ClassHelper.getIntField(tempObject, "screenState");
					 wantScreenOnResult = (Boolean)ClassHelper.callStaticMethod(clz, "wantScreenOn", screenState);
				 }
			  } catch (Exception e) {
					 Log.v(TAG, "after_updatePowerState--handle IllegalAccessException : ",e);
			}
			enable = useAutoBrightness && wantScreenOnResult;		
		 }
		 Log.v(TAG, "after_updatePowerState--enable============"+enable);
		 if (auroraBrightnessObserver != null) {
				auroraBrightnessObserver.setParam(param);
				if (enable) {
					mContext.getContentResolver()
							.registerContentObserver(
									Settings.System
											.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
									true, auroraBrightnessObserver);
				} else {
					mContext.getContentResolver().unregisterContentObserver(
							auroraBrightnessObserver);
				}
				Log.v(TAG, "after_updatePowerState--End");
			}else{
				Log.v(TAG, "after_updatePowerState--auroraBrightnessObserver is null");
			}
	}
	
	
	public final class AuroraBrightnessObserver extends android.database.ContentObserver{
		MethodHookParam mParam;
		
    	public AuroraBrightnessObserver(android.os.Handler handler) {
    		super(handler);
    		// TODO Auto-generated constructor stub
    	}
    	
    	public void setParam(MethodHookParam param){
    		mParam = param;
    	}

    	@Override
    	public void onChange(boolean selfChange) {
    		// TODO Auto-generated method stub
    		 Log.v(TAG, "after_updatePowerState--AuroraBrightnessObserver----onChange");
    		int br = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, 100);
    		
    		//animateScreenBrightness(clampScreenBrightness(br),BRIGHTNESS_RAMP_RATE_FAST);
    		final int value = ClassHelper.getStaticIntField(clz, "BRIGHTNESS_RAMP_RATE_FAST");
    		Integer interger = (Integer)ClassHelper.callMethod(mParam.thisObject, "clampScreenBrightness", new Class<?>[]{int.class},br);
    		ClassHelper.callMethod(mParam.thisObject, "animateScreenBrightness",new Class<?>[]{int.class,int.class},interger.intValue(),value);
    		 Log.v(TAG, "after_updatePowerState--AuroraBrightnessObserver----onChange----end");
        	}
	}
}
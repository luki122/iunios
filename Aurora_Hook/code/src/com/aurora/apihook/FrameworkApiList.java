package com.aurora.apihook;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

public class FrameworkApiList {
	
	/**
	 * 在原生方法执行之前执行
	 */
	public static final int INVOKE_TYPE_BEFORE = 1;
	
	/**
	 * 在原生方法执行之后执行
	 */
	public static final int INVOKE_TYPE_AFTER = 2;
	
	
	/**
	 * 该原生方法之前调用你自己的方法
	 */
	public static final HashMap<String,List<HookMethod>> mInvokeBeforeOriMethods; 
	/**
	 * 该原生方法调用之后执行自己的方法
	 */
	public static final HashMap<String,List<HookMethod>> mInvokeAfterOriMethods;
	
	
	private static FrameworkApiList mInstance;
	
	static{
		mInvokeBeforeOriMethods = new HashMap<String,List<HookMethod>>();
		mInvokeAfterOriMethods = new HashMap<String,List<HookMethod>>();
	}
	private FrameworkApiList(){
		
	}
	
	
	/**
	 * 获得FrameworkApiList的实例
	 * @return
	 */
	public static synchronized FrameworkApiList getInstance(){
		if(mInstance == null){
			mInstance = new FrameworkApiList();
		}
		return mInstance;
	}
	
	/**
	 * 
	 * @param invokeType 调用顺序，判定你的方法是在原生方法调用之前还是之后，主要有以下方式：
	 * <p>
	 * 1.调用之前（对应before回调方法）
	 * 2.调用之后（对应after方法）
	 * 3.调用之后并有返回值（对应afterbutreturn方法）
	 * 4.调用之前并不执行原生方法（对应beforebutreturn方法）
	 * </p>
	 * @param className  你自己的实现类，一般修改一个原生方法会对应一个自己的实现类
	 * @param methodName  你需要修改的原生方法的名称
	 * @param argsType   原生方法的参数类型（Class类型）
	 */
	public static synchronized void putHookMethod(int minVersion,int maxVersion,int invokeType,String className,String methodName,boolean isConstructor,Class<?>... argsType){
		List<HookMethod> methodList = null;
		switch (invokeType) {
		case INVOKE_TYPE_BEFORE:
			if(mInvokeBeforeOriMethods.containsKey(className)){
				methodList = mInvokeBeforeOriMethods.get(className);
			}
			break;
		case INVOKE_TYPE_AFTER:
			if(mInvokeAfterOriMethods.containsKey(className)){
				methodList = mInvokeAfterOriMethods.get(className);
			}
			break;

		default:
			break;
		}
		
		if(methodList == null){
			methodList = new ArrayList<HookMethod>();
		}
		HookMethod method = new HookMethod();
		method.setMethodName(methodName);
		method.addArgs(argsType);
		method.setSdkRange(minVersion, maxVersion);
		method.setConstructor(isConstructor);
		methodList.add(method);
		switch (invokeType) {
		case INVOKE_TYPE_BEFORE:
				mInvokeBeforeOriMethods.put(className, methodList);
			break;
		case INVOKE_TYPE_AFTER:
				mInvokeAfterOriMethods.put(className, methodList);
			break;

		default:
			break;
		}

		
	}
	
	public static  boolean isRegisted(String methodName,int invokeType){
		if(TextUtils.isEmpty(methodName)){
			return false;
		}
		
		return false;
	}
	
	
	@Deprecated
	public static  List<HookMethod> getHookMethods(String className){
		return null;
	}
	
	
	
	
	public static class HookMethod{
		private  String mMethodName;
		
		private  List<Class<?>> mArgsType;
		public HookMethod(){
			mArgsType = new ArrayList<Class<?>>();
		}
       private boolean isConstructor ;
		
       private int mMaxSdkVersion;
		
		private int mMinSdkVersion;
		
		public int getMinSdkVersion(){
			return mMinSdkVersion;
		}
		public void setSdkRange(int min,int max){
			this.mMinSdkVersion =min;
			this.mMaxSdkVersion = max;
		}
		
		public int getMaxSdkVersion(){
			return mMaxSdkVersion;
		}
		
		
		public void setConstructor(boolean yes){
			this.isConstructor = yes;
		}
		public boolean isConstructor(){
			return isConstructor;
		}
		public void setMethodName(String name){
			this.mMethodName = name;
		}
		
		public String getMethodName(){
			return mMethodName;
		}
		public  void addArgs(Class<?>... args){
			for(Class<?> arg:args){
				mArgsType.add(arg);
			}
		}
		
		public  List<Class<?>> getArgs(){
			return mArgsType;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}

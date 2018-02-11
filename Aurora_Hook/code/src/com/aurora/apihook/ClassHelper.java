package com.aurora.apihook;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.aurora.apihook.RefrectHelpers;
import com.aurora.apihook.RefrectHelpers.ClassNotFoundError;
import com.aurora.apihook.RefrectHelpers.InvocationTargetError;

public class ClassHelper {
	private static final String TAG = "ClassHelper";
	private static final HashMap<String, Field> fieldCache = new HashMap<String, Field>();
	/**
	 * Look up a class with the specified class loader (or the boot class loader if
	 * <code>classLoader</code> is <code>null</code>).
	 * <p>Class names can be specified in different formats:
	 * <ul><li>java.lang.Integer
	 * <li>int
	 * <li>int[]
	 * <li>[I
	 * <li>java.lang.String[]
	 * <li>[Ljava.lang.String;
	 * <li>android.app.ActivityThread.ResourcesKey
	 * <li>android.app.ActivityThread$ResourcesKey
	 * <li>android.app.ActivityThread$ResourcesKey[]</ul>
	 * <p>A {@link ClassNotFoundError} is thrown in case the class was not found.
	 */
	public static Class<?> findClass(String className, ClassLoader classLoader) {
		return RefrectHelpers.findClass(className, classLoader);
	}
	
	/**
	 * get current sdk(os) version  
	 * @return  current sdk version
	 */
	public static  int getCurrentSdkVersion(){
		return android.os.Build.VERSION.SDK_INT;
	}

	/**
	 * Look up a field in a class and set it to accessible. The result is cached.
	 * If the field was not found, a {@link NoSuchFieldError} will be thrown.
	 */
	public static Field findField(Class<?> clazz, String fieldName) {
        return RefrectHelpers.findField(clazz, fieldName);
	}


	/**
	 * Returns the first field of the given type in a class.
	 * Might be useful for Proguard'ed classes to identify fields with unique types.
	 * If no matching field was not found, a {@link NoSuchFieldError} will be thrown.
	 */
	public static Field findFirstFieldByExactType(Class<?> clazz, Class<?> type) {
		return RefrectHelpers.findFirstFieldByExactType(clazz, type);
	}

	public static boolean needCallSuperMethod(Object obj,String clzName){
		String name = obj.getClass().getName();
		return !clzName.equals(name);
	}

	/**
	 * Look up a method in a class and set it to accessible. The result is cached.
	 * If the method was not found, a {@link NoSuchMethodError} will be thrown.
	 *
	 * <p>The parameter types may either be specified as <code>Class</code> or <code>String</code>
	 * objects. In the latter case, the class is looked up using {@link #findClass} with the same
	 * class loader as the method's class.
	 */
	public static Method findMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) {
		return RefrectHelpers.findMethodExact(clazz, methodName, parameterTypes);
	}

	/** @see #findMethodExact(Class, String, Object...) */
	public static Method findMethodExact(String className, ClassLoader classLoader, String methodName, Object... parameterTypes) {
		return RefrectHelpers.findMethodExact(className, classLoader, methodName, parameterTypes);
	}

	/** @see #findMethodExact(Class, String, Object...) */
	public static Method findMethodExact(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return RefrectHelpers.findMethodExact(clazz, methodName, parameterTypes);
	}

	/**
	 * Returns an array of all methods in a class with the specified parameter types.
	 *
	 * The return type is optional, it will not be compared if it is {@code null}.
	 * Use {@code void.class} if you want to search for methods returning nothing.
	 */
	public static Method[] findMethodsByExactParameters(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
		return RefrectHelpers.findMethodsByExactParameters(clazz, returnType, parameterTypes);
	}

	/**
	 * Look up a method in a class and set it to accessible. The result is cached.
	 * This does not only look for exact matches, but for the closest match.
	 * If the method was not found, a {@link NoSuchMethodError} will be thrown.
	 * @see MethodUtils#getMatchingAccessibleMethod
	 */
	public static Method findMethodBestMatch(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return RefrectHelpers.findMethodBestMatch(clazz, methodName, parameterTypes);
	}

	/**
	 * Look up a method in a class and set it to accessible. Parameter types are
	 * determined from the <code>args</code> for the method call. The result is cached.
	 * This does not only look for exact matches, but for the closest match.
	 * If the method was not found, a {@link NoSuchMethodError} will be thrown.
	 */
	public static Method findMethodBestMatch(Class<?> clazz, String methodName, Object... args) {
		return RefrectHelpers.findMethodBestMatch(clazz, methodName, args);
	}

	/**
	 * Look up a method in a class and set it to accessible. Parameter types are
	 * preferably taken from the <code>parameterTypes</code>. Any item in this array that
	 * is <code>null</code> is determined from the corresponding item in <code>args</code>.
	 * The result is cached.
	 * This does not only look for exact matches, but for the closest match.
	 * If the method was not found, a {@link NoSuchMethodError} will be thrown.
	 */
	public static Method findMethodBestMatch(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] args) {
		return RefrectHelpers.findMethodBestMatch(clazz, methodName, parameterTypes, args);
	}

	/**
	 * Return an array with the classes of the given objects
	 */
	public static Class<?>[] getParameterTypes(Object... args) {
		return RefrectHelpers.getParameterTypes(args);
	}


	/**
	 * Return an array with the classes of the given objects
	 */
	public static Class<?>[] getClassesAsArray(Class<?>... clazzes) {
		return RefrectHelpers.getClassesAsArray(clazzes);
	}



	/**
	 * Return a constructor by class and parameter types
	 * @param clazz
	 * @param parameterTypes
	 * @return
	 */
	public static Constructor<?> findConstructorExact(Class<?> clazz, Object... parameterTypes) {
		return RefrectHelpers.findConstructorExact(clazz, parameterTypes);
	}

	/**
	 * @see <a>findConstructorExact(Class<?> clazz, Object... parameterTypes)</>
	 * @param className
	 * @param classLoader
	 * @param parameterTypes
	 * @return
	 */
	public static Constructor<?> findConstructorExact(String className, ClassLoader classLoader, Object... parameterTypes) {
		return RefrectHelpers.findConstructorExact(className,classLoader,parameterTypes);
	}

	/**
	 * @see <a>findConstructorExact(Class<?> clazz, Object... parameterTypes)</>
	 * @param clazz
	 * @param parameterTypes
	 * @return
	 */
	public static Constructor<?> findConstructorExact(Class<?> clazz, Class<?>... parameterTypes) {
		return RefrectHelpers.findConstructorExact(clazz, parameterTypes);
	}


	/**
	 * @see <a>findConstructorExact(Class<?> clazz, Object... parameterTypes)</>
	 * @param clazz
	 * @param parameterTypes
	 * @return
	 */
	public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Class<?>... parameterTypes) {
		return RefrectHelpers.findConstructorBestMatch(clazz, parameterTypes);
	}

	/**
	 * @see <a>findConstructorExact(Class<?> clazz, Object... parameterTypes)</>
	 * @param clazz
	 * @param args
	 * @return
	 */
	public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Object... args) {
		return RefrectHelpers.findConstructorBestMatch(clazz, args);
	}

	/**
	 * @see <a>findConstructorExact(Class<?> clazz, Object... parameterTypes)</>
	 * @param clazz
	 * @param parameterTypes
	 * @param args
	 * @return
	 */
	public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Class<?>[] parameterTypes, Object[] args) {
		return RefrectHelpers.findConstructorBestMatch(clazz, parameterTypes,args);
	}


	/******************************set field*******************************************/
	
	/**
	 * set a value to object field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setObjectField(Object obj, String fieldName, Object value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setObjectField(obj, fieldName, value);
	}

	/**
	 * set a value to boolean field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setBooleanField(Object obj, String fieldName, boolean value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setBooleanField(obj, fieldName, value);
	}

	/**
	 * set a value to byte field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setByteField(Object obj, String fieldName, byte value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setByteField(obj, fieldName, value);
	}

	/**
	 * set a value to char field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setCharField(Object obj, String fieldName, char value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setCharField(obj, fieldName, value);
	}

	/**
	 * set a value to double field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setDoubleField(Object obj, String fieldName, double value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setDoubleField(obj, fieldName, value);
	}

	/**
	 * set a value to float field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setFloatField(Object obj, String fieldName, float value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setFloatField(obj, fieldName, value);
	}

	/**
	 * set a value to int field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setIntField(Object obj, String fieldName, int value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setIntField(obj, fieldName, value);
	}

	/**
	 * set a value to long field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setLongField(Object obj, String fieldName, long value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setLongField(obj, fieldName, value);
	}

	/**
	 * set a value to short field
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setShortField(Object obj, String fieldName, short value) {
		if(obj == null){
			return;
		}
		RefrectHelpers.setShortField(obj, fieldName, value);
	}

	/***************************get field*****************************/
	/**
	 * get Object field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static Object getObjectField(Object obj, String fieldName) {
		
		return RefrectHelpers.getObjectField(obj, fieldName);
	}
	
	public static Object getObjectField(Object obj,Field field) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalAccessException e) {
			// should not happen
			throw new IllegalAccessError(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}
	
	/**
	 * get all enum constants
	 * @param enumClz
	 * @param enumName
	 * @return
	 */
	public static Object[] getEnumFields(Class<?> enumClz){
		if(enumClz == null){
			return null;
		}
		Object[] enumObjs = enumClz.getEnumConstants();
		if(enumObjs == null || enumObjs.length==0){
			return null;
		}
		return enumObjs;
	}
	

	/** 
	 * For inner classes, return the "this" reference of the surrounding class 
	 * */
	public static Object getSurroundingThis(Object obj) {
		return RefrectHelpers.getSurroundingThis(obj);
	}

	/**
	 *  get boolean field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static boolean getBooleanField(Object obj, String fieldName) {
		return RefrectHelpers.getBooleanField(obj, fieldName);
	}

	/**
	 *  get byte field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static byte getByteField(Object obj, String fieldName) {
		return RefrectHelpers.getByteField(obj, fieldName);
	}

	/**
	 *  get char field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static char getCharField(Object obj, String fieldName) {
		return RefrectHelpers.getCharField(obj, fieldName);
	}

	/**
	 *  get double field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static double getDoubleField(Object obj, String fieldName) {
		return RefrectHelpers.getDoubleField(obj, fieldName);
	}

	/**
	 *  get float field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static float getFloatField(Object obj, String fieldName) {
		return RefrectHelpers.getFloatField(obj, fieldName);
	}

	/**
	 *  get int field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static int getIntField(Object obj, String fieldName) {
		return RefrectHelpers.getIntField(obj, fieldName);
	}
	
	public static int getIntField(Object obj, Field field) {
		try {
			field.setAccessible(true);
			return field.getInt(obj);
		} catch (IllegalAccessException e) {
			// should not happen
			throw new IllegalAccessError(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}
	public static boolean getBooleanField(Object obj, Field field) {
		try {
			field.setAccessible(true);
			return field.getBoolean(obj);
		} catch (IllegalAccessException e) {
			// should not happen
			throw new IllegalAccessError(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}
	public static float getFloatField(Object obj, Field field) {
		try {
			field.setAccessible(true);
			return field.getFloat(obj);
		} catch (IllegalAccessException e) {
			// should not happen
			throw new IllegalAccessError(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}

	/**
	 *  get long field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static long getLongField(Object obj, String fieldName) {
		return RefrectHelpers.getLongField(obj, fieldName);
	}

	/**
	 *  get short field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static short getShortField(Object obj, String fieldName) {
		return RefrectHelpers.getShortField(obj, fieldName);
	}

	/********************set static field****************************************/
	/**
	 * set object value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticObjectField(Class<?> clazz, String fieldName, Object value) {
		 RefrectHelpers.setStaticObjectField(clazz, fieldName, value);
	}

	/**
	 * set boolean value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticBooleanField(Class<?> clazz, String fieldName, boolean value) {
		RefrectHelpers.setStaticBooleanField(clazz, fieldName, value);
	}

	/**
	 * set object byte to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticByteField(Class<?> clazz, String fieldName, byte value) {
		RefrectHelpers.setStaticByteField(clazz, fieldName, value);
	}

	/**
	 * set char value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticCharField(Class<?> clazz, String fieldName, char value) {
		RefrectHelpers.setStaticCharField(clazz, fieldName, value);
	}

	/**
	 * set double value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticDoubleField(Class<?> clazz, String fieldName, double value) {
		RefrectHelpers.setStaticDoubleField(clazz, fieldName, value);
	}

	/**
	 * set float value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticFloatField(Class<?> clazz, String fieldName, float value) {
		RefrectHelpers.setStaticFloatField(clazz, fieldName, value);
	}

	/**
	 * set int value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticIntField(Class<?> clazz, String fieldName, int value) {
		RefrectHelpers.setStaticIntField(clazz, fieldName, value);
	}

	/**
	 * set long value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticLongField(Class<?> clazz, String fieldName, long value) {
		RefrectHelpers.setStaticLongField(clazz, fieldName, value);
	}

	/**
	 * set short value to static field
	 * @param clazz
	 * @param fieldName
	 * @param value
	 */
	public static void setStaticShortField(Class<?> clazz, String fieldName, short value) {
		RefrectHelpers.setStaticShortField(clazz, fieldName, value);
	}

	/*********************get static field*******************/
	/**
	 * get Object static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static Object getStaticObjectField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticObjectField(clazz, fieldName);
	}

	/**
	 *  get boolean static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static boolean getStaticBooleanField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticBooleanField(clazz, fieldName);
	}

	/**
	 *  get byte static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static byte getStaticByteField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticByteField(clazz, fieldName);
	}

	/**
	 *  get char static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static char getStaticCharField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticCharField(clazz, fieldName);
	}

	/**
	 *  get double static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static double getStaticDoubleField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticDoubleField(clazz, fieldName);
	}

	/**
	 *  get float static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static float getStaticFloatField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticFloatField(clazz, fieldName);
	}

	/**
	 *  get int static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static int getStaticIntField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticIntField(clazz, fieldName);
	}

	/**
	 *  get long static field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static long getStaticLongField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticLongField(clazz, fieldName);
	}

	/**
	 * get short static field
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static short getStaticShortField(Class<?> clazz, String fieldName) {
		return RefrectHelpers.getStaticShortField(clazz, fieldName);
	}

	/*********************call methods*************************/
	
	/**
	 * Call instance or static method <code>methodName</code> for object <code>obj</code> with the arguments
	 * <code>args</code>. The types for the arguments will be determined automaticall from <code>args</code>
	 */
	public static Object callMethod(Object obj, String methodName, Object... args) {
		return RefrectHelpers.callMethod(obj, methodName, args);
	}
	
	public static Object callMethod( boolean callSuper,Object obj, String methodName,Object... args) {
		if(callSuper){
			Log.e(TAG, "CallSuper:"+callSuper+" :"+obj.getClass().getSuperclass().getName());
		return RefrectHelpers.callSuperMethod(obj, methodName,callSuper, args);
		}
		return RefrectHelpers.callMethod(obj, methodName, args); 
	}
	
	public static Object callMethod(Object obj,Class<?> clz,String methodName,Object...args){
		Object result = null;
		try{
			Method method = RefrectHelpers.findMethodBestMatch(clz, methodName, RefrectHelpers.getParameterTypes(args));
		if(method != null){
			method.setAccessible(true);
			result = method.invoke(obj, args);
		}
		}catch(Exception e){
			
		}
		return result;
	}
	
	public static Object callMethod(Object obj,Method method,Object...args){
		try {
			method.setAccessible(true);
			return method.invoke(obj, args);
		} catch (IllegalAccessException e) {
			// should not happen
			throw new IllegalAccessError(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw new InvocationTargetError(e.getCause());
		}
		
	}
	/**
	 * Call instance or static method <code>methodName</code> for object <code>obj</code> with the arguments
	 * <code>args</code>. The types for the arguments will be taken from <code>parameterTypes</code>.
	 * This array can have items that are <code>null</code>. In this case, the type for this parameter
	 * is determined from <code>args</code>.
	 */
	public static Object callMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
		return RefrectHelpers.callMethod(obj, methodName, parameterTypes, args);
	}

	/**
	 * Call static method <code>methodName</code> for class <code>clazz</code> with the arguments
	 * <code>args</code>. The types for the arguments will be determined automaticall from <code>args</code>
	 */
	public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
		return RefrectHelpers.callStaticMethod(clazz, methodName, args);
	}

	public static Object callStaticMethod(Method method, Object... args) {
		try {
			method.setAccessible(true);
			return method.invoke(null, args);
		} catch (IllegalAccessException e) {
			// should not happen
			throw new IllegalAccessError(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw new InvocationTargetError(e.getCause());
		}
	}
	/**
	 * Call static method <code>methodName</code> for class <code>clazz</code> with the arguments
	 * <code>args</code>. The types for the arguments will be taken from <code>parameterTypes</code>.
	 * This array can have items that are <code>null</code>. In this case, the type for this parameter
	 * is determined from <code>args</code>.
	 */
	public static Object callStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
		return RefrectHelpers.callStaticMethod(clazz, methodName, parameterTypes, args);
	}

	
	/*********************new instance an object**********************/

	/**
	 * get an instance of a class that you want
	 * @param clazz
	 * @param args
	 * @return
	 */
	public static Object newInstance(Class<?> clazz, Object... args) {
		return RefrectHelpers.newInstance(clazz, args);
	}

	/**
	 * @see <a>Object newInstance(Class<?> clazz, Object... args)</a>
	 * @param clazz
	 * @param parameterTypes
	 * @param args
	 * @return
	 */
	public static Object newInstance(Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
		return RefrectHelpers.newInstance(clazz, parameterTypes, args);
	}

}

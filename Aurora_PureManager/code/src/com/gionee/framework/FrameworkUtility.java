package com.gionee.framework;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.util.Log;

public class FrameworkUtility {

    private static final String TAG = "FrameworkUtility";

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "not found Class: " + className);
            return false;
        }
    }

    public static Class<?> createClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "createClass()" + e.getMessage());
        }
        return null;
    }

    public static Object getStaticFieldValue(Class<?> cls, String fieldName) {
        return getFieldValue(cls, null, fieldName);
    }

    public static Object getFieldValue(Class<?> cls, Object obj, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            return field.get(obj);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }

        return null;
    }

    public static Method getMethod(String className, String methodName, Class<?>... parameterTypes) {
        Class<?> storageVolumeClass = FrameworkUtility.createClass(className);
        if (storageVolumeClass == null) {
            return null;
        } else {
            return getMethod(storageVolumeClass, methodName, parameterTypes);
        }
    }

    public static Method getMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        try {
            return cls.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException: [" + methodName + "] in Class:" + cls.getName());
            return null;
        }
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName) {
        return invokeMethod(cls, null, methodName, null, null);
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Class<?>[] parasTypes,
            Object[] parasObjs) {
        return invokeMethod(cls, null, methodName, parasTypes, parasObjs);
    }

    public static Object invokeMethod(Class<?> cls, Object obj, String methodName) {
        return invokeMethod(cls, obj, methodName, null, null);
    }

    public static Object invokeMethod(Class<?> cls, Object obj, String methodName, Class<?>[] parasTypes,
            Object[] parasObjs) {
        Method method = getMethod(cls, methodName, parasTypes);
        try {
            if (method != null) {
                return method.invoke(obj, parasObjs);
            }
        } catch (Exception e) {

            e.printStackTrace();
            Log.w(TAG, "invokeStaticMethod() Exception: " + e.getLocalizedMessage());

            for (StackTraceElement ele : e.getStackTrace()) {
                Log.w(TAG, ele.toString());
            }

        }
        return null;
    }
}

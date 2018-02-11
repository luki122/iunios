package com.gionee.settings.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class ReflectHelper {

    public static Object getObjectByConstructor(String className) {

        Object returnObj = null;
        try {
            @SuppressWarnings("rawtypes")
            Class classType = Class.forName(className);
            returnObj = classType.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returnObj;
    }
    
    /**
     * 通过构造器取得实例
     * @param className 类的全限定名
     * @param intArgsClass 构造函数的参数类型
     * @param intArgs 构造函数的参数值
     * 
     * @return Object 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object getObjectByConstructor(String className, Class[] intArgsClass, Object[] intArgs) {

        Object returnObj = null;
        try {
            Class classType = Class.forName(className);
            // 找到指定的构造方法
            Constructor constructor = classType.getDeclaredConstructor(intArgsClass);
            // 设置安全检查，访问私有构造函数必须
            constructor.setAccessible(true);
            returnObj = constructor.newInstance(intArgs);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returnObj;
    }
    
    /**
     * 判断成员变量是否存在
     * @param Object 修改对象
     * @param filedName 指定成员变量名
     *
     * @return 
     */
    @SuppressWarnings("rawtypes")
    public static boolean isExistField(Object object, String filedName) {
        
        try {
            Class classType = object.getClass();
            classType.getDeclaredField(filedName);
        } catch (NoSuchFieldException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static boolean isExistPackage(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }
    
    public static boolean isExistClass(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
    
    /**
     * 修改成员变量的值
     * @param Object 修改对象
     * @param filedName 指定成员变量名
     * @param filedValue 修改的值
     *
     * @return 
     */
    @SuppressWarnings("rawtypes")
    public static void modifyFileValue(Object object, String filedName,
                                       String filedValue) {
        Class classType = object.getClass();
        Field fild = null;
        try {
            fild = classType.getDeclaredField(filedName);
            // 设置安全检查，访问私有成员变量必须
            fild.setAccessible(true);
            fild.set(object, filedValue);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 访问类成员变量
     * @param Object 访问对象
     * @param filedName 指定成员变量名
     * @return Object 取得的成员变量的值
     * */
    @SuppressWarnings("rawtypes")
    public static Object getFileValue(Object object, String filedName) {
        Class classType = object.getClass();
        Field fild = null;
        Object fildValue = null;
        try {
            fild = classType.getDeclaredField(filedName);
            // 设置安全检查，访问私有成员变量必须
            fild.setAccessible(true);
            fildValue = fild.get(object);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fildValue;
    }
    
    public static Object invokeMethod(Object object, String methodName) {
        return invokeMethod(object, methodName, null, null);
    }

    /**
     * 调用类的方法，包括私有
     * @param Object 访问对象
     * @param methodName 指定成员变量名
     * @param type 方法参数类型
     * @param value 方法参数
     * @return Object 方法的返回结果对象
     * */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object invokeMethod(Object object, String methodName,
                                   Class[] type, Object[] value) {
        Class classType = object.getClass();
        Method method = null;
        Object fildValue = null;
        try {
            if (type == null) {
                method = classType.getDeclaredMethod(methodName);
            } else {
                method = classType.getDeclaredMethod(methodName, type);
            }
            // 设置安全检查，访问私有成员方法必须
            method.setAccessible(true);
            fildValue = method.invoke(object, value);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fildValue;
    }
}

/*
    public static void main(String[] args) {
        RunBing runbing = new RunBing();
        runbing.setName("aaa");

        //访问成员变量
        String value = (String) ReflectHelper.getFileValue(runbing, "sex");
        System.out.println("value:" + value);
        
        //修改成员变量
        ReflectHelp.modifyFileValue(runbing, "sex", "bbb");
        System.out.println("value:" + runbing.getName());
        
        //检查修改后的变量值
        value = (String) ReflectHelper.getFileValue(runbing, "sex");
        System.out.println("value:" + value);

        //调用方法
        value = (String) ReflectHelper.invokeMethod(runbing, "add", new Class[] {},
                                               new Class[] {});
        System.out.println("value:" + value);

        //使用指定构造函数
        Class[] inArgs = new Class[]{String.class};
        Object[] inArgsParms = new Object[]{"hanj"};
        RunBing runBing2 = (RunBing)ReflectHelper.getObjectByConstructor("com.ccit.hj.reflect.RunBing",inArgs,inArgsParms);
        value = (String) ReflectHelper.getFileValue(runBing2, "name");
        System.out.println("cc -- value:" + value);
    }
*/
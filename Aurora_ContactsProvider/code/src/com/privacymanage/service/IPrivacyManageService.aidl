package com.privacymanage.service;

import com.privacymanage.data.AidlAccountData;

interface IPrivacyManageService {      
    /**
     * 设置指定模块的隐私数据的个数
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     * @param num 个数
     * @param accountId 
     */
    void setPrivacyNum(String pkgName,String className,int num,long accountId);
    
    /**
     * 获取当前的账户信息
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     */
    AidlAccountData getCurrentAccount(String pkgName,String className);
    
    /**
     * 获取所有隐私账户的id
     */
    long[] getAllAccountId();
    
    /**
     * 重置指定模块所有隐私空间下的隐私个数
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     */
    void resetPrivacyNumOfAllAccount(String pkgName,String className);
 }

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/james/MtkIcs/gionee_apps_ics_65x5/packages/apps/Contacts/src/com/gionee/aora/numarea/export/INumAreaManager.aidl
 */
package com.gionee.aora.numarea.export;
/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file INumAreaManager.aidl
 * 摘要:来电归属地管理类,主要提供两个接口,由归属地的Service产生
 *
 * @author yewei
 * @data 2011-5-20
 * @version 
 *
 */
public interface INumAreaManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.gionee.aora.numarea.export.INumAreaManager
{
private static final java.lang.String DESCRIPTOR = "com.gionee.aora.numarea.export.INumAreaManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.gionee.aora.numarea.export.INumAreaManager interface,
 * generating a proxy if needed.
 */
public static com.gionee.aora.numarea.export.INumAreaManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.gionee.aora.numarea.export.INumAreaManager))) {
return ((com.gionee.aora.numarea.export.INumAreaManager)iin);
}
return new com.gionee.aora.numarea.export.INumAreaManager.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getNumAreaInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.gionee.aora.numarea.export.NumAreaInfo _result = this.getNumAreaInfo(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getAreaNumInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
com.gionee.aora.numarea.export.NumAreaInfo[] _result = this.getAreaNumInfo(_arg0, _arg1);
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_getComAreaNumInfo:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.NumAreaInfo[] _result = this.getComAreaNumInfo();
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_updataDB:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.INumAreaObserver _arg0;
_arg0 = com.gionee.aora.numarea.export.INumAreaObserver.Stub.asInterface(data.readStrongBinder());
this.updataDB(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_cancelUpdata:
{
data.enforceInterface(DESCRIPTOR);
this.cancelUpdata();
reply.writeNoException();
return true;
}
case TRANSACTION_registObserver:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.INumAreaObserver _arg0;
_arg0 = com.gionee.aora.numarea.export.INumAreaObserver.Stub.asInterface(data.readStrongBinder());
boolean _result = this.registObserver(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_unregistObserver:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.INumAreaObserver _arg0;
_arg0 = com.gionee.aora.numarea.export.INumAreaObserver.Stub.asInterface(data.readStrongBinder());
boolean _result = this.unregistObserver(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.gionee.aora.numarea.export.INumAreaManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
	 * 根据号码获取归属地信息
	 * @param aPhoneNum
	 * 输入的号码
	 * @return
	 * 归属地信息
	 */
public com.gionee.aora.numarea.export.NumAreaInfo getNumAreaInfo(java.lang.String aPhoneNum) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.gionee.aora.numarea.export.NumAreaInfo _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(aPhoneNum);
mRemote.transact(Stub.TRANSACTION_getNumAreaInfo, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.gionee.aora.numarea.export.NumAreaInfo.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
// Gionee:wangth 20120821 add for CR00678951 begin
catch (Exception exception) {
    exception.printStackTrace();
    return null;
}
// Gionee:wangth 20120821 add for CR00678951 end
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * 根据输入字符串查找号码归属地
	 * @param aArea
	 *@param aTag
	 * 地区
	 * @return
	 * 存放归属地数据的List
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getAreaNumInfo(java.lang.String aArea, java.lang.String aTag) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.gionee.aora.numarea.export.NumAreaInfo[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(aArea);
_data.writeString(aTag);
mRemote.transact(Stub.TRANSACTION_getAreaNumInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(com.gionee.aora.numarea.export.NumAreaInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * 获取常用号码列表
	 * @param aArea
	 * 地区
	 * @return
	 * 存放归属地数据的List
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getComAreaNumInfo() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.gionee.aora.numarea.export.NumAreaInfo[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getComAreaNumInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(com.gionee.aora.numarea.export.NumAreaInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * 更新归属地数据库
	 * @param aObserver
	 * 侦听更新结果的观察者
	 */
public void updataDB(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((aObserver!=null))?(aObserver.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_updataDB, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 取消更新
	 */
public void cancelUpdata() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelUpdata, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 注册一个观察者
	 * @param aObserver
	 * 观察者对象
	 * @return
	 * 假如观察者列表中没有此对象并添加成功.返回true,其余情况为false
	 */
public boolean registObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((aObserver!=null))?(aObserver.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registObserver, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * 注销一个观察者
	 * @param aObserver
	 * 观察者对象
	 * @return
	 * 假如观察者列表中有此对象并删除成功,返回true,其余情况为false
	 */
public boolean unregistObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((aObserver!=null))?(aObserver.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregistObserver, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getNumAreaInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getAreaNumInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getComAreaNumInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_updataDB = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_cancelUpdata = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_registObserver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_unregistObserver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
/**
	 * 根据号码获取归属地信息
	 * @param aPhoneNum
	 * 输入的号码
	 * @return
	 * 归属地信息
	 */
public com.gionee.aora.numarea.export.NumAreaInfo getNumAreaInfo(java.lang.String aPhoneNum) throws android.os.RemoteException;
/**
	 * 根据输入字符串查找号码归属地
	 * @param aArea
	 *@param aTag
	 * 地区
	 * @return
	 * 存放归属地数据的List
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getAreaNumInfo(java.lang.String aArea, java.lang.String aTag) throws android.os.RemoteException;
/**
	 * 获取常用号码列表
	 * @param aArea
	 * 地区
	 * @return
	 * 存放归属地数据的List
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getComAreaNumInfo() throws android.os.RemoteException;
/**
	 * 更新归属地数据库
	 * @param aObserver
	 * 侦听更新结果的观察者
	 */
public void updataDB(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException;
/**
	 * 取消更新
	 */
public void cancelUpdata() throws android.os.RemoteException;
/**
	 * 注册一个观察者
	 * @param aObserver
	 * 观察者对象
	 * @return
	 * 假如观察者列表中没有此对象并添加成功.返回true,其余情况为false
	 */
public boolean registObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException;
/**
	 * 注销一个观察者
	 * @param aObserver
	 * 观察者对象
	 * @return
	 * 假如观察者列表中有此对象并删除成功,返回true,其余情况为false
	 */
public boolean unregistObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException;
}
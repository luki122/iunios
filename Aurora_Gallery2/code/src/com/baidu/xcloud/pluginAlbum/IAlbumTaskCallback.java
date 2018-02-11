/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/pluginAlbum/IAlbumTaskCallback.aidl
 */
package com.baidu.xcloud.pluginAlbum;
/**
 * 任务回调接口定义
 * 
 */
public interface IAlbumTaskCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback))) {
return ((com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback)iin);
}
return new com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
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
case TRANSACTION_onGetTaskStatus:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onGetTaskStatus(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_progressInterval:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.progressInterval();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_onGetTaskListFinished:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean> _arg0;
_arg0 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean.CREATOR);
this.onGetTaskListFinished(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onXcloudError:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onXcloudError(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
	 * 任务消息(任务的状态等,具体参见FileTaskStatusBean的定义)
	 * 
	 */
@Override public void onGetTaskStatus(com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean fileTaskStatusBean) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((fileTaskStatusBean!=null)) {
_data.writeInt(1);
fileTaskStatusBean.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onGetTaskStatus, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 设置更新进度的间隔时间。 默认间隔为2000ms, 允许的最小时间间隔是100ms。
	 * 
	 */
@Override public long progressInterval() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_progressInterval, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	  *任务列表
	  * 
	  */
@Override public void onGetTaskListFinished(java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean> fileTaskStatusBeanList) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(fileTaskStatusBeanList);
mRemote.transact(Stub.TRANSACTION_onGetTaskListFinished, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	  * xcloud 框架错误
	  */
@Override public void onXcloudError(int errorCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(errorCode);
mRemote.transact(Stub.TRANSACTION_onXcloudError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onGetTaskStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_progressInterval = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onGetTaskListFinished = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onXcloudError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
/**
	 * 任务消息(任务的状态等,具体参见FileTaskStatusBean的定义)
	 * 
	 */
public void onGetTaskStatus(com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean fileTaskStatusBean) throws android.os.RemoteException;
/**
	 * 设置更新进度的间隔时间。 默认间隔为2000ms, 允许的最小时间间隔是100ms。
	 * 
	 */
public long progressInterval() throws android.os.RemoteException;
/**
	  *任务列表
	  * 
	  */
public void onGetTaskListFinished(java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean> fileTaskStatusBeanList) throws android.os.RemoteException;
/**
	  * xcloud 框架错误
	  */
public void onXcloudError(int errorCode) throws android.os.RemoteException;
}

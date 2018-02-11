/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/pluginAlbum/IAlbumSetupCallback.aidl
 */
package com.baidu.xcloud.pluginAlbum;
/**
 * ICloudStorage 的初始化回调接口
 * 
 */
public interface IAlbumSetupCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback))) {
return ((com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback)iin);
}
return new com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback.Stub.Proxy(obj);
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
case TRANSACTION_setupFileDescriptor:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> _arg0;
_arg0 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo.CREATOR);
this.setupFileDescriptor(_arg0);
reply.writeNoException();
reply.writeTypedList(_arg0);
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
private static class Proxy implements com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback
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
	 * 设置FileDescriptor，解决跨进程文件访问权限问题
	 */
@Override public void setupFileDescriptor(java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> fileUpDownloadInfoList) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(fileUpDownloadInfoList);
mRemote.transact(Stub.TRANSACTION_setupFileDescriptor, _data, _reply, 0);
_reply.readException();
_reply.readTypedList(fileUpDownloadInfoList, com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo.CREATOR);
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
static final int TRANSACTION_setupFileDescriptor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onXcloudError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
	 * 设置FileDescriptor，解决跨进程文件访问权限问题
	 */
public void setupFileDescriptor(java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> fileUpDownloadInfoList) throws android.os.RemoteException;
/**
	  * xcloud 框架错误
	  */
public void onXcloudError(int errorCode) throws android.os.RemoteException;
}

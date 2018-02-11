/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/account/IBindDetailListener.aidl
 */
package com.baidu.xcloud.account;
public interface IBindDetailListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.account.IBindDetailListener
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.account.IBindDetailListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.account.IBindDetailListener interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.account.IBindDetailListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.account.IBindDetailListener))) {
return ((com.baidu.xcloud.account.IBindDetailListener)iin);
}
return new com.baidu.xcloud.account.IBindDetailListener.Stub.Proxy(obj);
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
case TRANSACTION_onBindResult:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AuthResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AuthResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onBindResult(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onException:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onException(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.baidu.xcloud.account.IBindDetailListener
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
@Override public void onBindResult(com.baidu.xcloud.account.AuthResponse response) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((response!=null)) {
_data.writeInt(1);
response.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onBindResult, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onException(java.lang.String errorMsg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(errorMsg);
mRemote.transact(Stub.TRANSACTION_onException, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onBindResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onException = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void onBindResult(com.baidu.xcloud.account.AuthResponse response) throws android.os.RemoteException;
public void onException(java.lang.String errorMsg) throws android.os.RemoteException;
}

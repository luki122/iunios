/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/account/IAuthLoginListener.aidl
 */
package com.baidu.xcloud.account;
/**
 * Interface, the call back for OAuth listener.
 */
public interface IAuthLoginListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.account.IAuthLoginListener
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.account.IAuthLoginListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.account.IAuthLoginListener interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.account.IAuthLoginListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.account.IAuthLoginListener))) {
return ((com.baidu.xcloud.account.IAuthLoginListener)iin);
}
return new com.baidu.xcloud.account.IAuthLoginListener.Stub.Proxy(obj);
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
case TRANSACTION_onComplete:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AuthResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AuthResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onComplete(_arg0);
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
case TRANSACTION_onCancel:
{
data.enforceInterface(DESCRIPTOR);
this.onCancel();
reply.writeNoException();
return true;
}
case TRANSACTION_onStateChanged:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onStateChanged(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.baidu.xcloud.account.IAuthLoginListener
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
	 * When login completed successfully, this method will be called.
	 * 
	 * @param response
	 *            
	 */
@Override public void onComplete(com.baidu.xcloud.account.AuthResponse response) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_onComplete, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * When Exception happens, this method will be called.
	 * 
	 * @param errorMsg
	 *            Error message.
	 */
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
/**
	 * When user canceled login, this method will be called.
	 */
@Override public void onCancel() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onCancel, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
      * When login state changed.
      * 
      * @param state : 1  check third token
      *                2  page start
      *                3  page finish
      *                4  login in back
      *                5  start coupon
      */
@Override public void onStateChanged(int state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
mRemote.transact(Stub.TRANSACTION_onStateChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onComplete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onException = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onCancel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onStateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
/**
	 * When login completed successfully, this method will be called.
	 * 
	 * @param response
	 *            
	 */
public void onComplete(com.baidu.xcloud.account.AuthResponse response) throws android.os.RemoteException;
/**
	 * When Exception happens, this method will be called.
	 * 
	 * @param errorMsg
	 *            Error message.
	 */
public void onException(java.lang.String errorMsg) throws android.os.RemoteException;
/**
	 * When user canceled login, this method will be called.
	 */
public void onCancel() throws android.os.RemoteException;
/**
      * When login state changed.
      * 
      * @param state : 1  check third token
      *                2  page start
      *                3  page finish
      *                4  login in back
      *                5  start coupon
      */
public void onStateChanged(int state) throws android.os.RemoteException;
}

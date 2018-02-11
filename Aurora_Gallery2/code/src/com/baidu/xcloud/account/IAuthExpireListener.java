/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/account/IAuthExpireListener.aidl
 */
package com.baidu.xcloud.account;
/**
 * Callback interface for logout.
 * 
 */
public interface IAuthExpireListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.account.IAuthExpireListener
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.account.IAuthExpireListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.account.IAuthExpireListener interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.account.IAuthExpireListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.account.IAuthExpireListener))) {
return ((com.baidu.xcloud.account.IAuthExpireListener)iin);
}
return new com.baidu.xcloud.account.IAuthExpireListener.Stub.Proxy(obj);
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
case TRANSACTION_onResult:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.onResult(_arg0);
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
private static class Proxy implements com.baidu.xcloud.account.IAuthExpireListener
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
	 * When logout completed, this method will be called..
	 * 
	 * @param result
	 *            true represent success or false represent failed
	 * 
	 */
@Override public void onResult(boolean result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((result)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_onResult, _data, _reply, 0);
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
}
static final int TRANSACTION_onResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onException = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
	 * When logout completed, this method will be called..
	 * 
	 * @param result
	 *            true represent success or false represent failed
	 * 
	 */
public void onResult(boolean result) throws android.os.RemoteException;
/**
     * When Exception happens, this method will be called.
     * 
     * @param errorMsg
     *            Error message.
     */
public void onException(java.lang.String errorMsg) throws android.os.RemoteException;
}

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/account/IAuth.aidl
 */
package com.baidu.xcloud.account;
public interface IAuth extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.account.IAuth
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.account.IAuth";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.account.IAuth interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.account.IAuth asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.account.IAuth))) {
return ((com.baidu.xcloud.account.IAuth)iin);
}
return new com.baidu.xcloud.account.IAuth.Stub.Proxy(obj);
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
case TRANSACTION_startAuth:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AuthInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AuthInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
com.baidu.xcloud.account.IAuthLoginListener _arg1;
_arg1 = com.baidu.xcloud.account.IAuthLoginListener.Stub.asInterface(data.readStrongBinder());
this.startAuth(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_expireToken:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.baidu.xcloud.account.IAuthExpireListener _arg1;
_arg1 = com.baidu.xcloud.account.IAuthExpireListener.Stub.asInterface(data.readStrongBinder());
this.expireToken(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_queryThirdBindDetails:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
com.baidu.xcloud.account.IBindDetailListener _arg2;
_arg2 = com.baidu.xcloud.account.IBindDetailListener.Stub.asInterface(data.readStrongBinder());
this.queryThirdBindDetails(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.baidu.xcloud.account.IAuth
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
     * Start xcloud account authentication, this is an async method.
     * 
     * @param authInfo
     *            account detail information      
     * @param listener
     *            authentication result callback
     */
@Override public void startAuth(com.baidu.xcloud.account.AuthInfo authInfo, com.baidu.xcloud.account.IAuthLoginListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((authInfo!=null)) {
_data.writeInt(1);
authInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_startAuth, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Expire xcloud accout access token, this is an async method.
     * 
     * @param token
     *            the access token
     * @param listener
     *            expire token result callback
     */
@Override public void expireToken(java.lang.String accessToken, com.baidu.xcloud.account.IAuthExpireListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(accessToken);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_expireToken, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Query bind details of third account, this is an async method.
     * 
     * @param apiKey
     *            api key of app
     * @param thirdToken
     *            the third access token
     * @param listener
     *            
     */
@Override public void queryThirdBindDetails(java.lang.String apiKey, java.lang.String thirdToken, com.baidu.xcloud.account.IBindDetailListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(apiKey);
_data.writeString(thirdToken);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_queryThirdBindDetails, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_startAuth = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_expireToken = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_queryThirdBindDetails = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * Start xcloud account authentication, this is an async method.
     * 
     * @param authInfo
     *            account detail information      
     * @param listener
     *            authentication result callback
     */
public void startAuth(com.baidu.xcloud.account.AuthInfo authInfo, com.baidu.xcloud.account.IAuthLoginListener listener) throws android.os.RemoteException;
/**
     * Expire xcloud accout access token, this is an async method.
     * 
     * @param token
     *            the access token
     * @param listener
     *            expire token result callback
     */
public void expireToken(java.lang.String accessToken, com.baidu.xcloud.account.IAuthExpireListener listener) throws android.os.RemoteException;
/**
     * Query bind details of third account, this is an async method.
     * 
     * @param apiKey
     *            api key of app
     * @param thirdToken
     *            the third access token
     * @param listener
     *            
     */
public void queryThirdBindDetails(java.lang.String apiKey, java.lang.String thirdToken, com.baidu.xcloud.account.IBindDetailListener listener) throws android.os.RemoteException;
}

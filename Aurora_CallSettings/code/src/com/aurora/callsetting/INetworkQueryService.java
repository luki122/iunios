/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: packages/apps/Phone/src/com/android/phone/INetworkQueryService.aidl
 */
package com.aurora.callsetting;
/**
 * Service interface to handle queries for available networks.  The
 * Phone application lets this service interface handle carrier 
 * availability queries instead of making direct calls to the 
 * GSMPhone layer.
 */
public interface INetworkQueryService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.aurora.callsetting.INetworkQueryService
{
private static final java.lang.String DESCRIPTOR = "com.aurora.callsetting.INetworkQueryService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.aurora.callsetting.INetworkQueryService interface,
 * generating a proxy if needed.
 */
public static com.aurora.callsetting.INetworkQueryService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.aurora.callsetting.INetworkQueryService))) {
return ((com.aurora.callsetting.INetworkQueryService)iin);
}
return new com.aurora.callsetting.INetworkQueryService.Stub.Proxy(obj);
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
case TRANSACTION_startNetworkQuery:
{
data.enforceInterface(DESCRIPTOR);
com.aurora.callsetting.INetworkQueryServiceCallback _arg0;
_arg0 = com.aurora.callsetting.INetworkQueryServiceCallback.Stub.asInterface(data.readStrongBinder());
this.startNetworkQuery(_arg0);
return true;
}
case TRANSACTION_stopNetworkQuery:
{
data.enforceInterface(DESCRIPTOR);
com.aurora.callsetting.INetworkQueryServiceCallback _arg0;
_arg0 = com.aurora.callsetting.INetworkQueryServiceCallback.Stub.asInterface(data.readStrongBinder());
this.stopNetworkQuery(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.aurora.callsetting.INetworkQueryService
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
     * Starts a network query if it has not been started yet, and
     * request a callback through the INetworkQueryServiceCallback
     * object on query completion.  If there is an existing request,
     * then just add the callback to the list of notifications
     * that will be sent upon query completion.
     */
@Override public void startNetworkQuery(com.aurora.callsetting.INetworkQueryServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_startNetworkQuery, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
/**
     * Tells the service that the requested query is to be ignored.
     * This may not do anything for the Query request in the 
     * underlying RIL, but it ensures that the callback is removed
     * from the list of notifications.
     */
@Override public void stopNetworkQuery(com.aurora.callsetting.INetworkQueryServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_stopNetworkQuery, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_startNetworkQuery = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stopNetworkQuery = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Starts a network query if it has not been started yet, and
     * request a callback through the INetworkQueryServiceCallback
     * object on query completion.  If there is an existing request,
     * then just add the callback to the list of notifications
     * that will be sent upon query completion.
     */
public void startNetworkQuery(com.aurora.callsetting.INetworkQueryServiceCallback cb) throws android.os.RemoteException;
/**
     * Tells the service that the requested query is to be ignored.
     * This may not do anything for the Query request in the 
     * underlying RIL, but it ensures that the callback is removed
     * from the list of notifications.
     */
public void stopNetworkQuery(com.aurora.callsetting.INetworkQueryServiceCallback cb) throws android.os.RemoteException;
}

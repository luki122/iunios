/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: packages/apps/Phone/src/org/codeaurora/ims/IImsServiceListener.aidl
 */
package org.codeaurora.ims;
/**
 * Interface used for registering callbacks with IMS Service. {@hide}
 */
public interface IImsServiceListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.codeaurora.ims.IImsServiceListener
{
private static final java.lang.String DESCRIPTOR = "org.codeaurora.ims.IImsServiceListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.codeaurora.ims.IImsServiceListener interface,
 * generating a proxy if needed.
 */
public static org.codeaurora.ims.IImsServiceListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.codeaurora.ims.IImsServiceListener))) {
return ((org.codeaurora.ims.IImsServiceListener)iin);
}
return new org.codeaurora.ims.IImsServiceListener.Stub.Proxy(obj);
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
case TRANSACTION_imsRegStateChanged:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.imsRegStateChanged(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_imsRegStateChangeReqFailed:
{
data.enforceInterface(DESCRIPTOR);
this.imsRegStateChangeReqFailed();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.codeaurora.ims.IImsServiceListener
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
     * Get the changed ims registration state
     */
@Override public void imsRegStateChanged(int regstate) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(regstate);
mRemote.transact(Stub.TRANSACTION_imsRegStateChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * IMS registration state change request failure callback
     */
@Override public void imsRegStateChangeReqFailed() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imsRegStateChangeReqFailed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_imsRegStateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_imsRegStateChangeReqFailed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Get the changed ims registration state
     */
public void imsRegStateChanged(int regstate) throws android.os.RemoteException;
/**
     * IMS registration state change request failure callback
     */
public void imsRegStateChangeReqFailed() throws android.os.RemoteException;
}

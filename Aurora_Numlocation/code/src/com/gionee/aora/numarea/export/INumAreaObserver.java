/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /media/myspace/6577/gionee_packages/packages/mtk/packages_4.0/apps/NumArea/src/com/gionee/aora/numarea/export/INumAreaObserver.aidl
 */
package com.gionee.aora.numarea.export;
public interface INumAreaObserver extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.gionee.aora.numarea.export.INumAreaObserver
{
private static final java.lang.String DESCRIPTOR = "com.gionee.aora.numarea.export.INumAreaObserver";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.gionee.aora.numarea.export.INumAreaObserver interface,
 * generating a proxy if needed.
 */
public static com.gionee.aora.numarea.export.INumAreaObserver asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.gionee.aora.numarea.export.INumAreaObserver))) {
return ((com.gionee.aora.numarea.export.INumAreaObserver)iin);
}
return new com.gionee.aora.numarea.export.INumAreaObserver.Stub.Proxy(obj);
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
case TRANSACTION_updata:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.Bundle _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.updata(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.gionee.aora.numarea.export.INumAreaObserver
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
public void updata(int aResultCode, android.os.Bundle bu) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(aResultCode);
if ((bu!=null)) {
_data.writeInt(1);
bu.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_updata, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_updata = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void updata(int aResultCode, android.os.Bundle bu) throws android.os.RemoteException;
}

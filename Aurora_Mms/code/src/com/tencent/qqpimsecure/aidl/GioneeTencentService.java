/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\sunwork\\workspace1127\\QQPhoneSecure_3.8_jinli\\src\\com\\tencent\\qqpimsecure\\aidl\\GioneeTencentService.aidl
 */
package com.tencent.qqpimsecure.aidl;
public interface GioneeTencentService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.tencent.qqpimsecure.aidl.GioneeTencentService
{
private static final java.lang.String DESCRIPTOR = "com.tencent.qqpimsecure.aidl.GioneeTencentService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.tencent.qqpimsecure.aidl.GioneeTencentService interface,
 * generating a proxy if needed.
 */
public static com.tencent.qqpimsecure.aidl.GioneeTencentService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.tencent.qqpimsecure.aidl.GioneeTencentService))) {
return ((com.tencent.qqpimsecure.aidl.GioneeTencentService)iin);
}
return new com.tencent.qqpimsecure.aidl.GioneeTencentService.Stub.Proxy(obj);
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
case TRANSACTION_insertToBlacklist:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.insertToBlacklist(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.tencent.qqpimsecure.aidl.GioneeTencentService
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
@Override public void insertToBlacklist(java.lang.String contact_name, java.lang.String phoneno) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(contact_name);
_data.writeString(phoneno);
mRemote.transact(Stub.TRANSACTION_insertToBlacklist, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_insertToBlacklist = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void insertToBlacklist(java.lang.String contact_name, java.lang.String phoneno) throws android.os.RemoteException;
}

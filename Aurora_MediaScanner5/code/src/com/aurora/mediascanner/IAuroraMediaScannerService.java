/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/project/AuroraMediaScanner/code/src/com/android/aurora/IAuroraMediaScannerService.aidl
 */
package com.aurora.mediascanner;
public interface IAuroraMediaScannerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.aurora.mediascanner.IAuroraMediaScannerService
{
private static final java.lang.String DESCRIPTOR = "com.aurora.mediascanner.IAuroraMediaScannerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.aurora.mediascanner.IAuroraMediaScannerService interface,
 * generating a proxy if needed.
 */
public static com.aurora.mediascanner.IAuroraMediaScannerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.aurora.mediascanner.IAuroraMediaScannerService))) {
return ((com.aurora.mediascanner.IAuroraMediaScannerService)iin);
}
return new com.aurora.mediascanner.IAuroraMediaScannerService.Stub.Proxy(obj);
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
case TRANSACTION_auroraRequestScanFile:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
com.aurora.mediascanner.IAuroraMediaScannerListener _arg2;
_arg2 = com.aurora.mediascanner.IAuroraMediaScannerListener.Stub.asInterface(data.readStrongBinder());
this.auroraRequestScanFile(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_auroraScanFile:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.auroraScanFile(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.aurora.mediascanner.IAuroraMediaScannerService
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
     * Requests the media scanner to scan a file.
     * @param path the path to the file to be scanned.
     * @param mimeType  an optional mimeType for the file.
     * If mimeType is null, then the mimeType will be inferred from the file extension.
     * @param listener an optional IMediaScannerListener. 
     * If specified, the caller will be notified when scanning is complete via the listener.
     */
@Override public void auroraRequestScanFile(java.lang.String path, java.lang.String mimeType, com.aurora.mediascanner.IAuroraMediaScannerListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
_data.writeString(mimeType);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_auroraRequestScanFile, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Older API, left in for backward compatibility.
     * Requests the media scanner to scan a file.
     * @param path the path to the file to be scanned.
     * @param mimeType  an optional mimeType for the file.
     * If mimeType is null, then the mimeType will be inferred from the file extension.
     */
@Override public void auroraScanFile(java.lang.String path, java.lang.String mimeType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
_data.writeString(mimeType);
mRemote.transact(Stub.TRANSACTION_auroraScanFile, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_auroraRequestScanFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_auroraScanFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Requests the media scanner to scan a file.
     * @param path the path to the file to be scanned.
     * @param mimeType  an optional mimeType for the file.
     * If mimeType is null, then the mimeType will be inferred from the file extension.
     * @param listener an optional IMediaScannerListener. 
     * If specified, the caller will be notified when scanning is complete via the listener.
     */
public void auroraRequestScanFile(java.lang.String path, java.lang.String mimeType, com.aurora.mediascanner.IAuroraMediaScannerListener listener) throws android.os.RemoteException;
/**
     * Older API, left in for backward compatibility.
     * Requests the media scanner to scan a file.
     * @param path the path to the file to be scanned.
     * @param mimeType  an optional mimeType for the file.
     * If mimeType is null, then the mimeType will be inferred from the file extension.
     */
public void auroraScanFile(java.lang.String path, java.lang.String mimeType) throws android.os.RemoteException;
}

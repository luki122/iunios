/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/project/AuroraMediaScanner/code/src/com/android/aurora/IAuroraMediaScannerListener.aidl
 */
package com.android.aurora;
public interface IAuroraMediaScannerListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.android.aurora.IAuroraMediaScannerListener
{
private static final java.lang.String DESCRIPTOR = "com.android.aurora.IAuroraMediaScannerListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.android.aurora.IAuroraMediaScannerListener interface,
 * generating a proxy if needed.
 */
public static com.android.aurora.IAuroraMediaScannerListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.android.aurora.IAuroraMediaScannerListener))) {
return ((com.android.aurora.IAuroraMediaScannerListener)iin);
}
return new com.android.aurora.IAuroraMediaScannerListener.Stub.Proxy(obj);
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
case TRANSACTION_auroraScanCompleted:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
android.net.Uri _arg1;
if ((0!=data.readInt())) {
_arg1 = android.net.Uri.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.auroraScanCompleted(_arg0, _arg1);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.android.aurora.IAuroraMediaScannerListener
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
     * Called when a IMediaScannerService.scanFile() call has completed.
     * @param path the path to the file that has been scanned.
     * @param uri the Uri for the file if the scanning operation succeeded 
     * and the file was added to the media database, or null if scanning failed. 
     */
@Override public void auroraScanCompleted(java.lang.String path, android.net.Uri uri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
if ((uri!=null)) {
_data.writeInt(1);
uri.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_auroraScanCompleted, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_auroraScanCompleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
/**
     * Called when a IMediaScannerService.scanFile() call has completed.
     * @param path the path to the file that has been scanned.
     * @param uri the Uri for the file if the scanning operation succeeded 
     * and the file was added to the media database, or null if scanning failed. 
     */
public void auroraScanCompleted(java.lang.String path, android.net.Uri uri) throws android.os.RemoteException;
}

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/lgy/Aurora/Aurora_Telecomm/code/src/com/mediatek/telecom/recording/IPhoneRecorder.aidl
 */
package com.mediatek.telecom.recording;
public interface IPhoneRecorder extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mediatek.telecom.recording.IPhoneRecorder
{
private static final java.lang.String DESCRIPTOR = "com.mediatek.telecom.recording.IPhoneRecorder";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mediatek.telecom.recording.IPhoneRecorder interface,
 * generating a proxy if needed.
 */
public static com.mediatek.telecom.recording.IPhoneRecorder asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mediatek.telecom.recording.IPhoneRecorder))) {
return ((com.mediatek.telecom.recording.IPhoneRecorder)iin);
}
return new com.mediatek.telecom.recording.IPhoneRecorder.Stub.Proxy(obj);
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
case TRANSACTION_listen:
{
data.enforceInterface(DESCRIPTOR);
com.mediatek.telecom.recording.IPhoneRecordStateListener _arg0;
_arg0 = com.mediatek.telecom.recording.IPhoneRecordStateListener.Stub.asInterface(data.readStrongBinder());
this.listen(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_remove:
{
data.enforceInterface(DESCRIPTOR);
this.remove();
reply.writeNoException();
return true;
}
case TRANSACTION_startRecord:
{
data.enforceInterface(DESCRIPTOR);
this.startRecord();
reply.writeNoException();
return true;
}
case TRANSACTION_stopRecord:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.stopRecord(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isRecording:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isRecording();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mediatek.telecom.recording.IPhoneRecorder
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
@Override public void listen(com.mediatek.telecom.recording.IPhoneRecordStateListener callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_listen, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void remove() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_remove, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void startRecord() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startRecord, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopRecord(boolean isMount) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((isMount)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_stopRecord, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean isRecording() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isRecording, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_listen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_remove = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_startRecord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_stopRecord = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_isRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void listen(com.mediatek.telecom.recording.IPhoneRecordStateListener callback) throws android.os.RemoteException;
public void remove() throws android.os.RemoteException;
public void startRecord() throws android.os.RemoteException;
public void stopRecord(boolean isMount) throws android.os.RemoteException;
public boolean isRecording() throws android.os.RemoteException;
}

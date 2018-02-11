/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/gouxiaomei/Aurora_GnMMITest/code/src/com/caf/fmradio/IFMRadioServiceCallbacks.aidl
 */
package com.caf.fmradio;
public interface IFMRadioServiceCallbacks extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.caf.fmradio.IFMRadioServiceCallbacks
{
private static final java.lang.String DESCRIPTOR = "com.caf.fmradio.IFMRadioServiceCallbacks";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.caf.fmradio.IFMRadioServiceCallbacks interface,
 * generating a proxy if needed.
 */
public static com.caf.fmradio.IFMRadioServiceCallbacks asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.caf.fmradio.IFMRadioServiceCallbacks))) {
return ((com.caf.fmradio.IFMRadioServiceCallbacks)iin);
}
return new com.caf.fmradio.IFMRadioServiceCallbacks.Stub.Proxy(obj);
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
case TRANSACTION_onEnabled:
{
data.enforceInterface(DESCRIPTOR);
this.onEnabled();
reply.writeNoException();
return true;
}
case TRANSACTION_onDisabled:
{
data.enforceInterface(DESCRIPTOR);
this.onDisabled();
reply.writeNoException();
return true;
}
case TRANSACTION_onRadioReset:
{
data.enforceInterface(DESCRIPTOR);
this.onRadioReset();
reply.writeNoException();
return true;
}
case TRANSACTION_onTuneStatusChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onTuneStatusChanged();
reply.writeNoException();
return true;
}
case TRANSACTION_onProgramServiceChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onProgramServiceChanged();
reply.writeNoException();
return true;
}
case TRANSACTION_onRadioTextChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onRadioTextChanged();
reply.writeNoException();
return true;
}
case TRANSACTION_onAlternateFrequencyChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onAlternateFrequencyChanged();
reply.writeNoException();
return true;
}
case TRANSACTION_onSignalStrengthChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onSignalStrengthChanged();
reply.writeNoException();
return true;
}
case TRANSACTION_onSearchComplete:
{
data.enforceInterface(DESCRIPTOR);
this.onSearchComplete();
reply.writeNoException();
return true;
}
case TRANSACTION_onSearchListComplete:
{
data.enforceInterface(DESCRIPTOR);
this.onSearchListComplete();
reply.writeNoException();
return true;
}
case TRANSACTION_onMute:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.onMute(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onAudioUpdate:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.onAudioUpdate(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onStationRDSSupported:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.onStationRDSSupported(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onRecordingStopped:
{
data.enforceInterface(DESCRIPTOR);
this.onRecordingStopped();
reply.writeNoException();
return true;
}
case TRANSACTION_onExtenRadioTextChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onExtenRadioTextChanged();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.caf.fmradio.IFMRadioServiceCallbacks
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
@Override public void onEnabled() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onEnabled, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onDisabled() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onDisabled, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onRadioReset() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onRadioReset, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onTuneStatusChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onTuneStatusChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onProgramServiceChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onProgramServiceChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onRadioTextChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onRadioTextChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onAlternateFrequencyChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onAlternateFrequencyChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onSignalStrengthChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSignalStrengthChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onSearchComplete() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSearchComplete, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onSearchListComplete() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSearchListComplete, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onMute(boolean bMuted) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bMuted)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_onMute, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onAudioUpdate(boolean bStereo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bStereo)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_onAudioUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onStationRDSSupported(boolean bRDSSupported) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bRDSSupported)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_onStationRDSSupported, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onRecordingStopped() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onRecordingStopped, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onExtenRadioTextChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onExtenRadioTextChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDisabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onRadioReset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onTuneStatusChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onProgramServiceChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_onRadioTextChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_onAlternateFrequencyChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_onSignalStrengthChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_onSearchComplete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_onSearchListComplete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_onMute = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_onAudioUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_onStationRDSSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_onRecordingStopped = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_onExtenRadioTextChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
}
public void onEnabled() throws android.os.RemoteException;
public void onDisabled() throws android.os.RemoteException;
public void onRadioReset() throws android.os.RemoteException;
public void onTuneStatusChanged() throws android.os.RemoteException;
public void onProgramServiceChanged() throws android.os.RemoteException;
public void onRadioTextChanged() throws android.os.RemoteException;
public void onAlternateFrequencyChanged() throws android.os.RemoteException;
public void onSignalStrengthChanged() throws android.os.RemoteException;
public void onSearchComplete() throws android.os.RemoteException;
public void onSearchListComplete() throws android.os.RemoteException;
public void onMute(boolean bMuted) throws android.os.RemoteException;
public void onAudioUpdate(boolean bStereo) throws android.os.RemoteException;
public void onStationRDSSupported(boolean bRDSSupported) throws android.os.RemoteException;
public void onRecordingStopped() throws android.os.RemoteException;
public void onExtenRadioTextChanged() throws android.os.RemoteException;
}

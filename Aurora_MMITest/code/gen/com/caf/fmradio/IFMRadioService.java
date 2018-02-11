/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/gouxiaomei/Aurora_GnMMITest/code/src/com/caf/fmradio/IFMRadioService.aidl
 */
package com.caf.fmradio;
public interface IFMRadioService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.caf.fmradio.IFMRadioService
{
private static final java.lang.String DESCRIPTOR = "com.caf.fmradio.IFMRadioService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.caf.fmradio.IFMRadioService interface,
 * generating a proxy if needed.
 */
public static com.caf.fmradio.IFMRadioService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.caf.fmradio.IFMRadioService))) {
return ((com.caf.fmradio.IFMRadioService)iin);
}
return new com.caf.fmradio.IFMRadioService.Stub.Proxy(obj);
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
case TRANSACTION_fmOn:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.fmOn();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_fmOff:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.fmOff();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_fmRadioReset:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.fmRadioReset();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isFmOn:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isFmOn();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isAnalogModeEnabled:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isAnalogModeEnabled();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isFmRecordingOn:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isFmRecordingOn();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isSpeakerEnabled:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isSpeakerEnabled();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_fmReconfigure:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.fmReconfigure();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_registerCallbacks:
{
data.enforceInterface(DESCRIPTOR);
com.caf.fmradio.IFMRadioServiceCallbacks _arg0;
_arg0 = com.caf.fmradio.IFMRadioServiceCallbacks.Stub.asInterface(data.readStrongBinder());
this.registerCallbacks(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallbacks:
{
data.enforceInterface(DESCRIPTOR);
this.unregisterCallbacks();
reply.writeNoException();
return true;
}
case TRANSACTION_mute:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.mute();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_routeAudio:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.routeAudio(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_unMute:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.unMute();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isMuted:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isMuted();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_startRecording:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.startRecording();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_stopRecording:
{
data.enforceInterface(DESCRIPTOR);
this.stopRecording();
reply.writeNoException();
return true;
}
case TRANSACTION_tune:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.tune(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_seek:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
boolean _result = this.seek(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_enableSpeaker:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.enableSpeaker(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_scan:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.scan(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_seekPI:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.seekPI(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_searchStrongStationList:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.searchStrongStationList(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getSearchList:
{
data.enforceInterface(DESCRIPTOR);
int[] _result = this.getSearchList();
reply.writeNoException();
reply.writeIntArray(_result);
return true;
}
case TRANSACTION_cancelSearch:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.cancelSearch();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getProgramService:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getProgramService();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getRadioText:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getRadioText();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getProgramType:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getProgramType();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getProgramID:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getProgramID();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setLowPowerMode:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
boolean _result = this.setLowPowerMode(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getPowerMode:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPowerMode();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_enableAutoAF:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
boolean _result = this.enableAutoAF(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_enableStereo:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
boolean _result = this.enableStereo(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isAntennaAvailable:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isAntennaAvailable();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isWiredHeadsetAvailable:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isWiredHeadsetAvailable();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isCallActive:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isCallActive();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getRssi:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getRssi();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getIoC:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getIoC();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getMpxDcc:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getMpxDcc();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getIntDet:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getIntDet();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSINR:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSINR();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setHiLoInj:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setHiLoInj(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_delayedStop:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
int _arg1;
_arg1 = data.readInt();
this.delayedStop(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_cancelDelayedStop:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.cancelDelayedStop(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_requestFocus:
{
data.enforceInterface(DESCRIPTOR);
this.requestFocus();
reply.writeNoException();
return true;
}
case TRANSACTION_setSinrSamplesCnt:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.setSinrSamplesCnt(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setSinrTh:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.setSinrTh(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setIntfDetLowTh:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.setIntfDetLowTh(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setIntfDetHighTh:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.setIntfDetHighTh(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getExtenRadioText:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getExtenRadioText();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.caf.fmradio.IFMRadioService
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
@Override public boolean fmOn() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_fmOn, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean fmOff() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_fmOff, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean fmRadioReset() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_fmRadioReset, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isFmOn() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isFmOn, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isAnalogModeEnabled() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isAnalogModeEnabled, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isFmRecordingOn() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isFmRecordingOn, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isSpeakerEnabled() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isSpeakerEnabled, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean fmReconfigure() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_fmReconfigure, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void registerCallbacks(com.caf.fmradio.IFMRadioServiceCallbacks cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallbacks, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterCallbacks() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_unregisterCallbacks, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean mute() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_mute, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean routeAudio(int device) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(device);
mRemote.transact(Stub.TRANSACTION_routeAudio, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean unMute() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_unMute, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isMuted() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isMuted, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean startRecording() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startRecording, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void stopRecording() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopRecording, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean tune(int frequency) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(frequency);
mRemote.transact(Stub.TRANSACTION_tune, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean seek(boolean up) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((up)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_seek, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void enableSpeaker(boolean speakerOn) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((speakerOn)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_enableSpeaker, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean scan(int pty) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(pty);
mRemote.transact(Stub.TRANSACTION_scan, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean seekPI(int piCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(piCode);
mRemote.transact(Stub.TRANSACTION_seekPI, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean searchStrongStationList(int numStations) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(numStations);
mRemote.transact(Stub.TRANSACTION_searchStrongStationList, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int[] getSearchList() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSearchList, _data, _reply, 0);
_reply.readException();
_result = _reply.createIntArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean cancelSearch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelSearch, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getProgramService() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getProgramService, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getRadioText() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRadioText, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getProgramType() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getProgramType, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getProgramID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getProgramID, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean setLowPowerMode(boolean bLowPower) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bLowPower)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setLowPowerMode, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getPowerMode() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPowerMode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean enableAutoAF(boolean bEnable) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bEnable)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_enableAutoAF, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean enableStereo(boolean bEnable) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bEnable)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_enableStereo, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isAntennaAvailable() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isAntennaAvailable, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isWiredHeadsetAvailable() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isWiredHeadsetAvailable, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isCallActive() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isCallActive, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getRssi() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRssi, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getIoC() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIoC, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getMpxDcc() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMpxDcc, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getIntDet() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIntDet, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSINR() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSINR, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setHiLoInj(int inj) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(inj);
mRemote.transact(Stub.TRANSACTION_setHiLoInj, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void delayedStop(long nDuration, int nType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(nDuration);
_data.writeInt(nType);
mRemote.transact(Stub.TRANSACTION_delayedStop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void cancelDelayedStop(int nType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nType);
mRemote.transact(Stub.TRANSACTION_cancelDelayedStop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void requestFocus() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_requestFocus, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean setSinrSamplesCnt(int samplesCnt) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(samplesCnt);
mRemote.transact(Stub.TRANSACTION_setSinrSamplesCnt, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean setSinrTh(int sinr) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sinr);
mRemote.transact(Stub.TRANSACTION_setSinrTh, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean setIntfDetLowTh(int intfLowTh) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(intfLowTh);
mRemote.transact(Stub.TRANSACTION_setIntfDetLowTh, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean setIntfDetHighTh(int intfHighTh) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(intfHighTh);
mRemote.transact(Stub.TRANSACTION_setIntfDetHighTh, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getExtenRadioText() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getExtenRadioText, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_fmOn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_fmOff = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_fmRadioReset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_isFmOn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_isAnalogModeEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_isFmRecordingOn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_isSpeakerEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_fmReconfigure = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_registerCallbacks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_unregisterCallbacks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_mute = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_routeAudio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_unMute = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_isMuted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_startRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_stopRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_tune = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_seek = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_enableSpeaker = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_scan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_seekPI = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_searchStrongStationList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_getSearchList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_cancelSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_getProgramService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_getRadioText = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_getProgramType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_getProgramID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
static final int TRANSACTION_setLowPowerMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
static final int TRANSACTION_getPowerMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
static final int TRANSACTION_enableAutoAF = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
static final int TRANSACTION_enableStereo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
static final int TRANSACTION_isAntennaAvailable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
static final int TRANSACTION_isWiredHeadsetAvailable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
static final int TRANSACTION_isCallActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 34);
static final int TRANSACTION_getRssi = (android.os.IBinder.FIRST_CALL_TRANSACTION + 35);
static final int TRANSACTION_getIoC = (android.os.IBinder.FIRST_CALL_TRANSACTION + 36);
static final int TRANSACTION_getMpxDcc = (android.os.IBinder.FIRST_CALL_TRANSACTION + 37);
static final int TRANSACTION_getIntDet = (android.os.IBinder.FIRST_CALL_TRANSACTION + 38);
static final int TRANSACTION_getSINR = (android.os.IBinder.FIRST_CALL_TRANSACTION + 39);
static final int TRANSACTION_setHiLoInj = (android.os.IBinder.FIRST_CALL_TRANSACTION + 40);
static final int TRANSACTION_delayedStop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 41);
static final int TRANSACTION_cancelDelayedStop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 42);
static final int TRANSACTION_requestFocus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 43);
static final int TRANSACTION_setSinrSamplesCnt = (android.os.IBinder.FIRST_CALL_TRANSACTION + 44);
static final int TRANSACTION_setSinrTh = (android.os.IBinder.FIRST_CALL_TRANSACTION + 45);
static final int TRANSACTION_setIntfDetLowTh = (android.os.IBinder.FIRST_CALL_TRANSACTION + 46);
static final int TRANSACTION_setIntfDetHighTh = (android.os.IBinder.FIRST_CALL_TRANSACTION + 47);
static final int TRANSACTION_getExtenRadioText = (android.os.IBinder.FIRST_CALL_TRANSACTION + 48);
}
public boolean fmOn() throws android.os.RemoteException;
public boolean fmOff() throws android.os.RemoteException;
public boolean fmRadioReset() throws android.os.RemoteException;
public boolean isFmOn() throws android.os.RemoteException;
public boolean isAnalogModeEnabled() throws android.os.RemoteException;
public boolean isFmRecordingOn() throws android.os.RemoteException;
public boolean isSpeakerEnabled() throws android.os.RemoteException;
public boolean fmReconfigure() throws android.os.RemoteException;
public void registerCallbacks(com.caf.fmradio.IFMRadioServiceCallbacks cb) throws android.os.RemoteException;
public void unregisterCallbacks() throws android.os.RemoteException;
public boolean mute() throws android.os.RemoteException;
public boolean routeAudio(int device) throws android.os.RemoteException;
public boolean unMute() throws android.os.RemoteException;
public boolean isMuted() throws android.os.RemoteException;
public boolean startRecording() throws android.os.RemoteException;
public void stopRecording() throws android.os.RemoteException;
public boolean tune(int frequency) throws android.os.RemoteException;
public boolean seek(boolean up) throws android.os.RemoteException;
public void enableSpeaker(boolean speakerOn) throws android.os.RemoteException;
public boolean scan(int pty) throws android.os.RemoteException;
public boolean seekPI(int piCode) throws android.os.RemoteException;
public boolean searchStrongStationList(int numStations) throws android.os.RemoteException;
public int[] getSearchList() throws android.os.RemoteException;
public boolean cancelSearch() throws android.os.RemoteException;
public java.lang.String getProgramService() throws android.os.RemoteException;
public java.lang.String getRadioText() throws android.os.RemoteException;
public int getProgramType() throws android.os.RemoteException;
public int getProgramID() throws android.os.RemoteException;
public boolean setLowPowerMode(boolean bLowPower) throws android.os.RemoteException;
public int getPowerMode() throws android.os.RemoteException;
public boolean enableAutoAF(boolean bEnable) throws android.os.RemoteException;
public boolean enableStereo(boolean bEnable) throws android.os.RemoteException;
public boolean isAntennaAvailable() throws android.os.RemoteException;
public boolean isWiredHeadsetAvailable() throws android.os.RemoteException;
public boolean isCallActive() throws android.os.RemoteException;
public int getRssi() throws android.os.RemoteException;
public int getIoC() throws android.os.RemoteException;
public int getMpxDcc() throws android.os.RemoteException;
public int getIntDet() throws android.os.RemoteException;
public int getSINR() throws android.os.RemoteException;
public void setHiLoInj(int inj) throws android.os.RemoteException;
public void delayedStop(long nDuration, int nType) throws android.os.RemoteException;
public void cancelDelayedStop(int nType) throws android.os.RemoteException;
public void requestFocus() throws android.os.RemoteException;
public boolean setSinrSamplesCnt(int samplesCnt) throws android.os.RemoteException;
public boolean setSinrTh(int sinr) throws android.os.RemoteException;
public boolean setIntfDetLowTh(int intfLowTh) throws android.os.RemoteException;
public boolean setIntfDetHighTh(int intfHighTh) throws android.os.RemoteException;
public java.lang.String getExtenRadioText() throws android.os.RemoteException;
}

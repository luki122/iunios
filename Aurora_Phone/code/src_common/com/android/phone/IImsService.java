/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: packages/apps/Phone/src/org/codeaurora/ims/IImsService.aidl
 */
package org.codeaurora.ims;
/**
 * Interface used to interact with IMS Service.
 *
 * {@hide}
 */
public interface IImsService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.codeaurora.ims.IImsService
{
private static final java.lang.String DESCRIPTOR = "org.codeaurora.ims.IImsService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.codeaurora.ims.IImsService interface,
 * generating a proxy if needed.
 */
public static org.codeaurora.ims.IImsService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.codeaurora.ims.IImsService))) {
return ((org.codeaurora.ims.IImsService)iin);
}
return new org.codeaurora.ims.IImsService.Stub.Proxy(obj);
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
case TRANSACTION_dial:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.dial(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
org.codeaurora.ims.IImsServiceListener _arg0;
_arg0 = org.codeaurora.ims.IImsServiceListener.Stub.asInterface(data.readStrongBinder());
int _result = this.registerCallback(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_deregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
org.codeaurora.ims.IImsServiceListener _arg0;
_arg0 = org.codeaurora.ims.IImsServiceListener.Stub.asInterface(data.readStrongBinder());
int _result = this.deregisterCallback(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setRegistrationState:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setRegistrationState(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getRegistrationState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getRegistrationState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_hangupUri:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
this.hangupUri(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_hangupWithReason:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _arg3;
_arg3 = (0!=data.readInt());
int _arg4;
_arg4 = data.readInt();
java.lang.String _arg5;
_arg5 = data.readString();
this.hangupWithReason(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
return true;
}
case TRANSACTION_getCallDetailsExtrasinCall:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String[] _result = this.getCallDetailsExtrasinCall(_arg0);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getImsDisconnectCauseInfo:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.getImsDisconnectCauseInfo(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getUriListinConf:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _result = this.getUriListinConf();
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_isVTModifyAllowed:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isVTModifyAllowed();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getProposedConnectionFailed:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.getProposedConnectionFailed(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isAddParticipantAllowed:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isAddParticipantAllowed();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_addParticipant:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
java.lang.String[] _arg3;
_arg3 = data.createStringArray();
this.addParticipant(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.codeaurora.ims.IImsService
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
     * Dial a number. This doesn't place the call. It displays
     * the Dialer screen.
     * @param number the number to be dialed. If null, this
     * would display the Dialer screen with no number pre-filled.
     */
@Override public void dial(java.lang.String number) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(number);
mRemote.transact(Stub.TRANSACTION_dial, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Register callback
     * @param imsServListener - IMS Service Listener
     */
@Override public int registerCallback(org.codeaurora.ims.IImsServiceListener imsServListener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((imsServListener!=null))?(imsServListener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Deregister callback
     * @param imsServListener - IMS Service Listener
     */
@Override public int deregisterCallback(org.codeaurora.ims.IImsServiceListener imsServListener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((imsServListener!=null))?(imsServListener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_deregisterCallback, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Set IMS Registration state
     * @param imsRegState - IMS Registration state
     */
@Override public void setRegistrationState(int imsRegState) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(imsRegState);
mRemote.transact(Stub.TRANSACTION_setRegistrationState, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Get IMS Registration state
     */
@Override public int getRegistrationState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRegistrationState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * HangupUri in an IMS Conference Call
     */
@Override public void hangupUri(int connectionId, java.lang.String userUri, java.lang.String confUri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(connectionId);
_data.writeString(userUri);
_data.writeString(confUri);
mRemote.transact(Stub.TRANSACTION_hangupUri, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Hangup for rejecting incoming call with a reason
     * This api will be used for following two scenarios -
     * - Reject incoming call
     * - While on active call, receive incoming call. Reject this
     *   incoming call.
     * connectionId - call id for the call
     * userUri - dial string or uri
     * confUri - uri associated with conference/multiparty call
     * mpty - true for conference/multiparty call
     * failCause - reason for hangup. Refer to CallFailCause.java for details
     * errorInfo - extra information associated with hangup
     */
@Override public void hangupWithReason(int connectionId, java.lang.String userUri, java.lang.String confUri, boolean mpty, int failCause, java.lang.String errorInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(connectionId);
_data.writeString(userUri);
_data.writeString(confUri);
_data.writeInt(((mpty)?(1):(0)));
_data.writeInt(failCause);
_data.writeString(errorInfo);
mRemote.transact(Stub.TRANSACTION_hangupWithReason, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Get the Call Details extras for the Call ID
     * @param callId - ID of the Call
     */
@Override public java.lang.String[] getCallDetailsExtrasinCall(int callId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(callId);
mRemote.transact(Stub.TRANSACTION_getCallDetailsExtrasinCall, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Get the Disconnect cause for Connection
     * @param callId - ID of the Call
     */
@Override public java.lang.String getImsDisconnectCauseInfo(int callId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(callId);
mRemote.transact(Stub.TRANSACTION_getImsDisconnectCauseInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Get List of User Uri in an IMS Conference Call
     */
@Override public java.lang.String[] getUriListinConf() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getUriListinConf, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Get the Service State for SMS service
     */
@Override public boolean isVTModifyAllowed() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isVTModifyAllowed, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * The system notifies about the failure (e.g. timeout) of the previous request to
     * change the type of the connection by re-sending the modify connection type request
     * with the status set to fail. After receiving an indication of call modify request
     * it will be possible to query for the status of the request.(see
     * {@link CallManager#registerForConnectionTypeChangeRequest(Handler, int, Object)}
     * ) If no request has been received, this function returns false, no error.
     *
     * @return true if the proposed connection type request failed (e.g. timeout).
     */
@Override public boolean getProposedConnectionFailed(int connIndex) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(connIndex);
mRemote.transact(Stub.TRANSACTION_getProposedConnectionFailed, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Returns true if the current phone supports the ability to add participant
     *
     */
@Override public boolean isAddParticipantAllowed() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isAddParticipantAllowed, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Api for adding participant
     * dialString - can be either a number or single or multiple uri
     * clir - will be default value. This is for future usage
     * callType - will be UNKNOWN. But in ImsPhone, this will take value of fg call.
     *            This is for future usage, in case call type should be passed through UI
     * String[] - can be made Parcelable incase its being used across process boundaries,
     *            which currently is not the case.
     */
@Override public void addParticipant(java.lang.String dialString, int clir, int callType, java.lang.String[] extra) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dialString);
_data.writeInt(clir);
_data.writeInt(callType);
_data.writeStringArray(extra);
mRemote.transact(Stub.TRANSACTION_addParticipant, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_dial = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_deregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setRegistrationState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getRegistrationState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_hangupUri = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_hangupWithReason = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getCallDetailsExtrasinCall = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getImsDisconnectCauseInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getUriListinConf = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_isVTModifyAllowed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getProposedConnectionFailed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_isAddParticipantAllowed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_addParticipant = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
}
/**
     * Dial a number. This doesn't place the call. It displays
     * the Dialer screen.
     * @param number the number to be dialed. If null, this
     * would display the Dialer screen with no number pre-filled.
     */
public void dial(java.lang.String number) throws android.os.RemoteException;
/**
     * Register callback
     * @param imsServListener - IMS Service Listener
     */
public int registerCallback(org.codeaurora.ims.IImsServiceListener imsServListener) throws android.os.RemoteException;
/**
     * Deregister callback
     * @param imsServListener - IMS Service Listener
     */
public int deregisterCallback(org.codeaurora.ims.IImsServiceListener imsServListener) throws android.os.RemoteException;
/**
     * Set IMS Registration state
     * @param imsRegState - IMS Registration state
     */
public void setRegistrationState(int imsRegState) throws android.os.RemoteException;
/**
     * Get IMS Registration state
     */
public int getRegistrationState() throws android.os.RemoteException;
/**
     * HangupUri in an IMS Conference Call
     */
public void hangupUri(int connectionId, java.lang.String userUri, java.lang.String confUri) throws android.os.RemoteException;
/**
     * Hangup for rejecting incoming call with a reason
     * This api will be used for following two scenarios -
     * - Reject incoming call
     * - While on active call, receive incoming call. Reject this
     *   incoming call.
     * connectionId - call id for the call
     * userUri - dial string or uri
     * confUri - uri associated with conference/multiparty call
     * mpty - true for conference/multiparty call
     * failCause - reason for hangup. Refer to CallFailCause.java for details
     * errorInfo - extra information associated with hangup
     */
public void hangupWithReason(int connectionId, java.lang.String userUri, java.lang.String confUri, boolean mpty, int failCause, java.lang.String errorInfo) throws android.os.RemoteException;
/**
     * Get the Call Details extras for the Call ID
     * @param callId - ID of the Call
     */
public java.lang.String[] getCallDetailsExtrasinCall(int callId) throws android.os.RemoteException;
/**
     * Get the Disconnect cause for Connection
     * @param callId - ID of the Call
     */
public java.lang.String getImsDisconnectCauseInfo(int callId) throws android.os.RemoteException;
/**
     * Get List of User Uri in an IMS Conference Call
     */
public java.lang.String[] getUriListinConf() throws android.os.RemoteException;
/**
     * Get the Service State for SMS service
     */
public boolean isVTModifyAllowed() throws android.os.RemoteException;
/**
     * The system notifies about the failure (e.g. timeout) of the previous request to
     * change the type of the connection by re-sending the modify connection type request
     * with the status set to fail. After receiving an indication of call modify request
     * it will be possible to query for the status of the request.(see
     * {@link CallManager#registerForConnectionTypeChangeRequest(Handler, int, Object)}
     * ) If no request has been received, this function returns false, no error.
     *
     * @return true if the proposed connection type request failed (e.g. timeout).
     */
public boolean getProposedConnectionFailed(int connIndex) throws android.os.RemoteException;
/**
     * Returns true if the current phone supports the ability to add participant
     *
     */
public boolean isAddParticipantAllowed() throws android.os.RemoteException;
/**
     * Api for adding participant
     * dialString - can be either a number or single or multiple uri
     * clir - will be default value. This is for future usage
     * callType - will be UNKNOWN. But in ImsPhone, this will take value of fg call.
     *            This is for future usage, in case call type should be passed through UI
     * String[] - can be made Parcelable incase its being used across process boundaries,
     *            which currently is not the case.
     */
public void addParticipant(java.lang.String dialString, int clir, int callType, java.lang.String[] extra) throws android.os.RemoteException;
}

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/pluginAlbum/IAlbum.aidl
 */
package com.baidu.xcloud.pluginAlbum;
public interface IAlbum extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.pluginAlbum.IAlbum
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.pluginAlbum.IAlbum";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.pluginAlbum.IAlbum interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.pluginAlbum.IAlbum asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.pluginAlbum.IAlbum))) {
return ((com.baidu.xcloud.pluginAlbum.IAlbum)iin);
}
return new com.baidu.xcloud.pluginAlbum.IAlbum.Stub.Proxy(obj);
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
case TRANSACTION_setupWithConfig:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
android.os.Bundle _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback.Stub.asInterface(data.readStrongBinder());
this.setupWithConfig(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getQuota:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg1;
_arg1 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.getQuota(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_deletePhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<java.lang.String> _arg1;
_arg1 = data.createStringArrayList();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.deletePhotos(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_movePhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> _arg1;
_arg1 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo.CREATOR);
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.movePhotos(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_makePhotoDir:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.makePhotoDir(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getPhotoMeta:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
boolean _arg2;
_arg2 = (0!=data.readInt());
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg3;
_arg3 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.getPhotoMeta(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_getPhotoList:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg4;
_arg4 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.getPhotoList(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
return true;
}
case TRANSACTION_getStreamFileList:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg4;
_arg4 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.getStreamFileList(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
return true;
}
case TRANSACTION_renamePhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> _arg1;
_arg1 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo.CREATOR);
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.renamePhotos(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_copyPhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> _arg1;
_arg1 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo.CREATOR);
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.copyPhotos(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_thumbnail:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg5;
_arg5 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.thumbnail(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
return true;
}
case TRANSACTION_diffWithCursor:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.diffWithCursor(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_shareLink:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.shareLink(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_cancelShare:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
com.baidu.xcloud.pluginAlbum.IAlbumCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.asInterface(data.readStrongBinder());
this.cancelShare(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_uploadPhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> _arg1;
_arg1 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo.CREATOR);
com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.asInterface(data.readStrongBinder());
this.uploadPhotos(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_downloadPhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> _arg1;
_arg1 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo.CREATOR);
com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.asInterface(data.readStrongBinder());
this.downloadPhotos(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_downloadFileAsSpecificType:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> _arg1;
_arg1 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo.CREATOR);
java.lang.String _arg2;
_arg2 = data.readString();
com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback _arg3;
_arg3 = com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.asInterface(data.readStrongBinder());
this.downloadFileAsSpecificType(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_downloadFileFromStream:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> _arg1;
_arg1 = data.createTypedArrayList(com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo.CREATOR);
com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback _arg2;
_arg2 = com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.asInterface(data.readStrongBinder());
this.downloadFileFromStream(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_processPhotoTask:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
long _arg2;
_arg2 = data.readLong();
com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback _arg3;
_arg3 = com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.asInterface(data.readStrongBinder());
this.processPhotoTask(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_processPhotoTaskList:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback _arg3;
_arg3 = com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.asInterface(data.readStrongBinder());
this.processPhotoTaskList(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_getPhotoTaskList:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.account.AccountInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.account.AccountInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback _arg1;
_arg1 = com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback.Stub.asInterface(data.readStrongBinder());
this.getPhotoTaskList(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.baidu.xcloud.pluginAlbum.IAlbum
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
	 * 初始化相关参数。
	 * 
	 * @param accountInfo  当前账户
	 * @param bundleConfig 相关设置参数的Bundle，具体参照Demo
     * @param albumSetupCallback  将在albumSetupCallback 接口中异步回调setupFileDescriptor方法
	 * @return
	 */
@Override public void setupWithConfig(com.baidu.xcloud.account.AccountInfo accountInfo, android.os.Bundle bundleConfig, com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback albumSetupCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((bundleConfig!=null)) {
_data.writeInt(1);
bundleConfig.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeStrongBinder((((albumSetupCallback!=null))?(albumSetupCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setupWithConfig, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 得到当前用户的空间配额信息，包括共空间和已用空字，由字节表示。
	 * 
	 * @param accountInfo     当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onGetQuota返回
	 * @return void 
	 */
@Override public void getQuota(com.baidu.xcloud.account.AccountInfo accountInfo, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getQuota, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 删除照片
	 * 
	 * @param photos 照片列表
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onDeletePhotos
	 * @return void 
	 */
@Override public void deletePhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<java.lang.String> photos, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeStringList(photos);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_deletePhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 移动照片
	 * 
	 * @param info 将要移动的照片列表。
	 * @param accountInfo  当前账户
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onMovePhotos
	 * @return void 
	 */
@Override public void movePhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> info, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeTypedList(info);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_movePhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 创建一个新照片文件夹
	 * 
	 * @param path 要创建的文件夹的路径
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onMakePhotoDir
	 * @return void 
	 */
@Override public void makePhotoDir(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(path);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_makePhotoDir, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 得到指定照片的元信息
	 * 
	 * @param photoPath 要得到元信息的照片路径	 
	 * @param showDirSize  showDirSize
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onGetPhotoMeta
	 * @return void 
	 */
@Override public void getPhotoMeta(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String photoPath, boolean showDirSize, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(photoPath);
_data.writeInt(((showDirSize)?(1):(0)));
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getPhotoMeta, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 得到照片列表。
	 * @param path 路径
	 * @param by 指定返回列表的排序方式，参数值有：time, name 或 size.
	 * @param order 指定返回列表的排序方式，参数值有：asc 或 desc.
	 * @param accountInfo  当前账户
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onGetPhotoList
	 * @return void 
	 */
@Override public void getPhotoList(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, java.lang.String by, java.lang.String order, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(path);
_data.writeString(by);
_data.writeString(order);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getPhotoList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 得到帐号下所有流文件(照片,音视频)
     * @param accountInfo  当前账户
     * @param type 流文件类型(video,audio,image)
     * @param start 缺省为0
     * @param limit 缺省为1000,可配置.
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onGetPhotoList
     * @return void 
     */
@Override public void getStreamFileList(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String type, int start, int limit, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(type);
_data.writeInt(start);
_data.writeInt(limit);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getStreamFileList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 更改多个照片名
	 * 
	 * @param info 将要更改照片名的列表。
	 * @return void
	 */
@Override public void renamePhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> info, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeTypedList(info);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_renamePhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 复制多个照片到另一个目录下
	 * 
	 * @param info 要复制的照片列表
	 * @return void
	 */
@Override public void copyPhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> info, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeTypedList(info);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_copyPhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 生成一个缩略图
	 * 
	 * @param path    原图像的路径
	 * @param quality 缩略图的质量，(0,100]。
	 * @param width   缩略图的宽，最大值为850。
	 * @param height  缩略图的高，最大值为580。
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onThumbnail
	 * @return void 
	 */
@Override public void thumbnail(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, int quality, int width, int height, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(path);
_data.writeInt(quality);
_data.writeInt(width);
_data.writeInt(height);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_thumbnail, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 文件增量更新操作查询接口
     * 
     * @param cursor
     *            用于标记更新断点。首次调用cursor=null；非首次调用，使用最后一次调用diff接口的返回结果中的cursor
     * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onDiffWithCursor
     * @return void 
     */
@Override public void diffWithCursor(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String cursor, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(cursor);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_diffWithCursor, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 对指定的照片创建一个链接进行分享,该链接24小时内有效
	 * 
	 * @param path 要分享的路径	 
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onShare
     * @return void 
	 */
@Override public void shareLink(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(path);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_shareLink, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 取消某一照片的链接
	 * 
	 * @param file 要取消的路径
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onCancelShare
     * @return void 
	 */
@Override public void cancelShare(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(path);
_data.writeStrongBinder((((iAlbumCallback!=null))?(iAlbumCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_cancelShare, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 上传照片
	 * 
	 * @param photoList  要上传的照片列表
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 * 
     * @return void 
	 */
@Override public void uploadPhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> photoList, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeTypedList(photoList);
_data.writeStrongBinder((((taskCallback!=null))?(taskCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_uploadPhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 下载照片
	 * 
	 * @param photoList  要下载的照片列表
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
	 */
@Override public void downloadPhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> photoList, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeTypedList(photoList);
_data.writeStrongBinder((((taskCallback!=null))?(taskCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_downloadPhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 对视频文件进行转码，实现实时观看视频功能
     * (为当前用户下载一个视频转码后的m3u8文件)<br>
     * 下载一个指定编码类型的文件，下载过程中listener监听下载进度。 您需要申请特别的权限才能使用该方法，请联系
     * bd，或发邮件到dev_support@baidu.com申请权限。
     * 
     * @param type
     *            仅限 M3U8_320_240、M3U8_480_224、M3U8_480_360、
     *            M3U8_640_480和M3U8_854_480, MP4_480P, MP4_360P
     * @param fileList  要下载的文件列表
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
     */
@Override public void downloadFileAsSpecificType(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> fileList, java.lang.String type, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeTypedList(fileList);
_data.writeString(type);
_data.writeStrongBinder((((taskCallback!=null))?(taskCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_downloadFileAsSpecificType, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 以流的方式下载一个文件到本地目录，下载过程中listener监听下载进度。 您需要申请特别的权限才能使用该方法，请联系
     * bd，或发邮件到dev_support@baidu.com申请权限。
     * 
     * @param fileList  要下载的文件列表
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
     */
@Override public void downloadFileFromStream(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> fileList, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeTypedList(fileList);
_data.writeStrongBinder((((taskCallback!=null))?(taskCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_downloadFileFromStream, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 暂停、继续、删除单个照片上传/下载任务
	 * 
	 * @param type 任务处理类型,参数有：pause,resume,remove
	 * @param taskId  文件任务ID
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 */
@Override public void processPhotoTask(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String type, long taskId, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(type);
_data.writeLong(taskId);
_data.writeStrongBinder((((taskCallback!=null))?(taskCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_processPhotoTask, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 暂停、继续、删除所有上传（download）、下载（download）或所有（all）任务
	 * 
     * @param accountInfo  当前账户
	 * @param type  type 任务处理类型,参数有：pause,resume,remove 
	 * @param fileType 要处理的文件任务类型,参数有：all,download,upload
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 */
@Override public void processPhotoTaskList(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String type, java.lang.String fileType, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(type);
_data.writeString(fileType);
_data.writeStrongBinder((((taskCallback!=null))?(taskCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_processPhotoTaskList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 获取当前账号的任务列表     
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回文件任务列表
	 */
@Override public void getPhotoTaskList(com.baidu.xcloud.account.AccountInfo accountInfo, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((accountInfo!=null)) {
_data.writeInt(1);
accountInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeStrongBinder((((taskCallback!=null))?(taskCallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getPhotoTaskList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_setupWithConfig = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getQuota = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_deletePhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_movePhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_makePhotoDir = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getPhotoMeta = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getPhotoList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getStreamFileList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_renamePhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_copyPhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_thumbnail = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_diffWithCursor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_shareLink = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_cancelShare = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_uploadPhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_downloadPhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_downloadFileAsSpecificType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_downloadFileFromStream = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_processPhotoTask = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_processPhotoTaskList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_getPhotoTaskList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
}
/**
	 * 初始化相关参数。
	 * 
	 * @param accountInfo  当前账户
	 * @param bundleConfig 相关设置参数的Bundle，具体参照Demo
     * @param albumSetupCallback  将在albumSetupCallback 接口中异步回调setupFileDescriptor方法
	 * @return
	 */
public void setupWithConfig(com.baidu.xcloud.account.AccountInfo accountInfo, android.os.Bundle bundleConfig, com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback albumSetupCallback) throws android.os.RemoteException;
/**
	 * 得到当前用户的空间配额信息，包括共空间和已用空字，由字节表示。
	 * 
	 * @param accountInfo     当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onGetQuota返回
	 * @return void 
	 */
public void getQuota(com.baidu.xcloud.account.AccountInfo accountInfo, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 删除照片
	 * 
	 * @param photos 照片列表
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onDeletePhotos
	 * @return void 
	 */
public void deletePhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<java.lang.String> photos, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 移动照片
	 * 
	 * @param info 将要移动的照片列表。
	 * @param accountInfo  当前账户
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onMovePhotos
	 * @return void 
	 */
public void movePhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> info, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 创建一个新照片文件夹
	 * 
	 * @param path 要创建的文件夹的路径
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onMakePhotoDir
	 * @return void 
	 */
public void makePhotoDir(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 得到指定照片的元信息
	 * 
	 * @param photoPath 要得到元信息的照片路径	 
	 * @param showDirSize  showDirSize
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onGetPhotoMeta
	 * @return void 
	 */
public void getPhotoMeta(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String photoPath, boolean showDirSize, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 得到照片列表。
	 * @param path 路径
	 * @param by 指定返回列表的排序方式，参数值有：time, name 或 size.
	 * @param order 指定返回列表的排序方式，参数值有：asc 或 desc.
	 * @param accountInfo  当前账户
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onGetPhotoList
	 * @return void 
	 */
public void getPhotoList(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, java.lang.String by, java.lang.String order, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
     * 得到帐号下所有流文件(照片,音视频)
     * @param accountInfo  当前账户
     * @param type 流文件类型(video,audio,image)
     * @param start 缺省为0
     * @param limit 缺省为1000,可配置.
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onGetPhotoList
     * @return void 
     */
public void getStreamFileList(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String type, int start, int limit, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 更改多个照片名
	 * 
	 * @param info 将要更改照片名的列表。
	 * @return void
	 */
public void renamePhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> info, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 复制多个照片到另一个目录下
	 * 
	 * @param info 要复制的照片列表
	 * @return void
	 */
public void copyPhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo> info, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 生成一个缩略图
	 * 
	 * @param path    原图像的路径
	 * @param quality 缩略图的质量，(0,100]。
	 * @param width   缩略图的宽，最大值为850。
	 * @param height  缩略图的高，最大值为580。
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onThumbnail
	 * @return void 
	 */
public void thumbnail(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, int quality, int width, int height, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
     * 文件增量更新操作查询接口
     * 
     * @param cursor
     *            用于标记更新断点。首次调用cursor=null；非首次调用，使用最后一次调用diff接口的返回结果中的cursor
     * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onDiffWithCursor
     * @return void 
     */
public void diffWithCursor(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String cursor, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 对指定的照片创建一个链接进行分享,该链接24小时内有效
	 * 
	 * @param path 要分享的路径	 
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onShare
     * @return void 
	 */
public void shareLink(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 取消某一照片的链接
	 * 
	 * @param file 要取消的路径
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onCancelShare
     * @return void 
	 */
public void cancelShare(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String path, com.baidu.xcloud.pluginAlbum.IAlbumCallback iAlbumCallback) throws android.os.RemoteException;
/**
	 * 上传照片
	 * 
	 * @param photoList  要上传的照片列表
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 * 
     * @return void 
	 */
public void uploadPhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> photoList, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException;
/**
	 * 下载照片
	 * 
	 * @param photoList  要下载的照片列表
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
	 */
public void downloadPhotos(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> photoList, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException;
/**
     * 对视频文件进行转码，实现实时观看视频功能
     * (为当前用户下载一个视频转码后的m3u8文件)<br>
     * 下载一个指定编码类型的文件，下载过程中listener监听下载进度。 您需要申请特别的权限才能使用该方法，请联系
     * bd，或发邮件到dev_support@baidu.com申请权限。
     * 
     * @param type
     *            仅限 M3U8_320_240、M3U8_480_224、M3U8_480_360、
     *            M3U8_640_480和M3U8_854_480, MP4_480P, MP4_360P
     * @param fileList  要下载的文件列表
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
     */
public void downloadFileAsSpecificType(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> fileList, java.lang.String type, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException;
/**
     * 以流的方式下载一个文件到本地目录，下载过程中listener监听下载进度。 您需要申请特别的权限才能使用该方法，请联系
     * bd，或发邮件到dev_support@baidu.com申请权限。
     * 
     * @param fileList  要下载的文件列表
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
     */
public void downloadFileFromStream(com.baidu.xcloud.account.AccountInfo accountInfo, java.util.List<com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo> fileList, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException;
/**
	 * 暂停、继续、删除单个照片上传/下载任务
	 * 
	 * @param type 任务处理类型,参数有：pause,resume,remove
	 * @param taskId  文件任务ID
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 */
public void processPhotoTask(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String type, long taskId, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException;
/**
	 * 暂停、继续、删除所有上传（download）、下载（download）或所有（all）任务
	 * 
     * @param accountInfo  当前账户
	 * @param type  type 任务处理类型,参数有：pause,resume,remove 
	 * @param fileType 要处理的文件任务类型,参数有：all,download,upload
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 */
public void processPhotoTaskList(com.baidu.xcloud.account.AccountInfo accountInfo, java.lang.String type, java.lang.String fileType, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException;
/**
	 * 获取当前账号的任务列表     
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回文件任务列表
	 */
public void getPhotoTaskList(com.baidu.xcloud.account.AccountInfo accountInfo, com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback taskCallback) throws android.os.RemoteException;
}

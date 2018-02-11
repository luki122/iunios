/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/snappyrain/xxxccc/PluginAlbumExplicitBindDemo/aidl/com/baidu/xcloud/pluginAlbum/IAlbumCallback.aidl
 */
package com.baidu.xcloud.pluginAlbum;
public interface IAlbumCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.baidu.xcloud.pluginAlbum.IAlbumCallback
{
private static final java.lang.String DESCRIPTOR = "com.baidu.xcloud.pluginAlbum.IAlbumCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.baidu.xcloud.pluginAlbum.IAlbumCallback interface,
 * generating a proxy if needed.
 */
public static com.baidu.xcloud.pluginAlbum.IAlbumCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.baidu.xcloud.pluginAlbum.IAlbumCallback))) {
return ((com.baidu.xcloud.pluginAlbum.IAlbumCallback)iin);
}
return new com.baidu.xcloud.pluginAlbum.IAlbumCallback.Stub.Proxy(obj);
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
case TRANSACTION_onGetQuota:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.QuotaResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.QuotaResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onGetQuota(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onDeletePhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.SimpleResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.SimpleResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onDeletePhotos(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onMakePhotoDir:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onMakePhotoDir(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onGetPhotoMeta:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.MetaResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.MetaResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onGetPhotoMeta(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onGetPhotoList:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onGetPhotoList(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onMovePhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onMovePhotos(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onRenamePhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onRenamePhotos(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onCopyPhotos:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onCopyPhotos(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onThumbnail:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onThumbnail(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onDiffWithCursor:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.DiffResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.DiffResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onDiffWithCursor(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onShare:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onShare(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onCancelShare:
{
data.enforceInterface(DESCRIPTOR);
com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse _arg0;
if ((0!=data.readInt())) {
_arg0 = com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onCancelShare(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onXcloudError:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onXcloudError(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.baidu.xcloud.pluginAlbum.IAlbumCallback
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
	 * 得到当前用户的的空间配额信息，包括共空间和已用空字，由字节表示。
	 * 
	 * @return QuotaResponse，由服务器返回得到。
	 */
@Override public void onGetQuota(com.baidu.xcloud.pluginAlbum.bean.QuotaResponse quotaResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((quotaResponse!=null)) {
_data.writeInt(1);
quotaResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onGetQuota, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 删除多个照片
	 * 
	 * @return SimpleResponse，服务器返回得到。
	 */
@Override public void onDeletePhotos(com.baidu.xcloud.pluginAlbum.bean.SimpleResponse simplefiedResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((simplefiedResponse!=null)) {
_data.writeInt(1);
simplefiedResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onDeletePhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 创建一个新的照片文件夹
	 * 
	 * @return FileInfoResponse， 服务器返回得到。
	 */
@Override public void onMakePhotoDir(com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse fileInfoResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((fileInfoResponse!=null)) {
_data.writeInt(1);
fileInfoResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onMakePhotoDir, _data, _reply, 0);
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
	 */
@Override public void onGetPhotoMeta(com.baidu.xcloud.pluginAlbum.bean.MetaResponse metaResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((metaResponse!=null)) {
_data.writeInt(1);
metaResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onGetPhotoMeta, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 得到指定目录下的照片列表。
	 * 
	 * @return ListInfoResponse, 服务器返回得到。
	 */
@Override public void onGetPhotoList(com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse listInfoResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((listInfoResponse!=null)) {
_data.writeInt(1);
listInfoResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onGetPhotoList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 移动多个照片到另一个目录中
	 * @return FileFromToResponse， 服务器返回得到。
	 */
@Override public void onMovePhotos(com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse fileFromToResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((fileFromToResponse!=null)) {
_data.writeInt(1);
fileFromToResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onMovePhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 更改照片名
	 * @return FileFromToResponse，服务器返回得到。
	 */
@Override public void onRenamePhotos(com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse fileFromToResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((fileFromToResponse!=null)) {
_data.writeInt(1);
fileFromToResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onRenamePhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 复制多个照片到另一个目录下
	 * @return FileFromToResponse， 服务器返回得到。
	 */
@Override public void onCopyPhotos(com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse fileFromToResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((fileFromToResponse!=null)) {
_data.writeInt(1);
fileFromToResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onCopyPhotos, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 生成缩略图
	 * @return ThumbnailResponse，服务器返回得到。
	 */
@Override public void onThumbnail(com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse thumbnailResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((thumbnailResponse!=null)) {
_data.writeInt(1);
thumbnailResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onThumbnail, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 增量更新操作查询接口
     * 
     * @return DiffResponse， 服务器返回得到。
     */
@Override public void onDiffWithCursor(com.baidu.xcloud.pluginAlbum.bean.DiffResponse diffResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((diffResponse!=null)) {
_data.writeInt(1);
diffResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onDiffWithCursor, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 分享照片(对指定的照片创建一个链接，该链接24小时内有效)
	 * @return FileLinkResponse, 服务器返回得到
	 */
@Override public void onShare(com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse fileLinkResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((fileLinkResponse!=null)) {
_data.writeInt(1);
fileLinkResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onShare, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * 取消分享(取消照片的链接)
	 * @return FileLinkResponse, 服务器返回得到
	 */
@Override public void onCancelShare(com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse fileLinkResponse) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((fileLinkResponse!=null)) {
_data.writeInt(1);
fileLinkResponse.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onCancelShare, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * xcloud 框架错误
	 */
@Override public void onXcloudError(int errorCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(errorCode);
mRemote.transact(Stub.TRANSACTION_onXcloudError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onGetQuota = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDeletePhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onMakePhotoDir = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onGetPhotoMeta = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onGetPhotoList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_onMovePhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_onRenamePhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_onCopyPhotos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_onThumbnail = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_onDiffWithCursor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_onShare = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_onCancelShare = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_onXcloudError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
}
/**
	 * 得到当前用户的的空间配额信息，包括共空间和已用空字，由字节表示。
	 * 
	 * @return QuotaResponse，由服务器返回得到。
	 */
public void onGetQuota(com.baidu.xcloud.pluginAlbum.bean.QuotaResponse quotaResponse) throws android.os.RemoteException;
/**
	 * 删除多个照片
	 * 
	 * @return SimpleResponse，服务器返回得到。
	 */
public void onDeletePhotos(com.baidu.xcloud.pluginAlbum.bean.SimpleResponse simplefiedResponse) throws android.os.RemoteException;
/**
	 * 创建一个新的照片文件夹
	 * 
	 * @return FileInfoResponse， 服务器返回得到。
	 */
public void onMakePhotoDir(com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse fileInfoResponse) throws android.os.RemoteException;
/**
	 * 得到指定照片的元信息
	 * 
	 */
public void onGetPhotoMeta(com.baidu.xcloud.pluginAlbum.bean.MetaResponse metaResponse) throws android.os.RemoteException;
/**
	 * 得到指定目录下的照片列表。
	 * 
	 * @return ListInfoResponse, 服务器返回得到。
	 */
public void onGetPhotoList(com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse listInfoResponse) throws android.os.RemoteException;
/**
	 * 移动多个照片到另一个目录中
	 * @return FileFromToResponse， 服务器返回得到。
	 */
public void onMovePhotos(com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse fileFromToResponse) throws android.os.RemoteException;
/**
	 * 更改照片名
	 * @return FileFromToResponse，服务器返回得到。
	 */
public void onRenamePhotos(com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse fileFromToResponse) throws android.os.RemoteException;
/**
	 * 复制多个照片到另一个目录下
	 * @return FileFromToResponse， 服务器返回得到。
	 */
public void onCopyPhotos(com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse fileFromToResponse) throws android.os.RemoteException;
/**
	 * 生成缩略图
	 * @return ThumbnailResponse，服务器返回得到。
	 */
public void onThumbnail(com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse thumbnailResponse) throws android.os.RemoteException;
/**
     * 增量更新操作查询接口
     * 
     * @return DiffResponse， 服务器返回得到。
     */
public void onDiffWithCursor(com.baidu.xcloud.pluginAlbum.bean.DiffResponse diffResponse) throws android.os.RemoteException;
/**
	 * 分享照片(对指定的照片创建一个链接，该链接24小时内有效)
	 * @return FileLinkResponse, 服务器返回得到
	 */
public void onShare(com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse fileLinkResponse) throws android.os.RemoteException;
/**
	 * 取消分享(取消照片的链接)
	 * @return FileLinkResponse, 服务器返回得到
	 */
public void onCancelShare(com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse fileLinkResponse) throws android.os.RemoteException;
/**
	 * xcloud 框架错误
	 */
public void onXcloudError(int errorCode) throws android.os.RemoteException;
}

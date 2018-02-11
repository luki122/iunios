package com.aurora.datauiapi.data;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;





















import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.bean.AlbumInfo;
import com.aurora.community.bean.GalleryHolder;
import com.aurora.community.bean.PhotoInfo;
import com.aurora.community.http.data.HttpRequestGetData;
import com.aurora.community.utils.BitmapUtil;
import com.aurora.community.utils.ColorThief;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.ThumbnailsUtil;
import com.aurora.datauiapi.data.bean.AddCommentHolder;
import com.aurora.datauiapi.data.bean.AddFavourHolder;
import com.aurora.datauiapi.data.bean.CancelFavourHolder;
import com.aurora.datauiapi.data.bean.ArticleHolder;
import com.aurora.datauiapi.data.bean.Attachnfo;
import com.aurora.datauiapi.data.bean.ClearAllMessageHolder;
import com.aurora.datauiapi.data.bean.CollectionOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.CommentHolder;
import com.aurora.datauiapi.data.bean.DeleteMessageHolder;
import com.aurora.datauiapi.data.bean.MessageBoxHolder;
import com.aurora.datauiapi.data.bean.MessageReadAllHolder;
import com.aurora.datauiapi.data.bean.NewsCategoryHolder;
import com.aurora.datauiapi.data.bean.NewsCategoryDataHolder;
import com.aurora.datauiapi.data.bean.NewsCategoryObject;
import com.aurora.datauiapi.data.bean.NewsInfoHolder;
import com.aurora.datauiapi.data.bean.PhotosDataInfo;
import com.aurora.datauiapi.data.bean.PhotosObject;
import com.aurora.datauiapi.data.bean.PostDeleteHolder;
import com.aurora.datauiapi.data.bean.PostDetailHolder;
import com.aurora.datauiapi.data.bean.PublishOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.UpArticleObject;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.CommandBoost;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.ICommunityManager;
import com.aurora.datauiapi.data.interf.INotifiableController;


public class CommunityManager extends BaseManager implements ICommunityManager {

	private static final String TAG = "CommunityManager";

	
	public CommunityManager(INotifiableController controller) {
	    super(controller);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aurora.datauiapi.data.interf.IAccountManager#loginAccount(com.aurora
	 * .datauiapi.data.implement.DataResponse, android.content.Context,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void loginAccount(final DataResponse<UserLoginObject> response,
			final Context context, final String acctName,
			final String pwdMD5, final String imei, final String validCode) {
		mHandler.post(new Command<UserLoginObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				
				/*String result = HttpRequestGetAccountData
						.getLoginObject(acctName, pwdMD5, imei, validCode);
				setResponse(response, result, UserLoginObject.class);
				if (response.value != null && response.value.getCode() == UserLoginObject.CODE_SUCCESS) {
				    saveUserInfoForLogin(response.value);
				}*/
			}
		});
	}

	@Override
	public void getNewsInfo(final DataResponse<NewsInfoHolder> response) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<NewsInfoHolder>(response, this) {
			@Override
			public void doRun() throws Exception {
				
				/*String result = HttpRequestGetAccountData
						.getLoginObject(acctName, pwdMD5, imei, validCode);
				setResponse(response, result, UserLoginObject.class);
				if (response.value != null && response.value.getCode() == UserLoginObject.CODE_SUCCESS) {
				    saveUserInfoForLogin(response.value);
				}*/
				
				String result = HttpRequestGetData.getNewsInfo();
				setResponse(response,result,NewsInfoHolder.class);
                Log.e("linp", "~~~~~~~~~command runnable getNewsInfo");	
			}
		});
	}
	@Override
	public void upPhotos(final DataResponse<ArrayList<PhotosObject>> response,
			final Map<String,String> up_pic,final ArrayList<String> paths) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<ArrayList<PhotosObject>>(response, this) {
			@Override
			public void doRun() throws Exception {
				
				/*String result = HttpRequestGetAccountData
						.getLoginObject(acctName, pwdMD5, imei, validCode);
				setResponse(response, result, UserLoginObject.class);
				if (response.value != null && response.value.getCode() == UserLoginObject.CODE_SUCCESS) {
				    saveUserInfoForLogin(response.value);
				}*/
				ArrayList<PhotosObject> p_obj = new ArrayList<PhotosObject>();
				for(int i = 0 ; i < paths.size()-1; i ++)
				{
					//FileInputStream fis = null;
					PhotosObject obj = new PhotosObject();
					String path = paths.get(i);
				
					if(up_pic.containsKey(path))
					{
						PhotosDataInfo data = new PhotosDataInfo();
						data.setAid(up_pic.get(path));
						obj.setData(data);
						p_obj.add(obj);
						continue;
					}
					
					int index = path.indexOf("file:///");
					if(index != -1)
					{
						path = path.substring(index+7);
					}
						//fis = new FileInputStream(path);
					
					Log.i(TAG, "zhangwei the color path="+path);
					Log.i(TAG, "zhangwei the color begin="+System.currentTimeMillis());
					/*BitmapFactory.Options newOpts = new BitmapFactory.Options();
	                newOpts.inJustDecodeBounds = false;// 只读边,不读内容
	                //newOpts.inJustDecodeBounds = false;
	                newOpts.inSampleSize = 16;// 设置采样率
	                newOpts.inPreferredConfig = Config.RGB_565;// 该模式是默认的,可不设
	                newOpts.inPurgeable = true;// 同时设置才会有效
	                newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
*/					//Bitmap  bitmap = BitmapFactory.decodeFile(path,newOpts);
					/*Bitmap  bitmap = BitmapUtil.compressImageFromFile(path, 100, 100);
					//Bitmap bitmap  = BitmapFactory.decodeStream(fis);
					List<int[]> rst = new ArrayList<int[]>();
			        try {
			        	rst = ColorThief.compute(bitmap, 5);
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
			        bitmap.recycle();
			        bitmap = null;
			        int[] dominantColor = rst.get(0);
			        
			        String color = SystemUtils.toHexEncoding(dominantColor[0], dominantColor[1], dominantColor[2]);
			        Log.i(TAG, "zhangwei the color end="+System.currentTimeMillis());
			        Log.i(TAG, "zhangwei the color value="+color);*/
			        //String color = "#ff"+dominantColor[0]+dominantColor[1]+dominantColor[2];
			        //int ttt = Color.rgb(dominantColor[0], dominantColor[1], dominantColor[2]);
					int index1 = path.lastIndexOf("/");
					String image_name = path.substring(index1+1);
				
					String result = HttpRequestGetData.uploadPhoto(image_name, path,"");
					//setResponse(response,result,PhotosObject.class);
					//fis.close();
					ObjectMapper mapper = new ObjectMapper();
	
					mapper.configure(
							DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
							true);
					mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
	
					mapper.getDeserializationConfig()
							.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
									false);
					if(null == result)
					{
						response.value = null;
						return;
					}
					
					if(result.equals("1"))
					{
						obj.setReturnCode(Globals.CODE_FAILED);
						obj.setErrorCode(Globals.CODE_FAILED);
						obj.setMsg(CommunityApp.getInstance().getResources().getString(R.string.upload_pic_too_large));
						p_obj.add(obj);
						response.value = p_obj;
						return;
					}
					obj  = (PhotosObject) mapper.readValue(result, PhotosObject.class);
					if(obj.getReturnCode() ==  Globals.CODE_SUCCESS)
					{
						p_obj.add(obj);
					}
					else
					{
						p_obj.add(obj);
						response.value = p_obj;
						return;
					}
	                
				}
				response.value = p_obj;
				Log.e("linp", "zhangwei the color end=");	
			}
		});
	}
	@Override
	public void postActivity() {
		/*
		 * if(failedRequests!=null){ failedRequests.clear(); }
		 */
		if (failedIORequests != null) {
			failedIORequests.clear();
		}
	}

	@Override
	public void getGalleryInfo(final DataResponse<GalleryHolder> rsp,
			Context context) {
		// TODO Auto-generated method stub
		/** code referenced from github */
		Log.e("linp", "~~~~~~~~~~~~~~~~~~~~getGalleryInfo");
		final ContentResolver cr = context.getContentResolver();

		final ArrayList<AlbumInfo> albumList = new ArrayList<AlbumInfo>();
		final ArrayList<PhotoInfo> photoList = new ArrayList<PhotoInfo>();

		mHandler.post(new CommandBoost<GalleryHolder>(rsp, this) {
			@Override
			public void doRun() throws Exception {

				ThumbnailsUtil.clear();
				photoList.clear();
				String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
						Thumbnails.DATA };
				Cursor cur = cr.query(Thumbnails.EXTERNAL_CONTENT_URI,
						projection, null, null, null);

				if (cur != null && cur.moveToFirst()) {
					int image_id;
					String image_path;
					int image_idColumn = cur
							.getColumnIndex(Thumbnails.IMAGE_ID);
					int dataColumn = cur.getColumnIndex(Thumbnails.DATA);
					do {
						image_id = cur.getInt(image_idColumn);
						image_path = cur.getString(dataColumn);
						ThumbnailsUtil.put(image_id, "file://" + image_path);
					} while (cur.moveToNext());
				}

				Cursor cursor = cr.query(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
						null, null, "date_modified DESC");

				String _path = "_data";
				String _album = "bucket_display_name";

				HashMap<String, AlbumInfo> myhash = new HashMap<String, AlbumInfo>();
				AlbumInfo albumInfo = null;
				PhotoInfo photoInfo = null;
				if (cursor != null && cursor.moveToFirst()) {
					do {
						int index = 0;
						int _id = cursor.getInt(cursor.getColumnIndex("_id"));
						String path = cursor.getString(cursor
								.getColumnIndex(_path));
						String album = cursor.getString(cursor
								.getColumnIndex(_album));
						ArrayList<PhotoInfo> stringList = new ArrayList<PhotoInfo>();
						photoInfo = new PhotoInfo();
						if (myhash.containsKey(album)) {
							albumInfo = myhash.remove(album);
							if (albumList.contains(albumInfo))
								index = albumList.indexOf(albumInfo);
							photoInfo.setImage_id(_id);
							photoInfo.setPath_file("file://" + path);
							photoInfo.setPath_absolute(path);
							albumInfo.getList().add(photoInfo);
							photoList.add(photoInfo);
							albumList.set(index, albumInfo);
							myhash.put(album, albumInfo);
						} else {
							albumInfo = new AlbumInfo();
							stringList.clear();
							photoInfo.setImage_id(_id);
							photoInfo.setPath_file("file://" + path);
							photoInfo.setPath_absolute(path);
							stringList.add(photoInfo);
							photoList.add(photoInfo);
							albumInfo.setImage_id(_id);
							albumInfo.setPath_file("file://" + path);
							albumInfo.setPath_absolute(path);
							albumInfo.setName_album(album);
							albumInfo.setList(stringList);
							albumList.add(albumInfo);
							myhash.put(album, albumInfo);
						}
					} while (cursor.moveToNext());
				}
				/** like set response */
				rsp.value = new GalleryHolder(albumList, photoList);
			}
		});

	}

	@Override
	public void getPublishOfUserCenter(
			final DataResponse<PublishOfUserCenterHolder> response,final int page,final int count,final String userId) {
		mHandler.post(new Command<PublishOfUserCenterHolder>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetData.getPublishOfUserCenter(page,count,userId);
				setResponse(response,result,PublishOfUserCenterHolder.class);
			}
		});
		
	}
	@Override
	public void getArticleInfo(final DataResponse<ArticleHolder> response,final String tid,final int page,final int count) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<ArticleHolder>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetData.getArticleListInfo(tid,page,count);
				setResponse(response,result,ArticleHolder.class);
			}
		});
	}

	@Override
	public void getCollectionOfUserCenter(
			final DataResponse<CollectionOfUserCenterHolder> response, final int page,final int count,final String userId) {
		mHandler.post(new Command<CollectionOfUserCenterHolder>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetData.getCollectionOfUserCenter(page,count,userId);
				setResponse(response,result,CollectionOfUserCenterHolder.class);
			}
		});
	}
		
	@Override
	public void getCategoryInfo(final DataResponse<NewsCategoryObject> resp) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<NewsCategoryObject>(resp, this) {
			@Override
			public void doRun() throws Exception{
				
				String result = HttpRequestGetData.getCategoryInfo();
				setResponse(resp,result,NewsCategoryObject.class);
				
			}
		});
		
	}

	@Override
	public void getPostDetail(final DataResponse<PostDetailHolder> resp,
			final String pid) {
		mHandler.post(new Command<PostDetailHolder>(resp, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetData.getPostDetail(pid);
				setResponse(resp, result, PostDetailHolder.class);
			}
		});
		
	}

	@Override
	public void getCategoryInfoTest(final DataResponse<NewsCategoryHolder> resp,
			final Context context, final String url) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<NewsCategoryHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				
				String result = HttpRequestGetData.getNewsInfo();
				setResponse(resp,result,NewsCategoryHolder.class);
				
			}
		});
	}

	@Override
	public void getCommentList(final DataResponse<CommentHolder> resp, final String pid,
			final int page, final int count) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<CommentHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				
				String result = HttpRequestGetData.getCommentList(pid, page, count);
				setResponse(resp,result,CommentHolder.class);
			}
		});
	}

	@Override
	public void addComment(final DataResponse<AddCommentHolder> resp,final String pid,
			final String commentContent, final String replyCid) {
		mHandler.post(new Command<AddCommentHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.addComment(pid, commentContent, replyCid);
				setResponse(resp,result,AddCommentHolder.class);
			}
		});
		
	}


	@Override
	public void upArticle(final DataResponse<UpArticleObject> resp,final String pid,final String gid,final ArrayList<Attachnfo> attachid,final String content,final String type,final String tags) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<UpArticleObject>(resp, this) {
			@Override
			public void doRun() throws Exception{
				Log.e("linp", "zhangwei the color end3");
				String result = HttpRequestGetData.uploadArticle(pid,gid,attachid,content,type,tags);
				setResponse(resp,result,UpArticleObject.class);
				Log.e("linp", "zhangwei the color end4");
			}
		});
	}


	@Override
	public void addFavour(final DataResponse<AddFavourHolder> resp, final String pid) {
		mHandler.post(new Command<AddFavourHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.addFavour(pid);
				setResponse(resp,result,AddFavourHolder.class);
			}
		});
		
	}
	
	@Override
	public void cancelFavour(final DataResponse<CancelFavourHolder> resp, final String pid) {
		mHandler.post(new Command<CancelFavourHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.cancelFavour(pid);
				setResponse(resp,result,CancelFavourHolder.class);
			}
		});
	}

	@Override
	public void deletePost(final DataResponse<PostDeleteHolder> resp, final String pid) {
		mHandler.post(new Command<PostDeleteHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.deletePost(pid);
				setResponse(resp,result,PostDeleteHolder.class);
			}
		});
		
	}

	@Override
	public void messageBox(final DataResponse<MessageBoxHolder> resp,final int pageCount,final String type,final String startId,final String startPage) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<MessageBoxHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.messageBox(pageCount,type,startId,startPage);
				setResponse(resp,result,MessageBoxHolder.class);
			}
		});
	}

	@Override
	public void messageReadAll(final DataResponse<MessageReadAllHolder> resp) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<MessageReadAllHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.messageReadAll();
				setResponse(resp,result,MessageReadAllHolder.class);
			}
		});
	}

	@Override
	public void clearAllMessage(final DataResponse<ClearAllMessageHolder> resp) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<ClearAllMessageHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.deleteAllMessage();
				setResponse(resp,result,ClearAllMessageHolder.class);
			}
		});
	}

	@Override
	public void deleteMessage(final DataResponse<DeleteMessageHolder> resp, final String nId) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<DeleteMessageHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.deleteMessage(nId);
				setResponse(resp,result,DeleteMessageHolder.class);
			}
		});
	}
	
}
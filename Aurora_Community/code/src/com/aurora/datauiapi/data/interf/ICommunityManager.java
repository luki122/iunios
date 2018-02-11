package com.aurora.datauiapi.data.interf;



import java.util.ArrayList;
import java.util.Map;

import android.content.Context;

import com.aurora.community.bean.GalleryHolder;
import com.aurora.datauiapi.data.bean.AddCommentHolder;
import com.aurora.datauiapi.data.bean.AddFavourHolder;
import com.aurora.datauiapi.data.bean.ArticleHolder;
import com.aurora.datauiapi.data.bean.Attachnfo;
import com.aurora.datauiapi.data.bean.CancelFavourHolder;
import com.aurora.datauiapi.data.bean.ClearAllMessageHolder;
import com.aurora.datauiapi.data.bean.CollectionOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.CommentHolder;
import com.aurora.datauiapi.data.bean.DeleteMessageHolder;
import com.aurora.datauiapi.data.bean.MessageBoxHolder;
import com.aurora.datauiapi.data.bean.MessageReadAllHolder;
import com.aurora.datauiapi.data.bean.NewsCategoryHolder;
import com.aurora.datauiapi.data.bean.NewsCategoryObject;
import com.aurora.datauiapi.data.bean.NewsInfoHolder;
import com.aurora.datauiapi.data.bean.PhotosObject;
import com.aurora.datauiapi.data.bean.PostDeleteHolder;
import com.aurora.datauiapi.data.bean.PostDetailHolder;
import com.aurora.datauiapi.data.bean.PublishOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.UpArticleObject;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.DataResponse;



public interface ICommunityManager {

	
	/** 
	* @Title: loginAccount
	* @Description: 登录
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void loginAccount(final DataResponse<UserLoginObject> response,final Context context,final String acctName,
	        final String pwdMD5,final String imei, final String validCode);
	/**getting news info */
	public void getNewsInfo(final DataResponse<NewsInfoHolder> resp);

	/**getting artcicle info */
	public void getArticleInfo(final DataResponse<ArticleHolder> resp,final String tid,final int page,final int count);

	/**uploda photos */
	public void upPhotos(final DataResponse<ArrayList<PhotosObject>> resp,final Map<String,String> up_pic,final ArrayList<String> paths);
	/**uploda photos */
	public void upArticle(final DataResponse<UpArticleObject> resp,final String pid,final String gid,final ArrayList<Attachnfo> attachid,final String content,final String type,String tags);
	
	public void getPublishOfUserCenter(DataResponse<PublishOfUserCenterHolder> resp,final int page,final int count,String userId);
	/**getting category info*/
	public void getCategoryInfo(final DataResponse<NewsCategoryObject> resp);
	public void getCategoryInfoTest(final DataResponse<NewsCategoryHolder> resp,final Context context,final String url);
	
	public void getGalleryInfo(final DataResponse<GalleryHolder>resp,final Context context);
	
	public void getCollectionOfUserCenter(DataResponse<CollectionOfUserCenterHolder> resp,final int page,final int count,final String userId);
	
	public void getPostDetail(DataResponse<PostDetailHolder> resp,final String pid);
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
	

	public void getCommentList(DataResponse<CommentHolder> resp,final String pid,final int page,final int count);
	
	public void addComment(DataResponse<AddCommentHolder> resp,final String pid,final String commentContent,final String replyCid);
	
	public void addFavour(DataResponse<AddFavourHolder> resp,final String pid);
	
	public void cancelFavour(DataResponse<CancelFavourHolder> resp,final String pid);
	
	public void deletePost(DataResponse<PostDeleteHolder> resp,final String pid);
	
	public void messageBox(DataResponse<MessageBoxHolder> resp,final int pageCount,final String type,final String startId,final String startPage);
	
	public void messageReadAll(DataResponse<MessageReadAllHolder> resp);
	
	public void clearAllMessage(DataResponse<ClearAllMessageHolder> resp);
	
	public void deleteMessage(DataResponse<DeleteMessageHolder> resp,String nId);
	
}

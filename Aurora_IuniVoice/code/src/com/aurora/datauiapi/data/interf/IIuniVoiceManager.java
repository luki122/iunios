package com.aurora.datauiapi.data.interf;

import android.content.Context;

import com.aurora.datauiapi.data.bean.AddCommentHolder;
import com.aurora.datauiapi.data.bean.AddFavourHolder;
import com.aurora.datauiapi.data.bean.AddScorerHolder;
import com.aurora.datauiapi.data.bean.AppUpgradeObject;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.ClearAllMessageHolder;
import com.aurora.datauiapi.data.bean.CommentInfoHolder;
import com.aurora.datauiapi.data.bean.DeleteMessageHolder;
import com.aurora.datauiapi.data.bean.ForumData;
import com.aurora.datauiapi.data.bean.HomepageListObject;
import com.aurora.datauiapi.data.bean.MessageReadAllHolder;
import com.aurora.datauiapi.data.bean.PostData;
import com.aurora.datauiapi.data.bean.SystemMsgHolder;
import com.aurora.datauiapi.data.bean.SystemPushMsgHolder;
import com.aurora.datauiapi.data.bean.UserInfoHolder;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.DataResponse;

public interface IIuniVoiceManager {

	/**
	 * @param @param response
	 * @param @param context
	 * @return void
	 * @throws
	 * @Title: loginAccount
	 * @Description: 登录
	 */
	public void loginAccount(final DataResponse<UserLoginObject> response,
			final Context context, final String acctName, final String pwdMD5,
			final String imei, final String validCode);

	/**
	 * Put in here everything that has to be cleaned up after leaving an
	 * activity.
	 */
	public void postActivity();

	/**
	 * 帖子回复
	 */
	public void addComment(DataResponse<AddCommentHolder> resp,
			final String pid, final String commentContent,
			final String replyCid, String hashId);

	/**
	 * 帖子点赞
	 */
	public void addFavour(DataResponse<AddFavourHolder> resp, final String pid,
			final String hash);

	/**
	 * 帖子评分
	 */
	void addScore(DataResponse<AddScorerHolder> resp, String fid, String pid,
			String hash, String reason, String score);

	/**
	 * 查询点赞与评分
	 * 
	 * @param resp
	 * @param pageId
	 * @param hashId
	 */
	public void queryDetailInfo(final DataResponse<CommentInfoHolder> resp,
			final String pageId, final String hashId);

//	public void messageReadAll(DataResponse<MessageReadAllHolder> resp);
//
//	public void clearAllMessage(DataResponse<ClearAllMessageHolder> resp);
//
//	public void deleteMessage(DataResponse<DeleteMessageHolder> resp, String nId);

	public void getHomePageList(DataResponse<HomepageListObject> response,
			int page, int tpp);

	public void getForumData(DataResponse<ForumData> response);

	public void getPostData(DataResponse<PostData> response, String fid,
			int page, int tpp);

	public void signDaily(DataResponse<BaseResponseObject> response,
			String formhash);

	public void publish(DataResponse<BaseResponseObject> response,
			String formhash, String subject, String message, String fid,
			int[] attachnew);

	/**
	 * 获取用户信息
	 */
	public void getUserInfo(DataResponse<UserInfoHolder> response);

	/**
	 * 获取系统消息推送
	 */
	public void getPushMessage(DataResponse<SystemMsgHolder> response, int page);
	/**
	 * 检查版本
	 */
	public void checkVersion(final DataResponse<AppUpgradeObject> response,
			final Context context);

	public void systemMsgDetail(DataResponse<BaseResponseObject> response,
			String nid);

	public void changeUserIcon(DataResponse<BaseResponseObject> response,
			String iconId);

	public void chanageUserIntroduce(DataResponse<BaseResponseObject> response,
			String content);

	public void getSystemPushMsg(DataResponse<SystemPushMsgHolder> response);
}

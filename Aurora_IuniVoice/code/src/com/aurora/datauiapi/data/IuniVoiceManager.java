package com.aurora.datauiapi.data;


import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.content.Context;

import com.aurora.datauiapi.data.bean.AddCommentHolder;
import com.aurora.datauiapi.data.bean.AddFavourHolder;
import com.aurora.datauiapi.data.bean.AddScorerHolder;
import com.aurora.datauiapi.data.bean.AppUpgradeObject;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.CommentInfoHolder;
import com.aurora.datauiapi.data.bean.ForumData;
import com.aurora.datauiapi.data.bean.HomepageListObject;
import com.aurora.datauiapi.data.bean.ImageUpObject;
import com.aurora.datauiapi.data.bean.PostData;
import com.aurora.datauiapi.data.bean.SystemMsgHolder;
import com.aurora.datauiapi.data.bean.SystemPushMsgHolder;
import com.aurora.datauiapi.data.bean.UserInfoHolder;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.IIuniVoiceManager;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.PublishActivity;
import com.aurora.iunivoice.http.data.HttpRequestGetData;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.Log;


public class IuniVoiceManager extends BaseManager implements IIuniVoiceManager {

    private static final String TAG = "IuniVoiceManager";

    public IuniVoiceManager(INotifiableController controller) {
        super(controller);
    }

    @Override
    public void getHomePageList(final DataResponse<HomepageListObject> response, final int page, final int tpp) {
        mHandler.post(new Command<HomepageListObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.getHomepageListData(page, tpp);
                setResponse(response, result, HomepageListObject.class);
            }
        });
    }

    public void getForumData(final DataResponse<ForumData> response) {
        mHandler.post(new Command<ForumData>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.getForumData();
                setResponse(response, result, ForumData.class);
            }
        });
    }

    public void getPostData(final DataResponse<PostData> response, final String fid,
                            final int page, final int tpp) {
        mHandler.post(new Command<PostData>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.getPostData(fid, page, tpp);
                setResponse(response, result, PostData.class);
            }
        });
    }

    @Override
    public void signDaily(final DataResponse<BaseResponseObject> response, final String formHash) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.signDaily(formHash);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }
    
    public void publish(final DataResponse<BaseResponseObject> response, final String formhash, 
    		final String subject, final String message, final String fid, final int[] attachnew) {
    	mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.publish(formhash, subject, message, fid, attachnew);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }
    
	public void uploadImageFiles(final DataResponse<ArrayList<ImageUpObject>> response,
			final Map<String, String> up_pic, final ArrayList<String> paths, final long operation) {
		mHandler.post(new Command<ArrayList<ImageUpObject>>(response, this) {
            @Override
            public void doRun() throws Exception {
            	
            	ArrayList<ImageUpObject> i_obj = new ArrayList<ImageUpObject>();
            	
        		for (int i = 0; i < paths.size(); i++) {
        			
        			if (PublishActivity.CANCLE_FLAG || operation != PublishActivity.OPERATION_TAG) {
        				break;
        			}
        			
        			ImageUpObject obj = new ImageUpObject();
        			
        			String path = paths.get(i);
        			
        			int index = path.indexOf("file:///");
					if (index != -1) {
						path = path.substring(index + 7);
					}
        			
        			int index1 = path.lastIndexOf("/");
        			String image_name = path.substring(index1 + 1);
        			
    				String result = "";
    				try {
    					File f = new File(path);
    					if (f.exists()) {
    						String suf = path.substring(path.lastIndexOf("."), path.length());
    						suf = suf.toLowerCase();
    						if (f.length() > 1024 * 1024 * 10) {
    							result = "-2";
    						} else if (!(suf.equals(".png") || suf.equals(".jpg") || suf.equals(".jpeg") || suf.equals(".gif"))) {
    							result = "-4";
    						} else {
    							result = HttpRequestGetData.uploadImageFile(image_name, path);
    						}
    					}
    				} catch (OutOfMemoryError error) {
    					error.printStackTrace();
    				}
    				
    				int r = -100;
    				try {
    					r = Integer.parseInt(result);
    				} catch (NumberFormatException e) {
    				}
    				
    				if (r > 0) {		// 成功
    					obj.setReturnCode(Globals.CODE_SUCCESS);
    					obj.setAttachnewId(r);
    					i_obj.add(obj);
    				} else {			// 失败
    					obj.setReturnCode(getReturnCode(r));
						obj.setErrorCode(getReturnCode(r));
						obj.setMsg(getReturnMsg(r));
						i_obj.add(obj);
						response.value = i_obj;
						return;
    				}
        			
        		}
        		response.value = i_obj;
            }
        });
    }
	/*
	-1: 内部错误;
    -2: 服务器限制无法上传那么大的附件;
    -3: 用户组无法上传如此大的附近;
    -4：不支持此类扩展名;
    -5: 文件类型限制无法上传那么大的附近;
    -6: 今日您已无法上传更多的附件;
    -7: 请选择图片文件(jpg, jpeg, gif, png)  
    -8: 附近无法保存  
    -9: 没有合法的文件被上传
	*/
	private int getReturnCode(int returnCode) {
		if (returnCode != -1 || returnCode != -2 || returnCode != -3 || returnCode != -4
			 || returnCode != -5 || returnCode != -6 || returnCode != -7 || returnCode != -8
			 || returnCode != -9) {
						 returnCode = Globals.CODE_FAILED;
		}
		return returnCode;
	}
	
	private String getReturnMsg(int returnCode) {
		String msg = "";
		switch (returnCode) {
		case -1:
			msg = "内部错误";
			break;
		case -2:
			msg = "服务器限制无法上传那么大的附件";
			break;
		case -3:
			msg = "用户组无法上传如此大的附件";
			break;
		case -4:
			msg = "不支持此类扩展名";
			break;
		case -5:
			msg = "文件类型限制无法上传那么大的附件";
			break;
		case -6:
			msg = "今日您已无法上传更多的附件";
			break;
		case -7:
			msg = "请选择图片文件(jpg, jpeg, gif, png)";
			break;
		case -8:
			msg = "附件无法保存";
			break;
		case -9:
			msg = "没有合法的文件被上传";
			break;
			default:
				msg = IuniVoiceApp.getInstance().getResources().getString(R.string.upload_pic_fail);
				break;
		}
		return msg;
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
    public void postActivity() {
		/*
		 * if(failedRequests!=null){ failedRequests.clear(); }
		 */
        if (failedIORequests != null) {
            failedIORequests.clear();
        }
    }

    /**
     * 获取用户信息
     */
	@Override
	public void getUserInfo(final DataResponse<UserInfoHolder> resp) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<UserInfoHolder>(resp, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.getUserInfo();
				setResponse(resp,result,UserInfoHolder.class);
			}
		});
	}
/**
 * 获取系统消息推送
 */
	@Override
	public void getPushMessage(final DataResponse<SystemMsgHolder> response,final int page) {
		mHandler.post(new Command<SystemMsgHolder>(response, this) {
			@Override
			public void doRun() throws Exception{
				String result = HttpRequestGetData.getPushMessage(page);
				setResponse(response,result,SystemMsgHolder.class);
				Log.e("jadon3", result);
			}
		});
	}
	/**
	 * 帖子回复
	 */
    @Override
    public void addComment(final DataResponse<AddCommentHolder> resp, final String pid,
                           final String commentContent, final String fid, final String hashId) {
        mHandler.post(new Command<AddCommentHolder>(resp, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.addComment(pid, commentContent, fid, hashId);
                setResponse(resp, result, AddCommentHolder.class);
            }
        });

    }

/**
 * 帖子点赞
 */
    @Override
    public void addFavour(final DataResponse<AddFavourHolder> resp, final String pid, final String hash) {
        mHandler.post(new Command<AddFavourHolder>(resp, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.addFavour(pid, hash);
                setResponse(resp, result, AddFavourHolder.class);
            }
        });
    }
/**
 * 帖子评分
 */
    @Override
    public void addScore(final DataResponse<AddScorerHolder> resp, final String fid, final String tid, final String hash, final String reason, final String score) {
        mHandler.post(new Command<AddScorerHolder>(resp, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.addScore(fid, tid, hash, reason, score);
                setResponse(resp, result, AddScorerHolder.class);
            }
        });
    }
/**
 * 查询点赞与评分
 * @param resp
 * @param pageId
 * @param hashId
 */
    @Override
    public void queryDetailInfo(final DataResponse<CommentInfoHolder> resp,
                                final String pageId, final String hashId) {
        mHandler.post(new Command<CommentInfoHolder>(resp, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetData.getDetailInfo(pageId, hashId);
                setResponse(resp, result, CommentInfoHolder.class);
            }
        });
    }
//    @Override
//    public void messageReadAll(final DataResponse<MessageReadAllHolder> resp) {
//        // TODO Auto-generated method stub
//        mHandler.post(new Command<MessageReadAllHolder>(resp, this) {
//            @Override
//            public void doRun() throws Exception {
//                String result = HttpRequestGetData.messageReadAll();
//                setResponse(resp, result, MessageReadAllHolder.class);
//            }
//        });
//    }
//
//    @Override
//    public void clearAllMessage(final DataResponse<ClearAllMessageHolder> resp) {
//        // TODO Auto-generated method stub
//        mHandler.post(new Command<ClearAllMessageHolder>(resp, this) {
//            @Override
//            public void doRun() throws Exception {
//                String result = HttpRequestGetData.deleteAllMessage();
//                setResponse(resp, result, ClearAllMessageHolder.class);
//            }
//        });
//    }

//    @Override
//    public void deleteMessage(final DataResponse<DeleteMessageHolder> resp, final String nId) {
//        // TODO Auto-generated method stub
//        mHandler.post(new Command<DeleteMessageHolder>(resp, this) {
//            @Override
//            public void doRun() throws Exception {
//                String result = HttpRequestGetData.deleteMessage(nId);
//                setResponse(resp, result, DeleteMessageHolder.class);
//            }
//        });
//    }

	@Override
	public void systemMsgDetail(final DataResponse<BaseResponseObject> response,final String nid) {
		// TODO Auto-generated method stub
		  mHandler.post(new Command<BaseResponseObject>(response, this) {
	            @Override
	            public void doRun() throws Exception {
	                String result = HttpRequestGetData.systemMsgDetail(nid);
	                setResponse(response, result, BaseResponseObject.class);
	            }
	        });
	}

	@Override
	public void changeUserIcon(final DataResponse<BaseResponseObject> response,
			final String filePath) {
		// TODO Auto-generated method stub
//		http://bbs.iuni.com/api/mobile/index.php?module=uploadavatar
		
		 mHandler.post(new Command<BaseResponseObject>(response, this) {
	            @Override
	            public void doRun() throws Exception {
	                String result = HttpRequestGetData.changeUserIcon(null,filePath);
	                setResponse(response, result, BaseResponseObject.class);
	            }
	        });
		
	}

	@Override
	public void chanageUserIntroduce(final DataResponse<BaseResponseObject> response,
			final String content) {
		 mHandler.post(new Command<BaseResponseObject>(response, this) {
	            @Override
	            public void doRun() throws Exception {
	                String result = HttpRequestGetData.changeUserIntroduce(content);
	                setResponse(response, result, BaseResponseObject.class);
	            }
	        });
		
	}

	@Override
	public void getSystemPushMsg(final DataResponse<SystemPushMsgHolder> response) {
		// TODO Auto-generated method stub
		 mHandler.post(new Command<SystemPushMsgHolder>(response, this) {
	            @Override
	            public void doRun() throws Exception {
	                String result = HttpRequestGetData.getSystemPushMsg();
	                setResponse(response, result, SystemPushMsgHolder.class);
	                Log.e("jadon3", result);
	            }
	        });
	}
    
	/**
	 * 检查版本
	 */
    @Override
	public void checkVersion(final DataResponse<AppUpgradeObject> response,
			final Context context) {
		mHandler.post(new Command<AppUpgradeObject>(response, this) {
			public void doRun() throws Exception {
				String result = HttpRequestGetData.checkVersion(context);
				setResponse(response, result, AppUpgradeObject.class);
			}
		});
	}

}
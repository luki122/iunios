package com.android.auroramusic.share;



import java.io.File;
import java.net.URL;
import com.android.music.R;
import com.android.auroramusic.downloadex.BitmapUtil;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;



public class AuroraWxShare implements IWXAPIEventHandler{

	private static final String TAG = "AuroraWxShare";
	private static AuroraWxShare mInstance;
	private Context mContext = null;
	private IWXAPI mWxApi;
	private AuroraWxShareCallBack mWxShareCallBack = null;
	public static final int AURORA_CMD_GETMESSAGE_FROM_WX = 0;
	public static final int AURORA_CMD_SHOWMESSAGE_FROM_WX = 1;
	static enum AuroraRespStatus {
		AURORA_RESP_OK,
		AURORA_RESP_CANCEL, 
		AURORA_RESP_SENT_FAILED,
		AURORA_RESP_DENIED, 
		AURORA_RESP_UNKNOW, 
	};
	private static final String AURORA_SHARE_DEFAULT_ARTIST = "IUNI OS";
	private static final String AURORA_SHARE_DEFAULT_TITLE = "Music";
	private static final int AURORA_PIC_SIZE = (32*1024);
	
	private boolean mbSupport = true;
	private int wxSdkVersion = 0;
	private boolean mbInstalled = true;
	
	
	public interface AuroraWxShareCallBack{
		public void OnAuroraWxReq(int req);
		public void OnAuroraWxResp(AuroraRespStatus respType);
	};
	
	
	protected AuroraWxShare(Context context) {
		mWxApi = WXAPIFactory.createWXAPI(context, Globals.SHARE_WX_APP_ID, true);
		mWxApi.registerApp(Globals.SHARE_WX_APP_ID);
		mContext = context;
		mWxShareCallBack = null;
		initVersion();
    }
	
	private boolean initVersion(){
		wxSdkVersion = mWxApi.getWXAppSupportAPI();
		if (wxSdkVersion >= Globals.SHARE_TIMELINE_SUPPORTED_VERSION) {
			mbSupport = true;
		} else {
			mbSupport = false;
		}
		return mbSupport;
	}
	
	public static synchronized AuroraWxShare getInstance(Context context) {
		if(mInstance == null)
            mInstance = new AuroraWxShare(context);
		
        return mInstance;
    }

	public void setHandleIntentAndCallBack(Intent intent, AuroraWxShareCallBack callBack) {
		if (mInstance != null && mInstance.mWxApi != null) {
			boolean flag = mInstance.mWxApi.handleIntent(intent, this);
			Log.i(TAG, "zll ---- setHandleIntent 1 flag:"+flag);
			registerWxShareCallBack(callBack);
		}
		
		return;
	}
	
	public static void unRegisterApp() {
		if (mInstance != null && mInstance.mWxApi != null) {
			mInstance.mWxApi.unregisterApp();
			mInstance.mWxShareCallBack = null;
		}
		mInstance = null;

		return;
	}
	
	private void registerWxShareCallBack(AuroraWxShareCallBack callBack) {
		if (mWxShareCallBack != null && callBack != null) {
			return;
		}
		
		mWxShareCallBack = callBack;
		return;
	}
	
	public void unRegisterWxShareCallBack() {
		mWxShareCallBack = null;
		return;
	}
	
	public void sendWebMusic2Wx(String url, boolean bSceneTimeline, String title, String artist) {
		if (mInstance == null || url == null || mWxApi == null) {
			return;
		}
		
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = url;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = title;
		msg.description = artist;
		Bitmap thumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_music);
		msg.thumbData = AuroraShareUtil.bmpToByteArray(thumb, true);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = bSceneTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		mWxApi.sendReq(req);
		
		return;
	}
	
	private Bitmap getShareBitmap(String path, String bitrate, String title, String artist, String album) {
		Bitmap bm = null;
		
		if ((path != null && !TextUtils.isEmpty(path))) {
			//bm = BitmapFactory.decodeFile(path);
			LogUtil.iv(TAG, "zll ---- getShareBitmap 1:");
			bm = BitmapUtil.decodeSampledBitmapFromFileForSmall(path, 70, 70);
		}
		
		if (bm == null) {
			try {
				
				//bm = BitmapFactory.decodeFile(imgpath);
			} catch (Exception e) {
				Log.i(TAG, "zll ---- getShareBitmap fail");
				bm = null;
			}
		}
		
		if (bm == null) {
			LogUtil.iv(TAG, "zll ---- getShareBitmap 3:");
			bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.album_art_default);
		}
		return bm;
	}
	
	//bSceneTimeline =true,则表示分享至 朋友圈，否则分享至会话:
	public void sendMusic2Wx(String url, String neturl, boolean bSceneTimeline, String title, String artist, String pic, String album, String bitrate, Bitmap bm) {
		if (mInstance == null || 
			(url == null && neturl == null)|| 
			mWxApi == null) {
			return;
		}
		
		if (!mWxApi.isWXAppInstalled()) {
			Toast.makeText(mContext, R.string.aurora_wx_not_installed, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (!mbSupport) {
			if(!initVersion()){
				Toast.makeText(mContext, "wxSdkVersion = " + Integer.toHexString(wxSdkVersion) + "\ntimeline not supported", Toast.LENGTH_LONG).show();
				return;
			}
		} 
		
		LogUtil.iv(TAG, "zll ---- sendMusic2Wx pic:"+pic+",url:"+url+",neturl:"+neturl+",bm:"+bm);
		WXMusicObject music = new WXMusicObject();
		//music.musicUrl = "http://www.baidu.com";
		music.musicDataUrl = url;
		music.musicUrl = neturl ;//url;//"http://staff2.ustc.edu.cn/~wdw/softdown/index.asp/0042515_05.ANDY.mp3";
		//music.musicUrl="http://120.196.211.49/XlFNM14sois/AKVPrOJ9CBnIN556OrWEuGhZvlDF02p5zIXwrZqLUTti4o6MOJ4g7C6FPXmtlh6vPtgbKQ==/31353278.mp3";
		//music.musicDataUrl = neturl;
		//music.musicLowBandUrl = url;//neturl;

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = music;
		msg.title = title;
		
		if (artist == null || 
			(artist != null && artist.isEmpty())) {
			msg.description = AURORA_SHARE_DEFAULT_ARTIST;
		} else {
			msg.description = artist;
		}
	
		Bitmap thumb = null;
		
		if (bm == null) {
			thumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.album_art_default);
		} else {
			thumb = bm;
		}
		
		/*if (pic == null || 
			(pic != null && pic.equals(AuroraSearchLyricActivity.DEFUALT_IMG_URL))) {
			thumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.album_art_default);
		} else {
			//thumb = getShareBitmap(pic, bitrate, title, artist, album);
		}*/

		msg.thumbData = AuroraShareUtil.bmpToByteArray(thumb, true);
		//Log.i(TAG, "zll ---- sendMusic2Wx 1.2 success thumbData:"+msg.thumbData.length);
		LogUtil.iv(TAG, "zll ---- getShareBitmap 4 msg.thumbData.length:"+msg.thumbData.length);
		if (msg.thumbData.length >= AURORA_PIC_SIZE) {
			thumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.album_art_default);
			msg.thumbData = AuroraShareUtil.bmpToByteArray(thumb, true);
		}
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("music");
		req.message = msg;
		req.scene = bSceneTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		boolean flag = mWxApi.sendReq(req);
		
		//Log.i(TAG, "zll ---- sendMusic2Wx 2 success:");
		return;
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	@Override
	public void onReq(BaseReq req) {
		Log.i(TAG, "zll ---- onReq req:"+req.getType());
		int ret = -1;
		
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			ret = AURORA_CMD_GETMESSAGE_FROM_WX;
			break;
			
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			ret = AURORA_CMD_SHOWMESSAGE_FROM_WX;
			break;
			
		default:
			break;
		}
		
		if (mWxShareCallBack != null) {
			mWxShareCallBack.OnAuroraWxReq(ret);
		}
	}

	@Override
	public void onResp(BaseResp resq) {
		Log.i(TAG, "zll ---- onResp resq errCode:"+resq.errCode+",errStr:"+resq.errStr);
		AuroraRespStatus ret = AuroraRespStatus.AURORA_RESP_UNKNOW;
		
		switch (resq.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			//result = R.string.errcode_success;
			ret = AuroraRespStatus.AURORA_RESP_UNKNOW;
			break;
			
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			//result = R.string.errcode_cancel;
			ret = AuroraRespStatus.AURORA_RESP_CANCEL;
			break;
			
		case BaseResp.ErrCode.ERR_SENT_FAILED:
			ret = AuroraRespStatus.AURORA_RESP_SENT_FAILED;
			break;
			
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			//result = R.string.errcode_deny;
			ret = AuroraRespStatus.AURORA_RESP_DENIED;
			break;
			
		default:
			//result = R.string.errcode_unknown;
			ret = AuroraRespStatus.AURORA_RESP_UNKNOW;
			break;
		}
		
		if (mWxShareCallBack != null) {
			mWxShareCallBack.OnAuroraWxResp(ret);
		}
	}
}

package com.aurora.iunivoice.share.wx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;
import android.widget.Toast;

import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.share.ShareCommonUtil;
import com.aurora.iunivoice.utils.Util;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

public class WxShareUtil {

    private static final int THUMB_SIZE = 150;

    private IWXAPI api;

    public WxShareUtil(Context context) {
        api = WXAPIFactory.createWXAPI(context, WxConstants.APP_ID, false);
    }

    public void registerApp(Context context) {
        api.registerApp(WxConstants.APP_ID);
    }

    public static abstract class ShareContent{
        protected abstract String getContent();
        protected abstract String getTitle();
        protected abstract String getURL();
        protected abstract int getPicResource();
    }

    /**
     * 设置分享链接的内容
     * @author Administrator
     *
     */
    public static class ShareContentWebpage extends ShareContent{
        private String title;
        private String content;
        private String url;
        private int picResource;
        public ShareContentWebpage(String title, String content,
                                   String url, int picResource){
            this.title = title;
            this.content = content;
            this.url = url;
            this.picResource = picResource;
        }

        @Override
        protected String getContent() {
            return content;
        }

        @Override
        protected String getTitle() {
            return title;
        }

        @Override
        protected String getURL() {
            return url;
        }

        @Override
        protected int getPicResource() {
            return picResource;
        }
    }

    public void shareWebPage(ShareContent shareContent, boolean timeline) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareContent.getURL();
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareContent.getTitle();
        //msg.description = shareContent.getContent();
        msg.description = "aaaaa";
        Bitmap thumb = BitmapFactory.decodeResource(IuniVoiceApp.getInstance().getResources(),
                R.drawable.share_icon);
        if(thumb == null){
            Toast.makeText(IuniVoiceApp.getInstance(), "图片不能为空", Toast.LENGTH_SHORT).show();
        }else{
            msg.thumbData = Util.bmpToByteArray(thumb, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        api.sendReq(req);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        int i;
        int j;
        if (bmp.getHeight() > bmp.getWidth()) {
            i = bmp.getWidth();
            j = bmp.getWidth();
        } else {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0,i, j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                //F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }

    public void sendText(Context context, String text, boolean timeline) {
        // 初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = text;

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        // 调用api接口发送数据到微信
        api.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }



    public void sendImage(Context context, Bitmap bmp, String filePath, String url, boolean timeline) {
        int type = 0;
        if (bmp != null) {
            type = 1;
        } else if (!TextUtils.isEmpty(filePath)) {
            type = 2;
        } else if (!TextUtils.isEmpty(url)) {
            type = 3;
        }

        if (type == 0) {
            return;
        }

        switch (type) {
            case 1: {
                try {
                    WXImageObject imgObj = new WXImageObject(bmp);

                    WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imgObj;

                    Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                    bmp.recycle();
                    msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 设置缩略图

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = buildTransaction("img");
                    req.message = msg;
                    req.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
                    api.sendReq(req);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getString(R.string.share_wx_send_fail), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 2: {
                try {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        return;
                    }

                    WXImageObject imgObj = new WXImageObject();
                    imgObj.setImagePath(filePath);

                    WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imgObj;

                    Bitmap bmpFromPath = getimage(filePath);
                    Bitmap thumbBmp = Bitmap.createScaledBitmap(bmpFromPath, THUMB_SIZE, THUMB_SIZE, true);
                    bmpFromPath.recycle();
                    msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = buildTransaction("img");
                    req.message = msg;
                    req.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
                    api.sendReq(req);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getString(R.string.share_wx_send_fail), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 3: {
                try {
                    WXImageObject imgObj = new WXImageObject();
                    imgObj.imageUrl = url;

                    WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imgObj;

                    Bitmap bmpFromUrl = BitmapFactory.decodeStream(new URL(url).openStream());
                    Bitmap thumbBmp = Bitmap.createScaledBitmap(bmpFromUrl, THUMB_SIZE, THUMB_SIZE, true);
                    bmpFromUrl.recycle();
                    msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = buildTransaction("img");
                    req.message = msg;
                    req.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
                    api.sendReq(req);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getString(R.string.share_wx_send_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public boolean isSupport(Context context) {
        return api.isWXAppInstalled() && api.isWXAppSupportAPI();
    }

    public void handleIntent(Intent intent, IWXAPIEventHandler handler) {
        api.handleIntent(intent, handler);
    }

    private Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了  
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        float hh = ShareCommonUtil.SCREEN_HEIGHT;
        float ww = ShareCommonUtil.SCREEN_WIDTH;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  
        int be = 1;//be=1表示不缩放  
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
            be = (int) ((newOpts.outHeight / hh));
        }
        if (be <= 0)
            be = 1;

        newOpts.inSampleSize = be;//设置缩放比例  
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩  
    }

    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos  
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10  
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

}

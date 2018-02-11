package aurora.lib.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import aurora.lib.app.AuroraActivity;

/**
 * @author leftaven 2013年9月29日 TODO 背景蒙层监听
 */
@SuppressLint("HandlerLeak")
public class AuroraAlphaListener {
	// coverView的alpha值
	private float image_alpha = 1.0f;
	// 线程是否运行判断变量
	private boolean isRunning = false;
	private Window window;
	private WindowManager.LayoutParams wl;
	private int type = 0;// 0 表示菜单消失，1表示显示菜单
	private Context context;
	private AuroraActivity activity;
	// Handler对象用来给UI_Thread的MessageQueue发送消息
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			wl.alpha = image_alpha;// 设置透明度,0.0为完全透明，1.0为完全不透明
			window.setAttributes(wl);
			// 刷新视图
		}
	};

	public AuroraAlphaListener(Context context) {
		this.context = context;
		initAlpha();
	}

	/**
	 * 初始化alpha值以及窗口数据
	 */
	public void initAlpha() {
		this.activity = (AuroraActivity) context;
		window = activity.getWindow();
		wl = window.getAttributes();
		isRunning = true;
		image_alpha = 1.0f;

	}

	// 更新Alpha
	private void updateAlpha(int type) {
		if (type == 1) {
			image_alpha -= 0.0018f;
			if (image_alpha <= 0.40f) {
				image_alpha = 0.40f;
				isRunning = false;
			}
		} else if (type == 0) {
			image_alpha += 0.0010f;
			if (image_alpha >= 1.0f) {
				image_alpha = 1.0f;
				isRunning = false;
			}
		} else {
			image_alpha = 0.40f;
			isRunning = false;
		}

		// 发送需要更新coverView视图的消息-->这里是发给主线程
		mHandler.sendMessage(mHandler.obtainMessage());
	}

	/**
	 * menu关闭，蒙层效果
	 */
	public void startMenuDismissThread() {
		isRunning = true;
		type = AuroraUtil.MENU_DISMISS;
		// 开启一个线程来让Alpha值递减
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isRunning) {
					try {
						Thread.sleep(1);
						// 更新Alpha值
						updateAlpha(type);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * menu打开，蒙层效果
	 */
	public void startMenuShowThread() {
		isRunning = true;
		type = AuroraUtil.MENU_SHOW;
		image_alpha = 1.0f;
		// 开启一个线程来让Alpha值递减
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isRunning) {
					try {
						Thread.sleep(1);
						// 更新Alpha值
						updateAlpha(type);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// updateAlpha(type);
				}
			}
		}).start();
		// 接受消息之后更新imageview视图
	}
}

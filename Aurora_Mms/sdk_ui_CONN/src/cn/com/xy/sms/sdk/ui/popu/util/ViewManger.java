package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleAirTable;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyCallsMessage;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBottomTwo;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleCodeHead;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleCodeTable;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleHorizTable;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTrainTable;
import cn.com.xy.sms.sdk.ui.popu.part.CardBodySplit;
import cn.com.xy.sms.sdk.ui.popu.part.TopBodySplit;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;
import cn.com.xy.sms.sdk.ui.popu.widget.IViewAttr;
import cn.com.xy.sms.sdk.util.StringUtils;

public class ViewManger {

	public static final int ONE_SIDE_POPUPVIEW = 1;
	private static final int TYPE_PADDING_11 = getIntDimen(
			Constant.getContext(), R.dimen.duoqu_type_padding_11);
	private static final int TYPE_VIEW_HEIGHT_11 = getIntDimen(
			Constant.getContext(), R.dimen.duoqu_type_view_height_11);
	private static final int TYPE_SPLIT_LR_MARGIN_111 = getIntDimen(
			Constant.getContext(), R.dimen.duoqu_type_split_lr_margin_111);
	private static final int TYPE_SPLIT_LR_MARGIN_112 = getIntDimen(
			Constant.getContext(), R.dimen.duoqu_type_split_lr_margin_112);
	private static final int TYPE_MARGIN_11 = getIntDimen(
			Constant.getContext(), R.dimen.duoqu_type_margin_11);

	/*
	 * 101-499 for head;501~899 for body;901~999 for button Don't set in
	 * multiples of 100 number
	 */
	private final static Integer VIEW_PART_ID[] = {
			ViewPartId.PART_HEAD_CODE,
			ViewPartId.PART_BODY_HORIZ_TABLE, ViewPartId.PART_BODY_AIR_TABLE,
			ViewPartId.PART_BODY_TRAIN_TABLE, ViewPartId.PART_BODY_CODE_TABLE,
			ViewPartId.PART_BOTTOM_TWO_BUTTON, ViewPartId.PART_BOTTOM_SPLIT,
			ViewPartId.PART_CARD_BODY_SPLIT,
	        ViewPartId.PART_BODY_CALLS_MESSAGE };// all part

	/**
	 * Head part
	 * 
	 * @param context
	 * @param message
	 * @param xyCallBack
	 * @param root
	 * @param partId
	 * @return
	 * @throws Exception
	 */
	private static UIPart getHeadUIPartByPartId(Activity context,
			BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root,
			int partId) throws Exception {
		UIPart part = null;
		switch (partId) {
		case ViewPartId.PART_HEAD_CODE:
			part = new BubbleCodeHead(context, message, xyCallBack,
					R.layout.duoqu_code_title_head, root, partId);
			part.mNeedFirstToPadding = false;
			break;
		default:
			break;
		}
		return part;
	}

	/**
	 * Body part
	 * 
	 * @param context
	 * @param message
	 * @param xyCallBack
	 * @param root
	 * @param partId
	 * @return
	 * @throws Exception
	 */
	private static UIPart getBodyUIPartByPartId(Activity context,
			BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root,
			int partId) throws Exception {
		UIPart part = null;
		switch (partId) {
		case ViewPartId.PART_BODY_HORIZ_TABLE:
			part = new BubbleHorizTable(context, message, xyCallBack,
					R.layout.duoqu_horz_table, root, partId);
			break;
		case ViewPartId.PART_BODY_AIR_TABLE:
			part = new BubbleAirTable(context, message, xyCallBack,
					R.layout.duoqu_air_body, root, partId);
			break;
		case ViewPartId.PART_BODY_TRAIN_TABLE:
			part = new BubbleTrainTable(context, message, xyCallBack,
					R.layout.duoqu_train_body, root, partId);
			break;
		case ViewPartId.PART_BODY_CODE_TABLE:
			part = new BubbleCodeTable(context, message, xyCallBack,
					R.layout.duoqu_code_body, root, partId);
			break;
        case ViewPartId.PART_BODY_CALLS_MESSAGE:
            part = new BubbleBodyCallsMessage(context, message, xyCallBack, R.layout.duoqu_bubble_body_callsmessage,
                    root, partId);
            break;
		default:
			break;
		}
		return part;
	}

	/**
	 * Foot part
	 * 
	 * @param context
	 * @param message
	 * @param xyCallBack
	 * @param root
	 * @param partId
	 * @return
	 * @throws Exception
	 */
	private static UIPart getFootUIPartByPartId(Activity context,
			BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root,
			int partId) throws Exception {
		UIPart part = null;
		switch (partId) {
		case ViewPartId.PART_BOTTOM_TWO_BUTTON:
			part = new BubbleBottomTwo(context, message, xyCallBack,
					R.layout.duoqu_bubble_bottom_two, root, partId);
			break;
		case ViewPartId.PART_BOTTOM_SPLIT:
			part = new TopBodySplit(context, message, xyCallBack,
					R.layout.duoqu_top_split, root, partId);
			part.putParam("MLR", 112);
			break;
		case ViewPartId.PART_CARD_BODY_SPLIT:
			part = new CardBodySplit(context, message, xyCallBack,
					R.layout.duoqu_card_bubble_split, root, partId);
			break;
		default:
			break;
		}
		return part;
	}

	static boolean checkHasViewPartId(int partId) throws Exception {
		for (Integer i : VIEW_PART_ID) {
			if (i == partId) {
				return true;
			}
		}
		throw new Exception("checkHasViewPartId partId: " + partId
				+ " not Find.");
	}

	public static void setViewBg(Context context, View view,
			String relativePath, int resId, int width) throws Exception {
		setViewBg(context, view, relativePath, resId, width, false);
	}
	
	public static void setViewBg(Context context, View view, String relativePath, int resId, int width, boolean cache)
			throws Exception {
		setViewBg(context, view, relativePath, resId, width, cache, false);
	}
	
	public static void setViewBg(Context context, View view,
			String relativePath, int resId, int width, boolean cache, boolean needColorDw)
			throws Exception {
		// LogManager.i("setViewBg", "relativePath=" + relativePath + "resId=" +
		// resId);
		// if (context == null)
		// return;
		try {
			Drawable dw = ViewUtil.getDrawable(context, relativePath, needColorDw,
					cache);
//			Drawable dw =null;
			if (dw != null) {
				ViewUtil.setBackground(view, dw.mutate());
			} else {
				if (resId != -1) {
					view.setBackgroundResource(resId);
					// view.setBackgroundDrawable(dw);
					// view.setTag(true);
					GradientDrawable myGrad = (GradientDrawable) view
							.getBackground();
					if (!StringUtils.isNull(relativePath)) {
						int color =ResourceCacheUtil
						.parseColor(relativePath);
						myGrad.setColor(color);
						width = width > 0?width:0;
//						if (width > 0) {
							myGrad.setStroke(width,color);
//						} else {
//							myGrad.setStroke(0,ResourceCacheUtil
//									.parseColor(relativePath));
//						}
					}
//					view.setBackground(myGrad);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void setViewBg(Context context, View view, String bgColor,
			String strokeColor, int resId, int width) throws Exception {
		// LogManager.i("setViewBg", "bgColor=" + bgColor + "strokeColor=" +
		// strokeColor + "resId=" + resId);
		if (context == null)
			return;
		try {
			if (view != null && !StringUtils.isNull(bgColor)
					&& !StringUtils.isNull(strokeColor)) {
				bgColor = bgColor.trim();
				strokeColor = strokeColor.trim();
				try {
					view.setBackgroundResource(resId);

					GradientDrawable myGrad = (GradientDrawable) view
							.getBackground();
					if (!StringUtils.isNull(bgColor)) {
						myGrad.setColor(ResourceCacheUtil.parseColor(bgColor));
					}
					if (!StringUtils.isNull(strokeColor)) {
						myGrad.setStroke(width,
								ResourceCacheUtil.parseColor(strokeColor));

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static View createContextByLayoutId(Context packAgeCtx,
			int layoutId, ViewGroup root) {
		try {
			LayoutInflater currentInflater = (LayoutInflater) packAgeCtx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return currentInflater.inflate(layoutId, root);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isPopupAble(Map<String, Object> handervalueMap,
			String titleNo) {

		try {
			// Log.i("handervalueMap", handervalueMap.toString());
			if (handervalueMap == null || StringUtils.isNull(titleNo))
				return false;
			int viewid = -1;
			if (handervalueMap.containsKey("View_viewid")) {
				String id = (String) handervalueMap.get("View_viewid");
				// if (LogManager.debug) {
				// Log.i("PopupMsgManager", "View_viewid=" + id);
				// }
				if (!StringUtils.isNull(id)) {
					viewid = Integer.parseInt(id);

					try {
						String viewPartParam = (String) handervalueMap
								.get("View_fdes");
						// viewPartParam="H254;B652,100000;F954950";
						Map<String, PartViewParam> viewPartParamMap = parseViewPartParam(viewPartParam);
						if (viewPartParamMap != null
								&& !viewPartParamMap.isEmpty()) {
							handervalueMap.put("viewPartParam",
									viewPartParamMap);
							if (viewid == ViewManger.ONE_SIDE_POPUPVIEW) {
								//
							} else if (viewid == ViewManger.ONE_SIDE_POPUPVIEW) {

							}
							return true;
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					return false;

				}
			} else {
				// if (LogManager.debug) {
				// Log.i("PopupMsgManager", "View_viewid is null");
				// }
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public static int getIdentifier(String name, String defType) {
		return Constant
				.getContext()
				.getResources()
				.getIdentifier(name, defType,
						Constant.getContext().getPackageName());
	}

	/**
	 * Dimen intValue
	 * 
	 * @param ctx
	 * @param dimenId
	 * @return
	 */
	public static int getIntDimen(Context ctx, int dimenId) {
		float f = ctx.getResources().getDimension(dimenId);
		return Math.round(f);
	}

	public static int getDouquAttrDimen(IViewAttr iAttr, int duoquAttrId) {
		Object f = iAttr.obtainStyledAttributes(IViewAttr.ATTR_TYPE_DIMEN,
				duoquAttrId);
		if (f != null) {
			return Math.round((Float) f);
		}
		return 0;
	}

	public static Object obtainStyledAttributes(TypedArray duoquAttr,
			byte styleType, int styleId) {
		Object obj = null;
		if (duoquAttr != null) {
			switch (styleType) {
			case IViewAttr.ATTR_TYPE_DIMEN:
				obj = duoquAttr.getDimension(styleId, -1);
				break;
			}
			duoquAttr.recycle();
		}
		return obj;
	}

	public static ArrayList<Integer> getViewPartList(String orgNo)
			throws Exception {

		ArrayList<Integer> res = new ArrayList<Integer>();
		int len = orgNo.length();
		int viewPartId;
		for (int i = 0; i < len; i += 3) {
			if (i + 3 > len)
				break;
			viewPartId = Integer.parseInt(orgNo.substring(i, i + 3));
			checkHasViewPartId(viewPartId);
			res.add(viewPartId);
		}
		return res;
	}

	private static void setPartViewParamRule(PartViewParam param,
			String paramStr) throws Exception {

		if (paramStr != null) {
			int len = paramStr.length();
			if (len > 0) {
				param.mNeedScroll = Integer.parseInt(paramStr.substring(0, 1)) == 1 ? true
						: false;
			}
			if (len > 1) {
				param.mAddImageMark = Integer
						.parseInt(paramStr.substring(1, 2)) == 1 ? true : false;
			}
			if (len > 3) {
				param.mBodyHeightType = Integer.parseInt(paramStr.substring(2,
						4));
			}
			if (len > 5) {
				param.mBodyMaxHeightType = Integer.parseInt(paramStr.substring(
						4, 6));
			}
			if (len > 7) {
				param.mPaddingLeftType = Integer.parseInt(paramStr.substring(6,
						8));
			}
			if (len > 9) {
				param.mPaddingTopType = Integer.parseInt(paramStr.substring(8,
						10));
			}
			if (len > 11) {
				param.mPaddingRightType = Integer.parseInt(paramStr.substring(
						10, 12));
			}
			if (len > 13) {
				param.mPaddingBottomType = Integer.parseInt(paramStr.substring(
						12, 14));
			}
			if (len > 15) {
				param.mUiPartMarginTopType = Integer.parseInt(paramStr
						.substring(14, 16));
			}
		}

	}

	public static Map<String, PartViewParam> parseViewPartParam(
			String uiPartParam) throws Exception {
		if (uiPartParam == null)
			return null;
		Map<String, PartViewParam> res = null;
		String[] attr = uiPartParam.split(";");
		if (attr != null) {
			res = new HashMap<String, PartViewParam>();
			PartViewParam temp = null;
			String tempStr = null;
			String typeKey = null;
			for (String str : attr) {
				temp = new PartViewParam();
				int index = str.indexOf(",");
				if (index > 0) {// View rules
					tempStr = str.substring(0, index);// View part
					str = str.substring(index + 1);// View the rules section
				} else {
					tempStr = str;
					str = null;// Don't view the rules
				}
				typeKey = tempStr.substring(0, 1);
				if (PartViewParam.HEAD.equals(typeKey)
						|| PartViewParam.FOOT.equals(typeKey)
						|| PartViewParam.BODY.equals(typeKey)) {
					temp.mLayOutList = getViewPartList(tempStr.substring(1));
					res.put(typeKey, temp);
					// Processing view rules
					setPartViewParamRule(temp, str);
				}
			}
		}
		return res;
	}

	/**
	 * Create watermark mark
	 * 
	 * @param packAgeCtx
	 * @return
	 */
	public static View getDuoquImgMark(Context packAgeCtx) {
		return ViewManger.createContextByLayoutId(packAgeCtx,
				R.layout.duoqu_img_mark, null);
	}

	/**
	 * Create time mark
	 * 
	 * @param packAgeCtx
	 * @return
	 */
	public static View getDuoquTimeMark(Context packAgeCtx) {
		return ViewManger.createContextByLayoutId(packAgeCtx,
				R.layout.duoqu_bottom_info, null);
	}

	public static UIPart getUIPartByPartId(Activity context,
			BusinessSmsMessage message, XyCallBack xyCallBack, ViewGroup root,
			int partId) throws Exception {
		UIPart part = null;
		if (partId < 500) {
			part = getHeadUIPartByPartId(context, message, xyCallBack, root,
					partId);
		} else if (partId < 900) {
			part = getBodyUIPartByPartId(context, message, xyCallBack, root,
					partId);

		} else if (partId >= 900) {
			part = getFootUIPartByPartId(context, message, xyCallBack, root,
					partId);
		}
		return part;
	}

	public static ScrollView createScrollView(final Context packAgeCtx,
			View root) {
		final ScrollView sView = (ScrollView) ViewManger
				.createContextByLayoutId(packAgeCtx,
						R.layout.duoqu_scroll_view, null);
		return sView;
	}

	public static ViewGroup createFrameViewGroup(Context packAgeCtx) {
		return (ViewGroup) ViewManger.createContextByLayoutId(packAgeCtx,
				R.layout.duoqu_frame_view, null);
	}

	public static RelativeLayout createRootView(Context packAgeCtx) {
		RelativeLayout rootView = new RelativeLayout(packAgeCtx);
		ViewGroup.LayoutParams lp = new LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rootView.setLayoutParams(lp);
		return rootView;
	}

	public static int setBodyViewPadding(Context context, View view,
			View childView, PartViewParam viewParam, int addPadding) {
		if (view == null || viewParam == null)
			return -1;
		int leftPadding = getBodyViewPadding(context,
				viewParam.mPaddingLeftType);
		int topPadding = getBodyViewPadding(context, viewParam.mPaddingTopType);
		int rightPadding = getBodyViewPadding(context,
				viewParam.mPaddingRightType);
		int bottomPadding = getBodyViewPadding(context,
				viewParam.mPaddingBottomType);

		if (leftPadding != 0 || topPadding != 0 || rightPadding != 0
				|| bottomPadding != 0) {
			view.setPadding(leftPadding, topPadding, rightPadding,
					bottomPadding);
		}

		return 1;
	}

	public static int getBodyViewPadding(Context context, int type) {
		int padding = 0;
		switch (type) {
		case 11:
			padding = TYPE_PADDING_11;
			break;
		default:
			break;
		}
		return padding;
	}

	public static int setBodyLayoutHeight(Context context,
			ViewGroup.LayoutParams lparam, int layoutHeightType,
			int sBodyPadding) {
		// if (layoutHeightType < 10)
		// return -1;
		int h = -1;
		switch (layoutHeightType) {
		case 11:
			h = TYPE_VIEW_HEIGHT_11;
			break;
		default:
			break;
		}
		if (h != -1) {
			lparam.height = h;
		}
		return h;
	}

	/**
	 * Margin data access procedures for internal use
	 * 
	 * @param context
	 * @param marginType
	 * @return
	 */
	public static int getInnerLayoutMargin(Context context, int marginType) {
		int margin = 0;
		switch (marginType) {
		case 111:
			margin = TYPE_SPLIT_LR_MARGIN_111;
			break;
		case 112:
			margin = TYPE_SPLIT_LR_MARGIN_112;
			break;
		default:
			break;
		}
		return margin;
	}

	public static int setLayoutMarginTop(Context context,
			ViewGroup.LayoutParams lparam, int marginTopType) {

		int marginTop = -1;
		if (lparam != null && lparam instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) lparam;

			switch (marginTopType) {
			case 11:
				marginTop = TYPE_MARGIN_11;
				break;
			default:
				break;
			}
			if (marginTop != -1 && lp != null) {
				lp.setMargins(lp.leftMargin, marginTop, lp.rightMargin,
						lp.bottomMargin);
			}
		}
		return marginTop;

	}

	public static void setViewTreeObserver(final View view,
			final XyCallBack callBack) {
		try {
			final ViewTreeObserver vto = view.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@SuppressLint("NewApi")
				public void onGlobalLayout() {
					try {
						view.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);

					} catch (java.lang.NoSuchMethodError ex) {
						try {
							view.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						} catch (java.lang.NoSuchMethodError e) {
							if (LogManager.debug) {
								e.printStackTrace();
							}
							// e.printStackTrace();
						} catch (Exception e) {
							//
							if (LogManager.debug) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						if (LogManager.debug) {
							e.printStackTrace();
						}
						// e.printStackTrace();
					}
					callBack.execute();
				}
			});
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Set the view of water ripple
	 * 
	 * @param view
	 */
	public static void setRippleDrawable(View view) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Whether can display the watermark mark
	 * 
	 * @param msg
	 * @return
	 */
	public static boolean displayMarkImage(BusinessSmsMessage msg) {

		return true;
	}

	/**
	 * Whether can display the time and the layout of the double card
	 * 
	 * @param msg
	 * @return
	 */
	public static boolean displayTime(BusinessSmsMessage msg) {

		return false;
	}

	public static int indexOfChild(View view, ViewGroup apView) {
		if (view == null || apView == null) {
			// android.util.Log.e("duoqu_xiaoyuan",
			// "indexOfChild view == null || apView == null");
			return -1;
		}
		int childCount = apView.getChildCount();
		View child = null;
		View tempChild = null;
		// android.util.Log.w("duoqu_xiaoyuan", "childCount : "+childCount);
		for (int i = 0; i < childCount; i++) {
			child = apView.getChildAt(i);
			if (child == view) {
				// android.util.Log.d("duoqu_xiaoyuan",
				// "indexOfChild child == views");
				return i;
			} else {
				tempChild = child
						.findViewById(cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager.DUOQU_BUBBLE_VIEW_ID);
				if (tempChild == null) {
					// android.util.Log.w("duoqu_xiaoyuan",
					// "indexOfChild not find tempChild.");
					continue;
				}
				if (tempChild == view) {
					// android.util.Log.w("duoqu_xiaoyuan",
					// " indexOfChild find tempChild tempChild == view");
					return i;
				}
			}
		}
		// android.util.Log.e("duoqu_xiaoyuan",
		// "indexOfChild not find tempChild: -1");
		return -1;
	}

	/**
	 * 是否含打开短信原文
	 * 
	 * @param message
	 * @return
	 */
	public static boolean isOpensmsEnable(BusinessSmsMessage message) {
		if (message != null) {
			String isOpensms_enable = (String) message
					.getValue("opensms_enable");
			if (!StringUtils.isNull(isOpensms_enable)
					&& isOpensms_enable.equals("true"))
				return true;
		}
		return false;
	}
}

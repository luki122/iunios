package com.aurora.voiceassistant.view;

import java.util.HashMap;
import java.util.Iterator;

import com.aurora.voiceassistant.account.TotalCount;
import com.aurora.voiceassistant.model.CFG;
import com.aurora.voiceassistant.model.Response;
import com.aurora.voiceassistant.model.RspXmlTq;
import com.aurora.voiceassistant.view.MainActivity.HideState;
import com.aurora.voiceassistant.view.MainActivity.OFFLINE_TYPE;
import com.aurora.voiceassistant.view.ViewimageActivity.pagerAdapter;
import com.aurora.voiceassistant.R;

import android.R.integer;
import android.R.layout;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity.Header;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class LayoutManager {
	private Context context;
	private Handler mHandler;
	private LayoutInflater mLayoutInflater;
	private LinearLayout mVoiceContentLayout;
	private LinearLayout mBaseItemView;
	private AlertDialog mClearScreenDialog;
	private AlertDialog mDeleteDialog;
	private MainActivity mainActivity;
	
	private boolean firstSearching = false;
	private PopupWindow mPopupWindow;
	
	public LayoutManager(Context context, Handler mHandler, LinearLayout contentLayout) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.mHandler = mHandler;
		this.mVoiceContentLayout = contentLayout;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	public void getMainActivity(MainActivity m) {
		mainActivity = m;
	}
	
	public int getChildCount() {
		return mVoiceContentLayout.getChildCount();
	}

	public void removeOutOfLengthItem() {
		if (mVoiceContentLayout.getChildCount() == 15) {
			Log.d("DEBUG","remove the first item when childcount == 15");
			mVoiceContentLayout.removeViewAt(1);
		}
	}
	
	public void clearAllItems() {
		mVoiceContentLayout.removeAllViews();
	}
	
	public void clearScreen() {
		String dialogTitle = context.getResources().getString(R.string.vs_dialogtitle);
		String dialogMessage = context.getResources().getString(R.string.vs_dialogmessage);
		String dialogYes = context.getResources().getString(R.string.vs_dialog_yes);
		String dialogNo = context.getResources().getString(R.string.vs_dialog_no);
		
		mClearScreenDialog = new AlertDialog.Builder(context)
		.setTitle(dialogTitle)
		.setMessage(dialogMessage)
		.setPositiveButton(dialogYes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
//				mVoiceContentLayout.removeAllViews();
				int childCount = mVoiceContentLayout.getChildCount();
				
				if (childCount <= 1) return;
				
				while (childCount > 1) {
					mVoiceContentLayout.removeViewAt(childCount - 1);
					childCount--;
				}
				
				Message msg = Message.obtain();
				msg.what = 2;						//MSG_TYPE_UPDATE_FILLVIEW = 2
				Bundle bundle = new Bundle();
				bundle.putInt("updatebyclearscreen", getFirstItemHeight());
				msg.setData(bundle);
				mHandler.sendMessage(msg);
				
				firstSearching = false;
			}
		})
		.setNegativeButton(dialogNo, null).show();
		
		mClearScreenDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mainActivity.setStatusBarBG(false);
				mainActivity.setClearScreenFlag(false);
				mainActivity.mCustomMenuProxyCallBack();
			}
		});
	}
	
	public void removeItemAt(int position) {
		mVoiceContentLayout.removeViewAt(position);
	}
	
	public void addItemToContent() {
		if (mBaseItemView == null) {
			return;
		}
		mVoiceContentLayout.addView(mBaseItemView);
		removeOutOfLengthItem();
		/*if (updateFillView != null) {
			mHandler.removeCallbacks(updateFillView);
		}
		mHandler.postDelayed(updateFillView, 100);*/
	}
	
	public int getFirstItemHeight(){
		return mVoiceContentLayout.getChildAt(0).getMeasuredHeight();
	}
	
	public int getLastItemHeight(){
		if(mBaseItemView!=null){
			return mBaseItemView.getMeasuredHeight();
		}
		return -1;
	}
	
	public int getLastInnerItemHeight() {
		int height = 0;
		if(mBaseItemView!=null){
			int childCount = mBaseItemView.getChildCount();
			LinearLayout resultLayout = (LinearLayout)mBaseItemView.getChildAt(childCount - 1);
			//for old model about searching and showing for result
			/*if (count == 4) {
				height = mBaseItemView.getChildAt(count - 1).getMeasuredHeight() + mBaseItemView.getChildAt(count - 2).getMeasuredHeight();
			} else {
				height = mBaseItemView.getChildAt(count - 1).getMeasuredHeight();
			}*/
			
			//for new model about searching and showing for result
			int resultChildCount = resultLayout.getChildCount();
			if (resultChildCount == 3) {
				int height1 = resultLayout.getChildAt(resultChildCount - 1).getMeasuredHeight();
				int height2 = resultLayout.getChildAt(resultChildCount - 2).getMeasuredHeight();
				height = height1 + height2;
				
			} else {	//count = 2
				View resultView = resultLayout.getChildAt(resultChildCount - 1);			//get the last inner item height
				height = resultView.getMeasuredHeight();
			}
			
			return height;
		}
		return -1;
	}
	
	public int getSecondItemFromTheLastHeight() {
		int childIndex = getChildCount() - 2;
		int height = 0;
		if (childIndex >= 0) {
			height = mVoiceContentLayout.getChildAt(childIndex).getHeight();
		} else {
			height = 0;
		} 
		return height;
	}

	public int getOffsetToScroll() {
		int childCount = getChildCount();
		int scrollHeight = 0;
		for (int i = 0; i < childCount - 1; i++) {
			scrollHeight += mVoiceContentLayout.getChildAt(i).getHeight();
		}
		return scrollHeight;
	}
	
	public void hidePreviousItems(HideState state) {
		int childCount = getChildCount();
		if (childCount <= 1) return;
		
		if (state == HideState.ExitVoice) {
			childCount = childCount - 1;
		}
		Log.d("DEBUG", "hidePreviousItems------state = "+state+" ---------------the child count  = "+childCount);
		for (int i = 1; i < childCount; i++) {
			LinearLayout baseItemLayout = (LinearLayout)mVoiceContentLayout.getChildAt(i);
			int childItemCount = baseItemLayout.getChildCount();
			
			LinearLayout resultLayout;
			resultLayout = (LinearLayout)baseItemLayout.getChildAt(childItemCount - 1);
			int resultChildCount = resultLayout.getChildCount();
			Log.d("DEBUG", "hidePreviousItems------resultChildCount = "+resultChildCount);
//			if (resultChildCount == 1) continue;
			if (resultChildCount <= 1) continue;
			
			LinearLayout resultView;
			final View answerView;
			if (resultChildCount == 3) {
				resultView = (LinearLayout)resultLayout.getChildAt(resultChildCount - 2);
			} else {
				resultView = (LinearLayout)resultLayout.getChildAt(resultChildCount - 1);
			}
			answerView = (View)resultLayout.getChildAt(0);
			
			final LinearLayout tabContentLayout = (LinearLayout) resultView.findViewById(R.id.tab_content);
			final RelativeLayout hiddenLayout = (RelativeLayout) resultView.findViewById(R.id.layout_list_item_hidden);
			if (hiddenLayout == null) continue;
			
			if (hiddenLayout.getVisibility() == View.GONE) {
				answerView.setVisibility(View.GONE);
				tabContentLayout.setVisibility(View.GONE);
				hiddenLayout.setVisibility(View.VISIBLE);
				hiddenLayout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						hiddenLayout.setVisibility(View.GONE);
						tabContentLayout.setVisibility(View.VISIBLE);
						answerView.setVisibility(View.VISIBLE);
						
						for (int j = 1; j < tabContentLayout.getChildCount(); j++) {
//							Log.d("DEBUG", "the answerView = "+tabContentLayout.getChildAt(j));
//							Log.d("DEBUG", "the answerView visibility = "+tabContentLayout.getChildAt(j).getVisibility());
							LinearLayout tabChildLayout = (LinearLayout)(tabContentLayout.getChildAt(j));
							View tabChildView = tabChildLayout.getChildAt(0);
//							Log.d("DEBUG", "the tabChildView = "+tabChildView);
							if (tabChildView instanceof WebView && tabContentLayout.getChildAt(j).getVisibility() == View.VISIBLE) {
								((WebView) tabChildView).setVisibility(View.VISIBLE);
							}
						}
					}
				});
			}
		}
	}
	
	public void showHidenWebViewInLastItem() {
		int childCount = getChildCount();
		if (childCount <= 1) return;
		
		LinearLayout baseItemLayout = (LinearLayout)mVoiceContentLayout.getChildAt(childCount - 1);
		int childItemCount = baseItemLayout.getChildCount();
		
		LinearLayout resultLayout;
		resultLayout = (LinearLayout)baseItemLayout.getChildAt(childItemCount - 1);
		int resultChildCount = resultLayout.getChildCount();
		Log.d("DEBUG", "showHidenWebViewInLastItem------resultChildCount = "+resultChildCount);
//		if (resultChildCount == 1) return;
		if (resultChildCount <= 1) return;
		
		LinearLayout resultView;
		final View answerView;
		if (resultChildCount == 3) {
			resultView = (LinearLayout)resultLayout.getChildAt(resultChildCount - 2);
		} else {
			resultView = (LinearLayout)resultLayout.getChildAt(resultChildCount - 1);
		}
		answerView = (View)resultLayout.getChildAt(0);
		
		LinearLayout tabContentLayout = (LinearLayout) resultView.findViewById(R.id.tab_content);
		if (tabContentLayout == null) return;
		
		for (int j = 1; j < tabContentLayout.getChildCount(); j++) {
			LinearLayout tabChildLayout = (LinearLayout)(tabContentLayout.getChildAt(j));
			View tabChildView = tabChildLayout.getChildAt(0);
			if (tabChildView instanceof WebView && tabContentLayout.getChildAt(j).getVisibility() == View.VISIBLE) {
				((WebView) tabChildView).setVisibility(View.VISIBLE);
				mainActivity.scrollToPreviousPosition();				
			}
		}
	}
	
	public LinearLayout getLastItem(){
		return mBaseItemView;
	}
	
	//miche-先构造View显示问题
	public void layoutToShowRequest(String request){
		mBaseItemView = new LinearLayout(context);
		mBaseItemView.setOrientation(LinearLayout.VERTICAL);
		
		//问：
		View meSaidLayout =  mLayoutInflater.inflate(R.layout.vs_list_item_quiz,null);
		TextView meSaidTextView = (TextView)meSaidLayout.findViewById(R.id.quiz_text);
		meSaidTextView.setText(request);
		
		mBaseItemView.removeAllViews();
		if(mBaseItemView != null){
			mBaseItemView.addView(meSaidLayout);
		}
		
		//添加监听
		meSaidTextView.setOnClickListener(mOnClickListener);
		meSaidTextView.setOnLongClickListener(mOnLongClickListener);
		
		//将View添加到页面
		addItemToContent();
	}
	
	//miche-封装结果
	public void construstResultView(Response response, View resultView){
		if (response == null) return;
		
		if(mBaseItemView != null){
			if(mBaseItemView.getChildCount() == 0 || mBaseItemView.getChildCount() > 3){
				//Offline start
				if (mainActivity.getOffLineType() != OFFLINE_TYPE.TIPS) {
					View meSaidLayout =  mLayoutInflater.inflate(R.layout.vs_list_item_quiz,null);
					TextView meSaidTextView = (TextView)meSaidLayout.findViewById(R.id.quiz_text);
					meSaidTextView.setText(response.getRequestString());
									
					mBaseItemView.removeAllViews();
					if(mBaseItemView != null){
						mBaseItemView.addView(meSaidLayout);
					}
					//添加监听
					meSaidTextView.setOnClickListener(mOnClickListener);
					meSaidTextView.setOnLongClickListener(mOnLongClickListener);
				}
				//Offline end
			}
			
			//添加结果,并显示
			LinearLayout resultLayout = new LinearLayout(context);
			resultLayout.setOrientation(LinearLayout.VERTICAL);
			
			if (response.getAnswerString() != null) {
				View herSaidLayout = mLayoutInflater.inflate(R.layout.vs_answer_text,null);
				TextView herSaidTextView = (TextView)herSaidLayout.findViewById(R.id.answer_text);
				herSaidTextView.setText(response.getAnswerString());
				resultLayout.addView(herSaidLayout);
			}
			
			if (resultView != null) {
				/*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);*/
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				float gap = context.getResources().getDimension(R.dimen.vs_resultview_gap);
				params.topMargin = (int)gap;
				resultView.setLayoutParams(params);
				resultLayout.addView(resultView);
				//提供商信息
				if (!firstSearching) {
					View view = addProviderInformation();
					resultLayout.addView(view);
					firstSearching = true;
				}
			}
			
			ObjectAnimator tran = ObjectAnimator.ofFloat(resultLayout, "translationY", 
					resultLayout.getTranslationY() - 20, resultLayout.getTranslationY());
			ObjectAnimator alpha = ObjectAnimator.ofFloat(resultLayout, "alpha", 0, 1f);
			
			AnimatorSet aSet = new AnimatorSet();
			aSet.setDuration(500);
			aSet.playTogether(tran,alpha);
			aSet.start();
			mBaseItemView.addView(resultLayout);
		}
	}
	
	public View contrustBaseItemView(String meSaidString, String herSaidString, View resultView) {
		mBaseItemView = new LinearLayout(context);
		mBaseItemView.setOrientation(LinearLayout.VERTICAL);
		
		View meSaidLayout =  mLayoutInflater.inflate(R.layout.vs_list_item_quiz,null);
		TextView meSaidTextView = (TextView)meSaidLayout.findViewById(R.id.quiz_text);
		meSaidTextView.setText(meSaidString);
	
		View herSaidLayout = mLayoutInflater.inflate(R.layout.vs_answer_text,null);
		/*TextView herSaidTextView = (TextView)herSaidLayout.findViewById(R.id.answer_text);
		herSaidTextView.setText(herSaidString);*/
		
		if (meSaidString == null && getChildCount() == 0) {
			herSaidLayout = getHelpLayoutView();
		}
		
		TextView herSaidTextView = (TextView)herSaidLayout.findViewById(R.id.answer_text);
		herSaidTextView.setText(herSaidString);
		
		meSaidTextView.setOnClickListener(mOnClickListener);
		meSaidTextView.setOnLongClickListener(mOnLongClickListener);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		float gap = context.getResources().getDimension(R.dimen.vs_resultview_gap);
		params.topMargin = (int)gap;
		
		mBaseItemView.removeAllViews();
		if (meSaidString != null) {
			mBaseItemView.addView(meSaidLayout);
		}
		if (herSaidString != null) {
			mBaseItemView.addView(herSaidLayout);
		}
		
		if (resultView != null) {
			LinearLayout resultLayout = new LinearLayout(context);
			resultView.setLayoutParams(params);
			resultLayout.addView(resultView);
			mBaseItemView.addView(resultLayout);
			if (!firstSearching) {
				addProviderInformation(params);
				firstSearching = true;
			}	
		} else {
			/*TextView textView = new TextView(context);
			textView.setText("add textview");
			textView.setTextSize(50);
			mBaseItemView.addView(textView);*/
		}
		return mBaseItemView;
	}
	
	public View addProviderInformation() {
		TextView mTextView = new TextView(context);
		String providerString = context.getResources().getString(R.string.vs_providerinfo);
		float stringSize = context.getResources().getDimension(R.dimen.vs_providerinfo_textsize);
		mTextView.setText(providerString);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, stringSize);
		mTextView.setTextColor(Color.parseColor("#80FFFFFF"));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = (int)context.getResources().getDimension(R.dimen.vs_providerinfo_margintop);
		params.rightMargin = (int)context.getResources().getDimension(R.dimen.vs_providerinfo_marginright);
		params.gravity = Gravity.RIGHT;
		mTextView.setLayoutParams(params);
		//mBaseItemView.addView(mTextView);
		return mTextView;
	}
	
	public void addProviderInformation(LinearLayout.LayoutParams params) {
		TextView mTextView = new TextView(context);
		String providerString = context.getResources().getString(R.string.vs_providerinfo);
		float stringSize = context.getResources().getDimension(R.dimen.vs_providerinfo_textsize);
		mTextView.setText(providerString);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, stringSize);
		mTextView.setTextColor(Color.parseColor("#80FFFFFF"));
		params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = (int)context.getResources().getDimension(R.dimen.vs_providerinfo_margintop);
		params.rightMargin = (int)context.getResources().getDimension(R.dimen.vs_providerinfo_marginright);
		params.gravity = Gravity.RIGHT;
		mTextView.setLayoutParams(params);
		mBaseItemView.addView(mTextView);
	}
	
	public View getHelpLayoutView() {
		View mHelpLayout = mLayoutInflater.inflate(R.layout.vs_answer_with_help,null);
				
		//The total view for help
		final ImageView mHelpImageView = (ImageView)mHelpLayout.findViewById(R.id.vs_helpimage);
		final LinearLayout mHelpContentLayout = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_content_layout);
		final LinearLayout mHelpContent = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_content);
		final TextView helpTips = (TextView)mHelpLayout.findViewById(R.id.vs_help_tips);
		
		//The offline item in help
		final View mHelpOfflineLayout = (RelativeLayout)mHelpLayout.findViewById(R.id.vs_help_sample_offline);
		final View mHelpOfflineText = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_offline_text_sample);
		final ImageView arrowOffline = (ImageView)mHelpLayout.findViewById(R.id.arrow_offline);
		final TextView offlineCall = (TextView)mHelpLayout.findViewById(R.id.vs_help_offline_call);
		final TextView offlineMessage = (TextView)mHelpLayout.findViewById(R.id.vs_help_offline_message);
		final TextView offlineOpen = (TextView)mHelpLayout.findViewById(R.id.vs_help_offline_open);
		
		//The nearby item in help
		final View mHelpNearbyLayout = (RelativeLayout)mHelpLayout.findViewById(R.id.vs_help_sample_nearby);
		final View mHelpNearbyText = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_nearby_text_sample);
		final ImageView arrowNearby = (ImageView)mHelpLayout.findViewById(R.id.arrow_nearby);
		final TextView nearbyFood = (TextView)mHelpLayout.findViewById(R.id.vs_help_nearby_food);
		final TextView nearbyBank = (TextView)mHelpLayout.findViewById(R.id.vs_help_nearby_bank);
		
		//The weather item in help
		final View mHelpWeatherLayout = (RelativeLayout)mHelpLayout.findViewById(R.id.vs_help_sample_weather);
		final View mHelpWeatherText = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_weather_text_sample);
		final ImageView arrowWeather = (ImageView)mHelpLayout.findViewById(R.id.arrow_weather);
		final TextView weatherToday = (TextView)mHelpLayout.findViewById(R.id.vs_help_weather_today);
		final TextView weatherBeijing = (TextView)mHelpLayout.findViewById(R.id.vs_help_weather_beijing);
		
		//The travel item in help
		final View mHelpTravelLayout = (RelativeLayout)mHelpLayout.findViewById(R.id.vs_help_sample_travel);
		final View mHelpTravelText = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_travel_text_sample);
		final ImageView arrowTravel = (ImageView)mHelpLayout.findViewById(R.id.arrow_travel);
		final TextView travelTrain = (TextView)mHelpLayout.findViewById(R.id.vs_help_travel_train);
		final TextView travelBus = (TextView)mHelpLayout.findViewById(R.id.vs_help_travel_bus);
		
		//The picture item in help
		final View mHelpPictureLayout = (RelativeLayout)mHelpLayout.findViewById(R.id.vs_help_sample_picture);
		final View mHelpPictureText = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_picture_text_sample);
		final ImageView arrowPicture = (ImageView)mHelpLayout.findViewById(R.id.arrow_picture);
		final TextView pictureBeauty = (TextView)mHelpLayout.findViewById(R.id.vs_help_picture_beauty);
		final TextView pictureVideo = (TextView)mHelpLayout.findViewById(R.id.vs_help_picture_video);
		
		final ViewGroup.LayoutParams params = mHelpContentLayout.getLayoutParams();
				
		mHelpImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//to hide the input method
				InputMethodManager imm = (InputMethodManager) context.getApplicationContext() .getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mHelpImageView.getWindowToken(), 0);
				
				if (mHelpContentLayout.getVisibility() == View.GONE) {
					mHelpContentLayout.setVisibility(View.VISIBLE);
					mHelpImageView.setBackgroundResource(R.drawable.vs_help_pressed);
					
					params.height = params.WRAP_CONTENT;
					mHelpContentLayout.setLayoutParams(params);
					showOrHideHelpContent(mHelpContentLayout, 0, helpTips, mHelpContent, true);
					
					//account start
					MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_SHOW, 1);
					//account end
					
				} else {
					mHelpImageView.setBackgroundResource(R.drawable.vs_help_normal);
					
					int helpLayoutHeight = mHelpContentLayout.getMeasuredHeight();
					showOrHideHelpContent(mHelpContentLayout, helpLayoutHeight, helpTips, mHelpContent, false);
					
					//to hide the sample text if help content has been hiden
					showOrHideSampleText(mHelpOfflineText, false, arrowOffline);
					showOrHideSampleText(mHelpNearbyText, false, arrowNearby);
					showOrHideSampleText(mHelpWeatherText, false, arrowWeather);
					showOrHideSampleText(mHelpTravelText, false, arrowTravel);
					showOrHideSampleText(mHelpPictureText, false, arrowPicture);
				}
				
				mHelpContentLayout.post(new Runnable() {
					@Override
					public void run() {
						/*Message msg = Message.obtain();
						msg.what = 2;						//MSG_TYPE_UPDATE_FILLVIEW = 2
						mHandler.sendMessage(msg);*/
						mainActivity.calculateFillViewHeight();
					}
				});
				
			}
		});
		
		//The offline item click event
		mHelpOfflineLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHelpOfflineText.getVisibility() == View.GONE) {
					showOrHideSampleText(mHelpOfflineText, true, arrowOffline);
					showOrHideSampleText(mHelpNearbyText, false, arrowNearby);
					showOrHideSampleText(mHelpWeatherText, false, arrowWeather);
					showOrHideSampleText(mHelpTravelText, false, arrowTravel);
					showOrHideSampleText(mHelpPictureText, false, arrowPicture);
				} else {
					showOrHideSampleText(mHelpOfflineText, false, arrowOffline);
				}
			}
		});
		offlineCall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = offlineCall.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		offlineMessage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = offlineMessage.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		offlineOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = offlineOpen.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
						
		//The nearby item click event
		mHelpNearbyLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHelpNearbyText.getVisibility() == View.GONE) {
					showOrHideSampleText(mHelpOfflineText, false, arrowOffline);
					showOrHideSampleText(mHelpNearbyText, true, arrowNearby);
					showOrHideSampleText(mHelpWeatherText, false, arrowWeather);
					showOrHideSampleText(mHelpTravelText, false, arrowTravel);
					showOrHideSampleText(mHelpPictureText, false, arrowPicture);
				} else {
					showOrHideSampleText(mHelpNearbyText, false, arrowNearby);
				}
			}
		});
		nearbyFood.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = nearbyFood.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		nearbyBank.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = nearbyBank.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		
		//The weather item click event
		mHelpWeatherLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHelpWeatherText.getVisibility() == View.GONE) {
					showOrHideSampleText(mHelpOfflineText, false, arrowOffline);
					showOrHideSampleText(mHelpWeatherText, true, arrowWeather);
					showOrHideSampleText(mHelpNearbyText, false, arrowNearby);
					showOrHideSampleText(mHelpTravelText, false, arrowTravel);
					showOrHideSampleText(mHelpPictureText, false, arrowPicture);
				} else {
					showOrHideSampleText(mHelpWeatherText, false, arrowWeather);
				}
			}
		});
		weatherToday.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = weatherToday.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		weatherBeijing.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = weatherBeijing.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		
		//The travel item click event
		mHelpTravelLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHelpTravelText.getVisibility() == View.GONE) {
					showOrHideSampleText(mHelpOfflineText, false, arrowOffline);
					showOrHideSampleText(mHelpTravelText, true, arrowTravel);
					showOrHideSampleText(mHelpNearbyText, false, arrowNearby);
					showOrHideSampleText(mHelpWeatherText, false, arrowWeather);
					showOrHideSampleText(mHelpPictureText, false, arrowPicture);
				} else {
					showOrHideSampleText(mHelpTravelText, false, arrowTravel);
				}
			}
		});
		travelTrain.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = travelTrain.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		travelBus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = travelBus.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		
		//The picture item click event
		mHelpPictureLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHelpPictureText.getVisibility() == View.GONE) {
					showOrHideSampleText(mHelpOfflineText, false, arrowOffline);
					showOrHideSampleText(mHelpPictureText, true, arrowPicture);
					showOrHideSampleText(mHelpNearbyText, false, arrowNearby);
					showOrHideSampleText(mHelpWeatherText, false, arrowWeather);
					showOrHideSampleText(mHelpTravelText, false, arrowTravel);
				} else {
					showOrHideSampleText(mHelpPictureText, false, arrowPicture);
				}
			}
		});
		pictureBeauty.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = pictureBeauty.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
		pictureVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mString = pictureVideo.getText().toString();
				mainActivity.startRequestByContent(mString);
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_HELP_EXAMPLE, 1);
				//account end
			}
		});
				
		return mHelpLayout;
	}
	
	public void showOrHideHelpContent(final View layout, final int dimen, final View helpTips, final View helpContent, final boolean flag) {
		final float transY = context.getResources().getDimension(R.dimen.vs_help_content_translation_y);
		final ViewGroup.LayoutParams params = layout.getLayoutParams();

		float startAlpha = flag? 0f:1f;
		float endAlpha = flag? 1f:0f;
		float startDimen = flag? 0f:dimen;
		float endDimen = flag? dimen:0f;
		int duration = 400;
		int delay = 50;
		
		ValueAnimator helpTipsAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha);
		helpTipsAnimator.setDuration(duration);
		helpTipsAnimator.setStartDelay(delay);
		helpTipsAnimator.setInterpolator(new OvershootInterpolator(2));
		helpTipsAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				helpTips.setAlpha(value);
				helpTips.setTranslationY((value - 1)*transY);
			}
		});
		
		ValueAnimator helpContentAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha);
		helpContentAnimator.setDuration(duration);
		helpContentAnimator.setInterpolator(new OvershootInterpolator(2));
		helpContentAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				helpContent.setAlpha(value);
				helpContent.setTranslationY((value - 1)*transY);
			}
		});
		
		ValueAnimator helpLayoutAnimator = null;
		if (!flag) {
			helpLayoutAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha);
			helpLayoutAnimator.setDuration(duration);
			helpLayoutAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					float value = (Float)arg0.getAnimatedValue();
						params.height = (int)(value*dimen);
						layout.setLayoutParams(params);
				}
			});
		}
		
		AnimatorSet mHelpAnimatorSet = new AnimatorSet();
		if (helpLayoutAnimator != null) {
			mHelpAnimatorSet.playTogether(helpTipsAnimator, helpContentAnimator, helpLayoutAnimator);
		} else {
			mHelpAnimatorSet.playTogether(helpTipsAnimator, helpContentAnimator);
		}
		
		mHelpAnimatorSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
//				Log.d("DEBUG", "start Animator show+++++++++++++++++++++++++++++start");
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
//				Log.d("DEBUG", "start Animator show+++++++++++++++++++++++++++++end");
				if (flag) {
					layout.setVisibility(View.VISIBLE);
				} else {
					layout.setVisibility(View.GONE);
				}
				
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mHelpAnimatorSet.start();

	}
	
	public void showOrHideSampleText(final View view, final boolean flag, ImageView imageView) {
		if (view.getVisibility() == View.GONE && !flag) {
			return;
		}
		if (view != null) {
			final ViewGroup.LayoutParams params = view.getLayoutParams();
			float dimen = 0;
			if (view.getId() == R.id.vs_help_offline_text_sample) {
				dimen = context.getResources().getDimension(R.dimen.vs_help_sample_text_offline_layout_height);
			} else {
				dimen = context.getResources().getDimension(R.dimen.vs_help_sample_text_layout_height);
			}
			
			float startAlpha = flag? 0f:1f;
			float endAlpha = flag? 1f:0f;
			float startDimen = flag? 0:dimen;
			float endDimen = flag? dimen:0;
			int duration = flag? 500:150;
			
			if (flag) {
				view.setVisibility(View.VISIBLE);
				imageView.setBackgroundResource(R.drawable.vs_help_arrows_pressed);
			} else {
				imageView.setBackgroundResource(R.drawable.vs_help_arrows_normal);
			}

			ValueAnimator mValueAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha);
			mValueAnimator.setDuration(duration);
			mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					float value = (Float)arg0.getAnimatedValue();
					view.setAlpha(value);
				}
			});
			
			ValueAnimator mValueAnimator1 = ValueAnimator.ofFloat(startDimen, endDimen);
			mValueAnimator1.setDuration(150);
			mValueAnimator1.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					float value = (Float)arg0.getAnimatedValue();
					params.height = (int)(value);
					view.setLayoutParams(params);
				}
			});
			
			AnimatorSet mAnimatorSet = new AnimatorSet();
			mAnimatorSet.playTogether(mValueAnimator, mValueAnimator1);
			
			mAnimatorSet.addListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO Auto-generated method stub
//					Log.d("DEBUG", "start Animator show+++++++++++++++++++++++++++++start");
				}
				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onAnimationEnd(Animator arg0) {
					// TODO Auto-generated method stub
//					Log.d("DEBUG", "start Animator show+++++++++++++++++++++++++++++end");
					if (flag) {
						view.setVisibility(View.VISIBLE);
					} else {
						view.setVisibility(View.GONE);
					}
					
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							/*Message msg = Message.obtain();
							msg.what = 2;						//MSG_TYPE_UPDATE_FILLVIEW = 2
							mHandler.sendMessage(msg);*/
							mainActivity.calculateFillViewHeight();
						}
					});
					
				}
				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			mAnimatorSet.start();
		}
	}
	
	public void setViewGone(View view, ImageView imageView) {
		if (view != null) {
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
				imageView.setBackgroundResource(R.drawable.vs_help_arrows_normal);
			}
		}
	}
	
	public void updateLayoutManager(Response response, ContentViewConstructor mViewConstructor) {

		String requestString = response.getRequestString();
		String answerString = response.getAnswerString();
		
		View view = null;
		if (requestString != null) {
			Log.d("DEBUG", "response.getFirstNodeType() = "+response.getFirstNodeType());
			switch(response.getFirstNodeType()) {
				case CFG.VIEW_TYPE_TEXT:
	//					convertView = answerAdapter(response,convertView);
					break;
				case CFG.VIEW_TYPE_JOSN:
					break;
				case CFG.VIEW_TYPE_XML:
					
					break;
				case CFG.VIEW_TYPE_XML_PIC:
					view = mViewConstructor.getPictureResultView(response);
					break;
				case CFG.VIEW_TYPE_XML_TIANQI:
					view = mViewConstructor.getWeatherResultView(response);
					break;
				case CFG.VIEW_TYPE_XML_NB:
					view = mViewConstructor.getNearbyResultView(response);
					break;
				case CFG.VIEW_TYPE_XML_CX_LC:
					view = mViewConstructor.getTrainResultView(response);
					break;
				case CFG.VIEW_TYPE_XML_CX_MSG_LC:
					view = mViewConstructor.getTrainMessageResultView(response);
					break;
				case CFG.VIEW_TYPE_XML_CX_KC:
					view = mViewConstructor.getBusResultView(response);
					break;
				case CFG.VIEW_TYPE_XML_CX_HB:
					view = mViewConstructor.getFlightResultView(response);
					break;
				case CFG.VIEW_TYPE_XML_BAIKE:
					view = mViewConstructor.getBaikeResultView(response);
					break;
				case CFG.VIEW_TYPE_WEBVIEW:
					view = mViewConstructor.getWebviewResultView(response);
					break;
				case CFG.VIEW_TYPE_SOGOUMAP_URL:
//					view = mViewConstructor.getWebviewResultView(response);
					view = mViewConstructor.getSoGouMapUrlResultView(response);
					Log.d("DEBUG", "the response for CFG.VIEW_TYPE_SOGOUMAP_URL+++++++++++++++++++++ ");
					break;
			}
		}
		contrustBaseItemView(requestString, answerString, view);
		addItemToContent();
	}

	//继续添加需要的“回答+结果”
	public void layoutToShowResult(Response response, ContentViewConstructor mViewConstructor) {

		String requestString = response.getRequestString();
		//String answerString = response.getAnswerString();

		View view = null;
		
		//Offline start
		OFFLINE_TYPE type = mainActivity.getOffLineType();
		if (type != OFFLINE_TYPE.NONE) {
			Log.d("DEBUG", "layoutToShowResult------------------------------type = "+type);
			
			if (type == OFFLINE_TYPE.TIPS) {
				//do nothing
			} else if (type == OFFLINE_TYPE.APPLICATION) {
				view = mViewConstructor.getApplicationResultView(null);
			} else {
				view = mViewConstructor.getCallingOrMessageOrContactsLayout(type);
			}

		} else {
		//Offline end
			if (requestString != null) {
				Log.d("DEBUG", "layoutToShowResult---------------response.getFirstNodeType() = "+response.getFirstNodeType());
				switch(response.getFirstNodeType()) {
					case CFG.VIEW_TYPE_TEXT:
							//convertView = answerAdapter(response,convertView);
						break;
					case CFG.VIEW_TYPE_JOSN:
//						view = mViewConstructor.getReminderAlarmView(response);
						view = mViewConstructor.getJSONDataResultView(response);
						break;
					case CFG.VIEW_TYPE_XML:
						
						break;
					case CFG.VIEW_TYPE_XML_PIC:
						view = mViewConstructor.getPictureResultView(response);
						break;
					case CFG.VIEW_TYPE_XML_TIANQI:
						view = mViewConstructor.getWeatherResultView(response);
						break;
					case CFG.VIEW_TYPE_XML_NB:
						view = mViewConstructor.getNearbyResultView(response);
						break;
					case CFG.VIEW_TYPE_XML_CX_LC:
						view = mViewConstructor.getTrainResultView(response);
						break;
					case CFG.VIEW_TYPE_XML_CX_MSG_LC:
						view = mViewConstructor.getTrainMessageResultView(response);
						break;
					case CFG.VIEW_TYPE_XML_CX_KC:
						view = mViewConstructor.getBusResultView(response);
						break;
					case CFG.VIEW_TYPE_XML_CX_HB:
						view = mViewConstructor.getFlightResultView(response);
						break;
					case CFG.VIEW_TYPE_XML_BAIKE:
						view = mViewConstructor.getBaikeResultView(response);
						break;
					case CFG.VIEW_TYPE_WEBVIEW:
						view = mViewConstructor.getWebviewResultView(response);
						break;
					case CFG.VIEW_TYPE_SOGOUMAP_URL:
						//view = mViewConstructor.getWebviewResultView(response);
						view = mViewConstructor.getSoGouMapUrlResultView(response);
						break;
				}
			}
		}
		construstResultView(response, view); //mBaseItemView
	}
	
	
	OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//To be defined
			//Log.d("DEBUG", "onClick Event-----to do something v.getTag = ");
			//Log.d("DEBUG", "get the text = "+((TextView)v).getText().toString());
		}
	};
	
	OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			mainActivity.setStatusBarBG(true);
			View menuView = getMenuForLongPress(v);
			mPopupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setFocusable(true);
			mPopupWindow.showAsDropDown(v);
			
			return true;
		}
	};
	
	public View getMenuForLongPress(final View targetView) {
		View menuView = mLayoutInflater.inflate(R.layout.vs_longpress_menu, null);
		
		final TextView copyTextView = (TextView)menuView.findViewById(R.id.vs_menu_copy);
		final TextView deleteTextView = (TextView)menuView.findViewById(R.id.vs_menu_delete);
		final TextView searchTextView = (TextView)menuView.findViewById(R.id.vs_menu_search);
		
		final String str = ((TextView)targetView).getText().toString();
		
		//copy click event
		copyTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindowMenu();
				
				ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
				cmb.setText(str);
			}
		});
		
		//delete click event
		deleteTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindowMenu();
				mainActivity.setStatusBarBG(true);
					
				String dialogTitlesingle = context.getResources().getString(R.string.vs_dialogtitle_single);
				String dialogMessagesingle = context.getResources().getString(R.string.vs_dialogmessage_single);
				String dialogYes = context.getResources().getString(R.string.vs_dialog_yes);
				String dialogNo = context.getResources().getString(R.string.vs_dialog_no);
					
				mDeleteDialog = new AlertDialog.Builder(context)
				.setTitle(dialogTitlesingle)
				.setMessage(dialogMessagesingle)
				.setPositiveButton(dialogYes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						LinearLayout layout = (LinearLayout)targetView.getParent().getParent();
//						layout.removeView(targetView);
//						layout.removeAllViews();
						mVoiceContentLayout.removeView(layout);
					}
				})
				.setNegativeButton(dialogNo, null).show();
			}
		});
		
		//search click event
		searchTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindowMenu();
				mainActivity.startRequestByContent(str);
			}
		});
		
		return menuView;
	}
	
	Runnable updateFillView = new Runnable() {
		@Override
		public void run() {
			Message msg = Message.obtain();
			msg.what = 2;							//MSG_TYPE_UPDATE_FILLVIEW = 2
			mHandler.sendMessage(msg);
		}
	};
	
	public void dismissClearScreenDialog() {
		if (mClearScreenDialog != null && mClearScreenDialog.isShowing()) {
			mClearScreenDialog.dismiss();
		}
	}

	public void dismissDeleteDialog() {
		if (mDeleteDialog != null && mDeleteDialog.isShowing()) {
			mDeleteDialog.dismiss();
		}
	}
	
	public void dismissPopupWindowMenu() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}
	}
	
	public Animator getHelpContentAnimator() {
		View mHelpLayout = mLayoutInflater.inflate(R.layout.vs_answer_with_help,null);
		LinearLayout mHelpContent = (LinearLayout)mHelpLayout.findViewById(R.id.vs_help_content);
		mHelpContent.setAlpha(0f);
		
		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mHelpContent, "alpha", 0f, 1f);
		alphaAnimator.setDuration(5000);
		ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(mHelpContent, "scaleY", 0.1f, 1f);
		translationAnimator.setDuration(3000);
		
		AnimatorSet mHelpAnimatorSet = new AnimatorSet();
		mHelpAnimatorSet.playTogether(alphaAnimator, translationAnimator);
		
		return mHelpAnimatorSet;
	}
	
	public void destroyAllWebView() {
		int childCount = getChildCount();
		if (childCount <= 1) return;
		
		for (int i = 1; i < childCount; i++) {
			LinearLayout baseItemLayout = (LinearLayout)mVoiceContentLayout.getChildAt(i);
			int childItemCount = baseItemLayout.getChildCount();
			
			LinearLayout resultLayout;
			resultLayout = (LinearLayout)baseItemLayout.getChildAt(childItemCount - 1);
			int resultChildCount = resultLayout.getChildCount();
			Log.d("DEBUG", "resultChildCount= "+resultChildCount);
//			if (resultChildCount == 1) continue;
			if (resultChildCount <= 1) continue;
			
			LinearLayout resultView;
			final View answerView;
			if (resultChildCount == 3) {
				resultView = (LinearLayout)resultLayout.getChildAt(resultChildCount - 2);
			} else {
				resultView = (LinearLayout)resultLayout.getChildAt(resultChildCount - 1);
			}
			answerView = (View)resultLayout.getChildAt(0);
			
			LinearLayout tabContentLayout = (LinearLayout) resultView.findViewById(R.id.tab_content);
			if (tabContentLayout == null) continue;
			
			int tabChildCount = tabContentLayout.getChildCount();
			Log.d("DEBUG", "the tab child count = "+tabChildCount);
			
			boolean isAllWeb = true;
			for (int j = 1; j < tabChildCount; j++) {
				LinearLayout childTabLayout = (LinearLayout)tabContentLayout.getChildAt(j);
				View childTabView = childTabLayout.getChildAt(0);
				
				if (childTabView == null) {
					continue;
				} else if (childTabView instanceof WebView | childTabView instanceof ImageView) {
					//do nothing
				} else {
					isAllWeb = false;
				}
				
			}
			
			for (int j = 1; j < tabChildCount; j++) {
				LinearLayout childTabLayout = (LinearLayout)tabContentLayout.getChildAt(j);
//				Log.d("DEBUG", "the tab child = "+childTabLayout);
				View childTabView = childTabLayout.getChildAt(0);
//				Log.d("DEBUG", "the tab content = "+childTabView);
				
				if (childTabView == null) continue;
				
				int tabImgLayoutId = 0;
				int radioBtnId = 0;
				int tabTextId;
				
				ContentViewConstructor mViewConstructor = mainActivity.getContentViewConstructor();
//				Log.d("DEBUG", "the mViewConstructor = "+mViewConstructor);
				if (mViewConstructor == null) continue;
				
				HashMap<Integer, Integer> tabLayoutMap = mViewConstructor.getTabLayoutIdMap();
//				Log.d("DEBUG", "the tabLayoutMap = "+tabLayoutMap);
				if (tabLayoutMap == null) continue;
				
				tabImgLayoutId = tabLayoutMap.get((j-1)*5 + 2);
				radioBtnId = tabLayoutMap.get((j-1)*5 + 3);
				tabTextId = tabLayoutMap.get((j-1)*5 + 4);
//				Log.d("DEBUG", "the tabTextId = "+tabTextId);
				
				LinearLayout tabImgLayout = (LinearLayout)tabContentLayout.findViewById(tabImgLayoutId);
				ImageView tabImgView = (ImageView)tabContentLayout.findViewById(radioBtnId);
				TextView tabText = (TextView)tabContentLayout.findViewById(tabTextId);
//				Log.d("DEBUG", "the tabTextTag getid = "+tabText.getId()+" and the tabText = "+tabText.getText());
				
				if (i == childCount - 1) {
					if (childTabLayout.getVisibility() == View.GONE) {
						if (childTabView instanceof WebView) {
							tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball);
							setTabImageView(tabText, tabImgView, true);
							
							childTabLayout.removeAllViews();
//							childTabLayout.setVisibility(View.GONE);
						}
					} else {
						if (childTabView instanceof WebView) {
							mainActivity.setScrollViewYOffset();
							((WebView) childTabView).setVisibility(View.GONE);
						}
					}
					
				} else {
					if (isAllWeb) {
						if (childTabLayout.getVisibility() == View.GONE) {
							if (childTabView instanceof WebView) {
								tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball);
								setTabImageView(tabText, tabImgView, true);
								
								childTabLayout.removeAllViews();
							}
						} else {
							if (childTabView instanceof WebView) {
								((WebView) childTabView).setVisibility(View.GONE);
							}
						}
						
					} else {
						if (childTabView instanceof WebView | childTabView instanceof ImageView) {
							tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball);
							setTabImageView(tabText, tabImgView, true);
							
							childTabLayout.removeAllViews();
							childTabLayout.setVisibility(View.GONE);
						} else {
							tabImgLayout.setBackgroundResource(R.drawable.vs_search_ball_b);
							setTabImageView(tabText, tabImgView, false);
							
							childTabLayout.setVisibility(View.VISIBLE);
						}
					}
				}
				
			}
			
		}
		
	}
	
	public void setTabImageView(TextView tabText, ImageView tabImgView, boolean flag) {
		if (tabText.getText().equals(context.getString(R.string.vs_tianqi))) {
			//天气
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_weather : R.drawable.vs_icon_weather_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_zhoubian))) {
			//周边
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_nearby : R.drawable.vs_icon_nearby_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_tupian))) {
			//图片
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_image : R.drawable.vs_icon_image_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_lc))) {
			//列车
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_longdistance_car : R.drawable.vs_icon_longdistance_car_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_lece))) {
			//列车
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_train : R.drawable.vs_icon_train_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_kece))) {
			//客车
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_longdistance_car : R.drawable.vs_icon_longdistance_car_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_hangban))) {
			//航班
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_plane : R.drawable.vs_icon_plane_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_baike))) {
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_encyclopedia : R.drawable.vs_icon_encyclopedia_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_wendai))) {
			//问答
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_wenda : R.drawable.vs_icon_wenda_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_sousuo))) {
			//搜索-保底
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_search : R.drawable.vs_icon_search_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_tieba))) {
			//贴吧
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_tieba : R.drawable.vs_icon_tieba_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_shipin))) {
			//视频
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_video : R.drawable.vs_icon_video_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_wenku))) {
			//文库
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_wenku : R.drawable.vs_icon_wenku_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_ditu))) {
			//地图
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_map : R.drawable.vs_icon_map_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_travel))) {
			//旅游
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_travel : R.drawable.vs_icon_travel_sl);
			
		} else if (tabText.getText().equals(context.getString(R.string.vs_news))) {
			//新闻
			tabImgView.setBackgroundResource(flag? R.drawable.vs_icon_news : R.drawable.vs_icon_news_sl);
			
		}
	}
	
}

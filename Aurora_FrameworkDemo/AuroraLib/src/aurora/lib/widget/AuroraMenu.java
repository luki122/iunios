package aurora.lib.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import aurora.lib.app.AuroraDialog;

import com.aurora.lib.R;

/**
 * @author leftaven
 * @2013年9月12日 aurora menu
 */
public class AuroraMenu extends AuroraMenuBase {
	private ViewGroup customView;
	private ImageView imageView1;
	private ImageView imageView2;
	private ImageView imageView3;
	private ImageView imageView4;
	private ImageView imageView5;
	private LinearLayout layout1;
	private LinearLayout layout2;
	private LinearLayout layout3;
	private TextView textView1;
	private TextView textView2;
	private TextView textView3;
	private ImageView resultImageView;
	private TextView resultTextView;
	private int position;
	
	/*first menu id for aurora menu*/
	public static final int FIRST = 1;

	public AuroraMenu(Context context,
			AuroraActionBar auroraActionBar,
			final OnAuroraMenuItemClickListener auroraMenuCallBack,
			AuroraMenuAdapterBase auroraMenuAdapter, int aniTabMenu, int resId) {
		this(auroraActionBar, context, resId, aniTabMenu);
		menuAdapter = auroraMenuAdapter;
		this.auroraMenuCallBack = auroraMenuCallBack;
		this.auroraActionBar = auroraActionBar;
		setClickListener(menuAdapter.getCount());// 设置回调监听
	}

	// Aurora <aven> <2013年9月16日> modify for actionbar menu
	public AuroraMenu(final AuroraActionBar auroraActionBar,
			Context context, int resId, int animMenu) {
		super(context);
		initCustomView(context, resId, animMenu);
	}

	/**
	 * 初始化自定义的menu视图
	 * 
	 * @param context
	 * @param resId
	 * @param animMenu
	 */
	private void initCustomView(Context context, int resId, int animMenu) {
		mContext = context;
		customView = (ViewGroup) LayoutInflater.from(context).inflate(resId,
				null);
		defaultMenu(customView, animMenu);
		// 设置menu外部可点击
		customView.setFocusable(true);
		customView.setFocusableInTouchMode(true);
		// 设置点击外部menu不消失
		setFocusable(false);
		setOutsideTouchable(false);
		customView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// 单击menu菜单时拦截事件
				if (keyCode == KeyEvent.KEYCODE_MENU && isShowing()) {
					dismissActionBarMenu();
				} else if (keyCode == KeyEvent.KEYCODE_BACK && isShowing()) {
					dismissActionBarMenu();
					// dismiss();
				}
				return false;
			}
		});
	}

	/**
	 * 底部菜单回调监听
	 */
	private View.OnClickListener onlClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			position = 0;
			int id = v.getId();
			
			if(id == R.id.aurora_action_bottom_bar_menu_item_layout1
					||id == R.id.aurora_action_bottom_bar_menu_item_text1
					||id == R.id.aurora_action_bottom_bar_menu_item_image1){
				position = 0;
			}else if(id ==  R.id.aurora_action_bottom_bar_menu_item_layout2
				||id ==  R.id.aurora_action_bottom_bar_menu_item_text2
				||id ==  R.id.aurora_action_bottom_bar_menu_item_image2){
				position = 1;
			}else if(id ==  R.id.aurora_action_bottom_bar_menu_item_layout3
				||id ==   R.id.aurora_action_bottom_bar_menu_item_text3
					||id ==   R.id.aurora_action_bottom_bar_menu_item_image3){
				position = 2;
			}else if(id == R.id.aurora_action_bottom_bar_menu_item_image4){
				position = 3;
			}else if(id == R.id.aurora_action_bottom_bar_menu_item_image5){
				position = 4;
			}
			if (auroraMenuCallBack != null) {
				auroraMenuItem = (AuroraMenuItem) menuAdapter.getItem(position);
				auroraMenuCallBack.auroraMenuItemClick(auroraMenuItem.getId());
				AuroraDialog.showFromPopupWindow();
	            //Log.e("menu", "clicked");
			}
		}
	};

	/**
	 * @param count
	 *            菜单项数
	 */
	public void setClickListener(int count) {
		menuItems = menuAdapter.getMenuItems();
		initViewByCount(count);
		setViewClickListener(count);
	}

	/**
	 * 点击菜单，图标变化
	 * 
	 * @param count
	 */
	private void setImageChangeListener(int count) {
       
		switch (count) {
		case 1:
			layout1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){ 
					    if(TextUtils.isEmpty(textView1.getText())){
					        imageView1.onTouchEvent(event);
					    }
						textView1.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
					    if(TextUtils.isEmpty(textView1.getText())){
					        imageView1.setPressed(false);
					    }
						//imageView1.setPressed(false);
						textView1.setPressed(false);
		            }
					return false;
				}
			});
//			textView1.setOnTouchListener(new View.OnTouchListener() {
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					if(event.getAction()==MotionEvent.ACTION_DOWN){  
//						imageView1.onTouchEvent(event);
//		            }
//					if(event.getAction()==MotionEvent.ACTION_UP){  
//						imageView1.setPressed(false);
//		            }
//					return false;
//				}
//			});
//			imageView1.setOnTouchListener(new View.OnTouchListener() {
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					if(event.getAction()==MotionEvent.ACTION_DOWN){  
//						textView1.onTouchEvent(event);
//		            }
//					if(event.getAction()==MotionEvent.ACTION_UP){  
//						textView1.setPressed(false);
//		            }
//					return false;
//				}
//			});

			break;
		case 2:
			layout1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView1.onTouchEvent(event);
						textView1.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView1.setPressed(false);
						textView1.setPressed(false);
		            }
					return false;
				}
			});
			textView1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView1.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView1.setPressed(false);
		            } 
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, textView1)){
							imageView1.setPressed(false);
						}
					}
					return false;
				}
			});
			
			imageView1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						textView1.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						textView1.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, imageView1)){
							textView1.setPressed(false);
						}
					}
					return false;
				}
			});

			layout2.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView2.onTouchEvent(event);
						textView2.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView2.setPressed(false);
						textView2.setPressed(false);
		            }
					return false;
				}
			});
			textView2.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView2.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView2.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, textView2)){
							imageView2.setPressed(false);
						}
					}
					return false;
				}
			});
			
			imageView2.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						textView2.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						textView2.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, imageView2)){
							textView2.setPressed(false);
						}
					}
					return false;
				}
			});

			break;

		case 3:
			layout1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView1.onTouchEvent(event);
						textView1.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView1.setPressed(false);
						textView1.setPressed(false);
		            }
					return false;
				}
			});
			textView1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView1.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView1.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, textView1)){
							imageView1.setPressed(false);
						}
					}
					return false;
				}
			});
			
			imageView1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						textView1.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						textView1.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, imageView1)){
							textView1.setPressed(false);
						}
					}
					return false;
				}
			});

			layout2.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView2.onTouchEvent(event);
						textView2.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView2.setPressed(false);
						textView2.setPressed(false);
		            }
					return false;
				}
			});
			textView2.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView2.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView2.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, textView2)){
							imageView2.setPressed(false);
						}
					}
					return false;
				}
			});

			imageView2.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						textView2.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						textView2.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, imageView2)){
							textView2.setPressed(false);
						}
					}
					return false;
				}
			});
			
			layout3.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView3.onTouchEvent(event);
						textView3.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView3.setPressed(false);
						textView3.setPressed(false);
		            }
					return false;
				}
			});
			textView3.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						imageView3.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						imageView3.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, textView3)){
							imageView3.setPressed(false);
						}
					}
					return false;
				}
			});
			
			imageView3.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_DOWN){  
						textView3.onTouchEvent(event);
		            }
					if(event.getAction()==MotionEvent.ACTION_UP){  
						textView3.setPressed(false);
		            }
					if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(auroraMenuIsOutOfBounds(mContext, event, imageView3)){
							textView3.setPressed(false);
						}
					}
					return false;
				}
			});

			break;
		}

	}

	/**
	 * 设置各个项的监听
	 * 
	 * @param count
	 */
	private void setViewClickListener(int count) {
		try {
			for (int i = 0; i < count; i++) {
				resultImageView = getImageViewByPosition(i);
				if (getActionBottomBarMenuImage(i) != 0) {
					resultImageView
							.setImageResource(getActionBottomBarMenuImage(i));
				}
				resultImageView.setOnClickListener(onlClickListener);
				if (count < 4) {
					resultTextView = getTitleViewByPosition(i);
					//Log.e("222222", "getActionBottomBarMenuImage = " + getActionBottomBarMenuImage(i));
					//Log.e("222222", "getActionBottomBarMenuTitle = " + getActionBottomBarMenuTitle(i));
					if ( getActionBottomBarMenuImage(i) == 0 ) {
						LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);
						lp.leftMargin = 0;
						resultTextView.setLayoutParams(lp);
					}
					if("".equals(getActionBottomBarMenuTitle(i))){
						resultTextView.setVisibility(View.GONE);
					}else{
						resultTextView.setText(getActionBottomBarMenuTitle(i));
						resultTextView.setOnClickListener(onlClickListener);
						if ( resultImageView instanceof AuroraAnimationImageView ) {
							((AuroraAnimationImageView)resultImageView).playAnim(false);
						}
					}
					if (i < 3) {
						getLayoutByPosition(i).setOnClickListener(
								onlClickListener);// 设置layout监听
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化各个视图
	 * 
	 * @param count
	 */
	private void initViewByCount(int count) {
		switch (count) {
		case 1:
			imageView1 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image1);
			textView1 = (TextView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_text1);
			layout1 = (LinearLayout) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_layout1);
			if(!TextUtils.isEmpty(textView1.getText()) && imageView1 instanceof AuroraAnimationImageView){
			    ((AuroraAnimationImageView)imageView1).playAnim(false);
			}
			break;
		case 2:
			imageView1 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image1);
			imageView2 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image2);

			textView1 = (TextView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_text1);
			textView2 = (TextView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_text2);
			layout1 = (LinearLayout) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_layout1);
			layout2 = (LinearLayout) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_layout2);
			break;
		case 3:
			imageView1 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image1);
			imageView2 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image2);
			imageView3 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image3);

			textView1 = (TextView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_text1);
			textView2 = (TextView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_text2);
			textView3 = (TextView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_text3);

			layout1 = (LinearLayout) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_layout1);
			layout2 = (LinearLayout) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_layout2);
			layout3 = (LinearLayout) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_layout3);
			break;
		case 4:
			imageView1 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image1);
			imageView2 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image2);
			imageView3 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image3);
			imageView4 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image4);
			break;
		case 5:
			imageView1 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image1);
			imageView2 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image2);
			imageView3 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image3);
			imageView4 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image4);
			imageView5 = (ImageView) customView
					.findViewById(R.id.aurora_action_bottom_bar_menu_item_image5);
			break;
		}
		if (count < 4)
			setImageChangeListener(count);
	}

	/**
	 * @param i
	 * @return bottom menu title
	 */
	private String getActionBottomBarMenuTitle(int position) {
		int title = menuItems.get(position).getTitle();
		if (title == 0)
			return "";
		return mContext.getResources().getString(title);
	}

	private int getActionBottomBarMenuImage(int position) {
		return menuItems.get(position).getIcon();
	}

	/**
	 * 增加菜单项
	 * 
	 * @param itemId
	 */
	public void addMenuItemById(int itemId) {
		findViewByItemId(itemId).setVisibility(View.VISIBLE);
	}

	/**
	 * 删除菜单项
	 * 
	 * @param itemId
	 */
	public void removeMenuItemById(int itemId) {
		findViewByItemId(itemId).setVisibility(View.GONE);
	}

	/**
	 * @param itemId
	 * @return 通过itemid 找到点击的view
	 */
	private View findViewByItemId(int itemId) {
		View resultView = null;
		for (int position = 0; position < menuItems.size(); position++) {
			auroraMenuItem = (AuroraMenuItem) menuAdapter.getItem(position);
			if (auroraMenuItem.getId() == itemId) {
				if (menuItems.size() < 4) {
					resultView = getLayoutByPosition(position);
				} else {
					resultView = getImageViewByPosition(position);
				}
				break;
			}
		}
		return resultView;
	}

	/**
	 * @param position
	 * @return 得到imageview
	 */
	private ImageView getImageViewByPosition(int position) {
		ImageView resultView = null;
		switch (position) {
		case 0:
			resultView = imageView1;
			break;
		case 1:
			resultView = imageView2;
			break;
		case 2:
			resultView = imageView3;
			break;
		case 3:
			resultView = imageView4;
			break;
		case 4:
			resultView = imageView5;
			break;
		}
		return resultView;
	}

	public LinearLayout getLayoutByPosition(int position) {
		LinearLayout linearLayout = null;
		switch (position) {
		case 0:
			linearLayout = layout1;
			break;
		case 1:
			linearLayout = layout2;
			break;
		case 2:
			linearLayout = layout3;
			break;
		}
		return linearLayout;
	}

	public TextView getTitleViewByPosition(int position) {
		TextView resultView = null;
		switch (position) {
		case 0:
			resultView = textView1;
			break;
		case 1:
			resultView = textView2;
			break;
		case 2:
			resultView = textView3;
			break;
		}
		return resultView;
	}

	/**
	 * 设置底部button是否可用
	 * 
	 * @param position
	 * @param isEnable
	 */
	public void setBottomMenuItemEnable(int position, boolean isEnable) {
		if ( position > 0 ) {
			if (menuItems.size() < 4 && position < 4) {
				getLayoutByPosition(position-1).setEnabled(isEnable);
				getImageViewByPosition(position-1).setEnabled(isEnable);
				getTitleViewByPosition(position-1).setEnabled(isEnable);
			} else {
				getImageViewByPosition(position-1).setEnabled(isEnable);
			}
		}
	}

	public ViewGroup getActionMenuView() {
		return customView;
	}

	/**
	 * @return 得到批量操作左边视图
	 */
	public View getActionMenuLeftView() {
		return customView.findViewById(R.id.aurora_action_bar_btn_left);
	}

	/**
	 * @return 得到批量操作右边视图
	 */
	public View getActionMenuRightView() {
		return customView.findViewById(R.id.aurora_action_bar_btn_right);
	}

	/**
	 * @param index
	 * @return 获取底部bar的view
	 */
	public View getAuroraBottomBarViewAt(int index) {
		View bottomView = null;
		switch (index) {
		case 1:
			bottomView = imageView1;
			break;
		case 2:
			bottomView = imageView2;
			break;
		case 3:
			bottomView = imageView3;
			break;
		case 4:
			bottomView = imageView4;
			break;
		case 5:
			bottomView = imageView5;
			break;
		}
		return bottomView;
	}

	private boolean auroraMenuIsOutOfBounds(Context context, MotionEvent event,View target) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
		return (x < -slop) || (y < -slop)
				|| (x > (target.getWidth()+slop))
				|| (y > (target.getHeight()+slop));
	}
}

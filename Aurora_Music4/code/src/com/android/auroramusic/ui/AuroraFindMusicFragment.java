package com.android.auroramusic.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.json.JSONObject;

import android.R.integer;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchViewButtonClickListener;
import aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.widget.AuroraListView;

import com.android.auroramusic.adapter.AuroraSearchAdapter;
import com.android.auroramusic.adapter.AuroraSearchAlbumAdapter;
import com.android.auroramusic.adapter.AuroraSearchArtistAdapter;
import com.android.auroramusic.adapter.AuroraSearchHistoryAdapter;
import com.android.auroramusic.adapter.AuroraSearchPagerAdapter;
import com.android.auroramusic.adapter.AuroraSearchSongAdapter;
import com.android.auroramusic.adapter.ImagePagerAdapter;
import com.android.auroramusic.adapter.ImagePagerAdapter.OnBannerClickListener;
import com.android.auroramusic.adapter.OnlineMusicMainAdapter;
import com.android.auroramusic.adapter.OnlineMusicMainAdapter.OnGridViewClickListener;
import com.android.auroramusic.model.AuroraRankItem;
import com.android.auroramusic.model.SearchItem;
import com.android.auroramusic.model.XiaMiSdkUtils;
import com.android.auroramusic.online.AuroraNetSearchActivity;
import com.android.auroramusic.online.AuroraNetTrackActivity;
import com.android.auroramusic.online.AuroraNetTrackDetail;
import com.android.auroramusic.online.AuroraNetTrackDetailActivity;
import com.android.auroramusic.online.AuroraRadioListActivity;
import com.android.auroramusic.online.AuroraRankList;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.IntentFactory;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ReportUtils;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.auroramusic.widget.AuroraDotView;
import com.android.auroramusic.widget.AuroraSearchView;
import com.android.auroramusic.widget.AuroraViewPager;
import com.android.auroramusic.widget.BannerViewPager;
import com.android.music.Application;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.xiami.music.model.RadioCategory;
import com.xiami.sdk.callback.OnlineAlbumsCallback;
import com.xiami.sdk.callback.OnlineArtistsCallback;
import com.xiami.sdk.callback.OnlineCollectsCallback;
import com.xiami.sdk.callback.OnlineSongsCallback;
import com.xiami.sdk.callback.RadioCategoriesCallback;
import com.xiami.sdk.callback.SearchAlbumsCallback;
import com.xiami.sdk.callback.SearchArtistsCallback;
import com.xiami.sdk.callback.SearchSongsCallback;
import com.xiami.sdk.entities.Banner;
import com.xiami.sdk.entities.OnlineAlbum;
import com.xiami.sdk.entities.OnlineArtist;
import com.xiami.sdk.entities.OnlineCollect;
import com.xiami.sdk.entities.OnlineSong;
import com.xiami.sdk.entities.QueryInfo;
import com.xiami.sdk.entities.RankType;
import com.xiami.sdk.entities.SearchSummaryResult;
import com.xiami.sdk.utils.ImageUtil;

/**
 * create time: 20140428
 * @author chenhl
 */

public class AuroraFindMusicFragment implements OnClickListener {

	private static final String TAG = "AuroraFindMusicFragment";
	private ListView mainListView;
	private AuroraListView searchListView;
	private View searchlayout_parent;
	private AuroraActivity mContext;
	private AuroraDotView mAuroraDotView;
	private View headView;
	private float yDown;
	private boolean isShowSearch = false;
	private int searchDefaultPadding = -129;
	private BannerViewPager bannerPage;
	private boolean isShowView = false;
	private OnlineMusicMainAdapter mOnlineMusicMainAdapter;
	private int countSize = 0;
	private boolean isFirstClick = true;
	private ProgressBar mProgressBar;
	private LayoutParams mLayoutParams;
	private AuroraSearchView searchView;
	private View noNetWork;
	private ImageView networkIcon;
	private TextView networkText;
	private Button networkButton;
	private int networkType = 0;
	private int mPageNo = 1;
	private static final int PAGE_SIZE = 15;
	private ArrayList<SearchItem> seachArrayList = new ArrayList<SearchItem>();
	private AuroraSearchAdapter searchAdapter = null;
	private Button btn_search;
	private String keyWord;
	private AuroraViewPager mAuroraViewPager;
	private Drawable oldDrawable;
	private LinearLayout.LayoutParams oldParams;
	private boolean isPlaying = false; // 动画是否在运行
	private Animation operatingAnim; // 播放按钮动画
	private AuroraClickListener clickListener;
	private AuroraSearchClick searchClick;
	private AuroraSearchHistoryAdapter mAuroraSearchHistoryAdapter, mAuroraSearchSustionAdapter;
	private FrameLayout searchviewLayout;

	public FrameLayout getSearchviewLayout() {
		return searchviewLayout;
	}

	private AuroraListView historyList/* , sustionList */;
	private List<String> historyKeywords = new ArrayList<String>();
	private View searchhistory;
	private boolean isClickHistory = false;
	private boolean isEntrySearch = false;
	private boolean isExitSearch = false;

	// ----------------add by tangjie 2014/12/26 start--------------//
	private ViewPager mViewPager;
	private AuroraSearchPagerAdapter mSearchPagerAdapter;
	private int mArtistPageNo = 1;
	private int mAlbumPageNo = 1;
	private ListView mSongListView, mAlbumListView, mArtistListView;
	private ArrayList<OnlineSong> mSongList;
	private ArrayList<OnlineAlbum> mAlbumList;
	private ArrayList<OnlineArtist> mArtistList;
	private List mTempList, mSongTempList, mArtistTempList, mAlbumTemptList;
	private AuroraSearchSongAdapter mAuroraSearchSongAdapter;
	private AuroraSearchArtistAdapter mAuroraSearchArtistAdapter;
	private AuroraSearchAlbumAdapter mAuroraSearchAlbumAdapter;
	private int mSongCount, mAlbumCount, mArtistCount;
	private View loadingView;
	private ImagePagerAdapter mPagerAdapter;
	private RadioGroup mRadioGroup;
	private final int MSG_SEARCH_FINISHED = 1;
	private final int MSG_LOAD_MORE_SONG = 2;
	private final int MSG_LOAD_MORE_ALBUM = 3;
	private final int MSG_LOAD_MORE_ARTIST = 4;
	private long mTimeStamp;
	private View mSearchResultView;
	private TextView mTipText;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingView != null) {
				loadingView.setVisibility(View.GONE);
			}
			switch (msg.what) {
			case MSG_SEARCH_FINISHED:
				refreshUI();
				break;
			case MSG_LOAD_MORE_SONG:
				if (mAuroraSearchSongAdapter != null) {
					mSongList.addAll(mTempList);
					mAuroraSearchSongAdapter.notifyDataSetChanged();
				}
				break;
			case MSG_LOAD_MORE_ALBUM:
				if (mAuroraSearchAlbumAdapter != null) {
					mAlbumList.addAll(mTempList);
					mAuroraSearchAlbumAdapter.notifyDataSetChanged();
				}
				break;
			case MSG_LOAD_MORE_ARTIST:
				if (mAuroraSearchArtistAdapter != null) {
					mArtistList.addAll(mTempList);
					mAuroraSearchArtistAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		};
	};

	// ----------------add by tangjie 2014/12/26 end--------------//
	@SuppressWarnings("deprecation")
	public void initview(View view, AuroraActivity context, AuroraViewPager pager) {
		this.mContext = context;
		mAuroraViewPager = pager;
		mainListView = (ListView) view.findViewById(R.id.aurora_online_music_main);

		searchlayout_parent = view.findViewById(R.id.aurora_searchlayout_parent);
		// searchView = view.findViewById(R.id.aurora_search);
		mProgressBar = (ProgressBar) view.findViewById(R.id.aurora_progress);
		noNetWork = view.findViewById(R.id.aurora_no_network);
		networkIcon = (ImageView) view.findViewById(R.id.id_no_network_icon);
		networkText = (TextView) view.findViewById(R.id.id_no_network_text);
		networkButton = (Button) view.findViewById(R.id.id_no_network_button);
		networkButton.setOnClickListener(this);
		headView = LayoutInflater.from(context).inflate(R.layout.aurora_online_music_head, null);
		searchDefaultPadding = context.getResources().getDimensionPixelSize(R.dimen.aurora_online_search_height);
		mContext.addSearchviewInwindowLayout();
		initeHead(headView);
		mainListView.addHeaderView(headView);
		mOnlineMusicMainAdapter = new OnlineMusicMainAdapter(context);
		mOnlineMusicMainAdapter.setOnGridViewClickListener(mOnGridViewClickListener);
		mainListView.setAdapter(mOnlineMusicMainAdapter);
		mainListView.setSelector(android.R.color.transparent);
		// mainListView.setOnTouchListener(mOntouchListener);
		mainListView.setOnScrollListener(mOnScrollListener);
		mainListView.setVisibility(View.GONE);
		mContext.getSearchView().setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (!hasFocus) {
					LinearLayout.LayoutParams layoutParams = new LayoutParams((int) mContext.getResources().getDimension(R.dimen.aurora_play_width), (int) mContext.getResources().getDimension(
							R.dimen.aurora_play_width));
					layoutParams.setMargins(0, 0, (int) mContext.getResources().getDimension(R.dimen.aurora_album_info_padding), 0);
					changeButton(0);
				} else {
					changeButton(1);
				}
			}
		});
		mContext.setOnSearchViewQuitListener(new OnSearchViewQuitListener() {

			@Override
			public boolean quit() {
				// TODO Auto-generated method stub
				mainListView.setVisibility(View.VISIBLE);
				searchlayout_parent.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
				mContext.setMenuEnable(true);
				mAuroraViewPager.setViewPageOnScrolled(false);
				isPlaying = false;
				changeButton(1);
				isEntrySearch = false;
				mViewPager.setCurrentItem(0);
				return false;
			}
		});
		// add by tangjie start 2014/07/17
		btn_search = mContext.getSearchViewRightButton();
		btn_search.setText(mContext.getResources().getString(R.string.songlist_cancel));
		oldDrawable = btn_search.getBackground();
		oldParams = (LayoutParams) btn_search.getLayoutParams();

		searchClick = new AuroraSearchClick();
		mContext.setOnSearchViewButtonListener(searchClick);
		operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate_anim);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		clickListener = new AuroraClickListener();
		// add by tangjie end

		// ----------------add by tangjie 2014/12/26 start--------------//
		mViewPager = (ViewPager) mSearchResultView.findViewById(R.id.aurora_id_viewpager);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				switch (arg0) {
				case 0:
					((RadioButton) mRadioGroup.findViewById(R.id.aurora_rb_song)).setChecked(true);
					if (mSongList != null && mSongList.size() == 0) {
						mTipText.setVisibility(View.VISIBLE);
					} else {
						mTipText.setVisibility(View.GONE);
					}
					break;
				case 1:
					((RadioButton) mRadioGroup.findViewById(R.id.aurora_rb_artist)).setChecked(true);
					if (mArtistList != null && mArtistList.size() == 0) {
						mTipText.setVisibility(View.VISIBLE);
					} else {
						mTipText.setVisibility(View.GONE);
					}
					break;
				case 2:
					((RadioButton) mRadioGroup.findViewById(R.id.aurora_rb_album)).setChecked(true);
					if (mAlbumList != null && mAlbumList.size() == 0) {
						mTipText.setVisibility(View.VISIBLE);
					} else {
						mTipText.setVisibility(View.GONE);
					}
					break;
				default:

					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		loadingView = mSearchResultView.findViewById(R.id.aurora_loading_parent);
		mRadioGroup = (RadioGroup) mSearchResultView.findViewById(R.id.aurora_rb_category);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup radiogroup, int i) {
				// TODO Auto-generated method stub
				switch (i) {
				case R.id.aurora_rb_song:
					mViewPager.setCurrentItem(0);
					break;
				case R.id.aurora_rb_artist:
					mViewPager.setCurrentItem(1);
					break;
				case R.id.aurora_rb_album:
					mViewPager.setCurrentItem(2);
					break;
				default:
					break;
				}
			}
		});
		// ----------------add by tangjie 2014/12/26 end--------------//
	}

	private void initeHead(View headView) {
		bannerPage = (BannerViewPager) headView.findViewById(R.id.aurora_id_banner);
		mAuroraDotView = (AuroraDotView) headView.findViewById(R.id.aurora_dot_layout);
		searchView = (AuroraSearchView) headView.findViewById(R.id.aurora_search);
		mLayoutParams = (LayoutParams) searchView.getLayoutParams();
		// mLayoutParams.topMargin = searchDefaultPadding;
		// searchView.setParams(mLayoutParams);
		searchviewLayout = (FrameLayout) mContext.getSearchViewGreyBackground();
		mAuroraSearchHistoryAdapter = new AuroraSearchHistoryAdapter(mContext, true);
		mAuroraSearchSustionAdapter = new AuroraSearchHistoryAdapter(mContext, false);
		if (searchviewLayout != null) {
			searchhistory = LayoutInflater.from(mContext).inflate(R.layout.aurora_searchview_history, null);
			historyList = (AuroraListView) searchhistory.findViewById(R.id.aurora_search_history);
			/*
			 * sustionList = (AuroraListView) searchhistory
			 * .findViewById(R.id.aurora_sustion);
			 */

			View bottomview = LayoutInflater.from(mContext).inflate(R.layout.aurora_search_history_foot, null);
			bottomview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					historyKeywords.clear();
					MusicUtils.mSongDb.clearSearchHistory();
					mAuroraSearchHistoryAdapter.notifyDataSetChanged();
					searchhistory.setVisibility(View.GONE);
				}
			});
			historyList.addFooterView(bottomview);
			// sustionList.setAdapter(mAuroraSearchSustionAdapter);
			historyList.setAdapter(mAuroraSearchHistoryAdapter);
			historyList.setOnItemClickListener(mOnItemClickListener);
			// sustionList.setOnItemClickListener(mOnItemClickListener);
			historyList.setOnTouchListener(mOnHistoryTouchListner);
			// sustionList.setOnTouchListener(mOnHistoryTouchListner);

			mSearchResultView = LayoutInflater.from(mContext).inflate(R.layout.aurora_netsearch_layout, null);
			mTipText = (TextView) mSearchResultView.findViewById(R.id.aurora_no_tips);
			searchviewLayout.addView(mSearchResultView);
			searchviewLayout.addView(searchhistory);
		}
		searchView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LogUtil.d(TAG, "onclicked!!");
				mContext.setMenuEnable(false);
				if (historyKeywords.size() == 0) {
					searchhistory.setVisibility(View.GONE);
				}
				showSearchState(true);
				mContext.showSearchviewLayout();
				mAuroraViewPager.setViewPageOnScrolled(true);
				isEntrySearch = true;
				isExitSearch = false;
				ReportUtils.getInstance(mContext.getApplicationContext()).reportMessage(ReportUtils.TAG_OL_SEARCH);
			}
		});
		mContext.setOnQueryTextListener(new SongSearchViewQueryTextChangeListener());
	}

	private void changeNetworkUi(int type) {
		networkType = type;
		if (type == 0) {
			networkIcon.setImageResource(R.drawable.aurora_no_network);
			networkText.setText(R.string.aurora_no_network);
			networkButton.setText(R.string.aurora_setting_network);
		} else {
			networkIcon.setImageResource(R.drawable.aurora_network_error);
			networkText.setText(R.string.aurora_network_error);
			networkButton.setText(R.string.aurora_retry);
		}
	}

	private OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		}

		@Override
		public void onScrollStateChanged(AbsListView arg0, int arg1) {

			// LogUtil.d(TAG, "scrollstate:" + arg1);
			if (arg1 == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				bannerPage.stopAutoScroll();
			} else if (arg1 == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
				bannerPage.startAutoScroll();
			}
		}

	};

	public void isLoadData() {
		
		if (mainListView.getVisibility() == View.GONE && isFirstClick) {
			LogUtil.d(TAG, "isLoadData............");
			if (AuroraMusicUtil.isNetWorkActive(mContext)) {
				isFirstClick = false;
				noNetWork.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.VISIBLE);
				mainListView.setVisibility(View.GONE);
				loadXiamiOnlineMusic();// add for xiamimusic
			} else {
				isFirstClick = true;
				changeNetworkUi(0);
				noNetWork.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mainListView.setVisibility(View.GONE);
			}

		}
	}

	private int parseJson(String json) {
		if (json == null) {
			return 0;
		}
		int code = 0;
		try {
			JSONObject obj = new JSONObject(json);
			code = obj.getInt("error_code");
			LogUtil.d(TAG, "code:" + code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return code;
	}

	private OnBannerClickListener mOnBannerClickListener = new OnBannerClickListener() {

		@Override
		public void onBannerClick(Banner item) {

			String source = item.getSourceId();
			LogUtil.d(TAG, "source:" + source);
			String[] split = source.split(":");
			if (split.length < 2) {
				return;
			}
			// ---add by tangjie 2014/05 start---//
			if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
				Toast.makeText(mContext, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
				return;
			}
			// ---add by tangjie 2014/05 end---//
			LogUtil.d(TAG, "id:" + split[1] + " type:" + split[0]);
			// banner 点击响应
			Intent intent = new Intent(mContext, AuroraNetTrackDetailActivity.class);
			intent.putExtra("tag", OnGridViewClickListener.BANNER);
			intent.putExtra("type", split[0]);
			intent.putExtra(AuroraNetTrackDetail.ID, split[1]);
			// intent.putExtra("title", item.mDescription);
			intent.putExtra("imageUrl", ImageUtil.transferImgUrl(item.getImageUrl(), 330));
			mContext.startActivity(intent);
		}

	};

	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			mAuroraDotView.setSelectDot(arg0 % countSize);
		}

	};

	private OnGridViewClickListener mOnGridViewClickListener = new OnGridViewClickListener() {

		// 点击更多处理
		@Override
		public void onMoreButtonClick(int type) {
			// ---add by tangjie 2014/05 start---//
			if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
				Toast.makeText(mContext, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
				return;
			}
			// ---add by tangjie 2014/05 end---//
			switch (type) {
			case OnGridViewClickListener.NEW_ALBUM:
				/*
				 * // add by ukiliu begin Intent mIntent = IntentFactory
				 * .newAlbumListOnlineIntent(mContext);
				 * mContext.startActivity(mIntent); // add by ukiliu end
				 */

				// ----modify by tangjie 2014/12/18 start----//
				Intent mIntent = new Intent(mContext, AuroraNetTrackActivity.class);
				mIntent.putExtra("title", mContext.getString(R.string.aurora_online_new_album));
				mIntent.putExtra("type", 1);
				// ----modify by tangjie 2014/12/18 end----//
				mContext.startActivity(mIntent);
				LogUtil.d(TAG, "NEW_ALBUM");
				break;
			case OnGridViewClickListener.RECOMMEND_PLAYLIST:
				mIntent = new Intent(mContext, AuroraNetTrackActivity.class);
				mContext.startActivity(mIntent);
				LogUtil.d(TAG, "RECOMMEND_PLAYLIST");
				break;
			case OnGridViewClickListener.RANKING:
				LogUtil.d(TAG, "RANKING");
				Intent intent = new Intent(mContext, AuroraRankList.class);
				mContext.startActivity(intent);
				break;
			}
		}

		// 点击每个item响应
		@Override
		public void onGridItemClick(int type, int postion, Object obj) {
			// ---add by tangjie 2014/05 start---//
			if (!AuroraMusicUtil.isNetWorkActive(mContext)) {
				Toast.makeText(mContext, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
				return;
			}
			// ---add by tangjie 2014/05 end---//
			switch (type) {
			case OnGridViewClickListener.NEW_ALBUM:
				OnlineAlbum info = (OnlineAlbum) obj;
				LogUtil.d(TAG, "NEW_ALBUM:" + postion + info.toString());
				// 新碟上架处理
				// -----modify by tangjie 2014/12/18 start-----//
				Intent intent = new Intent(mContext, AuroraNetTrackDetailActivity.class);
				intent.putExtra("tag", OnGridViewClickListener.NEW_ALBUM);
				intent.putExtra("title", info.getAlbumName());
				intent.putExtra(AuroraNetTrackDetail.ID, String.valueOf(info.getAlbumId()));
				intent.putExtra("imageUrl", ImageUtil.transferImgUrl(info.getImageUrl(), 330));
				intent.putExtra("artist", info.getArtistName());
				mContext.startActivity(intent);
				// -----modify by tangjie 2014/12/18 end-----//
				break;
			case OnGridViewClickListener.RECOMMEND_PLAYLIST:
				OnlineCollect playlist = (OnlineCollect) obj;
				LogUtil.d(TAG, "RECOMMEND_PLAYLIST:" + postion + playlist.toString());
				// 推荐歌单处理
				// -----modify by tangjie 2014/12/18 start-----//
				Intent intent2 = new Intent(mContext, AuroraNetTrackDetailActivity.class);
				intent2.putExtra("tag", OnGridViewClickListener.RECOMMEND_PLAYLIST);
				intent2.putExtra(AuroraNetTrackDetail.ID, String.valueOf(playlist.getListId()));
				intent2.putExtra("imageUrl", ImageUtil.transferImgUrl(playlist.getImageUrl(), 330));
				intent2.putExtra("playlist_tag", playlist.getDescription());
				mContext.startActivity(intent2);
				// -----modify by tangjie 2014/12/18 end-----//
				// 推荐歌单处理

				break;
			case 3:
				AuroraRankItem rankitem = (AuroraRankItem) obj;
				LogUtil.d(TAG, "RANKING:" + postion);
				// 排行榜处理
				// -----modify by tangjie 2014/12/18 start-----//
				Intent intent3 = new Intent(mContext, AuroraNetTrackDetailActivity.class);
				intent3.putExtra("tag", OnGridViewClickListener.RANKING);
				intent3.putExtra(AuroraNetTrackDetail.ID, String.valueOf(rankitem.getRanktype().ordinal()));
				intent3.putExtra("title", rankitem.getRankname());
				mContext.startActivity(intent3);
				// -----modify by tangjie 2014/12/18 end-----//
				break;
			case 2:
				RadioCategory radio = (RadioCategory) obj;
				Intent intent4 = new Intent(mContext, AuroraRadioListActivity.class);
				intent4.putExtra("type", postion);
				mContext.startActivity(intent4);
				break;
			}

		}

	};

	private int distance = 0;
	private OnTouchListener mOntouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			float curY = arg1.getRawY();
			switch (arg1.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// yDown=arg1.getRawY();
				LogUtil.d(TAG, "ACTION_DOWN...........yDown:" + yDown);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				yDown = 0;
				if (distance > 0) {
					ObjectAnimator obj = ObjectAnimator.ofInt(searchView, "topMargin", 0);
					obj.start();
					isShowSearch = true;
				} else {
					isShowSearch = false;
					ObjectAnimator obj = ObjectAnimator.ofInt(searchView, "topMargin", searchDefaultPadding);
					obj.start();
				}
				break;
			case MotionEvent.ACTION_MOVE:

				if (isScrollTop()) {
					if (yDown == 0) {
						yDown = arg1.getRawY();
					}
					int dis = (int) (curY - yDown);

					if (dis != 0) {
						distance = dis;
					}
					mLayoutParams.topMargin += dis;
					if (mLayoutParams.topMargin > 0) {
						mLayoutParams.topMargin = 0;
						isShowSearch = true;
					} else if (mLayoutParams.topMargin < searchDefaultPadding) {
						mLayoutParams.topMargin = searchDefaultPadding;
						isShowSearch = false;
					}
					searchView.setLayoutParams(mLayoutParams);
					if (mLayoutParams.topMargin > searchDefaultPadding) {
						mainListView.setSelection(0);
					}
					yDown = arg1.getRawY();
				}

				break;
			}

			return false;
		}
	};

	private boolean isScrollTop() {
		LogUtil.d(TAG, "headView.top:" + headView.getTop() + " maintop:" + mainListView.getTop());
		if (headView.getTop() == 0 || isShowSearch) {
			mainListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
			return true;
		}
		mainListView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		return false;
	}

	public void onResume() {
		if (isShowView)
			bannerPage.startAutoScroll();
		historyList.auroraOnResume();
		// sustionList.auroraOnResume();
		setPlayAnimation();
		if (!isEntrySearch) {
			mContext.getSearchView().getQueryTextView().clearFocus();
		}

	}

	public void onPause() {
		if (isShowView)
			bannerPage.stopAutoScroll();
		historyList.auroraOnPause();
		// sustionList.auroraOnPause();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.id_no_network_button:
			try {
				if (networkType == 0) {
					Intent intent = new Intent();
					intent.setClassName("com.android.settings", "com.android.settings.Settings");
					mContext.startActivity(intent);
				} else {
					isLoadData();
				}
			} catch (Exception e) {
				LogUtil.d(TAG, "start error!!");
			}
			break;
		default:
			break;
		}
	}

	// 搜索输入框数据变化接口 add by tangjie 2014/7/16 start
	class SongSearchViewQueryTextChangeListener implements OnSearchViewQueryTextChangeListener {
		public boolean onQueryTextSubmit(String query) {
			if (!TextUtils.isEmpty(query)) {
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mainListView.getWindowToken(), 0);
				showSearchView(query, mTimeStamp);
			}
			return false;
		}

		public boolean onQueryTextChange(String newText) {
			keyWord = newText;
			mTimeStamp = System.currentTimeMillis();
			if (TextUtils.isEmpty(newText.trim())) {

				if (searchlayout_parent.getVisibility() == View.VISIBLE) {
					mainListView.setVisibility(View.VISIBLE);
				}
				InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				if (mContext.getSearchView().getQueryTextView() != null)
					mContext.getSearchView().getQueryTextView().requestFocus();
				searchlayout_parent.setVisibility(View.GONE);
				if (!isExitSearch) {
					showSearchState(true);
				} else {
					isExitSearch = false;
					// searchviewLayout.setVisibility(View.GONE);
				}
				changeButton(1);
			} else {
				showSearchView(newText, mTimeStamp);
				if (searchlayout_parent.getVisibility() == View.GONE) {
					searchviewLayout.setVisibility(View.VISIBLE);
				}
				btn_search.setText(mContext.getResources().getString(R.string.songlist_cancel));
			}
			return false;
		}
	}

	public void hideSearch() {
		isEntrySearch = false;
		mContext.setMenuEnable(true);
		mContext.hideSearchviewLayout();
		mProgressBar.setVisibility(View.GONE);
		mAuroraViewPager.setViewPageOnScrolled(false);
	}

	public void changeButton(int type) {
		if (type == 0) {
			LinearLayout.LayoutParams layoutParams = new LayoutParams((int) mContext.getResources().getDimension(R.dimen.aurora_play_width), (int) mContext.getResources().getDimension(
					R.dimen.aurora_play_width));
			layoutParams.setMargins(0, 0, (int) mContext.getResources().getDimension(R.dimen.aurora_album_info_padding), 0);
			btn_search.setLayoutParams(layoutParams);
			btn_search.setBackgroundResource(R.drawable.song_playing);
			btn_search.setText("");
			setPlayAnimation();
			mContext.setOnSearchViewButtonListener(clickListener);
		} else {
			btn_search.setLayoutParams(oldParams);
			btn_search.setBackground(oldDrawable);
			// modify by tangjie 2015/01/12
			// if (keyWord != null && !TextUtils.isEmpty(keyWord)){
			// btn_search.setText(mContext.getResources().getString(
			// R.string.aurora_search));
			// }
			// else
			// btn_search.setText(mContext.getResources().getString(
			// R.string.songlist_cancel));
			btn_search.setText(mContext.getResources().getString(R.string.songlist_cancel));
			btn_search.clearAnimation();
			mContext.setOnSearchViewButtonListener(searchClick);
		}
	}

	class AuroraClickListener implements OnSearchViewButtonClickListener {

		@Override
		public boolean onSearchViewButtonClick() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext, AuroraPlayerActivity.class);
			mContext.startActivity(intent);
			mContext.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			return false;
		}

	}

	class AuroraSearchClick implements OnSearchViewButtonClickListener {

		@Override
		public boolean onSearchViewButtonClick() {
			// TODO Auto-generated method stub
			// if (keyWord != null && !TextUtils.isEmpty(keyWord.trim())) {
			// InputMethodManager imm = (InputMethodManager) mContext
			// .getSystemService(Context.INPUT_METHOD_SERVICE);
			// imm.hideSoftInputFromWindow(mainListView.getWindowToken(), 0);
			// showSearchView(keyWord, mTimeStamp);
			// btn_search.requestFocus();
			// } else {
			// hideSearch();
			// }
			hideSearch();
			return false;
		}

	}

	/**
	 * 设置播放动画
	 */
	public void setPlayAnimation() {
		if (!TextUtils.isEmpty(btn_search.getText())) {
			btn_search.clearAnimation();
			mContext.setOnSearchViewButtonListener(searchClick);
			return;
		}
		try {
			if (MusicUtils.sService != null) {
				if (MusicUtils.sService.isPlaying()) {
					btn_search.startAnimation(operatingAnim);
					if (!isPlaying) {
						isPlaying = true;
					}
				} else {
					btn_search.clearAnimation();
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			if (isPlaying) {
				btn_search.clearAnimation();
				isPlaying = false;
			}
		}
	}

	public void destroy() {
		if (searchAdapter != null) {
			searchAdapter.clearCache();
		}
		if (mOnlineMusicMainAdapter != null) {
			mOnlineMusicMainAdapter.clearViews();
		}
		if (mainListView != null) {
			mainListView.setAdapter(null);
			mOnlineMusicMainAdapter = null;
		}
		if (bannerPage != null) {
			bannerPage.setAdapter(null);
			mPagerAdapter = null;
		}
		// -------------add by tangjie start---------------//
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		if (mAuroraSearchSongAdapter != null) {
			mAuroraSearchSongAdapter.clearCache();
		}
		// -------------add by tangjie end----------------//

	}

	// add by tangjie end

	private void showSearchState(boolean is) {
		historyList.setVisibility(is ? View.VISIBLE : View.GONE);
		mSearchResultView.setVisibility(is ? View.GONE : View.VISIBLE);
		if (historyKeywords.size() == 0 && is) {
			searchhistory.setVisibility(View.GONE);
		} else {
			searchhistory.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 搜索联想和搜索历史点击响应
	 */
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			String keyText = "";
			if (arg0.getId() == R.id.aurora_search_history) {
				keyText = (String) arg0.getAdapter().getItem(historyKeywords.size() - arg2 - 1);
			} else {
				keyText = (String) arg0.getAdapter().getItem(arg2);
			}

			LogUtil.d(TAG, "keyText:" + keyText + " arg1:" + arg0);
			isClickHistory = true;
			mContext.getSearchView().setQuery(keyText, false);
			showSearchView(keyText, mTimeStamp);
		}
	};

	private OnTouchListener mOnHistoryTouchListner = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {

			switch (arg1.getAction()) {
			case MotionEvent.ACTION_DOWN:
				AuroraMusicUtil.hideInputMethod(mContext, arg0);
				break;
			}
			return false;
		}
	};

	public void hideSearchviewLayout() {
		isExitSearch = true;
	}

	// add by chenhl start for xiamimusic
	private List<OnlineAlbum> mOnlineAlbums = null;
	private List<OnlineCollect> mOnlineCollects = null;
	private List<Banner> mBanners = null;
	private List<AuroraRankItem> ranklist;
	private List<RadioCategory> mRadioCategories = null;

	private ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();

	private void loadXiamiOnlineMusic() {
		mOnlineAlbums = null;
		mOnlineCollects = null;
		mBanners = null;
		mRadioCategories = null;
		// get hot week album
		executor.submit(new Runnable() {

			@Override
			public void run() {
				Pair<QueryInfo, List<OnlineAlbum>> pair = XiaMiSdkUtils.getWeekHotAlbumsSync(mContext, 6, 1);
				if (pair != null && pair.second != null) {
					mOnlineAlbums = pair.second;
					if (mOnlineCollects != null && mOnlineAlbums != null && mBanners != null && mRadioCategories != null) {
						LogUtil.d(TAG, "-------getWeekHotAlbumsSync  Success");
						showRequestSuccess();
					}
				} else {
					LogUtil.d(TAG, "-------getWeekHotAlbumsSync  Fail");
					showRequestFail();
				}
			}
		});

		// get Collects

		executor.submit(new Runnable() {

			@Override
			public void run() {
				Pair<QueryInfo, List<OnlineCollect>> pair = XiaMiSdkUtils.getCollectsRecommendSync(mContext, 4, 1);
				if (pair != null && pair.second != null) {
					mOnlineCollects = pair.second;
					if (mOnlineCollects != null && mOnlineAlbums != null && mBanners != null && mRadioCategories != null) {
						LogUtil.d(TAG, "-------getCollectsRecommendSync Success");
						showRequestSuccess();
					}
				} else {
					LogUtil.d(TAG, "-------getCollectsRecommendSync Fail");
					showRequestFail();
				}
			}
		});

		// get banner

		executor.submit(new Runnable() {

			@Override
			public void run() {
				historyKeywords = MusicUtils.mSongDb.querySearchHistory();
				mBanners = XiaMiSdkUtils.fetchBannerSync(mContext);
				LogUtil.d(TAG, "-------fetchBannerSync");
				if (mBanners != null) {
					if (mOnlineCollects != null && mOnlineAlbums != null && mBanners != null && mRadioCategories != null) {
						showRequestSuccess();
						LogUtil.d(TAG, "-------fetchBannerSync Success");
					}
				} else {
					showRequestFail();
					LogUtil.d(TAG, "-------fetchBannerSync Fail");
				}
			}
		});

		// get radio
		executor.submit(new Runnable() {

			@Override
			public void run() {
				mRadioCategories = XiaMiSdkUtils.fetchRadioListsSync(mContext);
				Application.mRadioCategories = mRadioCategories;
				if (mRadioCategories != null) {
					if (mOnlineCollects != null && mOnlineAlbums != null && mBanners != null) {
						showRequestSuccess();
						LogUtil.d(TAG, "-------fetchRadioListsSync Success");
					}
				} else {
					showRequestFail();
					LogUtil.d(TAG, "-------fetchRadioListsSync Fail");
				}
			}
		});

	}

	private void showRequestFail() {
		LogUtil.d(TAG, "show network error!");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				isFirstClick = true;
				changeNetworkUi(1);
				noNetWork.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mainListView.setVisibility(View.GONE);
			}
		});
	}

	private void showRequestSuccess() {
		LogUtil.d(TAG, "showRequestSuccess");
		// 排行榜
		ranklist = new ArrayList<AuroraRankItem>();
		AuroraRankItem itme = new AuroraRankItem("虾米音乐榜", R.drawable.aurora_xiami_list, RankType.music_all);
		ranklist.add(itme);
		itme = new AuroraRankItem("虾米新歌榜", R.drawable.aurora_xiami_new_list, RankType.newmusic_all);
		ranklist.add(itme);
		itme = new AuroraRankItem("虾米原创榜", R.drawable.aurora_xiami_local_list, RankType.music_original);
		ranklist.add(itme);
		/*
		 * itme = new AuroraRankItem("虾米Demo榜", R.drawable.xiami_demo,
		 * RankType.music_demo); ranklist.add(itme);
		 */
		// itme = new AuroraRankItem("Hito中文榜", R.drawable.xiami_hito,
		// RankType.hito);
		// ranklist.add(itme);
		itme = new AuroraRankItem("Billboard单曲榜", R.drawable.aurora_biboard, RankType.billboard);
		ranklist.add(itme);
		mHandler.post(mUpdateOnlineMusic);
	}

	private Runnable mUpdateOnlineMusic = new Runnable() {

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			mProgressBar.setVisibility(View.GONE);
			if (mOnlineMusicMainAdapter == null) {

				return;
			}
			mAuroraSearchHistoryAdapter.addDatas(historyKeywords);
			mainListView.setVisibility(View.VISIBLE);
			mOnlineMusicMainAdapter.setDatas(mOnlineAlbums, mOnlineCollects, ranklist, mRadioCategories);
			if (mBanners.size() > ImagePagerAdapter.MAXNUM) {
				countSize = ImagePagerAdapter.MAXNUM;

			} else {
				countSize = mBanners.size();
			}
			mAuroraDotView.setDotCount(countSize);
			mPagerAdapter = new ImagePagerAdapter(mContext, mBanners, mOnBannerClickListener);
			if (countSize > 1) {
				mPagerAdapter.setInfiniteLoop(true);
			} else {
				mPagerAdapter.setInfiniteLoop(false);
			}
			bannerPage.setAdapter(mPagerAdapter);
			bannerPage.setCurrentItem(Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % countSize);
			bannerPage.setOnPageChangeListener(mPageChangeListener);
			bannerPage.setInterval(3000);
			bannerPage.setScrollDurationFactor(1000);
			isShowView = true;
			bannerPage.startAutoScroll();
		}
	};

	private void addHistory(String key) {
		if (MusicUtils.mSongDb.insertSearchHistory(key)) {
			if (historyKeywords.size() >= 5) {
				historyKeywords.remove(0);
			}
			historyKeywords.add(key);
			mAuroraSearchHistoryAdapter.notifyDataSetChanged();
		}
	}

	// add by chenhl end

	// ----------------add by tangjie 2014/12/26 start--------------//
	private void showSearchView(final String key, final long time) {
		mSongCount = Integer.MAX_VALUE;
		mAlbumCount = Integer.MAX_VALUE;
		mArtistCount = Integer.MAX_VALUE;
		mPageNo = 1;
		mAlbumPageNo = 1;
		mArtistPageNo = 1;
		searchviewLayout.setVisibility(View.VISIBLE);
		if (mAuroraSearchArtistAdapter != null) {
			mAuroraSearchArtistAdapter.clearCacheList();
		}
		if (mSearchPagerAdapter == null) {
			ArrayList<View> listViews = new ArrayList<View>();
			View songView = View.inflate(mContext, R.layout.aurora_viewpager_item, null);
			mSongListView = (ListView) songView.findViewById(R.id.aurora_online_music_search);
			mSongListView.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// TODO Auto-generated method stub
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						if (view.getLastVisiblePosition() == view.getCount() - 1 && view.getLastVisiblePosition() < mSongCount - 1 && mSongCount != Integer.MAX_VALUE) {
							loadMore(keyWord, 0);
						}
					}
				}

				@Override
				public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub

				}
			});
			mSongListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					OnlineSong item = (OnlineSong) mAuroraSearchSongAdapter.getItem(arg2);
					if (item != null) {
						if (AuroraMusicUtil.isNoPermission(item)) {
							Toast.makeText(mContext, R.string.aurora_play_permission, Toast.LENGTH_SHORT).show();
							return;
						}
						AuroraListItem listItem = new AuroraListItem(item.getSongId(), item.getSongName(), "", item.getAlbumName(), item.getAlbumId(), item.getArtistName(), 1, item.getImageUrl(),
								null, null, -1);
						ArrayList<AuroraListItem> list = new ArrayList<AuroraListItem>();
						list.add(listItem);
						MusicUtils.playAll(mContext, list, 0, 0, true);
						mSongListView.invalidateViews();
						addHistory(item.getSongName());
					}
				}
			});
			mSongListView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mSongListView.requestFocus();
					return false;
				}
			});
			View ablumView = View.inflate(mContext, R.layout.aurora_viewpager_item, null);
			mAlbumListView = (ListView) ablumView.findViewById(R.id.aurora_online_music_search);
			mAlbumListView.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// TODO Auto-generated method stub
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						if (view.getLastVisiblePosition() == view.getCount() - 1 && view.getLastVisiblePosition() < mAlbumCount - 1 && mAlbumCount != Integer.MAX_VALUE) {
							loadMore(keyWord, 1);
						}
					}
				}

				@Override
				public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub

				}
			});
			mAlbumListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					OnlineAlbum album = (OnlineAlbum) mAlbumList.get(arg2);
					Intent intent = new Intent(mContext, AuroraNetTrackDetailActivity.class);
					intent.putExtra("tag", OnGridViewClickListener.NEW_ALBUM);
					intent.putExtra("title", album.getAlbumName());
					intent.putExtra(AuroraNetTrackDetail.ID, String.valueOf(album.getAlbumId()));
					intent.putExtra("imageUrl", album.getImageUrl((int) mContext.getResources().getDimension(R.dimen.aurora_recommend_toplayout_height)));
					intent.putExtra("artist", album.getArtistName());
					mContext.startActivity(intent);
					addHistory(album.getAlbumName());
				}
			});
			mAlbumListView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO
					// Auto-generated
					// method stub
					mAlbumListView.requestFocus();
					return false;
				}
			});
			View artistView = View.inflate(mContext, R.layout.aurora_viewpager_item, null);
			mArtistListView = (ListView) artistView.findViewById(R.id.aurora_online_music_search);
			mArtistListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					OnlineArtist item = mArtistList.get(arg2);
					if (null != item) {
						Intent intent = new Intent(mContext, AuroraNetSearchActivity.class);
						intent.putExtra("title", item.getName());
						intent.putExtra("artist_id", item.getId());
						intent.putExtra("imageUrl", item.getImageUrl());
						intent.putExtra("song_count", item.getAlbumsCount());
						intent.putExtra("album_count", item.getAlbumsCount());
						mContext.startActivity(intent);
						addHistory(item.getName());
					}
				}
			});
			mArtistListView.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// TODO Auto-generated method stub
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						if (view.getLastVisiblePosition() == view.getCount() - 1 && view.getLastVisiblePosition() < mArtistCount - 1 && mArtistCount != Integer.MAX_VALUE) {
							loadMore(keyWord, 2);
						}
					}
				}

				@Override
				public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub

				}
			});
			mArtistListView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO
					// Auto-generated
					// method stub
					mArtistListView.requestFocus();
					return false;
				}
			});
			listViews.add(songView);
			listViews.add(artistView);
			listViews.add(ablumView);
			mSearchPagerAdapter = new AuroraSearchPagerAdapter(listViews);
			mViewPager.setAdapter(mSearchPagerAdapter);
		}
		if (mSongList == null) {
			mSongList = new ArrayList<OnlineSong>();
		} else {
			mSongList.clear();
			if (mAuroraSearchSongAdapter != null) {
				mAuroraSearchSongAdapter.notifyDataSetChanged();
			}
		}
		if (mArtistList == null) {
			mArtistList = new ArrayList<OnlineArtist>();
		} else {
			mArtistList.clear();
			if (mAuroraSearchArtistAdapter != null) {
				mAuroraSearchArtistAdapter.notifyDataSetChanged();
			}
		}
		if (mAlbumList == null) {
			mAlbumList = new ArrayList<OnlineAlbum>();
		} else {
			mAlbumList.clear();
			if (mAuroraSearchAlbumAdapter != null) {
				mAuroraSearchAlbumAdapter.notifyDataSetChanged();
			}
		}
		executor.execute(new AuroraRunnale(mHandler, time));
		// new Thread(new AuroraRunnale(mHandler, time)).start();
	}

	private void refreshUI() {
		mSearchResultView.setVisibility(View.VISIBLE);
		mSongList.clear();
		mAlbumList.clear();
		mArtistList.clear();
		mSongList.addAll(mSongTempList);
		mAlbumList.addAll(mAlbumTemptList);
		mArtistList.addAll(mArtistTempList);
		if (mAuroraSearchSongAdapter == null) {
			mAuroraSearchSongAdapter = new AuroraSearchSongAdapter(mContext, mSongList);
			mSongListView.setAdapter(mAuroraSearchSongAdapter);
		} else {
			mAuroraSearchSongAdapter.notifyDataSetChanged();
			mSongListView.setSelection(0);
		}
		if (mAuroraSearchArtistAdapter == null) {
			mAuroraSearchArtistAdapter = new AuroraSearchArtistAdapter(mContext, mArtistList);
			mArtistListView.setAdapter(mAuroraSearchArtistAdapter);
		} else {
			mAuroraSearchArtistAdapter.notifyDataSetChanged();
		}
		if (mAuroraSearchAlbumAdapter == null) {
			mAuroraSearchAlbumAdapter = new AuroraSearchAlbumAdapter(mContext, mAlbumList);
			mAlbumListView.setAdapter(mAuroraSearchAlbumAdapter);
		} else {
			mAuroraSearchAlbumAdapter.notifyDataSetChanged();
		}
		if (mSongList != null && mSongList.size() == 0) {
			mTipText.setVisibility(View.VISIBLE);
		} else {
			mTipText.setVisibility(View.GONE);
		}
		searchhistory.setVisibility(View.GONE);
	}

	private void loadMore(final String key, final int type) {
		loadingView.setVisibility(View.VISIBLE);
		executor.execute(new LoadMoreRunnale(key, type));
	}

	class LoadMoreRunnale implements Runnable {
		String key;
		int type;

		public LoadMoreRunnale(String key, int type) {
			super();
			this.key = key;
			this.type = type;
		}

		@Override
		public void run() {
			if (type == 0) {
				mPageNo += 1;
				Pair<QueryInfo, List<OnlineSong>> songPair = XiaMiSdkUtils.searchSongSync(mContext, key, PAGE_SIZE, mPageNo);
				if (songPair != null) {
					if (songPair.second != null) {
						mTempList = songPair.second;
					}
					mHandler.sendEmptyMessage(MSG_LOAD_MORE_SONG);
				}
			} else if (type == 1) {
				mAlbumPageNo += 1;
				Pair<QueryInfo, List<OnlineAlbum>> ablumPair = XiaMiSdkUtils.searchAlbumsSync(mContext, key, PAGE_SIZE, mAlbumPageNo);
				if (ablumPair != null && ablumPair.second != null) {
					mTempList = ablumPair.second;
				}
				mHandler.sendEmptyMessage(MSG_LOAD_MORE_ALBUM);
			} else if (type == 2) {
				mArtistPageNo += 1;
				Pair<QueryInfo, List<OnlineArtist>> artistPair = XiaMiSdkUtils.searchArtistsSync(mContext, key, PAGE_SIZE, mArtistPageNo);
				if (artistPair != null) {
					if (artistPair.second != null) {
						mTempList = artistPair.second;
					}
					mHandler.sendEmptyMessage(MSG_LOAD_MORE_ARTIST);
				}
			}

		}

	}

	class AuroraRunnale implements Runnable {
		long mSearchTime;
		Handler mHandler;

		public AuroraRunnale(Handler handler, long time) {
			mSearchTime = time;
			mHandler = handler;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mTimeStamp == mSearchTime) {
				SearchSummaryResult result = XiaMiSdkUtils.searchSummarySync(mContext, keyWord, 15);
				if (result != null) {
					mSongCount = result.getSongsCount();
					mSongTempList = result.getSongs();
					mArtistCount = result.getArtistsCount();
					mArtistTempList = result.getArtists();
					mAlbumCount = result.getAlbumsCount();
					mAlbumTemptList = result.getAlbums();
					LogUtil.d(TAG, "------mSongCount:" + mSongCount + " mArtistCount:" + mArtistCount + " mAlbumCount:" + mAlbumCount);
					if (mTimeStamp == mSearchTime) {
						mHandler.sendEmptyMessage(MSG_SEARCH_FINISHED);
					}
				}
			}
		}

	}
	// ----------------add by tangjie 2014/12/26 end--------------//
}

package com.aurora.community.activity.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.activity.MainActivity;
import com.aurora.community.activity.PostDetailActivity;
import com.aurora.community.adapter.NewsCategoryAdapter;
import com.aurora.community.totalCount.TotalCount;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.FragmentHelper;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.NewsCategoryData;
import com.aurora.datauiapi.data.bean.NewsCategoryObject;
import com.aurora.datauiapi.data.bean.TagInfo;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.umeng.analytics.MobclickAgent;

/**
 * @ClassName: NewsCategoryFragment
 * @Description: 圈子首页显示
 * @author jason
 * @date 2015年3月17日 下午5:37:18
 * 
 */
public class NewsCategoryFragment extends BaseFragment implements
		View.OnClickListener {

	private static final String TAG = "NewsCategoryFragment";

	private ListView mListViewSec;

	private ArrayList<NewsCategoryData> mSecondaryList = new ArrayList<NewsCategoryData>();
	private ArrayList<NewsCategoryData> mTopList = new ArrayList<NewsCategoryData>();

	private NewsCategoryAdapter adapter;

	private TextView leftTopicTitle;
	private TextView leftTopicComment;

	private TextView rightTopicTitle;
	private TextView rightTopicComment;

	private ImageView lefTopicImage;
	private ImageView rightTopicImage;

	private FrameLayout leftTopicClick;
	private FrameLayout RightTopicClick;

	private FragmentHelper mFragmentHelper;

	private View categoryHeadView;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		mDelegate = (MainActivity) activity;
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onActivityCreated(savedInstanceState);
		mComanager = new CommunityManager(this);
		// uploadaPhotos();
		mFragmentHelper = new FragmentHelper();

		if (mTopList.size() == 0) {
			getCategoryInfo();
		} else {
		}

	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if (!hidden) {
			// getCategoryInfo();
			MainActivity actvity = (MainActivity) getActivity();
			if (actvity != null) {
				actvity.setTitleRes(R.string.main_aurora_actionbar_title);
				actvity.enableBackItem(false);
			}
		}

	}
	
	public void refresh(){
		getCategoryInfo();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.news_category_fragment_layout,
				container, false);
	}

	@Override
	public void setupViews() {
		categoryHeadView = LayoutInflater.from(getActivity()).inflate(R.layout.headview_news_category, null);
		// TODO Auto-generated method stub
		mListViewSec = (ListView) getView().findViewById(
				R.id.lv_second_category_list);
		mListViewSec.addHeaderView(categoryHeadView);
		leftTopicTitle = (TextView) getView().findViewById(
				R.id.tv_left_topic_title);
		leftTopicComment = (TextView) getView().findViewById(
				R.id.tv_left_topic_comment);

		rightTopicTitle = (TextView) getView().findViewById(
				R.id.tv_right_topic_title);
		rightTopicComment = (TextView) getView().findViewById(
				R.id.tv_right_topic_comment);

		lefTopicImage = (ImageView) getView().findViewById(R.id.iv_left_topic);
		rightTopicImage = (ImageView) getView().findViewById(
				R.id.iv_right_topic);

		leftTopicClick = (FrameLayout) getView().findViewById(
				R.id.left_topic_click);
		RightTopicClick = (FrameLayout) getView().findViewById(
				R.id.right_topic_click);
		leftTopicClick.setOnClickListener(this);
		RightTopicClick.setOnClickListener(this);
		
		adapter = new NewsCategoryAdapter(getActivity(), mSecondaryList);
		mListViewSec.setAdapter(adapter);
		mListViewSec.setOnItemClickListener(listItemClickListener);
	}

	public void getCategoryInfo() {
		mComanager.getCategoryInfo(new DataResponse<NewsCategoryObject>() {
			@Override
			public void run() {
				Log.e("linp", "####################getCategoryInfo");
				// TODO Auto-generated method stub
				if (null != value) {
					if (value.getReturnCode() == Globals.CODE_SUCCESS
							&& isAdded()) {
						if (value.getData().getInfo().size() > 0) {
							filterList(value.getData().getInfo());
							setupTopView();
							adapter.notifyDataSetChanged();
						}
						if (getActivity() != null) {
							((MainActivity) getActivity()).hideNoNetWorkLayer();
						}
					} else {
						Log.d("linp", "error: " + value.getMsg());
						Log.e("linp",
								"NewsCategoryFragment in getting category info that not didn't attach activity itself.");
					}
				}

			}
		});
	}

	private void filterList(ArrayList<NewsCategoryData> list) {
		mTopList.clear();
		mSecondaryList.clear();
		CommunityApp cpp = (CommunityApp) getActivity().getApplicationContext();
		cpp.getTagInfos().clear();
		for (int i = 0; i < list.size(); i++) {
			TagInfo tag = new TagInfo();
			tag.setTid(list.get(i).getTid());
			tag.setTname(list.get(i).getTname());
			tag.setTsname(list.get(i).getTsname());
			cpp.addTag(tag);
			if (i < 2) {
				mTopList.add(list.get(i));
			} else {
				mSecondaryList.add(list.get(i));
			}
		}

	}

	private void setupTopView() {
		try {
			if (mTopList != null) {
				NewsCategoryData TopicLeft = mTopList.get(0);
				NewsCategoryData TopicRight = mTopList.get(1);
				if (TopicLeft != null) {
					leftTopicTitle.setText(TopicLeft.getTname());

					leftTopicComment.setText(getString(R.string.total_comment,
							TopicLeft.getPost_count()));
					ImageLoaderHelper.disPlay(TopicLeft.getCover(),
							lefTopicImage,
							DefaultUtil.getDefaultImageDrawable(getActivity()));
				}
				if (TopicRight != null) {
					rightTopicTitle.setText(TopicRight.getTname());
					rightTopicComment.setText(getString(R.string.total_comment,
							TopicRight.getPost_count()));
					ImageLoaderHelper.disPlay(TopicRight.getCover(),
							rightTopicImage,
							DefaultUtil.getDefaultImageDrawable(getActivity()));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private OnItemClickListener listItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			int position = (int)arg3;
			if (position == 0)
				/*new TotalCount(getActivity(), "300", "009", 1)
						.CountData();*/
				MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_GROUP3);
			else if (position == 1)
				/*new TotalCount(getActivity(), "300", "010", 1)
						.CountData();*/
				MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_GROUP4);
			else if (position == 2)
				/*new TotalCount(getActivity(), "300", "011", 1)
						.CountData();*/
				MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_GROUP5);
			onCategoryClickResponse(mSecondaryList.get(position)
					.getTid(),mSecondaryList.get(position).getTname(),mSecondaryList.get(position).getPost_count());
		}
		
	};
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int id = arg0.getId();
		switch (id) {
		case R.id.left_topic_click:
			onCategoryClickResponse(mTopList.get(0).getTid(),mTopList.get(0).getTname(),mTopList.get(0).getPost_count());
			/*new TotalCount(getActivity(), "300", "007", 1).CountData();*/
			MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_GROUP);
			break;
		case R.id.right_topic_click:
			onCategoryClickResponse(mTopList.get(1).getTid(),mTopList.get(1).getTname(),mTopList.get(0).getPost_count());
			/*new TotalCount(getActivity(), "300", "008", 1).CountData();*/
			MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_GROUP2);
			break;
		default:
			break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		Log.e("linp", "NewsCategoryFragment onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.e("linp", "NewsCategoryFragment onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		Log.e("linp", "NewsCategoryFragment onDetach");
		super.onDetach();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.e("linp", "NewsCategoryFragment onStart");
		super.onStart();
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		if (getActivity() != null) {
			((MainActivity) getActivity()).handleMessage(getTag());
		}

		Log.e("linp", "####################handleMessage");
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub
		Log.e("linp", "##############onWrongConnectionState");
		super.onWrongConnectionState(state, manager, source);
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		// TODO Auto-generated method stub
		Log.e("linp", "##############onError");
		super.onError(code, message, manager, e);
	}

}

package com.aurora.iunivoice.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.ListView;

import com.aurora.datauiapi.data.bean.UserInfoExtcredits;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.fragment.PersonalFragment;
import com.aurora.iunivoice.adapter.MineScoreAdapter;

public class MineScoreActivity extends BaseActivity {

       private ArrayList<UserInfoExtcredits> extcredits;
       private ListView lv_score;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mine_score);
		extcredits = getIntent().getParcelableArrayListExtra(PersonalFragment.USER_EXTCREDITS_KEY);
		setupViews();
	}

	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		setTitleRes(R.string.mine_score_text);
		lv_score = (ListView) findViewById(R.id.lv_scores);
		lv_score.setAdapter(new MineScoreAdapter(extcredits, this));
	}

}

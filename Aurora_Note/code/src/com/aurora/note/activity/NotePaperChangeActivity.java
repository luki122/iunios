package com.aurora.note.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.aurora.note.R;
import com.aurora.note.adapter.PaperListAdapter;
import com.aurora.note.ui.MultiColumnListView;
import com.aurora.note.util.Globals;

public class NotePaperChangeActivity extends AuroraActivity {

	public static final String PAPER_NAME = "paper_name";
	private String paperName;

	private PaperListAdapter paperAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.note_paper_change_activity);

		initActionBar();
		initData();
		initView();
	}

	private void initActionBar() {
		AuroraActionBar actionBar = getAuroraActionBar();
		actionBar.setTitle(R.string.note_change_paper);

		actionBar.getTitleView().setTextColor(getResources().getColor(R.color.note_main_text_color));
		actionBar.setBackgroundResource(R.drawable.aurora_action_bar_top_bg);

		ImageButton homeIcon = (ImageButton) actionBar.getHomeButton();
		homeIcon.setImageResource(R.drawable.aurora_action_bar_back);
	}

	private void initData() {
		paperName = getIntent().getStringExtra(PAPER_NAME);
		if (TextUtils.isEmpty(paperName)) {
			paperName = Globals.NOTE_PAPER_1;
		}
	}

	private void initView() {
		MultiColumnListView paperList = (MultiColumnListView) findViewById(R.id.paper_list);
		paperList.setSelector(R.color.white);
		paperList.setDivider(null);

		Resources resources = getResources();
		paperList.setColumnPadding(
				resources.getDimensionPixelOffset(R.dimen.note_paper_list_padding), 
				resources.getDimensionPixelOffset(R.dimen.note_paper_list_padding));

		View footer = getLayoutInflater().inflate(R.layout.note_paper_list_footer, null);
		paperList.addFooterView(footer);

		paperAdapter = new PaperListAdapter(this, paperName);
		paperList.setAdapter(paperAdapter);
	}

	@Override
	public void finish() {
		if (paperAdapter != null) {
			int which = paperAdapter.getWhichSelected();
			String newPaperName = Globals.NOTE_PAPERS[which];

			if (!newPaperName.equals(paperName)) {
				Intent intent = new Intent();
				intent.putExtra(PAPER_NAME, newPaperName);
				setResult(RESULT_OK, intent);
			} else {
				setResult(RESULT_CANCELED);
			}
		} else {
			setResult(RESULT_CANCELED);
		}

		super.finish();
	}

}

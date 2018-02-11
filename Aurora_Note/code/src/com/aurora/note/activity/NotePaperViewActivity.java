package com.aurora.note.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.aurora.note.R;

public class NotePaperViewActivity extends Activity {

	public static final String PAPER_RESOURCE_ID = "paper_resource_id";
	private int paperResourceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.note_paper_view_activity);

		initData();
		initView();
	}

	private void initData() {
		paperResourceId = getIntent().getIntExtra(PAPER_RESOURCE_ID, R.drawable.note_paper_view_01);
	}

	private void initView() {
		ImageView paperView = (ImageView) findViewById(R.id.paper_view);
		paperView.setImageResource(paperResourceId);
		paperView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}

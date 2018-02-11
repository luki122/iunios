package com.aurora.note.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.note.R;
import com.aurora.note.util.Globals;

public class PaperListAdapter extends BaseAdapter {

	private static final int[] PAPER_VIEW_RESOURCE_IDS = {
		R.drawable.note_paper_view_01,
		R.drawable.note_paper_view_02,
		R.drawable.note_paper_view_03,
		R.drawable.note_paper_view_04,
		R.drawable.note_paper_view_05,
		R.drawable.note_paper_view_06
	};

	private Context context;
	private int whichSelected;

	public int getWhichSelected() {
		return whichSelected;
	}

	public PaperListAdapter(Context context, int whichSelected) {
		this.context = context;
		this.whichSelected = whichSelected;
	}

	public PaperListAdapter(Context context, String paperName) {
		this.context = context;

		whichSelected = 0;
		if (!TextUtils.isEmpty(paperName) && paperName.startsWith(Globals.DRAWABLE_PROTOCOL)) {
			for (int i = 0; i < Globals.NOTE_PAPERS.length; i++) {
                if (Globals.NOTE_PAPERS[i].equals(paperName)) {
                    whichSelected = i;
                    break;
                }
            }
		}
	}

	@Override
	public int getCount() {
		return PAPER_VIEW_RESOURCE_IDS.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.note_paper_list_item, parent, false);

			holder = new ViewHolder();
			holder.paperView = (ImageView) convertView.findViewById(R.id.paper_view);
			holder.paperSelected = (ImageView) convertView.findViewById(R.id.paper_selected);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (whichSelected == position) {
			holder.paperSelected.setImageResource(R.drawable.note_paper_selected);
		} else {
			holder.paperSelected.setImageResource(R.drawable.note_paper_unselected);
		}

		final int which = position;
		final int paperViewResourceId = PAPER_VIEW_RESOURCE_IDS[position];
		holder.paperView.setImageResource(paperViewResourceId);
		holder.paperView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (whichSelected != which) {
					whichSelected = which;
					notifyDataSetChanged();
				}
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		private ImageView paperView;
		private ImageView paperSelected;
	}

}
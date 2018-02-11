package com.android.contacts.list;

import com.android.contacts.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

public abstract class BaseTextIconAdapter extends BaseAdapter implements OnItemClickListener, OnScrollListener{
    private LayoutInflater mInflater;
    protected Context mContext;
    public static final int STRANGE_NUMBER_ADD_NEW_CONTACT = 201;
    public static final int STRANGE_NUMBER_ADD_TO_CONTACT = 202;
    public static final int STRANGE_NUMBER_SEND_SMS = 203;

    public class ChoiceItem {
        private int textRes;
        private int iconRes;
        private int id;

        public ChoiceItem(int s, int b, int i) {
            textRes = s;
            iconRes = b;
            id = i;
        }
        
        public int getId() {
        	return id;
        }
    }

    public ChoiceItem[] mChoiceItems;

    public BaseTextIconAdapter(Context context) {
    	mContext = context;
        mInflater = LayoutInflater.from(context);
        initChoiceItems(context);
    }
    
    /*
     * for example 
     * mChoiceItems = new ChoiceItem[]{
     *  	new ChoiceItem(R.string.gn_dialpad_new_contact_item, R.drawable.gn_new_contact, STRANGE_NUMBER_ADD_NEW_CONTACT),
     *		new ChoiceItem(R.string.gn_dialpad_add_exist_contact_item, R.drawable.gn_add_existing_contact, STRANGE_NUMBER_ADD_TO_CONTACT),
     *		new ChoiceItem(R.string.gn_dialpad_send_message_item, R.drawable.gn_send_message, STRANGE_NUMBER_SEND_SMS)};
     *		*/
    public abstract void initChoiceItems(Context context);

    public int getCount() {
        return mChoiceItems.length;
    }

    /**
     * Return the ChoiceItem for a given position.
     */
    public Object getItem(int position) {
        return mChoiceItems[position];
    }

    /**
     * Return a unique ID for each possible choice.
     */
    public long getItemId(int position) {
        return mChoiceItems[position].getId();
    }

    /**
     * Make a view for each row.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.dialpad_chooser_list_item, null);
        }

        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText(mChoiceItems[position].textRes);

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        if (0 < mChoiceItems[position].iconRes) {
        	icon.setImageResource(mChoiceItems[position].iconRes);
        } else {
        	icon.setVisibility(View.GONE);
        }

        return convertView;
    }
    
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}
}
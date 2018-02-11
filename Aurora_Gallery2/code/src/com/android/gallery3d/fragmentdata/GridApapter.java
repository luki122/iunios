package com.android.gallery3d.fragmentdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.menu;
import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.fragmentapp.AnimationInterpolator;
import com.android.gallery3d.fragmentapp.GridItem;
import com.android.gallery3d.fragmentutil.ImageResizer;


public class GridApapter<T extends IGristItem> extends BaseAdapter implements View.OnClickListener
,View.OnLongClickListener{
	
	private static final String TAG = "GridApapter";
	private int                  mNumColumns;
    private ArrayList<Container> mContainers = null;
    
    public static final int VIEW_TYPE_HEADER = 0;
	public static final int VIEW_TYPE_ROW = 1;
	
	private final static String LAST_TITLE = "Last";
	private final static int Order_NumColumns = 6;
	
	private Context mContext = null;
	private int mGridItemSize = -1;
	private int m_Padding = 10;
	private int m_HeaderPadding = 0;
	private int m_HeaderPadding2 = 0;
	private int m_leftlayoutwidth = 0;
	private int m_leftspacewidth = 0;
	private int m_horizontalSpacing = 0;
	private int m_verticalSpacing = 0;
	private int m_paddingright = 0;
	private int m_Displaywidth = 0;
	private int m_Displayheight = 0;
	private MyInteger iMyInteger;
	private boolean m_NumColumnChanges = false;
	
	private AnimationInterpolator m_AnimationController = null;
	
	private ImageResizer mResizer;
	private LayoutInflater  mInflater;
	private boolean m_bVisble = false;
	private Map<Long, Boolean> mSelectMap = new HashMap<Long, Boolean>();
	//private static final String AURORA_DEFAULT_NUMBER_FONT_PATH = "system/fonts/Roboto-Thin.ttf";
	//private static final String AURORA_DEFAULT_NUMBER_FONT_PATH = "system/fonts/Roboto-Light.ttf";
	private Typeface auroraNumberTf;
	
	/*private static TextView m_wenziTextView = null;
	private static TextView m_dateTextView = null;
	private static TextView m_yearTextView = null;*/
	
	
	
	public static interface OnGridItemClickListener {
		public void onGridItemClicked(Long id, int position, View v);
	}

	private OnGridItemClickListener onClickListener = null;
	
	public static interface OnGridItemLongClickListener {
		public boolean onGridItemLongClicked(Long id, int position, View v);
	}
	
	private OnGridItemLongClickListener onLongClickListener = null;
	
    
    private class Container {
        String title;
        ArrayList<T> items;

        public Container(String title) {
            this.title = title;
            this.items = new ArrayList<T>();
        }
    }
    
    private void init_res() {
    	
    	m_leftlayoutwidth = (int)mContext.getResources().getDimension(R.dimen.date_layout_margin);
    	//m_leftspacewidth = (int)mContext.getResources().getDimension(R.dimen.grid_item_dater_space);
		m_horizontalSpacing = (int)mContext.getResources().getDimension(R.dimen.gridview_horizontalSpacing);
		m_verticalSpacing = m_horizontalSpacing;//(int)mContext.getResources().getDimension(R.dimen.gridview_verticalSpacing);
		
		m_paddingright = (int)mContext.getResources().getDimension(R.dimen.gridview_paddingright);
		m_HeaderPadding = (int)mContext.getResources().getDimension(R.dimen.header_hight);
		m_HeaderPadding2 = m_HeaderPadding + m_horizontalSpacing*2;
		
    	DisplayMetrics dm = new DisplayMetrics();
    	((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		m_Displaywidth = dm.widthPixels;
		m_Displayheight = dm.heightPixels;
		//Log.i(TAG, "zll ----- m_Displaywidth:"+m_Displaywidth+",m_Displayheight:"+m_Displayheight);
		
		if (mNumColumns == 6) {
			mGridItemSize = (m_Displaywidth-m_leftlayoutwidth-(mNumColumns-1)*m_horizontalSpacing- m_horizontalSpacing)/mNumColumns;
		}else {
			mGridItemSize = (m_Displaywidth-m_leftlayoutwidth-(mNumColumns-1)*m_horizontalSpacing- m_paddingright)/mNumColumns;
		}
		
		Log.i(TAG, "zll ----- mGridItemSize:"+mGridItemSize+",m_leftlayoutwidth:"+m_leftlayoutwidth+",m_paddingright:"+m_paddingright+",m_horizontalSpacing:"+m_horizontalSpacing);
		return;
	}
    
    public int getGridImgSize() {
		return mGridItemSize;
	}
	
    public GridApapter(Context context, int numColumns) {
        mNumColumns = numColumns;
        mContext = context;
        init_res();
        
        m_NumColumnChanges = false;
        mInflater = LayoutInflater.from(mContext);
        iMyInteger = new MyInteger();
        mContainers = new ArrayList<Container>();
        
        /*
        try {
        	auroraNumberTf = Typeface.createFromFile(AURORA_DEFAULT_NUMBER_FONT_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
        
        //m_AnimationController = new AnimationInterpolator();
        //mSelectMap.clear();
    }
    
    public void CreateItemsIndex(List<T> items) {
    	int order = 0;
    	if (mNumColumns == Order_NumColumns) {
    		order = 1;
		}
    	
        for (T item : items) {
            if (mContainers.size() == 0) {
                mContainers.add(new Container(item.getTitle(order)));
            }

            Container container = mContainers.get(mContainers.size() - 1);
            if (container.title.equals(item.getTitle(order))) {
            } else {
                mContainers.add(new Container(item.getTitle(order)));
                container = mContainers.get(mContainers.size() - 1);
            }
            
            //Log.i(TAG, "zll ---- item getTime:"+item.getTime()+",getTime:"+item.getTitle());
            container.items.add(item);
        }
        
        if (items.size() > 0) {
        	mContainers.add(new Container(LAST_TITLE));
		}
        
    }
    
    public void clearContainerList() {
    	if (mContainers.size() > 0) {
    		mContainers.clear();
		}
		return;
	}

    private int getRowSize(Container c) {
        return ((c.items.size() + mNumColumns - 1) / mNumColumns) + 1;
    }
    
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
        init_res();
        m_NumColumnChanges = true;
        GridItem.SetCloumsNum(mNumColumns);
    }
    
    public void clearSelectMap() {
    	mSelectMap.clear();
		return;
	}
    
    public void selectAllSelectMap() {
		return;
	}
    
    public void setSelectMap(HashMap<Long, Boolean> hMap) {
    	mSelectMap.clear();
    	mSelectMap = (HashMap<Long, Boolean>)hMap.clone();
    	//mSelectMap = hMap;
    	Log.i(TAG, "zll ----setSelectMap mSelectMap size:"+mSelectMap.size());
		return;
	}
    
    public void OnClickSetCheckImgVisible(boolean flag) {
    	GridItem.setCheckImageVisible(flag);
		return;
	}
    
	@Override
	public int getCount() {
		int count = 0;
        for (Container c : mContainers) {
            count += getRowSize(c);
        }

        //Log.i(TAG, "zll ---- getCount count:"+count);
        return count;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (isSectionHeader(position)) {
			return VIEW_TYPE_HEADER;
		}

		return VIEW_TYPE_ROW;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = null;
		//long time0 = System.currentTimeMillis();
		boolean isSectionheader = isSectionHeader(position);
		if (convertView == null) {
			if (isSectionheader) {
				v = mInflater.inflate(R.layout.section_header, null);
			} else {
				LinearLayout ll = (LinearLayout) mInflater.inflate(R.layout.listrow_item, null);
				v = ll;
				
				//Log.i(TAG, "zll ---- ffff 0.1 position:"+position);
				ll = (LinearLayout) ll.findViewById(R.id.listview_rowitem);
				for (int i = 0; i < mNumColumns; i++) {
					// add a child
					GridItem item = new GridItem(mContext);
					ll.addView(item, new LinearLayout.LayoutParams(mGridItemSize, mGridItemSize));
					//Log.i(TAG, "zll ---- ffff 0.2 position:"+position);
					if (i < mNumColumns - 1) {
						// now add space view
						View spaceItem = new View(mContext);
						ll.addView(spaceItem, new LinearLayout.LayoutParams(m_horizontalSpacing, mGridItemSize));
					}
				}
			}
		}else {
			v = convertView;
			/*if (!isSectionheader) {
				if (v.getLayoutParams().height != mGridItemSize) {
					v.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, mGridItemSize));
	            }
			}*/
		}
		
		if (isSectionheader) {
			//Log.i(TAG, "zll ---- ffff 0 position:"+position);
			TextView tView = (TextView)v;
			if (tView != null) {
				if (position == 0) {
					tView.setHeight(m_HeaderPadding);
				}else {
					tView.setHeight(m_HeaderPadding2);
				}
			}
		}else {
			
			//long time1 = System.currentTimeMillis();
			
			LinearLayout ll = (LinearLayout) v;
			
			LinearLayout rowPanel = (LinearLayout) ll.findViewById(R.id.listview_rowitem);
			
			//Log.i(TAG, "zll --- InitData need time xxxx1 is :"+(System.currentTimeMillis() - time1));
			boolean isLastRowInSection = isLastRowInSection(position, iMyInteger);
			int cursorStartAt = 0;
			
			//Log.i(TAG, "zll --- InitData need time xxxx1 is :"+(System.currentTimeMillis() - time1)+",itemContainer num:"+ll.getChildCount()+",all num:"+rowPanel.getChildCount());
			//long time2 = System.currentTimeMillis();
			
			//Log.i(TAG, "zll ---- ffff 1 position:"+position+",iMyInteger.cur_index;"+iMyInteger.cur_index);
			
			View tchild = (View)ll.getChildAt(0);
			if (isDateLayout(position)) {
				//Log.i(TAG, "zll ---- xxxxx 2 position:"+position);
				if (tchild != null) {
					//Log.i(TAG, "zll ---- xxxxx 2.1 position:"+position);
					showDateLayout(tchild, position);
					tchild.setVisibility(View.VISIBLE);
				}
			}else {
				//Log.i(TAG, "zll ---- xxxxx 3 position:"+position);
				if (tchild != null) {
					tchild.setVisibility(View.INVISIBLE);
				}
			}
			
			//Log.i(TAG, "zll ---- xxxxx 2 position:"+position);
			int startNoshowNum = 0;
			if (isLastRowInSection) {
				int gaps = 0;
				int childrenInLastRow = remainderInLastRowGroup(iMyInteger.cur_index);
				if (childrenInLastRow > 0) {
					gaps = childrenInLastRow - 1;
				}
				
				startNoshowNum = childrenInLastRow + gaps;
			}
			
			// set all children visible first
			for (int i = 0; i < 2 * mNumColumns - 1; i++) {
				// we need to hide grid item and gap
				View child = rowPanel.getChildAt(i);
				if (child == null) {
					continue;
				}
				
				if (startNoshowNum > 0) {
					if (i >= startNoshowNum) {
						child.setVisibility(View.INVISIBLE);
						continue;
					} else {
						child.setVisibility(View.VISIBLE);
					}
					
				} else {
					child.setVisibility(View.VISIBLE);
				}
				
				// leave alternate
				if (i % 2 == 0) {
					// its not gap, set listener on image button
					GridItem items = (GridItem)child;
					//Log.i(TAG, "zll ---- items.isHardwareAccelerated():"+items.isHardwareAccelerated());
					int itempos = getGridPosition(position, cursorStartAt);
					T tmpT = getGalleryItem(itempos);
					//Log.i(TAG, "zll ---- xxxxx 1.1 position:"+position+",itempos:"+itempos);
					if (tmpT != null) {
						ButtonViewHolder bholder = new ButtonViewHolder();
						bholder.positionInSection = itempos;
						bholder.parent = child;
						bholder.id = tmpT.getId();
						items.setTag(bholder);
						
						//Log.i(TAG, "zll ---- xxxxx 1.2 position:"+position+",itempos:"+itempos+",bholder.id:"+bholder.id);
						/*String []uriStr = new String[2]; 
						uriStr[0] = tmpT.getUri();
						uriStr[1] = tmpT.getFilePath();*/
						//items.SetRecyclingImageView(mResizer, tmpT.getFilePath(), tmpT.getType());
						items.SetRecyclingImageViewByObject(mResizer, tmpT, tmpT.getType());
						//items.SetRecyclingImageViewByUri(mResizer, tmpT.getUri(), tmpT.getType());
						items.setChecked(mSelectMap.get(bholder.id) == null ? false : mSelectMap.get(bholder.id));
						
						items.setOnClickListener(this);
						items.setOnLongClickListener(this);
					}
					
					cursorStartAt++;
				}
			}
			
		}
	
		
		return v;
	}

	public void RotateImages(int position, View mView) {
		/*
		TextView tView = (TextView)mInflater.inflate(R.layout.section_header, null);
		if (mView instanceof TextView) {
			return;
		}
		
		ViewGroup gViewGroup = (ViewGroup)mView;
		if (gViewGroup == null) {
			return;
		}
		Log.i(TAG, "zll --- RotateImages gViewGroup.getChildCount():"+gViewGroup.getChildCount());
		ViewGroup group = null;
		if (gViewGroup.getChildCount() >= 2) {
			group = (ViewGroup)gViewGroup.getChildAt(1);
		}
		
		if (group == null) {
			return;
		}
		
		for (int i = 0; i < 2 * mNumColumns - 1; i++) {
			View child = group.getChildAt(i);
			if (child instanceof GridItem) {
				
				float cX = child.getWidth() / 2.0f;
				float cY = child.getHeight() / 2.0f;
				Log.i(TAG, "zll ---- interpolatedTime cX:"+cX+",cY:"+cY);
				
				RotateAnimation rotateAnim  = new RotateAnimation(cX, cY,RotateAnimation.ROTATE_DECREASE);
				
				rotateAnim.setInterpolatedTimeListener(new InterpolatedTimeListener() {
					
					@Override
					public void interpolatedTime(float interpolatedTime) {
						
						if (interpolatedTime > 0.4) {
							return;
						}
					}
				});
				rotateAnim.setFillAfter(true);
				child.startAnimation(rotateAnim);
			}
		}*/
		
		return;
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}
	
	private void showDateLayout(View viewgroup, int position) {
		synchronized (this) {
			if (viewgroup == null) {
				return;
			}
			
			String tmpstr = getDateTitle(position);
			if (tmpstr.length() == 0 || tmpstr.equals("")) {
				return;
			}
			
			if (mNumColumns == 6) {
				TextView wenziTextView = (TextView)viewgroup.findViewById(R.id.date_wenzi);
				/*if (m_wenziTextView == null) {
					m_wenziTextView = (TextView)viewgroup.findViewById(R.id.date_wenzi);
				}*/
				wenziTextView.setText(mContext.getString(R.string.date_month));
			}
			
			TextView dateTextView = (TextView)viewgroup.findViewById(R.id.date_date);
			/*if (m_dateTextView == null) {
				m_dateTextView = (TextView)viewgroup.findViewById(R.id.date_date);
			}*/
			dateTextView.setText(tmpstr.substring(tmpstr.length()-2, tmpstr.length()));
			
			
			TextView yearTextView = (TextView)viewgroup.findViewById(R.id.date_year);
			/*if (m_yearTextView == null) {
				m_yearTextView = (TextView)viewgroup.findViewById(R.id.date_year);
			}*/
			yearTextView.setText(tmpstr.substring(0, tmpstr.length()-3));
			// 0 : use chinese fonts
			// 1 : use number fonts
			// 2 : use english fonts
			//public void auroraSetFontType(int whichType)
			{
				try {
					yearTextView.setTypeface(auroraNumberTf);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//Log.i(TAG, "zll ---- showDateLayout tmpstr:"+tmpstr);
		}
		return;
	}
	
	private String getDateTitle(int position) {
		
		if (mContainers != null && mContainers.size() >0) {
			int index = getDaterIndex(position);
			return mContainers.get(index).title;
		}
		return "";
	}
	
	private int getDaterIndex(int position) {
		if (position > 0) {
			int totalRowSize = 0;
			if (position == 1) {
				return 0;
			}
			
			int index = 0;
			for (Container c : mContainers) {
				int rowSize = getRowSize(c);
				totalRowSize += rowSize;
				index++;
				if (position == totalRowSize+1) {
					return index;
				}
			}
		}
		
		return 0;
	}
	
	public boolean isDateLayout(int position)
	{
		if (position > 0) {
			int totalRowSize = 0;
			if (position == 1) {
				return true;
			}
			
			//Log.i(TAG, "zll --- mContainers size:"+mContainers.size());
			for (Container c : mContainers) {
				int rowSize = getRowSize(c);
				
				if (position == totalRowSize+1) {
					return true;
				}
				totalRowSize += rowSize;
			}
		}
		
		return false;
	}
	
	public int getDateLayoutPosition(int index)
	{
		int totalRowSize = 0;
		if (index == 0) {
			return 1;
		}
		
		for (Container c : mContainers) {
			totalRowSize += getRowSize(c);
			if (index < totalRowSize) {
				return totalRowSize;
			}
		}
		
		return 0;
	}
	
	public boolean isSectionHeader(int position) {

		int totalRowSize = 0;
		if (position == 0)
			return true;
		
		for (Container c : mContainers) {
			if (totalRowSize == position) {
				return true;
			}
			
			int rowSize = getRowSize(c);
			totalRowSize += rowSize;
		}

		return false;
	}
	
	private boolean isLastRowInSection(int position, MyInteger tInteger) {
		int totalRowSize = 0;
		tInteger.cur_index = 0;
		
		for (Container c : mContainers) {
			int rowSize = getRowSize(c);
			totalRowSize += rowSize;
			if (position == totalRowSize - 1)
			{
				return true;
			}

			tInteger.cur_index++;
			if (position < totalRowSize) {
				break;
			}
		}

		return false;
	}
	
	//original is getTitle
	public String getHeaderTitle(int position) {
        int totalRowSize = 0;
        for (Container c : mContainers) {
        	
        	totalRowSize += getRowSize(c);
            if (totalRowSize >= position) {
                return c.title;
            }
        }

        return "";
    }

	private int remainderInLastRowGroup(int header) {
		int remainder = 0;
	
    	if (mContainers != null && mContainers.size() > 0) {
    		remainder = (mContainers.get(header).items.size())%mNumColumns;
		}
    	
    	return remainder;
    	//return remainder == 0 ? 0 : mNumColumns - remainder; 
    }
	
	public T getGalleryItem(int cur_pos){
		T mT = null;
		int totalNum = 0;
		int index = 0;
		int lastNum = 0;
		
		if (mContainers.size() <= 0) {
			return null;
		}
		
		for (Container c : mContainers){
			
			totalNum += mContainers.get(index).items.size();
			//Log.i(TAG, "zll ---- cur_pos:"+cur_pos+",index:"+index+",totalNum:"+totalNum+",lastNum:"+lastNum);
			if (totalNum-1 >= cur_pos) {
				if (index != 0) {
					cur_pos -= lastNum;
				}
				mT = mContainers.get(index).items.get(cur_pos);
				break;
			}
			
			lastNum = totalNum;
			index++;
		}
		
		return mT;
	}
	
	private int getGridPosition(int position, int num){
		int pos = -1;
	    int i= 0;
	    int filler = 0;
	    int totalRowSize = 0;
	    
		for (Container c : mContainers){
			
			totalRowSize += getRowSize(c);
			
			if (i > 0) {
				filler += unFilledSpacesInRowsGroup(i-1);
			}
			
			if (totalRowSize >= position) {
				pos = mNumColumns*(position -i-1)+num - filler;
				return pos;
			}
			
			i++;
		}
		
		return pos;
	}
	
	public int getListViewIndexFromItem(T t) {
		int index = 0;
		int i = 0;
		int header = 0;
		int order = 0;
		int nowrows = 0;
		
		if (t == null) {
			return 0;
		}
		
		if (mNumColumns == Order_NumColumns) {
    		order = 1;
		}
		
		for (Container c : mContainers) {
			if (t.getTitle(order).equals(mContainers.get(header).title)) {
				ArrayList<T> tmpList = mContainers.get(header).items;
				for (int j = 0; j < tmpList.size(); j++) {
					if (t.getUri().equals(tmpList.get(j).getUri())) {
						index = (j/mNumColumns) + nowrows+1;
						//Log.i(TAG, "zll --- getListViewIndexFromItem index:"+index+",getUri():"+t.getUri()+",j:"+j+",size():"+tmpList.size());
						break;
					}
				}
			}
			
			nowrows += getRowSize(c);
			header++;
		}
		
		return index;
	}
	
	private int unFilledSpacesInRowsGroup(int header) {
		int remainder = 0;
	
    	if (mContainers != null && mContainers.size() > 0) {
    		remainder = (mContainers.get(header).items.size())%mNumColumns;
		}
    	
    	return remainder == 0 ? 0 : mNumColumns - remainder; 
    }
	
	private int getHeaderToatalItemsCount(int position) {
		int totalCount = 0;
		int index = 0;
		
		for (Container c : mContainers)
		{
			totalCount += mContainers.get(index).items.size();
			if (index >= position)
			{
				break; 
			}
			
			index++;
		}
		
		return totalCount;
	}
	
	public int getListViewPosition(int position, int num){
		int pos = -1;
	    int i= 0;
	    int filler = 0;
	    int totalRowSize = 0;

		for (Container c : mContainers){
			
			totalRowSize += getRowSize(c);
			
			if (i > 0) {
				filler += unFilledSpacesInRowsGroup(i-1);
			}
			
			if (totalRowSize >= position) {
				pos = mNumColumns*(position -i-1)+num - filler;
				return pos;
			}
			
			i++;
		}
		
		if (pos < 0) {
			pos = 0;
		}
		return pos;
	}
	
	public void setOnListener(OnGridItemClickListener listener) {
		this.onClickListener = listener;
	}
	
	public void setOnLongListener(OnGridItemLongClickListener listener) {
		this.onLongClickListener = listener;
	}
	
	public void setImageResizer(ImageResizer resizer) {
		this.mResizer = resizer;
	}
	
	public int getSpaceBetweenChildrenInRow() {
		return m_verticalSpacing;
	}
	
	@Override
	public void onClick(View v) {
		ButtonViewHolder holder = (ButtonViewHolder) v.getTag();
		if (this.onClickListener != null) {
			onClickListener.onGridItemClicked(holder.id, holder.positionInSection, holder.parent);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		ButtonViewHolder holder = (ButtonViewHolder) v.getTag();
		if (this.onLongClickListener != null) {
			return onLongClickListener.onGridItemLongClicked(holder.id, holder.positionInSection, holder.parent);
		}
		
		return false;
	}
	
	public static class ButtonViewHolder {
		int 	positionInSection;
		long	id;
		View 	parent;
	}
	
	private class MyInteger{
		int cur_index;
	}
	
}

package com.aurora.note.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap.Config;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.note.R;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.ui.AuroraTextViewSnippet;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.util.imagecache.ImageResizer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.Date;

public class NoteListAdapter extends BaseAdapter {

	// private static final String IMAGE_CACHE_DIR = "thumbnail";
	// private static final String TAG_SEPARATOR = "，";
	private LayoutInflater mInflater = null;
	private Context mContext;
	private ArrayList<NoteResult> mListToDisplay = new ArrayList<NoteResult>();
	private DisplayImageOptions optionsImage;
	// private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private ImageResizer mImageResizer;
	public boolean mDeleteFlag = false;

	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();

	private String mQueryText;

	private int mTotalHeight = 0;
	private int mPicWeith = 0;

	public NoteListAdapter(Context context, ArrayList<NoteResult> list, String queryText) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mListToDisplay = list;
		mQueryText = queryText;

		Resources resources = context.getResources();
		mTotalHeight = Math.round(resources.getDimension(R.dimen.note_list_item_total_height));
		mPicWeith = Math.round(resources.getDimension(R.dimen.note_list_item_pic_width));

		/*optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.forum_loading_default)
				.showImageForEmptyUri(R.drawable.forum_loading_failed)
				.showImageOnFail(R.drawable.forum_loading_failed)
				.displayer(new RoundedBitmapDisplayer(10))
				.cacheInMemory(true).build();*/
		optionsImage = new DisplayImageOptions.Builder()
          .imageScaleType(ImageScaleType.EXACTLY/*IN_SAMPLE_INT*/)
          .showImageOnLoading(R.drawable.forum_loading_default)
          .showImageForEmptyUri(R.drawable.forum_loading_failed)
          .showImageOnFail(R.drawable.forum_loading_failed)
         /* .cacheInMemory(true)*/.bitmapConfig(Config.RGB_565).build();
		//.cacheOnDisc(true)

//		ImageCacheParams cacheParams = new ImageCacheParams(mContext, IMAGE_CACHE_DIR);
		mImageResizer = new ImageResizer(mContext, mPicWeith, mTotalHeight);
		mImageResizer.setLoadingImage(R.drawable.forum_loading_default);
		//mImageResizer.addImageCache((Activity)mContext, IMAGE_CACHE_DIR);
//		mImageResizer.addImageCache(, cacheParams);
	}

	@Override
	public int getCount() {
		if (mListToDisplay == null) {
			return 0;
		}
		return mListToDisplay.size();
	}

	@Override
	public NoteResult getItem(int position) {
		return mListToDisplay.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NoteViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout front = (RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front);
			mInflater.inflate(R.layout.note_list_item, front);
			holder = new NoteViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (NoteViewHolder) convertView.getTag();
		}

		convertView.findViewById(com.aurora.R.id.control_padding).setPadding(0, 0, 0, 0);
		convertView.findViewById(com.aurora.R.id.aurora_listview_divider).setVisibility(View.VISIBLE);

		String content = mListToDisplay.get(position).getContent();
		int numImages = mListToDisplay.get(position).getImage_count();
		int numVideos = mListToDisplay.get(position).getVideo_count();
		int numSounds = mListToDisplay.get(position).getSound_count();

		// Title display
		String noteTitle = buildNoteTitle(content, numImages, numVideos, numSounds);
		// holder.mTitle.setText(noteTitle);

		if (mQueryText != null && mQueryText.length() > 0) {
			if (noteTitle.indexOf(mQueryText) != -1) {
				SpannableStringBuilder titleBuilder = new SpannableStringBuilder(noteTitle);
				titleBuilder.setSpan(
						new ForegroundColorSpan(mContext.getResources().getColor(com.aurora.R.color.aurora_highlighted_color)), 
						noteTitle.indexOf(mQueryText),
						noteTitle.indexOf(mQueryText) + mQueryText.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.title.setText(titleBuilder);
			} else {
				holder.title.setText(noteTitle);
			}

			String noteSearchSummary = buildSearchSummary(mContext, content, noteTitle, mQueryText);
			/*SpannableStringBuilder ssb = new SpannableStringBuilder(noteSearchSummary);
			ssb.setSpan(
					new ForegroundColorSpan(mContext.getResources().getColor(R.color.text_color_highlight)), 
					noteSearchSummary.indexOf(mQueryText),
					noteSearchSummary.indexOf(mQueryText) + mQueryText.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			holder.summary.setText(ssb);*/
			holder.summary.setText(noteSearchSummary, mQueryText);
		} else {
			holder.title.setText(noteTitle);

			String noteSummary = buildNoteSummary(mContext, content, noteTitle);
			holder.summary.setText(noteSummary);
		}

		// Modify Time display
		holder.modifyTime.setText(TimeUtils.getDataTimeFromLongOth(mListToDisplay.get(position).getUpdate_time()));

		// Alert setting
		if (mListToDisplay.get(position).getIs_warn() == 1) {
			// if has alert, alert icon show.
			holder.alertIcon.setVisibility(View.VISIBLE);
			Date now = new Date();
			if (now.getTime() < mListToDisplay.get(position).getWarn_time()) {
				holder.alertIcon.setImageResource(R.drawable.ic_note_main_alert);
			} else {
				holder.alertIcon.setImageResource(R.drawable.ic_note_main_alert_expired);
			}
		} else {
			holder.alertIcon.setVisibility(View.GONE);
		}

		if (numImages > 0 && numVideos > 0) {
			holder.videoIcon.setVisibility(View.VISIBLE);
		} else {
			holder.videoIcon.setVisibility(View.GONE);
		}

		if (numSounds > 0) {
			holder.voiceIcon.setVisibility(View.VISIBLE);
		} else {
			holder.voiceIcon.setVisibility(View.GONE);
		}

		ViewGroup.LayoutParams params = holder.attachmentPhoto.getLayoutParams();
		params.width = mPicWeith;
		params.height = mTotalHeight;
		holder.attachmentPhoto.setLayoutParams(params);

		// mAttachments display
		if (numImages + numVideos > 0) {
			holder.attachments.setVisibility(View.VISIBLE);
			if (numImages > 0) {
				String firstPic = SystemUtils.getAllImageList(content).get(0);
				/*String firstPic = firstPic.substring(7);
				// 开始图片异步加载
				Bitmap bitmap = BitmapUtil.getBitmap(firstPic, 282, 201);
				if(null == bitmap) {
					bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.forum_loading_failed);
				}
				holder.attachmentPhoto.setImageBitmap(bm);
				mImageResizer.loadImage(1+firstPic, holder.attachmentPhoto);*/

				imageLoader.displayImage(firstPic, holder.attachmentPhoto, optionsImage);

				holder.attachmentPhoto.setVisibility(View.VISIBLE);
				holder.attachmentVideo.setVisibility(View.GONE);
			} else {
				String firstCut = SystemUtils.find(content, Globals.ATTACHMENT_VIDEO_PATTERN).get(0);
				firstCut = firstCut.substring(7);
				/*Bitmap bm = BitmapUtil.getVideoThumbnail(firstCut, 282, 201, Images.Thumbnails.MINI_KIND);
				if (null == bm) {
					bm = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.forum_loading_failed);
				}
				holder.attachmentVideoCut.setImageBitmap(bm);*/

				mImageResizer.loadImage(2+firstCut, holder.attachmentVideoCut);

				holder.attachmentPhoto.setVisibility(View.GONE);
				holder.attachmentVideo.setVisibility(View.VISIBLE);
			}
		} else {
			holder.attachments.setVisibility(View.GONE);
		}

		if (mDeleteFlag) {
			resetListItemHeight(convertView);
		}

		return convertView;
	}

/*	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}*/

	static class NoteViewHolder {
		public TextView title;
		public AuroraTextViewSnippet summary;
		public TextView modifyTime;
		public ImageView alertIcon;
		public ImageView videoIcon;
		public ImageView voiceIcon;
		public View attachments;
		public ImageView attachmentPhoto;
		public View attachmentVideo;
		public ImageView attachmentVideoCut;

		public NoteViewHolder(View convertView) {
			this.title = (TextView) convertView.findViewById(R.id.note_title);
			this.summary = (AuroraTextViewSnippet) convertView.findViewById(R.id.note_summary);
			this.modifyTime = (TextView) convertView.findViewById(R.id.note_modify_time);
			this.alertIcon = (ImageView) convertView.findViewById(R.id.note_alert_image);
			this.videoIcon = (ImageView) convertView.findViewById(R.id.note_video_image);
			this.voiceIcon = (ImageView) convertView.findViewById(R.id.note_voice_image);
			this.attachments = convertView.findViewById(R.id.note_attachments);
			this.attachmentPhoto = (ImageView) convertView.findViewById(R.id.note_attachment_photo);
			this.attachmentVideo = convertView.findViewById(R.id.note_attachment_video);
			this.attachmentVideoCut = (ImageView) convertView.findViewById(R.id.note_attachment_video_cut);
		}

	}

	private String buildNoteTitle(String fullText, int pics, int videos, int sounds) {
		String temp = "";
		String[][] object = { new String[] { Globals.ATTACHMENT_ALL_PATTERN, "" } };
		String[] lines = SystemUtils.replace(fullText, object).trim().split("\n");
		if (lines.length > 0) {
			temp = lines[0].trim();
		}
		if (temp.length() > 0) {
			return temp;
		} else {
			if (pics > 0) {
				temp = mContext.getString(R.string.title_num_of_images,
						pics);
			}
			if (videos > 0) {
				temp = temp
						+ mContext.getString(R.string.title_num_of_videos,
								videos);
			}
			if (sounds > 0) {
				temp = temp
						+ mContext.getString(R.string.title_num_of_sounds,
								sounds);
			}
		}
		return temp;
		
	}

    public String buildNoteSummary(Context context, String fullText, String noteTitle) {
        String temp = noteTitle;

        String[][] object = { new String[] { Globals.ATTACHMENT_ALL_PATTERN, "" } };
        String[] lines = SystemUtils.replace(fullText, object).trim().split("\n");

        if (lines.length > 1) {
        	String moreContent = "";
        	int lineCount = 0;
        	for (int i = 1; i < lines.length; i++) {
        		String text = lines[i].trim();
        		if (text.length() > 0) {
        			moreContent += text + "\n";
        			lineCount++;
        		}
        		if (lineCount == 2) break;
        	}
        	if (moreContent.length() > 0) {
        		temp = moreContent;
        	}
        }

        return temp;
    }

    public String buildSearchSummary(Context context, String fullText, String noteTitle, String queryText) {
    	String temp = noteTitle;

        String[][] object = { new String[] { Globals.ATTACHMENT_ALL_PATTERN, "" } };
        String[] lines = SystemUtils.replace(fullText, object).trim().split("\n");

        if (lines.length > 0) {
        	String moreContent = "";
        	int lineCount = 0;
        	for (int i = 0; i < lines.length; i++) {
        		String text = lines[i].trim();
        		if (text.indexOf(queryText) != -1 || lineCount == 1) {
        			moreContent += text + "\n";
        			lineCount++;
        			if (text.length() >= 30) break;
        		}
        		if (lineCount == 2) break;
        	}
        	if (moreContent.length() > 0) {
        		temp = moreContent;
        	}
        }

        return temp;
    }

	public void setAuroraListHasDelete(boolean flag) {
        mDeleteFlag = flag;
    }
	
	private void resetListItemHeight(View convertView) {
		Object convertTag = convertView.getTag();
		if (null == convertTag || !(convertTag instanceof NoteViewHolder)) {
			return;
		}

		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) convertView
				.getLayoutParams();
		ViewGroup.LayoutParams lpFront = (ViewGroup.LayoutParams) convertView
				.findViewById(com.aurora.R.id.aurora_listview_front)
				.getLayoutParams();
		LinearLayout.LayoutParams lpRubbish = (LinearLayout.LayoutParams) convertView
				.findViewById(com.aurora.R.id.aurora_listview_back)
				.getLayoutParams();

		if (null != lp && null != lpFront && null != lpRubbish) {
			lp.height = mTotalHeight + 1;
			lpFront.height = mTotalHeight;
			lpRubbish.height = mTotalHeight;
			convertView.findViewById(com.aurora.R.id.content).setAlpha(255);
		}
	}

}
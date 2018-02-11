package com.aurora.note.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.note.R;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.ui.AuroraTextViewSnippet;
import com.aurora.note.ui.AuroraTextViewSnippet2;
import com.aurora.note.util.BitmapUtil;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.util.imagecache.ImageResizer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.Date;

public class NoteListAdapter2 extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private Context mContext;
	private ArrayList<NoteResult> mListToDisplay = new ArrayList<NoteResult>();

	private DisplayImageOptions optionsImage;
	private ImageResizer mImageResizer;

	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();

	private String mQueryText;

	private int mPicWidth = 0;
	private int mPicHeight = 0;

	private int mPicMinHeight = 0;
	private int mPicMaxHeight = 0;

	public NoteListAdapter2(Context context, ArrayList<NoteResult> list, String queryText) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mListToDisplay = list;
		mQueryText = queryText;

		Resources resources = context.getResources();
		mPicWidth = resources.getDisplayMetrics().widthPixels / 2 - 
				resources.getDimensionPixelOffset(R.dimen.note_list2_padding) - 
				resources.getDimensionPixelOffset(R.dimen.note_list2_padding_left) * 2 - 2;
		mPicHeight = resources.getDimensionPixelOffset(R.dimen.note_list2_item_pic_height);
		mPicMinHeight = resources.getDimensionPixelOffset(R.dimen.note_list2_item_pic_min_height);
		mPicMaxHeight = resources.getDimensionPixelOffset(R.dimen.note_list2_item_pic_max_height);

		optionsImage = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY)
			.showImageOnLoading(R.color.image_loading_default)
			.showImageForEmptyUri(R.color.transparent)
			.showImageOnFail(R.color.transparent)
			//.displayer(new RoundedBitmapDisplayer(10))
			.cacheInMemory(true)
			.bitmapConfig(Config.RGB_565)
			.build();

		mImageResizer = new ImageResizer(mContext, mPicWidth, mPicHeight);
		mImageResizer.setLoadingImage(R.color.video_loading_default);
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
			convertView = mInflater.inflate(R.layout.note_list_item_2, null);
			holder = new NoteViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (NoteViewHolder) convertView.getTag();
		}

		NoteResult noteResult = mListToDisplay.get(position);
		String content = noteResult.getContent();
		int numImages = noteResult.getImage_count();
		int numVideos = noteResult.getVideo_count();
		int numSounds = noteResult.getSound_count();

		// Title display
		String noteTitle = buildNoteTitle(content, numImages, numVideos, numSounds);
		// holder.mTitle.setText(noteTitle);

		if (mQueryText != null && mQueryText.length() > 0) {
			String noteTitleLower = noteTitle.toLowerCase();
			String queryTextLower = mQueryText.toLowerCase();
			if (noteTitleLower.indexOf(queryTextLower) != -1) {
				holder.title.setText(noteTitle, mQueryText);
			} else {
				holder.title.setText(noteTitle);
			}

			String noteSearchSummary = buildSearchSummary(mContext, content, noteTitle, mQueryText);
			holder.summary.setText(noteSearchSummary, mQueryText);
		} else {
			holder.title.setText(noteTitle);

			String noteSummary = buildNoteSummary(mContext, content, noteTitle);
			holder.summary.setText(noteSummary);
		}

		// Modify Time display
		holder.modifyTime.setText(TimeUtils.getDataTimeFromLongOth(mListToDisplay.get(position).getUpdate_time()));

		if (noteResult.getIs_warn() == 0 && (numImages == 0 || numVideos == 0) && numSounds == 0) {
			holder.diriverView.setVisibility(View.GONE);
			holder.spaceView.setVisibility(View.VISIBLE);
			holder.iconsView.setVisibility(View.GONE);
		} else {
			holder.diriverView.setVisibility(View.VISIBLE);
			holder.spaceView.setVisibility(View.GONE);
			holder.iconsView.setVisibility(View.VISIBLE);
		}

		// Alert setting
		if (noteResult.getIs_warn() == 1) {
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

		// mAttachments display
		if (numImages + numVideos > 0) {
			holder.attachments.setVisibility(View.VISIBLE);
			if (numImages > 0) {
				String firstPic = SystemUtils.getAllImageList(content).get(0);

				ViewGroup.LayoutParams params = holder.attachmentPhotoView.getLayoutParams();
				params.width = mPicWidth;
				params.height = mPicHeight;

				if (firstPic != null && firstPic.startsWith(Globals.DRAWABLE_PROTOCOL)) {
					int resourceId = -1;
					if (Globals.PRESET_IMAGE_CHUNJIE.equals(firstPic)) {
						resourceId = R.drawable.image_chunjie;
					} else if (Globals.PRESET_IMAGE_QINGRENJIE.equals(firstPic)) {
						resourceId = R.drawable.image_qingrenjie;
					}

					if (resourceId != -1) {
						firstPic = Globals.DRAWABLE_PROTOCOL + resourceId;
						int[] wh = BitmapUtil.computeWH_3(mContext.getResources(), resourceId);
						if (wh[0] != 0 && wh[1] != 0) {
							params.height = mPicWidth * wh[1] / wh[0];
						}
					} else {
						firstPic = "";
					}
				} else {
					int[] wh = BitmapUtil.computeWH_4(firstPic);
					if (wh[0] != 0 && wh[1] != 0) {
						params.height = mPicWidth * wh[1] / wh[0];
					} else {
						wh = BitmapUtil.computeWH_1(firstPic);
						if (wh[0] != 0 && wh[1] != 0) {
							params.height = mPicWidth * wh[1] / wh[0];
						}
					}
				}

				if (params.height > mPicMaxHeight) {
					params.height = mPicMaxHeight;
				} else if (params.height < mPicMinHeight) {
					params.height = mPicMinHeight;
				}
				holder.attachmentPhotoView.setLayoutParams(params);

				/*String firstPic = firstPic.substring(7);
				// 开始图片异步加载
				Bitmap bitmap = BitmapUtil.getBitmap(firstPic, 282, 201);
				if(null == bitmap) {
					bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.forum_loading_failed);
				}
				holder.attachmentPhoto.setImageBitmap(bitmap);
				mImageResizer.loadImage(1 + firstPic, holder.attachmentPhoto);*/

				// imageLoader.displayImage(firstPic, holder.attachmentPhoto, optionsImage);
				imageLoader.displayImage(firstPic, holder.attachmentPhotoView, optionsImage, true);

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

				mImageResizer.loadImage(2 + firstCut, holder.attachmentVideoView);

				holder.attachmentPhoto.setVisibility(View.GONE);
				holder.attachmentVideo.setVisibility(View.VISIBLE);
			}
		} else {
			holder.attachments.setVisibility(View.GONE);
		}

		return convertView;
	}

	static class NoteViewHolder {
		public AuroraTextViewSnippet2 title;
		public AuroraTextViewSnippet summary;
		public TextView modifyTime;
		public View diriverView;
		public View spaceView;
		public View iconsView;
		public ImageView alertIcon;
		public ImageView videoIcon;
		public ImageView voiceIcon;
		public View attachments;
		public View attachmentPhoto;
		public ImageView attachmentPhotoView;
		public View attachmentVideo;
		public ImageView attachmentVideoView;

		public NoteViewHolder(View convertView) {
			this.title = (AuroraTextViewSnippet2) convertView.findViewById(R.id.note_title);
			this.summary = (AuroraTextViewSnippet) convertView.findViewById(R.id.note_summary);
			this.modifyTime = (TextView) convertView.findViewById(R.id.note_modify_time);
			this.diriverView = (View) convertView.findViewById(R.id.note_diriver);
			this.spaceView = (View) convertView.findViewById(R.id.note_space);
			this.iconsView = (View) convertView.findViewById(R.id.note_images);
			this.alertIcon = (ImageView) convertView.findViewById(R.id.note_alert_image);
			this.videoIcon = (ImageView) convertView.findViewById(R.id.note_video_image);
			this.voiceIcon = (ImageView) convertView.findViewById(R.id.note_voice_image);
			this.attachments = convertView.findViewById(R.id.note_attachments);
			this.attachmentPhoto = convertView.findViewById(R.id.note_attachment_photo);
			this.attachmentPhotoView = (ImageView) convertView.findViewById(R.id.note_attachment_photo_view);
			this.attachmentVideo = convertView.findViewById(R.id.note_attachment_video);
			this.attachmentVideoView = (ImageView) convertView.findViewById(R.id.note_attachment_video_view);
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
				temp = mContext.getString(R.string.title_num_of_images, pics);
			}
			if (videos > 0) {
				temp = temp+ mContext.getString(R.string.title_num_of_videos, videos);
			}
			if (sounds > 0) {
				temp = temp+ mContext.getString(R.string.title_num_of_sounds, sounds);
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
					if (!moreContent.equals("")) {
						moreContent += "\n";
					}
					moreContent += text;
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
			for (int i = 0; i < lines.length; i++) {
				String text = lines[i].trim();
				String textLower = text.toLowerCase();
				if (textLower.indexOf(queryText.toLowerCase()) != -1) {
					moreContent += text;
					break;
				}
			}
			if (moreContent.length() > 0) {
				temp = moreContent;
			}
		}

		return temp;
	}

}
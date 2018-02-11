package com.aurora.note.bean;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author jason 新建备忘数据bean
 */
public class NoteResult implements Parcelable, Serializable {

	private static final long serialVersionUID = 5512682686570647909L;

	// 对应序列ID
	private int id;
	// 是否为预置备忘录
	private int is_preset;
	// 背景图路径
	private String backgroundPath;
	// UUID
	private String uuid;
	// 备忘录内容
	private String content;
	// 备忘录文字内容
	private String character;
	// 图片数量
	private int image_count;
	// 视频数量
	private int video_count;
	// 音频数量
	private int sound_count;
	// 标签1内容
	private String label1;
	// 标签2内容
	private String label2;
	// 是否提醒
	private int is_warn;
	// 提醒时间
	private long warn_time;
	// 创建时间
	private long create_time;
	// 更新时间
	private long update_time;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIs_preset() {
		return is_preset;
	}

	public void setIs_preset(int is_preset) {
		this.is_preset = is_preset;
	}

	public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCharacter() {
		return character;
	}

	public void setCharacter(String character) {
		this.character = character;
	}

	public int getImage_count() {
		return image_count;
	}

	public void setImage_count(int image_count) {
		this.image_count = image_count;
	}

	public int getVideo_count() {
		return video_count;
	}

	public void setVideo_count(int video_count) {
		this.video_count = video_count;
	}

	public int getSound_count() {
		return sound_count;
	}

	public void setSound_count(int sound_count) {
		this.sound_count = sound_count;
	}

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public int getIs_warn() {
		return is_warn;
	}

	public void setIs_warn(int is_warn) {
		this.is_warn = is_warn;
	}

	public long getWarn_time() {
		return warn_time;
	}

	public void setWarn_time(long warn_time) {
		this.warn_time = warn_time;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time) {
		this.update_time = update_time;
	}

	public NoteResult() {
		
	}

	public NoteResult(Parcel in) {
		id = in.readInt();
		is_preset = in.readInt();
		backgroundPath = in.readString();
		uuid = in.readString();

		content = in.readString();
		character = in.readString();
		image_count = in.readInt();
		video_count = in.readInt();
		sound_count = in.readInt();
		label1 = in.readString();
		label2= in.readString();

		is_warn= in.readInt();
		warn_time= in.readLong();
		create_time= in.readLong();
		update_time= in.readLong();
	}

	public  static final Parcelable.Creator<NoteResult> CREATOR = new Creator<NoteResult>() {  
        @Override  
        public NoteResult createFromParcel(Parcel source) {  
        	return new NoteResult(source);
        }

        @Override  
        public NoteResult[] newArray(int size) {  
            return new NoteResult[size];  
        }  
    };

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeInt(this.is_preset);
		dest.writeString(this.backgroundPath);
		dest.writeString(this.uuid);

		dest.writeString(this.content);
		dest.writeString(this.character);
		dest.writeInt(this.image_count);
		dest.writeInt(this.video_count);
		dest.writeInt(this.sound_count);
		dest.writeString(this.label1);
		dest.writeString(this.label2);

		dest.writeInt(this.is_warn);
		dest.writeLong(this.warn_time);
		dest.writeLong(this.create_time);
		dest.writeLong(this.update_time);
	}

}

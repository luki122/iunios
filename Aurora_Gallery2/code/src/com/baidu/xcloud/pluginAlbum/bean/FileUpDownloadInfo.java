package com.baidu.xcloud.pluginAlbum.bean;

import java.io.File;
import java.io.IOException;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

/**
 * 该类在调用move或copy方法时作为参数
 */
public class FileUpDownloadInfo extends FileFromToInfo implements Parcelable {

    public final static int TYPE_UPLOAD = 0;
    public final static int TYPE_DOWNLOAD = 1;

    /**
     * 文件描述符，多进程访问时使用
     */
    private ParcelFileDescriptor targetFD = null;

    /**
     * 类型（上传或下载）
     */
    private int type;

    /**
     * 上传文件时，出现同名时的策略 ： 服务端同名文件时，是报错、重命名还是覆盖。 "overwrite" 表示覆盖原有文件; "newcopy" 对文件进行重命名，命名规则是"文件名_日期.后缀";"other"
     * 服务端会报错，提示文件已存在 .
     */
    public String uploadSameFilePolicy;

    private boolean hasPFD = false;

    /**
     * 值为"overwrite" 表示覆盖同名文件
     */
    public static final String OVER_WRITE = "overWrite";
    /**
     * 值为"rename" 表示重命名成新文件
     */
    public static final String RENAME = "rename";
    /**
     * 服务端会报错，提示文件已存在
     */
    public static final String OTHER = "other";

    /**
     * 对于上传文件，source 是本地文件路径，target 是远端要上传的文件路径； 对于下载文件，source 是远端要下载的文件路径,target是本地文件路径
     * 
     * @param source 源文件路径
     * @param target 目标文件路径
     * @throws IOException
     */
    public FileUpDownloadInfo(String source, String target, int type) throws IOException {
        this(source, target, type, true);
    }

    /**
     * 对于上传文件，source 是本地文件路径，target 是远端要上传的文件路径； 对于下载文件，source 是远端要下载的文件路径,target是本地文件路径
     * 
     * @param source 源文件路径
     * @param target 目标文件路径
     * @throws IOException
     */
    public FileUpDownloadInfo(String source, String target, int type, boolean initFD) throws IOException {
        super(source, target);
        this.type = type;
        if (initFD) {
            initFd();
        }
    }

    /**
     * 文件上传时的构造函数
     * 
     * @param source 源文件
     * @param target 目标文件
     * @param type 类型
     * @param uploadSameFilePolicy 相同文件时的处理策略
     * @throws IOException
     */
    public FileUpDownloadInfo(String source, String target, int type, String uploadSameFilePolicy) throws IOException {
        this(source, target, type);// 可以直接填写默认值：TYPE_UPLOAD。现在不直接填写的原因是如果要处理下载文件名相同的情况。
        this.uploadSameFilePolicy = uploadSameFilePolicy;
    }

    /**
     * 设置文件描述符
     */
    public void setupFileDescriptor() {
        if (targetFD == null) {
            try {
                initFd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化文件描述符
     */
    private void initFd() throws IOException {
        File file = null;
        if (type == TYPE_UPLOAD) {
            file = new File(source);
            targetFD = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } else if (type == TYPE_DOWNLOAD) {
            // 下载时如果文件不存在就创建一个空的文件。
            file = new File(target);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            targetFD = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
        }
        if (targetFD != null) {
            hasPFD = true;
        }
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        type = in.readInt();
        uploadSameFilePolicy = in.readString();
        int has = in.readInt();
        if (has == 1) {
            targetFD = in.readFileDescriptor();
            // setupFileDescriptor();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(type);
        dest.writeString(uploadSameFilePolicy);
        dest.writeInt(hasPFD ? 1 : 0);
        if (hasPFD) {
            // if (targetFD != null)
            // targetFD.writeToParcel(dest, flags);
            dest.writeFileDescriptor(targetFD.getFileDescriptor());
        }

    }

    /**
     * 获取文件描述符
     */
    public ParcelFileDescriptor getTargetFD() {
        return targetFD;
    }

    /**
     * 设置文件描述符
     */
    public void setTargetFD(ParcelFileDescriptor targetFD) {
        this.targetFD = targetFD;
    }

    /**
     * 获取任务类型
     */
    public int getType() {
        return type;
    }

    /**
     * 设置任务类型
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * 设置同名策略
     */
    public void setUploadSameFilePolicy(String sameFilePolicy) {
        this.uploadSameFilePolicy = sameFilePolicy;
    }

    /**
     * 获取同名策略
     */
    public String getUploadSameFilePolicy() {
        return uploadSameFilePolicy;
    }

    public static final Parcelable.Creator<FileUpDownloadInfo> CREATOR = new Parcelable.Creator<FileUpDownloadInfo>() {
        public FileUpDownloadInfo createFromParcel(Parcel in) {
            return new FileUpDownloadInfo(in);
        }

        public FileUpDownloadInfo[] newArray(int size) {
            return new FileUpDownloadInfo[size];
        }
    };

    public FileUpDownloadInfo() {

    }

    private FileUpDownloadInfo(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        return new StringBuffer(1024).append("type:").append(type).append(" source:").append(source).append(" target:")
                .append(target).append(" uploadSameFilePolicy:").append(uploadSameFilePolicy).append(" targetFD:")
                .append(targetFD.toString()).toString();
    }
}

package com.baidu.xcloud.pluginAlbum.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class FileTaskStatusBean extends BundleObject implements Parcelable {

    public static final int TYPE_TASK_UPLOAD = 1;
    public static final int TYPE_TASK_DOWNLOAD = 2;

    /************************************************************
     * 以下为statusType的type
     */
    public static final int STATUS_BEGIN = 1;
    public static final int STATUS_END = 2;
    public static final int STATUS_PROGRESS = 3;
    public static final int STATUS_CANCELD = 4;
    /**********************************************************/

    /**********************************************************
     * 以下为任务处理的statusTaskCode
     */
    /**
     * 等待执行状态
     */
    public static final int STATE_TASK_PENDING = 100;

    /**
     * 正在运行状态
     */
    public static final int STATE_TASK_RUNNING = 101;

    /**
     * 暂停状态
     */
    public static final int STATE_TASK_PAUSE = 102;

    /**
     * 运行结束失败状态
     */
    public static final int STATE_TASK_FAILED = 103;
    /**
     * 创建状态
     */
    public static final int STATE_TASK_CREATE = 104;

    /**
     * 创建失败
     */
    public static final int STATE_TASK_CREATE_FAILED = 105;

    /**
     * 运行结束成功状态
     */
    public static final int STATE_TASK_DONE = 106;

    /**
     * 取消
     */
    public static final int STATE_TASK_CANCELLED = 110;

    /**********************************************************/

    /**
     * 任务处理类型：暂停
     * 
     */
    public static final String PROCESS_TYPE_PAUSE = "PAUSE";
    /**
     * 任务处理类型：继续
     * 
     */
    public static final String PROCESS_TYPE_RESUME = "RESUME";
    /**
     * 任务处理类型：移除
     * 
     */
    public static final String PROCESS_TYPE_REMOVE = "REMOVE";
    /**
     * 任务处理类型：重新开始
     * 
     */
    public static final String PROCESS_TYPE_RESTART = "RESTART";

    /**
     * 任务类型：所有任务
     * 
     */
    public static final String TASK_TYPE_ALL = "ALL";
    /**
     * 任务类型：上传任务
     * 
     */
    public static final String TASK_TYPE_UPLOAD = "UPLOAD";
    /**
     * 任务类型：下载任务
     * 
     */
    public static final String TASK_TYPE_DOWNLOAD = "DOWNLOAD";

    public enum ProcessType {

        /**
         * 暂停
         */
        PAUSE,
        /**
         * 继续
         */
        RESUME,
        /**
         * 重新启动
         */
        RESTART,
        /**
         * 删除
         */
        REMOVE
    }

    public enum TaskType {

        /**
         * 所有任务
         */
        ALL,
        /**
         * 下载任务
         */
        DOWNLOAD,
        /**
         * 上传任务
         */
        UPLOAD
    }

    /** 0 代表 成功 */
    private int errorCode = ErrorCode.Error_DefaultError;

    /** 如果操作失败，则记录一些相关信息 */
    private String message = "";

    /**
     * 任务类型
     */
    private int type;
    /**
     * 任务当前状态
     */
    protected int statusType = STATUS_BEGIN;
    /**
     * 任务当前状态码
     */
    private int statusTaskCode = -1;

    /**
     * 源文件路径
     */
    private String source = "";

    /**
     * 目标文件路径
     */
    private String target = "";

    /**
     * 文件名称
     */
    private String fileName = "";
    /**
     * 任务ID
     */
    private long fileTaskId = -1;
    /**
     * 任务大小
     */
    private long totalSize = -1;
    /**
     * 任务当前完成大小
     */
    private long currentSize = -1;

    public FileTaskStatusBean(String source, String target, int type, long taskId, String fileName, long totalSize,
            long currentSize, int statusType, int statusTaskCode, int errorCode, String message) {
        super();
        this.source = source;
        this.target = target;
        this.type = type;
        this.fileTaskId = taskId;
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.statusType = statusType;
        this.statusTaskCode = statusTaskCode;
        this.errorCode = errorCode;
        this.message = message;
    }

    public FileTaskStatusBean(String source, String target, int type, long taskId, String fileName, long totalSize,
            long currentSize, int statusType, int statusTaskCode) {
        super();
        this.source = source;
        this.target = target;
        this.type = type;
        this.fileTaskId = taskId;
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.statusType = statusType;
        this.statusTaskCode = statusTaskCode;
    }

    public FileTaskStatusBean(String source, String target, int type, long taskId, String fileName, int statusType,
            int statusTaskCode, int errorCode, String message) {
        super();
        this.source = source;
        this.target = target;
        this.type = type;
        this.fileTaskId = taskId;
        this.fileName = fileName;
        this.statusType = statusType;
        this.statusTaskCode = statusTaskCode;
        this.errorCode = errorCode;
        this.message = message;
    }

    public FileTaskStatusBean(long taskId, int statusType, int errorCode, String message) {
        super();
        this.fileTaskId = taskId;
        this.statusType = statusType;
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * 获取任务ID
     */
    public long getFileTaskId() {
        return fileTaskId;
    }

    /**
     * 设置任务ID
     * 
     * @param taskId
     */
    public void setFileTaskId(long taskId) {
        this.fileTaskId = taskId;
    }

    /**
     * 获取文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 获取当前任务大小
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * 设置当前任务大小
     */
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * 获取任务当前完成大小
     */
    public long getCurrentSize() {
        return currentSize;
    }

    /**
     * 设置任务当前完成大小
     */
    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
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
     * 获取任务当前状态
     */
    public int getStatusType() {
        return statusType;
    }

    /**
     * 设置任务当前状态
     */
    public void setStatusType(int statusType) {
        this.statusType = statusType;
    }

    public static final Parcelable.Creator<FileTaskStatusBean> CREATOR = new Parcelable.Creator<FileTaskStatusBean>() {
        @Override
        public FileTaskStatusBean createFromParcel(Parcel in) {
            return new FileTaskStatusBean(in);
        }

        @Override
        public FileTaskStatusBean[] newArray(int size) {
            return new FileTaskStatusBean[size];
        }
    };

    public FileTaskStatusBean() {
    }

    private FileTaskStatusBean(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        message = in.readString();
        source = in.readString();
        target = in.readString();
        fileName = in.readString();
        fileTaskId = in.readLong();
        totalSize = in.readLong();
        currentSize = in.readLong();
        type = in.readInt();
        errorCode = in.readInt();
        statusType = in.readInt();
        statusTaskCode = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(message);
        dest.writeString(source);
        dest.writeString(target);
        dest.writeString(fileName);
        dest.writeLong(fileTaskId);
        dest.writeLong(totalSize);
        dest.writeLong(currentSize);
        dest.writeInt(type);
        dest.writeInt(errorCode);
        dest.writeInt(statusType);
        dest.writeInt(statusTaskCode);
    }

    /**
     * 获取错误码
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误码
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取任务信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置任务信息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取源文件路径
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置源文件路径
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取目标文件路径
     */
    public String getTarget() {
        return target;
    }

    /**
     * 设置目标文件路径
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * 获取任务当前状态码
     */
    public int getStatusTaskCode() {
        return statusTaskCode;
    }

    /**
     * 设置任务当前状态码
     */
    public void setStatusTaskCode(int statusTaskCode) {
        this.statusTaskCode = statusTaskCode;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(512);
        sb.append(super.toString());
        sb.append("message:" + message);
        sb.append(" source:" + source);
        sb.append(" target:" + target);
        sb.append(" fileName:" + fileName);
        sb.append(" fileTaskId:" + fileTaskId);
        sb.append(" totalSize:" + totalSize);
        sb.append(" currentSize:" + currentSize);
        sb.append(" type:" + type);
        sb.append(" errorCode:" + errorCode);
        sb.append(" statusType:" + statusType);
        sb.append(" statusTaskCode:" + statusTaskCode);
        return sb.toString();
    }
    
  //wenyongzhe 2015.8.31 auroraStartCheckBoxAppearingAnim
    private Boolean isCheck = false;
	public Boolean getIsCheck() {
		return isCheck;
	}
	public void setIsCheck(Boolean isCheck) {
		this.isCheck = isCheck;
	}
    
}
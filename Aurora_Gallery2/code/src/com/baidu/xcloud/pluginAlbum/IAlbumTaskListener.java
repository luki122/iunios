package com.baidu.xcloud.pluginAlbum;

import java.util.List;

import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public interface IAlbumTaskListener {

    /**
     * 回调文件任务的状态更新消息（开始、进度、结束）
     * 
     * @param task 文件任务
     * @param errorCode Code
     * @param msg 消息
     */
    public void onGetTaskStatus(FileTaskStatusBean fileTaskStatusBean);

    /**
     * 返回更新进度的间隔时间。 默认间隔为2000ms, 允许的最小时间间隔是100ms。
     * 
     * @return
     */
    public long progressInterval();

    public void onGetTaskListFinished(List<FileTaskStatusBean> fileTaskStatusBeanList);

}

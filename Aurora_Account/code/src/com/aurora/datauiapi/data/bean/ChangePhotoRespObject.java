package com.aurora.datauiapi.data.bean;

/**
 * 修改图像接口返回response的包装对象
 * @author JimXia
 *
 * @date 2014年10月16日 下午3:50:35
 */
public class ChangePhotoRespObject extends BaseResponseObject{
    private String photo;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
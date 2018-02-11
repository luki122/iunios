package com.aurora.datauiapi.data.bean;

public class CommentInfoHolder extends BaseResponseObject {
    private CommentObject data;

    public CommentObject getData() {
        return data;
    }

    public void setData(CommentObject data) {
        this.data = data;
    }
}

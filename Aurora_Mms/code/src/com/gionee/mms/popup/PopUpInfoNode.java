/*
 *
 * Copyright (C) 2012 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.popup;


public class PopUpInfoNode {
    private int index;
    private String number;
    private String name;
    private int simId;
    private String date;
    private String body;
    private String area;
    private int type;
    private String msgUri;
    private PopUpInfoNode previousNode;
    private PopUpInfoNode nextNode;
    private int threadId;
    private String responseStr;
    // Gionee fangbin 20120517 added for CR00596563 start
    private boolean mIsRead = false;
    // Gionee fangbin 20120517 added for CR00596563 end
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSimId() {
        return simId;
    }

    public void setSimId(int simId) {
        this.simId = simId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsgUri() {
        return msgUri;
    }

    public void setMsgUri(String msgUri) {
        this.msgUri = msgUri;
    }

    public PopUpInfoNode getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(PopUpInfoNode previousNode) {
        this.previousNode = previousNode;
    }

    public PopUpInfoNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(PopUpInfoNode nextNode) {
        this.nextNode = nextNode;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getResponseStr() {
        return responseStr;
    }

    public void setResponseStr(String responseStr) {
        this.responseStr = responseStr;
    }

    // Gionee fangbin 20120517 added for CR00596563 start
    public boolean ismIsRead() {
        return mIsRead;
    }
    
    public void setmIsRead(boolean mIsRead) {
        this.mIsRead = mIsRead;
    }
    // Gionee fangbin 20120517 added for CR00596563 end
}

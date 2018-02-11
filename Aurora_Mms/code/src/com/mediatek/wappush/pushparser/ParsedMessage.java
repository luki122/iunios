
package com.mediatek.wappush.pushparser;

public abstract class ParsedMessage {
    private String ml;

    private String mm;

    private String mn;

    private int simId;

    public ParsedMessage(String paramString) {
        this.ml = paramString;
    }

    public String type() {
        return this.ml;
    }

    public void setSenderAddr(String paramString) {
        this.mm = paramString;
    }

    public String getSenderAddr() {
        return this.mm;
    }

    public void setServiceCenterAddr(String paramString) {
        this.mn = paramString;
    }

    public String getServiceCenterAddr() {
        return this.mn;
    }

    public void setSimId(int paramInt) {
        this.simId = paramInt;
    }

    public int getSimId() {
        return this.simId;
    }

    public String toString() {
        return null;
    }
}

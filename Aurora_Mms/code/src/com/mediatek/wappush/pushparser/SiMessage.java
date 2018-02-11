
package com.mediatek.wappush.pushparser;

public class SiMessage extends ParsedMessage {
    public static String TYPE = "SI";

    public static final int ACTION_NONE = 0;

    public static final int ACTION_LOW = 1;

    public static final int ACTION_MEDIUM = 2;

    public static final int ACTION_HIGH = 3;

    public static final int ACTION_DELETE = 4;

    public String url;

    public String siid;

    public int action;

    public int create;

    public int expiration;

    public String text;

    public SiMessage(String paramString) {
        super(paramString);
    }

    public String toString() {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("Push Message:" + TYPE + "\n");
        localStringBuilder.append("text:");
        localStringBuilder.append(this.text);
        localStringBuilder.append("\nuri:");
        localStringBuilder.append(this.url);
        localStringBuilder.append("\naction:");
        localStringBuilder.append(this.action);
        localStringBuilder.append("\ncreate:");
        localStringBuilder.append(this.create);
        localStringBuilder.append("\nexpires:");
        localStringBuilder.append(this.expiration);
        localStringBuilder.append("\nid:");
        localStringBuilder.append(this.siid);
        return localStringBuilder.toString();
    }
}

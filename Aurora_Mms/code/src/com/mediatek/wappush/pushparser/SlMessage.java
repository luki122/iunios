
package com.mediatek.wappush.pushparser;

public class SlMessage extends ParsedMessage {
    public static final String TYPE = "SL";

    public static final int ACTION_LOW = 1;

    public static final int ACTION_HIGH = 2;

    public static final int ACTION_CACHE = 3;

    public String url;

    public int action;

    public SlMessage(String paramString) {
        super(paramString);
    }

    public String toString() {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("Push Message:SL\n");
        localStringBuilder.append("\nuri:");
        localStringBuilder.append(this.url);
        localStringBuilder.append("\naction:");
        localStringBuilder.append(this.action);
        return localStringBuilder.toString();
    }
}

package com.mediatek.wappush.pushparser;

import java.util.ArrayList;

public class CoMessage extends ParsedMessage {
    public static String TYPE = "CO";

    public ArrayList<String> objects;

    public ArrayList<String> services;

    public CoMessage(String paramString) {
        super(paramString);
    }

    public String toString() {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("Push Message Type:" + TYPE + "\n");
        localStringBuilder.append("\nobject-uri:");
        localStringBuilder.append(this.objects.toString());
        localStringBuilder.append("\nservice-uri:");
        localStringBuilder.append(this.services.toString());
        return localStringBuilder.toString();
    }
}

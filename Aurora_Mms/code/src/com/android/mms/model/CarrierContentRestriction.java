/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.mms.model;

import java.util.ArrayList;

import android.content.ContentResolver;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.gionee.mms.GnContentType;
import com.aurora.android.mms.ContentType;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.ContentRestrictionException;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.MmsConfig;
import com.android.mms.ResolutionException;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.data.WorkingMessage;

public class CarrierContentRestriction implements ContentRestriction {
    private static final ArrayList<String> sSupportedImageTypes;
    private static final ArrayList<String> sSupportedAudioTypes;
    private static final ArrayList<String> sSupportedVideoTypes;
    // add for vcard
    private static final ArrayList<String> sSupportedTypes;

    static {
        sSupportedImageTypes = ContentType.getImageTypes();
        sSupportedAudioTypes = ContentType.getAudioTypes();
        // Aurora xuyong 2014-04-14 added for bug #4117 start
        sSupportedAudioTypes.add("audio/aac-adts");
        // Aurora xuyong 2014-04-14 added for bug #4117 end
        sSupportedVideoTypes = ContentType.getVideoTypes();
        // add for vcard
        sSupportedTypes      = ContentType.getSupportedTypes();
    }

    public CarrierContentRestriction() {
    }

    public void checkMessageSize(int messageSize, int increaseSize, ContentResolver resolver)
            throws ContentRestrictionException {
        if ( (messageSize < 0) || (increaseSize < 0) ) {
            throw new ContentRestrictionException("Negative message size"
                    + " or increase size");
        }
        int newSize = messageSize + increaseSize;

        if ( (newSize < 0) || (newSize > MmsConfig.getUserSetMmsSizeLimit(true)) ) {
            throw new ExceedMessageSizeException("Exceed message size limitation");
        }
    }

    public void checkResolution(int width, int height) throws ContentRestrictionException {
        if ( (width > MmsConfig.getMaxImageWidth()) || (height > MmsConfig.getMaxImageHeight()) ) {
            throw new ResolutionException("content resolution exceeds restriction.");
        }
    }

    public void checkImageContentType(String contentType)
            throws ContentRestrictionException {
        if (null == contentType) {
            throw new ContentRestrictionException("Null content type to be check");
        }

        checkRestrictedContentType(contentType);
        if (!sSupportedImageTypes.contains(contentType)) {
            throw new UnsupportContentTypeException("Unsupported image content type : "
                    + contentType);
        }
    }

    public void checkAudioContentType(String contentType)
            throws ContentRestrictionException {
        if (null == contentType) {
            throw new ContentRestrictionException("Null content type to be check");
        }

        checkRestrictedContentType(contentType);
        if (!sSupportedAudioTypes.contains(contentType)) {
            throw new UnsupportContentTypeException("Unsupported audio content type : "
                    + contentType);
        }
    }

    public void checkVideoContentType(String contentType)
            throws ContentRestrictionException {
        if (null == contentType) {
            throw new ContentRestrictionException("Null content type to be check");
        }

        checkRestrictedContentType(contentType);
        
        if (!sSupportedVideoTypes.contains(contentType)) {
            throw new UnsupportContentTypeException("Unsupported video content type : "
                    + contentType);
        }
    }

    // add for vcard
    public void checkFileAttachmentContentType(String contentType) throws ContentRestrictionException {
        if (null == contentType) {
            throw new ContentRestrictionException("Null content type to be check");
        }

        if (!sSupportedTypes.contains(contentType)) {
            throw new UnsupportContentTypeException("Unsupported content type : " + contentType);
        }
    }

    private void checkRestrictedContentType(String contentType)
    throws ContentRestrictionException{
        if (WorkingMessage.sCreationMode != 0 && !GnContentType.isRestrictedType(contentType)){
            throw new ContentRestrictionException("Restricted content type:" + contentType);
        }
        
    }
}

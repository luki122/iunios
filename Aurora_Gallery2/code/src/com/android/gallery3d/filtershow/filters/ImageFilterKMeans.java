/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.gallery3d.filtershow.filters;

import android.graphics.Bitmap;
import android.text.format.Time;

import com.android.gallery3d.R;

public class ImageFilterKMeans extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "KMEANS";
    private int mSeed = 0;

    public ImageFilterKMeans() {
        mName = "KMeans";

        // set random seed for session
        Time t = new Time();
        t.setToNow();
        mSeed = (int) t.toMillis(false);
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("KMeans");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterKMeans.class);
        representation.setMaximum(20);
        representation.setMinimum(2);
        representation.setValue(4);
        representation.setDefaultValue(4);
        representation.setPreviewValue(4);
        representation.setTextId(R.string.kmeans);
        representation.setSupportsPartialRendering(true);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int width, int height,
            Bitmap large_ds_bm, int lwidth, int lheight, Bitmap small_ds_bm,
            int swidth, int sheight, int p, int seed);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
		return bitmap;

    }
}

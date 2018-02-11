package com.android.gallery3d.filtershow.filters;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.gallery3d.R;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.gallery3d.filtershow.auroraeffects.ImageProcessor;
import com.android.gallery3d.filtershow.editors.ImageOnlyEditor;
import com.android.gallery3d.util.GalleryUtils;

public class ImageFilterAuroraEffectJapanBackLight extends ImageFilter {
	
	private static final String SERIALIZATION_NAME = "AURORA_EFFECT_JAPAN_BACK_LIGHT";
    private static final String TAG = "ImageFilterAuroraEffects";

	public ImageFilterAuroraEffectJapanBackLight() {
		mName = "AuroraEffectJapanBackLight";
	}
	
	public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterDirectRepresentation("AuroraEffectJapanBackLight");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterAuroraEffectJapanBackLight.class);
        representation.setFilterType(FilterRepresentation.TYPE_AURORA_EFFECTS);
        representation.setTextId(R.string.aurora_effects);
        representation.setShowParameterValue(false);
        representation.setEditorId(ImageOnlyEditor.ID);
        representation.setSupportsPartialRendering(true);
        representation.setIsBooleanFilter(true);
        return representation;
    }
	
	@Override
	public void useRepresentation(FilterRepresentation representation) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
		ImageProcessor.generate(bitmap, ImageProcessor.MEID_JAPANBACKLIGHT);
		
		//SQF TEST BEGIN
		//GalleryUtils.writeBitmapToFile(bitmap, "/mnt/sdcard/feng666.jpg");
        //SQF TEST END
		
		
		return bitmap;
	}
}

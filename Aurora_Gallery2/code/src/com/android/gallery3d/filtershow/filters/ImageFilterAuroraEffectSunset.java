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
import com.android.gallery3d.util.MyLog;

public class ImageFilterAuroraEffectSunset extends ImageFilter {
	
	private static final String SERIALIZATION_NAME = "AURORA_EFFECT_SUNSET";
    private static final String TAG = "ImageFilterAuroraEffects";

	public ImageFilterAuroraEffectSunset() {
		mName = "AuroraEffectSunset";
	}
	
	public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterDirectRepresentation("AuroraEffectSunset");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterAuroraEffectSunset.class);
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
		
		//MyLog.i2("SQF_LOG", "ImageFilterAuroraEffectSunset::apply");
		
		ImageProcessor.generate(bitmap, ImageProcessor.MEID_SUNSET);
		if(bitmap.hasAlpha()){
			//Log.i("SQF_LOG", "ImageFilterAuroraEffectSunset::apply  hasAlpha......");
			bitmap.setHasAlpha(false);
		}
		//SQF TEST BEGIN
		/*
		GalleryUtils.writeBitmapToFile(bitmap, "/mnt/sdcard/feng111.jpg");
        */
		//SQF TEST END
		
		return bitmap;
	}
}

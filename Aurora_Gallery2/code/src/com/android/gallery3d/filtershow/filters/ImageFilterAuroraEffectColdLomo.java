package com.android.gallery3d.filtershow.filters;


import com.android.gallery3d.R;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.gallery3d.filtershow.auroraeffects.ImageProcessor;
import com.android.gallery3d.filtershow.editors.ImageOnlyEditor;

public class ImageFilterAuroraEffectColdLomo extends ImageFilter {
	
	private static final String SERIALIZATION_NAME = "AURORA_EFFECT_COLD_LOMO";
    private static final String TAG = "ImageFilterAuroraEffects";

	public ImageFilterAuroraEffectColdLomo() {
		mName = "AuroraEffectColdLomo";
	}
	
	public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterDirectRepresentation("AuroraEffectColdLomo");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterAuroraEffectColdLomo.class);
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
		ImageProcessor.generate(bitmap, ImageProcessor.MEID_COLDLOMO);
		return bitmap;
	}
}

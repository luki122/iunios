package com.android.gallery3d.filtershow.filters;


import com.android.gallery3d.R;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.gallery3d.filtershow.auroraeffects.ImageProcessor;
import com.android.gallery3d.filtershow.editors.ImageOnlyEditor;

public class ImageFilterAuroraEffectReversal extends ImageFilter {
	
	private static final String SERIALIZATION_NAME = "AURORA_EFFECT_REVERSAL";
    private static final String TAG = "ImageFilterAuroraEffects";

	public ImageFilterAuroraEffectReversal() {
		mName = "AuroraEffectReversal";
	}
	
	public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterDirectRepresentation("AuroraEffectReversal");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterAuroraEffectReversal.class);
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
		//
		//Bitmap dest = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
		//return dest;
		ImageProcessor.generate(bitmap, ImageProcessor.MEID_REVERSAL);//ImageProcessor.MEID_CRAYON
		return bitmap;
		//return bitmap;
	}
}

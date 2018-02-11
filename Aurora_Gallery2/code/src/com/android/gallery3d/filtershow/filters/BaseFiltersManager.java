/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.auroraeffects.ImageProcessor;
import com.android.gallery3d.filtershow.pipeline.ImagePreset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public abstract class BaseFiltersManager implements FiltersManagerInterface {
    protected HashMap<Class, ImageFilter> mFilters = null;
    protected HashMap<String, FilterRepresentation> mRepresentationLookup = null;
    private static final String LOGTAG = "BaseFiltersManager";

    protected ArrayList<FilterRepresentation> mLooks = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mBorders = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mTools = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mEffects = new ArrayList<FilterRepresentation>();//aurora filter effects

    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    protected ArrayList<FilterRepresentation> mAuroraRepresentations = new ArrayList<FilterRepresentation>();//just rotate and crop
    protected ArrayList<FilterRepresentation> mBeautyShotsRepresentations = new ArrayList<FilterRepresentation>();
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    private static int mImageBorderSize = 4; // in percent

    protected void init() {
        mFilters = new HashMap<Class, ImageFilter>();
        mRepresentationLookup = new HashMap<String, FilterRepresentation>();
        Vector<Class> filters = new Vector<Class>();
        addFilterClasses(filters);
        for (Class filterClass : filters) {
            try {
                Object filterInstance = filterClass.newInstance();
                if (filterInstance instanceof ImageFilter) {
                    mFilters.put(filterClass, (ImageFilter) filterInstance);

                    FilterRepresentation rep =
                        ((ImageFilter) filterInstance).getDefaultRepresentation();
                    if (rep != null) {
                        addRepresentation(rep);
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRepresentation(FilterRepresentation rep) {
        mRepresentationLookup.put(rep.getSerializationName(), rep);
    }

    public FilterRepresentation createFilterFromName(String name) {
        try {
            return mRepresentationLookup.get(name).copy();
        } catch (Exception e) {
            Log.v(LOGTAG, "unable to generate a filter representation for \"" + name + "\"");
            e.printStackTrace();
        }
        return null;
    }

    public ImageFilter getFilter(Class c) {
        return mFilters.get(c);
    }

    @Override
    public ImageFilter getFilterForRepresentation(FilterRepresentation representation) {
        return mFilters.get(representation.getFilterClass());
    }

    public FilterRepresentation getRepresentation(Class c) {
        ImageFilter filter = mFilters.get(c);
        if (filter != null) {
            return filter.getDefaultRepresentation();
        }
        return null;
    }

    public void freeFilterResources(ImagePreset preset) {
        if (preset == null) {
            return;
        }
        Vector<ImageFilter> usedFilters = preset.getUsedFilters(this);
        for (Class c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (!usedFilters.contains(filter)) {
                filter.freeResources();
            }
        }
    }

    public void freeRSFilterScripts() {
        for (Class c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (filter != null && filter instanceof ImageFilterRS) {
                ((ImageFilterRS) filter).resetScripts();
            }
        }
    }

    protected void addFilterClasses(Vector<Class> filters) {
    	
        //Aurora <SQF> <2014-09-16>  for NEW_UI begin
    	filters.add(ImageFilterAuroraEffectNormal.class);
    	filters.add(ImageFilterAuroraEffectReversal.class);
    	filters.add(ImageFilterAuroraEffectCrayon.class);
    	filters.add(ImageFilterAuroraEffectFleetingTime.class);
    	filters.add(ImageFilterAuroraEffectCosmetologyBackLight.class);
    	filters.add(ImageFilterAuroraEffectJapanBackLight.class);
    	filters.add(ImageFilterAuroraEffectColdLomo.class);
    	filters.add(ImageFilterAuroraEffectSunset.class);
    	filters.add(ImageFilterAuroraEffectSoftPink.class);
    	
    	filters.add(ImageFilterAuroraEffectWarmLomo.class);
    	filters.add(ImageFilterAuroraEffectReflection.class);
    	filters.add(ImageFilterAuroraEffectLightBeam.class);
    	filters.add(ImageFilterAuroraEffectSnowFlakes.class);
    	
        //Aurora <SQF> <2014-09-16>  for NEW_UI end
    	
    	filters.add(ImageFilterTinyPlanet.class);
        filters.add(ImageFilterRedEye.class);
        filters.add(ImageFilterWBalance.class);
        filters.add(ImageFilterExposure.class);
        filters.add(ImageFilterVignette.class);
        filters.add(ImageFilterGrad.class);
        filters.add(ImageFilterContrast.class);
        filters.add(ImageFilterShadows.class);
        filters.add(ImageFilterHighlights.class);
        filters.add(ImageFilterVibrance.class);
        filters.add(ImageFilterSharpen.class);
        filters.add(ImageFilterCurves.class);
        filters.add(ImageFilterDraw.class);
        filters.add(ImageFilterHue.class);
        filters.add(ImageFilterChanSat.class);
        filters.add(ImageFilterSaturated.class);
        filters.add(ImageFilterBwFilter.class);
        filters.add(ImageFilterNegative.class);
        filters.add(ImageFilterEdge.class);
        filters.add(ImageFilterKMeans.class);
        filters.add(ImageFilterFx.class);
        filters.add(ImageFilterBorder.class);
        filters.add(ImageFilterColorBorder.class);
    }

    public ArrayList<FilterRepresentation> getLooks() {
        return mLooks;
    }

    public ArrayList<FilterRepresentation> getBorders() {
        return mBorders;
    }

    public ArrayList<FilterRepresentation> getTools() {
        return mTools;
    }

    public ArrayList<FilterRepresentation> getEffects() {
        return mEffects;
    }
    
    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    public ArrayList<FilterRepresentation> getAuroraRepresentations() {
    	return mAuroraRepresentations;
    }
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    //Aurora <SQF> <2014-12-25>  for NEW_UI begin
    public ArrayList<FilterRepresentation> getBeautyShotRepresentations() {
    	return mBeautyShotsRepresentations;
    }
    //Aurora <SQF> <2014-12-25>  for NEW_UI end

    public void addBorders(Context context) {

        // Do not localize
        String[] serializationNames = {
                "FRAME_4X5",
                "FRAME_BRUSH",
                "FRAME_GRUNGE",
                "FRAME_SUMI_E",
                "FRAME_TAPE",
                "FRAME_BLACK",
                "FRAME_BLACK_ROUNDED",
                "FRAME_WHITE",
                "FRAME_WHITE_ROUNDED",
                "FRAME_CREAM",
                "FRAME_CREAM_ROUNDED"
        };
        
        //Aurora <SQF> <2014-07-04>  for NEW_UI begin
    	if(mBorders != null) {
    		mBorders.clear();
    	}
        //Aurora <SQF> <2014-07-04>  for NEW_UI end

        // The "no border" implementation
        int i = 0;
        FilterRepresentation rep = new FilterImageBorderRepresentation(0);
        mBorders.add(rep);

        // Regular borders
        ArrayList <FilterRepresentation> borderList = new ArrayList<FilterRepresentation>();


        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_4x5);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_brush);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_grunge);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_sumi_e);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_tape);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.BLACK, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.BLACK, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.WHITE, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.WHITE, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        int creamColor = Color.argb(255, 237, 237, 227);
        rep = new FilterColorBorderRepresentation(creamColor, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(creamColor, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        for (FilterRepresentation filter : borderList) {
            filter.setSerializationName(serializationNames[i++]);
            addRepresentation(filter);
            mBorders.add(filter);
        }

    }

    public void addLooks(Context context) {
        int[] drawid = {
                R.drawable.filtershow_fx_0005_punch,
                R.drawable.filtershow_fx_0000_vintage,
                R.drawable.filtershow_fx_0004_bw_contrast,
                R.drawable.filtershow_fx_0002_bleach,
                R.drawable.filtershow_fx_0001_instant,
                R.drawable.filtershow_fx_0007_washout,
                R.drawable.filtershow_fx_0003_blue_crush,
                R.drawable.filtershow_fx_0008_washout_color,
                R.drawable.filtershow_fx_0006_x_process
        };

        int[] fxNameid = {
                R.string.ffx_punch,
                R.string.ffx_vintage,
                R.string.ffx_bw_contrast,
                R.string.ffx_bleach,
                R.string.ffx_instant,
                R.string.ffx_washout,
                R.string.ffx_blue_crush,
                R.string.ffx_washout_color,
                R.string.ffx_x_process
        };

        // Do not localize.
        String[] serializationNames = {
                "LUT3D_PUNCH",
                "LUT3D_VINTAGE",
                "LUT3D_BW",
                "LUT3D_BLEACH",
                "LUT3D_INSTANT",
                "LUT3D_WASHOUT",
                "LUT3D_BLUECRUSH",
                "LUT3D_WASHOUT_COLOR",
                "LUT3D_XPROCESS"
        };
        //Aurora <SQF> <2014-07-04>  for NEW_UI begin
    	if(mLooks != null) {
    		mLooks.clear();
    	}
        //Aurora <SQF> <2014-07-04>  for NEW_UI end

        FilterFxRepresentation nullFx =
                new FilterFxRepresentation(context.getString(R.string.none),
                        0, R.string.none);
        mLooks.add(nullFx);

        for (int i = 0; i < drawid.length; i++) {
            FilterFxRepresentation fx = new FilterFxRepresentation(
                    context.getString(fxNameid[i]), drawid[i], fxNameid[i]);
            fx.setSerializationName(serializationNames[i]);
            ImagePreset preset = new ImagePreset();
            preset.addFilter(fx);
            FilterUserPresetRepresentation rep = new FilterUserPresetRepresentation(
                    context.getString(fxNameid[i]), preset, -1);
            mLooks.add(rep);
            addRepresentation(fx);
        }
    }

    public void addEffects() {
        //Aurora <SQF> <2014-07-04>  for NEW_UI begin
    	if(mEffects != null) {
    		mEffects.clear();
    	}
        //Aurora <SQF> <2014-07-04>  for NEW_UI end


        //Aurora <SQF> <2014-09-16>  for NEW_UI begin
        //ORIGINALLY:
        /*
    	mEffects.add(getRepresentation(ImageFilterAuroraEffects.class));
    	mEffects.add(getRepresentation(ImageFilterTinyPlanet.class));
        mEffects.add(getRepresentation(ImageFilterWBalance.class));
        mEffects.add(getRepresentation(ImageFilterExposure.class));
        mEffects.add(getRepresentation(ImageFilterVignette.class));
        mEffects.add(getRepresentation(ImageFilterGrad.class));
        mEffects.add(getRepresentation(ImageFilterContrast.class));
        mEffects.add(getRepresentation(ImageFilterShadows.class));
        mEffects.add(getRepresentation(ImageFilterHighlights.class));
        mEffects.add(getRepresentation(ImageFilterVibrance.class));
        mEffects.add(getRepresentation(ImageFilterSharpen.class));
        mEffects.add(getRepresentation(ImageFilterCurves.class));
        mEffects.add(getRepresentation(ImageFilterHue.class));
        mEffects.add(getRepresentation(ImageFilterChanSat.class));
        mEffects.add(getRepresentation(ImageFilterBwFilter.class));
        mEffects.add(getRepresentation(ImageFilterNegative.class));
        mEffects.add(getRepresentation(ImageFilterEdge.class));
        mEffects.add(getRepresentation(ImageFilterKMeans.class));
        */
        //SQF MODIFIED TO:
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectNormal.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectReversal.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectCrayon.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectFleetingTime.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectCosmetologyBackLight.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectJapanBackLight.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectColdLomo.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectSunset.class));
    	mEffects.add(getRepresentation(ImageFilterAuroraEffectSoftPink.class));

    	//mEffects.add(getRepresentation(ImageFilterAuroraEffectWarmLomo.class));
    	//mEffects.add(getRepresentation(ImageFilterAuroraEffectReflection.class));
    	//mEffects.add(getRepresentation(ImageFilterAuroraEffectLightBeam.class));
    	//mEffects.add(getRepresentation(ImageFilterAuroraEffectSnowFlakes.class));
        //Aurora <SQF> <2014-09-16>  for NEW_UI end
    }

    public void addTools(Context context) {

        int[] textId = {
                R.string.crop,
                //R.string.straighten,	//Aurora <SQF> <2014-6-3>  for annotation
                R.string.rotate,
                //R.string.mirror	//Aurora <SQF> <2014-6-3>  for annotation
        };

        int[] overlayId = {
                R.drawable.filtershow_button_geometry_crop_normal,
                //R.drawable.filtershow_button_geometry_straighten, //Aurora <SQF> <2014-6-3>  for annotation
                R.drawable.filtershow_button_geometry_rotate_normal,
                //R.drawable.filtershow_button_geometry_flip  //Aurora <SQF> <2014-6-3>  for annotation
        };
        
        //Aurora <SQF> <2014-6-4>  for NEW_UI begin
        int[] pressedOverlayId = {
        		R.drawable.filtershow_button_geometry_crop_pressed,
        		R.drawable.filtershow_button_geometry_rotate_pressed,
        };
        //Aurora <SQF> <2014-6-4>  for NEW_UI end

        FilterRepresentation[] geometryFilters = {
                new FilterCropRepresentation(),
                //new FilterStraightenRepresentation(),	//Aurora <SQF> <2014-6-3>  for annotation
                new FilterRotateRepresentation(),
                //new FilterMirrorRepresentation()		//Aurora <SQF> <2014-6-3>  for annotation
        };
        
        //Aurora <SQF> <2014-07-04>  for NEW_UI begin
        if(mTools != null) {
        	mTools.clear();
        }
        //Aurora <SQF> <2014-07-04>  for NEW_UI end
        
        for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = geometryFilters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            //Aurora <SQF> <2014-6-4>  for NEW_UI begin
            geometry.setPressedOverlayId(pressedOverlayId[i]);
            //Aurora <SQF> <2014-6-4>  for NEW_UI end
            geometry.setOverlayOnly(true);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            mTools.add(geometry);
        }

        //mTools.add(getRepresentation(ImageFilterRedEye.class));
        //mTools.add(getRepresentation(ImageFilterDraw.class)); //Aurora <SQF> <2014-6-3>  for annotation
    }
    

    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    public void addAuroraRepresentations(Context context) {
    	if(mAuroraRepresentations != null) {
    		mAuroraRepresentations.clear();
    	}

    	int[] textId = {
                R.string.crop,
                R.string.rotate,
        };

        int[] overlayId = {
                R.drawable.filtershow_button_geometry_crop_normal,
                R.drawable.filtershow_button_geometry_rotate_normal,
        };

        int[] pressedOverlayId = {
        		R.drawable.filtershow_button_geometry_crop_pressed,
        		R.drawable.filtershow_button_geometry_rotate_pressed,
        };

    	FilterRepresentation[] filters = {
                new FilterCropRepresentation(),
                new FilterRotateRepresentation(),
        };
    	
    	for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = filters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            geometry.setPressedOverlayId(pressedOverlayId[i]);
            geometry.setOverlayOnly(true);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            mAuroraRepresentations.add(geometry);
        }
    }
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    //Aurora <SQF> <2014-12-25>  for NEW_UI begin
    public void addBeautyShotRepresentations(Context context) {
    	if(mBeautyShotsRepresentations != null) {
    		mBeautyShotsRepresentations.clear();
    	}

    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectNormal.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectReversal.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectCrayon.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectFleetingTime.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectCosmetologyBackLight.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectJapanBackLight.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectColdLomo.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectSunset.class));
    	mBeautyShotsRepresentations.add(getRepresentation(ImageFilterAuroraEffectSoftPink.class));
    }
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    

    public void removeRepresentation(ArrayList<FilterRepresentation> list,
                                          FilterRepresentation representation) {
        for (int i = 0; i < list.size(); i++) {
            FilterRepresentation r = list.get(i);
            if (r.getFilterClass() == representation.getFilterClass()) {
                list.remove(i);
                break;
            }
        }
    }

    public void setFilterResources(Resources resources) {
        ImageFilterBorder filterBorder = (ImageFilterBorder) getFilter(ImageFilterBorder.class);
        filterBorder.setResources(resources);
        ImageFilterFx filterFx = (ImageFilterFx) getFilter(ImageFilterFx.class);
        filterFx.setResources(resources);
    }
}

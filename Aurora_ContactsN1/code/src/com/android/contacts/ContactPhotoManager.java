/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts;

import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.util.MemoryUtils;
import com.android.contacts.util.UriUtils;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;

import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import android.graphics.PorterDuff.Mode;

/**
 * Asynchronously loads contact photos and maintains a cache of photos.
 */
public abstract class ContactPhotoManager implements ComponentCallbacks2 {
    static final String TAG = "ContactPhotoManager";
    static final boolean DEBUG = false; // Don't submit with true

    public static final String CONTACT_PHOTO_SERVICE = "contactPhotos";
    
    //Gionee:huangzy 20120823 add for CR00678496 start
    public static final int DEFAULT_UNKOWN_CONTACT_PHOTO = -11;
    public static final int DEFAULT_HOT_LINES_PHOTO = -13;
    //Gionee:huangzy 20120823 add for CR00678496 end

    public static int getDefaultAvatarResId(boolean hires, boolean darkTheme) {
        if (hires && darkTheme)
            return R.drawable.ic_contact_picture_180_holo_dark;
        if (hires)
            return R.drawable.ic_contact_picture_180_holo_light;
        if (darkTheme)
            return R.drawable.ic_contact_picture_holo_dark;
        return R.drawable.aurora_ic_contact_picture;
    }

    public static abstract class DefaultImageProvider {
        public abstract void applyDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase);
        // The following lines are provided and maintained by Mediatek inc.
        public abstract void applySimDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase);
        public abstract void applyUsimDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase);
        public abstract void applyCard1DefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase);
        public abstract void applyCard2DefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase);
        // The following lines are provided and maintained by Mediatek inc.
        
        //Gionee:huangzy 20120823 add for CR00678496 start
        public abstract void applyUnkonwContactDefaultImage(ImageView view, boolean hires, boolean darkTheme);
        //Gionee:huangzy 20120823 add for CR00678496 end
        
        // gionee xuhz 20121121 add for GIUI2.0 start
        public abstract void applyAddStarredDefaultImage(ImageView view, boolean hires, boolean darkTheme);
        // gionee xuhz 20121121 add for GIUI2.0 end

        //Gionee:huangzy 20130131 add for CR00770449 start
        public boolean applyColorNameImage(ImageView view, boolean hires, boolean darkTheme,
        		boolean puase, int simIndex) {
        	if (!puase) {
        		if (ResConstant.isContactPhotoNameSupport()) {
            		if (setContactNameAsPhoto(view, hires, darkTheme, simIndex)) {
            			return true;
            		}
            	}
        	}
        	
        	if (ResConstant.isContactPhotoColorSupport()) {
        		if (setColorfulDefaultContactPhoto(view, hires, darkTheme, simIndex)) {
        			return true;
        		}
        	}
        	
        	return false;
        }
        //Gionee:huangzy 20130131 add for CR00770449 end
        
        //Gionee:huangzy 20130131 add for CR00770449 start
        private static final int CONTACT_TOTAL_COLOR = 3;
        private static final int CONTACT_TOTAL_TYPE = 4;
        
        private static BitmapDrawable[] mContactNameDrawable = new BitmapDrawable[16];
        private static float[] mContactNameCoordinate;
        private static Map<String, Bitmap> mNameBitmapCache = new HashMap<String, Bitmap>(33);
        public static boolean setContactNameAsPhoto(ImageView view, boolean hires, boolean darkTheme, int type) {
        	if (null == view || hires || type < -1 || type > 2) {
        		return false;
        	}
        	
        	if (!(view.getTag() instanceof Pair<?, ?>)) {
        		return false;
        	}
        	
        	Pair<Integer, String> pair = (Pair<Integer, String>) view.getTag(); 
        	String name = pair.second;
        	int bmIndex = type + 1;
        	
        	if (!TextUtils.isEmpty(name)) {
        		if (name.length() > 1) {
        			name = name.substring(name.length() - 1);
        		}
        		
        		/*if (name.matches("[0-9a-zA-Z]*")) {
        			return false;
        		}*/
        		if (!name.matches("[\\u4e00-\\u9fa5]+")) {
        			return false;
        		}
        		
        		if (ResConstant.isContactPhotoColorSupport()) {
        			int colorIndex = pair.first%CONTACT_TOTAL_COLOR + 1;
        			bmIndex+=(colorIndex * CONTACT_TOTAL_TYPE);
        		}
        		
        		int icRes = 0;
        		BitmapDrawable drawable = mContactNameDrawable[bmIndex];
        		if (null == drawable || drawable.getBitmap().isRecycled()) {
        			switch (bmIndex) {
    				case 0:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_dark :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_light;	
    					break;
    				case 1:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim_dark :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim_light;	
    					break;
    				case 2:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_dark :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim1_light;	
    					break;
    				case 3:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_dark :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim2_light;
    					break;
    					//    					
    				case 4:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_color1 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_color1;
    					break;
    				case 5:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim_color1 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim_color1;
    					break;
    				case 6:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_color1 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim1_color1;
    					break;
    				case 7:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_color1 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim2_color1;
    					break;
    					//
    				case 8:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_color2 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_color2;
    					break;
    				case 9:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim_color2 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim_color2;
    					break;
    				case 10:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_color2 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim1_color2;
    					break;
    				case 11:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_color2 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim2_color2;
    					break;
    					//
    				case 12:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_color3 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_color3;
    					break;
    				case 13:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim_color3 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim_color3;
    					break;
    				case 14:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_color3 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim1_color3;
    					break;
    				case 15:
    					icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_color3 :
    	        			R.drawable.gn_dial_ic_contact_name_picutre_sim2_color3;
    					break;
    				default:
    					return false;
    				}
            		
        			Resources res = view.getContext().getResources();
        			//for CR00786443 begin
        			/*mContactNameDrawable[index] = drawable = new BitmapDrawable(
        					res, BitmapFactory.decodeResource(res, icRes));*/
        			mContactNameDrawable[bmIndex] = drawable = (BitmapDrawable)res.getDrawable(icRes);
        			//for CR00786443 end
        		}
        		
        		
        		if (null != drawable) {
        			Bitmap nameBitmap = null;
        			synchronized(DefaultImageProvider.class) {
        				nameBitmap = mNameBitmapCache.get(name);
        			}
        			if (null == nameBitmap || nameBitmap.isRecycled()) {
        				int width = drawable.getIntrinsicWidth();
                        int height = drawable.getIntrinsicHeight();
                        nameBitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
                        Canvas canvas = new Canvas(nameBitmap);
                        Paint paint = ResConstant.getContactPhotoNamePaint();
                        if (null == mContactNameCoordinate) {
                        	mContactNameCoordinate = new float[2];
                        	FontMetrics fontMetrics = paint.getFontMetrics();
                            float fontHeight = fontMetrics.bottom - fontMetrics.top;
                            mContactNameCoordinate[0] = width>>1;
                            mContactNameCoordinate[1] = height - (height - fontHeight) / 2 - fontMetrics.bottom;                    
                        }
                        
                        canvas.drawText(name, mContactNameCoordinate[0], mContactNameCoordinate[1], paint);
                        mNameBitmapCache.put(name, nameBitmap);
                        canvas.setBitmap(null);
        			}
        			view.setBackground(drawable);
                    view.setImageBitmap(nameBitmap);
                    
                    return true;
        		}
        	}
        	
        	return false;
        }
        
        private static BitmapDrawable[] mContactColorfulDrawable = new BitmapDrawable[12];
        public static boolean setColorfulDefaultContactPhoto(ImageView view, boolean hires, boolean darkTheme, int type) {
        	if (null == view || hires || type < -1 || type > 2) {
        		return false;
        	}
        	
        	if (!(view.getTag() instanceof Pair<?, ?>)) {
        		return false;
        	}
        	
        	if ((view.getTag() instanceof Pair<?, ?>)) {
        		Pair<Integer, String> pair = (Pair<Integer, String>) view.getTag();
        		int colorIndex = pair.first%CONTACT_TOTAL_COLOR;        		
        		int bmIndex = type + 1 + colorIndex*CONTACT_TOTAL_TYPE;
        		
        		int icRes = 0;
        		BitmapDrawable drawable = mContactColorfulDrawable[bmIndex];
        		
        		if (null == drawable || drawable.getBitmap().isRecycled()) {
    	    		switch (type) {
    				case -1:
    					switch (colorIndex) {
    					case 0:
    						icRes = (R.drawable.ic_contact_picture_holo_color1);
    						break;
    					case 1:
    						icRes = (R.drawable.ic_contact_picture_holo_color2);
    						break;
    					case 2:
    						icRes = (R.drawable.ic_contact_picture_holo_color3);
    						break;
    					}
    					break;
    				case 0:
    					switch (colorIndex) {
    					case 0:
    						icRes = (R.drawable.ic_contact_picture_sim_holo_color1);
    						break;
    					case 1:
    						icRes = (R.drawable.ic_contact_picture_sim_holo_color2);
    						break;
    					case 2:
    						icRes = (R.drawable.ic_contact_picture_sim_holo_color3);
    						break;
    					}
    					break;
    				case 1:
    					switch (colorIndex) {
    					case 0:
    						icRes = (R.drawable.ic_contact_picture_sim1_holo_color1);
    						break;
    					case 1:
    						icRes = (R.drawable.ic_contact_picture_sim1_holo_color2);
    						break;
    					case 2:
    						icRes = (R.drawable.ic_contact_picture_sim1_holo_color3);
    						break;
    					}
    					break;
    				case 2:
    					switch (colorIndex) {
    					case 0:
    						icRes = (R.drawable.ic_contact_picture_sim2_holo_color1);
    						break;
    					case 1:
    						icRes = (R.drawable.ic_contact_picture_sim2_holo_color2);
    						break;
    					case 2:
    						icRes = (R.drawable.ic_contact_picture_sim2_holo_color3);
    						break;
    					}
    					break;
    				}
    	    		
    	    		if (0 != icRes) {
    	    			Resources res = view.getContext().getResources();
    	    			//for CR00786443 begin
    	    			/*mContactColorfulDrawable[bmIndex] = drawable = new BitmapDrawable(res,
    	    					BitmapFactory.decodeResource(res, icRes))*/;
    					mContactColorfulDrawable[bmIndex] = drawable = (BitmapDrawable)res.getDrawable(icRes);
    					//for CR00786443 end
    	    		}
        		}
        		
        		if (null != drawable) {
        			view.setImageDrawable(null);
        			view.setBackground(drawable);
        			return true;
        		}
        	}
        	return false;
        }

        public static void onTrimMemory(int level) {
        	Map<String, Bitmap> nameBitmapCache = null;
        	synchronized(DefaultImageProvider.class) {
        		nameBitmapCache = mNameBitmapCache;
            	mNameBitmapCache = new HashMap<String, Bitmap>(30);
        	}
        	
        	/*if (null != nameBitmapCache) {
        		for (Bitmap entry : nameBitmapCache.values()) {
            		entry.recycle();
            	}
            	nameBitmapCache.clear();	
        	}*/
        }
    }

    // The following lines are provided and maintained by Mediatek inc.
    public static int getSimDefaultAvatarResId(boolean hires, boolean darkTheme) {
        if (hires && darkTheme)
            return R.drawable.ic_contact_picture_sim_holo_dark;
        if (hires)
            return R.drawable.ic_contact_picture_sim_contact;
        if (darkTheme)
            return R.drawable.ic_contact_picture_sim_holo_dark;
        return R.drawable.ic_contact_picture_sim_contact;
    }

    public static int getUsimDefaultAvatarResId(boolean hires, boolean darkTheme) {
        if (hires && darkTheme)
            return R.drawable.ic_contact_picture_usim_holo_dark;
        if (hires)
            return R.drawable.ic_contact_picture_usim_contact;
        if (darkTheme)
            return R.drawable.ic_contact_picture_usim_holo_dark;
        return R.drawable.ic_contact_picture_usim_contact;
    }
    
    public static int getCard1DefaultAvatarResId(boolean hires, boolean darkTheme) {
        if (hires && darkTheme)
            return R.drawable.ic_contact_picture_sim1_holo_dark;
        if (hires)
            return R.drawable.ic_contact_picture_sim1_contact;
        if (darkTheme)
            return R.drawable.ic_contact_picture_sim1_holo_dark;
        return R.drawable.ic_contact_picture_sim1_contact;
    }
    
    public static int getCard2DefaultAvatarResId(boolean hires, boolean darkTheme) {
        if (hires && darkTheme)
            return R.drawable.ic_contact_picture_sim2_holo_dark;
        if (hires)
            return R.drawable.ic_contact_picture_sim2_contact;
        if (darkTheme)
            return R.drawable.ic_contact_picture_sim2_holo_dark;
        return R.drawable.ic_contact_picture_sim2_contact;
    }
    // The following lines are provided and maintained by Mediatek inc.
    
    // The following lines are provided and maintained by Mediatek inc.
    protected static Context msApplicationContext;
    private static Drawable msDrawablePhone180HoloDark = null;
    private static Drawable msDrawablePhone180HoloLight = null;
    private static Drawable msDrawablePHoneHoloDark = null;
    private static Drawable msDrawablePhoneHoloLight = null;

    private static Drawable msDrawableSimIcon = null;
    private static Drawable msDrawableSimHoloDarkIcon = null;
    
    private static Drawable msDrawableUsimIcon = null;
    private static Drawable msDrawableUsimHoloDarkIcon = null;
    
    private static Drawable msDrawableCard1Icon = null;
    private static Drawable msDrawableCard1HoloDarkIcon = null;
    
    private static Drawable msDrawableCard2Icon = null;
    private static Drawable msDrawableCard2HoloDarkIcon = null;

    private static Drawable msDefaultIcon;

    public static int msPhotoWidth;
    public static int msPhotoHeight;

    private static Drawable getImageDrawalbe(int res) {
        Drawable drawRet = null;

        if (null != msApplicationContext) {
            //msDrawableUSIMIcon = mContext.getResources().getDrawable(res);
        	//for CR00786443 begin
            /*Bitmap bitmap = BitmapFactory.decodeResource(msApplicationContext.getResources(), res);*/
        	Bitmap bitmap = ((BitmapDrawable)(msApplicationContext.getResources().
        			getDrawable(res))).getBitmap();
            //for CR00786443 end
            if (null != bitmap) {
            	Bitmap tmp = bitmap;
            	bitmap = Bitmap.createScaledBitmap(bitmap, msPhotoWidth, msPhotoHeight, true);
                drawRet = new BitmapDrawable(msApplicationContext.getResources(), bitmap);
                
                //Gionee <huangzy> <2013-06-14> remove for CR00786443 begin
                /*if (!tmp.sameAs(bitmap)) {
                	tmp.recycle();
                }*/
                //Gionee <huangzy> <2013-06-14> remove for CR00786443 end
            }
        }
        return drawRet;
    }


    private static Drawable getImageDrawalbe(int res, boolean hires) {
        Drawable drawRet = null;

        if (null != msApplicationContext) {
            //msDrawableUSIMIcon = mContext.getResources().getDrawable(res);
        	//for CR00786443 begin
            /*Bitmap bitmap = BitmapFactory.decodeResource(msApplicationContext.getResources(), res);*/
        	Bitmap bitmap = ((BitmapDrawable)(msApplicationContext.getResources().
        			getDrawable(res))).getBitmap();
            //for CR00786443 end
            if (null != bitmap) {
            	Bitmap tmp = bitmap;
            	if (!hires) {
            		bitmap = Bitmap.createScaledBitmap(bitmap, msPhotoWidth, msPhotoHeight, true);
            	}
            	drawRet = new BitmapDrawable(msApplicationContext.getResources(), bitmap);
            	
            	//Gionee <huangzy> <2013-06-14> remove for CR00786443 begin
                /*if (!tmp.sameAs(bitmap)) {
                	tmp.recycle();
                }*/
            	//Gionee <huangzy> <2013-06-14> remove for CR00786443 end
            }
        }
        return drawRet;
	}
    
    public static Drawable getDefaulPhoneDrawable(boolean hires, boolean darkTheme) {
        int res = 0;
        if (hires && darkTheme) {
            res  = R.drawable.ic_contact_picture_180_holo_dark;
            if (null == msDrawablePhone180HoloDark) {
                msDrawablePhone180HoloDark = getImageDrawalbe(res, hires);
            }
            return msDrawablePhone180HoloDark;
        } else if (hires) {
            res = R.drawable.ic_contact_picture_180_holo_light;
            if (null == msDrawablePhone180HoloLight) {
                msDrawablePhone180HoloLight = getImageDrawalbe(res, hires);
            }
            return msDrawablePhone180HoloLight;
        } else if (darkTheme) {
            res = R.drawable.ic_contact_picture_holo_dark;
            if (null == msDrawablePHoneHoloDark) {
                msDrawablePHoneHoloDark = getImageDrawalbe(res);
            }
            return msDrawablePHoneHoloDark;
        } else {
            res = R.drawable.aurora_contact_header_example_icon;
            if (null == msDrawablePhoneHoloLight) {
                msDrawablePhoneHoloLight = getImageDrawalbe(res);
            }
            return msDrawablePhoneHoloLight;
        }
    }

	public static Drawable getSimDrawble(boolean hires, boolean darkTheme) {
        int res = 0;
        if (hires && darkTheme) {
            res = R.drawable.ic_contact_picture_sim_holo_dark;
        } else if (hires) {
            res = R.drawable.ic_contact_picture_sim_contact;
        } else if (darkTheme) {
            res = R.drawable.ic_contact_picture_sim_holo_dark;
        } else {
            // aurora <wangth> <2013-12-30> modify for aurora begin
//            res = R.drawable.ic_contact_picture_sim_contact;
            res = R.drawable.aurora_sim_contact;
            // aurora <wangth> <2013-12-30> modify for aurora end
        }

        // aurora <wangth> <2013-12-30> add for aurora begin
        if (R.drawable.aurora_sim_contact == res) {
            if (null == msDrawableSimIcon) {
                msDrawableSimIcon = getImageDrawalbe(res);
            }
            return msDrawableSimIcon;
        }
        // aurora <wangth> <2013-12-30> add for aurora end
        
        if (R.drawable.ic_contact_picture_sim_holo_dark == res) {
            if (null == msDrawableSimHoloDarkIcon) {
                msDrawableSimHoloDarkIcon = getImageDrawalbe(res);
            }
            return msDrawableSimHoloDarkIcon;
        }
        if (R.drawable.ic_contact_picture_sim_contact == res) {
            if (null == msDrawableSimIcon) {
                msDrawableSimIcon = getImageDrawalbe(res);
            }
            return msDrawableSimIcon;
        }
        Log.e(TAG, "Contacts load SIM photo error!!");
        return null;
    }

    public static Drawable getUsimDrawble(boolean hires, boolean darkTheme) {
        int res = 0;
        if (hires && darkTheme) {
            res = R.drawable.ic_contact_picture_usim_holo_dark;
        } else if (hires) {
            res = R.drawable.ic_contact_picture_usim_contact;
        } else if (darkTheme) {
            res = R.drawable.ic_contact_picture_usim_holo_dark;
        } else {
            // aurora <wangth> <2013-12-30> modify for aurora begin
//            res = R.drawable.ic_contact_picture_usim_contact;
            res = R.drawable.aurora_sim_contact;
            // aurora <wangth> <2013-12-30> modify for aurora end
        }
        
        // aurora <wangth> <2013-12-30> add for aurora begin
        if (R.drawable.aurora_sim_contact == res) {
            if (null == msDrawableUsimIcon) {
                msDrawableUsimIcon = getImageDrawalbe(res);
            }
            return msDrawableUsimIcon;
        }
        // aurora <wangth> <2013-12-30> add for aurora end

        if (R.drawable.ic_contact_picture_usim_holo_dark == res) {
            if (null == msDrawableUsimHoloDarkIcon) {
                msDrawableUsimHoloDarkIcon = getImageDrawalbe(res);
            }
            return msDrawableUsimHoloDarkIcon;
        }
        if (R.drawable.ic_contact_picture_usim_contact == res) {
            if (null == msDrawableUsimIcon) {
                msDrawableUsimIcon = getImageDrawalbe(res);
            }
            return msDrawableUsimIcon;
        }
        Log.e(TAG, "Contacts load USIM photo error!!");
        return null;
    }
    public static Drawable getCard1Drawble(boolean hires, boolean darkTheme) {
        int res = 0;
        if (hires && darkTheme) {
            res = R.drawable.ic_contact_picture_sim1_holo_dark;
        } else if (hires) {
            res = R.drawable.ic_contact_picture_sim1_contact;
        } else if (darkTheme) {
            res = R.drawable.ic_contact_picture_sim1_holo_dark;
        } else {
            res = R.drawable.ic_contact_picture_sim1_contact;
        }

        if (R.drawable.ic_contact_picture_sim1_holo_dark == res) {
            if (null == msDrawableCard1HoloDarkIcon) {
                msDrawableCard1HoloDarkIcon = getImageDrawalbe(res);
            }
            return msDrawableCard1HoloDarkIcon;
        }
        if (R.drawable.ic_contact_picture_sim1_contact == res) {
            if (null == msDrawableCard1Icon) {
                msDrawableCard1Icon = getImageDrawalbe(res);
            }
            return msDrawableCard1Icon;
        }
        Log.e(TAG, "Contacts load Card1 photo error!!");
        return null;
    }
    public static Drawable getCard2Drawble(boolean hires, boolean darkTheme) {
        int res = 0;
        if (hires && darkTheme) {
            res = R.drawable.ic_contact_picture_sim2_holo_dark;
        } else if (hires) {
            res = R.drawable.ic_contact_picture_sim2_contact;
        } else if (darkTheme) {
            res = R.drawable.ic_contact_picture_sim2_holo_dark;
        } else {
            res = R.drawable.ic_contact_picture_sim2_contact;
        }

        if (R.drawable.ic_contact_picture_sim2_holo_dark == res) {
            if (null == msDrawableCard2HoloDarkIcon) {
                msDrawableCard2HoloDarkIcon = getImageDrawalbe(res);
            }
            return msDrawableCard2HoloDarkIcon;
        }
        if (R.drawable.ic_contact_picture_sim2_contact == res) {
            if (null == msDrawableCard2Icon) {
                msDrawableCard2Icon = getImageDrawalbe(res);
            }
            return msDrawableCard2Icon;
        }
        Log.e(TAG, "Contacts load Card2 photo error!!");
        return null;
    }

    //  The previous lines are provided and maintained by Mediatek Inc.

    private static class AvatarDefaultImageProvider extends DefaultImageProvider {
        @Override
        public void applyDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
            /*
             * Bug Fix by Mediatek Begin. Original Android¡¯s code: //
             * view.setImageResource(getDefaultAvatarResId(hires, darkTheme));
             * CR ID: Descriptions: Performance issue
             */
        	
            //Gionee:huangzy 20130131 add for CR00770449 start
        	if(applyColorNameImage(view, hires, darkTheme, puase, -1)) {
        		return;
        	}
            //Gionee:huangzy 20130131 add for CR00770449 end
        	
        	view.setImageDrawable(null);
        	view.setBackground(getDefaulPhoneDrawable(hires, darkTheme));
            /*
             * Bug Fix by Mediatek End.
             */
        }
        // The following lines are provided and maintained by Mediatek inc.
        public void applySimDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
        	//Gionee:huangzy 20130131 add for CR00770449 start
        	if(applyColorNameImage(view, hires, darkTheme, puase, 0)) {
        		return;
        	}
            //Gionee:huangzy 20130131 add for CR00770449 end
        	
            // view.setImageResource(getSimDefaultAvatarResId(hires, darkTheme));
            view.setImageDrawable(getSimDrawble(hires, darkTheme));            
        }
        public void applyUsimDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
        	//Gionee:huangzy 20130131 add for CR00770449 start
        	if(applyColorNameImage(view, hires, darkTheme, puase, 0)) {
        		return;
        	}
            //Gionee:huangzy 20130131 add for CR00770449 end
        	
            //view.setImageResource(getUsimDefaultAvatarResId(hires, darkTheme));
            view.setImageDrawable(getUsimDrawble(hires, darkTheme));
        }
        public void applyCard1DefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
            //view.setImageResource(getUsimDefaultAvatarResId(hires, darkTheme));

        	//Gionee:huangzy 20130131 add for CR00770449 start
        	if(applyColorNameImage(view, hires, darkTheme, puase, 1)) {
        		return;
        	}
            //Gionee:huangzy 20130131 add for CR00770449 end
            
            view.setImageDrawable(getCard1Drawble(hires, darkTheme));
        }
        public void applyCard2DefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {

        	//Gionee:huangzy 20130131 add for CR00770449 start
        	if(applyColorNameImage(view, hires, darkTheme, puase, 2)) {
        		return;
        	}
            //Gionee:huangzy 20130131 add for CR00770449 end
        	
            //view.setImageResource(getUsimDefaultAvatarResId(hires, darkTheme));
            view.setImageDrawable(getCard2Drawble(hires, darkTheme));
        }
        // The following lines are provided and maintained by Mediatek inc.

        //Gionee:huangzy 20120823 add for CR00678496 start
        public void applyUnkonwContactDefaultImage(ImageView view, boolean hires, boolean darkTheme) {
        	if (darkTheme) {
            	view.setImageResource(R.drawable.gn_contact_default_unknow_for_dark);
        	} else {
        		// aurora ukiliu 2013-10-15 modify for aurora ui begin
//            	view.setImageResource(R.drawable.gn_contact_default_unknow);
        		//pic for call detail
        		view.setImageResource(R.drawable.aurora_ic_contact_picture);
        		// aurora ukiliu 2013-10-15 modify for aurora ui end
        	}
        }
        //Gionee:huangzy 20120823 add for CR00678496 end
        
        // gionee xuhz 20121121 add for GIUI2.0 start
        public void applyAddStarredDefaultImage(ImageView view, boolean hires, boolean darkTheme) {
  
            	view.setImageResource(R.drawable.gn_contact_default_add_starred);
        	
        }
        // gionee xuhz 20121121 add for GIUI2.0 end
    }

    private static class BlankDefaultImageProvider extends DefaultImageProvider {
        private static Drawable sDrawable;

        @Override
        public void applyDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
            if (sDrawable == null) {
                Context context = view.getContext();
                sDrawable = new ColorDrawable(context.getResources().getColor(
                        R.color.image_placeholder));
            }
            view.setImageDrawable(sDrawable);
        }

        @Override
        public void applySimDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
            if (sDrawable == null) {
                Context context = view.getContext();
                sDrawable = new ColorDrawable(context.getResources().getColor(
                        R.color.image_placeholder));
            }
            view.setImageDrawable(sDrawable);
            
        }

        @Override
        public void applyUsimDefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
            if (sDrawable == null) {
                Context context = view.getContext();
                sDrawable = new ColorDrawable(context.getResources().getColor(
                        R.color.image_placeholder));
            }
            view.setImageDrawable(sDrawable);
            
        }
        
        @Override
        public void applyCard1DefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
            if (sDrawable == null) {
                Context context = view.getContext();
                sDrawable = new ColorDrawable(context.getResources().getColor(
                        R.color.image_placeholder));
            }
            view.setImageDrawable(sDrawable);
            
        }
        
        @Override
        public void applyCard2DefaultImage(ImageView view, boolean hires, boolean darkTheme, boolean puase) {
            if (sDrawable == null) {
                Context context = view.getContext();
                sDrawable = new ColorDrawable(context.getResources().getColor(
                        R.color.image_placeholder));
            }
            view.setImageDrawable(sDrawable);
            
        }
        
        //Gionee:huangzy 20120823 add for CR00678496 start
        public void applyUnkonwContactDefaultImage(ImageView view, boolean hires, boolean darkTheme) {
        	if (darkTheme) {
            	view.setImageResource(R.drawable.gn_contact_default_unknow_for_dark);
        	} else {
            	view.setImageResource(R.drawable.gn_contact_default_unknow);
        	}
        }
        //Gionee:huangzy 20120823 add for CR00678496 end
        
        // gionee xuhz 20121121 add for GIUI2.0 start
        public void applyAddStarredDefaultImage(ImageView view, boolean hires, boolean darkTheme) {
        	view.setImageResource(R.drawable.gn_contact_default_add_starred);
        }
        // gionee xuhz 20121121 add for GIUI2.0 end
    }

    public static final DefaultImageProvider DEFAULT_AVATER = new AvatarDefaultImageProvider();

    public static final DefaultImageProvider DEFAULT_BLANK = new BlankDefaultImageProvider();

    /**
     * Requests the singleton instance of {@link AccountTypeManager} with data
     * bound from the available authenticators. This method can safely be called
     * from the UI thread.
     */
    public static ContactPhotoManager getInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        msApplicationContext = context.getApplicationContext();
        if (0 == msPhotoWidth || 0 == msPhotoHeight) {
            msPhotoWidth = (int) msApplicationContext.getResources().getDimension(
                    R.dimen.aurora_detail_contact_photo_size);
            msPhotoHeight = msPhotoWidth;
        }

        ContactPhotoManager service = (ContactPhotoManager) applicationContext
                .getSystemService(CONTACT_PHOTO_SERVICE);
        if (service == null) {
            service = createContactPhotoManager(applicationContext);
            Log.e(TAG, "No contact photo service in context: " + applicationContext);
        }
        return service;
    }

    public static synchronized ContactPhotoManager createContactPhotoManager(Context context) {
        return new ContactPhotoManagerImpl(context);
    }

    /**
     * Load photo into the supplied image view. If the photo is already cached,
     * it is displayed immediately. Otherwise a request is sent to load the
     * photo from the database.
     */
    public abstract void loadPhoto(ImageView view, long photoId, boolean hires, boolean darkTheme,
            DefaultImageProvider defaultProvider);

    /**
     * Calls
     * {@link #loadPhoto(ImageView, long, boolean, boolean, DefaultImageProvider)}
     * with {@link #DEFAULT_AVATER}.
     */
    public final void loadPhoto(ImageView view, long photoId, boolean hires, boolean darkTheme) {
        loadPhoto(view, photoId, hires, darkTheme, DEFAULT_AVATER);
    }

    /**
     * Load photo into the supplied image view. If the photo is already cached,
     * it is displayed immediately. Otherwise a request is sent to load the
     * photo from the location specified by the URI.
     */
    public abstract void loadPhoto(ImageView view, Uri photoUri, boolean hires, boolean darkTheme,
            DefaultImageProvider defaultProvider);

    /**
     * Calls
     * {@link #loadPhoto(ImageView, Uri, boolean, boolean, DefaultImageProvider)}
     * with {@link #DEFAULT_AVATER}.
     */
    public final void loadPhoto(ImageView view, Uri photoUri, boolean hires, boolean darkTheme) {
        loadPhoto(view, photoUri, hires, darkTheme, DEFAULT_AVATER);
    }

    /**
     * Remove photo from the supplied image view. This also cancels current
     * pending load request inside this photo manager.
     */
    public abstract void removePhoto(ImageView view);

    /**
     * Temporarily stops loading photos from the database.
     */
    public abstract void pause();

    /**
     * Resumes loading photos from the database.
     */
    public abstract void resume();

    /**
     * Marks all cached photos for reloading. We can continue using cache but
     * should also make sure the photos haven't changed in the background and
     * notify the views if so.
     */
    public abstract void refreshCache();

    /**
     * Initiates a background process that over time will fill up cache with
     * preload photos.
     */
    public abstract void preloadPhotosInBackground();

    // ComponentCallbacks2
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    // ComponentCallbacks2
    @Override
    public void onLowMemory() {
    }

    // ComponentCallbacks2
    @Override
    public void onTrimMemory(int level) {
    	DefaultImageProvider.onTrimMemory(level);
    	Log.i("James", "ContactPhotoManager  onTrimMemory(int level)");
    }
    
    public static void setContactPhotoViewTag(ImageView photoView, String name, int position, boolean fouceTagNull) {
		if (null == photoView) {
			return;
		}
		
		if (fouceTagNull) {
			photoView.setTag(null);
			return;
		}
		
		if (ResConstant.isContactPhotoNameSupport()) {
			photoView.setTag(new Pair<Integer, String>(position, name));			
		} else if (ResConstant.isContactPhotoColorSupport()) {
			photoView.setTag(new Pair<Integer, String>(position, null));
		} else {
			photoView.setTag(null);
		}
	}
    //Gionee:huangzy 20130131 add for CR00770449 end
}

class ContactPhotoManagerImpl extends ContactPhotoManager implements Callback {
    private static final String LOADER_THREAD_NAME = "ContactPhotoLoader";

    /**
     * Type of message sent by the UI thread to itself to indicate that some
     * photos need to be loaded.
     */
    private static final int MESSAGE_REQUEST_LOADING = 1;

    /**
     * Type of message sent by the loader thread to indicate that some photos
     * have been loaded.
     */
    private static final int MESSAGE_PHOTOS_LOADED = 2;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String[] COLUMNS = new String[] {
            Photo._ID, Photo.PHOTO
    };

    /**
     * Maintains the state of a particular photo.
     */
    /*
     * New Feature by Mediatek Begin.
     *   Original Android¡¯s code:
         //    private static class BitmapHolder {
         //        final byte[] bytes;
         //
         //        volatile boolean fresh;
         //
         //        Bitmap bitmap;
         //
         //        Reference<Bitmap> bitmapRef;
         //
         //        public BitmapHolder(byte[] bytes) {
         //            this.bytes = bytes;
         //            this.fresh = true;
         //        }
         //    }
     *   CR ID: ALPS00097126
     *   Descriptions: ¡­
     */
    private static class BitmapHolder {
        boolean mbIsUriBitmap;
        final byte[] bytes;

        volatile boolean fresh;

        BitmapDrawable bitmap;

        SoftReference<BitmapDrawable> bitmapRef;

        public BitmapHolder(byte[] bytes) {
            this.bytes = bytes;
            this.fresh = true;
            this.mbIsUriBitmap = false;
        }
        public BitmapHolder(byte[] bytes, boolean bIsUriBitmap) {
            this(bytes);
            this.mbIsUriBitmap = bIsUriBitmap;
        }
    }
    /*
     * New Feature by Mediatek End.
     */

    private final Context mContext;

    /**
     * An LRU cache for bitmap holders. The cache contains bytes for photos just
     * as they come from the database. Each holder has a soft reference to the
     * actual bitmap.
     */
    private final LruCache<Object, BitmapHolder> mBitmapHolderCache;

    /**
     * Cache size threshold at which bitmaps will not be preloaded.
     */
    private final int mBitmapHolderCacheRedZoneBytes;

    /**
     * Level 2 LRU cache for bitmaps. This is a smaller cache that holds the
     * most recently used bitmaps to save time on decoding them from bytes (the
     * bytes are stored in {@link #mBitmapHolderCache}.
     */
    // private final LruCache<Object, Bitmap> mBitmapCache;
    private final LruCache<Object, BitmapDrawable> mBitmapCache;

    /**
     * A map from ImageView to the corresponding photo ID or uri, encapsulated
     * in a request. The request may swapped out before the photo loading
     * request is started.
     */
    private final ConcurrentHashMap<ImageView, Request> mPendingRequests = new ConcurrentHashMap<ImageView, Request>();

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler mMainThreadHandler = new Handler(this);

    /**
     * Thread responsible for loading photos from the database. Created upon the
     * first request.
     */
    private LoaderThread mLoaderThread;

    /**
     * A gate to make sure we only send one instance of MESSAGE_PHOTOS_NEEDED at
     * a time.
     */
    private boolean mLoadingRequested;

    /**
     * Flag indicating if the image loading is paused.
     */
    private boolean mPaused;

    /** Cache size for {@link #mBitmapHolderCache} for devices with "large" RAM. */
    private static final int HOLDER_CACHE_SIZE = 2000000;

    /** Cache size for {@link #mBitmapCache} for devices with "large" RAM. */
    private static final int BITMAP_CACHE_SIZE = 36864 * 48; // 1728K

    private static final int LARGE_RAM_THRESHOLD = 640 * 1024 * 1024;

    /** For debug: How many times we had to reload cached photo for a stale entry */
    private final AtomicInteger mStaleCacheOverwrite = new AtomicInteger();

    /** For debug: How many times we had to reload cached photo for a fresh entry.  Should be 0. */
    private final AtomicInteger mFreshCacheOverwrite = new AtomicInteger();

    public ContactPhotoManagerImpl(Context context) {
        mContext = context;

        final float cacheSizeAdjustment =
                (MemoryUtils.getTotalMemorySize() >= LARGE_RAM_THRESHOLD) ? 1.0f : 0.5f;
        final int bitmapCacheSize = (int) (cacheSizeAdjustment * BITMAP_CACHE_SIZE);
        mBitmapCache = new LruCache<Object, BitmapDrawable>(bitmapCacheSize) {
            @Override protected int sizeOf(Object key, BitmapDrawable value) {
                //Gionee <xuhz> <2013-08-01> add for CR00844946 begin
            	if (value == null) {
            		return 0;
            	}
            	if (value.getBitmap() == null) {
            		return 0;
            	}
                //Gionee <xuhz> <2013-08-01> add for CR00844946 end
                return value.getBitmap().getByteCount();
            }

            @Override protected void entryRemoved(
                    boolean evicted, Object key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                if (DEBUG) dumpStats();
            }
        };
        final int holderCacheSize = (int) (cacheSizeAdjustment * HOLDER_CACHE_SIZE);
        mBitmapHolderCache = new LruCache<Object, BitmapHolder>(holderCacheSize) {
            @Override protected int sizeOf(Object key, BitmapHolder value) {
                return value.bytes != null ? value.bytes.length : 0;
            }

            @Override protected void entryRemoved(
                    boolean evicted, Object key, BitmapHolder oldValue, BitmapHolder newValue) {
                if (DEBUG) dumpStats();
            }
        };
        mBitmapHolderCacheRedZoneBytes = (int) (holderCacheSize * 0.75);
        Log.i(TAG, "Cache adj: " + cacheSizeAdjustment);
        if (DEBUG) {
            Log.d(TAG, "Cache size: " + btk(mBitmapHolderCache.maxSize())
                    + " + " + btk(mBitmapCache.maxSize()));
        }
    }

    /** Converts bytes to K bytes, rounding up.  Used only for debug log. */
    private static String btk(int bytes) {
        return ((bytes + 1023) / 1024) + "K";
    }

    private static final int safeDiv(int dividend, int divisor) {
        return (divisor  == 0) ? 0 : (dividend / divisor);
    }

    /**
     * Dump cache stats on logcat.
     */
    private void dumpStats() {
        if (!DEBUG) return;
        {
            int numHolders = 0;
            int rawBytes = 0;
            int bitmapBytes = 0;
            int numBitmaps = 0;
            for (BitmapHolder h : mBitmapHolderCache.snapshot().values()) {
                numHolders++;
                if (h.bytes != null) {
                    rawBytes += h.bytes.length;
                }
                // Bitmap b = h.bitmapRef != null ? h.bitmapRef.get() : null;
                BitmapDrawable b = h.bitmapRef != null ? h.bitmapRef.get() : null;
                if (b != null) {
                    numBitmaps++;
                    // bitmapBytes += b.getByteCount();
                    bitmapBytes += b.getBitmap().getByteCount();
                }
            }
            Log.d(TAG, "L1: " + btk(rawBytes) + " + " + btk(bitmapBytes) + " = "
                    + btk(rawBytes + bitmapBytes) + ", " + numHolders + " holders, "
                    + numBitmaps + " bitmaps, avg: "
                    + btk(safeDiv(rawBytes, numHolders))
                    + "," + btk(safeDiv(bitmapBytes,numBitmaps)));
            Log.d(TAG, "L1 Stats: " + mBitmapHolderCache.toString()
                    + ", overwrite: fresh=" + mFreshCacheOverwrite.get()
                    + " stale=" + mStaleCacheOverwrite.get());
        }

        {
            int numBitmaps = 0;
            int bitmapBytes = 0;
            // for (Bitmap b : mBitmapCache.snapshot().values()) {
            for (BitmapDrawable b : mBitmapCache.snapshot().values()) {
                numBitmaps++;
                // bitmapBytes += b.getByteCount();
                bitmapBytes += b.getBitmap().getByteCount();
            }
            Log.d(TAG, "L2: " + btk(bitmapBytes) + ", " + numBitmaps + " bitmaps"
                    + ", avg: " + btk(safeDiv(bitmapBytes, numBitmaps)));
            // We don't get from L2 cache, so L2 stats is meaningless.
        }
    }

    @Override
    public void onTrimMemory(int level) {
    	super.onTrimMemory(level);
    	
        if (DEBUG) Log.d(TAG, "onTrimMemory: " + level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            // Clear the caches.  Note all pending requests will be removed too.
            clear();
        }
    }

    @Override
    public void preloadPhotosInBackground() {
        ensureLoaderThread();
        mLoaderThread.requestPreloading();
    }

    @Override
    public void loadPhoto(ImageView view, long photoId, boolean hires, boolean darkTheme,
            DefaultImageProvider defaultProvider) {
        if (photoId == 0) {
            // No photo is needed
            defaultProvider.applyDefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);
        }
        //Gionee:huangzy 20120823 add for CR00678496 start
        else if (DEFAULT_UNKOWN_CONTACT_PHOTO == photoId) {
        	defaultProvider.applyUnkonwContactDefaultImage(view, hires, darkTheme);
        	// gionee xuhz 20120905 add for CR00682522 CR00687248 start
        	mPendingRequests.remove(view);
        	// gionee xuhz 20120905 add for CR00682522 CR00687248 end
        }
        //Gionee:huangzy 20120823 add for CR00678496 end
        // The following lines are provided and maintained by Mediatek inc.
        else if (photoId == -1) {
            defaultProvider.applySimDefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        } else if (photoId == -2) {
            defaultProvider.applyUsimDefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        } else if (photoId == -3) {
            defaultProvider.applyCard1DefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        } else if (photoId == -4) {
            defaultProvider.applyCard2DefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        }
        // The following lines are provided and maintained by Mediatek inc.
        else {
            if (DEBUG) Log.d(TAG, "loadPhoto request: " + photoId);
            loadPhotoByIdOrUri(view, Request.createFromId(photoId, hires, darkTheme,
                    defaultProvider));
        }
    }

    @Override
    public void loadPhoto(ImageView view, Uri photoUri, boolean hires, boolean darkTheme,
            DefaultImageProvider defaultProvider) {
        if (photoUri == null) {
            // No photo is needed
            defaultProvider.applyDefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);
        } 
        // The following lines are provided and maintained by Mediatek inc.
        else if (photoUri.toString().equals("content://sim")) {
            defaultProvider.applySimDefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        } else if (photoUri.toString().equals("content://usim")) {
            defaultProvider.applyUsimDefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        } else if (photoUri.toString().equals("content://slot0")) {
            defaultProvider.applyCard1DefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        } else if (photoUri.toString().equals("content://slot1")) {
            defaultProvider.applyCard2DefaultImage(view, hires, darkTheme, mPaused);
            mPendingRequests.remove(view);

        }
        // The following lines are provided and maintained by Mediatek inc.
        // gionee xuhz 20121121 add for GIUI2.0 start
        else if (photoUri.toString().equals("content://add_starred")) {
            defaultProvider.applyAddStarredDefaultImage(view, hires, darkTheme);
            mPendingRequests.remove(view);

        }
        // gionee xuhz 20121121 add for GIUI2.0 end
        else {
        	loadPhotoByIdOrUri(view, Request.createFromUri(photoUri, hires, darkTheme,
                    defaultProvider));
        }
    }

    private void loadPhotoByIdOrUri(ImageView view, Request request) {
        boolean cached = loadCachedPhoto(view, request);
        if (cached) {
            mPendingRequests.remove(view);
        } else {
            mPendingRequests.put(view, request);
            if (!mPaused) {
                // Send a request to start loading photos
                requestLoading();
            }
        }
    }

    @Override
    public void removePhoto(ImageView view) {
        view.setImageDrawable(null);
        mPendingRequests.remove(view);
    }

    @Override
    public void refreshCache() {
        if (DEBUG) Log.d(TAG, "refreshCache");
        for (BitmapHolder holder : mBitmapHolderCache.snapshot().values()) {
            holder.fresh = false;
        }
    }

    /**
     * Checks if the photo is present in cache. If so, sets the photo on the
     * view.
     * 
     * @return false if the photo needs to be (re)loaded from the provider.
     */
    private boolean loadCachedPhoto(ImageView view, Request request) {
        BitmapHolder holder = mBitmapHolderCache.get(request.getKey());
        if (holder == null) {
            // The bitmap has not been loaded - should display the placeholder
            // image.
            request.applyDefaultImage(view);
            return false;
        }

        if (holder.bytes == null) {
            request.applyDefaultImage(view);
            return holder.fresh;
        }

        // Optionally decode bytes into a bitmap
        inflateBitmap(holder);

        // view.setImageBitmap(holder.bitmap);
        view.setImageDrawable(holder.bitmap);

        if (holder.bitmap != null) {
            // Put the bitmap in the LRU cache
            mBitmapCache.put(request, holder.bitmap);
        }

        // Soften the reference
        holder.bitmap = null;

        return holder.fresh;
    }

    /**
     * If necessary, decodes bytes stored in the holder to Bitmap.  As long as the
     * bitmap is held either by {@link #mBitmapCache} or by a soft reference in
     * the holder, it will not be necessary to decode the bitmap.
     */
    private static void inflateBitmap(BitmapHolder holder) {
        byte[] bytes = holder.bytes;
        if (bytes == null || bytes.length == 0) {
            return;
        }

        // Check the soft reference. If will be retained if the bitmap is also
        // in the LRU cache, so we don't need to check the LRU cache explicitly.
        if (holder.bitmapRef != null) {
            holder.bitmap = holder.bitmapRef.get();
            if (holder.bitmap != null) {
                return;
            }
        }

        //Gionee:huangzy 20120728 modify for CR00658678 start
        try {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
//            holder.bitmap = bitmap;
//            holder.bitmapRef = new SoftReference<Bitmap>(bitmap);

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
            bitmap = ContactDetailDisplayUtils.toRoundBitmap(bitmap);
            BitmapDrawable drawable = null;
            if (!holder.mbIsUriBitmap) {
            	Bitmap tmp = bitmap;
            	bitmap = Bitmap.createScaledBitmap(bitmap, msPhotoWidth, msPhotoHeight, true);
                drawable = new BitmapDrawable(msApplicationContext.getResources(), bitmap);
                if (!tmp.sameAs(bitmap)) {
                	tmp.recycle();	
                }
            } else {
                drawable = new BitmapDrawable(msApplicationContext.getResources(), bitmap);
            }
            drawable.setAntiAlias(true);
            drawable.setFilterBitmap(true);
            holder.bitmap = drawable;
            holder.bitmapRef = new SoftReference<BitmapDrawable>(drawable);
            
            if (DEBUG) {
                Log.d(TAG, "inflateBitmap " + btk(bytes.length) + " -> "
                        + bitmap.getWidth() + "x" + bitmap.getHeight()
                        + ", " + btk(bitmap.getByteCount()));
            }
        } catch (OutOfMemoryError e) {
            // Do nothing - the photo will appear to be missing
        }
        //Gionee:huangzy 20120728 modify for CR00658678 end
    }

    public void clear() {
        if (DEBUG) Log.d(TAG, "clear");
        mPendingRequests.clear();
        mBitmapHolderCache.evictAll();
        mBitmapCache.evictAll();
    }

    @Override
    public void pause() {
        mPaused = true;
    }

    @Override
    public void resume() {
        mPaused = false;
        if (DEBUG) dumpStats();
        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    /**
     * Sends a message to this thread itself to start loading images. If the
     * current view contains multiple image views, all of those image views will
     * get a chance to request their respective photos before any of those
     * requests are executed. This allows us to load images in bulk.
     */
    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    /**
     * Processes requests on the main thread.
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOADING: {
                mLoadingRequested = false;
                if (!mPaused) {
                    ensureLoaderThread();
                    mLoaderThread.requestLoading();
                }
                return true;
            }

            case MESSAGE_PHOTOS_LOADED: {
                if (!mPaused) {
                    processLoadedImages();
                }
                if (DEBUG) dumpStats();
                return true;
            }
        }
        return false;
    }

    public void ensureLoaderThread() {
        if (mLoaderThread == null) {
            mLoaderThread = new LoaderThread(mContext.getContentResolver());
            mLoaderThread.start();
        }
    }

    /**
     * Goes over pending loading requests and displays loaded photos. If some of
     * the photos still haven't been loaded, sends another request for image
     * loading.
     */
    private void processLoadedImages() {
        Iterator<ImageView> iterator = mPendingRequests.keySet().iterator();
        while (iterator.hasNext()) {
            ImageView view = iterator.next();
            Request key = mPendingRequests.get(view);
            boolean loaded = loadCachedPhoto(view, key);
            if (loaded) {
                iterator.remove();
            }
        }

        softenCache();

        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    /**
     * Removes strong references to loaded bitmaps to allow them to be garbage
     * collected if needed. Some of the bitmaps will still be retained by
     * {@link #mBitmapCache}.
     */
    private void softenCache() {
        for (BitmapHolder holder : mBitmapHolderCache.snapshot().values()) {
            holder.bitmap = null;
        }
    }

    /**
     * Stores the supplied bitmap in cache.
     */
    private void cacheBitmap(Object key, byte[] bytes, boolean preloading) {
        if (DEBUG) {
            BitmapHolder prev = mBitmapHolderCache.get(key);
            if (prev != null && prev.bytes != null) {
                Log.d(TAG, "Overwriting cache: key=" + key + (prev.fresh ? " FRESH" : " stale"));
                if (prev.fresh) {
                    mFreshCacheOverwrite.incrementAndGet();
                } else {
                    mStaleCacheOverwrite.incrementAndGet();
                }
            }
            Log.d(TAG, "Caching data: key=" + key + ", " + btk(bytes.length));
        }
        
        BitmapHolder holder = null;
        if (key instanceof Uri) {
            holder = new BitmapHolder(bytes, true);
        } else {
            holder = new BitmapHolder(bytes);
        }

        holder.fresh = true;

        // Unless this image is being preloaded, decode it right away while
        // we are still on the background thread.
        if (!preloading) {
            inflateBitmap(holder);
        }

        mBitmapHolderCache.put(key, holder);
    }

    /**
     * Populates an array of photo IDs that need to be loaded.
     */
    private void obtainPhotoIdsAndUrisToLoad(Set<Long> photoIds, Set<String> photoIdsAsStrings,
            Set<Uri> uris) {
        photoIds.clear();
        photoIdsAsStrings.clear();
        uris.clear();

        /*
         * Since the call is made from the loader thread, the map could be
         * changing during the iteration. That's not really a problem:
         * ConcurrentHashMap will allow those changes to happen without throwing
         * exceptions. Since we may miss some requests in the situation of
         * concurrent change, we will need to check the map again once loading
         * is complete.
         */
        Iterator<Request> iterator = mPendingRequests.values().iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            BitmapHolder holder = mBitmapHolderCache.get(request);
            if (holder == null || !holder.fresh) {
                if (request.isUriRequest()) {
                    uris.add(request.mUri);
                } else {
                    photoIds.add(request.mId);
                    photoIdsAsStrings.add(String.valueOf(request.mId));
                }
            }
        }
    }

    /**
     * The thread that performs loading of photos from the database.
     */
    private class LoaderThread extends HandlerThread implements Callback {
        private static final int BUFFER_SIZE = 1024 * 16;

        private static final int MESSAGE_PRELOAD_PHOTOS = 0;

        private static final int MESSAGE_LOAD_PHOTOS = 1;

        /**
         * A pause between preload batches that yields to the UI thread.
         */
        private static final int PHOTO_PRELOAD_DELAY = 1000;

        /**
         * Number of photos to preload per batch.
         */
        private static final int PRELOAD_BATCH = 25;

        /**
         * Maximum number of photos to preload. If the cache size is 2Mb and the
         * expected average size of a photo is 4kb, then this number should be
         * 2Mb/4kb = 500.
         */
        private static final int MAX_PHOTOS_TO_PRELOAD = 100;

        private final ContentResolver mResolver;

        private final StringBuilder mStringBuilder = new StringBuilder();

        private final Set<Long> mPhotoIds = Sets.newHashSet();

        private final Set<String> mPhotoIdsAsStrings = Sets.newHashSet();

        private final Set<Uri> mPhotoUris = Sets.newHashSet();

        private final List<Long> mPreloadPhotoIds = Lists.newArrayList();

        private Handler mLoaderThreadHandler;

        private byte mBuffer[];

        private static final int PRELOAD_STATUS_NOT_STARTED = 0;

        private static final int PRELOAD_STATUS_IN_PROGRESS = 1;

        private static final int PRELOAD_STATUS_DONE = 2;

        private int mPreloadStatus = PRELOAD_STATUS_NOT_STARTED;

        public LoaderThread(ContentResolver resolver) {
            super(LOADER_THREAD_NAME);
            mResolver = resolver;
        }

        public void ensureHandler() {
            if (mLoaderThreadHandler == null) {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
        }

        /**
         * Kicks off preloading of the next batch of photos on the background
         * thread. Preloading will happen after a delay: we want to yield to the
         * UI thread as much as possible.
         * <p>
         * If preloading is already complete, does nothing.
         */
        public void requestPreloading() {
            if (mPreloadStatus == PRELOAD_STATUS_DONE) {
                return;
            }

            ensureHandler();
            if (mLoaderThreadHandler.hasMessages(MESSAGE_LOAD_PHOTOS)) {
                return;
            }

            mLoaderThreadHandler.sendEmptyMessageDelayed(MESSAGE_PRELOAD_PHOTOS,
                    PHOTO_PRELOAD_DELAY);
        }

        /**
         * Sends a message to this thread to load requested photos. Cancels a
         * preloading request, if any: we don't want preloading to impede
         * loading of the photos we need to display now.
         */
        public void requestLoading() {
            ensureHandler();
            mLoaderThreadHandler.removeMessages(MESSAGE_PRELOAD_PHOTOS);
            mLoaderThreadHandler.sendEmptyMessage(MESSAGE_LOAD_PHOTOS);
        }

        /**
         * Receives the above message, loads photos and then sends a message to
         * the main thread to process them.
         */
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PRELOAD_PHOTOS:
                    preloadPhotosInBackground();
                    break;
                case MESSAGE_LOAD_PHOTOS:
                    loadPhotosInBackground();
                    break;
            }
            return true;
        }

        /**
         * The first time it is called, figures out which photos need to be
         * preloaded. Each subsequent call preloads the next batch of photos and
         * requests another cycle of preloading after a delay. The whole process
         * ends when we either run out of photos to preload or fill up cache.
         */
        private void preloadPhotosInBackground() {
            if (mPreloadStatus == PRELOAD_STATUS_DONE) {
                return;
            }

            if (mPreloadStatus == PRELOAD_STATUS_NOT_STARTED) {
                queryPhotosForPreload();
                if (mPreloadPhotoIds.isEmpty()) {
                    mPreloadStatus = PRELOAD_STATUS_DONE;
                } else {
                    mPreloadStatus = PRELOAD_STATUS_IN_PROGRESS;
                }
                requestPreloading();
                return;
            }

            if (mBitmapHolderCache.size() > mBitmapHolderCacheRedZoneBytes) {
                mPreloadStatus = PRELOAD_STATUS_DONE;
                return;
            }

            mPhotoIds.clear();
            mPhotoIdsAsStrings.clear();

            int count = 0;
            int preloadSize = mPreloadPhotoIds.size();
            while (preloadSize > 0 && mPhotoIds.size() < PRELOAD_BATCH) {
                preloadSize--;
                count++;
                Long photoId = mPreloadPhotoIds.get(preloadSize);
                mPhotoIds.add(photoId);
                mPhotoIdsAsStrings.add(photoId.toString());
                mPreloadPhotoIds.remove(preloadSize);
            }

            loadPhotosFromDatabase(true);

            if (preloadSize == 0) {
                mPreloadStatus = PRELOAD_STATUS_DONE;
            }

            Log.v(TAG, "Preloaded " + count + " photos.  Cached bytes: "
                    + mBitmapHolderCache.size());

            requestPreloading();
        }

        private void queryPhotosForPreload() {
            Cursor cursor = null;
            try {
                Uri uri = Contacts.CONTENT_URI.buildUpon().appendQueryParameter(
                        ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
                        .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
                                String.valueOf(MAX_PHOTOS_TO_PRELOAD)).build();
                cursor = mResolver.query(uri, new String[] {
                    Contacts.PHOTO_ID
                }, Contacts.PHOTO_ID + " NOT NULL AND " + Contacts.PHOTO_ID + "!=0", null,
                        Contacts.STARRED + " DESC, " + Contacts.LAST_TIME_CONTACTED + " DESC");

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // Insert them in reverse order, because we will be
                        // taking
                        // them from the end of the list for loading.
                        mPreloadPhotoIds.add(0, cursor.getLong(0));
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private void loadPhotosInBackground() {
            obtainPhotoIdsAndUrisToLoad(mPhotoIds, mPhotoIdsAsStrings, mPhotoUris);
            loadPhotosFromDatabase(false);
            loadRemotePhotos();
            requestPreloading();
        }

        private void loadPhotosFromDatabase(boolean preloading) {
            if (mPhotoIds.isEmpty()) {
                return;
            }

            // Remove loaded photos from the preload queue: we don't want
            // the preloading process to load them again.
            if (!preloading && mPreloadStatus == PRELOAD_STATUS_IN_PROGRESS) {
                for (Long id : mPhotoIds) {
                    mPreloadPhotoIds.remove(id);
                }
                if (mPreloadPhotoIds.isEmpty()) {
                    mPreloadStatus = PRELOAD_STATUS_DONE;
                }
            }

            mStringBuilder.setLength(0);
            mStringBuilder.append(Photo._ID + " IN(");
            for (int i = 0; i < mPhotoIds.size(); i++) {
                if (i != 0) {
                    mStringBuilder.append(',');
                }
                mStringBuilder.append('?');
            }
            mStringBuilder.append(')');

            Cursor cursor = null;
            try {
                if (DEBUG) Log.d(TAG, "Loading " + TextUtils.join(",", mPhotoIdsAsStrings));
                cursor = mResolver.query(Data.CONTENT_URI, COLUMNS, mStringBuilder.toString() + " and is_privacy > -1",
                        mPhotoIdsAsStrings.toArray(EMPTY_STRING_ARRAY), null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Long id = cursor.getLong(0);
                        byte[] bytes = cursor.getBlob(1);
                        cacheBitmap(id, bytes, preloading);
                        mPhotoIds.remove(id);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            // Remaining photos were not found in the contacts database (but
            // might be in profile).
            for (Long id : mPhotoIds) {
                if (ContactsContract.isProfileId(id)) {
                    Cursor profileCursor = null;
                    try {
                        profileCursor = mResolver.query(ContentUris.withAppendedId(
                                Data.CONTENT_URI, id), COLUMNS, "is_privacy > -1", null, null);
                        if (profileCursor != null && profileCursor.moveToFirst()) {
                            cacheBitmap(profileCursor.getLong(0), profileCursor.getBlob(1),
                                    preloading);
                        } else {
                            // Couldn't load a photo this way either.
                            cacheBitmap(id, null, preloading);
                        }
                    } finally {
                        if (profileCursor != null) {
                            profileCursor.close();
                        }
                    }
                } else {
                    // Not a profile photo and not found - mark the cache
                    // accordingly
                    cacheBitmap(id, null, preloading);
                }
            }

            mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
        }

        private void loadRemotePhotos() {
            for (Uri uri : mPhotoUris) {
                if (mBuffer == null) {
                    mBuffer = new byte[BUFFER_SIZE];
                }
                try {
                    if (DEBUG) Log.d(TAG, "Loading " + uri);
                    InputStream is = mResolver.openInputStream(uri);
                    if (is != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            int size;
                            while ((size = is.read(mBuffer)) != -1) {
                                baos.write(mBuffer, 0, size);
                            }
                        } finally {
                            is.close();
                        }
                        cacheBitmap(uri, baos.toByteArray(), false);
                        mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
                    } else {
                        Log.v(TAG, "Cannot load photo " + uri);
                        cacheBitmap(uri, null, false);
                    }
                } catch (Exception ex) {
                    Log.v(TAG, "Cannot load photo " + uri, ex);
                    cacheBitmap(uri, null, false);
                }
            }
        }
    }

    /**
     * A holder for either a Uri or an id and a flag whether this was requested
     * for the dark or light theme
     */
    private static final class Request {
        private final long mId;

        private final Uri mUri;

        private final boolean mDarkTheme;

        private final boolean mHires;

        private final DefaultImageProvider mDefaultProvider;

        private Request(long id, Uri uri, boolean hires, boolean darkTheme,
                DefaultImageProvider defaultProvider) {
            mId = id;
            mUri = uri;
            mDarkTheme = darkTheme;
            mHires = hires;
            mDefaultProvider = defaultProvider;
        }

        public static Request createFromId(long id, boolean hires, boolean darkTheme,
                DefaultImageProvider defaultProvider) {
            return new Request(id, null /* no URI */, hires, darkTheme, defaultProvider);
        }

        public static Request createFromUri(Uri uri, boolean hires, boolean darkTheme,
                DefaultImageProvider defaultProvider) {
            return new Request(0 /* no ID */, uri, hires, darkTheme, defaultProvider);
        }

        public boolean isDarkTheme() {
            return mDarkTheme;
        }

        public boolean isHires() {
            return mHires;
        }

        public boolean isUriRequest() {
            return mUri != null;
        }

        @Override
        public int hashCode() {
            if (mUri != null)
                return mUri.hashCode();

            // copied over from Long.hashCode()
            return (int) (mId ^ (mId >>> 32));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Request))
                return false;
            final Request that = (Request) o;
            // Don't compare equality of mHires and mDarkTheme fields because
            // these are only used
            // in the default contact photo case. When the contact does have a
            // photo, the contact
            // photo is the same regardless of mHires and mDarkTheme, so we
            // shouldn't need to put
            // the photo request on the queue twice.
            return mId == that.mId && UriUtils.areEqual(mUri, that.mUri);
        }

        public Object getKey() {
            return mUri == null ? mId : mUri;
        }

        public void applyDefaultImage(ImageView view) {
            mDefaultProvider.applyDefaultImage(view, mHires, mDarkTheme, true);
        }
    }
}

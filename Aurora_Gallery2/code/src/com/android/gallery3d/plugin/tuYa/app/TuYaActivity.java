package com.android.gallery3d.plugin.tuYa.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import aurora.app.AuroraProgressDialog;
import aurora.app.AuroraAlertDialog;
import android.content.DialogInterface;
import com.android.gallery3d.ui.TiledTexture;
import com.android.gallery3d.plugin.tuYa.ui.DrawingSelectView;
import com.android.gallery3d.plugin.tuYa.ui.PaintColorSelectView;
import com.android.gallery3d.plugin.tuYa.ui.PaintSizeSelectView;
import com.android.gallery3d.plugin.tuYa.ui.TuYaView;
import com.android.gallery3d.app.Gallery;
import aurora.app.AuroraActivity;

public class TuYaActivity extends AuroraActivity implements OnClickListener, OnLongClickListener,
        SaveCopyTask.Callback, DrawingManager.DrawingChangeListener {
    private static final String TAG = "TuYaActivity";
    private static final String FLAG_START_SINGLE_PHOTO = "start_single_photo";
    private static final int MSG_IMAGE_LOAD_START = 0;
    private static final int MSG_IMAGE_LOAD_FINISH = 1;
    private static final int SELECT_PICTURE = 1;
    private TuYaView mTuYaView;
    private int mTuYaViewWidth;
    private int mTuYaViewHeight;
    private Handler mHandler;
    private AuroraProgressDialog mProgressDialog;
    private Uri mSourceUri;
    private View mUndoView;
    private View mRedoView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder.Callback mCallback;
    private int mCurDrawingId = R.id.tuya_id_pathline;
    private PaintSizeSelectView mPaintSizeSelectView;
    private PaintColorSelectView mPaintColorSelectView;
    private View mMosaicBlurSelectView;
    private View mSaveButton;
    private boolean mLoadFinish;
    private RectF mMosaicUnitSrc = new RectF();
    private RectF mMosaicUnitDst = new RectF();
    private boolean mIsStartSinglePhoto = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaintManager.getInstance().init(this);
        setContentView(R.layout.tu_ya_main_layout);
        mIsStartSinglePhoto = getIntent().getBooleanExtra(FLAG_START_SINGLE_PHOTO, true);

        initDrawingSelectView();
        initPaintSizeSelectView();
        initPaintColorSelectView();
        initMosaicBlurSelectView();

        initTuYaView();
        initTuYaViewSurfaceHolder();
        initHandler();
        initProgressDialog();
        initUndoView();
        initRedoView();
        initSaveButton();
        intiDropButton();
    }

    private void initDrawingSelectView() {
        DrawingSelectView drawingSelectView = (DrawingSelectView) findViewById(R.id.tuya_id_drawing_select_panel);
        drawingSelectView.init(mCurDrawingId);
        drawingSelectView.setDrawingListener(new DrawingSelectView.DrawingListener() {

            @Override
            public void onDrawingSelected(int drawingViewId) {
                mCurDrawingId = drawingViewId;
                PaintManager paintManager = PaintManager.getInstance();
                mPaintSizeSelectView.updateSelectView(paintManager.getPaintSizeViewId(paintManager
                        .getCurDrawingPaintSize(drawingViewId)));
                if (drawingViewId == R.id.tuya_id_mosaic) {
                    mMosaicBlurSelectView.setVisibility(View.VISIBLE);
                    mPaintColorSelectView.setVisibility(View.INVISIBLE);
                    mSurfaceView.setVisibility(View.VISIBLE);
                } else {
                    mPaintColorSelectView.setVisibility(View.VISIBLE);
                    mMosaicBlurSelectView.setVisibility(View.INVISIBLE);
                    mPaintColorSelectView.updateSelectView(paintManager.getPaintColorViewId(paintManager
                            .getCurDrawingPaintColor(drawingViewId)));
                }
                mTuYaView.setCurDrawingId(drawingViewId);
            }
        });
    }

    private void initPaintSizeSelectView() {
        PaintSizeSelectView paintSizeSelectView = (PaintSizeSelectView) findViewById(R.id.tuya_id_paint_size_select_panel);
        mPaintSizeSelectView = paintSizeSelectView;
        PaintManager paintManager = PaintManager.getInstance();
        paintSizeSelectView.init(paintManager.getPaintSizeViewId(paintManager
                .getCurDrawingPaintSize(mCurDrawingId)));
        paintSizeSelectView.setPaintSizeListener(new PaintSizeSelectView.PaintSizeListener() {

            @Override
            public void onPaintSizeSelected(int viewId) {
                PaintManager paintManager = PaintManager.getInstance();
                int newPaintSize = paintManager.getViewPaintSize(viewId);
                if (mCurDrawingId == R.id.tuya_id_mosaic) {
                    paintManager.updateMosaicPaint(TuYaActivity.this, mCurDrawingId, newPaintSize,
                            paintManager.getMosaicPaintBlur());
                    return;
                }
                int color = paintManager.getCurDrawingPaintColor(mCurDrawingId);
                switch (mCurDrawingId) {
                    case R.id.tuya_id_pathline:
                        paintManager.updatePathLinePaint(TuYaActivity.this, mCurDrawingId, newPaintSize, color);
                        return;
                    case R.id.tuya_id_circle:
                        paintManager.updateCirclePaint(TuYaActivity.this, mCurDrawingId, newPaintSize, color);
                        return;
                    case R.id.tuya_id_rectangle:
                        paintManager.updateRectanglePaint(TuYaActivity.this, mCurDrawingId, newPaintSize, color);
                        return;
                    case R.id.tuya_id_arrow:
                        paintManager.updateArrowPaint(TuYaActivity.this, mCurDrawingId, newPaintSize, color);
                        return;
                    default:
                        Utils.assertTrue(false);
                        break;
                }
            }
        });
    }

    private void initPaintColorSelectView() {
        PaintColorSelectView paintColorSelectView = (PaintColorSelectView) findViewById(R.id.tuya_id_paint_color_select_panel);
        mPaintColorSelectView = paintColorSelectView;
        PaintManager paintManager = PaintManager.getInstance();
        paintColorSelectView.init(paintManager.getPaintColorViewId(paintManager
                .getCurDrawingPaintColor(mCurDrawingId)));
        paintColorSelectView.setPaintColorListener(new PaintColorSelectView.PaintColorListener() {

            @Override
            public void onPaintColorSelected(int viewId) {
                PaintManager paintManager = PaintManager.getInstance();
                int newPaintColor = paintManager.getViewPaintColor(viewId);
                int paintSize = paintManager.getCurDrawingPaintSize(mCurDrawingId);
                switch (mCurDrawingId) {
                    case R.id.tuya_id_pathline:
                        paintManager.updatePathLinePaint(TuYaActivity.this, mCurDrawingId, paintSize, newPaintColor);
                        return;
                    case R.id.tuya_id_circle:
                        paintManager.updateCirclePaint(TuYaActivity.this, mCurDrawingId, paintSize, newPaintColor);
                        return;
                    case R.id.tuya_id_rectangle:
                        paintManager.updateRectanglePaint(TuYaActivity.this, mCurDrawingId, paintSize, newPaintColor);
                        return;
                    case R.id.tuya_id_arrow:
                        paintManager.updateArrowPaint(TuYaActivity.this, mCurDrawingId, paintSize, newPaintColor);
                        return;
                    default:
                        Utils.assertTrue(false);
                        break;
                }
            }
        });
    }

    private void initMosaicBlurSelectView() {
        View view = findViewById(R.id.tuya_id_mosaic_blur_select_panel);
        mMosaicBlurSelectView = view;
        SeekBar mosaicBlurValueBar = (SeekBar) findViewById(R.id.tuya_id_mosaic_blur_select_bar);
        final PaintManager paintManager = PaintManager.getInstance();
        mosaicBlurValueBar.setProgress(paintManager.getMosaicPaintBlur() - TuYaUtils.MOSAIC_MIN_GRID_SIZE);
        mosaicBlurValueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!mLoadFinish) {
                    return;
                }
                if (mCurDrawingId != R.id.tuya_id_mosaic) {
                    return;
                }
                paintManager.updateMosaicPaint(TuYaActivity.this, mCurDrawingId,
                        paintManager.getCurDrawingPaintSize(mCurDrawingId), progress);
                drawMosaicBg();
            }
        });
    }

    private void initTuYaViewSurfaceHolder() {
        mSurfaceView = (SurfaceView) findViewById(R.id.tuya_id_mosaic_content);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                drawMosaicBg();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                drawMosaicBg();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        };
        surfaceHolder.addCallback(mCallback);
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_IMAGE_LOAD_FINISH:
                        mLoadFinish = true;
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        Bitmap bitmap = (Bitmap) msg.obj;
                        if (bitmap == null) {
                            Toast toast = Toast.makeText(TuYaActivity.this, R.string.tuya_st_msg_load_fail,
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            finish();
                        } else {
                            mTuYaView.setTuYaBitmap(bitmap);
                        }
                        break;
                    case MSG_IMAGE_LOAD_START:
                        mProgressDialog.setTitle(R.string.tuya_st_msg_loading);//setMessage
                        mProgressDialog.show();
                        break;
                    default:
                        Utils.assertTrue(false);
                }
            }
        };
    }

    private void initTuYaView() {
        TuYaView tuYaView = (TuYaView) findViewById(R.id.tuya_id_content);
        tuYaView.setCurDrawingId(mCurDrawingId);
        mTuYaView = tuYaView;
        int tuYaViewW = tuYaView.getWidth();
        int tuYaViewH = tuYaView.getHeight();
        if (tuYaViewW != mTuYaViewWidth && tuYaViewH != mTuYaViewHeight) {
            mTuYaViewWidth = tuYaViewW;
            mTuYaViewHeight = tuYaViewH;
            loadBitmap();
        }
        tuYaView.setOnSizeChangeListener(new TuYaView.OnSizeChangeListener() {

            @Override
            public void onSizeChange(int w, int h) {
                if (w != mTuYaViewWidth && h != mTuYaViewHeight) {
                    mTuYaViewWidth = w;
                    mTuYaViewHeight = h;
                    loadBitmap();
                }
            }
        });
        tuYaView.getDrawingManager().setDrawingChangeListener(this);
    }

    private void initProgressDialog() {
        mProgressDialog = new AuroraProgressDialog(this);
    }

    private void initUndoView() {
        View undoView = findViewById(R.id.tuya_id_undo);
        undoView.setOnClickListener(this);
        undoView.setOnLongClickListener(this);
        undoView.setEnabled(false);
        mUndoView = undoView;
    }

    private void initRedoView() {
        View redoView = findViewById(R.id.tuya_id_redo);
        redoView.setOnClickListener(this);
        redoView.setEnabled(false);
        mRedoView = redoView;
    }

    private void initSaveButton() {
        mSaveButton = findViewById(R.id.tuya_id_save_image);
        mSaveButton.setOnClickListener(this);
        mSaveButton.setEnabled(false);
    }

    private void intiDropButton() {
        findViewById(R.id.tuya_id_drop_image).setOnClickListener(this);
    }

    private void loadBitmap() {
        if (mSourceUri == null) {
            mSourceUri = getIntent().getData();
        }
        Uri uri = mSourceUri;
        if (uri == null) {
            pickImage();
        } else {
            loadBitmap(uri);
        }
    }

    private void loadBitmap(final Uri uri) {
        mSourceUri = uri;
        if (mTuYaViewWidth == 0 || mTuYaViewHeight == 0) {
            return;
        }

        final Context context = getApplicationContext();
        new Thread() {
            public void run() {
                mHandler.sendEmptyMessageDelayed(MSG_IMAGE_LOAD_START, 500);
				/*
                if (!GalleryUtils.checkExternalStoragePermission()) {
                    mHandler.removeMessages(MSG_IMAGE_LOAD_START);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_IMAGE_LOAD_FINISH, null));
                    return;
                }  TYM
                */
                int rotation = TuYaUtils.getRotation(context, uri);
                Bitmap bitmap = TuYaUtils.loadBitmap(context, uri, mTuYaViewWidth, mTuYaViewHeight);
                if (bitmap != null && rotation > 1) {
                    Bitmap tempBitmap = TuYaUtils.rotateToPortrait(bitmap, rotation);
                    if (tempBitmap != bitmap) {
                        bitmap.recycle();
                        bitmap = tempBitmap;
                    }
                }
                if (bitmap != null) {
                    bitmap = TuYaUtils.transformBackground(bitmap);
                }
                mHandler.removeMessages(MSG_IMAGE_LOAD_START);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_IMAGE_LOAD_FINISH, bitmap));
            }

        }.start();
    }

    @Override
    public void onClick(View v) {
        if (!mLoadFinish) {
            return;
        }
        int id = v.getId();

        switch (id) {
            case R.id.tuya_id_undo:
                mTuYaView.undo();
                break;
            case R.id.tuya_id_redo:
                mTuYaView.redo();
                break;
            case R.id.tuya_id_drop_image:
                if (mProgressDialog.isShowing()) {
                    return;
                }
                finish();
                break;
            case R.id.tuya_id_save_image:
                if (isFinishing()) {
                    return;
                }
                saveImage();
                break;
            default:
                break;
        }
    }

    private void saveImage() {
        if (mTuYaView.getDrawingManager().hasModifications()) {
            mProgressDialog.setTitle(R.string.tuya_st_msg_save);
            mProgressDialog.show();
            new SaveCopyTask(getApplicationContext(), mSourceUri, this, mTuYaView.getDrawingManager());
        } else {
            finish();
        }
    }

    private void pickImage() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction("com.gionee.gallery.intent.action.GET_CONTENT");
            startActivityForResult(intent, SELECT_PICTURE);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "tuya select image, activity not found");
        }

    }

    @Override
    public void onComplete(Uri saveUri, int failedType, String absolutePath) {
        setActivityResult(saveUri);
        mProgressDialog.dismiss();
        if (null == saveUri) {
            cannotSaveImage(failedType);
        } else {
            absolutePath = TuYaUtils.convertToFriendlyPath(this, absolutePath);
            Toast toast = Toast.makeText(this, absolutePath, Toast.LENGTH_SHORT);
            toast.show();
            startSinglePhoto(saveUri);
        }
        finish();
    }

    private void startSinglePhoto(Uri uri) {
        if (mIsStartSinglePhoto) {
            Intent intent = new Intent(this, Gallery.class);//paul modify GoogleGalleryActivity
            intent.setData(uri);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra("SingleItemOnly", false);
            startActivity(intent);
        }

    }

    private void setActivityResult(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        setResult(RESULT_OK, intent);
    }

    private void cannotSaveImage(int failedType) {
        Toast toast;
        if (SaveCopyTask.SPACE_FULL_FAILED_SAVE == failedType) {
            toast = Toast.makeText(this, R.string.tuya_st_msg_memory_full_failed_save, Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(this, R.string.tuya_st_msg_failed_to_save, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                loadBitmap(selectedImageUri);
            }
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (!mLoadFinish) {
            return;
        }

        if (mTuYaView.getDrawingManager().hasModifications()) {
            showConfirmDialog();
        } else {
            super.onBackPressed();
        }
    }
	
	private DialogInterface.OnClickListener mCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			TuYaActivity.this.finish();
		}
	};
	
	private DialogInterface.OnClickListener mOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			saveImage();
		}
	};

    private void showConfirmDialog() {
        AuroraAlertDialog.Builder confirmDialog = new AuroraAlertDialog.Builder(this);
        confirmDialog.setMessage(R.string.tuya_st_msg_save_content);
		confirmDialog.setPositiveButton(R.string.tuya_st_save_image, mOkListener);
		confirmDialog.setNegativeButton(R.string.tuya_st_drop_image, mCancelListener);
        confirmDialog.show();
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.tuya_id_undo) {
            mTuYaView.reDraw();
            return true;
        }
        return false;
    }

    @Override
    public void onDrawingSizeChange(int drawingSize, int reDoDrawingSize) {
        if (drawingSize > 0) {
            mUndoView.setEnabled(true);
            mSaveButton.setEnabled(true);
        } else {
            mUndoView.setEnabled(false);
            mSaveButton.setEnabled(false);
        }

        if (reDoDrawingSize > 0) {
            mRedoView.setEnabled(true);
        } else {
            mRedoView.setEnabled(false);
        }
    }

    private void drawMosaicBg() {
        Bitmap original = mTuYaView.getOriginalBitmap();
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (original == null || surfaceHolder == null) {
            return;
        }
        Rect src = new Rect(0, 0, original.getWidth(), original.getHeight());
        RectF showRect = mTuYaView.getShowRectF();
        Paint paint = new Paint(Paint.DITHER_FLAG);
        Canvas c = surfaceHolder.lockCanvas();
        c.drawColor(getResources().getColor(R.color.tu_ya_col_cover_bg));
        c.drawBitmap(original, src, showRect, paint);
        drawMosaicUnit(original, showRect, c);
        surfaceHolder.unlockCanvasAndPost(c);
    }

    private void drawMosaicUnit(Bitmap original, RectF showRect, Canvas c) {
        int w = original.getWidth();
        int h = original.getHeight();
        int gridSize = PaintManager.getInstance().getMosaicPaintBlur();
        int horCount = (int) Math.ceil(w / (float) gridSize);
        int verCount = (int) Math.ceil(h / (float) gridSize);
        Paint mosaicPaint = new Paint(Paint.DITHER_FLAG);
        mosaicPaint.setAntiAlias(true);
        float scaleX = showRect.width() / w;
        float scaleY = showRect.height() / h;
        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int l = gridSize * horIndex;
                int t = gridSize * verIndex;
                int r = l + gridSize;
                if (r > w) {
                    r = w;
                }
                int b = t + gridSize;
                if (b > h) {
                    b = h;
                }
                int color = original.getPixel(l, t);
                mMosaicUnitSrc.set(l, t, r, b);
                mosaicPaint.setColor(color);
                TiledTexture.mapRect(mMosaicUnitDst, mMosaicUnitSrc, 0, 0,
                        showRect.left, showRect.top, scaleX, scaleY);
                c.drawRect(mMosaicUnitDst, mosaicPaint);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //YouJuUtils.youjuResume(this); TYM
        //YouJuUtils.youjuEvent(this, YouJuUtils.getEnterTuya());
    }

    @Override
    protected void onPause() {
        super.onPause();
        //YouJuUtils.youjuPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSurfaceView.getHolder().removeCallback(mCallback);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mLoadFinish) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }
}

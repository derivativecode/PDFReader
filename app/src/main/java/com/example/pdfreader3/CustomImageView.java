package com.example.pdfreader3;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import androidx.appcompat.widget.AppCompatImageView;

public class CustomImageView extends AppCompatImageView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    String TAG = CustomImageView.class.getSimpleName();

    float mScaleFactor;

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    Matrix matrix, matrix2;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float intermediateScale = 1.3f;
    float maxScale = 1.5f;
    float[] m;

    public enum Zoom {
        SINGLE,
        DOUBLE,
        NONE,
    }
    Zoom zoomed = Zoom.NONE;

    //boolean zoomed = false;

    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;

    ScaleGestureDetector mScaleDetector;
    GestureDetector mGestureDetector;

    Context context;


    public CustomImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setOnDoubleTapListener(this);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        matrix2 = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);

        matrix2.set(matrix);
        //setAnimationMatrix(matrix2);

        setScaleType(ScaleType.MATRIX);

        setOnTouchListener((v, event) -> {
            mScaleDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);

            PointF curr = new PointF(event.getX(), event.getY());

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(curr);
                    start.set(last);
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        float fixTransX = getFixDragTrans(deltaX, viewWidth,origWidth * saveScale);
                        float fixTransY = getFixDragTrans(deltaY, viewHeight,origHeight * saveScale);
                        matrix.postTranslate(fixTransX, fixTransY);
                        fixTrans();
                        last.set(curr.x, curr.y);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK && yDiff < CLICK)
                        performClick();
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }

            setImageMatrix(matrix);
            invalidate();
            return true; // indicate event was handled
        });
    }

    public void setMaxZoom(float x) {
        maxScale = x;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }


    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // Double tap is detected
        Log.d(TAG, "Double tap detected");


        SubActivityReader.toggleZoomed(true);

        Drawable drawable = getDrawable();
        int width = drawable.getBounds().width();
        int height = drawable.getBounds().height();
        int bitmapWidth = drawable.getIntrinsicWidth(); //this is the bitmap's width
        int bitmapHeight = drawable.getIntrinsicHeight(); //this is the bitmap's height



        Log.d(TAG, "onDoubleTap: " + getX() + ", " + getY());
        Log.d(TAG, "onDoubleTap: " + getLeft() + ", " + getTop() + ", " + getRight() + ", " + getBottom());
        Log.d(TAG, "onDoubleTap: " + viewWidth + ", " + viewHeight);
        Log.d(TAG, "onDoubleTap: " + width + ", " + height);
        Log.d(TAG, "onDoubleTap: " + bitmapWidth + ", " + bitmapHeight);

        // 2-Stage ZOOM
/*        switch(zoomed){
            case NONE:
                setMaxZoom(1.3f);
                zoomed = Zoom.SINGLE;
                SubActivityReader.toggleZoomed(true);
                break;

            case SINGLE:
                setMaxZoom(2f);
                zoomed=Zoom.DOUBLE;
                SubActivityReader.toggleZoomed(true);
                break;

            case DOUBLE:
                setMaxZoom(1f);
                zoomed = Zoom.NONE;
                SubActivityReader.toggleZoomed(false);
                break;
        }*/


        float origScale = saveScale;
//        float mScaleFactor;

        if (saveScale == maxScale) {
            saveScale = minScale;
            mScaleFactor = minScale / origScale;
        } else {
            saveScale = maxScale;
            mScaleFactor = maxScale / origScale;
        }

        //Log.d(TAG, "onDoubleTap: " + e.getX() + ", " + e.getY());

        Log.d(TAG, "onDoubleTap: " + matrix.toShortString());


        PointF click = new PointF(e.getX(), e.getY());

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);

                final Matrix matrix = t.getMatrix();

                /**
                 * interpolatedTime: 0.0 --> 1.0
                 *
                 */
//                float sx = 1 + (interpolatedTime * maxScale);
//                float sy = 1 + (interpolatedTime * maxScale);
//                matrix.setScale(sx, sy, viewWidth / 2, viewHeight / 2);

//                float x = (interpolatedTime * maxScale)+ e.getX();
//                float y = (interpolatedTime * maxScale) + e.getY();

                float scaleX = (interpolatedTime * 1) + 1;
                float scaleY = (interpolatedTime * 1) + 1;

                float transX = (1 - interpolatedTime) * viewWidth/2;
                float transY = (1 - interpolatedTime) * viewHeight/2;

//                matrix.postScale(scaleX, scaleY, transX, transY);
                //matrix.postTranslate(x, y);
                int bitmapWidth = getDrawable().getIntrinsicWidth();
                int bitmapHeight = getDrawable().getIntrinsicHeight();

                //Log.d(TAG, "applyTransformation: " + bitmapWidth + ", " + bitmapHeight);
                matrix.postTranslate(0, interpolatedTime*bitmapHeight);
        }
        };
        anim.setDuration(5000);
        anim.setFillAfter(true);
        this.startAnimation(anim);




        //matrix.postTranslate(-originx, -originy);

        //scale *= zoom;

        // NEW


        matrix2.setScale(mScaleFactor, mScaleFactor, viewWidth/2, viewHeight/2);

//        MatrixAnimation matrixAnimation = new MatrixAnimation(matrix, matrix2, click, true);
//        startAnimation(matrixAnimation);


        //matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);

        fixTrans();

        return false;
    }


    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
            else
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }

    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            //Log.d(TAG, "fixTrans: " + fixTransX + ", " + fixTransY);
            matrix.postTranslate(fixTransX, fixTransY);
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /*viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        if (saveScale == 1) {
            // Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }*/
        fixTrans();
    }


}
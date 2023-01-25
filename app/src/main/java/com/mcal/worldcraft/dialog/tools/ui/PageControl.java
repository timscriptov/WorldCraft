package com.mcal.worldcraft.dialog.tools.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class PageControl extends LinearLayout {
    private final Context mContext;
    private Drawable activeDrawable;
    private Drawable inactiveDrawable;
    private ArrayList<ImageView> indicators;
    private int mCurrentPage = 0;
    private int mIndicatorSize = 7;
    private OnPageControlClickListener mOnPageControlClickListener = null;
    private int mPageCount = 0;

    public PageControl(Context context) {
        super(context);
        mContext = context;
        initPageControl();
    }

    public PageControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initPageControl();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPageControl() {
        Log.i("PageControl", "Initialising PageControl");
        indicators = new ArrayList<>();
        activeDrawable = new ShapeDrawable();
        inactiveDrawable = new ShapeDrawable();
        activeDrawable.setBounds(0, 0, mIndicatorSize, mIndicatorSize);
        inactiveDrawable.setBounds(0, 0, mIndicatorSize, mIndicatorSize);
        Shape s1 = new OvalShape();
        s1.resize(mIndicatorSize, mIndicatorSize);
        Shape s2 = new OvalShape();
        s2.resize(mIndicatorSize, mIndicatorSize);
        int[] i = {16842808, 16842810};
        TypedArray a = mContext.getTheme().obtainStyledAttributes(i);
        ((ShapeDrawable) activeDrawable).getPaint().setColor(a.getColor(0, -12303292));
        ((ShapeDrawable) inactiveDrawable).getPaint().setColor(a.getColor(1, -3355444));
        ((ShapeDrawable) activeDrawable).setShape(s1);
        ((ShapeDrawable) inactiveDrawable).setShape(s2);
        mIndicatorSize = (int) (mIndicatorSize * getResources().getDisplayMetrics().density);
        setOnTouchListener((View v, MotionEvent event) -> {
            if (mOnPageControlClickListener != null) {
                if (event.getAction() == 1) {
                    if (getOrientation() == LinearLayout.HORIZONTAL) {
                        if (event.getX() < getWidth() / 2) {
                            if (mCurrentPage > 0) {
                                mOnPageControlClickListener.goBackwards();
                            }
                        } else if (mCurrentPage < mPageCount - 1) {
                            mOnPageControlClickListener.goForwards();
                        }
                    } else if (event.getY() < getHeight() / 2) {
                        if (mCurrentPage > 0) {
                            mOnPageControlClickListener.goBackwards();
                        }
                    } else if (mCurrentPage < mPageCount - 1) {
                        mOnPageControlClickListener.goForwards();
                    }
                    return false;
                }
            }
            return true;
        });
    }

    public Drawable getActiveDrawable() {
        return activeDrawable;
    }

    public void setActiveDrawable(Drawable d) {
        activeDrawable = d;
        indicators.get(mCurrentPage).setBackgroundDrawable(activeDrawable);
    }

    public Drawable getInactiveDrawable() {
        return inactiveDrawable;
    }

    public void setInactiveDrawable(Drawable d) {
        inactiveDrawable = d;
        for (int i = 0; i < mPageCount; i++) {
            indicators.get(i).setBackgroundDrawable(inactiveDrawable);
        }
        indicators.get(mCurrentPage).setBackgroundDrawable(activeDrawable);
    }

    public int getPageCount() {
        return mPageCount;
    }

    public void setPageCount(int pageCount) {
        mPageCount = pageCount;
        for (int i = 0; i < pageCount; i++) {
            ImageView imageView = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorSize, mIndicatorSize);
            params.setMargins(mIndicatorSize / 2, mIndicatorSize, mIndicatorSize / 2, mIndicatorSize);
            imageView.setLayoutParams(params);
            imageView.setBackgroundDrawable(inactiveDrawable);
            indicators.add(imageView);
            addView(imageView);
        }
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage < mPageCount) {
            indicators.get(mCurrentPage).setBackgroundDrawable(inactiveDrawable);
            indicators.get(currentPage).setBackgroundDrawable(activeDrawable);
            mCurrentPage = currentPage;
        }
    }

    public int getIndicatorSize() {
        return mIndicatorSize;
    }

    public void setIndicatorSize(int indicatorSize) {
        mIndicatorSize = indicatorSize;
        for (int i = 0; i < mPageCount; i++) {
            indicators.get(i).setLayoutParams(new LinearLayout.LayoutParams(mIndicatorSize, mIndicatorSize));
        }
    }

    public OnPageControlClickListener getOnPageControlClickListener() {
        return mOnPageControlClickListener;
    }

    public void setOnPageControlClickListener(OnPageControlClickListener onPageControlClickListener) {
        mOnPageControlClickListener = onPageControlClickListener;
    }

    public interface OnPageControlClickListener {
        void goBackwards();

        void goForwards();
    }
}

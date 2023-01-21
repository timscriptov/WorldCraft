package com.solverlabs.worldcraft.dialog.tools.ui;

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
    private int mCurrentPage;
    private int mIndicatorSize;
    private OnPageControlClickListener mOnPageControlClickListener;
    private int mPageCount;

    public PageControl(Context context) {
        super(context);
        this.mIndicatorSize = 7;
        this.mPageCount = 0;
        this.mCurrentPage = 0;
        this.mOnPageControlClickListener = null;
        this.mContext = context;
        initPageControl();
    }

    public PageControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIndicatorSize = 7;
        this.mPageCount = 0;
        this.mCurrentPage = 0;
        this.mOnPageControlClickListener = null;
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initPageControl();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPageControl() {
        Log.i("PageControl", "Initialising PageControl");
        this.indicators = new ArrayList<>();
        this.activeDrawable = new ShapeDrawable();
        this.inactiveDrawable = new ShapeDrawable();
        this.activeDrawable.setBounds(0, 0, this.mIndicatorSize, this.mIndicatorSize);
        this.inactiveDrawable.setBounds(0, 0, this.mIndicatorSize, this.mIndicatorSize);
        Shape s1 = new OvalShape();
        s1.resize(this.mIndicatorSize, this.mIndicatorSize);
        Shape s2 = new OvalShape();
        s2.resize(this.mIndicatorSize, this.mIndicatorSize);
        int[] i = {16842808, 16842810};
        TypedArray a = this.mContext.getTheme().obtainStyledAttributes(i);
        ((ShapeDrawable) this.activeDrawable).getPaint().setColor(a.getColor(0, -12303292));
        ((ShapeDrawable) this.inactiveDrawable).getPaint().setColor(a.getColor(1, -3355444));
        ((ShapeDrawable) this.activeDrawable).setShape(s1);
        ((ShapeDrawable) this.inactiveDrawable).setShape(s2);
        this.mIndicatorSize = (int) (this.mIndicatorSize * getResources().getDisplayMetrics().density);
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
        return this.activeDrawable;
    }

    public void setActiveDrawable(Drawable d) {
        this.activeDrawable = d;
        this.indicators.get(this.mCurrentPage).setBackgroundDrawable(this.activeDrawable);
    }

    public Drawable getInactiveDrawable() {
        return this.inactiveDrawable;
    }

    public void setInactiveDrawable(Drawable d) {
        this.inactiveDrawable = d;
        for (int i = 0; i < this.mPageCount; i++) {
            this.indicators.get(i).setBackgroundDrawable(this.inactiveDrawable);
        }
        this.indicators.get(this.mCurrentPage).setBackgroundDrawable(this.activeDrawable);
    }

    public int getPageCount() {
        return this.mPageCount;
    }

    public void setPageCount(int pageCount) {
        this.mPageCount = pageCount;
        for (int i = 0; i < pageCount; i++) {
            ImageView imageView = new ImageView(this.mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(this.mIndicatorSize, this.mIndicatorSize);
            params.setMargins(this.mIndicatorSize / 2, this.mIndicatorSize, this.mIndicatorSize / 2, this.mIndicatorSize);
            imageView.setLayoutParams(params);
            imageView.setBackgroundDrawable(this.inactiveDrawable);
            this.indicators.add(imageView);
            addView(imageView);
        }
    }

    public int getCurrentPage() {
        return this.mCurrentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage < this.mPageCount) {
            this.indicators.get(this.mCurrentPage).setBackgroundDrawable(this.inactiveDrawable);
            this.indicators.get(currentPage).setBackgroundDrawable(this.activeDrawable);
            this.mCurrentPage = currentPage;
        }
    }

    public int getIndicatorSize() {
        return this.mIndicatorSize;
    }

    public void setIndicatorSize(int indicatorSize) {
        this.mIndicatorSize = indicatorSize;
        for (int i = 0; i < this.mPageCount; i++) {
            this.indicators.get(i).setLayoutParams(new LinearLayout.LayoutParams(this.mIndicatorSize, this.mIndicatorSize));
        }
    }

    public OnPageControlClickListener getOnPageControlClickListener() {
        return this.mOnPageControlClickListener;
    }

    public void setOnPageControlClickListener(OnPageControlClickListener onPageControlClickListener) {
        this.mOnPageControlClickListener = onPageControlClickListener;
    }

    public interface OnPageControlClickListener {
        void goBackwards();

        void goForwards();
    }
}

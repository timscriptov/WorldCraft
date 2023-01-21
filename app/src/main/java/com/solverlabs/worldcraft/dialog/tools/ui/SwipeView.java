package com.solverlabs.worldcraft.dialog.tools.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.Collection;

public class SwipeView extends HorizontalScrollView {
    private static int DEFAULT_SWIPE_THRESHOLD = 60;
    private final Context mContext;
    protected boolean mCallScrollToPageInOnLayout;
    private int SCREEN_WIDTH;
    private int mCurrentPage;
    private boolean mJustInterceptedAndIgnored;
    private LinearLayout mLinearLayout;
    private boolean mMostlyScrollingInX;
    private boolean mMostlyScrollingInY;
    private int mMotionStartX;
    private int mMotionStartY;
    private OnPageChangedListener mOnPageChangedListener;
    private View.OnTouchListener mOnTouchListener;
    private PageControl mPageControl;
    private int mPageWidth;
    private SwipeOnTouchListener mSwipeOnTouchListener;

    public SwipeView(Context context) {
        super(context);
        this.mMostlyScrollingInX = false;
        this.mMostlyScrollingInY = false;
        this.mJustInterceptedAndIgnored = false;
        this.mCallScrollToPageInOnLayout = false;
        this.mCurrentPage = 0;
        this.mPageWidth = 0;
        this.mOnPageChangedListener = null;
        this.mPageControl = null;
        this.mContext = context;
        initSwipeView();
    }

    public SwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMostlyScrollingInX = false;
        this.mMostlyScrollingInY = false;
        this.mJustInterceptedAndIgnored = false;
        this.mCallScrollToPageInOnLayout = false;
        this.mCurrentPage = 0;
        this.mPageWidth = 0;
        this.mOnPageChangedListener = null;
        this.mPageControl = null;
        this.mContext = context;
        initSwipeView();
    }

    public SwipeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMostlyScrollingInX = false;
        this.mMostlyScrollingInY = false;
        this.mJustInterceptedAndIgnored = false;
        this.mCallScrollToPageInOnLayout = false;
        this.mCurrentPage = 0;
        this.mPageWidth = 0;
        this.mOnPageChangedListener = null;
        this.mPageControl = null;
        this.mContext = context;
        initSwipeView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSwipeView() {
        Log.i("SwipeView", "Initialising SwipeView");
        this.mLinearLayout = new LinearLayout(this.mContext);
        this.mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        super.addView(this.mLinearLayout, -1, new FrameLayout.LayoutParams(-1, -1));
        setSmoothScrollingEnabled(true);
        setHorizontalFadingEdgeEnabled(false);
        setHorizontalScrollBarEnabled(false);
        Display display = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.SCREEN_WIDTH = display.getWidth();
        this.mPageWidth = this.SCREEN_WIDTH;
        this.mCurrentPage = 0;
        this.mSwipeOnTouchListener = new SwipeOnTouchListener();
        super.setOnTouchListener(this.mSwipeOnTouchListener);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return false;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        requestFocus();
    }

    public void addAllViews(@NonNull Collection<View> viewList) {
        for (View view : viewList) {
            ViewGroup.LayoutParams layoutParams = getChildLayoutParams(view);
            if (this.mLinearLayout.getChildCount() == 0) {
                layoutParams = getLeftMargin(layoutParams);
            } else if (this.mLinearLayout.getChildCount() == viewList.size() - 1) {
                layoutParams = getRightMargin(layoutParams);
            }
            addView(view, -1, layoutParams);
        }
    }

    @NonNull
    private LinearLayout.LayoutParams getRightMargin(ViewGroup.LayoutParams layoutParams) {
        LinearLayout.LayoutParams firstViewlayoutParams = new LinearLayout.LayoutParams(layoutParams);
        firstViewlayoutParams.rightMargin = getFirstOrLastMargin();
        return firstViewlayoutParams;
    }

    @NonNull
    private LinearLayout.LayoutParams getLeftMargin(ViewGroup.LayoutParams layoutParams) {
        LinearLayout.LayoutParams firstViewlayoutParams = new LinearLayout.LayoutParams(layoutParams);
        firstViewlayoutParams.leftMargin = getFirstOrLastMargin();
        return firstViewlayoutParams;
    }

    private int getFirstOrLastMargin() {
        return (this.SCREEN_WIDTH - this.mPageWidth) / 2;
    }

    @Override
    public void addView(View child) {
        addView(child, -1);
    }

    @Override
    public void addView(View child, int index) {
        addView(child, index, getChildLayoutParams(child));
    }

    @NonNull
    private ViewGroup.LayoutParams getChildLayoutParams(@NonNull View child) {
        if (child.getLayoutParams() == null) {
            return new FrameLayout.LayoutParams(this.mPageWidth, -1);
        }
        ViewGroup.LayoutParams params = child.getLayoutParams();
        params.width = this.mPageWidth;
        return params;
    }

    @Override
    public void addView(View child, @NonNull ViewGroup.LayoutParams params) {
        params.width = this.mPageWidth;
        addView(child, -1, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        requestLayout();
        invalidate();
        this.mLinearLayout.addView(child, index, params);
    }

    @Override
    public void removeAllViews() {
        this.mLinearLayout.removeAllViews();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mCallScrollToPageInOnLayout) {
            scrollToPage(this.mCurrentPage);
            this.mCallScrollToPageInOnLayout = false;
        }
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnTouchListener = onTouchListener;
    }

    public LinearLayout getChildContainer() {
        return this.mLinearLayout;
    }

    public int getSwipeThreshold() {
        return DEFAULT_SWIPE_THRESHOLD;
    }

    public void setSwipeThreshold(int swipeThreshold) {
        DEFAULT_SWIPE_THRESHOLD = swipeThreshold;
    }

    public int getCurrentPage() {
        return this.mCurrentPage;
    }

    public int getPageCount() {
        return this.mLinearLayout.getChildCount();
    }

    public void scrollToPage(int page) {
        scrollToPage(page, false);
    }

    public void smoothScrollToPage(int page) {
        scrollToPage(page, true);
    }

    private void scrollToPage(int page, boolean smooth) {
        int oldPage = this.mCurrentPage;
        if (page >= getPageCount() && getPageCount() > 0) {
            page--;
        } else if (page < 0) {
            page = 0;
        }
        if (smooth) {
            smoothScrollTo(this.mPageWidth * page, 0);
        } else {
            scrollTo(this.mPageWidth * page, 0);
        }
        this.mCurrentPage = page;
        if (this.mOnPageChangedListener != null && oldPage != page) {
            this.mOnPageChangedListener.onPageChanged(oldPage, page);
        }
        if (this.mPageControl != null && oldPage != page) {
            this.mPageControl.setCurrentPage(page);
        }
        this.mCallScrollToPageInOnLayout = !this.mCallScrollToPageInOnLayout;
    }

    public int setPageWidth(int pageWidth) {
        this.mPageWidth = pageWidth;
        return getFirstOrLastMargin();
    }

    public int calculatePageSize(@NonNull ViewGroup.MarginLayoutParams childLayoutParams) {
        return setPageWidth(childLayoutParams.leftMargin + childLayoutParams.width + childLayoutParams.rightMargin);
    }

    public int getPageWidth() {
        return this.mPageWidth;
    }

    public PageControl getPageControl() {
        return this.mPageControl;
    }

    public void setPageControl(@NonNull PageControl pageControl) {
        this.mPageControl = pageControl;
        pageControl.setPageCount(getPageCount());
        pageControl.setCurrentPage(this.mCurrentPage);
        pageControl.setOnPageControlClickListener(new PageControl.OnPageControlClickListener() {
            @Override
            public void goForwards() {
                smoothScrollToPage(mCurrentPage + 1);
            }

            @Override
            public void goBackwards() {
                smoothScrollToPage(mCurrentPage - 1);
            }
        });
    }

    public OnPageChangedListener getOnPageChangedListener() {
        return this.mOnPageChangedListener;
    }

    public void setOnPageChangedListener(OnPageChangedListener onPageChangedListener) {
        this.mOnPageChangedListener = onPageChangedListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        if (ev.getAction() == 0) {
            this.mMotionStartX = (int) ev.getX();
            this.mMotionStartY = (int) ev.getY();
            if (!this.mJustInterceptedAndIgnored) {
                this.mMostlyScrollingInX = false;
                this.mMostlyScrollingInY = false;
            }
        } else if (ev.getAction() == 2) {
            detectMostlyScrollingDirection(ev);
        }
        if (this.mMostlyScrollingInY) {
            return false;
        }
        if (this.mMostlyScrollingInX) {
            this.mJustInterceptedAndIgnored = true;
            return true;
        }
        return result;
    }

    private void detectMostlyScrollingDirection(MotionEvent ev) {
        if (!this.mMostlyScrollingInX && !this.mMostlyScrollingInY) {
            float xDistance = Math.abs(this.mMotionStartX - ev.getX());
            float yDistance = Math.abs(this.mMotionStartY - ev.getY());
            if (yDistance > xDistance + 5.0f) {
                this.mMostlyScrollingInY = true;
            } else if (xDistance > yDistance + 5.0f) {
                this.mMostlyScrollingInX = true;
            }
        }
    }

    public interface OnPageChangedListener {
        void onPageChanged(int i, int i2);
    }

    public class SwipeOnTouchListener implements View.OnTouchListener {
        private int mDistanceX;
        private boolean mFirstMotionEvent;
        private int mPreviousDirection;
        private boolean mSendingDummyMotionEvent;

        private SwipeOnTouchListener() {
            this.mSendingDummyMotionEvent = false;
            this.mFirstMotionEvent = true;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (((mOnTouchListener != null && !mJustInterceptedAndIgnored) || (mOnTouchListener != null && this.mSendingDummyMotionEvent)) && mOnTouchListener.onTouch(v, event)) {
                if (event.getAction() == 1) {
                    actionUp(event);
                    return true;
                }
                return true;
            } else if (this.mSendingDummyMotionEvent) {
                this.mSendingDummyMotionEvent = false;
                return false;
            } else {
                switch (event.getAction()) {
                    case 0:
                        return actionDown(event);
                    case 1:
                        return actionUp(event);
                    case 2:
                        return actionMove(event);
                    default:
                        return false;
                }
            }
        }

        private boolean actionDown(@NonNull MotionEvent event) {
            mMotionStartX = (int) event.getX();
            mMotionStartY = (int) event.getY();
            this.mFirstMotionEvent = false;
            return false;
        }

        private boolean actionMove(@NonNull MotionEvent event) {
            int newDirection;
            int newDistance = mMotionStartX - ((int) event.getX());
            if (newDistance < 0) {
                newDirection = this.mDistanceX + 4 <= newDistance ? 1 : -1;
            } else {
                newDirection = this.mDistanceX + (-4) <= newDistance ? 1 : -1;
            }
            if (newDirection != this.mPreviousDirection && !this.mFirstMotionEvent) {
                mMotionStartX = (int) event.getX();
                this.mDistanceX = mMotionStartX - ((int) event.getX());
            } else {
                this.mDistanceX = newDistance;
            }
            this.mPreviousDirection = newDirection;
            if (mJustInterceptedAndIgnored) {
                this.mSendingDummyMotionEvent = true;
                dispatchTouchEvent(MotionEvent.obtain(event.getDownTime(), event.getEventTime(), 0, mMotionStartX, mMotionStartY, event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags()));
                mJustInterceptedAndIgnored = false;
                return true;
            }
            return false;
        }

        private boolean actionUp(MotionEvent event) {
            float edgePosition;
            float fingerUpPosition = getScrollX();
            float numberOfPages = mLinearLayout.getMeasuredWidth() / mPageWidth;
            float fingerUpPage = fingerUpPosition / mPageWidth;
            if (this.mPreviousDirection != 1) {
                if (this.mDistanceX < (-SwipeView.DEFAULT_SWIPE_THRESHOLD)) {
                    edgePosition = ((int) fingerUpPage) * mPageWidth;
                } else {
                    edgePosition = Math.round(fingerUpPage) == 0 ? ((int) fingerUpPage) * mPageWidth : mCurrentPage * mPageWidth;
                }
            } else {
                edgePosition = this.mDistanceX > SwipeView.DEFAULT_SWIPE_THRESHOLD ? ((float) mCurrentPage) < numberOfPages - 1.0f ? ((int) (fingerUpPage + 1.0f)) * mPageWidth : mCurrentPage * mPageWidth : ((float) Math.round(fingerUpPage)) == numberOfPages - 1.0f ? ((int) (fingerUpPage + 1.0f)) * mPageWidth : mCurrentPage * mPageWidth;
            }
            smoothScrollToPage(((int) edgePosition) / mPageWidth);
            this.mFirstMotionEvent = true;
            this.mDistanceX = 0;
            mMostlyScrollingInX = false;
            mMostlyScrollingInY = false;
            return true;
        }
    }
}

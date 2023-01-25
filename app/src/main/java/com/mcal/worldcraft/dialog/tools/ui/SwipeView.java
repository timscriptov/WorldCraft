package com.mcal.worldcraft.dialog.tools.ui;

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
    protected boolean mCallScrollToPageInOnLayout = false;
    private int SCREEN_WIDTH;
    private int mCurrentPage = 0;
    private boolean mJustInterceptedAndIgnored = false;
    private LinearLayout mLinearLayout;
    private boolean mMostlyScrollingInX = false;
    private boolean mMostlyScrollingInY = false;
    private int mMotionStartX;
    private int mMotionStartY;
    private OnPageChangedListener mOnPageChangedListener = null;
    private View.OnTouchListener mOnTouchListener;
    private PageControl mPageControl = null;
    private int mPageWidth = 0;
    private SwipeOnTouchListener mSwipeOnTouchListener;

    public SwipeView(Context context) {
        super(context);
        mContext = context;
        initSwipeView();
    }

    public SwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initSwipeView();
    }

    public SwipeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initSwipeView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSwipeView() {
        Log.i("SwipeView", "Initialising SwipeView");
        mLinearLayout = new LinearLayout(mContext);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        super.addView(mLinearLayout, -1, new FrameLayout.LayoutParams(-1, -1));
        setSmoothScrollingEnabled(true);
        setHorizontalFadingEdgeEnabled(false);
        setHorizontalScrollBarEnabled(false);
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        SCREEN_WIDTH = display.getWidth();
        mPageWidth = SCREEN_WIDTH;
        mCurrentPage = 0;
        mSwipeOnTouchListener = new SwipeOnTouchListener();
        super.setOnTouchListener(mSwipeOnTouchListener);
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
            if (mLinearLayout.getChildCount() == 0) {
                layoutParams = getLeftMargin(layoutParams);
            } else if (mLinearLayout.getChildCount() == viewList.size() - 1) {
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
        return (SCREEN_WIDTH - mPageWidth) / 2;
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
            return new FrameLayout.LayoutParams(mPageWidth, -1);
        }
        ViewGroup.LayoutParams params = child.getLayoutParams();
        params.width = mPageWidth;
        return params;
    }

    @Override
    public void addView(View child, @NonNull ViewGroup.LayoutParams params) {
        params.width = mPageWidth;
        addView(child, -1, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        requestLayout();
        invalidate();
        mLinearLayout.addView(child, index, params);
    }

    @Override
    public void removeAllViews() {
        mLinearLayout.removeAllViews();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mCallScrollToPageInOnLayout) {
            scrollToPage(mCurrentPage);
            mCallScrollToPageInOnLayout = false;
        }
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

    public LinearLayout getChildContainer() {
        return mLinearLayout;
    }

    public int getSwipeThreshold() {
        return DEFAULT_SWIPE_THRESHOLD;
    }

    public void setSwipeThreshold(int swipeThreshold) {
        DEFAULT_SWIPE_THRESHOLD = swipeThreshold;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getPageCount() {
        return mLinearLayout.getChildCount();
    }

    public void scrollToPage(int page) {
        scrollToPage(page, false);
    }

    public void smoothScrollToPage(int page) {
        scrollToPage(page, true);
    }

    private void scrollToPage(int page, boolean smooth) {
        int oldPage = mCurrentPage;
        if (page >= getPageCount() && getPageCount() > 0) {
            page--;
        } else if (page < 0) {
            page = 0;
        }
        if (smooth) {
            smoothScrollTo(mPageWidth * page, 0);
        } else {
            scrollTo(mPageWidth * page, 0);
        }
        mCurrentPage = page;
        if (mOnPageChangedListener != null && oldPage != page) {
            mOnPageChangedListener.onPageChanged(oldPage, page);
        }
        if (mPageControl != null && oldPage != page) {
            mPageControl.setCurrentPage(page);
        }
        mCallScrollToPageInOnLayout = !mCallScrollToPageInOnLayout;
    }

    public int setPageWidth(int pageWidth) {
        mPageWidth = pageWidth;
        return getFirstOrLastMargin();
    }

    public int calculatePageSize(@NonNull ViewGroup.MarginLayoutParams childLayoutParams) {
        return setPageWidth(childLayoutParams.leftMargin + childLayoutParams.width + childLayoutParams.rightMargin);
    }

    public int getPageWidth() {
        return mPageWidth;
    }

    public PageControl getPageControl() {
        return mPageControl;
    }

    public void setPageControl(@NonNull PageControl pageControl) {
        mPageControl = pageControl;
        pageControl.setPageCount(getPageCount());
        pageControl.setCurrentPage(mCurrentPage);
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
        return mOnPageChangedListener;
    }

    public void setOnPageChangedListener(OnPageChangedListener onPageChangedListener) {
        mOnPageChangedListener = onPageChangedListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        if (ev.getAction() == 0) {
            mMotionStartX = (int) ev.getX();
            mMotionStartY = (int) ev.getY();
            if (!mJustInterceptedAndIgnored) {
                mMostlyScrollingInX = false;
                mMostlyScrollingInY = false;
            }
        } else if (ev.getAction() == 2) {
            detectMostlyScrollingDirection(ev);
        }
        if (mMostlyScrollingInY) {
            return false;
        }
        if (mMostlyScrollingInX) {
            mJustInterceptedAndIgnored = true;
            return true;
        }
        return result;
    }

    private void detectMostlyScrollingDirection(MotionEvent ev) {
        if (!mMostlyScrollingInX && !mMostlyScrollingInY) {
            float xDistance = Math.abs(mMotionStartX - ev.getX());
            float yDistance = Math.abs(mMotionStartY - ev.getY());
            if (yDistance > xDistance + 5.0f) {
                mMostlyScrollingInY = true;
            } else if (xDistance > yDistance + 5.0f) {
                mMostlyScrollingInX = true;
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
            mSendingDummyMotionEvent = false;
            mFirstMotionEvent = true;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (((mOnTouchListener != null && !mJustInterceptedAndIgnored) || (mOnTouchListener != null && mSendingDummyMotionEvent)) && mOnTouchListener.onTouch(v, event)) {
                if (event.getAction() == 1) {
                    actionUp(event);
                    return true;
                }
                return true;
            } else if (mSendingDummyMotionEvent) {
                mSendingDummyMotionEvent = false;
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
            mFirstMotionEvent = false;
            return false;
        }

        private boolean actionMove(@NonNull MotionEvent event) {
            int newDirection;
            int newDistance = mMotionStartX - ((int) event.getX());
            if (newDistance < 0) {
                newDirection = mDistanceX + 4 <= newDistance ? 1 : -1;
            } else {
                newDirection = mDistanceX + (-4) <= newDistance ? 1 : -1;
            }
            if (newDirection != mPreviousDirection && !mFirstMotionEvent) {
                mMotionStartX = (int) event.getX();
                mDistanceX = mMotionStartX - ((int) event.getX());
            } else {
                mDistanceX = newDistance;
            }
            mPreviousDirection = newDirection;
            if (mJustInterceptedAndIgnored) {
                mSendingDummyMotionEvent = true;
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
            if (mPreviousDirection != 1) {
                if (mDistanceX < (-SwipeView.DEFAULT_SWIPE_THRESHOLD)) {
                    edgePosition = ((int) fingerUpPage) * mPageWidth;
                } else {
                    edgePosition = Math.round(fingerUpPage) == 0 ? ((int) fingerUpPage) * mPageWidth : mCurrentPage * mPageWidth;
                }
            } else {
                edgePosition = mDistanceX > SwipeView.DEFAULT_SWIPE_THRESHOLD ? ((float) mCurrentPage) < numberOfPages - 1.0f ? ((int) (fingerUpPage + 1.0f)) * mPageWidth : mCurrentPage * mPageWidth : ((float) Math.round(fingerUpPage)) == numberOfPages - 1.0f ? ((int) (fingerUpPage + 1.0f)) * mPageWidth : mCurrentPage * mPageWidth;
            }
            smoothScrollToPage(((int) edgePosition) / mPageWidth);
            mFirstMotionEvent = true;
            mDistanceX = 0;
            mMostlyScrollingInX = false;
            mMostlyScrollingInY = false;
            return true;
        }
    }
}

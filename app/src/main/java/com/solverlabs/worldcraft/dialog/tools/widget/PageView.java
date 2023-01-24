package com.solverlabs.worldcraft.dialog.tools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.solverlabs.worldcraft.dialog.tools.ui.SwipeView;

public class PageView extends BounceSwipeView {
    private Adapter mAdapter;
    private boolean mCarouselMode = false;
    private int mCurrentPage;
    private int mOffset;
    private SwipeView.OnPageChangedListener mOnPageChangedListener;

    public PageView(Context context) {
        super(context);
        initView();
    }

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        setBounceEnabled(false);
    }

    public int getRealCurrentPage() {
        return mCurrentPage;
    }

    @Override
    public int setPageWidth(int pageWidth) {
        mOffset = super.setPageWidth(pageWidth);
        return mOffset;
    }

    public boolean getCarouselEnabled() {
        return mCarouselMode;
    }

    public void setCarouselEnabled(boolean enabled) {
        mCarouselMode = enabled;
        setBounceEnabled(!enabled);
    }

    public void setAdapter(BaseAdapter adapter, final int startPosition) {
        mAdapter = adapter;
        if (mAdapter != null) {
            mCurrentPage = startPosition;
            fillCarousel(startPosition);
            post(() -> {
                if (mCarouselMode || startPosition != 0) {
                    if (mCarouselMode || startPosition != mAdapter.getCount() - 1) {
                        PageView.super.scrollToPage(1);
                    } else {
                        PageView.super.scrollToPage(2);
                    }
                    return;
                }
                PageView.super.scrollToPage(0);
            });
            if (mAdapter.getCount() <= 1) {
                setBounceEnabled(true);
            }
        }
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(BaseAdapter adapter) {
        setAdapter(adapter, 0);
    }

    private int getAdapterPageCount() {
        if (mAdapter != null) {
            if (mAdapter.getCount() == 2 && mCarouselMode) {
                return 4;
            }
            return mAdapter.getCount();
        }
        return -1;
    }

    private void emptyCarousel() {
        getChildContainer().removeAllViews();
    }

    private void fillCarousel(int page) {
        emptyCarousel();
        if (mAdapter.getCount() == 1) {
            loadPage(0, 0, null);
        } else if (mAdapter.getCount() == 2) {
            if (!mCarouselMode) {
                loadPage(0, 0, null);
                loadPage(1, 1, null);
            } else if (page == 0) {
                loadPage(1, 0, null);
                loadPage(0, 1, null);
                loadPage(1, 2, null);
            } else {
                loadPage(0, 0, null);
                loadPage(1, 1, null);
                loadPage(0, 2, null);
            }
        } else if (mAdapter.getCount() > 2) {
            if (page == 0 && mCarouselMode) {
                loadPage(mAdapter.getCount() - 1, 0, null);
                loadPage(0, 1, null);
                loadPage(1, 2, null);
            } else if (page == 0) {
                loadPage(0, 0, null);
                loadPage(1, 1, null);
                loadPage(2, 2, null);
            } else if (page == mAdapter.getCount() - 1 && mCarouselMode) {
                loadPage(page - 1, 0, null);
                loadPage(mAdapter.getCount() - 1, 1, null);
                loadPage(0, 2, null);
            } else if (page == mAdapter.getCount() - 1 && !mCarouselMode) {
                loadPage(mAdapter.getCount() - 3, 0, null);
                loadPage(mAdapter.getCount() - 2, 1, null);
                loadPage(mAdapter.getCount() - 1, 2, null);
            } else {
                loadPage(page - 1, 0, null);
                loadPage(page, 1, null);
                loadPage(page + 1, 2, null);
            }
            resetMargins();
        }
    }

    @Override
    public SwipeView.OnPageChangedListener getOnPageChangedListener() {
        return mOnPageChangedListener;
    }

    @Override
    public void setOnPageChangedListener(SwipeView.OnPageChangedListener onPageChangedListener) {
        mOnPageChangedListener = onPageChangedListener;
    }

    private void loadPage(int page, int position, View convertView) {
        if (mAdapter.getCount() == 2 && page > 1) {
            page -= 2;
        }
        super.addView(mAdapter.getView(page, convertView, getChildContainer()), position);
    }

    @Override
    public void smoothScrollToPage(int page) {
        scrollToPage(page, true);
    }

    @Override
    public void scrollToPage(int page) {
        scrollToPage(page, false);
    }

    private void scrollToPage(int page, boolean smooth) {
        if ((!mCarouselMode && getCurrentPage() == getPageCount() - 1 && page >= getCurrentPage()) || (!mCarouselMode && getCurrentPage() == 0 && page <= 0)) {
            doAtEdgeAnimation();
        } else if (getCurrentPage() != page) {
            rearrangePages(getCurrentPage(), page, smooth);
            notifiyAssignedOnPageChangedListener(page);
        }
    }

    private void notifiyAssignedOnPageChangedListener(int newPage) {
        if (mOnPageChangedListener != null) {
            if (mCarouselMode && mCurrentPage == 0 && newPage == 2) {
                mOnPageChangedListener.onPageChanged(mAdapter.getCount() - 1, mCurrentPage);
            } else if (mCarouselMode && mCurrentPage == mAdapter.getCount() - 1 && newPage == 0) {
                mOnPageChangedListener.onPageChanged(0, mCurrentPage);
            } else if (!mCarouselMode && mCurrentPage == 1 && newPage == 1) {
                mOnPageChangedListener.onPageChanged(0, 1);
            } else if (!mCarouselMode && mCurrentPage == mAdapter.getCount() - 1 && newPage == mAdapter.getCount() - 1) {
                mOnPageChangedListener.onPageChanged(mCurrentPage, mAdapter.getCount() - 2);
            } else if (newPage == 2) {
                mOnPageChangedListener.onPageChanged(mCurrentPage - 1, mCurrentPage);
            } else {
                mOnPageChangedListener.onPageChanged(mCurrentPage + 1, mCurrentPage);
            }
        }
    }

    private void rearrangePages(int oldPage, int newPage, final boolean smooth) {
        final int pageToScrollTo;
        if (getAdapterPageCount() > 1) {
            if (newPage >= oldPage + 1) {
                if (mCarouselMode || (mCurrentPage < getAdapterPageCount() - 2 && mCurrentPage > 0)) {
                    mCallScrollToPageInOnLayout = false;
                    scrollTo(getScrollX() - getPageWidth(), 0);
                    forwardsMove();
                    pageToScrollTo = 1;
                } else if (mCurrentPage <= 0) {
                    mCurrentPage = 1;
                    pageToScrollTo = 1;
                } else {
                    mCurrentPage = getAdapterPageCount() - 1;
                    pageToScrollTo = 2;
                }
            } else if (newPage <= oldPage - 1) {
                if (mCarouselMode || (mCurrentPage > 1 && mCurrentPage < getAdapterPageCount() - 1)) {
                    mCallScrollToPageInOnLayout = false;
                    scrollTo(getScrollX() + getPageWidth(), 0);
                    backwardsMove();
                    pageToScrollTo = 1;
                } else if (mCurrentPage >= getAdapterPageCount() - 1) {
                    mCurrentPage = getAdapterPageCount() - 2;
                    pageToScrollTo = 1;
                } else {
                    mCurrentPage = 0;
                    pageToScrollTo = 0;
                }
            } else {
                pageToScrollTo = 1;
            }
            post(() -> {
                if (smooth) {
                    PageView.super.smoothScrollToPage(pageToScrollTo);
                } else {
                    PageView.super.scrollToPage(pageToScrollTo);
                }
            });
        }
    }

    private void forwardsMove() {
        if (mCurrentPage < getAdapterPageCount() - 1) {
            mCurrentPage++;
        } else {
            mCurrentPage = 0;
        }
        if (mCurrentPage < getAdapterPageCount() - 1) {
            forwardsRearrange(mCurrentPage + 1);
        } else {
            forwardsRearrange(0);
        }
    }

    private void backwardsMove() {
        if (mCurrentPage > 0) {
            mCurrentPage--;
        } else {
            mCurrentPage = getAdapterPageCount() - 1;
        }
        if (mCurrentPage > 0) {
            backwardsRearrange(mCurrentPage - 1);
        } else {
            backwardsRearrange(getAdapterPageCount() - 1);
        }
    }

    private void forwardsRearrange(int frontPageToLoad) {
        View convertView = getChildContainer().getChildAt(0);
        getChildContainer().removeViewAt(0);
        loadPage(frontPageToLoad, 2, convertView);
        resetMargins();
    }

    private void backwardsRearrange(int backPageToLoad) {
        View convertView = getChildContainer().getChildAt(2);
        getChildContainer().removeViewAt(2);
        loadPage(backPageToLoad, 0, convertView);
        resetMargins();
    }

    private void resetMargins() {
        if (mOffset > 0) {
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(0).getLayoutParams()).leftMargin = mOffset;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(0).getLayoutParams()).rightMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(1).getLayoutParams()).leftMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(1).getLayoutParams()).rightMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(2).getLayoutParams()).leftMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(2).getLayoutParams()).rightMargin = mOffset;
        }
    }

    public void itemAddedToAdapter(int position) {
        if (position <= mCurrentPage) {
            mCurrentPage++;
        }
        if (mAdapter.getCount() > 1) {
            setBounceEnabled(false);
        }
        refill(position);
    }

    public void itemRemovedFromAdapter(int position) {
        if (position <= mCurrentPage && mCurrentPage != 0) {
            mCurrentPage--;
        }
        refill(position);
    }

    private void refill(int position) {
        if (mCurrentPage == 0) {
            if (position == getAdapterPageCount() - 1 || position <= mCurrentPage + 1) {
                fillCarousel(mCurrentPage);
            }
        } else if (mCurrentPage == getAdapterPageCount() - 1) {
            if (position >= mCurrentPage || position == 0) {
                fillCarousel(mCurrentPage);
            }
        } else if (position >= mCurrentPage - 1 && position <= mCurrentPage + 1) {
            fillCarousel(mCurrentPage);
        }
    }
}

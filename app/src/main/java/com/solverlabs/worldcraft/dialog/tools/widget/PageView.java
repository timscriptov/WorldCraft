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
    private boolean mCarouselMode;
    private int mCurrentPage;
    private int mOffset;
    private SwipeView.OnPageChangedListener mOnPageChangedListener;

    public PageView(Context context) {
        super(context);
        this.mCarouselMode = false;
        initView();
    }

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCarouselMode = false;
        initView();
    }

    public PageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCarouselMode = false;
        initView();
    }

    private void initView() {
        setBounceEnabled(false);
    }

    public int getRealCurrentPage() {
        return this.mCurrentPage;
    }

    @Override
    public int setPageWidth(int pageWidth) {
        this.mOffset = super.setPageWidth(pageWidth);
        return this.mOffset;
    }

    public void setCarouselEnabled(boolean enabled) {
        this.mCarouselMode = enabled;
        setBounceEnabled(!enabled);
    }

    public boolean getCarouselEnabled() {
        return this.mCarouselMode;
    }

    public void setAdapter(BaseAdapter adapter) {
        setAdapter(adapter, 0);
    }

    public void setAdapter(BaseAdapter adapter, final int startPosition) {
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            this.mCurrentPage = startPosition;
            fillCarousel(startPosition);
            post(() -> {
                if (PageView.this.mCarouselMode || startPosition != 0) {
                    if (PageView.this.mCarouselMode || startPosition != PageView.this.mAdapter.getCount() - 1) {
                        PageView.super.scrollToPage(1);
                    } else {
                        PageView.super.scrollToPage(2);
                    }
                    return;
                }
                PageView.super.scrollToPage(0);
            });
            if (this.mAdapter.getCount() <= 1) {
                setBounceEnabled(true);
            }
        }
    }

    public Adapter getAdapter() {
        return this.mAdapter;
    }

    private int getAdapterPageCount() {
        if (this.mAdapter != null) {
            if (this.mAdapter.getCount() == 2 && this.mCarouselMode) {
                return 4;
            }
            return this.mAdapter.getCount();
        }
        return -1;
    }

    private void emptyCarousel() {
        getChildContainer().removeAllViews();
    }

    private void fillCarousel(int page) {
        emptyCarousel();
        if (this.mAdapter.getCount() == 1) {
            loadPage(0, 0, null);
        } else if (this.mAdapter.getCount() == 2) {
            if (!this.mCarouselMode) {
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
        } else if (this.mAdapter.getCount() > 2) {
            if (page == 0 && this.mCarouselMode) {
                loadPage(this.mAdapter.getCount() - 1, 0, null);
                loadPage(0, 1, null);
                loadPage(1, 2, null);
            } else if (page == 0) {
                loadPage(0, 0, null);
                loadPage(1, 1, null);
                loadPage(2, 2, null);
            } else if (page == this.mAdapter.getCount() - 1 && this.mCarouselMode) {
                loadPage(page - 1, 0, null);
                loadPage(this.mAdapter.getCount() - 1, 1, null);
                loadPage(0, 2, null);
            } else if (page == this.mAdapter.getCount() - 1 && !this.mCarouselMode) {
                loadPage(this.mAdapter.getCount() - 3, 0, null);
                loadPage(this.mAdapter.getCount() - 2, 1, null);
                loadPage(this.mAdapter.getCount() - 1, 2, null);
            } else {
                loadPage(page - 1, 0, null);
                loadPage(page, 1, null);
                loadPage(page + 1, 2, null);
            }
            resetMargins();
        }
    }

    @Override
    public void setOnPageChangedListener(SwipeView.OnPageChangedListener onPageChangedListener) {
        this.mOnPageChangedListener = onPageChangedListener;
    }

    @Override
    public SwipeView.OnPageChangedListener getOnPageChangedListener() {
        return this.mOnPageChangedListener;
    }

    private void loadPage(int page, int position, View convertView) {
        if (this.mAdapter.getCount() == 2 && page > 1) {
            page -= 2;
        }
        super.addView(this.mAdapter.getView(page, convertView, getChildContainer()), position);
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
        if ((!this.mCarouselMode && getCurrentPage() == getPageCount() - 1 && page >= getCurrentPage()) || (!this.mCarouselMode && getCurrentPage() == 0 && page <= 0)) {
            doAtEdgeAnimation();
        } else if (getCurrentPage() != page) {
            rearrangePages(getCurrentPage(), page, smooth);
            notifiyAssignedOnPageChangedListener(page);
        }
    }

    private void notifiyAssignedOnPageChangedListener(int newPage) {
        if (this.mOnPageChangedListener != null) {
            if (this.mCarouselMode && this.mCurrentPage == 0 && newPage == 2) {
                this.mOnPageChangedListener.onPageChanged(this.mAdapter.getCount() - 1, this.mCurrentPage);
            } else if (this.mCarouselMode && this.mCurrentPage == this.mAdapter.getCount() - 1 && newPage == 0) {
                this.mOnPageChangedListener.onPageChanged(0, this.mCurrentPage);
            } else if (!this.mCarouselMode && this.mCurrentPage == 1 && newPage == 1) {
                this.mOnPageChangedListener.onPageChanged(0, 1);
            } else if (!this.mCarouselMode && this.mCurrentPage == this.mAdapter.getCount() - 1 && newPage == this.mAdapter.getCount() - 1) {
                this.mOnPageChangedListener.onPageChanged(this.mCurrentPage, this.mAdapter.getCount() - 2);
            } else if (newPage == 2) {
                this.mOnPageChangedListener.onPageChanged(this.mCurrentPage - 1, this.mCurrentPage);
            } else {
                this.mOnPageChangedListener.onPageChanged(this.mCurrentPage + 1, this.mCurrentPage);
            }
        }
    }

    private void rearrangePages(int oldPage, int newPage, final boolean smooth) {
        final int pageToScrollTo;
        if (getAdapterPageCount() > 1) {
            if (newPage >= oldPage + 1) {
                if (this.mCarouselMode || (this.mCurrentPage < getAdapterPageCount() - 2 && this.mCurrentPage > 0)) {
                    this.mCallScrollToPageInOnLayout = false;
                    scrollTo(getScrollX() - getPageWidth(), 0);
                    forwardsMove();
                    pageToScrollTo = 1;
                } else if (this.mCurrentPage <= 0) {
                    this.mCurrentPage = 1;
                    pageToScrollTo = 1;
                } else {
                    this.mCurrentPage = getAdapterPageCount() - 1;
                    pageToScrollTo = 2;
                }
            } else if (newPage <= oldPage - 1) {
                if (this.mCarouselMode || (this.mCurrentPage > 1 && this.mCurrentPage < getAdapterPageCount() - 1)) {
                    this.mCallScrollToPageInOnLayout = false;
                    scrollTo(getScrollX() + getPageWidth(), 0);
                    backwardsMove();
                    pageToScrollTo = 1;
                } else if (this.mCurrentPage >= getAdapterPageCount() - 1) {
                    this.mCurrentPage = getAdapterPageCount() - 2;
                    pageToScrollTo = 1;
                } else {
                    this.mCurrentPage = 0;
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
        if (this.mCurrentPage < getAdapterPageCount() - 1) {
            this.mCurrentPage++;
        } else {
            this.mCurrentPage = 0;
        }
        if (this.mCurrentPage < getAdapterPageCount() - 1) {
            forwardsRearrange(this.mCurrentPage + 1);
        } else {
            forwardsRearrange(0);
        }
    }

    private void backwardsMove() {
        if (this.mCurrentPage > 0) {
            this.mCurrentPage--;
        } else {
            this.mCurrentPage = getAdapterPageCount() - 1;
        }
        if (this.mCurrentPage > 0) {
            backwardsRearrange(this.mCurrentPage - 1);
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
        if (this.mOffset > 0) {
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(0).getLayoutParams()).leftMargin = this.mOffset;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(0).getLayoutParams()).rightMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(1).getLayoutParams()).leftMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(1).getLayoutParams()).rightMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(2).getLayoutParams()).leftMargin = 0;
            ((LinearLayout.LayoutParams) getChildContainer().getChildAt(2).getLayoutParams()).rightMargin = this.mOffset;
        }
    }

    public void itemAddedToAdapter(int position) {
        if (position <= this.mCurrentPage) {
            this.mCurrentPage++;
        }
        if (this.mAdapter.getCount() > 1) {
            setBounceEnabled(false);
        }
        refill(position);
    }

    public void itemRemovedFromAdapter(int position) {
        if (position <= this.mCurrentPage && this.mCurrentPage != 0) {
            this.mCurrentPage--;
        }
        refill(position);
    }

    private void refill(int position) {
        if (this.mCurrentPage == 0) {
            if (position == getAdapterPageCount() - 1 || position <= this.mCurrentPage + 1) {
                fillCarousel(this.mCurrentPage);
            }
        } else if (this.mCurrentPage == getAdapterPageCount() - 1) {
            if (position >= this.mCurrentPage || position == 0) {
                fillCarousel(this.mCurrentPage);
            }
        } else if (position >= this.mCurrentPage - 1 && position <= this.mCurrentPage + 1) {
            fillCarousel(this.mCurrentPage);
        }
    }
}

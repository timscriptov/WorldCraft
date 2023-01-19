package com.solverlabs.worldcraft.dialog.tools.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.solverlabs.worldcraft.dialog.tools.ui.SwipeView;
import com.solverlabs.worldcraft.dialog.tools.util.AnimationUtil;


public class BounceSwipeView extends SwipeView {
    private static final int ANIMATION_DURATION = 120;
    private static final boolean BOUNCING_ON_LEFT = true;
    private static final boolean BOUNCING_ON_RIGHT = false;
    private static final int FRAME_DURATION = 30;
    private static final int NUMBER_OF_FRAMES = 4;
    Handler mEaseAnimationFrameHandler;
    private boolean mAtEdge;
    private float mAtEdgePreviousPosition;
    private float mAtEdgeStartPosition;
    private boolean mBounceEnabled;
    private boolean mBouncingSide;
    private Context mContext;
    private int mCurrentAnimationFrame;
    private View.OnTouchListener mOnTouchListener;
    private int mPaddingChange;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingStartValue;
    private SharedPreferences mSharedPreferences;

    public BounceSwipeView(Context context) {
        super(context);
        this.mAtEdge = false;
        this.mBounceEnabled = true;
        this.mContext = context;
        initBounceSwipeView();
    }

    public BounceSwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAtEdge = false;
        this.mBounceEnabled = true;
        this.mContext = context;
        initBounceSwipeView();
    }

    public BounceSwipeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAtEdge = false;
        this.mBounceEnabled = true;
        this.mContext = context;
        initBounceSwipeView();
    }

    static /* synthetic */ int access$108(BounceSwipeView x0) {
        int i = x0.mCurrentAnimationFrame;
        x0.mCurrentAnimationFrame = i + 1;
        return i;
    }

    private void initBounceSwipeView() {
        super.setOnTouchListener(new BounceViewOnTouchListener());
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.mEaseAnimationFrameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int newPadding = AnimationUtil.quadraticOutEase(BounceSwipeView.this.mCurrentAnimationFrame, BounceSwipeView.this.mPaddingStartValue, -BounceSwipeView.this.mPaddingChange, 4.0f);
                if (BounceSwipeView.this.mBouncingSide) {
                    BounceSwipeView.super.setPadding(newPadding, BounceSwipeView.this.getPaddingTop(), BounceSwipeView.this.getPaddingRight(), BounceSwipeView.this.getPaddingBottom());
                } else if (!BounceSwipeView.this.mBouncingSide) {
                    BounceSwipeView.super.setPadding(BounceSwipeView.this.getPaddingLeft(), BounceSwipeView.this.getPaddingTop(), newPadding, BounceSwipeView.this.getPaddingBottom());
                }
                BounceSwipeView.access$108(BounceSwipeView.this);
                if (BounceSwipeView.this.mCurrentAnimationFrame <= 4) {
                    BounceSwipeView.this.mEaseAnimationFrameHandler.sendEmptyMessageDelayed(0, 30L);
                }
            }
        };
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        this.mPaddingLeft = left;
        this.mPaddingRight = right;
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnTouchListener = onTouchListener;
    }

    public boolean getBounceEnabled() {
        return this.mBounceEnabled;
    }

    public void setBounceEnabled(boolean enabled) {
        this.mBounceEnabled = enabled;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doBounceBackEaseAnimation() {
        if (this.mBouncingSide) {
            this.mPaddingChange = getPaddingLeft() - this.mPaddingLeft;
            this.mPaddingStartValue = getPaddingLeft();
        } else if (!this.mBouncingSide) {
            this.mPaddingChange = getPaddingRight() - this.mPaddingRight;
            this.mPaddingStartValue = getPaddingRight();
        }
        this.mCurrentAnimationFrame = 0;
        this.mEaseAnimationFrameHandler.removeMessages(0);
        this.mEaseAnimationFrameHandler.sendEmptyMessage(0);
    }

    public void doAtEdgeAnimation() {
        if (getCurrentPage() == 0) {
            this.mBouncingSide = true;
            super.setPadding(getPaddingLeft() + 50, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        } else if (getCurrentPage() == getPageCount() - 1) {
            this.mBouncingSide = false;
            super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + 50, getPaddingBottom());
            scrollTo(getScrollX() + 50, getScrollY());
        }
        doBounceBackEaseAnimation();
    }

    /* JADX INFO: Access modifiers changed from: private */

    public class BounceViewOnTouchListener implements View.OnTouchListener {
        private BounceViewOnTouchListener() {
        }

        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            if (BounceSwipeView.this.mOnTouchListener == null || !BounceSwipeView.this.mOnTouchListener.onTouch(view, ev)) {
                if (BounceSwipeView.this.mBounceEnabled) {
                    switch (ev.getAction()) {
                        case 1:
                            if (BounceSwipeView.this.mAtEdge) {
                                BounceSwipeView.this.mAtEdge = false;
                                BounceSwipeView.this.mAtEdgePreviousPosition = 0.0f;
                                BounceSwipeView.this.mAtEdgeStartPosition = 0.0f;
                                BounceSwipeView.this.doBounceBackEaseAnimation();
                                return true;
                            }
                            break;
                        case 2:
                            int maxScrollAmount = ((BounceSwipeView.this.getPageCount() - 1) * BounceSwipeView.this.getPageWidth()) - (BounceSwipeView.this.getPageWidth() % 2);
                            if ((BounceSwipeView.this.getScrollX() == 0 && !BounceSwipeView.this.mAtEdge) || (BounceSwipeView.this.getScrollX() == maxScrollAmount && !BounceSwipeView.this.mAtEdge)) {
                                BounceSwipeView.this.mAtEdge = true;
                                BounceSwipeView.this.mAtEdgeStartPosition = ev.getX();
                                BounceSwipeView.this.mAtEdgePreviousPosition = ev.getX();
                                break;
                            } else if (BounceSwipeView.this.getScrollX() == 0) {
                                BounceSwipeView.this.mAtEdgePreviousPosition = ev.getX();
                                BounceSwipeView.this.mBouncingSide = true;
                                BounceSwipeView.super.setPadding(((int) (BounceSwipeView.this.mAtEdgePreviousPosition - BounceSwipeView.this.mAtEdgeStartPosition)) / 2, BounceSwipeView.this.getPaddingTop(), BounceSwipeView.this.getPaddingRight(), BounceSwipeView.this.getPaddingBottom());
                                return true;
                            } else if (BounceSwipeView.this.getScrollX() < maxScrollAmount) {
                                BounceSwipeView.this.mAtEdge = false;
                                break;
                            } else {
                                BounceSwipeView.this.mAtEdgePreviousPosition = ev.getX();
                                BounceSwipeView.this.mBouncingSide = false;
                                int newRightPadding = ((int) (BounceSwipeView.this.mAtEdgeStartPosition - BounceSwipeView.this.mAtEdgePreviousPosition)) / 2;
                                if (newRightPadding >= BounceSwipeView.this.mPaddingRight) {
                                    BounceSwipeView.super.setPadding(BounceSwipeView.this.getPaddingLeft(), BounceSwipeView.this.getPaddingTop(), newRightPadding, BounceSwipeView.this.getPaddingBottom());
                                } else {
                                    BounceSwipeView.super.setPadding(BounceSwipeView.this.getPaddingLeft(), BounceSwipeView.this.getPaddingTop(), BounceSwipeView.this.mPaddingRight, BounceSwipeView.this.getPaddingBottom());
                                }
                                BounceSwipeView.this.scrollTo((int) (maxScrollAmount + ((BounceSwipeView.this.mAtEdgeStartPosition - BounceSwipeView.this.mAtEdgePreviousPosition) / 2.0f)), BounceSwipeView.this.getScrollY());
                                return true;
                            }
                        default:
                            break;
                    }
                }
                return false;
            }
            return true;
        }
    }
}

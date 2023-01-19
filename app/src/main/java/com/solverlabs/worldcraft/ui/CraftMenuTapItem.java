package com.solverlabs.worldcraft.ui;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.text.Readout;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.chunk.tile_entity.Inventory;
import com.solverlabs.worldcraft.factories.CraftFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;

import org.apache.commons.compress.archivers.cpio.CpioConstants;


public class CraftMenuTapItem {
    public static final float HEIGHT = 70.0f;
    public static final float WIDTH = 300.0f;
    private static final float HALF_HEIGHT = 35.0f;
    private static final float HALF_WIDTH = 150.0f;
    private static final float TAP_TIME = 0.3f;
    public static boolean mIsResetFocus = false;
    private final CraftFactory.CraftItem mCraftItem;
    private final Inventory mInventory;
    private final ItemFactory.Item mItem;
    private final String mName;
    private final TexturedShape mTexShape;
    private final float mX;
    private final float mY;
    public BoundingRectangle mBounds;
    public boolean mCanBeCrafted;
    public int mInnerColour = Colour.packInt(241, 241, 249, CpioConstants.C_IWUSR);
    public boolean mIsSelected;
    private ColouredShape mBottomBound;
    private int mCount;
    private Readout mCountTextShape;
    private long mDownTime = -1;
    private ColouredShape mInnerShape;
    private TextShape mNameShape;
    private Touch.Pointer mTouch;
    private ColouredShape mIpBound;
    private float mYOffset;

    public CraftMenuTapItem(@NonNull CraftFactory.CraftItem craftItem, Inventory inventory, float x, float y) {
        mCraftItem = craftItem;
        mItem = ItemFactory.Item.getItemByID(craftItem.getID());
        mTexShape = mItem.itemShape.clone();
        mInventory = inventory;
        mName = craftItem.name();
        mX = x;
        mY = y;
        mBounds = new BoundingRectangle(x, y, 300.0f, 70.0f);
    }

    public CraftFactory.CraftItem getmCraftItem() {
        return mCraftItem;
    }

    public void draw(StackedRenderer sr, float deltaY) {
        mBounds.y.set(mY + deltaY + mYOffset);
        if (mIsSelected) {
            drawInnerShape(sr, deltaY);
        }
        drawBounds(sr, deltaY);
        sr.render();
        sr.pushMatrix();
        sr.translate(mX + 10.0f, ((mY + HALF_HEIGHT) - 10.0f) + deltaY + mYOffset, 0.0f);
        drawName(sr);
        sr.translate(220.0f, -15.0f, 0.0f);
        drawCount(sr);
        sr.translate(0.0f, 25.0f, 0.0f);
        sr.scale(50.0f, 50.0f, 1.0f);
        mTexShape.render(sr);
        sr.popMatrix();
        sr.render();
    }

    private void drawCount(StackedRenderer sr) {
        if (mCountTextShape == null) {
            mCountTextShape = new Readout(GUI.getFont(), Colour.white, " ", false, 2, 0);
        }
        mCountTextShape.updateValue(mCount);
        if (mCanBeCrafted) {
            mCountTextShape.colours = ShapeUtil.expand(Colour.white, mCountTextShape.vertexCount());
        } else {
            mCountTextShape.colours = ShapeUtil.expand(Colour.darkgrey, mCountTextShape.vertexCount());
        }
        mCountTextShape.render(sr);
    }

    private void drawName(StackedRenderer sr) {
        if (mNameShape == null) {
            mNameShape = GUI.getFont().buildTextShape(mName, Colour.white);
        }
        if (mCanBeCrafted) {
            mNameShape.colours = ShapeUtil.expand(Colour.white, mNameShape.vertexCount());
        } else {
            mNameShape.colours = ShapeUtil.expand(Colour.darkgrey, mNameShape.vertexCount());
        }
        mNameShape.render(sr);
    }

    private void drawInnerShape(StackedRenderer sr, float deltaY) {
        if (mInnerShape == null) {
            Shape is = ShapeUtil.innerQuad(mBounds.x.getMin() + 2.0f, mBounds.y.getMin(), mBounds.x.getMax() - 2.0f, mBounds.y.getMax(), mBounds.y.getSpan(), 0.0f);
            mInnerShape = new ColouredShape(is, mInnerColour, (State) null);
        }
        mInnerShape.set(mX, mY + deltaY + mYOffset, 0.0f);
        mInnerShape.render(sr);
    }

    private void drawBounds(StackedRenderer sr, float deltaY) {
        if (mIpBound == null) {
            Shape s = ShapeUtil.line(3.0f, 0.0f, 0.0f, 300.0f, 0.0f);
            mIpBound = new ColouredShape(s, Colour.white, (State) null);
            mBottomBound = new ColouredShape(s, Colour.darkgrey, (State) null);
        }
        sr.pushMatrix();
        sr.translate(mX, mY + mYOffset + deltaY, 0.0f);
        mBottomBound.render(sr);
        sr.translate(0.0f, 67.0f, 0.0f);
        mIpBound.render(sr);
        sr.popMatrix();
    }

    private void canBeCrafted() {
        boolean z = true;
        mCanBeCrafted = false;
        int temp = 0;
        for (int i = 0; i < mCraftItem.getMaterial().length; i++) {
            if (mInventory.getItemTotalCount(mCraftItem.getMaterial()[i][0]) >= mCraftItem.getMaterial()[i][1]) {
                temp++;
            }
        }
        if (temp != mCraftItem.getMaterial().length) {
            z = false;
        }
        mCanBeCrafted = z;
    }

    public void checkCount() {
        mCount = mInventory.getItemTotalCount(mCraftItem.getID());
    }

    public boolean pointerAdded(Touch.Pointer p) {
        if (mTouch != null || !mBounds.contains(p.x, p.y)) {
            return false;
        }
        mIsResetFocus = true;
        mTouch = p;
        mDownTime = System.currentTimeMillis();
        return true;
    }

    public void pointerRemoved(Touch.Pointer p) {
        if (mTouch == p && mTouch != null) {
            long delta = System.currentTimeMillis() - mDownTime;
            if (((float) delta) < 300.0f) {
                onTap();
            }
            mTouch = null;
        }
    }

    public void reset() {
    }

    private void onTap() {
        mIsSelected = true;
    }

    public void setShown(boolean isShown) {
        if (isShown) {
            setYOffset(0.0f);
        }
    }

    public float getYOffset() {
        return mYOffset;
    }

    public void setYOffset(float f) {
        mYOffset = f;
    }

    public float getmY() {
        return mYOffset + mY;
    }

    public void translateYOffset(float yOffset) {
        mYOffset += yOffset;
    }

    public void checkItem() {
        canBeCrafted();
        checkCount();
    }

    public ItemFactory.Item getmItem() {
        return mItem;
    }
}

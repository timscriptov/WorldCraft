package com.solverlabs.worldcraft.ui;

import android.opengl.GLES10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.text.Readout;
import com.solverlabs.droid.rugl.text.TextLayout;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.chunk.tile_entity.Inventory;
import com.solverlabs.worldcraft.chunk.tile_entity.TileEntity;
import com.solverlabs.worldcraft.factories.CraftFactory;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import java.util.ArrayList;
import java.util.Iterator;

public class CraftMenu implements Touch.TouchListener {
    private final CustomButton blocksButton;
    private ColouredShape boundShape;
    private final CustomButton craftTap;
    private final TapPad.Listener craftTapListener;
    private TextShape descriptionShape;
    private final CustomTapPad exitTap;
    private final TapPad.Listener exitTapListener;
    private ColouredShape fillTitleShape;
    private final TapPad.Listener groupButtonListener;
    private ColouredShape innerShape;
    private final Inventory inventory;
    private boolean isWorkBanch;
    private final CustomButton itemsButton;
    private Readout materialCountShape;
    private boolean needToScroll;
    private float prevYpoint;
    private ColouredShape scissorBoundShape;
    private boolean show;
    private TextLayout textLayout;
    private TextShape titleTextShape;
    private final CustomButton toolsButton;
    private Touch.Pointer touch;
    private float touchDelta;
    private static final float RATIO_Y = Game.screenHeight / Game.gameHeight;
    private static final float RATIO_X = Game.screenWidth / Game.gameWidth;
    private final ArrayList<CraftMenuTapItem> craftItems = new ArrayList<>();
    public BoundingRectangle bounds = new BoundingRectangle(0.0f, 0.0f, Game.gameWidth, Game.gameHeight);
    public int innerColour = Colour.packInt(148, 134, 123, 255);
    public int boundsColour = Colour.packFloat(0.0f, 0.0f, 0.0f, 0.8f);
    private final ArrayList<CustomButton> buttonsGroup = new ArrayList<>();
    public BoundingRectangle scissorBound = new BoundingRectangle(150.0f, 10.0f, 300.0f, 462.0f);
    private int activeGroupNumber = 1;

    public CraftMenu(Inventory inventory) {
        this.inventory = inventory;
        initCraftItems(false);
        this.exitTap = new CustomTapPad(Game.gameWidth - 68.0f, Game.gameHeight - 68.0f, 60.0f, 60.0f, GUI.getFont(), "X");
        this.exitTapListener = new TapPad.Listener() { 
            @Override 
            public void onTap(TapPad pad) {
                CraftMenu.this.showOrHide(false);
            }

            @Override 
            public void onLongPress(TapPad pad) {
            }

            @Override 
            public void onFlick(TapPad pad, int horizontal, int vertical) {
            }

            @Override 
            public void onDoubleTap(TapPad pad) {
            }
        };
        this.exitTap.listener = this.exitTapListener;
        this.craftTap = new CustomButton(500.0f, 200.0f, 200.0f, 180.0f, DescriptionFactory.emptyText);
        this.craftTapListener = new TapPad.Listener() { 
            @Override 
            public void onTap(TapPad pad) {
                CraftMenu.this.doCraft();
            }

            @Override 
            public void onLongPress(TapPad pad) {
            }

            @Override 
            public void onFlick(TapPad pad, int horizontal, int vertical) {
            }

            @Override 
            public void onDoubleTap(TapPad pad) {
            }
        };
        this.craftTap.listener = this.craftTapListener;
        this.toolsButton = new CustomButton(25.0f, Game.gameHeight - 125.0f, 100.0f, 100.0f, "1");
        this.toolsButton.drawText = false;
        this.buttonsGroup.add(this.toolsButton);
        this.blocksButton = new CustomButton(25.0f, Game.gameHeight - 250.0f, 100.0f, 100.0f, "2");
        this.blocksButton.drawText = false;
        this.buttonsGroup.add(this.blocksButton);
        this.itemsButton = new CustomButton(25.0f, Game.gameHeight - 375.0f, 100.0f, 100.0f, "3");
        this.itemsButton.drawText = false;
        this.buttonsGroup.add(this.itemsButton);
        this.groupButtonListener = new TapPad.Listener() { 
            @Override 
            public void onTap(TapPad pad) {
                for (CustomButton button : CraftMenu.this.buttonsGroup) {
                    button.setSelected(false);
                }
                pad.isSelected = true;
                if (pad instanceof CustomButton) {
                    CraftMenu.this.activeGroupNumber = Integer.parseInt(((CustomButton) pad).getText());
                }
                CraftMenu.this.initCraftItems(CraftMenu.this.isWorkBanch);
            }

            @Override 
            public void onLongPress(TapPad pad) {
            }

            @Override 
            public void onFlick(TapPad pad, int horizontal, int vertical) {
            }

            @Override 
            public void onDoubleTap(TapPad pad) {
            }
        };
        this.toolsButton.listener = this.groupButtonListener;
        this.blocksButton.listener = this.groupButtonListener;
        this.itemsButton.listener = this.groupButtonListener;
    }

    public void initCraftItems(boolean isWorkBanch) {
        this.craftItems.clear();
        float y = 402.0f;
        for (int i = 0; i < CraftFactory.CraftItem.values().length; i++) {
            CraftFactory.CraftItem craftItem = CraftFactory.CraftItem.values()[i];
            if (isWorkBanch && craftItem.getGroup() == this.activeGroupNumber) {
                addCraftItem(150.0f, y, craftItem);
                y -= 70.0f;
            } else if (!craftItem.isNeedWorkBanch() && craftItem.getGroup() == this.activeGroupNumber) {
                addCraftItem(150.0f, y, craftItem);
                y -= 70.0f;
            }
        }
        this.craftItems.get(0).isSelected = true;
    }

    private void addCraftItem(float x, float y, CraftFactory.CraftItem craftItem) {
        CraftMenuTapItem tapItem = new CraftMenuTapItem(craftItem, this.inventory, x, y);
        tapItem.checkItem();
        this.craftItems.add(tapItem);
    }

    public void doCraft() {
        if (getSelectedItem() != null) {
            CraftFactory.CraftItem craftItem = getSelectedItem().getCraftItem();
            if (getSelectedItem().canBeCrafted) {
                int count = craftItem.getCount();
                for (int i = 0; i < count; i++) {
                    this.inventory.add(craftItem.getID());
                }
                for (int i2 = 0; i2 < craftItem.getMaterial().length; i2++) {
                    for (int j = 0; j < craftItem.getMaterial()[i2][1]; j++) {
                        this.inventory.decItem(craftItem.getMaterial()[i2][0]);
                    }
                }
                for (CraftMenuTapItem item : this.craftItems) {
                    item.checkItem();
                }
            }
        }
    }

    public void draw(StackedRenderer sr) {
        if (this.show) {
            drawInnerBound(sr);
            drawBound(sr);
            this.exitTap.draw(sr);
            sr.render();
            this.craftTap.draw(sr);
            this.toolsButton.draw(sr);
            drawButtonTitle(sr, this.toolsButton.getX() + (this.toolsButton.getWidth() / 2.0f), this.toolsButton.getY() + (this.toolsButton.getHeight() / 2.0f), ItemFactory.Item.IronPick.itemShape);
            this.blocksButton.draw(sr);
            drawButtonTitle(sr, this.blocksButton.getX() + (this.blocksButton.getWidth() / 2.0f), this.blocksButton.getY() + (this.blocksButton.getHeight() / 2.0f), ItemFactory.Item.BrickBlock.itemShape);
            this.itemsButton.draw(sr);
            drawButtonTitle(sr, this.itemsButton.getX() + (this.itemsButton.getWidth() / 2.0f), this.itemsButton.getY() + (this.itemsButton.getHeight() / 2.0f), ItemFactory.Item.ItemsLabel.itemShape);
            drawCraftMaterial(sr);
            drawCraftMaterialCount(sr);
            GLES10.glEnable(3089);
            GLES10.glScissor((int) (150.0f * RATIO_X), (int) (10.0f * RATIO_Y), (int) (400.0f * RATIO_X), (int) (462.0f * RATIO_Y));
            for (int i = 0; i < this.craftItems.size(); i++) {
                this.craftItems.get(i).draw(sr, this.touchDelta);
            }
            sr.render();
            GLES10.glDisable(3089);
            drawTitle(sr);
            drawScissorBound(sr);
            drawItemDescription(sr);
            sr.render();
        }
    }

    private void drawTitle(StackedRenderer sr) {
        if (this.titleTextShape == null) {
            Font font = GUI.getFont();
            this.titleTextShape = font.buildTextShape("Craft", Colour.white);
            this.titleTextShape.translate(((280.0f - font.getStringLength(TileEntity.FURNACE_ID)) / 2.0f) + 450.0f + 20.0f, (Game.gameHeight - font.size) - 20.0f, 0.0f);
            Shape s = ShapeUtil.filledQuad(450.0f, Game.gameHeight - 8.0f, 730.0f, Game.gameHeight - 70.0f, 0.0f);
            this.fillTitleShape = new ColouredShape(s, Colour.packFloat(0.0f, 0.0f, 0.0f, 0.5f), null);
        }
        this.fillTitleShape.render(sr);
        sr.render();
        this.titleTextShape.render(sr);
    }

    private void drawInnerBound(StackedRenderer sr) {
        if (this.innerShape == null) {
            Shape is = ShapeUtil.innerQuad(this.bounds.x.getMin(), this.bounds.y.getMin(), this.bounds.x.getMax(), this.bounds.y.getMax(), this.bounds.y.getSpan(), 0.0f);
            this.innerShape = new ColouredShape(is, this.innerColour, null);
        }
        this.innerShape.render(sr);
    }

    private void drawBound(StackedRenderer sr) {
        if (this.boundShape == null) {
            Shape bs = ShapeUtil.innerQuad(this.bounds.x.getMin(), this.bounds.y.getMin(), this.bounds.x.getMax(), this.bounds.y.getMax(), 8.0f, 0.0f);
            this.boundShape = new ColouredShape(bs, this.boundsColour, null);
        }
        this.boundShape.render(sr);
    }

    private void drawScissorBound(StackedRenderer sr) {
        if (this.scissorBoundShape == null) {
            Shape bs = ShapeUtil.innerQuad(150.0f, 10.0f, 450.0f, 472.0f, 2.0f, 0.0f);
            this.scissorBoundShape = new ColouredShape(bs, Colour.black, null);
        }
        this.scissorBoundShape.render(sr);
    }

    private void drawCraftMaterial(StackedRenderer sr) {
        float xOffset;
        float yOffset;
        if (getSelectedItem() != null) {
            CraftFactory.CraftItem craftItem = getSelectedItem().getCraftItem();
            for (int i = 0; i < craftItem.getMaterial().length; i++) {
                ItemFactory.Item item = ItemFactory.Item.getItemByID(craftItem.getMaterial()[i][0]);
                if (i < 2) {
                    xOffset = i * 90;
                    yOffset = 0.0f;
                } else {
                    xOffset = (i - 2) * 90;
                    yOffset = -80.0f;
                }
                sr.pushMatrix();
                sr.translate(550.0f + xOffset, 340.0f + yOffset, 0.0f);
                sr.scale(50.0f, 50.0f, 1.0f);
                item.itemShape.render(sr);
                sr.popMatrix();
            }
            sr.render();
        }
    }

    private void drawCraftMaterialCount(StackedRenderer sr) {
        float xOffset;
        float yOffset;
        if (this.materialCountShape == null) {
            this.materialCountShape = new Readout(GUI.getFont(), Colour.white, " ", false, 2, 1);
        }
        if (getSelectedItem() != null) {
            CraftFactory.CraftItem craftItem = getSelectedItem().getCraftItem();
            for (int i = 0; i < craftItem.getMaterial().length; i++) {
                byte itemID = craftItem.getMaterial()[i][0];
                float totalCount = this.inventory.getItemTotalCount(itemID);
                float neededCount = totalCount + ((craftItem.getMaterial()[i][1] * 1.0f) / 10.0f);
                this.materialCountShape.updateValue(neededCount);
                if (i < 2) {
                    xOffset = i * 90;
                    yOffset = 0.0f;
                } else {
                    xOffset = (i - 2) * 90;
                    yOffset = -80.0f;
                }
                sr.pushMatrix();
                sr.translate(510.0f + xOffset, 295.0f + yOffset, 0.0f);
                if (totalCount >= craftItem.getMaterial()[i][1]) {
                    this.materialCountShape.colours = ShapeUtil.expand(Colour.white, this.materialCountShape.vertexCount());
                } else {
                    this.materialCountShape.colours = ShapeUtil.expand(Colour.darkgrey, this.materialCountShape.vertexCount());
                }
                this.materialCountShape.render(sr);
                sr.popMatrix();
                sr.render();
            }
        }
    }

    private void drawButtonTitle(@NonNull StackedRenderer sr, float x, float y, @NonNull TexturedShape itemShape) {
        sr.pushMatrix();
        sr.translate(x, y, 0.0f);
        sr.scale(60.0f, 60.0f, 1.0f);
        itemShape.render(sr);
        sr.popMatrix();
        sr.render();
    }

    private void drawItemDescription(StackedRenderer sr) {
        String description;
        Font font = GUI.getFont();
        if (getSelectedItem() != null && (description = getSelectedItem().getItem().getDescription()) != null && !description.equals(DescriptionFactory.emptyText)) {
            this.textLayout = new TextLayout(description, font, null, 230.0f, Colour.white);
            this.textLayout.textShape.scale(1.2f, 1.2f, 1.2f);
            this.textLayout.textShape.translate(500.0f, 150.0f, 0.0f);
            this.textLayout.textShape.render(sr);
        }
    }

    @Override 
    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch == null && this.bounds.contains(p.x, p.y) && this.show) {
            this.touch = p;
            for (CraftMenuTapItem item : this.craftItems) {
                item.pointerAdded(this.touch);
            }
            this.exitTap.pointerAdded(this.touch);
            this.craftTap.pointerAdded(this.touch);
            this.toolsButton.pointerAdded(this.touch);
            this.blocksButton.pointerAdded(this.touch);
            this.itemsButton.pointerAdded(this.touch);
            if (this.scissorBound.contains(p.x, p.y)) {
                this.needToScroll = true;
                this.prevYpoint = this.touch.y;
                return true;
            }
            return true;
        }
        return false;
    }

    @Override 
    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch == p && this.touch != null) {
            for (CraftMenuTapItem item : this.craftItems) {
                item.pointerRemoved(this.touch);
                item.translateYOffset(this.touchDelta);
            }
            this.exitTap.pointerRemoved(this.touch);
            this.craftTap.pointerRemoved(this.touch);
            this.toolsButton.pointerRemoved(this.touch);
            this.blocksButton.pointerRemoved(this.touch);
            this.itemsButton.pointerRemoved(this.touch);
            this.touch = null;
            this.touchDelta = 0.0f;
            this.needToScroll = false;
        }
    }

    @Override 
    public void reset() {
    }

    @Nullable
    private CraftMenuTapItem getSelectedItem() {
        for (int i = 0; i < this.craftItems.size(); i++) {
            if (this.craftItems.get(i).isSelected) {
                return this.craftItems.get(i);
            }
        }
        return null;
    }

    public void advance() {
        if (this.show) {
            this.exitTap.advance();
            this.craftTap.advance();
            this.toolsButton.advance();
            this.blocksButton.advance();
            this.itemsButton.advance();
            if (CraftMenuTapItem.isResetFocus) {
                for (CraftMenuTapItem item : this.craftItems) {
                    item.isSelected = false;
                }
                CraftMenuTapItem.isResetFocus = false;
            }
            if (this.needToScroll && this.craftItems.size() > 7) {
                this.touchDelta = this.touch.y - this.prevYpoint;
            }
            if (!this.craftItems.isEmpty() && this.craftItems.size() > 7) {
                normalizeScroll();
            }
        }
    }

    private void normalizeScroll() {
        CraftMenuTapItem lastItem = this.craftItems.get(this.craftItems.size() - 1);
        float bottomPoint = lastItem.getY();
        float topPoint = this.craftItems.get(0).bounds.y.getMax();
        if (this.touchDelta + topPoint < this.scissorBound.y.getMax()) {
            this.touchDelta = 0.0f;
            for (CraftMenuTapItem item : this.craftItems) {
                item.setYOffset(0.0f);
            }
        }
        if (this.touchDelta + bottomPoint > this.bounds.y.getMin() && this.craftItems.get(0).getYOffset() != 0.0f) {
            float yOffset = (70.0f * (this.craftItems.size() - 1)) - 394.0f;
            this.touchDelta = 0.0f;
            for (CraftMenuTapItem item2 : this.craftItems) {
                item2.setYOffset(yOffset);
            }
        }
    }

    public boolean isVisible() {
        return this.show;
    }

    public void setShow(boolean isShow) {
        this.show = isShow;
        for (CraftMenuTapItem item : this.craftItems) {
            item.setShown(isShow);
        }
    }

    public void showOrHide(boolean isWorkBanch) {
        setShow(!this.show);
        this.isWorkBanch = isWorkBanch;
        if (isVisible()) {
            initCraftItems(isWorkBanch);
        }
    }
}

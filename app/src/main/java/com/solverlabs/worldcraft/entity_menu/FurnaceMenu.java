package com.solverlabs.worldcraft.entity_menu;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

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
import com.solverlabs.droid.rugl.text.TextLayout;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.chunk.tile_entity.Furnace;
import com.solverlabs.worldcraft.chunk.tile_entity.Inventory;
import com.solverlabs.worldcraft.chunk.tile_entity.TileEntity;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.math.MathUtils;
import com.solverlabs.worldcraft.ui.CustomButton;
import com.solverlabs.worldcraft.ui.GUI;

import java.util.ArrayList;


public class FurnaceMenu extends CustomMenu {
    private static final int FUEL_SELECTED = 2;
    private static final int MATERIAL_SELECTED = 1;
    private static final float SCALE_VALUE = 100.0f;
    private static final int crafteItemColour = Colour.packFloat(0.8f, 0.8f, 0.8f, 0.5f);
    private final TapPad.Listener buttonGroupListener;
    private final ArrayList<CustomButton> buttonsGroup;
    private final CustomButton craftedItemButton;
    private final TapPad.Listener craftedItemButtonListener;
    private final CustomButton fuelButton;
    private final CustomButton materialButton;
    private final ArrayList<CustomTapItem> tapItems;
    public BoundingRectangle scissorBound;
    private TexturedShape activeArrowShape;
    private TexturedShape activeFireShape;
    private TexturedShape arrowShape;
    private CustomTapItem craftedTapItem;
    private TextLayout descriptionLayout;
    private ColouredShape fillTitleShape;
    private TexturedShape fireShape;
    private CustomTapItem fuelTapItem;
    private Furnace furnace;
    private CustomTapItem materialTapItem;
    private boolean needToScroll;
    private float prevYpoint;
    private ColouredShape scissorBoundShape;
    private int selectedItemNumber;
    private TextShape title;
    private float touchDelta;

    public FurnaceMenu(final Inventory inventory) {
        super(inventory);
        this.scissorBound = new BoundingRectangle(20.0f, 10.0f, 400.0f, 400.0f);
        this.tapItems = new ArrayList<>();
        this.materialButton = new CustomButton(460.0f, 280.0f, SCALE_VALUE, SCALE_VALUE, "1");
        this.fuelButton = new CustomButton(460.0f, 90.0f, SCALE_VALUE, SCALE_VALUE, "2");
        this.craftedItemButton = new CustomButton(660.0f, 185.0f, SCALE_VALUE, SCALE_VALUE, DescriptionFactory.emptyText);
        this.buttonsGroup = new ArrayList<>();
        this.selectedItemNumber = 1;
        this.materialButton.drawText = false;
        this.materialButton.isStroke = true;
        this.materialButton.isSelected = true;
        this.buttonsGroup.add(this.materialButton);
        this.fuelButton.drawText = false;
        this.fuelButton.isStroke = true;
        this.buttonsGroup.add(this.fuelButton);
        this.buttonGroupListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                removeItemFromFurance(pad);
            }

            @Override
            public void onLongPress(TapPad pad) {
                removeItemFromFurance(pad);
            }

            @Override
            public void onFlick(TapPad pad, int horizontal, int vertical) {
            }

            @Override
            public void onDoubleTap(TapPad pad) {
            }
        };
        this.craftedItemButtonListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                InventoryItem craftedItem;
                if (furnace != null && (craftedItem = furnace.getCraftedItem()) != null && !craftedItem.isEmpty()) {
                    int count = craftedItem.getCount();
                    for (int i = 0; i < count; i++) {
                        if (inventory.add(craftedItem.getItemID())) {
                            craftedItem.decCount();
                        }
                    }
                    if (furnace.getCraftedItem().isEmpty()) {
                        furnace.removeAllCraftedItem();
                    }
                    initFuranceItems(inventory);
                }
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
        this.materialButton.listener = this.buttonGroupListener;
        this.fuelButton.listener = this.buttonGroupListener;
        this.craftedItemButton.listener = this.craftedItemButtonListener;
    }

    public void removeItemFromFurance(TapPad pad) {
        if (pad instanceof CustomButton) {
            this.selectedItemNumber = Integer.parseInt(((CustomButton) pad).getText());
        }
        if (!pad.isSelected) {
            for (CustomButton button : this.buttonsGroup) {
                button.setSelected(false);
            }
            pad.isSelected = true;
        } else {
            if (this.selectedItemNumber == 1 && this.furnace != null && this.furnace.getMaterial() != null) {
                this.inventory.add(this.furnace.getMaterialId());
                this.furnace.decMaterial();
                if (this.furnace.getMaterial() == null) {
                    this.materialTapItem = null;
                }
            }
            if (this.selectedItemNumber == 2 && this.furnace != null && this.furnace.getFuel() != null) {
                this.inventory.add(this.furnace.getFuelId());
                this.furnace.decFuel();
                if (this.furnace.getFuel() == null) {
                    this.fuelTapItem = null;
                }
            }
        }
        initFuranceItems(this.inventory);
    }

    public void addItemToFurnace(InventoryItem inventoryItem) {
        if (this.selectedItemNumber == 2) {
            if (this.furnace.addFuel(inventoryItem.m83clone())) {
                this.inventory.decItem(inventoryItem);
                if (inventoryItem.isEmpty() || inventoryItem.isFull()) {
                    initFuranceItems(this.inventory);
                }
            }
            if (this.fuelTapItem == null) {
                this.fuelTapItem = new CustomTapItem(this.furnace.getFuel(), 510.0f, 140.0f);
                this.fuelTapItem.isDrawBounds = false;
            }
        }
        if (this.selectedItemNumber == 1) {
            if (this.furnace.addMaterial(inventoryItem.m83clone())) {
                this.inventory.decItem(inventoryItem);
                if (inventoryItem.isEmpty() || inventoryItem.isFull()) {
                    initFuranceItems(this.inventory);
                }
            }
            if (this.materialTapItem == null) {
                this.materialTapItem = new CustomTapItem(this.furnace.getMaterial(), 510.0f, 330.0f);
                this.materialTapItem.isDrawBounds = false;
            }
        }
    }

    public void initFuranceItems(@NonNull Inventory inventory) {
        this.tapItems.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            final InventoryItem inventoryItem = inventory.getAllInventoryItems().get(i);
            if (!inventoryItem.isEmpty() && ((this.selectedItemNumber == 2 && inventoryItem.isUseAsFuel()) || (this.selectedItemNumber == 1 && inventoryItem.isUseAsMaterial()))) {
                CustomTapItem furnaceTapItem = new CustomTapItem(inventoryItem) {
                    @Override
                    protected void onTap() {
                        addItemToFurnace(inventoryItem);
                    }

                    @Override
                    protected void onLongPress() {
                        addItemToFurnace(inventoryItem);
                    }
                };
                this.tapItems.add(furnaceTapItem);
            }
        }
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch != null || !this.bounds.contains(p.x, p.y) || !this.show) {
            return false;
        }
        this.touch = p;
        this.exitTap.pointerAdded(this.touch);
        for (int i = 0; i < this.tapItems.size(); i++) {
            this.tapItems.get(i).pointerAdded(this.touch);
        }
        this.materialButton.pointerAdded(this.touch);
        this.fuelButton.pointerAdded(this.touch);
        this.craftedItemButton.pointerAdded(this.touch);
        if (this.scissorBound.contains(p.x, p.y)) {
            this.needToScroll = true;
            this.prevYpoint = this.touch.y;
        }
        return true;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch == p && this.touch != null) {
            this.exitTap.pointerRemoved(this.touch);
            for (int i = 0; i < this.tapItems.size(); i++) {
                this.tapItems.get(i).translateYOffset(this.touchDelta);
                this.tapItems.get(i).pointerRemoved(this.touch);
            }
            this.materialButton.pointerRemoved(this.touch);
            this.fuelButton.pointerRemoved(this.touch);
            this.craftedItemButton.pointerRemoved(this.touch);
            this.touch = null;
            this.touchDelta = 0.0f;
            this.needToScroll = false;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void draw(StackedRenderer sr) {
        super.draw(sr);
        if (this.show) {
            GLES10.glEnable(3089);
            GLES10.glScissor((int) (20.0f * RATIO_X), (int) (10.0f * RATIO_Y), (int) (420.0f * RATIO_X), (int) (400.0f * RATIO_Y));
            float x = 65.0f;
            float y = 365.0f;
            float yOffset = 0.0f;
            int k = 0;
            for (int i = 0; i < this.tapItems.size(); i++) {
                if (k == 5) {
                    k = 0;
                    yOffset += 1.0f;
                    x = 65.0f;
                    y = 365.0f - (84.0f * yOffset);
                }
                this.tapItems.get(i).setPosition(x, y);
                this.tapItems.get(i).draw(sr, this.touchDelta);
                x += 84.0f;
                k++;
            }
            sr.render();
            GLES10.glDisable(3089);
            drawScissorBound(sr);
            drawTitle(sr);
            ItemFactory.Item item = ItemFactory.Item.getItemByID(this.furnace.getCraftedItemId());
            if (this.materialTapItem != null) {
                this.materialTapItem.draw(sr, 0.0f);
                if (this.craftedTapItem == null && item != null) {
                    sr.pushMatrix();
                    sr.translate(710.0f, 235.0f, 0.0f);
                    sr.scale(60.0f, 60.0f, 1.0f);
                    TexturedShape itemShape = (TexturedShape) item.itemShape.clone();
                    itemShape.colours = ShapeUtil.expand(crafteItemColour, itemShape.vertexCount());
                    itemShape.render(sr);
                    sr.popMatrix();
                }
            }
            if (this.fuelTapItem != null) {
                this.fuelTapItem.draw(sr, 0.0f);
            }
            if (this.craftedTapItem != null) {
                this.craftedTapItem.draw(sr, 0.0f);
            }
            if (this.furnace.getMaterial() != null) {
                drawDescription(sr);
            }
            this.materialButton.draw(sr);
            this.fuelButton.draw(sr);
            this.craftedItemButton.draw(sr);
            drawFurnaceProcessItems(sr);
            this.exitTap.draw(sr);
        }
    }

    private void drawDescription(StackedRenderer sr) {
        String description = this.furnace.getMaterial().getItem().getDescription();
        if (description != null && !description.equals(DescriptionFactory.emptyText)) {
            this.descriptionLayout = new TextLayout(description, GUI.getFont(), null, 150.0f, Colour.white);
            this.descriptionLayout.textShape.translate(590.0f, 145.0f, 0.0f);
            this.descriptionLayout.textShape.render(sr);
        }
    }

    private void drawScissorBound(StackedRenderer sr) {
        if (this.scissorBoundShape == null) {
            Shape bs = ShapeUtil.innerQuad(20.0f, 10.0f, 440.0f, 410.0f, 4.0f, 0.0f);
            this.scissorBoundShape = new ColouredShape(bs, Colour.black, (State) null);
        }
        this.scissorBoundShape.render(sr);
    }

    protected void drawTitle(StackedRenderer sr) {
        if (this.title == null) {
            Font font = GUI.getFont();
            this.title = font.buildTextShape(TileEntity.FURNACE_ID, Colour.white);
            this.title.translate(((Game.mGameWidth - font.getStringLength(TileEntity.FURNACE_ID)) / 2.0f) - 50.0f, (Game.mGameHeight - font.size) - 20.0f, 0.0f);
            Shape s = ShapeUtil.filledQuad(20.0f, Game.mGameHeight - 10.0f, Game.mGameWidth - 68.0f, Game.mGameHeight - 70.0f, 0.0f);
            this.fillTitleShape = new ColouredShape(s, Colour.packFloat(0.0f, 0.0f, 0.0f, 0.5f), (State) null);
        }
        this.fillTitleShape.render(sr);
        sr.render();
        this.title.render(sr);
    }

    private void drawFurnaceProcessItems(StackedRenderer sr) {
        if (this.fireShape == null) {
            this.fireShape = ItemFactory.Item.getShape(12, 13);
            this.fireShape.scale(120.0f, 120.0f, 120.0f);
            this.fireShape.translate(510.0f, 235.0f, 0.0f);
            this.activeFireShape = ItemFactory.Item.getShape(13, 13);
            this.activeFireShape.scale(120.0f, 120.0f, 120.0f);
            this.activeFireShape.translate(510.0f, 235.0f, 0.0f);
            this.arrowShape = ItemFactory.Item.getShape(14, 13);
            this.arrowShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
            this.arrowShape.translate(610.0f, 235.0f, 0.0f);
            this.activeArrowShape = ItemFactory.Item.getShape(15, 13);
            this.activeArrowShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
            this.activeArrowShape.translate(610.0f, 235.0f, 0.0f);
        }
        this.arrowShape.render(sr);
        this.fireShape.render(sr);
        sr.render();
        int progressPercent = this.furnace.getProcessPercent();
        int fuelProgressPercent = this.furnace.getFuelProcessPercent();
        GLES10.glEnable(3089);
        GLES10.glScissor((int) (580.0f * RATIO_X), (int) (210.0f * RATIO_Y), (int) (((65.0f * RATIO_X) * progressPercent) / SCALE_VALUE), (int) (RATIO_Y * SCALE_VALUE));
        this.activeArrowShape.render(sr);
        sr.render();
        GLES10.glDisable(3089);
        GLES10.glEnable(3089);
        GLES10.glScissor((int) (480.0f * RATIO_X), (int) (210.0f * RATIO_Y), (int) (RATIO_X * SCALE_VALUE), (int) (((45.0f * RATIO_Y) * (100 - fuelProgressPercent)) / SCALE_VALUE));
        this.activeFireShape.render(sr);
        sr.render();
        GLES10.glDisable(3089);
    }

    public void advance() {
        if (this.furnace != null) {
            if (this.furnace.getMaterial() == null) {
                this.materialTapItem = null;
            }
            if (this.furnace.getFuel() == null) {
                this.fuelTapItem = null;
            }
            if (this.furnace.getCraftedItem() != null) {
                if (this.craftedTapItem == null) {
                    this.craftedTapItem = new CustomTapItem(this.furnace.getCraftedItem(), 710.0f, 235.0f);
                    this.craftedTapItem.isDrawBounds = false;
                }
            } else {
                this.craftedTapItem = null;
            }
        }
        this.materialButton.advance();
        this.fuelButton.advance();
        this.craftedItemButton.advance();
        this.exitTap.advance();
        for (int i = 0; i < this.tapItems.size(); i++) {
            this.tapItems.get(i).advance();
        }
        if (this.show) {
            if (this.needToScroll && this.tapItems.size() / 5 >= 5) {
                this.touchDelta = this.touch.y - this.prevYpoint;
            }
            if (!this.tapItems.isEmpty() && this.tapItems.size() / 5 >= 5) {
                normalizeScroll();
            }
        }
    }

    private void normalizeScroll() {
        CustomTapItem lastItem = this.tapItems.get(this.tapItems.size() - 1);
        float bottomPoint = lastItem.getY() + lastItem.getYOffset();
        float topPoint = this.tapItems.get(0).bounds.y.getMax();
        if (this.touchDelta + topPoint < this.scissorBound.y.getMax()) {
            this.touchDelta = 0.0f;
            for (CustomTapItem item : this.tapItems) {
                item.setYOffset(0.0f);
            }
        }
        if (this.touchDelta + bottomPoint > this.scissorBound.y.getMin()) {
            float yOffset = (80.0f * MathUtils.roundUp(this.tapItems.size() / 5)) - 375.0f;
            this.touchDelta = 0.0f;
            for (CustomTapItem item2 : this.tapItems) {
                item2.setYOffset(yOffset);
            }
        }
    }

    public void showOrHide() {
        setShow(!this.show);
        if (isVisible()) {
            initFuranceItems(this.inventory);
        }
    }

    public Furnace getFurnace() {
        return this.furnace;
    }

    public void setFurnace(@NonNull Furnace furnace) {
        this.furnace = furnace;
        if (furnace.getFuel() != null) {
            this.fuelTapItem = new CustomTapItem(furnace.getFuel(), 510.0f, 140.0f);
            this.fuelTapItem.isDrawBounds = false;
        }
        if (furnace.getMaterial() != null) {
            this.materialTapItem = new CustomTapItem(furnace.getMaterial(), 510.0f, 330.0f);
            this.materialTapItem.isDrawBounds = false;
        }
        if (furnace.getCraftedItem() != null) {
            this.craftedTapItem = new CustomTapItem(furnace.getCraftedItem(), 710.0f, 235.0f);
            this.craftedTapItem.isDrawBounds = false;
        }
    }
}

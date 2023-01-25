package com.mcal.worldcraft.entity_menu;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.Game;
import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.Shape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.input.TapPad;
import com.mcal.droid.rugl.input.Touch;
import com.mcal.droid.rugl.text.Font;
import com.mcal.droid.rugl.text.TextLayout;
import com.mcal.droid.rugl.text.TextShape;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.droid.rugl.util.geom.BoundingRectangle;
import com.mcal.worldcraft.chunk.tile_entity.Furnace;
import com.mcal.worldcraft.chunk.tile_entity.Inventory;
import com.mcal.worldcraft.chunk.tile_entity.TileEntity;
import com.mcal.worldcraft.factories.DescriptionFactory;
import com.mcal.worldcraft.factories.ItemFactory;
import com.mcal.worldcraft.inventory.InventoryItem;
import com.mcal.worldcraft.math.MathUtils;
import com.mcal.worldcraft.ui.CustomButton;
import com.mcal.worldcraft.ui.GUI;

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
        scissorBound = new BoundingRectangle(20.0f, 10.0f, 400.0f, 400.0f);
        tapItems = new ArrayList<>();
        materialButton = new CustomButton(460.0f, 280.0f, SCALE_VALUE, SCALE_VALUE, "1");
        fuelButton = new CustomButton(460.0f, 90.0f, SCALE_VALUE, SCALE_VALUE, "2");
        craftedItemButton = new CustomButton(660.0f, 185.0f, SCALE_VALUE, SCALE_VALUE, DescriptionFactory.emptyText);
        buttonsGroup = new ArrayList<>();
        selectedItemNumber = 1;
        materialButton.drawText = false;
        materialButton.isStroke = true;
        materialButton.isSelected = true;
        buttonsGroup.add(materialButton);
        fuelButton.drawText = false;
        fuelButton.isStroke = true;
        buttonsGroup.add(fuelButton);
        buttonGroupListener = new TapPad.Listener() {
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
        craftedItemButtonListener = new TapPad.Listener() {
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
        materialButton.listener = buttonGroupListener;
        fuelButton.listener = buttonGroupListener;
        craftedItemButton.listener = craftedItemButtonListener;
    }

    public void removeItemFromFurance(TapPad pad) {
        if (pad instanceof CustomButton) {
            selectedItemNumber = Integer.parseInt(((CustomButton) pad).getText());
        }
        if (!pad.isSelected) {
            for (CustomButton button : buttonsGroup) {
                button.setSelected(false);
            }
            pad.isSelected = true;
        } else {
            if (selectedItemNumber == MATERIAL_SELECTED && furnace != null && furnace.getMaterial() != null) {
                inventory.add(furnace.getMaterialId());
                furnace.decMaterial();
                if (furnace.getMaterial() == null) {
                    materialTapItem = null;
                }
            }
            if (selectedItemNumber == FUEL_SELECTED && furnace != null && furnace.getFuel() != null) {
                inventory.add(furnace.getFuelId());
                furnace.decFuel();
                if (furnace.getFuel() == null) {
                    fuelTapItem = null;
                }
            }
        }
        initFuranceItems(inventory);
    }

    public void addItemToFurnace(InventoryItem inventoryItem) {
        if (selectedItemNumber == FUEL_SELECTED) {
            if (furnace.addFuel(inventoryItem.clone())) {
                inventory.decItem(inventoryItem);
                if (inventoryItem.isEmpty() || inventoryItem.isFull()) {
                    initFuranceItems(inventory);
                }
            }
            if (fuelTapItem == null) {
                fuelTapItem = new CustomTapItem(furnace.getFuel(), 510.0f, 140.0f);
                fuelTapItem.isDrawBounds = false;
            }
        }
        if (selectedItemNumber == MATERIAL_SELECTED) {
            if (furnace.addMaterial(inventoryItem.clone())) {
                inventory.decItem(inventoryItem);
                if (inventoryItem.isEmpty() || inventoryItem.isFull()) {
                    initFuranceItems(inventory);
                }
            }
            if (materialTapItem == null) {
                materialTapItem = new CustomTapItem(furnace.getMaterial(), 510.0f, 330.0f);
                materialTapItem.isDrawBounds = false;
            }
        }
    }

    public void initFuranceItems(@NonNull Inventory inventory) {
        tapItems.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            final InventoryItem inventoryItem = inventory.getAllInventoryItems().get(i);
            if (!inventoryItem.isEmpty() && ((selectedItemNumber == FUEL_SELECTED && inventoryItem.isUseAsFuel()) || (selectedItemNumber == MATERIAL_SELECTED && inventoryItem.isUseAsMaterial()))) {
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
                tapItems.add(furnaceTapItem);
            }
        }
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (touch == null && bounds.contains(p.x, p.y) && show) {
            touch = p;
            exitTap.pointerAdded(touch);
            for (int i = 0; i < tapItems.size(); i++) {
                tapItems.get(i).pointerAdded(touch);
            }
            materialButton.pointerAdded(touch);
            fuelButton.pointerAdded(touch);
            craftedItemButton.pointerAdded(touch);
            if (scissorBound.contains(p.x, p.y)) {
                needToScroll = true;
                prevYpoint = touch.y;
            }
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (touch == p && touch != null) {
            exitTap.pointerRemoved(touch);
            for (int i = 0; i < tapItems.size(); i++) {
                tapItems.get(i).translateYOffset(touchDelta);
                tapItems.get(i).pointerRemoved(touch);
            }
            materialButton.pointerRemoved(touch);
            fuelButton.pointerRemoved(touch);
            craftedItemButton.pointerRemoved(touch);
            touch = null;
            touchDelta = 0.0f;
            needToScroll = false;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void draw(StackedRenderer sr) {
        super.draw(sr);
        if (show) {
            GLES10.glEnable(3089);
            GLES10.glScissor((int) (20.0f * RATIO_X), (int) (10.0f * RATIO_Y), (int) (420.0f * RATIO_X), (int) (400.0f * RATIO_Y));
            float x = 65.0f;
            float y = 365.0f;
            float yOffset = 0.0f;
            int k = 0;
            for (int i = 0; i < tapItems.size(); i++) {
                if (k == 5) {
                    k = 0;
                    yOffset += 1.0f;
                    x = 65.0f;
                    y = 365.0f - (84.0f * yOffset);
                }
                tapItems.get(i).setPosition(x, y);
                tapItems.get(i).draw(sr, touchDelta);
                x += 84.0f;
                k++;
            }
            sr.render();
            GLES10.glDisable(3089);
            drawScissorBound(sr);
            drawTitle(sr);
            ItemFactory.Item item = ItemFactory.Item.getItemByID(furnace.getCraftedItemId());
            if (materialTapItem != null) {
                materialTapItem.draw(sr, 0.0f);
                if (craftedTapItem == null && item != null) {
                    sr.pushMatrix();
                    sr.translate(710.0f, 235.0f, 0.0f);
                    sr.scale(60.0f, 60.0f, 1.0f);
                    TexturedShape itemShape = item.itemShape.clone();
                    itemShape.colours = ShapeUtil.expand(crafteItemColour, itemShape.vertexCount());
                    itemShape.render(sr);
                    sr.popMatrix();
                }
            }
            if (fuelTapItem != null) {
                fuelTapItem.draw(sr, 0.0f);
            }
            if (craftedTapItem != null) {
                craftedTapItem.draw(sr, 0.0f);
            }
            if (furnace.getMaterial() != null) {
                drawDescription(sr);
            }
            materialButton.draw(sr);
            fuelButton.draw(sr);
            craftedItemButton.draw(sr);
            drawFurnaceProcessItems(sr);
            exitTap.draw(sr);
        }
    }

    private void drawDescription(StackedRenderer sr) {
        String description = furnace.getMaterial().getItem().getDescription();
        if (description != null && !description.equals(DescriptionFactory.emptyText)) {
            descriptionLayout = new TextLayout(description, GUI.getFont(), null, 150.0f, Colour.white);
            descriptionLayout.textShape.translate(590.0f, 145.0f, 0.0f);
            descriptionLayout.textShape.render(sr);
        }
    }

    private void drawScissorBound(StackedRenderer sr) {
        if (scissorBoundShape == null) {
            Shape bs = ShapeUtil.innerQuad(20.0f, 10.0f, 440.0f, 410.0f, 4.0f, 0.0f);
            scissorBoundShape = new ColouredShape(bs, Colour.black, (State) null);
        }
        scissorBoundShape.render(sr);
    }

    protected void drawTitle(StackedRenderer sr) {
        if (title == null) {
            Font font = GUI.getFont();
            title = font.buildTextShape(TileEntity.FURNACE_ID, Colour.white);
            title.translate(((Game.gameWidth - font.getStringLength(TileEntity.FURNACE_ID)) / 2.0f) - 50.0f, (Game.gameHeight - font.size) - 20.0f, 0.0f);
            Shape s = ShapeUtil.filledQuad(20.0f, Game.gameHeight - 10.0f, Game.gameWidth - 68.0f, Game.gameHeight - 70.0f, 0.0f);
            fillTitleShape = new ColouredShape(s, Colour.packFloat(0.0f, 0.0f, 0.0f, 0.5f), (State) null);
        }
        fillTitleShape.render(sr);
        sr.render();
        title.render(sr);
    }

    private void drawFurnaceProcessItems(StackedRenderer sr) {
        if (fireShape == null) {
            fireShape = ItemFactory.Item.getShape(12, 13);
            fireShape.scale(120.0f, 120.0f, 120.0f);
            fireShape.translate(510.0f, 235.0f, 0.0f);
            activeFireShape = ItemFactory.Item.getShape(13, 13);
            activeFireShape.scale(120.0f, 120.0f, 120.0f);
            activeFireShape.translate(510.0f, 235.0f, 0.0f);
            arrowShape = ItemFactory.Item.getShape(14, 13);
            arrowShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
            arrowShape.translate(610.0f, 235.0f, 0.0f);
            activeArrowShape = ItemFactory.Item.getShape(15, 13);
            activeArrowShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
            activeArrowShape.translate(610.0f, 235.0f, 0.0f);
        }
        arrowShape.render(sr);
        fireShape.render(sr);
        sr.render();
        int progressPercent = furnace.getProcessPercent();
        int fuelProgressPercent = furnace.getFuelProcessPercent();
        GLES10.glEnable(3089);
        GLES10.glScissor((int) (580.0f * RATIO_X), (int) (210.0f * RATIO_Y), (int) (((65.0f * RATIO_X) * progressPercent) / SCALE_VALUE), (int) (RATIO_Y * SCALE_VALUE));
        activeArrowShape.render(sr);
        sr.render();
        GLES10.glDisable(3089);
        GLES10.glEnable(3089);
        GLES10.glScissor((int) (480.0f * RATIO_X), (int) (210.0f * RATIO_Y), (int) (RATIO_X * SCALE_VALUE), (int) (((45.0f * RATIO_Y) * (100 - fuelProgressPercent)) / SCALE_VALUE));
        activeFireShape.render(sr);
        sr.render();
        GLES10.glDisable(3089);
    }

    public void advance() {
        if (furnace != null) {
            if (furnace.getMaterial() == null) {
                materialTapItem = null;
            }
            if (furnace.getFuel() == null) {
                fuelTapItem = null;
            }
            if (furnace.getCraftedItem() != null) {
                if (craftedTapItem == null) {
                    craftedTapItem = new CustomTapItem(furnace.getCraftedItem(), 710.0f, 235.0f);
                    craftedTapItem.isDrawBounds = false;
                }
            } else {
                craftedTapItem = null;
            }
        }
        materialButton.advance();
        fuelButton.advance();
        craftedItemButton.advance();
        exitTap.advance();
        for (int i = 0; i < tapItems.size(); i++) {
            tapItems.get(i).advance();
        }
        if (show) {
            if (needToScroll && tapItems.size() / 5 >= 5) {
                touchDelta = touch.y - prevYpoint;
            }
            if (!tapItems.isEmpty() && tapItems.size() / 5 >= 5) {
                normalizeScroll();
            }
        }
    }

    private void normalizeScroll() {
        CustomTapItem lastItem = tapItems.get(tapItems.size() - 1);
        float bottomPoint = lastItem.getY() + lastItem.getYOffset();
        float topPoint = tapItems.get(0).bounds.y.getMax();
        if (touchDelta + topPoint < scissorBound.y.getMax()) {
            touchDelta = 0.0f;
            for (CustomTapItem item : tapItems) {
                item.setYOffset(0.0f);
            }
        }
        if (touchDelta + bottomPoint > scissorBound.y.getMin()) {
            float yOffset = (80.0f * MathUtils.roundUp(tapItems.size() / 5)) - 375.0f;
            touchDelta = 0.0f;
            for (CustomTapItem item2 : tapItems) {
                item2.setYOffset(yOffset);
            }
        }
    }

    public void showOrHide() {
        setShow(!show);
        if (isVisible()) {
            initFuranceItems(inventory);
        }
    }

    public Furnace getFurnace() {
        return furnace;
    }

    public void setFurnace(@NonNull Furnace furnace) {
        this.furnace = furnace;
        if (furnace.getFuel() != null) {
            fuelTapItem = new CustomTapItem(furnace.getFuel(), 510.0f, 140.0f);
            fuelTapItem.isDrawBounds = false;
        }
        if (furnace.getMaterial() != null) {
            materialTapItem = new CustomTapItem(furnace.getMaterial(), 510.0f, 330.0f);
            materialTapItem.isDrawBounds = false;
        }
        if (furnace.getCraftedItem() != null) {
            craftedTapItem = new CustomTapItem(furnace.getCraftedItem(), 710.0f, 235.0f);
            craftedTapItem.isDrawBounds = false;
        }
    }
}

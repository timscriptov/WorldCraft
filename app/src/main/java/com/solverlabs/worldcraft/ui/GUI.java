package com.solverlabs.worldcraft.ui;

import android.opengl.GLES10;
import android.util.Log;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.input.AbstractTouchStick;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.input.TouchStickArea;
import com.solverlabs.droid.rugl.res.FontLoader;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.blockentity.BlockEntityPainter;
import com.solverlabs.worldcraft.entity_menu.ChestMenu;
import com.solverlabs.worldcraft.entity_menu.FurnaceMenu;
import com.solverlabs.worldcraft.mob.MobPainter;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;

import java.util.ArrayList;

public class GUI {
    public static final int HEIGHT = 480;
    public static final int WIDTH = 800;
    private static final String BAN_PLAYER = "Ban Player";
    private static final String REPORT_ABUSE = "Report Abuse";
    private static final float radius = 50.0f;
    private static final float size = 150.0f;
    private static Font font = null;
    public final TouchStickArea left = new TouchStickArea(0.0f, 0.0f, size, size, radius);
    public final TouchStickArea right = new TouchStickArea(160.0f, 75.0f, 650.0f, 405.0f, 400.0f);
    public final TapPad blockMenuTap = new TapPad(570.0f, 0.0f, 100.0f, 90.0f);
    private final Game game;
    private final Player player;
    private final World world;
    private final ArrayList<Touch.TouchListener> widgets = new ArrayList<>();
    private final StackedRenderer r = new StackedRenderer();
    public ChatBox chatBox;
    public ImageSwitcherTapPad chatSwitcherTap;
    public TapPad.Listener chatSwitcherTapListener;
    public CustomTapPad chatTap;
    public TapPad.Listener chatTapListener;
    public ChestMenu chestMenu;
    public CraftMenu craftMenu;
    public CustomTapPad craftMenuTap;
    public TapPad.Listener craftTapListener;
    public DamageBar damageBar;
    public TapPad.Listener exitTapListener;
    public FoodBar foodBar;
    public FurnaceMenu furnaceMenu;
    public Hand hand;
    public HealthBar healthBar;
    /**
     * Виджет с быстрым доступом к предметам
     */
    public Hotbar hotbar;
    public Interaction interaction;
    /**
     * Виджет для открытия инвенторя
     */
    public InventoryMenu inventoryMenu;
    /**
     * Кнопка прыжок
     */
    public CustomTapPad jumpTap;
    public CustomTapPad leaveBedTap;
    public TapPad.Listener leaveBedTapListener;
    public CustomTapPad menuTap;
    public CustomTapPad reportAbuseTap;
    public TapPad.Listener reportAbuseTapListener;
    public TapPad.Listener selectBlocksListener;
    public SleepBar sleepBar;
    private TextShape notification;
    private Touch.TouchListener touchListener;
    private float notifyTime = 0.0f;

    public GUI(final Player player, World world, FPSCamera camera, MobPainter mobAggregator, BlockEntityPainter entityPainter, Game game) {
        this.world = world;
        this.player = player;
        this.game = game;
        loadFont();
        this.hand = new Hand(player);
        this.hotbar = new Hotbar(player);
        this.inventoryMenu = new InventoryMenu(player);
        if (GameMode.isSurvivalMode()) {
            this.healthBar = new HealthBar(player);
            this.foodBar = new FoodBar(player);
            this.damageBar = new DamageBar(player);
            this.sleepBar = new SleepBar(player, world);
            this.craftMenu = new CraftMenu(player.inventory);
            this.furnaceMenu = new FurnaceMenu(player.inventory);
            this.chestMenu = new ChestMenu(player.inventory);
        }
        initListeners();
        this.menuTap = new CustomTapPad(720.0f, 430.0f, 80.0f, radius, font, "Menu");
        this.menuTap.listener = this.exitTapListener;
        if (GameMode.isMultiplayerMode()) {
            this.reportAbuseTap = new CustomTapPad(540.0f, 430.0f, 180.0f, radius, font, Multiplayer.isRoomOwner() ? BAN_PLAYER : REPORT_ABUSE);
            this.reportAbuseTap.listener = this.reportAbuseTapListener;
            this.chatTap = new CustomTapPad(0.0f, 430.0f, 80.0f, radius, font, "Chat");
            this.chatTap.listener = this.chatTapListener;
            this.chatSwitcherTap = new ImageSwitcherTapPad(80.0f, 430.0f, radius, radius, 0, 14, 1, 14);
            this.chatSwitcherTap.listener = this.chatSwitcherTapListener;
        }
        if (GameMode.isMultiplayerMode() || GameMode.isSurvivalMode()) {
            this.chatBox = new ChatBox(20.0f, 160.0f, 500.0f, 30.0f, font);
        }
        this.interaction = new Interaction(player, world, camera, this.hand, mobAggregator, entityPainter, this.chatBox, game.getGameActivity());
        if (GameMode.isSurvivalMode()) {
            this.craftMenuTap = new CustomTapPad(0.0f, 430.0f, 80.0f, radius, font, "Craft");
            this.craftMenuTap.listener = this.craftTapListener;
            this.leaveBedTap = new CustomTapPad(190.0f, 90.0f, 420.0f, 90.0f, font, "Leave bed", true);
            this.leaveBedTap.listener = this.leaveBedTapListener;
        }
        this.jumpTap = new CustomTapPad(700.0f, 0.0f, 100.0f, 90.0f, font, "JUMP");
        this.jumpTap.listener = player.jumpCrouchListener;
        this.blockMenuTap.listener = this.selectBlocksListener;
        initWidgets();
        Multiplayer.instance.interaction = this.interaction;
        Touch.addListener(this.touchListener);
        AbstractTouchStick.ClickListener strikeyRight = new AbstractTouchStick.ClickListener() {
            @Override
            public void onClick() {
                interaction.action(player.inHand, right.getPointer().x, right.getPointer().y);
                if (Multiplayer.instance.movementHandler != null) {
                    Multiplayer.instance.movementHandler.action((byte) 1);
                }
            }

            @Override
            public void onClickHold(boolean active) {
                interaction.stickTouch = right.getPointer();
                interaction.touchSticksHeld = active;
                if (Multiplayer.instance.movementHandler != null) {
                    Multiplayer.instance.movementHandler.action(active ? (byte) 2 : (byte) 3);
                }
            }

            @Override
            public void onMove() {
                interaction.stickTouch = right.getPointer();
            }

            @Override
            public void onUp() {
                interaction.stickTouch = null;
            }
        };
        this.right.draw = false;
        this.right.listener = strikeyRight;
    }

    public static Font getFont() {
        return font;
    }

    private void initWidgets() {
        this.widgets.add((Touch.TouchListener) this.blockMenuTap);
        this.widgets.add(this.inventoryMenu);
        if (GameMode.isSurvivalMode()) {
            this.widgets.add((Touch.TouchListener) this.craftMenuTap);
            this.widgets.add(this.craftMenu);
            this.widgets.add(this.furnaceMenu);
            this.widgets.add(this.chestMenu);
            this.widgets.add((Touch.TouchListener) this.leaveBedTap);
        }
        this.widgets.add(this.hotbar);
        this.widgets.add((Touch.TouchListener) this.menuTap);
        this.widgets.add((Touch.TouchListener) this.jumpTap);
        if (GameMode.isMultiplayerMode()) {
            this.widgets.add((Touch.TouchListener) this.reportAbuseTap);
            this.widgets.add((Touch.TouchListener) this.chatTap);
            this.widgets.add((Touch.TouchListener) this.chatSwitcherTap);
        }
        if (GameMode.isSurvivalMode()) {
            this.widgets.add(this.sleepBar);
            this.widgets.add(this.damageBar);
        }
        this.widgets.add((Touch.TouchListener) this.left);
        this.widgets.add((Touch.TouchListener) this.right);
    }

    private void initListeners() {
        this.exitTapListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                if (world != null) {
                    world.showGameMenu();
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
        this.selectBlocksListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                inventoryMenu.show();
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
        this.chatTapListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                if (world != null) {
                    world.showChat();
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
        this.chatSwitcherTapListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                chatSwitcherTap.switchState();
                chatBox.setVisible(!chatSwitcherTap.isOn());
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
        this.reportAbuseTapListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                if (world != null) {
                    world.showReportAbuse();
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
        this.leaveBedTapListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                player.setKeptDownAt(0L);
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
        this.craftTapListener = new TapPad.Listener() {
            @Override
            public void onTap(TapPad pad) {
                showCraftMenu(false);
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
        this.touchListener = new Touch.TouchListener() {
            @Override
            public void pointerRemoved(Touch.Pointer p) {
                for (int i = 0; i < widgets.size(); i++) {
                    widgets.get(i).pointerRemoved(p);
                    p.isUse = false;
                }
            }

            @Override
            public boolean pointerAdded(Touch.Pointer p) {
                boolean eaten = false;
                for (int i = 0; i < widgets.size() && !eaten; i++) {
                    eaten = widgets.get(i).pointerAdded(p);
                }
                return false;
            }

            @Override
            public void reset() {
                for (int i = 0; i < widgets.size(); i++) {
                    widgets.get(i).reset();
                }
            }
        };
    }

    private void loadFont() {
        ResourceLoader.loadNow(new FontLoader(R.raw.font, false) {
            @Override
            public void fontLoaded() {
                GUI.font = this.resource;
            }
        });
    }

    public void advance(float delta, FPSCamera cam) {
        this.interaction.advance(delta);
        if (GameMode.isSurvivalMode()) {
            if (this.interaction.showCraftingTable) {
                showCraftMenu(true);
                this.interaction.showCraftingTable = false;
            }
            if (this.interaction.showChestMenu) {
                showChestMenu(this.interaction.tileEntityLocation);
                this.interaction.showChestMenu = false;
            }
            if (this.interaction.showFurnaceMenu) {
                showFurnaceMenu(this.interaction.tileEntityLocation);
                this.interaction.showFurnaceMenu = false;
            }
            this.healthBar.advance(delta);
            this.foodBar.advance(delta);
            this.craftMenu.advance();
            this.furnaceMenu.advance();
            this.chestMenu.advance();
            this.damageBar.advance(delta);
            this.sleepBar.advance(delta);
            if (this.sleepBar != null) {
                this.leaveBedTap.advance();
                this.leaveBedTap.isVisible = this.sleepBar.isFirstCircle();
            }
            if (this.player.justAttacked()) {
                this.craftMenu.setShow(false);
                this.furnaceMenu.setShow(false);
            }
        }
        if (needToDrawGui()) {
            if (GameMode.isSurvivalMode()) {
                this.craftMenuTap.advance();
            }
            this.left.advance();
            this.right.advance();
            if (this.inventoryMenu.isShow()) {
                this.inventoryMenu.advance();
            }
            this.blockMenuTap.advance();
            this.menuTap.advance();
            this.jumpTap.advance();
            this.hotbar.advance(delta);
            this.hand.advance(delta);
        }
        if (GameMode.isMultiplayerMode()) {
            this.reportAbuseTap.advance();
            this.chatTap.advance();
            this.chatSwitcherTap.advance();
        }
        if (GameMode.isMultiplayerMode() || GameMode.isSurvivalMode()) {
            this.chatBox.advance(delta);
        }
        this.notifyTime -= delta;
        if (this.notifyTime < 0.0f) {
            this.notification = null;
        }
        cam.advance(this.right.x, this.right.y);
    }

    public void draw() {
        GLUtil.scaledOrtho(Game.gameWidth, Game.gameHeight, Game.screenWidth, Game.screenHeight, -1.0f, 1.0f);
        GLES10.glClear(256);
        if (needToDrawGui() && (this.sleepBar == null || !this.sleepBar.isFirstCircle())) {
            this.hand.draw(this.r);
            this.left.draw(this.r);
            this.right.draw(this.r);
            this.r.render();
            this.menuTap.draw(this.r);
            if (GameMode.isMultiplayerMode()) {
                this.reportAbuseTap.draw(this.r);
                this.chatTap.draw(this.r);
                this.chatSwitcherTap.draw(this.r);
            }
            if (GameMode.isMultiplayerMode() || GameMode.isSurvivalMode()) {
                this.chatBox.draw(this.r);
            }
            if (this.player.ghost) {
                this.jumpTap.setText("WALK");
            } else {
                this.jumpTap.setText("JUMP");
            }
            this.jumpTap.draw(this.r);
            this.hotbar.draw(this.r);
            if (GameMode.isSurvivalMode()) {
                this.healthBar.draw(this.r);
                this.foodBar.draw(this.r);
            }
            this.inventoryMenu.draw(this.r);
            this.blockMenuTap.draw(this.r);
            if (GameMode.isSurvivalMode()) {
                this.craftMenuTap.draw(this.r);
            }
        }
        if (GameMode.isSurvivalMode()) {
            this.craftMenu.draw(this.r);
            this.furnaceMenu.draw(this.r);
            this.chestMenu.draw(this.r);
            this.leaveBedTap.draw(this.r);
        }
        if (this.notification != null) {
            this.notification.render(this.r);
        }
        if (GameMode.isSurvivalMode()) {
            this.sleepBar.draw(this.r);
            this.damageBar.draw(this.r);
        }
        this.r.render();
    }

    private boolean needToDrawGui() {
        return GameMode.isCreativeMode() || !(this.craftMenu == null || this.craftMenu.isVisible() || this.furnaceMenu == null || this.furnaceMenu.isVisible() || this.chestMenu == null || this.chestMenu.isVisible());
    }

    public void notify(String string) {
        Log.i(Game.RUGL_TAG, "Notification: " + string);
        if (font != null) {
            this.notification = font.buildTextShape(string, Colour.black);
            this.notification.translate((800.0f - this.notification.getBounds().x.getSpan()) / 2.0f, 100.0f, 0.0f);
            this.notifyTime = 1.5f;
        }
    }

    public void showCraftMenu(boolean isWorkBanch) {
        this.craftMenu.showOrHide(isWorkBanch);
        this.inventoryMenu.setShow(false);
    }

    public void showFurnaceMenu(Vector3i pos) {
        this.furnaceMenu.setFurnace(this.world.getFurnace(pos));
        this.furnaceMenu.showOrHide();
    }

    public void showChestMenu(Vector3i pos) {
        this.chestMenu.setChest(this.world.getChest(pos));
        this.chestMenu.showOrHide();
    }
}

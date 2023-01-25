package com.mcal.worldcraft.ui;

import android.opengl.GLES10;
import android.util.Log;

import com.mcal.droid.rugl.Game;
import com.mcal.droid.rugl.gl.GLUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.input.AbstractTouchStick;
import com.mcal.droid.rugl.input.TapPad;
import com.mcal.droid.rugl.input.Touch;
import com.mcal.droid.rugl.input.TouchStickArea;
import com.mcal.droid.rugl.res.FontLoader;
import com.mcal.droid.rugl.res.ResourceLoader;
import com.mcal.droid.rugl.text.Font;
import com.mcal.droid.rugl.text.TextShape;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.droid.rugl.util.FPSCamera;
import com.mcal.droid.rugl.util.geom.Vector3i;
import com.mcal.worldcraft.GameMode;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.R;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.blockentity.BlockEntityPainter;
import com.mcal.worldcraft.entity_menu.ChestMenu;
import com.mcal.worldcraft.entity_menu.FurnaceMenu;
import com.mcal.worldcraft.mob.MobPainter;
import com.mcal.worldcraft.multiplayer.Multiplayer;

import java.util.ArrayList;

/**
 * Holds the touchsticks
 */
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
    /**
     * Еда
     */
    public FoodBar foodBar;
    public FurnaceMenu furnaceMenu;
    /**
     * Рука
     */
    public Hand hand;
    /**
     * Жизни
     */
    public HealthBar healthBar;
    /**
     * Инвентарь
     */
    public Hotbar hotbar;
    public Interaction interaction;
    /**
     * Меню инвенторя
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

    /**
     * @param player
     * @param world
     * @param camera
     * @param mobAggregator
     */
    public GUI(final Player player, World world, FPSCamera camera, MobPainter mobAggregator, BlockEntityPainter entityPainter, Game game) {
        this.world = world;
        this.player = player;
        this.game = game;
        loadFont();
        hand = new Hand(player);
        hotbar = new Hotbar(player);
        inventoryMenu = new InventoryMenu(player);
        if (GameMode.isSurvivalMode()) {
            healthBar = new HealthBar(player);
            foodBar = new FoodBar(player);
            damageBar = new DamageBar(player);
            sleepBar = new SleepBar(player, world);
            craftMenu = new CraftMenu(player.inventory);
            furnaceMenu = new FurnaceMenu(player.inventory);
            chestMenu = new ChestMenu(player.inventory);
        }
        initListeners();
        menuTap = new CustomTapPad(720.0f, 430.0f, 80.0f, radius, font, "Menu");
        menuTap.listener = exitTapListener;
        if (GameMode.isMultiplayerMode()) {
            reportAbuseTap = new CustomTapPad(540.0f, 430.0f, 180.0f, radius, font, Multiplayer.isRoomOwner() ? BAN_PLAYER : REPORT_ABUSE);
            reportAbuseTap.listener = reportAbuseTapListener;
            chatTap = new CustomTapPad(0.0f, 430.0f, 80.0f, radius, font, "Chat");
            chatTap.listener = chatTapListener;
            chatSwitcherTap = new ImageSwitcherTapPad(80.0f, 430.0f, radius, radius, 0, 14, 1, 14);
            chatSwitcherTap.listener = chatSwitcherTapListener;
        }
        if (GameMode.isMultiplayerMode() || GameMode.isSurvivalMode()) {
            chatBox = new ChatBox(20.0f, 160.0f, 500.0f, 30.0f, font);
        }
        interaction = new Interaction(player, world, camera, hand, mobAggregator, entityPainter, chatBox, game.getGameActivity());
        if (GameMode.isSurvivalMode()) {
            craftMenuTap = new CustomTapPad(0.0f, 430.0f, 80.0f, radius, font, "Craft");
            craftMenuTap.listener = craftTapListener;
            leaveBedTap = new CustomTapPad(190.0f, 90.0f, 420.0f, 90.0f, font, "Leave bed", true);
            leaveBedTap.listener = leaveBedTapListener;
        }
        jumpTap = new CustomTapPad(700.0f, 0.0f, 100.0f, 90.0f, font, "JUMP");
        jumpTap.listener = player.jumpCrouchListener;
        blockMenuTap.listener = selectBlocksListener;
        initWidgets();
        Multiplayer.instance.interaction = interaction;
        Touch.addListener(touchListener);
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
        right.draw = false;
        right.listener = strikeyRight;
    }

    public static Font getFont() {
        return font;
    }

    private void initWidgets() {
        widgets.add(blockMenuTap);
        widgets.add(inventoryMenu);
        if (GameMode.isSurvivalMode()) {
            widgets.add(craftMenuTap);
            widgets.add(craftMenu);
            widgets.add(furnaceMenu);
            widgets.add(chestMenu);
            widgets.add(leaveBedTap);
        }
        widgets.add(hotbar);
        widgets.add(menuTap);
        widgets.add(jumpTap);
        if (GameMode.isMultiplayerMode()) {
            widgets.add(reportAbuseTap);
            widgets.add(chatTap);
            widgets.add(chatSwitcherTap);
        }
        if (GameMode.isSurvivalMode()) {
            widgets.add(sleepBar);
            widgets.add(damageBar);
        }
        widgets.add(left);
        widgets.add(right);
    }

    private void initListeners() {
        exitTapListener = new TapPad.Listener() {
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
        selectBlocksListener = new TapPad.Listener() {
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
        chatTapListener = new TapPad.Listener() {
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
        chatSwitcherTapListener = new TapPad.Listener() {
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
        reportAbuseTapListener = new TapPad.Listener() {
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
        leaveBedTapListener = new TapPad.Listener() {
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
        craftTapListener = new TapPad.Listener() {
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
        touchListener = new Touch.TouchListener() {
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
                GUI.font = resource;
            }
        });
    }

    /**
     * @param delta time delta
     * @param cam   to apply steering to
     */
    public void advance(float delta, FPSCamera cam) {
        interaction.advance(delta);
        if (GameMode.isSurvivalMode()) {
            if (interaction.showCraftingTable) {
                showCraftMenu(true);
                interaction.showCraftingTable = false;
            }
            if (interaction.showChestMenu) {
                showChestMenu(interaction.tileEntityLocation);
                interaction.showChestMenu = false;
            }
            if (interaction.showFurnaceMenu) {
                showFurnaceMenu(interaction.tileEntityLocation);
                interaction.showFurnaceMenu = false;
            }
            healthBar.advance(delta);
            foodBar.advance(delta);
            craftMenu.advance();
            furnaceMenu.advance();
            chestMenu.advance();
            damageBar.advance(delta);
            sleepBar.advance(delta);
            if (sleepBar != null) {
                leaveBedTap.advance();
                leaveBedTap.isVisible = sleepBar.isFirstCircle();
            }
            if (player.justAttacked()) {
                craftMenu.setShow(false);
                furnaceMenu.setShow(false);
            }
        }
        if (needToDrawGui()) {
            if (GameMode.isSurvivalMode()) {
                craftMenuTap.advance();
            }
            left.advance();
            right.advance();
            if (inventoryMenu.isShow()) {
                inventoryMenu.advance();
            }
            blockMenuTap.advance();
            menuTap.advance();
            jumpTap.advance();
            hotbar.advance(delta);
            hand.advance(delta);
        }
        if (GameMode.isMultiplayerMode()) {
            reportAbuseTap.advance();
            chatTap.advance();
            chatSwitcherTap.advance();
        }
        if (GameMode.isMultiplayerMode() || GameMode.isSurvivalMode()) {
            chatBox.advance(delta);
        }
        notifyTime -= delta;
        if (notifyTime < 0.0f) {
            notification = null;
        }
        cam.advance(right.x, right.y);
    }

    /**
     * Note that the projection matrix will be changed and the depth buffer
     * cleared in here
     */
    public void draw() {
        GLUtil.scaledOrtho(Game.gameWidth, Game.gameHeight, Game.screenWidth, Game.screenHeight, -1.0f, 1.0f);
        GLES10.glClear(GLES10.GL_DEPTH_BUFFER_BIT);
        if (needToDrawGui() && (sleepBar == null || !sleepBar.isFirstCircle())) {
            hand.draw(r);
            left.draw(r);
            right.draw(r);
            r.render();
            menuTap.draw(r);
            if (GameMode.isMultiplayerMode()) {
                reportAbuseTap.draw(r);
                chatTap.draw(r);
                chatSwitcherTap.draw(r);
            }
            if (GameMode.isMultiplayerMode() || GameMode.isSurvivalMode()) {
                chatBox.draw(r);
            }
            if (player.ghost) {
                jumpTap.setText("WALK");
            } else {
                jumpTap.setText("JUMP");
            }
            jumpTap.draw(r);
            hotbar.draw(r);
            if (GameMode.isSurvivalMode()) {
                healthBar.draw(r);
                foodBar.draw(r);
            }
            inventoryMenu.draw(r);
            blockMenuTap.draw(r);
            if (GameMode.isSurvivalMode()) {
                craftMenuTap.draw(r);
            }
        }
        if (GameMode.isSurvivalMode()) {
            craftMenu.draw(r);
            furnaceMenu.draw(r);
            chestMenu.draw(r);
            leaveBedTap.draw(r);
        }
        if (notification != null) {
            notification.render(r);
        }
        if (GameMode.isSurvivalMode()) {
            sleepBar.draw(r);
            damageBar.draw(r);
        }
        r.render();
    }

    private boolean needToDrawGui() {
        return GameMode.isCreativeMode() || !(craftMenu == null || craftMenu.isVisible() || furnaceMenu == null || furnaceMenu.isVisible() || chestMenu == null || chestMenu.isVisible());
    }

    /**
     * @param string
     */
    public void notify(String string) {
        Log.i(Game.RUGL_TAG, "Notification: " + string);
        if (font != null) {
            notification = font.buildTextShape(string, Colour.black);
            notification.translate((800.0f - notification.getBounds().x.getSpan()) / 2.0f, 100.0f, 0.0f);
            notifyTime = 1.5f;
        }
    }

    public void showCraftMenu(boolean isWorkBanch) {
        craftMenu.showOrHide(isWorkBanch);
        inventoryMenu.setShow(false);
    }

    public void showFurnaceMenu(Vector3i pos) {
        furnaceMenu.setFurnace(world.getFurnace(pos));
        furnaceMenu.showOrHide();
    }

    public void showChestMenu(Vector3i pos) {
        chestMenu.setChest(world.getChest(pos));
        chestMenu.showOrHide();
    }
}

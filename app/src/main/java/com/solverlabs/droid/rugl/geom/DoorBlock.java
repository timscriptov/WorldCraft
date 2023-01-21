package com.solverlabs.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.chunk.Chunklet;
import com.solverlabs.worldcraft.chunk.GeometryGenerator;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.skin.geometry.Parallelepiped;

import org.jetbrains.annotations.Contract;


public class DoorBlock {
    private static final float DEPTH = 0.125f;
    private static final byte DOOR_OPEN = 4;
    private static final byte NORTH_EAST_DIRECTION = 0;
    private static final byte NORTH_WEST_DIRECTION = 3;
    private static final byte SOUTH_EAST_DIRECTION = 1;
    private static final byte SOUTH_WEST_DIRECTION = 2;
    private static final byte UPPER_DOOR_BLOCK = 8;
    private static final float WIDTH = 1.0f;
    private static final float HEIGHT = 2.0f;
    private static final Vector3f DOOR_SIZE = new Vector3f(WIDTH, HEIGHT, 0.125f);
    private static final int[] woodTc = {1, 5, 1, 5, 1, 5, 1, 5, 1, 5, 1, 5};
    private static final int[] ironTc = {2, 5, 2, 5, 2, 5, 2, 5, 2, 5, 2, 5};
    private static final float SXTN = 0.0625f;
    private static final Parallelepiped woodDoorParallelepiped = Parallelepiped.createParallelepiped(WIDTH, HEIGHT, 0.125f, SXTN, woodTc);
    private static final Parallelepiped ironDoorParallelepiped = Parallelepiped.createParallelepiped(WIDTH, HEIGHT, 0.125f, SXTN, ironTc);
    private static final ColouredShape doorPreviewShape = new ColouredShape(ShapeUtil.cuboid(0.0f, 0.0f, 0.0f, WIDTH, HEIGHT, 0.125f), Colour.packFloat(WIDTH, WIDTH, WIDTH, 0.3f), ShapeUtil.state);

    private DoorBlock() {
    }

    @NonNull
    public static TexturedShape getItemShape(byte id) {
        return isWoodDoor(id) ? ItemFactory.Item.getShape(ItemFactory.WOOD_DOOR_TEX_COORDS) : ItemFactory.Item.getShape(ItemFactory.IRON_DOOR_TEX_COORDS);
    }

    public static byte getClosedState(byte blockType) {
        if (blockType == 64) {
            return BlockFactory.CLOSED_WOOD_DOOR_ID;
        }
        if (blockType == 66) {
            return BlockFactory.CLOSED_IRON_DOOR_ID;
        }
        return blockType;
    }

    public static void renderPreview(@NonNull World world, StackedRenderer renderer) {
        byte previewBlockData = world.getPreviewBlockData();
        boolean isOpen = isDoorOpen(previewBlockData);
        byte direction = (byte) (previewBlockData & 3);
        if (isDoor(world.getDownPreviewBlockType())) {
            renderer.translate(0.0f, -1.0f, 0.0f);
        }
        ColouredShape shape = getPreviewShape();
        switch (direction) {
            case 0:
                if (isOpen) {
                    renderer.translate(0.125f, 0.0f, 0.0f);
                    renderer.rotate(270.0f, 0.0f, WIDTH, 0.0f);
                    break;
                } else {
                    renderer.translate(0.0f, 0.0f, 0.875f);
                    break;
                }
            case 1:
                if (isOpen) {
                    renderer.rotate(0.0f, 0.0f, WIDTH, 0.0f);
                    break;
                } else {
                    renderer.translate(0.125f, 0.0f, 0.0f);
                    renderer.rotate(270.0f, 0.0f, WIDTH, 0.0f);
                    break;
                }
            case 2:
                if (isOpen) {
                    renderer.translate(WIDTH, 0.0f, 0.0f);
                    renderer.rotate(270.0f, 0.0f, WIDTH, 0.0f);
                    break;
                } else {
                    renderer.rotate(0.0f, 0.0f, WIDTH, 0.0f);
                    break;
                }
            case 3:
                if (isOpen) {
                    renderer.translate(0.0f, 0.0f, 0.875f);
                    break;
                } else {
                    renderer.translate(WIDTH, 0.0f, 0.0f);
                    renderer.rotate(270.0f, 0.0f, WIDTH, 0.0f);
                    break;
                }
        }
        shape.render(renderer);
    }

    public static void generateGeometry(Chunklet c, ShapeBuilder opaqueShapeBuilder, int x, int y, int z, byte blockId, int colour, byte data) {
        boolean isUpperBlock = isUpperDoorBlock(data);
        if (!isUpperBlock && isDoor(c.blockType(x, y + 1, z))) {
            addFaces(c, x, y, z, colour, opaqueShapeBuilder, blockId, data);
        }
    }

    public static void actionDoor(byte blockType, @NonNull World world, @NonNull Vector3i blockLocation) {
        Chunklet chunklet = world.getChunklet(blockLocation.x, blockLocation.y, blockLocation.z);
        rotateDoorBlock(chunklet.parent, blockLocation, blockType);
        GeometryGenerator.generate(chunklet, false);
        byte data = world.getBlockDataAbsolute(blockLocation.x, blockLocation.y, blockLocation.z);
        SoundManager.playDoorChangeType(isDoorOpen(data), 0.0f);
    }

    private static void rotateDoorBlock(@NonNull Chunk chunk, @NonNull Vector3i blockLocation, byte blockType) {
        float x = blockLocation.x;
        float y = blockLocation.y;
        float z = blockLocation.z;
        byte bottomBlockData = chunk.blockDataForPosition(x, y - WIDTH, z);
        byte bottomBlockType = chunk.blockTypeForPosition(x, y - WIDTH, z);
        byte data = chunk.blockDataForPosition(x, y, z);
        if (isDoor(bottomBlockType) && !isUpperDoorBlock(bottomBlockData)) {
            data = (byte) (data | 8);
            chunk.setBlockTypeForPosition(x, y, z, blockType, data);
        }
        boolean isUpperDoorBlock = isUpperDoorBlock(data);
        if (isUpperDoorBlock) {
            chunk.setBlockTypeForPosition(x, y, z, blockType, (byte) (data ^ 4));
            byte downBlockData = chunk.blockDataForPosition(x, y - WIDTH, z);
            chunk.setBlockTypeForPosition(x, y - WIDTH, z, blockType, (byte) (downBlockData ^ 4));
            return;
        }
        chunk.setBlockTypeForPosition(x, y, z, blockType, (byte) (data ^ 4));
        byte upperBlockData = chunk.blockDataForPosition(x, WIDTH + y, z);
        chunk.setBlockTypeForPosition(x, y + WIDTH, z, blockType, (byte) (upperBlockData ^ 4));
    }

    private static boolean isUpperDoorBlock(byte data) {
        return (data & 8) == 8;
    }

    public static void breakDoor(@NonNull Chunk chunk, @NonNull Vector3i blockLocation) {
        int x = blockLocation.x;
        int y = blockLocation.y;
        int z = blockLocation.z;
        byte data = chunk.blockDataForPosition(x, y, z);
        boolean isUpperDoorBlock = isUpperDoorBlock(data);
        if (isUpperDoorBlock) {
            chunk.setBlockTypeForPosition(x, y, z, (byte) 0, (byte) 0);
            chunk.setBlockTypeForPosition(x, y - 1, z, (byte) 0, (byte) 0);
            return;
        }
        chunk.setBlockTypeForPosition(x, y, z, (byte) 0, (byte) 0);
        chunk.setBlockTypeForPosition(x, y + 1, z, (byte) 0, (byte) 0);
    }

    @NonNull
    @Contract("_, _ -> new")
    public static Vector3i getDoorDownBlockLocation(@NonNull Chunk chunk, @NonNull Vector3i blockLocation) {
        int x = blockLocation.x;
        int y = blockLocation.y;
        int z = blockLocation.z;
        byte blockType = chunk.blockTypeForPosition(x, y, z);
        if (isDoor(blockType)) {
            byte downBlockType = chunk.blockTypeForPosition(x, y - 1, z);
            if (isDoor(downBlockType)) {
                return new Vector3i(x, y - 1, z);
            }
        }
        return new Vector3i(blockLocation);
    }

    public static boolean placeDoor(Chunk chunk, @NonNull Vector3i placementTargetBlock, InventoryItem invItem, Player player) {
        int x = placementTargetBlock.x;
        int y = placementTargetBlock.y;
        int z = placementTargetBlock.z;
        byte direction = 0;
        switch (player.getCurrentWorldSide()) {
            case North:
                direction = 2;
                break;
            case South:
                direction = 0;
                break;
            case East:
                direction = 3;
                break;
            case West:
                direction = 1;
                break;
        }
        if (chunk.blockTypeForPosition(x, y + 1, z) == 0) {
            chunk.setBlockTypeForPosition(x, y, z, invItem.getItemID(), direction);
            chunk.setBlockTypeForPosition(x, y + 1, z, invItem.getItemID(), (byte) (direction | 8));
            if (GameMode.isSurvivalMode()) {
                player.inventory.decItem(invItem);
            }
            return true;
        }
        return false;
    }

    public static void updateBlockBounds(BoundingCuboid blockBounds, float x, float y, float z, @NonNull World world) {
        Byte data = world.getBlockDataAbsolute((int) x, (int) y, (int) z);
        if (data != null) {
            boolean isOpen = isDoorOpen(data);
            byte direction = (byte) (data & 3);
            switch (direction) {
                case 0:
                    if (isOpen) {
                        blockBounds.set(x, y, z, x + 0.125f, HEIGHT + y, WIDTH + z);
                        return;
                    } else {
                        blockBounds.set(x, y, z + 0.875f, x + WIDTH, HEIGHT + y, WIDTH + z);
                        return;
                    }
                case 1:
                    if (isOpen) {
                        blockBounds.set(x, y, z, x + WIDTH, HEIGHT + y, z + 0.125f);
                        return;
                    } else {
                        blockBounds.set(x, y, z, x + 0.125f, HEIGHT + y, WIDTH + z);
                        return;
                    }
                case 2:
                    if (isOpen) {
                        blockBounds.set(x + 0.875f, y, z, x + WIDTH, HEIGHT + y, WIDTH + z);
                        return;
                    } else {
                        blockBounds.set(x, y, z, x + WIDTH, HEIGHT + y, z + 0.125f);
                        return;
                    }
                case 3:
                    if (isOpen) {
                        blockBounds.set(x, y, z + 0.875f, x + WIDTH, HEIGHT + y, WIDTH + z);
                        return;
                    } else {
                        blockBounds.set(x + 0.875f, y, z, x + WIDTH, HEIGHT + y, WIDTH + z);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private static void addFaces(Chunklet c, int x, int y, int z, int colour, ShapeBuilder opaque, byte id, byte data) {
        Parallelepiped p = getParallelepiped(id).clone();
        boolean[] invertTexture = {false, false, false, true, false, false};
        Vector3f blockSize = getBlockSize();
        byte direction = (byte) (data & 3);
        boolean isOpen = isDoorOpen(data);
        switch (direction) {
            case 0:
                if (isOpen) {
                    p.rotate(1.5707964f, 0, 1, 0);
                    break;
                }
                break;
            case 1:
                if (isOpen) {
                    p.rotate(6.2831855f, 0, 1, 0);
                    break;
                } else {
                    p.rotate(4.712389f, 0, 1, 0);
                    break;
                }
            case 2:
                if (isOpen) {
                    p.rotate(4.712389f, 0, 1, 0);
                    break;
                } else {
                    p.rotate(3.1415927f, 0, 1, 0);
                    break;
                }
            case 3:
                if (isOpen) {
                    p.rotate(3.1415927f, 0, 1, 0);
                    break;
                } else {
                    p.rotate(1.5707964f, 0, 1, 0);
                    break;
                }
        }
        Vector3f offset = getOffset(direction, isOpen);
        float faceX = x + offset.x;
        float faceY = y + offset.y;
        float faceZ = z + offset.z;
        addFace(p, faceX, faceY, faceZ, p.south, blockSize.z, blockSize.y, colour, opaque, invertTexture[0]);
        addFace(p, faceX, faceY, faceZ, p.north, blockSize.z, blockSize.y, colour, opaque, invertTexture[1]);
        addFace(p, faceX, faceY, faceZ, p.west, blockSize.x, blockSize.y, colour, opaque, invertTexture[2]);
        addFace(p, faceX, faceY, faceZ, p.east, blockSize.x, blockSize.y, colour, opaque, invertTexture[3]);
        addFace(p, faceX, faceY, faceZ, p.bottom, blockSize.x, blockSize.z, colour, opaque, invertTexture[4]);
        addFace(p, faceX, faceY, faceZ, p.top, blockSize.x, blockSize.z, colour, opaque, invertTexture[5]);
    }

    private static void addFace(@NonNull Parallelepiped facing, float x, float y, float z, Parallelepiped.Face f, float width, float height, int colour, ShapeBuilder shapBuilder, boolean invertTexture) {
        facing.face(f, x, y, z, width, height, colour, shapBuilder, invertTexture);
    }

    @NonNull
    @Contract("_, _ -> new")
    private static Vector3f getOffset(byte direction, boolean isOpen) {
        switch (direction) {
            case 0:
                if (!isOpen) {
                    return new Vector3f(0.0f, 0.0f, 0.875f);
                }
                return new Vector3f(-0.4375f, 0.0f, 0.4375f);
            case 1:
                if (!isOpen) {
                    return new Vector3f(-0.4375f, 0.0f, 0.4375f);
                }
                return new Vector3f(0.0f, 0.0f, 0.0f);
            case 2:
                if (!isOpen) {
                    return new Vector3f(0.0f, 0.0f, 0.0f);
                }
                return new Vector3f(0.4375f, 0.0f, 0.4375f);
            case 3:
                if (!isOpen) {
                    return new Vector3f(0.4375f, 0.0f, 0.4375f);
                }
                return new Vector3f(0.0f, 0.0f, 0.875f);
            default:
                return new Vector3f(0.0f, 0.0f, 0.0f);
        }
    }

    private static Parallelepiped getParallelepiped(byte id) {
        if (isWoodDoor(id)) {
            return woodDoorParallelepiped;
        }
        if (isIronDoor(id)) {
            return ironDoorParallelepiped;
        }
        return woodDoorParallelepiped;
    }

    public static boolean isDoor(BlockFactory.Block b) {
        return b != null && isDoor(b.id);
    }

    public static boolean isDoor(Byte blockType) {
        return blockType != null && (blockType == 62 || blockType == 64 || blockType == 65 || blockType == 66);
    }

    public static boolean isWoodDoor(byte blockType) {
        return blockType == 62 || blockType == 64;
    }

    public static boolean isOpenedDoor(byte id) {
        return id == 64 || id == 66;
    }

    private static boolean isIronDoor(byte blockType) {
        return blockType == 65 || blockType == 66;
    }

    private static boolean isDoorOpen(byte data) {
        return (data & 4) == 4;
    }

    private static ColouredShape getPreviewShape() {
        return doorPreviewShape;
    }

    private static Vector3f getBlockSize() {
        return DOOR_SIZE;
    }
}

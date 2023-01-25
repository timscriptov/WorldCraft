package com.mcal.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.droid.rugl.util.geom.Vector3i;
import com.mcal.worldcraft.GameMode;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.chunk.Chunk;
import com.mcal.worldcraft.chunk.Chunklet;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.factories.ItemFactory;
import com.mcal.worldcraft.inventory.InventoryItem;
import com.mcal.worldcraft.skin.geometry.Parallelepiped;

import org.jetbrains.annotations.Contract;


public class BedBlock {
    private static final float BED_NS_DEPTH = 1.0f;
    private static final float BED_NS_WIDTH = 1.0f;
    private static final byte BLOCK_WITH_PILLOW = 8;
    private static final float DEPTH = 1.0f;
    private static final byte EAST = 3;
    private static final byte NORTH = 2;
    private static final byte SOUTH = 0;
    private static final byte WEST = 1;
    private static final float WIDTH = 1.0f;
    private static final float HEIGHT = 0.5625f;
    private static final Vector3f BED_SIZE = new Vector3f(1.0f, HEIGHT, 1.0f);
    private static final float[] tcWithPillow = {8.0f, 9.4375f, 5.0f, 9.4375f, 7.0f, 9.4375f, 7.0f, 9.4375f, 7.0f, 8.0f, 4.0f, 11.0f};
    private static final float[] tcWithoutPillow = {8.0f, 9.4375f, 5.0f, 9.4375f, 6.0f, 9.4375f, 6.0f, 9.4375f, 6.0f, 8.0f, 4.0f, 11.0f};
    private static final float SXTN = 0.0625f;
    private static final Parallelepiped bedWithPillow = Parallelepiped.createParallelepiped(1.0f, HEIGHT, 1.0f, SXTN, tcWithPillow);
    private static final Parallelepiped bedWithOutPillow = Parallelepiped.createParallelepiped(1.0f, HEIGHT, 1.0f, SXTN, tcWithoutPillow);
    private static final ColouredShape bedPreviewShape = new ColouredShape(ShapeUtil.cuboid(-0.01f, -0.01f, -0.01f, 2.01f, 0.5725f, 1.01f), Colour.packFloat(1.0f, 1.0f, 1.0f, 0.3f), ShapeUtil.state);

    private BedBlock() {
    }

    public static boolean isBed(BlockFactory.Block b) {
        return b != null && isBed(b.id);
    }

    public static boolean isBed(Byte blockType) {
        return blockType != null && blockType == BlockFactory.BED_ID;
    }

    @NonNull
    @Contract("_ -> new")
    public static TexturedShape getItemShape(byte id) {
        return ItemFactory.Item.getShape(ItemFactory.BED_TEX_COORDS);
    }

    public static byte getDefaultState(byte blockType) {
        return BlockFactory.BED_ID;
    }

    public static void renderPreview(@NonNull World world, StackedRenderer renderer) {
        Byte previewBlockData = world.getPreviewBlockData();
        boolean isPillow = (previewBlockData & 8) == 8;
        byte direction = (byte) (previewBlockData & 3);
        switch (direction) {
            case SOUTH:
                renderer.rotate(90.0f, 0.0f, 1.0f, 0.0f);
                if (!isPillow) {
                    renderer.translate(-1.0f, 0.0f, 0.0f);
                } else {
                    renderer.translate(-2.0f, 0.0f, 0.0f);
                }
                break;
            case WEST:
                if (isPillow) {
                    renderer.translate(-1.0f, 0.0f, 0.0f);
                    break;
                }
                break;
            case NORTH:
                renderer.rotate(90.0f, 0.0f, 1.0f, 0.0f);
                if (isPillow) {
                    renderer.translate(-1.0f, 0.0f, 0.0f);
                    break;
                } else {
                    renderer.translate(-2.0f, 0.0f, 0.0f);
                    break;
                }
            case EAST:
                if (!isPillow) {
                    renderer.translate(-1.0f, 0.0f, 0.0f);
                    break;
                }
                break;
        }
        getPreviewShape().render(renderer);
    }

    public static void generateGeometry(Chunklet c, ShapeBuilder opaqueShapeBuilder, int x, int y, int z, byte data, int colour) {
        boolean isPillow = isPillow(data);
        byte direction = (byte) (data & 3);
        Parallelepiped p = isPillow ? bedWithPillow.clone() : bedWithOutPillow.clone();
        if (direction == 0 && !isPillow && !isBed(c.blockType(x, y, z - 1)) && !isBed(c.blockType(x, y, z + 1))) {
            c.setBlockType(x, y, z - 1, BlockFactory.BED_ID, (byte) (data | 8));
            return;
        }
        rotateBed(direction, p);
        addFaces(c, x, y, z, colour, opaqueShapeBuilder, p, isPillow);
    }

    private static void rotateBed(byte direction, Parallelepiped p) {
        switch (direction) {
            case 0:
                p.rotate(4.712389f, 0, 1, 0);
                return;
            case 1:
                p.rotate(3.1415927f, 0, 1, 0);
                return;
            case 2:
                p.rotate(1.5707964f, 0, 1, 0);
                return;
            case 3:
            default:
                return;
        }
    }

    private static boolean isPillow(byte data) {
        return (data & 8) == 8;
    }

    public static void breakBed(Chunk chunk, @NonNull Vector3i blockLocation, @NonNull Player player) {
        int x = blockLocation.x;
        int y = blockLocation.y;
        int z = blockLocation.z;
        if (player.spawnBedExists(x, y, z)) {
            player.setWorldStartPosAsSpawnPos();
        }
        byte data = chunk.blockDataForPosition(x, y, z);
        boolean isPillow = isPillow(data);
        byte direction = (byte) (data & 3);
        chunk.setBlockTypeForPosition(x, y, z, (byte) 0, (byte) 0);
        switch (direction) {
            case 0:
                if (isPillow) {
                    chunk.setBlockTypeForPosition(x, y, z + 1, (byte) 0, (byte) 0);
                    return;
                } else {
                    chunk.setBlockTypeForPosition(x, y, z - 1, (byte) 0, (byte) 0);
                    return;
                }
            case 1:
                if (isPillow) {
                    chunk.setBlockTypeForPosition(x - 1, y, z, (byte) 0, (byte) 0);
                    return;
                } else {
                    chunk.setBlockTypeForPosition(x + 1, y, z, (byte) 0, (byte) 0);
                    return;
                }
            case 2:
                if (isPillow) {
                    chunk.setBlockTypeForPosition(x, y, z - 1, (byte) 0, (byte) 0);
                    return;
                } else {
                    chunk.setBlockTypeForPosition(x, y, z + 1, (byte) 0, (byte) 0);
                    return;
                }
            case 3:
                if (isPillow) {
                    chunk.setBlockTypeForPosition(x + 1, y, z, (byte) 0, (byte) 0);
                    return;
                } else {
                    chunk.setBlockTypeForPosition(x - 1, y, z, (byte) 0, (byte) 0);
                    return;
                }
            default:
                return;
        }
    }

    @NonNull
    @Contract("_, _ -> new")
    public static Vector3i getBedLeftBlockLocation(@NonNull Chunk chunk, @NonNull Vector3i blockLocation) {
        int x = blockLocation.x;
        int y = blockLocation.y;
        int z = blockLocation.z;
        byte blockType = chunk.blockTypeForPosition(x, y, z);
        if (isBed(blockType)) {
            byte leftBlockType = chunk.blockTypeForPosition(x - 1, y, z);
            if (isBed(leftBlockType)) {
                return new Vector3i(x - 1, y, z);
            }
        }
        return new Vector3i(blockLocation);
    }

    public static boolean placeBed(Chunk chunk, @NonNull Vector3i placementTargetBlock, InventoryItem invItem, @NonNull Player player) {
        int x = placementTargetBlock.x;
        int y = placementTargetBlock.y;
        int z = placementTargetBlock.z;
        switch (player.getCurrentWorldSide()) {
            case North:
                if (chunk.blockTypeForPosition(x, y, z + 1) == 0) {
                    chunk.setBlockTypeForPosition(x, y, z, invItem.getItemID(), (byte) 2);
                    chunk.setBlockTypeForPosition(x, y, z + 1, invItem.getItemID(), (byte) 10);
                    if (GameMode.isSurvivalMode()) {
                        player.inventory.decItem(invItem);
                    }
                    return true;
                }
                return false;
            case South:
                if (chunk.blockTypeForPosition(x, y, z - 1) == 0) {
                    chunk.setBlockTypeForPosition(x, y, z, invItem.getItemID(), (byte) 0);
                    chunk.setBlockTypeForPosition(x, y, z - 1, invItem.getItemID(), (byte) 8);
                    if (GameMode.isSurvivalMode()) {
                        player.inventory.decItem(invItem);
                    }
                    return true;
                }
                return false;
            case West:
                if (chunk.blockTypeForPosition(x + 1, y, z) == 0) {
                    chunk.setBlockTypeForPosition(x, y, z, invItem.getItemID(), (byte) 1);
                    chunk.setBlockTypeForPosition(x + 1, y, z, invItem.getItemID(), (byte) 9);
                    if (GameMode.isSurvivalMode()) {
                        player.inventory.decItem(invItem);
                    }
                    return true;
                }
                return false;
            case East:
                if (chunk.blockTypeForPosition(x - 1, y, z) == 0) {
                    chunk.setBlockTypeForPosition(x, y, z, invItem.getItemID(), (byte) 3);
                    chunk.setBlockTypeForPosition(x - 1, y, z, invItem.getItemID(), (byte) 11);
                    if (GameMode.isSurvivalMode()) {
                        player.inventory.decItem(invItem);
                    }
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private static void addFaces(Chunklet c, int x, int y, int z, int colour, ShapeBuilder opaque, Parallelepiped p, boolean isPillow) {
        Vector3f blockSize = BED_SIZE;
        float faceX = x;
        float faceY = y;
        float faceZ = z;
        boolean[] invCoords = {false, false, true, false, false, false};
        if (!isPillow) {
            addFace(p, faceX, faceY, faceZ, p.south, blockSize.z, blockSize.y, colour, opaque, invCoords[0]);
        }
        if (isPillow) {
            addFace(p, faceX, faceY, faceZ, p.north, blockSize.z, blockSize.y, colour, opaque, invCoords[1]);
        }
        addFace(p, faceX, faceY, faceZ, p.west, blockSize.x, blockSize.y, colour, opaque, invCoords[2]);
        addFace(p, faceX, faceY, faceZ, p.east, blockSize.x, blockSize.y, colour, opaque, invCoords[3]);
        addFace(p, faceX, faceY, faceZ, p.bottom, blockSize.x, blockSize.z, colour, opaque, invCoords[4]);
        addFace(p, faceX, faceY, faceZ, p.top, blockSize.x, blockSize.z, colour, opaque, invCoords[5]);
    }

    private static ColouredShape getPreviewShape() {
        return bedPreviewShape;
    }

    private static void addFace(@NonNull Parallelepiped facing, float x, float y, float z, Parallelepiped.Face f, float width, float height, int colour, ShapeBuilder shapeBuilder, boolean invertCoords) {
        facing.face(f, x, y, z, width, height, colour, shapeBuilder, invertCoords);
    }
}

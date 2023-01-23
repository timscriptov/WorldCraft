package com.solverlabs.worldcraft.chunk;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.CompiledShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.WireUtil;
import com.solverlabs.droid.rugl.gl.Renderer;
import com.solverlabs.droid.rugl.gl.VBOShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.Frustum;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.factories.BlockFactory;

/**
 * A 16 * 16 * 16 cube of a {@link Chunk}
 */
public class Chunklet {
    /**
     * Parent chunk
     */
    public final Chunk parent;
    /**
     * World coordinate
     */
    public final int x;
    /**
     * World coordinate
     */
    public final int y;
    /**
     * World coordinate
     */
    public final int z;
    public boolean geomDirty = true;
    /**
     * <code>true</code> if the north side of this chunklet is completely opaque
     */
    public boolean northSheet = true;
    /**
     * <code>true</code> if the bottom side of this chunklet is completely opaque
     */
    public boolean bottomSheet = true;
    /**
     * <code>true</code> if the east side of this chunklet is completely opaque
     */
    public boolean eastSheet = true;
    /**
     * <code>true</code> if the south side of this chunklet is completely opaque
     */
    public boolean southSheet = true;
    /**
     * <code>true</code> if the top side of this chunklet is completely opaque
     */
    public boolean topSheet = true;
    /**
     * <code>true</code> if the west side of this chunklet is completely opaque
     */
    public boolean westSheet = true;
    /**
     * Stops us revisiting this chunklet when we flood-fill the view frustum to
     * find which chunklets to render
     */
    public int drawFlag = 0;
    /**
     * <code>true</code> if we're waiting on being processed by the
     * geometry-generating thread
     */
    boolean geomPending = false;
    /**
     * This is where we hold a new solid geometry vbo, fresh from the generation
     * thread
     */
    private VBOShape pendingSolid;
    /**
     * This is where we hold a new transparent geometry vbo, fresh from the
     * generation thread
     */
    private VBOShape pendingTransparent;
    /**
     * Solid geometry for GL1.0 users
     */
    private CompiledShape solidVA;
    /**
     * Solid geometry
     */
    private VBOShape solidVBO;
    private boolean solidVBOInvalidated;
    /**
     * Transparent geometry for GL1.0 users
     */
    private CompiledShape transparentVA;
    /**
     * Transparent geometry
     */
    private VBOShape transparentVBO;
    private boolean transparentVBOInvalidated;
    private ColouredShape outline = null;
    private boolean empty = true;
    private boolean allSheetsNotEmpty = false;
    private boolean boundariesEmptyChecked = false;

    /**
     * @param parent
     * @param y      in chunk index coordinates
     */
    public Chunklet(@NonNull Chunk parent, int y) {
        this.parent = parent;
        x = parent.chunkX * 16;
        this.y = y * 16;
        z = parent.chunkZ * 16;
    }

    void findSheets() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                byte bt = blockType(0, i, j);
                northSheet &= BlockFactory.opaque(bt);

                bt = blockType(15, i, j);
                southSheet &= BlockFactory.opaque(bt);

                bt = blockType(i, j, 0);
                eastSheet &= BlockFactory.opaque(bt);

                bt = blockType(i, j, 15);
                westSheet &= BlockFactory.opaque(bt);

                bt = blockType(i, 15, j);
                topSheet &= BlockFactory.opaque(bt);

                bt = blockType(i, 0, j);
                bottomSheet &= BlockFactory.opaque(bt);
            }
        }
        allSheetsNotEmpty = northSheet && southSheet && eastSheet && westSheet && topSheet && bottomSheet;
        empty = !northSheet && !southSheet && !eastSheet && !westSheet && !topSheet && !bottomSheet;
        for (int x = 0; x < 16 && empty; x++) {
            for (int z = 0; z < 16 && empty; z++) {
                for (int k = 0; k < 16 && empty; k++) {
                    empty = (blockType(x, k, z) == 0) & empty;
                }
            }
        }
    }

    /**
     * @return <code>true</code> if there is no solid geometry in this chunklet
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return The distance from the center of this chunklet to the point
     */
    public float distanceSq(float x, float y, float z) {
        float dx = (this.x + 8.0f) - x;
        float dy = (this.y + 8.0f) - y;
        float dz = (this.z + 8.0f) - z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    /**
     * Call this to refresh the chunklet's geometry the next time it is rendered
     */
    public void geomDirty() {
        findSheets();
        geomDirty = true;
        boundariesEmptyChecked = false;
    }

    /**
     * Draws the solid geometry
     *
     * @param r Renderer to use if we are in gl1.0
     */
    public void drawSolid(Renderer r) {
        if (!allSheetsNotEmpty) {
            if (solidVBOInvalidated) {
                if (solidVBO != null) {
                    solidVBO.delete();
                }
                solidVBO = pendingSolid;
                pendingSolid = null;
                solidVBOInvalidated = false;
            }
            if (solidVBO != null) {
                solidVBO.draw();
            }
            if (solidVA != null) {
                solidVA.render(r);
            }
        }
    }

    /**
     * Draws the transparent geometry
     *
     * @param r Renderer to use if we are in gl1.0
     */
    public void drawTransparent(Renderer r) {
        if (!allSheetsNotEmpty) {
            if (transparentVBOInvalidated) {
                if (transparentVBO != null) {
                    transparentVBO.delete();
                }
                transparentVBO = pendingTransparent;
                pendingTransparent = null;
                transparentVBOInvalidated = false;
            }
            if (transparentVBO != null) {
                transparentVBO.draw();
            }
            if (transparentVA != null) {
                transparentVA.render(r);
            }
        }
    }

    /**
     * @param synchronous <code>true</code> to generate right now, before doing anything
     *                    else, <code>false</code> to do it in another thread
     */
    public void generateGeometry(boolean synchronous) {
        if (empty && !boundariesEmptyChecked) {
            // need to check the sides of neighbouring blocks too
            for (int i = 0; i < 16 && empty; i++) {
                for (int j = 0; j < 16 && empty; j++) {
                    empty &= (blockType(-1, i, j) == 0);
                    empty &= (blockType(16, i, j) == 0);

                    empty &= (blockType(i, -1, j) == 0);
                    empty &= (blockType(i, 16, j) == 0);

                    empty &= (blockType(i, j, -1) == 0);
                    empty &= (blockType(i, j, 16) == 0);
                }
            }
            boundariesEmptyChecked = true;
        }
        if (!empty && geomDirty && !geomPending && !allSheetsNotEmpty) {
            geomPending = true;
            GeometryGenerator.generate(this, synchronous);
        }
    }

    public void changeSunLight() {
        if (solidVBO != null || transparentVBO != null) {
            if (solidVBO != null) {
                solidVBO.getDataBuffer().position(0);
            }
            if (transparentVBO != null) {
                transparentVBO.getDataBuffer().position(0);
            }
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockFactory.Block b = BlockFactory.getBlock(blockType(x, y, z));
                        if (b == null || !b.opaque) {
                            float colour = light(x, y, z);
                            setColourForFace(x - 1, y, z, b, colour);
                            setColourForFace(x + 1, y, z, b, colour);
                            setColourForFace(x, y, z - 1, b, colour);
                            setColourForFace(x, y, z + 1, b, colour);
                            setColourForFace(x, y + 1, z, b, colour);
                            setColourForFace(x, y - 1, z, b, colour);
                        }
                    }
                }
            }
            if (solidVBO != null) {
                solidVBO.getDataBuffer().position(0);
                solidVBO.delete();
            }
            if (transparentVBO != null) {
                transparentVBO.getDataBuffer().position(0);
                transparentVBO.delete();
            }
        }
    }

    private void setColourForFace(int x, int y, int z, BlockFactory.Block facing, float light) {
        BlockFactory.Block b = BlockFactory.getBlock(blockType(x, y, z));
        if (b != null && b != facing) {
            try {
                if (b.opaque) {
                    if (solidVBO != null) {
                        synchronized (solidVBO) {
                            solidVBO.setColour(light);
                        }
                    }
                } else if (transparentVBO != null) {
                    synchronized (transparentVBO) {
                        transparentVBO.setColour(light);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param solid
     * @param transparent
     */
    public void geometryComplete(VBOShape solid, VBOShape transparent) {
        geomPending = false;
        geomDirty = false;
        pendingSolid = solid;
        pendingTransparent = transparent;
        transparentVBOInvalidated = true;
        solidVBOInvalidated = true;
    }

    /**
     * @param solid
     * @param transparent
     */
    public void geometryComplete(CompiledShape solid, CompiledShape transparent) {
        geomPending = false;
        geomDirty = false;
        solidVA = solid;
        transparentVA = transparent;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return the so-indexed block
     */
    public byte blockType(int x, int y, int z) {
        return parent.blockType(x, this.y + y, z);
    }

    public byte blockData(int x, int y, int z) {
        return parent.blockData(x, this.y + y, z);
    }

    public void setBlockType(int x, int y, int z, byte type, byte data) {
        parent.setBlockTypeWithoutGeometryRecalculate(x, this.y + y, z, type, data);
    }

    public Chunk blockChunk(int x, int y, int z) {
        return parent.blockChunk(x, this.y + y, z);
    }

    public byte blockType(@NonNull Vector3i position) {
        return blockType(position.x, position.y, position.z);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return The light value of the so-indexed block
     */
    public float light(int x, int y, int z) {
        int sl = parent.skyLight(x, this.y + y, z);
        int sl2 = sl - (15 - parent.world.getSunlight());
        int bl = parent.blockLight(x, this.y + y, z);
        int l = Math.max(sl2, bl);
        if (l < 4) {
            l = 4;
        }
        return (float) Math.pow(0.8d, 15 - l);
    }

    /**
     * @param frustum
     * @return The intersection status of the chunk and frustum
     */
    public Frustum.Result intersection(@NonNull Frustum frustum) {
        return frustum.cuboidIntersects(x, y, z, x + 16, y + 16, z + 16);
    }

    @NonNull
    public String toString() {
        return "Chunklet @ " + x + ", " + y + ", " + z;
    }

    /**
     * Draws wireframe outline
     *
     * @param r
     */
    public void drawOutline(Renderer r) {
        if (solidVBO != null || transparentVBO != null || geomPending) {
            if (outline == null) {
                Shape s = WireUtil.unitCube();
                s.scale(15.5f, 15.5f, 15.5f);
                s.translate(0.25f, 0.25f, 0.25f);
                s.translate(x, y, z);
                outline = new ColouredShape(s, Colour.black, WireUtil.state);
            }
            outline.render(r);
        }
    }

    /**
     * Deletes VBOs
     */
    public void unload() {
        if (solidVBO != null) {
            solidVBO.delete();
            solidVBO = null;
        }
        if (transparentVBO != null) {
            transparentVBO.delete();
            transparentVBO = null;
        }
    }

    public int hashCode() {
        int hash = x + 17;
        return (((hash * 31) + y) * 13) + z;
    }

    public boolean equals(Object o) {
        if (o instanceof Chunklet) {
            Chunklet c = (Chunklet) o;
            return c.x == x && c.y == y && c.z == z;
        }
        return false;
    }
}

package com.solverlabs.worldcraft.chunk;

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


public class Chunklet {
    public final Chunk parent;
    public final int x;
    public final int y;
    public final int z;
    public boolean geomDirty = true;
    public boolean northSheet = true;
    public boolean bottomSheet = true;
    public boolean eastSheet = true;
    public boolean southSheet = true;
    public boolean topSheet = true;
    public boolean westSheet = true;
    public int drawFlag = 0;
    boolean geomPending = false;
    private VBOShape pendingSolid;
    private VBOShape pendingTransparent;
    private CompiledShape solidVA;
    private VBOShape solidVBO;
    private boolean solidVBOInvalidated;
    private CompiledShape transparentVA;
    private VBOShape transparentVBO;
    private boolean transparentVBOInvalidated;
    private ColouredShape outline = null;
    private boolean empty = true;
    private boolean allSheetsNotEmpty = false;
    private boolean boundariesEmptyChecked = false;

    public Chunklet(Chunk parent, int y) {
        this.parent = parent;
        this.x = parent.chunkX * 16;
        this.y = y * 16;
        this.z = parent.chunkZ * 16;
    }

    void findSheets() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                byte bt = blockType(0, i, j);
                this.northSheet &= BlockFactory.opaque(bt);
                byte bt2 = blockType(15, i, j);
                this.southSheet &= BlockFactory.opaque(bt2);
                byte bt3 = blockType(i, j, 0);
                this.eastSheet &= BlockFactory.opaque(bt3);
                byte bt4 = blockType(i, j, 15);
                this.westSheet &= BlockFactory.opaque(bt4);
                byte bt5 = blockType(i, 15, j);
                this.topSheet &= BlockFactory.opaque(bt5);
                byte bt6 = blockType(i, 0, j);
                this.bottomSheet &= BlockFactory.opaque(bt6);
            }
        }
        this.allSheetsNotEmpty = this.northSheet && this.southSheet && this.eastSheet && this.westSheet && this.topSheet && this.bottomSheet;
        this.empty = !this.northSheet && !this.southSheet && !this.eastSheet && !this.westSheet && !this.topSheet && !this.bottomSheet;
        for (int x = 0; x < 16 && this.empty; x++) {
            for (int z = 0; z < 16 && this.empty; z++) {
                for (int k = 0; k < 16 && this.empty; k++) {
                    this.empty = (blockType(x, k, z) == 0) & this.empty;
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public float distanceSq(float x, float y, float z) {
        float dx = (this.x + 8.0f) - x;
        float dy = (this.y + 8.0f) - y;
        float dz = (this.z + 8.0f) - z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public void geomDirty() {
        findSheets();
        this.geomDirty = true;
        this.boundariesEmptyChecked = false;
    }

    public void drawSolid(Renderer r) {
        if (!this.allSheetsNotEmpty) {
            if (this.solidVBOInvalidated) {
                if (this.solidVBO != null) {
                    this.solidVBO.delete();
                }
                this.solidVBO = this.pendingSolid;
                this.pendingSolid = null;
                this.solidVBOInvalidated = false;
            }
            if (this.solidVBO != null) {
                this.solidVBO.draw();
            }
            if (this.solidVA != null) {
                this.solidVA.render(r);
            }
        }
    }

    public void drawTransparent(Renderer r) {
        if (!this.allSheetsNotEmpty) {
            if (this.transparentVBOInvalidated) {
                if (this.transparentVBO != null) {
                    this.transparentVBO.delete();
                }
                this.transparentVBO = this.pendingTransparent;
                this.pendingTransparent = null;
                this.transparentVBOInvalidated = false;
            }
            if (this.transparentVBO != null) {
                this.transparentVBO.draw();
            }
            if (this.transparentVA != null) {
                this.transparentVA.render(r);
            }
        }
    }

    public void generateGeometry(boolean synchronous) {
        if (this.empty && !this.boundariesEmptyChecked) {
            for (int i = 0; i < 16 && this.empty; i++) {
                for (int j = 0; j < 16 && this.empty; j++) {
                    this.empty = (blockType(-1, i, j) == 0) & this.empty;
                    this.empty = (blockType(16, i, j) == 0) & this.empty;
                    this.empty = (blockType(i, -1, j) == 0) & this.empty;
                    this.empty = (blockType(i, 16, j) == 0) & this.empty;
                    this.empty = (blockType(i, j, -1) == 0) & this.empty;
                    this.empty = (blockType(i, j, 16) == 0) & this.empty;
                }
            }
            this.boundariesEmptyChecked = true;
        }
        if (!this.empty && this.geomDirty && !this.geomPending && !this.allSheetsNotEmpty) {
            this.geomPending = true;
            GeometryGenerator.generate(this, synchronous);
        }
    }

    public void changeSunLight() {
        if (this.solidVBO != null || this.transparentVBO != null) {
            if (this.solidVBO != null) {
                this.solidVBO.getDataBuffer().position(0);
            }
            if (this.transparentVBO != null) {
                this.transparentVBO.getDataBuffer().position(0);
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
            if (this.solidVBO != null) {
                this.solidVBO.getDataBuffer().position(0);
                this.solidVBO.delete();
            }
            if (this.transparentVBO != null) {
                this.transparentVBO.getDataBuffer().position(0);
                this.transparentVBO.delete();
            }
        }
    }

    private void setColourForFace(int x, int y, int z, BlockFactory.Block facing, float light) {
        BlockFactory.Block b = BlockFactory.getBlock(blockType(x, y, z));
        if (b != null && b != facing) {
            try {
                if (b.opaque) {
                    if (this.solidVBO != null) {
                        synchronized (this.solidVBO) {
                            this.solidVBO.setColour(light);
                        }
                    }
                } else if (this.transparentVBO != null) {
                    synchronized (this.transparentVBO) {
                        this.transparentVBO.setColour(light);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void geometryComplete(VBOShape solid, VBOShape transparent) {
        this.geomPending = false;
        this.geomDirty = false;
        this.pendingSolid = solid;
        this.pendingTransparent = transparent;
        this.transparentVBOInvalidated = true;
        this.solidVBOInvalidated = true;
    }

    public void geometryComplete(CompiledShape solid, CompiledShape transparent) {
        this.geomPending = false;
        this.geomDirty = false;
        this.solidVA = solid;
        this.transparentVA = transparent;
    }

    public byte blockType(int x, int y, int z) {
        return this.parent.blockType(x, this.y + y, z);
    }

    public byte blockData(int x, int y, int z) {
        return this.parent.blockData(x, this.y + y, z);
    }

    public void setBlockType(int x, int y, int z, byte type, byte data) {
        this.parent.setBlockTypeWithoutGeometryRecalculate(x, this.y + y, z, type, data);
    }

    public Chunk blockChunk(int x, int y, int z) {
        return this.parent.blockChunk(x, this.y + y, z);
    }

    public byte blockType(Vector3i position) {
        return blockType(position.x, position.y, position.z);
    }

    public float light(int x, int y, int z) {
        int sl = this.parent.skyLight(x, this.y + y, z);
        int sl2 = sl - (15 - this.parent.world.getSunlight());
        int bl = this.parent.blockLight(x, this.y + y, z);
        int l = Math.max(sl2, bl);
        if (l < 4) {
            l = 4;
        }
        return (float) Math.pow(0.8d, 15 - l);
    }

    public Frustum.Result intersection(Frustum frustum) {
        return frustum.cuboidIntersects(this.x, this.y, this.z, this.x + 16, this.y + 16, this.z + 16);
    }

    public String toString() {
        return "Chunklet @ " + this.x + ", " + this.y + ", " + this.z;
    }

    public void drawOutline(Renderer r) {
        if (this.solidVBO != null || this.transparentVBO != null || this.geomPending) {
            if (this.outline == null) {
                Shape s = WireUtil.unitCube();
                s.scale(15.5f, 15.5f, 15.5f);
                s.translate(0.25f, 0.25f, 0.25f);
                s.translate(this.x, this.y, this.z);
                this.outline = new ColouredShape(s, Colour.black, WireUtil.state);
            }
            this.outline.render(r);
        }
    }

    public void unload() {
        if (this.solidVBO != null) {
            this.solidVBO.delete();
            this.solidVBO = null;
        }
        if (this.transparentVBO != null) {
            this.transparentVBO.delete();
            this.transparentVBO = null;
        }
    }

    public int hashCode() {
        int hash = this.x + 17;
        return (((hash * 31) + this.y) * 13) + this.z;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Chunklet)) {
            return false;
        }
        Chunklet c = (Chunklet) o;
        return c.x == this.x && c.y == this.y && c.z == this.z;
    }
}

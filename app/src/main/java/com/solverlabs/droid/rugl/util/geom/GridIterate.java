package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;

public class GridIterate {
    private final Vector3f start = new Vector3f();
    private final Vector3f end = new Vector3f();
    private final Vector3f p = new Vector3f();
    private final Vector3f q = new Vector3f();
    private final BoundingCuboid segBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private final BoundingCuboid gridBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    public Vector3i lastGridCoords = new Vector3i();
    public Move lastGridExit = Move.COMPLETE;
    private Move xDir;
    private Move yDir;
    private Move zDir;
    private boolean done = false;

    private static Move clip(Vector3f p, Vector3f q, int x, int y, int z) {
        clipPoint(p, q, x, y, z);
        return clipPoint(q, p, x, y, z);
    }

    private static Move clipPoint(@NonNull Vector3f p, Vector3f q, int x, int y, int z) {
        Move m = Move.COMPLETE;
        if (p.x < x) {
            float d = (x - p.x) / (q.x - p.x);
            p.y += (q.y - p.y) * d;
            p.z += (q.z - p.z) * d;
            p.x = x;
            m = Move.X_LOW;
        }
        if (p.x > x + 1) {
            float d2 = ((x + 1) - p.x) / (q.x - p.x);
            p.y += (q.y - p.y) * d2;
            p.z += (q.z - p.z) * d2;
            p.x = x + 1;
            m = Move.X_HIGH;
        }
        if (p.y < y) {
            float d3 = (y - p.y) / (q.y - p.y);
            p.x += (q.x - p.x) * d3;
            p.z += (q.z - p.z) * d3;
            p.y = y;
            m = Move.Y_LOW;
        }
        if (p.y > y + 1) {
            float d4 = ((y + 1) - p.y) / (q.y - p.y);
            p.x += (q.x - p.x) * d4;
            p.z += (q.z - p.z) * d4;
            p.y = y + 1;
            m = Move.Y_HIGH;
        }
        if (p.z < z) {
            float d5 = (z - p.z) / (q.z - p.z);
            p.x += (q.x - p.x) * d5;
            p.y += (q.y - p.y) * d5;
            p.z = z;
            m = Move.Z_LOW;
        }
        if (p.z > z + 1) {
            float d6 = ((z + 1) - p.z) / (q.z - p.z);
            p.x += (q.x - p.x) * d6;
            p.y += (q.y - p.y) * d6;
            p.z = z + 1;
            return Move.Z_HIGH;
        }
        return m;
    }

    public void setSeg(float startx, float starty, float startz, float endx, float endy, float endz) {
        this.start.set(startx, starty, startz);
        this.end.set(endx, endy, endz);
        this.lastGridCoords.set((int) Math.floor(this.start.x), (int) Math.floor(this.start.y), (int) Math.floor(this.start.z));
        this.xDir = startx < endx ? Move.X_HIGH : Move.X_LOW;
        this.yDir = starty < endy ? Move.Y_HIGH : Move.Y_LOW;
        this.zDir = startz < endz ? Move.Z_HIGH : Move.Z_LOW;
        this.segBounds.x.set(startx, endx);
        this.segBounds.y.set(starty, endy);
        this.segBounds.z.set(startz, endz);
        this.lastGridExit = null;
        this.done = false;
    }

    public boolean isDone() {
        return this.done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void next() {
        this.gridBounds.x.set(this.lastGridCoords.x, this.lastGridCoords.x + 1);
        this.gridBounds.y.set(this.lastGridCoords.y, this.lastGridCoords.y + 1);
        this.gridBounds.z.set(this.lastGridCoords.z, this.lastGridCoords.z + 1);
        this.p.set(this.start);
        this.q.set(this.end);
        this.lastGridExit = clip(this.p, this.q, this.lastGridCoords.x, this.lastGridCoords.y, this.lastGridCoords.z);
        if (this.lastGridExit.x != 0 && this.lastGridExit.x != this.xDir.x) {
            this.lastGridExit = this.yDir;
        }
        if (this.lastGridExit.y != 0 && this.lastGridExit.y != this.yDir.y) {
            this.lastGridExit = this.zDir;
        }
        if (this.lastGridExit.z != 0 && this.lastGridExit.z != this.zDir.z) {
            this.lastGridExit = this.xDir;
        }
        if (this.lastGridExit == Move.COMPLETE || !this.segBounds.intersects(this.gridBounds)) {
            this.done = true;
            return;
        }
        this.lastGridCoords.x += this.lastGridExit.x;
        this.lastGridCoords.y += this.lastGridExit.y;
        this.lastGridCoords.z += this.lastGridExit.z;
    }

    public enum Move {
        X_LOW(-1, 0, 0),
        X_HIGH(1, 0, 0),
        Y_LOW(0, -1, 0),
        Y_HIGH(0, 1, 0),
        Z_LOW(0, 0, -1),
        Z_HIGH(0, 0, 1),
        COMPLETE(0, 0, 0);

        private final int x;
        private final int y;
        private final int z;

        Move(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}

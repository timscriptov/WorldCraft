package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;

/**
 * Allows us to iterate over a unit-grid over a line segment
 */
public class GridIterate {
    private final Vector3f start = new Vector3f(), end = new Vector3f(),
            p = new Vector3f(), q = new Vector3f();
    private final BoundingCuboid segBounds = new BoundingCuboid(0, 0, 0, 0, 0,
            0);
    private final BoundingCuboid gridBounds = new BoundingCuboid(0, 0, 0, 0, 0,
            0);
    /**
     * The coordinates of the last grid square
     */
    public Vector3i lastGridCoords = new Vector3i();
    /**
     * The direction of the last move
     */
    public Move lastGridExit = null;
    private Move xDir, yDir, zDir;
    private boolean done = false;

    private static Move clip(final Vector3f p, final Vector3f q, final int x,
                             final int y, final int z) {
        clipPoint(p, q, x, y, z);
        return clipPoint(q, p, x, y, z);
    }

    private static Move clipPoint(@NonNull final Vector3f p, final Vector3f q,
                                  final int x, final int y, final int z) {
        Move m = Move.COMPLETE;

        if (p.x < x) {
            final float d = (x - p.x) / (q.x - p.x);
            p.y += (q.y - p.y) * d;
            p.z += (q.z - p.z) * d;
            p.x = x;

            m = Move.X_LOW;
        }

        if (p.x > x + 1) {
            final float d = (x + 1 - p.x) / (q.x - p.x);
            p.y += (q.y - p.y) * d;
            p.z += (q.z - p.z) * d;
            p.x = x + 1;

            m = Move.X_HIGH;
        }

        if (p.y < y) {
            final float d = (y - p.y) / (q.y - p.y);
            p.x += (q.x - p.x) * d;
            p.z += (q.z - p.z) * d;
            p.y = y;

            m = Move.Y_LOW;
        }

        if (p.y > y + 1) {
            final float d = (y + 1 - p.y) / (q.y - p.y);
            p.x += (q.x - p.x) * d;
            p.z += (q.z - p.z) * d;
            p.y = y + 1;

            m = Move.Y_HIGH;
        }

        if (p.z < z) {
            final float d = (z - p.z) / (q.z - p.z);
            p.x += (q.x - p.x) * d;
            p.y += (q.y - p.y) * d;
            p.z = z;

            m = Move.Z_LOW;
        }

        if (p.z > z + 1) {
            final float d = (z + 1 - p.z) / (q.z - p.z);
            p.x += (q.x - p.x) * d;
            p.y += (q.y - p.y) * d;
            p.z = z + 1;

            m = Move.Z_HIGH;
        }

        return m;
    }

    /**
     * @param startx
     * @param starty
     * @param startz
     * @param endx
     * @param endy
     * @param endz
     */
    public void setSeg(final float startx, final float starty,
                       final float startz, final float endx, final float endy,
                       final float endz) {
        start.set(startx, starty, startz);
        end.set(endx, endy, endz);

        lastGridCoords.set((int) Math.floor(start.x),
                (int) Math.floor(start.y), (int) Math.floor(start.z));

        xDir = startx < endx ? Move.X_HIGH : Move.X_LOW;
        yDir = starty < endy ? Move.Y_HIGH : Move.Y_LOW;
        zDir = startz < endz ? Move.Z_HIGH : Move.Z_LOW;

        segBounds.x.set(startx, endx);
        segBounds.y.set(starty, endy);
        segBounds.z.set(startz, endz);

        lastGridExit = null;
        done = false;
    }

    /**
     * We are done with the line segment
     *
     * @return <code>true</code> if the end has been reached
     */
    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    /**
     * Advances to the next grid square
     */
    public void next() {
        gridBounds.x.set(lastGridCoords.x, lastGridCoords.x + 1);
        gridBounds.y.set(lastGridCoords.y, lastGridCoords.y + 1);
        gridBounds.z.set(lastGridCoords.z, lastGridCoords.z + 1);

        p.set(start);
        q.set(end);

        // clip to grid
        lastGridExit =
                clip(p, q, lastGridCoords.x, lastGridCoords.y, lastGridCoords.z);

        // watch for corner-case confusion
        if (lastGridExit.x != 0 && lastGridExit.x != xDir.x) {
            lastGridExit = yDir;
        }
        if (lastGridExit.y != 0 && lastGridExit.y != yDir.y) {
            lastGridExit = zDir;
        }
        if (lastGridExit.z != 0 && lastGridExit.z != zDir.z) {
            lastGridExit = xDir;
        }

        if (lastGridExit == Move.COMPLETE || !segBounds.intersects(gridBounds)) {
            done = true;
        } else {
            lastGridCoords.x += lastGridExit.x;
            lastGridCoords.y += lastGridExit.y;
            lastGridCoords.z += lastGridExit.z;
        }
    }

    public enum Move {
        /***/
        X_LOW(-1, 0, 0),
        /***/
        X_HIGH(1, 0, 0),
        /***/
        Y_LOW(0, -1, 0),
        /***/
        Y_HIGH(0, 1, 0),
        /***/
        Z_LOW(0, 0, -1),
        /***/
        Z_HIGH(0, 0, 1),
        /***/
        COMPLETE(0, 0, 0);

        private final int x;

        private final int y;

        private final int z;

        private Move(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}

package com.solverlabs.droid.rugl.geom.line;

import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.util.geom.LineUtils;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.VectorUtils;

import java.util.LinkedList;
import java.util.List;


public class Line {
    static final /* synthetic */ boolean $assertionsDisabled;

    static {
        $assertionsDisabled = !Line.class.desiredAssertionStatus();
    }

    private final LinkedList<Vector2f> points = new LinkedList<>();
    public float width = 1.0f;
    public LineCap cap = null;
    public LineJoin join = null;
    private Vector2f lastAdded = null;
    private Vector2f lastButOneAdded = null;

    static void addQuad(List<Short> indices, short lastLeft, short lastRight, short nextLeft, short nextRight) {
        if ($assertionsDisabled || lastLeft != lastRight) {
            if (!$assertionsDisabled && lastLeft == nextLeft) {
                throw new AssertionError();
            }
            if (!$assertionsDisabled && lastLeft == nextRight) {
                throw new AssertionError();
            }
            if (!$assertionsDisabled && lastRight == nextLeft) {
                throw new AssertionError();
            }
            if (!$assertionsDisabled && lastRight == nextRight) {
                throw new AssertionError();
            }
            if (!$assertionsDisabled && nextLeft == nextRight) {
                throw new AssertionError();
            }
            Short pl = new Short(lastLeft);
            Short pr = new Short(lastRight);
            Short nl = new Short(nextLeft);
            Short nr = new Short(nextRight);
            indices.add(pl);
            indices.add(nl);
            indices.add(pr);
            indices.add(nl);
            indices.add(nr);
            indices.add(pr);
            return;
        }
        throw new AssertionError();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void addTriangle(Short i1, Short i2, Short i3, int ccw, List<Short> indices) {
        if (ccw == 1) {
            indices.add(i1);
            indices.add(i2);
            indices.add(i3);
            return;
        }
        indices.add(i1);
        indices.add(i3);
        indices.add(i2);
    }

    public void addPoint(Vector2f p) {
        if (this.lastAdded == null || this.lastAdded.x != p.x || this.lastAdded.y != p.y) {
            if (this.lastAdded == null || this.lastButOneAdded == null) {
                this.points.add(p);
                this.lastButOneAdded = this.lastAdded;
                this.lastAdded = p;
            } else if (p.x != this.lastAdded.x || p.y != this.lastAdded.y) {
                if (!$assertionsDisabled && this.points.size() < 2) {
                    throw new AssertionError(this.points.size());
                }
                int ccw = LineUtils.relativeCCW(this.lastButOneAdded, this.lastAdded, p);
                if (ccw == 0) {
                    this.points.removeLast();
                    this.points.add(p);
                    this.lastAdded = p;
                    return;
                }
                this.points.add(p);
                this.lastButOneAdded = this.lastAdded;
                this.lastAdded = p;
            }
        }
    }

    public int pointCount() {
        return this.points.size();
    }

    public void clear() {
        this.points.clear();
        this.lastAdded = null;
        this.lastButOneAdded = null;
    }

    public Shape buildLine(float z) {
        Shape s = null;
        if (this.points.size() >= 2) {
            List<Vector2f> v = new LinkedList<>();
            List<Short> i = new LinkedList<>();
            Vector2f current = this.points.removeFirst();
            Vector2f next = this.points.removeFirst();
            start(current, next, v, i);
            short lastLeft = 0;
            short lastRight = 1;
            while (!this.points.isEmpty()) {
                Vector2f last = current;
                current = next;
                next = this.points.removeFirst();
                short lastIndex = (short) (v.size() - 1);
                int turn = corner(last, current, next, v, i);
                if (!$assertionsDisabled && turn == 0) {
                    throw new AssertionError();
                }
                if (turn == -1) {
                    short nextLeft = (short) (lastIndex + 2);
                    short nextRight = (short) (lastIndex + 1);
                    addQuad(i, lastRight, lastLeft, nextRight, nextLeft);
                    lastLeft = nextLeft;
                    lastRight = (short) (lastIndex + 3);
                } else if (turn == 1) {
                    short nextLeft2 = (short) (lastIndex + 1);
                    short nextRight2 = (short) (lastIndex + 2);
                    addQuad(i, lastRight, lastLeft, nextRight2, nextLeft2);
                    lastLeft = (short) (lastIndex + 3);
                    lastRight = nextRight2;
                } else if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            short lastIndex2 = (short) (v.size() - 1);
            end(current, next, v, i);
            short nextRight3 = (short) (lastIndex2 + 2);
            addQuad(i, lastRight, lastLeft, nextRight3, (short) (lastIndex2 + 1));
            if (!$assertionsDisabled && !this.points.isEmpty()) {
                throw new AssertionError();
            }
            s = new Shape(ShapeUtil.extractVerts(v, z), ShapeUtil.extractIndices(i));
        }
        clear();
        return s;
    }

    public Shape buildLoop(float z) {
        Shape s = null;
        if (this.points.size() == 2) {
            return buildLine(z);
        }
        if (this.points.size() >= 3) {
            Vector2f first = this.points.getFirst();
            Vector2f last = this.points.removeLast();
            Vector2f penultimate = this.points.getLast();
            if (LineUtils.relativeCCW(penultimate, last, first) != 0) {
                this.points.add(last);
            }
            if (this.points.size() == 2) {
                return buildLine(z);
            }
            List<Vector2f> v = new LinkedList<>();
            List<Short> i = new LinkedList<>();
            Vector2f first2 = this.points.removeFirst();
            Vector2f first3 = first2;
            Vector2f second = this.points.removeFirst();
            this.points.add(first3);
            this.points.add(second);
            Vector2f current = second;
            Vector2f next = this.points.removeFirst();
            int turn = corner(first3, current, next, v, i);
            short firstLeft = -1;
            short firstRight = -1;
            short lastLeft = -1;
            short lastRight = -1;
            if (turn == 1) {
                firstLeft = 1;
                firstRight = 0;
                lastLeft = 1;
                lastRight = 2;
            } else if (turn == -1) {
                firstLeft = 0;
                firstRight = 1;
                lastLeft = 2;
                lastRight = 1;
            } else if (!$assertionsDisabled) {
                throw new AssertionError();
            }
            while (!this.points.isEmpty()) {
                Vector2f prev = current;
                current = next;
                next = this.points.removeFirst();
                int lastIndex = v.size() - 1;
                int turn2 = corner(prev, current, next, v, i);
                if (turn2 == 1) {
                    short nextLeft = (short) (lastIndex + 2);
                    addQuad(i, lastLeft, lastRight, nextLeft, (short) (lastIndex + 1));
                    lastLeft = nextLeft;
                    lastRight = (short) (lastIndex + 3);
                } else if (turn2 == -1) {
                    short nextRight = (short) (lastIndex + 2);
                    addQuad(i, lastLeft, lastRight, (short) (lastIndex + 1), nextRight);
                    lastLeft = (short) (lastIndex + 3);
                    lastRight = nextRight;
                } else if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            if (!$assertionsDisabled && lastLeft == -1) {
                throw new AssertionError();
            }
            addQuad(i, lastLeft, lastRight, firstLeft, firstRight);
            s = new Shape(ShapeUtil.extractVerts(v, z), ShapeUtil.extractIndices(i));
        }
        clear();
        return s;
    }

    private void start(Vector2f first, Vector2f second, List<Vector2f> verts, List<Short> indices) {
        Vector2f dir = Vector2f.sub(second, first, null);
        dir.normalise();
        dir.scale(this.width / 2.0f);
        VectorUtils.rotate90(dir);
        verts.add(Vector2f.add(first, dir, null));
        verts.add(Vector2f.sub(first, dir, null));
        if (this.cap != null) {
            VectorUtils.rotateMinus90(dir);
            dir.normalise();
            this.cap.createVerts(first, dir, (short) (verts.size() - 2), (short) (verts.size() - 1), this.width, verts, indices);
        }
    }

    private void end(Vector2f penultimate, Vector2f last, List<Vector2f> verts, List<Short> indices) {
        Vector2f dir = Vector2f.sub(penultimate, last, null);
        if (dir.x == 0.0f && dir.y == 0.0f) {
            dir.set(1.0f, 0.0f);
        }
        dir.normalise();
        dir.scale(this.width / 2.0f);
        VectorUtils.rotate90(dir);
        verts.add(Vector2f.sub(last, dir, null));
        verts.add(Vector2f.add(last, dir, null));
        if (this.cap != null) {
            VectorUtils.rotateMinus90(dir);
            dir.normalise();
            this.cap.createVerts(last, dir, (short) (verts.size() - 1), (short) (verts.size() - 2), this.width, verts, indices);
        }
    }

    private int corner(Vector2f previous, Vector2f current, Vector2f next, List<Vector2f> verts, List<Short> indices) {
        int ccw = LineUtils.relativeCCW(previous, current, next);
        if (ccw != 0) {
            Vector2f pn = Vector2f.sub(current, previous, null);
            pn.normalise();
            pn.scale(this.width / 2.0f);
            float x = pn.x;
            pn.x = -pn.y;
            pn.y = x;
            Vector2f nn = Vector2f.sub(next, current, null);
            nn.normalise();
            nn.scale(this.width / 2.0f);
            float x2 = nn.x;
            nn.x = -nn.y;
            nn.y = x2;
            pn.scale(ccw);
            nn.scale(ccw);
            Vector2f pp1 = Vector2f.sub(previous, pn, null);
            Vector2f pp2 = Vector2f.sub(current, pn, null);
            Vector2f np1 = Vector2f.sub(current, nn, null);
            Vector2f np2 = Vector2f.sub(next, nn, null);
            Vector2f corner = LineUtils.lineIntersection(pp1, pp2, np1, np2, null);
            pn.scale(2.0f);
            nn.scale(2.0f);
            Vector2f pleft = Vector2f.add(corner, pn, null);
            Vector2f nleft = Vector2f.add(corner, nn, null);
            verts.add(pleft);
            verts.add(corner);
            verts.add(nleft);
            if (this.join != null) {
                this.join.createVerts(pleft, corner, nleft, current, verts, indices);
            }
        }
        return ccw;
    }

    public Shape buildSegmentShape(Vector2f start, Vector2f end, float z) {
        List<Vector2f> v = new LinkedList<>();
        List<Short> i = new LinkedList<>();
        start(start, end, v, i);
        int lastIndex = v.size() - 1;
        end(start, end, v, i);
        short nextLeft = (short) (lastIndex + 1);
        short nextRight = (short) (lastIndex + 2);
        addQuad(i, (short) 0, (short) 1, nextLeft, nextRight);
        return new Shape(ShapeUtil.extractVerts(v, z), ShapeUtil.extractIndices(i));
    }
}

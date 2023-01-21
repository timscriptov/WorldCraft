package com.solverlabs.droid.rugl.util;

import androidx.annotation.NonNull;

import java.util.List;

public class RectanglePacker<P> {
    private int border;
    private RectanglePacker<P>.Node root;

    public RectanglePacker(int width, int height, int border) {
        this.border = 0;
        this.root = new Node(new Rectangle(0, 0, width, height));
        this.border = border;
    }

    public void inspectRectangles(List<Rectangle> rectangles) {
        this.root.getRectangles(rectangles);
    }

    public Rectangle findRectangle(P item) {
        return this.root.findRectange(item);
    }

    public void clear() {
        this.root = new Node(this.root.rect);
    }

    public Rectangle insert(int width, int height, P o) {
        RectanglePacker<P>.Node n = this.root.insert((this.border * 2) + width, (this.border * 2) + height, o);
        if (n != null) {
            return new Rectangle(n.rect.x + this.border, n.rect.y + this.border, n.rect.width - (this.border * 2), n.rect.height - (this.border * 2));
        }
        return null;
    }

    public boolean remove(P o) {
        return this.root.remove(o);
    }

    public int getWidth() {
        return this.root.rect.width;
    }

    public int getHeight() {
        return this.root.rect.height;
    }

    public enum Fit {
        FAIL,
        PERFECT,
        FIT
    }

    public static class Rectangle {
        public final int height;
        public final int width;
        public final int x;
        public final int y;

        private Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private Rectangle(@NonNull Rectangle r) {
            this.x = r.x;
            this.y = r.y;
            this.width = r.width;
            this.height = r.height;
        }

        @NonNull
        public String toString() {
            return "[ " + this.x + ", " + this.y + ", " + this.width + ", " + this.height + " ]";
        }
    }

    public class Node {
        final boolean assertionsDisabled = !RectanglePacker.class.desiredAssertionStatus();
        private final Rectangle rect;
        private RectanglePacker<P>.Node left;
        private P occupier;
        private RectanglePacker<P>.Node right;

        private Node(Rectangle r) {
            this.occupier = null;
            this.left = null;
            this.right = null;
            this.rect = r;
        }

        public Rectangle findRectange(P item) {
            if (isLeaf()) {
                if (item == this.occupier) {
                    return this.rect;
                }
                return null;
            }
            Rectangle l = this.left.findRectange(item);
            return l == null ? this.right.findRectange(item) : l;
        }

        public RectanglePacker<P>.Node insert(int width, int height, P o) {
            if (!isLeaf()) {
                RectanglePacker<P>.Node r = this.left.insert(width, height, o);
                if (r == null) {
                    return this.right.insert(width, height, o);
                }
                return r;
            } else if (this.occupier == null) {
                Fit fit = fits(width, height);
                switch (fit) {
                    case FAIL:
                        return null;
                    case PERFECT:
                        this.occupier = o;
                        return this;
                    case FIT:
                        split(width, height);
                        break;
                }
                return this.left.insert(width, height, o);
            } else {
                return null;
            }
        }

        private boolean isLeaf() {
            return this.left == null;
        }

        private boolean isOccupied() {
            return this.occupier != null || !isLeaf();
        }

        public boolean remove(P o) {
            if (isLeaf()) {
                if (this.occupier == o) {
                    this.occupier = null;
                    return true;
                }
                return false;
            }
            boolean found = this.left.remove(o);
            if (!found) {
                found = this.right.remove(o);
            }
            if (found && !this.left.isOccupied() && !this.right.isOccupied()) {
                this.left = null;
                this.right = null;
                return found;
            }
            return found;
        }

        private void split(int width, int height) {
            Rectangle l;
            Rectangle r;
            int dw = this.rect.width - width;
            int dh = this.rect.height - height;
            if (!assertionsDisabled && dw < 0) {
                throw new AssertionError();
            }
            if (!assertionsDisabled && dh < 0) {
                throw new AssertionError();
            }
            if (dw > dh) {
                l = new Rectangle(this.rect.x, this.rect.y, width, this.rect.height);
                r = new Rectangle(l.x + width, this.rect.y, this.rect.width - width, this.rect.height);
            } else {
                l = new Rectangle(this.rect.x, this.rect.y, this.rect.width, height);
                r = new Rectangle(this.rect.x, l.y + height, this.rect.width, this.rect.height - height);
            }
            this.left = new Node(l);
            this.right = new Node(r);
        }

        private Fit fits(int width, int height) {
            if (width <= this.rect.width && height <= this.rect.height) {
                if (width == this.rect.width && height == this.rect.height) {
                    return Fit.PERFECT;
                }
                return Fit.FIT;
            }
            return Fit.FAIL;
        }

        public void getRectangles(@NonNull List<Rectangle> rectangles) {
            rectangles.add(this.rect);
            if (!isLeaf()) {
                this.left.getRectangles(rectangles);
                this.right.getRectangles(rectangles);
            }
        }
    }
}

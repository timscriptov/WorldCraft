package com.solverlabs.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.Renderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.Matrix4f;


public class ColouredShape extends Shape {
    public int[] colours;
    public State state;
    private float lastRotateAngle;

    public ColouredShape(Shape s, int colour, State state) {
        this(s, ShapeUtil.expand(colour, s.vertexCount()), state);
    }

    public ColouredShape(Shape s, int[] colours, State state) {
        super(s);
        this.lastRotateAngle = -400.0f;
        this.colours = colours;
        this.state = state == null ? GLUtil.typicalState : state;
        sanity();
    }

    public ColouredShape(ColouredShape cs) {
        super(cs);
        this.lastRotateAngle = -400.0f;
        this.colours = cs.colours;
        this.state = cs.state;
    }

    private void sanity() throws IllegalArgumentException {
        if (this.colours.length != vertexCount()) {
            throw new IllegalArgumentException("Colour count mismatch\n" + this);
        }
    }

    public void render(@NonNull Renderer renderer) {
        this.state = renderer.intern(this.state);
        renderer.addGeometry(this.vertices, null, this.colours, this.indices, this.state);
    }

    @Override
    public ColouredShape clone() {
        return new ColouredShape(super.clone(), this.colours.clone(), this.state);
    }

    @Override
    public int bytes() {
        return super.bytes() + (this.colours.length * 4);
    }

    @Override
    public ColouredShape transform(Matrix4f m) {
        super.transform(m);
        return this;
    }

    @Override
    public ColouredShape translate(float x, float y, float z) {
        super.translate(x, y, z);
        return this;
    }

    @Override
    public ColouredShape set(float x, float y, float z) {
        float setX = x - getBounds().x.getMin();
        float setY = y - getBounds().y.getMin();
        float setZ = z - getBounds().z.getMin();
        translate(setX, setY, setZ);
        return this;
    }

    @Override
    public ColouredShape scale(float x, float y, float z) {
        super.scale(x, y, z);
        return this;
    }

    public float getLastRotateAngle() {
        return this.lastRotateAngle;
    }

    @Override
    public ColouredShape rotate(float angle, float ax, float ay, float az) {
        super.rotate(angle, ax, ay, az);
        this.lastRotateAngle = angle;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder(super.toString());
        if (this.colours != null) {
            buff.append("\ncolours");
            for (int i = 0; i < this.colours.length; i++) {
                buff.append("\n\t");
                buff.append(Colour.toString(this.colours[i]));
            }
        }
        return buff.toString();
    }
}

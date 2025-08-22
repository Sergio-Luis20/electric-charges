package br.sergio.charges;

import java.io.Serial;
import java.io.Serializable;

public record Vector(double x, double y) implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Vector NULL = new Vector(0, 0);

    public static final Vector UNIT_V_I = new Vector(1, 0);
    public static final Vector UNIT_V_J = new Vector(0, 1);

    public Vector() {
        this(0, 0);
    }

    public Vector(Vector v) {
        this(v.x, v.y);
    }

    public Vector(Vector origin, Vector end) {
        this(end.x - origin.x, end.y - origin.y);
    }

    public Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y);
    }

    public Vector subtract(Vector v) {
        return new Vector(x - v.x, y - v.y);
    }

    public Vector multiplyByScalar(double scalar) {
        return new Vector(x * scalar, y * scalar);
    }

    public double dotProduct(Vector v) {
        return x * v.x + y * v.y;
    }

    public boolean isNull() {
        return x == 0 && y == 0;
    }

    public Vector unitVector() {
        return multiplyByScalar(1 / magnitude());
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquared() {
        return dotProduct(this);
    }

    public Vector newMagnitude(double magnitude) {
        return unitVector().multiplyByScalar(magnitude);
    }

    public double angle(Vector v) {
        return Math.acos(dotProduct(v) / (magnitude() * v.magnitude()));
    }

    public Vector x(double x) {
        return new Vector(x, y);
    }

    public Vector y(double y) {
        return new Vector(x, y);
    }

    public Vector getXComponent() {
        return new Vector(x, 0);
    }

    public Vector getYComponent() {
        return new Vector(0, y);
    }

    @Override
    public Vector clone() {
        return new Vector(this);
    }

    @Override
    public String toString() {
        return "[" + Utils.formatDouble(x, 3) + ", " + Utils.formatDouble(y, 3) + "]";
    }

}

package br.sergio.charges;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public record Vector(double x, double y, double z) implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Vector NULL = new Vector(0, 0, 0);

    public static final Vector UNIT_V_I = new Vector(1, 0, 0);
    public static final Vector UNIT_V_J = new Vector(0, 1, 0);
    public static final Vector UNIT_V_K = new Vector(0, 0, 1);

    public Vector() {
        this(0, 0, 0);
    }

    public Vector(Vector v) {
        this(v.x, v.y, v.z);
    }

    public Vector(Vector origin, Vector end) {
        this(end.x - origin.x, end.y - origin.y, end.z - origin.z);
    }

    public Vector(double x, double y) {
        this(x, y, 0);
    }

    public Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y, z + v.z);
    }

    public Vector subtract(Vector v) {
        return new Vector(x - v.x, y - v.y, z - v.z);
    }

    public Vector multiplyByScalar(double scalar) {
        return new Vector(x * scalar, y * scalar, z * scalar);
    }

    public double dotProduct(Vector v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector crossProduct(Vector v) {
        double x = this.y * v.z - v.y * this.z;
        double y = v.x * this.z - this.x * v.z;
        double z = this.x * v.y - v.x * this.y;
        return new Vector(x, y, z);
    }

    public boolean isNull() {
        return x == 0 && y == 0 && z == 0;
    }

    public Vector unitVector() {
        return multiplyByScalar(1 / magnitude());
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
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
        return new Vector(x, y, z);
    }

    public Vector y(double y) {
        return new Vector(x, y, z);
    }

    public Vector z(double z) {
        return new Vector(x, y, z);
    }

    public Vector getXComponent() {
        return new Vector(x, 0, 0);
    }

    public Vector getYComponent() {
        return new Vector(0, y, 0);
    }

    public Vector getZComponent() {
        return new Vector(0, 0, z);
    }

    @Override
    public Vector clone() {
        return new Vector(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof Vector(double vx, double vy, double vz)) {
            return x == vx && y == vy && z == vz;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        String i = Utils.formatDouble(x, 3) + "i";
        String j = (y >= 0 ? "+" : "") + Utils.formatDouble(y, 3) + "j";
        String k = (z >= 0 ? "+" : "") + Utils.formatDouble(z, 3) + "k";
        return i + j + k;
    }

}

package br.sergio.charges;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;

@Getter
public class Charge {

    public static final double K0 = 1e-7 * 299792458 * 299792458;
    public static final double K0_PX = K0 * Math.pow(Game.PIXELS_PER_METER, 3);
    public static final double G = 6.6743e-11;
    public static final double G_PX = G * Math.pow(Game.PIXELS_PER_METER, 3);
    public static final double CHARGE_MASS = 2.5;
    public static final double ELEMENTAL_CHARGE = 1.60217663 * 1e-19;
    public static final double ELEMENTAL_CHARGE_MULTIPLE = 4.5e11;
    public static final double TRAIL_LIFE_TIME = 4; // seconds

    @Setter
    private Vector position, velocity, acceleration, netForce, linearMomentum;
    private double charge, mass;
    private ChargeType type;
    private Deque<TrailPoint> trail;

    public Charge(ChargeType type) {
        this.type = type;
        charge = chargeValue(type);
        mass = CHARGE_MASS;
        trail = new ArrayDeque<>();
        position = velocity = acceleration = netForce = linearMomentum = Vector.NULL;
    }

    public Charge(Vector position, Vector velocity, Vector acceleration, double mass, double charge) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.mass = mass;
        this.charge = charge;
        this.netForce = acceleration.multiplyByScalar(mass);
        this.linearMomentum = velocity.multiplyByScalar(mass);
        this.type = ChargeType.of(charge);
        this.trail = new ArrayDeque<>();
    }

    @Override
    public String toString() {
        return """
                {
                    "position": "%s",
                    "velocity": "%s",
                    "acceleration": "%s",
                    "netForce": "%s",
                    "linearMomentum": "%s",
                    "mass": %s,
                    "charge": %s,
                    "type": "%s"
                }\
                """.formatted(
                position,
                velocity,
                acceleration,
                netForce,
                linearMomentum,
                mass,
                charge,
                type
        );
    }

    public static double chargeValue(ChargeType type) {
        return type.getSign() * ELEMENTAL_CHARGE_MULTIPLE * ELEMENTAL_CHARGE;
    }

}

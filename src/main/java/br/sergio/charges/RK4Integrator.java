package br.sergio.charges;

import static br.sergio.charges.Game.CHARGE_RADIUS;

import java.util.List;

import br.sergio.charges.Game.Flags;

public class RK4Integrator implements Integrator {

    @Override
    public void integrate(Game game, double deltaTime) {
        List<Charge> charges = game.getCharges();
        List<Vector> netForces = Integrator.computeNetForces(game);
        int size = charges.size();
        assert size == netForces.size();

        Derivative[] k1 = new Derivative[size];
        Derivative[] k2 = new Derivative[size];
        Derivative[] k3 = new Derivative[size];
        Derivative[] k4 = new Derivative[size];

        double halfTime = deltaTime * 0.5;

        for (int i = 0; i < size; i++) {
            Charge charge = charges.get(i);
            Vector initialPos = charge.getPosition();
            Vector initialVel = charge.getVelocity();

            Vector pos, vel = initialVel;

            Vector accel = size < 2 ? charge.getAcceleration() : netForces.get(i).multiplyByScalar(1.0 / charge.getMass());

            k1[i] = new Derivative(vel, accel);

            pos = initialPos.add(k1[i].dPosition.multiplyByScalar(halfTime));
            vel = initialVel.add(k1[i].dVelocity.multiplyByScalar(halfTime));
            k2[i] = new Derivative(vel, computeAccelerationTemp(game, i, pos));

            pos = initialPos.add(k2[i].dPosition.multiplyByScalar(halfTime));
            vel = initialVel.add(k2[i].dVelocity.multiplyByScalar(halfTime));
            k3[i] = new Derivative(vel, computeAccelerationTemp(game, i, pos));

            pos = initialPos.add(k3[i].dPosition.multiplyByScalar(deltaTime));
            vel = initialVel.add(k3[i].dVelocity.multiplyByScalar(deltaTime));
            k4[i] = new Derivative(vel, computeAccelerationTemp(game, i, pos));
        }

        double timeOverSix = deltaTime / 6.0;
        double timeOverThree = deltaTime / 3.0;
        for (int i = 0; i < size; i++) {
            Charge charge = charges.get(i);
            Vector newPos = charge.getPosition()
                    .add(k1[i].dPosition.multiplyByScalar(timeOverSix))
                    .add(k2[i].dPosition.multiplyByScalar(timeOverThree))
                    .add(k3[i].dPosition.multiplyByScalar(timeOverThree))
                    .add(k4[i].dPosition.multiplyByScalar(timeOverSix));
            Vector newVel = charge.getVelocity()
                    .add(k1[i].dVelocity.multiplyByScalar(timeOverSix))
                    .add(k2[i].dVelocity.multiplyByScalar(timeOverThree))
                    .add(k3[i].dVelocity.multiplyByScalar(timeOverThree))
                    .add(k4[i].dVelocity.multiplyByScalar(timeOverSix));
            Vector[] fix = Integrator.takeChargeOutOfTheWall(game, newPos, newVel);
            double mass = charge.getMass();
            charge.setPosition(fix[0]);
            charge.setVelocity(fix[1]);
            charge.setAcceleration(k1[i].dVelocity);
            charge.setNetForce(k1[i].dVelocity.multiplyByScalar(mass));
            charge.setLinearMomentum(fix[1].multiplyByScalar(mass));
        }
    }

    private Vector computeAccelerationTemp(Game game, int index, Vector newPos) {
        List<Charge> charges = game.getCharges();
        int size = charges.size();
        Charge currentCharge = charges.get(index);
        if (size < 2) {
            return currentCharge.getAcceleration();
        }
        Flags flags = game.getFlags();
        if (!flags.electricForce.get() && !flags.gravitationalForce.get()) {
            return Vector.NULL;
        }
        Vector acc = Vector.NULL;
        for (int i = 0; i < size; i++) {
            if (i == index) {
                continue;
            }
            Charge otherCharge = charges.get(i);
            Vector direction = otherCharge.getPosition().subtract(newPos);
            double distance = direction.magnitude();
            if (distance < 2 * CHARGE_RADIUS) {
                continue;
            }
            if (flags.electricForce.get()) {
                double forceMagnitude = -Charge.K0_PX * currentCharge.getCharge()
                        * otherCharge.getCharge() / (distance * distance);
                Vector force = direction.newMagnitude(forceMagnitude);
                acc = acc.add(force.multiplyByScalar(1.0 / currentCharge.getMass()));
            }
            if (flags.gravitationalForce.get()) {
                double forceMagnitude = Charge.G_PX * currentCharge.getMass()
                        * otherCharge.getMass() / (distance * distance);
                Vector force = direction.newMagnitude(forceMagnitude);
                acc = acc.add(force.multiplyByScalar(1.0 / currentCharge.getMass()));
            }
        }
        return acc;
    }

    record Derivative(Vector dPosition, Vector dVelocity) {}

}

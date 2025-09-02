package br.sergio.charges;

import static br.sergio.charges.Game.CHARGE_RADIUS;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import br.sergio.charges.Game.Flags;

public interface Integrator {

    void integrate(Game game, double deltaTime);

    static List<Vector> computeNetForces(Game game) {
        List<Charge> charges = game.getCharges();
        Flags flags = game.getFlags();
        List<Vector> netForces = new ArrayList<>(charges.size());
        if (!flags.electricForce.get() && !flags.gravitationalForce.get()) {
            int size = charges.size();
            Vector zero = new Vector();
            for (int i = 0; i < size; i++) {
                netForces.add(zero);
            }
            return netForces;
        }
        for (Charge currentCharge : charges) {
            Vector netForce = new Vector();
            for (Charge otherCharge : charges) {
                if (otherCharge == currentCharge) {
                    continue;
                }
                Vector direction = otherCharge.getPosition().subtract(currentCharge.getPosition());
                double distance = direction.magnitude();
                if (distance < 2 * CHARGE_RADIUS) {
                    continue;
                }
                if (flags.electricForce.get()) {
                    double forceMagnitude = -Charge.K0_PX * currentCharge.getCharge()
                            * otherCharge.getCharge() / (distance * distance);
                    Vector force = direction.newMagnitude(forceMagnitude);
                    netForce = netForce.add(force);
                }
                if (flags.gravitationalForce.get()) {
                    double forceMagnitude = Charge.G_PX * currentCharge.getMass()
                            * otherCharge.getMass() / (distance * distance);
                    Vector force = direction.newMagnitude(forceMagnitude);
                    netForce = netForce.add(force);
                }
            }
            netForces.add(netForce);
        }
        return netForces;
    }

    static Vector[] takeChargeOutOfTheWall(Game game, Vector pos, Vector vel) {
        Vector correctedPos = pos;
        Vector correctedVel = vel;

        Dimension size = game.getSize();

        // Colisão com parede esquerda
        if (pos.x() - CHARGE_RADIUS <= 0 && vel.x() < 0) {
            double t = (CHARGE_RADIUS - pos.x()) / vel.x();
            correctedPos = pos.add(vel.multiplyByScalar(t));
            correctedVel = new Vector(-vel.x(), vel.y());
        }

        // Colisão com parede direita
        if (pos.x() + CHARGE_RADIUS >= size.width && vel.x() > 0) {
            double t = (size.width - CHARGE_RADIUS - pos.x()) / vel.x();
            correctedPos = pos.add(vel.multiplyByScalar(t));
            correctedVel = new Vector(-vel.x(), vel.y());
        }

        // Colisão com parede superior
        if (pos.y() - CHARGE_RADIUS <= 0 && vel.y() < 0) {
            double t = (CHARGE_RADIUS - pos.y()) / vel.y();
            correctedPos = pos.add(vel.multiplyByScalar(t));
            correctedVel = new Vector(vel.x(), -vel.y());
        }

        // Colisão com parede inferior
        if (pos.y() + CHARGE_RADIUS >= size.height && vel.y() > 0) {
            double t = (size.height - CHARGE_RADIUS - pos.y()) / vel.y();
            correctedPos = pos.add(vel.multiplyByScalar(t));
            correctedVel = new Vector(vel.x(), -vel.y());
        }

        return new Vector[]{correctedPos, correctedVel};
    }

}

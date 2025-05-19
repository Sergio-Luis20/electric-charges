package br.sergio.charges;

import br.sergio.charges.Game.Flags;
import lombok.AllArgsConstructor;

import java.awt.Dimension;

import static br.sergio.charges.Game.CHARGE_RADIUS;

@AllArgsConstructor
public abstract class Integrator {

    protected final Game game;

    public void beforeLoop() {
    }

    public void afterLoop() {
    }

    public abstract void integrate(Charge currentCharge, double deltaTime);

    protected Vector computeNetForce(Charge currentCharge, Vector position) {
        Vector netForce = new Vector();
        Flags flags = game.getFlags();
        if (flags.electricForce.get() || flags.gravitationalForce.get()) {
            for (Charge otherCharge : game.getCharges()) {
                if (otherCharge == currentCharge) {
                    continue;
                }
                Vector direction = otherCharge.getPosition().subtract(position);
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
        }
        return netForce;
    }

    protected Vector[] takeChargeOutOfTheWall(Vector pos, Vector vel) {
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

        return new Vector[] {correctedPos, correctedVel};
    }

}

package br.sergio.charges;

import java.util.List;

public class DefaultIntegrator implements Integrator {

    @Override
    public void integrate(Game game, double deltaTime) {
        List<Charge> charges = game.getCharges();
        List<Vector> netForces = Integrator.computeNetForces(game);
        int size = charges.size();
        assert size == netForces.size();
        for (int i = 0; i < size; i++) {
            Charge currentCharge = charges.get(i);
            Vector netForce = netForces.get(i);
            double mass = currentCharge.getMass();
            Vector acceleration = size >= 2 || !netForce.isNull() ? netForce.multiplyByScalar(1.0 / mass) : currentCharge.getAcceleration();
            Vector initialVel = currentCharge.getVelocity();
            Vector finalPos = currentCharge.getPosition().add(initialVel.multiplyByScalar(deltaTime))
                    .add(acceleration.multiplyByScalar(deltaTime * deltaTime / 2.0));
            Vector finalVel = initialVel.add(acceleration.multiplyByScalar(deltaTime));
            Vector[] result = Integrator.takeChargeOutOfTheWall(game, finalPos, finalVel);
            currentCharge.setPosition(result[0]);
            currentCharge.setVelocity(result[1]);
            currentCharge.setAcceleration(acceleration);
            currentCharge.setNetForce(netForce);
            currentCharge.setLinearMomentum(result[1].multiplyByScalar(mass));
        }
    }

}

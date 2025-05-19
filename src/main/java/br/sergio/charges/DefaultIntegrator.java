package br.sergio.charges;

public class DefaultIntegrator extends Integrator {

    public DefaultIntegrator(Game game) {
        super(game);
    }

    @Override
    public void integrate(Charge currentCharge, double deltaTime) {
        Vector netForce = computeNetForce(currentCharge, currentCharge.getPosition());
        Vector acceleration;
        if (game.getCharges().size() >= 2) {
            acceleration = netForce.multiplyByScalar(1 / currentCharge.getMass());
        } else {
            acceleration = netForce.isNull() ? currentCharge.getAcceleration() : netForce.multiplyByScalar(1 / currentCharge.getMass());
        }

        Vector finalPos = currentCharge.getPosition().add(currentCharge.getVelocity().multiplyByScalar(deltaTime))
                .add(acceleration.multiplyByScalar(deltaTime * deltaTime / 2));
        Vector finalVel = currentCharge.getVelocity().add(acceleration.multiplyByScalar(deltaTime));

        Vector[] result = takeChargeOutOfTheWall(finalPos, finalVel);

        currentCharge.setPosition(result[0]);
        currentCharge.setVelocity(result[1]);
        currentCharge.setAcceleration(acceleration);
        currentCharge.setNetForce(netForce);
        currentCharge.setLinearMomentum(result[1].multiplyByScalar(currentCharge.getMass()));
    }

}

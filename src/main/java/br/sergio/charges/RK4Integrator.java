package br.sergio.charges;

public class RK4Integrator extends Integrator {

    private static final Derivative NULL = new Derivative(Vector.NULL, Vector.NULL);

    public RK4Integrator(Game game) {
        super(game);
    }

    @Override
    public void integrate(Charge currentCharge, double deltaTime) {
        Vector x = currentCharge.getPosition();
        Vector v = currentCharge.getVelocity();

        Derivative a = evaluate(currentCharge, x, v, deltaTime, NULL);
        Derivative b = evaluate(currentCharge, x, v, deltaTime * 0.5, a);
        Derivative c = evaluate(currentCharge, x, v, deltaTime * 0.5, b);
        Derivative d = evaluate(currentCharge, x, v, deltaTime, c);

        Vector dxdt = a.dPosition.add(b.dPosition.multiplyByScalar(2))
                .add(c.dPosition.multiplyByScalar(2))
                .add(d.dPosition)
                .multiplyByScalar(1.0 / 6.0);

        Vector dvdt = a.dVelocity.add(b.dVelocity.multiplyByScalar(2))
                .add(c.dVelocity.multiplyByScalar(2))
                .add(d.dVelocity)
                .multiplyByScalar(1.0 / 6.0);

        Vector newPosition = x.add(dxdt.multiplyByScalar(deltaTime));
        Vector newVelocity = v.add(dvdt.multiplyByScalar(deltaTime));

        Vector[] result = takeChargeOutOfTheWall(newPosition, newVelocity);
        double mass = currentCharge.getMass();

        currentCharge.setPosition(result[0]);
        currentCharge.setVelocity(result[1]);
        currentCharge.setAcceleration(dvdt);
        currentCharge.setNetForce(dvdt.multiplyByScalar(mass));
        currentCharge.setLinearMomentum(result[1].multiplyByScalar(mass));
    }

    private Derivative evaluate(Charge charge, Vector position, Vector velocity, double deltaTime, Derivative d) {
        Vector newPosition = position.add(d.dPosition.multiplyByScalar(deltaTime));
        Vector newVelocity = velocity.add(d.dVelocity.multiplyByScalar(deltaTime));
        Vector netForce = computeNetForce(charge, newPosition);
        Vector acceleration = netForce.multiplyByScalar(1 / charge.getMass());
        return new Derivative(newVelocity, acceleration);
    }

    record Derivative(Vector dPosition, Vector dVelocity) {}

}

package br.sergio.charges;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
public class Game extends Canvas implements Runnable {

    public static final int MIN_WIDTH = 800, MIN_HEIGHT = 600;
    public static final int CHARGE_RADIUS = 10;
    public static final double METERS_PER_PIXEL = 2.54e-2 / Toolkit.getDefaultToolkit().getScreenResolution();
    public static final double PIXELS_PER_METER = 1 / METERS_PER_PIXEL;
    public static final Color INFO_TARGET_COLOR = new Color(0xffc0ff);
    public static final Color BACKGROUND_COLOR = new Color(9, 10, 17);

    private final Flags flags;
    private final List<Charge> charges;
    private final List<Runnable> runners;
    private final Object loopLock = new Object();

    @Setter
    private volatile Charge infoTarget;
    private volatile long frameRate;
    private int exitCode;
    private double simulatedTime = 0;
    private Integrator integrator;

    @Setter
    private ControlPanel controlPanel;

    public Game() {
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        charges = new ArrayList<>();
        runners = new ArrayList<>();
        flags = new Flags();
        integrator = new RK4Integrator();
    }

    public void stop() {
        stop(0);
    }

    public void stop(int exitCode) {
        flags.running.set(false);
        if (flags.paused.get()) {
            setPaused(false);
        }
        this.exitCode = exitCode;
    }

    public void setPaused(boolean paused) {
        flags.paused.set(paused);
    }

    public void addCyclicRunner(Runnable runner) {
        synchronized (runners) {
            runners.add(runner);
        }
    }

    private void update(double deltaTime) {
        simulatedTime += deltaTime;
        synchronized (charges) {
            Iterator<Charge> iterator = charges.iterator();
            Dimension size = getSize();
            while (iterator.hasNext()) {
                Charge charge = iterator.next();
                Vector pos = charge.getPosition();
                double x = pos.x();
                double y = pos.y();
                if (x < -CHARGE_RADIUS || x > size.width + CHARGE_RADIUS
                        || y < -CHARGE_RADIUS || y > size.height + CHARGE_RADIUS) {
                    iterator.remove();
                    if (charge == infoTarget) {
                        infoTarget = null;
                    }
                    controlPanel.updateChargeAmount();
                }
            }
            integrator.integrate(this, deltaTime);
            for (Charge charge : charges) {
                Deque<TrailPoint> trail = charge.getTrail();
                trail.addLast(new TrailPoint(charge.getPosition(), simulatedTime));
                while (!trail.isEmpty() && simulatedTime - trail.peekFirst().timestamp() > Charge.TRAIL_LIFE_TIME) {
                    trail.removeFirst();
                }
            }
        }
    }

    private Vector rotate(Vector v, double angle) {
        double magnitude = v.magnitude();
        double initialAngle = Math.atan2(v.y(), v.x());
        double finalAngle = initialAngle + angle;

        double newX = magnitude * Math.cos(finalAngle);
        double newY = magnitude * Math.sin(finalAngle);

        return new Vector(newX, newY);
    }

    private void drawVector(Graphics2D g, Vector pos, Vector v, boolean isPosItself) {
        Vector origin, end;
        if (isPosItself) {
            origin = pos;
            end = v;
        } else {
            origin = pos.add(v.newMagnitude(CHARGE_RADIUS));
            end = origin.add(v);
        }

        g.drawLine((int) origin.x(), (int) origin.y(), (int) end.x(), (int) end.y());

        double arrowArmLen = 5;
        Vector arm = end.subtract(origin).multiplyByScalar(-1).newMagnitude(arrowArmLen);
        double angle = Math.PI / 6;
        Vector leftArm = end.add(rotate(arm, -angle));
        Vector rightArm = end.add(rotate(arm, angle));

        g.drawLine((int) end.x(), (int) end.y(), (int) leftArm.x(), (int) leftArm.y());
        g.drawLine((int) end.x(), (int) end.y(), (int) rightArm.x(), (int) rightArm.y());
    }

    private void drawTrail(Graphics2D g, Charge charge) {
        Color originalColor = g.getColor();

        Deque<TrailPoint> trail = charge.getTrail();
        Color baseColor = charge == infoTarget ? INFO_TARGET_COLOR : charge.getType().getColor();

        TrailPoint prev = null;
        for (TrailPoint current : trail) {
            if (prev != null) {
                double age = simulatedTime - current.timestamp();
                double alpha = 1 - age / Charge.TRAIL_LIFE_TIME;
                if (alpha < 0) alpha = 0;

                Color faded = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (alpha * 120));
                g.setColor(faded);
                g.setStroke(new BasicStroke(2f));
                g.drawLine(
                        (int) prev.position().x(), (int) prev.position().y(),
                        (int) current.position().x(), (int) current.position().y()
                );
            }
            prev = current;
        }

        g.setColor(originalColor);
    }

    private void drawCharge(Graphics2D g, Charge charge) {
        Vector pos = charge.getPosition();
        Color color = charge == infoTarget ? INFO_TARGET_COLOR : charge.getType().getColor();

        if (flags.showingPath.get()) {
            drawTrail(g, charge);
        }

        int glowDiameter = CHARGE_RADIUS + 35;
        float[] dist = {0, 1};
        Color[] colors = {
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 120),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)
        };

        RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Double(pos.x(), pos.y()),
                glowDiameter / 2.0f,
                dist,
                colors,
                MultipleGradientPaint.CycleMethod.NO_CYCLE
        );

        Paint originalPaint = g.getPaint();
        g.setPaint(paint);
        g.fillOval(
                (int) (pos.x() - glowDiameter / 2.0),
                (int) (pos.y() - glowDiameter / 2.0),
                glowDiameter,
                glowDiameter
        );
        g.setPaint(originalPaint);

        g.setFont(g.getFont().deriveFont(Font.BOLD, 16));

        String text = charge.getType().getSymbol();
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getAscent();
        int textX = (int) (pos.x() - textWidth / 2.0 + 1);
        int textY = (int) (pos.y() + (textHeight - metrics.getDescent()) / 2.0);

        g.setStroke(new BasicStroke(2.0f));
        g.setColor(color);
        g.drawString(text, textX, textY);

        int diameter = 2 * CHARGE_RADIUS;
        g.drawOval((int) (pos.x() - CHARGE_RADIUS), (int) (pos.y() - CHARGE_RADIUS), diameter, diameter);

        if (flags.showingPositionVector.get()) {
            g.setColor(Color.WHITE);
            drawVector(g, new Vector(0, getSize().height), pos, true);
        }

        if (flags.showingVelocityVector.get()) {
            g.setColor(Color.GREEN);
            drawVector(g, pos, charge.getVelocity(), false);
        }

        if (flags.showingAccelerationVector.get()) {
            g.setColor(Color.YELLOW);
            drawVector(g, pos, charge.getAcceleration(), false);
        }

        if (flags.showingNetForceVector.get()) {
            g.setColor(Color.MAGENTA);
            drawVector(g, pos, charge.getNetForce(), false);
        }

        if (flags.showingLinearMomentumVector.get()) {
            g.setColor(Color.LIGHT_GRAY);
            drawVector(g, pos, charge.getLinearMomentum(), false);
        }
    }

    private void render() {
        if (!isDisplayable() || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        BufferStrategy bufferStrategy = getBufferStrategy();
        if (bufferStrategy == null) {
            try {
                createBufferStrategy(2);
            } catch (Exception e) {
                log.error("Could not create BufferStrategy.", e);
            }
            return;
        }
        Graphics2D g;
        try {
            g = (Graphics2D) bufferStrategy.getDrawGraphics();
        } catch (Exception e) {
            log.error("Could not get Graphics2D.", e);
            return;
        }
        try {
            Dimension size = getSize();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, size.width, size.height);
            synchronized (charges) {
                for (Charge charge : charges) {
                    drawCharge(g, charge);
                }
            }
        } finally {
            g.dispose();
            bufferStrategy.show();
            Toolkit.getDefaultToolkit().sync();
        }
    }

    @Override
    public void run() {
        flags.running.set(true);
        try {
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            System.out.println("Iniciando game loop com densidade pixelar de " + dpi + "px/in");
            long startTime, endTime, deltaTime;
            startTime = System.nanoTime();
            long delta = 0;
            long frames = 0;
            while (flags.running.get()) {
                endTime = System.nanoTime();
                deltaTime = endTime - startTime;
                startTime = endTime;
                frames++;
                delta += deltaTime;
                if (delta >= 1e9) {
                    delta -= (long) 1e9;
                    frameRate = frames;
                    frames = 0;
                }
                synchronized (loopLock) {
                    if (!flags.paused.get()) {
                        update(deltaTime * 1e-9);
                    }
                    render();
                    synchronized (runners) {
                        runners.forEach(Runnable::run);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in game loop.", e);
            exitCode = -1;
        }
    }

    public static class Flags {

        public final AtomicBoolean running,
                                   paused,
                                   showingPositionVector,
                                   showingVelocityVector,
                                   showingAccelerationVector,
                                   showingNetForceVector,
                                   showingLinearMomentumVector,
                                   showingPath,
                                   electricForce,
                                   gravitationalForce;

        public Flags() {
            running = new AtomicBoolean();
            paused = new AtomicBoolean();
            showingPositionVector = new AtomicBoolean();
            showingVelocityVector = new AtomicBoolean();
            showingAccelerationVector = new AtomicBoolean();
            showingNetForceVector = new AtomicBoolean();
            showingLinearMomentumVector = new AtomicBoolean();
            showingPath = new AtomicBoolean();
            electricForce = new AtomicBoolean();
            gravitationalForce = new AtomicBoolean();

            electricForce.set(true);
        }

    }

}

package ec;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import br.sergio.math.Vector;

public class Environment extends Canvas implements Runnable {
	
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int sl = 20;
	private static final long serialVersionUID = -5469356628665257570L;
	private static final double k = 9 * Math.pow(10, 9);
	private static final float mass = 0.2f;
	private Thread thread;
	private boolean running;
	private long lastTime;
	private List<Charge> charges = new ArrayList<>();
	private Queue<Runnable> queue = new LinkedList<>();
	
	public Environment() {
		super();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		addMouseListener(new ClickListener(this));
		thread = new Thread(this);
	}
	
	public void startThread() {
		running = true;
		thread.start();
	}
	
	public void tick() {
		int size = charges.size();
		Vector[] initialPos = new Vector[size];
		for(int i = 0; i < size; i++) {
			Charge charge = charges.get(i);
			initialPos[i] = new Vector(charge.x, charge.y);
		}
		long nowTime = System.nanoTime();
		double timeVariation = (nowTime - lastTime) / Math.pow(10, 9);
		lastTime = nowTime;
		double factor = Math.pow(timeVariation, 2) / 2;
		List<Charge> toRemove = new ArrayList<>();
		for(int i = 0; i < size; i++) {
			Charge charge = charges.get(i);
			Vector s0 = initialPos[i];
			Vector netForce = Vector.NULL;
			Rectangle rect = new Rectangle((int) s0.getX(), (int) s0.getY(), sl, sl);
			boolean neutral = false;
			for(int j = 0; j < size; j++) {
				Charge another = charges.get(j);
				if(charge == another) {
					continue;
				}
				Vector anotherS0 = initialPos[j];
				Rectangle anotherRect = new Rectangle((int) anotherS0.getX(), (int) anotherS0.getY(), sl, sl);
				Vector distVariation = initialPos[i].subtract(initialPos[j]);
				if(rect.x == anotherRect.x && rect.y == anotherRect.y) {
					continue;
				}
				if(rect.intersects(anotherRect)) {
					if(charge.getCharge() * another.getCharge() < 0) {
						toRemove.add(charge);
						toRemove.add(another);
						neutral = true;
						break;
					} else {
						Vector w = charge.getVelocity();
						Vector u = w.projection(distVariation);
						Vector v = w.subtract(u);
						charge.setVelocity(v.subtract(u));
					}
				}
				double d2 = Math.pow(distVariation.getMagnitude(), 2);
				Vector force = distVariation.versor().multiplyByScalar(k * charge.getCharge() * another.getCharge() / d2);
				netForce = netForce.add(force);
			}
			if(neutral) {
				continue;
			}
			Vector accel = netForce.multiplyByScalar(1 / mass);
			Vector vel = charge.getVelocity();
			Vector s = s0.add(vel.multiplyByScalar(timeVariation)).add(accel.multiplyByScalar(factor));
			Vector newVel = vel.add(accel.multiplyByScalar(timeVariation));
			charge.setVelocity(newVel);
			charge.x = s.getX();
			charge.y = s.getY();
			if((charge.x < 0 && charge.x + sl >= 0) || (charge.x + sl > WIDTH && charge.x <= WIDTH)) {
				charge.setVelocity(newVel.getI().multiplyByScalar(-1).add(newVel.getJ()));
			} else if(charge.x + sl < 0 || charge.x > WIDTH) {
				toRemove.add(charge);
			}
			if((charge.y < 0 && charge.y + sl >= 0) || (charge.y + sl > HEIGHT && charge.y <= HEIGHT)) {
				charge.setVelocity(newVel.getJ().multiplyByScalar(-1).add(newVel.getI()));
			} else if(charge.y + sl < 0 || charge.y > HEIGHT) {
				toRemove.add(charge);
			}
		}
		charges.removeAll(toRemove);
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		for(Charge charge : charges) {
			g.drawImage(charge.getImage(), (int) charge.x - sl / 2, (int) charge.y - sl / 2, sl, sl, null);
		}
		g.dispose();
		bs.show();
	}
	
	@Override
	public void run() {
		lastTime = System.nanoTime();
		while(running) {
			tick();
			render();
			Runnable runnable = queue.poll();
			if(runnable != null) {
				runnable.run();
			}
		}
	}
	
	public List<Charge> getCharges() {
		return charges;
	}
	
	public Queue<Runnable> getQueue() {
		return queue;
	}
}

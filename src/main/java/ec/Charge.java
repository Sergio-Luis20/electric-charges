package ec;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import br.sergio.math.Vector;

public class Charge {
	
	private static double scale = Math.pow(10, -2.5);
	public double x, y;
	private double charge;
	private BufferedImage image;
	private Vector velocity;
	
	public Charge(double charge) {
		try {
			this.charge = charge * scale;
			image = ImageIO.read(getClass().getResourceAsStream(charge >= 0 ? "/proton.png" : "/eletron.png"));
			velocity = Vector.NULL;
		} catch(IOException e) {
			throw new RuntimeException("Deu bosta");
		}
	}
	
	public double getCharge() {
		return charge;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public Vector getVelocity() {
		return velocity;
	}
	
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}
}

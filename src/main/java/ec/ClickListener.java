package ec;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ClickListener implements MouseListener {
	
	private Environment environment;
	
	public ClickListener(Environment environment) {
		this.environment = environment;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		environment.getQueue().add(() -> {
			Charge charge;
			if(e.getButton() == MouseEvent.BUTTON1) {
				charge = new Charge(1);
			} else if(e.getButton() == MouseEvent.BUTTON3) {
				charge = new Charge(-1);
			} else {
				return;
			}
			Point pos = environment.getMousePosition();
			charge.x = pos.x;
			charge.y = pos.y;
			if(charge.x < Environment.sl) {
				charge.x = Environment.sl;
			} else if(charge.x > Environment.WIDTH - Environment.sl / 2) {
				charge.x = Environment.WIDTH - Environment.sl / 2;
			}
			if(charge.y < Environment.sl) {
				charge.y = Environment.sl;
			} else if(charge.y > Environment.HEIGHT - Environment.sl / 2) {
				charge.y = Environment.HEIGHT - Environment.sl / 2;
			}
			environment.getCharges().add(charge);
		});
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
}

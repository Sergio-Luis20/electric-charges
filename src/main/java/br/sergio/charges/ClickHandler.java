package br.sergio.charges;

import lombok.AllArgsConstructor;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@AllArgsConstructor
public class ClickHandler extends MouseAdapter {

    private Game game;
    private ControlPanel controlPanel;

    @Override
    public void mouseClicked(MouseEvent event) {
        List<Charge> charges = game.getCharges();
        synchronized (charges) {
            Point pos = game.getMousePosition();
            int x = pos.x;
            int y = pos.y;

            Charge clickedCharge = clickedCharge(charges, new Vector(x, y));

            if (clickedCharge != null) {
                switch (event.getButton()) {
                    case MouseEvent.BUTTON1 -> {
                        charges.remove(clickedCharge);
                        if (game.getInfoTarget() == clickedCharge) {
                            game.setInfoTarget(null);
                        }
                        controlPanel.updateChargeAmount();
                    }
                    case MouseEvent.BUTTON3 -> game.setInfoTarget(clickedCharge == game.getInfoTarget() ? null : clickedCharge);
                }
            } else {
                if (event.isShiftDown()) {
                    boolean resume = !game.getFlags().paused.get();
                    if (resume) {
                        game.setPaused(true);
                    }
                    CustomCharge customCharge = new CustomCharge(game, x, y, resume);
                    customCharge.setVisible(true);
                    return;
                }
                Charge charge = switch (event.getButton()) {
                    case MouseEvent.BUTTON1 -> new Charge(ChargeType.PROTON);
                    case MouseEvent.BUTTON2 -> new Charge(ChargeType.NEUTRON);
                    case MouseEvent.BUTTON3 -> new Charge(ChargeType.ELECTRON);
                    default -> null;
                };
                if (charge == null) return;
                charge.setPosition(Utils.fixPosition(game, x, y));
                charges.add(charge);
                controlPanel.updateChargeAmount();
            }
        }
    }

    private Charge clickedCharge(List<Charge> charges, Vector position) {
        for (Charge charge : charges) {
            if (charge.getPosition().subtract(position).magnitude() < Game.CHARGE_RADIUS) {
                return charge;
            }
        }
        return null;
    }

}

package br.sergio.charges;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class ControlPanel extends JPanel {

    private Game game;

    private JLabel chargeAmountLabel;
    private JLabel positiveChargeAmountLabel;
    private JLabel negativeChargeAmountLabel;
    private JLabel neutralChargeAmountLabel;

    private JTextArea chargeInfo;

    public ControlPanel(Game game) {
        this.game = game;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(23, 28, 47));
        setMinimumSize(new Dimension(600, Game.MIN_HEIGHT));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JButton pauseResume = new JButton("Pausar animação");
        pauseResume.setAlignmentX(Component.LEFT_ALIGNMENT);
        pauseResume.setMaximumSize(new Dimension(155, 23));
        pauseResume.setFocusable(false);
        pauseResume.addActionListener(event -> {
            boolean paused = !game.getFlags().paused.get();
            pauseResume.setText(paused ? "Continuar animação" : "Pausar animação");
            game.setPaused(paused);
        });
        add(pauseResume);

        JCheckBox showPositionVector = new JCheckBox("Mostrar vetor posição");
        JCheckBox showVelocityVector = new JCheckBox("Mostrar vetor velocidade");
        JCheckBox showAccelerationVector = new JCheckBox("Mostrar vetor aceleração");
        JCheckBox showNetForceVector = new JCheckBox("Mostrar vetor força resultante");
        JCheckBox showLinearMomentumVector = new JCheckBox("Mostrar vetor momento linear");
        JCheckBox showPath = new JCheckBox("Mostrar trajetória");

        showPositionVector.addItemListener(event -> game.getFlags().showingPositionVector.set(event.getStateChange() == ItemEvent.SELECTED));
        showVelocityVector.addItemListener(event -> game.getFlags().showingVelocityVector.set(event.getStateChange() == ItemEvent.SELECTED));
        showAccelerationVector.addItemListener(event -> game.getFlags().showingAccelerationVector.set(event.getStateChange() == ItemEvent.SELECTED));
        showNetForceVector.addItemListener(event -> game.getFlags().showingNetForceVector.set(event.getStateChange() == ItemEvent.SELECTED));
        showLinearMomentumVector.addItemListener(event -> game.getFlags().showingLinearMomentumVector.set(event.getStateChange() == ItemEvent.SELECTED));
        showPath.addItemListener(event -> game.getFlags().showingPath.set(event.getStateChange() == ItemEvent.SELECTED));

        addCheckBox(showPositionVector);
        addCheckBox(showVelocityVector);
        addCheckBox(showAccelerationVector);
        addCheckBox(showNetForceVector);
        addCheckBox(showLinearMomentumVector);
        addCheckBox(showPath);

        chargeAmountLabel = new JLabel("Total de cargas: 0");
        positiveChargeAmountLabel = new JLabel("Total de cargas positivas: 0");
        negativeChargeAmountLabel = new JLabel("Total de cargas negativas: 0");
        neutralChargeAmountLabel = new JLabel("Total de cargas neutras: 0");

        addJLabel(chargeAmountLabel);
        addJLabel(positiveChargeAmountLabel);
        addJLabel(negativeChargeAmountLabel);
        addJLabel(neutralChargeAmountLabel);

        JButton removeAllCharges = new JButton("Remover todas as cargas");
        removeAllCharges.setAlignmentX(Component.LEFT_ALIGNMENT);
        removeAllCharges.setFocusable(false);
        removeAllCharges.addActionListener(event -> {
            java.util.List<Charge> charges = game.getCharges();
            synchronized (charges) {
                charges.clear();
                game.setInfoTarget(null);
                updateChargeAmount();
            }
        });
        add(removeAllCharges);

        JLabel fpsLabel = new JLabel("FPS: 0");
        addJLabel(fpsLabel);

        game.addCyclicRunner(() -> fpsLabel.setText("FPS: " + game.getFrameRate()));

        JCheckBox electricForce = new JCheckBox("Força elétrica");
        JCheckBox gravitationalForce = new JCheckBox("Força gravitacional");
        electricForce.setSelected(true);

        electricForce.addItemListener(event -> game.getFlags().electricForce.set(event.getStateChange() == ItemEvent.SELECTED));
        gravitationalForce.addItemListener(event -> game.getFlags().gravitationalForce.set(event.getStateChange() == ItemEvent.SELECTED));

        addCheckBox(electricForce);
        addCheckBox(gravitationalForce);

        JLabel clickToViewInfo = new JLabel("Clique numa carga com botão direito para ver informações");
        addJLabel(clickToViewInfo);

        chargeInfo = new JTextArea();
        chargeInfo.setLineWrap(true);
        chargeInfo.setEditable(false);
        chargeInfo.setForeground(new Color(0xc0c0c0));
        chargeInfo.setBackground(getBackground());
        chargeInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        chargeInfo.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane infoPane = new JScrollPane();
        infoPane.setViewportView(chargeInfo);
        infoPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(infoPane);

        game.addCyclicRunner(() -> showChargeInfo(game.getInfoTarget()));
    }

    private void addCheckBox(JCheckBox checkBox) {
        checkBox.setBackground(getBackground());
        checkBox.setForeground(new Color(0xc0c0c0));
        checkBox.setFocusable(false);
        checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(checkBox);
    }

    private void addJLabel(JLabel label) {
        label.setForeground(new Color(0xc0c0c0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
    }

    public void updateChargeAmount() {
        List<Charge> charges = game.getCharges();
        chargeAmountLabel.setText("Total de cargas: " + charges.size());
        positiveChargeAmountLabel.setText("Total de cargas positivas: "
                + charges.stream().filter(c -> c.getType() == ChargeType.POSITIVE).count());
        negativeChargeAmountLabel.setText("Total de cargas negativas: "
                + charges.stream().filter(c -> c.getType() == ChargeType.NEGATIVE).count());
        neutralChargeAmountLabel.setText("Total de cargas neutras: "
                + charges.stream().filter(c -> c.getType() == ChargeType.NEUTRAL).count());
    }

    public void showChargeInfo(Charge c) {
        String infoText;
        if (c == null) {
            infoText = "";
        } else {
            double charge = c.getCharge();
            double mass = c.getMass();
            Vector pixelPosition = c.getPosition().y(game.getSize().height - c.getPosition().y());
            Vector position = pixelPosition.multiplyByScalar(Game.METERS_PER_PIXEL);
            Vector pixelVelocity = c.getVelocity().y(-c.getVelocity().y());
            Vector velocity = pixelVelocity.multiplyByScalar(Game.METERS_PER_PIXEL);
            Vector pixelAcceleration = c.getAcceleration().y(-c.getAcceleration().y());
            Vector acceleration = pixelAcceleration.multiplyByScalar(Game.METERS_PER_PIXEL);
            Vector pixelNetForce = c.getNetForce().y(-c.getNetForce().y());
            Vector netForce = pixelNetForce.multiplyByScalar(Game.METERS_PER_PIXEL);
            Vector pixelLinearMomentum = c.getLinearMomentum().y(-c.getLinearMomentum().y());
            Vector linearMomentum = pixelLinearMomentum.multiplyByScalar(Game.METERS_PER_PIXEL);
            double pixelKineticEnergy = mass * pixelVelocity.magnitudeSquared() / 2;
            double kineticEnergy = mass * velocity.magnitudeSquared() / 2;
            infoText = """
                    Carga: %s C
                    Massa: %s kg

                    Posição:
                    Metros: %s (Módulo: %s m)
                    Pixels: %s (Módulo: %s px)
                    
                    Velocidade:
                    Metros: %s (Módulo: %s m/s)
                    Pixels: %s (Módulo: %s px/s)
                    
                    Aceleração:
                    Metros: %s (Módulo: %s m/s²)
                    Pixels: %s (Módulo: %s px/s²)
                    
                    Força Resultante:
                    Metros: %s (Módulo: %s N)
                    Pixels: %s (Módulo: %s kg·px/s²)
                    
                    Momento Linear:
                    Metros: %s (Módulo: %s kg·m/s)
                    Pixels: %s (Módulo: %s kg·px/s)
                    
                    Energia Cinética:
                    Metros: %s J
                    Pixels: %s kg·px²/s²
                    
                    Observações:
                    
                    Como animações dependem de taxas de quadro, há
                    discretização e descontinuidade, o que impede
                    a conservação de energia no longo prazo. Além disso,
                    outra coisa que agrava esse efeito é o fato de que
                    cargas aqui não colidem; quando estão atravessando
                    outras, a força resultante é definida para 0 para
                    evitar que ela exploda devido à distância tender a 0.
                    
                    Assume-se que 1 px = %s m nesta simulação.
                    
                    A constante eletrostática usada é
                    k₀ = %s N·m²/C² = %s kg·px³/C².
                    
                    A constante gravitacional usada é
                    G = %s m³/(kg·s²) = %s px³/(kg·s²).
                    
                    Os números mostrados aqui estão aproximados
                    para uma quantidade limitada de casas decimais.
                    Seus valores reais internamente podem ter uma
                    precisão maior.
                    """.formatted(
                    Utils.formatDouble(charge, 3),
                    Utils.formatDouble(mass, 3),
                    position,
                    Utils.formatDouble(position.magnitude(), 3),
                    pixelPosition,
                    Utils.formatDouble(pixelPosition.magnitude(), 3),
                    velocity,
                    Utils.formatDouble(velocity.magnitude(), 3),
                    pixelVelocity,
                    Utils.formatDouble(pixelVelocity.magnitude(), 3),
                    acceleration,
                    Utils.formatDouble(acceleration.magnitude(), 3),
                    pixelAcceleration,
                    Utils.formatDouble(pixelAcceleration.magnitude(), 3),
                    netForce,
                    Utils.formatDouble(netForce.magnitude(), 3),
                    pixelNetForce,
                    Utils.formatDouble(pixelNetForce.magnitude(), 3),
                    linearMomentum,
                    Utils.formatDouble(linearMomentum.magnitude(), 3),
                    pixelLinearMomentum,
                    Utils.formatDouble(pixelLinearMomentum.magnitude(), 3),
                    Utils.formatDouble(kineticEnergy, 3),
                    Utils.formatDouble(pixelKineticEnergy, 3),
                    Utils.formatDouble(Game.METERS_PER_PIXEL, 3),
                    Utils.formatDouble(Charge.K0, 3),
                    Utils.formatDouble(Charge.K0_PX, 3),
                    Utils.formatDouble(Charge.G, 4),
                    Utils.formatDouble(Charge.G_PX, 4)
            );
        }
        chargeInfo.setText(infoText);
    }

}

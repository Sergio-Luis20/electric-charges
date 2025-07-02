package br.sergio.charges;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

@Slf4j
public class CustomCharge extends JDialog {

    private Game game;
    private double x, y;
    private boolean resume;
    private double initialCharge;

    public CustomCharge(Game game, double x, double y, boolean resume, double initialCharge) {
        super(Main.frame, "Carga personalizada", true);
        this.game = game;
        this.x = x;
        this.y = y;
        this.resume = resume;
        this.initialCharge = initialCharge;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resume(false);
            }
        });
        setResizable(false);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI(contentPanel);
        setContentPane(contentPanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void buildUI(JPanel contentPane) {
        Dimension textFieldSize = new Dimension(200, 25);

        JLabel vLabel = new JLabel("Velocidade");
        vLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JRadioButton vM = new JRadioButton("m/s");
        JRadioButton vPx = new JRadioButton("px/s");
        vM.setFocusable(false);
        vPx.setFocusable(false);
        JPanel vPanel = new JPanel();
        vPanel.setLayout(new BoxLayout(vPanel, BoxLayout.X_AXIS));
        vPanel.add(vM);
        vPanel.add(Box.createHorizontalStrut(20));
        vPanel.add(vPx);
        ButtonGroup vGroup = new ButtonGroup();
        vGroup.add(vM);
        vGroup.add(vPx);
        vM.setSelected(true);
        JTextField vx = new JTextField();
        JTextField vy = new JTextField();
        vx.setMaximumSize(textFieldSize);
        vy.setMaximumSize(textFieldSize);
        vx.setText("0");
        vy.setText("0");

        JLabel aLabel = new JLabel("Aceleração");
        aLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JRadioButton aM = new JRadioButton("m/s²");
        JRadioButton aPx = new JRadioButton("px/s²");
        aM.setFocusable(false);
        aPx.setFocusable(false);
        JPanel aPanel = new JPanel();
        aPanel.setLayout(new BoxLayout(aPanel, BoxLayout.X_AXIS));
        aPanel.add(aM);
        aPanel.add(Box.createHorizontalStrut(20));
        aPanel.add(aPx);
        ButtonGroup aGroup = new ButtonGroup();
        aGroup.add(aM);
        aGroup.add(aPx);
        aM.setSelected(true);
        JTextField ax = new JTextField();
        JTextField ay = new JTextField();
        ax.setMaximumSize(textFieldSize);
        ay.setMaximumSize(textFieldSize);
        ax.setText("0");
        ay.setText("0");

        JLabel scalars = new JLabel("Escalares");
        scalars.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField m = new JTextField();
        m.setMaximumSize(textFieldSize);
        m.setText(Utils.formatDouble(Charge.CHARGE_MASS).replace('.', ',').toLowerCase());

        JTextField c = new JTextField();
        c.setMaximumSize(textFieldSize);
        c.setText(Utils.formatDouble(initialCharge).replace('.', ',').toLowerCase());

        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(event -> resume(false));

        JLabel syntaxLabel = getSyntaxLabel();

        JButton addCharge = new JButton("Adicionar carga");
        addCharge.addActionListener(event -> {
            boolean error = false;
            try {
                double velX = Double.parseDouble(vx.getText().trim().replace(',', '.'));
                double velY = Double.parseDouble(vy.getText().trim().replace(',', '.'));
                Vector vel = new Vector(velX, -velY);
                if (vM.isSelected()) {
                    vel = vel.multiplyByScalar(Game.PIXELS_PER_METER);
                }

                double accelX = Double.parseDouble(ax.getText().trim().replace(',', '.'));
                double accelY = Double.parseDouble(ay.getText().trim().replace(',', '.'));
                Vector accel = new Vector(accelX, -accelY);
                if (aM.isSelected()) {
                    accel = accel.multiplyByScalar(Game.PIXELS_PER_METER);
                }

                double mass = Double.parseDouble(m.getText().trim().replace(',', '.'));
                if (mass <= 0) {
                    throw new NumberFormatException("The mass must be positive, but was " + mass);
                }
                double charge = Double.parseDouble(c.getText().trim().replace(',', '.'));

                Charge customCharge = new Charge(Utils.fixPosition(game, x, y), vel, accel, mass, charge);
                List<Charge> charges = game.getCharges();
                synchronized (charges) {
                    charges.add(customCharge);
                }
                game.setInfoTarget(customCharge);
            } catch (NumberFormatException e) {
                error = true;
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao adicionar carga", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (!error) {
                    resume(true);
                }
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.add(cancel);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(addCharge);

        addAll(
                contentPane,
                vLabel,
                vPanel,
                pair("Velocidade X", vx),
                pair("Velocidade Y", vy),
                Box.createVerticalStrut(20),
                aLabel,
                aPanel,
                pair("Aceleração X", ax),
                pair("Aceleração Y", ay),
                Box.createVerticalStrut(20),
                scalars,
                pair("Massa (kg)", m),
                pair("Carga (C)", c),
                Box.createVerticalStrut(20),
                syntaxLabel,
                Box.createVerticalStrut(20),
                buttonsPanel
        );
    }

    private static JLabel getSyntaxLabel() {
        final int width = 400;
        JLabel syntaxLabel = new JLabel("""
            <html><body><div style="width: %dpx; text-align: justify; text-justify: inter-word;">
            A sintaxe de escrita dos números em notação científica, quando necessária, \
            resume-se a usar a letra "e" (independentemente de caixa alta ou baixa) para introduzir \
            o expoente do 10, que por sua vez deve ser um inteiro. Valores devem estar no intervalo \
            [%s, %s] e os expoentes devem estar no intervalo [%s, %s] por causa da precisão de 64 bits. \
            Exemplos: 12,34·10⁻⁵⁶ ➜ 12,34e-56; -78·10⁹ ➜ -78e9; 10¹¹ ➜ 1e11.\
            </div></body></html>\
            """.formatted(width,
                        String.valueOf(Double.MIN_VALUE).toLowerCase().replace('.', ','),
                        String.valueOf(Double.MAX_VALUE).toLowerCase().replace('.', ','),
                        Double.MIN_EXPONENT,
                        Double.MAX_EXPONENT
                )
        );
        syntaxLabel.setOpaque(false);
        syntaxLabel.setFocusable(false);
        syntaxLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return syntaxLabel;
    }

    private JPanel pair(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        field.setPreferredSize(field.getMaximumSize());
        field.setMinimumSize(field.getMaximumSize());

        JLabel label = new JLabel(labelText);
        panel.add(label);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(Box.createHorizontalGlue());
        panel.add(field);

        return panel;
    }

    private void addAll(JPanel contentPane, Component... components) {
        for (Component c : components) {
            c.setFocusable(false);
            contentPane.add(c);
        }
    }

    private void resume(boolean delay) {
        dispose();
        if (resume) {
            if (delay) {
                Thread.ofVirtual().name("GameResumer").start(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("Interrupted while sleeping", e);
                    } finally {
                        game.setPaused(false);
                    }
                });
            } else {
                game.setPaused(false);
            }
        }
    }

}

package br.sergio.charges;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class Main {

    public static final JFrame frame = new JFrame("Charges");

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.warn("Could not set look and feel to system.", e);
		}

		Game game = new Game();
		ControlPanel controlPanel = new ControlPanel(game);

        game.addMouseListener(new ClickHandler(game, controlPanel));

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        Dimension gameMinSize = game.getMinimumSize();
        Dimension controlPanelMinSize = controlPanel.getMinimumSize();
        Dimension frameMinSize = new Dimension(gameMinSize.width + controlPanelMinSize.width, gameMinSize.height + controlPanelMinSize.height);

        frame.setMinimumSize(frameMinSize);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, game, controlPanel);
        splitPane.setResizeWeight(0.7); // 70% game, 30% controlPanel
        splitPane.setDividerSize(0);
        splitPane.setEnabled(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(splitPane, BorderLayout.CENTER);

		frame.add(contentPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent event) {
				game.stop();
			}

		});
        frame.addComponentListener(new ComponentAdapter() {
            
            @Override
            public void componentResized(ComponentEvent event) {
                synchronized (game.getLoopLock()) {
                    frame.revalidate();
                    controlPanel.showChargeInfo(game.getInfoTarget());
                }
            }

        });
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		Thread gameThread = new Thread(game, "GameThread");
		gameThread.setDaemon(true);
		gameThread.start();
		try {
			gameThread.join();
		} catch (Exception e) {
			log.error("Interrupted while joining game thread.", e);
		}
		System.exit(game.getExitCode());
	}

}
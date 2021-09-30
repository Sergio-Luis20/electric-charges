package ec;

import javax.swing.JFrame;

public class Main {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Cargas elétricas");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Environment environment = new Environment();
		frame.add(environment);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		environment.startThread();
		frame.setVisible(true);
	}
}

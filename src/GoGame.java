import javax.swing.*;


public class GoGame extends JFrame {
	
	public GoGame() {
		add(new GoBoard());
	}

	public static void main(String[] args) {
		JFrame frame = new GoGame();
		frame.setTitle("Go");
		frame.setSize(800, 700);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

package vectorCalc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class JumpDialogWindowTester implements ActionListener {
	
	static JumpDialogWindow s;
	
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		System.out.println(s.getSelectedMovementName());
		s.close();
	}

	public static void main(String[] args) {
		String[] categories = {"1", "Froggers", "3"};
		String[][] movementOptions =
		{{"Single Jump", "Double Jump", "Triple Jump"}, {"Vault"}, {"GP Roll", "Roll Boost"}};
		s = new JumpDialogWindow("Choose Initial Movement", categories, movementOptions);
		s.display();
		JButton confirm = s.getConfirmButton();
		confirm.addActionListener(new JumpDialogWindowTester());
	}
}

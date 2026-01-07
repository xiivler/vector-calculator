package com.vectorcalculator;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

public class CoordinateWindow implements ActionListener {
	
	String[] categories;
	String[][] movementNames;
	String title;
	
	JTextField x_field;
	JTextField y_field;
	JTextField z_field;

	JComboBox<String> categoryComboBox;
	JPanel panel;
	DefaultListModel<String> movementModel;
	JList<String> movementList;
	JScrollPane movementScrollPane;
	JButton confirm;
	JPanel confirm_panel;
	JFrame frame;

	double x;
	double y;
	double z;
	String coordinates;
	
	public CoordinateWindow(String title) {
		this.title = title;
		
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));

		x_field = new JTextField(10);
		y_field = new JTextField(10);
		z_field = new JTextField(10);
		JLabel x_label = new JLabel("X: ", JLabel.RIGHT);
		JLabel y_label = new JLabel("Y: ", JLabel.RIGHT);
		JLabel z_label = new JLabel("Z: ", JLabel.RIGHT);

		JPanel x_panel = new JPanel();
		x_panel.setBorder(new EmptyBorder(new Insets(10, 20, 0, 20)));
     	x_panel.setLayout(new BoxLayout(x_panel, BoxLayout.X_AXIS));
		x_panel.add(x_label);
		x_panel.add(x_field);

		JPanel y_panel = new JPanel();
		y_panel.setBorder(new EmptyBorder(new Insets(10, 20, 0, 20)));
     	y_panel.setLayout(new BoxLayout(y_panel, BoxLayout.X_AXIS));
		y_panel.add(y_label);
		y_panel.add(y_field);

		JPanel z_panel = new JPanel();
		z_panel.setBorder(new EmptyBorder(new Insets(10, 20, 5, 20)));
     	z_panel.setLayout(new BoxLayout(z_panel, BoxLayout.X_AXIS));
		z_panel.add(z_label);
		z_panel.add(z_field);

		confirm = new JButton("Confirm");
		confirm.setActionCommand("Confirm");
		
		confirm_panel = new JPanel();
		confirm_panel.add(confirm);

		all.add(x_panel);
		all.add(y_panel);
		all.add(z_panel);
		all.add(confirm_panel);
		
		frame = new JFrame(title);
		frame.add(all);
		frame.setSize(200, 180);
		frame.setResizable(false);
		//frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getRootPane().setDefaultButton(confirm);

		Action dispatchClosing = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				frame.dispatchEvent(
					new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
			};

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);

		JRootPane rootPane = frame.getRootPane();
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "closeWindow");
		rootPane.getActionMap().put("closeWindow", dispatchClosing); 
	}

	public static Double stringToDouble(String s) {
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException ex) {
			return 0.0;
		}
	}
	
	public void display(double x, double y, double z) {
		x_field.setText(VectorCalculator.numberToString(x));
		y_field.setText(VectorCalculator.numberToString(y));
		z_field.setText(VectorCalculator.numberToString(z));
		frame.setLocationRelativeTo(VectorCalculator.f);
		frame.getRootPane().setDefaultButton(confirm);
		frame.setVisible(true);
		x_field.requestFocus();
	}

	public String getCoordinates() {
		x = stringToDouble(x_field.getText());
		y = stringToDouble(y_field.getText());
		z = stringToDouble(z_field.getText());
		coordinates = VectorCalculator.toCoordinateString(x, y, z);
		return coordinates;
	}
	
	public JButton getConfirmButton() {
		return confirm;
	}
	
	public void close() {
		frame.dispose();
	}
	
	public void actionPerformed(ActionEvent e) {	
	}
}

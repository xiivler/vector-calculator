package com.vectorcalculator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class JumpDialogWindow implements ActionListener {
	
	String[] categories;
	String[][] movementNames;
	String title;
	
	JComboBox<String> categoryComboBox;
	JPanel categoryPanel;
	DefaultListModel<String> movementModel;
	JList<String> movementList;
	JScrollPane movementScrollPane;
	JButton confirm;
	JPanel confirmPanel;
	JFrame frame;
	
	public JumpDialogWindow(String title, String[] categories, String[][] movementNames) {
		this.categories = categories;
		this.movementNames = movementNames;
		this.title = title;
		
		categoryComboBox = new JComboBox<String>(categories);
		//categoryComboBox.setPreferredSize(new Dimension(200, categoryComboBox.getHeight()));
		categoryComboBox.addActionListener(this);
		
		categoryPanel = new JPanel();
		categoryPanel.add(categoryComboBox);
		
		movementModel = new DefaultListModel<String>();
		for (String s : movementNames[0])
			movementModel.addElement(s);
		movementList = new JList<String>(movementModel);
		movementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		movementList.setSelectedIndex(0);
		
		movementScrollPane = new JScrollPane(movementList);
		
		confirm = new JButton("Confirm");
		confirm.setActionCommand("Confirm");
		
		confirmPanel = new JPanel();
		confirmPanel.add(confirm);
		
		frame = new JFrame(title);
		frame.add(categoryPanel, BorderLayout.NORTH);
		frame.add(movementScrollPane, BorderLayout.CENTER);
		frame.add(confirmPanel, BorderLayout.SOUTH);
		frame.setSize(300, 205);
		frame.setResizable(false);
		//frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getRootPane().setDefaultButton(confirm);
	}
	
	public void display() {
		frame.setLocationRelativeTo(VectorCalculator.f);
		frame.getRootPane().setDefaultButton(confirm);
		frame.setVisible(true);
		movementList.requestFocus();
	}
	
	public JButton getConfirmButton() {
		return confirm;
	}
	
	public String getSelectedMovementName() {
		return movementList.getSelectedValue();
	}
	
	public void close() {
		frame.dispose();
	}
	
	public void actionPerformed(ActionEvent e) {
		//Debug.println(e);
		//Debug.println(categoryComboBox.getSelectedIndex());
		
		movementModel.clear();
		for (String s : movementNames[categoryComboBox.getSelectedIndex()])
			movementModel.addElement(s);
		movementList.setSelectedIndex(0);
		
	}
}

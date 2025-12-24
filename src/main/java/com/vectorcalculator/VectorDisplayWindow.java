package com.vectorcalculator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.vectorcalculator.Properties.CameraType;
import com.vectorcalculator.Properties.AngleType;

public class VectorDisplayWindow {

	static Properties p = Properties.p;

	static PrintWriter print = null;
	static boolean printTSV = true;
	static String lastPrintLine = "";
	static String currentPrintLine = "";

	static double v0;
	
	static JTable infoTable;
	static JTable dataTable;
	static DefaultTableModel dataTableModel;
	static TableModel infoTableModel;
	
	static String[] infoColumnTitles = {"Attribute", "Value"};
	static String[][] infoColumnData = {{"Initial Angle", ""}, {"Final Position", ""}, {"Horizontal Displacement", ""}, {"Vertical Displacement", ""}, {"Total Frames", ""}};
	static final int INFO_ANGLE_TYPE_ROW = 0;
	static final int FINAL_POSITION_ROW = 1;
	static final int HORIZONTAL_DISPLACEMENT_ROW = 2;
	static final int VERTICAL_DISPLACEMENT_ROW = 3;
	static final int TOTAL_FRAMES_ROW = 4;

	static final int NX_TAS = 0;
	static final int TSV_TAS = 1;
	static final int TSV_TAS_2 = 2;
	
	static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Joystick (R; θ)", "Position (X, Y, Z)", "Velocity (Vx, Vy, Vz)", "Hor. Speed (V; θ)", "Value"};
	//static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Hold Angle", "X", "Y", "Z", "Vx", "Vy", "Vz", "Horizontal Speed"};
	
	static JFrame frame;

	static ArrayList<Inputs> inputs;

	static JTextField scriptPathField;
	static String scriptPath = "";
	static File scriptFile = new File(scriptPath);

	static JComboBox scriptTypeComboBox;
	static JButton create;
	static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	static SimpleMotion[] simpleMotions;
	static double initialAngle;
	static double targetAngle;

	static boolean shiftMotion = false; //for newer mods where motion inputs have to be 1f earlier

	static double cameraAngle;
	
	static {
		
		//INFO TABLE
		
		infoTable = new JTable(infoColumnData, infoColumnTitles) {
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		infoTableModel = infoTable.getModel();
		
		infoTable.setFillsViewportHeight(true);
		infoTable.getTableHeader().setFont(VectorCalculator.tableFont);
		infoTable.setFont(VectorCalculator.tableFont);
		infoTable.setRowHeight(infoTable.getRowHeight() + 2);
		infoTable.setColumnSelectionAllowed(true);
		infoTable.getTableHeader().setReorderingAllowed(false);
		
		infoTable.getColumnModel().getColumn(0).setMinWidth(260);
		infoTable.getColumnModel().getColumn(0).setMaxWidth(260);
		
		JScrollPane infoScrollPane = new JScrollPane(infoTable);
		infoScrollPane.setPreferredSize(new Dimension(500, 115));
		
		
		//DATA TABLE
		
		dataTableModel = new DefaultTableModel(0, 5);
		dataTableModel.setColumnIdentifiers(dataColumnTitles);
		JTable dataTable = new JTable(dataTableModel) {
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		dataTable.setFillsViewportHeight(true);
		dataTable.getTableHeader().setFont(VectorCalculator.tableFont);
		dataTable.setFont(VectorCalculator.tableFont);
		dataTable.setRowHeight(dataTable.getRowHeight() + 2);
		dataTable.setColumnSelectionAllowed(true);
		
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		dataTable.getColumnModel().getColumn(2).setPreferredWidth(160);
		dataTable.getColumnModel().getColumn(3).setPreferredWidth(240);
		dataTable.getColumnModel().getColumn(4).setPreferredWidth(400);
		dataTable.getColumnModel().getColumn(5).setPreferredWidth(360);
		dataTable.getColumnModel().getColumn(6).setPreferredWidth(240);
		dataTable.getColumnModel().getColumn(7).setPreferredWidth(160);
		
		JScrollPane dataScrollPane = new JScrollPane(dataTable);

		//EXPORT SETTINGS

		JPanel export = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel scriptTypeLabel = new JLabel("Script Format: ", JLabel.RIGHT);
		scriptTypeComboBox = new JComboBox<String>(new String[]{"nx-TAS", "TSV-TAS (Practice Mod)", "TSV-TAS (Lunakit)"});
		scriptTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (scriptTypeComboBox.getSelectedIndex() == TSV_TAS_2) {
					setShiftMotion(true);
				}
				else {
					setShiftMotion(false);
				}
			}
		});
		JLabel exportLabel = new JLabel("Script Path: ", JLabel.RIGHT);
		JButton browse = new JButton("Browse");
		browse.setActionCommand("browse");
		create = new JButton("Create");
		create.setActionCommand("export");
		create.setEnabled(false);
		JButton copy = new JButton("Copy to Clipboard");
		copy.setActionCommand("clipboard");
		scriptPathField = new JTextField(20);
		scriptPathField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				validatePath();
			}

			public void removeUpdate(DocumentEvent e) {
				validatePath();
			}

			public void insertUpdate(DocumentEvent e) {
				validatePath();
			}
		});

		export.add(scriptTypeLabel);
		export.add(scriptTypeComboBox);
		export.add(exportLabel);
		export.add(scriptPathField);
		export.add(browse);
		export.add(create);
		export.add(copy);

		ButtonListener buttonListen = new ButtonListener();
		browse.addActionListener(buttonListen);
		create.addActionListener(buttonListen);
		copy.addActionListener(buttonListen);
		
		frame = new JFrame("Calculations: " + VectorCalculator.projectName);
		frame.add(infoScrollPane, BorderLayout.NORTH);
		frame.add(dataScrollPane, BorderLayout.CENTER);
		frame.add(export, BorderLayout.SOUTH);
		frame.setSize(1160, 600);
	}
	
	private static void validatePath() {
		scriptPath = scriptPathField.getText();
		scriptFile = new File(scriptPath);
		if (!scriptFile.isDirectory() && scriptFile.getParentFile() != null && scriptFile.getParentFile().isDirectory()) {
			Debug.println("Valid path");
			Paths.get(scriptPath);
			create.setEnabled(true);
			if (scriptFile.exists()) {
				create.setText("Append");
			}
			else {
				create.setText("Create");
			}
		}
		else {
			create.setEnabled(false);
			Debug.println("Invalid path");
		}
	}

	private static void setShiftMotion(boolean b) {
		if (shiftMotion != b) {
			shiftMotion = b;
			generateData(simpleMotions, initialAngle, targetAngle);
		}
	}

	private static String toCoordinates(double x, double y, double z) {
		return String.format("(%.3f, %.3f, %.3f)", x, y, z);
	}
	
	private static String toVelocityVector(double vx, double vy, double vz) {
		return String.format("(%.3f, %.3f, %.3f)", vx, vy, vz);
	}

	private static String toPolarCoordinates(double r, double theta) {
		return String.format("(%.3f; %.3f)", r, theta);
	}

	private static String toPolarCoordinatesJoystick(double r, double theta) {
		if (theta == SimpleMotion.NO_ANGLE) {
			return "";
		}
		if (r == 1) {
			return String.format("(1; %.4f)", theta);
		}
		else {
			return String.format("(%.2f; %.4f)", r, theta);
		}
	}
	
	private static String shorten(double d, int decimalPlaces) {
		String format = "%." + decimalPlaces + "f";
		return String.format(format, d);
		
		/*
		int trimIndex = s.length() - 1;
		while (s.charAt(trimIndex) == '0')
			trimIndex--;
		if (s.charAt(trimIndex) != '.')
			trimIndex++;
		return s.substring(0, trimIndex);
		*/
	}
	
	private static double reduceAngle(double angle) {
		double d = Math.toDegrees(angle);
		while (d >= 360)
			d -= 360;
		while (d < 0)
			d += 360;
		return d;
	}
	
	public static void clearDataTable() {
		dataTableModel.setRowCount(0);
	}
	
	public static void generateData(SimpleMotion[] simpleMotions, double initialAngle, double targetAngle) {
		frame.setTitle("Calculations: " + VectorCalculator.projectName);

		VectorDisplayWindow.simpleMotions = simpleMotions;
		VectorDisplayWindow.initialAngle = initialAngle;
		VectorDisplayWindow.targetAngle = targetAngle;
		if (p.cameraType == CameraType.ABSOLUTE) {
			cameraAngle = Math.PI / 2;
		}
		else {
			if (p.cameraType == CameraType.INITIAL) {
				cameraAngle = initialAngle;
			}
			else if (p.cameraType == CameraType.TARGET) {
				cameraAngle = targetAngle;
			}
			else {
				cameraAngle = Math.toRadians(p.customCameraAngle);
			}
			if (p.xAxisZeroDegrees) {
				cameraAngle = Math.PI / 2 - cameraAngle;
			}
		}
		
		v0 = p.initialHorizontalSpeed;
		
		clearDataTable();
		
		dataTableModel.addRow(new Object[] {0, "", "", "", toCoordinates(p.x0, p.y0, p.z0), toVelocityVector(v0 * Math.cos(initialAngle), 0, v0 * Math.sin(initialAngle)), toPolarCoordinates(v0, reduceAngle(initialAngle))});
		
		double x = p.x0;
		double y = p.y0;
		double z = p.z0;
		
		double[][] info = null;

		inputs = new ArrayList<Inputs>();
		inputs.add(new Inputs());

		int row = 1;
		boolean firstDive = true;
		for (int index = 0; index < simpleMotions.length; index++) {
			SimpleMotion motion = simpleMotions[index];
			if (motion.frames == 0) {
				continue;
			}
			if (motion.movement.movementType.equals("Dive") && firstDive) {
				firstDive = false;
				for (int i = 0; i < motion.frames - 1; i++) {
					motion.movement.inputs1.add(Inputs.Y);
				}
			}
			motion.calcDisp();
			motion.setInitialCoordinates(x, y, z);
			info = motion.calcFrameByFrame();
			//for (double[] ds : info)
			//	Debug.println(Arrays.toString(ds));
			int startRow = row;
			for (int i = 0; i < info.length; i++, row++) {
				double theta = SimpleMotion.NO_ANGLE;
				if (info[i][7] != SimpleMotion.NO_ANGLE) {
					theta = reduceAngle(info[i][7] - cameraAngle + Math.PI / 2);
				}

				Object[] rowContents = new Object[8];
				rowContents[0] = row;
				rowContents[1] = "";
				rowContents[2] = "";
				rowContents[3] = toPolarCoordinatesJoystick(info[i][8], theta);
				rowContents[4] = toCoordinates(info[i][0], info[i][1], info[i][2]);
				rowContents[5] = toVelocityVector(info[i][3], info[i][4], info[i][5]);
				double velocityAngle;
				if (p.xAxisZeroDegrees) {
					velocityAngle = reduceAngle(Math.atan2(info[i][5], info[i][3]));
				}
				else {
					velocityAngle = reduceAngle(Math.atan2(info[i][3], info[i][5]));
				}
				if (info[i][6] == 0) {
					if (p.xAxisZeroDegrees) {
						rowContents[6] = toPolarCoordinates(info[i][6], reduceAngle(Math.PI / 2 - motion.initialAngle));
					}
					else {
						rowContents[6] = toPolarCoordinates(info[i][6], reduceAngle(motion.initialAngle));
					}
				}
				else {
					rowContents[6] = toPolarCoordinates(info[i][6], velocityAngle);
				}
				if (info[i][4] < 0) { //how efficient the jump is
					double speedInTargetDirection = info[i][6] * Math.cos(Math.atan2(info[i][3], info[i][5]) - targetAngle);
					double value = -1 / ((info[i][4] / speedInTargetDirection) - 1);
					rowContents[7] = String.format("%.3f", value);
				}
				
				dataTableModel.addRow(rowContents);

				//configure the Inputs array
				inputs.add(new Inputs(info[i][8], theta));
				if (i < motion.movement.inputs1.size()) {
					int offset = -1;
					int input1 = motion.movement.inputs1.get(i);
					if (input1 >= Inputs.M && shiftMotion) {
						offset = -2;
					}
					if (row + offset >= 0) {
						if (inputs.get(row + offset).input1 == Inputs.NONE) {
							inputs.get(row + offset).input1 = input1;
						}
						else {
							inputs.get(row + offset).input2 = input1;
						}
					}
				}
				if (i < motion.movement.inputs2.size()) {
					int offset = -1;
					int input2 = motion.movement.inputs2.get(i);
					if (input2 >= Inputs.M && shiftMotion) {
						offset = -2;
					}
					if (row + offset >= 0) {
						if (inputs.get(row + offset).input1 == Inputs.NONE) {
							inputs.get(row + offset).input1 = input2;
						}
						else {
							inputs.get(row + offset).input2 = input2;
						}
					}
				}
			}
			x = info[info.length - 1][0];
			y = info[info.length - 1][1];
			z = info[info.length - 1][2];
			Debug.println(motion.movement.displayName);
			dataTableModel.setValueAt(motion.movement.displayName, startRow, 1);
		}

		//display the inputs
		for (int i = 0; i < row; i++) {
			int input1 = inputs.get(i).input1;
			int input2 = inputs.get(i).input2;
			String displayString = "";
			if (input1 != Inputs.NONE) {
				displayString += Inputs.displayInputs[input1];
				if (input2 != Inputs.NONE) {
					displayString += ", " + Inputs.displayInputs[input2];
				}
			}
			dataTableModel.setValueAt(displayString, i, 2);
		}
	
		if (p.angleType == AngleType.TARGET) {
			infoTableModel.setValueAt("Initial Angle", INFO_ANGLE_TYPE_ROW, 0);
			infoTableModel.setValueAt(shorten(reduceAngle(initialAngle), 4), INFO_ANGLE_TYPE_ROW, 1);
		}
		else {
			infoTableModel.setValueAt("Target Angle", INFO_ANGLE_TYPE_ROW, 0);
			infoTableModel.setValueAt(shorten(reduceAngle(targetAngle), 4), INFO_ANGLE_TYPE_ROW, 1);
		}
		infoTableModel.setValueAt(toCoordinates(x, y, z), FINAL_POSITION_ROW, 1);
		infoTableModel.setValueAt(shorten(Math.sqrt(Math.pow(x - p.x0, 2) + Math.pow(z - p.z0, 2)), 3), HORIZONTAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt(shorten(y - p.y0, 3), VERTICAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt("" + (row - 1), TOTAL_FRAMES_ROW, 1);
	}
	
	public static void display() {
		frame.setVisible(true);
	}

	public static void generateTSVTAS(boolean toClipboard) {
		
		String clipboardString = "";

		if (!toClipboard) {
			try {
				print = new PrintWriter(new FileOutputStream(scriptFile, true));
				print.println("\\\\\tOptimized using Vector Calculator");
			}
			catch (FileNotFoundException e) {
				return;
			}
		}

		Inputs currentInputs = inputs.get(0);
		Inputs oldInputs = currentInputs;
		int identicalLineCount = 1;
		
		for (int i = 1; i < inputs.size(); i++) {
			currentInputs = inputs.get(i);
			if (currentInputs.equals(oldInputs)) {
				identicalLineCount++;
			}
			else {
				String line = identicalLineCount + "\t" + oldInputs.toTSV();
				if (toClipboard) {
					clipboardString += line + "\n";
				}
				else {
					print.println(line);
				}
				identicalLineCount = 1;
				oldInputs = currentInputs;
			}
		}
		String line = identicalLineCount + "\t" + currentInputs.toTSV();
		if (toClipboard) {
			clipboardString += line;
			clipboard.setContents(new StringSelection(clipboardString), null);
		}
		else {
			print.println(line);
			print.close();
		}
	}

	public static void generateNXTAS(boolean toClipboard) {
		
		int startLine = 0;

		String clipboardString = "";

		if (!toClipboard) {
			try {
				if (scriptFile.exists()) {
					Scanner read = new Scanner(scriptFile);
					String line = "";
					while (read.hasNextLine()) {
						line = read.nextLine();
					}
					Scanner readToken = new Scanner(line);
					if (readToken.hasNextInt()) {
						startLine = readToken.nextInt() + 1;
					}
					read.close();
					readToken.close();
				}
				print = new PrintWriter(new FileOutputStream(scriptFile, true));
			}
			catch (FileNotFoundException e) {
				return;
			}
		}
		
		for (int i = 0; i < inputs.size(); i++) {
			String line = (i + startLine) + " " + inputs.get(i).toNXTAS();
			if (toClipboard) {
				clipboardString += line;
				if (i < inputs.size() - 1) {
					clipboardString += "\n";
				}
			}
			else {
				print.println(line);
			}
		}
		if (toClipboard) {
			clipboard.setContents(new StringSelection(clipboardString), null);
		}
		else {
			print.close();
		}
	}

	static class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String com = evt.getActionCommand();

			if (com.equals("browse")) {
				JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				j.setDialogTitle("Choose Script Location");
				
				if (scriptPathField.getText().length() > 0)
					j.setSelectedFile(new File(scriptPathField.getText()));

				j.setDialogType(JFileChooser.SAVE_DIALOG);
				if (j.showDialog(null, "OK") == JFileChooser.APPROVE_OPTION) {
					scriptPathField.setText(j.getSelectedFile().getAbsolutePath());
				}
			}
			else if (com.equals("export")) {
				Debug.println("Export file");
				if (scriptTypeComboBox.getSelectedIndex() == TSV_TAS) {
					generateTSVTAS(false);
				}
				else if (scriptTypeComboBox.getSelectedIndex() == TSV_TAS_2) {
					generateTSVTAS(false);
				}
				else {
					generateNXTAS(false);
				}
			}
			else if (com.equals("clipboard")) {
				Debug.println("Copy to clipboard");
				if (scriptTypeComboBox.getSelectedIndex() == TSV_TAS) {
					generateTSVTAS(true);
				}
				else if (scriptTypeComboBox.getSelectedIndex() == TSV_TAS_2) {
					generateTSVTAS(true);
				}
				else {
					generateNXTAS(true);
				}
			}

			validatePath();
		}
	}
}

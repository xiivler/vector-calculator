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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import com.vectorcalculator.Properties.GroundType;
import com.vectorcalculator.Properties.Mode;
//import com.vectorcalculator.Properties.AngleType;

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
	static String[][] infoColumnData = {{"Initial Angle", ""}, {"Target Angle", ""}, {"Initial Joystick Angle", ""}, {"Initial Facing Angle", ""}, {"Final Position", ""}, {"Horizontal Displacement", ""}, {"Ledge Horizontal Displacement", ""}, {"Vertical Displacement", ""}, {"Total Frames", ""}, {"Made Jump", ""}};
	static final int INITIAL_ANGLE_ROW = 0;
	static final int TARGET_ANGLE_ROW = 1;
	static final int INITIAL_JOYSTICK_ANGLE_ROW = 2;
	static final int INITIAL_FACING_ANGLE_ROW = 3;
	static final int FINAL_POSITION_ROW = 4;
	static final int HORIZONTAL_DISPLACEMENT_ROW = 5;
	static final int LEDGE_HORIZONTAL_DISPLACEMENT_ROW = 6;
	static final int VERTICAL_DISPLACEMENT_ROW = 7;
	static final int TOTAL_FRAMES_ROW = 8;
	static final int MADE_JUMP_ROW = 9;

	static final int NX_TAS = 0;
	static final int TSV_TAS = 1;
	static final int TSV_TAS_2 = 2;
	
	static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Joystick (R; θ)", "Position (X, Y, Z)", "Velocity (Vx, Vy, Vz)", "Hor. Speed (V; θ)", "Facing Angle", "Value"};
	//static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Hold Angle", "X", "Y", "Z", "Vx", "Vy", "Vz", "Horizontal Speed"};
	
	static JFrame frame;

	static ArrayList<Inputs> inputs;

	static JTextField scriptPathField;
	static String scriptPath = "";
	static File scriptFile = new File(scriptPath);

	static JComboBox scriptTypeComboBox;
	static JButton create;
	static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	static VectorMaximizer maximizer;
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
		infoTable.setShowGrid(false);
		
		infoTable.getColumnModel().getColumn(0).setMinWidth(260);
		infoTable.getColumnModel().getColumn(0).setMaxWidth(260);
		
		JScrollPane infoScrollPane = new JScrollPane(infoTable);
		infoScrollPane.setPreferredSize(new Dimension(500, infoTable.getRowHeight() * (infoTable.getRowCount() + 1) + 8));
		
		
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
		dataTable.setShowGrid(false);
		
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		dataTable.getColumnModel().getColumn(2).setPreferredWidth(160);
		dataTable.getColumnModel().getColumn(3).setPreferredWidth(200);
		dataTable.getColumnModel().getColumn(4).setPreferredWidth(400);
		dataTable.getColumnModel().getColumn(5).setPreferredWidth(360);
		dataTable.getColumnModel().getColumn(6).setPreferredWidth(260);
		dataTable.getColumnModel().getColumn(7).setPreferredWidth(170);
		dataTable.getColumnModel().getColumn(8).setPreferredWidth(100);
		
		JScrollPane dataScrollPane = new JScrollPane(dataTable);

		//EXPORT SETTINGS

		JPanel export = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel scriptTypeLabel = new JLabel("Script Format: ", JLabel.RIGHT);
		scriptTypeComboBox = new JComboBox<String>(new String[]{"nx-TAS", "TSV-TAS (Practice Mod)", "TSV-TAS (Lunakit)"});
		scriptTypeComboBox.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (index == NX_TAS && Properties.p_calculated != null && Properties.p_calculated.twoPlayerMode) {
					c.setEnabled(false);
				}
				return c;
			}
		});
		scriptTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (scriptTypeComboBox.getSelectedIndex() == NX_TAS && Properties.p_calculated != null && Properties.p_calculated.twoPlayerMode) {
					scriptTypeComboBox.setSelectedIndex(p.scriptType);
					return;
				}
				p.scriptType = scriptTypeComboBox.getSelectedIndex();
				if (p.scriptType == TSV_TAS_2) {
					setShiftMotion(true);
				}
				else {
					setShiftMotion(false);
				}
				VectorCalculator.checkIfSaved(true);
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
		frame.setSize(1250, 700);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowOpened(java.awt.event.WindowEvent e) {
				MainJMenuBar.updateCalculatorMenuItems();
			}
			public void windowClosed(java.awt.event.WindowEvent e) {
				MainJMenuBar.updateCalculatorMenuItems();
			}
		});
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
			p.scriptPath = scriptPath;
			VectorCalculator.checkIfSaved(false);
		}
		else {
			create.setEnabled(false);
			Debug.println("Invalid path");
		}
	}

	private static void setShiftMotion(boolean b) {
		if (shiftMotion != b) {
			shiftMotion = b;
			generateData(maximizer);
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

	private static String toPolarCoordinatesJoystick(double r, double theta, int precision) {
		if (theta == SimpleMotion.NO_ANGLE) {
			return "";
		}
		if (r == 1) {
			return String.format("(1; %." + precision + "f)", theta);
		}
		else {
			return String.format("(%.2f; %." + precision + "f)", r, theta);
		}
	}
	
	private static String shorten(double d, int decimalPlaces) {
		String format = "%." + decimalPlaces + "f";
		return String.format(format, d);
	}
	
	public static double reduceAngle(double angle) {
		double d = Math.toDegrees(angle);
		while (d >= 360)
			d -= 360;
		while (d < 0)
			d += 360;
		return d;
	}

	public static double reduceAngleRad(double angle) {
		while (angle >= Math.PI * 2)
			angle -= Math.PI * 2;
		while (angle < 0)
			angle += Math.PI * 2;
		return angle;
	}
	
	public static void clearDataTable() {
		dataTableModel.setRowCount(0);
	}

	public static void refresh() {
		if (frame == null || !frame.isVisible() || maximizer == null) {
			return;
		}
		generateData(maximizer);
	}

	public static void refreshAngle(double adjustment) { //TODO also update top stuff
		if (frame == null || !frame.isVisible() || maximizer == null) {
			return;
		}
		maximizer.adjustBy(adjustment);
		generateData(maximizer);
	}

	public static void initialize() {
		//updateScriptTypeAvailability();
		shiftMotion = (p.scriptType == TSV_TAS_2);
		scriptPath = p.scriptPath;
		scriptPathField.setText(p.scriptPath);
		scriptFile = new File(p.scriptPath);
		scriptTypeComboBox.setSelectedIndex(p.scriptType);
	}

	//disallows nx-TAS while in two-player mode, switching to Lunakit format if it was selected
	public static void updateScriptTypeAvailability() {
		if (p.twoPlayerMode && p.scriptType == NX_TAS) {
			p.scriptType = TSV_TAS_2;
			scriptTypeComboBox.setSelectedIndex(p.scriptType);
			shiftMotion = true;
		}
		scriptTypeComboBox.repaint();
	}
	
	public static void generateData(VectorMaximizer maximizer) {
		frame.setTitle("Calculations: " + VectorCalculator.projectName);

		boolean madeJump = false;
		int finalCapThrowFrame = -1;

		VectorDisplayWindow.maximizer = maximizer;
		VectorDisplayWindow.simpleMotions = maximizer.getMotions();
		double initialAngle = maximizer.getInitialAngle();
		double targetAngle = maximizer.getTargetAngle();
		double initialAngleAbsolute = maximizer.initialAngle;
		double targetAngleAbsolute = maximizer.targetAngle;
		if (p.cameraType == CameraType.ABSOLUTE) {
			cameraAngle = Math.PI;
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
		
		dataTableModel.addRow(new Object[] {0, "", "", "", toCoordinates(p.x0, p.y0, p.z0), toVelocityVector(v0 * Math.sin(initialAngleAbsolute), 0, v0 * Math.cos(initialAngleAbsolute)), toPolarCoordinates(v0, reduceAngle(initialAngle)), String.format("%.3f", reduceAngle(maximizer.initialRotation))});
		
		double x = p.x0;
		double y = p.y0;
		double z = p.z0;

		double targetXDisp = p.x1 - p.x0;
		double targetZDisp = p.z1 - p.z0;
		double targetDisp = Math.sqrt(targetXDisp * targetXDisp + targetZDisp * targetZDisp);
		
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
				if (!p.twoPlayerMode) {
					for (int i = 0; i < motion.frames - 1; i++) {
						motion.movement.inputs1.add(Inputs.Y);
					}
				}
			}
			else if (motion.movement.movementType.equals("Homing Triple Throw") && motion.frames >= 24 && simpleMotions[index + 1].frames >= 6) { //home later
				ArrayList<Integer> inputs = motion.movement.inputs1;
				Debug.println("Wah");
				inputs.clear();
				inputs.add(Inputs.MU);
				for (int i = 0; i < 8; i++) {
					inputs.add(Inputs.NONE);
				}
				inputs.add(Inputs.MD);
			}
			else if (motion.movement.movementType.contains("(No Vector)")) { //crouch rolls and roll boosts where Mario stays in the roll as long as possible
				for (int i = 1; i < motion.frames - 10; i++) {
					motion.movement.inputs1.add(Inputs.ZL);
				}
			}
			else if (motion.movement.movementType.equals("Fake Throw")) {
				boolean cbBefore = false;
				for (int j = 0; j < index; j++) {
					if (Movement.isCapBounce(simpleMotions[j].movement.movementType))
						cbBefore = true;
				}
				if (cbBefore) {
					ArrayList<Integer> inputs = new ArrayList<Integer>();
					int duration = motion.frames;
					if (simpleMotions[index + 1].movement.movementType.equals("Falling"))
						duration += simpleMotions[index + 1].frames;
					int framesWait = Math.max(2, 31 - duration); //make sure to press B early, earlier if the cap throw is very short
					inputs.add(Inputs.P2B);
					for (int i = 0; i < framesWait; i++) {
						inputs.add(Inputs.NONE);
					}
					inputs.add(Inputs.Y);
					motion.movement.inputs1 = inputs;
					motion.movement.inputOffset = -2 - framesWait;
				}
				else {
					motion.movement.inputs1.add(Inputs.P2Y); //this button press returns cappy ASAP
					inputs.get(row - 1).P2_r = 1;
					inputs.get(row - 1).P2_theta = reduceAngle(targetAngleAbsolute - cameraAngle + Math.PI / 2);
				}
			}
			else if (index == maximizer.variableMovement2Index && maximizer.hasVariableCapThrow2) {
				finalCapThrowFrame = row;
			}
			else if (motion.movement.movementType.equals("Reverse Bonk")) {
				double[] cappyPos = maximizer.getFinalCapThrowPosition();
				double[] marioPos = {simpleMotions[index - 1].dispX, simpleMotions[index - 1].dispY, simpleMotions[index - 1].dispZ};

				double reverseBonkDistance = 120; //how far cappy should be from Mario

				double reverseBonkAngle = simpleMotions[index].initialAngle;

				double[] cappyTarget = {marioPos[0] + reverseBonkDistance * Math.sin(reverseBonkAngle + Math.PI), marioPos[1], marioPos[2] + reverseBonkDistance * Math.cos(reverseBonkAngle + Math.PI)}; //where cappy needs to move to
				
				double direction = Math.atan2(cappyTarget[0] - cappyPos[0], cappyTarget[2] - cappyPos[2]); //angle cappy needs to move in
				double cappyJoystickTheta = reduceAngle(direction - cameraAngle + Math.PI / 2);
				
				double hDistance = Math.hypot(cappyTarget[0] - cappyPos[0], cappyTarget[2] - cappyPos[2]); //distance cappy needs to cover
				double cappySpeed = Movement.isTT(p.fctType) ? Movement.CAPPY_FAST_SPEED : Movement.CAPPY_SPEED;
				double trueFrames = hDistance / cappySpeed;
				int frames = (int) Math.ceil(trueFrames);
				double cappyJoystickRadius = trueFrames / frames;

				int framesJump = 0; //how many frames cappy should attempt to jump
				if (cappyPos[1] < cappyTarget[1]) {
					framesJump = (int) Math.ceil((cappyTarget[1] - cappyPos[1]) / Movement.CAPPY_JUMP_V_SPEED);
				}

				int startFrame = finalCapThrowFrame + Movement.CT_FRAMES_UNTIL_FULLY_THROWN[p.fctType] - 1; //assumes motion throw
				//System.out.println(p.fctType);

				for (int i = 0; i < frames; i++) {
					inputs.get(startFrame + i).P2_r = cappyJoystickRadius;
					inputs.get(startFrame + i).P2_theta = cappyJoystickTheta;
				}

				int cappyGPRow = row - Movement.CAPPY_GP_FRAMES;
				Inputs GPFrameInputs = inputs.get(cappyGPRow);
				if (GPFrameInputs.input1 == Inputs.NONE)
					GPFrameInputs.input1 = Inputs.P2ZL;
				else
					GPFrameInputs.input2 = Inputs.P2ZL;

				if (inputs.get(cappyGPRow - 1).P2_theta == SimpleMotion.NO_ANGLE && framesJump > 0) { //cappy can jump to get closer to target height
					if (GPFrameInputs.input2 == Inputs.NONE)
						GPFrameInputs.input2 = Inputs.P2B;
					int curRow = cappyGPRow - 1;
					int framesJumpAchieved = 0;
					while (inputs.get(curRow).P2_theta == SimpleMotion.NO_ANGLE && framesJumpAchieved < framesJump) {
						if (inputs.get(curRow).input1 == Inputs.NONE)
							inputs.get(curRow).input1 = Inputs.P2B;
						else
							inputs.get(curRow).input2 = Inputs.P2B;
						framesJumpAchieved++;
						curRow--;
					}
				}

				// System.out.println("Cappy Position: " + Arrays.toString(cappyPos));
				// System.out.println("Mario Position: " + Arrays.toString(marioPos));
				// System.out.println("Cappy Target: " + Arrays.toString(cappyTarget));
				// System.out.println("Cappy Joystick: (" + cappyJoystickRadius + "; " + cappyJoystickTheta + ")");
				// System.out.println("Horizontal Distance: " + hDistance);
				// System.out.println("Movement Frames: " + frames);
				// System.out.println("GP Frame: " + (row - Movement.CAPPY_GP_FRAMES));
			}
			motion.calcDisp();
			motion.setInitialCoordinates(x, y, z);
			info = motion.calcFrameByFrame();
			//for (double[] ds : info)
			//	Debug.println(Arrays.toString(ds));
			int startRow = row;
			double upwarpOffset = 0;

			for (int i = 0; i < info.length; i++, row++) {
				Object[] rowContents = new Object[9];
				rowContents[0] = row;
				rowContents[1] = "";
				rowContents[2] = "";

				x = info[i][0];
				y = info[i][1] + upwarpOffset;
				z = info[i][2];
				double dispX = p.x0 - x;
				double dispZ = p.z0 - z;
				if (y + p.getUpwarpMinusError() >= p.y1 && (!p.targetCoordinatesGiven || Math.sqrt(dispX * dispX + dispZ * dispZ) > targetDisp)) {
					if (!madeJump && p.targetCoordinatesGiven) {
						madeJump = true;
						rowContents[1] = "(Made Jump)";
					}
					if (y < p.y1 && !firstDive && index == simpleMotions.length - 1) {
						if (p.targetCoordinatesGiven || i == info.length - 1) {
							upwarpOffset = p.y1 - info[i][1];
							y = p.y1;
							if (!p.targetCoordinatesGiven) {
								rowContents[1] = "(Upwarp)";
							}
						}
					}
				}
				if (i == info.length - 1) {
					if (p.groundTypeFirstGP == GroundType.GROUND && firstDive && !motion.movement.movementType.equals("Moonwalk") && !motion.movement.movementType.equals("Coyote Time")) {
						if (y < p.groundHeightFirstGP) {
							y = p.groundHeightFirstGP;
							rowContents[1] = "(Hit Ground)";
						}
					}
					else if (p.groundTypeSecondGP == GroundType.GROUND && Movement.isCapBounce(motion.movement.movementType)) {
						if (y < p.groundHeightSecondGP) {
							y = p.groundHeightSecondGP;
							rowContents[1] = "(Hit Ground)";
						}
					}
					else if (maximizer.hasRainbowSpin && index == maximizer.rainbowSpinIndex + 1) {
						double groundHeightRS = -Double.MAX_VALUE;
						if (index <= maximizer.variableCapThrow1Index && p.groundTypeFirstGP == GroundType.GROUND)
							groundHeightRS = p.groundHeightFirstGP;
						else if (p.groundTypeSecondGP == GroundType.GROUND)
							groundHeightRS = p.groundHeightSecondGP;
						if (y < groundHeightRS) {
							y = groundHeightRS;
							rowContents[1] = "(Hit Ground)";
						}
					}
				}

				double theta = SimpleMotion.NO_ANGLE;
				if (info[i][7] != SimpleMotion.NO_ANGLE) {
					theta = reduceAngle(info[i][7] - cameraAngle + Math.PI / 2);
				}

				rowContents[3] = toPolarCoordinatesJoystick(info[i][8], theta, 3);
				rowContents[4] = toCoordinates(x, y, z);
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
				rowContents[7] = String.format("%.3f", reduceAngle(info[i][9]));
				if (info[i][4] < 0) { //how efficient the jump is
					double speedInTargetDirection = info[i][6] * Math.cos(Math.atan2(info[i][3], info[i][5]) - targetAngleAbsolute);
					double value = -1 / ((info[i][4] / speedInTargetDirection) - 1);
					rowContents[8] = String.format("%.3f", value);
				}
				
				dataTableModel.addRow(rowContents);

				//configure the Inputs array
				if (row == 1 && p.framesRun > 0) {
					inputs.remove(0);
					inputs.add(new Inputs(info[i][8], theta, info[i][7]));
				}
				inputs.add(new Inputs(info[i][8], theta, info[i][7]));
				if (i < motion.movement.inputs1.size()) {
					int offset = motion.movement.inputOffset;
					int input1 = motion.movement.inputs1.get(i);
					if (index == maximizer.variableCapThrow1Index) {
						if (p.mode != Mode.CALCULATE && !p.twoPlayerMode) {
							input1 = Movement.CT_INPUT[maximizer.ctType];
							motion.movement.displayName = Movement.CT_NAMES[maximizer.ctType];
						}
					}
					if (Inputs.isMotion(input1) && shiftMotion) {
						offset--;
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
					int offset = motion.movement.inputOffset;
					int input2 = motion.movement.inputs2.get(i);
					if (Inputs.isMotion(input2) && shiftMotion) {
						offset--;
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

			Debug.println(motion.movement.displayName);
			if (!motion.movement.displayName.equals(""))
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
	
		infoTableModel.setValueAt(shorten(reduceAngle(initialAngle), 4), INITIAL_ANGLE_ROW, 1);
		infoTableModel.setValueAt(shorten(reduceAngle(initialAngleAbsolute - cameraAngle + Math.PI / 2), 4), INITIAL_JOYSTICK_ANGLE_ROW, 1);
		infoTableModel.setValueAt(shorten(reduceAngle(maximizer.initialRotation), 4), INITIAL_FACING_ANGLE_ROW, 1);
		infoTableModel.setValueAt(shorten(reduceAngle(targetAngle), 4), TARGET_ANGLE_ROW, 1);
		infoTableModel.setValueAt(toCoordinates(x, y, z), FINAL_POSITION_ROW, 1);
		double horizontalDisp = Math.sqrt((x - p.x0) * (x - p.x0) + (z - p.z0) * (z - p.z0));
		double initialRotationDiff = reduceAngleRad(targetAngleAbsolute + Math.PI - maximizer.initialRotation);
		double initialLedgeDisp = 30 * Math.cos((initialRotationDiff + Math.PI / 3) % (2.0 / 3.0 * Math.PI) - Math.PI / 3);
		double finalRotationDiff = reduceAngleRad(targetAngleAbsolute - maximizer.finalRotation);
		double finalLedgeDisp = 30 * Math.cos((finalRotationDiff + Math.PI / 3) % (2.0 / 3.0 * Math.PI) - Math.PI / 3);
		double trueHorizontalDisp = horizontalDisp + initialLedgeDisp + finalLedgeDisp;
		infoTableModel.setValueAt(shorten(horizontalDisp, 3), HORIZONTAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt(shorten(trueHorizontalDisp, 3), LEDGE_HORIZONTAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt(shorten(y - p.y0, 3), VERTICAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt("" + (row - 1), TOTAL_FRAMES_ROW, 1);
		if (!p.targetCoordinatesGiven)
			infoTableModel.setValueAt("N/A", MADE_JUMP_ROW, 1);
		else if (madeJump)
			infoTableModel.setValueAt("Yes", MADE_JUMP_ROW, 1);
		else
			infoTableModel.setValueAt("No", MADE_JUMP_ROW, 1);

		updateScriptTypeAvailability();
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

package com.vectorcalculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
//import javax.swing.dataTable.DefaultTableModel;
//import javax.swing.dataTable.TableCellEditor;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import com.vectorcalculator.Properties.CameraType;
import com.vectorcalculator.Properties.AngleType;

public class VectorCalculator extends JPanel {
	
	static Properties p = Properties.getInstance();
	static boolean stop = false;
	static boolean saved = true;
	static boolean initialized = false;

	static String projectName = "Untitled Project";

	static Font tableFont = new Font("Verdana", Font.PLAIN, 14);

	static String jarParentFolder;
	static File userDefaults;
	static File factoryDefaults;
	
	//category for falling for height calculator?
	static String[] initialMovementCategories = {"Distance Jumps", "Height Jumps", "Roll Cancel Vectors", "Rolls", "Object-Dependent Motion"};
	static String[][] initialMovementNames =
		{{"Single Jump", "Double Jump", "Triple Jump", "Vault", "Cap Return Jump", "Long Jump", "Optimal Distance Motion"},
		{"Triple Jump", "Ground Pound Jump", "Backflip", "Sideflip", "Vault", "Spin Jump"},
		{"Motion Cap Throw RCV", "Single Throw RCV", "Upthrow RCV", "Downthrow RCV", "Double Throw RCV", "Spinthrow RCV", "Triple Throw RCV", "Fakethrow RCV", "Optimal Distance RCV"},
		{"Ground Pound Roll", "Crouch Roll", "Roll Boost"},
		{"Horizontal Pole/Fork Flick", "Motion Horizontal Pole/Fork Flick", "Motion Vertical Pole/Fork Flick", "Small NPC Bounce", "Large NPC Bounce", "Ground Pound Object/Enemy Bounce", "Uncapture", "Bouncy Object Bounce", "Flower Bounce", "Flip Forward", "Swinging Jump"}}; //flower spinpound for height calculator
	
	//static String[] midairPresetCategories = {"Distance Jumps"};
	static String[] midairPresetNames = {"Custom", "Spinless", "Simple Tech", "Simple Tech Rainbow Spin First", "MCCT First", "MCCT First (Triple Throw)", "CBV First", "CBV First (Triple Throw)"};
	
	static String[] midairMovementNames = {"Motion Cap Throw", "Triple Throw", "Homing Motion Cap Throw", "Homing Triple Throw", "Rainbow Spin", "Dive", "Cap Bounce", "2P Midair Vault"};

	static final int MCCT = 0, TT = 1, HMCCT = 2, HTT = 3, RS = 4, DIVE = 5, CB = 6, P2CB = 7;

	static final int[][][] midairPresets =
		//custom (nothing to start)
		{new int[0][0],
		//spinless
	 	{{MCCT, 28}, {DIVE, 25}, {CB, 44}, {MCCT, 31}, {DIVE, 25}},
		//simple tech
		{{MCCT, 28}, {DIVE, 25}, {CB, 43}, {RS, 32}, {MCCT, 30}, {DIVE, 25}},
		//simple tech rainbow spin first
		{{RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 43}, {MCCT, 30}, {DIVE, 25}},
		//mcct first
		{{HMCCT, 36}, {RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 42}, {MCCT, 31}, {DIVE, 25}},
		//tt first
		{{HTT, 30}, {RS, 32}, {MCCT, 28}, {DIVE, 26}, {CB, 42}, {MCCT, 31}, {DIVE, 25}},
		//cbv first
		{{MCCT, 28}, {DIVE, 25}, {CB, 42}, {HMCCT, 36}, {RS, 32}, {MCCT, 31}, {DIVE, 25}},
		//cbv first tt
		{{MCCT, 28}, {DIVE, 26}, {CB, 42}, {HTT, 30}, {RS, 32}, {MCCT, 30}, {DIVE, 24}}};

	static int INITIAL_COORDINATES_ROW = 0;
	static int ANGLE_TYPE_ROW = 1;
	static int ANGLE_ROW = 2;
	static int INITIAL_MOVEMENT_TYPE_ROW = 3;
	static int MOVEMENT_DURATION_TYPE_ROW = 4;
	static int MOVEMENT_DURATION_ROW = 5;
	static int HOLD_JUMP_FRAMES_ROW = 6;
	static int MOONWALK_FRAMES_ROW = 7;
	static int INITIAL_HORIZONTAL_SPEED_ROW = 8;
	static int VECTOR_DIRECTION_ROW = 9;
	static int DIVE_CAP_BOUNCE_ANGLE_ROW = 10;
	static int DIVE_CAP_BOUNCE_TOLERANCE_ROW = 11;
	static int DIVE_DECEL_ROW = 12;
	static int MIDAIR_TYPE_ROW = 13;
	static int GRAVITY_ROW = 14;
	static int HYPEROPTIMIZE_ROW = 15;
	static int AXIS_ORDER_ROW = 16;
	static int CAMERA_TYPE_ROW = 17;
	static int CAMERA_ROW = 18;

	static int ANGLE_2_ROW = -1;

	static void addAngle2Row() {
		genPropertiesModel.insertRow(ANGLE_ROW, new Object[]{"", 0});
		ANGLE_2_ROW = 2;
		ANGLE_ROW++;
		INITIAL_MOVEMENT_TYPE_ROW++;
		MOVEMENT_DURATION_TYPE_ROW++;
		MOVEMENT_DURATION_ROW++;
		HOLD_JUMP_FRAMES_ROW++;
		MOONWALK_FRAMES_ROW++;
		INITIAL_HORIZONTAL_SPEED_ROW++;
		VECTOR_DIRECTION_ROW++;
		DIVE_CAP_BOUNCE_ANGLE_ROW++;
		DIVE_CAP_BOUNCE_TOLERANCE_ROW++;
		DIVE_DECEL_ROW++;
		MIDAIR_TYPE_ROW++;
		GRAVITY_ROW++;
		HYPEROPTIMIZE_ROW++;		
		AXIS_ORDER_ROW++;
		CAMERA_TYPE_ROW++;
		CAMERA_ROW++;
	}

	static void removeAngle2Row() {
		genPropertiesModel.removeRow(ANGLE_2_ROW);
		ANGLE_2_ROW = -1;
		ANGLE_ROW--;
		INITIAL_MOVEMENT_TYPE_ROW--;
		MOVEMENT_DURATION_TYPE_ROW--;
		MOVEMENT_DURATION_ROW--;
		HOLD_JUMP_FRAMES_ROW--;
		MOONWALK_FRAMES_ROW--;
		INITIAL_HORIZONTAL_SPEED_ROW--;
		VECTOR_DIRECTION_ROW--;
		DIVE_CAP_BOUNCE_ANGLE_ROW--;
		DIVE_CAP_BOUNCE_TOLERANCE_ROW--;
		DIVE_DECEL_ROW--;
		MIDAIR_TYPE_ROW--;
		GRAVITY_ROW--;
		HYPEROPTIMIZE_ROW--;
		AXIS_ORDER_ROW--;
		CAMERA_TYPE_ROW--;
		CAMERA_ROW--;
	}

	static void saveMidairs() {
		p.midairs = new int[movementModel.getRowCount()][2];
		List<String> types = Arrays.asList(midairMovementNames);
		for (int i = 0; i < movementModel.getRowCount(); i++) {
			p.midairs[i][0] = types.indexOf(movementModel.getValueAt(i, 0).toString());
			p.midairs[i][1] = Integer.parseInt(movementModel.getValueAt(i, 1).toString());
		}
		/* for (int i = 0; i < p.midairs.length; i++)
			System.out.println(p.midairs[i][0] + ", " + p.midairs[i][1]); */
	}

	static final int LOCK_NONE = 0;
	static final int LOCK_FRAMES = 1;
	static final int LOCK_VERTICAL_DISPLACEMENT = 2;

	static int lastInitialMovementFrame;
	
	static Movement initialMovement = new Movement(p.initialMovementName);
	static SimpleMotion initialMotion = new SimpleMotion(initialMovement, p.initialFrames);
	static boolean chooseJumpFrames = true;
	static boolean chooseInitialHorizontalSpeed = true;
	static int lockDurationType = LOCK_NONE;
	
	static boolean forceEdit = false;
	static boolean add_ic_listener = true;
	static boolean add_tc_listener = true;

	static JLabel errorMessage;
	
	static String[] attributeTitles = {"Parameter", "Value"};
	static String[] movementTitles = {"Midair Movement Type", "Number of Frames"};
	static String[] movementRows = {"Motion Cap Throw", "8"};
	
	static JTable genPropertiesTable;
	static DefaultTableModel genPropertiesModel;
	static JumpDialogWindow dialogWindow = new JumpDialogWindow("Choose Initial Movement", initialMovementCategories, initialMovementNames);
	//static JumpDialogWindow presetsWindow = new JumpDialogWindow("Choose Midair Preset", midairPresetCategories, midairPresetNames);
	static CoordinateWindow initial_CoordinateWindow = new CoordinateWindow("Initial Coordinates");
	static CoordinateWindow target_CoordinateWindow = new CoordinateWindow("Target Coordinates");
	static DefaultTableModel movementModel = new DefaultTableModel(0, 2);
	static JTable movementTable;

	static JButton add;
	static JButton remove;
	static JButton solveVector;
	static JButton calculateVector;

	static String[] genPropertiesTitles = {"Property", "Value"};
	static Object[][] genProperties =
		{{"Initial Coordinates", "(0, 0, 0)"},
		{"Calculate Using", "Target Coordinates"},
		{"Target Coordinates", "(0, 0, 3000)"},
		{"Initial Movement Type", p.initialMovementName},
		{"Initial Movement Duration Type", "Frames"},
		{"Initial Movement Frames", p.initialFrames},
		{"Frames of Holding A/B", p.framesJump},
		{"Moonwalk Frames", p.framesMoonwalk},
		{"Initial Horizontal Speed", (int) p.initialHorizontalSpeed},
		{"Initial Vector Direction", "Left"},
		{"Edge Cap Bounce Angle", p.diveCapBounceAngle},
		{"Edge Cap Bounce Tolerance", p.diveCapBounceTolerance},
		{"First Dive Deceleration", "0"},
		{"Midairs", "Spinless"},
		{"Gravity", "Regular"},
		{"Hyperoptimize Cap Throws", "True"},
		{"0 Degree Axis", "X"},
		{"Camera Angle", "Target Angle"}};
	
	static JFrame f = new JFrame(projectName);

	public static double round(double d, int places) {
		return ((int) (d * Math.pow(10, places) + .5)) / (double) Math.pow(10, places);
	}

	public static String numberToString(double v) {
		if ((int) v == v) {
			return Integer.toString((int) v);
		}
		else {
			return Double.toString(v);
		}
	}

	public static String toCoordinateString(double x, double y, double z) {
		return "(" + numberToString(x) + ", " + numberToString(y) + ", " + numberToString(z) + ")";
	}

	public static void lockDurationType(int value) {
		lockDurationType = value;
		Debug.println(lockDurationType);
		if (lockDurationType == LOCK_FRAMES) {
			genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_ROW, 0);
			if (p.durationFrames == false) {
				p.durationFrames = true;
				genPropertiesTable.setValueAt(initialMovement.minRecommendedFrames, MOVEMENT_DURATION_ROW, 0);
				p.initialFrames = initialMovement.minRecommendedFrames;
			}
		}
		else if (lockDurationType == LOCK_VERTICAL_DISPLACEMENT) {
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
			if (p.durationFrames == true) {
				p.durationFrames = false;
				genPropertiesTable.setValueAt(0, MOVEMENT_DURATION_ROW, 1);
				p.initialDispY = 0;
			}
		}
	}

	public static void setAngleType(AngleType type, boolean coordinates) {
		forceEdit = true;
		AngleType oldAngleType = p.angleType;
		Debug.println(oldAngleType);
		Debug.println(type);
		p.angleType = type;
		if (oldAngleType != AngleType.BOTH && type == AngleType.BOTH) {
			addAngle2Row();
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_2_ROW, 0);
			if (oldAngleType == AngleType.INITIAL) {
				p.targetAngle = p.initialAngle;
				genPropertiesTable.setValueAt("Target Angle", ANGLE_TYPE_ROW, 1);
				genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
				genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
			}
			else if (oldAngleType == AngleType.TARGET) {
				p.initialAngle = p.targetAngle;
			}
			genPropertiesTable.setValueAt(p.initialAngle, ANGLE_2_ROW, 1);
		}
		else if (oldAngleType == AngleType.BOTH && type == AngleType.TARGET) {
			removeAngle2Row();
			//genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
			//genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
			//genPropertiesTable.setValueAt("Target Angle", ANGLE_TYPE_ROW, 1);
		}
		else if (oldAngleType == AngleType.BOTH && type == AngleType.INITIAL) {
			removeAngle2Row();
			coordinates = false;
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_ROW, 0);
			genPropertiesTable.setValueAt(p.initialAngle, ANGLE_ROW, 1);
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_TYPE_ROW, 1);
		}
		else if (oldAngleType == AngleType.TARGET && type == AngleType.INITIAL) {
			if (coordinates) {
				p.initialAngle = round(p.targetAngle, 3);
				coordinates = false;
			}
			else {
				p.initialAngle = p.targetAngle;
			}
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_ROW, 0);
			genPropertiesTable.setValueAt(p.initialAngle, ANGLE_ROW, 1);
		}
		else if (oldAngleType == AngleType.INITIAL && type == AngleType.TARGET) {
			if (coordinates) {
				genPropertiesTable.setValueAt("Target Coordinates", ANGLE_ROW, 0);
				genPropertiesTable.setValueAt("(0, 0, 0)", ANGLE_ROW, 1);
				p.x1 = 0;
				p.y1 = 0;
				p.z1 = 0;
			}
			else {
				genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
				p.targetAngle = p.initialAngle;
			}
		}
		else if (oldAngleType == AngleType.TARGET && type == AngleType.TARGET || oldAngleType == AngleType.BOTH && type == AngleType.BOTH) {
			if (!p.targetCoordinates && coordinates) {
				genPropertiesTable.setValueAt("Target Coordinates", ANGLE_ROW, 0);
				genPropertiesTable.setValueAt("(0, 0, 0)", ANGLE_ROW, 1);
				p.x1 = 0;
				p.y1 = 0;
				p.z1 = 0;
			}
			else if (p.targetCoordinates && !coordinates) {
				genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
				if (p.targetAngle == (int) p.targetAngle)
					genPropertiesTable.setValueAt((int) p.targetAngle, ANGLE_ROW, 1);
				else {
					genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
				}
			}
		}
		Debug.println("Initial Angle: " + p.initialAngle);
		Debug.println("Target Angle: " + p.targetAngle);
		p.targetCoordinates = coordinates;
		forceEdit = false;
	}

	public static void setCameraType(CameraType type) {
		Debug.println("Setting camera type to " + type);
		CameraType oldCameraType = p.cameraType;
		p.cameraType = type;
		if (p.cameraType == CameraType.CUSTOM && oldCameraType != CameraType.CUSTOM) {
			if ((int) p.customCameraAngle == p.customCameraAngle) {
				genPropertiesModel.addRow(new Object[]{"Custom Camera Angle", (int) p.customCameraAngle});
			}
			else {
				genPropertiesModel.addRow(new Object[]{"Custom Camera Angle", p.customCameraAngle});
			}
		}
		else if (p.cameraType != CameraType.CUSTOM && oldCameraType == CameraType.CUSTOM) {
			genPropertiesModel.removeRow(genPropertiesModel.getRowCount() - 1);
		}
	}

	public static void targetCoordinatesToTargetAngle() {
		p.targetAngle = Math.toDegrees(Math.atan2(p.x1 - p.x0, p.z1 - p.z0));
		if (p.xAxisZeroDegrees) {
			p.targetAngle = 90 - p.targetAngle;
		}
		if (p.targetAngle < 0) {
			p.targetAngle += 360;
		}
		Debug.println("Target Angle from Coordinates: " + p.targetAngle);
	}
	
	public static int getMoonwalkDisp() {
		if (p.framesMoonwalk == 0)
			return 0;
		else if (p.framesMoonwalk == 1)
			return -3;
		else if (p.framesMoonwalk == 2)
			return -9;
		else if (p.framesMoonwalk == 3)
			return -18;
		else if (p.framesMoonwalk == 4)
			return -30;
		else if (p.framesMoonwalk == 5)
			return -45;
		else //impossible case
			return 0;
	}

	//replaces the current midairs with the preset of the given index
	public static void addPreset(int index) {
		Debug.println("Switching to preset " + index);
		
		addPreset(midairPresets[index]);

		if (index == 0) {
			add.setEnabled(true);
			remove.setEnabled(true);
		}
		else {
			add.setEnabled(false);
			remove.setEnabled(false);
		}
		p.currentPresetIndex = index;
	}

	public static void addPreset(int[][] preset) {
		movementModel.setRowCount(0);
		for (int[] row : preset) {
			movementModel.addRow(new Object[]{midairMovementNames[row[0]], row[1]});
		}
		if (p.currentPresetIndex >= 0) {
			add.setEnabled(false);
			remove.setEnabled(false);
		}
	}

	public static void updateInitialMovement(boolean suggestSpeed) {
		initialMovement = new Movement(p.initialMovementName);
		genPropertiesModel.setValueAt(p.initialMovementName, INITIAL_MOVEMENT_TYPE_ROW, 1);
		double suggestedSpeed = initialMovement.getSuggestedSpeed();
		p.initialHorizontalSpeed = suggestedSpeed;
		if (initialMovement.variableJumpFrames()) {
			if (!chooseJumpFrames) {
				chooseJumpFrames = true;
				p.framesJump = 10;
			}
			genPropertiesModel.setValueAt(p.framesJump, HOLD_JUMP_FRAMES_ROW, 1);	
		}
		else {
			chooseJumpFrames = false;
			genPropertiesModel.setValueAt("N/A", HOLD_JUMP_FRAMES_ROW, 1);
		}
		if (initialMovement.canMoonwalk) {
			if (!p.canMoonwalk) {
				p.canMoonwalk = true;
				p.framesMoonwalk = 0;
			}
			genPropertiesModel.setValueAt(p.framesMoonwalk, MOONWALK_FRAMES_ROW, 1);
		}
		else {
			p.canMoonwalk = false;
			p.framesMoonwalk = 0;
			genPropertiesModel.setValueAt("N/A", MOONWALK_FRAMES_ROW, 1);
		}
		if (initialMovement.variableInitialHorizontalSpeed()) {
			if (suggestSpeed) {
				chooseInitialHorizontalSpeed = true;
				if (suggestedSpeed == (int) suggestedSpeed)
					genPropertiesModel.setValueAt((int) initialMovement.getSuggestedSpeed(), INITIAL_HORIZONTAL_SPEED_ROW, 1);
				else
					genPropertiesModel.setValueAt(initialMovement.getSuggestedSpeed(), INITIAL_HORIZONTAL_SPEED_ROW, 1);
			}
			//TODO
			else {
				genPropertiesModel.setValueAt(p.initialHorizontalSpeed, INITIAL_HORIZONTAL_SPEED_ROW, 1);
			}
		}
		else {
			chooseInitialHorizontalSpeed = false;
			genPropertiesModel.setValueAt("N/A", INITIAL_HORIZONTAL_SPEED_ROW, 1);
			p.initialHorizontalSpeed = 0;
		}
		if (p.initialMovementName.contains("RCV")) {
			setAngleType(AngleType.BOTH, p.targetCoordinates);
		}
		else if (p.angleType == AngleType.BOTH) { //switch back to just Initial or Target angle
			setAngleType(AngleType.TARGET, p.targetCoordinates);
		}
		
		if (p.initialMovementName.contains("Optimal Distance")) {
			lockDurationType(LOCK_VERTICAL_DISPLACEMENT);
		}
		else {
			lockDurationType(LOCK_NONE);
		}
	}

	public static void saveProperties(File file, boolean updateCurrentFile) {
		boolean saveSuccess = Properties.save(file);
		if (updateCurrentFile) {
			saved = saveSuccess;
			if (saved) {
				projectName = file.getName();
				f.setTitle(projectName);
				VectorDisplayWindow.frame.setTitle("Calculations: " + VectorCalculator.projectName);
			}
		}
		else if (!saveSuccess) {
			errorMessage.setText("Error: Save failed");
		}
	}

	public static void loadProperties(File file, boolean defaults) {
		Properties pl = Properties.load(file);
		if (pl == null) {
			if (defaults) {
				errorMessage.setText("Error: Defaults could not be loaded");
			}
			else {
				errorMessage.setText("Error: File could not be loaded");
			}
		}

		p.x0 = pl.x0;
		p.y0 = pl.y0;
		p.z0 = pl.z0;
		genPropertiesTable.setValueAt(toCoordinateString(p.x0, p.y0, p.z0), INITIAL_COORDINATES_ROW, 1);

		setAngleType(pl.angleType, pl.targetCoordinates);
		p.x1 = pl.x1;
		p.y1 = pl.y1;
		p.z1 = pl.z1;
		p.initialAngle = pl.initialAngle;
		p.targetAngle = pl.targetAngle;
		if (p.angleType == AngleType.TARGET || p.angleType == AngleType.BOTH) {
			if (p.targetCoordinates) {
				genPropertiesTable.setValueAt("Target Coordinates", ANGLE_TYPE_ROW, 1);
				genPropertiesTable.setValueAt(toCoordinateString(p.x1, p.y1, p.z1), ANGLE_ROW, 1);
			}
			else {
				genPropertiesTable.setValueAt("Target Angle", ANGLE_TYPE_ROW, 1);
				genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
			}
		}
		if (p.angleType == AngleType.INITIAL) {
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_TYPE_ROW, 1);
			genPropertiesTable.setValueAt(p.initialAngle, ANGLE_ROW, 1);
		}
		if (p.angleType == AngleType.BOTH) {
			genPropertiesTable.setValueAt(p.initialAngle, ANGLE_2_ROW, 1);
		}
		p.initialMovementName = pl.initialMovementName;
		p.durationFrames = pl.durationFrames;
		p.initialFrames = pl.initialFrames;
		p.initialHorizontalSpeed = pl.initialHorizontalSpeed;
		updateInitialMovement(false);
		if (p.durationFrames) {
			genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Initial Movement Frames", MOVEMENT_DURATION_ROW, 0);
			genPropertiesTable.setValueAt(pl.initialFrames, MOVEMENT_DURATION_ROW, 1);
		}
		else {
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
			genPropertiesTable.setValueAt(pl.initialDispY, MOVEMENT_DURATION_ROW, 1);
		}
		p.framesJump = pl.framesJump;
		p.initialDispY = pl.initialDispY;
		p.framesMoonwalk = pl.framesMoonwalk;
		if (p.canMoonwalk) {
			genPropertiesTable.setValueAt(p.framesMoonwalk, MOONWALK_FRAMES_ROW, 1);
		}
		p.rightVector = pl.rightVector;
		if (p.rightVector) {
			genPropertiesTable.setValueAt("Right", VECTOR_DIRECTION_ROW, 1);
		}
		else {
			genPropertiesTable.setValueAt("Left", VECTOR_DIRECTION_ROW, 1);
		}
		p.diveCapBounceAngle = pl.diveCapBounceAngle;
		p.diveCapBounceTolerance = pl.diveCapBounceTolerance;
		p.diveFirstFrameDecel = pl.diveFirstFrameDecel;
		genPropertiesTable.setValueAt(p.diveCapBounceAngle, DIVE_CAP_BOUNCE_ANGLE_ROW, 1);
		genPropertiesTable.setValueAt(p.diveCapBounceTolerance, DIVE_CAP_BOUNCE_TOLERANCE_ROW, 1);
		genPropertiesTable.setValueAt(p.diveFirstFrameDecel, DIVE_DECEL_ROW, 1);
		p.currentPresetIndex = pl.currentPresetIndex;
		genPropertiesTable.setValueAt(midairPresetNames[p.currentPresetIndex], MIDAIR_TYPE_ROW, 1);
		p.onMoon = pl.onMoon;
		if (p.onMoon) {
			genPropertiesTable.setValueAt("Moon", GRAVITY_ROW, 1);
		}
		else {
			genPropertiesTable.setValueAt("Regular", GRAVITY_ROW, 1);
		}
		p.hyperoptimize = pl.hyperoptimize;
		if (p.hyperoptimize) {
			genPropertiesTable.setValueAt("True", HYPEROPTIMIZE_ROW, 1);
		}
		else {
			genPropertiesTable.setValueAt("False", HYPEROPTIMIZE_ROW, 1);
		}
		p.xAxisZeroDegrees = pl.xAxisZeroDegrees;
		if (p.xAxisZeroDegrees) {
			genPropertiesTable.setValueAt("X", AXIS_ORDER_ROW, 1);
		}
		else {
			genPropertiesTable.setValueAt("Z", AXIS_ORDER_ROW, 1);
		}
		setCameraType(pl.cameraType);
		p.customCameraAngle = pl.customCameraAngle;
		String cameraString = "Absolute";
		if (p.cameraType == CameraType.TARGET)
			cameraString = "Target Angle";
		else if (p.cameraType == CameraType.INITIAL)
			cameraString = "Initial Angle";
		else if (p.cameraType == CameraType.CUSTOM) {
			cameraString = "Custom";
			genPropertiesTable.setValueAt(p.customCameraAngle, CAMERA_ROW, 1);
		}
		genPropertiesTable.setValueAt(cameraString, CAMERA_TYPE_ROW, 1);
		p.midairs = pl.midairs;
		addPreset(p.midairs);
		
		if (initialized && defaults && Properties.isUnsaved()) {
			saved = false;
			f.setTitle("*" + projectName);
		}
		else {
			saved = true;
			if (!defaults) {
				projectName = file.getName();
				f.setTitle(projectName);
				VectorDisplayWindow.frame.setTitle("Calculations: " + VectorCalculator.projectName);
			}
		}
	}

	static class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {
		  public MyComboBoxRenderer(String[] items) {
		    super(items);
		  }

		  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		      boolean hasFocus, int row, int column) {
		    if (isSelected) {
		      setForeground(table.getSelectionForeground());
		      super.setBackground(table.getSelectionBackground());
		    } else {
		      setForeground(table.getForeground());
		      setBackground(table.getBackground());
		    }
		    setSelectedItem(value);
		    return this;
		  }
	}

	static class MyComboBoxEditor extends DefaultCellEditor {
		  public MyComboBoxEditor(String[] items) {
		    super(new JComboBox(items));
		  }
	}
	
	static class ButtonListener implements ActionListener {
		 public void actionPerformed(ActionEvent evt) {
			 if (evt.getActionCommand() == "add") {
				 movementModel.addRow(movementRows);
				 //movementPropertyTables.add(new MovementProperties("Motion Cap Throw"));
				 //movementPropertiesTable.setModel(movementPropertyTables.get(0).generateTableModel());
			 }
			 else if (evt.getActionCommand() == "remove") {
				 movementTable.removeEditor();
				 int[] rowsRemove = movementTable.getSelectedRows();
				 if (rowsRemove.length > 0)
					 for (int i = rowsRemove.length - 1; i >= 0; i--) {
						 int removeRowIndex = rowsRemove[i];
						 movementModel.removeRow(removeRowIndex);
						 if (movementModel.getRowCount() > 0)
							 if (removeRowIndex == movementModel.getRowCount())
								 movementTable.setRowSelectionInterval(removeRowIndex - 1, removeRowIndex - 1);
							 else
								 movementTable.setRowSelectionInterval(removeRowIndex, removeRowIndex);
					 }
			 }
			 else if (evt.getActionCommand() == "solve") {
				Solver solver = new Solver();
				//  for (double i = -50; i <= 50; i += 1) {
				//  	p.y1 = i;
				if (solver.solve(3)) { //2 might even be okay for jumps with HCT
					VectorMaximizer maximizer = getMaximizer();
					if (maximizer != null) {
						maximizer.alwaysDiveTurn = true;
						maximizer.maximize();
						boolean possible = maximizer.isDiveCapBouncePossible(true, true, true, false);
						//maximizer.alwaysDiveTurn = true;
						maximizer.maximize();
						//maximizer.alwaysDiveTurn = true;
						possible = maximizer.isDiveCapBouncePossible(true, true, true, false);
						genPropertiesTable.setValueAt(round(p.diveCapBounceAngle, 3), DIVE_CAP_BOUNCE_ANGLE_ROW, 1);
						genPropertiesTable.setValueAt(round(p.diveFirstFrameDecel, 3), DIVE_DECEL_ROW, 1);
						System.out.println("Possible: " + possible + " " + maximizer.ctType);
						//maximizer.maximize();
						VectorDisplayWindow.generateData(maximizer, maximizer.getInitialAngle(), maximizer.getTargetAngle());
						VectorDisplayWindow.display();
						//System.out.println("Cappy position: " + );
						//System.out.println(((DiveTurn)maximizer.motions[maximizer.variableCapThrow1Index + 3]).getCapBounceFrame(((ComplexVector)maximizer.motions[maximizer.variableCapThrow1Index]).getCappyPosition(maximizer.ctType)));
					}
				}
				else {
					errorMessage.setText("Error: Movement cannot reach target height");
				}
					//i = p.y1 + solver.bestYDisp + .01; //to test what the biggest bestYDisp is
				 	//i += 1;
				//}
				
				//Debug.println();
			 }
			 else if (evt.getActionCommand() == "calculate") {
				saveMidairs();
				VectorMaximizer maximizer = null;
				if (p.targetCoordinates) {
					targetCoordinatesToTargetAngle();
				}
				if (p.initialMovementName.equals("Optimal Distance Motion")) {
					p.initialMovementName = "Triple Jump";
					p.framesJump = 10;
					initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
					VectorMaximizer maximizerTJ = calculate();
					p.initialMovementName = "Optimal Distance RCV";
					initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
					VectorMaximizer maximizerRC = calculate();
					p.initialMovementName = "Sideflip";
					initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
					VectorMaximizer maximizerSideflip = calculate();
					System.out.println("TJ: " + maximizerTJ.bestDisp);
					System.out.println("RC: " + maximizerRC.bestDisp);
					System.out.println("SF: " + maximizerSideflip.bestDisp);
					if (maximizerTJ != null && maximizerRC != null && maximizerSideflip != null) {
						if (maximizerTJ.bestDisp > maximizerRC.bestDisp && maximizerTJ.bestDisp > maximizerSideflip.bestDisp) {
							maximizer = maximizerTJ;
						}
						else if (maximizerRC.bestDisp > maximizerTJ.bestDisp && maximizerRC.bestDisp > maximizerSideflip.bestDisp) {
							maximizer = maximizerRC;
						}
						else {
							maximizer = maximizerSideflip;
						}
					}
					p.initialMovementName = "Optimal Distance Motion";
				}
				else {
					maximizer = calculate();
				}
				if (maximizer != null) {
					VectorDisplayWindow.generateData(maximizer, maximizer.getInitialAngle(), maximizer.getTargetAngle());
					VectorDisplayWindow.display();
				}
				 
				Debug.println();
			 }
		 }
	}

	public static VectorMaximizer getMaximizer() {
		Movement.onMoon = p.onMoon;
		MovementNameListPreparer movementPreparer = new MovementNameListPreparer();
		String errorText = movementPreparer.prepareList();
		lastInitialMovementFrame = movementPreparer.lastInitialMovementFrame;
		movementPreparer.print();

		if (errorText.equals("")) {
			errorMessage.setText("");
			VectorMaximizer maximizer = new VectorMaximizer(movementPreparer);
			return maximizer;
		}
		else {
			errorMessage.setText("Error: " + errorText);
			return null;
		}
	}

	public static VectorMaximizer calculate() {
		VectorMaximizer maximizer = getMaximizer();
		if (maximizer != null)
			maximizer.maximize();
		return maximizer;
	}

	public static void main(String[] args) {
		try {
			jarParentFolder = new File(VectorCalculator.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
		}
		catch (Exception ex) {
			jarParentFolder = "~";
		}
		//for debugging
		jarParentFolder = ".";
		userDefaults = new File(VectorCalculator.jarParentFolder + "/user-defaults.xml");
		factoryDefaults = new File(VectorCalculator.jarParentFolder + "/factory-defaults.xml");

		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.setOpaque(true);
		
		//GENERAL PROPERTIES TABLE
		genPropertiesModel = new DefaultTableModel(genProperties, genPropertiesTitles);
		genPropertiesTable = new JTable(genPropertiesModel) {
			
			public TableCellEditor getCellEditor(int row, int column)
            {
                int modelColumn = convertColumnIndexToModel( column );

                if (modelColumn == 1 && row == ANGLE_TYPE_ROW)
                {
					String[] options;
					if (p.angleType == AngleType.BOTH)
						options = new String[]{"Target Angle", "Target Coordinates"};
					else
						options = new String[]{"Initial Angle", "Target Angle", "Target Coordinates"};
                    JComboBox<String> angle = new JComboBox<String>(options);
                    return new DefaultCellEditor(angle);
                }
				else if (modelColumn == 1 && row == MOVEMENT_DURATION_TYPE_ROW)
                {
					if (lockDurationType == LOCK_NONE) {
						String[] options = {"Frames", "Vertical Displacement"};
						JComboBox<String> choice = new JComboBox<String>(options);
						return new DefaultCellEditor(choice);
					}
					else {
						return null;
					}
                }
                else if (modelColumn == 1 && row == VECTOR_DIRECTION_ROW)
                {
                	String[] options = {"Left", "Right"}; //can add LOCK_NONE option
                    JComboBox<String> angle = new JComboBox<String>(options);
                    return new DefaultCellEditor(angle);
                }
				else if (modelColumn == 1 && row == MIDAIR_TYPE_ROW)
                {
                    JComboBox<String> angle = new JComboBox<String>(midairPresetNames);
                    return new DefaultCellEditor(angle);
                }
                else if (modelColumn == 1 && row == GRAVITY_ROW)
                {
                	String[] options = {"Regular", "Moon"};
                    JComboBox<String> gravity = new JComboBox<String>(options);
                    return new DefaultCellEditor(gravity);
                }
				else if (modelColumn == 1 && row == HYPEROPTIMIZE_ROW)
                {
                	String[] options = {"True", "False"};
                    JComboBox<String> choice = new JComboBox<String>(options);
                    return new DefaultCellEditor(choice);
                }
				else if (modelColumn == 1 && row == CAMERA_TYPE_ROW)
                {
                	String[] options = {"Initial Angle", "Target Angle", "Absolute", "Custom"};
                    JComboBox<String> choice = new JComboBox<String>(options);
                    return new DefaultCellEditor(choice);
                }
				else if (modelColumn == 1 && row == AXIS_ORDER_ROW) {
					String[] options = {"X", "Z"};
                    JComboBox<String> choice = new JComboBox<String>(options);
                    return new DefaultCellEditor(choice);
				}
                else
                    return super.getCellEditor(row, column);
            }
        
			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0 || row == INITIAL_COORDINATES_ROW || (row == ANGLE_ROW && p.targetCoordinates) || row == INITIAL_MOVEMENT_TYPE_ROW || (row == HOLD_JUMP_FRAMES_ROW && !chooseJumpFrames) || (row == MOONWALK_FRAMES_ROW && !p.canMoonwalk) || (row == INITIAL_HORIZONTAL_SPEED_ROW && !chooseInitialHorizontalSpeed))
					return false;
				return true;
			}
			
			@Override
			public Component prepareEditor(TableCellEditor editor, int row, int column) {
			    Component c = super.prepareEditor(editor, row, column);
			    if (c instanceof JTextComponent) {
			        ((JTextComponent) c).selectAll();
			    }
			    return c;
			}
		};
		
		genPropertiesTable.setFillsViewportHeight(true);
		genPropertiesTable.getTableHeader().setFont(tableFont);
		genPropertiesTable.setFont(tableFont);
		genPropertiesTable.setRowHeight(genPropertiesTable.getRowHeight() + 2);
		genPropertiesTable.setColumnSelectionAllowed(true);
		genPropertiesTable.getTableHeader().setReorderingAllowed(false);
		
		genPropertiesTable.getColumnModel().getColumn(0).setMinWidth(260);
		genPropertiesTable.getColumnModel().getColumn(0).setMaxWidth(260);
		
		JScrollPane genPropertiesScrollPane = new JScrollPane(genPropertiesTable);
		genPropertiesScrollPane.setPreferredSize(new Dimension(500, genPropertiesTable.getRowHeight() * (genProperties.length + 2) + 25));
		
		ListSelectionModel genPropertiesSelectionModel = genPropertiesTable.getSelectionModel();
		
		//initial movement type selector
		genPropertiesTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				//this used to not be all part of the conditional
				if (genPropertiesTable.rowAtPoint(evt.getPoint()) == INITIAL_MOVEMENT_TYPE_ROW && genPropertiesTable.columnAtPoint(evt.getPoint()) == 1) {
					dialogWindow.display();
					JButton confirm = dialogWindow.getConfirmButton();
					confirm.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							//Debug.println(dialogWindow.getSelectedMovementName());
							p.initialMovementName = dialogWindow.getSelectedMovementName();
							updateInitialMovement(true);
							dialogWindow.close();	
						}
					});
				}
				else if (genPropertiesTable.rowAtPoint(evt.getPoint()) == INITIAL_COORDINATES_ROW && genPropertiesTable.columnAtPoint(evt.getPoint()) == 1) {
					initial_CoordinateWindow.display(p.x0, p.y0, p.z0);
					if (add_ic_listener) {
						(initial_CoordinateWindow.getConfirmButton()).addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								initial_CoordinateWindow.findCoordinates();
								p.x0 = initial_CoordinateWindow.x;
								p.y0 = initial_CoordinateWindow.y;
								p.z0 = initial_CoordinateWindow.z;
								genPropertiesModel.setValueAt(initial_CoordinateWindow.coordinates, INITIAL_COORDINATES_ROW, 1);
								initial_CoordinateWindow.close();
							}
						});
					}
					add_ic_listener = false;
				}
				else if (p.targetCoordinates && genPropertiesTable.rowAtPoint(evt.getPoint()) == ANGLE_ROW && genPropertiesTable.columnAtPoint(evt.getPoint()) == 1) {
					target_CoordinateWindow.display(p.x1, p.y1, p.z1);
					// JButton target_confirm = target_CoordinateWindow.getConfirmButton();
					// for (ActionListener al : target_confirm.getActionListeners()) {
					// 	target_confirm.removeActionListener(al);
					// }
					if (add_tc_listener) {
						(target_CoordinateWindow.getConfirmButton()).addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								target_CoordinateWindow.findCoordinates();
								p.x1 = target_CoordinateWindow.x;
								p.y1 = target_CoordinateWindow.y;
								p.z1 = target_CoordinateWindow.z;
								targetCoordinatesToTargetAngle();
								genPropertiesModel.setValueAt(target_CoordinateWindow.coordinates, ANGLE_ROW, 1);
								target_CoordinateWindow.close();
								Debug.println("Coords: " + target_CoordinateWindow.coordinates);
								Debug.println("Angle row: " + ANGLE_ROW);
							}
						});
					}
					add_tc_listener = false;
				}
			}
		});
			
		//fix bad values and update angle type in info properties table
		genPropertiesModel.addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);

				int row = e.getFirstRow();

				if (row >= genPropertiesModel.getRowCount()) {
					return;
				}

				if (!forceEdit) { //forceEdit allows a part of the program to ignore these rules
					if (row == ANGLE_ROW) {
						if (p.angleType == AngleType.TARGET || p.angleType == AngleType.BOTH) {
							if (!p.targetCoordinates) {
								try {
									p.targetAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
								}
								catch (NumberFormatException ex) {
									p.targetAngle = 0;
									genPropertiesTable.setValueAt(0, row, 1);
								}
							}
						}
						else {
							try {
								p.initialAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {
								p.initialAngle = 0;
								genPropertiesTable.setValueAt(0, row, 1);
							}
						}
					}
					else if (row == ANGLE_2_ROW) {
						if (p.angleType == AngleType.BOTH) {
							try {
								p.initialAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {
								p.initialAngle = 0;
								genPropertiesTable.setValueAt(0, row, 1);
							}
						}
					}
					else if (row == ANGLE_TYPE_ROW) {
						if (genPropertiesTable.getValueAt(row, 1).equals("Initial Angle")) {
							setAngleType(AngleType.INITIAL, p.targetCoordinates);
						}
						else if (genPropertiesTable.getValueAt(row, 1).equals("Target Angle")) {
							if (p.angleType != AngleType.BOTH)
								setAngleType(AngleType.TARGET, false);
							else
								setAngleType(AngleType.BOTH, false);
						}
						else if (genPropertiesTable.getValueAt(row, 1).equals("Target Coordinates")) {
							if (p.angleType != AngleType.BOTH)
								setAngleType(AngleType.TARGET, true);
							else
								setAngleType(AngleType.BOTH, true);
						}
					}
					else if (row == INITIAL_MOVEMENT_TYPE_ROW || row == MOVEMENT_DURATION_ROW) {
						try {
							//movementType = genPropertiesModel.getValueAt(VectorCalculator.INITIAL_MOVEMENT_TYPE_ROW, 1).toString();
							int minFrames = initialMovement.minFrames;
							if (p.durationFrames) {
								p.initialFrames = Integer.parseInt(genPropertiesTable.getValueAt(MOVEMENT_DURATION_ROW, 1).toString());
								if (p.initialFrames < minFrames) {
									p.initialFrames = minFrames;
									genPropertiesTable.setValueAt(minFrames, MOVEMENT_DURATION_ROW, 1);
								}
							}
							else {
								p.initialDispY = Double.parseDouble(genPropertiesTable.getValueAt(MOVEMENT_DURATION_ROW, 1).toString());
								//add checks to make sure it isn't too big?
							}
						}
						catch (NumberFormatException ex) {
							if (p.durationFrames) {
								genPropertiesTable.setValueAt(1, MOVEMENT_DURATION_ROW, 1);
								p.initialFrames = 1;
							}
						}
					}
					else if (row == MOVEMENT_DURATION_TYPE_ROW) {
						if (lockDurationType == LOCK_NONE) {
							boolean oldDurationFrames = p.durationFrames;
							p.durationFrames = genPropertiesTable.getValueAt(MOVEMENT_DURATION_TYPE_ROW, 1).equals("Frames");
							Debug.println(p.durationFrames);
							initialMovement.initialHorizontalSpeed = p.initialHorizontalSpeed;
							Debug.println(initialMovement.initialHorizontalSpeed);
							initialMotion = initialMovement.getMotion(p.initialFrames, false, false);//new SimpleMotion(initialMovement, p.initialFrames);
							if (p.durationFrames && !oldDurationFrames) {
								genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_ROW, 0);
								p.initialFrames = initialMotion.calcFrames(p.initialDispY - getMoonwalkDisp());
								genPropertiesTable.setValueAt(p.initialFrames, MOVEMENT_DURATION_ROW, 1);
							}
							else if (!p.durationFrames && oldDurationFrames) {
								genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
								p.initialDispY = initialMotion.calcDispY(p.initialFrames) + getMoonwalkDisp();
								genPropertiesTable.setValueAt(p.initialDispY, MOVEMENT_DURATION_ROW, 1);
							}
						}
					}
					else if (row == HOLD_JUMP_FRAMES_ROW) {
						if (chooseJumpFrames) {
							p.framesJump = 0;
							try {
								p.framesJump = Integer.parseInt(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {};
							if (p.framesJump > 10) {
								p.framesJump = 10;
								genPropertiesTable.setValueAt(p.framesJump, row, 1);
							}
							if (p.framesJump < 1) {
								p.framesJump = 1;
								genPropertiesTable.setValueAt(p.framesJump, row, 1);
							}
							//genPropertiesTable.setValueAt(p.framesJump, row, 1);
						}
					}
					else if (row == MOONWALK_FRAMES_ROW) {
						if (p.canMoonwalk) {
							p.framesMoonwalk = 0;
							try {
								p.framesMoonwalk = Integer.parseInt(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {};
							if (p.framesMoonwalk > 5) {
								p.framesMoonwalk = 5;
								genPropertiesTable.setValueAt(p.framesMoonwalk, row, 1);
							}
							if (p.framesMoonwalk < 0) {
								p.framesMoonwalk = 0;
								genPropertiesTable.setValueAt(p.framesMoonwalk, row, 1);
							}
						}
					}
					else if (row == INITIAL_HORIZONTAL_SPEED_ROW) {
						if (chooseInitialHorizontalSpeed) {
							p.initialHorizontalSpeed = 0;
							try {
								p.initialHorizontalSpeed = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {
								genPropertiesTable.setValueAt(p.initialHorizontalSpeed, row, 1);
							}
							if (p.initialHorizontalSpeed < 0) {
								p.initialHorizontalSpeed = 0;
								genPropertiesTable.setValueAt(p.initialHorizontalSpeed, row, 1);
							}
						}
					}
					else if (row == VECTOR_DIRECTION_ROW) {
						p.rightVector = genPropertiesTable.getValueAt(row, 1).equals("Right");
					}
					else if (row == DIVE_CAP_BOUNCE_ANGLE_ROW) {
						try {
							p.diveCapBounceAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
						}
						catch (NumberFormatException ex) {
							p.diveCapBounceAngle = 0;
							genPropertiesTable.setValueAt(0, row, 1);
						}
						if (p.diveCapBounceAngle > 41.2) {
							p.diveCapBounceAngle = 41.2;
							genPropertiesTable.setValueAt(41.2, row, 1);
						}
						else if (p.diveCapBounceAngle < 0) {
							p.diveCapBounceAngle = 0;
							genPropertiesTable.setValueAt(0, row, 1);
						}
					}
					else if (row == DIVE_CAP_BOUNCE_TOLERANCE_ROW) {
						try {
							p.diveCapBounceTolerance = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
						}
						catch (NumberFormatException ex) {
							p.diveCapBounceTolerance = 0;
							genPropertiesTable.setValueAt(0, row, 1);
						}
						if (p.diveCapBounceTolerance > 1) {
							p.diveCapBounceTolerance = 1;
							genPropertiesTable.setValueAt(1, row, 1);
						}
						else if (p.diveCapBounceTolerance < 0) {
							p.diveCapBounceTolerance = 0;
							genPropertiesTable.setValueAt(0, row, 1);
						}
					}
					else if (row == DIVE_DECEL_ROW) {
						try {
							p.diveFirstFrameDecel = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
						}
						catch (NumberFormatException ex) {
							p.diveFirstFrameDecel = 0;
							genPropertiesTable.setValueAt(0, row, 1);
						}
						if (p.diveFirstFrameDecel > .5) {
							p.diveFirstFrameDecel = .5;
							genPropertiesTable.setValueAt(1, row, 1);
						}
						else if (p.diveFirstFrameDecel <= .05 && p.diveFirstFrameDecel != 0) { //can't have strength of less than or equal to .1 unless it is 0
							p.diveFirstFrameDecel = 0;
							genPropertiesTable.setValueAt(0, row, 1);
						}
					}
					else if (row == MIDAIR_TYPE_ROW) {
						int presetIndex = Arrays.asList(midairPresetNames).indexOf((String) genPropertiesTable.getValueAt(row, 1));
						if (presetIndex != p.currentPresetIndex) {
							addPreset(presetIndex);
						}
					}
					else if (row == GRAVITY_ROW) {
						p.onMoon = genPropertiesTable.getValueAt(row, 1).equals("Moon");
						Movement.onMoon = p.onMoon;
					}
					else if (row == HYPEROPTIMIZE_ROW) {
						p.hyperoptimize = genPropertiesTable.getValueAt(row, 1).equals("True");
					}
					else if (row == AXIS_ORDER_ROW) {
						p.xAxisZeroDegrees = genPropertiesTable.getValueAt(row, 1).equals("X");
					}
					else if (row == CAMERA_TYPE_ROW) {
						String choice = (String) genPropertiesTable.getValueAt(row, 1);
						if (choice.equals("Initial Angle")) {
							setCameraType(CameraType.INITIAL);
						}
						else if (choice.equals("Target Angle")) {
							setCameraType(CameraType.TARGET);
						}
						else if (choice.equals("Absolute")) {
							setCameraType(CameraType.ABSOLUTE);
						}
						else {
							setCameraType(CameraType.CUSTOM);
						}
					}
					else if (row == CAMERA_ROW) {
						p.customCameraAngle = 0;
						try {
							p.customCameraAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
						}
						catch (NumberFormatException ex) {
							genPropertiesTable.setValueAt(0, row, 1);
						}
					}
				}
				
				//make whole numbers not have decimal places
				if (row != INITIAL_COORDINATES_ROW && (row != ANGLE_ROW || !p.targetCoordinates)) {
					String setString = genPropertiesTable.getValueAt(row, 1).toString();
					if (setString.contains(".")) {
						double setValue = Double.parseDouble(setString);
						if (setValue == (int) setValue)
							genPropertiesTable.setValueAt((int) setValue, row, 1);
					}
				}

				initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);

				if (initialized && saved && Properties.isUnsaved()) {
					System.out.println("Unsaved");
					saved = false;
					f.setTitle("*" + projectName);
				}
			}
		});	
		
		
		//MOVEMENT TABLE
		
		movementModel.setColumnIdentifiers(movementTitles);
		
		movementTable = new JTable(movementModel) {
		
			@Override
			public Component prepareEditor(TableCellEditor editor, int row, int column) {
			    Component c = super.prepareEditor(editor, row, column);
			    if (c instanceof JTextComponent) {
			        ((JTextComponent) c).selectAll();
			    } 
			    return c;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0 && p.currentPresetIndex > 0)
					return false;
				return true;
			}
		};
		
		movementTable.setFillsViewportHeight(true);
		movementTable.getTableHeader().setFont(tableFont);
		movementTable.setFont(tableFont);
		movementTable.setRowHeight(movementTable.getRowHeight() + 2);
		movementTable.setPreferredScrollableViewportSize(new Dimension(300, 185));
		//movementTable.setColumnSelectionAllowed(true);
		movementTable.getTableHeader().setReorderingAllowed(false);
		
		JScrollPane movementScrollPane = new JScrollPane(movementTable);
		
		TableColumn movementColumn = movementTable.getColumnModel().getColumn(0);
		movementColumn.setCellEditor(new MyComboBoxEditor(midairMovementNames));
		
		movementModel.addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				/* if (movementModel.getRowCount() == 0) {
					remove.setEnabled(false);
				}
				else if (p.currentPresetIndex == 0) {
					remove.setEnabled(true);
				} */
				if (e.getType() == TableModelEvent.UPDATE) {
					int row = e.getFirstRow();
					Movement changedRowMovement = new Movement(movementTable.getValueAt(row, 0).toString());
					if (e.getColumn() == 0) {
						if (changedRowMovement.getSuggestedFrames() > Integer.parseInt(movementTable.getValueAt(row, 1).toString()))
							movementTable.setValueAt(changedRowMovement.getSuggestedFrames(), row, 1);	
					}
					else {
						try {
							if (Integer.parseInt(movementTable.getValueAt(row, 1).toString()) < changedRowMovement.getMinFrames())
								movementTable.setValueAt(changedRowMovement.getMinFrames(), row, 1);
						}
						catch (NumberFormatException ex) {
							movementTable.setValueAt(changedRowMovement.getSuggestedFrames(), row, 1);	
						}
					}
				}

				saveMidairs();
				if (initialized && saved && Properties.isUnsaved()) {
					saved = false;
					f.setTitle("*" + projectName);
				}
			}
		});
		
	
		//BUTTONS AND ERROR MESSAGE
		
		JPanel buttons = new JPanel(new BorderLayout());
		JPanel error = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel movementEdit = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel calculateVectorPanel = new JPanel();
		//movementEdit.add(movementEditInside, BorderLayout.WEST);
		buttons.add(movementEdit, BorderLayout.WEST);
		buttons.add(calculateVectorPanel, BorderLayout.EAST);
		errorMessage = new JLabel("");
		errorMessage.setForeground(Color.RED);
		error.add(errorMessage);
		buttons.add(error, BorderLayout.SOUTH);
		
		add = new JButton("+");
		remove = new JButton("-");
		solveVector = new JButton("Solve");
		calculateVector = new JButton("Calculate Vectors");
		add.setActionCommand("add");
		remove.setActionCommand("remove");
		calculateVector.setActionCommand("calculate");
		solveVector.setActionCommand("solve");
		
		movementEdit.add(add);
		movementEdit.add(remove);
		calculateVectorPanel.add(solveVector);
		calculateVectorPanel.add(calculateVector);
		
		ButtonListener buttonListen = new ButtonListener();
		add.addActionListener(buttonListen);
		remove.addActionListener(buttonListen);
		solveVector.addActionListener(buttonListen);
		calculateVector.addActionListener(buttonListen);
		
		//addPreset(7);

		loadProperties(userDefaults, true);
		p.file = null; //so we don't save to it
		initialized = true;

		//CREATING THE WINDOW
		
		JPanel nonResize = new JPanel(new BorderLayout());
		nonResize.add(genPropertiesScrollPane, BorderLayout.NORTH);
		//nonResize.add(movementPropertiesScrollPane, BorderLayout.EAST);
		//nonResize.add(infoScrollPane, BorderLayout.CENTER);
		nonResize.add(movementScrollPane, BorderLayout.CENTER);
		nonResize.add(buttons, BorderLayout.SOUTH);
		//nonResize.setPreferredSize(new Dimension(infoTable.getPreferredSize().width, 450));
		
		/*
		JPanel resize = new JPanel(new BorderLayout());
		resize.add(infoScrollPane, BorderLayout.NORTH);
		resize.add(dataScrollPane, BorderLayout.CENTER);
		*/

		MainJMenuBar menuBar = new MainJMenuBar();
		f.setJMenuBar(menuBar);
		f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					menuBar.promptSaveAndClose();
				}
			});

		f.add(nonResize, BorderLayout.CENTER);
		
		//f.add(resize, BorderLayout.CENTER);
		f.setSize(600, 600);
		//f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		
		//DEBUG PREPOLUATE MOVEMENT
		
/* 		movementModel.addRow(new String[]{"Motion Cap Throw", "32"});
		movementModel.addRow(new String[]{"Dive", "25"});
		movementModel.addRow(new String[]{"Cap Bounce", "42"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "32"});
		movementModel.addRow(new String[]{"Dive", "25"}); */

/* 		movementModel.addRow(new String[]{"Motion Cap Throw", "9"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "3"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "8"});
		movementModel.addRow(new String[]{"Dive", "25"}); */

/* 		movementModel.addRow(new String[]{"Motion Cap Throw", "24"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "36"});
		movementModel.addRow(new String[]{"Rainbow Spin", "32"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "24"});
		movementModel.addRow(new String[]{"Dive", "25"}); */

/* 		movementModel.addRow(new String[]{"Rainbow Spin", "32"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "24"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "36"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "24"});
		movementModel.addRow(new String[]{"Dive", "25"}); */

/* 		movementModel.addRow(new String[]{"Homing Triple Throw", "36"});
		movementModel.addRow(new String[]{"Rainbow Spin", "32"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "42"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "25"}); */
		

/* 		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "42"});
		movementModel.addRow(new String[]{"Homing Triple Throw", "36"});
		movementModel.addRow(new String[]{"Rainbow Spin", "32"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "25"}); */

		//setAngleType(AngleType.BOTH);
		//p.initialAngle = 65;
		//p.targetAngle = 90;
		//*/
	}

}

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import com.vectorcalculator.Properties.GroundMode;
import com.vectorcalculator.Properties.GroundType;
import com.vectorcalculator.Properties.HctDirection;
import com.vectorcalculator.Properties.HctType;
import com.vectorcalculator.Properties.Mode;
//import com.apple.laf.ClientPropertyApplicator.Property;
//import com.vectorcalculator.Properties.AngleType;
import com.vectorcalculator.Properties.CalculateUsing;

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

	static final int GENERAL_TAB = 0, MIDAIR_TAB = 1;
	
	static enum Parameter {
		mode("Calculator Mode"), initial_coordinates("Initial Coordinates"), calculate_using("Calculate Using"),
		initial_angle("Initial Angle"), target_angle("Target Angle"), target_coordinates("Target Coordinates"),
		midairs("Midairs"), triple_throw("Triple Throw"), gravity("Gravity"), hyperoptimize("Hyperoptimize Cap Throws"), zero_axis("0 Degree Axis"), camera("Camera Angle"),
		custom_camera_angle("Custom Camera Angle"), initial_movement_category("Initial Movement Category"), initial_movement("Initial Movement Type"),
		duration_type("Duration Type"), initial_frames("Frames"), initial_displacement("Vertical Displacement"),
		jump_button_frames("Frames of Holding A/B"), moonwalk_frames("Moonwalk Frames"), initial_speed("Initial Horizontal Speed"),
		vector_direction("Vector Direction"), dive_angle("Edge Cap Bounce Angle"),
		dive_angle_tolerance("Edge Cap Bounce Angle Tolerance"), dive_deceleration("First Dive Deceleration"),
		dive_turn("Turn During First Dive"), hct_type("Homing Throw Type"), hct_angle("Homing Throw Angle"),
		hct_neutral("Neutral Joystick During Homing"), hct_direction("Homing Direction"),
		hct_homing_frame("Frames Before Home"), hct_cap_return_frame("Frames Until Cappy Returns"),
		ground_mode("Ground Under Midairs"), ground_type("Type"), ground_height("Height"),
		ground_type_firstGP("Type Under First GP"), ground_height_firstGP("Height Under First GP"),
		ground_type_CB("Type Under CB"), ground_height_CB("Height Under CB"),
		ground_type_secondGP("Type Under Second GP"), ground_height_secondGP("Height Under Second GP"),
		upwarp("Maximum Upwarp");

		String name;

		Parameter(String name) {
			this.name = name;
		}
	}

	static ArrayList<Parameter> rowParams = new ArrayList<Parameter>();

	static ArrayList<Parameter> getRowParams() {
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		if (p.currentTab == GENERAL_TAB) {
			params.add(Parameter.mode);
			params.add(null);
			params.add(Parameter.initial_coordinates);
			params.add(Parameter.calculate_using);
			if (p.initialAngleGiven)
				params.add(Parameter.initial_angle);
			if (p.targetAngleGiven)
				params.add(Parameter.target_angle);
			if (p.targetCoordinatesGiven)
				params.add(Parameter.target_coordinates);
			params.add(null);
			params.add(Parameter.initial_movement_category);
			if (!p.initialMovementCategory.equals("Optimal Distance Motion"))
				params.add(Parameter.initial_movement);
			if (p.chooseDurationType)
				params.add(Parameter.duration_type);
			if (p.durationFrames)
				params.add(Parameter.initial_frames);
			else if (p.mode != Mode.SOLVE)
				params.add(Parameter.initial_displacement);
			if (p.chooseJumpFrames)
				params.add(Parameter.jump_button_frames);
			if (p.canMoonwalk)
				params.add(Parameter.moonwalk_frames);
			if (p.chooseInitialHorizontalSpeed)
				params.add(Parameter.initial_speed);
			params.add(Parameter.vector_direction);
			params.add(null);
			params.add(Parameter.midairs);
			if (p.canTripleThrow)
				params.add(Parameter.triple_throw);
			params.add(Parameter.upwarp);
			params.add(null);
			params.add(Parameter.gravity);
			params.add(Parameter.zero_axis);
			params.add(Parameter.camera);
			if (p.cameraType == CameraType.CUSTOM)
				params.add(Parameter.custom_camera_angle);
		}
		else if (p.currentTab == MIDAIR_TAB) {
			params.add(Parameter.mode);
			params.add(null);
			params.add(Parameter.midairs);
			if (p.canTripleThrow)
				params.add(Parameter.triple_throw);
			params.add(Parameter.upwarp);
			if (p.diveCapBounce) {
				params.add(null);
				params.add(Parameter.dive_angle);
				params.add(Parameter.dive_angle_tolerance);
				params.add(Parameter.dive_deceleration);
				params.add(Parameter.dive_turn);
			}
			if (p.hct) {
				params.add(null);
				params.add(Parameter.hct_type);
				if (p.hctType == HctType.CUSTOM) {
					params.add(Parameter.hct_angle);
					params.add(Parameter.hct_neutral);
					params.add(Parameter.hct_direction);
					params.add(Parameter.hct_homing_frame);
					params.add(Parameter.hct_cap_return_frame);
				}
			}
			params.add(null);
			params.add(Parameter.ground_mode);
			if (p.groundMode == GroundMode.UNIFORM) {
				params.add(Parameter.ground_type);
				if (p.groundType != GroundType.NONE)
					params.add(Parameter.ground_height);
			}
			else if (p.groundMode == GroundMode.VARIED) {
				params.add(Parameter.ground_type_firstGP);
				if (p.groundTypeFirstGP != GroundType.NONE)
					params.add(Parameter.ground_height_firstGP);
				params.add(Parameter.ground_type_CB);
				if (p.groundTypeCB != GroundType.NONE)
					params.add(Parameter.ground_height_CB);
				params.add(Parameter.ground_type_secondGP);
				if (p.groundTypeSecondGP != GroundType.NONE)
					params.add(Parameter.ground_height_secondGP);
			}
		}
		return params;
	}

	//refreshes property rows to display the parameters in the array
	static void refreshPropertiesRows(ArrayList<Parameter> params, boolean forceRefresh) {
		if (p.currentTab != tabbedPane.getSelectedIndex())
			tabbedPane.setSelectedIndex(p.currentTab);
		else if (params.equals(rowParams) && !forceRefresh) {
			//System.out.println("No need to refresh");
			return;
		}
		rowParams = params;
		genPropertiesModel.setRowCount(0);
		settingPropertyRow = true;
		for (Parameter param : params) {
			if (param == null) {
				genPropertiesModel.addRow(new Object[]{"", ""});
				genPropertiesTable.setRowHeight(genPropertiesTable.getRowCount() - 1, 10);
			}
			else
				genPropertiesModel.addRow(new Object[]{param.name, PropertyToDisplayValue(param)});
		}
		settingPropertyRow = false;
	}

	static void setPropertiesRow(Parameter param) {
		int index = rowParams.indexOf(param);
		if (index != -1)
			setPropertiesRow(index);
	}

	static void setPropertiesRow(int row) {
		settingPropertyRow = true;
		// for (Parameter p : rowParams) {
		// 	System.out.print(p + ", ");
		// }
		// System.out.println();
		Parameter param = rowParams.get(row);
		//System.out.println(param + ", " + row);
		genPropertiesTable.setValueAt(param.name, row, 0);
		genPropertiesTable.setValueAt(PropertyToDisplayValue(param), row, 1);
		settingPropertyRow = false;
	}
	
	static String PropertyToDisplayValue(Parameter param) {
		Object value;
		switch(param) {
		case mode:
			value = p.mode.name;
			break;
		case initial_coordinates:
			value = toCoordinateString(p.x0, p.y0, p.z0);
			break;
		case calculate_using:
			value = p.calculateUsing.name;
			break;
		case initial_angle:
			value = p.initialAngle;
			break;
		case target_angle:
			value = p.targetAngle;
			break;
		case target_coordinates:
			value = toCoordinateString(p.x1, p.y1, p.z1);
			break;
		case midairs:
			value = p.midairPreset;
			break;
		case triple_throw:
			value = p.tripleThrow ? "Yes" : "No";
			break;
		case upwarp:
			value = p.upwarp;
			break;
		case gravity:
			value = p.onMoon ? "Moon" : "Regular";
			break;
		case hyperoptimize:
			value = p.hyperoptimize ? "Yes" : "No";
			break;
		case zero_axis:
			value = p.xAxisZeroDegrees ? "X" : "Z";
			break;
		case camera:
			value = p.cameraType.name;
			break;
		case custom_camera_angle:
			value = p.customCameraAngle;
			break;
		case initial_movement_category:
			value = p.initialMovementCategory;
			break;
		case initial_movement:
			value = p.initialMovementName;
			break;
		case duration_type:
			value = p.durationFrames ? "Frames" : "Vertical Displacement";
			break;
		case initial_frames:
			value = p.initialFrames;
			break;
		case initial_displacement:
			value = p.initialDispY;
			break;
		case jump_button_frames:
			value = p.framesJump;
			break;
		case moonwalk_frames:
			value = p.framesMoonwalk;
			break;
		case initial_speed:
			value = p.initialHorizontalSpeed;
			break;
		case vector_direction:
			value = p.rightVector ? "Right" : "Left";
			break;
		case dive_angle:
			value = round(p.diveCapBounceAngle, 3);
			break;
		case dive_angle_tolerance:
			value = round(p.diveCapBounceTolerance, 3);
			break;
		case dive_deceleration:
			value = round(p.diveFirstFrameDecel, 3);
			break;
		case dive_turn:
			value = p.diveTurn ? "Yes" : "No";
			break;
		case hct_type:
			value = p.hctType.name;
			break;
		case hct_angle:
			value = p.hctThrowAngle;
			break;
		case hct_neutral:
			value = p.hctNeutralHoming ? "Yes" : "No";
			break;
		case hct_direction:
			value = p.hctDirection.name;
			break;
		case hct_homing_frame:
			value = p.hctHomingFrame;
			break;
		case hct_cap_return_frame:
			value = p.hctCapReturnFrame;
			break;
		case ground_mode:
			value = p.groundMode.name;
			break;
		case ground_type:
			value = p.groundType.name;
			break;
		case ground_height:
			value = p.groundHeightFirstGP;
			break;
		case ground_type_firstGP:
			value = p.groundTypeFirstGP.name;
			break;
		case ground_height_firstGP:
			value = p.groundHeightFirstGP;
			break;
		case ground_type_CB:
			value = p.groundTypeCB.name;
			break;
		case ground_height_CB:
			value = p.groundHeightCB;
			break;
		case ground_type_secondGP:
			value = p.groundTypeSecondGP.name;
			break;
		case ground_height_secondGP:
			value = p.groundHeightSecondGP;
			break;
		default:
			return "";
		}
		String valueString = value.toString();
		try {
			double valueDouble = Double.parseDouble(valueString);
			return numberToString(valueDouble);
		}
		catch (Exception ex) {
			return valueString;
		}
	}

	// static void setPropertiesRow(int row, String key, Object value) {
	// 	genPropertiesTable.setValueAt(key, row, 0);
	// 	genPropertiesTable.setValueAt(value, row, 0);
	// }

	static double parseDoubleWithDefault(Object value, double defaultVal) {
		try {
			return Double.parseDouble(value.toString());
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	static int parseIntWithDefault(Object value, int defaultVal) {
		try {
			return Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	static double clampDouble(double val, double min, double max) {
		return Math.max(min, Math.min(max, val));
	}

	static int clampInt(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	static void parseCoordinates(String coordString, double[] coords) {
		try {
			String s = coordString.replaceAll("[()\\s]", "");
			String[] parts = s.split(",");
			for (int i = 0; i < Math.min(3, parts.length); i++) {
				coords[i] = Double.parseDouble(parts[i].trim());
			}
		} catch (Exception e) {
			// keep defaults
		}
	}

	static void setProperty(Parameter param) {
		int index = rowParams.indexOf(param);
		if (index != -1)
			setProperty(index);
	}

	static void setProperty(int row) {
		if (row >= 0)
			setProperty(rowParams.get(row), genPropertiesTable.getValueAt(row, 1));
	}

	//sets a property value based on the display value in the table
	static void setProperty(Parameter param, Object value) {
		switch(param) {
		case mode:
			p.mode = Properties.Mode.fromName(value.toString());
			if (p.mode == Mode.SOLVE) {
				setProperty(Parameter.gravity, "Regular");
				if (p.midairPreset.equals("Custom"))
					setProperty(Parameter.midairs, "Spinless");
			}
			else if (p.mode == Mode.SOLVE_CB) {
				setProperty(Parameter.gravity, "Regular");
			}
			calculateVector.setText(p.mode.name);
			break;
		case initial_coordinates:
			double[] coords = new double[3];
			parseCoordinates(value.toString(), coords);
			p.x0 = coords[0];
			p.y0 = coords[1];
			p.z0 = coords[2];
			break;
		case calculate_using:
			String val = value.toString();
			p.calculateUsing = Properties.CalculateUsing.fromName(val);
			updateCalculateUsing();
			break;
		case target_coordinates:
			double[] tcoords = new double[3];
			parseCoordinates(value.toString(), tcoords);
			p.x1 = tcoords[0];
			p.y1 = tcoords[1];
			p.z1 = tcoords[2];
			setProperty(Parameter.target_angle, targetCoordinatesToTargetAngle());
			break;
		case initial_angle:
			p.initialAngle = parseDoubleWithDefault(value, 0);
			break;
		case target_angle:
			p.targetAngle = parseDoubleWithDefault(value, 0);
			break;
		case initial_movement_category:
			String oldInitialMovementCategory = p.initialMovementCategory;
			p.initialMovementCategory = value.toString();
			if (!oldInitialMovementCategory.equals(p.initialMovementCategory)) {
				p.initialMovementName = initialMovementDefaults[Arrays.asList(initialMovementCategories).indexOf(p.initialMovementCategory)];
				updateInitialMovement();
			}
			break;
		case initial_movement:
			p.initialMovementName = value.toString();
			updateInitialMovement();
			break;
		case duration_type:
			boolean oldDurationFrames = p.durationFrames;
			p.durationFrames = value.toString().equals("Frames");
			initialMovement.initialHorizontalSpeed = p.initialHorizontalSpeed;
			initialMotion = initialMovement.getMotion(p.initialFrames, false, false);
			if (p.durationFrames && !oldDurationFrames)
				setProperty(Parameter.initial_frames, initialMotion.calcFrames(p.initialDispY - getMoonwalkDisp()));
			else if (!p.durationFrames && oldDurationFrames)
				setProperty(Parameter.initial_displacement, initialMotion.calcDispY(p.initialFrames) + getMoonwalkDisp());
			break;
		case initial_frames:
			int minFrames = initialMovement.minFrames;
			p.initialFrames = clampInt(parseIntWithDefault(value, minFrames), minFrames, Integer.MAX_VALUE);
			break;
		case initial_displacement:
			p.initialDispY = parseDoubleWithDefault(value, 0);
			break;
		case jump_button_frames:
			p.framesJump = clampInt(parseIntWithDefault(value, 1), 1, 10);
			break;
		case moonwalk_frames:
			p.framesMoonwalk = clampInt(parseIntWithDefault(value, 0), 0, 5);
			break;
		case initial_speed:
			if (p.chooseInitialHorizontalSpeed)
				p.initialHorizontalSpeed = Math.max(0, parseDoubleWithDefault(value, 0));
			break;
		case vector_direction:
			p.rightVector = value.toString().equals("Right");
			break;
		case dive_angle:
			p.diveCapBounceAngle = clampDouble(parseDoubleWithDefault(value, 0), 0, 41.2);
			break;
		case dive_angle_tolerance:
			p.diveCapBounceTolerance = clampDouble(parseDoubleWithDefault(value, 0), 0, 1);
			break;
		case dive_deceleration:
			double decel = parseDoubleWithDefault(value, 0);
			if (decel > 0.5) decel = 0.5;
			else if (decel <= 0.05 && decel != 0) decel = 0;
			p.diveFirstFrameDecel = decel;
			break;
		case midairs:
			String name = value.toString();
			boolean oldCanTripleThrow = p.canTripleThrow;
			p.canTripleThrow = !(name.equals("Simple Tech Rainbow Spin First") || name.equals("Custom"));
			if (!p.canTripleThrow || (!oldCanTripleThrow && p.canTripleThrow))
				p.tripleThrow = false;
			if (!name.equals(p.midairPreset))
				addPreset(name, false);
			if (name.equals("Custom") && p.mode == Mode.SOLVE) {
				System.out.println("Should switch");
				setProperty(Parameter.mode, Mode.SOLVE_CB.name);
				
			}
			break;
		case triple_throw:
			boolean oldTripleThrow = p.tripleThrow;
			p.tripleThrow = value.toString().equals("Yes");
			if (oldTripleThrow != p.tripleThrow)
				addPreset(p.midairPreset, false);
			break;
		case gravity:
			p.onMoon = value.toString().equals("Moon");
			break;
		case upwarp:
			p.upwarp = clampDouble(parseDoubleWithDefault(value, 40), 0, 40);
			break;
		case hyperoptimize:
			p.hyperoptimize = value.toString().equals("Yes");
			break;
		case zero_axis:
			p.xAxisZeroDegrees = value.toString().equals("X");
			break;
		case camera:
			p.cameraType = Properties.CameraType.fromName(value.toString());
			break;
		case custom_camera_angle:
			p.customCameraAngle = parseDoubleWithDefault(value, 0);
			break;
		case dive_turn:
			p.diveTurn = value.toString().equals("Yes");
			break;
		case hct_type:
			p.hctType = Properties.HctType.fromName(value.toString());
			if (p.hctType == HctType.RELAX) {
				setProperty(Parameter.hct_angle, 60);
				setProperty(Parameter.hct_neutral, "Yes");
				setProperty(Parameter.hct_direction, "Down");
				setProperty(Parameter.hct_homing_frame, 19);
				setProperty(Parameter.hct_cap_return_frame, 36);
			}
			else if (p.hctType == HctType.RELAXLESS) {
				setProperty(Parameter.hct_angle, 60);
				setProperty(Parameter.hct_neutral, "No");
				setProperty(Parameter.hct_direction, "Down");
				setProperty(Parameter.hct_homing_frame, 19);
				setProperty(Parameter.hct_cap_return_frame, 38);
			}
			break;
		case hct_angle:
			p.hctThrowAngle = parseDoubleWithDefault(value, 60);
			break;
		case hct_neutral:
			p.hctNeutralHoming = value.toString().equals("Yes");
			break;
		case hct_direction:
			p.hctDirection = Properties.HctDirection.fromName(value.toString());
			break;
		case hct_homing_frame:
			p.hctHomingFrame = clampInt(parseIntWithDefault(value, 19), 0, Integer.MAX_VALUE);
			break;
		case hct_cap_return_frame:
			p.hctCapReturnFrame = clampInt(parseIntWithDefault(value, 36), 23, Integer.MAX_VALUE);
			updateHCTDuration();
			break;
		case ground_mode:
			GroundMode oldGroundMode = p.groundMode;
			p.groundMode = Properties.GroundMode.fromName(value.toString());
			if (p.groundMode == GroundMode.NONE)
				updateAllGround(GroundType.NONE, 0);
			else if (oldGroundMode == GroundMode.NONE)
				updateAllGround(GroundType.GROUND, 0);
			else if (p.groundMode == GroundMode.UNIFORM && oldGroundMode == GroundMode.VARIED)
				updateAllGround(p.groundTypeFirstGP, p.groundHeightFirstGP);
			break;
		case ground_type:
			updateAllGround(Properties.GroundType.fromName(value.toString()), p.groundHeight);
			break;
		case ground_height:
			updateAllGround(p.groundType, parseDoubleWithDefault(value, 0));
			break;
		case ground_type_firstGP:
			p.groundTypeFirstGP = Properties.GroundType.fromName(value.toString());
			break;
		case ground_height_firstGP:
			p.groundHeightFirstGP = parseDoubleWithDefault(value, 0);
			break;
		case ground_type_CB:
			p.groundTypeCB = Properties.GroundType.fromName(value.toString());
			break;
		case ground_height_CB:
			p.groundHeightCB = parseDoubleWithDefault(value, 0);
			break;
		case ground_type_secondGP:
			p.groundTypeSecondGP = Properties.GroundType.fromName(value.toString());
			break;
		case ground_height_secondGP:
			p.groundHeightSecondGP = parseDoubleWithDefault(value, 0);
			break;
		}
		setPropertiesRow(param); //refresh this row
	}

	static void updateAllGround(GroundType groundType, double groundHeight) {
		p.groundType = groundType;
		p.groundHeight = groundHeight;
		p.groundTypeFirstGP = p.groundType;
		p.groundHeightFirstGP = p.groundHeight;
		p.groundTypeCB = p.groundType;
		p.groundHeightCB = p.groundHeight;
		p.groundTypeSecondGP = p.groundType;
		p.groundHeightSecondGP = p.groundHeight;
	}

	static void updateCalculateUsing() {
		boolean oldInitialAngleGiven = p.initialAngleGiven;
		boolean oldTargetAngleGiven = p.targetAngleGiven;
		boolean oldTargetCoordinatesGiven = p.targetCoordinatesGiven;
		p.initialAngleGiven = (p.initialAndTargetGiven || p.calculateUsing == CalculateUsing.INITIAL_ANGLE);
		p.targetAngleGiven = (p.calculateUsing == CalculateUsing.TARGET_ANGLE);
		p.targetCoordinatesGiven = (p.calculateUsing == CalculateUsing.TARGET_COORDINATES);
		//handle if we don't know what the value should be
		if (p.initialAngleGiven && !oldInitialAngleGiven)
			setProperty(Parameter.initial_angle, p.targetAngle);
		if (p.targetAngleGiven && !(oldTargetAngleGiven || oldTargetCoordinatesGiven))
			setProperty(Parameter.target_angle, p.initialAngle);
		if (p.targetCoordinatesGiven && !oldTargetCoordinatesGiven)
			setProperty(Parameter.target_coordinates, "(0, 0, 0)");
	}

	//category for falling for height calculator?
	//static String[] initialMovementCategories = {"Distance Jumps", "Height Jumps", "Roll Cancel Vectors", "Rolls", "Object-Dependent Motion"};
	static String[] initialMovementCategories = {"Jump", "RCV", "Roll", "Fork Flick", "Bounce", "Misc", "Optimal Distance Motion"};
	static String[][] initialMovementNames =
		{{"Single Jump", "Double Jump", "Triple Jump", "Vault", "Cap Return Jump", "Long Jump", "Ground Pound Jump", "Backflip", "Sideflip", "Spin Jump"},
		{"Motion Cap Throw RCV", "Single Throw RCV", "Upthrow RCV", "Downthrow RCV", "Double Throw RCV", "Spinthrow RCV", "Triple Throw RCV", "Fakethrow RCV", "Optimal Distance RCV"},
		{"Ground Pound Roll", "Crouch Roll", "Roll Boost"},
		{"Horizontal Pole/Fork Flick", "Motion Horizontal Pole/Fork Flick", "Motion Vertical Pole/Fork Flick"},
		{"Small NPC Bounce", "Large NPC Bounce", "Ground Pound Object/Enemy Bounce", "Bouncy Object Bounce", "Flower Bounce"},
		{"Uncapture", "Flip Forward", "Swinging Jump"},
		{"Optimal Distance Motion", "Optimal Distance RCV"}};
	static String[] initialMovementDefaults = {"Triple Jump", "Motion Cap Throw RCV", "Ground Pound Roll", "Motion Horizontal Pole/Fork Flick", "Large NPC Bounce", "Uncapture", "Optimal Distance Motion"};
	// static String[][] initialMovementNames =
	// 	{{"Single Jump", "Double Jump", "Triple Jump", "Vault", "Cap Return Jump", "Long Jump", "Optimal Distance Motion"},
	// 	{"Triple Jump", "Ground Pound Jump", "Backflip", "Sideflip", "Vault", "Spin Jump"},
	// 	{"Motion Cap Throw RCV", "Single Throw RCV", "Upthrow RCV", "Downthrow RCV", "Double Throw RCV", "Spinthrow RCV", "Triple Throw RCV", "Fakethrow RCV", "Optimal Distance RCV"},
	// 	{"Ground Pound Roll", "Crouch Roll", "Roll Boost"},
	// 	{"Horizontal Pole/Fork Flick", "Motion Horizontal Pole/Fork Flick", "Motion Vertical Pole/Fork Flick", "Small NPC Bounce", "Large NPC Bounce", "Ground Pound Object/Enemy Bounce", "Uncapture", "Bouncy Object Bounce", "Flower Bounce", "Flip Forward", "Swinging Jump"}}; //flower spinpound for height calculator
	
	static String[] midairPresetNames = {"Spinless", "Simple Tech", "Simple Tech Rainbow Spin First", "MCCT First", "CBV First", "Custom"};
	//static String[] midairPresetsWithTT = {"Spinless", "Spinless (Triple Throw)", "Simple Tech", "Simple Tech (Triple Throw)", "Simple Tech Rainbow Spin First", "MCCT First", "MCCT First (Triple Throw)", "CBV First", "CBV First (Triple Throw)"};
	
	static String[] midairMovementNames = {"Motion Cap Throw", "Triple Throw", "Homing Motion Cap Throw", "Homing Triple Throw", "Rainbow Spin", "Dive", "Cap Bounce", "2P Midair Vault"};

	static final int MCCT = 0, TT = 1, HMCCT = 2, HTT = 3, RS = 4, DIVE = 5, CB = 6, P2CB = 7;

	// static final int[][][] midairPresets =
	// 	//custom (nothing to start)
	// 	{new int[0][0],
	// 	//spinless
	//  	{{MCCT, 28}, {DIVE, 25}, {CB, 44}, {MCCT, 31}, {DIVE, 25}},
	// 	//simple tech
	// 	{{MCCT, 28}, {DIVE, 25}, {CB, 43}, {RS, 32}, {MCCT, 30}, {DIVE, 25}},
	// 	//simple tech rainbow spin first
	// 	{{RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 43}, {MCCT, 30}, {DIVE, 25}},
	// 	//mcct first
	// 	{{HMCCT, 36}, {RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 42}, {MCCT, 31}, {DIVE, 25}},
	// 	//tt first
	// 	{{HTT, 30}, {RS, 32}, {MCCT, 28}, {DIVE, 26}, {CB, 42}, {MCCT, 31}, {DIVE, 25}},
	// 	//cbv first
	// 	{{MCCT, 28}, {DIVE, 25}, {CB, 42}, {HMCCT, 36}, {RS, 32}, {MCCT, 31}, {DIVE, 25}},
	// 	//cbv first tt
	// 	{{MCCT, 28}, {DIVE, 26}, {CB, 42}, {HTT, 30}, {RS, 32}, {MCCT, 30}, {DIVE, 24}}};

	// static int[][] spinless = {{MCCT, 28}, {DIVE, 25}, {CB, 44}, {MCCT, 31}, {DIVE, 25}};
	// static int[][] simpleTech = {{MCCT, 28}, {DIVE, 25}, {CB, 43}, {RS, 32}, {MCCT, 30}, {DIVE, 25}};
	// static int[][] simpleTechRainbowSpinFirst = {{RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 43}, {MCCT, 30}, {DIVE, 25}};
	// static int[][] mcctFirst = {{HMCCT, 36}, {RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 42}, {MCCT, 31}, {DIVE, 25}};
	// static int[][] ttFirst = {{HTT, 30}, {RS, 32}, {MCCT, 28}, {DIVE, 26}, {CB, 42}, {MCCT, 31}, {DIVE, 25}};
	// static int[][] cbvFirst = {{MCCT, 28}, {DIVE, 25}, {CB, 42}, {HMCCT, 36}, {RS, 32}, {MCCT, 31}, {DIVE, 25}};
	// static int[][] cbvFirstTT = {{MCCT, 28}, {DIVE, 26}, {CB, 42}, {HTT, 30}, {RS, 32}, {MCCT, 30}, {DIVE, 24}};

	static void saveMidairs() {
		p.midairs = new int[movementModel.getRowCount()][2];
		p.hct = false;
		p.diveCapBounce = false;
		List<String> types = Arrays.asList(midairMovementNames);
		for (int i = 0; i < movementModel.getRowCount(); i++) {
			p.midairs[i][0] = types.indexOf(movementModel.getValueAt(i, 0).toString());
			p.midairs[i][1] = Integer.parseInt(movementModel.getValueAt(i, 1).toString());
			if (i > 0 && p.midairs[i][0] == CB && p.midairs[i - 1][0] == DIVE)
				p.diveCapBounce = true;
			else if (p.midairs[i][0] == HMCCT)
				p.hct = true;
		}
		/* for (int i = 0; i < p.midairs.length; i++)
			System.out.println(p.midairs[i][0] + ", " + p.midairs[i][1]); */
	}

	static void updateHCTDuration() {
		for (int i = 0; i < movementModel.getRowCount(); i++) {
			if (movementModel.getValueAt(i, 0).toString().equals("Homing Motion Cap Throw")) {
				int currentDuration = Integer.parseInt(movementModel.getValueAt(i, 1).toString());
				if (currentDuration < p.hctCapReturnFrame)
					movementModel.setValueAt(p.hctCapReturnFrame, i, 1);
			}
		}
	}

	//static final int LOCK_NONE = 0;
	//static final int LOCK_FRAMES = 1;
	//static final int LOCK_VERTICAL_DISPLACEMENT = 2;

	static int lastInitialMovementFrame;
	
	static Movement initialMovement = new Movement(p.initialMovementName);
	static SimpleMotion initialMotion = new SimpleMotion(initialMovement, p.initialFrames);
	
	static boolean settingPropertyRow = false;
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
	//static JButton solveVector;
	static JButton calculateVector;

	static String[] genPropertiesTitles = {"Property", "Value"};
	// static Object[][] genProperties =
	// 	{{"Initial Coordinates", "(0, 0, 0)"},
	// 	{"Calculate Using", "Target Coordinates"},
	// 	{"Target Coordinates", "(0, 0, 3000)"},
	// 	{"Initial Movement Type", p.initialMovementName},
	// 	{"Initial Movement Duration Type", "Frames"},
	// 	{"Initial Movement Frames", p.initialFrames},
	// 	{"Frames of Holding A/B", p.framesJump},
	// 	{"Moonwalk Frames", p.framesMoonwalk},
	// 	{"Initial Horizontal Speed", (int) p.initialHorizontalSpeed},
	// 	{"Initial Vector Direction", "Left"},
	// 	{"Edge Cap Bounce Angle", p.diveCapBounceAngle},
	// 	{"Edge Cap Bounce Angle Tolerance", p.diveCapBounceTolerance},
	// 	{"First Dive Deceleration", "0"},
	// 	{"Midairs", "Spinless"},
	// 	{"Gravity", "Regular"},
	// 	{"Hyperoptimize Cap Throws", "True"},
	// 	{"0 Degree Axis", "X"},
	// 	{"Camera Angle", "Target Angle"}};
	
	static JFrame f = new JFrame(projectName);
	static JPanel all;
	static JTabbedPane tabbedPane;

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

	// public static void lockDurationType(int value) {
	// 	p.lockDurationType = value;
	// 	Debug.println(p.lockDurationType);
	// 	if (p.lockDurationType == LOCK_FRAMES) {
	// 		genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_TYPE_ROW, 1);
	// 		genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_ROW, 0);
	// 		if (p.durationFrames == false) {
	// 			p.durationFrames = true;
	// 			genPropertiesTable.setValueAt(initialMovement.minRecommendedFrames, MOVEMENT_DURATION_ROW, 0);
	// 			p.initialFrames = initialMovement.minRecommendedFrames;
	// 		}
	// 	}
	// 	else if (p.lockDurationType == LOCK_VERTICAL_DISPLACEMENT) {
	// 		genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_TYPE_ROW, 1);
	// 		genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
	// 		if (p.durationFrames == true) {
	// 			p.durationFrames = false;
	// 			genPropertiesTable.setValueAt(0, MOVEMENT_DURATION_ROW, 1);
	// 			p.initialDispY = 0;
	// 		}
	// 	}
	// }

	// public static void setAngleType(AngleType type, boolean coordinates) {
	// 	forceEdit = true;
	// 	AngleType oldAngleType = p.angleType;
	// 	Debug.println(oldAngleType);
	// 	Debug.println(type);
	// 	p.angleType = type;
	// 	if (oldAngleType != AngleType.BOTH && type == AngleType.BOTH) {
	// 		addAngle2Row();
	// 		genPropertiesTable.setValueAt("Initial Angle", ANGLE_2_ROW, 0);
	// 		if (oldAngleType == AngleType.INITIAL) {
	// 			p.targetAngle = p.initialAngle;
	// 			genPropertiesTable.setValueAt("Target Angle", ANGLE_TYPE_ROW, 1);
	// 			genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
	// 			genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
	// 		}
	// 		else if (oldAngleType == AngleType.TARGET) {
	// 			p.initialAngle = p.targetAngle;
	// 		}
	// 		genPropertiesTable.setValueAt(p.initialAngle, ANGLE_2_ROW, 1);
	// 	}
	// 	else if (oldAngleType == AngleType.BOTH && type == AngleType.TARGET) {
	// 		removeAngle2Row();
	// 		//genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
	// 		//genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
	// 		//genPropertiesTable.setValueAt("Target Angle", ANGLE_TYPE_ROW, 1);
	// 	}
	// 	else if (oldAngleType == AngleType.BOTH && type == AngleType.INITIAL) {
	// 		removeAngle2Row();
	// 		coordinates = false;
	// 		genPropertiesTable.setValueAt("Initial Angle", ANGLE_ROW, 0);
	// 		genPropertiesTable.setValueAt(p.initialAngle, ANGLE_ROW, 1);
	// 		genPropertiesTable.setValueAt("Initial Angle", ANGLE_TYPE_ROW, 1);
	// 	}
	// 	else if (oldAngleType == AngleType.TARGET && type == AngleType.INITIAL) {
	// 		if (coordinates) {
	// 			p.initialAngle = round(p.targetAngle, 3);
	// 			coordinates = false;
	// 		}
	// 		else {
	// 			p.initialAngle = p.targetAngle;
	// 		}
	// 		genPropertiesTable.setValueAt("Initial Angle", ANGLE_ROW, 0);
	// 		genPropertiesTable.setValueAt(p.initialAngle, ANGLE_ROW, 1);
	// 	}
	// 	else if (oldAngleType == AngleType.INITIAL && type == AngleType.TARGET) {
	// 		if (coordinates) {
	// 			genPropertiesTable.setValueAt("Target Coordinates", ANGLE_ROW, 0);
	// 			genPropertiesTable.setValueAt("(0, 0, 0)", ANGLE_ROW, 1);
	// 			p.x1 = 0;
	// 			p.y1 = 0;
	// 			p.z1 = 0;
	// 		}
	// 		else {
	// 			genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
	// 			p.targetAngle = p.initialAngle;
	// 		}
	// 	}
	// 	else if (oldAngleType == AngleType.TARGET && type == AngleType.TARGET || oldAngleType == AngleType.BOTH && type == AngleType.BOTH) {
	// 		if (!p.targetCoordinates && coordinates) {
	// 			genPropertiesTable.setValueAt("Target Coordinates", ANGLE_ROW, 0);
	// 			genPropertiesTable.setValueAt("(0, 0, 0)", ANGLE_ROW, 1);
	// 			p.x1 = 0;
	// 			p.y1 = 0;
	// 			p.z1 = 0;
	// 		}
	// 		else if (p.targetCoordinates && !coordinates) {
	// 			genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
	// 			if (p.targetAngle == (int) p.targetAngle)
	// 				genPropertiesTable.setValueAt((int) p.targetAngle, ANGLE_ROW, 1);
	// 			else {
	// 				genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
	// 			}
	// 		}
	// 	}
	// 	Debug.println("Initial Angle: " + p.initialAngle);
	// 	Debug.println("Target Angle: " + p.targetAngle);
	// 	p.targetCoordinates = coordinates;
	// 	forceEdit = false;
	// }

	// public static void setCameraType(CameraType type) {
	// 	Debug.println("Setting camera type to " + type);
	// 	CameraType oldCameraType = p.cameraType;
	// 	p.cameraType = type;
	// 	if (p.cameraType == CameraType.CUSTOM && oldCameraType != CameraType.CUSTOM) {
	// 		if ((int) p.customCameraAngle == p.customCameraAngle) {
	// 			genPropertiesModel.addRow(new Object[]{"Custom Camera Angle", (int) p.customCameraAngle});
	// 		}
	// 		else {
	// 			genPropertiesModel.addRow(new Object[]{"Custom Camera Angle", p.customCameraAngle});
	// 		}
	// 	}
	// 	else if (p.cameraType != CameraType.CUSTOM && oldCameraType == CameraType.CUSTOM) {
	// 		genPropertiesModel.removeRow(genPropertiesModel.getRowCount() - 1);
	// 	}
	// }

	public static double targetCoordinatesToTargetAngle() {
		double targetAngle = Math.toDegrees(Math.atan2(p.x1 - p.x0, p.z1 - p.z0));
		if (p.xAxisZeroDegrees)
			targetAngle = 90 - targetAngle;
		if (targetAngle < 0)
			targetAngle += 360;
		Debug.println("Target Angle from Coordinates: " + targetAngle);
		return targetAngle;
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

	public static int[][] getPreset(String name) {
		if (!p.tripleThrow) {
			switch(name) {
				case "Spinless":
					return new int[][]{{MCCT, 28}, {DIVE, 25}, {CB, 44}, {MCCT, 31}, {DIVE, 25}};
				case "Simple Tech":
					return new int[][]{{MCCT, 28}, {DIVE, 25}, {CB, 43}, {RS, 32}, {MCCT, 30}, {DIVE, 25}};
				case "Simple Tech Rainbow Spin First":
					return new int[][]{{RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 43}, {MCCT, 30}, {DIVE, 25}};
				case "MCCT First":
					return new int[][]{{HMCCT, 36}, {RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 42}, {MCCT, 31}, {DIVE, 25}};
				case "CBV First":
					return new int[][]{{MCCT, 28}, {DIVE, 25}, {CB, 42}, {HMCCT, 36}, {RS, 32}, {MCCT, 31}, {DIVE, 25}};
				default:
					return new int[0][0];
			}
		}
		else {
			switch(name) {
				case "Spinless":
					return new int[][]{{TT, 28}, {DIVE, 25}, {CB, 44}, {MCCT, 31}, {DIVE, 25}};
				case "Simple Tech":
					return new int[][]{{TT, 28}, {DIVE, 25}, {CB, 43}, {RS, 32}, {MCCT, 30}, {DIVE, 25}};
				case "MCCT First":
					return new int[][]{{HTT, 30}, {RS, 32}, {MCCT, 28}, {DIVE, 26}, {CB, 42}, {MCCT, 31}, {DIVE, 25}};
				case "CBV First":
					return new int[][]{{MCCT, 28}, {DIVE, 26}, {CB, 42}, {HTT, 30}, {RS, 32}, {MCCT, 30}, {DIVE, 24}};
				default:
					return new int[0][0];
			}
		}
	}

	//load being true means to load whatever the midairs actually are instead of their default values
	public static void addPreset(String name, boolean load) {
		if (name.equals("Custom")) {
			add.setEnabled(true);
			remove.setEnabled(true);
		}
		else {
			add.setEnabled(false);
			remove.setEnabled(false);
		}
		if (load)
			addPreset(p.midairs);
		else
			addPreset(getPreset(name));
		p.midairPreset = name;
	}

	//replaces the current midairs with the preset of the given index
	// public static void addPreset(int index) {
	// 	//Debug.println("Switching to preset " + index);
		
	// 	addPreset(midairPresets[index]);

	// 	if (index == 0) {
	// 		add.setEnabled(true);
	// 		remove.setEnabled(true);
	// 	}
	// 	else {
	// 		add.setEnabled(false);
	// 		remove.setEnabled(false);
	// 	}
	// 	p.currentPresetIndex = index;
	// }

	public static void addPreset(int[][] preset) {
		movementModel.setRowCount(0);
		for (int[] row : preset) {
			movementModel.addRow(new Object[]{midairMovementNames[row[0]], row[1]});
		}
		saveMidairs();
		// if (p.midairPreset.equals("Custom")) {
		// 	add.setEnabled(false);
		// 	remove.setEnabled(false);
		// }
	}

	public static void updateInitialMovement() {
		boolean suggestSpeed = p.initialHorizontalSpeed == initialMovement.getSuggestedSpeed();
		initialMovement = new Movement(p.initialMovementName);
		if (initialMovement.variableJumpFrames()) {
			if (!p.chooseJumpFrames) {
				p.chooseJumpFrames = true;
				p.framesJump = 10;
			}
		}
		else
			p.chooseJumpFrames = false;
		boolean oldCanMoonwalk = p.canMoonwalk;
		p.canMoonwalk = initialMovement.canMoonwalk;
		if (!(p.canMoonwalk && oldCanMoonwalk))
			p.framesMoonwalk = 0;
		p.chooseInitialHorizontalSpeed = initialMovement.variableInitialHorizontalSpeed();
		if (p.chooseInitialHorizontalSpeed) {
			if (suggestSpeed) //if the player is sticking to the suggested speed, give it again
				p.initialHorizontalSpeed = initialMovement.getSuggestedSpeed();
			else
				p.initialHorizontalSpeed = Math.min(p.initialHorizontalSpeed, initialMovement.trueSpeedCap);
		}
		else
			p.initialHorizontalSpeed = initialMovement.initialHorizontalSpeed;
		// if (initialMovement.variableInitialHorizontalSpeed()) {
		// 	if (suggestSpeed) {
		// 		p.chooseInitialHorizontalSpeed = true;
		// 		p.initialHorizontalSpeed = initialMovement.getSuggestedSpeed();
		// 		if (suggestedSpeed == (int) suggestedSpeed)
		// 			genPropertiesModel.setValueAt((int) initialMovement.getSuggestedSpeed(), INITIAL_HORIZONTAL_SPEED_ROW, 1);
		// 		else
		// 			genPropertiesModel.setValueAt(initialMovement.getSuggestedSpeed(), INITIAL_HORIZONTAL_SPEED_ROW, 1);
		// 	}
		// 	else if (capSpeed) {
		// 		genPropertiesModel.setValueAt(Math.min(p.initialHorizontalSpeed, initialMovement.trueSpeedCap), INITIAL_HORIZONTAL_SPEED_ROW, 1);
		// 	}
		// 	else {
		// 		genPropertiesModel.setValueAt(p.initialHorizontalSpeed, INITIAL_HORIZONTAL_SPEED_ROW, 1);
		// 	}
		//}
		// else {
		// 	p.chooseInitialHorizontalSpeed = false;
		// 	p.initialHorizontalSpeed = 0;
		// }
		p.initialAndTargetGiven = (p.initialMovementName.contains("RCV"));
		updateCalculateUsing();
		
		
		if (p.initialMovementName.contains("Optimal Distance")) {
			p.chooseDurationType = false;
			p.durationFrames = false;
		}
		else if (p.mode == Mode.SOLVE) {
			p.chooseDurationType = false;
			p.durationFrames = true;
		}
		else
			p.chooseDurationType = true;
		refreshPropertiesRows(getRowParams(), true);
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
		
		Properties.copyAttributes(pl, p);
			
		initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
		if (p.midairPreset.equals("Custom"))
			addPreset(p.midairs);
		else 
			addPreset(p.midairPreset, true);
		refreshPropertiesRows(getRowParams(), true);

		calculateVector.setText(p.mode.name);

		VectorDisplayWindow.frame.dispatchEvent(new WindowEvent(VectorDisplayWindow.frame, WindowEvent.WINDOW_CLOSING));
		VectorDisplayWindow.initialize();

		/* p.x0 = pl.x0;
		p.y0 = pl.y0;
		p.z0 = pl.z0;
		genPropertiesTable.setValueAt(toCoordinateString(p.x0, p.y0, p.z0), INITIAL_COORDINATES_ROW, 1);

		setAngleType(p.angleType, pl.targetCoordinates); //first make it so targetCoordinates match with pl to handle RCV case
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
				//genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
				genPropertiesTable.setValueAt(p.targetAngle, ANGLE_ROW, 1);
			}
		}
		if (p.angleType == AngleType.INITIAL) {
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_TYPE_ROW, 1);
			//genPropertiesTable.setValueAt("Initial Angle", ANGLE_ROW, 0);
			genPropertiesTable.setValueAt(p.initialAngle, ANGLE_ROW, 1);
		}
		if (p.angleType == AngleType.BOTH) {
			genPropertiesTable.setValueAt(p.initialAngle, ANGLE_2_ROW, 1);
		}
		p.initialMovementName = pl.initialMovementName;
		p.durationFrames = pl.durationFrames;
		p.initialFrames = pl.initialFrames;
		p.initialHorizontalSpeed = pl.initialHorizontalSpeed;
		updateInitialMovement(false, false);
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
			genPropertiesTable.setValueAt("Yes", HYPEROPTIMIZE_ROW, 1);
		}
		else {
			genPropertiesTable.setValueAt("No", HYPEROPTIMIZE_ROW, 1);
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
		
		VectorDisplayWindow.frame.dispatchEvent(new WindowEvent(VectorDisplayWindow.frame, WindowEvent.WINDOW_CLOSING));

		p.scriptType = pl.scriptType;
		p.scriptPath = pl.scriptPath;

		VectorDisplayWindow.initialize(); */
		
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
			 if (evt.getActionCommand().equals("add")) {
				 movementModel.addRow(movementRows);
				 //movementPropertyTables.add(new MovementProperties("Motion Cap Throw"));
				 //movementPropertiesTable.setModel(movementPropertyTables.get(0).generateTableModel());
			 }
			 else if (evt.getActionCommand().equals("remove")) {
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
			//  else if (evt.getActionCommand() == "solve") {
			//  }
			 else if (evt.getActionCommand().equals("calculate")) {
				boolean optimalDistanceMotion = p.initialMovementName.equals("Optimal Distance Motion");
				if (p.mode == Mode.SOLVE) {
					Solver solver;
					if (optimalDistanceMotion) {
						p.initialMovementName = "Triple Jump";
						p.framesJump = 10;
						initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
						Solver solverTJ = new Solver();
						solverTJ.solve(4);
						p.initialMovementName = "Motion Cap Throw RCV";
						initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
						Solver solverRCV = new Solver();
						solverRCV.solve(3);
						p.initialMovementName = "Sideflip";
						initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
						Solver solverSideflip = new Solver();
						solverSideflip.solve(4);
						// System.out.println("TJ: " + maximizerTJ.bestDisp);
						// System.out.println("RC: " + maximizerRC.bestDisp);
						// System.out.println("SF: " + maximizerSideflip.bestDisp);
						if (solverTJ.bestDisp > solverRCV.bestDisp && solverTJ.bestDisp > solverSideflip.bestDisp) {
							solver = solverTJ;
							p.initialMovementName = "Triple Jump";
						}
						else if (solverRCV.bestDisp > solverTJ.bestDisp && solverRCV.bestDisp > solverSideflip.bestDisp) {
							solver = solverRCV;
							p.initialMovementName = "Motion Cap Throw RCV";
						}
						else {
							solver = solverSideflip;
							p.initialMovementName = "Sideflip";
						}
						// p.initialFrames = solver.preset[0][1];
						// initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
						// VectorCalculator.addPreset(solver.preset);
						solver.test(solver.bestDurations,true);
					}
					else {
						solver = new Solver();
						int delta = p.initialMovementName.contains("RCV") ? 4 : 3;
						solver.solve(delta);
					}
					if (solver.bestDisp == 0) {
						errorMessage.setText("Error: Movement cannot reach target height");
						return;
					}
					VectorMaximizer maximizer = getMaximizer();
					if (maximizer != null) {
						//p.diveTurn should be set correctly by the solver
						maximizer.maximize();
						//System.out.println(maximizer.variableCapThrow1FallingFrames);
						//System.out.println(maximizer.fallingFrames);
						boolean possible = maximizer.isDiveCapBouncePossible(-1, solver.singleThrowAllowed, false, true, true, solver.ttAllowed) >= 0;
						maximizer.recalculateDisps();
						maximizer.adjustToGivenAngle();
						//maximizer.maximize();
						//possible = maximizer.isDiveCapBouncePossible(true, true, true, false);
						setPropertiesRow(Parameter.dive_angle);
						setPropertiesRow(Parameter.dive_deceleration);
						//genPropertiesTable.setValueAt(round(p.diveCapBounceAngle, 3), DIVE_CAP_BOUNCE_ANGLE_ROW, 1);
						//genPropertiesTable.setValueAt(round(p.diveFirstFrameDecel, 3), DIVE_DECEL_ROW, 1);
						System.out.println("Possible: " + possible + " " + maximizer.ctType);
						//maximizer.maximize();
						VectorDisplayWindow.generateData(maximizer, maximizer.getInitialAngle(), maximizer.getTargetAngle());
						VectorDisplayWindow.display();
						//System.out.println("Cappy position: " + );
						//System.out.println(((DiveTurn)maximizer.motions[maximizer.variableCapThrow1Index + 3]).getCapBounceFrame(((ComplexVector)maximizer.motions[maximizer.variableCapThrow1Index]).getCappyPosition(maximizer.ctType)));
					}
					if (optimalDistanceMotion) {
						setProperty(Parameter.initial_movement, "Optimal Distance Motion");
					}
					if (p.hctType != HctType.CUSTOM) { //reload preset so that hct angle gets reset to 60 degrees for next use of the calculator
						setProperty(Parameter.hct_type, p.hctType.name);
					}
				}
				else if (p.mode == Mode.CALCULATE) {
					saveMidairs();
					VectorMaximizer maximizer = null;
					if (p.targetCoordinates) {
						p.targetAngle = targetCoordinatesToTargetAngle();
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
						maximizer = getMaximizer();
						maximizer.maximize();
						maximizer.recalculateDisps();
						maximizer.adjustToGivenAngle();
					}
					if (maximizer != null) {
						VectorDisplayWindow.generateData(maximizer, maximizer.getInitialAngle(), maximizer.getTargetAngle());
						VectorDisplayWindow.display();
					}
					Debug.println();
				}
			}
		}
	}

	public static VectorMaximizer getMaximizer() {
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

	//runs the maximizer and adjusts it to the given initial or target angle
	public static VectorMaximizer calculate() {
		VectorMaximizer maximizer = getMaximizer();
		if (maximizer != null)
			maximizer.maximize();
		maximizer.adjustToGivenAngle();
		return maximizer;
	}

	public static DefaultCellEditor dropdown(String[] options) {
		return new DefaultCellEditor(new JComboBox<String>(options));
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

		all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.setOpaque(true);
		
		//GENERAL PROPERTIES TABLE
		genPropertiesModel = new DefaultTableModel(null, genPropertiesTitles); //may have to actually load something
		genPropertiesTable = new JTable(genPropertiesModel) {
			
			public TableCellEditor getCellEditor(int row, int column)
            {
                int modelColumn = convertColumnIndexToModel(column);

				if (modelColumn == 0)
					return null; //return super.getCellEditor(row, column);
				switch(rowParams.get(row)) {
					case calculate_using:
						String[] options = p.initialAndTargetGiven ? new String[]{"Target Angle", "Target Coordinates"} :
																	 new String[]{"Initial Angle", "Target Angle", "Target Coordinates"};
						return dropdown(options);
					case duration_type:
						return dropdown(new String[]{"Frames", "Vertical Displacement"});
					case vector_direction:
						return dropdown(new String[]{"Left", "Right"});
					case midairs:
						DefaultCellEditor dropdown = dropdown(midairPresetNames);
						// if (p.mode != Mode.SOLVE)
						// 	((JComboBox<String>) dropdown.getComponent()).addItem("Custom");
						return dropdown;
					case triple_throw:
						return dropdown(new String[]{"Yes", "No"});
					case gravity:
						return dropdown(new String[]{"Regular", "Moon"});
					case hyperoptimize:
						return dropdown(new String[]{"Yes", "No"});
					case camera:
						return dropdown(new String[]{"Initial Angle", "Target Angle", "Absolute", "Custom"});
					case zero_axis:
						return dropdown(new String[]{"X", "Z"});
					case initial_movement_category:
						return dropdown(initialMovementCategories);
					case initial_movement:
						return dropdown(initialMovementNames[Arrays.asList(initialMovementCategories).indexOf(p.initialMovementCategory)]);
					case hct_type:
						return dropdown(HctType.names);
					case hct_direction:
						return dropdown(new String[]{"Up", "Down", "Left", "Right"});
					case hct_neutral:
						return dropdown(new String[]{"Yes", "No"});
					case dive_turn:
						return dropdown(new String[]{"Yes", "No"});
					case ground_mode:
						return dropdown(new String[]{"None", "Uniform", "Varied"});
					case ground_type:
						return dropdown(new String[]{"Ground", "Lava/Poison"});
					case ground_type_firstGP:
					case ground_type_CB:
					case ground_type_secondGP:
						return dropdown(new String[]{"None", "Ground", "Lava/Poison"});
					case mode:
						if (p.midairPreset.equals("Custom"))
							return dropdown(new String[]{"Calculate (Solve Cap Bounce)", "Calculate"});
						else
							return dropdown(new String[]{"Solve", "Calculate (Solve Cap Bounce)", "Calculate"});
					default:
						return super.getCellEditor(row, column);
				}
            }
        
			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0)
					return false;
				Parameter param = rowParams.get(row);
				switch(param) {
					case null:
					case initial_coordinates:
					case target_coordinates:
						return false;
					case initial_speed:
						return p.chooseInitialHorizontalSpeed;
					case gravity:
						return p.mode == Mode.CALCULATE;
					default:
						return true;
				}
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
		
		genPropertiesTable.getColumnModel().getColumn(0).setMinWidth(265);
		genPropertiesTable.getColumnModel().getColumn(0).setMaxWidth(265);
		
		JScrollPane genPropertiesScrollPane = new JScrollPane(genPropertiesTable);
		genPropertiesScrollPane.setPreferredSize(new Dimension(500, genPropertiesTable.getRowHeight() * 10 + 25));
		
		ListSelectionModel genPropertiesSelectionModel = genPropertiesTable.getSelectionModel();
		
		//initial movement type selector
		genPropertiesTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = genPropertiesTable.rowAtPoint(evt.getPoint());
				int column = genPropertiesTable.columnAtPoint(evt.getPoint());
				if (row == -1 || column == -1 || column == 0)
					return;
				Parameter param = rowParams.get(row);
				switch(param) {
					// case initial_movement:
					// 	dialogWindow.display();
					// 	JButton confirm = dialogWindow.getConfirmButton();
					// 	confirm.addActionListener(new ActionListener() {
					// 		public void actionPerformed(ActionEvent e) {
					// 			//Debug.println(dialogWindow.getSelectedMovementName());
					// 			p.initialMovementName = dialogWindow.getSelectedMovementName();
					// 			updateInitialMovement(p.initialHorizontalSpeed == initialMovement.getSuggestedSpeed(), true); //update to have suggested speed if the player currently has the suggested speed for the current movement
					// 			dialogWindow.close();	
					// 		}
					// 	});
					// 	break;
					case null:
						break;
					case initial_coordinates:
						initial_CoordinateWindow.display(p.x0, p.y0, p.z0);
						if (add_ic_listener) {
							(initial_CoordinateWindow.getConfirmButton()).addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									setProperty(Parameter.initial_coordinates, initial_CoordinateWindow.getCoordinates());
									initial_CoordinateWindow.close();
								}
							});
						}
						add_ic_listener = false;
						break;
					case target_coordinates:
						target_CoordinateWindow.display(p.x1, p.y1, p.z1);
						if (add_tc_listener) {
							(target_CoordinateWindow.getConfirmButton()).addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									setProperty(Parameter.target_coordinates, target_CoordinateWindow.getCoordinates());
									target_CoordinateWindow.close();
								}
							});
						}
						add_tc_listener = false;
						break;
					default:
						break;
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

				if (!settingPropertyRow) {
					setProperty(row);
					refreshPropertiesRows(getRowParams(), false);

					initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);

					if (initialized && saved && Properties.isUnsaved()) {
						System.out.println("Unsaved");
						saved = false;
						f.setTitle("*" + projectName);
					}
				}

				/* if (!forceEdit) { //forceEdit allows a part of the program to ignore these rules
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
						if (p.lockDurationType == LOCK_NONE) {
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
						if (p.chooseJumpFrames) {
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
						if (p.chooseInitialHorizontalSpeed) {
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
					}
					else if (row == HYPEROPTIMIZE_ROW) {
						p.hyperoptimize = genPropertiesTable.getValueAt(row, 1).equals("Yes");
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
				} */
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
				if (column == 0 && !p.midairPreset.equals("Custom"))
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
				refreshPropertiesRows(getRowParams(), false);
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
		//solveVector = new JButton("Solve");
		calculateVector = new JButton("Solve");
		add.setActionCommand("add");
		remove.setActionCommand("remove");
		calculateVector.setActionCommand("calculate");
		//solveVector.setActionCommand("solve");
		
		movementEdit.add(add);
		movementEdit.add(remove);
		//calculateVectorPanel.add(solveVector);
		calculateVectorPanel.add(calculateVector);
		
		ButtonListener buttonListen = new ButtonListener();
		add.addActionListener(buttonListen);
		remove.addActionListener(buttonListen);
		//solveVector.addActionListener(buttonListen);
		calculateVector.addActionListener(buttonListen);
		
		//addPreset(7);

		//System.out.println("Script Type: " + p.scriptType);

		//CREATING THE WINDOW
		
		JPanel nonResize = new JPanel(new BorderLayout());
		//nonResize.add(genPropertiesScrollPane, BorderLayout.NORTH);
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

		JPanel tabPanel = new JPanel();
		tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            	p.currentTab = tabbedPane.getSelectedIndex();
				refreshPropertiesRows(getRowParams(), false);
            }
        });
        tabbedPane.addTab("General Properties",genPropertiesScrollPane);
        tabbedPane.addTab("Midair Properties", null);
        tabbedPane.setPreferredSize(new Dimension(600, 400));
		tabPanel.add(tabbedPane);

		//genPropertiesTable.setTableHeader(null);

		MainJMenuBar menuBar = new MainJMenuBar();
		f.setJMenuBar(menuBar);
		f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					menuBar.promptSaveAndClose();
				}
			});

		f.add(tabPanel, BorderLayout.NORTH);
		f.add(nonResize, BorderLayout.CENTER);

		//load the user default properties
		loadProperties(userDefaults, true);
		p.file = null; //so we don't save to it
		initialized = true;
		
		//f.add(resize, BorderLayout.CENTER);
		f.setSize(600, 600);
		//f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

}

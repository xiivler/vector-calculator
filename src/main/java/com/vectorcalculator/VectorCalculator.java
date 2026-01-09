package com.vectorcalculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import com.vectorcalculator.Properties.CameraType;
import com.vectorcalculator.Properties.GroundMode;
import com.vectorcalculator.Properties.GroundType;
import com.vectorcalculator.Properties.HctType;
import com.vectorcalculator.Properties.Mode;
import com.vectorcalculator.Properties.TripleThrow;
import com.vectorcalculator.Properties.TurnDuringDive;
import com.vectorcalculator.Properties.CalculateUsing;

public class VectorCalculator extends JPanel {
	
	public static final int WINDOW_WIDTH = 550;
	public static final int PROPERTIES_TABLE_HEIGHT = 435;
	public static final int MIDAIR_PANEL_HEIGHT = 275;

	static Properties p = Properties.getInstance();
	static boolean stop = false;
	static boolean saved = true;
	static boolean initialized = false;
	//static boolean editedSinceCalculate = true;
	static boolean loading = false;
	static boolean cellsEditable = true;
	static boolean addingPreset = false;

	static File file = null;

	static String projectName = "Untitled Project";

	static Font tableFont = new Font("Verdana", Font.PLAIN, 14);

	static String jarParentFolder;
	static File userDefaults;

	static final int GENERAL_TAB = 0, MIDAIR_TAB = 1;
	
	static enum Parameter {
		mode("Calculator Mode"), initial_coordinates("Initial Coordinates"), calculate_using("Calculate Using"),
		solve_for_initial_angle("Solve For Initial Angle"), initial_angle("Initial Angle"), target_angle("Target Angle"), target_coordinates("Target Coordinates"),
		target_y_position("Target Y Position"),
		midairs("Midairs"), triple_throw("Triple Throw"), gravity("Gravity"), turnarounds("Enable Turnarounds"), zero_axis("0 Degree Axis"), camera("Camera Angle"),
		custom_camera_angle("Custom Camera Angle"), initial_movement_category("Initial Movement"), initial_movement("Initial Movement Type"),
		duration_type("Duration Type"), initial_frames("Frames"), initial_displacement("Vertical Displacement"),
		vault_cap_return_frame("Vault Cap Return Frame"), duration_search_range("Duration Search Range"),
		jump_button_frames("Frames of Holding A/B"), moonwalk_frames("Moonwalk Frames"), initial_speed("Initial Horizontal Speed"),
		vector_direction("Vector Direction"), dive_angle("Edge Cap Bounce Angle"),
		dive_angle_tolerance("Edge Cap Bounce Angle Tolerance"), dive_deceleration("First Dive Deceleration"),
		dive_turn("Turn During First Dive"), cb_cap_return_frame("CB Cap Return Frame"), hct_type("Homing Throw Type"), hct_angle("Homing Throw Angle"),
		hct_neutral("Neutral Joystick During Homing"), hct_direction("Homing Direction"),
		hct_homing_frame("Frames Before Home"), hct_cap_return_frame("HCT Cap Return Frame"),
		ground_mode("Ground/Liquid Under Midairs"), ground_type("Type"), ground_height("Height"),
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
			// Show solve_for_initial_angle only if mode is Solve and initialAndTargetGiven is true
			if (p.mode == Mode.SOLVE && p.initialAndTargetGiven) {
				params.add(Parameter.solve_for_initial_angle);
				if (!p.solveForInitialAngle)
					params.add(Parameter.initial_angle);
			}
			else if (p.initialAngleGiven)
				params.add(Parameter.initial_angle);

			if (p.targetAngleGiven)
				params.add(Parameter.target_angle);
			if (p.targetCoordinatesGiven)
				params.add(Parameter.target_coordinates);
			else
				params.add(Parameter.target_y_position);
			params.add(null);

			params.add(Parameter.initial_movement_category);
			if (!p.initialMovementCategory.equals("None")) {
				if (!p.initialMovementCategory.equals("Optimal Distance Motion"))
					params.add(Parameter.initial_movement);
				if (p.chooseDurationType)
					params.add(Parameter.duration_type);
				if (p.durationFrames)
					params.add(Parameter.initial_frames);
				else if (p.mode != Mode.SOLVE)
					params.add(Parameter.initial_displacement);
				if (p.initialMovementName.equals("Vault"))
					params.add(Parameter.vault_cap_return_frame);
				if (p.chooseJumpFrames)
					params.add(Parameter.jump_button_frames);
				if (p.canMoonwalk)
					params.add(Parameter.moonwalk_frames);
			}
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
			params.add(null);

			params.add(Parameter.duration_search_range);
			params.add(null);

			params.add(Parameter.turnarounds);

			if (p.diveCapBounce) {
				params.add(null);
				params.add(Parameter.dive_angle);
				params.add(Parameter.dive_angle_tolerance);
				params.add(Parameter.dive_deceleration);
				params.add(Parameter.dive_turn);
				params.add(Parameter.cb_cap_return_frame);
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
			//Debug.println("No need to refresh");
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
		Parameter param = rowParams.get(row);
		genPropertiesTable.setValueAt(param.name, row, 0);
		genPropertiesTable.setValueAt(PropertyToDisplayValue(param), row, 1);
		settingPropertyRow = false;
	}
	
	static String PropertyToDisplayValue(Parameter param) {
		Object value;
		switch(param) {
		case duration_search_range:
			value = p.durationSearchRange;
			break;
		case solve_for_initial_angle:
			value = p.solveForInitialAngle ? "Yes" : "No";
			break;
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
		case target_y_position:
			value = p.y1;
			break;
		case midairs:
			value = p.midairPreset;
			break;
		case triple_throw:
			value = p.tripleThrow.displayName;
			break;
		case upwarp:
			value = p.upwarp;
			break;
		case gravity:
			value = p.onMoon ? "Moon" : "Regular";
			break;
		case turnarounds:
			value = p.turnarounds ? "Yes" : "No";
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
			value = p.diveTurn.displayName;
			break;
		case cb_cap_return_frame:
			value = p.cbCapReturnFrame;
			break;
		case vault_cap_return_frame:
			value = p.vaultCapReturnFrame;
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
		if (param == null)
			return;
		switch(param) {
		case mode:
			Mode oldMode = p.mode;
			p.mode = Properties.Mode.fromName(value.toString());
			if (p.mode == Mode.SOLVE) {
				setProperty(Parameter.gravity, "Regular");
				if (p.midairPreset.equals("Custom"))
					setProperty(Parameter.midairs, "Spinless");
				if (p.initialAndTargetGiven && oldMode != Mode.SOLVE)
					setProperty(Parameter.solve_for_initial_angle, "Yes");
			}
			else if (p.mode == Mode.SOLVE_DIVES) {
				setProperty(Parameter.gravity, "Regular");
				p.solveForInitialAngle = false;
			}
			else {
				if (p.diveTurn == TurnDuringDive.TEST)
					setProperty(Parameter.dive_turn, "Yes");
				p.solveForInitialAngle = false;
			}
			calculateVector.setText(p.mode.name);
			updateDurationType();
			MainJMenuBar.updateCalculatorMenuItems();
			p.canTestTripleThrow = p.mode != Mode.CALCULATE && (p.midairPreset.equals("Spinless") || p.midairPreset.equals("Simple Tech"));
			if (!p.canTestTripleThrow && p.tripleThrow == TripleThrow.TEST)
				setProperty(Parameter.triple_throw, "No");
			break;
		case initial_coordinates:
			double[] coords = new double[3];
			parseCoordinates(value.toString(), coords);
			p.x0 = coords[0];
			p.y0 = coords[1];
			p.z0 = coords[2];
			if (p.targetCoordinatesGiven)
				setProperty(Parameter.target_angle, targetCoordinatesToTargetAngle());
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
		case target_y_position:
			p.y1 = parseDoubleWithDefault(value, 0);
			break;
		case solve_for_initial_angle:
			p.solveForInitialAngle = value.toString().equals("Yes");
		case initial_angle:
			p.initialAngle = parseDoubleWithDefault(value, 0);
			break;
		case target_angle:
			p.targetAngle = parseDoubleWithDefault(value, 0);
			break;
		case initial_movement_category:
			String oldInitialMovementCategory = p.initialMovementCategory;
			p.initialMovementCategory = value.toString();
			if (p.initialMovementCategory.equals("None")) {
				p.initialMovementName = "None";
				p.initialFrames = 0;
				p.initialDispY = 0;
				p.initialHorizontalSpeed = 0;
				updateInitialMovement();
			}
			else if (!oldInitialMovementCategory.equals(p.initialMovementCategory)) {
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
			p.initialDispY = initialMotion.calcDispY(p.initialFrames) + getMoonwalkDisp();
			break;
		case initial_displacement:
			p.initialDispY = parseDoubleWithDefault(value, 0);
			p.initialFrames = initialMotion.calcFrames(p.initialDispY - getMoonwalkDisp());
			break;
		case duration_search_range:
			p.durationSearchRange = clampInt(parseIntWithDefault(value, 4), 2, 5);
			break;
		case vault_cap_return_frame:
			p.vaultCapReturnFrame = clampInt(parseIntWithDefault(value, 28), 0, Integer.MAX_VALUE);
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
			p.canTripleThrow = !(name.equals("Simple Tech Rainbow Spin First") || name.equals("Custom") || name.equals("None"));
			p.canTestTripleThrow = p.mode != Mode.CALCULATE && (name.equals("Spinless") || name.equals("Simple Tech"));
			if (!p.canTripleThrow || (!oldCanTripleThrow && p.canTripleThrow))
				setProperty(Parameter.triple_throw, "No");
			if (!p.canTestTripleThrow && p.tripleThrow == TripleThrow.TEST)
				setProperty(Parameter.triple_throw, "No");
			if (!name.equals(p.midairPreset))
				addPreset(name, false);
			if ((name.equals("Custom") || name.equals("None")) && p.mode == Mode.SOLVE) {
				Debug.println("Should switch");
				setProperty(Parameter.mode, Mode.SOLVE_DIVES.name);
				
			}
			break;
		case triple_throw:
			p.tripleThrow = TripleThrow.fromDisplayName(value.toString());
			int[][] oldMidairs = p.midairs;
			addPreset(p.midairPreset, false);
			for (int i = 0; i < p.midairs.length; i++) { //copy all the durations the user has customized
				p.midairs[i][1] = oldMidairs[i][1];
			}
			if (p.tripleThrow != TripleThrow.NO && p.midairPreset.equals("CBV First")) { //conform cb duration
				p.midairs[2][1] = Math.min(p.midairs[2][1], 36);
			}
			addPreset(p.midairs);
			break;
		case gravity:
			p.onMoon = value.toString().equals("Moon");
			break;
		case upwarp:
			p.upwarp = clampDouble(parseDoubleWithDefault(value, 40), 0, 40);
			break;
		case turnarounds:
			p.turnarounds = value.toString().equals("Yes");
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
			p.diveTurn = Properties.TurnDuringDive.fromDisplayName(value.toString());
			break;
		case cb_cap_return_frame:
			p.cbCapReturnFrame = clampInt(parseIntWithDefault(value, 25), 0, Integer.MAX_VALUE);
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
		if (p.initialAndTargetGiven) {
			p.initialAngleGiven = true;
			p.targetAngleGiven = p.calculateUsing != CalculateUsing.TARGET_COORDINATES;
			p.targetCoordinatesGiven = p.calculateUsing != CalculateUsing.TARGET_ANGLE;
		}
		else {
			p.initialAngleGiven = p.calculateUsing == CalculateUsing.INITIAL_ANGLE;
			p.targetAngleGiven = p.calculateUsing == CalculateUsing.TARGET_ANGLE;
			p.targetCoordinatesGiven = p.calculateUsing == CalculateUsing.TARGET_COORDINATES;
		}
		//these cases handle if we don't know what the value should be
		if (p.initialAngleGiven && !oldInitialAngleGiven)
			setProperty(Parameter.initial_angle, p.targetAngle);
		if (p.targetAngleGiven && !(oldTargetAngleGiven || oldTargetCoordinatesGiven))
			setProperty(Parameter.target_angle, p.initialAngle);
		if (p.targetCoordinatesGiven && !oldTargetCoordinatesGiven)
			setProperty(Parameter.target_coordinates, toCoordinateString(p.x0, p.y1, p.z0));
		if (p.targetAngleGiven && oldTargetCoordinatesGiven)
			setProperty(Parameter.target_angle, targetCoordinatesToTargetAngle());
	}

	static void updateDurationType() {
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
	}

	static String[] initialMovementCategories = {"Jump", "RCV", "Roll", "Fork Flick", "Bounce", "Misc", "Optimal Distance Motion", "None"};
	static String[][] initialMovementNames =
		{{"Single Jump", "Double Jump", "Triple Jump", "Vault", "Cap Return Jump", "Long Jump", "Ground Pound Jump", "Backflip", "Sideflip", "Spin Jump"},
		{"Motion Cap Throw RCV", "Single Throw RCV", "Upthrow RCV", "Downthrow RCV", "Double Throw RCV", "Spinthrow RCV", "Triple Throw RCV", "Fakethrow RCV", "Optimal Distance RCV"},
		{"Ground Pound Roll", "Crouch Roll", "Crouch Roll (No Vector)", "Roll Boost", "Roll Boost (No Vector)"},
		{"Horizontal Pole/Fork Flick", "Motion Horizontal Pole/Fork Flick", "Motion Vertical Pole/Fork Flick"},
		{"Small NPC Bounce", "Large NPC Bounce", "Ground Pound Object/Enemy Bounce", "Bouncy Object Bounce", "Flower Bounce"},
		{"Uncapture", "Flip Forward", "Swinging Jump"},
		{"Optimal Distance Motion", "Optimal Distance RCV"},
		{"None"}};
	static String[] initialMovementDefaults = {"Triple Jump", "Motion Cap Throw RCV", "Ground Pound Roll", "Motion Horizontal Pole/Fork Flick", "Large NPC Bounce", "Uncapture", "Optimal Distance Motion", "None"};
	
	static String[] midairPresetNames = {"Spinless", "Simple Tech", "Simple Tech Rainbow Spin First", "MCCT First", "CBV First", "None", "Custom"};
	
	static String[] midairMovementNames = {"Motion Cap Throw", "Single Throw", "Triple Throw", "Homing Motion Cap Throw", "Homing Triple Throw", "Rainbow Spin", "Dive", "Cap Bounce", "2P Midair Vault"};

	static final int MCCT = 0, CT = 1, TT = 2, HMCCT = 3, HTT = 4, RS = 5, DIVE = 6, CB = 7, P2CB = 8;

	static void saveMidairs() {
		if (p.midairPreset.equals("Custom"))
			p.tripleThrow = TripleThrow.NO;
		p.midairs = new int[movementModel.getRowCount()][2];
		p.hct = false;
		p.diveCapBounce = false;
		p.firstCTIndex = -1;
		List<String> types = Arrays.asList(midairMovementNames);
		for (int i = 0; i < movementModel.getRowCount(); i++) {
			p.midairs[i][0] = types.indexOf(movementModel.getValueAt(i, 0).toString());
			p.midairs[i][1] = Integer.parseInt(movementModel.getValueAt(i, 1).toString());
			if (i > 0 && p.midairs[i][0] == CB && p.midairs[i - 1][0] == DIVE) {
				p.diveCapBounce = true;
				if (i > 1 && p.midairs[i - 2][0] == MCCT || p.midairs[i - 2][0] == CT || p.midairs[i - 2][0] == TT) {
					p.firstCTIndex = i - 2;
				}
			}
			else if (p.midairs[i][0] == HMCCT)
				p.hct = true;
			else if (p.midairPreset.equals("Custom") && p.midairs[i][0] == TT) {
				p.tripleThrow = TripleThrow.YES;
			}
		}
		/* for (int i = 0; i < p.midairs.length; i++)
			Debug.println(p.midairs[i][0] + ", " + p.midairs[i][1]); */
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

	static int lastInitialMovementFrame;
	
	static Movement initialMovement = new Movement(p.initialMovementName);
	static SimpleMotion initialMotion = new SimpleMotion(initialMovement, p.initialFrames);
	
	static boolean settingPropertyRow = false;
	static boolean add_ic_listener = true;
	static boolean add_tc_listener = true;

	static JLabel errorMessage;
	static JLabel progressMessage;
	
	static String[] attributeTitles = {"Parameter", "Value"};
	static String[] movementTitles = {"Midair Movement Type", "Number of Frames"};
	static String[] movementRows = {"Motion Cap Throw", "8"};
	
	static JTable genPropertiesTable;
	static DefaultTableModel genPropertiesModel;
	static JumpDialogWindow dialogWindow = new JumpDialogWindow("Choose Initial Movement", initialMovementCategories, initialMovementNames);
	static CoordinateWindow initial_CoordinateWindow = new CoordinateWindow("Initial Coordinates");
	static CoordinateWindow target_CoordinateWindow = new CoordinateWindow("Target Coordinates");
	static DefaultTableModel movementModel = new DefaultTableModel(0, 2);
	static JTable movementTable;

	static JButton add;
	static JButton remove;
	static JButton calculateVector;

	static String[] genPropertiesTitles = {"Property", "Value"};
	
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
		if (p.tripleThrow == TripleThrow.NO) {
			switch(name) {
				case "Spinless":
					return new int[][]{{MCCT, 28}, {DIVE, 25}, {CB, 44}, {MCCT, 31}, {DIVE, 25}};
				case "Simple Tech":
					return new int[][]{{MCCT, 28}, {DIVE, 25}, {CB, 36}, {RS, 32}, {MCCT, 30}, {DIVE, 25}};
				case "Simple Tech Rainbow Spin First":
					return new int[][]{{RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 43}, {MCCT, 30}, {DIVE, 25}};
				case "MCCT First":
					return new int[][]{{HMCCT, 36}, {RS, 32}, {MCCT, 28}, {DIVE, 25}, {CB, 42}, {MCCT, 31}, {DIVE, 25}};
				case "CBV First":
					return new int[][]{{MCCT, 28}, {DIVE, 25}, {CB, 42}, {HMCCT, 36}, {RS, 32}, {MCCT, 31}, {DIVE, 25}};
				case "None":
				default:
					return new int[0][0];
			}
		}
		else {
			switch(name) {
				case "Spinless":
					return new int[][]{{TT, 28}, {DIVE, 25}, {CB, 44}, {MCCT, 31}, {DIVE, 25}};
				case "Simple Tech":
					return new int[][]{{TT, 28}, {DIVE, 25}, {CB, 36}, {RS, 32}, {MCCT, 30}, {DIVE, 25}};
				case "MCCT First":
					return new int[][]{{HTT, 30}, {RS, 32}, {MCCT, 28}, {DIVE, 26}, {CB, 42}, {MCCT, 31}, {DIVE, 25}};
				case "CBV First":
					return new int[][]{{MCCT, 28}, {DIVE, 26}, {CB, 36}, {HTT, 30}, {RS, 32}, {MCCT, 30}, {DIVE, 24}};
				case "None":
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
			if (load && p.midairs != null)
				addPreset(p.midairs);
		}
		else {
			add.setEnabled(false);
			remove.setEnabled(false);
			if (load && p.midairs != null)
				addPreset(p.midairs);
			else
				addPreset(getPreset(name));
		}
		p.midairPreset = name;
		MainJMenuBar.updateCalculatorMenuItems();
	}

	public static void addPreset(int[][] preset) {
		addingPreset = true;
		movementModel.setRowCount(0);
		for (int[] row : preset) {
			movementModel.addRow(new Object[]{midairMovementNames[row[0]], row[1]});
		}
		addingPreset = false;
		saveMidairs();
	}

	public static void updateInitialMovement() {
		boolean suggestSpeed = p.initialHorizontalSpeed == initialMovement.getSuggestedSpeed();
		initialMovement = new Movement(p.initialMovementName);
		if (initialMovement.variableJumpFrames()) {
			if (!p.chooseJumpFrames) {
				p.chooseJumpFrames = true;
				p.framesJump = 10;
				p.initialFrames = Math.max(p.initialFrames, 10);
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
		p.initialAndTargetGiven = (p.initialMovementName.contains("RCV"));
		if (p.mode == Mode.SOLVE && p.initialAndTargetGiven)
			setProperty(Parameter.solve_for_initial_angle, "Yes");
		p.initialFrames = Math.max(p.initialFrames, initialMovement.minFrames);
		if (p.initialAndTargetGiven && p.calculateUsing == CalculateUsing.INITIAL_ANGLE) {
			setProperty(Parameter.calculate_using, "Target Angle");
		}
		else {
			updateCalculateUsing();
		}
		
		updateDurationType();
		refreshPropertiesRows(getRowParams(), true);
	}

	public static void saveProperties(File file, boolean updateCurrentFile, boolean defaults) {
		Properties.p_toSave = new Properties();
		Properties.copyAttributes(p, Properties.p_toSave);
		if (Properties.p_calculated == null || !Properties.p_calculated.equals(p) || defaults) {
			Properties.p_toSave.savedDataTableRows = null;
			Properties.p_toSave.savedInfoTableRows = null;
		}
		
		boolean saveSuccess = Properties.save(file, defaults);
		if (!saveSuccess) {
			if (defaults)
				JOptionPane.showMessageDialog(f, "Failed to save user defaults.", "Save Error", JOptionPane.ERROR_MESSAGE);
			else
				JOptionPane.showMessageDialog(f, "Failed to save the file.", "Save Error", JOptionPane.ERROR_MESSAGE);
			setErrorText("Error: Save failed");
		}
		if (updateCurrentFile) {
			saved = saveSuccess;
			if (saved) {
				projectName = file.getName();
				f.setTitle(projectName);
				VectorDisplayWindow.frame.setTitle("Calculations: " + VectorCalculator.projectName);
			}
		}
	}

	//if dispose is true, editedSinceCalculate is set to true and the snapshot of the display window tables is trashed
	public static void checkIfSaved(boolean dispose) {
		if (Properties.isSaved()) {
			saved = true;
			f.setTitle(projectName);
		}
		else if (initialized) {
			saved = false;
			f.setTitle("*" + projectName);
		}
	}

	public static void loadProperties(Properties pl, boolean changeTab) {
		loading = true;

		VectorCalculator.clearMessage();

		int currentTab = p.currentTab;
		Properties.copyAttributes(pl, p);
		if (!changeTab)
			p.currentTab = currentTab;
			
		initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
		addPreset(p.midairPreset, true);
		refreshPropertiesRows(getRowParams(), true);

		calculateVector.setText(p.mode.name);
		MainJMenuBar.updateCalculatorMenuItems();

		selectParamRow(p.selectedParam);
		if (p.movementSelectedRow >= 0 && p.movementSelectedCol >= 0 && p.movementSelectedRow < movementTable.getRowCount() && p.movementSelectedCol < movementTable.getColumnCount()) {
			movementTable.changeSelection(p.movementSelectedRow, p.movementSelectedCol, false, false);
		}

		loading = false;
	}

	public static void loadUserDefaults() {
		try {
			loadProperties(userDefaults, true);
		}
		catch (Exception ex) {
			loadProperties(new Properties(), false);
		}
	}

	public static void loadProperties(File file, boolean defaults) {
		loading = true;

		Properties pl = Properties.load(file, defaults);
		if (pl == null && !defaults) {
			setErrorText("Error: File could not be loaded");
		}
		
		loadProperties(pl, false);

		loading = true; //the other loadProperties sets it to false

		VectorDisplayWindow.frame.dispatchEvent(new WindowEvent(VectorDisplayWindow.frame, WindowEvent.WINDOW_CLOSING));
		VectorDisplayWindow.initialize();

		if (p.savedInfoTableRows != null) {
			Properties.p_calculated = new Properties();
			Properties.copyAttributes(p, Properties.p_calculated);
			VectorDisplayWindow.display();
		}
		
		if (initialized && defaults && !Properties.isSaved()) {
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
		if (defaults)
			UndoManager.recordState(true);
		else
			UndoManager.clear();

		loading = false;
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
			 else if (evt.getActionCommand().equals("calculate")) {
				clearMessage();
				if (p.targetCoordinatesGiven) {
					p.targetAngle = targetCoordinatesToTargetAngle();
				}
				boolean optimalDistanceMotion = p.initialMovementName.equals("Optimal Distance Motion");
				if (p.mode == Mode.SOLVE || p.mode == Mode.SOLVE_DIVES) {
					if (p.initialMovementName.equals("Optimal Distance RCV")) {
						setErrorText("Error: Optimal Distance RCV not supported in this mode");
						return;
					}

					boolean diveSolver = p.mode == Mode.SOLVE_DIVES;
					TurnDuringDive oldTurnDuringDive = p.diveTurn;
					SolverInterface solver;
					if (optimalDistanceMotion) {
						p.initialMovementName = "Triple Jump";
						p.framesJump = 10;
						initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
						SolverInterface solverTJ = runSolver(diveSolver, false);
						int[][] tjMidairs = p.midairs;
						//solverTJ.solve(p.durationSearchRange);
						p.initialMovementName = "Motion Cap Throw RCV";
						initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
						SolverInterface solverRCV = runSolver(diveSolver, false);
						int[][] rcvMidairs = p.midairs;
						//solverRCV.solve(Math.min(p.durationSearchRange, 3));
						p.initialMovementName = "Sideflip";
						initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
						SolverInterface solverSideflip = runSolver(diveSolver, false);
						int[][] sideflipMidairs = p.midairs;
						//solverSideflip.solve(p.durationSearchRange);
						// Debug.println("TJ: " + maximizerTJ.bestDisp);
						// Debug.println("RC: " + maximizerRC.bestDisp);
						// Debug.println("SF: " + maximizerSideflip.bestDisp);
						if (solverTJ.getBestDisp() > solverRCV.getBestDisp() && solverTJ.getBestDisp() > solverSideflip.getBestDisp()) {
							solver = solverTJ;
							p.initialMovementName = "Triple Jump";
							addPreset(tjMidairs);
						}
						else if (solverRCV.getBestDisp() > solverTJ.getBestDisp() && solverRCV.getBestDisp() > solverSideflip.getBestDisp()) {
							solver = solverRCV;
							p.initialMovementName = "Motion Cap Throw RCV";
							addPreset(rcvMidairs);
						}
						else {
							solver = solverSideflip;
							p.initialMovementName = "Sideflip";
							addPreset(sideflipMidairs);
						}
						retest(solver, diveSolver);
					}
					else if (p.initialMovementName.contains("Crouch Roll")) {
						p.initialMovementName = "Crouch Roll";
						initialMovement = new Movement(p.initialMovementName);
						SolverInterface solverCR = runSolver(diveSolver, false);
						p.initialMovementName = "Crouch Roll (No Vector)";
						initialMovement = new Movement(p.initialMovementName);
						SolverInterface solverCRNV = runSolver(diveSolver, false);
						if (solverCR.getBestDisp() > solverCRNV.getBestDisp()) {
							solver = solverCR;
							p.initialMovementName = "Crouch Roll";
						} else {
							solver = solverCRNV;
							p.initialMovementName = "Crouch Roll (No Vector)";
						}
						setPropertiesRow(Parameter.initial_movement);
						initialMovement = new Movement(p.initialMovementName);
						retest(solver, diveSolver);
					}
					else if (p.initialMovementName.contains("Roll Boost")) {
						p.initialMovementName = "Roll Boost";
						initialMovement = new Movement(p.initialMovementName);
						SolverInterface solverRB = runSolver(diveSolver, false);
						p.initialMovementName = "Roll Boost (No Vector)";
						initialMovement = new Movement(p.initialMovementName);
						SolverInterface solverRBNV = runSolver(diveSolver, false);
						if (solverRB.getBestDisp() > solverRBNV.getBestDisp()) {
							solver = solverRB;
							p.initialMovementName = "Roll Boost";
						} else {
							solver = solverRBNV;
							p.initialMovementName = "Roll Boost (No Vector)";
						}
						setPropertiesRow(Parameter.initial_movement);
						initialMovement = new Movement(p.initialMovementName);
						retest(solver, diveSolver);
					}
					else {
						solver = runSolver(diveSolver, true);
					}
					saveMidairs();
					VectorMaximizer maximizer = solver.getMaximizer();
					if (solver.solveSuccess() && maximizer != null) {
						if (p.firstCTIndex >= 0) {
							if (maximizer.ctType == Movement.TT || maximizer.ctType == Movement.TTU || maximizer.ctType == Movement.TTD || maximizer.ctType == Movement.TTL || maximizer.ctType == Movement.TTR)
								p.midairs[p.firstCTIndex][0] = TT;
							else if (maximizer.ctType == Movement.CT)
								p.midairs[p.firstCTIndex][0] = CT;
							else
								p.midairs[p.firstCTIndex][0] = MCCT;
						}
						addPreset(p.midairs);
						setPropertiesRow(Parameter.dive_angle);
						setPropertiesRow(Parameter.dive_deceleration);
						//System.out.println("Possible: " + possible + " " + maximizer.ctType);
						VectorDisplayWindow.generateData(maximizer);
						VectorDisplayWindow.display();
					}
					if (optimalDistanceMotion) {
						setProperty(Parameter.initial_movement, "Optimal Distance Motion");
						p.durationFrames = false;
					}
					if (p.hctType != HctType.CUSTOM) { //reload preset so that hct angle gets reset to 60 degrees for next use of the calculator
						setProperty(Parameter.hct_type, p.hctType.name);
					}
					setProperty(Parameter.dive_turn, oldTurnDuringDive.displayName); //set back to "Test Both" if that is the setting
				}
				else if (p.mode == Mode.CALCULATE) {
					saveMidairs();
					VectorMaximizer maximizer = null;
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
						Debug.println("TJ: " + maximizerTJ.bestDisp);
						Debug.println("RC: " + maximizerRC.bestDisp);
						Debug.println("SF: " + maximizerSideflip.bestDisp);
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
						if (maximizer != null) {
							maximizer.maximize();
							if (maximizer.hasError()) {
								setErrorText(maximizer.error);
								return;
							}
							maximizer.recalculateDisps();
							maximizer.adjustToGivenAngle();
						}
					}
					if (maximizer != null) {
						VectorDisplayWindow.generateData(maximizer);
						VectorDisplayWindow.display();
					}
					Debug.println();
					
				}
				//for all calculate modes
				Properties.p_calculated = new Properties();
                Properties.copyAttributes(p, Properties.p_calculated);
				UndoManager.recordState(false);
			}
		}
	}

	public static VectorMaximizer getMaximizer() {
		MovementNameListPreparer movementPreparer = new MovementNameListPreparer();
		String errorText = movementPreparer.prepareList();
		lastInitialMovementFrame = movementPreparer.lastInitialMovementFrame;
		movementPreparer.print();

		if (errorText.equals("")) {
			clearMessage();
			VectorMaximizer maximizer = new VectorMaximizer(movementPreparer);
			return maximizer;
		}
		else {
			setErrorText("Error: " + errorText);
			return null;
		}
	}

	public static SolverInterface runSolver(boolean diveSolver, boolean displayError) {
		SolverInterface solver;
		if (diveSolver) {
			solver = new DiveSolver();
			solver.solve(40);
		}
		else {
			solver = new Solver();
			int delta = p.initialMovementName.contains("RCV") ? Math.min(p.durationSearchRange, 3) : p.durationSearchRange;
			solver.solve(delta);
		}
		if (!solver.solveSuccess() && displayError) {
			setErrorText(solver.getError());
		}
		return solver;
	}

	public static void retest(SolverInterface solverInterface, boolean diveSolver) {
		if (!diveSolver) {
			Solver solver = (Solver) solverInterface;
			solver.test(solver.bestDurations, true, solver.hasRCV);
		}
		else
			solverInterface.solve(40);
		if (!solverInterface.solveSuccess()) {
			setErrorText(solverInterface.getError());
		}
	}

	//runs the maximizer and adjusts it to the given initial or target angle
	public static VectorMaximizer calculate() {
		VectorMaximizer maximizer = getMaximizer();
		if (maximizer != null)
			maximizer.maximize();
		maximizer.recalculateDisps();
		maximizer.adjustToGivenAngle();
		if (maximizer.hasError())
			maximizer.bestDisp = 0;
		return maximizer;
	}

	public static DefaultCellEditor dropdown(String[] options) {
		return new DefaultCellEditor(new JComboBox<String>(options));
	}

	// Check if a keystroke matches a known menu accelerator
	public static boolean isMenuAccelerator(KeyStroke ks) {
		if (ks == null) return false;
		int keyCode = ks.getKeyCode();
		int modifiers = ks.getModifiers();
		int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		// Check common menu shortcuts
		// File menu: N, O, S, R (Shift), A (Alt)
		if ((modifiers & shortcut) != 0) {
			if (keyCode == KeyEvent.VK_N || keyCode == KeyEvent.VK_O || keyCode == KeyEvent.VK_S) {
				return true;
			}
		}
		// Redo: Ctrl+Y
		if ((modifiers & shortcut) != 0 && keyCode == KeyEvent.VK_Y) {
			return true;
		}
		// Undo: Ctrl+Z
		if ((modifiers & shortcut) != 0 && keyCode == KeyEvent.VK_Z) {
			return true;
		}
		// Calculator menu (Ctrl+R, Ctrl+=, Ctrl+Shift+=, Ctrl+-, Ctrl+0)
		if ((modifiers & shortcut) != 0) {
			if (keyCode == KeyEvent.VK_R) {
				return true;
			}
			if (keyCode == KeyEvent.VK_EQUALS) {
				return true;
			}
			if (keyCode == KeyEvent.VK_MINUS) {
				return true;
			}
			if (keyCode == KeyEvent.VK_0) {
				return true;
			}
		}
		// View menu: Ctrl+1, Ctrl+2
		if ((modifiers & shortcut) != 0) {
			if (keyCode == KeyEvent.VK_1 || keyCode == KeyEvent.VK_2) {
				return true;
			}
		}
		return false;
	}

	public static void selectParamRow(Parameter param) {
		Debug.println(param);
		if (param == null) {
			genPropertiesTable.clearSelection();
			return;
		}
		int index = rowParams.indexOf(param);
		if (index >= 0)
			SwingUtilities.invokeLater(() -> {
				genPropertiesTable.changeSelection(index, 1, false, false);});
	}

	public static void setProgressText(String progress) {
		progressMessage.setText(progress);
		if (progress != "")
			System.out.println(progress);
	}

	public static void setErrorText(String error) {
		errorMessage.setText(error);
		if (error != "")
			System.out.println(error);
	}

	public static void clearMessage() { //clears progress and error text
		progressMessage.setText("");
		errorMessage.setText("");
	}

	public static void main(String[] args) {
		File program;
		try {
			program = new File(VectorCalculator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (program.isDirectory()) //running from IDE
				jarParentFolder = ".";
			else
				jarParentFolder = program.getParent();
		}
		catch (URISyntaxException e) {
			jarParentFolder = "~";
		}
		userDefaults = new File(VectorCalculator.jarParentFolder, "user-defaults.xml");

		all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.setOpaque(true);
		
		//GENERAL PROPERTIES TABLE
		genPropertiesModel = new DefaultTableModel(null, genPropertiesTitles); //may have to actually load something
		genPropertiesTable = new JTable(genPropertiesModel) {
			
			public TableCellEditor getCellEditor(int row, int column) {
                int modelColumn = convertColumnIndexToModel(column);

				if (modelColumn == 0)
					return null; //return super.getCellEditor(row, column);
				switch(rowParams.get(row)) {
					case solve_for_initial_angle:
						return dropdown(new String[]{"Yes", "No"});
					case calculate_using:
						String[] options = p.initialAndTargetGiven ? new String[]{"Target Angle", "Target Coordinates"} :
																	 new String[]{"Initial Angle", "Target Angle", "Target Coordinates"};
						return dropdown(options);
					case duration_type:
						return dropdown(new String[]{"Frames", "Vertical Displacement"});
					case vector_direction:
						return dropdown(new String[]{"Left", "Right"});
					case midairs:
						return dropdown(midairPresetNames);
					case triple_throw:
						if (p.canTestTripleThrow)
							return dropdown(new String[]{"Yes", "No", "Test Both"});
						else
							return dropdown(new String[]{"Yes", "No"});
					case gravity:
						return dropdown(new String[]{"Regular", "Moon"});
					case turnarounds:
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
						if (p.mode == Mode.SOLVE || p.mode == Mode.SOLVE_DIVES)
							return dropdown(new String[]{"Yes", "No", "Test Both"});
						else
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
						if (p.midairPreset.equals("Custom") || p.midairPreset.equals("None"))
							return dropdown(new String[]{"Calculate (Solve Dives)", "Calculate"});
						else
							return dropdown(new String[]{"Solve", "Calculate (Solve Dives)", "Calculate"});
					default:
						return super.getCellEditor(row, column);
				}
            }
        
			@Override
			public boolean isCellEditable(int row, int column) {
				if (!cellsEditable)
					return false;
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
		
		// Prevent menu accelerators from activating cell editors
		genPropertiesTable.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				KeyStroke ks = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());
				if (isMenuAccelerator(ks) && MainJMenuBar.instance != null && MainJMenuBar.instance.hasEnabledAccelerator(ks)) {
					cellsEditable = false;
					if (genPropertiesTable.getCellEditor() != null) {
						genPropertiesTable.getCellEditor().cancelCellEditing();
					}
				}
			}
		});
		
		JScrollPane genPropertiesScrollPane = new JScrollPane(genPropertiesTable);
		
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
					case null:
						break;
					case initial_coordinates:
						initial_CoordinateWindow.display(p.x0, p.y0, p.z0);
						if (add_ic_listener) {
							(initial_CoordinateWindow.getConfirmButton()).addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									setProperty(Parameter.initial_coordinates, initial_CoordinateWindow.getCoordinates());
									initial_CoordinateWindow.close();
									p.selectedParam = Parameter.initial_coordinates;
									UndoManager.recordState(true);
									checkIfSaved(true);
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
									p.selectedParam = Parameter.target_coordinates;
									UndoManager.recordState(true);
									checkIfSaved(true);
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
				int col = e.getColumn();

				if (row >= genPropertiesModel.getRowCount() || row < 0 || col != 1) {
					return;
				}

				if (!settingPropertyRow) {
					Parameter param = rowParams.get(row);
					setProperty(row);
					refreshPropertiesRows(getRowParams(), false);

					initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);

					checkIfSaved(true);

					selectParamRow(param);
					p.selectedParam = param;

					// record undo state for user edits
					if (initialized && !loading) {
						UndoManager.recordState(true);
					}
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
				if (!cellsEditable)
					return false;
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
		movementTable.getTableHeader().setReorderingAllowed(false);
		
		// Prevent menu accelerators from activating cell editors
		movementTable.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				KeyStroke ks = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());
				if (isMenuAccelerator(ks) && MainJMenuBar.instance != null && MainJMenuBar.instance.hasEnabledAccelerator(ks)) {
					cellsEditable = false;
					if (movementTable.getCellEditor() != null) {
						movementTable.getCellEditor().cancelCellEditing();
					}
				}
			}
		});
		
		JScrollPane movementScrollPane = new JScrollPane(movementTable);
		
		TableColumn movementColumn = movementTable.getColumnModel().getColumn(0);
		movementColumn.setCellEditor(new MyComboBoxEditor(midairMovementNames));
		
		// Add selection listener to update menu when selection changes
		movementTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				MainJMenuBar.updateCalculatorMenuItems();
			}
		});
		
		movementModel.addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
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
				// record undo state for midairs edits
				if (initialized && !loading && !addingPreset) {
					UndoManager.recordState(true);
				}
				checkIfSaved(true);
			}
		});
		
	
		//BUTTONS AND ERROR MESSAGE
		
		JPanel buttons = new JPanel(new BorderLayout());
		JPanel error = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel movementEdit = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel calculateVectorPanel = new JPanel();
		buttons.add(movementEdit, BorderLayout.WEST);
		buttons.add(calculateVectorPanel, BorderLayout.EAST);
		errorMessage = new JLabel("");
		errorMessage.setForeground(Color.RED);
		error.add(errorMessage);
		progressMessage = new JLabel("");
		progressMessage.setForeground(new Color(102, 153, 0));
		error.add(progressMessage);
		buttons.add(error, BorderLayout.SOUTH);
		
		add = new JButton("+");
		remove = new JButton("-");
		calculateVector = new JButton("Solve");
		add.setActionCommand("add");
		remove.setActionCommand("remove");
		calculateVector.setActionCommand("calculate");
		
		movementEdit.add(add);
		movementEdit.add(remove);
		calculateVectorPanel.add(calculateVector);
		
		ButtonListener buttonListen = new ButtonListener();
		add.addActionListener(buttonListen);
		remove.addActionListener(buttonListen);
		calculateVector.addActionListener(buttonListen);

		//CREATING THE WINDOW
		
		JPanel nonResize = new JPanel(new BorderLayout());
		nonResize.add(movementScrollPane, BorderLayout.CENTER);
		nonResize.add(buttons, BorderLayout.SOUTH);

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
        tabbedPane.setPreferredSize(new Dimension(WINDOW_WIDTH, PROPERTIES_TABLE_HEIGHT));
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
		loadUserDefaults();
		VectorCalculator.file = null; //so we don't save to it
		// initialize undo history to current state
		UndoManager.clear();
		UndoManager.recordState(true);
		initialized = true;
		MainJMenuBar.updateCalculatorMenuItems();
		
		//f.add(resize, BorderLayout.CENTER);
		f.setSize(WINDOW_WIDTH, PROPERTIES_TABLE_HEIGHT + MIDAIR_PANEL_HEIGHT);
		//f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

}

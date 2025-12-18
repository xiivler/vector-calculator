package vectorCalc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

public class VectorCalculator extends JPanel {
	
	static Font tableFont = new Font("Verdana", Font.PLAIN, 14);
	
	//category for falling for height calculator?
	static String[] initialMovementCategories = {"Distance Jumps", "Height Jumps", "Roll Cancel Vectors", "Rolls", "Object-Dependent Motion"};
	static String[][] initialMovementNames =
		{{"Single Jump", "Double Jump", "Triple Jump", "Vault", "Cap Return Jump", "Long Jump", "Optimal Distance Motion"},
		{"Triple Jump", "Ground Pound Jump", "Backflip", "Sideflip", "Vault", "Spin Jump"},
		{"Motion Cap Throw RCV", "Single Throw RCV", "Upthrow RCV", "Downthrow RCV", "Double Throw RCV", "Spinthrow RCV", "Triple Throw RCV", "Fakethrow RCV", "Optimal Distance RCV"},
		{"Ground Pound Roll", "Crouch Roll", "Roll Boost"},
		{"Horizontal Pole/Fork Flick", "Motion Horizontal Pole/Fork Flick", "Motion Vertical Pole/Fork Flick", "Small NPC Bounce", "Large NPC Bounce", "Ground Pound Object/Enemy Bounce", "Uncapture", "Bouncy Object Bounce", "Flower Bounce", "Flip Forward", "Swinging Jump"}}; //flower spinpound for height calculator
	
	static String[] midairMovementNames = {"Motion Cap Throw", "Triple Throw", "Homing Motion Cap Throw", "Homing Triple Throw", "Rainbow Spin", "Dive", "Cap Bounce", "2P Midair Vault"};
	
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
	static int GRAVITY_ROW = 11;
	static int HYPEROPTIMIZE_ROW = 12;
	static int AXIS_ORDER_ROW = 13;
	static int CAMERA_TYPE_ROW = 14;
	static int CAMERA_ROW = 15;

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
		GRAVITY_ROW--;
		HYPEROPTIMIZE_ROW--;
		AXIS_ORDER_ROW--;
		CAMERA_TYPE_ROW--;
		CAMERA_ROW--;
	}

	static enum AngleType {
		INITIAL, TARGET, BOTH
	}

	static enum CameraType {
		INITIAL, TARGET, ABSOLUTE, CUSTOM
	}

	static final int LOCK_NONE = 0;
	static final int LOCK_FRAMES = 1;
	static final int LOCK_VERTICAL_DISPLACEMENT = 2;
	
	static double x0 = 0, y0 = 0, z0 = 0;
	static double x1 = 0, y1 = 0, z1 = 3000;
	static boolean targetCoordinates = true;
	static double initialAngle = 0;
	static double targetAngle = 0;
	static AngleType angleType = AngleType.TARGET;
	static String initialMovementName = "Triple Jump";
	static boolean durationFrames = true;
	static int initialFrames = 70;
	static double initialDispY = 0;
	static int framesJump = 10;
	static boolean canMoonwalk = true;
	static int framesMoonwalk = 0;
	static double initialHorizontalSpeed = 24;
	static boolean rightVector = false;
	static double diveCapBounceAngle = 0; //how many more degrees the cap throw should be to the side than the dive angle
	static boolean onMoon = false;
	static boolean hyperoptimize = true;
	static boolean xAxisZeroDegrees = true;
	static CameraType cameraType = CameraType.TARGET;
	static double customCameraAngle = 0;
	
	static Movement initialMovement = new Movement(initialMovementName);
	static SimpleMotion initialMotion = new SimpleMotion(initialMovement, initialFrames);
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
	static CoordinateWindow initial_CoordinateWindow = new CoordinateWindow("Initial Coordinates");
	static CoordinateWindow target_CoordinateWindow = new CoordinateWindow("Target Coordinates");
	static DefaultTableModel movementModel = new DefaultTableModel(0, 2);
	static JTable movementTable;

	static String[] genPropertiesTitles = {"Property", "Value"};
	static Object[][] genProperties =
		{{"Initial Coordinates", "(0, 0, 0)"},
		{"Calculate Using", "Target Coordinates"},
		{"Target Coordinates", "(0, 0, 3000)"},
		{"Initial Movement Type", initialMovementName},
		{"Initial Movement Duration Type", "Frames"},
		{"Initial Movement Frames", initialFrames},
		{"Frames of Holding A/B", framesJump},
		{"Moonwalk Frames", framesMoonwalk},
		{"Initial Horizontal Speed", (int) initialHorizontalSpeed},
		{"Initial Vector Direction", "Left"},
		{"Edge Cap Bounce Angle", "0"},
		{"Gravity", "Regular"},
		{"Hyperoptimize Cap Throws", "True"},
		{"0 Degree Axis", "X"},
		{"Camera Angle", "Target Angle"}};
	
	static JFrame f = new JFrame("Configure Movement");
	
	public static BigDecimal round(double d, int places) {
			return BigDecimal.valueOf(d).setScale(places, RoundingMode.HALF_UP);
		}

	public static void lockDurationType(int value) {
		lockDurationType = value;
		Debug.println(lockDurationType);
		if (lockDurationType == LOCK_FRAMES) {
			genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_ROW, 0);
			if (durationFrames == false) {
				durationFrames = true;
				genPropertiesTable.setValueAt(initialMovement.minRecommendedFrames, MOVEMENT_DURATION_ROW, 0);
				initialFrames = initialMovement.minRecommendedFrames;
			}
		}
		else if (lockDurationType == LOCK_VERTICAL_DISPLACEMENT) {
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
			if (durationFrames == true) {
				durationFrames = false;
				genPropertiesTable.setValueAt(0, MOVEMENT_DURATION_ROW, 1);
				initialDispY = 0;
			}
		}
	}

	public static void setAngleType(AngleType type, boolean coordinates) {
		forceEdit = true;
		AngleType oldAngleType = angleType;
		Debug.println(oldAngleType);
		Debug.println(type);
		angleType = type;
		if (oldAngleType != AngleType.BOTH && type == AngleType.BOTH) {
			addAngle2Row();
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_2_ROW, 0);
			if (oldAngleType == AngleType.INITIAL) {
				targetAngle = initialAngle;
				genPropertiesTable.setValueAt("Target Angle", ANGLE_TYPE_ROW, 1);
				genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
				genPropertiesTable.setValueAt(targetAngle, ANGLE_ROW, 1);
			}
			else if (oldAngleType == AngleType.TARGET) {
				initialAngle = targetAngle;
			}
			genPropertiesTable.setValueAt(initialAngle, ANGLE_2_ROW, 1);
		}
		else if (oldAngleType == AngleType.BOTH && type == AngleType.TARGET) {
			removeAngle2Row();
			//genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
			//genPropertiesTable.setValueAt(targetAngle, ANGLE_ROW, 1);
			//genPropertiesTable.setValueAt("Target Angle", ANGLE_TYPE_ROW, 1);
		}
		else if (oldAngleType == AngleType.BOTH && type == AngleType.INITIAL) {
			removeAngle2Row();
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_ROW, 0);
			genPropertiesTable.setValueAt(initialAngle, ANGLE_ROW, 1);
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_TYPE_ROW, 1);
		}
		else if (oldAngleType == AngleType.TARGET && type == AngleType.INITIAL) {
			genPropertiesTable.setValueAt("Initial Angle", ANGLE_ROW, 0);
			initialAngle = targetAngle;
		}
		else if (oldAngleType == AngleType.INITIAL && type == AngleType.TARGET) {
			if (coordinates) {
				genPropertiesTable.setValueAt("Target Coordinates", ANGLE_ROW, 0);
				genPropertiesTable.setValueAt("(0, 0, 0)", ANGLE_ROW, 1);
				x1 = 0;
				y1 = 0;
				z1 = 0;
			}
			else {
				genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
				targetAngle = initialAngle;
			}
		}
		else if (oldAngleType == AngleType.TARGET && type == AngleType.TARGET || oldAngleType == AngleType.BOTH && type == AngleType.BOTH) {
			if (!targetCoordinates && coordinates) {
				genPropertiesTable.setValueAt("Target Coordinates", ANGLE_ROW, 0);
				genPropertiesTable.setValueAt("(0, 0, 0)", ANGLE_ROW, 1);
				x1 = 0;
				y1 = 0;
				z1 = 0;
			}
			else if (targetCoordinates && !coordinates) {
				genPropertiesTable.setValueAt("Target Angle", ANGLE_ROW, 0);
				if (targetAngle == (int) targetAngle)
					genPropertiesTable.setValueAt((int) targetAngle, ANGLE_ROW, 1);
				else {
					genPropertiesTable.setValueAt(targetAngle, ANGLE_ROW, 1);
				}
			}
		}
		Debug.println("Initial Angle: " + initialAngle);
		Debug.println("Target Angle: " + targetAngle);
		targetCoordinates = coordinates;
		forceEdit = false;
	}

	public static void setCameraType(CameraType type) {
		Debug.println("Setting camera type to " + type);
		CameraType oldCameraType = cameraType;
		cameraType = type;
		if (cameraType == CameraType.CUSTOM && oldCameraType != CameraType.CUSTOM) {
			if ((int) customCameraAngle == customCameraAngle) {
				genPropertiesModel.addRow(new Object[]{"Custom Camera Angle", (int) customCameraAngle});
			}
			else {
				genPropertiesModel.addRow(new Object[]{"Custom Camera Angle", customCameraAngle});
			}
		}
		else if (cameraType != CameraType.CUSTOM && oldCameraType == CameraType.CUSTOM) {
			genPropertiesModel.removeRow(genPropertiesModel.getRowCount() - 1);
		}
	}

	public static void targetCoordinatesToTargetAngle() {
		targetAngle = Math.toDegrees(Math.atan2(x1 - x0, z1 - z0));
		if (xAxisZeroDegrees) {
			targetAngle = 90 - targetAngle;
		}
		if (targetAngle < 0) {
			targetAngle += 360;
		}
		Debug.println("Target Angle from Coordinates: " + targetAngle);
	}
	
	public static int getMoonwalkDisp() {
		if (framesMoonwalk == 0)
			return 0;
		else if (framesMoonwalk == 1)
			return -3;
		else if (framesMoonwalk == 2)
			return -9;
		else if (framesMoonwalk == 3)
			return -18;
		else if (framesMoonwalk == 4)
			return -30;
		else if (framesMoonwalk == 5)
			return -45;
		else //impossible case
			return 0;
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
			 else if (evt.getActionCommand() == "calculate") {
				VectorMaximizer maximizer = null;
				if (targetCoordinates) {
					targetCoordinatesToTargetAngle();
				}
				if (initialMovementName.equals("Optimal Distance Motion")) {
					initialMovementName = "Triple Jump";
					framesJump = 10;
					initialMovement = new Movement(initialMovementName, initialHorizontalSpeed, framesJump);
					VectorMaximizer maximizerTJ = calculate();
					initialMovementName = "Optimal Distance RCV";
					initialMovement = new Movement(initialMovementName, initialHorizontalSpeed, framesJump);
					VectorMaximizer maximizerRC = calculate();
					if (maximizerTJ != null && maximizerRC != null) {
						if (maximizerTJ.bestDisp > maximizerRC.bestDisp) {
							maximizer = maximizerTJ;
						}
						else {
							maximizer = maximizerRC;
						}
					}
					initialMovementName = "Optimal Distance Motion";
				}
				else {
					maximizer = calculate();
				}
				if (maximizer != null) {
					VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
					VectorDisplayWindow.display();
				}
				 
				Debug.println();
			 }
		 }
	}

	public static VectorMaximizer calculate() {
		Movement.onMoon = onMoon;
		MovementNameListPreparer movementPreparer = new MovementNameListPreparer();
		String errorText = movementPreparer.prepareList();
		movementPreparer.print();

		if (errorText.equals("")) {
			errorMessage.setText("");
			VectorMaximizer maximizer = new VectorMaximizer(movementPreparer);
			maximizer.maximize(); 
			return maximizer;
		}
		else {
			errorMessage.setText("Error: " + errorText);
			return null;
		}
	}

	public static void main(String[] args) {
		
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
					if (angleType == AngleType.BOTH)
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
                else if (modelColumn == 1 && row == GRAVITY_ROW)
                {
                	String[] options = {"Regular", "Moon"};
                    JComboBox<String> angle = new JComboBox<String>(options);
                    return new DefaultCellEditor(angle);
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
				if (column == 0 || row == INITIAL_COORDINATES_ROW || (row == ANGLE_ROW && targetCoordinates) || row == INITIAL_MOVEMENT_TYPE_ROW || (row == HOLD_JUMP_FRAMES_ROW && !chooseJumpFrames) || (row == MOONWALK_FRAMES_ROW && !canMoonwalk) || (row == INITIAL_HORIZONTAL_SPEED_ROW && !chooseInitialHorizontalSpeed))
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
		genPropertiesScrollPane.setPreferredSize(new Dimension(500, genPropertiesTable.getRowHeight() * (genProperties.length + 1) + 25));
		
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
							initialMovementName = dialogWindow.getSelectedMovementName();
							initialMovement = new Movement(initialMovementName);
							genPropertiesModel.setValueAt(initialMovementName, INITIAL_MOVEMENT_TYPE_ROW, 1);
							double suggestedSpeed = initialMovement.getSuggestedSpeed();
							initialHorizontalSpeed = suggestedSpeed;
							if (!chooseJumpFrames && initialMovement.variableJumpFrames()) {
								chooseJumpFrames = true;
								genPropertiesModel.setValueAt(10, HOLD_JUMP_FRAMES_ROW, 1);
								framesJump = 10;
							}
							else if (chooseJumpFrames && !initialMovement.variableJumpFrames()) {
								chooseJumpFrames = false;
								genPropertiesModel.setValueAt("N/A", HOLD_JUMP_FRAMES_ROW, 1);
							}
							if (!canMoonwalk && initialMovement.canMoonwalk) {
								canMoonwalk = true;
								framesMoonwalk = 0;
								genPropertiesModel.setValueAt(0, MOONWALK_FRAMES_ROW, 1);
							}
							else if (canMoonwalk && !initialMovement.canMoonwalk) {
								canMoonwalk = false;
								framesMoonwalk = 0;
								genPropertiesModel.setValueAt("N/A", MOONWALK_FRAMES_ROW, 1);
							}
							if (initialMovement.variableInitialHorizontalSpeed()) {
								chooseInitialHorizontalSpeed = true;
								if (suggestedSpeed == (int) suggestedSpeed)
									genPropertiesModel.setValueAt((int) initialMovement.getSuggestedSpeed(), INITIAL_HORIZONTAL_SPEED_ROW, 1);
								else
									genPropertiesModel.setValueAt(initialMovement.getSuggestedSpeed(), INITIAL_HORIZONTAL_SPEED_ROW, 1);
								//TODO
							}
							else {
								chooseInitialHorizontalSpeed = false;
								genPropertiesModel.setValueAt("N/A", INITIAL_HORIZONTAL_SPEED_ROW, 1);
								initialHorizontalSpeed = 0;
							}
							if (initialMovementName.contains("RCV")) {
								setAngleType(AngleType.BOTH, targetCoordinates);
							}
							else if (angleType == AngleType.BOTH) { //switch back to just Initial or Target angle
								setAngleType(AngleType.TARGET, targetCoordinates);
							}
							
							if (initialMovementName.contains("Optimal Distance")) {
								lockDurationType(LOCK_VERTICAL_DISPLACEMENT);
							}
							else {
								lockDurationType(LOCK_NONE);
							}
							dialogWindow.close();	
						}
					});
				}
				else if (genPropertiesTable.rowAtPoint(evt.getPoint()) == INITIAL_COORDINATES_ROW && genPropertiesTable.columnAtPoint(evt.getPoint()) == 1) {
					initial_CoordinateWindow.display(x0, y0, z0);
					if (add_ic_listener) {
						(initial_CoordinateWindow.getConfirmButton()).addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								initial_CoordinateWindow.findCoordinates();
								x0 = initial_CoordinateWindow.x;
								y0 = initial_CoordinateWindow.y;
								z0 = initial_CoordinateWindow.z;
								genPropertiesModel.setValueAt(initial_CoordinateWindow.coordinates, INITIAL_COORDINATES_ROW, 1);
								initial_CoordinateWindow.close();
							}
						});
					}
					add_ic_listener = false;
				}
				else if (targetCoordinates && genPropertiesTable.rowAtPoint(evt.getPoint()) == ANGLE_ROW && genPropertiesTable.columnAtPoint(evt.getPoint()) == 1) {
					target_CoordinateWindow.display(x1, y1, z1);
					// JButton target_confirm = target_CoordinateWindow.getConfirmButton();
					// for (ActionListener al : target_confirm.getActionListeners()) {
					// 	target_confirm.removeActionListener(al);
					// }
					if (add_tc_listener) {
						(target_CoordinateWindow.getConfirmButton()).addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								target_CoordinateWindow.findCoordinates();
								x1 = target_CoordinateWindow.x;
								y1 = target_CoordinateWindow.y;
								z1 = target_CoordinateWindow.z;
								targetCoordinatesToTargetAngle();
								genPropertiesModel.setValueAt(target_CoordinateWindow.coordinates, ANGLE_ROW, 1);
								target_CoordinateWindow.close();
								System.out.println("Coords: " + target_CoordinateWindow.coordinates);
								System.out.println("Angle row: " + ANGLE_ROW);
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
				initialMovement = new Movement(initialMovementName, initialHorizontalSpeed, framesJump);

				int row = e.getFirstRow();

				if (row >= genPropertiesModel.getRowCount()) {
					return;
				}

				if (!forceEdit) { //forceEdit allows a part of the program to ignore these rules
					if (row == ANGLE_ROW) {
						if (angleType == AngleType.TARGET || angleType == AngleType.BOTH) {
							if (!targetCoordinates) {
								try {
									targetAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
								}
								catch (NumberFormatException ex) {
									targetAngle = 0;
									genPropertiesTable.setValueAt(0, row, 1);
								}
							}
						}
						else {
							try {
								initialAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {
								initialAngle = 0;
								genPropertiesTable.setValueAt(0, row, 1);
							}
						}
					}
					else if (row == ANGLE_2_ROW) {
						if (angleType == AngleType.BOTH) {
							try {
								initialAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {
								initialAngle = 0;
								genPropertiesTable.setValueAt(0, row, 1);
							}
						}
					}
					else if (row == ANGLE_TYPE_ROW) {
						if (genPropertiesTable.getValueAt(row, 1).equals("Initial Angle")) {
							setAngleType(AngleType.INITIAL, targetCoordinates);
						}
						else if (genPropertiesTable.getValueAt(row, 1).equals("Target Angle")) {
							if (angleType != AngleType.BOTH)
								setAngleType(AngleType.TARGET, false);
							else
								setAngleType(AngleType.BOTH, false);
						}
						else if (genPropertiesTable.getValueAt(row, 1).equals("Target Coordinates")) {
							if (angleType != AngleType.BOTH)
								setAngleType(AngleType.TARGET, true);
							else
								setAngleType(AngleType.BOTH, true);
						}
					}
					else if (row == INITIAL_MOVEMENT_TYPE_ROW || row == MOVEMENT_DURATION_ROW) {
						try {
							//movementType = genPropertiesModel.getValueAt(VectorCalculator.INITIAL_MOVEMENT_TYPE_ROW, 1).toString();
							int minFrames = initialMovement.minFrames;
							if (durationFrames) {
								initialFrames = Integer.parseInt(genPropertiesTable.getValueAt(MOVEMENT_DURATION_ROW, 1).toString());
								if (initialFrames < minFrames) {
									initialFrames = minFrames;
									genPropertiesTable.setValueAt(minFrames, MOVEMENT_DURATION_ROW, 1);
								}
							}
							else {
								initialDispY = Double.parseDouble(genPropertiesTable.getValueAt(MOVEMENT_DURATION_ROW, 1).toString());
								//add checks to make sure it isn't too big?
							}
						}
						catch (NumberFormatException ex) {
							if (durationFrames) {
								genPropertiesTable.setValueAt(1, MOVEMENT_DURATION_ROW, 1);
								initialFrames = 1;
							}
						}
					}
					else if (row == MOVEMENT_DURATION_TYPE_ROW) {
						if (lockDurationType == LOCK_NONE) {
							boolean oldDurationFrames = durationFrames;
							durationFrames = genPropertiesTable.getValueAt(MOVEMENT_DURATION_TYPE_ROW, 1).equals("Frames");
							Debug.println(durationFrames);
							initialMovement.initialHorizontalSpeed = initialHorizontalSpeed;
							Debug.println(initialMovement.initialHorizontalSpeed);
							initialMotion = initialMovement.getMotion(initialFrames, false, false);//new SimpleMotion(initialMovement, initialFrames);
							if (durationFrames && !oldDurationFrames) {
								genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_ROW, 0);
								initialFrames = initialMotion.calcFrames(initialDispY - getMoonwalkDisp());
								genPropertiesTable.setValueAt(initialFrames, MOVEMENT_DURATION_ROW, 1);
							}
							else if (!durationFrames && oldDurationFrames) {
								genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
								initialDispY = initialMotion.calcDispY(initialFrames) + getMoonwalkDisp();
								genPropertiesTable.setValueAt(initialDispY, MOVEMENT_DURATION_ROW, 1);
							}
						}
					}
					else if (row == HOLD_JUMP_FRAMES_ROW) {
						if (chooseJumpFrames) {
							framesJump = 0;
							try {
								framesJump = Integer.parseInt(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {};
							if (framesJump > 10) {
								framesJump = 10;
								genPropertiesTable.setValueAt(framesJump, row, 1);
							}
							if (framesJump < 1) {
								framesJump = 1;
								genPropertiesTable.setValueAt(framesJump, row, 1);
							}
							//genPropertiesTable.setValueAt(framesJump, row, 1);
						}
					}
					else if (row == MOONWALK_FRAMES_ROW) {
						if (canMoonwalk) {
							framesMoonwalk = 0;
							try {
								framesMoonwalk = Integer.parseInt(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {};
							if (framesMoonwalk > 5) {
								framesMoonwalk = 5;
								genPropertiesTable.setValueAt(framesMoonwalk, row, 1);
							}
							if (framesMoonwalk < 0) {
								framesMoonwalk = 0;
								genPropertiesTable.setValueAt(framesMoonwalk, row, 1);
							}
						}
					}
					else if (row == INITIAL_HORIZONTAL_SPEED_ROW) {
						if (chooseInitialHorizontalSpeed) {
							initialHorizontalSpeed = 0;
							try {
								initialHorizontalSpeed = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
							}
							catch (NumberFormatException ex) {
								genPropertiesTable.setValueAt(initialHorizontalSpeed, row, 1);
							}
							if (initialHorizontalSpeed < 0) {
								initialHorizontalSpeed = 0;
								genPropertiesTable.setValueAt(initialHorizontalSpeed, row, 1);
							}
						}
					}
					else if (row == VECTOR_DIRECTION_ROW) {
						rightVector = genPropertiesTable.getValueAt(row, 1).equals("Right");
					}
					else if (row == DIVE_CAP_BOUNCE_ANGLE_ROW) {
						try {
							diveCapBounceAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
						}
						catch (NumberFormatException ex) {
							diveCapBounceAngle = 0;
							genPropertiesTable.setValueAt(0, row, 1);
						}
						if (diveCapBounceAngle > 18.4)
							genPropertiesTable.setValueAt(18.4, row, 1);
						else if (diveCapBounceAngle < 0)
							genPropertiesTable.setValueAt(0, row, 1);
					}
					else if (row == GRAVITY_ROW) {
						onMoon = genPropertiesTable.getValueAt(row, 1).equals("Moon");
						Movement.onMoon = onMoon;
					}
					else if (row == HYPEROPTIMIZE_ROW) {
						hyperoptimize = genPropertiesTable.getValueAt(row, 1).equals("True");
					}
					else if (row == AXIS_ORDER_ROW) {
						xAxisZeroDegrees = genPropertiesTable.getValueAt(row, 1).equals("X");
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
						customCameraAngle = 0;
						try {
							customCameraAngle = Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
						}
						catch (NumberFormatException ex) {
							genPropertiesTable.setValueAt(0, row, 1);
						}
					}
				}
				
				//make whole numbers not have decimal places
				if (row != INITIAL_COORDINATES_ROW && (row != ANGLE_ROW || !targetCoordinates)) {
					String setString = genPropertiesTable.getValueAt(row, 1).toString();
					if (setString.contains(".")) {
						double setValue = Double.parseDouble(setString);
						if (setValue == (int) setValue)
							genPropertiesTable.setValueAt((int) setValue, row, 1);
					}
				}

				initialMovement = new Movement(initialMovementName, initialHorizontalSpeed, framesJump);
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
		
		JButton add = new JButton("+");
		JButton remove = new JButton("-");
		JButton calculateVector = new JButton("Calculate Vectors");
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
		
		f.add(nonResize, BorderLayout.CENTER);
		//f.add(resize, BorderLayout.CENTER);
		f.setSize(600, 540);
		//f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		
		//DEBUG PREPOLUATE MOVEMENT
		
/* 		movementModel.addRow(new String[]{"Motion Cap Throw", "24"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "42"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "24"});
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

		movementModel.addRow(new String[]{"Homing Triple Throw", "36"});
		movementModel.addRow(new String[]{"Rainbow Spin", "32"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "42"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "25"});
		

/* 		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "21"});
		movementModel.addRow(new String[]{"Cap Bounce", "42"});
		movementModel.addRow(new String[]{"Homing Triple Throw", "36"});
		movementModel.addRow(new String[]{"Rainbow Spin", "32"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "29"});
		movementModel.addRow(new String[]{"Dive", "25"}); */

		//setAngleType(AngleType.BOTH);
		//initialAngle = 65;
		//targetAngle = 90;
		//*/
	}

}

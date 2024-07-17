package vectorCalc;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
//import javax.swing.dataTable.DefaultTableModel;
//import javax.swing.dataTable.TableCellEditor;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class VectorCalculator extends JPanel {
	
	static Font tableFont = new Font("Verdana", Font.PLAIN, 14);
	
	//category for falling for height calculator?
	static String[] initialMovementCategories = {"Distance Jumps", "Height Jumps", "Roll Cancels", "Rolls", "Object-Dependent Motion"};
	static String[][] initialMovementNames =
		{{"Single Jump", "Double Jump", "Triple Jump", "Vault", "Cap Return Jump", "Long Jump"},
		{"Triple Jump", "Ground Pound Jump", "Backflip", "Sideflip", "Vault", "Spin Jump"},
		{"Motion Cap Throw Roll Cancel", "Single Throw Roll Cancel", "Upthrow Roll Cancel", "Downthrow Roll Cancel", "Double Throw Roll Cancel", "Spinthrow Roll Cancel", "Triple Throw Roll Cancel", "Fakethrow Roll Cancel", "Optimal Distance Roll Cancel"},
		{"Ground Pound Roll", "Crouch Roll", "Roll Boost"},
		{"Horizontal Pole/Fork Flick", "Motion Horizontal Pole/Fork Flick", "Motion Vertical Pole/Fork Flick", "Small NPC Bounce", "Large NPC Bounce", "Ground Pound NPC Bounce", "Uncapture", "Bouncy Object Bounce", "Flower Bounce", "Flip Forward", "Swinging Jump"}}; //flower spinpound for height calculator
	
	static String[] midairMovementNames = {"Motion Cap Throw", "Triple Throw", "Homing Motion Cap Throw", "Homing Triple Throw", "Rainbow Spin", "Dive", "Cap Bounce", "2P Midair Vault"};
	
	static String[] genPropertiesTitles = {"Property", "Value"};
	static Object[][] genProperties =
		{{"Initial X Position", 0},
		{"Initial Y Position", 0},
		{"Initial Z Position", 0},
		{"Angle", 0},
		{"Angle Type", "Target Angle"},
		{"Initial Movement Type", "Single Jump"},
		{"Initial Movement Duration Type", "Frames"},
		{"Initial Movement Frames", 60},
		{"Frames of Holding Jump", 10},
		{"Initial Horizontal Speed", 24},
		{"Initial Vector Direction", "Left"},
		{"Gravity", "Regular"}};
	static final int X_ROW = 0;
	static final int Y_ROW = 1;
	static final int Z_ROW = 2;
	static final int ANGLE_ROW = 3;
	static final int ANGLE_TYPE_ROW = 4;
	static final int INITIAL_MOVEMENT_TYPE_ROW = 5;
	static final int MOVEMENT_DURATION_TYPE_ROW = 6;
	static final int MOVEMENT_DURATION_ROW = 7;
	static final int HOLD_JUMP_FRAMES_ROW = 8;
	static final int INITIAL_HORIZONTAL_SPEED_ROW = 9;
	static final int VECTOR_DIRECTION_ROW = 10;
	static final int GRAVITY_ROW = 11;
	
	static int framesJump = 10;
	static int initialFrames = 60;
	static double initialDispY = 0;
	static double initialHorizontalSpeed = 24;
	static String initialMovementName = "Single Jump";
	
	static Movement initialMovement = new Movement("Single Jump");
	static SimpleMotion initialMotion = new SimpleMotion(initialMovement, 60);
	static boolean chooseJumpFrames = true;
	static boolean chooseInitialHorizontalSpeed = true;
	static boolean durationFrames = true;
	static int lockDurationType = 0;

	static final int NONE = 0;
	static final int FRAMES = 1;
	static final int VERTICAL_DISPLACEMENT = 2;
	
	static JLabel errorMessage;
	
	static String[] attributeTitles = {"Parameter", "Value"};
	static String[] movementTitles = {"Midair Movement Type", "Number of Frames"};
	static String[] movementRows = {"Motion Cap Throw", "8"};
	
	static JTable genPropertiesTable;
	static TableModel genPropertiesModel;
	static JumpDialogWindow dialogWindow = new JumpDialogWindow("Choose Initial Movement", initialMovementCategories, initialMovementNames);
	static DefaultTableModel movementModel = new DefaultTableModel(0, 2);
	static JTable movementTable;
	
	static JFrame f = new JFrame("Configure Movement");
	
	public static BigDecimal round(double d, int places) {
			return BigDecimal.valueOf(d).setScale(places, RoundingMode.HALF_UP);
		}

	public static void lockDurationType(int value) {
		lockDurationType = value;
		System.out.println(lockDurationType);
		if (lockDurationType == FRAMES) {
			genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_ROW, 0);
			if (durationFrames == false) {
				durationFrames = true;
				genPropertiesTable.setValueAt(initialMovement.minRecommendedFrames, MOVEMENT_DURATION_ROW, 0);
			}
		}
		else if (lockDurationType == VERTICAL_DISPLACEMENT) {
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_TYPE_ROW, 1);
			genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
			if (durationFrames == true) {
				durationFrames = false;
				genPropertiesTable.setValueAt(0, MOVEMENT_DURATION_ROW, 1);
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
			 else if (evt.getActionCommand() == "calculate") {
				 Movement.onMoon = genPropertiesModel.getValueAt(GRAVITY_ROW, 1).toString().equals("Moon");
				 MovementNameListPreparer movementPreparer = new MovementNameListPreparer();
				 String errorText = movementPreparer.prepareList();
				 movementPreparer.print();
				 
				 if (errorText.equals("")) {
					 errorMessage.setText("");
					 VectorMaximizer maximizer = new VectorMaximizer(movementPreparer);
					 maximizer.maximize(); 
					 VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
					 VectorDisplayWindow.display();
				 }
				 else
				 	errorMessage.setText("Error: " + errorText);
				 
				 System.out.println();
			 }
		 }
	}

	public static void main(String[] args) {
		
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.setOpaque(true);
		
		//GENERAL PROPERTIES TABLE
		
		genPropertiesTable = new JTable(genProperties, genPropertiesTitles) {
			
			public TableCellEditor getCellEditor(int row, int column)
            {
                int modelColumn = convertColumnIndexToModel( column );

                if (modelColumn == 1 && row == ANGLE_TYPE_ROW)
                {
                	String[] options = {"Target Angle", "Initial Angle"};
                    JComboBox<String> angle = new JComboBox<String>(options);
                    return new DefaultCellEditor(angle);
                }
				else if (modelColumn == 1 && row == MOVEMENT_DURATION_TYPE_ROW)
                {
					if (lockDurationType == NONE) {
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
                	String[] options = {"Left", "Right"}; //can add none option
                    JComboBox<String> angle = new JComboBox<String>(options);
                    return new DefaultCellEditor(angle);
                }
                else if (modelColumn == 1 && row == GRAVITY_ROW)
                {
                	String[] options = {"Regular", "Moon"};
                    JComboBox<String> angle = new JComboBox<String>(options);
                    return new DefaultCellEditor(angle);
                }
                else
                    return super.getCellEditor(row, column);
            }
        
			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0 || row == INITIAL_MOVEMENT_TYPE_ROW || (row == HOLD_JUMP_FRAMES_ROW && !chooseJumpFrames) || (row == INITIAL_HORIZONTAL_SPEED_ROW && !chooseInitialHorizontalSpeed))
					return false;
				return true;
			}
			
			@Override
			public boolean isCellSelected(int row, int column) {
		        if (!getRowSelectionAllowed() && !getColumnSelectionAllowed()) {
		            return false;
		        }
		        if (column == 0)
		        	return false;
		        return (!getRowSelectionAllowed() || isRowSelected(row)) &&
		               (!getColumnSelectionAllowed() || isColumnSelected(column));
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
		genPropertiesScrollPane.setPreferredSize(new Dimension(500, genPropertiesTable.getRowHeight() * genProperties.length + 25));
		
		genPropertiesModel = genPropertiesTable.getModel();
		ListSelectionModel genPropertiesSelectionModel = genPropertiesTable.getSelectionModel();
		
		//initial movement type selector
		genPropertiesTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (genPropertiesTable.rowAtPoint(evt.getPoint()) == INITIAL_MOVEMENT_TYPE_ROW && genPropertiesTable.columnAtPoint(evt.getPoint()) == 1)
					dialogWindow.display();
				JButton confirm = dialogWindow.getConfirmButton();
				confirm.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//System.out.println(dialogWindow.getSelectedMovementName());
						initialMovementName = dialogWindow.getSelectedMovementName();
						initialMovement = new Movement(initialMovementName);
						genPropertiesModel.setValueAt(initialMovementName, INITIAL_MOVEMENT_TYPE_ROW, 1);
						double suggestedSpeed = initialMovement.getSuggestedSpeed();
						if (!chooseJumpFrames && initialMovement.variableJumpFrames()) {
							chooseJumpFrames = true;
							genPropertiesModel.setValueAt(10, HOLD_JUMP_FRAMES_ROW, 1);
						}
						else if (chooseJumpFrames && !initialMovement.variableJumpFrames()) {
							chooseJumpFrames = false;
							genPropertiesModel.setValueAt("N/A", HOLD_JUMP_FRAMES_ROW, 1);
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
						}
						
						if (initialMovementName.equals("Optimal Distance Roll Cancel")) {
							lockDurationType(VERTICAL_DISPLACEMENT);
						}
						else {
							lockDurationType(NONE);
						}
						dialogWindow.close();	
					}
				});
			}
		});
			
		//fix bad values and update angle type in info properties table
		genPropertiesModel.addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				initialMovement = new Movement(initialMovementName, initialHorizontalSpeed, framesJump);

				int row = e.getFirstRow();
				int column = e.getColumn();
				if (row == MOVEMENT_DURATION_ROW || row == INITIAL_MOVEMENT_TYPE_ROW) {
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
				else if (row == MOVEMENT_DURATION_TYPE_ROW) {
					if (lockDurationType == NONE) {
						System.out.println("oops");
						boolean oldDurationFrames = durationFrames;
						durationFrames = genPropertiesTable.getValueAt(MOVEMENT_DURATION_TYPE_ROW, 1).equals("Frames");
						System.out.println(durationFrames);
						initialMovement.initialHorizontalSpeed = initialHorizontalSpeed;
						System.out.println(initialMovement.initialHorizontalSpeed);
						initialMotion = initialMovement.getMotion(initialFrames, false, false);//new SimpleMotion(initialMovement, initialFrames);
						if (durationFrames && !oldDurationFrames) {
							genPropertiesTable.setValueAt("Frames", MOVEMENT_DURATION_ROW, 0);
							genPropertiesTable.setValueAt(initialMotion.calcFrames(initialDispY), MOVEMENT_DURATION_ROW, 1);
						}
						else if (!durationFrames && oldDurationFrames) {
							genPropertiesTable.setValueAt("Vertical Displacement", MOVEMENT_DURATION_ROW, 0);
							genPropertiesTable.setValueAt(initialMotion.calcDispY(initialFrames), MOVEMENT_DURATION_ROW, 1);
						}
					}
				}
				/*
				else if (row == INITIAL_HORIZONTAL_SPEED_ROW) {
					try {
						if (Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString()) < - initialMovement.getTrueSpeedCap())
							genPropertiesTable.setValueAt(- initialMovement.getTrueSpeedCap(), row, 1);
						else if (Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString()) > initialMovement.getTrueSpeedCap())
							genPropertiesTable.setValueAt(initialMovement.getTrueSpeedCap(), row, 1);
					}
					catch (NumberFormatException ex) {
						genPropertiesTable.setValueAt(0, row, 1);
					}
				}
				*/		
				else if (row != ANGLE_TYPE_ROW && row != INITIAL_MOVEMENT_TYPE_ROW && row != VECTOR_DIRECTION_ROW && row != GRAVITY_ROW)
					try {
						Double.parseDouble(genPropertiesTable.getValueAt(row, 1).toString());
					}
					catch (NumberFormatException ex) {
						genPropertiesTable.setValueAt(0, row, 1);
					}
				
				//make whole numbers not have decimal places
				String setString = genPropertiesTable.getValueAt(row, 1).toString();
				if (setString.contains(".")) {
					double setValue = Double.parseDouble(setString);
					if (setValue == (int) setValue)
						genPropertiesTable.setValueAt((int) setValue, row, 1);
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
		f.setSize(600, 480);
		//f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		
		//DEBUG PREPOLUATE MOVEMENT
		/*
		movementModel.addRow(new String[]{"Motion Cap Throw", "20"});
		movementModel.addRow(new String[]{"Dive", "20"});
		movementModel.addRow(new String[]{"Cap Bounce", "20"});
		movementModel.addRow(new String[]{"Motion Cap Throw", "20"});
		movementModel.addRow(new String[]{"Dive", "20"});
		genPropertiesModel.setValueAt("Motion Cap Throw Roll Cancel", INITIAL_MOVEMENT_TYPE_ROW, 1);
		genPropertiesModel.setValueAt(120, MOVEMENT_DURATION_ROW, 1);
		genPropertiesModel.setValueAt(29.94, INITIAL_HORIZONTAL_SPEED_ROW, 1);
		*/
	}

}

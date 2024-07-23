package vectorCalc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class VectorDisplayWindow {

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
	static String[][] infoColumnData = {{"Initial Angle", ""}, {"Final X Position", ""}, {"Final Y Position", ""}, {"Final Z Position", ""}, {"Horizontal Displacement", ""}, {"Vertical Displacement", ""}, {"Total Frames", ""}};
	static final int INFO_ANGLE_TYPE_ROW = 0;
	static final int XF_ROW = 1;
	static final int YF_ROW = 2;
	static final int ZF_ROW = 3;
	static final int HORIZONTAL_DISPLACEMENT_ROW = 4;
	static final int VERTICAL_DISPLACEMENT_ROW = 5;
	static final int TOTAL_FRAMES_ROW = 6;
	
	static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Joystick (R; θ)", "Position (X, Y, Z)", "Velocity (Vx, Vy, Vz)", "Hor. Speed (V; θ)"};
	//static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Hold Angle", "X", "Y", "Z", "Vx", "Vy", "Vz", "Horizontal Speed"};
	
	static JFrame frame;
	
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
		infoScrollPane.setPreferredSize(new Dimension(500, 151));
		
		
		//DATA TABLE
		
		dataTableModel = new DefaultTableModel(0, 7);
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
		//dataTable.getColumnModel().getColumn(7).setPreferredWidth(160);
		
		JScrollPane dataScrollPane = new JScrollPane(dataTable);
		
		frame = new JFrame("Vector Calculations");
		frame.add(infoScrollPane, BorderLayout.NORTH);
		frame.add(dataScrollPane, BorderLayout.CENTER);
		frame.setSize(1000, 600);
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
		theta = reduceAngle(theta);
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
		if (printTSV) {
			try {
            	File destination = new File("vector.tsv");
            	print = new PrintWriter(destination);
				print.println("\\\\\tOptimized using Vector Calculator");
				print.println("\\\\\tCopy and paste this into your script");
				print.println("\\\\\tAdjust the below variable as needed");
				print.println("$TA = 0");
				currentPrintLine = "";
				if (simpleMotions[0].movement.TSVInputs.size() > 0) {
					currentPrintLine = simpleMotions[0].movement.TSVInputs.get(0).toLowerCase();
				}
				lastPrintLine = currentPrintLine;
        	}
			catch (FileNotFoundException e) {}
		}
		
		v0 = VectorCalculator.initialHorizontalSpeed;
		
		clearDataTable();
		
		dataTableModel.addRow(new Object[] {0, "", "", "", toCoordinates(VectorCalculator.x0, VectorCalculator.y0, VectorCalculator.z0), toVelocityVector(v0 * Math.cos(initialAngle), 0, v0 * Math.sin(initialAngle)), toPolarCoordinates(v0, Math.toDegrees(initialAngle))});
		
		double x = VectorCalculator.x0;
		double y = VectorCalculator.y0;
		double z = VectorCalculator.z0;
		
		double[][] info = null;
		
		int identicalLineCount = 1;

		int row = 1;
		for (int index = 0; index < simpleMotions.length; index++) {
			SimpleMotion motion = simpleMotions[index];
			if (motion.frames == 0) {
				continue;
			}
			//if (printTSV && print != null && motion.movement.displayName != "" && motion.movement.movementType != "Ground Pound") {
			//	print.println("\\\\\t" + motion.movement.displayName);
			//}
			motion.calcDisp();
			motion.setInitialCoordinates(x, y, z);
			info = motion.calcFrameByFrame();
			//for (double[] ds : info)
			//	System.out.println(Arrays.toString(ds));
			int startRow = row;
			for (int i = 0; i < info.length; i++, row++) {
				Object[] rowContents = new Object[7];
				rowContents[0] = row;
				rowContents[1] = "";
				rowContents[2] = "";
				rowContents[3] = toPolarCoordinatesJoystick(info[i][8], info[i][7]);
				rowContents[4] = toCoordinates(info[i][0], info[i][1], info[i][2]);
				rowContents[5] = toVelocityVector(info[i][3], info[i][4], info[i][5]);
				double velocityAngle = reduceAngle(Math.atan2(info[i][5], info[i][3]));
				if (info[i][6] == 0) {
					rowContents[6] = toPolarCoordinates(info[i][6], reduceAngle(motion.initialAngle));
				}
				else {
					rowContents[6] = toPolarCoordinates(info[i][6], velocityAngle);
				}
				dataTableModel.addRow(rowContents);
				if (i < motion.movement.inputs.size())
					dataTableModel.setValueAt(motion.movement.inputs.get(i), row - 1, 2);

				if (printTSV && print != null) {
					currentPrintLine = "";
					int tabs = 0;
					if (i + 1 < motion.movement.TSVInputs.size()) {
						currentPrintLine = motion.movement.TSVInputs.get(i + 1).toLowerCase();
					}
					else if (i == info.length - 1 && index + 1 < simpleMotions.length && simpleMotions[index + 1].movement.TSVInputs.size() > 0) {
						currentPrintLine = simpleMotions[index + 1].movement.TSVInputs.get(0).toLowerCase();
					}
					if (currentPrintLine.contains("\t")) {
						tabs = 1;
					}
					while (tabs < 2) {
						currentPrintLine += '\t';
						tabs++;
					}
					if (info[i][7] != SimpleMotion.NO_ANGLE) {
						System.out.println(Math.toDegrees(info[i][7] - targetAngle + Math.PI / 2));
						double theta = reduceAngle(info[i][7] - targetAngle + Math.PI / 2); //finding the holding angle if the camera is facing the target angle's direction
						double r = info[i][8];
						if (r == 1) {
							if (Math.round(theta) * 10000 == Math.round(theta * 10000)) { //check if equal with 4 decimal places
								currentPrintLine += String.format("ls($TA + %d)", Math.round(theta));
							}
							else {
								currentPrintLine += String.format("ls($TA + %.4f)", theta);
							}
						}
						else {
							if (Math.round(theta) * 10000 == Math.round(theta * 10000)) {
								currentPrintLine += String.format("ls(%.2f; $TA + %d)", r, Math.round(theta));
							}
							else {
								currentPrintLine += String.format("ls(%.2f; $TA + %.4f)", r, theta);
							}
						}
					}
					
					System.out.println("Current:" + currentPrintLine);
					System.out.println("Last:" + lastPrintLine);
					if (currentPrintLine.equals(lastPrintLine)) {
						identicalLineCount++;
					}
					else {
						String printLine = identicalLineCount + "\t" + lastPrintLine;
						System.out.println(printLine);
						print.println(printLine);
						identicalLineCount = 1;
					}
					lastPrintLine = currentPrintLine;
				}
			}
			x = info[info.length - 1][0];
			y = info[info.length - 1][1];
			z = info[info.length - 1][2];
			System.out.println(motion.movement.displayName);
			dataTableModel.setValueAt(motion.movement.displayName, startRow, 1);
		}

		if (printTSV && print != null) {
			if (identicalLineCount > 1) {
				print.println(identicalLineCount + "\t" + lastPrintLine);
			}
			print.close();
		}
	
		if (VectorCalculator.angleType == VectorCalculator.AngleType.TARGET) {
			infoTableModel.setValueAt("Initial Angle", INFO_ANGLE_TYPE_ROW, 0);
			infoTableModel.setValueAt(shorten(Math.toDegrees(initialAngle), 4), INFO_ANGLE_TYPE_ROW, 1);
		}
		else {
			infoTableModel.setValueAt("Target Angle", INFO_ANGLE_TYPE_ROW, 0);
			infoTableModel.setValueAt(shorten(Math.toDegrees(targetAngle), 4), INFO_ANGLE_TYPE_ROW, 1);
		}
		infoTableModel.setValueAt(shorten(x, 3), XF_ROW, 1);
		infoTableModel.setValueAt(shorten(y, 3), YF_ROW, 1);
		infoTableModel.setValueAt(shorten(z, 3), ZF_ROW, 1);
		infoTableModel.setValueAt(shorten(Math.sqrt(Math.pow(x - VectorCalculator.x0, 2) + Math.pow(z - VectorCalculator.z0, 2)), 3), HORIZONTAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt(shorten(y - VectorCalculator.y0, 3), VERTICAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt("" + (row - 1), TOTAL_FRAMES_ROW, 1);
	}
	
	public static void display() {
		frame.setVisible(true);
	}
}

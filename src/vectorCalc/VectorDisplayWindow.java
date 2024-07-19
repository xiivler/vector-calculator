package vectorCalc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class VectorDisplayWindow {

	static double x0;
	static double y0;
	static double z0;
	static double v0;
	
	static JTable infoTable;
	static JTable dataTable;
	static DefaultTableModel dataTableModel;
	static TableModel infoTableModel;
	
	static TableModel genPropertiesModel;
	
	static String[] infoColumnTitles = {"Attribute", "Value"};
	static String[][] infoColumnData = {{"Initial Angle", ""}, {"Final X Position", ""}, {"Final Y Position", ""}, {"Final Z Position", ""}, {"Horizontal Displacement", ""}, {"Vertical Displacement", ""}, {"Total Frames", ""}};
	static final int INFO_ANGLE_TYPE_ROW = 0;
	static final int XF_ROW = 1;
	static final int YF_ROW = 2;
	static final int ZF_ROW = 3;
	static final int HORIZONTAL_DISPLACEMENT_ROW = 4;
	static final int VERTICAL_DISPLACEMENT_ROW = 5;
	static final int TOTAL_FRAMES_ROW = 6;
	
	static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Hold Angle", "Position (X, Y, Z)", "Velocity (Vx, Vy, Vz)", "Hor. Speed (V; θ)"};
	//static String[] dataColumnTitles = {"Frame", "Movement Type", "Input(s)", "Hold Angle", "X", "Y", "Z", "Vx", "Vy", "Vz", "Horizontal Speed"};
	
	static JFrame frame;
	
	static {
		
		//INFO TABLE
		genPropertiesModel = VectorCalculator.genPropertiesModel;
		
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
		dataTable.getColumnModel().getColumn(3).setPreferredWidth(160);
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
		x0 = Double.parseDouble(genPropertiesModel.getValueAt(VectorCalculator.X_ROW, 1).toString());
		y0 = Double.parseDouble(genPropertiesModel.getValueAt(VectorCalculator.Y_ROW, 1).toString());
		z0 = Double.parseDouble(genPropertiesModel.getValueAt(VectorCalculator.Z_ROW, 1).toString());
		v0 = VectorCalculator.initialHorizontalSpeed;
		
		clearDataTable();
		
		dataTableModel.addRow(new Object[] {0, "", "", "", toCoordinates(x0, y0, z0), toVelocityVector(v0 * Math.cos(initialAngle), 0, v0 * Math.sin(initialAngle)), toPolarCoordinates(v0, Math.toDegrees(initialAngle))});
		
		double x = x0;
		double y = y0;
		double z = z0;
		
		double[][] info = null;
		
		int row = 1;
		for (SimpleMotion motion : simpleMotions) {
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
				if (info[i][7] != SimpleMotion.NO_ANGLE)
					rowContents[3] = shorten(reduceAngle(info[i][7]), 4);
				else
					rowContents[3] = "";
				rowContents[4] = toCoordinates(info[i][0], info[i][1], info[i][2]);
				rowContents[5] = toVelocityVector(info[i][3], info[i][4], info[i][5]);
				double velocityAngle = Math.toDegrees(Math.atan2(info[i][5], info[i][3]));
				if (velocityAngle < 0) {
					velocityAngle += 360;
				}
				rowContents[6] = toPolarCoordinates(info[i][6], velocityAngle);
				dataTableModel.addRow(rowContents);
				if (i < motion.movement.inputs.size())
					dataTableModel.setValueAt(motion.movement.inputs.get(i), row - 1, 2);
			}
			x = info[info.length - 1][0];
			y = info[info.length - 1][1];
			z = info[info.length - 1][2];
			System.out.println(motion.movement.displayName);
			dataTableModel.setValueAt(motion.movement.displayName, startRow, 1);	
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
		infoTableModel.setValueAt(shorten(Math.sqrt(Math.pow(x - x0, 2) + Math.pow(z - z0, 2)), 3), HORIZONTAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt(shorten(y - y0, 3), VERTICAL_DISPLACEMENT_ROW, 1);
		infoTableModel.setValueAt("" + (row - 1), TOTAL_FRAMES_ROW, 1);
	}
	
	public static void display() {
		frame.setVisible(true);
	}
}

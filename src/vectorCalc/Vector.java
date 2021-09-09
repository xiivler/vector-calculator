package vectorCalc;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Vector {
	
	double initialAngle;
	double normalAngle;
	double vectorAngle;
	
	double[] preVectorSpeeds = new double[0];
	int[] preVectorFrames = new int[0];
	double initialVectorSpeed;
	double accel;
	
	int frames;
	
	double displacement;
	double dispForward;
	double dispSideways;
	double x0;
	double y0;
	
	private boolean rightVector;
	
	public Object[][] data;
	
	public Vector(double angle, boolean vectorAngleGiven, double initialVectorSpeed, double accel, int frames, double x0, double y0, boolean rightVector) {
		
		if (vectorAngleGiven)
			this.vectorAngle = Math.toRadians(angle);
		else
			this.initialAngle = Math.toRadians(angle);
		
		this.initialVectorSpeed = initialVectorSpeed;
		this.accel = accel;
		this.frames = frames;
		this.x0 = x0;
		this.y0 = y0;
		this.rightVector = rightVector;
		
		calcDisp(frames);
		this.displacement = Math.sqrt(Math.pow(dispForward, 2) + Math.pow(dispSideways, 2));
		calcAngles(vectorAngleGiven);
		
		this.data = new Object[frames + 1][4];
		calcData();
	}
	
public Vector(double angle, boolean vectorAngleGiven, double[] preVectorSpeeds, int[] preVectorFrames, double initialVectorSpeed, double accel, int frames, double x0, double y0, boolean rightVector) {
		
		if (vectorAngleGiven)
			this.vectorAngle = Math.toRadians(angle);
		else
			this.initialAngle = Math.toRadians(angle);
		
		this.preVectorSpeeds = preVectorSpeeds;
		this.preVectorFrames = preVectorFrames;
		this.initialVectorSpeed = initialVectorSpeed;
		this.accel = accel;
		this.frames = frames;
		this.x0 = x0;
		this.y0 = y0;
		this.rightVector = rightVector;
		
		calcDisp(frames);
		this.displacement = Math.sqrt(Math.pow(dispForward, 2) + Math.pow(dispSideways, 2));
		calcAngles(vectorAngleGiven);
		
		this.data = new Object[frames + 1][4];
		calcData();
	}
	
	private void calcDisp(int numFrames) {
		dispForward = 0;
		for (int i = 0; i < preVectorFrames.length; i++) {
			dispForward += preVectorSpeeds[i] * Math.min(preVectorFrames[i], numFrames);
			numFrames -= Math.min(preVectorFrames[i], numFrames);
		}
		dispForward += initialVectorSpeed * numFrames;
		if (numFrames <= (int) (initialVectorSpeed / accel))
			dispSideways = accel / 2 * numFrames * (numFrames + 1);
		else
			dispSideways = .5 * initialVectorSpeed * (2 * numFrames - (int) (initialVectorSpeed / accel) + 1);
	}
	
	private void calcAngles(boolean vectorAngleGiven) {
		
		if (vectorAngleGiven) {
			if (rightVector)
				initialAngle = vectorAngle + Math.atan(dispSideways / dispForward);
			else
				initialAngle = vectorAngle - Math.atan(dispSideways / dispForward);
		}
		
		else
			if (rightVector)
				vectorAngle = initialAngle - Math.atan(dispSideways / dispForward);
			else
				vectorAngle = initialAngle + Math.atan(dispSideways / dispForward);
		
		if (rightVector)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
	}
	
	private void calcData() {
		//System.out.println("Vectoring to move in direction " + Math.toDegrees(vectorAngle) + "° from initial speed " + initialVectorSpeed + " for "+ frames + " frames:");
		//System.out.println();
		//System.out.println("Frame   Speed");
		for (int i = 0; i <= frames; i++) {
			data[i][0] = i;
			data[i][1] = calcPos(i);
			data[i][2] = calcVel(i);
			data[i][3] = calcSpeed(i);
		}
		System.out.println();
		//System.out.printf("Initial angle: %.3f\n", Math.toDegrees(initialAngle));
		//System.out.printf("Angle of sideways acceleration: %.3f\n", Math.toDegrees(normalAngle));
		//System.out.printf("Total displacement: %.3f\n", displacement);
	}
	
	private String calcPos(int numFrames) {
		calcDisp(numFrames);
		double x_numFrames = x0 + dispForward * Math.cos(initialAngle) + dispSideways * Math.cos(normalAngle);
		double y_numFrames = y0 + dispForward * Math.sin(initialAngle) + dispSideways * Math.sin(normalAngle);
		return String.format("(%.3f, %.3f)", x_numFrames, y_numFrames);
	}
	
	private String calcVel(int numFrames) {
		//int framesExplored = 0;
		double vx_numFrames;
		double vy_numFrames;
		for (int i = 0; i < preVectorFrames.length; i++) {
			if (numFrames - preVectorFrames[i] <= 0) {
				vx_numFrames = preVectorSpeeds[i] * Math.cos(initialAngle);
				vy_numFrames = preVectorSpeeds[i] * Math.sin(initialAngle);
				return String.format("<%.3f, %.3f>\n", vx_numFrames, vy_numFrames);
			}
			else
				numFrames -= preVectorFrames[i];
		}
		vx_numFrames = initialVectorSpeed * Math.cos(initialAngle) + Math.min(accel * numFrames, initialVectorSpeed) * Math.cos(normalAngle);
		vy_numFrames = initialVectorSpeed * Math.sin(initialAngle) + Math.min(accel * numFrames, initialVectorSpeed) * Math.sin(normalAngle);
		return String.format("<%.3f, %.3f>\n", vx_numFrames, vy_numFrames);
	}
	
	private BigDecimal calcSpeed(int numFrames) {
		for (int i = 0; i < preVectorFrames.length; i++) {
			if (numFrames - preVectorFrames[i] <= 0) {
				return BigDecimal.valueOf(preVectorSpeeds[i]);
			}
			else
				numFrames -= preVectorFrames[i];
		}
		return VectorCalculator.round(Math.sqrt(Math.pow(initialVectorSpeed, 2) + Math.pow(Math.min(accel * numFrames, initialVectorSpeed), 2)), 3);
	}
}

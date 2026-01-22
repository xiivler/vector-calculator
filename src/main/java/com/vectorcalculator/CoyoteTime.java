package com.vectorcalculator;

//calculates the behavior of vectorable movement if the joystick angle is consistent
public class CoyoteTime extends SimpleMotion {
	
	public CoyoteTime(Movement movement, int frames) {
		super(movement, frames);
		initialForwardVelocity = p.initialHorizontalSpeed;
	}

	public CoyoteTime(Movement movement, double initialAngle, int frames) {
		super(movement, initialAngle, frames);
		initialForwardVelocity = p.initialHorizontalSpeed;
	}

	Properties p = Properties.p;
	
	double normalAngle;

	public static final double WALKING_DECEL = 14.0 / 120.0;
	public static final int WALKING_Y_VEL = -7;
	
	public double calcDispForward() {
		dispForward = 0;
		double forwardVelocity = initialForwardVelocity;

		for (int i = 0; i < frames; i++) {
			dispForward += forwardVelocity;
			if (i < frames - 1 && forwardVelocity > 14 && !p.initialMovementName.contains("Rocket Flower")) {
				forwardVelocity -= WALKING_DECEL;
				if (forwardVelocity < 14)
					forwardVelocity = 14;
			}
		}

		finalForwardVelocity = forwardVelocity;
		finalSpeed = forwardVelocity;

		return dispForward;
	}

	public double calcFinalVerticalVelocity() {
		return WALKING_Y_VEL;
	}

	public double calcDispY() {
		return WALKING_Y_VEL * frames;
	}

	//must run calcDisp() first to calculate acceleration values and vectorFrames
	//column 0-2: (X, Y, Z), column 3-5: (X-vel, Y-vel, Z-vel), column 6: horizontal speed, column 7: holding angle, column 8: holding radius
	public double[][] calcFrameByFrame() {
		//maybe shouldn't use the disps for this
		dispX = x0;
		dispY = y0;
		dispZ = z0;
		double cosInitialAngle = Math.cos(initialAngle);
		double sinInitialAngle = Math.sin(initialAngle);
		double forwardVelocity = initialForwardVelocity;
		double zVelocity;
		double xVelocity;
		double[][] info = new double[frames][9];
		for (int i = 0; i < frames; i++) {
			info[i][7] = initialAngle;
			info[i][8] = 1;
			zVelocity = forwardVelocity * cosInitialAngle;
			xVelocity = forwardVelocity * sinInitialAngle;
			dispZ += zVelocity;
			if (i >= movement.frameOffset) {
				dispY += WALKING_Y_VEL;
				info[i][4] = WALKING_Y_VEL;
			}
			else
				info[i][4] = 0;
			dispX += xVelocity;
			info[i][0] = dispX;
			info[i][1] = dispY;
			info[i][2] = dispZ;
			info[i][3] = xVelocity;
			info[i][5] = zVelocity;
			info[i][6] = forwardVelocity;
			if (forwardVelocity > 14 && !p.initialMovementName.contains("Rocket Flower")) {
				forwardVelocity -= WALKING_DECEL;
				if (forwardVelocity < 14)
					forwardVelocity = 14;
			}
		}	
		return info;
	}
}


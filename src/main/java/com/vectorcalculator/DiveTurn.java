package com.vectorcalculator;

//calculates the behavior of vectorable movement if the joystick angle is consistent
public class DiveTurn extends SimpleMotion {
	
	double baseSidewaysAccel = .125;
	
	double normalAngle;

	double dispForward;
	double dispSideways;
	
	double finalSidewaysVelocity;
	
	boolean rightTurn;
	
	public DiveTurn(Movement movement, boolean rightTurn, int frames) {
		
		super(movement, frames);
		this.rightTurn = rightTurn;
		this.holdingAngle = NORMAL_ANGLE;

		if (rightTurn)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
	}
	
	
	public DiveTurn(Movement movement, double initialAngle, double holdingAngle, boolean rightTurn, int frames) {
		
		super(movement, initialAngle, frames);
		this.baseSidewaysAccel = movement.vectorAccel;
		this.rightTurn = rightTurn;
		this.holdingAngle = holdingAngle;

		if (rightTurn)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
	}
	
	public void calcDisp() {
		dispForward = 0;
		dispSideways = 0;
		double forwardVelocity = 20;
		double sidewaysVelocity = 0;
		double sidewaysAccel = baseSidewaysAccel * Math.sin(holdingAngle);

		//cap at 20 u/fr speed
		//double multiplier

		double velocityAngle = 0;
		for (int i = 0; i < frames; i++) {
			forwardVelocity -= sidewaysAccel * Math.sin(velocityAngle);
			sidewaysVelocity += sidewaysAccel * Math.cos(velocityAngle);
			dispForward += forwardVelocity;
			dispSideways += sidewaysVelocity;
			velocityAngle = Math.atan(sidewaysVelocity / forwardVelocity);
		}

		finalForwardVelocity = forwardVelocity;
		finalSidewaysVelocity = sidewaysVelocity;
	}
	
	public void calcDispCoords() {
		dispZ = dispForward * Math.cos(initialAngle) + dispSideways * Math.cos(normalAngle);
		dispX = dispForward * Math.sin(initialAngle) + dispSideways * Math.sin(normalAngle);
	}
	
	//requires calcDisp() to be called first
	public double calcFinalAngle() {		
		if (rightTurn)
			finalAngle = initialAngle - Math.atan(finalSidewaysVelocity / finalForwardVelocity);
		else
			finalAngle = initialAngle + Math.atan(finalSidewaysVelocity / finalForwardVelocity);
		return finalAngle;
		
	}
	
	//requires calcDisp() to be called first
	public double calcFinalSpeed() {
		finalSpeed = Math.sqrt(Math.pow(finalForwardVelocity, 2) + Math.pow(finalSidewaysVelocity, 2));
		return finalSpeed;
	}
	
	public double calcFinalRotation() {
		calcFinalAngle();
		finalRotation = finalAngle;
		return finalRotation;
	}
	
	//must run calcDisp() first to calculate acceleration values and vectorFrames
	//column 0-2: (X, Y, Z), column 3-5: (X-vel, Y-vel, Z-vel), column 6: horizontal speed, column 7: holding angle, column 8: holding radius
	public double[][] calcFrameByFrame() {
		dispForward = 0;
		dispSideways = 0;
		dispX = x0;
		dispY = y0;
		dispZ = z0;
		double gravity;
		if (Movement.onMoon)
			gravity = movement.moonGravity;
		else
			gravity = movement.gravity;
		double cosInitialAngle = Math.cos(initialAngle);
		double sinInitialAngle = Math.sin(initialAngle);
		double cosNormalAngle = Math.cos(normalAngle);
		double sinNormalAngle = Math.sin(normalAngle);
		double forwardVelocity = initialForwardVelocity;
		double sidewaysVelocity = 0;
		double zVelocity;
		double yVelocity = movement.initialVerticalSpeed;
		double xVelocity;
		
		double holdingAngleAdjusted;
		if (rightTurn)
			holdingAngleAdjusted = initialAngle - holdingAngle;
		else
			holdingAngleAdjusted = initialAngle + holdingAngle;
		
		double sidewaysAccel = baseSidewaysAccel * Math.sin(holdingAngle);
		double velocityAngle = 0;

		double deltaVelocityAngle = Math.atan(sidewaysAccel / initialForwardVelocity);

		double[][] info = new double[frames][9];
		for (int i = 0; i < frames; i++) {
			forwardVelocity -= sidewaysAccel * Math.sin(velocityAngle);
			sidewaysVelocity += sidewaysAccel * Math.cos(velocityAngle);
			velocityAngle = Math.atan(sidewaysVelocity / forwardVelocity);

			zVelocity = forwardVelocity * cosInitialAngle + sidewaysVelocity * cosNormalAngle;
			xVelocity = forwardVelocity * sinInitialAngle + sidewaysVelocity * sinNormalAngle;

			if (i >= movement.framesAtMaxVerticalSpeed + movement.frameOffset) {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			dispZ += zVelocity;
			if (i >= movement.frameOffset) {
				dispY += yVelocity;
				info[i][4] = yVelocity;
			}
			else
				info[i][4] = 0;
			dispX += xVelocity;
			info[i][0] = dispX;
			info[i][1] = dispY;
			info[i][2] = dispZ;
			info[i][3] = xVelocity;
			info[i][5] = zVelocity;
			info[i][6] = Math.sqrt(Math.pow(zVelocity, 2) + Math.pow(xVelocity, 2));
			if (rightTurn) {
				info[i][7] = holdingAngleAdjusted;
				holdingAngleAdjusted -= deltaVelocityAngle;
			}
			else {
				info[i][7] = holdingAngleAdjusted;
				holdingAngleAdjusted += deltaVelocityAngle;
			}
			info[i][8] = 1;
		}	
		return info;
	}

	public int getCapBounceFrame(double cappyPos[]) {
		double dispX = x0;
		double dispY = y0;
		double dispZ = z0;
		double gravity;
		if (Movement.onMoon)
			gravity = movement.moonGravity;
		else
			gravity = movement.gravity;
		double cosInitialAngle = Math.cos(initialAngle);
		double sinInitialAngle = Math.sin(initialAngle);
		double cosNormalAngle = Math.cos(normalAngle);
		double sinNormalAngle = Math.sin(normalAngle);
		double forwardVelocity = initialForwardVelocity;
		double sidewaysVelocity = 0;
		double zVelocity;
		double yVelocity = movement.initialVerticalSpeed;
		double xVelocity;

		double sidewaysAccel = baseSidewaysAccel * Math.sin(holdingAngle);
		double velocityAngle = 0;

		for (int i = 0; i < frames; i++) {
			forwardVelocity -= sidewaysAccel * Math.sin(velocityAngle);
			sidewaysVelocity += sidewaysAccel * Math.cos(velocityAngle);
			velocityAngle = Math.atan(sidewaysVelocity / forwardVelocity);

			zVelocity = forwardVelocity * cosInitialAngle + sidewaysVelocity * cosNormalAngle;
			xVelocity = forwardVelocity * sinInitialAngle + sidewaysVelocity * sinNormalAngle;

			dispX += xVelocity;
			if (i >= movement.framesAtMaxVerticalSpeed + movement.frameOffset) {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			dispY += yVelocity;
			dispZ += zVelocity;

			double diffX = dispX - cappyPos[0];
			double diffZ = dispZ - cappyPos[2];
			double hDistToCappy = Math.sqrt(diffX * diffX + diffZ * diffZ);
			double footY = dispY + 40;
			double bodyY = dispY + 75;
			double headY = dispY + 110;
			double cappyCatchY = cappyPos[1] + 20;
			double distBodyCappy = distance(dispX, bodyY, dispZ, cappyPos[0], cappyPos[1], cappyPos[2]);
			double distHeadCappy = distance(dispX, headY, dispZ, cappyPos[0], cappyPos[1], cappyPos[2]);
			double diffYFootCappyCatch = footY - cappyCatchY;
			double footCappyCatchAngle = Math.atan2(diffYFootCappyCatch, hDistToCappy);
			System.out.printf("%d %.3f %.3f %.3f %.3f\n", i + 1, distBodyCappy, distHeadCappy, diffYFootCappyCatch, Math.toDegrees(footCappyCatchAngle));
			if (distBodyCappy < 150 || distHeadCappy < 140 || (diffYFootCappyCatch < 70 && footCappyCatchAngle >= Math.toRadians(20))) {
					return i + 1;
			}
		}
		return -1; //won't bounce
	}

	public double distance(double x0, double y0, double z0, double x1, double y1, double z1) {
		double diffX = x1 - x0;
		double diffY = y1 - y0;
		double diffZ = z1 - z0;
		return Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
	} 
	
	public void setInitialAngle(double angle) {
	
		initialAngle = angle;
		
		if (rightTurn)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;

	}
	
	public void adjustInitialAngle(double angle) {
		
		initialAngle += angle;
		
		if (rightTurn)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
	}
	
	public void setHoldingAngle(double angle) {
		holdingAngle = angle;
	}
		
	public void setFrames(int n) {
		frames = n;
	}
}


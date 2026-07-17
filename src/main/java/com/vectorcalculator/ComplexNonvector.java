package com.vectorcalculator;

//calculates the motion for a vector where the joystick angles vary from frame to frame
public class ComplexNonvector extends SimpleMotion {
	
	Properties p = Properties.p;
	
	double[] holdingAngles;
	boolean[] holdingMinRadius;

	public static final double MIN_RADIUS = 0.11;
	
	double forwardVelocity;
	double sidewaysVelocity;

	boolean rightTurn;

	double normalAngle;

	double dispForward;
	double dispSideways;

	double finalSidewaysVelocity;

	double baseSidewaysAccel;
	
	public ComplexNonvector(Movement movement, boolean rightTurn, int frames) {
		super(movement, frames);
		this.rightTurn = rightTurn;
		this.baseSidewaysAccel = movement.forwardAccel; //TODO: use sideways accel for this
		if (rightTurn)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
	}
	
	public ComplexNonvector(Movement movement, double initialAngle, double[] holdingAngles, boolean rightTurn, int frames) {
		super(movement, initialAngle, frames);
		this.holdingAngles = holdingAngles;
		this.holdingMinRadius = new boolean[holdingAngles.length];
		this.rightTurn = rightTurn;
		this.baseSidewaysAccel = movement.forwardAccel;
		if (rightTurn)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
	}
	
	public ComplexNonvector(Movement movement, double initialAngle, double[] holdingAngles, boolean rightTurn) {
		super(movement, initialAngle, holdingAngles.length);
		this.holdingAngles = holdingAngles;
		this.holdingMinRadius = new boolean[holdingAngles.length];
		this.rightTurn = rightTurn;
		this.baseSidewaysAccel = movement.forwardAccel;
		if (rightTurn)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
	}
	
	public void calcDisp() {
		dispForward = 0;
		dispSideways = 0;
		double forwardVelocity = initialForwardVelocity;
		double velocityCap = Math.max(initialForwardVelocity, defaultSpeedCap);
		double sidewaysVelocity = 0;

		double velocityAngle = 0; //straight ahead

		for (int i = 0; i < frames; i++) {
			if (holdingAngles[i] != NO_ANGLE) {
				double forwardAccel;
				double sidewaysAccel;
				double holdingAngleAdjusted = holdingAngles[i] - velocityAngle;
				if (holdingAngleAdjusted <= NORMAL_ANGLE && holdingAngleAdjusted >= -NORMAL_ANGLE) {
					forwardAccel = baseForwardAccel * Math.cos(holdingAngleAdjusted);
				}
				else {
					forwardAccel = baseBackwardAccel * Math.cos(holdingAngleAdjusted);
				}
				sidewaysAccel = baseSidewaysAccel * Math.sin(holdingAngleAdjusted);
				if (holdingMinRadius[i]) {
					forwardAccel *= MIN_RADIUS;
					sidewaysAccel *= MIN_RADIUS;
				}
				forwardVelocity += forwardAccel * Math.cos(velocityAngle) - sidewaysAccel * Math.sin(velocityAngle);
				sidewaysVelocity += forwardAccel * Math.sin(velocityAngle) + sidewaysAccel * Math.cos(velocityAngle); //is it -forwardAccel * sin?
				//forwardVelocity += forwardAccel;
				//sidewaysVelocity += sidewaysAccel;

				double normalizer = Math.min(velocityCap / Math.hypot(forwardVelocity, sidewaysVelocity), 1);
				forwardVelocity *= normalizer;
				sidewaysVelocity *= normalizer;
				
				velocityAngle = Math.atan(sidewaysVelocity / forwardVelocity);

				dispForward += forwardVelocity;
				dispSideways += sidewaysVelocity;
			}
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
		if (movement.movementType.equals("Backflip"))
			return initialAngle;
		calcFinalAngle();
		finalRotation = finalAngle;
		return finalRotation;
	}
	
	//must run calcDisp() first to calculate acceleration values and vectorFrames
		//column 0-2: (X, Y, Z), column 3-5: (X-vel, Y-vel, Z-vel), column 6: horizontal speed, column 7: holding angle
		public double[][] calcFrameByFrame() {
			dispForward = 0;
			dispSideways = 0;
			dispX = x0;
			dispY = y0;
			dispZ = z0;
			double gravity;
			if (p.onMoon)
				gravity = movement.moonGravity;
			else
				gravity = movement.gravity;
			double cosInitialAngle = Math.cos(initialAngle);
			double sinInitialAngle = Math.sin(initialAngle);
			double cosNormalAngle = Math.cos(normalAngle);
			double sinNormalAngle = Math.sin(normalAngle);
			double forwardVelocity = initialForwardVelocity;
			double velocityCap = Math.max(initialForwardVelocity, defaultSpeedCap);
			double velocityAngle = 0;
			double sidewaysVelocity = 0;
			double zVelocity;
			double xVelocity;
			double yVelocity = movement.initialVerticalSpeed;
			
			double[] holdingAnglesAdjusted = new double[frames];
			for (int i = 0; i < frames; i++)
				if (holdingAngles[i] == NO_ANGLE)
					holdingAnglesAdjusted[i] = NO_ANGLE;
				else if (rightTurn)
					holdingAnglesAdjusted[i] = initialAngle - holdingAngles[i];
				else
					holdingAnglesAdjusted[i] = initialAngle + holdingAngles[i];
			
			double[][] info = new double[frames][9];
			for (int i = 0; i < frames; i++) {	
				if (holdingAngles[i] != NO_ANGLE) {
					double normalizer = Math.min(velocityCap / Math.hypot(forwardVelocity, sidewaysVelocity), 1);
					forwardVelocity *= normalizer;
					sidewaysVelocity *= normalizer;
					
					double forwardAccel;
					double sidewaysAccel;
					double holdingAngleAdjusted = holdingAngles[i] - velocityAngle;
					if (holdingAngleAdjusted <= NORMAL_ANGLE && holdingAngleAdjusted >= -NORMAL_ANGLE) {
						forwardAccel = baseForwardAccel * Math.cos(holdingAngleAdjusted);
					}
					else {
						forwardAccel = baseBackwardAccel * Math.cos(holdingAngleAdjusted);
					}
					sidewaysAccel = baseSidewaysAccel * Math.sin(holdingAngleAdjusted);
					if (holdingMinRadius[i]) {
						forwardAccel *= MIN_RADIUS;
						sidewaysAccel *= MIN_RADIUS;
					}
					forwardVelocity += forwardAccel * Math.cos(velocityAngle) - sidewaysAccel * Math.sin(velocityAngle);
					sidewaysVelocity += forwardAccel * Math.sin(velocityAngle) + sidewaysAccel * Math.cos(velocityAngle); //is it -forwardAccel * sin?
					//forwardVelocity += forwardAccel;
					//sidewaysVelocity += sidewaysAccel;
					
					velocityAngle = Math.atan(sidewaysVelocity / forwardVelocity);
	
					dispForward += forwardVelocity;
					dispSideways += sidewaysVelocity;
				}

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
				info[i][7] = holdingAnglesAdjusted[i];
				if (holdingMinRadius[i]) {
					info[i][8] = MIN_RADIUS;
				}
				else if (holdingAnglesAdjusted[i] == NO_ANGLE) {
					info[i][8] = 0;
				}
				else {
					info[i][8] = 1;
				}
			}	
			return info;
		}
	
	public void setHoldingAngles(double angles[]) {
		holdingAngles = angles;
		holdingMinRadius = new boolean[holdingAngles.length];
	}

	public void setHolding(double angles[], boolean radii[]) {
		holdingAngles = angles;
		holdingMinRadius = radii;
	}

	public void setHoldingAngle(double angle) {
		holdingAngle = angle;
		holdingAngles = new double[frames];
		for (int i = 0; i < frames; i++) {
			holdingAngles[i] = angle;
		}
		holdingMinRadius = new boolean[frames];
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
		
	public void setFrames(int n) {
		frames = n;
	}
}

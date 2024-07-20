package vectorCalc;

public class ComplexVector extends SimpleVector {
	
	double[] holdingAngles;
	boolean[] holdingMinRadius;

	public static final double MIN_RADIUS = 0.11;
	
	double forwardVelocity;
	double sidewaysVelocity;
	
	public ComplexVector(Movement movement, boolean rightVector, int frames) {
		super(movement, rightVector, frames);
	}
	
	public ComplexVector(Movement movement, double initialAngle, double[] holdingAngles, boolean rightVector, int frames) {
		super(movement, initialAngle, holdingAngles[0], rightVector, frames);
		this.holdingAngles = holdingAngles;
		this.holdingMinRadius = new boolean[holdingAngles.length];
	}
	
	public ComplexVector(Movement movement, double initialAngle, double[] holdingAngles, boolean rightVector) {
		super(movement, initialAngle, holdingAngles[0], rightVector, holdingAngles.length);
		this.holdingAngles = holdingAngles;
		this.holdingMinRadius = new boolean[holdingAngles.length];
	}
	
	public double calcDispSideways() {
		if (optimalForwardAccel)
			vectorFrames = Math.max(frames - Math.max((int) Math.ceil((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0), 0);
		else
			vectorFrames = frames;
		
		dispSideways = 0;
		sidewaysVelocity = 0;
		for (int i = frames - vectorFrames; i < frames; i++)
			stepSideways(i);
		finalSidewaysVelocity = sidewaysVelocity;
		
		return dispSideways;
	}
	
	public void stepSideways(int i) {
		if (holdingAngles[i] != NO_ANGLE) {
			if (holdingMinRadius[i]) {
				sidewaysVelocity += MIN_RADIUS * baseSidewaysAccel * Math.sin(holdingAngles[i]);
			}
			else {
				sidewaysVelocity += baseSidewaysAccel * Math.sin(holdingAngles[i]);
			}
		}
		if (sidewaysVelocity > forwardVelocityCap)
			sidewaysVelocity = forwardVelocityCap;
		dispSideways += sidewaysVelocity;
	}
	
	public double calcUnoptimalDispForward() {
		dispForward = 0;
		forwardVelocity = initialForwardVelocity;
		for (int i = 0; i < frames; i++)
			stepForward(i);
		finalSidewaysVelocity = forwardVelocity;
		
		return dispForward;
	}
	
	public void stepForward(int i) {
		if (holdingAngles[i] != NO_ANGLE) {
			double accelValue;
			if (holdingAngles[i] <= NORMAL_ANGLE) {
				accelValue = baseForwardAccel;
			}
			else {
				accelValue = baseBackwardAccel;
			}
			if (holdingMinRadius[i]) {
				accelValue *= MIN_RADIUS;
			}
			forwardVelocity += accelValue * Math.cos(holdingAngles[i]);
		}
		if (forwardVelocity > forwardVelocityCap)
			forwardVelocity = forwardVelocityCap;
		dispForward += forwardVelocity;
	}
	
	public void calcDisp() {
		
		if (!optimalForwardAccel && initialForwardVelocity < defaultSpeedCap)
			dispForward = calcUnoptimalDispForward();
		else
			dispForward = calcDispForward();
		dispSideways = calcDispSideways();
		
	}
	
	public double calcFinalAngle() {
		
		if (rightVector)
			finalAngle = initialAngle - Math.atan(sidewaysVelocity / finalForwardVelocity);
		else
			finalAngle = initialAngle + Math.atan(sidewaysVelocity / finalForwardVelocity);
		return finalAngle;
		
	}
	
	//does not currently account for fast turnarounds
	public double calcFinalRotation() {
		double rotation = initialRotation;
		double oldRotation;
		double adjustedHoldingAngle;
		double rotationVelocity = 0;

		int i = 0;
		//when holding forwards
		if (optimalForwardAccel)
			while (i < frames - vectorFrames) {
				//System.out.println("step: " + Math.toDegrees(rotation));
				oldRotation = rotation;
				if (rotation > initialAngle) {
					rotationVelocity -= rotationalAccel;
					if (rotationVelocity < -maxRotationalSpeed)
						rotationVelocity = -rotationalSpeedAfterMax;
				}
				else {
					rotationVelocity += rotationalAccel;
					if (rotationVelocity > maxRotationalSpeed)
						rotationVelocity = rotationalSpeedAfterMax;
				}
						
				rotation += rotationVelocity;
				if ((oldRotation <= initialAngle && initialAngle <= rotation) || (rotation <= initialAngle && initialAngle <= oldRotation)) {
					rotation = initialAngle;
					rotationVelocity = 0;
					i = frames - vectorFrames;
					break;
				}
				i++;
			}
		
		while (i < frames) {
			//System.out.println("step: " + Math.toDegrees(rotation));
			oldRotation = rotation;
			
			if (holdingAngles[i] == NO_ANGLE)
				adjustedHoldingAngle = rotation;
			else if (rightVector)
				adjustedHoldingAngle = initialAngle - holdingAngles[i];
			else
				adjustedHoldingAngle = initialAngle + holdingAngles[i];
			
			if (rotation > adjustedHoldingAngle) {
				if (rotationVelocity > 0)
					rotationVelocity = 0;
				rotationVelocity -= rotationalAccel;
				if (rotationVelocity < -maxRotationalSpeed)
					rotationVelocity = -rotationalSpeedAfterMax;
			}
			else {
				if (rotationVelocity < 0)
					rotationVelocity = 0;
				rotationVelocity += rotationalAccel;
				if (rotationVelocity > maxRotationalSpeed)
					rotationVelocity = rotationalSpeedAfterMax;
			}
			rotation += rotationVelocity;
			
			if ((oldRotation <= adjustedHoldingAngle && adjustedHoldingAngle <= rotation) || (rotation <= adjustedHoldingAngle && adjustedHoldingAngle <= oldRotation)) {
				rotation = adjustedHoldingAngle;
				rotationVelocity = 0;
			}
			i++;
		}
		
		finalRotation = rotation;
		return finalRotation;
	}
	
	public double calcFinalSpeed() {
		finalSpeed = Math.sqrt(Math.pow(finalForwardVelocity, 2) + Math.pow(finalSidewaysVelocity, 2));
		return finalSpeed;
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
			double xVelocity;
			double zVelocity;
			double yVelocity = movement.initialVerticalSpeed;
			int nonVectorFrames = frames - vectorFrames;
			
			double[] holdingAnglesAdjusted = new double[frames];
			for (int i = 0; i < frames; i++)
				if (i < nonVectorFrames)
					holdingAnglesAdjusted[i] = initialAngle;
				else if (holdingAngles[i] == NO_ANGLE)
					holdingAnglesAdjusted[i] = NO_ANGLE;
				else if (rightVector)
					holdingAnglesAdjusted[i] = initialAngle - holdingAngles[i];
				else
					holdingAnglesAdjusted[i] = initialAngle + holdingAngles[i];
			
			double[][] info = new double[frames][8];
			for (int i = 0; i < frames; i++) {	
				if (forwardVelocity < forwardVelocityCap) {
					if (i >= nonVectorFrames) {
						if (holdingAngles[i] != NO_ANGLE) {
							double accelValue;
							if (holdingAngles[i] <= NORMAL_ANGLE) {
								accelValue = baseForwardAccel;
							}
							else {
								accelValue = baseBackwardAccel;
							}
							if (holdingMinRadius[i]) {
								accelValue *= MIN_RADIUS;
							}
							forwardVelocity += accelValue * Math.cos(holdingAngles[i]);
						}
					}
					else
						forwardVelocity += baseForwardAccel;
					if (forwardVelocity > forwardVelocityCap)
						forwardVelocity = forwardVelocityCap;
				}
				if (sidewaysVelocity < forwardVelocityCap && i >= nonVectorFrames && holdingAngles[i] != NO_ANGLE) {
					if (holdingMinRadius[i]) {
						sidewaysVelocity += MIN_RADIUS * baseSidewaysAccel * Math.sin(holdingAngles[i]);
					}
					else {
						sidewaysVelocity += baseSidewaysAccel * Math.sin(holdingAngles[i]);
					}
					if (sidewaysVelocity > forwardVelocityCap)
						sidewaysVelocity = forwardVelocityCap;
				}
				xVelocity = forwardVelocity * cosInitialAngle + sidewaysVelocity * cosNormalAngle;
				zVelocity = forwardVelocity * sinInitialAngle + sidewaysVelocity * sinNormalAngle;
				if (i >= movement.framesAtMaxVerticalSpeed + movement.frameOffset) {
					yVelocity -= gravity;
					if (yVelocity < movement.fallSpeedCap)
						yVelocity = movement.fallSpeedCap;
				}
				dispX += xVelocity;
				if (i >= movement.frameOffset) {
					dispY += yVelocity;
					info[i][4] = yVelocity;
				}
				else
					info[i][4] = 0;
				dispZ += zVelocity;
				info[i][0] = dispX;
				info[i][1] = dispY;
				info[i][2] = dispZ;
				info[i][3] = xVelocity;
				info[i][5] = zVelocity;
				info[i][6] = Math.sqrt(Math.pow(xVelocity, 2) + Math.pow(zVelocity, 2));
				info[i][7] = holdingAnglesAdjusted[i];
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
}

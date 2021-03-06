package vectorCalc;

public class SimpleVector extends SimpleMotion {
	
	boolean optimalForwardAccel = true;
	
	//double initialAngle;
	double normalAngle;
	
	//double initialForwardVelocity;
	double baseSidewaysAccel;
	double sidewaysAccel;
	
	//int frames;
	
	double dispForward;
	double dispSideways;
	//double dispX;
	//double dispZ;
	
	double finalSidewaysVelocity;
	
	boolean rightVector;
	
	int vectorFrames;
	
	public SimpleVector(Movement movement, boolean rightVector, int frames) {
		
		super(movement, frames);
		this.rightVector = rightVector;
		this.baseSidewaysAccel = movement.vectorAccel;
		this.holdingAngle = NORMAL_ANGLE;
		vectorFrames = frames - Math.max((int) Math.ceil((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0);
	}
	
	
	public SimpleVector(Movement movement, double initialAngle, double holdingAngle, boolean rightVector, int frames) {
		
		super(movement, initialAngle, frames);
		//this.initialAngle = initialAngle;
		//this.initialForwardVelocity = movement.initialHorizontalSpeed;
		this.baseSidewaysAccel = movement.vectorAccel;
		//this.frames = frames;
		this.rightVector = rightVector;
		
		if (rightVector)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
		
		this.holdingAngle = holdingAngle;
		vectorFrames = frames - Math.max((int) Math.ceil((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0);
	}

	public double calcDispSideways() {
		
		if (holdingAngle == NORMAL_ANGLE)
			sidewaysAccel = baseSidewaysAccel;
		else if (holdingAngle == NO_ANGLE) {
			sidewaysAccel = 0;
			return 0;
		}
		else
			sidewaysAccel = baseSidewaysAccel * Math.sin(holdingAngle); //holding angle is the angle away from the initial angle you are holding
		
		if (optimalForwardAccel)
			vectorFrames = Math.max(frames - Math.max((int) Math.ceil((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0), 0);
		else
			vectorFrames = frames;
		
		int framesToMaxSidewaysSpeed = (int) (forwardVelocityCap / sidewaysAccel);
		if (vectorFrames >= 0) {
			if (vectorFrames <= (int) (forwardVelocityCap / sidewaysAccel))
				return sidewaysAccel / 2 * vectorFrames * (vectorFrames + 1);
			else
				return sidewaysAccel * (framesToMaxSidewaysSpeed + 1) / 2 * framesToMaxSidewaysSpeed + forwardVelocityCap * (vectorFrames - framesToMaxSidewaysSpeed);
		}
		else
			return 0;
	}
	
	public void calcDisp() {
		
		if (!optimalForwardAccel)
			forwardAccel = baseForwardAccel * Math.abs(Math.cos(holdingAngle));
		
		dispForward = calcDispForward();
		dispSideways = calcDispSideways();
		
		finalSidewaysVelocity = Math.min(sidewaysAccel * vectorFrames, finalForwardVelocity);
		
	}
	
	public void calcDispCoords() {
		
		dispX = dispForward * Math.cos(initialAngle) + dispSideways * Math.cos(normalAngle);
		dispZ = dispForward * Math.sin(initialAngle) + dispSideways * Math.sin(normalAngle);
		
	}
	
	//requires calcDisp() to be called first
	public double calcFinalAngle() {		
		if (rightVector)
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
	
	//does not currently account for fast turnarounds, returns -1 if no frames to rotation can be calculated
	public double calcFramesToRotation(double targetRotation) {
		double rotation = initialRotation;
		double oldRotation;
		double rotationVelocity = 0;
		
		int i = 0;
		//when holding forwards
		if (optimalForwardAccel)
			while (i < frames - vectorFrames) {
				oldRotation = rotation;
				if (rotation > initialAngle) {
					rotationVelocity -= ROTATIONAL_ACCEL;
					if (rotationVelocity < -MAX_ROTATIONAL_SPEED)
						rotationVelocity = -ROTATIONAL_SPEED_AFTER_MAX;
				}
				else {
					rotationVelocity += ROTATIONAL_ACCEL;
					if (rotationVelocity > MAX_ROTATIONAL_SPEED)
						rotationVelocity = ROTATIONAL_SPEED_AFTER_MAX;
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
			
			if (holdingAngle != NO_ANGLE)
				while (i < frames) {
					//System.out.println("step: " + Math.toDegrees(rotation));
					oldRotation = rotation;
					
					if (rotation > targetRotation) {
						if (rotationVelocity > 0)
							rotationVelocity = 0;
						rotationVelocity -= ROTATIONAL_ACCEL;
						if (rotationVelocity < -MAX_ROTATIONAL_SPEED)
							rotationVelocity = -ROTATIONAL_SPEED_AFTER_MAX;
					}
					else {
						if (rotationVelocity < 0)
							rotationVelocity = 0;
						rotationVelocity += ROTATIONAL_ACCEL;
						if (rotationVelocity > MAX_ROTATIONAL_SPEED)
							rotationVelocity = ROTATIONAL_SPEED_AFTER_MAX;
					}
					rotation += rotationVelocity;
					
					if ((oldRotation <= targetRotation && targetRotation <= rotation) || (rotation <= targetRotation && targetRotation <= oldRotation)) {
						if (rotation == targetRotation) {
							finalRotation = rotation;
							return i + 1;
						}
						else {
							rotation = targetRotation;
							rotationVelocity = 0;
							break;
						}
					}
					i++;
				}
		
		finalRotation = rotation;	
		if (rotation == targetRotation)	
			return i + .5; //useful for VectorMaximizer class to know whether the rotation is reached exactly on the frame
		else
			return -1;
	}
	
	public double calcFinalRotation() {
		double adjustedHoldingAngle;
		if (rightVector)
			adjustedHoldingAngle = initialAngle - holdingAngle;
		else
			adjustedHoldingAngle = initialAngle + holdingAngle;
		calcFramesToRotation(adjustedHoldingAngle);
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
		double yVelocity = movement.initialVerticalSpeed;
		double zVelocity;

		int nonVectorFrames = frames - vectorFrames;
		
		double holdingAngleAdjusted;
		if (holdingAngle == NO_ANGLE)
			holdingAngleAdjusted = NO_ANGLE;
		else if (rightVector)
			holdingAngleAdjusted = initialAngle - holdingAngle;
		else
			holdingAngleAdjusted = initialAngle + holdingAngle;
		
		double[][] info = new double[frames][8];
		for (int i = 0; i < frames; i++) {
			if (forwardVelocity < forwardVelocityCap) {
				forwardVelocity += forwardAccel;
				if (forwardVelocity > forwardVelocityCap)
					forwardVelocity = forwardVelocityCap;
			}
			if (sidewaysVelocity < forwardVelocityCap && i >= nonVectorFrames) {
				sidewaysVelocity += sidewaysAccel;
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
			if (i < nonVectorFrames)
				info[i][7] = initialAngle;
			else
				info[i][7] = holdingAngleAdjusted;
		}	
		return info;
	}
	
	public void setInitialAngle(double angle) {
	
		initialAngle = angle;
		
		if (rightVector)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;

	}
	
	public void adjustInitialAngle(double angle) {
		
		initialAngle += angle;
		
		if (rightVector)
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
	
	public void setOptimalForwardAccel(boolean b) {
		optimalForwardAccel = b;
	}

}


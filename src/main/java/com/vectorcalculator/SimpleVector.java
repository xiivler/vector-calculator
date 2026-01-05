package com.vectorcalculator;

//calculates the behavior of vectorable movement if the joystick angle is consistent
public class SimpleVector extends SimpleMotion {
	
	Properties p = Properties.p;
	
	boolean optimalForwardAccel = true; //if true, the holding angle will be overridden to be 0 to until full speed is reached from accelerating forward
	
	double normalAngle;
	
	double baseSidewaysAccel;
	double sidewaysAccel;
	
	double dispForward;
	double dispSideways;
	
	double finalSidewaysVelocity;

	double sidewaysVelocityCap;
	
	boolean rightVector;
	
	int vectorFrames;
	
	public SimpleVector(Movement movement, boolean rightVector, int frames) {
		
		super(movement, frames);
		this.rightVector = rightVector;

		if (rightVector)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
			
		this.baseSidewaysAccel = movement.vectorAccel;
		this.holdingAngle = NORMAL_ANGLE;
		//to account for the fact that sometimes the cap throw doesn't quite rotate right
		if (movement.movementType.equals("Dive Cap Bounce")) {
			this.holdingAngle -= Math.toRadians(.5);
		}
		vectorFrames = frames - Math.max((int) Math.ceil((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0);
		if (movement.movementType.equals("Sideflip"))
			sidewaysVelocityCap = Double.MAX_VALUE;
		else
			sidewaysVelocityCap = forwardVelocityCap;
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
		if (vectorFrames < 0) {
			vectorFrames = 0;
		}
		sidewaysVelocityCap = forwardVelocityCap;
	}

	public double calcDispSideways() {
		
		if (frames == 0) {
			sidewaysAccel = 0;
			return 0;
		}

		if (holdingAngle == NORMAL_ANGLE)
			sidewaysAccel = baseSidewaysAccel;
		else if (holdingAngle == NO_ANGLE) {
			sidewaysAccel = 0;
			return 0;
		}
		else
			sidewaysAccel = baseSidewaysAccel * Math.sin(holdingAngle); //holding angle is the angle away from the initial angle you are holding
		
		if (optimalForwardAccel && !movement.movementType.equals("Sideflip"))
			vectorFrames = Math.max(frames - Math.max((int) Math.ceil((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0), 0);
		else
			vectorFrames = frames;
		
		int framesToMaxSidewaysSpeed = (int) (sidewaysVelocityCap / sidewaysAccel);
		if (vectorFrames >= 0) {
			if (vectorFrames <= (int) (sidewaysVelocityCap / sidewaysAccel) || movement.movementType.equals("Sideflip"))
				return sidewaysAccel / 2 * vectorFrames * (vectorFrames + 1);
			else
				return sidewaysAccel * (framesToMaxSidewaysSpeed + 1) / 2 * framesToMaxSidewaysSpeed + sidewaysVelocityCap * (vectorFrames - framesToMaxSidewaysSpeed);
		}
		else
			return 0;
	}
	
	public void calcDisp() {
		
		if (!optimalForwardAccel)
			forwardAccel = baseForwardAccel * Math.abs(Math.cos(holdingAngle));
		
		dispForward = calcDispForward();
		dispSideways = calcDispSideways();
		
		if (movement.movementType.equals("Sideflip")) {
			finalSidewaysVelocity = sidewaysAccel * frames;
		}
		else {
			finalSidewaysVelocity = Math.min(sidewaysAccel * vectorFrames, Math.min(finalForwardVelocity, sidewaysVelocityCap));
		}
	}
	
	public void calcDispCoords() {
		
		dispZ = dispForward * Math.cos(initialAngle) + dispSideways * Math.cos(normalAngle);
		dispX = dispForward * Math.sin(initialAngle) + dispSideways * Math.sin(normalAngle);
		
	}
	
	//requires calcDisp() to be called first
	public double calcFinalAngle() {		
		if (rightVector)
			finalAngle = initialAngle - Math.atan2(finalSidewaysVelocity, finalForwardVelocity);
		else
			finalAngle = initialAngle + Math.atan2(finalSidewaysVelocity, finalForwardVelocity);
		return finalAngle;
		
	}
	
	//requires calcDisp() to be called first
	public double calcFinalSpeed() {
		finalSpeed = Math.sqrt(Math.pow(finalForwardVelocity, 2) + Math.pow(finalSidewaysVelocity, 2));
		return finalSpeed;
	}
	
	public double[] calcRelativeRotations() { //calculate rotations relative to the initial velocity angle; if initialRotation is negative, that means it's to the left of the initial velocity if we're vectoring right or the opposite if we're vectoring left
		double relativeInitialRotation;
		if (rightVector) {
			relativeInitialRotation = initialAngle - initialRotation;
		}
		else {
			relativeInitialRotation = initialRotation - initialAngle;
		}
		double rotation = relativeInitialRotation;
		double[] rotations = new double[frames];
		double rotationVelocity = 0;

		int i = 0;
		//when holding forwards, rotate until facing the forward direction
		if (optimalForwardAccel) {
			while (i < frames - vectorFrames) {
				if (rotation > 0) {
					rotationVelocity -= rotationalAccel;
					if (rotationVelocity < -maxRotationalSpeed)
						rotationVelocity = -rotationalSpeedAfterMax;
				}
				else if (rotation < 0) {
					rotationVelocity += rotationalAccel;
					if (rotationVelocity > maxRotationalSpeed)
						rotationVelocity = rotationalSpeedAfterMax;
				}
						
				rotation += rotationVelocity;

				if ((rotations[i - 1] <= 0 && 0 <= rotation) || (rotation <= 0 && 0 <= rotations[i - 1])) {
					rotation = 0;
					rotationVelocity = 0;
				}
				rotations[i] = rotation;
				i++;
			}
		}
		
		//now keep rotating until we reach the angle that we're holding
		if (holdingAngle != NO_ANGLE) {
			while (i < frames) {
				if (rotationVelocity < 0) {
					rotationVelocity = 0;
				}
				rotationVelocity += rotationalAccel;
				if (rotationVelocity > maxRotationalSpeed) {
					rotationVelocity = rotationalSpeedAfterMax;
				}
				rotation += rotationVelocity;

				if (rotation > holdingAngle) {
					rotation = holdingAngle;
				}
				rotations[i] = rotation;
				i++;
			}
		}
		else {
			while (i < frames) {
				rotations[i] = rotations[i - 1];
			}
		}

		return rotations;
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
			
			if (holdingAngle != NO_ANGLE)
				while (i < frames) {
					//Debug.println("step: " + Math.toDegrees(rotation));
					oldRotation = rotation;
					
					if (rotation > targetRotation) {
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
	//column 0-2: (X, Y, Z), column 3-5: (X-vel, Y-vel, Z-vel), column 6: horizontal speed, column 7: holding angle, column 8: holding radius
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
		double sidewaysVelocity = 0;
		double zVelocity;
		double yVelocity = movement.initialVerticalSpeed;
		double xVelocity;

		int nonVectorFrames = frames - vectorFrames;
		
		double holdingAngleAdjusted;
		if (holdingAngle == NO_ANGLE)
			holdingAngleAdjusted = NO_ANGLE;
		else if (rightVector)
			holdingAngleAdjusted = initialAngle - holdingAngle;
		else
			holdingAngleAdjusted = initialAngle + holdingAngle;
		
		double[][] info = new double[frames][9];
		for (int i = 0; i < frames; i++) {
			if (forwardVelocity < forwardVelocityCap) {
				forwardVelocity += forwardAccel;
				if (forwardVelocity > forwardVelocityCap)
					forwardVelocity = forwardVelocityCap;
			}
			if (i == frames - 1)
				forwardVelocity -= yank;
			if (sidewaysVelocity < sidewaysVelocityCap && i >= nonVectorFrames) {
				sidewaysVelocity += sidewaysAccel;
				if (sidewaysVelocity > sidewaysVelocityCap)
					sidewaysVelocity = sidewaysVelocityCap;
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
			if (i < nonVectorFrames) {
				info[i][7] = initialAngle;
			}
			else if (yank > 0 && i == frames - 1) {
				info[i][7] = initialAngle + Math.PI;
			}
			else {
				info[i][7] = holdingAngleAdjusted;
			}
			if (info[i][7] == NO_ANGLE) {
				info[i][8] = 0;
			}
			else if (yank > 0 && i == frames - 1) {
				info[i][8] = yank / baseBackwardAccel;
			}
			else {
				info[i][8] = 1;
			}
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


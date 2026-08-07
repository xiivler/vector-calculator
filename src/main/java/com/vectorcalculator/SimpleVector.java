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
			
		this.baseSidewaysAccel = movement.sidewaysAccel;
		this.holdingAngle = NORMAL_ANGLE;
		//to account for the fact that sometimes the cap throw doesn't quite rotate right, and only loses thousandths of a unit
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
		this.baseSidewaysAccel = movement.sidewaysAccel;
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
	
	//TODO update this
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
		double angularVelocity = 0;

		int i = 0;
		//when holding forwards, rotate until facing the forward direction
		if (optimalForwardAccel) {
			while (i < frames - vectorFrames) {
				if (rotation > 0) {
					angularVelocity -= angularAccel;
					if (angularVelocity < -maxAngVel)
						angularVelocity = -rotationalSpeedAfterMax;
				}
				else if (rotation < 0) {
					angularVelocity += angularAccel;
					if (angularVelocity > maxAngVel)
						angularVelocity = rotationalSpeedAfterMax;
				}
						
				rotation += angularVelocity;

				if ((rotations[i - 1] <= 0 && 0 <= rotation) || (rotation <= 0 && 0 <= rotations[i - 1])) {
					rotation = 0;
					angularVelocity = 0;
				}
				rotations[i] = rotation;
				i++;
			}
		}
		
		//now keep rotating until we reach the angle that we're holding
		if (holdingAngle != NO_ANGLE) {
			while (i < frames) {
				if (angularVelocity < 0) {
					angularVelocity = 0;
				}
				angularVelocity += angularAccel;
				if (angularVelocity > maxAngVel) {
					angularVelocity = rotationalSpeedAfterMax;
				}
				rotation += angularVelocity;

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

	public static final double ROTATION_ERROR = 0.001;

	//calculates one frame of horizontal rotation based on the previous and the current holding angle
	//does not account for holding radii that are less than 1 for non-fast turnaround frames
	public RotationStep calcRotationStep(double holdingAngle, RotationStep prevRotationStep) {
		double prevRotation = prevRotationStep.rotation;
		double prevAngVel = prevRotationStep.angVel;
		double prevHoldingAngle = prevRotationStep.holdingAngle;
		RotationDirection prevRotationDirection = prevRotationStep.rotationDirection;

		double angVel = prevAngVel;
		double rotation = prevRotation;
		RotationDirection rotationDirection = RotationDirection.NONE;

		if (holdingAngle != SimpleMotion.NO_ANGLE) {
			double holdingAngleAdjusted = initialAngle + (rightVector ? -holdingAngle : holdingAngle);
			double prevHoldingAngleAdjusted = initialAngle + (rightVector ? -prevHoldingAngle : prevHoldingAngle);
			System.out.println("Holding " + Math.toDegrees(holdingAngleAdjusted));
			System.out.println("Prev Rotation " + Math.toDegrees(prevRotation));
			if (prevHoldingAngle == SimpleMotion.NO_ANGLE) //previous holding angle is Mario's previous rotation if joystick was neutral (or in the first step)
				prevHoldingAngleAdjusted = prevRotation;
			while (holdingAngleAdjusted < prevRotation - Math.PI)
				holdingAngleAdjusted += Math.PI * 2;
			while (holdingAngleAdjusted > prevRotation + Math.PI)
				holdingAngleAdjusted -= Math.PI * 2;
			while (prevHoldingAngleAdjusted < holdingAngleAdjusted - Math.PI)
				prevHoldingAngleAdjusted += Math.PI * 2;
			while (prevHoldingAngleAdjusted > holdingAngleAdjusted + Math.PI)
				prevHoldingAngleAdjusted -= Math.PI * 2;
			double joystickRotationDelta = holdingAngleAdjusted - prevHoldingAngleAdjusted;
			rotationDirection = (holdingAngleAdjusted < prevRotation ? RotationDirection.CW : RotationDirection.CCW); //rotate CW if we are holding to the right, CCW if we are holding to the left
			//we can continue rotating CW if we keep shifting the joystick CW, or vice versa for CCW
			if (prevRotationDirection == RotationDirection.CW && joystickRotationDelta <= -Math.toRadians(3))
				rotationDirection = RotationDirection.CW;
			else if (prevRotationDirection == RotationDirection.CCW && joystickRotationDelta >= Math.toRadians(3))
				rotationDirection = RotationDirection.CCW;
			double holdingDiff = Math.abs(holdingAngleAdjusted - prevRotation);
			System.out.println("Holding Diff: " + Math.toDegrees(holdingDiff));
			if (holdingDiff >= Math.toRadians(135)) { //fast turnaround
				angVel = Math.toRadians(25);
			}
			else {
				System.out.println("Joystick Delta: " + Math.toDegrees(joystickRotationDelta));
				System.out.println("Rotation Direction: " + rotationDirection);
				if (joystickRotationDelta > 0 && prevRotationDirection == RotationDirection.CW) //apply counterrotation
					angVel -= joystickRotationDelta;
				else if (joystickRotationDelta < 0 && prevRotationDirection == RotationDirection.CCW)
					angVel += joystickRotationDelta;
				if (angVel < 0)
					angVel = 0;
				System.out.println("Ang Vel After Counterrotation: " + Math.toDegrees(angVel));

				if (holdingDiff < Math.toRadians(1)) { //slow down because angle is close
					angVel -= Math.toRadians(0.6);
					if (angVel < 0)
						angVel = 0;
				}
				else if ((!Movement.isMidairCapThrow(movement.movementType) && angVel >= maxAngVel - ROTATION_ERROR) || angVel > maxAngVel) { //cap throws don't quite reach max ang vel, so they don't get this slowdown
					angVel -= Math.toRadians(2.5);
				}
				else {
					angVel = Math.min(angVel + angularAccel, maxAngVel);
				}
			}
			System.out.println("Ang Vel: " + Math.toDegrees(angVel));

			rotation = prevRotation + (rotationDirection == RotationDirection.CW ? -1 : 1) * angVel; //apply angular velocity CW or CCW
			while (rotation < prevRotation - Math.PI)
				rotation += Math.PI * 2;
			while (rotation > prevRotation + Math.PI)
				rotation -= Math.PI * 2;
			while (holdingAngleAdjusted < prevRotation - Math.PI)
				holdingAngleAdjusted += Math.PI * 2;
			while (holdingAngleAdjusted > prevRotation + Math.PI)
				holdingAngleAdjusted -= Math.PI * 2;

			//System.out.println("Tentative Rotation: " + Math.toDegrees(rotation));
			//System.out.println("Prev Rotation: " + Math.toDegrees(prevRotation));
			//System.out.println("Holding Angle Adjusted: " + Math.toDegrees(holdingAngleAdjusted));
			if ((prevRotation <= holdingAngleAdjusted && holdingAngleAdjusted <= rotation) || (rotation <= holdingAngleAdjusted && holdingAngleAdjusted <= prevRotation)) { //stop rotating because the holding angle was achieved; note that this does not affect the angular velocity
				rotation = holdingAngleAdjusted;
				if (prevRotation == rotation)
					rotationDirection = RotationDirection.NONE;
			}
		}
		else { //if holding no angle, angular velocity still decreases, but just isn't applied
			rotationDirection = RotationDirection.NONE;
			angVel = prevAngVel - Math.toRadians(0.6);
			if (angVel < 0)
				angVel = 0;
		}

		System.out.println();
		
		return new RotationStep(rotation, angVel, holdingAngle, rotationDirection);
	}

	//does not currently account for fast turnarounds, returns -1 if no frames to rotation can be calculated
	public double calcFramesToRotation(double targetRotation) {
		//double rotation = initialRotation;
		//double oldRotation;
		//double angularVelocity = 0;

		//when holding forwards
		//int i = 0;
		//if (optimalForwardAccel)
			//while (i < frames - vectorFrames) {
				// oldRotation = rotation;
				// if (rotation > initialAngle) {
				// 	angularVelocity -= angularAccel;
				// 	if (angularVelocity < -maxAngVel)
				// 		angularVelocity = -rotationalSpeedAfterMax;
				// }
				// else {
				// 	angularVelocity += angularAccel;
				// 	if (angularVelocity > maxAngVel)
				// 		angularVelocity = rotationalSpeedAfterMax;
				// }
						
				// rotation += angularVelocity;
				// if ((oldRotation <= initialAngle && initialAngle <= rotation) || (rotation <= initialAngle && initialAngle <= oldRotation)) {
				// 	rotation = initialAngle;
				// 	angularVelocity = 0;
				// 	i = frames - vectorFrames;
				// 	break;
				// }

		//System.out.println("Initiating CFTR");

		double rotation = initialRotation;
		RotationStep rotationStep = new RotationStep(initialRotation, 0, SimpleMotion.NO_ANGLE, RotationDirection.NONE);
		
		for (int i = 0; i < frames; i++) {
			//System.out.println("CFTR Step " + i);
			double actualHoldingAngle = (optimalForwardAccel && i < frames - vectorFrames) ? 0 : holdingAngle;
			RotationStep prevRotationStep = rotationStep;
			rotationStep = calcRotationStep(actualHoldingAngle, prevRotationStep);

			rotation = rotationStep.rotation;
			double prevRotation = prevRotationStep.rotation;
			while (rotation < targetRotation - Math.PI)
				rotation += Math.PI * 2;
			while (rotation > targetRotation + Math.PI)
				rotation -= Math.PI * 2;
			while (prevRotation < targetRotation - Math.PI)
				rotation += Math.PI * 2;
			while (prevRotation > targetRotation + Math.PI)
				rotation -= Math.PI * 2;

			if ((prevRotation <= targetRotation && targetRotation <= rotation) || (rotation <= targetRotation && targetRotation <= prevRotation)) {
				finalRotation = rotation;
				if (rotation == targetRotation)
					return i + 1;
				else
					return i + .5;
			}
		}

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
		RotationStep rotationStep = new RotationStep(initialRotation, 0, SimpleMotion.NO_ANGLE, RotationDirection.NONE);

		int nonVectorFrames = frames - vectorFrames;
		
		double holdingAngleAdjusted;
		if (holdingAngle == NO_ANGLE)
			holdingAngleAdjusted = NO_ANGLE;
		else if (rightVector)
			holdingAngleAdjusted = initialAngle - holdingAngle;
		else
			holdingAngleAdjusted = initialAngle + holdingAngle;
		
		double[][] info = new double[frames][10];
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
			double actualHoldingAngle = (optimalForwardAccel && i < frames - vectorFrames) ? 0 : holdingAngle;
			RotationStep prevRotationStep = rotationStep;
			System.out.println("Frame " + (i + 1));
			rotationStep = calcRotationStep(actualHoldingAngle, prevRotationStep);
			info[i][9] = rotationStep.rotation;
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

	public void setInitialForwardVelocity(double initialForwardVelocity) {
		this.initialForwardVelocity = initialForwardVelocity;
	}

	protected class RotationStep {
		double rotation;
		double angVel;
		double holdingAngle;
		RotationDirection rotationDirection;

		public RotationStep(double rotation, double angVel, double holdingAngle, RotationDirection rotationDirection) {
			this.rotation = rotation;
			this.angVel = angVel;
			this.holdingAngle = holdingAngle;
			this.rotationDirection = rotationDirection;
		}
	}

	protected enum RotationDirection {
		CW, CCW, NONE
	}
}

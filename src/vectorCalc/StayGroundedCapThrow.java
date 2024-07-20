package vectorCalc;

//for a rc into spinpound
public class StayGroundedCapThrow extends GroundedCapThrow {
	
	double initialHoldingAngle;

	boolean debug = true;

	boolean startStraight = false; //makes it so your velocity starts off in the direciton of 90, gaining around .05 of a unit

	double rotationAngles[];

	public static final double DECAY_MULTIPLIER = .95;

	public StayGroundedCapThrow(Movement movement, boolean goRight) {
		super(movement, goRight);
	}
	
	//for this, finalAngleDiff is 
	public StayGroundedCapThrow(Movement movement, double initialAngle, double initialHoldingAngle, double finalAngleDiff, boolean goRight) {
		super(movement, initialAngle, finalAngleDiff, goRight);
		this.initialHoldingAngle = initialHoldingAngle;
	}
	
	//does not account for starting with unoptimal forward currentVelocity and then accelerating
	public void calcDisp() {
		this.velocityAngles = new double[frames];
		this.holdingAngles = new double[frames];
		this.rotationAngles = new double[frames];

		holdingAngles[0] = initialHoldingAngle;

		dispForward = 0;
		dispSideways = 0;

		double velocityForward = 0;
		double velocitySideways = 0;
		
		double angleChange = 0;
		double rotationalSpeed = 0;
		turningFrames = 0;

		double friction = 0;
		int framesAtMaxRotationalSpeed = 0;

		//currentVelocity parallel to cap throw angle
		double parallelVelocity = initialForwardVelocity * Math.cos(initialHoldingAngle);
		double parallelVelocityDirection = initialHoldingAngle;
		double perpendicularVelocity = initialForwardVelocity * Math.sin(initialHoldingAngle);
		double perpendicularVelocityDirection = initialHoldingAngle - Math.PI / 2;

		parallelVelocity *= DECAY_MULTIPLIER;

		double rotation = Math.min(initialHoldingAngle, CT_ROTATIONAL_VELOCITY); //rotate up to 5 degrees
		
		if (startStraight) {
			velocityForward = perpendicularVelocity * Math.cos(perpendicularVelocityDirection) + parallelVelocity * Math.cos(parallelVelocityDirection);
			velocitySideways = perpendicularVelocity * Math.sin(perpendicularVelocityDirection) + parallelVelocity * Math.sin(parallelVelocityDirection);
			rotation = -Math.atan2(velocitySideways, velocityForward);
		}

		parallelVelocityDirection += rotation;
		perpendicularVelocityDirection += rotation;
		rotationAngles[0] = initialHoldingAngle + rotation;

		velocityForward = perpendicularVelocity * Math.cos(perpendicularVelocityDirection) + parallelVelocity * Math.cos(parallelVelocityDirection);
		velocitySideways = perpendicularVelocity * Math.sin(perpendicularVelocityDirection) + parallelVelocity * Math.sin(parallelVelocityDirection);
		velocityAngles[0] = Math.atan2(velocitySideways, velocityForward);
		double currentVelocity = Math.sqrt(velocityForward * velocityForward + velocitySideways * velocitySideways);

		dispForward += velocityForward;
		dispSideways += velocitySideways;
		
		if (debug) {
			System.out.println("Disp 0: " + dispForward + ", " + dispSideways);
			System.out.println("Vel 0: (" + velocityForward + ", " + velocitySideways + ") (" + currentVelocity + "; " + Math.toDegrees(Math.PI / 2 - velocityAngles[0]) + ")");
			System.out.println("Rotation: " + Math.toDegrees(Math.PI / 2 - rotationAngles[0]));
			System.out.println();
		}

		boolean holdForward = true;
		int i = 1;
		
		while (holdForward) {
			parallelVelocity *= DECAY_MULTIPLIER;
			velocityForward = perpendicularVelocity * Math.cos(perpendicularVelocityDirection) + parallelVelocity * Math.cos(parallelVelocityDirection);
			velocitySideways = perpendicularVelocity * Math.sin(perpendicularVelocityDirection) + parallelVelocity * Math.sin(parallelVelocityDirection);
			double neutralVelocityAngle = Math.atan2(velocitySideways, velocityForward);
			currentVelocity = Math.sqrt(velocityForward * velocityForward + velocitySideways * velocitySideways);
			//System.out.println("NVA: " + Math.toDegrees(neutralVelocityAngle));

			if (neutralVelocityAngle != 0) {
				perpendicularVelocityDirection -= neutralVelocityAngle;
				parallelVelocityDirection -= neutralVelocityAngle;
				velocityForward = perpendicularVelocity * Math.cos(perpendicularVelocityDirection) + parallelVelocity * Math.cos(parallelVelocityDirection);
				velocitySideways = perpendicularVelocity * Math.sin(perpendicularVelocityDirection) + parallelVelocity * Math.sin(parallelVelocityDirection);
				holdingAngles[i] = 0;
				rotationAngles[i] = rotationAngles[i - 1] - neutralVelocityAngle;
			}

			velocityAngles[i] = Math.atan2(velocitySideways, velocityForward);
			dispForward += velocityForward;
			dispSideways += velocitySideways;

			if (debug) {
				System.out.println("Step: " + i);
				System.out.println("Disp: " + dispForward + ", " + dispSideways);
				System.out.println("Vel: (" + velocityForward + ", " + velocitySideways + ") (" + currentVelocity + "; " + Math.toDegrees(Math.PI / 2 - velocityAngles[i]) + ")");
				System.out.println("Hold: " + Math.toDegrees(Math.PI / 2 - holdingAngles[i]));
				System.out.println("Rotation: " + Math.toDegrees(Math.PI / 2 - rotationAngles[i]));
			}

			i++;
			
			int remainingFrames = PRE_HOOK_FRAMES - i;
			double maxRotation = CT_ROTATIONAL_VELOCITY * remainingFrames;
			double minFinalAngleDiff = rotationAngles[i - 1] - maxRotation; //go to the side a bunch and then hook
			if (debug) {
				System.out.println("Max Rotation: " + Math.toDegrees(maxRotation));
				System.out.println("Min Final Angle Diff: " + Math.toDegrees(minFinalAngleDiff));
				System.out.println();
			}
			if (finalAngleDiff < minFinalAngleDiff) {
				//we did too many frames and need to revert one
				i--;
				parallelVelocity /= DECAY_MULTIPLIER;
				perpendicularVelocityDirection += neutralVelocityAngle;
				parallelVelocityDirection += neutralVelocityAngle;
				dispForward -= velocityForward;
				dispSideways -= velocitySideways;
				holdForward = false;
			}

			if (i > 14) {
				holdForward = false;
			}
		}

		int remainingFrames = PRE_HOOK_FRAMES - i;
		double currentFinalAngleDiff = rotationAngles[i - 1];
		double neededRotation = currentFinalAngleDiff - finalAngleDiff;
		double firstRotation = neededRotation - CT_ROTATIONAL_VELOCITY * (remainingFrames - 1);

		if (debug) {
			System.out.println("First Rotation: " + Math.toDegrees(firstRotation));
		}

		boolean isFirstTurnFrame = true;

		for (; i < PRE_HOOK_FRAMES; i++) {
			parallelVelocity *= DECAY_MULTIPLIER;
			velocityForward = perpendicularVelocity * Math.cos(perpendicularVelocityDirection) + parallelVelocity * Math.cos(parallelVelocityDirection);
			velocitySideways = perpendicularVelocity * Math.sin(perpendicularVelocityDirection) + parallelVelocity * Math.sin(parallelVelocityDirection);
			//double neutralVelocityAngle = Math.atan2(velocitySideways, velocityForward);
			currentVelocity = Math.sqrt(velocityForward * velocityForward + velocitySideways * velocitySideways);
			//System.out.println("NVA: " + Math.toDegrees(neutralVelocityAngle));

			double rotationalVelocity;
			if (isFirstTurnFrame) {
				rotationalVelocity = -firstRotation;
			}
			else {
				rotationalVelocity = -CT_ROTATIONAL_VELOCITY;
			}
			perpendicularVelocityDirection += rotationalVelocity;
			parallelVelocityDirection += rotationalVelocity;
			velocityForward = perpendicularVelocity * Math.cos(perpendicularVelocityDirection) + parallelVelocity * Math.cos(parallelVelocityDirection);
			velocitySideways = perpendicularVelocity * Math.sin(perpendicularVelocityDirection) + parallelVelocity * Math.sin(parallelVelocityDirection);
			rotationAngles[i] = rotationAngles[i - 1] + rotationalVelocity;

			velocityAngles[i] = Math.atan2(velocitySideways, velocityForward);
			if (isFirstTurnFrame) {
				holdingAngles[i] = velocityAngles[i];
			}
			else {
				holdingAngles[i] = -NORMAL_ANGLE;
			}
			dispForward += velocityForward;
			dispSideways += velocitySideways;

			isFirstTurnFrame = false;

			if (debug) {
				System.out.println("Step: " + i);
				System.out.println("Disp: " + dispForward + ", " + dispSideways);
				System.out.println("Vel: (" + velocityForward + ", " + velocitySideways + ") (" + currentVelocity + "; " + Math.toDegrees(Math.PI / 2 - velocityAngles[i]) + ")");
				System.out.println("Hold: " + Math.toDegrees(Math.PI / 2 - holdingAngles[i]));
				System.out.println("Rotation: " + Math.toDegrees(Math.PI / 2 - rotationAngles[i]));
			}
		}

		rotationalSpeed = 0;

		//now for the hook
		//2 extra frames for spin and spinjump, take out one if just spin
		for (; i < frames - 2; i++) {
			currentVelocity -= WALKING_DECEL;
			rotationalSpeed += rotationalAccel;
			if (rotationalSpeed > maxRotationalSpeed) {
				rotationalSpeed = maxRotationalSpeed;
			}
			rotationAngles[i] = rotationAngles[i - 1] - rotationalSpeed;
			velocityAngles[i] = rotationAngles[i];
			holdingAngles[i] = finalAngleDiff - Math.PI / 2;
			velocityForward = currentVelocity * Math.cos(velocityAngles[i]);
			velocitySideways = currentVelocity * Math.sin(velocityAngles[i]);
			dispForward += velocityForward;
			dispSideways += velocitySideways;

			if (debug) {
				System.out.println("Step: " + i);
				System.out.println("Disp: " + dispForward + ", " + dispSideways);
				System.out.println("Vel: (" + velocityForward + ", " + velocitySideways + ") (" + currentVelocity + "; " + Math.toDegrees(Math.PI / 2 - velocityAngles[i]) + ")");
				System.out.println("Hold: " + Math.toDegrees(Math.PI / 2 - holdingAngles[i]));
				System.out.println("Rotation: " + Math.toDegrees(Math.PI / 2 - rotationAngles[i]));
			}
		}

		//frame of spinning
		currentVelocity *= .95;
		double velocitySide = .5;
		double velocityAhead = Math.sqrt(currentVelocity * currentVelocity - velocitySide * velocitySide);
		double velocityAngleAdjust = Math.atan2(velocitySide, velocityAhead);

		velocityAngles[i] = velocityAngles[i - 1] - velocityAngleAdjust;
		rotationAngles[i] = velocityAngles[i] - Math.PI / 2;
		holdingAngles[i] = velocityAngles[i];
		velocityForward = currentVelocity * Math.cos(velocityAngles[i]);
		velocitySideways = currentVelocity * Math.sin(velocityAngles[i]);
		dispForward += velocityForward;
		dispSideways += velocitySideways;

		if (debug) {
			System.out.println("Step: " + i);
			System.out.println("Disp: " + dispForward + ", " + dispSideways);
			System.out.println("Vel: (" + velocityForward + ", " + velocitySideways + ") (" + currentVelocity + "; " + Math.toDegrees(Math.PI / 2 - velocityAngles[i]) + ")");
			System.out.println("Hold: " + Math.toDegrees(Math.PI / 2 - holdingAngles[i]));
			System.out.println("Rotation: " + Math.toDegrees(Math.PI / 2 - rotationAngles[i]));
		}

		i++;

		//frame of spinjump
		velocitySide = .3;
		velocityAhead = 8;
		currentVelocity = Math.sqrt(velocityAhead * velocityAhead + velocitySide * velocitySide);
		velocityAngleAdjust = Math.atan2(velocitySide, velocityAhead);

		velocityAngles[i] = velocityAngles[i - 1] - velocityAngleAdjust;
		rotationAngles[i] = velocityAngles[i] - Math.PI / 2;
		holdingAngles[i] = velocityAngles[i];
		
		velocityForward = currentVelocity * Math.cos(velocityAngles[i]);
		velocitySideways = currentVelocity * Math.sin(velocityAngles[i]);
		dispForward += velocityForward;
		dispSideways += velocitySideways;

		if (debug) {
			System.out.println("Step: " + i);
			System.out.println("Disp: " + dispForward + ", " + dispSideways);
			System.out.println("Vel: (" + velocityForward + ", " + velocitySideways + ") (" + currentVelocity + "; " + Math.toDegrees(Math.PI / 2 - velocityAngles[i]) + ")");
			System.out.println("Hold: " + Math.toDegrees(Math.PI / 2 - holdingAngles[i]));
			System.out.println("Rotation: " + Math.toDegrees(Math.PI / 2 - rotationAngles[i]));
		}
		
		//idk if this part is right
		finalSpeed = currentVelocity;
		if (goRight) {
			finalAngle = initialAngle - velocityAngles[frames - 1];
		}
		else {
			finalAngle = initialAngle + velocityAngles[frames - 1];
		}

		/*
		for (int i = firstTurnFrame + 1; i < frames; i++) {
			currentVelocity -= WALKING_DECEL;
			if (rotationalSpeed < maxRotationalSpeed) {
				rotationalSpeed += rotationalAccel;
				if (rotationalSpeed > maxRotationalSpeed) {
					rotationalSpeed = maxRotationalSpeed;
				}
			}
			else if (i == frames - 1 && !spreadOutOvershoot) {
				velocityAngles[i] = finalAngleDiff;
			}
			//TODO: also track the velocities so you know what stick angles to hold
			else {
				if (remainingOvershoot > 0 && spreadOutOvershoot) { //there could be non-max acceleration for 2 frames if the first isn't enough
					overshootSpreadFrames = Math.min(frames - i, (int) Math.ceil((maxRotationalSpeed - rotationalSpeed) / rotationalAccel));
					double rotationalSpeedReduction = Math.min(remainingOvershoot / overshootSpreadFrames, rotationalAccel); //get rid of some of the overshoot (if there are 4 frames and 5 degrees of overshoot, you can lose 5/4 of your angular currentVelocity to have none left)
					remainingOvershoot -= rotationalSpeedReduction * overshootSpreadFrames;
					rotationalSpeed -= rotationalSpeedReduction;
				}
				velocityAngles[i] = velocityAngles[i - 1] + rotationalSpeed;
			}
			//System.out.println("Step " + i + ": " + Math.toDegrees(rotationalSpeed) + ", " + Math.toDegrees(velocityAngles[i]));
			dispForward += currentVelocity * Math.cos(velocityAngles[i]);
			dispSideways += currentVelocity * Math.sin(velocityAngles[i]);
			//System.out.println("Current sideways disp: " + dispSideways);
			//System.out.println("Current angle: " + Math.toDegrees(velocityAngles[i]));
		}
			*/
	}

	private void printDebug() {

	}

	public void calcVelocityAngle(int i, double holdingAngle) {
		if (holdingAngle == NO_ANGLE) {
			return;
		}
		if (i == 0) {
			velocityAngles[0] = Math.min(holdingAngle, maxRotationalSpeed);
		}
		else { //rotate by the angle you're holding relative to the current currentVelocity, but limit to maxRotationalSpeed
			double relativeAngle = holdingAngle - velocityAngles[i - 1];
			if (relativeAngle < 0) {
				velocityAngles[i] = velocityAngles[i - 1] - Math.min(-relativeAngle, maxRotationalSpeed);
			}
			else {
				velocityAngles[i] = velocityAngles[i - 1] + Math.min(relativeAngle, maxRotationalSpeed);
			}
		}
	}

	public void calcDispCoords() {	
		dispX = dispForward * Math.cos(initialAngle) + dispSideways * Math.cos(normalAngle);
		dispZ = dispForward * Math.sin(initialAngle) + dispSideways * Math.sin(normalAngle);	
	}

	public double calcDispY() {
		if (frames > PRE_HOOK_FRAMES) {
			return -1.5 * PRE_HOOK_FRAMES + -7 * (frames - PRE_HOOK_FRAMES);
		}
		else {
			return -1.5 * frames;
		}
	}

	public double calcDispY(int frames) {
		double dispY = 0; //not the real one
		double yVelocity = -1.5;
		double gravity;
		if (Movement.onMoon)
			gravity = movement.moonGravity;
		else
			gravity = movement.gravity;
		for (int i = 0; i < frames; i++) {
			if (i < PRE_HOOK_FRAMES) {
				yVelocity = -1.5;
			}
			else if (i < movement.maxFrames) {
				yVelocity = -7;
			}
			else {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			dispY += yVelocity;
		}
		return dispY;
	}

	//doesn't use main frames int because it includes the fall too
	public int calcFrames(double maxDispY) {
		dispY = 0;
		double yVelocity = -1.5;
		double gravity;
		if (Movement.onMoon)
			gravity = movement.moonGravity;
		else
			gravity = movement.gravity;
		int frames = -1; //don't use the main one for this
		while (dispY >= maxDispY) {
			frames++;
			if (frames < PRE_HOOK_FRAMES) {
				yVelocity = -1.5;
			}
			else if (frames < movement.maxFrames) {
				yVelocity = -7;
			}
			else {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			dispY += yVelocity;
		}
			return Math.max(0, frames); //possibly return max of frames and minFrames + 1 instead
	}

	public double[][] calcFrameByFrame() {
		dispX = x0;
		dispY = y0;
		dispZ = z0;
		double currentVelocity = initialForwardVelocity;
		double xVelocity;
		double yVelocity = PRE_HOOK_Y_VEL;
		double zVelocity;
		double[][] info = new double[frames][8];
		for (int i = 0; i < frames; i++) {
			double currentVelocityAngle;
			double currentHoldingAngle;
			if (goRight) {
				currentVelocityAngle = initialAngle - velocityAngles[i];
				currentHoldingAngle = initialAngle - holdingAngles[i];
			}
			else {
				currentVelocityAngle = initialAngle + velocityAngles[i];
				currentHoldingAngle = initialAngle + holdingAngles[i];
			}
			if (holdingAngles[i] == NO_ANGLE) {
				currentHoldingAngle = NO_ANGLE;
			}
			//System.out.println(holdingAngles[i]);
			if (i >= PRE_HOOK_FRAMES) { //when the hook happens, change vertical currentVelocity and start reducing current currentVelocity
				yVelocity = POST_HOOK_Y_VEL;
				currentVelocity -= WALKING_DECEL;
			}
			xVelocity = currentVelocity * Math.cos(currentVelocityAngle);
			zVelocity = currentVelocity * Math.sin(currentVelocityAngle);
			dispX += xVelocity;
			dispY += yVelocity;
			dispZ += zVelocity;
			info[i][4] = yVelocity;
			info[i][0] = dispX;
			info[i][1] = dispY;
			info[i][2] = dispZ;
			info[i][3] = xVelocity;
			info[i][5] = zVelocity;
			info[i][6] = currentVelocity;
			info[i][7] = currentHoldingAngle;
		}	
		return info;
	}

	public double calcFinalVerticalVelocity() {
		if (frames <= 15) {
			return -1.5;
		}
		else {
			return -7;
		}
	}

	public void adjustInitialAngle(double amount) {
		initialAngle += amount;
		trueInitialAngle += amount;
	}
	
/*
	public double calcUnoptimalDispForward() {
		dispForward = 0;
		forwardVelocity = initialForwardVelocity;
		for (int i = 0; i < frames; i++)
			stepForward(i);
		finalSidewaysVelocity = forwardVelocity;
		
		return dispForward;
	}
	
	public void stepForward(int i) {

		if (holdingAngles[i] != NO_ANGLE)
			forwardVelocity += baseForwardAccel * Math.cos(holdingAngles[i]);
		if (forwardVelocity > forwardVelocityCap)
			forwardVelocity = forwardVelocityCap;
		dispForward += forwardVelocity;
	}
	
	public double calcFinalAngle() {
		
		if (rightVector)
			finalAngle = initialAngle - Math.atan(sidewaysVelocity / finalForwardVelocity);
		else
			finalAngle = initialAngle + Math.atan(sidewaysVelocity / finalForwardVelocity);
		return finalAngle;
		
	}
		*/

	/*
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
						if (holdingAngles[i] != NO_ANGLE)
							forwardVelocity += baseForwardAccel * Math.cos(holdingAngles[i]);
					}
					else
						forwardVelocity += baseForwardAccel;
					if (forwardVelocity > forwardVelocityCap)
						forwardVelocity = forwardVelocityCap;
				}
				if (sidewaysVelocity < forwardVelocityCap && i >= nonVectorFrames && holdingAngles[i] != NO_ANGLE) {
					sidewaysVelocity += baseSidewaysAccel * Math.sin(holdingAngles[i]);
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
			*/
}

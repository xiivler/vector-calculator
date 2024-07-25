package vectorCalc;

//motion for a roll cancel up to once Mario starts Falling instead
public class GroundedCapThrow extends SimpleMotion {

	//TODO: enfoce minframes
	
	double[] velocityAngles;
	double[] holdingAngles;

	double normalAngle;
	double finalAngleDiff;
	double trueInitialAngleDiff; //angle at which to start this motion relative to the stated initial angle; calculate as target - initial for goRight = true (a left vector once you begin falling)

	double trueInitialAngle;

	double capThrowAngle; //could just use holdingAngle for this
	double finalAngle; //how much we want to rotate by
	int postHookFrames;

	int turningFrames; //number of frames the player turns after the hook (up to all of them)
	double overshoot; //how much the angle overshoots on the last frame
	boolean spreadOutOvershoot = true; //whether to spread out the overshoot to get slightly more speed but force you to micromanage more
	boolean spreadOutOvershootExtreme = true; //whether to allow using SMALL_ROTATIONAL_ACCEL frames to be slightly more precise with small angles and eke out ~.1 more unit of distance

	public static final double WALKING_DECEL = 14.0 / 120.0 ;
	public static final int PRE_HOOK_FRAMES = 15;
	public static final double PRE_HOOK_Y_VEL = -1.5;
	public static final double POST_HOOK_Y_VEL = -7.0;
	public static final double CT_ROTATIONAL_VELOCITY = Math.toRadians(5);
	public static final double SMALL_ROTATIONAL_ACCEL = Math.toRadians(0.325);
	public static final int ROTATIONAL_ACCEL_FRAMES = 5;
	public static final double FRICTION_COEFFICIENT = Math.toRadians(.01);
	public static final double WALKING_SPEED = 14;

	double dispSideways;

	boolean goRight;
	
	public GroundedCapThrow(Movement movement, boolean goRight) {
		super(movement, movement.minFrames);
		this.velocityAngles = new double[frames];
		this.holdingAngles = new double[frames];
		postHookFrames = frames - PRE_HOOK_FRAMES;
		this.goRight = goRight;
		if (goRight)
			normalAngle = initialAngle - Math.PI / 2;
		else
			normalAngle = initialAngle + Math.PI / 2;
		this.trueInitialAngleDiff = 0;
	}
	
	public GroundedCapThrow(Movement movement, double initialAngle, double finalAngleDiff, boolean goRight) {
		super(movement, initialAngle, movement.minFrames);
		//this.finalAngle = finalAngle;
		this.finalAngleDiff = finalAngleDiff;
		//Debug.println("Frames: " + frames);
		this.velocityAngles = new double[frames];
		this.holdingAngles = new double[frames];
		postHookFrames = frames - PRE_HOOK_FRAMES;
		//messy right now because you should be able to tell from the initial and final angles
		this.goRight = goRight;
		if (goRight) {
			normalAngle = initialAngle - Math.PI / 2;
			finalAngle = initialAngle - finalAngleDiff; //TODO: reduce angle?
		}
		else {
			normalAngle = initialAngle + Math.PI / 2;
			finalAngle = initialAngle + finalAngleDiff;
		}
		this.trueInitialAngleDiff = 0;
	}

	//trueinitialdiff should be negative for an optimal vector
	public GroundedCapThrow(Movement movement, double initialAngle, double trueInitialAngleDiff, double finalAngleDiff, boolean goRight) {
		this(movement, initialAngle, finalAngleDiff, goRight);
		this.trueInitialAngleDiff = trueInitialAngleDiff;
		if (goRight) {
			trueInitialAngle = initialAngle - trueInitialAngleDiff;
		}
		else {
			trueInitialAngle = initialAngle + trueInitialAngleDiff;
		}
	}
	
	//does not account for starting with unoptimal forward velocity and then accelerating
	public void calcDisp() {
		this.velocityAngles = new double[frames];
		this.holdingAngles = new double[frames];

		dispForward = 0;
		dispSideways = 0;
		
		double angleChange = 0;
		double rotationalSpeed = 0;
		turningFrames = 0;

		double friction = 0;
		int framesAtMaxRotationalSpeed = 0;

		//see how much we can rotate during the hook
		while (turningFrames < postHookFrames && angleChange < finalAngleDiff) {
			if (rotationalSpeed < maxRotationalSpeed) {
				rotationalSpeed += rotationalAccel;
			}
			if (rotationalSpeed == maxRotationalSpeed) {
				framesAtMaxRotationalSpeed++;
				friction = FRICTION_COEFFICIENT * framesAtMaxRotationalSpeed * framesAtMaxRotationalSpeed;
			}
			angleChange += rotationalSpeed - friction;
			turningFrames++;
		}
		//if we can't turn Mario enough after the hook, get the rest of the angle change by throwing cappy at more of an angle
		if (turningFrames == postHookFrames && angleChange < finalAngleDiff) {
			capThrowAngle = finalAngleDiff - angleChange; 
		}
		else {
			capThrowAngle = 0;
			overshoot = angleChange - finalAngleDiff;
			if (Math.abs(overshoot - rotationalSpeed) <= Math.toRadians(.01) && turningFrames > 1) { //ignore really small overshoots
				overshoot = 0;
				turningFrames--;
			}
			else {
				overshoot += friction; //we're going to be cutting out at least the last frame of friction
				//Debug.println("Friction: " + Math.toDegrees(friction));
			}
		}

		//now that we know the capThrowAngle we can calculate the first frame
		velocityAngles[0] = Math.min(capThrowAngle, CT_ROTATIONAL_VELOCITY) + trueInitialAngleDiff; //rotate up to 5 degrees
		if (capThrowAngle == 0) {
			holdingAngles[0] = NO_ANGLE; //don't need to hold anything if cap throwing straight ahead
			//holdingAngles[1] = NO_ANGLE;
		}
		else {
			holdingAngles[0] = capThrowAngle + trueInitialAngleDiff;
			//holdingAngles[1] = 0; //make it so you're going straight again
		}
		dispForward += initialForwardVelocity * Math.cos(velocityAngles[0]);
		dispSideways += initialForwardVelocity * Math.sin(velocityAngles[0]);

		//for frames 1 to 14 only need to hold until your velocity angle is 0
		//dispForward += initialForwardVelocity * (PRE_HOOK_FRAMES - 1);
		for (int i = 1; i < PRE_HOOK_FRAMES; i++) { //i = 1 should have holding angle of 0 to get back to straight
			if (velocityAngles[i - 1] != 0) {
				holdingAngles[i] = 0;
				if (velocityAngles[i - 1] > CT_ROTATIONAL_VELOCITY) {
					velocityAngles[i] = velocityAngles[i - 1] - CT_ROTATIONAL_VELOCITY;
				}
				else if (velocityAngles[i - 1] < 0 - CT_ROTATIONAL_VELOCITY) {
					velocityAngles[i] = velocityAngles[i - 1] + CT_ROTATIONAL_VELOCITY;
				}
				else {
					velocityAngles[i] = 0;
				}
				dispForward += initialForwardVelocity * Math.cos(velocityAngles[i]);
				dispSideways += initialForwardVelocity * Math.sin(velocityAngles[i]);
			}
			else {
				holdingAngles[i] = NO_ANGLE;
				velocityAngles[i] = 0;
				dispForward += initialForwardVelocity;
			}
		}

		//for frames after the hook but before we turn, angle is also 0, but we begin decelerating
		double currentVelocity = initialForwardVelocity;
		
		for (int i = 0; i < postHookFrames - turningFrames; i++) {
			currentVelocity -= WALKING_DECEL;
			if (currentVelocity < WALKING_SPEED) {
				currentVelocity = WALKING_SPEED;
			}
			dispForward += currentVelocity;
		}

		//now we rotate for the rest of the hook
		double remainingOvershoot = overshoot; //we want to spread it out
		int overshootSpreadFrames = Math.min(turningFrames, ROTATIONAL_ACCEL_FRAMES); //how many frames we can affect
		
		//TODO: potentially calculate stick angle here
		
		//first frame of turn
		int firstTurnFrame = frames - turningFrames;
		if (turningFrames > 0) {
			currentVelocity -= WALKING_DECEL;
			if (currentVelocity < WALKING_SPEED) {
				currentVelocity = WALKING_SPEED;
			}
			//use small rotational accel if there's a big enough overshoot; this only gives you like .1 units so maybe just disable this logic
			double potentialOvershootReduction = (rotationalAccel - SMALL_ROTATIONAL_ACCEL) * overshootSpreadFrames;
			if (remainingOvershoot >= potentialOvershootReduction && spreadOutOvershoot && spreadOutOvershootExtreme) {
				rotationalSpeed = SMALL_ROTATIONAL_ACCEL;
				remainingOvershoot -= potentialOvershootReduction;
				holdingAngles[firstTurnFrame] = capThrowAngle + Math.PI / 6; //anything <45 degrees from cap throw angle will do
			}
			else {
				rotationalSpeed = rotationalAccel;
				holdingAngles[firstTurnFrame] = capThrowAngle + NORMAL_ANGLE;
			}
			velocityAngles[firstTurnFrame] = capThrowAngle + rotationalSpeed; //we hook, and begin accelerating immediately
			//Debug.println("Step " + firstTurnFrame + ": " + Math.toDegrees(rotationalSpeed) + ", " + Math.toDegrees(velocityAngles[firstTurnFrame]));
			dispForward += currentVelocity * Math.cos(velocityAngles[firstTurnFrame]);
			dispSideways += currentVelocity * Math.sin(velocityAngles[firstTurnFrame]);

			//Debug.println(Math.toDegrees(remainingOvershoot));
			//remaining frames of turn
			boolean hasUndershot = false;
			framesAtMaxRotationalSpeed = 0;
			friction = FRICTION_COEFFICIENT;
			for (int i = firstTurnFrame + 1; i < frames; i++) {
				currentVelocity -= WALKING_DECEL;
				if (currentVelocity < WALKING_SPEED) {
					currentVelocity = WALKING_SPEED;
				}
				overshootSpreadFrames--;
				if (remainingOvershoot == 0 || rotationalSpeed == SMALL_ROTATIONAL_ACCEL || !spreadOutOvershoot) {
					if (rotationalSpeed == SMALL_ROTATIONAL_ACCEL) {
						holdingAngles[i] = capThrowAngle + NORMAL_ANGLE;
					}
					else {
						holdingAngles[i] = holdingAngles[i - 1];
					}
					rotationalSpeed += rotationalAccel;
				}
				else {
					int biggestFrictionFrame = turningFrames - ROTATIONAL_ACCEL_FRAMES;
					double biggestFriction = FRICTION_COEFFICIENT * biggestFrictionFrame * biggestFrictionFrame;
					if (remainingOvershoot != overshoot && remainingOvershoot > 3 * SMALL_ROTATIONAL_ACCEL && turningFrames > 5) {
						remainingOvershoot += biggestFriction;
					}
					//Debug.println(Math.toDegrees(remainingOvershoot));
					double overshootRotationalSpeedReduction = Math.min(remainingOvershoot / overshootSpreadFrames, rotationalAccel);
					//Debug.println(Math.toDegrees(overshootRotationalSpeedReduction));
					double oldRotationalSpeed = rotationalSpeed;
					rotationalSpeed += rotationalAccel - overshootRotationalSpeedReduction;
					//Debug.println(Math.toDegrees(rotationalSpeed));
					remainingOvershoot -= overshootRotationalSpeedReduction * overshootSpreadFrames;
					//if we accidentally took too much off of a frame that was 6.5 deg/fr before
					if (turningFrames > 5 && !hasUndershot) {
						//int biggestFrictionFrame = turningFrames - ROTATIONAL_ACCEL_FRAMES;
						//double biggestFriction = FRICTION_COEFFICIENT * biggestFrictionFrame * biggestFrictionFrame;
						double undershoot = Math.max(maxRotationalSpeed - (rotationalSpeed + rotationalAccel * overshootSpreadFrames), 0);
						//Debug.println("Undershoot: " + Math.toDegrees(undershoot));
						rotationalSpeed += undershoot / (overshootSpreadFrames + 1);
						hasUndershot = true;
					}
					double trueRotationalAccel = rotationalSpeed - oldRotationalSpeed;
					holdingAngles[i] = holdingAngles[i - 1] - (rotationalAccel - trueRotationalAccel); //counterrotate by how much less you want to accelerate
				}
				if (rotationalSpeed >= maxRotationalSpeed - friction) { //including friction
					rotationalSpeed = maxRotationalSpeed - friction;
					framesAtMaxRotationalSpeed++;
					friction = FRICTION_COEFFICIENT * (framesAtMaxRotationalSpeed + 1) * (framesAtMaxRotationalSpeed + 1);
				}
				if (i == frames - 1 && !spreadOutOvershoot) {
					rotationalSpeed -= overshoot;
					if (rotationalSpeed < SMALL_ROTATIONAL_ACCEL) {
						rotationalSpeed = SMALL_ROTATIONAL_ACCEL;
					}
					else if (rotationalSpeed < rotationalAccel) {
						rotationalSpeed = rotationalAccel;
					}
				}
				velocityAngles[i] = velocityAngles[i - 1] + rotationalSpeed;
				//Debug.println("Step " + i + ": " + Math.toDegrees(rotationalSpeed) + ", " + Math.toDegrees(velocityAngles[i]));
				dispForward += currentVelocity * Math.cos(velocityAngles[i]);
				dispSideways += currentVelocity * Math.sin(velocityAngles[i]);
			}
		}

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
			if (currentVelocity < WALKING_SPEED) {
				currentVelocity = WALKING_SPEED;
			}
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
					double rotationalSpeedReduction = Math.min(remainingOvershoot / overshootSpreadFrames, rotationalAccel); //get rid of some of the overshoot (if there are 4 frames and 5 degrees of overshoot, you can lose 5/4 of your angular velocity to have none left)
					remainingOvershoot -= rotationalSpeedReduction * overshootSpreadFrames;
					rotationalSpeed -= rotationalSpeedReduction;
				}
				velocityAngles[i] = velocityAngles[i - 1] + rotationalSpeed;
			}
			//Debug.println("Step " + i + ": " + Math.toDegrees(rotationalSpeed) + ", " + Math.toDegrees(velocityAngles[i]));
			dispForward += currentVelocity * Math.cos(velocityAngles[i]);
			dispSideways += currentVelocity * Math.sin(velocityAngles[i]);
			//Debug.println("Current sideways disp: " + dispSideways);
			//Debug.println("Current angle: " + Math.toDegrees(velocityAngles[i]));
		}
			*/
	}

	public void calcVelocityAngle(int i, double holdingAngle) {
		if (holdingAngle == NO_ANGLE) {
			return;
		}
		if (i == 0) {
			velocityAngles[0] = Math.min(holdingAngle, maxRotationalSpeed);
		}
		else { //rotate by the angle you're holding relative to the current velocity, but limit to maxRotationalSpeed
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
		dispZ = dispForward * Math.cos(initialAngle) + dispSideways * Math.cos(normalAngle);
		dispX = dispForward * Math.sin(initialAngle) + dispSideways * Math.sin(normalAngle);	
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
		dispZ = z0;
		dispY = y0;
		dispX = x0;
		double currentVelocity = initialForwardVelocity;
		double zVelocity;
		double yVelocity = PRE_HOOK_Y_VEL;
		double xVelocity;
		double[][] info = new double[frames][9];
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
			//Debug.println(holdingAngles[i]);
			if (i >= PRE_HOOK_FRAMES) { //when the hook happens, change vertical velocity and start reducing current velocity
				yVelocity = POST_HOOK_Y_VEL;
				currentVelocity -= WALKING_DECEL;
				if (currentVelocity < WALKING_SPEED) {
					currentVelocity = WALKING_SPEED;
				}
			}
			zVelocity = currentVelocity * Math.cos(currentVelocityAngle);
			xVelocity = currentVelocity * Math.sin(currentVelocityAngle);
			dispZ += zVelocity;
			dispY += yVelocity;
			dispX += xVelocity;
			info[i][4] = yVelocity;
			info[i][0] = dispZ;
			info[i][1] = dispY;
			info[i][2] = dispX;
			info[i][3] = zVelocity;
			info[i][5] = xVelocity;
			info[i][6] = currentVelocity;
			info[i][7] = currentHoldingAngle;
			if (currentHoldingAngle == NO_ANGLE) {
				info[i][8] = 0;
			}
			else {
				info[i][8] = 1;
			}
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
}

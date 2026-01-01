package com.vectorcalculator;

//calculates the behavior of movement without vectoring
public class SimpleMotion {
	
	Properties p = Properties.p;
	
	public static final double NORMAL_ANGLE = Math.PI / 2;
	public static final double BACK_ANGLE = Math.PI;
	public static final double NO_ANGLE = -Double.MAX_VALUE;
	
	double rotationalAccel;
	double maxRotationalSpeed;
	double rotationalSpeedAfterMax;
	
	Movement movement;
	
	double initialAngle;
	double holdingAngle = NO_ANGLE;
	
	double initialForwardVelocity;
	
	int frames;
	
	double dispForward;
	double dispZ;
	double dispY;
	double dispX;
	
	double finalForwardVelocity;
	double forwardVelocityCap;
	double finalVerticalVelocity;
	
	double defaultSpeedCap;
	double baseForwardAccel;
	double baseBackwardAccel;
	double forwardAccel;

	double yank = 0; //holding backward on final frame, the amount of speed decrease (can be up to baseBackwardAccel)
	
	double finalAngle;
	double finalSpeed;
	
	double initialRotation = 0;
	double finalRotation = 0;
	
	double z0 = 0;
	double y0 = 0;
	double x0 = 0;
	
	public SimpleMotion(Movement movement, int frames) {
		this.rotationalAccel = movement.rotationalAccel;
		this.maxRotationalSpeed = movement.maxRotationalSpeed;
		this.rotationalSpeedAfterMax = movement.rotationalSpeedAfterMax;
		this.movement = movement;
		this.initialForwardVelocity = movement.initialHorizontalSpeed;
		this.frames = frames;
		this.defaultSpeedCap = movement.defaultSpeedCap;
		this.baseForwardAccel = movement.forwardAccel;
		this.baseBackwardAccel = movement.backwardAccel;
		this.forwardAccel = baseForwardAccel;
		
		if (initialForwardVelocity > defaultSpeedCap)
			forwardVelocityCap = initialForwardVelocity;
		else
			forwardVelocityCap = defaultSpeedCap;
	}
	
	public SimpleMotion(Movement movement, double initialAngle, int frames) {
		this(movement, frames);
		this.initialAngle = initialAngle;	
	}
	
	public double calcDispForward() {
		
		if (initialForwardVelocity >= defaultSpeedCap)
			finalForwardVelocity = initialForwardVelocity;
		else
			finalForwardVelocity = Math.min(initialForwardVelocity + forwardAccel * frames, defaultSpeedCap);

		int framesToMaxSpeed = Math.max((int) ((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0);
		
		finalForwardVelocity -= yank;

		//if the movement is up to speed just multiply velocity by time
		if (initialForwardVelocity >= defaultSpeedCap)
			return initialForwardVelocity * frames - yank;
		
		//if the jump is not yet up to speed let it accelerate to the speed cap
		//note that the acceleration will already be applied in the first frame because it can be (hence initialForwardVelocity + forwardAccel)
		else {
			if (frames <= framesToMaxSpeed)
				return (2 * initialForwardVelocity + forwardAccel * (frames + 1)) / 2 * frames - yank;
			else
				return (2 * initialForwardVelocity + forwardAccel * (framesToMaxSpeed + 1)) / 2 * framesToMaxSpeed + finalForwardVelocity * (frames - framesToMaxSpeed) - yank;
		}
	}
		
	public void calcDisp() {
		dispForward = calcDispForward();
		
		finalSpeed = finalForwardVelocity;
		finalAngle = initialAngle;
	}
	
	public void calcDispCoords() {
		dispZ = dispForward * Math.cos(initialAngle);
		dispX = dispForward * Math.sin(initialAngle);
	}
	
	public double calcFinalAngle() {
		return finalAngle;
	}
	
	public double calcFinalSpeed() {
		return finalSpeed;
	}
	
	public void calcDispDispCoordsAngleSpeed() {
		calcDisp();
		calcDispCoords();
		calcFinalAngle();
		calcFinalSpeed();
	}
	
	public double calcFinalRotation() {
		finalRotation = initialRotation;
		return finalRotation;
	}
	
	public double calcFinalVerticalVelocity() {
		if (frames <= movement.frameOffset)
			finalVerticalVelocity = 0;
		else
			if (p.onMoon)
				finalVerticalVelocity = Math.max(movement.initialVerticalSpeed - movement.moonGravity * (frames - movement.frameOffset), movement.fallSpeedCap);
			else
				finalVerticalVelocity = Math.max(movement.initialVerticalSpeed - movement.gravity * (frames - movement.frameOffset), movement.fallSpeedCap);
		return finalVerticalVelocity;
	}

	public double calcDispY() {
		dispY = 0;
		double yVelocity = movement.initialVerticalSpeed;
		double gravity;
		if (p.onMoon)
			gravity = movement.moonGravity;
		else
			gravity = movement.gravity;
		for (int i = 0; i < frames; i++) {
			if (i >= movement.framesAtMaxVerticalSpeed + movement.frameOffset) {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			if (i >= movement.frameOffset) {
				dispY += yVelocity;
			}
		}
		finalVerticalVelocity = yVelocity;
		return dispY;
	}

	public double calcDispY(int frames) {
		this.frames = frames;
		return calcDispY();
	}

	//given a Y displacement, return the last frame that is at least as high as the displacement
	//if this is not possible, return the frames to the peak of the motion
	public int calcFrames(double maxDispY) {
		maxDispY -= 0.0001; //account for lack of double precision
		dispY = 0;
		double yVelocity = movement.initialVerticalSpeed;
		double gravity;
		if (p.onMoon)
			gravity = movement.moonGravity;
		else
			gravity = movement.gravity;
		frames = -1;
		int maxHeightFrames = 0;
		boolean isPossible = false;
		while (dispY >= maxDispY || yVelocity > 0) {
			frames++;
			if (dispY >= maxDispY) {
				isPossible = true;
			}
			if (frames >= movement.framesAtMaxVerticalSpeed + movement.frameOffset) {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			if (frames >= movement.frameOffset) {
				dispY += yVelocity;
				if (yVelocity > 0) {
					maxHeightFrames = frames;
				}
			}
		}
		if (!isPossible) {
			return maxHeightFrames;
		}
		else {
			return frames;
		}
	}
	
	//column 0-2: (X, Y, Z), column 3-5: (X-vel, Y-vel, Z-vel), column 6: horizontal speed, , column 7: holding radius, column 8: holding angle
	public double[][] calcFrameByFrame() {
		//maybe shouldn't use the disps for this
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
		double forwardVelocity = initialForwardVelocity;
		double zVelocity;
		double yVelocity = movement.initialVerticalSpeed;
		double xVelocity;
		double[][] info = new double[frames][9];
		for (int i = 0; i < frames; i++) {
			if (forwardVelocity < forwardVelocityCap || (movement.movementType.equals("Moonwalk") && i > 0) ){
				info[i][7] = initialAngle;
				info[i][8] = 1;
				forwardVelocity += forwardAccel;
				if (forwardVelocity > forwardVelocityCap)
					forwardVelocity = forwardVelocityCap;
			}
			//doesn't actually do anything for small angles
			/*
			else if (movement.movementType.equals("Dive")) { //sometimes the cap throw rotation doesn't work out quite right
				info[i][7] = initialAngle;
				info[i][8] = 1;
			}
				*/
			else {
				info[i][7] = NO_ANGLE;
				info[i][8] = 0;
			}
			if (i >= movement.framesAtMaxVerticalSpeed + movement.frameOffset) {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			zVelocity = forwardVelocity * cosInitialAngle;
			xVelocity = forwardVelocity * sinInitialAngle;
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
			info[i][6] = forwardVelocity;
		}	
		return info;
	}
	
	public void setInitialAngle(double angle) {
		initialAngle = angle;
	}
	
	public void setFrames(int n) {
		frames = n;
	}
	
	public void setInitialRotation(double rotation) {
		initialRotation = rotation;
	}
	
	public void setInitialCoordinates(double x, double y, double z) {
		x0 = x;
		y0 = y;
		z0 = z;
	}
	
	public void setYank(double yank) {
		this.yank = yank;
	}

	public void adjustInitialAngle(double amount) {
		initialAngle += amount;
	}

}

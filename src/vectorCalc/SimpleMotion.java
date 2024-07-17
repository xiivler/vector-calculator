package vectorCalc;

public class SimpleMotion {
	
	public static final double NORMAL_ANGLE = Math.PI / 2;
	public static final double NO_ANGLE = Double.MIN_VALUE;
	
	double rotationalAccel;
	double maxRotationalSpeed;
	double rotationalSpeedAfterMax;
	
	Movement movement;
	
	double initialAngle;
	double holdingAngle = NO_ANGLE;
	
	double initialForwardVelocity;
	
	int frames;
	
	double dispForward;
	double dispX;
	double dispY;
	double dispZ;
	
	double finalForwardVelocity;
	double forwardVelocityCap;
	double finalVerticalVelocity;
	
	double defaultSpeedCap;
	double baseForwardAccel;
	double forwardAccel;
	
	double finalAngle;
	double finalSpeed;
	
	double initialRotation = 0;
	double finalRotation = 0;
	
	double x0 = 0;
	double y0 = 0;
	double z0 = 0;
	
	public SimpleMotion(Movement movement, int frames) {
		this.rotationalAccel = movement.rotationalAccel;
		this.maxRotationalSpeed = movement.maxRotationalSpeed;
		this.rotationalSpeedAfterMax = movement.rotationalSpeedAfterMax;
		this.movement = movement;
		this.initialForwardVelocity = movement.initialHorizontalSpeed;
		this.frames = frames;
		this.defaultSpeedCap = movement.defaultSpeedCap;
		this.baseForwardAccel = movement.forwardAccel;
		this.forwardAccel = baseForwardAccel;
		
		if (initialForwardVelocity > defaultSpeedCap)
			forwardVelocityCap = initialForwardVelocity;
		else
			forwardVelocityCap = defaultSpeedCap;
	}
	
	public SimpleMotion(Movement movement, double initialAngle, int frames) {
		this(movement, frames);
		this.initialAngle = initialAngle;
		this.frames = frames;
		
	}
	
	public double calcDispForward() {
		
		if (initialForwardVelocity >= defaultSpeedCap)
			finalForwardVelocity = initialForwardVelocity;
		else
			finalForwardVelocity = Math.min(initialForwardVelocity + forwardAccel * frames, defaultSpeedCap);
		
		int framesToMaxSpeed = Math.max((int) ((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0);
		
		//if the movement is up to speed just multiply velocity by time
		if (initialForwardVelocity >= defaultSpeedCap)
			return initialForwardVelocity * frames;
		
		//if the jump is not yet up to speed let it accelerate to the speed cap
		//note that the acceleration will already be applied in the first frame because it can be (hence initialForwardVelocity + forwardAccel)
		else {
			if (frames <= framesToMaxSpeed)
				return (2 * initialForwardVelocity + forwardAccel * (frames + 1)) / 2 * frames;
			else
				return (2 * initialForwardVelocity + forwardAccel * (framesToMaxSpeed + 1)) / 2 * framesToMaxSpeed + finalForwardVelocity * (frames - framesToMaxSpeed);
		}
	}
		
	public void calcDisp() {
		dispForward = calcDispForward();
		
		finalSpeed = finalForwardVelocity;
		finalAngle = initialAngle;
	}
	
	public void calcDispCoords() {
		dispX = dispForward * Math.cos(initialAngle);
		dispZ = dispForward * Math.sin(initialAngle);
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
			if (Movement.onMoon)
				finalVerticalVelocity = Math.max(movement.initialVerticalSpeed - movement.moonGravity * (frames - movement.frameOffset), movement.fallSpeedCap);
			else
				finalVerticalVelocity = Math.max(movement.initialVerticalSpeed - movement.gravity * (frames - movement.frameOffset), movement.fallSpeedCap);
		return finalVerticalVelocity;
	}
	
	//column 0-2: (X, Y, Z), column 3-5: (X-vel, Y-vel, Z-vel), column 6: horizontal speed, column 7: holding angle
	public double[][] calcFrameByFrame() {
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
		double forwardVelocity = initialForwardVelocity;
		double xVelocity;
		double yVelocity = movement.initialVerticalSpeed;
		double zVelocity;
		double[][] info = new double[frames][8];
		for (int i = 0; i < frames; i++) {
			if (forwardVelocity < forwardVelocityCap) {
				info[i][7] = initialAngle;
				forwardVelocity += forwardAccel;
				if (forwardVelocity > forwardVelocityCap)
					forwardVelocity = forwardVelocityCap;
			}
			else
				info[i][7] = NO_ANGLE;
			if (i >= movement.framesAtMaxVerticalSpeed + movement.frameOffset) {
				yVelocity -= gravity;
				if (yVelocity < movement.fallSpeedCap)
					yVelocity = movement.fallSpeedCap;
			}
			xVelocity = forwardVelocity * cosInitialAngle;
			zVelocity = forwardVelocity * sinInitialAngle;
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
	
	public void adjustInitialAngle(double amount) {
		initialAngle += amount;
	}

}

package com.vectorcalculator;

import java.util.Arrays;

//calculates the motion for a vector where the joystick angles vary from frame to frame
public class ComplexVector extends SimpleVector {
	
	Properties p = Properties.p;
	
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
		// if (optimalForwardAccel)
		// 	vectorFrames = Math.max(frames - Math.max((int) Math.ceil((defaultSpeedCap - initialForwardVelocity) / forwardAccel), 0), 0);
		// else
		// 	vectorFrames = frames;
		
		dispSideways = 0;
		sidewaysVelocity = 0;
		//for (int i = frames - vectorFrames; i < frames; i++)
		for (int i = 0; i < frames; i++)
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
	
	public double calcDispForward() {
		dispForward = 0;
		forwardVelocity = initialForwardVelocity;
		for (int i = 0; i < frames; i++)
			stepForward(i);
		finalForwardVelocity = forwardVelocity;
		
		return dispForward;
	}
	
	public void stepForward(int i) {
		if (holdingAngles[i] != NO_ANGLE) {
			double accelValue;
			if (holdingAngles[i] <= NORMAL_ANGLE && holdingAngles[i] >= -NORMAL_ANGLE) {
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
		//System.out.println(movement.movementType + " " + frames);
		//System.out.println(Arrays.toString(holdingAngles));

		RotationStep rotationStep = new RotationStep(initialRotation, 0, NO_ANGLE, RotationDirection.NONE);
		
		for (int i = 0; i < frames; i++) {
			//System.out.println(i);
			RotationStep prevRotationStep = rotationStep;
			rotationStep = calcRotationStep(holdingAngles[i], prevRotationStep);
		}

		finalRotation = rotationStep.rotation;

		return finalRotation;
	}

	//calculate rotations relative to the initial velocity angle; if initialRotation is negative, that means it's to the left of the initial velocity if we're vectoring right or the opposite if we're vectoring left
	public double[] calcRelativeRotations() {
		double[] relativeRotations = new double[frames];

		RotationStep rotationStep = new RotationStep(initialRotation, 0, NO_ANGLE, RotationDirection.NONE);
			
		for (int i = 0; i < frames; i++) {
			RotationStep prevRotationStep = rotationStep;
			rotationStep = calcRotationStep(holdingAngles[i], prevRotationStep);
			relativeRotations[i] = rightVector ? (initialAngle - rotationStep.rotation) : (rotationStep.rotation - initialAngle);
		}

		return relativeRotations;
	}
	
	public double calcFinalSpeed() {
		finalSpeed = Math.sqrt(finalForwardVelocity * finalForwardVelocity + finalSidewaysVelocity * finalSidewaysVelocity);
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
			double xVelocity;
			double yVelocity = movement.initialVerticalSpeed;
			RotationStep rotationStep = new RotationStep(initialRotation, 0, NO_ANGLE, RotationDirection.NONE);
			//int nonVectorFrames = frames - vectorFrames;
			
			double[] holdingAnglesAdjusted = new double[frames];
			for (int i = 0; i < frames; i++)
				//if (i < nonVectorFrames)
				//	holdingAnglesAdjusted[i] = initialAngle;
				if (holdingAngles[i] == NO_ANGLE)
					holdingAnglesAdjusted[i] = NO_ANGLE;
				else if (rightVector)
					holdingAnglesAdjusted[i] = initialAngle - holdingAngles[i];
				else
					holdingAnglesAdjusted[i] = initialAngle + holdingAngles[i];
			
			double[][] info = new double[frames][10];
			for (int i = 0; i < frames; i++) {	
				//apply forward/backward accel
				if (holdingAngles[i] != NO_ANGLE) {
					double accelValue;
					if (holdingAngles[i] <= NORMAL_ANGLE && holdingAngles[i] >= -NORMAL_ANGLE) {
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
				//apply sideways accel
				if (holdingAngles[i] != NO_ANGLE) {
					if (holdingMinRadius[i]) {
						sidewaysVelocity += MIN_RADIUS * baseSidewaysAccel * Math.sin(holdingAngles[i]);
					}
					else {
						sidewaysVelocity += baseSidewaysAccel * Math.sin(holdingAngles[i]);
					}
					if (sidewaysVelocity > forwardVelocityCap)
						sidewaysVelocity = forwardVelocityCap;
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
				info[i][6] = Math.sqrt(zVelocity * zVelocity + xVelocity * xVelocity);
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
				RotationStep prevRotationStep = rotationStep;
				//System.out.println("Frame " + (i + 1));
				rotationStep = calcRotationStep(holdingAngles[i], prevRotationStep);
				info[i][9] = rotationStep.rotation;
			}	
			return info;
		}
	
	public void setHoldingAngles(double angles[]) {
		if (angles.length > 0)
			holdingAngle = angles[0];
		holdingAngles = angles;
		holdingMinRadius = new boolean[holdingAngles.length];
	}

	public void setHolding(double angles[], boolean radii[]) {
		if (angles.length > 0)
			holdingAngle = angles[0];
		holdingAngles = angles;
		holdingMinRadius = radii;
	}

	public double[] getCappyPosition(int throwType) {
		double throwAngle; //adjusted for the initial angle of the movement
		if (rightVector)
			throwAngle = initialAngle - holdingAngles[0];
		else
			throwAngle = initialAngle + holdingAngles[0];
		double throwNormalAngle = throwAngle - Math.PI / 2;
		int throwFrame = Movement.CT_FRAMES[throwType] - 1;
		// Debug.println("Throw Type" + throwType);
		// Debug.println(frames);

		//get position at frame of cap throw
		double dispX = x0;
		double dispY = y0;
		double dispZ = z0;
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
		double xVelocity;
		double yVelocity = movement.initialVerticalSpeed;
		//int nonVectorFrames = frames - vectorFrames;
		
		double[] holdingAnglesAdjusted = new double[frames];
		for (int i = 0; i <= throwFrame; i++)
			//if (i < nonVectorFrames)
			//	holdingAnglesAdjusted[i] = initialAngle;
			if (holdingAngles[i] == NO_ANGLE)
				holdingAnglesAdjusted[i] = NO_ANGLE;
			else if (rightVector)
				holdingAnglesAdjusted[i] = initialAngle - holdingAngles[i];
			else
				holdingAnglesAdjusted[i] = initialAngle + holdingAngles[i];
		
		for (int i = 0; i <= throwFrame; i++) {	
			//apply forward/backward accel
			//if (i >= nonVectorFrames) {
				if (holdingAngles[i] != NO_ANGLE) {
					double accelValue;
					if (holdingAngles[i] <= NORMAL_ANGLE && holdingAngles[i] >= -NORMAL_ANGLE) {
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
			//}
			//else
			//	forwardVelocity += baseForwardAccel;
			if (forwardVelocity > forwardVelocityCap)
				forwardVelocity = forwardVelocityCap;
			//apply sideways accel
			//if (i >= nonVectorFrames && holdingAngles[i] != NO_ANGLE) {
			if (holdingAngles[i] != NO_ANGLE) {
				if (holdingMinRadius[i]) {
					sidewaysVelocity += MIN_RADIUS * baseSidewaysAccel * Math.sin(holdingAngles[i]);
				}
				else {
					sidewaysVelocity += baseSidewaysAccel * Math.sin(holdingAngles[i]);
				}
				if (sidewaysVelocity > forwardVelocityCap)
					sidewaysVelocity = forwardVelocityCap;
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
			}
			dispX += xVelocity;
		}

		//simulate throwing cappy
		double cappyDispF = Movement.CT_DISPS[throwType][0];
		double cappyDispV = Movement.CT_DISPS[throwType][1];
		double cappyDispS = Movement.CT_DISPS[throwType][2];
		
		double[] cappyPos = new double[3];
		cappyPos[0] = dispX + cappyDispF * Math.sin(throwAngle) + cappyDispS * Math.sin(throwNormalAngle);
		cappyPos[1] = dispY + cappyDispV;
		cappyPos[2] = dispZ + cappyDispF * Math.cos(throwAngle) + cappyDispS * Math.cos(throwNormalAngle);
		Debug.printf("Mario Pos: %.3f %.3f %.3f\n", dispX, dispY, dispZ);
		Debug.printf("Cappy Pos: %.3f %.3f %.3f\n", cappyPos[0], cappyPos[1], cappyPos[2]);
		Debug.printf("Cappy Throw Angle: %.3f\n", Math.toDegrees(throwAngle));
		return cappyPos;
	}

	public void setHoldingAngle(double angle) {
		holdingAngle = angle;
		holdingAngles = new double[frames];
		for (int i = 0; i < frames; i++) {
			holdingAngles[i] = angle;
		}
		holdingMinRadius = new boolean[frames];
	}
}
